package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Iterator;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
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
		if (selection==null) return;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (object.equals(obj)) {
				expandTo(selection);
				break;
			}
		}
	}

	public void initialize(Object modelObject) {
		setHeadingText("Multiple Selection");
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
		tableViewer.setLabelProvider(
			UpdateUIPlugin.getDefault().getLabelProvider());
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
		counter.setText(size + " items selected.");
		tableViewer.setInput(selection);
	}
}