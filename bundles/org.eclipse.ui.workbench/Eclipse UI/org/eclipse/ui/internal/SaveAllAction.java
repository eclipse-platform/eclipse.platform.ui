package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Global action that saves all targets in the
 * workbench that implement ISaveTarget interface.
 * The action keeps track of opened save targets
 * and their 'save' state. If none of the currently
 * opened targets needs saving, it will disable.
 * This action is somewhat different from all
 * other global actions in that it works on
 * multiple targets at the same time i.e. it
 * does not disconnect from the target when it
 * becomes deactivated.
 */
public class SaveAllAction extends PartEventAction
	implements IPageListener, IPropertyListener
{
	private WorkbenchWindow window;
/**
 * The default constructor.
 */
public SaveAllAction(WorkbenchWindow window) {
	super(WorkbenchMessages.getString("SaveAll.text"));//$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SaveAll.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.SAVE_ALL);
	setEnabled(false);
	this.window = window;
	window.addPageListener(this);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ALL_ACTION);
}
/**
 * Notifies the listener that a page has been activated.
 */
public void pageActivated(IWorkbenchPage page) {
	updateState();
}
/**
 * Notifies the listener that a page has been closed
 */
public void pageClosed(IWorkbenchPage page) {
	updateState();
}
/**
 * Notifies the listener that a page has been opened.
 */
public void pageOpened(IWorkbenchPage page) {
}
/**
 * Implements part listener.
 */
public void partClosed(IWorkbenchPart part) {
	if (part instanceof IEditorPart) {
		part.removePropertyListener(this);
		updateState();	
	}
}
/**
 * Implements part listener.
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
 * Cycles through the list of active save targets
 * and saves those that need it.
 */
public void run() {
	IWorkbenchPage page = window.getActivePage();
	if (page != null)
		page.saveAllEditors(false);
}
/**
 * Updates availability depending on number of
 * targets that need saving.
 */
protected void updateState() {
	IWorkbenchPage page = window.getActivePage();
	setEnabled(page != null && page.getDirtyEditors().length > 0);
}
}
