package org.eclipse.update.internal.ui.views;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public abstract class BaseTreeView extends BaseView {
	/**
	 * The constructor.
	 */
	public BaseTreeView() {
	}

	protected StructuredViewer createViewer(Composite parent, int styles) {
		return new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | styles);
	}
	
	public TreeViewer getTreeViewer() {
		return (TreeViewer)getViewer();
	}
}