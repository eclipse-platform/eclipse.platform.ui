/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.menus;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICallbackUpdater;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.menus.ICommandCallback;

/**
 * @since 3.3
 * 
 */
public class ToggleContextHandler extends AbstractHandler implements
		ICallbackUpdater {
	private static final String TOGGLE_ID = "toggleContext.contextId";
	Map contextActivations = new HashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String contextId = event.getParameter(TOGGLE_ID);
		if (event.getApplicationContext() instanceof IEvaluationContext) {
			IEvaluationContext app = (IEvaluationContext) event
					.getApplicationContext();
			IWorkbenchWindow window = (IWorkbenchWindow) app
					.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
			IContextService contextService = (IContextService) window
					.getService(IContextService.class);
			IContextActivation a = (IContextActivation) contextActivations
					.get(contextId);

			// toggle the context active or not
			if (a == null) {
				contextActivations.put(contextId, contextService
						.activateContext(contextId));
			} else {
				contextService.deactivateContext(a);
				contextActivations.remove(contextId);
			}

			// now we should update any menu items/tool items that refer
			// to toggleContext(contextId) ... this request means
			// only update the UI that points to this specific context
			// id ... not the other, non-interesting ones.
			ICommandService commandService = (ICommandService) window
					.getService(ICommandService.class);
			Map filter = new HashMap();
			filter.put(TOGGLE_ID, contextId);
			commandService.refreshCallbacks(event.getCommand().getId(), filter);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICallbackUpdater#updateCallback(org.eclipse.core.runtime.IAdaptable,
	 *      java.util.Map)
	 */
	public void updateCallback(IAdaptable callback, Map parameters) {
		// get the standard platform UI feedback object
		ICommandCallback feedback = (ICommandCallback) callback
				.getAdapter(ICommandCallback.class);
		if (feedback == null) {
			return;
		}

		// the checked state depends on if we have an activation for that
		// context ID or not
		String contextId = (String) parameters.get(TOGGLE_ID);
		feedback.setChecked(contextActivations.get(contextId) != null);
	}
}
