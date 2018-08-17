/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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

package org.eclipse.ui.tests.menus;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.3
 *
 */
public class ToggleContextHandler extends AbstractHandler implements
		IElementUpdater {
	private static final String TOGGLE_ID = "toggleContext.contextId";
	Map<String, IContextActivation> contextActivations = new HashMap<>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String contextId = event.getParameter(TOGGLE_ID);
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		IContextService contextService = window
				.getService(IContextService.class);
		IContextActivation a = contextActivations
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
		ICommandService commandService = window
				.getService(ICommandService.class);
		Map<String, String> filter = new HashMap<>();
		filter.put(TOGGLE_ID, contextId);
		commandService.refreshElements(event.getCommand().getId(), filter);
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {

		// the checked state depends on if we have an activation for that
		// context ID or not
		String contextId = (String) parameters.get(TOGGLE_ID);
		element.setChecked(contextActivations.get(contextId) != null);
	}
}
