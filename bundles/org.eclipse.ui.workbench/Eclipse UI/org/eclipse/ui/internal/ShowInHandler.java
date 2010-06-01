/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.services.WorkbenchSourceProvider;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * The show in command, which only needs a target id.
 * 
 * @since 3.4
 */
public class ShowInHandler extends AbstractHandler implements IElementUpdater {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String targetId = event
				.getParameter(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET);
		if (targetId == null) {
			throw new ExecutionException("No targetId specified"); //$NON-NLS-1$
		}

		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISourceProviderService sps = (ISourceProviderService)activeWorkbenchWindow.getService(ISourceProviderService.class);
		if (sps != null) {
			ISourceProvider sp = sps.getSourceProvider(ISources.SHOW_IN_SELECTION);
			if (sp instanceof WorkbenchSourceProvider) {
				((WorkbenchSourceProvider)sp).checkActivePart(true);
			}
		}

		ShowInContext context = getContext(HandlerUtil
				.getShowInSelection(event), HandlerUtil.getShowInInput(event));
		if (context == null) {
			return null;
		}

		IWorkbenchPage page= activeWorkbenchWindow.getActivePage();

		try {
			IViewPart view = page.showView(targetId);
			IShowInTarget target = getShowInTarget(view);
			if (!(target != null && target.show(context))) {
				page.getWorkbenchWindow().getShell().getDisplay().beep();
			}
			((WorkbenchPage) page).performedShowIn(targetId); // TODO: move
			// back up
		} catch (PartInitException e) {
			throw new ExecutionException("Failed to show in", e); //$NON-NLS-1$
		}

		return null;
	}

	/**
	 * Returns the <code>ShowInContext</code> to show in the selected target,
	 * or <code>null</code> if there is no valid context to show.
	 * <p>
	 * This implementation obtains the context from global variables provide.
	 * showInSelection and showInInput should be available.
	 * <p>
	 * 
	 * @return the <code>ShowInContext</code> to show or <code>null</code>
	 */
	private ShowInContext getContext(ISelection showInSelection, Object input) {
		if (input == null && showInSelection == null) {
			return null;
		}
		return new ShowInContext(input, showInSelection);
	}

	/**
	 * Returns the <code>IShowInTarget</code> for the given part, or
	 * <code>null</code> if it does not provide one.
	 * 
	 * @param targetPart
	 *            the target part
	 * @return the <code>IShowInTarget</code> or <code>null</code>
	 */
	private IShowInTarget getShowInTarget(IWorkbenchPart targetPart) {
		return (IShowInTarget) Util.getAdapter(targetPart, IShowInTarget.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement,
	 *      java.util.Map)
	 */
	public void updateElement(UIElement element, Map parameters) {
		String targetId = (String) parameters
				.get(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_PARM_TARGET);
		if (targetId == null || targetId.length() == 0) {
			return;
		}
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();
		IViewDescriptor desc = reg.find(targetId);
		if (desc != null) {
			element.setIcon(desc.getImageDescriptor());
			element.setText(desc.getLabel());
		}
	}
}
