/*******************************************************************************
 * Copyright (c) 2019, 2020 Thomas Wolf and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.eclipse.jface.text.MultiStringMatcher;
import org.eclipse.jface.text.MultiStringMatcher.Match;

public class MultiStringMatcherTest {

	private static Match run(String text, String... needles) {
		return run(text, 0, needles);
	}

	private static Match run(String text, int offset, String... needles) {
		return run(new TestCharSequence(text), offset, needles);
	}

	private static Match run(TestCharSequence text, int offset, String... needles) {
		Match result = MultiStringMatcher.indexOf(text, offset, needles);
		assertEquals("Algorithm backtracked", 0, text.getBackTrack());
		return result;
	}

	private static void test(Match m, String expected, int index) {
		assertNotNull("No match", m);
		assertEquals("Unexpected match", expected, m.getText());
		assertEquals("Unexpected index", index, m.getOffset());
	}

	private static void testList(List<Match> matches, String expected) {
		Collections.sort(matches, (a, b) -> {
			int cmp = Integer.compare(a.getOffset(), b.getOffset());
			if (cmp != 0) {
				return cmp;
			}
			return Integer.compare(a.getText().length(), b.getText().length());
		});
		assertEquals("Unexpected results", expected, matches.toString());
	}

	@Test
	public void test001() throws Exception {
		Match m = run("dcccacabcccabcc", "ab", "cab");
		test(m, "cab", 5);
	}

	@Test
	public void test002() throws Exception {
		Match m = run("dcccacabcccabcc", "ab", "abc");
		test(m, "abc", 6);
	}

	@Test
	public void test003() throws Exception {
		Match m = run("dcccacabcccabcc", "ab", "cxb");
		test(m, "ab", 6);
	}

	@Test
	public void test004() throws Exception {
		Match m = run("dcccacabcccabcc", "abc", "cabx");
		test(m, "abc", 6);
	}

	@Test
	public void test005() throws Exception {
		Match m = run("dacabddd", "ac", "cab");
		test(m, "ac", 1);
	}

	@Test
	public void test006() throws Exception {
		Match m = run("dacabddd", "aca", "cab");
		test(m, "aca", 1);
	}

	@Test
	public void test007() throws Exception {
		Match m = run("dacabddd", "acab", "cab");
		test(m, "acab", 1);
	}

	@Test
	public void test008() throws Exception {
		Match m = run("ddddddcac", "ac", "cab");
		test(m, "ac", 7);
	}

	@Test
	public void test009() throws Exception {
		Match m = run("dddddcacddd", "cacx", "ac");
		test(m, "ac", 6);
	}

	@Test
	public void test010() throws Exception {
		Match m = run("ddddddcac", "ac", "cac");
		test(m, "cac", 6);
	}

	@Test
	public void test011() throws Exception {
		Match m = run("a", "a", "b", "ab");
		test(m, "a", 0);
	}

	@Test
	public void test012() throws Exception {
		Match m = run("b", "a", "b", "ab");
		test(m, "b", 0);
	}

	@Test
	public void test013() throws Exception {
		Match m = run("ab", "a", "b", "ab");
		test(m, "ab", 0);
	}

	@Test
	public void test014() throws Exception {
		Match m = run("", "a", "b", "ab");
		assertNull("Expected no match", m);
	}

	@Test
	public void test015() throws Exception {
		Match m = run("dddca", "ac", "cac", "ab");
		assertNull("Expected no match", m);
	}

	@Test
	public void test016() throws Exception {
		Match m = run("ab", "ab", "b");
		test(m, "ab", 0);
	}

	@Test
	public void test017() throws Exception {
		Match m = run("ushers", "he", "she", "his", "hers");
		test(m, "she", 1);
	}

	@Test
	public void test018() throws Exception {
		Match m = run("dddhisheddd", "he", "she", "his", "hers");
		test(m, "his", 3);
	}

	@Test
	public void test019() throws Exception {
		Match m = run("sotat", "at", "art", "oars", "soar");
		test(m, "at", 3);
	}

	@Test
	public void test020() throws Exception {
		Match m = run("xxx", "x", "xx", "xxx");
		test(m, "xxx", 0);
	}

	@Test
	public void test021() throws Exception {
		Match m = run("xx", "x", "xx", "xxx");
		test(m, "xx", 0);
	}

	@Test
	public void test022() throws Exception {
		Match m = run("x", "x", "xx", "xxx");
		test(m, "x", 0);
	}

	@Test
	public void test023() throws Exception {
		Match m = run("Lorem\r\nIpsum", "\n", "\r\n", "\r");
		test(m, "\r\n", 5);
	}

	@Test
	public void test024() throws Exception {
		Match m = run("dcccacabcccabcc", "ab", "abcd");
		test(m, "ab", 6);
	}

	@Test
	public void test025() throws Exception {
		Match m = run("dcccacabcccabcc", "abcd", "bccc");
		test(m, "bccc", 7);
	}

	@Test
	public void test026() throws Exception {
		Match m = run("xxx", 1, "x", "xx", "xxx");
		test(m, "xx", 1);
	}

	@Test
	public void test027() throws Exception {
		Match m = run("xxx", 2, "x", "xx", "xxx");
		test(m, "x", 2);
	}

	@Test
	public void test028() throws Exception {
		Match m = run("dddhisheddd", 7, "he", "she", "his", "hers");
		assertNull("Expected no match", m);
	}

	@Test
	public void test029() throws Exception {
		Match m = run("Lorem001Ipsum", "1", "01", "0");
		test(m, "0", 5);
	}

	@Test
	public void test030() throws Exception {
		Match m = run("Lorem01Ipsum", "1", "01", "0");
		test(m, "01", 5);
	}

	@Test
	public void test031() throws Exception {
		Match m = run("ddcababababababcabxdd", "ca", "cabx", "ababc");
		test(m, "ca", 2);
	}

	@Test
	public void test032() throws Exception {
		Match m = run("ddcababcabxdd", "ca", "cabx", "cababc", "ababc");
		test(m, "cababc", 2);
	}

	@Test
	public void test033() throws Exception {
		Match m = run("ddcababcdd", "ca", "cabx", "cababc", "ababc");
		test(m, "cababc", 2);
	}

	@Test
	public void test034() throws Exception {
		Match m = run("ddcababcdd", "cababx", "ababc");
		test(m, "ababc", 3);
	}

	@Test
	public void test035() throws Exception {
		Match m = run("ddcababcdd", "cababx", "abab", "ababc");
		test(m, "ababc", 3);
	}

	@Test
	public void test036() throws Exception {
		Match m = run("ddcabababcdd", "ab", "abab", "cabababcy", "cabababcyy");
		test(m, "abab", 3);
	}

	@Test
	public void test037() throws Exception {
		Match m = run("ddcabababcdd", "ab", "abab", "cabababc", "cabababcyy");
		test(m, "cabababc", 2);
	}

	@Test
	public void test038() throws Exception {
		Match m = run("ddcabababcdd", "ab", "bababcd", "cabababcy", "cabababcyy");
		test(m, "ab", 3);
	}

	@Test
	public void test039() throws Exception {
		Match m = run("ddcabababcddf", "bababcd", "bababcddf", "cabababcy", "cabababcyy");
		test(m, "bababcddf", 4);
	}

	@Test
	public void test040() throws Exception {
		Match m = run("ddcabababcddffxx", "ffxx", "bababcd", "bababcddffy", "cabababcy", "cabababcyy");
		test(m, "bababcd", 4);
	}

	@Test
	public void multi001() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("he", "she", "his", "hers");
		List<Match> matches = m.find("ushers", 0);
		testList(matches, "[[she, 1], [he, 2], [hers, 2]]");
	}

	@Test
	public void multi002() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("he", "she", "his", "hers");
		List<Match> matches = m.find("dddhisheddd", 0);
		testList(matches, "[[his, 3], [she, 5], [he, 6]]");
	}

	@Test
	public void multi003() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("he", "she", "his", "sh", "is");
		List<Match> matches = m.find("dddhisheddd", 0);
		testList(matches, "[[his, 3], [is, 4], [sh, 5], [she, 5], [he, 6]]");
	}

	@Test
	public void multi004() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("ab", "about", "at", "ate", "be", "bed", "edge", "get");
		List<Match> matches = m.find("abedgetab", 0);
		testList(matches, "[[ab, 0], [be, 1], [bed, 1], [edge, 2], [get, 4], [ab, 7]]");
	}

	@Test
	public void multi005() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("at", "art", "oars", "soar");
		List<Match> matches = m.find("soars", 0);
		testList(matches, "[[soar, 0], [oars, 1]]");
	}

	@Test
	public void multi006() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("at", "art", "oars", "soar");
		List<Match> matches = m.find("oart", 0);
		testList(matches, "[[art, 1]]");
	}

	@Test
	public void multi007() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("at", "art", "oars", "soar");
		List<Match> matches = m.find("soarsoars", 0);
		testList(matches, "[[soar, 0], [oars, 1], [soar, 4], [oars, 5]]");
	}

	@Test
	public void multi008() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("at", "art", "oars", "soar");
		List<Match> matches = m.find("sotat", 0);
		testList(matches, "[[at, 3]]");
	}

	@Test
	public void multi009() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("i", "in", "tin", "sting");
		List<Match> matches = m.find("sting", 0);
		testList(matches, "[[sting, 0], [tin, 1], [i, 2], [in, 2]]");
	}

	@Test
	public void multi010() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("i", "in", "tin", "sting", "hastings");
		List<Match> matches = m.find("hastings", 0);
		testList(matches, "[[hastings, 0], [sting, 2], [tin, 3], [i, 4], [in, 4]]");
	}

	@Test
	public void multi011() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("x", "xx", "xxx");
		List<Match> matches = m.find("xxx", 0);
		testList(matches, "[[x, 0], [xx, 0], [xxx, 0], [x, 1], [xx, 1], [x, 2]]");
	}

	@Test
	public void multi012() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("she", "his", "hers");
		List<Match> matches = m.find("dddhiheddd", 0);
		assertEquals("Expected no match", 0, matches.size());
	}

	@Test
	public void multi013() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("he", "she", "his", "hers");
		List<Match> matches = m.find("dddhisheddd", 4);
		testList(matches, "[[she, 5], [he, 6]]");
	}

	@Test
	public void multi014() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.create("x", "xx", "xxx");
		List<Match> matches = m.find("xxx", 2);
		testList(matches, "[[x, 2]]");
	}

	@Test
	public void noStrings001() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.builder().build();
		assertNull("Expected no match", m.indexOf("dhihedd", 0));
		List<Match> matches = m.find("dddhiheddd", 0);
		assertEquals("Expected no match", 0, matches.size());
	}

	@Test
	public void noStrings002() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.builder().add("").build();
		assertNull("Expected no match", m.indexOf("dhihedd", 0));
		List<Match> matches = m.find("dddhiheddd", 0);
		assertEquals("Expected no match", 0, matches.size());
	}

	@Test
	public void noStrings003() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.builder().add((String[]) null).build();
		assertNull("Expected no match", m.indexOf("dhihedd", 0));
		List<Match> matches = m.find("dddhiheddd", 0);
		assertEquals("Expected no match", 0, matches.size());
	}

	@Test
	public void fluent001() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.builder().add("he", "she", "his", "hers").build();
		test(m.indexOf("ushers", 0), "she", 1);
	}

	@Test
	public void fluent002() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.builder().add("he", "she").add("his", "hers").build();
		testList(m.find("ushers", 0), "[[she, 1], [he, 2], [hers, 2]]");
	}

	@Test
	public void fluent003() throws Exception {
		MultiStringMatcher m = MultiStringMatcher.builder().add("he").add(null, "she", "").add("his", "hers").build();
		testList(m.find("ushers", 0), "[[she, 1], [he, 2], [hers, 2]]");
	}

	@Test
	public void addAfterBuild() throws Exception {
		MultiStringMatcher.Builder b = MultiStringMatcher.builder().add("he", "she").add("his", "hers");
		b.build();
		assertThrows(IllegalStateException.class, () -> b.add("us"));
	}

	@Test
	public void reuseBuilder() throws Exception {
		MultiStringMatcher.Builder b = MultiStringMatcher.builder().add("he", "she").add("his", "hers");
		b.build();
		assertThrows(IllegalStateException.class, () -> b.build());
	}

	@Test
	public void scan001() throws Exception {
		TestCharSequence text = new TestCharSequence("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		Match m = run(text, 0, "x", "xx", "xxx", "xxxx");
		test(m, "xxxx", 0);
		assertEquals("Scanned too far", 3, text.getLastIndex());
	}

	@Test
	public void scan002() throws Exception {
		TestCharSequence text = new TestCharSequence("ddcababababababcabxdd");
		Match m = run(text, 0, "ca", "cabx", "ababc");
		test(m, "ca", 2);
		assertEquals("Scanned too far", 5, text.getLastIndex());
	}

	@Test
	public void scan003() throws Exception {
		TestCharSequence text = new TestCharSequence("ddcabarbarazz");
		Match m = run(text, 0, "a", "cabby", "barbara");
		test(m, "a", 3);
		assertEquals("Scanned too far", 5, text.getLastIndex());
	}

	private static class TestCharSequence implements CharSequence {

		private final String value;

		private int lastIndex = -1;

		private int backtrack = 0;

		public TestCharSequence(String value) {
			this.value = value;
		}

		@Override
		public int length() {
			return value.length();
		}

		@Override
		public char charAt(int index) {
			if (index < lastIndex) {
				backtrack = Math.min(backtrack, index - lastIndex);
			}
			lastIndex = index;
			return value.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			throw new UnsupportedOperationException();
		}

		public int getLastIndex() {
			return lastIndex;
		}

		public int getBackTrack() {
			return backtrack;
		}
	}
}
