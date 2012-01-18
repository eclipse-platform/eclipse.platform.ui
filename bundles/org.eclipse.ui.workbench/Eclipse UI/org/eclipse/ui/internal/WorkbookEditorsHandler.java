/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
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
				ISWTResourceUtilities utils = (ISWTResourceUtilities) HandlerUtil
						.getVariableChecked(event, IResourceUtilities.class.getName());
				EPartService partService = (EPartService) HandlerUtil.getVariableChecked(event,
						EPartService.class.getName());
				final BasicPartList editorList = new BasicPartList(workbenchWindow.getShell(),
						SWT.ON_TOP, SWT.V_SCROLL | SWT.H_SCROLL, partService, activeStack, utils);
				editorList.setInput();

				Point size = editorList.computeSizeHint();
				editorList.setSize(size.x, size.y);

				Rectangle bounds = workbenchWindow.getShell().getBounds();
				int x = (bounds.width / 2) + bounds.x;
				int y = (bounds.height / 2) + bounds.y;
				x = x - (size.x / 2);
				y = y - (size.y / 2);

				// adjust for monitor bounds as necessary
				Monitor monitor = editorList.getShell().getMonitor();
				Rectangle monitorBounds = monitor.getClientArea();
				if (x + size.x > monitorBounds.x + monitorBounds.width) {
					x = monitorBounds.x + monitorBounds.width - size.x;
				}
				if (y + size.y > monitorBounds.y + monitorBounds.height) {
					y = monitorBounds.y + monitorBounds.height - size.y;
				}

				editorList.setLocation(new Point(x, y));
				editorList.setVisible(true);
				editorList.setFocus();
				editorList.getShell().addListener(SWT.Deactivate, new Listener() {
					public void handleEvent(Event event) {
						editorList.getShell().getDisplay().asyncExec(new Runnable() {
							public void run() {
								editorList.dispose();
							}
						});
					}
				});
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
