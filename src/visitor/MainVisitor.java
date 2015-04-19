package visitor;

import java.util.Stack;

import exception.IncorrectReturnTypeFunctionException;
import exception.NoReturnFunctionException;
import exception.IncorrectArgumentTypeFunctionCallException;
import exception.IncorrectVariableTypeException;
import exception.TooFewArgumentsFunctionCallException;
import exception.TooManyArgumentsFunctionCallException;
import exception.UndefinedFunctionException;
import exception.UndefinedVariableException;
import symbol.FunctionSymbol;
import symbol.VariableSymbol;
import util.VariableTypeDetector;
import antlr4.vrjassBaseVisitor;
import antlr4.vrjassParser;
import antlr4.vrjassParser.AltInitContext;
import antlr4.vrjassParser.ArgumentContext;
import antlr4.vrjassParser.ArgumentsContext;
import antlr4.vrjassParser.DivContext;
import antlr4.vrjassParser.FunctionDefinitionContext;
import antlr4.vrjassParser.FunctionExpressionContext;
import antlr4.vrjassParser.FunctionStatementContext;
import antlr4.vrjassParser.InitContext;
import antlr4.vrjassParser.IntegerContext;
import antlr4.vrjassParser.LocalVariableStatementContext;
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

	protected FunctionFinder functionFinder;
	
	protected VariableFinder variableFinder;
	
	protected FunctionSymbol function;
	
	protected boolean hasReturn;
	
	protected VariableSymbol variable;
	
	protected String expressionType;
	
	protected String output;
	
	public MainVisitor(vrjassParser parser) {
		this.functionFinder = new FunctionFinder(this);
		this.variableFinder = new VariableFinder(this);
		
		this.functionFinder.visit(parser.init());
		parser.reset();
		
		this.variableFinder.visit(parser.init());
		parser.reset();
		
		this.output = this.visit(parser.init());
	}
	
	@Override
	public String visitVariable(VariableContext ctx) {
		String funcName = null;
		
		if (this.function != null) {
			funcName = this.function.getName();
		}
		
		if (this.variableFinder.get(funcName, ctx.getText()) == null) {
			throw new UndefinedVariableException(ctx.getStart());
		}
		
		return ctx.getText();
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
		String right = this.visit(ctx.right);
		
		this.expressionType = "integer";
		return left + '/' + right;
	}
	
	@Override
	public String visitMult(MultContext ctx) {
		String left = this.visit(ctx.left);
		String right = this.visit(ctx.right);
		
		this.expressionType = "integer";
		return left + '*' + right;
	}
	
	@Override
	public String visitMinus(MinusContext ctx) {
		String left = this.visit(ctx.left);
		String right = this.visit(ctx.right);
		
		this.expressionType = "integer";
		return left + '-' + right;
	}
	
	@Override
	public String visitPlus(PlusContext ctx) {
		String left = this.visit(ctx.left);
		String right = this.visit(ctx.right);
		
		this.expressionType = "integer";
		return left + '+' + right;
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
		FunctionSymbol func = this.functionFinder.get(name);
		int argumentsCount = ctx.arguments().argument().size();
		FunctionSymbol prevFunction = this.function;
		
		if (func == null) {
			throw new UndefinedFunctionException(ctx.functionName);
		}
		
		if (argumentsCount > func.getParams().size()) {
			throw new TooManyArgumentsFunctionCallException(ctx.functionName);
		}
		
		if (argumentsCount < func.getParams().size()) {
			throw new TooFewArgumentsFunctionCallException(ctx.functionName);
		}
		
		this.function = func;
		
		String result = name + "(" + this.visit(ctx.arguments()) + ")";
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
		String variableName = this.visit(ctx.varName);
		VariableSymbol prevVar = this.variable;
		
		this.variable = this.variableFinder.get(this.function, variableName);
		String result = "set " + variableName + "=" + this.visit(ctx.value);
		
		if (!this.variable.getType().equals(this.expressionType)) {
			throw new IncorrectVariableTypeException(
				ctx.varName.getStart(),
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
		String result = "local " + variableType + " " + variableName;
		
		if (ctx.value != null) {
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
		Stack<String> statements = new Stack<String>();
		
		for (StatementContext stat : ctx.statement()) {
			statements.push(this.visit(stat));
			
			if (statements.lastElement() == null) {
				statements.pop();
			}
		}
		
		return String.join(System.lineSeparator(), statements);
	}
	
	@Override
	public String visitFunctionDefinition(FunctionDefinitionContext ctx) {
		String name = ctx.functionName.getText();
		String params = this.visit(ctx.parameters());
		String type = this.visit(ctx.returnType());
		FunctionSymbol prevFunc = this.function;
		boolean prevHasReturn = this.hasReturn;
		
		this.function = this.functionFinder.get(name);
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
		
		return result;
	}
	
	@Override
	public String visitInit(InitContext ctx) {
		Stack<String> result = new Stack<String>();
		
		for (AltInitContext alt : ctx.altInit()) {
			result.push(this.visit(alt));
			
			if (result.lastElement() == null) {
				result.pop();
			}
		}
		
		return String.join(System.lineSeparator(), result);
	}

	public String getOutput() {
		return this.output;
	}
	
}
