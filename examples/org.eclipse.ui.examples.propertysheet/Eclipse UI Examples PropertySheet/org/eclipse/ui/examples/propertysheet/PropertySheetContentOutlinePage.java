package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.IPageSite;
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
/**
 * Creates and registers the popup menu for this page
 */
public void init(IPageSite pageSite) {
	super.init(pageSite);
	// Configure the context menu.
	MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
	menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));	
	menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));	 //$NON-NLS-1$

	TreeViewer viewer = getTreeViewer();
	Menu menu = menuMgr.createContextMenu(viewer.getTree());
	viewer.getTree().setMenu(menu);
	// Be sure to register it so that other plug-ins can add actions.
	getSite().registerContextMenu("org.eclipse.ui.examples.propertysheet.outline", menuMgr, viewer);
}	
}
