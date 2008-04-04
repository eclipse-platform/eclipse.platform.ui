/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.statushandlers.StatusManager;

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
 * @deprecated  See IWorkbench.showPerspective methods.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenPerspectiveMenu extends PerspectiveMenu {
    private IAdaptable pageInput;

    private IMenuManager parentMenuManager;

    private boolean replaceEnabled = true;

    private static String PAGE_PROBLEMS_TITLE = WorkbenchMessages.OpenPerspectiveMenu_pageProblemsTitle; 

    private static String PAGE_PROBLEMS_MESSAGE = WorkbenchMessages.OpenPerspectiveMenu_errorUnknownInput;

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
     * Return whether or not the menu can be run. Answer true unless the current mode
     * is replace and the replaceEnabled flag is false.
     */
    private boolean canRun() {
        if (openPerspectiveSetting().equals(
                IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE)) {
			return replaceEnabled;
		}
        return true;
    }

    /**
     * Return the current perspective setting.
     */
    private String openPerspectiveSetting() {
        return PrefUtil.getAPIPreferenceStore().getString(
                IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
    }

    /**
     * Runs an action for a particular perspective. Opens the perspective supplied
     * in a new window or a new page depending on the workbench preference.
     *
     * @param desc the selected perspective
     */
    protected void run(IPerspectiveDescriptor desc) {
        openPage(desc, 0);
    }

    /**
     * Runs an action for a particular perspective. Check for shift or control events
     * to decide which event to run.
     *
     * @param desc the selected perspective
     * @param event the event sent along with the selection callback
     */
    protected void run(IPerspectiveDescriptor desc, SelectionEvent event) {
        openPage(desc, event.stateMask);
    }

    /* (non-Javadoc)
     * Opens a new page with a particular perspective and input.
     */
    private void openPage(IPerspectiveDescriptor desc, int keyStateMask) {
        // Verify page input.
        if (pageInput == null) {			
			StatusUtil.handleStatus(PAGE_PROBLEMS_TITLE
					+ ": " + PAGE_PROBLEMS_MESSAGE, StatusManager.SHOW); //$NON-NLS-1$
			return;
        }

        // Open the page.
        try {
            getWindow().getWorkbench().showPerspective(desc.getId(),
                    getWindow(), pageInput);
        } catch (WorkbenchException e) {
			StatusUtil.handleStatus(
					PAGE_PROBLEMS_TITLE + ": " + e.getMessage(), e,  //$NON-NLS-1$
					StatusManager.SHOW);
		}
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
            if (canRun() && parentMenuManager != null) {
				parentMenuManager.update(true);
			}
        }
    }
}
