package visitor;

import java.util.Stack;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import exception.ElementNoAccessException;
import exception.EqualNotEqualComparisonException;
import exception.IncorrectReturnTypeFunctionException;
import exception.InitializeArrayVariableException;
import exception.InvalidArrayVariableIndexException;
import exception.LessGreaterComparisonException;
import exception.LogicalException;
import exception.MathematicalExpressionException;
import exception.NoReturnFunctionException;
import exception.IncorrectArgumentTypeFunctionCallException;
import exception.IncorrectVariableTypeException;
import exception.NoScopeVisibilityException;
import exception.TooFewArgumentsFunctionCallException;
import exception.TooManyArgumentsFunctionCallException;
import exception.UndefinedFunctionException;
import exception.UndefinedVariableException;
import exception.VariableIsNotArrayException;
import symbol.FunctionSymbol;
import symbol.VariableSymbol;
import symbol.Visibility;
import util.Prefix;
import util.VariableTypeDetector;
import antlr4.vrjassBaseVisitor;
import antlr4.vrjassParser;
import antlr4.vrjassParser.AltInitContext;
import antlr4.vrjassParser.ArgumentContext;
import antlr4.vrjassParser.ArgumentsContext;
import antlr4.vrjassParser.ComparisonContext;
import antlr4.vrjassParser.DivContext;
import antlr4.vrjassParser.FunctionDefinitionContext;
import antlr4.vrjassParser.FunctionExpressionContext;
import antlr4.vrjassParser.FunctionStatementContext;
import antlr4.vrjassParser.GlobalVariableStatementContext;
import antlr4.vrjassParser.GlobalsContext;
import antlr4.vrjassParser.InitContext;
import antlr4.vrjassParser.IntegerContext;
import antlr4.vrjassParser.LibraryDefinitionContext;
import antlr4.vrjassParser.LibraryStatementsContext;
import antlr4.vrjassParser.LocalVariableStatementContext;
import antlr4.vrjassParser.LogicalContext;
import antlr4.vrjassParser.MinusContext;
import antlr4.vrjassParser.MultContext;
import antlr4.vrjassParser.ParameterContext;
import antlr4.vrjassParser.ParametersContext;
import antlr4.vrjassParser.ParenthesisContext;
import antlr4.vrjassParser.PlusContext;
import antlr4.vrjassParser.ReturnStatementContext;
import antlr4.vrjassParser.ReturnTypeContext;
import antlr4.vrjassParser.SetVariableStatementContext;
import antlr4.vrjassParser.StatementContext;
import antlr4.vrjassParser.StatementsContext;
import antlr4.vrjassParser.StringContext;
import antlr4.vrjassParser.VariableContext;
import antlr4.vrjassParser.VariableTypeContext;

public class MainVisitor extends vrjassBaseVisitor<String> {

	protected Prefix prefixer;
	
	protected FunctionFinder functionFinder;
	
	protected VariableFinder variableFinder;
	
	protected String scopeName;
	
	protected Stack<String> requiredLibraries;
	
	protected FunctionSymbol function;
	
	protected boolean hasReturn;
	
	protected VariableSymbol variable;
	
	protected String expressionType;
	
	protected Stack<String> globalsBlock;
	protected Stack<String> functions;
	
	protected String output;
	
	public MainVisitor(vrjassParser parser) {
		this.prefixer = new Prefix();
		this.requiredLibraries = new Stack<String>();
		
		this.globalsBlock = new Stack<String>();
		this.functions = new Stack<String>();
		
		this.functionFinder = new FunctionFinder(this);
		this.variableFinder = new VariableFinder(this);
		
		this.functionFinder.visit(parser.init());
		parser.reset();
		
		this.variableFinder.visit(parser.init());
		parser.reset();
		
		this.output = this.visit(parser.init());
	}
	
	protected String getPrefixedName(String scopeName, String element, boolean _public) {
		if (scopeName != null) {
			if (_public) {
				return scopeName + "_" + element;
			} else {
				return scopeName + "__" + element;
			}
		}
		
		return element;
	}
	
	public String getPrefix(Token visibility, String scopeName) {
		return this.prefixer.get(visibility, scopeName);
	}
	
