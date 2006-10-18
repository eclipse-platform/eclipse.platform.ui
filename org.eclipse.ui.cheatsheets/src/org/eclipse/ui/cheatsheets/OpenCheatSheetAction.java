/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.cheatsheets;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.views.CheatSheetView;

/**
 * Action for opening a cheat sheet. The cheat sheet can be specified 
 * either by a cheat sheet id or by a URL of a cheat sheet content file.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class OpenCheatSheetAction extends Action {
	private String id;
	private String name;
	private URL url;
	private String xml;
	private String basePath;

	/**
	 * Creates an action that opens the cheat sheet with the given id.
	 * The cheat sheet content file is located via the
	 * <code>org.eclipse.ui.cheatsheets.cheatSheetContent</code>
	 * extension point.
	 * 
	 * @param id the cheat sheet id
	 * @exception IllegalArgumentException if <code>id</code>
	 * is <code>null</code>
	 */
	public OpenCheatSheetAction(String id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		this.id = id;
	}
	
	/**
	 * Creates an action that opens the cheat sheet with the 
	 * given cheat sheet content file.
	 * 
	 * @param id the id to give this cheat sheet
	 * @param name the name to give this cheat sheet
	 * @param url URL of the cheat sheet content file
	 * @exception IllegalArgumentException if the parameters
	 * are <code>null</code>
	 */
	public OpenCheatSheetAction(String id, String name, URL url) {
		if (id == null || name == null || url == null) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.name = name;
		this.url = url;
	}
	
	/**
	 * Creates an action that opens a cheat sheet using
	 * XML passed in as a string.
	 * 
	 * @param id the id to give this cheat sheet
	 * @param name the name to give this cheat sheet
	 * @param xml the cheatsheet content in xml format
	 * @exception IllegalArgumentException if the parameters
	 * are <code>null</code>
	 * @deprecated
	 * @since 3.3
	 */
	public OpenCheatSheetAction(String id, String name, String xml) {
		if (id == null || name == null || xml == null) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.name = name;
		this.xml = xml;
	}
	
	/**
	 * Creates an action that opens a cheat sheet using
	 * XML passed in as a string.
	 * 
	 * @param id the id to give this cheat sheet
	 * @param name the name to give this cheat sheet
	 * @param xml the cheatsheet content in xml format
	 * @param baseURL is a URL which is only required if the cheat sheet is
	 * a composite cheat sheet which has tasks which use path parameters in which 
	 * case the paths will be relative to baseURL. May be <code>null</code>
	 * if this is not a composite cheat sheet
	 * @exception IllegalArgumentException if the parameters
	 * are <code>null</code>
	 * @since 3.3
	 */
	public OpenCheatSheetAction(String id, String name, String xml, URL baseURL) {
		if (id == null || name == null || xml == null) {
			throw new IllegalArgumentException();
		}
		this.id = id;
		this.name = name;
		this.xml = xml;
		if (baseURL !=null) {
			basePath = baseURL.toExternalForm();
		}
	}


	/* (non-javadoc)
	 * This action will try to launch the cheat sheet view and populate
	 * it with the content specified either in the URL or the content
	 * file specified in the cheatsheetContent extension point
	 * for the cheat sheet with the id passed to this action.
	 * @see IAction#run()
	 */
	public void run() {
		IWorkbench workbench = CheatSheetPlugin.getPlugin().getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		CheatSheetView view = (CheatSheetView) page.findView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
		if (view == null) {
			try {
				view = (CheatSheetView)page.showView(ICheatSheetResource.CHEAT_SHEET_VIEW_ID);
				page.activate(view);
			} catch (PartInitException pie) {
				String message = Messages.LAUNCH_SHEET_ERROR;
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, pie);
				CheatSheetPlugin.getPlugin().getLog().log(status);
				org.eclipse.jface.dialogs.ErrorDialog.openError(window.getShell(), Messages.CHEAT_SHEET_ERROR_OPENING, null, pie.getStatus());
				return;
			}
		}
		// Depending on which constructor was used open the cheat sheet view from a
		// URL, an XML string or based on the id
		if(url != null) {
			view.setInput(id, name, url);
		} else if (xml != null) {
			view.setInputFromXml(id, name, xml, basePath);
		} else {
			view.setInput(id);
		}
		page.bringToTop(view);
	}
}
