/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.QuickStartAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.internal.NewWizardDropDownAction;
import org.eclipse.ui.internal.TipsAndTricksAction;
import org.eclipse.ui.internal.actions.ProjectPropertyDialogAction;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Access to standard actions provided by the IDE workbench (including
 * those of the generic workbench).
 * <p>
 * Most of the functionality of this class is provided by
 * static methods and fields.
 * Example usage:
 * <pre>
 * MenuManager menu = ...;
 * IDEActionFactory.IWorkbenchAction closeEditorAction
 * 	  = IDEActionFactory.CLOSE.create(window);
 * menu.add(closeEditorAction);
 * IDEActionFactory.IWorkbenchAction closeProjectAction
 * 	  = IDEActionFactory.CLOSE_PROJECT.create(window);
 * menu.add(closeProjectAction);
 * </pre>
 * </p>
 * <p>
 * Clients may declare further subclasses that provide additional
 * application-specific action factories.
 * </p>
 * 
 * @since 3.0
 */
public abstract class IDEActionFactory extends ActionFactory {

	/**
	 * Creates a new IDE workbench action factory with the given id.
	 * 
	 * @param actionId the id of actions created by this action factory
	 */
	protected IDEActionFactory(String actionId) {
		super(actionId);
	}

	/**
	 * IDE-specific workbench action: Add bookmark.
	 * This action is a {@link Retarget Retarget} action with 
	 * id "bookmark". This action maintains its enablement state.
	 */
	public static final ActionFactory BOOKMARK = new ActionFactory("bookmark") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.addBookMark")); //$NON-NLS-1$ //$NON-NLS-2$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.addBookMark.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.edit.addBookMark"); //$NON-NLS-1$
			return action;
		}
	};

	/**
	 * IDE-specific workbench action: Add task.
	 * This action is a {@link Retarget Retarget} action with 
	 * id "addTask". This action maintains its enablement state.
	 */
	public static final ActionFactory ADD_TASK = new ActionFactory("addTask") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.addTask")); //$NON-NLS-1$ //$NON-NLS-2$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.addTask.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.edit.addTask"); //$NON-NLS-1$
			return action;
		}
	};

	/**
	 * IDE-specific workbench action: Open project.
	 * This action is a {@link Retarget Retarget} action with 
	 * id "openProject". This action maintains its enablement state.
	 */
	public static final ActionFactory OPEN_PROJECT = new ActionFactory("openProject") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.openProject")); //$NON-NLS-1$ //$NON-NLS-2$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.openProject.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.project.openProject"); //$NON-NLS-1$
			return action;
		}
	};

	/**
	 * IDE-specific workbench action: Close project.
	 * This action is a {@link Retarget Retarget} action with 
	 * id "closeProject". This action maintains its enablement state.
	 */
	public static final ActionFactory CLOSE_PROJECT = new ActionFactory("closeProject") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.closeProject")); //$NON-NLS-1$ //$NON-NLS-2$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.closeProject.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.project.closeProject"); //$NON-NLS-1$
			return action;
		}
	};

	/**
	 * IDE-specific workbench action: Build project.
	 * This action is a {@link Retarget Retarget} action with 
	 * id "buildProject". This action maintains its enablement state.
	 */
	public static final ActionFactory BUILD_PROJECT = new ActionFactory("buildProject") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.buildProject")); //$NON-NLS-1$ //$NON-NLS-2$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.buildProject.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.project.buildProject"); //$NON-NLS-1$
			return action;
		}
	};

	/**
	 * IDE-specific workbench action: Rebuild project.
	 * This action is a {@link Retarget Retarget} action with 
	 * id "rebuildProject". This action maintains its enablement state.
	 */
	public static final ActionFactory REBUILD_PROJECT = new ActionFactory("rebuildProject") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			RetargetAction action = new RetargetAction(getId(), IDEWorkbenchMessages.getString("Workbench.rebuildProject")); //$NON-NLS-1$ //$NON-NLS-2$
			action.setToolTipText(IDEWorkbenchMessages.getString("Workbench.rebuildProject.ToolTip")); //$NON-NLS-1$
			window.getPartService().addPartListener(action);
			action.setActionDefinitionId("org.eclipse.ui.project.rebuildProject"); //$NON-NLS-1$
			return action;
		}
	};

	/**
	 * IDE-specific workbench action: Open project properties.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory OPEN_PROJECT_PROPERTIES = new ActionFactory("projectProperties") { //$NON-NLS-1$
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
	 * IDE-specific workbench action: New.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory NEW = new ActionFactory("new") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			IWorkbenchAction action = new NewWizardAction(window);
			action.setId(getId());
			return action;
		}
	};
	
	/**
	 * IDE-specific workbench action: Opens the "new" wizard drop down.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory NEW_WIZARD_DROP_DOWN = new ActionFactory("newWizardDropDown") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			// @issue we are creating a NEW action just to pass to NewWizardDropDownAction
			IWorkbenchAction innerAction = IDEActionFactory.NEW.create(window);
			IWorkbenchAction action = new NewWizardDropDownAction(window, innerAction);
			action.setId(getId());
			return action;
		}
	};
	
	/**
	 * IDE-specific workbench action: Import.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory IMPORT = new ActionFactory("import") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			IWorkbenchAction action = new ImportResourcesAction(window);
			action.setId(getId());
			return action;
		}
	};
	
	/**
	 * IDE-specific workbench action: Export.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory EXPORT = new ActionFactory("export") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			IWorkbenchAction action = new ExportResourcesAction(window);
			action.setId(getId());
			return action;
		}
	};
	
	/**
	 * IDE-specific workbench action: Quick start.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory QUICK_START = new ActionFactory("quickStart") { //$NON-NLS-1$
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
	 * IDE-specific workbench action: Tips and tricks.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory TIPS_AND_TRICKS = new ActionFactory("tipsAndTricks") { //$NON-NLS-1$
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
	
	/**
	 * IDE-specific workbench action: Full build.
	 * This action maintains its enablement state.
	 */
	public static final ActionFactory REBUILD_ALL = new ActionFactory("rebuildAll") { //$NON-NLS-1$
		/* (non-javadoc) method declared on ActionFactory */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			IWorkbenchAction action = new GlobalBuildAction(window, IncrementalProjectBuilder.FULL_BUILD);
			action.setId(getId());
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
			IWorkbenchAction action = new GlobalBuildAction(window, IncrementalProjectBuilder.INCREMENTAL_BUILD);
			action.setId(getId());
			return action;
		}
	};
}