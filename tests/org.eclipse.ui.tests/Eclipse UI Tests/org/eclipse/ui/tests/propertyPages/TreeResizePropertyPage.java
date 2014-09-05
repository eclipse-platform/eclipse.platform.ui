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


import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * The TreeResizePropertyPage is an example of a property page with this resize
 * capability
 * 
 * @since 3.3
 * 
 */
public class TreeResizePropertyPage extends PropertyPage {

	/**
	 * Constructor for TableResizePropertyPage.
	 */
	public TreeResizePropertyPage() {
		super();
	}

	private void addFirstSection(Composite parent) {

		Composite enclosingComposite = new Composite(parent, SWT.NONE);
		enclosingComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		enclosingComposite.setLayout(layout);

		Tree tree = new Tree(enclosingComposite, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		tree.setLayoutData(data);
		
		TreeColumnLayout treeLayout = new TreeColumnLayout();

		tree.setHeaderVisible(true);
		TreeColumn column = new TreeColumn(tree, SWT.NULL);
		column.setText("Column 1");
		treeLayout.setColumnData(column, new ColumnWeightData(50, 100, true));

		column = new TreeColumn(tree, SWT.NULL);
		column.setText("Column 2");
		treeLayout.setColumnData(column, new ColumnWeightData(50, 100, true));

		enclosingComposite.setLayout(treeLayout);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		return true;
	}

}
