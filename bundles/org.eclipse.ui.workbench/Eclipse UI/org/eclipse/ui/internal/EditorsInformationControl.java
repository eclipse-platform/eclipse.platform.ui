/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class EditorsInformationControl
	extends AbstractTableInformationControl {

	public EditorsInformationControl(
		Shell parent,
		int shellStyle,
		int treeStyle) {
		super(parent, shellStyle, treeStyle);
		setBackgroundColor(new Color(parent.getDisplay(), 255, 255, 255));
	}

	protected TableViewer createTableViewer(Composite parent, int style) {
		Table table = new Table(parent, SWT.SINGLE | (style & ~SWT.MULTI));
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableViewer tableViewer = new TableViewer(table);

		tableViewer.addFilter(new NamePatternFilter());

		tableViewer.setContentProvider(new EditorsContentProvider());
		tableViewer.setSorter(new ViewerSorter());
		tableViewer.setLabelProvider(new EditorsLabelProvider());

		return tableViewer;
	}

	public void setInput(Object information) {
		EditorWorkbook workbook = (EditorWorkbook) information; 
		inputChanged(workbook, workbook.getVisibleEditor());
	}

	/* (non-Javadoc)
	 * @see AbstractTableInformationControl#gotoSelectedElement()
	 */
	protected void gotoSelectedElement() {
		Object sel = getSelectedElement();
		if (sel != null) {
			EditorWorkbook workbook = (EditorWorkbook) getTableViewer().getInput();
			EditorPane editor = (EditorPane) sel;
			workbook.setVisibleEditor(editor);
			editor.setFocus();
		}
		//dispose();
	}
}
