/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.NewWizardDropDownAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.TipsAndTricksAction;
import org.eclipse.ui.internal.ide.actions.BuildCleanAction;
import org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction;
import org.eclipse.ui.internal.ide.actions.ProjectPropertyDialogAction;
import org.eclipse.ui.internal.ide.actions.RetargetActionWithDefault;
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

	private static class WorkbenchCommandAction extends CommandAction implements
			ActionFactory.IWorkbenchAction {
		/**
		 * @param commandIdIn
		 * @param window
		 */
		public WorkbenchCommandAction(String commandIdIn,
				IWorkbenchWindow window) {
			super(window, commandIdIn);
		}
	}

    /**
     * Prevents instantiation.
     */
    private IDEActionFactory() {
        // do nothing
    }

    /**
     * IDE-specific workbench action (id: "addTask", commandId: "org.eclipse.ui.edit.addTask"): Add task.
     * This action is a {@link RetargetAction}. This action maintains its enablement state.
     */
    public static final ActionFactory ADD_TASK = new ActionFactory("addTask", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_ADD_TASK) { 
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.Workbench_addTask);
            action.setToolTipText(IDEWorkbenchMessages.Workbench_addTaskToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "bookmark", commandId: "org.eclipse.ui.edit.addBookmark"): Add bookmark.
     * This action is a {@link RetargetAction}. This action maintains its enablement state.
     */
    public static final ActionFactory BOOKMARK = new ActionFactory("bookmark", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_ADD_BOOKMARK) { 
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.Workbench_addBookmark);
            action.setToolTipText(IDEWorkbenchMessages.Workbench_addBookmarkToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "build", commandId: "org.eclipse.ui.project.buildAll"):
     * Incremental build. This action maintains its enablement state.
     */
    public static final ActionFactory BUILD = new ActionFactory("build",  //$NON-NLS-1$
    		IWorkbenchCommandConstants.PROJECT_BUILD_ALL) {
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());

            action.setText(IDEWorkbenchMessages.GlobalBuildAction_text);
            action.setToolTipText(IDEWorkbenchMessages.GlobalBuildAction_toolTip);
            action.setImageDescriptor(IDEInternalWorkbenchImages
                    .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC));
            action.setDisabledImageDescriptor(IDEInternalWorkbenchImages
                    .getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED));
			return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "buildClean"): Build clean.
     * This action maintains its enablement state.
     * @since 3.0
     */
    public static final ActionFactory BUILD_CLEAN = new ActionFactory(
            "buildClean") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new BuildCleanAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "buildAutomatically"): Build automatically.
     * This action maintains its enablement state.
     * @since 3.0
     */
    public static final ActionFactory BUILD_AUTOMATICALLY = new ActionFactory(
            "buildAutomatically") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new ToggleAutoBuildAction(window);
            action.setId(getId());
            action.setActionDefinitionId("org.eclipse.ui.project.buildAutomatically"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "buildProject", commandId: "org.eclipse.ui.project.buildProject"):
     * Incremental build. This action is a {@link RetargetAction}. This action maintains its enablement state.
     */
    public static final ActionFactory BUILD_PROJECT = new ActionFactory(
            "buildProject", IWorkbenchCommandConstants.PROJECT_BUILD_PROJECT) { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetActionWithDefault(getId(),
                    IDEWorkbenchMessages.Workbench_buildProject);
            action.setToolTipText(IDEWorkbenchMessages.Workbench_buildProjectToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "closeProject", commandId: "org.eclipse.ui.project.closeProject"):
     * Close project. This action is a {@link RetargetAction}. This action maintains its enablement state.
     */
    public static final ActionFactory CLOSE_PROJECT = new ActionFactory(
            "closeProject", IWorkbenchCommandConstants.PROJECT_CLOSE_PROJECT) { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.CloseResourceAction_text);
            action.setToolTipText(IDEWorkbenchMessages.CloseResourceAction_text);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "closeUnrelatedProjects", commandId: "org.eclipse.ui.project.closeUnrelatedProjects"):
     * Close unrelated projects.
     * <p>
     * This action closes all projects that are unrelated to the selected projects. A
     * project is unrelated if it is not directly or transitively referenced by one 
     * of the selected projects, and does not directly or transitively reference
     * one of the selected projects.
     * </p>
     * This action is a {@link RetargetAction} with 
     * id "closeUnrelatedProjects". This action maintains its enablement state.
     * @see IProject#getReferencedProjects()
     * @see IProject#getReferencingProjects()
     * @see IProject#close(org.eclipse.core.runtime.IProgressMonitor)
     * @since 3.2
     */
    public static final ActionFactory CLOSE_UNRELATED_PROJECTS = new ActionFactory(
            "closeUnrelatedProjects", IWorkbenchCommandConstants.PROJECT_CLOSE_UNRELATED_PROJECTS) { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text);
            action.setToolTipText(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId()); 
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "newWizardDropDown"): Opens the "new" wizard drop down, including
     * resource-specific items for Project... and Example...
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
            NewWizardMenu newWizardMenu = new NewWizardMenu(window);
            IWorkbenchAction action = new NewWizardDropDownAction(window,
                    innerAction, newWizardMenu);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "openProject", commandId: "org.eclipse.ui.project.openProject"):
     * Open project. This action is a {@link RetargetAction}. This action maintains its enablement state.
     */
    public static final ActionFactory OPEN_PROJECT = new ActionFactory(
            "openProject", IWorkbenchCommandConstants.PROJECT_OPEN_PROJECT) { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.OpenResourceAction_text);
            action.setToolTipText(IDEWorkbenchMessages.OpenResourceAction_toolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId()); 
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "openWorkspace"): Open workspace.
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
     * IDE-specific workbench action (id: "projectProperties"): Open project properties.
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
     * IDE-specific workbench action (id: "quickStart"): Quick start.
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
            IWorkbenchAction action = new org.eclipse.ui.actions.QuickStartAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "rebuildAll"): Full build.
     * This action maintains its enablement state.
     * 
     * @deprecated as of 3.0, this action no longer appears in the UI (was deprecated in 3.1)
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
     * IDE-specific workbench action (id: "rebuildProject"): Rebuild project.
     * This action is a {@link RetargetAction} with 
     * id "rebuildProject". This action maintains its enablement state.
     * 
     * @deprecated as of 3.0, this action no longer appears in the UI (was deprecated in 3.1)
     */
    public static final ActionFactory REBUILD_PROJECT = new ActionFactory(
            "rebuildProject") { //$NON-NLS-1$
        /* (non-javadoc) method declared on ActionFactory */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.Workbench_rebuildProject);
            action.setToolTipText(IDEWorkbenchMessages.Workbench_rebuildProjectToolTip);
            window.getPartService().addPartListener(action);
            action
                    .setActionDefinitionId("org.eclipse.ui.project.rebuildProject"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * IDE-specific workbench action (id: "tipsAndTricks", commandId: "org.eclipse.ui.help.tipsAndTricksAction"):
     * Tips and tricks. This action maintains its enablement state.
     */
    public static final ActionFactory TIPS_AND_TRICKS = new ActionFactory(
            "tipsAndTricks", IWorkbenchCommandConstants.HELP_TIPS_AND_TRICKS) { //$NON-NLS-1$
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
