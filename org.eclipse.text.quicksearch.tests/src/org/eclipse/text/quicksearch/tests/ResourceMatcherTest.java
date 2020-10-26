/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Path;
import org.eclipse.text.quicksearch.internal.core.pathmatch.ResourceMatcher;
import org.eclipse.text.quicksearch.internal.core.pathmatch.ResourceMatchers;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ResourceMatcherTest {

	@Test
	public void simpleRelativePattern() throws Exception {
		assertMatch(true, "*.java", "/myproject/something/nested/foo.java");
		assertMatch(false, "*.java", "/myproject/foo.class");
	}

	@Test
	public void commaSeparatedPaths() throws Exception {
		String[] patterns = new String[] { //
				"*.java,*.properties", //
				"*.java, *.properties", //
				"*.java ,*.properties", //
				"*.java , *.properties", //
				" *.java  ,  *.properties ", //
				" *.java  ,,  *.properties ", //
				" *.java  ,  ,  *.properties ", //
				" *.java  ,*.foo,  *.properties ", //
		};
		for (String pattern : patterns) {
			assertMatch(true, pattern, "/myproject/something/nested/foo.java");
			assertMatch(true, pattern, "/myproject/something/nested/application.properties");
			assertMatch(false, pattern, "/myproject/something/nested/test.log");
		}
	}

	@Test
	public void complexRelativePattern() throws Exception {
		assertMatch(true, "src/**/*.java", "/myproject/src/my/package/Foo.java");
		assertMatch(false, "src/**/*.java", "/myproject/resources/my/package/Foo.java");
	}

	@Test
	public void absolutePath() throws Exception {
		assertMatch(true, "/myproject/**/*.java", "/myproject/src/my/package/Foo.java");
		assertMatch(false, "/myproject/**/*.java", "/otherproject/src/my/package/Foo.java");
	}

	private void assertMatch(boolean expectedMatch, String patterns, String path) {
		assertTrue(new Path(path).isAbsolute());
		ResourceMatcher matcher = ResourceMatchers.commaSeparatedPaths(patterns);
		assertEquals("Wrong match with pattern: '" + patterns + "'", expectedMatch, matcher.matches(new MockResource(path)));

		// Most ResourceMatchers have a custom toString. Do a quick test to check for thrown exceptions.
		assertNotNull(matcher.toString());
	}
}
