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
package org.eclipse.update.internal.ui.forms;

import java.util.Iterator;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.ui.forms.internal.*;

public class MultipleSelectionForm extends UpdateWebForm {
	private IStructuredSelection selection;
	private Label counter;
	private TableViewer tableViewer;

	class ViewProvider
		extends DefaultContentProvider
		implements IStructuredContentProvider {
		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return selection.toArray();
		}
	}

	public MultipleSelectionForm(UpdateFormPage page) {
		super(page);
	}

	public void dispose() {
		super.dispose();
	}

	public void objectChanged(Object object, String property) {
		if (selection == null)
			return;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (object.equals(obj)) {
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						expandTo(selection);
					}
				});
				break;
			}
		}
	}

	public void initialize(Object modelObject) {
		setHeadingText(UpdateUI.getString("MultipleSelectionForm.title"));
		super.initialize(modelObject);
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = layout.rightMargin = 10;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 20;
		layout.numColumns = 1;

		FormWidgetFactory factory = getFactory();
		counter = factory.createLabel(parent, "");
		TableData td = new TableData();
		td.align = TableData.FILL;
		counter.setLayoutData(td);
		tableViewer = new TableViewer(factory.createTable(parent, SWT.NULL));
		tableViewer.setContentProvider(new ViewProvider());
		tableViewer.setLabelProvider(UpdateUI.getDefault().getLabelProvider());
		td = new TableData();
		td.align = TableData.FILL;
		td.valign = TableData.FILL;
		tableViewer.getTable().setLayoutData(td);
	}

	public void expandTo(Object obj) {
		String name = "";

		if (obj != null && obj instanceof IStructuredSelection) {
			selection = (IStructuredSelection) obj;
			refresh();
			tableViewer.getControl().getParent().layout(true);
			tableViewer.getControl().getParent().getParent().layout(true);
			updateSize();
		}
	}
	private void refresh() {
		int size = selection.size();
		counter.setText(
			UpdateUI.getFormattedMessage(
				"MultipleSelectionForm.counter",
				"" + size));
		tableViewer.setInput(selection);
	}
}
