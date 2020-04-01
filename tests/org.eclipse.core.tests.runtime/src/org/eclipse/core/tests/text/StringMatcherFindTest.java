/*******************************************************************************
 * Copyright (C) 2020, Thomas Wolf <thomas.wolf@paranor.ch> and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.core.tests.text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.eclipse.core.text.StringMatcher;
import org.eclipse.core.text.StringMatcher.Position;

/**
 * Poor man's JUnit 3.8 parameterized tests for plain text matching in
 * {@link StringMatcher}.
 */
public class StringMatcherFindTest extends TestCase {

	/** Marker in test inputs for empty strings. */
	private static final String EMPTY = "<empty>";

	protected static class TestData {

		int line;

		String pattern;

		String text;

		int from;

		int to;

		int start;

		int end;

		@Override
		public String toString() {
			return "line " + line + ": " + (start >= 0 ? "OK" : "NOK") + " pattern=" + pattern + ", text=" + text + '['
					+ from + ',' + to + ']';
		}
	}

	public static TestData[] getTestData(String fileName) throws IOException {
		List<TestData> data = new ArrayList<>();
		File testData = RuntimeTestsPlugin.getTestData("testData/text/" + fileName);
		try (BufferedReader reader = Files.newBufferedReader(testData.toPath(), StandardCharsets.UTF_8)) {
			String line;
			for (int i = 1; (line = reader.readLine()) != null; i++) {
				String l = line.trim();
				if (l.isEmpty() || l.charAt(0) == '#') {
					continue;
				}
				String[] parts = line.split("\t");
				if (parts.length < 6) {
					fail("File " + fileName + ": invalid test input line " + i + ": " + line);
				}
				TestData item = new TestData();
				item.line = i;
				if (EMPTY.equals(parts[0])) {
					parts[0] = "";
				}
				item.pattern = parts[0];
				if (EMPTY.equals(parts[1])) {
					parts[1] = "";
				}
				item.text = parts[1];
				item.from = Integer.parseInt(parts[2]);
				item.to = Integer.parseInt(parts[3]);
				item.start = Integer.parseInt(parts[4]);
				item.end = Integer.parseInt(parts[5]);
				data.add(item);
			}
		}
		return data.toArray(new TestData[0]);
	}

	public static Test suite() {
		TestSuite tests = new TestSuite();
		TestData[] items;
		try {
			items = getTestData("StringMatcherFindTest.txt");
			if (items == null || items.length == 0) {
				tests.addTest(TestSuite.warning("No tests found in StringMatcherFindTest.txt"));
				return tests;
			}
		} catch (IOException e) {
			tests.addTest(TestSuite.warning("Could not load test data: " + e));
			return tests;
		}
		for (TestData item : items) {
			tests.addTest(new TestCase("find[" + item + ']') {

				@Override
				protected void runTest() throws Throwable {
					StringMatcher matcher = new StringMatcher(item.pattern, true, false);
					Position position = matcher.find(item.text, item.from, item.to);
					if (item.start < 0) {
						assertNull("No match expected", position);
					} else {
						assertEquals("Unexpected match result", new Position(item.start, item.end), position);
					}
				}
			});
		}
		return tests;
	}
}
