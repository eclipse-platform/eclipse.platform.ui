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
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.e4.compatibility.WorkbenchWindow;

/**
 * Shows the given view. If no view is specified in the parameters, then this
 * opens the view selection dialog.
 * 
 * @since 3.1
 */
public final class ShowViewHandler extends AbstractHandler {

	/**
	 * The name of the parameter providing the view identifier.
	 */
	private static final String PARAMETER_NAME_VIEW_ID = "org.eclipse.ui.views.showView.viewId"; //$NON-NLS-1$
    private boolean makeFast = false;
	private static final String PARAMETER_MAKE_FAST = "org.eclipse.ui.views.showView.makeFast"; //$NON-NLS-1$
  
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
        this.makeFast = makeFast;
    }
    
	public final Object execute(final ExecutionEvent event)
			throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		// Get the view identifier, if any.
		final Map parameters = event.getParameters();
		final Object value = parameters.get(PARAMETER_NAME_VIEW_ID);
		makeFast = "true".equals(parameters.get(PARAMETER_MAKE_FAST)); //$NON-NLS-1$
		
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
	private final void openOther(IWorkbenchWindow window) {
		if (window.getActivePage() == null) {
			return;
		}

		MWindow model = ((WorkbenchWindow) window).getModel();
		IEclipseContext context = model.getContext();
		ECommandService commandService = (ECommandService) context.get(ECommandService.class.getName());
		EHandlerService handlerService = (EHandlerService) model.getContext().get(
				EHandlerService.class.getName());
		handlerService.executeHandler(new ParameterizedCommand(commandService
				.getCommand("e4.show.view"), null)); //$NON-NLS-1$
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

        if (makeFast) {
			// TODO compat: we need to do something about fast views
			E4Util.unsupported("ShowViewHandler: makeFast"); //$NON-NLS-1$
			// WorkbenchPage wp = (WorkbenchPage) activePage;
			// Perspective persp = wp.getActivePerspective();
			//
			// // If we're making a fast view then use the new mechanism
			// directly
			// boolean useNewMinMax = Perspective.useNewMinMax(persp);
			// if (useNewMinMax) {
			// IViewReference ref = persp.getViewReference(viewId, null);
			// if (ref == null)
			// return;
			//
			// persp.getFastViewManager().addViewReference(FastViewBar.FASTVIEWBAR_ID,
			// -1, ref, true);
			// wp.activate(ref.getPart(true));
			//        		
			// return;
			// }
			//            
			// IViewReference ref = wp.findViewReference(viewId);
			//            
			// if (ref == null) {
			// IViewPart part = wp.showView(viewId, null,
			// IWorkbenchPage.VIEW_CREATE);
			// ref = (IViewReference)wp.getReference(part);
			// }
			//            
			// if (!wp.isFastView(ref)) {
			// wp.addFastView(ref);
			// }
			// wp.activate(ref.getPart(true));
        } else {
            activePage.showView(viewId);
        }
		
	}
}