	public Visibility getVisibility(Token visibility) {
		if (visibility == null) {
			return Visibility.PUBLIC;
		}
		
		if (visibility.getText().equals("private")) {
			return Visibility.PRIVATE;
		}
		
		return Visibility.PUBLIC;
	}
	
	public boolean hasAccess(
			FunctionSymbol function,
			String callingScope,
			Stack<String> libraries) {
		if (function == null) {
			return false;
		}
		
		if (function.getVisibility() == Visibility.PRIVATE) {
			if (!function.getScopeName().equals(callingScope)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean hasAccessToVariable(
			VariableSymbol variable,
			String callingScope,
			Stack<String> libraries) {
		if (variable == null) {
			return false;
		}
		
		if (variable.getVisibility() == Visibility.PRIVATE) {
			if (!variable.getScopeName().equals(callingScope)) {
				return false;
			}
		}
		
		return true;
	}
		
	@Override
	public String visitVariable(VariableContext ctx) {
		String name = ctx.varName.getText();
		String indexType = (ctx.index != null) ? this.visit(ctx.index) : null;
		VariableSymbol variable = this.variableFinder.get(this.function, name, this.scopeName);
		
		if (variable == null) {
			throw new UndefinedVariableException(ctx.varName);
		}
		
		boolean access;
		
		if (variable.isGlobal()) {
			access = this.hasAccessToVariable(
				variable, this.scopeName, this.requiredLibraries
			);
		} else {
			access = this.hasAccessToVariable(
				variable, this.function.getName(), this.requiredLibraries
			);
		}
		
		if (!access) {
			throw new ElementNoAccessException(ctx.varName);
		}
		
		if (ctx.index != null && !"integer".equals(indexType)) {
			throw new InvalidArrayVariableIndexException(ctx.index.getStart());
		}
		
		this.expressionType = variable.getType();
		return variable.getName();
	}
		
	@Override
	public String visitVariableType(VariableTypeContext ctx) {
		String type = ctx.getText();
		
		if (VariableTypeDetector.isUserType(type)) {
			type = "integer";
		}
		
		return type;
	}
	
	@Override
	public String visitString(StringContext ctx) {
		this.expressionType = "string";
		return ctx.getText();
	}
	
	@Override
	public String visitInteger(IntegerContext ctx) {
		this.expressionType = "integer";
		return ctx.INT().getText();
	}
	
	@Override
	public String visitParenthesis(ParenthesisContext ctx) {
		return "(" + this.visit(ctx.expression()) + ")";
	}
	
	@Override
	public String visitDiv(DivContext ctx) {
		String left = this.visit(ctx.left);
		String leftType = this.expressionType;
		boolean leftIsNumeric = "real".equals(leftType) || "integer".equals(leftType);
		
		String right = this.visit(ctx.right);
		String rightType = this.expressionType;
		boolean rightIsNumeric = "real".equals(rightType) || "integer".equals(rightType);
		
		if (!leftIsNumeric) {
			throw new MathematicalExpressionException(ctx.left.getStart());
		}
		
		if (!rightIsNumeric) {
			throw new MathematicalExpressionException(ctx.right.getStart());
		}
		
		this.expressionType = "integer";
		return left + '/' + right;
	}
	
	@Override
	public String visitMult(MultContext ctx) {
		String left = this.visit(ctx.left);
		String leftType = this.expressionType;
		boolean leftIsNumeric = "real".equals(leftType) || "integer".equals(leftType);
		
		String right = this.visit(ctx.right);
		String rightType = this.expressionType;
		boolean rightIsNumeric = "real".equals(rightType) || "integer".equals(rightType);
		
		if (!leftIsNumeric) {
			throw new MathematicalExpressionException(ctx.left.getStart());
		}
		
		if (!rightIsNumeric) {
			throw new MathematicalExpressionException(ctx.right.getStart());
		}
		
		this.expressionType = "integer";
		return left + '*' + right;
	}
	
	@Override
	public String visitMinus(MinusContext ctx) {
		String left = this.visit(ctx.left);
		String leftType = this.expressionType;
		boolean leftIsNumeric = "real".equals(leftType) || "integer".equals(leftType);
		
		String right = this.visit(ctx.right);
		String rightType = this.expressionType;
		boolean rightIsNumeric = "real".equals(rightType) || "integer".equals(rightType);
		
		if (!leftIsNumeric) {
			throw new MathematicalExpressionException(ctx.left.getStart());
		}
		
		if (!rightIsNumeric) {
			throw new MathematicalExpressionException(ctx.right.getStart());
		}
		
		this.expressionType = "integer";
		return left + '-' + right;
	}
	
	@Override
	public String visitPlus(PlusContext ctx) {
		String left = this.visit(ctx.left);
		String leftType = this.expressionType;
		boolean leftIsNumeric = "real".equals(leftType) || "integer".equals(leftType);
		
		String right = this.visit(ctx.right);
		String rightType = this.expressionType;
		boolean rightIsNumeric = "real".equals(rightType) || "integer".equals(rightType);
		
		if (!leftIsNumeric) {
			throw new MathematicalExpressionException(ctx.left.getStart());
		}
		
		if (!rightIsNumeric) {
			throw new MathematicalExpressionException(ctx.right.getStart());
		}
		
		this.expressionType = "integer";
		return left + '+' + right;
	}
	
	@Override
	public String visitComparison(ComparisonContext ctx) {
		String left = this.visit(ctx.left);
		String leftType = this.expressionType;
		boolean leftIsNumeric = "real".equals(leftType) || "integer".equals(leftType);
		
		String right = this.visit(ctx.right);
		String rightType = this.expressionType;
		boolean rightIsNumeric = "real".equals(rightType) || "integer".equals(rightType);
		
		String operator = ctx.operator.getText();
		
		switch (operator) {
		case "==":
		case "!=":
			if (!leftType.equals(rightType)) {
				if (!leftIsNumeric || !rightIsNumeric) {
					throw new EqualNotEqualComparisonException(ctx.getStart());
				}
			}
			
			break;
		
		case "<":
		case ">":
		case "<=":
		case ">=":
			if (!leftIsNumeric) {
				throw new LessGreaterComparisonException(ctx.left.getStart());
			}
			
			if (!rightIsNumeric) {
				throw new LessGreaterComparisonException(ctx.right.getStart());
			}
			
			break;
		}
		
		this.expressionType = "boolean";
		return left + operator + right;
	}
	
	@Override
	public String visitLogical(LogicalContext ctx) {
		String left = this.visit(ctx.left);
		String leftType = this.expressionType;
		boolean leftIsBoolean = "boolean".equals(leftType);
		
		String right = this.visit(ctx.right);
		String rightType = this.expressionType;
		boolean rightIsBoolean = "boolean".equals(rightType);
		
		if (!leftIsBoolean) {
			throw new LogicalException(ctx.left.getStart());
		}
		
		if (!rightIsBoolean) {
			throw new LogicalException(ctx.right.getStart());
		}
		
		this.expressionType = "boolean";
		return left + " " + ctx.operator.getText() + " " + right;
	}
	
	@Override
	public String visitArgument(ArgumentContext ctx) {
		return this.visit(ctx.expression());
	}
	
	@Override
	public String visitArguments(ArgumentsContext ctx) {
		Stack<String> params = this.function.getParams();
		Stack<String> args = new Stack<String>();
		String prevExprType = this.expressionType;
		int i = 0;
		
		for (ArgumentContext arg : ctx.argument()) {
			args.push(this.visit(arg));
			
			if (!params.get(i).equals(this.expressionType)) {
				throw new IncorrectArgumentTypeFunctionCallException(
					arg.getStart(),
					params.get(i),
					this.expressionType
				);
			}
			
			this.expressionType = null;
		}
		
		this.expressionType = prevExprType;
		
		return String.join(",", args);
	}
	
	@Override
	public String visitReturnType(ReturnTypeContext ctx) {
		return ctx.getText();
	}
	
	@Override
	public String visitParameter(ParameterContext ctx) {
		return this.visit(ctx.variableType()) + ' ' + ctx.ID().getText();
	}
		
	@Override
	public String visitParameters(ParametersContext ctx) {
		Stack<String> params = new Stack<String>();
		
		if (ctx.parameter().size() == 0) {
			params.push("nothing");
		} else {
			for (ParameterContext param : ctx.parameter()) {
				params.push(this.visit(param));
			}
		}
		
		return String.join(",", params);
	}
	
	@Override
	public String visitFunctionExpression(FunctionExpressionContext ctx) {
		String name = ctx.functionName.getText();
		FunctionSymbol func = this.functionFinder.get(name, this.scopeName);
		
		if (func == null) {
			throw new UndefinedFunctionException(ctx.functionName);
		}
		
		int argumentsCount = ctx.arguments().argument().size();
		FunctionSymbol prevFunction = this.function;
		
		if (!this.hasAccess(func, this.scopeName, this.requiredLibraries)) {
			throw new ElementNoAccessException(ctx.functionName);
		}
		
		if (argumentsCount > func.getParams().size()) {
			throw new TooManyArgumentsFunctionCallException(ctx.functionName);
		}
		
		if (argumentsCount < func.getParams().size()) {
			throw new TooFewArgumentsFunctionCallException(ctx.functionName);
		}
		
		this.function = func;
		
		String result = func.getName() + "(" + this.visit(ctx.arguments()) + ")";
		this.expressionType = this.function.getReturnType();
		
		this.function = prevFunction;
		
		return result;
	}
	
	@Override
	public String visitFunctionStatement(FunctionStatementContext ctx) {
		return "call " + this.visit(ctx.functionExpression());
	}
	
	@Override
	public String visitReturnStatement(ReturnStatementContext ctx) {
		String result = "return " + this.visit(ctx.expression());
		
		if (!this.function.getReturnType().equals(this.expressionType)) {
			throw new IncorrectReturnTypeFunctionException(
				ctx.getStart(),
				this.function,
				this.expressionType
			);
		}
		
		this.hasReturn = true;
		
		return result;
	}
	
	@Override
	public String visitSetVariableStatement(SetVariableStatementContext ctx) {
		String variableName = ctx.varName.getText();
		String index = (ctx.index == null) ? "" : "[" + this.visit(ctx.index) + "]";
		String indexType = this.expressionType;
		String operator = ctx.operator.getText();
		VariableSymbol prevVar = this.variable;
		String result;
		
		this.variable = this.variableFinder.get(this.function, variableName, this.scopeName);
		
		if (this.variable == null) {
			throw new UndefinedVariableException(ctx.varName);
		}
		
		result = "set " + this.variable.getName() + index + "=";
		
		switch (operator) {
		case "/=":
		case "*=":
		case "-=":
		case "+=":
			result += this.variable.getName() + index + operator.replace("=", "");
		}
		
		result += this.visit(ctx.value);
		
		if (ctx.index != null && !this.variable.isArray()) {
			throw new VariableIsNotArrayException(ctx.varName);
		}
		
		if (ctx.index != null && !"integer".equals(indexType)) {
			throw new InvalidArrayVariableIndexException(ctx.index.getStart());
		}
		
		if (!this.variable.getType().equals(this.expressionType)) {
			throw new IncorrectVariableTypeException(
				ctx.varName,
				this.variable.getType(),
				this.expressionType
			);
		}
		
		this.variable = prevVar;
		
		return result;
	}
	
	@Override
	public String visitLocalVariableStatement(LocalVariableStatementContext ctx) {
		String variableName = ctx.varName.getText();
		String variableType = this.visit(ctx.variableType());
		String array = (ctx.array != null) ? " array" : "";
		String result = "local " + variableType + array + " " + variableName;
		
		if (ctx.value != null) {
			if (ctx.array != null) {
				throw new InitializeArrayVariableException(ctx.varName);
			}
			
			result += "=" + this.visit(ctx.value);
			
			if (!ctx.variableType().getText().equals(this.expressionType)) {
				throw new IncorrectVariableTypeException(
					ctx.varName,
					ctx.variableType().getText(),
					this.expressionType
				);
			}
		}
		
		return result;
	}
	
	@Override
	public String visitStatements(StatementsContext ctx) {
		Stack<String> variables = new Stack<String>();
		Stack<String> statements = new Stack<String>();
		Stack<String> result = new Stack<String>();
		String visited;
		
		for (StatementContext stat : ctx.statement()) {
			visited = this.visit(stat);
			
			if (visited == null) {
				continue;
			}
			
			if (stat.localVariableStatement() != null) {
				variables.push(visited);
			} else {
				statements.push(visited);
			}
		}
		
		result.addAll(variables);
		result.addAll(statements);
		
		return String.join(System.lineSeparator(), result);
	}
	
	@Override
	public String visitFunctionDefinition(FunctionDefinitionContext ctx) {
		String prefix = this.getPrefix(ctx.visibility, this.scopeName);
		String name = prefix + ctx.functionName.getText();
		String params = this.visit(ctx.parameters());
		String type = this.visit(ctx.returnType());
		FunctionSymbol prevFunc = this.function;
		boolean prevHasReturn = this.hasReturn;
		
		if (ctx.visibility != null && this.scopeName == null) {
			throw new NoScopeVisibilityException(ctx.functionName);
		}
		
		this.function = this.functionFinder.get(name, this.scopeName);
		this.hasReturn = false;
		
		String result =
				"function " + name +
				" takes " + params +
				" returns " + type + System.lineSeparator() +
				this.visit(ctx.statements()) + System.lineSeparator() +
				"endfunction";
		
		if (!type.equals("nothing")) {
			if (!this.hasReturn) {
				throw new NoReturnFunctionException(ctx.getStart(), this.function);
			}
		}
		
		this.hasReturn = prevHasReturn;
		this.function = prevFunc;
		
		this.functions.push(result);
		
		return result;
	}
	
	@Override
	public String visitGlobalVariableStatement(
			GlobalVariableStatementContext ctx) {
		String prefix = this.getPrefix(ctx.visibility, this.scopeName);
		String variableType = this.visit(ctx.variableType());
		String array = (ctx.array != null) ? " array" : "";
		String variableName = prefix + ctx.varName.getText();
		
		if (ctx.visibility != null && this.scopeName == null) {
			throw new NoScopeVisibilityException(ctx.varName);
		}
		
		String result = variableType + array + " " + variableName;
		
		if (ctx.value != null) {
			if (ctx.array != null) {
				throw new InitializeArrayVariableException(ctx.varName);
			}
			
			result += "=" + this.visit(ctx.value);
			
			if (!ctx.variableType().getText().equals(this.expressionType)) {
				throw new IncorrectVariableTypeException(
					ctx.varName,
					ctx.variableType().getText(),
					this.expressionType
				);
			}
		}
		
		return result;
	}
	
	@Override
	public String visitGlobals(GlobalsContext ctx) {
		String visited;
		
		for (GlobalVariableStatementContext global : ctx.globalVariableStatement()) {
			visited = this.visit(global);
			
			if (visited != null) {
				this.globalsBlock.push(visited);
			}
		}
		
		return null;
	}
	
	@Override
	public String visitLibraryDefinition(LibraryDefinitionContext ctx) {
		String prevScopeName = this.scopeName;
		Stack<String> prevRequiredLibraries = this.requiredLibraries;
		Stack<String> result = new Stack<String>();
		String visited;
		
		this.scopeName = ctx.libraryName.getText();
		this.requiredLibraries = new Stack<String>();
		
		if (ctx.requirements() != null) {
			for (TerminalNode req : ctx.requirements().ID()) {
				this.requiredLibraries.push(req.getText());
			}
		}
		
		for (LibraryStatementsContext library : ctx.libraryStatements()) {
			visited = this.visit(library);
			
			if (visited != null) {
				result.push(visited);
			}
		}
		
		this.scopeName = prevScopeName;
		this.requiredLibraries = prevRequiredLibraries;
		
		return String.join(System.lineSeparator(), result);
	}
	
	@Override
	public String visitInit(InitContext ctx) {
		Stack<String> result = new Stack<String>();
		
		for (AltInitContext alt : ctx.altInit()) {
			this.visit(alt);
		}
		
		if (this.globalsBlock.size() != 0) {
			result.push("globals");
			result.addAll(this.globalsBlock);
			result.push("endglobals");
		}
		
		result.addAll(this.functions);
		
		return String.join(System.lineSeparator(), result);
	}

	public String getOutput() {
		return this.output;
	}
	
}
