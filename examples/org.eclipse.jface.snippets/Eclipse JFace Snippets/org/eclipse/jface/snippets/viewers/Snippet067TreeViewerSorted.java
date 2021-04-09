/*******************************************************************************
 * Copyright (c) 2019 Red Hat and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Bug 443717
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

public class Snippet067TreeViewerSorted {
	private static class MyContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).children.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}
			return ((MyModel) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			return !((MyModel) element).children.isEmpty();
		}

	}

	public static class MyModel {
		private MyModel parent;
		private List<MyModel> children;
		private String name;
		private int populationSize;
		private int category = 0;

		public MyModel(String locationName, int populationSize, MyModel parent) {
			this.parent = parent;
			this.children = new ArrayList<>();
			this.name = locationName;
			this.populationSize = populationSize;
		}

		public void addChild(MyModel child) {
			this.children.add(child);
		}

		public void setCategory(int category) {
			this.category = category;
		}

		public int getPopulationSize() {
			return this.populationSize;
		}

		public int getCategory() {
			return this.category;
		}

		@Override
		public String toString() {
			return this.name + ": " + NumberFormat.getInstance().format(this.populationSize);
		}

	}

	public Snippet067TreeViewerSorted(Shell shell, Composite composite) {
		final TreeViewer v = new TreeViewer(shell);
		v.setLabelProvider(new LabelProvider());
		v.setComparator(new MyComparator());
		v.setContentProvider(new MyContentProvider());
		createColumn(v.getTree(), "Values");
		v.setInput(createModel());

		// Combo
		FormData comboFormData = new FormData(300, 50);
		comboFormData.left = new FormAttachment(0, 30);
		comboFormData.right = new FormAttachment(100, -30);
		comboFormData.bottom = new FormAttachment(100, -30);

		Combo changeSortingCombo = new Combo(composite, SWT.READ_ONLY);
		changeSortingCombo.setToolTipText("Change Sorting Method");
		changeSortingCombo.setLayoutData(comboFormData);
		changeSortingCombo.add("Sort Method: Descending by Population");
		changeSortingCombo.add("Sort Method: Comparator (Categories)");
		changeSortingCombo.select(0);

		changeSortingCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = changeSortingCombo.getSelectionIndex();
				if (selectedIndex == 0) {
					v.setComparator(new MyComparator());
					v.setLabelProvider(new DefaultLabelProvider());
				} else if (selectedIndex == 1) {
					v.setComparator(new MyCategoryComparator());
					v.setLabelProvider(new MyLabelProvider());
				}
				v.update(v, null);
				shell.layout(true, true);

			}
		});

		// Category Legend
		Composite legendComposite = new Composite(composite, SWT.NONE);
		final TableViewer legendV = new TableViewer(legendComposite);
		FormData legendFormData = new FormData(300, 300);
		legendFormData.right = new FormAttachment(100, -30);
		legendFormData.left = new FormAttachment(0, 30);
		legendFormData.top = new FormAttachment(0, 30);
		legendFormData.bottom = new FormAttachment(changeSortingCombo, -30);

		legendComposite.setLayoutData(legendFormData);
		legendComposite.setLayout(new FillLayout());
		legendV.setLabelProvider(new LegendLabelProvider());
		legendV.setContentProvider(ArrayContentProvider.getInstance());
		final String[] legendCategories = { "Population > 1,000,000", "Population > 500,000", "Population > 250,000",
				"Population > 50,000", "Population > 25,000", "Population > 5000", "Population <= 5000" };
		legendV.setInput(legendCategories);
		legendV.update(legendV, null);

		// Legend label
		Label legendLabel = new Label(composite, SWT.BORDER);
		legendLabel.setSize(50, 25);
		legendLabel.setText("Legend");
		FormData labelFormData = new FormData(50, 25);
		labelFormData.bottom = new FormAttachment(legendComposite, 0);
		labelFormData.right = new FormAttachment(legendComposite, 300);
		legendLabel.setLayoutData(labelFormData);

	}

	public static class DefaultLabelProvider extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider, ITableColorProvider {
		FontRegistry registry = new FontRegistry();

		@Override
		public Color getForeground(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			return registry.defaultFont();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
	}

	public static class MyLabelProvider extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider, ITableColorProvider {
		FontRegistry registry = new FontRegistry();

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			if (((MyModel) element).getCategory() == 0) {
				return registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
			}

			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			int category = ((MyModel) element).getCategory();
			Display display = Display.getCurrent();
			if (category == 0) {
				return display.getSystemColor(SWT.COLOR_RED);
			} else if (category == 1) {
				return display.getSystemColor(SWT.COLOR_GRAY);
			} else if (category == 2) {
				return display.getSystemColor(SWT.COLOR_YELLOW);
			} else if (category == 3) {
				return display.getSystemColor(SWT.COLOR_BLUE);
			} else if (category == 4) {
				return display.getSystemColor(SWT.COLOR_GREEN);
			} else if (category == 5) {
				return display.getSystemColor(SWT.COLOR_CYAN);
			} else if (category == 6) {
				return display.getSystemColor(SWT.COLOR_WHITE);
			}

			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			int category = ((MyModel) element).getCategory();
			if (category == 0 || category == 3) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
			}
			return null;
		}

	}

	public static class LegendLabelProvider extends LabelProvider
			implements IBaseLabelProvider, ITableFontProvider, ITableColorProvider {
		FontRegistry registry = new FontRegistry();

		@Override
		public Font getFont(Object element, int columnIndex) {
			return registry.getBold(Display.getCurrent().getSystemFont().getFontData()[0].getName());
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			String legendItemText = (String) element;
			Display display = Display.getCurrent();
			switch (legendItemText) {
			case "Population > 1,000,000":
				return display.getSystemColor(SWT.COLOR_RED);
			case "Population > 500,000":
				return display.getSystemColor(SWT.COLOR_GRAY);
			case "Population > 250,000":
				return display.getSystemColor(SWT.COLOR_YELLOW);
			case "Population > 50,000":
				return display.getSystemColor(SWT.COLOR_BLUE);
			case "Population > 25,000":
				return display.getSystemColor(SWT.COLOR_GREEN);
			case "Population > 5000":
				return display.getSystemColor(SWT.COLOR_CYAN);
			case "Population <= 5000":
				return display.getSystemColor(SWT.COLOR_WHITE);
			default:
				break;
			}

			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			String legendItemText = (String) element;
			if (legendItemText.equals("Population > 1,000,000") || legendItemText.equals("Population > 50,000")) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
			}
			return null;
		}

	}

	public static class MyCategoryComparator extends ViewerComparator {
		@Override
		public int category(Object element) {
			MyModel location = (MyModel) element;
			int populationSize = location.getPopulationSize();

			int category = 6;

			if (populationSize > 5000) {
				category = 5;
			}

			if (populationSize > 25000) {
				category = 4;
			}

			if (populationSize > 50000) {
				category = 3;
			}

			if (populationSize > 250000) {
				category = 2;
			}

			if (populationSize > 500000) {
				category = 1;
			}
			if (populationSize > 1000000) {
				category = 0;
			}

			location.setCategory(category);

			return category;

		}
	}

	public static class MyComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			MyModel location1 = (MyModel) e1;
			MyModel location2 = (MyModel) e2;
			return Integer.compare(location2.getPopulationSize(), location1.getPopulationSize());
		}
	}

	public void createColumn(Tree tr, String text) {
		TreeColumn column = new TreeColumn(tr, SWT.NONE);
		column.setWidth(100);
		column.setText(text);
		tr.setHeaderVisible(true);
	}

	private MyModel createModel() {

		MyModel root = new MyModel("Canada", 35151728, null);

		MyModel provinceTree1 = new MyModel("Ontario", 14446515, root);
		provinceTree1.addChild(new MyModel("Toronto", 2731571, provinceTree1));
		provinceTree1.addChild(new MyModel("Ottawa", 934243, provinceTree1));
		provinceTree1.addChild(new MyModel("Mississauga", 721599, provinceTree1));

		MyModel provinceTree2 = new MyModel("Quebec", 8433301, root);
		provinceTree2.addChild(new MyModel("Montréal", 3407963, provinceTree2));
		provinceTree2.addChild(new MyModel("Québec", 661011, provinceTree2));
		provinceTree2.addChild(new MyModel("Ottawa (Gatineau) ", 236329, provinceTree2));

		MyModel provinceTree3 = new MyModel("Nova Scotia", 965382, root);
		provinceTree3.addChild(new MyModel("Halifax", 297943, provinceTree3));
		provinceTree3.addChild(new MyModel("Sydney", 31597, provinceTree3));
		provinceTree3.addChild(new MyModel("Truro", 23261, provinceTree3));

		MyModel provinceTree4 = new MyModel("New Brunswick", 772094, root);
		provinceTree4.addChild(new MyModel("Saint John", 67575, provinceTree4));
		provinceTree4.addChild(new MyModel("Moncton", 71889, provinceTree4));
		provinceTree4.addChild(new MyModel("Dieppe", 11897, provinceTree4));

		MyModel provinceTree5 = new MyModel("Manitoba", 1360396, root);
		provinceTree5.addChild(new MyModel("Winnipeg", 705244, provinceTree5));
		provinceTree5.addChild(new MyModel("Brandon", 48859, provinceTree5));
		provinceTree5.addChild(new MyModel("Steinbach", 15829, provinceTree5));

		MyModel provinceTree6 = new MyModel("British Columbia", 5020302, root);
		provinceTree6.addChild(new MyModel("Vancouver", 603502, provinceTree6));
		provinceTree6.addChild(new MyModel("Surrey", 468251, provinceTree6));
		provinceTree6.addChild(new MyModel("Burnaby", 224218, provinceTree6));

		MyModel provinceTree7 = new MyModel("Prince Edward Island", 154748, root);
		provinceTree7.addChild(new MyModel("Charlottetown", 36094, provinceTree7));
		provinceTree7.addChild(new MyModel("Summerside", 14829, provinceTree7));
		provinceTree7.addChild(new MyModel("Alberton", 1145, provinceTree7));

		MyModel provinceTree8 = new MyModel("Saskatchewan", 1168423, root);
		provinceTree8.addChild(new MyModel("Saskatoon", 246376, provinceTree8));
		provinceTree8.addChild(new MyModel("Regina", 215106, provinceTree8));
		provinceTree8.addChild(new MyModel("Prince Albert", 35926, provinceTree8));

		MyModel provinceTree9 = new MyModel("Alberta", 4345737, root);
		provinceTree9.addChild(new MyModel("Calgary", 1239220, provinceTree9));
		provinceTree9.addChild(new MyModel("Edmonton", 932546, provinceTree9));
		provinceTree9.addChild(new MyModel("Red Deer", 100418, provinceTree9));

		MyModel provinceTree10 = new MyModel("Newfoundland and Labrador", 523790, root);
		provinceTree10.addChild(new MyModel("St John's", 108860, provinceTree10));
		provinceTree10.addChild(new MyModel("Conception Bay South", 26199, provinceTree10));
		provinceTree10.addChild(new MyModel("Mount Pearl", 231210, provinceTree10));

		root.addChild(provinceTree1);
		root.addChild(provinceTree2);
		root.addChild(provinceTree3);
		root.addChild(provinceTree4);
		root.addChild(provinceTree5);
		root.addChild(provinceTree6);
		root.addChild(provinceTree7);
		root.addChild(provinceTree8);
		root.addChild(provinceTree9);
		root.addChild(provinceTree10);

		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FormLayout());
		new Snippet067TreeViewerSorted(shell, composite);
		shell.layout(true, true);

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

}
