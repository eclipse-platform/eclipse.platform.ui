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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * A menu for window creation in the workbench.  
 * <p>
 * An <code>OpenNewWindowMenu</code> is used to populate a menu with
 * "Open Window" actions.  One item is added for each shortcut perspective,
 * as defined by the product ini.  If the user selects one of these items a new window is 
 * created in the workbench with the given perspective.  
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
 * @deprecated See IWorkbench.showPerspective methods.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenNewWindowMenu extends PerspectiveMenu {
    private IAdaptable pageInput;

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
    public OpenNewWindowMenu(IWorkbenchWindow window) {
        this(window, null);
    }

    /**
     * Constructs a new instance of <code>OpenNewPageMenu</code>.  
     *
     * @param window the window where a new page is created if an item within
     *		the menu is selected
     * @param input the page input
     */
    public OpenNewWindowMenu(IWorkbenchWindow window, IAdaptable input) {
        super(window, "Open New Page Menu");//$NON-NLS-1$
        this.pageInput = input;
    }

    /* (non-Javadoc)
     * Opens a new window with a particular perspective and input.
     */
    protected void run(IPerspectiveDescriptor desc) {
        // Verify page input.
        if (pageInput == null) {
        	StatusUtil.handleStatus(
					WorkbenchMessages.OpenNewWindowMenu_dialogTitle + ": " + //$NON-NLS-1$
							WorkbenchMessages.OpenNewWindowMenu_unknownInput,
					StatusManager.SHOW);
			return;
        }

        // Open the page.
        try {
            getWindow().getWorkbench().openWorkbenchWindow(desc.getId(),
                    pageInput);
        } catch (WorkbenchException e) {
			StatusUtil.handleStatus(
					WorkbenchMessages.OpenNewWindowMenu_dialogTitle + ": " + //$NON-NLS-1$
							e.getMessage(), e, StatusManager.SHOW);
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
}
