/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.menus.UIElement;

/**
 * Handler that toggles the visibility of the coolbar/perspective bar in a given
 * window.
 *
 * @since 3.3
 */
public class ToggleCoolbarHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		if (activeWorkbenchWindow instanceof WorkbenchWindow) {
			WorkbenchWindow window = (WorkbenchWindow) activeWorkbenchWindow;
			window.toggleToolbarVisibility();
		}

		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchLocationService wls = element.getServiceLocator().getService(IWorkbenchLocationService.class);
		IWorkbenchWindow window = wls.getWorkbenchWindow();
		if (window == null || !(window instanceof WorkbenchWindow))
			return;
		element.setText(
				isCoolbarVisible((WorkbenchWindow) window) ? WorkbenchMessages.ToggleCoolbarVisibilityAction_hide_text
						: WorkbenchMessages.ToggleCoolbarVisibilityAction_show_text);
	}

	/**
	 * Return whether the coolbar is currently visible.
	 *
	 * @param window the window to test
	 * @return whether or not the coolbar is visible
	 */
	private boolean isCoolbarVisible(WorkbenchWindow window) {
		return window.getCoolBarVisible() || window.getPerspectiveBarVisible();
	}
}
