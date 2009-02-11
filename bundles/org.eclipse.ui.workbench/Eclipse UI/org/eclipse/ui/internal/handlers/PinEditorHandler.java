/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
 * 
 */
public class PinEditorHandler extends AbstractHandler implements
		IElementUpdater {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			return null;
		}
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor == null) {
			return null;
		}
		IWorkbenchPartReference ref = window.getActivePage().getReference(
				editor);
		if (ref instanceof WorkbenchPartReference) {
			WorkbenchPartReference concreteRef = (WorkbenchPartReference) ref;

			concreteRef.setPinned(!concreteRef.isPinned());
			ICommandService commandService = (ICommandService) window
					.getService(ICommandService.class);
			commandService.refreshElements(event.getCommand().getId(), null);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.
	 * menus.UIElement, java.util.Map)
	 */
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchWindow window = (IWorkbenchWindow) element
				.getServiceLocator().getService(IWorkbenchWindow.class);
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
		IWorkbenchPartReference ref = page.getReference(
				editor);
		if (ref instanceof WorkbenchPartReference) {
			WorkbenchPartReference concreteRef = (WorkbenchPartReference) ref;
			element.setChecked(concreteRef.isPinned());
		}
	}

}
