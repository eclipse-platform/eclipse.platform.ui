package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. 
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ui.*;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 *	Closes all active editors
 */
public class CloseAllSavedAction extends PartEventAction implements IPageListener, IPropertyListener{
	private IWorkbenchWindow workbench;	
/**
 *	Create an instance of this class
 */
public CloseAllSavedAction(IWorkbenchWindow aWorkbench) {
	super(WorkbenchMessages.getString("CloseAllSavedAction.text")); //$NON-NLS-1$
	this.workbench = aWorkbench;
	setToolTipText(WorkbenchMessages.getString("CloseAllSavedAction.toolTip")); //$NON-NLS-1$
	//Should create a ID in IWorkbenchActionConstants when it becames API?
	setId("closeAllSaved"); //$NON-NLS-1$
	updateState();
	aWorkbench.addPageListener(this);
	WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_SAVED_ACTION);
}
/**
 * Notifies this listener that the given page has been activated.
 *
 * @param page the page that was activated
 * @see IWorkbenchWindow#setActivePage
 */
public void pageActivated(org.eclipse.ui.IWorkbenchPage page) {
	updateState();
}
/**
 * Notifies this listener that the given page has been closed.
 *
 * @param page the page that was closed
 * @see IWorkbenchPage#close
 */
public void pageClosed(org.eclipse.ui.IWorkbenchPage page) {
	updateState();
}
/**
 * Notifies this listener that the given page has been opened.
 *
 * @param page the page that was opened
 * @see IWorkbenchWindow#openPage
 */
public void pageOpened(org.eclipse.ui.IWorkbenchPage page) {}
/**
 * A part has been closed.
 */
public void partClosed(IWorkbenchPart part) {
	if (part instanceof IEditorPart) {
		part.removePropertyListener(this);
		updateState();	
	}
}
/**
 * A part has been opened.
 */
public void partOpened(IWorkbenchPart part) {	
	if (part instanceof IEditorPart) {
		part.addPropertyListener(this);
		updateState();	
	}
}
/**
 * Indicates that a property has changed.
 *
 * @param source the object whose property has changed
 * @param propID the property which has changed.  In most cases this property ID
 * should be defined as a constant on the source class.
 */
public void propertyChanged(Object source, int propID) {
	if (source instanceof IEditorPart) {
		if (propID == IEditorPart.PROP_DIRTY) {
			updateState();
		}
	}
}
/**
 *	The user has invoked this action
 */
public void run() {
	WorkbenchPage page = (WorkbenchPage)workbench.getActivePage();
	if (page != null)
		page.closeAllSavedEditors();
}
/**
 * Enable the action if there at least one editor open.
 */
private void updateState() {
	WorkbenchPage page = (WorkbenchPage)workbench.getActivePage();
	if(page == null) {
		setEnabled(false);
		return;
	}
	IEditorReference editors[] = page.getSortedEditors();
	for (int i = 0; i < editors.length; i++) {
		if(!editors[i].isDirty()) {
			setEnabled(true);
			return;
		}
	}
	setEnabled(false);
}
}
