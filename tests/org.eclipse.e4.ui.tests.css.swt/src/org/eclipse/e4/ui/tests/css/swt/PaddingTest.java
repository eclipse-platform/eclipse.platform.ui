/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
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
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code CSSPropertyPaddingSWTHandler}. Padding only takes effect
 * when the target widget is a {@link CTabFolder} whose renderer exposes
 * {@code getPadding()} returning a {@link Rectangle} and
 * {@code setPadding(int,int,int,int)}; the handler reaches them via
 * reflection. The default {@link CTabFolderRenderer} does not expose those
 * methods, so the test installs a capturing subclass that does.
 */
public class PaddingTest extends CSSSWTTestCase {

	/**
	 * Captures arguments passed to the reflective {@code setPadding} call.
	 * The handler invokes {@code setPadding(left, right, top, bottom)}; this
	 * stub stores the four values in that order so the tests can assert on
	 * them directly. Must be public so the handler's
	 * {@code Method.invoke} from another package succeeds.
	 */
	public static class CapturingRenderer extends CTabFolderRenderer {
		Rectangle padding = new Rectangle(0, 0, 0, 0);
		int[] lastSet;

		CapturingRenderer(CTabFolder folder) {
			super(folder);
		}

		public Rectangle getPadding() {
			return padding;
		}

		public void setPadding(int left, int right, int top, int bottom) {
			lastSet = new int[] { left, right, top, bottom };
			padding = new Rectangle(top, right, bottom, left);
		}
	}

	private CapturingRenderer applyToCapturingFolder(String styleSheet) {
		engine = createEngine(styleSheet, display);
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new FillLayout());
		CTabFolder folder = new CTabFolder(shell, SWT.NONE);
		CapturingRenderer renderer = new CapturingRenderer(folder);
		folder.setRenderer(renderer);
		engine.applyStyles(shell, true);
		return renderer;
	}

	@Test
	void testPaddingShorthandSingleValue() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding: 15px }");
		// setPadding(left, right, top, bottom)
		assertArrayEquals(new int[] { 15, 15, 15, 15 }, renderer.lastSet);
	}

	@Test
	void testPaddingShorthandTwoValues() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding: 10px 20px }");
		// vertical 10, horizontal 20
		assertArrayEquals(new int[] { 20, 20, 10, 10 }, renderer.lastSet);
	}

	@Test
	void testPaddingShorthandFourValues() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding: 10px 15px 20px 40px }");
		// top 10, right 15, bottom 20, left 40
		assertArrayEquals(new int[] { 40, 15, 10, 20 }, renderer.lastSet);
	}

	@Test
	void testPaddingTop() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding-top: 7px }");
		assertEquals(7, renderer.padding.x, "top stored in Rectangle.x");
	}

	@Test
	void testPaddingRight() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding-right: 8px }");
		assertEquals(8, renderer.padding.y, "right stored in Rectangle.y");
	}

	@Test
	void testPaddingBottom() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding-bottom: 9px }");
		assertEquals(9, renderer.padding.width, "bottom stored in Rectangle.width");
	}

	@Test
	void testPaddingLeft() {
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding-left: 11px }");
		assertEquals(11, renderer.padding.height, "left stored in Rectangle.height");
	}

	@Test
	void testPaddingOnNonCTabFolderIsSilentlyIgnored() {
		// Padding on a non-CTabFolder widget must not crash. The handler's
		// setPadding helper only acts on CTabFolder, every other widget is a
		// no-op.
		assertDoesNotThrow(() -> {
			engine = createEngine("Button { padding: 10px }", display);
			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			shell.setLayout(new FillLayout());
			Composite panel = new Composite(shell, SWT.NONE);
			panel.setLayout(new FillLayout());
			new Button(panel, SWT.PUSH);
			engine.applyStyles(shell, true);
		});
	}

	@Test
	void testPaddingOnDefaultRendererIsSilentlyIgnored() {
		// CTabFolder's default renderer does not expose getPadding/setPadding,
		// so the handler's reflective call throws NoSuchMethodException which
		// is caught and swallowed. Locks in this behaviour: applying padding
		// to a folder with the default renderer must not surface an error.
		assertDoesNotThrow(() -> {
			engine = createEngine("CTabFolder { padding: 10px 20px }", display);
			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			shell.setLayout(new FillLayout());
			new CTabFolder(shell, SWT.NONE);
			engine.applyStyles(shell, true);
		});
	}

	@Test
	void testPaddingNonPxUnitIsIgnored() {
		// Padding values must be in CSS_PX units; other units (em, %, etc.)
		// fall through and the renderer keeps its previous value.
		CapturingRenderer renderer = applyToCapturingFolder("CTabFolder { padding-top: 50% }");
		assertEquals(0, renderer.padding.x, "non-px padding-top must be ignored");
	}
}
