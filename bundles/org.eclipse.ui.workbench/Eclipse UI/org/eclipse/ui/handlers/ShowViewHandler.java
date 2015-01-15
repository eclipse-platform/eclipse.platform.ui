/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430988
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 455527
 *******************************************************************************/
package org.eclipse.ui.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;

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
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = HandlerUtil.getActiveShell(event);
		IEclipseContext ctx = workbenchWindow.getService(IEclipseContext.class);
		EModelService modelService = workbenchWindow.getService(EModelService.class);
		EPartService partService = workbenchWindow.getService(EPartService.class);
		MApplication app = workbenchWindow.getService(MApplication.class);
		MWindow window = workbenchWindow.getService(MWindow.class);

		// Get the view identifier, if any.
		final Map<?, ?> parameters = event.getParameters();
		final Object value = parameters.get(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID);

		if (value == null) {
			openOther(shell, app, window, modelService, ctx, partService);
		} else {
			try {
				openView((String) value, partService);
			} catch (PartInitException e) {
				throw new ExecutionException("Part could not be initialized", e); //$NON-NLS-1$
			}
		}

		return null;
	}

	/**
	 * Opens a view selection dialog, allowing the user to chose a view.
	 */
	private final void openOther(final Shell shell, MApplication app, MWindow window, EModelService modelService,
			IEclipseContext context, EPartService partService) {

		final ShowViewDialog dialog = new ShowViewDialog(shell, app, window, modelService, partService, context);
		dialog.open();

		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}

		final MPartDescriptor[] descriptors = dialog.getSelection();
		for (MPartDescriptor descriptor : descriptors) {
			partService.showPart(descriptor.getElementId(), PartState.ACTIVATE);
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
	private final void openView(final String viewId, EPartService partService) throws PartInitException {
		partService.showPart(viewId, PartState.ACTIVATE);
	}
}
