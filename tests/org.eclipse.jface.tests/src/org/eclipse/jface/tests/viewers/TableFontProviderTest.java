/*******************************************************************************
 * Copyright (c) 2004, 2023 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.tests.viewers.TableViewerTest.TableTestLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The TableFontProviderTest is a test suite designed to test
 * ITableFontProviders.
 */
public class TableFontProviderTest extends StructuredViewerTest {
	Font font1;

	Font font2;

	/**
	 * Create a new instance of the receiver
	 *
	 * @param name
	 */
	public TableFontProviderTest(String name) {
		super(name);
	}

	/**
	 * Test the general font provider.
	 */
	public void testFontProvider() {
		TableViewer viewer = (TableViewer) fViewer;

		FontViewLabelProvider provider = new FontViewLabelProvider();
		viewer.setLabelProvider(provider);

		// refresh so that the colors are set
		fViewer.refresh();

		compareFontDatas(viewer.getTable().getItem(0).getFont(0), font1);
		compareFontDatas(viewer.getTable().getItem(0).getFont(1), font1);

		provider.fExtended = false;

	}

	/**
	 * Test that the fonts are being set.
	 *
	 */
	public void testTableItemsFontProvider() {
		TableViewer viewer = (TableViewer) fViewer;

		TableFontViewLabelProvider provider = new TableFontViewLabelProvider();
		viewer.setLabelProvider(provider);

		Table table = viewer.getTable();

		fViewer.refresh();

		compareFontDatas(font1, table.getItem(0).getFont(0));
		compareFontDatas(font2, table.getItem(0).getFont(1));
		provider.fExtended = false;

	}

	@Override
	public void setUp() {
		super.setUp();
		font1 = JFaceResources.getFont(JFaceResources.BANNER_FONT);
		font2 = JFaceResources.getFont(JFaceResources.HEADER_FONT);

	}

	/**
	 * Run as a stand alone test
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TableFontProviderTest.class);
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent);
		viewer.setContentProvider(new TestModelContentProvider());
		viewer.getTable().setLinesVisible(true);

		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		String headers[] = { "column 1 header", "column 2 header" };//$NON-NLS-1$ //$NON-NLS-2$

		ColumnLayoutData layouts[] = { new ColumnWeightData(100), new ColumnWeightData(100) };

		final TableColumn columns[] = new TableColumn[headers.length];

		for (int i = 0; i < headers.length; i++) {
			layout.addColumnData(layouts[i]);
			TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
			tc.setResizable(layouts[i].resizable);
			tc.setText(headers[i]);
			columns[i] = tc;
		}
		return viewer;
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		TableItem ti = (TableItem) fViewer.testFindItem(first);
		Table table = ti.getParent();
		return table.getItemCount();
	}

	@Override
	protected String getItemText(int at) {
		Table table = (Table) fViewer.getControl();
		return table.getItem(at).getText();
	}

	private static void compareFontDatas(Font font1, Font font2) {

		FontData[] font1Data = font1.getFontData();
		FontData[] font2Data = font2.getFontData();

		assertEquals("Mismatched sizes", font1Data.length, font2Data.length);
		for (int a = 0; a < font2Data.length; a++) {
			assertEquals("Mismatched fontData", font1Data[a], font2Data[a]);
		}

	}

	class TableFontViewLabelProvider extends TableTestLabelProvider implements ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int index) {
			return null;
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return font1;

			default:
				return font2;
			}
		}

	}

	/**
	 * A label provider that does not provide font support entry by entry.
	 */
	class FontViewLabelProvider extends TableTestLabelProvider implements IFontProvider {
		@Override
		public Font getFont(Object element) {
			return font1;
		}
	}

}
