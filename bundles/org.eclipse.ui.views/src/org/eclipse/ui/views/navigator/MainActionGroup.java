/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Collapse all action
 *      Sebastian Davids <sdavids@gmx.de> - Images for menu items
************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * The main action group for the navigator.
 * This contains a few actions and several subgroups.
 */
public class MainActionGroup extends ResourceNavigatorActionGroup {

	protected AddBookmarkAction addBookmarkAction;
	protected AddTaskAction addTaskAction;	
	protected PropertyDialogAction propertyDialogAction;
	protected ImportResourcesAction importAction;
	protected ExportResourcesAction exportAction;
	protected CollapseAllAction collapseAllAction;
	protected ToggleLinkingAction toggleLinkingAction;
	
	protected GotoActionGroup gotoGroup;
	protected OpenActionGroup openGroup;
	protected RefactorActionGroup refactorGroup;
	protected WorkingSetFilterActionGroup workingSetGroup;
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
		
		addBookmarkAction = new AddBookmarkAction(shell);
		addTaskAction = new AddTaskAction(shell);		
		propertyDialogAction =
			new PropertyDialogAction(shell, navigator.getViewer());
		
		importAction = new ImportResourcesAction(navigator.getSite().getWorkbenchWindow());
		importAction.setDisabledImageDescriptor(getImageDescriptor("dtool16/import_wiz.gif")); //$NON-NLS-1$
		importAction.setImageDescriptor(getImageDescriptor("etool16/import_wiz.gif")); //$NON-NLS-1$		
		importAction.setHoverImageDescriptor(getImageDescriptor("ctool16/import_wiz.gif")); //$NON-NLS-1$

		exportAction = new ExportResourcesAction(navigator.getSite().getWorkbenchWindow());
		exportAction.setDisabledImageDescriptor(getImageDescriptor("dtool16/export_wiz.gif")); //$NON-NLS-1$
		exportAction.setImageDescriptor(getImageDescriptor("etool16/export_wiz.gif")); //$NON-NLS-1$
		exportAction.setHoverImageDescriptor(getImageDescriptor("ctool16/export_wiz.gif")); //$NON-NLS-1$

		collapseAllAction = new CollapseAllAction(navigator, ResourceNavigatorMessages.getString("CollapseAllAction.title"));//$NON-NLS-1$
		collapseAllAction.setToolTipText(ResourceNavigatorMessages.getString("CollapseAllAction.toolTip")); //$NON-NLS-1$
		collapseAllAction.setImageDescriptor(getImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$
		collapseAllAction.setHoverImageDescriptor(getImageDescriptor("clcl16/collapseall.gif")); //$NON-NLS-1$

		toggleLinkingAction = new ToggleLinkingAction(
			navigator, 
			ResourceNavigatorMessages.getString("ToggleLinkingAction.text")); //$NON-NLS-1$
		toggleLinkingAction.setToolTipText(
			ResourceNavigatorMessages.getString("ToggleLinkingAction.toolTip")); //$NON-NLS-1$
		toggleLinkingAction.setImageDescriptor(getImageDescriptor("elcl16/synced.gif"));//$NON-NLS-1$
		toggleLinkingAction.setHoverImageDescriptor(getImageDescriptor("clcl16/synced.gif"));//$NON-NLS-1$
	}
	
	/**
	 * Makes the sub action groups.
	 */
	protected void makeSubGroups() {
		gotoGroup = new GotoActionGroup(navigator);
		openGroup = new OpenActionGroup(navigator);
		refactorGroup = new RefactorActionGroup(navigator);
		IPropertyChangeListener workingSetUpdater = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				
				if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
					IResourceNavigator navigator = getNavigator();
					Object newValue = event.getNewValue();
					
					if (newValue instanceof IWorkingSet) {	
						navigator.setWorkingSet((IWorkingSet) newValue);
					}
					else 
					if (newValue == null) {
						navigator.setWorkingSet(null);
					}
				}
			}
		};
		TreeViewer treeView = navigator.getViewer(); 
		Shell shell = treeView.getControl().getShell();
		workingSetGroup = new WorkingSetFilterActionGroup(shell, workingSetUpdater);
		workingSetGroup.setWorkingSet(navigator.getWorkingSet());
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
		
		gotoGroup.fillContextMenu(menu);
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
		workingSetGroup.fillActionBars(actionBars);
		sortAndFilterGroup.fillActionBars(actionBars);
		workspaceGroup.fillActionBars(actionBars);
		
		IMenuManager menu = actionBars.getMenuManager();
		menu.add(toggleLinkingAction);
		
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(collapseAllAction);		
		toolBar.add(toggleLinkingAction);
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
		workingSetGroup.updateActionBars();
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
		workingSetGroup.dispose();
		sortAndFilterGroup.dispose();
		workspaceGroup.dispose();
		super.dispose();
	}
}
