/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.WorkbenchPage; // illegal
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

/**
 * Action for opening a cheat sheet. The cheat sheet can be specified 
 * either by a cheat sheet id or by a URL of a cheat sheet content file.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * TODO (lorne) - should rename this to "OpenCheatSheetAction" to be consistent with terms used elsewhere
 * TODO (lorne) - marked as final
 * @since 3.0
 */
public final class LaunchCheatSheetAction extends Action {
	CheatSheetElement element;
	URL csURL;

	/**
	 * Creates an action that opens the cheat sheet with the given id.
	 * The cheat sheet content file is located via the
	 * <code>org.eclipse.ui.cheatsheet.cheatSheetContent</code>
	 * extension point.
	 * 
	 * @param id the cheat sheet id
	 * @exception IllegalArgumentException if <code>id</code>
	 * is <code>null</code>
	 */
	public LaunchCheatSheetAction(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		this.element = CheatSheetRegistryReader.getInstance().findCheatSheet(id);
	}
	
	/**
	 * Creates an action that opens the cheat sheet with the 
	 * given cheat sheet content file.
	 * 
	 * @param url URL of the cheat sheet content file
	 * @param name the name to give this cheat sheet; 
	 * <code>null</code> is equivalent to the empty string
	 * @param id the id to give this cheat sheet;
	 * <code>null</code> is equivalent to the empty string
	 * @exception IllegalArgumentException if <code>url</code>
	 * is <code>null</code>
	 */
	public LaunchCheatSheetAction(URL url, String name, String id){
		if (url == null) {
			throw new IllegalArgumentException();
		}
		this.csURL = url;
		if (name == null) {
			name = ""; //$NON-NLS-1$
		}
		element = new CheatSheetElement(name);
		if (id == null) {
			id = ""; //$NON-NLS-1$
		}
		element.setID(id);
		
		element.setContentFile(url.toString());
	}

	/* (non-javadoc)
	 * This action will try to launch the cheat sheet view and populate
	 * it with the content specified either in the URL or the content
	 * file specified in the cheatsheetContent extension point
	 * for the cheat sheet with the id passed to this action.
	 * @see IAction#run()
	 */
	public void run() {

		/* TODO (lorne) - action fails silently when id does not correspond to a known cheatsheet
		 * it would better to report some kind of error in this case.
		 */
		if (element == null) {
			return;
		}

		IWorkbench myworkbench = CheatSheetPlugin.getPlugin().getWorkbench();
		IWorkbenchWindow window = myworkbench.getActiveWorkbenchWindow();

		IWorkbenchPage page = window.getActivePage();
		// TODO (lorne) - the plug-in must not reference internal classes (like WorkbenchPage) of other plug-ins
		WorkbenchPage realpage = (WorkbenchPage) page;

		CheatSheetView newview = (CheatSheetView) page.findView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
		if (newview != null) {
			// TODO (lorne) - won't setContent clobber a cheat sheet execution already in progress?
			newview.setContent(element);
			page.bringToTop(newview);
		} else {
			try {
//TODO: Port problem, update the following to open the view correctly.
//				IViewReference viewref = realpage.getViewFactory().createView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
//				CheatSheetView view = (CheatSheetView) viewref.getPart(true);
				CheatSheetView view = (CheatSheetView)realpage.showView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
//				IViewReference viewref = realpage.findViewReference(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
//				realpage.addFastView(viewref);
				page.activate(view);
				view.setContent(element);
			} catch (PartInitException pie) {
				String message = CheatSheetPlugin.getResourceString(ICheatSheetResource.LAUNCH_SHEET_ERROR);
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, pie);
				CheatSheetPlugin.getPlugin().getLog().log(status);
				org.eclipse.jface.dialogs.ErrorDialog.openError(new Shell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEET_ERROR_OPENING), null, pie.getStatus());
				return;
			}
		}

		// Update most recently used cheat sheets list.
		CheatSheetPlugin.getPlugin().getCheatSheetHistory().add(element);
	}
}
