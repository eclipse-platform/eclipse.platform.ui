/*******************************************************************************
 * Copyright (c) 2008, 2015 Versant Corp. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 ******************************************************************************/

package org.eclipse.ui.views.properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;

/**
 * @since 3.4
 */
public class NewPropertySheetHandler extends AbstractHandler {

	/**
	 * Command id
	 */
	public static final String ID = "org.eclipse.ui.views.properties.NewPropertySheetCommand"; //$NON-NLS-1$

	/**
	 * Whether new PVs are pinned when newly opened
	 */
	private static final boolean PIN_NEW_PROPERTY_VIEW = Boolean.parseBoolean(System.getProperty("org.eclipse.ui.views.properties.pinNewPV", Boolean.FALSE.toString())); //$NON-NLS-1$

	/**
	 * First tries to find a suitable instance to reuse for the given context,
	 * then creates a new instance if necessary.
	 *
	 * @param event
	 *            An event containing all the information about the current
	 *            state of the application; must not be <code>null</code>.
	 * @return an instance for the given context
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePartChecked(event);

		PropertyShowInContext context = getShowInContext(event);
		try {
			PropertySheet sheet = findPropertySheet(event, context);
			sheet.show(context);
			if (activePart instanceof PropertySheet parent) {
				parent.setPinned(true);
			} else if(!sheet.isPinned()) {
				sheet.setPinned(PIN_NEW_PROPERTY_VIEW);
			}
		} catch (PartInitException e) {
			throw new ExecutionException("Part could not be initialized", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @param event {@link ExecutionEvent} for which the
	 *              {@link PropertyShowInContext} is requested
	 * @return a {@link PropertyShowInContext} containing the the {@link ISelection}
	 *         and {@link IWorkbenchPart} for the given {@link ExecutionEvent}
	 * @throws ExecutionException If the active part variable is not found.
	 */
	protected PropertyShowInContext getShowInContext(ExecutionEvent event)
			throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePartChecked(event);
		if (activePart instanceof PropertySheet sheet) {
			return (PropertyShowInContext) sheet.getShowInContext();
		}
		IShowInSource adapter = Adapters.adapt(activePart, IShowInSource.class);
		if (adapter != null) {
			ShowInContext showInContext = adapter.getShowInContext();
			return new PropertyShowInContext(activePart, showInContext);
		}
		return new PropertyShowInContext(activePart, HandlerUtil
				.getShowInSelection(event));
	}

	/**
	 * Returns a PropertySheet instance
	 *
	 * @param event   {@link ExecutionEvent} for which the {@link PropertySheet} is
	 *                requested
	 * @param context a {@link ShowInContext} to handle
	 * @return a PropertySheet that can handle the given {@link ShowInContext}
	 * @throws PartInitException  if the view could not be initialized
	 * @throws ExecutionException If the active part variable is not found.
	 */
	protected PropertySheet findPropertySheet(ExecutionEvent event,
			PropertyShowInContext context) throws PartInitException,
			ExecutionException {
		IWorkbenchPage page = HandlerUtil.getActivePartChecked(event).getSite()
				.getPage();
		String secondaryId = null;
		if (HandlerUtil.getActivePart(event) instanceof PropertySheet) {
			secondaryId = Long.toString(System.currentTimeMillis());
		} else {
			IViewReference[] refs = page.getViewReferences();
			for (IViewReference viewReference : refs) {
				if (IPageLayout.ID_PROP_SHEET.equals(viewReference.getId())) {
					secondaryId = Long.toString(System.currentTimeMillis());
					PropertySheet sheet = (PropertySheet) viewReference
							.getView(true);
					if (!sheet.isPinned()
							|| (sheet.isPinned() && sheet.getShowInContext()
									.equals(context))) {
						secondaryId = sheet.getViewSite().getSecondaryId();
						break;
					}
				}
			}
		}
		return (PropertySheet) page.showView(IPageLayout.ID_PROP_SHEET,
				secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
	}
}
