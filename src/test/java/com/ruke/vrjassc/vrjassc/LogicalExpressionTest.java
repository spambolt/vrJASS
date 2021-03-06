package com.ruke.vrjassc.vrjassc;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ruke.vrjassc.vrjassc.exception.LogicalException;
import com.ruke.vrjassc.vrjassc.util.Compile;

public class LogicalExpressionTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void correct() {
		Compile compile = new Compile();
		String code = "function foo takes nothing returns nothing" + System.lineSeparator()
				+ "local boolean bar" + System.lineSeparator()
				+ "set bar=bar or bar" + System.lineSeparator()
				+ "set bar=bar and bar" + System.lineSeparator()
				+ "set bar=bar or (1==1 and 1<2) and 5<=2 or 1!=2" + System.lineSeparator()
				+ "endfunction";

		assertEquals(code, compile.run(code));
	}

	@Test
	public void incorrectOr() {
		Compile compile = new Compile();
		String code = "function foo takes nothing returns nothing\n"
				+ "local boolean bar\n" + "set bar=bar or \"nope\"\n"
				+ "endfunction";

		expectedEx.expect(LogicalException.class);
		expectedEx
				.expectMessage("3:15 Logical operator or/and can only be used with booleans");

		compile.run(code);
	}

	@Test
	public void incorrectAnd() {
		Compile compile = new Compile();
		String code = "function foo takes nothing returns nothing\n"
				+ "local boolean bar\n" + "set bar=bar and \"nope\"\n"
				+ "endfunction";

		expectedEx.expect(LogicalException.class);
		expectedEx
				.expectMessage("3:16 Logical operator or/and can only be used with booleans");

		compile.run(code);
	}

}
