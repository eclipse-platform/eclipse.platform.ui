/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchPage;

import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.*;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

/**
 * <p>This action class can be used to launch a cheat sheet in the cheat sheets view.
 * A cheat sheet may be launched using it's id, or by passing a URL of the cheat sheet content file
 * location.</p>
 */
public class LaunchCheatSheetAction extends Action {
	CheatSheetElement element;
	URL csURL;

	/**
	 * Constructor.  The id of passed to this constructor must match the id
	 * of a cheat sheet specified in a client's implementation of the cheatsheetContent extension point.
	 * @param id the id of the cheat sheet to launch
	 */
	public LaunchCheatSheetAction(String id) {
		this.element = CheatSheetRegistryReader.getInstance().findCheatSheet(id);
	}
	
	/**
	 * This constructor can be used to launch a cheat sheet that is not specified by a plugin
	 * implementing the cheatsheetContent extension point.  
	 * @param url the url of the cheat sheet content file location.
	 * @param name the name to give this cheat sheet.
	 * @param id the unique id to assign this cheat sheet.
	 */
	public LaunchCheatSheetAction(URL url, String name, String id){
		csURL = url;
		if(name == null)
			name = ""; //$NON-NLS-1$
		element = new CheatSheetElement(name);
		if(id == null)
			id = ""; //$NON-NLS-1$
		element.setID(id);
		
		element.setContentFile(url.toString());
	}

	/**
	 * Method called when this action is run.
	 * This action will try to launch the cheat sheets view and populate it with the content 
	 * specified either in the URL or the content file specified in the cheatsheetContent extension point
	 * for the cheat sheet with the id passed to this action.
	 */
	public void run() {

		if (element == null) {
			return;
		}

		IWorkbench myworkbench = CheatSheetPlugin.getPlugin().getWorkbench();
		IWorkbenchWindow window = myworkbench.getActiveWorkbenchWindow();

		IWorkbenchPage page = window.getActivePage();
		WorkbenchPage realpage = (WorkbenchPage) page;

		CheatSheetView newview = (CheatSheetView) page.findView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
		if (newview != null) {
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
