/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.quickaccess;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Handler for quick access pop-up dialog, showing UI elements such as editors,
 * views, commands.
 * 
 */
public class QuickAccessHandler extends AbstractHandler {

	private IWorkbenchWindow window;

	/**
	 * The constructor.
	 */
	public QuickAccessHandler() {
	}

	public Object execute(ExecutionEvent executionEvent) {

		window = HandlerUtil.getActiveWorkbenchWindow(executionEvent);
		if (window == null) {
			return null;
		}

		MWindow mWindow = ((WorkbenchWindow) window).getModel();
		EModelService modelService = mWindow.getContext().get(EModelService.class);
		MToolControl searchField = (MToolControl) modelService.find("SearchField", mWindow); //$NON-NLS-1$
		if (searchField == null) {
			return null;
		}
		Control control = (Control) searchField.getWidget();
		if (!((WorkbenchWindow) window).isToolbarVisible()) {
			((WorkbenchWindow) window).toggleToolbarVisibility();
			control = (Control) searchField.getWidget();
		}
		// the workbench configurer may override visibility; if so, focus should
		// not change
		if (((WorkbenchWindow) window).isToolbarVisible() && control != null) {
			Control previousFocusControl = control.getDisplay().getFocusControl();
			control.setFocus();
			SearchField field = (SearchField) searchField.getObject();
			field.activate(previousFocusControl);
		}
		return null;
	}

}