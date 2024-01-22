/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.menus.UIElement;

/**
 * Replacement for the PinEditorAction.
 */
public class PinEditorHandler extends AbstractHandler implements IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			return null;
		}
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor == null) {
			return null;
		}
		IWorkbenchPartReference ref = window.getActivePage().getReference(editor);
		if (ref instanceof WorkbenchPartReference) {
			WorkbenchPartReference concreteRef = (WorkbenchPartReference) ref;

			concreteRef.setPinned(!concreteRef.isPinned());
			ICommandService commandService = window.getService(ICommandService.class);
			commandService.refreshElements(event.getCommand().getId(), null);
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchWindow window = element.getServiceLocator().getService(IWorkbenchWindow.class);
		if (window == null) {
			return;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return;
		}
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			return;
		}
		IWorkbenchPartReference ref = page.getReference(editor);
		if (ref instanceof WorkbenchPartReference) {
			WorkbenchPartReference concreteRef = (WorkbenchPartReference) ref;
			element.setChecked(concreteRef.isPinned());
		}
	}

}
