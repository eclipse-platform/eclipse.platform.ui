package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Page for the content outliner
 */
public class PropertySheetContentOutlinePage extends ContentOutlinePage {

	private IAdaptable model;
/**
 * Create a new instance of the reciver using adapatable
 * as the model.
 */
public PropertySheetContentOutlinePage(IAdaptable adaptable) {
	this.model = adaptable;
}
/* (non-Javadoc)
 * Method declared on Page
 */
public void createControl(Composite parent) {
	super.createControl(parent);
	getTreeViewer().setContentProvider(new WorkbenchContentProvider());
	getTreeViewer().setLabelProvider(new WorkbenchLabelProvider());
	getTreeViewer().setInput(this.model); 
	return;
}
}
