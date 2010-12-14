/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.CloseAllSavedAction;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.NavigationHistoryAction;
import org.eclipse.ui.internal.ToggleEditorsVisibilityAction;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Access to standard actions provided by the workbench.
 * <p>
 * Most of the functionality of this class is provided by static methods and
 * fields. Example usage:
 * 
 * <pre>
 * MenuManager menu = ...;
 * ActionFactory.IWorkbenchAction closeEditorAction
 *    = ActionFactory.CLOSE.create(window);
 * menu.add(closeEditorAction);
 * </pre>
 * </p>
 * <p>
 * Clients may declare other classes that provide additional application-specific
 * action factories.
 * </p>
 * 
 * @since 3.0
 */
public abstract class ActionFactory {

    /**
     * Interface for a workbench action.
     */
    public interface IWorkbenchAction extends IAction {
        /**
         * Disposes of this action. Once disposed, this action cannot be used.
         * This operation has no effect if the action has already been
         * disposed.
         */
        public void dispose();
    }
    
    private static class WorkbenchCommandAction extends CommandAction implements
			IWorkbenchAction {
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
	 * Workbench action (id: "about", commandId: "org.eclipse.ui.help.aboutAction"): Displays the
	 * About dialog. This action maintains its enablement state.
	 */
    public static final ActionFactory ABOUT = new ActionFactory("about", //$NON-NLS-1$
    		IWorkbenchCommandConstants.HELP_ABOUT) {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
		 */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}

			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);

			action.setId(getId());
			IProduct product = Platform.getProduct();
			String productName = null;
			if (product != null) {
				productName = product.getName();
			}
			if (productName == null) {
				productName = ""; //$NON-NLS-1$
			}

