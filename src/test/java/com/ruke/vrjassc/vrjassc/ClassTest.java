package com.ruke.vrjassc.vrjassc;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ruke.vrjassc.vrjassc.exception.UndefinedMethodException;
import com.ruke.vrjassc.vrjassc.exception.UndefinedPropertyException;
import com.ruke.vrjassc.vrjassc.util.Compile;

public class ClassTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void defaultAllocation() {
		Compile compile = new Compile();
		String code = "struct Foo\n" + "endstruct";

		String result = "globals\n"
				+ "integer array struct_Foo_s__recycle\n"
				+ "endglobals\n"
				+ "function struct_s_Foo_allocate takes nothing returns integer\n"
				+ "local integer instance=struct_Foo_s__recycle[0]\n"
				+ "if (struct_Foo_s__recycle[0]==0) then\n"
				+ "set struct_Foo_s__recycle[0]=instance+1\n"
				+ "else\n"
				+ "set struct_Foo_s__recycle[0]=struct_Foo_s__recycle[instance]\n"
				+ "endif\n"
				+ "return instance\n"
				+ "endfunction\n"
				+ "function struct_Foo_deallocate takes integer this returns nothing\n"
				+ "set struct_Foo_s__recycle[this]=struct_Foo_s__recycle[0]\n"
				+ "set struct_Foo_s__recycle[0]=this\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void extendsArray() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n" + "endstruct";

