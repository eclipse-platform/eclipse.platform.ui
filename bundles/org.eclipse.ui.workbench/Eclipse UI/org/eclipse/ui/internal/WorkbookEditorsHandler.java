/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Shows a list of open editors in the current or last active workbook.
 * 
 * @since 3.4
 * 
 */
public class WorkbookEditorsHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		WorkbenchPage page = (WorkbenchPage) workbenchWindow.getActivePage();
		if (page != null) {
			MUIElement area = page.findSharedArea();
			if (area instanceof MPlaceholder) {
				area = ((MPlaceholder) area).getRef();
			}

			MPartStack activeStack = getActiveStack(area);
			if (activeStack != null) {
				if (activeStack.getRenderer() instanceof StackRenderer
						&& activeStack.getWidget() instanceof CTabFolder) {
					StackRenderer stackRenderer = (StackRenderer) activeStack.getRenderer();
					stackRenderer.showAvailableItems(activeStack,
							(CTabFolder) activeStack.getWidget());
				}
			}
		}
		return null;
	}

	private MPartStack getActiveStack(Object element) {
		if (element instanceof MPartStack) {
			return (MPartStack) element;
		} else if (element instanceof MElementContainer<?>) {
			return getActiveStack(((MElementContainer<?>) element).getSelectedElement());
		}
		return null;
	}

}
