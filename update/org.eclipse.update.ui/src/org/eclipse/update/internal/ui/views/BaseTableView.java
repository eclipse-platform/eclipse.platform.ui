package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */

public abstract class BaseTableView extends BaseView {
	private TableViewer viewer;

	/**
	 * The constructor.
	 */
	public BaseTableView() {
	}

	protected StructuredViewer createViewer(Composite parent, int style) {
		return viewer =
			new TableViewer(
				parent,
				SWT.MULTI
					| SWT.H_SCROLL
					| SWT.V_SCROLL
					| SWT.FULL_SELECTION
					| style);
	}
	
	public TableViewer getTableViewer() {
		return (TableViewer)getViewer();
	}
}