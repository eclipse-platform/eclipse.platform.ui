package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.dialogs.SelectPerspectiveDialog;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.*;

/**
 * A menu for window creation in the workbench.  
 * <p>
 * An <code>OpenPerspectiveMenu</code> is used to populate a menu with
 * actions that will open a new perspective. If the user selects one of 
 * these items either a new page is added to the workbench, a new 
 * workbench window is created with the chosen perspective or the current
 * perspective will be replaced with the new onw.
 * </p><p>
 * The visible perspectives within the menu may also be updated dynamically to
 * reflect user preference.
 * </p><p>
 * The input for the page is determined by the value of <code>pageInput</code>.
 * The input should be passed into the constructor of this class or set using
 * the <code>setPageInput</code> method.
 * </p><p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenPerspectiveMenu extends PerspectiveMenu {
	private IAdaptable pageInput;
	private IMenuManager parentMenuManager;
	private boolean replaceEnabled = true;
	private IWorkbenchWindow window;
	private IPerspectiveRegistry reg;

	private static String PAGE_PROBLEMS_TITLE = WorkbenchMessages.getString("OpenPerspectiveMenu.pageProblem"); //$NON-NLS-1$
	private static String PAGE_PROBLEMS_MESSAGE = WorkbenchMessages.getString("OpenPerspectiveMenu.errorUnknownInput"); //$NON-NLS-1$
	private static String WINDOW_PROBLEMS_TITLE = WorkbenchMessages.getString("OpenPerspectiveMenu.dialogTitle"); //$NON-NLS-1$
	private static String WINDOW_PROBLEMS_MESSAGE = WorkbenchMessages.getString("OpenPerspectiveMenu.unknownInput"); //$NON-NLS-1$
/**
 * Constructs a new menu.
 */
public OpenPerspectiveMenu(IMenuManager menuManager, IWorkbenchWindow window) {
	this(window);
	this.parentMenuManager = menuManager;
}
/**
 * Constructs a new instance of <code>OpenNewPageMenu</code>. 
 * <p>
 * If this method is used be sure to set the page input by invoking
 * <code>setPageInput</code>.  The page input is required when the user
 * selects an item in the menu.  At that point the menu will attempt to
 * open a new page with the selected perspective and page input.  If there
 * is no page input an error dialog will be opened.
 * </p>
 *
 * @param window the window where a new page is created if an item within
 *		the menu is selected
 */
public OpenPerspectiveMenu(IWorkbenchWindow window) {
	this(window, null);
	showActive(true);
}
/**
 * Constructs a new instance of <code>OpenNewPageMenu</code>.  
 *
 * @param window the window where a new page is created if an item within
 *		the menu is selected
 * @param input the page input
 */
public OpenPerspectiveMenu(IWorkbenchWindow window, IAdaptable input) {
	super(window, "Open New Page Menu");//$NON-NLS-1$
	this.pageInput = input;
}
/**
 * Return the alternate mask for this platform. It is control on win32 and
 * shift alt on other platforms.
 * @return int
 */
private int alternateMask() {
	if (SWT.getPlatform().equals("win32"))//$NON-NLS-1$
		return SWT.CONTROL;
	else
		return SWT.ALT | SWT.SHIFT;
}
/**
 * Return whether or not the menu can be run. Answer true unless the current perspective
 * is replace and the replaceEnabled flag is false.
 * @return String
 */
private boolean canRun() {
	if (openPerspectiveSetting()
		.equals(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE))
		return replaceEnabled;
	return true;
}
/**
 * Return the current perspective setting.
 * @return String
 */
private String openPerspectiveSetting() {
	return WorkbenchPlugin.getDefault().getPreferenceStore().getString(
		IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
}
/**
 * Runs an action for a particular perspective. Opens the persepctive supplied
 * in a new window or a new page depending on the workbench preference.
 *
 * @param desc the selected perspective
 */
protected void run(IPerspectiveDescriptor desc) {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	String perspectiveSetting =
		store.getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);

	runWithPerspectiveValue(desc, perspectiveSetting);
}
/**
 * Runs an action for a particular perspective. Check for shift or control events
 * to decide which event to run.
 *
 * @param desc the selected perspective
 * @param event SelectionEvent - the event send along with the selection callback
 */
protected void run(IPerspectiveDescriptor desc, SelectionEvent event) {

	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	String perspectiveSetting =
		store.getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);

	int stateMask = event.stateMask & (SWT.CONTROL | SWT.SHIFT | SWT.ALT);
	if (stateMask == alternateMask())
		perspectiveSetting =
			store.getString(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE);
	else {
		if (stateMask == SWT.SHIFT)
			perspectiveSetting =
				store.getString(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE);
	}

	runWithPerspectiveValue(desc, perspectiveSetting);
}
/* (non-Javadoc)
 * Opens a new page with a particular perspective and input.
 */
private void runInNewPage(IPerspectiveDescriptor desc) {
	// Verify page input.
	if (pageInput == null) {
		MessageDialog.openError(
			getWindow().getShell(),
			PAGE_PROBLEMS_TITLE,
			PAGE_PROBLEMS_MESSAGE);
		return;
	}

	// Open the page.
	try {
		getWindow().openPage(desc.getId(), pageInput);
	} catch (WorkbenchException e) {
		MessageDialog.openError(
			getWindow().getShell(),
			PAGE_PROBLEMS_TITLE,
			e.getMessage());
	}
}
/* (non-Javadoc)
 * Opens a new window with a particular perspective and input.
 */
private void runInNewWindow(IPerspectiveDescriptor desc) {
	// Verify page input.
	if (pageInput == null) {
		MessageDialog.openError(
			getWindow().getShell(),
			WINDOW_PROBLEMS_TITLE,
			WINDOW_PROBLEMS_MESSAGE);
		return;
	}

	// Open the page.
	try {
		getWindow().getWorkbench().openWorkbenchWindow(desc.getId(), pageInput);
	} catch (WorkbenchException e) {
		MessageDialog.openError(
			getWindow().getShell(),
			WINDOW_PROBLEMS_TITLE,
			e.getMessage());
	}
}
/**
 * Run the action.
 */
private void runReplaceCurrent(IPerspectiveDescriptor desc) {
	IWorkbenchPage persp = getWindow().getActivePage();
	if (persp != null) {
		persp.setPerspective(desc);
	}
}
/**
 * Runs an action for a particular perspective. Opens the perspective supplied
 * in a new window or a new page depending on the workbench preference.
 *
 * @param desc the descriptor used to build the menu
 * @param perspectiveSetting the selected perspective
 */
private void runWithPerspectiveValue(
	IPerspectiveDescriptor desc,
	String perspectiveSetting) {
	if (perspectiveSetting
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW))
		runInNewWindow(desc);
	if (perspectiveSetting
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE))
		runInNewPage(desc);
	if (perspectiveSetting
		.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE))
		runReplaceCurrent(desc);
}
/**
 * Sets the page input.  
 *
 * @param input the page input
 */
public void setPageInput(IAdaptable input) {
	pageInput = input;
}
/**
 * Set whether replace menu item is enabled within its parent menu.
 */
public void setReplaceEnabled(boolean isEnabled) {
	if (replaceEnabled != isEnabled) {
		replaceEnabled = isEnabled;
		if (canRun() && parentMenuManager != null)
			parentMenuManager.update(true);
	}
}
}
