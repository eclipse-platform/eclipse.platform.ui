/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430988
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 455527
 *******************************************************************************/
package org.eclipse.ui.handlers;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Shows the given view. If no view is specified in the parameters, then this
 * opens the view selection dialog.
 *
 * @since 3.1
 */
public final class ShowViewHandler extends AbstractHandler {

	/**
	 * Creates a new ShowViewHandler that will open the view in its default
	 * location.
	 */
	public ShowViewHandler() {
	}

	/**
	 * Creates a new ShowViewHandler that will optionally force the view to become a
	 * fast view.
	 *
	 * @param makeFast if true, the view will be moved to the fast view bar (even if
	 *                 it already exists elsewhere). If false, the view will be
	 *                 shown in its default location. Calling with false is
	 *                 equivalent to using the default constructor.
	 */
	public ShowViewHandler(boolean makeFast) {

	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		EPartService partService = workbenchWindow.getService(EPartService.class);
		MApplication app = workbenchWindow.getService(MApplication.class);

		// Get the view identifier, if any.
		final Object id = event.getParameters().get(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID);

		// let user select one or more
		if (!(id instanceof String)) {
			openOther(event, workbenchWindow, app, partService);
			return null;
		}

		MPartDescriptor viewDescriptor = getViewDescriptor(app, (String) id);
		if (viewDescriptor == null) {
			handleMissingView(id);
			return null;
		}

		openView(workbenchWindow, viewDescriptor, partService);
		return null;
	}

	/**
	 * Opens a view selection dialog, allowing the user to chose a view.
	 */
	private static void openOther(ExecutionEvent event, IWorkbenchWindow workbenchWindow, MApplication app,
			EPartService partService) {
		Shell shell = HandlerUtil.getActiveShell(event);
		IEclipseContext ctx = workbenchWindow.getService(IEclipseContext.class);
		EModelService modelService = workbenchWindow.getService(EModelService.class);
		MWindow window = workbenchWindow.getService(MWindow.class);

		final ShowViewDialog dialog = new ShowViewDialog(shell, app, window, modelService, partService, ctx);
		dialog.open();

		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}

		final MPartDescriptor[] descriptors = dialog.getSelection();
		for (MPartDescriptor descriptor : descriptors) {
			openView(workbenchWindow, descriptor, partService);
		}
	}

	/**
	 * Opens the view with the given descriptor.
	 *
	 * @param viewDescriptor The view to open; must not be <code>null</code>
	 */
	private static void openView(IWorkbenchWindow window, final MPartDescriptor viewDescriptor,
			EPartService partService) {
		/*
		 * TODO: see bug 483699: the code below duplicates the code in
		 * org.eclipse.ui.internal.quickaccess.ViewElement#execute() and should be
		 * refactored to some user friendly API
		 */
		String viewId = viewDescriptor.getElementId();
		if (CompatibilityPart.COMPATIBILITY_VIEW_URI.equals(viewDescriptor.getContributionURI())) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					page.showView(viewId);
				} catch (PartInitException e) {
					handleViewError(viewId, e);
				}
			}
		} else {
			MPart part = partService.findPart(viewId);
			if (part == null) {
				MPlaceholder placeholder = partService.createSharedPart(viewId);
				part = (MPart) placeholder.getRef();
			}
			partService.showPart(part, PartState.ACTIVATE);
		}
	}

	private static MPartDescriptor getViewDescriptor(MApplication app, String id) {
		List<MPartDescriptor> descriptors = app.getDescriptors();
		for (MPartDescriptor descriptor : descriptors) {
			if (id.equals(descriptor.getElementId()) && isView(descriptor)) {
				return descriptor;
			}
		}
		return null;
	}

	private static boolean isView(MPartDescriptor descriptor) {
		return descriptor.getTags().contains("View"); //$NON-NLS-1$
	}

	private static void handleViewError(String id, PartInitException e) {
		StatusUtil.handleStatus(e.getStatus(), "View could not be opened: " + id, //$NON-NLS-1$
				StatusManager.SHOW);
	}

	private static void handleMissingView(final Object id) {
		ExecutionException e = new ExecutionException("View could not be found: " + id); //$NON-NLS-1$
		StatusUtil.handleStatus(e, StatusManager.SHOW);
	}
}
