package com.ruke.vrjassc.vrjassc.exception;

import org.antlr.v4.runtime.Token;

public abstract class CompileException extends RuntimeException {

	protected int line;
	protected int charPos;

	public CompileException(Token token) {
		if (token != null) {
			this.line = token.getLine();
			this.charPos = token.getCharPositionInLine();
		}
	}

	public int getLine() {
		return this.line;
	}

	public int getCharPos() {
		return this.charPos;
	}

	@Override
	public String getMessage() {
		return String.format("%d:%d", this.line, this.charPos);
	}

}
