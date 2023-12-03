/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.labelProviders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.tests.viewers.ViewerTestCase;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.junit.Before;
import org.junit.Test;

/**
 * Most of the setup has been taken from
 * org.eclipse.jface.snippets.viewers.Snippet010OwnerDraw.java
 *
 * @since 3.4
 */
public class DecoratingStyledCellLabelProviderTest extends ViewerTestCase {

	// static ResourceManager resourceManager = PlatformUI.getWorkbench().
	private class TestCellLabelProvider extends CellLabelProvider implements IStyledLabelProvider, IFontProvider {

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			cell.setText((element == null) ? "" : element.toString());
			cell.setImage(getImage(element));
			cell.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
			cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE));
			cell.setBackground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
		}

		@Override
		public Image getImage(Object element) {
			// create a resource manager that holds onto images
			// OR create image set, dispose in teardown

			return fViewer.getControl().getDisplay().getSystemImage(SWT.ICON_WARNING);
		}

		@Override
		public StyledString getStyledText(Object element) {
			return new StyledString(element.toString(), StyledString.COUNTER_STYLER);
		}

		@Override
		public Font getFont(Object element) {
			return JFaceResources.getFont(JFaceResources.BANNER_FONT);
		}

	}

	private class TestLabelDecorator implements ILabelDecorator, IColorDecorator {

		@Override
		public Image decorateImage(Image image, Object element) {
			return image;
		}

		@Override
		public String decorateText(String text, Object element) {
			return text;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Color decorateBackground(Object element) {
			return fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
		}

		@Override
		public Color decorateForeground(Object element) {
			return fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE);
		}
	}

	private CountryEntry[] entries;
	protected String changeMe = "OLD";
	private static int COLUMN_COUNT = 3;

	@Before
	@Override
	public void setUp() {
		entries = new CountryEntry[3];
		entries[0] = new AustriaEntry();
		entries[1] = new GermanyEntry();
		entries[2] = new EnglandEntry();
		super.setUp();
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.FULL_SELECTION);

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return entries;
			}

			@Override
			public void inputChanged(org.eclipse.jface.viewers.Viewer viewer, Object oldInput, Object newInput) {
			}

		});
		createColumns(viewer);
		viewer.setLabelProvider(createLabelProvider());

		viewer.setSelection(new StructuredSelection(entries[1]));

		return viewer;
	}

	/**
	 * @return {@link DecoratingStyledCellLabelProvider}
	 */
	private DecoratingStyledCellLabelProvider createLabelProvider() {
		return new DecoratingStyledCellLabelProvider(new TestCellLabelProvider(), getDecorator(), getContext());
	}

	private static IDecorationContext getContext() {
		return new IDecorationContext() {

			@Override
			public String[] getProperties() {
				return null;
			}

			@Override
			public Object getProperty(String property) {
				return null;
			}
		};
	}

	private ILabelDecorator getDecorator() {
		return new TestLabelDecorator();
	}

	private ILabelProviderListener getListener() {
		return event -> changeMe = "been changed";
	}

	/**
	 * Create the columns to be used in the tree.
	 */
	private static void createColumns(TableViewer viewer) {
		TableLayout layout = new TableLayout();
		Table table = viewer.getTable();
		table.setLayout(layout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		for (int i = 0; i < COLUMN_COUNT; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			layout.addColumnData(new ColumnPixelData(100));
			tc.setText(getTitleFor(i));
		}
	}

	@Override
	protected void setInput() {
		fViewer.setInput(this);
	}

	// the tests
	@Test
	public void testGetDecorationContext() {
		assertNotNull(getDecoratingStyledLabelProvider().getDecorationContext());
	}

	@Test
	public void testSetDecorationContext() {
		try {
			getDecoratingStyledLabelProvider().setDecorationContext(null);
			fail("DecoratingStyledCellLabelProvider.setDecorationContext did not throw an exception when passed null");
		} catch (AssertionFailedException e) {
			// A Good Thing.
		}
	}

	@Test
	public void testUpdate() {
		Table table = ((TableViewer) fViewer).getTable();
		String before = table.getItem(0).toString();
		entries[0].name = "Updated";
		fViewer.refresh();
		assertNotSame(before, table.getItem(0).toString());
	}

	@Test
	public void testGetForeground() {
		// TODO: Incomplete test
		// fViewer.getControl().getShell().setFocus();
		//
		// long stopTime = System.currentTimeMillis() + 1000;
		// while (stopTime < System.currentTimeMillis()) {
		// Display.getCurrent().readAndDispatch();
		// }
		//
		// Table table = ((TableViewer) fViewer).getTable();
		//
		// TableItem ti = table.getItem(0);
		//
		// Color widget = ti.getForeground();
		// assertEquals(widget,
		// getDecoratingStyledLabelProvider().getForeground(
		// ti));
	}

	@Test
	public void testGetBackground() {
		// TODO: Incomplete test

		// Table table = ((TableViewer) fViewer).getTable();
		// TableItem ti = table.getItem(0);
		// Color d = ((DecoratingStyledCellLabelProvider) ((TableViewer)
		// fViewer)
		// .getLabelProvider(0)).getBackground(ti);
		// assertEquals(d,
		// getDecoratingStyledLabelProvider().getBackground(ti));
	}

	@Test
	public void testGetFont() {
		// TODO: Incomplete test

		// Table table = ((TableViewer) fViewer).getTable();
		// TableItem ti = table.getItem(0);

		// assertEquals(f, getDecoratingStyledLabelProvider().getFont(ti));
	}

	@Test
	public void testGetImage() {
		Table table = ((TableViewer) fViewer).getTable();

		assertEquals(table.getItem(0).getImage(), getDecoratingStyledLabelProvider().getImage(table.getItem(0)));

	}

	@Test
	public void testGetLabelDecorator() {
		assertNotNull(getDecoratingStyledLabelProvider().getLabelDecorator());

		getDecoratingStyledLabelProvider().setLabelDecorator(null);
		assertNull(getDecoratingStyledLabelProvider().getLabelDecorator());
	}

	@Test
	public void testSetLabelDecorator() {
		ILabelDecorator labelDecorator = getDecorator();
		getDecoratingStyledLabelProvider().setLabelDecorator(labelDecorator);
		assertEquals(labelDecorator, getDecoratingStyledLabelProvider().getLabelDecorator());

	}

	@Test
	public void testAddListener() {
		String old = changeMe; // String will change because the listener will
		// be listening for it
		ILabelProviderListener listener = getListener();
		getDecoratingStyledLabelProvider().addListener(listener);
		getDecoratingStyledLabelProvider().setLabelDecorator(getDecorator());
		assertNotSame(old, changeMe);
	}

	@Test
	public void testRemoveListener() {
		String old = changeMe = "OLD";
		ILabelProviderListener listener = getListener();
		getDecoratingStyledLabelProvider().addListener(listener);
		getDecoratingStyledLabelProvider().removeListener(listener);
		getDecoratingStyledLabelProvider().setLabelDecorator(getDecorator());
		assertEquals(old, changeMe);
	}

	@Test
	public void testIsLabelProperty() {
		boolean check = getDecoratingStyledLabelProvider().isLabelProperty("element", "property");
		assertTrue(check);
	}

	@Test
	public void testDispose() {
		fShell.dispose();
		assertFalse(fViewer.getLabelProvider() instanceof DecoratingStyledCellLabelProvider);
		// the viewer will return a new LabelProvider if the current is null
	}

	/**
	 * @return Returns the {@link DecoratingStyledCellLabelProvider} used for this
	 *         test
	 */
	private DecoratingStyledCellLabelProvider getDecoratingStyledLabelProvider() {
		return ((DecoratingStyledCellLabelProvider) fViewer.getLabelProvider());
	}

	class CountryEntry {

		String name;

		String cupYear;

		private final String baseName;

		/**
		 * Create a new instance of the receiver.
		 */
		CountryEntry(String countryName, String englishName, String worldCupYear) {
			name = countryName;
			cupYear = worldCupYear;
			baseName = englishName;
		}

		@Override
		public String toString() {
			return name + " " + cupYear + " " + baseName;
		}

		public int getHeight(Event event) {
			switch (event.index) {
			case 0:
				return event.gc.textExtent(name).y;
			case 1:
				return 50;
			case 2:
				return event.gc.textExtent(cupYear).y;
			default:
				return 10;
			}
		}

		public int getWidth(Event event) {

			switch (event.index) {
			case 0:
				return event.gc.textExtent(getDisplayString().toString()).x + 4;

			case 1:
				return 200;

			case 2:
				return event.gc.textExtent(cupYear).x + 5;

			default:
				return 10;
			}
		}

		/**
		 * Draw the flag in bounds.
		 */
		protected void drawFlag(Event event) {
			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE));

			Rectangle bounds = event.getBounds();
			bounds.width += 100;
			event.gc.fillRectangle(bounds);
		}

		/**
		 * Draw the cup year
		 */
		private void drawCupYear(Event event) {
			event.gc.drawText(cupYear, event.x, event.y);

		}

		/**
		 * Draw the name of the receiver.
		 */
		protected void drawName(Event event) {

			StringBuilder buffer = getDisplayString();

			Display display = fViewer.getControl().getDisplay();
			TextLayout layout = new TextLayout(display);
			layout.setText(buffer.toString());

			TextStyle plain = new TextStyle(JFaceResources.getFont(JFaceResources.DEFAULT_FONT),
					display.getSystemColor(SWT.COLOR_LIST_FOREGROUND),
					display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			TextStyle italic = new TextStyle(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT),
					display.getSystemColor(SWT.COLOR_BLUE), display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			layout.setStyle(plain, 0, name.length() - 1);
			layout.setStyle(italic, name.length(), buffer.length() - 1);

			layout.draw(event.gc, event.x, event.y);

			layout.dispose();

		}

		private StringBuilder getDisplayString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append(name);
			buffer.append(" (");
			buffer.append(baseName);
			buffer.append(")");
			return buffer;
		}

		public void draw(Event event) {

			switch (event.index) {
			case 0:
				drawName(event);
				break;
			case 1:
				drawFlag(event);
				break;
			case 2:
				drawCupYear(event);
				break;

			default:
				break;
			}

		}
	}

	private class GermanyEntry extends CountryEntry {

		GermanyEntry() {
			super("Deutschland", "Germany", "1990");
		}

		@Override
		protected void drawFlag(Event event) {

			Rectangle bounds = event.getBounds();
			bounds.width += 100;
			int stripeHeight = bounds.height / 3;
			Rectangle stripe = new Rectangle(bounds.x, bounds.y, bounds.width, stripeHeight);

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_BLACK));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_YELLOW));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(stripe);

		}

	}

	private class AustriaEntry extends CountryEntry {

		AustriaEntry() {
			super("\u00D6sterreich", "Austria", "TBD");
		}

		@Override
		protected void drawFlag(Event event) {

			Rectangle bounds = event.getBounds();
			bounds.width += 100;
			int stripeHeight = bounds.height / 3;
			Rectangle stripe = new Rectangle(bounds.x, bounds.y, bounds.width, stripeHeight);

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_WHITE));
			event.gc.fillRectangle(stripe);

			stripe.y += stripeHeight;

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(stripe);

		}
	}

	private class EnglandEntry extends CountryEntry {
		EnglandEntry() {
			super("Blighty", "England", "1966");
		}

		@Override
		protected void drawFlag(Event event) {

			Rectangle bounds = event.getBounds();
			bounds.width += 100;

			event.gc.setBackground(fViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_RED));
			event.gc.fillRectangle(new Rectangle(bounds.width / 2 + bounds.x - 5, bounds.y, 10, bounds.height));
			event.gc.fillRectangle(new Rectangle(bounds.x, bounds.height / 2 + bounds.y - 5, bounds.width, 10));

		}
	}

	private static String getTitleFor(int i) {
		switch (i) {
		case 0:
			return "Name";
		case 1:
			return "Flag";
		case 2:
			return "World Cup Year";
		}
		return "Unknown";
	}

}
