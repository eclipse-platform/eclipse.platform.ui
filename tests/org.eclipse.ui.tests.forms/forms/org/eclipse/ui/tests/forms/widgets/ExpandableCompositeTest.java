/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Tests for expandable composite
 */
public class ExpandableCompositeTest extends TestCase {
	private static final int TITLE_TWIST = ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR;
	private static final String TEXT1 = "Text";
	private static Display display;
	private static long delay = 100;
	private Shell shell;
	private ExpandableCompositeForTest ec;
	private Rectangle ecbounds;
	// change this to true if you want to see test is slow motion
	private boolean humanWatching = false;

	private class ExpandableCompositeForTest extends ExpandableComposite {
		private Control separator;
		private Control description;

		public ExpandableCompositeForTest(Composite parent, int style, int expansionStyle) {
			super(parent, style, expansionStyle);
		}

		public void setSeparatorControl(Control separator) {
			this.separator = separator;
		}

		@Override
		public Control getSeparatorControl() {
			return separator;
		}

		@Override
		public Control getDescriptionControl() {
			return description;
		}

		public void setDescriptionControl(Control description) {
			this.description = description;
		}
	}

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}

	}

	private static Point getTextExtend(String str) {
		GC gc = new GC(display);
		try {
			return gc.stringExtent(str);
		} finally {
			gc.dispose();
		}
	}

	@Override
	public void setUp() throws Exception {
		shell = new Shell(display);
		shell.setSize(600, 400);
		shell.setLayout(new GridLayout());
		shell.open();
	}

	@Override
	public void tearDown() throws Exception {
		if (humanWatching)
			dispatch(1000);
		shell.dispose();
	}

	private static void dispatch() {
		while (display.readAndDispatch()) {
		}
	}

	private static void dispatch(int msec) {
		long cur = System.currentTimeMillis();
		do {
			dispatch();
			long pass = System.currentTimeMillis() - cur;
			if (pass < msec) {
				// not doing display.sleep() because its automated tests,
				// nothing will cause any display events
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// ignore
				}
			} else
				break;
		} while (true);
	}

	private Composite rectangleComposite(final Composite parent, final int x, final int y) {
		return new Composite(parent, SWT.NONE) {
			@Override
			public Point computeSize(int wHint, int hHint, boolean changed) {
				return new Point(x, y);
			}
		};
	}

	private Composite createClient() {
		Composite client = rectangleComposite(ec, 100, 100);
		client.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
		ec.setClient(client);
		return client;
	}

	private Composite createTextClient(int w, int h) {
		Composite textClient = rectangleComposite(ec, w, h);
		textClient.setBackground(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
		ec.setTextClient(textClient);
		return textClient;
	}

	private Composite createSeparator(int w) {
		Composite sep = rectangleComposite(ec, w, 20);
		sep.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		ec.setSeparatorControl(sep);
		return sep;
	}

	private Composite createDescriptionControl(int w, int h) {
		Composite sep = rectangleComposite(ec, w, 20);
		sep.setBackground(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		ec.setDescriptionControl(sep);
		return sep;
	}

	public void createExtendableComposite(String text, int flags) {
		ec = new ExpandableCompositeForTest(shell, SWT.NONE, flags);
		ec.setText(text);
		ec.setBackground(display.getSystemColor(SWT.COLOR_RED));
		ec.addExpansionListener(new IExpansionListener() {

			@Override
			public void expansionStateChanging(ExpansionEvent e) {

			}

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				ec.getParent().layout(true);
			}
		});
	}

	private Rectangle update() {
		shell.layout(true, true);
		if (humanWatching)
			dispatch(300);
		else
			dispatch();
		ecbounds = ec.getBounds();

		return ecbounds;
	}

	@Test
	public void testExpCompNoClient() {
		createExtendableComposite(TEXT1, TITLE_TWIST);
		Rectangle bounds1 = update();
		ec.setExpanded(true);
		Rectangle bounds2 = update();
		assertEquals(bounds1, bounds2);

		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE);

		Rectangle bounds3 = update();
		assertTrue(bounds3.width < bounds2.width);
	}

	@Test
	public void testExpCompWithClient() {
		createExtendableComposite(TEXT1, TITLE_TWIST);
		createClient();
		Rectangle bounds1 = update();
		ec.setExpanded(true);
		Rectangle bounds2 = update();
		// our client is 100 tall + ver spacing
		bounds2.height -= 100 + ec.clientVerticalSpacing;

		assertEquals(bounds1, bounds2);

		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE);
		createClient();

		Rectangle bounds3 = update();
		assertTrue(bounds3.width < bounds2.width);

	}

	@Test
	public void testExpCompWithClientAndCompact() {
		createExtendableComposite(TEXT1, TITLE_TWIST);
		// no client
		Rectangle bounds1 = update();

		createExtendableComposite(TEXT1, TITLE_TWIST | ExpandableComposite.COMPACT);
		createClient(); // add client
		Rectangle bounds2 = update();

		assertTrue(bounds1.width == bounds2.width);

		ec.setExpanded(true); // now it should be bigger
		Rectangle bounds3 = update();
		assertTrue(bounds3.width > bounds2.width);
	}

	@Test
	public void testExpCompWithAndWithoutClientCompact() {
		createExtendableComposite(TEXT1, TITLE_TWIST | ExpandableComposite.COMPACT);
		// no client
		Rectangle bounds1 = update();

		createClient(); // client
		Rectangle bounds2 = update();

		assertEquals(bounds1, bounds2);
	}

	@Test
	public void testExpCompWithTextClient() {
		int fontSize = getTextExtend(TEXT1).y;
		final int SMALL_BOX_H = 3;
		final int BIG_BOX_H = fontSize * 2;
		final int BIG_W = 80;
		createExtendableComposite(TEXT1, TITLE_TWIST);
		Rectangle bounds1 = update();

		// text client height less then text height
		createExtendableComposite(TEXT1, TITLE_TWIST);
		createTextClient(BIG_W, SMALL_BOX_H);
		Rectangle bounds2 = update();
		assertTrue(bounds2.width >= bounds1.width + BIG_W);
		assertTrue(bounds2.height == bounds1.height);

		// text client height more then text height
		createExtendableComposite(TEXT1, TITLE_TWIST);
		createTextClient(BIG_W, BIG_BOX_H);
		Rectangle bounds3 = update();
		assertTrue(bounds2.width == bounds3.width);
		assertTrue(bounds3.height >= BIG_BOX_H);

		// text client height more then text height, left alignment
		createExtendableComposite(TEXT1, TITLE_TWIST | ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
		createTextClient(BIG_W, BIG_BOX_H);
		Rectangle bounds3l = update();
		assertTrue(bounds2.width == bounds3l.width);
		assertTrue(bounds3l.height >= BIG_BOX_H);

		// no title
		createExtendableComposite(TEXT1, TITLE_TWIST | ExpandableComposite.NO_TITLE);
		Rectangle bounds4 = update();
		assertTrue(bounds4.width < bounds1.width);
		assertTrue(bounds4.height < bounds1.height);

		// pure only toggle header
		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE);
		Rectangle boundsToggle = update();
		assertTrue(boundsToggle.width > 0);
		assertTrue(boundsToggle.height > 0);

		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE);
		createTextClient(BIG_W, SMALL_BOX_H); // text client is small
		Rectangle bounds5 = update();
		assertTrue(bounds5.width >= boundsToggle.width + BIG_W);
		assertTrue(bounds5.height == boundsToggle.height);

		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE
				| ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
		createTextClient(BIG_W, SMALL_BOX_H); // text client is small
		Rectangle bounds5l = update();
		assertTrue(bounds5l.width >= boundsToggle.width + BIG_W);
		assertTrue(bounds5l.height == boundsToggle.height);

		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE);
		createTextClient(BIG_W, BIG_BOX_H); // text client bigger then font size
											// and toggle
		Rectangle bounds6 = update();
		assertEquals(BIG_BOX_H, bounds6.height);

		ec.setExpanded(true);
		Rectangle bounds7 = update();
		assertEquals(bounds6, bounds7);

		// no toggle
		createExtendableComposite(TEXT1, ExpandableComposite.NO_TITLE);
		createTextClient(BIG_W, BIG_BOX_H);

		Rectangle bounds8 = update();
		assertEquals(BIG_BOX_H, bounds8.height);
		assertEquals(BIG_W + 4, bounds8.width); // +4 maybe a bug
	}

	@Test
	public void testExpCompWithTextSeparator() {
		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE);
		createSeparator(10);
		checkSeparator();
		ec.setExpanded(true);
		checkSeparator();

		// with client
		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE);
		createSeparator(10);
		createClient();
		checkSeparator();
		ec.setExpanded(true);
		checkSeparator();

		// with client and description
		createExtendableComposite(TEXT1, ExpandableComposite.TWISTIE);
		createSeparator(10);
		createDescriptionControl(50, 20);
		createClient();
		checkSeparator();
		ec.setExpanded(true);
		update();
		Rectangle bounds = ec.getBounds();
		Rectangle sepBounds = ec.getSeparatorControl().getBounds();
		assertTrue(sepBounds.width == bounds.width);

		Rectangle cb = ec.getClient().getBounds();
		Rectangle db = ec.getDescriptionControl().getBounds();
		assertEquals(bounds.height - db.height - 3 - cb.height - ec.clientVerticalSpacing,
				sepBounds.y + sepBounds.height);

	}

	public void checkSeparator() {
		update();
		Rectangle bounds = ec.getBounds();
		Rectangle sepBounds = ec.getSeparatorControl().getBounds();
		assertTrue(sepBounds.width == bounds.width);
		if (ec.isExpanded() && ec.getClient() != null) {
			Rectangle cb = ec.getClient().getBounds();
			assertEquals(bounds.height - cb.height - ec.clientVerticalSpacing, sepBounds.y + sepBounds.height);
		} else
			assertEquals(bounds.height, sepBounds.y + sepBounds.height);
	}
}
