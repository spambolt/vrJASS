package symbol;

public class MethodSymbol extends FunctionSymbol implements ClassMemberSymbol {

	protected boolean _static;
	
	public MethodSymbol(
			String name,
			String type,
			boolean isStatic,
			Visibility visibility,
			Symbol parent) {
		super(name, type, visibility, parent);
		this._static = isStatic;
	}

	@Override
	public boolean isStatic() {
		return this._static;
	}

}
