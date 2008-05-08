/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.internal.ReopenEditorMenu;

import junit.framework.TestCase;

/**
 * @since 3.4
 * 
 */
public class ReopenMenuTest extends TestCase {
	private static class TextInfo {
		int index;
		String filename;
		String path;
		String ltrExpected;
		String rtlExpected;

		public TextInfo(int index, String filename, String path,
				String ltrExpected, String rtlExpected) {
			this.index = index;
			this.filename = filename;
			this.path = path;
			this.ltrExpected = ltrExpected;
			this.rtlExpected = rtlExpected;
		}
	}

	private TextInfo[] data = {
			new TextInfo(
					1,
					"ReopenMenuTest.java",
					"org.eclipse.ui.tests/Eclipse UI Tests/org/eclipse/ui/tests/internal/ReopenMenuTest.java",
					"&2 ReopenMenuTest.java  [org.eclipse.ui...]",
					"ReopenMenuTest.java  [org.eclipse.ui...] &2"),
			new TextInfo(2, "A.java", "ex/src/ex/A.java",
					"&3 A.java  [ex/src/ex]", "A.java  [ex/src/ex] &3"),
			new TextInfo(
					3,
					"AReallyLongNameLazyFoxJumpsSomeStupidRiver.java",
					"org.eclipse.ui.tests/Eclipse UI Tests/org/eclipse/ui/tests/internal/AReallyLongNameLazyFoxJumpsSomeStupidRiver.java",
					"&4 AReallyLongNameLazyFoxJumpsSomeStupid...",
					"AReallyLongNameLazyFoxJumpsSomeStupid... &4"), };

	/**
	 * @param name
	 */
	public ReopenMenuTest(String name) {
		super(name);
	}

	public void testLtr() {
		for (int i = 0; i < data.length; i++) {
			TextInfo info = data[i];
			String expected = TextProcessor.process(info.ltrExpected,
					TextProcessor.getDefaultDelimiters() + "[]");
			String val = ReopenEditorMenu.calcText(info.index, info.filename,
					info.path, false);
			assertEquals("testing item " + i, expected, val);
		}
	}

	public void testRtl() {
		for (int i = 0; i < data.length; i++) {
			TextInfo info = data[i];
			String expected = TextProcessor.process(info.rtlExpected,
					TextProcessor.getDefaultDelimiters() + "[]");
			String val = ReopenEditorMenu.calcText(info.index, info.filename,
					info.path, true);
			assertEquals("testing item " + i, expected, val);
		}
	}
}
