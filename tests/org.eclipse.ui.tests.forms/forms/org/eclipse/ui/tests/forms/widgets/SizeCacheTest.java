/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.SizeCache;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SizeCacheTest {
	private static Display display;
	private Shell shell;
	private static final String SHORT_TEXT = "Hedgehog";
	private static final String LONG_TEXT = "A hedgehog is any of the spiny mammals of the subfamily Erinaceinae, in the order Erinaceomorpha. " //$NON-NLS-1$
			+ "There are seventeen species of hedgehog in five genera, found through parts of Europe, Asia, Africa and New Zealand. " //$NON-NLS-1$
			;
	private Font font;
	// change this to true if you want to see test is slow motion
	private boolean humanWatching = Boolean.valueOf(System.getProperty("junit.human.watching"));
	private SizeCache sizeCache;
	private Control control;
	private Point expectedSize;

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}
	}

	@Before
	public void setUp() throws Exception {
		font = new Font(display, "Arial", 12, SWT.NORMAL);
		shell = new Shell(display);
		shell.setSize(600, 400);
		shell.setLayout(GridLayoutFactory.fillDefaults().create());
		shell.setFont(font);
		shell.open();
	}

	@After
	public void tearDown() throws Exception {
		if (humanWatching)
			dispatch(1000);
		shell.dispose();
		font.dispose();
	}

	private static void dispatch0() {
		while (display.readAndDispatch()) {
		}
	}

	private static void dispatch(int msec) {
		long cur = System.currentTimeMillis();
		do {
			dispatch0();
			long pass = System.currentTimeMillis() - cur;
			if (pass < msec) {
				// not doing display.sleep() because its automated tests,
				// nothing will cause any display events
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			} else
				break;
		} while (true);
	}

	private Label createLabel(Composite comp, String text, int style) {
		Label l = new Label(comp, style);
		l.setText(text);
		l.setFont(comp.getFont());
		return l;
	}

	private Button createButton(Composite comp, String text, int style) {
		Button l = new Button(comp, style);
		l.setText(text);
		l.setFont(comp.getFont());
		return l;
	}

	/**
	 * This does automatic check to make sure that cached size of control is the
	 * same as calculated size. It also return current calulated size if needs
	 * to be used for further testing.
	 */
	private Point checkSizeEquals(int whint, int hhint) {
		expectedSize = controlComputeSize(whint, hhint);
		// this is just for show if somebody is watching
		control.setSize(expectedSize);
		dispatch();

		checkDoubleCall(whint, hhint);
		checkPreferedThenOther(whint, hhint);
		return expectedSize;
	}

	private Point controlComputeSize(int wHint, int hHint) {
		Point adjusted = computeHintOffset();

		int adjustedWidthHint = wHint;
		if (adjustedWidthHint != SWT.DEFAULT) {
			adjustedWidthHint = Math.max(0, wHint - adjusted.x);
		}

		int adjustedHeightHint = hHint;
		if (adjustedHeightHint != SWT.DEFAULT) {
			adjustedHeightHint = Math.max(0, hHint - adjusted.y);
		}

		Point result = control.computeSize(adjustedWidthHint, adjustedHeightHint, true);

		// Ignore any component if the hint was something other than SWT.DEFAULT.
		// There's no way to measure hints smaller than the adjustment value and
		// somecontrols have buggy computeSize methods that don't return non-default
		// hints verbatim. The purpose of this test is to verify SizeCache, not the
		// controls, and we don't want such quirks to create failures, so we correct the
		// result here if necessary.
		if (wHint != SWT.DEFAULT) {
			result.x = wHint;
		}

		if (hHint != SWT.DEFAULT) {
			result.y = hHint;
		}

		return result;
	}

	private Point checkAlterate(int whint, int hhint) {
		Point expectedSize1 = controlComputeSize(-1, hhint);
		Point expectedSize2 = controlComputeSize(whint, -1);
		resetCache();
		checkCacheComputeSize(expectedSize1, -1, hhint);
		checkCacheComputeSize(expectedSize1, -1, hhint);
		// switch
		checkCacheComputeSize(expectedSize2, whint, -1);
		checkCacheComputeSize(expectedSize2, whint, -1);
		return expectedSize1;
	}

	private Point computeHintOffset() {
		Point size = new Point(0, 0);
		if (control instanceof Scrollable) {
			// For scrollables, subtract off the trim size
			Scrollable scrollable = (Scrollable) control;
			Rectangle trim = scrollable.computeTrim(0, 0, 0, 0);

			size.x = trim.width;
			size.y = trim.height;
		} else {
			// For non-composites, subtract off 2 * the border size
			size.x = control.getBorderWidth() * 2;
			size.y = size.x;
		}
		return size;
	}

	private void checkPreferedThenOther(int whint, int hhint) {
		resetCache();
		sizeCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		checkCacheComputeSize(expectedSize, whint, hhint);
	}

	private void checkDoubleCall(int whint, int hhint) {
		resetCache();
		checkCacheComputeSize(expectedSize, whint, hhint);
		// calling this again should return same value
		checkCacheComputeSize(expectedSize, whint, hhint);
	}

	private void resetCache() {
		sizeCache = new SizeCache(control);
	}

	private void checkCacheComputeSize(Point calcSize, int whint, int hhint) {
		Point cachedSize = sizeCache.computeSize(whint, hhint);
		assertEquals(calcSize, cachedSize);
	}

	private void update() {
		shell.layout(true, true);
		dispatch();
	}

	private void dispatch() {
		if (humanWatching)
			dispatch(200);
		else
			dispatch0();
	}

	private Composite createComposite(Composite parent, int flags) {
		Composite comp = new Composite(parent, flags);
		comp.setFont(parent.getFont());
		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_MAGENTA));
		return comp;
	}

	private Composite createFillComp(Composite parent, int flags) {
		Composite comp = createComposite(parent, flags);
		GridLayoutFactory.fillDefaults().applyTo(comp);
		Label l = createLabel(comp, LONG_TEXT, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(l);
		return comp;
	}

	private Composite createFixedComp(Composite parent, int flags) {
		Composite comp = createComposite(parent, flags);
		GridLayoutFactory.fillDefaults().applyTo(comp);
		Label l = createLabel(comp, SHORT_TEXT, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(l);
		return comp;
	}

	/**
	 * This does automatic test to make sure that cached size of control is the
	 * same as calculated size with different given hints
	 *
	 * @param inner
	 */
	private void checkCacheSize(Control inner) {
		update();
		control = inner;
		Point size = controlComputeSize(SWT.DEFAULT, SWT.DEFAULT);
		int w = size.x;
		int h = size.y;

		checkSizeEquals(SWT.DEFAULT, SWT.DEFAULT);

		checkSizeEquals(w, SWT.DEFAULT); // preferred width
		checkSizeEquals(w / 2, SWT.DEFAULT); // half of preferred width
		checkSizeEquals(w * 2, SWT.DEFAULT); // bigger then preferred with
		checkSizeEquals(SWT.DEFAULT, h);
		checkSizeEquals(SWT.DEFAULT, h / 2);
		checkSizeEquals(SWT.DEFAULT, h * 2);

		checkSizeEquals(w, h);
		checkSizeEquals(w / 2, h * 2);
		checkSizeEquals(w * 2, h * 2);

		checkSizeEquals(1, SWT.DEFAULT);
		checkSizeEquals(SWT.DEFAULT, 1);
		checkSizeEquals(0, 0);

		checkAlterate(w, h);
		checkAlterate(w * 2, h * 2);
		checkAlterate(w / 2, h / 2);
	}

	@Test
	public void testFixedLabel() {
		checkCacheSize(createLabel(shell, SHORT_TEXT, SWT.NONE));
	}

	@Test
	public void testWrapLabel() {
		checkCacheSize(createLabel(shell, SHORT_TEXT, SWT.WRAP));
	}

	@Test
	public void testFixedComp() {
		checkCacheSize(createFixedComp(shell, SWT.NONE));
	}

	@Test
	public void testFixedCompWithWrapFlag() {
		checkCacheSize(createFixedComp(shell, SWT.WRAP));
	}

	@Test
	public void testFillComp() {
		checkCacheSize(createFillComp(shell, SWT.NONE));
	}

	@Test
	public void testFillCompWithWrapFlag() {
		checkCacheSize(createFillComp(shell, SWT.WRAP));
	}

	@Test
	public void testFillCompWithBorder() {
		checkCacheSize(createFillComp(shell, SWT.BORDER));
	}

	@Test
	public void testWrapCompNonWrapLabels() {
		Composite inner = createComposite(shell, SWT.NONE);
		inner.setLayout(new TableWrapLayout());
		createLabel(inner, SHORT_TEXT, SWT.NONE);
		createLabel(inner, LONG_TEXT, SWT.NONE);
		checkCacheSize(inner);
	}

	@Test
	public void testWrapCompWrapLabels() {
		Composite inner = createComposite(shell, SWT.NONE);
		inner.setLayout(new TableWrapLayout());
		createLabel(inner, SHORT_TEXT, SWT.WRAP);
		createLabel(inner, LONG_TEXT, SWT.WRAP);
		checkCacheSize(inner);
	}

	@Test
	public void testFixedLabelLong() {
		checkCacheSize(createLabel(shell, LONG_TEXT, SWT.NONE));
	}

	@Test
	public void testWrapLabelLong() {
		checkCacheSize(createLabel(shell, LONG_TEXT, SWT.WRAP));
	}

	@Test
	public void testHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.NONE);
		link.setText(LONG_TEXT);
		link.setFont(shell.getFont());
		checkCacheSize(link);
	}

	@Test
	public void testHyperlinkWithBorder() {
		Hyperlink link = new Hyperlink(shell, SWT.BORDER);
		link.setText(LONG_TEXT);
		link.setFont(shell.getFont());
		checkCacheSize(link);
	}

	public void suppressed_testWrapHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.WRAP);
		link.setText(LONG_TEXT);
		link.setFont(shell.getFont());
		checkCacheSize(link);
	}

	public void suppressed_testButton() {
		checkCacheSize(createButton(shell, LONG_TEXT, SWT.PUSH));
	}

	@Test
	public void testCheckButton() {
		checkCacheSize(createButton(shell, LONG_TEXT, SWT.CHECK));
	}

	@Test
	public void testWrapCompButtonsWrap() {
		Composite inner = createComposite(shell, SWT.NONE);
		inner.setLayout(new TableWrapLayout());
		createButton(inner, SHORT_TEXT, SWT.WRAP | SWT.CHECK);
		createButton(inner, LONG_TEXT, SWT.WRAP | SWT.CHECK);
		checkCacheSize(inner);
	}

	@Test
	public void testWrapCompWrapLabels3() {
		Composite inner = createComposite(shell, SWT.NONE);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 3;
		inner.setLayout(layout);
		createLabel(inner, SHORT_TEXT, SWT.WRAP);
		createLabel(inner, LONG_TEXT, SWT.WRAP);
		checkCacheSize(inner);
	}

	@Test
	public void testGripWrap3() {
		Composite inner = createComposite(shell, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(inner);
		GridDataFactory gdf = GridDataFactory.fillDefaults();
		createLabel(inner, SHORT_TEXT, SWT.WRAP).setLayoutData(gdf.create());
		createLabel(inner, LONG_TEXT, SWT.WRAP).setLayoutData(gdf.create());
		createLabel(inner, SHORT_TEXT, SWT.NONE).setLayoutData(gdf.create());
		checkCacheSize(inner);
	}
}
