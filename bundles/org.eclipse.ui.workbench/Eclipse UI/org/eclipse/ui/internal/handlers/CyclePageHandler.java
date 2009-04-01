/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.util.Geometry;

import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.CycleBaseHandler;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.PageSwitcher;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Displays a dialog for cycling through pages of a view. A view may implement
 * its pages however it wishes. As long as the view creates a
 * {@link PageSwitcher} object, {@link CyclePageHandler} will handle the cycling
 * of pages.
 * 
 * @since 3.4
 * 
 */
public class CyclePageHandler extends CycleBaseHandler {

	/**
	 * The character limit before text is truncated.
	 */
	private static final int TEXT_LIMIT = 65;
	private PageSwitcher pageSwitcher;
	private LocalResourceManager lrm;

	public CyclePageHandler(PageSwitcher pageSwitcher) {
		this.pageSwitcher = pageSwitcher;
	}

	protected void addItems(Table table, WorkbenchPage page) {
		Object[] pages = pageSwitcher.getPages();
		for (int i = 0; i < pages.length; i++) {
			TableItem item = null;
			item = new TableItem(table, SWT.NONE);
			ImageDescriptor imageDescriptor = pageSwitcher
					.getImageDescriptor(pages[i]);
			if (imageDescriptor != null) {
				if (lrm == null) {
					lrm = new LocalResourceManager(JFaceResources
							.getResources());
				}
				item.setImage(lrm.createImage(imageDescriptor));
			}
			item.setData(pages[i]);
			String name = pageSwitcher.getName(pages[i]);
			if (name.length() > TEXT_LIMIT) {
				name = name.substring(0, TEXT_LIMIT) + "..."; //$NON-NLS-1$
			}
			item.setText(name);
		}
	}
	
	protected int getCurrentItemIndex() {
		return pageSwitcher.getCurrentPageIndex();
	}

	protected ParameterizedCommand getBackwardCommand() {
		final ICommandService commandService = (ICommandService) window
				.getWorkbench().getService(ICommandService.class);
		final Command command = commandService
.getCommand(IWorkbenchCommandConstants.NAVIGATE_PREVIOUS_PAGE);
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = (ICommandService) window
				.getWorkbench().getService(ICommandService.class);
		final Command command= commandService.getCommand(IWorkbenchCommandConstants.NAVIGATE_NEXT_PAGE);
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	protected String getTableHeader(IWorkbenchPart activePart) {
		if (activePart instanceof WorkbenchPart) {
			return ((WorkbenchPart) activePart).getPartName();
		}

		return activePart.getTitle();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (event.getCommand().getId().equals(IWorkbenchCommandConstants.NAVIGATE_NEXT_PAGE)) {
			gotoDirection = true;
		} else {
			gotoDirection = false;
		}
		super.execute(event);
		if (lrm != null) {
			lrm.dispose();
			lrm = null;
		}
		return null;
	}

	protected void setDialogLocation(final Shell dialog,
			IWorkbenchPart activePart) {
		if (dialog == null)
			return;

		// Default to center on the display
		Point dlgAnchor = Geometry.centerPoint(dialog.getDisplay().getBounds());
		
		// Center the dialog within the activePart's pane (if any)
		if (activePart != null) {
			WorkbenchPart wbPart = (WorkbenchPart) activePart;
			PartSite site = (PartSite) wbPart.getSite();
			Control paneCtrl = site.getPane().getControl();

			// Get the center of the view pane's control
			Rectangle viewBounds = paneCtrl.getBounds();
			Point vCenter = Geometry.centerPoint(viewBounds);
			
			// Map it to the display
			dlgAnchor = paneCtrl.getParent().toDisplay(vCenter);
		}
		
		// Offset the point by half the dialog size
		Rectangle dialogBounds = dialog.getBounds();
		dlgAnchor.x -= (dialogBounds.width / 2);
		dlgAnchor.y -= (dialogBounds.height / 2);

		dialog.setLocation(dlgAnchor);
	}

	public void dispose() {
		super.dispose();
		this.pageSwitcher = null;
	}

	protected void activate(IWorkbenchPage page, Object selectedItem) {
		if (selectedItem == null) {
			return;
		}

		pageSwitcher.activatePage(selectedItem);
	}
}
