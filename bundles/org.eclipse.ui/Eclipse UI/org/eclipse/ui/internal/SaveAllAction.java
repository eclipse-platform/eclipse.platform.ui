package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

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
	private IWorkbenchPage activePage = null;
	private ArrayList targets = null;
/**
 * The default constructor.
 */
public SaveAllAction(WorkbenchWindow window) {
	super("saveAll");//$NON-NLS-1$
	setText(WorkbenchMessages.getString("SaveAll.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SaveAll.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.SAVE_ALL);
	setAccelerator(SWT.CTRL | SWT.SHIFT |'s');
	setEnabled(false);
	this.window = window;
	window.addPageListener(this);
	WorkbenchHelp.setHelp(this, IHelpContextIds.SAVE_ALL_ACTION);
}
/**
 * Notifies the listener that a page has been activated.
 */
public void pageActivated(IWorkbenchPage page) {
	activePage = page;
	refreshTargets();
	testEnable();
}
/**
 * Notifies the listener that a page has been closed
 */
public void pageClosed(IWorkbenchPage page) {
	activePage = null;
	refreshTargets();
	testEnable();
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
		if (targets != null)
			targets.remove(part);
		testEnable();	
	}
}
/**
 * Implements part listener.
 */
public void partOpened(IWorkbenchPart part) {
	if (part instanceof IEditorPart) {
		part.addPropertyListener(this);
		if (targets != null)
			targets.add(part);
		testEnable();	
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
			testEnable();
		}
	}
}
/**
 * Refresh the editor list from the active perspective.
 */
protected void refreshTargets() {
	targets = null;
	if (activePage != null) {
		IEditorPart [] array = activePage.getEditors();
		targets = new ArrayList(array.length);
		for (int nX = 0, length = array.length; nX < length; nX ++) {
			targets.add(array[nX]);
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
protected void testEnable() {
	if (targets != null && targets.size() > 0) {
		for (int i=0; i<targets.size(); i++) {
			IEditorPart saveTarget = (IEditorPart)targets.get(i);
			if (saveTarget.isDirty()) {
				setEnabled(true);
				return;
			}
		}
	}
	setEnabled(false);
}
}
