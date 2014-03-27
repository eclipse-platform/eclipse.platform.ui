/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Shows the given view. If no view is specified in the parameters, then this
 * opens the view selection dialog.
 * 
 * @since 3.1
 */
public final class ShowViewHandler extends AbstractHandler {

  
    /**
     * Creates a new ShowViewHandler that will open the view in its default location.
     */
    public ShowViewHandler() {
    }
    
    /**
     * Creates a new ShowViewHandler that will optionally force the view to become
     * a fast view.
     * 
     * @param makeFast if true, the view will be moved to the fast view bar (even if it already
     * exists elsewhere). If false, the view will be shown in its default location. Calling with
     * false is equivalent to using the default constructor.
     */    
    public ShowViewHandler(boolean makeFast) {

    }
    
	@Override
	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		// Get the view identifier, if any.
		final Map parameters = event.getParameters();
		final Object value = parameters
				.get(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID);
		

		if (value == null) {
			openOther(window);
		} else {
            try {
                openView((String) value, window);
            } catch (PartInitException e) {
                throw new ExecutionException("Part could not be initialized", e); //$NON-NLS-1$
            }
		}

		return null;
	}

	/**
	 * Opens a view selection dialog, allowing the user to chose a view.
	 */
	private final void openOther(final IWorkbenchWindow window) {
		final IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		
		final ShowViewDialog dialog = new ShowViewDialog(window,
				WorkbenchPlugin.getDefault().getViewRegistry());
		dialog.open();
		
		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}
		
		final IViewDescriptor[] descriptors = dialog.getSelection();
		for (int i = 0; i < descriptors.length; ++i) {
			try {
                openView(descriptors[i].getId(), window);
			} catch (PartInitException e) {
				StatusUtil.handleStatus(e.getStatus(),
						WorkbenchMessages.ShowView_errorTitle
								+ ": " + e.getMessage(), //$NON-NLS-1$
						StatusManager.SHOW);
			}
		}
	}

	/**
	 * Opens the view with the given identifier.
	 * 
	 * @param viewId
	 *            The view to open; must not be <code>null</code>
	 * @throws PartInitException
	 *             If the part could not be initialized.
	 */
	private final void openView(final String viewId,
			final IWorkbenchWindow activeWorkbenchWindow)
			throws PartInitException {

		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return;
		}

		activePage.showView(viewId);
		
	}
}