			action.setText(NLS.bind(WorkbenchMessages.AboutAction_text,
					productName));
			action.setToolTipText(NLS.bind(
					WorkbenchMessages.AboutAction_toolTip, productName));
			window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.ABOUT_ACTION);
			return action;
		}
	};

	/**
	 * Workbench action (id: "activateEditor", commandId: "org.eclipse.ui.window.activateEditor"):
	 * Activate the most recently used editor. This action maintains its enablement state.
	 */
    public static final ActionFactory ACTIVATE_EDITOR = new ActionFactory(
            "activateEditor", IWorkbenchCommandConstants.WINDOW_ACTIVATE_EDITOR) {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
			action.setId(getId());
			action.setText(WorkbenchMessages.ActivateEditorAction_text);
			action
					.setToolTipText(WorkbenchMessages.ActivateEditorAction_toolTip);
			return action;
		}
    };

	/**
	 * Workbench action (id: "back", commandId: "org.eclipse.ui.navigate.back"): Back. This action
	 * is a {@link RetargetAction} with id "back". This action maintains its enablement state.
	 */
    public static final ActionFactory BACK = new ActionFactory("back", //$NON-NLS-1$
    		IWorkbenchCommandConstants.NAVIGATE_BACK) {
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new LabelRetargetAction(getId(), WorkbenchMessages.Workbench_back);
            action.setToolTipText(WorkbenchMessages.Workbench_backToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "backardHistory", commandId: "org.eclipse.ui.navigate.backwardHistory"):
	 * Backward in the navigation history. This action maintains its enablement state.
	 */
    public static final ActionFactory BACKWARD_HISTORY = new ActionFactory(
            "backardHistory", IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY) {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new NavigationHistoryAction(window, false);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "close", commandId: "org.eclipse.ui.file.close"): Close the active
	 * editor. This action maintains its enablement state.
	 */
    public static final ActionFactory CLOSE = new ActionFactory("close",//$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_CLOSE) {
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.CloseEditorAction_text);
            action.setToolTipText(WorkbenchMessages.CloseEditorAction_toolTip);
            return action;
        }
    };

	/**
	 * Workbench action (id: "closeAll", commandId: "org.eclipse.ui.file.closeAll"): Close all open
	 * editors. This action maintains its enablement state.
	 */
    public static final ActionFactory CLOSE_ALL = new ActionFactory("closeAll",//$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_CLOSE_ALL) {
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.CloseAllAction_text);
            action.setToolTipText(WorkbenchMessages.CloseAllAction_toolTip);
            return action;
        }
    };

	/**
	 * Workbench action (id: "closeOthers", commandId: "org.eclipse.ui.file.closeOthers"): Close all
	 * editors except the one that is active. This action maintains its enablement state.
	 * 
	 * @since 3.2
	 */
    public static final ActionFactory CLOSE_OTHERS = new ActionFactory("closeOthers",//$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_CLOSE_OTHERS) {
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
			action.setId(getId());
			action.setText(WorkbenchMessages.CloseOthersAction_text);
			action.setToolTipText(WorkbenchMessages.CloseOthersAction_toolTip);
			return action;
        }
    };

	/**
	 * Workbench action (id: "closeAllPerspectives", commandId: "org.eclipse.ui.window.closeAllPerspectives"):
	 * Closes all perspectives. This action maintains its enablement state.
	 */
    public static final ActionFactory CLOSE_ALL_PERSPECTIVES = new ActionFactory(
            "closeAllPerspectives", IWorkbenchCommandConstants.WINDOW_CLOSE_ALL_PERSPECTIVES) {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            
            action.setId(getId());
            action.setText(WorkbenchMessages.CloseAllPerspectivesAction_text);
            action.setToolTipText(WorkbenchMessages.CloseAllPerspectivesAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action, IWorkbenchHelpContextIds.CLOSE_ALL_PAGES_ACTION);
            
            return action;
        }
    };

	/**
	 * Workbench action (id: "closeAllSaved"): Close all open editors except those with unsaved
	 * changes. This action maintains its enablement state.
	 */
    public static final ActionFactory CLOSE_ALL_SAVED = new ActionFactory(
            "closeAllSaved") {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new CloseAllSavedAction(window);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "closePerspective", commandId: "org.eclipse.ui.window.closePerspective"):
	 * Closes the current perspective. This action maintains its enablement state.
	 */
    public static final ActionFactory CLOSE_PERSPECTIVE = new ActionFactory(
    "closePerspective", IWorkbenchCommandConstants.WINDOW_CLOSE_PERSPECTIVE) {//$NON-NLS-1$
    	/*
    	 * (non-Javadoc)
    	 * 
    	 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
    	 */
    	public IWorkbenchAction create(IWorkbenchWindow window) {
    		if (window == null) {
    			throw new IllegalArgumentException();
    		}
    		WorkbenchCommandAction action = new WorkbenchCommandAction(
    				getCommandId(), window);

    		action.setId(getId());
    		action.setText(WorkbenchMessages.
    				ClosePerspectiveAction_text);
    		action.setToolTipText(WorkbenchMessages.
    				ClosePerspectiveAction_toolTip);
    		window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.CLOSE_PAGE_ACTION);
    		return action;
    	}
    };

	/**
	 * Workbench action (id: "intro", commandId: "org.eclipse.ui.help.quickStartAction"): Activate
	 * the introduction extension. This action should not be instantiated if no intro is provided.
	 * Use code like:
	 * 
	 * <pre>
	 * if (window.getWorkbench().getIntroManager().hasIntro()) {
	 * 	introAction= ActionFactory.INTRO.create(window);
	 * 	register(introAction);
	 * }
	 * </pre>
	 */
    public static final ActionFactory INTRO = new ActionFactory("intro", //$NON-NLS-1$
    		IWorkbenchCommandConstants.HELP_WELCOME) {
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);

			action.setId(getId());
			action.setText(IntroMessages.Intro_action_text);
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.INTRO_ACTION);
			IntroDescriptor introDescriptor = ((Workbench) window.getWorkbench())
					.getIntroDescriptor();
			if (introDescriptor != null)
				action.setImageDescriptor(introDescriptor.getImageDescriptor());

			return action;
        }
    };

	/**
	 * Workbench action (id: "copy", commandId: "org.eclipse.ui.edit.copy"): Copy. This action is a
	 * {@link RetargetAction} with id "copy". This action maintains its enablement state.
	 */
    public static final ActionFactory COPY = new ActionFactory("copy", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_COPY) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_copy);
            action.setToolTipText(WorkbenchMessages.Workbench_copyToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            ISharedImages sharedImages = window.getWorkbench()
                    .getSharedImages();
            action.setImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
            action.setDisabledImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "cut", commandId: "org.eclipse.ui.edit.cut"): Cut. This action is a
	 * {@link RetargetAction} with id "cut". This action maintains its enablement state.
	 */
    public static final ActionFactory CUT = new ActionFactory("cut", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_CUT) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_cut);
            action.setToolTipText(WorkbenchMessages.Workbench_cutToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            ISharedImages sharedImages = window.getWorkbench()
                    .getSharedImages();
            action.setImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
            action.setDisabledImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "delete", commandId: "org.eclipse.ui.edit.delete"): Delete. This action
	 * is a {@link RetargetAction} with id "delete". This action maintains its enablement state.
	 */
    public static final ActionFactory DELETE = new ActionFactory("delete", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_DELETE) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_delete);
            action.setToolTipText(WorkbenchMessages.Workbench_deleteToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            action.enableAccelerator(false);
            window.getWorkbench().getHelpSystem().setHelp(action,
                    IWorkbenchHelpContextIds.DELETE_RETARGET_ACTION);
            ISharedImages sharedImages = window.getWorkbench()
                    .getSharedImages();
            action.setImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
            action
                    .setDisabledImageDescriptor(sharedImages
                            .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "editActionSets", commandId: "org.eclipse.ui.window.customizePerspective"):
	 * Edit the action sets. This action maintains its enablement state.
	 */
    public static final ActionFactory EDIT_ACTION_SETS = new ActionFactory(
            "editActionSets", IWorkbenchCommandConstants.WINDOW_CUSTOMIZE_PERSPECTIVE) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.EditActionSetsAction_text);
            action.setToolTipText(WorkbenchMessages.EditActionSetsAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.EDIT_ACTION_SETS_ACTION);
            
            return action;
        }
    };

	/**
	 * Workbench action (id: "export", commandId: "org.eclipse.ui.file.export"): Opens the export
	 * wizard. This action maintains its enablement state.
	 */
    public static final ActionFactory EXPORT = new ActionFactory("export", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_EXPORT) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.ExportResourcesAction_fileMenuText);
            action.setToolTipText(WorkbenchMessages.ExportResourcesAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.EXPORT_ACTION);
            action.setImageDescriptor(WorkbenchImages
                    .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ));
            return action;
        }
        
    };

	/**
	 * Workbench action (id: "find", commandId: "org.eclipse.ui.edit.findReplace"): Find. This
	 * action is a {@link RetargetAction} with id "find". This action maintains its enablement
	 * state.
	 */
    public static final ActionFactory FIND = new ActionFactory("find", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_findReplace);
            action.setToolTipText(WorkbenchMessages.Workbench_findReplaceToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            // Find's images are commented out due to a conflict with Search.
            // See bug 16412.
            //		action.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SEARCH_SRC));
            //		action.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SEARCH_SRC_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "forward", commandId: "org.eclipse.ui.navigate.forward"): Forward. This
	 * action is a {@link RetargetAction} with id "forward". This action maintains its enablement
	 * state.
	 */
    public static final ActionFactory FORWARD = new ActionFactory("forward", //$NON-NLS-1$
    		IWorkbenchCommandConstants.NAVIGATE_FORWARD) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_forward);
            action.setToolTipText(WorkbenchMessages.Workbench_forwardToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "forwardHistory", commandId: "org.eclipse.ui.navigate.forwardHistory"):
	 * Forward in the navigation history. This action maintains its enablement state.
	 */
    public static final ActionFactory FORWARD_HISTORY = new ActionFactory(
            "forwardHistory", IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new NavigationHistoryAction(window, true);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "goInto", commandId: "org.eclipse.ui.navigate.goInto"): Go Into. This
	 * action is a {@link RetargetAction} with id "goInto". This action maintains its enablement
	 * state.
	 */
    public static final ActionFactory GO_INTO = new ActionFactory("goInto", //$NON-NLS-1$
    		IWorkbenchCommandConstants.NAVIGATE_GO_INTO) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_goInto);
            action.setToolTipText(WorkbenchMessages.Workbench_goIntoToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "import", commandId: "org.eclipse.ui.file.import"): Opens the import
	 * wizard. This action maintains its enablement state.
	 */
    public static final ActionFactory IMPORT = new ActionFactory("import", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_IMPORT) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.ImportResourcesAction_text);
            action.setToolTipText(WorkbenchMessages.ImportResourcesAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.IMPORT_ACTION);
            action.setImageDescriptor(WorkbenchImages
                    .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ));
            return action;

        }
    };

	/**
	 * Workbench action (id: "lockToolBar"): Lock/unlock the workbench window tool bar. This action
	 * maintains its enablement state.
	 */
	public static final ActionFactory LOCK_TOOL_BAR = new ActionFactory(
			"lockToolBar", IWorkbenchCommandConstants.WINDOW_LOCK_TOOLBAR) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setId(getId());
			action.setText(WorkbenchMessages.LockToolBarAction_text);
			action.setToolTipText(WorkbenchMessages.LockToolBarAction_toolTip);
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.LOCK_TOOLBAR_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "maximize", commandId: "org.eclipse.ui.window.maximizePart"):
	 * Maximize/restore the active part. This action maintains its enablement state.
	 */
    public static final ActionFactory MAXIMIZE = new ActionFactory("maximize", //$NON-NLS-1$
    		IWorkbenchCommandConstants.WINDOW_MAXIMIZE_ACTIVE_VIEW_OR_EDITOR) {
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setToolTipText(WorkbenchMessages.MaximizePartAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.MAXIMIZE_PART_ACTION);
            
            return action;
        }
    };

	/**
	 * Workbench action (id: "minimize", commandId: "org.eclipse.ui.window.minimizePart"): Minimizes
	 * the active part. This action maintains its enablement state.
	 * 
	 * @since 3.1
	 */
    public static final ActionFactory MINIMIZE = new ActionFactory("minimize", //$NON-NLS-1$
    		IWorkbenchCommandConstants.WINDOW_MINIMIZE_ACTIVE_VIEW_OR_EDITOR) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
			action.setId(getId());
			action.setToolTipText(WorkbenchMessages.MinimizePartAction_toolTip);
			window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.MINIMIZE_PART_ACTION);
			return action;
        }
    };

	/**
	 * Workbench action (id: "move", commandId: "org.eclipse.ui.edit.move"): Move. This action is a
	 * {@link RetargetAction} with id "move". This action maintains its enablement state.
	 */
    public static final ActionFactory MOVE = new ActionFactory("move", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_MOVE) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_move);
            action.setToolTipText(WorkbenchMessages.Workbench_moveToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "new", commandId: "org.eclipse.ui.newWizard"): Opens the new wizard
	 * dialog. This action maintains its enablement state.
	 */
    public static final ActionFactory NEW = new ActionFactory("new", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_NEW) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            ISharedImages images = window.getWorkbench().getSharedImages();
            action.setImageDescriptor(images
                    .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
            action.setDisabledImageDescriptor(images
                    .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
            action.setText(WorkbenchMessages.NewWizardAction_text);
            action.setToolTipText(WorkbenchMessages.NewWizardAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.NEW_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "newWizardDropDown"): Drop-down action which shows shows the new wizard
	 * drop down, or opens the new wizard dialog when pressed. For use in the toolbar. This action
	 * maintains its enablement state.
	 * 
	 * @since 3.1
	 */
    public static final ActionFactory NEW_WIZARD_DROP_DOWN = new ActionFactory(
            "newWizardDropDown") { //$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new NewWizardDropDownAction(window);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "next", commandId: "org.eclipse.ui.navigate.next"): Next. This action
	 * is a {@link RetargetAction} with id "next". This action maintains its enablement state.
	 */
    public static final ActionFactory NEXT = new ActionFactory("next", //$NON-NLS-1$
    		IWorkbenchCommandConstants.NAVIGATE_NEXT) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_next);
            action.setToolTipText(WorkbenchMessages.Workbench_nextToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "nextEditor", commandId: "org.eclipse.ui.window.nextEditor"): Next
	 * editor. This action maintains its enablement state.
	 * <p>
	 * <code>NEXT_EDITOR</code> and <code>PREVIOUS_EDITOR</code> form a cycle action pair. For a
	 * given window, use {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory NEXT_EDITOR = new ActionFactory(
            "nextEditor", IWorkbenchCommandConstants.WINDOW_NEXT_EDITOR) {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			IWorkbenchAction action = new WorkbenchCommandAction(
					getCommandId(), window);

			action.setId(getId());
			action.setText(WorkbenchMessages.CycleEditorAction_next_text);
			action.setToolTipText(WorkbenchMessages.CycleEditorAction_next_toolTip);
            // @issue missing action ids
			window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.CYCLE_EDITOR_FORWARD_ACTION);
            
			return action;
		}
    };

	/**
	 * Workbench action (id: "nextPart", commandId: "org.eclipse.ui.window.nextView"): Next part.
	 * This action maintains its enablement state.
	 * <p>
	 * <code>NEXT_PART</code> and <code>PREVIOUS_PART</code> form a cycle action pair. For a given
	 * window, use {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory NEXT_PART = new ActionFactory("nextPart", //$NON-NLS-1$
    		IWorkbenchCommandConstants.WINDOW_NEXT_VIEW) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.CyclePartAction_next_text);
			action.setToolTipText(WorkbenchMessages.CyclePartAction_next_toolTip);
			// @issue missing action ids
			window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.CYCLE_PART_FORWARD_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "nextPerspective", commandId: "org.eclipse.ui.window.nextPerspective"):
	 * Next perspective. This action maintains its enablement state.
	 * <p>
	 * <code>NEXT_PERSPECTIVE</code> and <code>PREVIOUS_PERSPECTIVE</code> form a cycle action pair.
	 * For a given window, use {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory NEXT_PERSPECTIVE = new ActionFactory(
            "nextPerspective", IWorkbenchCommandConstants.WINDOW_NEXT_PERSPECTIVE) {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.CyclePerspectiveAction_next_text);
            action.setToolTipText(WorkbenchMessages.CyclePerspectiveAction_next_toolTip);
            // @issue missing action ids
            window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.CYCLE_PERSPECTIVE_FORWARD_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "openNewWindow", commandId: "org.eclipse.ui.window.newWindow"): Open a
	 * new workbench window. This action maintains its enablement state.
	 */
    public static final ActionFactory OPEN_NEW_WINDOW = new ActionFactory(
            "openNewWindow", IWorkbenchCommandConstants.WINDOW_NEW_WINDOW) {//$NON-NLS-1$
        
    	
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.OpenInNewWindowAction_text);
            action.setToolTipText(WorkbenchMessages.OpenInNewWindowAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
            		IWorkbenchHelpContextIds.OPEN_NEW_WINDOW_ACTION);
            return action;
        }
        
    };

	/**
	 * Workbench action (id: "paste", commandId: "org.eclipse.ui.edit.paste"): Paste. This action is
	 * a {@link RetargetAction} with id "paste". This action maintains its enablement state.
	 */
    public static final ActionFactory PASTE = new ActionFactory("paste", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_PASTE) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_paste);
            action.setToolTipText(WorkbenchMessages.Workbench_pasteToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            ISharedImages sharedImages = window.getWorkbench()
                    .getSharedImages();
            action.setImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
            action.setDisabledImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "preferences", commandId: "org.eclipse.ui.window.preferences"):
	 * Displays the Preferences dialog. This action maintains its enablement state.
	 */
    public static final ActionFactory PREFERENCES = new ActionFactory(
            "preferences", IWorkbenchCommandConstants.WINDOW_PREFERENCES) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

            WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.OpenPreferences_text);
			action.setToolTipText(WorkbenchMessages.OpenPreferences_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
            		IWorkbenchHelpContextIds.OPEN_PREFERENCES_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "previous", commandId: "org.eclipse.ui.navigate.previous"): Previous.
	 * This action is a {@link RetargetAction} with id "previous". This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory PREVIOUS = new ActionFactory("previous", //$NON-NLS-1$
    		IWorkbenchCommandConstants.NAVIGATE_PREVIOUS) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_previous);
            action.setToolTipText(WorkbenchMessages.Workbench_previousToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "previousEditor", commandId: "org.eclipse.ui.window.previousEditor"):
	 * Previous editor. This action maintains its enablement state.
	 * <p>
	 * <code>NEXT_EDITOR</code> and <code>PREVIOUS_EDITOR</code> form a cycle action pair. For a
	 * given window, use {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory PREVIOUS_EDITOR = new ActionFactory(
            "previousEditor", IWorkbenchCommandConstants.WINDOW_PREVIOUS_EDITOR) {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			IWorkbenchAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.CycleEditorAction_prev_text);
            action.setToolTipText(WorkbenchMessages.CycleEditorAction_prev_toolTip);
            // @issue missing action ids
            window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.CYCLE_EDITOR_BACKWARD_ACTION);

            return action;
        }
    };

	/**
	 * Workbench action (id: "previousPart", commandId: "org.eclipse.ui.window.previousView"):
	 * Previous part. This action maintains its enablement state.
	 * <p>
	 * <code>NEXT_PART</code> and <code>PREVIOUS_PART</code> form a cycle action pair. For a given
	 * window, use {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory PREVIOUS_PART = new ActionFactory(
            "previousPart", IWorkbenchCommandConstants.WINDOW_PREVIOUS_VIEW) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
			action.setText(WorkbenchMessages.CyclePartAction_prev_text);
			action.setToolTipText(WorkbenchMessages.CyclePartAction_prev_toolTip);
			// @issue missing action ids
			window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.CYCLE_PART_BACKWARD_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "previousPerspective", commandId: "org.eclipse.ui.window.previousPerspective"):
	 * Previous perspective. This action maintains its enablement state.
	 * <p>
	 * <code>NEXT_PERSPECTIVE</code> and <code>PREVIOUS_PERSPECTIVE</code> form a cycle action pair.
	 * For a given window, use {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory PREVIOUS_PERSPECTIVE = new ActionFactory(
            "previousPerspective", IWorkbenchCommandConstants.WINDOW_PREVIOUS_PERSPECTIVE) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.CyclePerspectiveAction_prev_text);
            action.setToolTipText(WorkbenchMessages.CyclePerspectiveAction_prev_toolTip);
            // @issue missing action ids
            window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.CYCLE_PERSPECTIVE_BACKWARD_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "print", commandId: "org.eclipse.ui.file.print"): Print. This action is
	 * a {@link RetargetAction} with id "print". This action maintains its enablement state.
	 */
    public static final ActionFactory PRINT = new ActionFactory("print", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_PRINT) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_print);
            action.setToolTipText(WorkbenchMessages.Workbench_printToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            action
                    .setImageDescriptor(WorkbenchImages
                            .getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
            action
                    .setDisabledImageDescriptor(WorkbenchImages
                            .getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "properties", commandId: "org.eclipse.ui.file.properties"): Properties.
	 * This action is a {@link RetargetAction} with id "properties". This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory PROPERTIES = new ActionFactory(
            "properties", IWorkbenchCommandConstants.FILE_PROPERTIES) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_properties);
            action.setToolTipText(WorkbenchMessages.Workbench_propertiesToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "quit", commandId: "org.eclipse.ui.file.exit"): Quit (close the
	 * workbench). This action maintains its enablement state.
	 */
    public static final ActionFactory QUIT = new ActionFactory("quit", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_EXIT) {
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.Exit_text);
            action.setToolTipText(WorkbenchMessages.Exit_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.QUIT_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "redo", commandId: "org.eclipse.ui.edit.redo"): Redo. This action is a
	 * {@link RetargetAction} with id "redo". This action maintains its enablement state.
	 */
    public static final ActionFactory REDO = new ActionFactory("redo", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_REDO) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            LabelRetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_redo);
            action.setToolTipText(WorkbenchMessages.Workbench_redoToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            ISharedImages sharedImages = window.getWorkbench()
                    .getSharedImages();
            action.setImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
            action.setDisabledImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "refresh", commandId: "org.eclipse.ui.file.refresh"): Refresh. This
	 * action is a {@link RetargetAction} with id "refresh". This action maintains its enablement
	 * state.
	 */
    public static final ActionFactory REFRESH = new ActionFactory("refresh", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_REFRESH) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_refresh);
            action.setToolTipText(WorkbenchMessages.Workbench_refreshToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "rename", commandId: "org.eclipse.ui.edit.rename"): Rename. This action
	 * is a {@link RetargetAction} with id "rename". This action maintains its enablement state.
	 */
    public static final ActionFactory RENAME = new ActionFactory("rename", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_RENAME) {
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_rename);
            action.setToolTipText(WorkbenchMessages.Workbench_renameToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "resetPerspective", commandId: "org.eclipse.ui.window.resetPerspective"):
	 * Resets the current perspective. This action maintains its enablement state.
	 */
    public static final ActionFactory RESET_PERSPECTIVE = new ActionFactory(
            "resetPerspective", IWorkbenchCommandConstants.WINDOW_RESET_PERSPECTIVE) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);

			action.setId(getId());
			action.setText(WorkbenchMessages.ResetPerspective_text);
			action.setToolTipText(WorkbenchMessages.ResetPerspective_toolTip);
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
			return action;
        }
    };

	/**
	 * Workbench action (id: "revert", commandId: "org.eclipse.ui.file.revert"): Revert. This action
	 * is a {@link RetargetAction} with id "revert". This action maintains its enablement state.
	 */
    public static final ActionFactory REVERT = new ActionFactory("revert", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_REVERT) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_revert);
            action.setToolTipText(WorkbenchMessages.Workbench_revertToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "save", commandId: "org.eclipse.ui.file.save"): Save the active editor.
	 * This action maintains its enablement state.
	 */
    public static final ActionFactory SAVE = new ActionFactory("save", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_SAVE) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setText(WorkbenchMessages.SaveAction_text);
			action.setToolTipText(WorkbenchMessages.SaveAction_toolTip);
            action.setId(getId());
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.SAVE_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "saveAll", commandId: "org.eclipse.ui.file.saveAll"): Save all open
	 * editors with unsaved changes. This action maintains its enablement state.
	 */
    public static final ActionFactory SAVE_ALL = new ActionFactory("saveAll", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_SAVE_ALL) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			IWorkbenchAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setText(WorkbenchMessages.SaveAll_text);
			action.setToolTipText(WorkbenchMessages.SaveAll_toolTip);
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.SAVE_ALL_ACTION);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "saveAs", commandId: "org.eclipse.ui.file.saveAs"): Save As for the
	 * active editor. This action maintains its enablement state.
	 */
    public static final ActionFactory SAVE_AS = new ActionFactory("saveAs", //$NON-NLS-1$
    		IWorkbenchCommandConstants.FILE_SAVE_AS) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			IWorkbenchAction action = new WorkbenchCommandAction(getCommandId(), window);
			action.setText(WorkbenchMessages.SaveAs_text);
			action.setToolTipText(WorkbenchMessages.SaveAs_toolTip);
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.SAVE_AS_ACTION);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "savePerspective", commandId: "org.eclipse.ui.window.savePerspective"):
	 * Save the current perspective. This action maintains its enablement state.
	 */
    public static final ActionFactory SAVE_PERSPECTIVE = new ActionFactory(
            "savePerspective", IWorkbenchCommandConstants.WINDOW_SAVE_PERSPECTIVE_AS) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);

			action.setId(getId());
			action.setText(WorkbenchMessages.SavePerspective_text);
			action.setToolTipText(WorkbenchMessages.SavePerspective_toolTip);
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
			return action;
		}
    };

	/**
	 * Workbench action (id: "selectAll", commandId: "org.eclipse.ui.edit.selectAll"): Select All.
	 * This action is a {@link RetargetAction} with id "selectAll". This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory SELECT_ALL = new ActionFactory(
            "selectAll", IWorkbenchCommandConstants.EDIT_SELECT_ALL) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new RetargetAction(getId(),WorkbenchMessages.Workbench_selectAll);
            action.setToolTipText(WorkbenchMessages.Workbench_selectAllToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "showEditor"): Show/hide the editor area. This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory SHOW_EDITOR = new ActionFactory(
            "showEditor") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new ToggleEditorsVisibilityAction(window);
            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "showOpenEditors"): Show a list of open (and recently closed) editors.
	 * This action maintains its enablement state.
	 */
    public static final ActionFactory SHOW_OPEN_EDITORS = new ActionFactory(
            "showOpenEditors") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.window.switchToEditor", window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.WorkbenchEditorsAction_label);
            // @issue missing action id
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.WORKBENCH_EDITORS_ACTION);
            return action;
        }
    };

	/**
	 * Workbench action (id: "showWorkbookEditors"): Shows a list of open editors in the current or
	 * last active workbook.
	 */
    public static final ActionFactory SHOW_WORKBOOK_EDITORS = new ActionFactory(
            "showWorkBookEditors") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            
            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.window.openEditorDropDown", window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.WorkbookEditorsAction_label);
            
            return action;
        }
    };

	/**
	 * Workbench action (id: "showQuickAccess"): Shows a list of UI elements like editors, views,
	 * perspectives etc.
	 * 
	 * @since 3.3
	 */
	public static final ActionFactory SHOW_QUICK_ACCESS = new ActionFactory(
			"showQuickAccess") { //$NON-NLS-1$

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
		 */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.window.quickAccess", window); //$NON-NLS-1$
			action.setId(getId());
			action.setText(WorkbenchMessages.QuickAccessAction_text);
			action.setToolTipText(WorkbenchMessages.QuickAccessAction_toolTip);
			return action;
		}

	};

	/**
	 * Workbench action (id: "showPartPaneMenu"): Show the part pane menu. This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory SHOW_PART_PANE_MENU = new ActionFactory(
            "showPartPaneMenu") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.window.showSystemMenu",window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.ShowPartPaneMenuAction_text);
            action.setToolTipText(WorkbenchMessages.ShowPartPaneMenuAction_toolTip);
            return action;
        }
    };

	/**
	 * Workbench action (id: "showViewMenu", commandId: "org.eclipse.ui.window.showViewMenu"): Show
	 * the view menu. This action maintains its enablement state.
	 */
    public static final ActionFactory SHOW_VIEW_MENU = new ActionFactory(
            "showViewMenu", IWorkbenchCommandConstants.WINDOW_SHOW_VIEW_MENU) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
            action.setId(getId());
            action.setText(WorkbenchMessages.ShowViewMenuAction_text);
            action.setToolTipText(WorkbenchMessages.ShowViewMenuAction_toolTip);
            return action;
        }
    };

	/**
	 * Workbench action (id: "undo", commandId: "org.eclipse.ui.edit.undo"): Undo. This action is a
	 * {@link RetargetAction} with id "undo". This action maintains its enablement state.
	 */
    public static final ActionFactory UNDO = new ActionFactory("undo", //$NON-NLS-1$
    		IWorkbenchCommandConstants.EDIT_UNDO) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            LabelRetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_undo);
            action.setToolTipText(WorkbenchMessages.Workbench_undoToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            ISharedImages sharedImages = window.getWorkbench()
                    .getSharedImages();
            action.setImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
            action.setDisabledImageDescriptor(sharedImages
                    .getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
            return action;
        }
    };

	/**
	 * Workbench action (id: "up", commandId: "org.eclipse.ui.navigate.up"): Up. This action is a
	 * {@link RetargetAction} with id "up". This action maintains its enablement state.
	 */
    public static final ActionFactory UP = new ActionFactory("up", //$NON-NLS-1$
    		IWorkbenchCommandConstants.NAVIGATE_UP) {
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            RetargetAction action = new LabelRetargetAction(getId(),WorkbenchMessages.Workbench_up);
            action.setToolTipText(WorkbenchMessages.Workbench_upToolTip);
            window.getPartService().addPartListener(action);
            action.setActionDefinitionId(getCommandId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "helpContents", commandId: "org.eclipse.ui.help.helpContents"): Open
	 * the help contents. This action is always enabled.
	 */
    public static final ActionFactory HELP_CONTENTS = new ActionFactory(
            "helpContents", IWorkbenchCommandConstants.HELP_HELP_CONTENTS) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);

			// support for allowing a product to override the text for the
			// action
			String overrideText = PrefUtil.getAPIPreferenceStore().getString(
					IWorkbenchPreferenceConstants.HELP_CONTENTS_ACTION_TEXT);
			if ("".equals(overrideText)) { //$NON-NLS-1$
				action.setText(WorkbenchMessages.HelpContentsAction_text);
				action.setToolTipText(WorkbenchMessages.HelpContentsAction_toolTip);
			} else {
				action.setText(overrideText);
				action.setToolTipText(Action.removeMnemonics(overrideText));
			}
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.HELP_CONTENTS_ACTION);
			action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "helpSearch", commandId: "org.eclipse.ui.help.helpSearch"): Open the
	 * help search. This action is always enabled.
	 * 
	 * @since 3.1
	 */
    public static final ActionFactory HELP_SEARCH = new ActionFactory(
            "helpSearch", IWorkbenchCommandConstants.HELP_HELP_SEARCH) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);

			// support for allowing a product to override the text for the
			// action
			String overrideText = PrefUtil.getAPIPreferenceStore().getString(
					IWorkbenchPreferenceConstants.HELP_SEARCH_ACTION_TEXT);
			if ("".equals(overrideText)) { //$NON-NLS-1$
				action.setText(WorkbenchMessages.HelpSearchAction_text);
				action.setToolTipText(WorkbenchMessages.HelpSearchAction_toolTip);
			} else {
				action.setText(overrideText);
				action.setToolTipText(Action.removeMnemonics(overrideText));
			}
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.HELP_SEARCH_ACTION);
			action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "dynamicHelp", commandId: "org.eclipse.ui.help.dynamicHelp"): Open the
	 * dynamic help. This action is always enabled.
	 * 
	 * @since 3.1
	 */
    public static final ActionFactory DYNAMIC_HELP = new ActionFactory(
            "dynamicHelp", IWorkbenchCommandConstants.HELP_DYNAMIC_HELP) {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

			WorkbenchCommandAction action = new WorkbenchCommandAction(getCommandId(), window);

			// support for allowing a product to override the text for the
			// action
			String overrideText = PrefUtil.getAPIPreferenceStore().getString(
					IWorkbenchPreferenceConstants.DYNAMIC_HELP_ACTION_TEXT);
			if ("".equals(overrideText)) { //$NON-NLS-1$
				action.setText(WorkbenchMessages.DynamicHelpAction_text);
				action.setToolTipText(WorkbenchMessages.DynamicHelpAction_toolTip);
			} else {
				action.setText(overrideText);
				action.setToolTipText(Action.removeMnemonics(overrideText));
			}
			window.getWorkbench().getHelpSystem()
					.setHelp(action, IWorkbenchHelpContextIds.DYNAMIC_HELP_ACTION);

            action.setId(getId());
            return action;
        }
    };

	/**
	 * Workbench action (id: "openPerspectiveDialog", commandId: "org.eclipse.ui.perspectives.showPerspective"):
	 * Open the Open Perspective dialog. This action is always enabled.
	 * 
	 * @since 3.1
	 */
    public static final ActionFactory OPEN_PERSPECTIVE_DIALOG = new ActionFactory(
            "openPerspectiveDialog", IWorkbenchCommandConstants.PERSPECTIVES_SHOW_PERSPECTIVE) {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
			action.setId(getId());
	        action.setText(WorkbenchMessages.OpenPerspectiveDialogAction_text);
	        action.setToolTipText(WorkbenchMessages.OpenPerspectiveDialogAction_tooltip);
	        action.setImageDescriptor(WorkbenchImages.getImageDescriptor(
	              IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE));

			return action;
        }
    };

	/**
	 * Workbench action (id: "newEditor", commandId: "org.eclipse.ui.window.newEditor"): Open a new
	 * editor on the active editor's input. This action maintains its enablement state.
	 * 
	 * @since 3.1
	 */
    public static final ActionFactory NEW_EDITOR = new ActionFactory(
            "newEditor", IWorkbenchCommandConstants.WINDOW_NEW_EDITOR) {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

			WorkbenchCommandAction action = new WorkbenchCommandAction(
					getCommandId(), window);
			action.setId(getId());
			action.setText(WorkbenchMessages.NewEditorAction_text);
			action.setToolTipText(WorkbenchMessages.NewEditorAction_tooltip);

			return action;
        }
    };

	/**
	 * Workbench action (id: "toggleCoolbar"): Toggle the visibility of the coolbar and perspective
	 * switcher. This will only enable visibility of the coolbar and perspective bar if the window
	 * advisor creating the window allowed for their visibility initially.
	 * 
	 * @since 3.3
	 */
	public static final ActionFactory TOGGLE_COOLBAR = new ActionFactory(
			"toggleCoolbar") { //$NON-NLS-1$

		public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.ToggleCoolbarAction", window); //$NON-NLS-1$
			action.setId(getId());
			// set the default text - this will be updated by the handler
			action.setText(WorkbenchMessages.ToggleCoolbarVisibilityAction_hide_text);
			action.setToolTipText(WorkbenchMessages.ToggleCoolbarVisibilityAction_toolTip);
			return action;
		}
	};
    
    /**
	 * Establishes bi-direction connections between the forward and backward
	 * actions of a cycle pair.
	 * <p>
	 * Example usage:
	 * 
	 * <pre>
	 * ActionFactory.IWorkbenchAction nextEditorAction = ActionFactory.NEXT_EDITOR
	 * 		.create(window);
	 * ActionFactory.IWorkbenchAction previousEditorAction = ActionFactory.PREVIOUS_EDITOR
	 * 		.create(window);
	 * ActionFactory.linkCycleActionPair(nextEditorAction, previousEditorAction);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param next
	 *            the action that moves forward
	 * @param previous
	 *            the action that moves backward
	 */
    public static void linkCycleActionPair(IWorkbenchAction next,
            IWorkbenchAction previous) {
    }

    /**
     * Id of actions created by this action factory.
     */
    private final String actionId;
    
    /**
     * Optional ID for this action.
     */
    private final String commandId;

    /**
     * Creates a new workbench action factory with the given id.
     * 
     * @param actionId
     *            the id of actions created by this action factory
     */
    protected ActionFactory(String actionId) {
    	this(actionId, null);
    }

	/**
	 * Create a new workbench action factory with the given IDs.
	 * 
	 * @param actionId
	 *            the id of actions created by this action factory
	 * @param commandId
	 *            the matching command id
	 * @since 3.5
	 */
    protected ActionFactory(String actionId, String commandId) {
    	this.actionId = actionId;
    	this.commandId = commandId;
    }

    /**
     * Creates a new standard action for the given workbench window. The action
     * has an id as specified by the particular factory.
     * <p>
     * Actions automatically register listeners against the workbench window so
     * that they can keep their enablement state up to date. Ordinarily, the
     * window's references to these listeners will be dropped automatically
     * when the window closes. However, if the client needs to get rid of an
     * action while the window is still open, the client must call
     * {@link IWorkbenchAction#dispose dispose}to give the action an
     * opportunity to deregister its listeners and to perform any other
     * cleanup.
     * </p>
     * 
     * @param window
     *            the workbench window
     * @return the workbench action
     */
    public abstract IWorkbenchAction create(IWorkbenchWindow window);

    /**
     * Returns the id of this action factory.
     * 
     * @return the id of actions created by this action factory
     */
    public String getId() {
        return actionId;
    }

	/**
	 * Return the command id of this action factory.
	 * 
	 * @return the command id of the action created by this action factory. May
	 *         be <code>null</code>.
	 * @since 3.5
	 */
    public String getCommandId() {
    	return commandId;
    }
}
