package org.eclipse.ui.views.navigator;

/**********************************************************************
Copyright (c) 2000, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Sebastian Davids <sdavids@gmx.de> - Collapse all action
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main action group for the navigator.
 * This contains a few actions and several subgroups.
 */
public class MainActionGroup extends ResourceNavigatorActionGroup {

	protected AddBookmarkAction addBookmarkAction;
	protected AddTaskAction addTaskAction;	
	protected NewWizardAction newWizardAction;
	protected PropertyDialogAction propertyDialogAction;
	protected ImportResourcesAction importAction;
	protected ExportResourcesAction exportAction;
	protected CollapseAllAction collapseAllAction;
	
	protected GotoActionGroup gotoGroup;
	protected OpenActionGroup openGroup;
	protected RefactorActionGroup refactorGroup;
	protected SortAndFilterActionGroup sortAndFilterGroup;
	protected WorkspaceActionGroup workspaceGroup;

	/**
	 * Constructs the main action group.
	 */
	public MainActionGroup(IResourceNavigator navigator) {
		super(navigator);
		makeSubGroups();
	}

	/**
	 * Makes the actions contained directly in this action group.
	 */
	protected void makeActions() {
		Shell shell = navigator.getSite().getShell();
		IWorkbench workbench = navigator.getSite().getWorkbenchWindow().getWorkbench();
		addBookmarkAction = new AddBookmarkAction(shell);
		addTaskAction = new AddTaskAction(shell);		
		newWizardAction = new NewWizardAction();
		propertyDialogAction =
			new PropertyDialogAction(shell, navigator.getViewer());
		importAction = new ImportResourcesAction(workbench);
		exportAction = new ExportResourcesAction(workbench);
		collapseAllAction = new CollapseAllAction(navigator, ResourceNavigatorMessages.getString("CollapseAllAction.title")); //$NON-NLS-1$
		collapseAllAction.setToolTipText(ResourceNavigatorMessages.getString("CollapseAllAction.toolTip")); //$NON-NLS-1$
		collapseAllAction.setImageDescriptor(getImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$
	}
	
	/**
	 * Makes the sub action groups.
	 */
	protected void makeSubGroups() {
		gotoGroup = new GotoActionGroup(navigator);
		openGroup = new OpenActionGroup(navigator);
		refactorGroup = new RefactorActionGroup(navigator);
		sortAndFilterGroup = new SortAndFilterActionGroup(navigator);
		workspaceGroup = new WorkspaceActionGroup(navigator);
	}
	
	/**
	 * Extends the superclass implementation to set the context in the subgroups.
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		gotoGroup.setContext(context);
		openGroup.setContext(context);
		refactorGroup.setContext(context);
		sortAndFilterGroup.setContext(context);
		workspaceGroup.setContext(context);
	}
	
	/**
	 * Fills the context menu with the actions contained in this group
	 * and its subgroups.
	 * 
	 * @param menu the context menu
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		boolean onlyFilesSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FILE);
		

		MenuManager newMenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.new")); //$NON-NLS-1$
		menu.add(newMenu);
		new NewWizardMenu(newMenu, navigator.getSite().getWorkbenchWindow(), false);
		
		openGroup.fillContextMenu(menu);
		menu.add(new Separator());
		
		refactorGroup.fillContextMenu(menu);
		menu.add(new Separator());
		
		menu.add(importAction);
		menu.add(exportAction);
		importAction.selectionChanged(selection);
		exportAction.selectionChanged(selection);
		menu.add(new Separator());
				
		if (onlyFilesSelected) {
			addBookmarkAction.selectionChanged(selection);
			menu.add(addBookmarkAction);
		}
		menu.add(new Separator());
		
		workspaceGroup.fillContextMenu(menu);
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		menu.add(new Separator());

		if (selection.size() == 1) {
			propertyDialogAction.selectionChanged(selection);
			menu.add(propertyDialogAction);
		}
	}
	
	/**
	 * Returns the image descriptor with the given relative path.
	 */
	private ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
			URL installURL = plugin.getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
			
	/**
	 * Adds the actions in this group and its subgroups to the action bars.
	 */
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.PROPERTIES,
			propertyDialogAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.BOOKMARK,
			addBookmarkAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.ADD_TASK,
			addTaskAction);
			
		gotoGroup.fillActionBars(actionBars);
		openGroup.fillActionBars(actionBars);
		refactorGroup.fillActionBars(actionBars);
		sortAndFilterGroup.fillActionBars(actionBars);
		workspaceGroup.fillActionBars(actionBars);
		
		IToolBarManager toolBar = actionBars.getToolBarManager();
		
		toolBar.add(new Separator());
		toolBar.add(collapseAllAction);		
	}
	
	/**
	 * Updates the actions which were added to the action bars,
	 * delegating to the subgroups as necessary.
	 */
	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		propertyDialogAction.setEnabled(selection.size() == 1);
		addBookmarkAction.selectionChanged(selection);
		addTaskAction.selectionChanged(selection);		
		
		gotoGroup.updateActionBars();
		openGroup.updateActionBars();
		refactorGroup.updateActionBars();
		sortAndFilterGroup.updateActionBars();
		workspaceGroup.updateActionBars();
	} 
	
	/**
	 * Runs the default action (open file) by delegating the open group.
	 */
	public void runDefaultAction(IStructuredSelection selection) {
		openGroup.runDefaultAction(selection);
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action,
 	 * delegating to the subgroups as necessary.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		refactorGroup.handleKeyPressed(event);
		workspaceGroup.handleKeyPressed(event);
	}
	
	/**
	 * Extends the superclass implementation to dispose the subgroups.
	 */
	public void dispose() {
		gotoGroup.dispose();
		openGroup.dispose();
		refactorGroup.dispose();
		sortAndFilterGroup.dispose();
		workspaceGroup.dispose();
		super.dispose();
	}
}
