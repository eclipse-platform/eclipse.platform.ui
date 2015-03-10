/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.performance;

import org.eclipse.jface.tests.performance.JFacePerformanceSuite;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IColorDecorator;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

/**
 * Test scrolling performance with various label styles
 * @since 3.5
 */
public class LabelProviderTest extends BasicPerformanceTest {

	private class CountryEntry {
		private String name;
		private String cupYear;
		private String baseName;

		// Those are an OS resources and should not be freed
		private Image image;
		private Color bkColor;
		private Color fgColor;

		public CountryEntry(Display display, int i) {
			name = "\u00D6sterreich";
			cupYear = "Austria";
			baseName = Integer.toString(i + 100);

			switch (i % 3) {
				case 0:
					image = display.getSystemImage(SWT.ICON_WARNING);
					bkColor = display.getSystemColor(SWT.COLOR_BLUE);
					fgColor = display.getSystemColor(SWT.COLOR_RED);
					break;
				case 1:
					image = display.getSystemImage(SWT.ICON_ERROR);
					bkColor = display.getSystemColor(SWT.COLOR_GREEN);
					fgColor = display.getSystemColor(SWT.COLOR_BLUE);
					break;
				case 2:
					image = display.getSystemImage(SWT.ICON_QUESTION);
					bkColor = display.getSystemColor(SWT.COLOR_RED);
					fgColor = display.getSystemColor(SWT.COLOR_GREEN);
					break;
			}
		}

		public Image getImage() {
			return image;
		}

		public Color getBackgroundColor() {
			return bkColor;
		}

		public Color getForegroundColor() {
			return fgColor;
		}

		public String toString() {
			return name + " " + cupYear + " " + baseName;
		}
	}

	private class TestCellLabelProvider extends CellLabelProvider implements IStyledLabelProvider, IFontProvider {

		private boolean useColor;

		public TestCellLabelProvider(boolean useColor) {
			this.useColor = useColor;
		}

		public void update(ViewerCell cell) {
			// NOTE: this method is not called in the current performance
			// test so its contents has no effect on the performance results.
			Object element = cell.getElement();
			if (!(element instanceof CountryEntry))
					return;
			cell.setText(element.toString());
			cell.setImage(getImage(element));
			if (useColor) {
				cell.setForeground(((CountryEntry)element).getForegroundColor());
				cell.setBackground(((CountryEntry)element).getBackgroundColor());
			}
		}

		public Image getImage(Object element) {
			if (element instanceof CountryEntry)
				return ((CountryEntry)element).getImage();
			return null;
		}

		public StyledString getStyledText(Object element) {
			return new StyledString(element.toString(), useColor ? StyledString.COUNTER_STYLER : null);
		}

		public Font getFont(Object element) {
			return null;
		}
	}

	private static final int ITEM_COUNT = 2000;
	private static final int ITERATIONS = 5;
	private static final int MIN_ITERATIONS = 5;

	private CountryEntry[] entries;
	private Shell fShell;
	private StructuredViewer fViewer;

	private boolean styled;
	private boolean colors;

	/**
	 * @param styled <code>true</code to use DecoratingStyledCellLabelProvider
	 * @param colors Run test with color on or off
	 */
	public LabelProviderTest(String testName, boolean styled, boolean colors) {
		super(testName);
		this.styled = styled;
		this.colors = colors;
	}

    protected void runTest() throws Throwable {
		if (styled)
			fViewer.setLabelProvider(getDecoratingStyledCellLabelProvider(colors));
		else
			fViewer.setLabelProvider(getDecoratingLabelProvider(colors));

		final Tree tree = ((TreeViewer) fViewer).getTree();
		fShell.setFocus();

		exercise(new TestRunnable() {
			public void run() {
				startMeasuring();
				for (int i = 0; i < ITEM_COUNT / 5; i++) {
					tree.setTopItem(tree.getItem(i * 5));
					processEvents();
				}
				stopMeasuring();
			}
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	protected StructuredViewer createViewer(Shell parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.FULL_SELECTION);

		viewer.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				return entries;
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				return false;
			}

			public Object[] getElements(Object inputElement) {
				return entries;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});

		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(data);
		viewer.setSelection(new StructuredSelection(entries[1]));
		return viewer;
	}

	protected void doSetUp() throws Exception {
		super.doSetUp();

		Display display = Display.getCurrent();
		if (display == null)
			display = new Display();

		entries = new CountryEntry[ITEM_COUNT];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = new CountryEntry(display, i);
		}

		fShell = new Shell(display);
		fShell.setSize(500, 500);
		fShell.setLayout(new FillLayout());
		fViewer = createViewer(fShell);
		fViewer.setUseHashlookup(true);
		fViewer.setInput(this);
		fShell.open();
	}

	protected void doTearDown() throws Exception {
		super.doTearDown();
		if (fShell != null) {
			fShell.close();
			fShell = null;
		}
	}

	private DecoratingStyledCellLabelProvider getDecoratingStyledCellLabelProvider(boolean useColor) {
		// create our own context to avoid using default context
		IDecorationContext context = new IDecorationContext() {
				public String[] getProperties() {
					return null;
				}
				public Object getProperty(String property) {
					return null;
				}
			};
		return new DecoratingStyledCellLabelProvider(
				new TestCellLabelProvider(useColor), useColor ? getDecorator() : null, context);
	}

	private ILabelDecorator getDecorator() {
		return new TestLabelDecorator();
	}

	private class TestLabelDecorator implements ILabelDecorator, IColorDecorator {

		public Image decorateImage(Image image, Object element) {
			return image;
		}

		public String decorateText(String text, Object element) {
			return text;
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Color decorateBackground(Object element) {
			if (element instanceof CountryEntry)
				return ((CountryEntry)element).getBackgroundColor();
			return null;
		}

		public Color decorateForeground(Object element) {
			if (element instanceof CountryEntry)
				return ((CountryEntry)element).getForegroundColor();
			return null;
		}
	}

	DecoratingLabelProvider getDecoratingLabelProvider(boolean useColor) {
		return new DecoratingLabelProvider(new ILabelProvider() {

			public Image getImage(Object element) {
				if (element instanceof CountryEntry)
					return ((CountryEntry)element).getImage();
				return null;
			}

			public String getText(Object element) {
				return element.toString();
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}
		}, useColor ? getDecorator() : null);
	}
}
