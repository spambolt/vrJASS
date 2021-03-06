package com.ruke.vrjassc.vrjassc;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ruke.vrjassc.vrjassc.util.Compile;

public class LibraryTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void correct() {
		Compile compile = new Compile();
		String code = "library foo\n"
				+ "function bar takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "private function nope takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "public function yep takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "endlibrary";

		String result = "function bar takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction" + System.lineSeparator()
				+ "function foo__nope takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction" + System.lineSeparator()
				+ "function foo_yep takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void noCollisionPublic() {
		Compile compile = new Compile();
		String code = "library foo\n"
				+ "public function yep takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "endlibrary\n"
				+ "library bar\n"
				+ "public function yep takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "endlibrary";

		String result = "function foo_yep takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction" + System.lineSeparator()
				+ "function bar_yep takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void noCollisionPrivate() {
		Compile compile = new Compile();
		String code = "library foo\n"
				+ "private function yep takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "endlibrary\n"
				+ "library bar\n"
				+ "private function yep takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "endlibrary";

		String result = "function foo__yep takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction" + System.lineSeparator()
				+ "function bar__yep takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction";

		assertEquals(result, compile.run(code));
	}

	@Test
	public void requires() {
		Compile compile = new Compile();
		String code = "library foo requires bar, other\n" + "endlibrary\n"
				+ "library bar\n" + "endlibrary\n" + "library other\n"
				+ "endlibrary";

		assertEquals("", compile.run(code));
	}

	@Test
	public void functionSort() {
		Compile compile = new Compile();
		String code = "library lorem requires foo\n"
				+ "public function ipsum takes nothing returns nothing\n"
				+ "call foo_bar()\n"
				+ "endfunction\n"
				+ "endlibrary\n"
				+ "library foo\n"
				+ "public function bar takes nothing returns nothing\n"
				+ "endfunction\n"
				+ "endlibrary";

		String result = "function foo_bar takes nothing returns nothing"
				+ System.lineSeparator()
				+ System.lineSeparator()
				+ "endfunction" + System.lineSeparator()
				+ "function lorem_ipsum takes nothing returns nothing" + System.lineSeparator()
				+ "call foo_bar()" + System.lineSeparator()
				+ "endfunction";

		assertEquals(result, compile.run(code));
	}

}
