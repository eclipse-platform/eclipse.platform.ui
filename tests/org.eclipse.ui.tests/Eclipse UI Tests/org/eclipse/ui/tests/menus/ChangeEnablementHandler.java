/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.3
 */
public class ChangeEnablementHandler extends AbstractHandler {
	private static final String CONTEXT_ID = "org.eclipse.ui.menus.contexts.test2";

	private IContextManagerListener fContextManagerListener;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		init(PlatformUI.getWorkbench());

		MessageDialog.openInformation(window.getShell(), "Hello",
				"Hello context change command!");
		return null;
	}

	private void init(IServiceLocator serviceLocator) {
		if (fContextManagerListener == null) {
			IContextService service = serviceLocator
					.getService(IContextService.class);
			service.addContextManagerListener(getContextListener());
		}
	}

	private IContextManagerListener getContextListener() {
		if (fContextManagerListener == null) {
			fContextManagerListener = contextManagerEvent -> {
				if (contextManagerEvent.isActiveContextsChanged()) {
					setEnabled(contextManagerEvent.getContextManager().getActiveContextIds().contains(CONTEXT_ID));
				}
			};
		}
		return fContextManagerListener;
	}

	boolean fEnabled = true;

	@Override
	public boolean isEnabled() {
		return fEnabled;
	}

	private void setEnabled(boolean enabled) {
		if (fEnabled != enabled) {
			fEnabled = enabled;
			fireHandlerChanged(new HandlerEvent(this, true, false));
		}
	}

	@Override
	public void dispose() {
		if (fContextManagerListener != null) {
			IContextService service = PlatformUI
					.getWorkbench().getService(IContextService.class);
			service.removeContextManagerListener(fContextManagerListener);
			fContextManagerListener = null;
		}
	}
}
