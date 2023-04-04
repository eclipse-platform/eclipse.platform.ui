/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ChooseWorkspaceDialogTests {

	/**
	 * Validates that {@link ChooseWorkspaceDialog#filterDuplicatedPaths(String[])}
	 * filters out non-unique paths.
	 *
	 * @see <a href=
	 *      "https://github.com/eclipse-platform/eclipse.platform.ui/issues/312">GitHub
	 *      issue 312</a>
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=531611">Eclipse
	 *      bug 531611</a>
	 */
	@Test
	public void testFilterDuplicatedPaths() {
		String[] testPaths = {
				"some/location1/",
				"/some/location1/",
				"/some/location1",
				"/some/location2/",
				"/some///location1",
				"//some//location1//",
		};
		String[] expectedFilteredPaths = { "some/location1/", "/some/location1/", "/some/location2/", };
		adoptToWindows(testPaths);
		adoptToWindows(expectedFilteredPaths);
		List<String> actualFilteredPaths = ChooseWorkspaceDialog.filterDuplicatedPaths(testPaths);
		assertEquals("Non-unique paths were not filtered as expected", Arrays.asList(expectedFilteredPaths),
				actualFilteredPaths);
	}

	@Test
	public void forceBuild() {
		assertEquals(1, 1);
	}

	static void adoptToWindows(String[] paths) {
		if (File.separatorChar == '\\') {
			for (int i = 0; i < paths.length; i++) {
				paths[i] = paths[i].replace('/', '\\');
			}
		}
	}
}
