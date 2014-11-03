/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.propertyPages;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The TableResizePropertyPage is an example of a property page with this resize
 * capability
 *
 * @since 3.3
 *
 */
public class TableResizePropertyPage extends PropertyPage {

	/**
	 * Constructor for TableResizePropertyPage.
	 */
	public TableResizePropertyPage() {
		super();
	}

	private void addFirstSection(Composite parent) {

		Composite enclosingComposite = new Composite(parent, SWT.NONE);
		enclosingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		enclosingComposite.setLayout(layout);

		Table table = new Table(enclosingComposite, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumnLayout tableLayout = new TableColumnLayout();

		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);

		table.setHeaderVisible(true);
		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText("Column 1");
		tableLayout.setColumnData(column, new ColumnWeightData(50, 100, true));

		column = new TableColumn(table, SWT.NULL);
		column.setText("Column 2");

		tableLayout.setColumnData(column,new ColumnWeightData(50, 100, true));
		enclosingComposite.setLayout(tableLayout);

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);

		return composite;
	}

	@Override
	public boolean performOk() {
		return true;
	}

}
