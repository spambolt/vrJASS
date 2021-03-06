package com.ruke.vrjassc.vrjassc.symbol;

import org.antlr.v4.runtime.Token;

public class PropertySymbol extends VariableSymbol implements ClassMemberSymbol {

	protected boolean _static;

	public PropertySymbol(String name, String type, boolean isStatic,
			boolean isArray, Visibility visibility, Symbol parent, Token token) {
		super(name, type, false, isArray, false, visibility, parent, token);
		this._static = isStatic;
		
		if (visibility == null) {
			this.visibility = Visibility.PUBLIC;
		}
	}

	@Override
	public String getFullName() {
		if (this.isStatic()) {
			return super.getFullName().replaceFirst("struct", "struct_s");
		}

		return super.getFullName();
	}

	@Override
	public boolean isStatic() {
		return this._static;
	}
	
	@Override
	public boolean isAbstract() {
		return false;
	}

}
