/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Collapse all action (25826)
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;
import org.eclipse.ui.operations.UndoRedoActionGroup;

/**
 * The main action group for the navigator.
 * This contains a few actions and several subgroups.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
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
    
    protected UndoRedoActionGroup undoRedoGroup;

    protected WorkspaceActionGroup workspaceGroup;

    private IResourceChangeListener resourceChangeListener;

    private NewWizardMenu newWizardMenu;

    /**
     * Constructs the main action group.
     * 
     * @param navigator the navigator view
     */
    public MainActionGroup(IResourceNavigator navigator) {
        super(navigator);
        resourceChangeListener = new IResourceChangeListener() {
            public void resourceChanged(IResourceChangeEvent event) {
                handleResourceChanged(event);
            }
        };
        ResourcesPlugin.getWorkspace().addResourceChangeListener(
                resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
        makeSubGroups();
    }

    /**
     * Handles a resource changed event by updating the enablement
     * if one of the selected projects is opened or closed.
     */
    protected void handleResourceChanged(IResourceChangeEvent event) {
        ActionContext context = getContext();
        if (context == null) {
            return;
        }

        final IStructuredSelection selection = (IStructuredSelection) context
                .getSelection();
        if (ResourceSelectionUtil.allResourcesAreOfType(selection,
                IResource.PROJECT) == false) {
            return;
        }
        List sel = selection.toList();
        IResourceDelta delta = event.getDelta();
        if (delta == null) {
            return;
        }
        IResourceDelta[] projDeltas = delta
                .getAffectedChildren(IResourceDelta.CHANGED);
        for (int i = 0; i < projDeltas.length; ++i) {
            IResourceDelta projDelta = projDeltas[i];
            //changing the project open state or description will affect open/close/build action enablement
            if ((projDelta.getFlags() & (IResourceDelta.OPEN | IResourceDelta.DESCRIPTION)) != 0) {
                if (sel.contains(projDelta.getResource())) {
                    getNavigator().getSite().getShell().getDisplay().syncExec(
                            new Runnable() {
                                public void run() {
                                    addTaskAction.selectionChanged(selection);
                                    gotoGroup.updateActionBars();
                                    refactorGroup.updateActionBars();
                                    workspaceGroup.updateActionBars();
                                }
                            });
                }
            }
        }
    }

    /**
     * Makes the actions contained directly in this action group.
     */
    protected void makeActions() {
        IShellProvider provider = navigator.getSite();

        newWizardMenu = new NewWizardMenu(navigator.getSite().getWorkbenchWindow());
        addBookmarkAction = new AddBookmarkAction(provider, true);
        addTaskAction = new AddTaskAction(provider);
        propertyDialogAction = new PropertyDialogAction(provider, navigator
                .getViewer());

        importAction = new ImportResourcesAction(navigator.getSite()
                .getWorkbenchWindow());
        importAction
                .setDisabledImageDescriptor(getImageDescriptor("dtool16/import_wiz.gif")); //$NON-NLS-1$
        importAction
                .setImageDescriptor(getImageDescriptor("etool16/import_wiz.gif")); //$NON-NLS-1$		

        exportAction = new ExportResourcesAction(navigator.getSite()
                .getWorkbenchWindow());
        exportAction
                .setDisabledImageDescriptor(getImageDescriptor("dtool16/export_wiz.gif")); //$NON-NLS-1$
        exportAction
                .setImageDescriptor(getImageDescriptor("etool16/export_wiz.gif")); //$NON-NLS-1$		
        
        
        collapseAllAction = new CollapseAllAction(navigator,
                ResourceNavigatorMessages.CollapseAllAction_title);
        collapseAllAction.setToolTipText(ResourceNavigatorMessages.CollapseAllAction_toolTip);
        collapseAllAction
                .setImageDescriptor(getImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$

        toggleLinkingAction = new ToggleLinkingAction(navigator,
                ResourceNavigatorMessages.ToggleLinkingAction_text);
        toggleLinkingAction.setToolTipText(ResourceNavigatorMessages.ToggleLinkingAction_toolTip);
        toggleLinkingAction
                .setImageDescriptor(getImageDescriptor("elcl16/synced.gif"));//$NON-NLS-1$
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

                if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET
                        .equals(property)) {
                    IResourceNavigator navigator = getNavigator();
                    Object newValue = event.getNewValue();

                    if (newValue instanceof IWorkingSet) {
                        navigator.setWorkingSet((IWorkingSet) newValue);
                    } else if (newValue == null) {
                        navigator.setWorkingSet(null);
                    }
                }
            }
        };
        TreeViewer treeView = navigator.getViewer();
        Shell shell = treeView.getControl().getShell();
        workingSetGroup = new WorkingSetFilterActionGroup(shell,
                workingSetUpdater);
        workingSetGroup.setWorkingSet(navigator.getWorkingSet());
        sortAndFilterGroup = new SortAndFilterActionGroup(navigator);
        workspaceGroup = new WorkspaceActionGroup(navigator);
        IUndoContext workspaceContext= (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		undoRedoGroup= new UndoRedoActionGroup(getNavigator().getSite(), workspaceContext, true);

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
        undoRedoGroup.setContext(context);
    }

    /**
     * Fills the context menu with the actions contained in this group
     * and its subgroups.
     * 
     * @param menu the context menu
     */
    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();

        MenuManager newMenu = new MenuManager(ResourceNavigatorMessages.ResourceNavigator_new);
        menu.add(newMenu);
        newMenu.add(newWizardMenu);

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

        workspaceGroup.fillContextMenu(menu);

        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menu
                .add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
                        + "-end")); //$NON-NLS-1$
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
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),
                propertyDialogAction);
        actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(),
                addBookmarkAction);
        actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(),
                addTaskAction);

        gotoGroup.fillActionBars(actionBars);
        openGroup.fillActionBars(actionBars);
        refactorGroup.fillActionBars(actionBars);
        workingSetGroup.fillActionBars(actionBars);
        sortAndFilterGroup.fillActionBars(actionBars);
        workspaceGroup.fillActionBars(actionBars);
        undoRedoGroup.fillActionBars(actionBars);

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
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();
        propertyDialogAction.setEnabled(selection.size() == 1);
        addBookmarkAction.selectionChanged(selection);
        addTaskAction.selectionChanged(selection);

        gotoGroup.updateActionBars();
        openGroup.updateActionBars();
        refactorGroup.updateActionBars();
        workingSetGroup.updateActionBars();
        sortAndFilterGroup.updateActionBars();
        workspaceGroup.updateActionBars();
        undoRedoGroup.updateActionBars();
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
     * Extends the superclass implementation to dispose the 
     * actions in this group and its subgroups.
     */
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                resourceChangeListener);

        newWizardMenu.dispose();
        collapseAllAction.dispose();
        importAction.dispose();
        exportAction.dispose();
        propertyDialogAction.dispose();
        toggleLinkingAction.dispose();

        gotoGroup.dispose();
        openGroup.dispose();
        refactorGroup.dispose();
        sortAndFilterGroup.dispose();
        workingSetGroup.dispose();
        workspaceGroup.dispose();
        undoRedoGroup.dispose();
        super.dispose();
    }
}
