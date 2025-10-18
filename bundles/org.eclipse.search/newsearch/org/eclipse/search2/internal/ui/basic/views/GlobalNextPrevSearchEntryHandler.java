/*******************************************************************************
 * Copyright (c) 2024 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Global handler for navigating to next/previous search results without requiring
 * focus on the Search view. This handler provides a seamless workflow for
 * navigating through search results while editing.
 * 
 * @since 3.17
 */
public class GlobalNextPrevSearchEntryHandler extends AbstractHandler implements IExecutableExtension {
	private String searchCommand = IWorkbenchCommandConstants.NAVIGATE_NEXT;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ICommandService cs = window.getService(ICommandService.class);

		// Show the Search view
		Command showView = cs.getCommand(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
		HashMap<String, Object> parms = new HashMap<String, Object>();
		parms.put(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID, "org.eclipse.search.ui.views.SearchView");
		ParameterizedCommand showSearchView = ParameterizedCommand.generateCommand(showView, parms);

		IHandlerService hs = window.getService(IHandlerService.class);
		try {
			// Execute the sequence: show search view -> navigate -> activate editor
			hs.executeCommand(showSearchView, (Event)event.getTrigger());
			hs.executeCommand(searchCommand, (Event)event.getTrigger());
			hs.executeCommand(IWorkbenchCommandConstants.WINDOW_ACTIVATE_EDITOR, (Event)event.getTrigger());
		} catch (NotDefinedException | NotEnabledException | NotHandledException e) {
			throw new ExecutionException(e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if ("previous".equals(data)) {
			searchCommand = IWorkbenchCommandConstants.NAVIGATE_PREVIOUS;
		}
	}
}
