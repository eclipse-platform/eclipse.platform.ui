/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 409633
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 *
 * @author Prakash G.R.
 *
 * @since 3.7
 *
 */
public class LockToolBarHandler extends AbstractHandler {

	private static final String TOOLBAR_SEPARATOR = "toolbarSeparator"; //$NON-NLS-1$
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		WorkbenchWindow window = (WorkbenchWindow) HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MTrimmedWindow winModel = window.getService(MTrimmedWindow.class);
		EModelService modelService = window.getService(EModelService.class);

		if (window != null) {
			ICoolBarManager coolBarManager = window.getCoolBarManager2();
			if (coolBarManager != null) {
				// lock is the opposite of the original value before toggle
				boolean lock = !HandlerUtil.toggleCommandState(event.getCommand());
				final List<MToolBar> children = modelService.findElements(winModel, null, MToolBar.class, null);
				for (MToolBar el : children) {
					if (!el.getTags().contains(TOOLBAR_SEPARATOR)) {
						if (lock) {
							// locks the toolbars
							if (!el.getTags().contains(IPresentationEngine.NO_MOVE)) {
								el.getTags().add(IPresentationEngine.NO_MOVE);
							}
							if (el.getTags().contains(IPresentationEngine.DRAGGABLE)) {
								el.getTags().remove(IPresentationEngine.DRAGGABLE);
							}
						} else {
							// unlocks the toolbars
							if (el.getTags().contains(IPresentationEngine.NO_MOVE)) {
								el.getTags().remove(IPresentationEngine.NO_MOVE);
							}
							if (!el.getTags().contains(IPresentationEngine.DRAGGABLE)) {
								el.getTags().add(IPresentationEngine.DRAGGABLE);
							}
						}
						// Force the render, and then the call of frameMeIfPossible.
						el.setToBeRendered(false);
						el.setToBeRendered(true);
					}
				}
			}
		}
		return null;
	}

}
