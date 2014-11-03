/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.menus;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.3
 *
 */
public class HelloUpdateHandler extends AbstractHandler implements
		IElementUpdater {

	private String myLabelState = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		MessageDialog.openInformation(window.getShell(), "Hello",
				"Hello label update command!");
		myLabelState = "My New Item";
		ICommandService cs = window
				.getService(ICommandService.class);
		cs.refreshElements(event.getCommand().getId(), null);
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		if (myLabelState == null) {
			return;
		}
		element.setText(myLabelState);
	}
}
