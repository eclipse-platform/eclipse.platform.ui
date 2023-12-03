/*******************************************************************************
 * Copyright (c) 2013, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.InternalHandlerUtil;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Saves all active editors
 * <p>
 * Replacement for SaveAllAction
 * </p>
 *
 * @since 3.7
 */
public class SaveAllHandler extends AbstractSaveHandler {

	public SaveAllHandler() {
		registerEnablement();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			((WorkbenchPage) page).saveAllEditors(false, false, true);
		}
		EPartService partService = getPartService(window);
		if (partService != null && (partService.getDirtyParts().size() > 0)) {
			partService.saveAll(false);
		}
		return null;
	}

	@Override
	protected EvaluationResult evaluate(IEvaluationContext context) {

		IWorkbenchWindow window = InternalHandlerUtil.getActiveWorkbenchWindow(context);
		// no window? not active
		if (window == null)
			return EvaluationResult.FALSE;
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();

		// no page? not active
		if (page == null)
			return EvaluationResult.FALSE;

		// if at least one dirty part, then we are active
		if (page.getDirtyParts().length > 0)
			return EvaluationResult.TRUE;

		EPartService partService = getPartService(window);
		if (partService != null && (partService.getDirtyParts().size() > 0)) {
			return EvaluationResult.TRUE;
		}

		// Since Save All also saves saveables from non-part sources,
		// look if any such saveables exist and are dirty.
		SaveablesList saveablesList = (SaveablesList) window.getWorkbench()
				.getService(ISaveablesLifecycleListener.class);
		if (saveablesList == null) {
			return EvaluationResult.FALSE;
		}

		for (ISaveablesSource nonPartSource : saveablesList.getNonPartSources()) {
			Saveable[] saveables = nonPartSource.getSaveables();
			for (Saveable saveable : saveables) {
				if (saveable.isDirty()) {
					return EvaluationResult.TRUE;
				}
			}
		}

		// if nothing, then we are not active
		return EvaluationResult.FALSE;
	}

	private EPartService getPartService(IWorkbenchWindow window) {
		EPartService partService = null;
		if (window instanceof WorkbenchWindow) {
			try {
				partService = ((WorkbenchWindow) window).getModel().getContext().get(EPartService.class);
			} catch (Exception e) {
				// do nothing
			}
		}
		return partService;
	}
}
