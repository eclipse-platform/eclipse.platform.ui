/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.QuickStartAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.NewWizardDropDownAction;
import org.eclipse.ui.internal.ide.TipsAndTricksAction;
import org.eclipse.ui.internal.ide.actions.BuildCleanAction;
import org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction;
import org.eclipse.ui.internal.ide.actions.ProjectPropertyDialogAction;
import org.eclipse.ui.internal.ide.actions.ToggleAutoBuildAction;

/**
 * Access to standard actions provided by the IDE workbench (including
 * those of the generic workbench).
 * <p>
 * The functionality of this class is provided by static fields.
 * Example usage:
 * <pre>
 * MenuManager menu = ...;
 * ActionFactory.IWorkbenchAction closeProjectAction
 * 	  = IDEActionFactory.CLOSE_PROJECT.create(window);
 * menu.add(closeProjectAction);
 * </pre>
 * </p>
 * 
 * @since 3.0
 */
public final class IDEActionFactory {

    /**
     * Prevents instantiation.
     */
    private IDEActionFactory() {
        // do nothing
    }

    /**
     * IDE-specific workbench action: Add task.
     * This action is a {@link RetargetAction} with 
     * id "addTask". This action maintains its enablement state.
     */
    public static final ActionFactory ADD_TASK = new ActionFactory("addTask") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),
                    IDEWorkbenchMessages.getString("Workbench.addTask")); //$NON-NLS-1$ //$NON-NLS-2$
            action.setToolTipText(IDEWorkbenchMessages
                    .getString("Workbench.addTaskToolTip")); //$NON-NLS-1$
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId("org.eclipse.ui.edit.addTask"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Add bookmark.
     * This action is a {@link RetargetAction} with 
     * id "bookmark". This action maintains its enablement state.
     */
    public static final ActionFactory BOOKMARK = new ActionFactory("bookmark") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),
                    IDEWorkbenchMessages.getString("Workbench.addBookmark")); //$NON-NLS-1$ //$NON-NLS-2$
            action.setToolTipText(IDEWorkbenchMessages
                    .getString("Workbench.addBookmarkToolTip")); //$NON-NLS-1$
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId("org.eclipse.ui.edit.addBookmark"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Incremental build.
     * This action maintains its enablement state.
     */
    public static final ActionFactory BUILD = new ActionFactory("build") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new GlobalBuildAction(window,
                    IncrementalProjectBuilder.INCREMENTAL_BUILD);
        }
    };

    /**
     * IDE-specific workbench action: Build clean
     * This action maintains its enablement state.
     */
    public static final ActionFactory BUILD_CLEAN = new ActionFactory(
            "buildClean") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new BuildCleanAction(window);
        }
    };

    /**
     * IDE-specific workbench action: Build automaticaly
     * This action maintains its enablement state.
     */
    public static final ActionFactory BUILD_AUTOMATICALLY = new ActionFactory(
            "buildAutomatically") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            return new ToggleAutoBuildAction(window);
        }
    };

    /**
     * IDE-specific workbench action: Incremental build.
     * This action is a {@link RetargetAction} with 
     * id "buildProject". This action maintains its enablement state.
     */
    public static final ActionFactory BUILD_PROJECT = new ActionFactory(
            "buildProject") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),
                    IDEWorkbenchMessages.getString("Workbench.buildProject")); //$NON-NLS-1$
            action.setToolTipText(IDEWorkbenchMessages
                    .getString("Workbench.buildProjectToolTip")); //$NON-NLS-1$
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId("org.eclipse.ui.project.buildProject"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Close project.
     * This action is a {@link RetargetAction} with 
     * id "closeProject". This action maintains its enablement state.
     */
    public static final ActionFactory CLOSE_PROJECT = new ActionFactory(
            "closeProject") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),
                    IDEWorkbenchMessages.getString("Workbench.closeProject")); //$NON-NLS-1$ //$NON-NLS-2$
            action.setToolTipText(IDEWorkbenchMessages
                    .getString("Workbench.closeProjectToolTip")); //$NON-NLS-1$
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId("org.eclipse.ui.project.closeProject"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Opens the "new" wizard drop down.
     * This action maintains its enablement state.
     */
    public static final ActionFactory NEW_WIZARD_DROP_DOWN = new ActionFactory(
            "newWizardDropDown") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            // @issue we are creating a NEW action just to pass to NewWizardDropDownAction
            IWorkbenchAction innerAction = ActionFactory.NEW.create(window);
            IWorkbenchAction action = new NewWizardDropDownAction(window,
                    innerAction);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Open project.
     * This action is a {@link RetargetAction} with 
     * id "openProject". This action maintains its enablement state.
     */
    public static final ActionFactory OPEN_PROJECT = new ActionFactory(
            "openProject") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),
                    IDEWorkbenchMessages.getString("Workbench.openProject")); //$NON-NLS-1$ //$NON-NLS-2$
            action.setToolTipText(IDEWorkbenchMessages
                    .getString("Workbench.openProjectToolTip")); //$NON-NLS-1$
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId("org.eclipse.ui.project.openProject"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Open workspace.
     * This action maintains its enablement state.
     */
    public static final ActionFactory OPEN_WORKSPACE = new ActionFactory(
            "openWorkspace") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new OpenWorkspaceAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Open project properties.
     * This action maintains its enablement state.
     */
    public static final ActionFactory OPEN_PROJECT_PROPERTIES = new ActionFactory(
            "projectProperties") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new ProjectPropertyDialogAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Quick start.
     * This action maintains its enablement state.
     * 
     * @deprecated the IDE now uses the new intro mechanism
     */
    public static final ActionFactory QUICK_START = new ActionFactory(
            "quickStart") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new QuickStartAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Full build.
     * This action maintains its enablement state.
     */
    public static final ActionFactory REBUILD_ALL = new ActionFactory(
            "rebuildAll") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new GlobalBuildAction(window,
                    IncrementalProjectBuilder.FULL_BUILD);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Rebuild project.
     * This action is a {@link RetargetAction} with 
     * id "rebuildProject". This action maintains its enablement state.
     */
    public static final ActionFactory REBUILD_PROJECT = new ActionFactory(
            "rebuildProject") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),
                    IDEWorkbenchMessages.getString("Workbench.rebuildProject")); //$NON-NLS-1$ //$NON-NLS-2$
            action.setToolTipText(IDEWorkbenchMessages
                    .getString("Workbench.rebuildProjectToolTip")); //$NON-NLS-1$
            window.getPartService().addPartListener(action);
            action
                    .setActionDefinitionId("org.eclipse.ui.project.rebuildProject"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action: Tips and tricks.
     * This action maintains its enablement state.
     */
    public static final ActionFactory TIPS_AND_TRICKS = new ActionFactory(
            "tipsAndTricks") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new TipsAndTricksAction(window);
            action.setId(getId());
            return action;
        }
    };

}