/*******************************************************************************
 * Copyright (c) 2015, 2017 QNX Software Systems and others.
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
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.tests.forms.layout.ControlFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for expandable composite
 */
public class ExpandableCompositeTest {
	private static final int SHORT_CONTROL_WIDTH = 58;

	private int defaultFlags = ExpandableComposite.TWISTIE;
	private static Display display;
	private Shell shell;
	private ExpandableCompositeForTest ec;
	private Rectangle ecbounds;
	private static String shortText = "Hedgehog";
	private static String longText = "A hedgehog is any of the spiny mammals of the subfamily Erinaceinae, in the order Erinaceomorpha. " //$NON-NLS-1$
			+ "There are seventeen species of hedgehog in five genera, found through parts of Europe, Asia, Africa and New Zealand. " //$NON-NLS-1$
	;
	private Font font;
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

	private Point getTextExtend(String str) {
		GC gc = new GC(display);
		gc.setFont(font);
		try {
			return gc.stringExtent(str);
		} finally {
			gc.dispose();
		}
	}

	@Before
	public void setUp() throws Exception {
		font = new Font(display, "Arial", 12, SWT.NORMAL);
		shell = new Shell(display);
		shell.setSize(600, 400);
		GridLayoutFactory.fillDefaults().applyTo(shell);
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
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			} else
				break;
		} while (true);
	}

	private Composite rectangleComposite(final Composite parent, final int x, final int y) {
		return ControlFactory.create(parent, x, y);
	}

	private Composite createClient() {
		Composite client = rectangleComposite(ec, 200, 100);
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

	private void createExtendableComposite(String text, int flags) {
		ec = new ExpandableCompositeForTest(shell, SWT.NONE, flags);
		ec.setFont(font);
		ec.setText(text);
		ec.setBackground(display.getSystemColor(SWT.COLOR_GRAY));
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
		createExtendableComposite(shortText, defaultFlags | ExpandableComposite.TITLE_BAR);
		Rectangle bounds1 = update();
		ec.setExpanded(true);

		Rectangle bounds2 = update();
		assertTrue(bounds1.width == bounds2.width);
		assertEquals(bounds1, bounds2);

		createExtendableComposite(shortText, defaultFlags);

		Rectangle bounds3 = update();
		assertTrue(bounds3.width < bounds2.width);
	}

	@Test
	public void testExpCompWithClient() {
		createExtendableComposite(shortText, defaultFlags | ExpandableComposite.TITLE_BAR);
		createClient();
		Rectangle bounds1 = update();
		ec.setExpanded(true);
		Rectangle bounds2 = update();
		// our client is 100 tall + ver spacing
		bounds2.height -= 100 + ec.clientVerticalSpacing;

		assertEquals(bounds1, bounds2);

		createExtendableComposite(shortText, defaultFlags);
		createClient();

		Rectangle bounds3 = update();
		assertTrue(bounds3.width < bounds2.width);

	}

	@Test
	public void testExpCompWithClientAndCompact() {
		createExtendableComposite(shortText, defaultFlags);
		// no client
		Rectangle bounds1 = update();

		createExtendableComposite(shortText, defaultFlags | ExpandableComposite.COMPACT);
		createClient(); // add client
		Rectangle bounds2 = update();

		assertTrue(bounds1.width == bounds2.width);

		ec.setExpanded(true); // now it should be bigger
		Rectangle bounds3 = update();
		assertTrue(bounds3.width > bounds2.width);
	}

	@Test
	public void testExpCompWithAndWithoutClientCompact() {
		createExtendableComposite(shortText, defaultFlags | ExpandableComposite.COMPACT);
		// no client
		Rectangle bounds1 = update();

		createClient(); // client
		Rectangle bounds2 = update();

		assertEquals(bounds1, bounds2);
	}

	@Test
	public void testExpCompWithTextClient() {
		int fontSize = getTextExtend(shortText).y;
		final int SMALL_BOX_H = 3;
		final int BIG_BOX_H = fontSize * 2;
		final int BIG_W = 80;
		createExtendableComposite(shortText, defaultFlags);
		Rectangle bounds1 = update();

		// text client height less then text height
		createExtendableComposite(shortText, defaultFlags);
		createTextClient(BIG_W, SMALL_BOX_H);
		Rectangle bounds2 = update();
		assertTrue(bounds2.width >= bounds1.width + BIG_W);
		assertTrue(bounds2.height == bounds1.height);

		// text client height more then text height
		createExtendableComposite(shortText, defaultFlags);
		createTextClient(BIG_W, BIG_BOX_H);
		Rectangle bounds3 = update();
		assertTrue(bounds2.width == bounds3.width);
		assertTrue(bounds3.height >= BIG_BOX_H);

		// text client height more then text height, left alignment
		createExtendableComposite(shortText, defaultFlags | ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
		createTextClient(BIG_W, BIG_BOX_H);
		Rectangle bounds3l = update();
		assertTrue(bounds2.width == bounds3l.width);
		assertTrue(bounds3l.height >= BIG_BOX_H);

		// no title
		createExtendableComposite(shortText, defaultFlags | ExpandableComposite.NO_TITLE);
		Rectangle bounds4 = update();
		assertTrue(bounds4.width < bounds1.width);
		assertTrue(bounds4.height < bounds1.height);

		// pure only toggle header
		createExtendableComposite(shortText, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE);
		Rectangle boundsToggle = update();
		assertTrue(boundsToggle.width > 0);
		assertTrue(boundsToggle.height > 0);

		createExtendableComposite(shortText, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE);
		createTextClient(BIG_W, SMALL_BOX_H); // text client is small
		Rectangle bounds5 = update();
		assertTrue(bounds5.width >= boundsToggle.width + BIG_W);
		assertTrue(bounds5.height == boundsToggle.height);

		createExtendableComposite(shortText, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE
				| ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
		createTextClient(BIG_W, SMALL_BOX_H); // text client is small
		Rectangle bounds5l = update();
		assertTrue(bounds5l.width >= boundsToggle.width + BIG_W);
		assertTrue(bounds5l.height == boundsToggle.height);

		createExtendableComposite(shortText, ExpandableComposite.TWISTIE | ExpandableComposite.NO_TITLE);
		createTextClient(BIG_W, BIG_BOX_H); // text client bigger then font size
											// and toggle
		Rectangle bounds6 = update();
		assertEquals(BIG_BOX_H, bounds6.height);

		ec.setExpanded(true);
		Rectangle bounds7 = update();
		assertEquals(bounds6, bounds7);

		// no toggle
		createExtendableComposite(shortText, ExpandableComposite.NO_TITLE);
		createTextClient(BIG_W, BIG_BOX_H);

		Rectangle bounds8 = update();
		assertEquals(BIG_BOX_H, bounds8.height);
		assertEquals(BIG_W, bounds8.width);
	}

	@Test
	public void testExpCompWithTextSeparator() {
		createExtendableComposite(shortText, ExpandableComposite.TWISTIE);
		createSeparator(10);
		checkSeparator();
		ec.setExpanded(true);
		checkSeparator();

		// with client
		createExtendableComposite(shortText, ExpandableComposite.TWISTIE);
		createSeparator(10);
		createClient();
		checkSeparator();
		ec.setExpanded(true);
		checkSeparator();

		// with client and description
		createExtendableComposite(shortText, ExpandableComposite.TWISTIE);
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

	private void checkSeparator() {
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

	private void width500() {
		GridData layoutData = new GridData();
		layoutData.widthHint = 500;
		ec.setLayoutData(layoutData);
	}

	@Test
	public void testLabelLong() {
		createExtendableComposite(longText, 0);
		width500();
		Rectangle bounds = update();
		assertEquals(500, bounds.width);
		assertTextLines(4, bounds);
	}

	@Test
	public void testLinkLong() {
		createExtendableComposite(longText, ExpandableComposite.FOCUS_TITLE);
		width500();
		Rectangle bounds = update();
		assertAround("Width", 500, bounds.width, 8);
		assertTextLines(4, bounds);
	}

	private void assertTextLines(int lines, Rectangle bounds) {
		Point textExtend = getTextExtend(shortText);
		// it will be around "lines" lines of text
		assertAround("Expected " + lines + " lines of text", (textExtend.y * lines), bounds.height, textExtend.y * 2);
	}

	private Label createLabel(Composite comp, String text) {
		Label l = new Label(comp, SWT.WRAP);
		l.setText(text);
		l.setFont(font);
		return l;
	}

	@Test
	public void testLabelLongAndTextClientLabel() {
		createExtendableComposite(longText, 0);
		width500();

		Label client = createLabel(ec, longText);
		ec.setTextClient(client);

		Rectangle bounds = update();
		assertEquals(500, bounds.width);
		assertAround("Text Client Width", 500 / 2, client.getBounds().width, 3);
		assertTextLines(7, bounds);
	}

	private Composite createComposite(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackground(comp.getDisplay().getSystemColor(SWT.COLOR_MAGENTA));
		return comp;
	}

	private Composite createFillComp(Composite parent) {
		Composite comp = createComposite(parent);
		GridLayoutFactory.fillDefaults().applyTo(comp);
		Label l = createLabel(comp, longText);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING).applyTo(l);
		return comp;
	}

	private Composite createFixedComp(Composite parent) {
		Composite comp = createComposite(parent);
		GridLayoutFactory.fillDefaults().applyTo(comp);
		Control control = ControlFactory.create(comp, SHORT_CONTROL_WIDTH, 15);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(control);
		return comp;
	}

	@Test
	public void testLabelLongAndTextClientComp() {
		createExtendableComposite(longText, 0);
		width500();

		Control client = createFillComp(ec);
		ec.setTextClient(client);

		Rectangle bounds = update();

		assertEquals("Width", 500, bounds.width);
		assertAround("Text Client width", 500 / 2, client.getBounds().width, 3);
		assertTextLines(7, bounds);
	}

	@Test
	public void testLabelShortAndTextClientComp() {
		createExtendableComposite(shortText, 0);
		width500();

		Control client = createFillComp(ec);
		ec.setTextClient(client);

		Rectangle bounds = update();

		assertEquals("Width", 500, bounds.width);
		int w = getTextExtend(shortText).x;
		assertAround("Text Client width", 500 - w, client.getBounds().width, 8);
		assertTextLines(4, bounds);
	}

	private void assertAround(String prefix, int len1, int len2, int delta) {
		assertTrue(prefix + ": expected around " + len1 + " pixes +/- " + delta + " but was " + len2,
				len1 - delta <= len2 && len2 <= len1 + delta);
	}

	@Test
	public void testLabelLongAndTextClientCompFixed() {
		createExtendableComposite(longText, 0);
		width500();

		Control client = createFixedComp(ec);
		ec.setTextClient(client);

		Rectangle bounds = update();

		assertEquals(SHORT_CONTROL_WIDTH, client.getBounds().width);
		assertTextLines(4, bounds);
		assertAround("Width", 500, bounds.width, 8);
	}

	@Test
	public void testLabelLongAndTextClientCompFixedL() {
		createExtendableComposite(longText, ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
		width500();

		Control client = createFixedComp(ec);
		ec.setTextClient(client);

		Rectangle bounds = update();

		// not sure +8
		assertAround("Text Client width", SHORT_CONTROL_WIDTH, client.getBounds().width, 8);
		assertTextLines(4, bounds);
		assertAround("Width", 500, bounds.width, 2);
	}

	@Test
	public void testTwistieIsVerticallyCentered() {
		createExtendableComposite(shortText,
				ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT | ExpandableComposite.TWISTIE);
		width500();
		update();

		Control[] children = ec.getChildren();

		int textCenter = Geometry.centerPoint(children[1].getBounds()).y;
		int twistieCenter = Geometry.centerPoint(children[0].getBounds()).y;

		assertAround("Twisty position", textCenter, twistieCenter, 1);
	}
}
