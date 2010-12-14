/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.InternalHandlerUtil;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * Saves all active editors
 * <p>
 * Replacement for SaveAllAction
 * </p>
 * 
 * @since 3.7
 * 
 */
public class SaveAllHandler extends AbstractSaveHandler {

	public SaveAllHandler() {
		registerEnablement();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			((WorkbenchPage) page).saveAllEditors(false, true);
		}

		return null;
	}

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

		// Since Save All also saves saveables from non-part sources,
		// look if any such saveables exist and are dirty.
		SaveablesList saveablesList = (SaveablesList) window.getWorkbench().getService(
				ISaveablesLifecycleListener.class);
		ISaveablesSource[] nonPartSources = saveablesList.getNonPartSources();
		for (int i = 0; i < nonPartSources.length; i++) {
			Saveable[] saveables = nonPartSources[i].getSaveables();
			for (int j = 0; j < saveables.length; j++) {
				if (saveables[j].isDirty()) {
					return EvaluationResult.TRUE;
				}
			}
		}

		// if nothing, then we are not active
		return EvaluationResult.FALSE;
	}

}
