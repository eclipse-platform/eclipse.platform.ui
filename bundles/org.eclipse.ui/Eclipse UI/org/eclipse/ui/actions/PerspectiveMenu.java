package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A menu for perspective selection.  
 * <p>
 * A <code>PerspectiveMenu</code> is used to populate a menu with
 * pespective items.  One item is added to the menu for each perspective
 * shortcut, as defined by the product info.  If the user selects one of these items 
 * an action is performed for the selected perspective.
 * </p><p>
 * The visible perspectives within the menu may also be updated dynamically to
 * reflect user preference.
 * </p><p>
 * This class is abstract.  Subclasses must implement the <code>run</code> method,
 * which performs a specialized action for the selected perspective.
 * </p>
 */
public abstract class PerspectiveMenu extends ContributionItem {
	private IWorkbenchWindow window;
	private static IPerspectiveRegistry reg;
	private boolean showActive = false;
/**
 * Constructs a new instance of <code>PerspectiveMenu</code>.  
 *
 * @param window the window containing this menu
 * @param id the menu id
 */
public PerspectiveMenu(IWorkbenchWindow window, String id) {
	super(id);
	this.window = window;
	if (reg == null)
		reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
}
/* (non-Javadoc)
 * Creates a menu item for a perspective.
 */
void createMenuItem(Menu menu, int index, 
	final IPerspectiveDescriptor desc, int nAccelerator, boolean bCheck) 
{
	MenuItem mi = new MenuItem(menu, bCheck ? SWT.RADIO : SWT.PUSH, index);
	if (nAccelerator < 10)
		mi.setText("&" + nAccelerator + "  " + desc.getLabel());
	else
		mi.setText(desc.getLabel());
	mi.setSelection(bCheck);
	mi.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			run(desc,e);
		}
	});
}
/* (non-Javadoc)
 * Creates a menu item for "other".
 */
void createOtherItem(Menu menu, int index) 
{
	MenuItem mi = new MenuItem(menu, SWT.PUSH, index);
	mi.setText("&Other...");
	mi.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			runOther(e);
		}
	});
}
/* (non-Javadoc)
 * Fills the menu with perspective items.
 */
public void fill(Menu menu, int index) 
{
	int nAccelerator = 0;
	
	// If the menu is empty just return.
	ArrayList persps = getShortcuts();
	if (persps.size() == 0)
		return;

	// Get the checked persp.
	String checkID = null;
	if (showActive) {
		IWorkbenchPage activePage = window.getActivePage();
		if ((activePage != null) && (activePage.getPerspective() != null))
			checkID = activePage.getPerspective().getId();
	}

	// Add shortcuts.
	for (int i = 0; i < persps.size(); i++) {
		IPerspectiveDescriptor desc = (IPerspectiveDescriptor)persps.get(i);
		createMenuItem(menu, index, desc, nAccelerator, desc.getId().equals(checkID));
		++ index;
		++ nAccelerator;
	}

	// Add others item..
	if (nAccelerator > 0) {
		new MenuItem(menu, SWT.SEPARATOR, index);
		++ index;
	}
	createOtherItem(menu, index);
}
/* (non-Javadoc)
 * Returns the shortcut perspectives.
 *
 * The shortcut list is formed from the default perspective (dynamic) and
 * the the product perspectives (static).  For performance, we implement a
 * shortcut cache which is only updated if the default perspective changes.
 */
private ArrayList getShortcuts() 
{
	ArrayList list = new ArrayList();

	// Add default perspective.
	String def = reg.getDefaultPerspective();
	IPerspectiveDescriptor desc = reg.findPerspectiveWithId(def);
	if (desc != null)
		list.add(desc);

	// Add friendly perspectives.
	IWorkbenchPage page = window.getActivePage();
	if (page != null) {
		ArrayList friends = ((WorkbenchPage)page).getPerspectiveActions();
		if (friends != null) {
			for (int nX = 0; nX < friends.size(); nX ++) {
				String perspID = (String)friends.get(nX);
				desc = reg.findPerspectiveWithId(perspID);
				if (desc != null && !list.contains(desc))
					list.add(desc);
			}
		}
	}

	return list;
}
/**
 * Returns the window for this menu.
 *
 * @returns the window 
 */
protected IWorkbenchWindow getWindow() {
	return window;
}
/* (non-Javadoc)
 * Returns whether this menu is dynamic.
 */
public boolean isDynamic() {
	return true;
}
/**
 * Runs an action for a particular perspective.  The behavior of the
 * action is defined by the subclass.
 *
 * @param desc the selected perspective
 */
protected abstract void run(IPerspectiveDescriptor desc);
/**
 * Runs an action for a particular perspective.  The behavior of the
 * action is defined by the subclass.
 *
 * @param desc the selected perspective
 * @param event SelectionEvent - the event send along with the selection callback
 */
protected void run(IPerspectiveDescriptor desc, SelectionEvent event) {
	//Do a run without the descriptor by default
	run(desc);
}
/* (non-Javadoc)
 * Show the "other" dialog, select a perspective, and run it.
 */
void runOther() {
	SelectPerspectiveDialog dlg = new SelectPerspectiveDialog(window.getShell(), reg);
	dlg.open();
	if (dlg.getReturnCode() == Window.CANCEL)
		return;
	IPerspectiveDescriptor desc = dlg.getSelection();
	if (desc != null) {
		run(desc);
	}
}
/* (non-Javadoc)
 * Show the "other" dialog, select a perspective, and run it. Pass on the selection
 * event should the meny need it.
 */
void runOther(SelectionEvent event) {
	SelectPerspectiveDialog dlg = new SelectPerspectiveDialog(window.getShell(), reg);
	dlg.open();
	if (dlg.getReturnCode() == Window.CANCEL)
		return;
	IPerspectiveDescriptor desc = dlg.getSelection();
	if (desc != null) {
		run(desc,event);
	}
}
/**
 * Sets the showActive flag.  If <code>showActive == true</code> then the
 * active perspective is hilighted with a check mark.
 *
 * @param the new showActive flag
 */
protected void showActive(boolean b) {
	showActive = b;;
}
}
