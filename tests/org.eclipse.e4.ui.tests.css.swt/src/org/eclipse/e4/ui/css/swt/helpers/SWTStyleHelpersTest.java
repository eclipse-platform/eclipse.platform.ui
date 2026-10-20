/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class SWTStyleHelpersTest {

	private static List<String> words(int style) {
		return List.of(SWTStyleHelpers.getSWTWidgetStyleAsString(style, " ").split(" "));
	}

	@Test
	void noStyleBitsProducesEmptyString() {
		assertEquals("", SWTStyleHelpers.getSWTWidgetStyleAsString(SWT.NONE, " "));
	}

	@Test
	void styleBitIsReportedByName() {
		assertTrue(words(SWT.BORDER).contains("SWT.BORDER"));
	}

	@Test
	void everyStyleBitOfAWidgetIsReported() {
		List<String> styles = words(SWT.PUSH | SWT.BORDER);
		assertTrue(styles.contains("SWT.PUSH"));
		assertTrue(styles.contains("SWT.BORDER"));
	}

	@Test
	void constantsSharingABitAreBothReported() {
		// SWT.DOWN and SWT.BOTTOM are the same value, and the dark theme selects on both
		assertEquals(SWT.DOWN, SWT.BOTTOM);
		List<String> styles = words(SWT.DOWN);
		assertTrue(styles.contains("SWT.DOWN"));
		assertTrue(styles.contains("SWT.BOTTOM"));
	}

	@Test
	void stylesAreJoinedWithTheGivenSeparator() {
		String styles = SWTStyleHelpers.getSWTWidgetStyleAsString(SWT.PUSH | SWT.BORDER, ",");
		assertTrue(styles.contains("SWT.PUSH,"));
		assertEquals(-1, styles.indexOf(' '));
	}

	@Test
	void disposedWidgetHasNoStyle() {
		Shell shell = new Shell(Display.getDefault(), SWT.BORDER);
		shell.dispose();
		assertEquals("", SWTStyleHelpers.getSWTWidgetStyleAsString(shell));
	}
}
