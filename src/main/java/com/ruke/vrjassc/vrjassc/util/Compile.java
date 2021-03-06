package com.ruke.vrjassc.vrjassc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import com.ruke.vrjassc.vrjassc.antlr4.vrjassLexer;
import com.ruke.vrjassc.vrjassc.antlr4.vrjassParser;
import com.ruke.vrjassc.vrjassc.exception.CompileException;
import com.ruke.vrjassc.vrjassc.visitor.MainVisitor;
import com.ruke.vrjassc.vrjassc.visitor.SymbolVisitor;

public class Compile {

	protected String commonPath;
	protected String blizzardPath;

	public Compile setCommonPath(String path) {
		this.commonPath = path;
		return this;
	}

	public Compile setBlizzardPath(String path) {
		this.blizzardPath = path;
		return this;
	}

	public String run(String code) throws CompileException {
		Preprocessor preprocessor;
		TextMacro textMacro;
		ModulePreprocessor module;
		
		ANTLRInputStream is = null;
		vrjassLexer lexer = null;
		TokenStream token = null;
		vrjassParser parser = null;
		SymbolVisitor symbolVisitor = null;
		MainVisitor mainVisitor = null;

		code = code.replace("\t", "    ");
		
		textMacro = new TextMacro(code);
		code = textMacro.getOutput();
		
		module = new ModulePreprocessor(code);
		code = module.getOutput();

		preprocessor = new Preprocessor(code);
		preprocessor.add(new ClassPreprocessor());
		preprocessor.run();
		
		code = preprocessor.getOutput();
		
		if (this.commonPath != null && this.blizzardPath != null) {
			Path commonj = Paths.get(this.commonPath);
			String commonjCode = null;

			try {
				commonjCode = String.join(System.lineSeparator(), Files.readAllLines(commonj));
			} catch (IOException e) {
				new ErrorWindow("common.j file was not found", "", 0);
				e.printStackTrace();
			}

			Path blizzardj = Paths.get(this.blizzardPath);
			String blizzardjCode = null;

			try {
				blizzardjCode = String.join(System.lineSeparator(), Files.readAllLines(blizzardj));
			} catch (IOException e) {
				new ErrorWindow("blizzard.j file was not found", "", 0);
				e.printStackTrace();
			}

			String commonBlizzardCode = commonjCode + System.lineSeparator() + blizzardjCode;

			is = new ANTLRInputStream(commonBlizzardCode);
			lexer = new vrjassLexer(is);
			token = new CommonTokenStream(lexer);
			parser = new vrjassParser(token);
			mainVisitor = new MainVisitor(parser);

			symbolVisitor = mainVisitor.getSymbolVisitor();
		} else {
			symbolVisitor = new SymbolVisitor();
		}

		is = new ANTLRInputStream(code);
		lexer = new vrjassLexer(is);
		token = new CommonTokenStream(lexer);
		parser = new vrjassParser(token);
		mainVisitor = new MainVisitor(parser, symbolVisitor);

		return mainVisitor.getOutput();
	}

	public String runFromFile(String pathname) throws CompileException,
			IOException {
		File file = new File(pathname);
		Reader reader = new FileReader(file);
		BufferedReader buf = new BufferedReader(reader);
		Stack<String> lines = new Stack<String>();

		while (lines.push(buf.readLine()) != null) {
		}

		lines.pop(); // last line is null

		reader.close();
		buf.close();

		return this.run(String.join(System.lineSeparator(), lines));
	}

}