		assertEquals("", compile.run(code));
	}

	@Test
	public void visibilityNoScope() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n"
				+ "method bar takes nothing returns nothing\n" + "endmethod\n"
				+ "private method nope takes nothing returns nothing\n"
				+ "call this.bar()\n" + "endmethod\n"
				+ "public method yep takes nothing returns nothing\n"
				+ "call this.nope()\n" + "endmethod\n" + "endstruct";

		String result = "function struct_Foo_bar takes integer this returns nothing\n\n"
				+ "endfunction\n"
				+ "function struct_Foo__nope takes integer this returns nothing\n"
				+ "call struct_Foo_bar(this)\n"
				+ "endfunction\n"
				+ "function struct_Foo_yep takes integer this returns nothing\n"
				+ "call struct_Foo__nope(this)\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void properties() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n" + "integer i\n"
				+ "method bar takes nothing returns integer\n"
				+ "return this.i\n" + "endmethod\n" + "endstruct";

		String result = "globals\n"
				+ "integer array struct_Foo_i\n"
				+ "endglobals\n"
				+ "function struct_Foo_bar takes integer this returns integer\n"
				+ "return struct_Foo_i[this]\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void undefinedProperty() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n"
				+ "method bar takes nothing returns nothing\n"
				+ "local integer i = this.i\n" + "endmethod\n" + "endstruct";

		expectedEx.expect(UndefinedPropertyException.class);
		expectedEx
				.expectMessage("3:23 Class <Foo> does not have a property called <i>");

		compile.run(code);
	}

	@Test
	public void undefinedMethod() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n"
				+ "method bar takes nothing returns nothing\n"
				+ "call this.nope()\n" + "endmethod\n" + "endstruct";

		expectedEx.expect(UndefinedMethodException.class);
		expectedEx
				.expectMessage("3:10 Class <Foo> does not have a method called <nope>");

		compile.run(code);
	}

	@Test
	public void accessMemberThroughVariables() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n" + "integer i\n"
				+ "private method nope takes nothing returns nothing\n"
				+ "endmethod\n" + "method bar takes nothing returns integer\n"
				+ "local Foo f=this\n" + "local integer a=f.i\n"
				+ "call f.nope()\n" + "return a\n" + "endmethod\n"
				+ "endstruct";

		String result = "globals\n"
				+ "integer array struct_Foo_i\n"
				+ "endglobals\n"
				+ "function struct_Foo__nope takes integer this returns nothing\n\n"
				+ "endfunction\n"
				+ "function struct_Foo_bar takes integer this returns integer\n"
				+ "local integer f=this\n"
				+ "local integer a=struct_Foo_i[f]\n"
				+ "call struct_Foo__nope(f)\n" + "return a\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void accessMemberThroughMethods() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n" + "integer i\n"
				+ "private method nope takes nothing returns Foo\n"
				+ "return this\n" + "endmethod\n"
				+ "method bar takes nothing returns integer\n"
				+ "local Foo f=this.nope()\n" + "local integer a=f.i\n"
				+ "call f.nope()\n" + "return a\n" + "endmethod\n"
				+ "endstruct";

		String result = "globals\n"
				+ "integer array struct_Foo_i\n"
				+ "endglobals\n"
				+ "function struct_Foo__nope takes integer this returns integer\n"
				+ "return this\n"
				+ "endfunction\n"
				+ "function struct_Foo_bar takes integer this returns integer\n"
				+ "local integer f=struct_Foo__nope(this)\n"
				+ "local integer a=struct_Foo_i[f]\n"
				+ "call struct_Foo__nope(f)\n" + "return a\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void staticMethod() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n"
				+ "private static method nope takes integer a returns nothing\n"
				+ "endmethod\n"
				+ "static method bar takes nothing returns nothing\n"
				+ "call Foo.nope(1)\n" + "call Foo.bar()\n" + "endmethod\n"
				+ "endstruct";

		String result = "function struct_Foo__nope takes integer a returns nothing\n\n"
				+ "endfunction\n"
				+ "function struct_Foo_bar takes nothing returns nothing\n"
				+ "call struct_Foo__nope(1)\n"
				+ "call struct_Foo_bar()\n"
				+ "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void staticProperty() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n"
				+ "public static integer i = 3\n" + "endstruct\n"
				+ "function bar takes nothing returns integer\n"
				+ "return Foo.i\n" + "endfunction";

		String result = "globals\n" + "integer struct_Foo_i=3\n"
				+ "endglobals\n"
				+ "function bar takes nothing returns integer\n"
				+ "return struct_Foo_i\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void creatingInstance() {
		Compile compile = new Compile();
		String code = "struct Foo extends array\n"
				+ "public static Foo instances\n" + "public integer i\n"
				+ "public static method create takes nothing returns Foo\n"
				+ "return Foo.instances\n" + "endmethod\n" + "endstruct\n"
				+ "function bar takes nothing returns integer\n"
				+ "local Foo instance = Foo.create()\n" + "set instance.i=3\n"
				+ "return instance.i\n" + "endfunction";

		String result = "globals\n" + "integer struct_Foo_instances\n"
				+ "integer array struct_Foo_i\n" + "endglobals\n"
				+ "function struct_Foo_create takes nothing returns integer\n"
				+ "return struct_Foo_instances\n" + "endfunction\n"
				+ "function bar takes nothing returns integer\n"
				+ "local integer instance=struct_Foo_create()\n"
				+ "set struct_Foo_i[instance]=3\n"
				+ "return struct_Foo_i[instance]\n" + "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void classExtend() {
		Compile compile = new Compile();
		String code = "struct Person extends array\n" + "private string name\n"
				+ "public method getName takes nothing returns string\n"
				+ "return this.name\n" + "endmethod\n" + "endstruct\n"
				+ "struct Ruke extends Person\n" + "private string lastname\n"
				+ "public method getLastName takes nothing returns string\n"
				+ "return this.lastname\n" + "endmethod\n"
				+ "public method getFullName takes nothing returns string\n"
				+ "return this.getLastName() + \" \" + this.getName()\n"
				+ "endmethod\n" + "endstruct";

		String result = "globals\n"
				+ "string array struct_Person_name\n"
				+ "string array struct_Ruke_lastname\n"
				+ "endglobals\n"
				+ "function struct_Person_getName takes integer this returns string\n"
				+ "return struct_Person_name[this]\n"
				+ "endfunction\n"
				+ "function struct_Ruke_getLastName takes integer this returns string\n"
				+ "return struct_Ruke_lastname[this]\n"
				+ "endfunction\n"
				+ "function struct_Ruke_getFullName takes integer this returns string\n"
				+ "return struct_Ruke_getLastName(this)+\" \"+struct_Person_getName(this)\n"
				+ "endfunction";

		assertEquals(result, compile.run(code));
	}

}