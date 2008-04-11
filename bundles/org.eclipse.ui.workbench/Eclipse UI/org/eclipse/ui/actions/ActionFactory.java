/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.Map;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.CloseAllSavedAction;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.IntroAction;
import org.eclipse.ui.internal.LockToolBarAction;
import org.eclipse.ui.internal.NavigationHistoryAction;
import org.eclipse.ui.internal.OpenPreferencesAction;
import org.eclipse.ui.internal.ResetPerspectiveAction;
import org.eclipse.ui.internal.SaveAction;
import org.eclipse.ui.internal.SaveAllAction;
import org.eclipse.ui.internal.SaveAsAction;
import org.eclipse.ui.internal.SavePerspectiveAction;
import org.eclipse.ui.internal.ToggleEditorsVisibilityAction;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.actions.DynamicHelpAction;
import org.eclipse.ui.internal.actions.HelpContentsAction;
import org.eclipse.ui.internal.actions.HelpSearchAction;
import org.eclipse.ui.services.IServiceLocator;

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
		
		public WorkbenchCommandAction(String commandIdIn, Map parameterMap,
				IServiceLocator serviceLocator) {
			super(serviceLocator, commandIdIn, parameterMap);
		}
	}

    /**
     * Workbench action: Displays the About dialog. This action maintains its
     * enablement state.
     */
    public static final ActionFactory ABOUT = new ActionFactory("about") { //$NON-NLS-1$

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
					"org.eclipse.ui.help.aboutAction", window); //$NON-NLS-1$

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
	 * Workbench action (id "activateEditor"): Activate the most recently used
	 * editor. This action maintains its enablement state.
	 */
    public static final ActionFactory ACTIVATE_EDITOR = new ActionFactory(
            "activateEditor") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.window.activateEditor", window); //$NON-NLS-1$
			action.setId(getId());
			action.setText(WorkbenchMessages.ActivateEditorAction_text);
			action
					.setToolTipText(WorkbenchMessages.ActivateEditorAction_toolTip);
			return action;
		}
    };

    /**
	 * Workbench action (id "back"): Back. This action is a
	 * {@link RetargetAction} with id "back". This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory BACK = new ActionFactory("back") {//$NON-NLS-1$
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
            action.setActionDefinitionId("org.eclipse.ui.navigate.back"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "backardHistory"): Backward in the navigation
     * history. This action maintains its enablement state.
     */
    public static final ActionFactory BACKWARD_HISTORY = new ActionFactory(
            "backardHistory") {//$NON-NLS-1$
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
     * Workbench action (id "close"): Close the active editor. This action
     * maintains its enablement state.
     */
    public static final ActionFactory CLOSE = new ActionFactory("close") {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.file.close",window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.CloseEditorAction_text);
            action.setToolTipText(WorkbenchMessages.CloseEditorAction_toolTip);
            return action;
        }
    };

    /**
     * Workbench action (id "closeAll"): Close all open editors. This action
     * maintains its enablement state.
     */
    public static final ActionFactory CLOSE_ALL = new ActionFactory("closeAll") {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.file.closeAll",window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.CloseAllAction_text);
            action.setToolTipText(WorkbenchMessages.CloseAllAction_toolTip);
            return action;
        }
    };
    
    /**
     * Workbench action (id "closeOthers"): Close all editors except the one that 
	 * is active. This action maintains its enablement state.
	 * 
	 * @since 3.2
     */
    public static final ActionFactory CLOSE_OTHERS = new ActionFactory("closeOthers") {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.file.closeOthers", window); //$NON-NLS-1$
			action.setId(getId());
			action.setText(WorkbenchMessages.CloseOthersAction_text);
			action.setToolTipText(WorkbenchMessages.CloseOthersAction_toolTip);
			return action;
        }
    };

    /**
	 * Workbench action (id "closeAllPerspectives"): Closes all perspectives.
	 * This action maintains its enablement state.
	 */
    public static final ActionFactory CLOSE_ALL_PERSPECTIVES = new ActionFactory(
            "closeAllPerspectives") {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction
            ("org.eclipse.ui.window.closeAllPerspectives", window); //$NON-NLS-1$
            
            action.setId(getId());
            action.setText(WorkbenchMessages.CloseAllPerspectivesAction_text);
            action.setToolTipText(WorkbenchMessages.CloseAllPerspectivesAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action, IWorkbenchHelpContextIds.CLOSE_ALL_PAGES_ACTION);
            
            return action;
        }
    };

    /**
     * Workbench action (id "closeAllSaved"): Close all open editors except
     * those with unsaved changes. This action maintains its enablement state.
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
     * Workbench action (id "closePerspective"): Closes the current
     * perspective. This action maintains its enablement state.
     */
    public static final ActionFactory CLOSE_PERSPECTIVE = new ActionFactory(
    "closePerspective") {//$NON-NLS-1$
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
    				"org.eclipse.ui.window.closePerspective", window); //$NON-NLS-1$

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
     * Workbench action (id "intro"): Activate the introduction extension.
     */
    public static final ActionFactory INTRO = new ActionFactory("intro") {//$NON-NLS-1$
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new IntroAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "copy"): Copy. This action is a
     * {@link RetargetAction} with id "copy". This action maintains
     * its enablement state.
     */
    public static final ActionFactory COPY = new ActionFactory("copy") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.copy"); //$NON-NLS-1$
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
     * Workbench action (id "cut"): Cut. This action is a
     * {@link RetargetAction} with id "cut". This action maintains
     * its enablement state.
     */
    public static final ActionFactory CUT = new ActionFactory("cut") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.cut"); //$NON-NLS-1$
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
     * Workbench action (id "delete"): Delete. This action is a
     * {@link RetargetAction} with id "delete". This action maintains
     * its enablement state.
     */
    public static final ActionFactory DELETE = new ActionFactory("delete") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.delete"); //$NON-NLS-1$
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
     * Workbench action (id "editActionSets"): Edit the action sets. This
     * action maintains its enablement state.
     */
    public static final ActionFactory EDIT_ACTION_SETS = new ActionFactory(
            "editActionSets") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.window.customizePerspective", window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.EditActionSetsAction_text);
            action.setToolTipText(WorkbenchMessages.EditActionSetsAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.EDIT_ACTION_SETS_ACTION);
            
            return action;
        }
    };

    /**
     * Workbench action (id "export"): Opens the export wizard. This action
     * maintains its enablement state.
     */
    public static final ActionFactory EXPORT = new ActionFactory("export") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.file.export", window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.ExportResourcesAction_text);
            action.setToolTipText(WorkbenchMessages.ExportResourcesAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.EXPORT_ACTION);
            action.setImageDescriptor(WorkbenchImages
                    .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ));
            return action;
        }
        
    };

    /**
     * Workbench action (id "find"): Find. This action is a
     * {@link RetargetAction} with id "find". This action maintains
     * its enablement state.
     */
    public static final ActionFactory FIND = new ActionFactory("find") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.findReplace"); //$NON-NLS-1$
            // Find's images are commented out due to a conflict with Search.
            // See bug 16412.
            //		action.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SEARCH_SRC));
            //		action.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_SEARCH_SRC_DISABLED));
            return action;
        }
    };

    /**
     * Workbench action (id "forward"): Forward. This action is a
     * {@link RetargetAction} with id "forward". This action
     * maintains its enablement state.
     */
    public static final ActionFactory FORWARD = new ActionFactory("forward") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.navigate.forward"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "forwardHistory"): Forward in the navigation
     * history. This action maintains its enablement state.
     */
    public static final ActionFactory FORWARD_HISTORY = new ActionFactory(
            "forwardHistory") {//$NON-NLS-1$
        
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
     * Workbench action (id "goInto"): Go Into. This action is a
     * {@link RetargetAction} with id "goInto". This action maintains
     * its enablement state.
     */
    public static final ActionFactory GO_INTO = new ActionFactory("goInto") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.navigate.goInto"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "import"): Opens the import wizard. This action
     * maintains its enablement state.
     */
    public static final ActionFactory IMPORT = new ActionFactory("import") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            
            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.file.import", window); //$NON-NLS-1$
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
     * Workbench action (id "lockToolBar"): Lock/unlock the workbench window
     * tool bar. This action maintains its enablement state.
     */
    public static final ActionFactory LOCK_TOOL_BAR = new ActionFactory(
            "lockToolBar") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new LockToolBarAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "maximize"): Maximize/restore the active part. This
     * action maintains its enablement state.
     */
    public static final ActionFactory MAXIMIZE = new ActionFactory("maximize") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.window.maximizePart", window); //$NON-NLS-1$
            action.setId(getId());
            action.setToolTipText(WorkbenchMessages.MaximizePartAction_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.MAXIMIZE_PART_ACTION);
            
            return action;
        }
    };

    /**
     * Workbench action (id "minimize"): Minimizes the active part. This
     * action maintains its enablement state.
     * 
     * @since 3.1
     */
    public static final ActionFactory MINIMIZE = new ActionFactory("minimize") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.window.minimizePart", window); //$NON-NLS-1$
			action.setId(getId());
			action.setToolTipText(WorkbenchMessages.MinimizePartAction_toolTip);
			window.getWorkbench().getHelpSystem().setHelp(action,
					IWorkbenchHelpContextIds.MINIMIZE_PART_ACTION);
			return action;
        }
    };
    
    /**
	 * Workbench action (id "move"): Move. This action is a
	 * {@link RetargetAction} with id "move". This action maintains its
	 * enablement state.
	 */
    public static final ActionFactory MOVE = new ActionFactory("move") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.move"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "new"): Opens the new wizard dialog. This action maintains
     * its enablement state.
     */
    public static final ActionFactory NEW = new ActionFactory("new") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.newWizard", window); //$NON-NLS-1$
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
     * Workbench action (id "newWizardDropDown"): Drop-down action which shows shows the new wizard drop down, 
     * or opens the new wizard dialog when pressed.  For use in the toolbar.
     * This action maintains its enablement state.
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
     * Workbench action (id "next"): Next. This action is a
     * {@link RetargetAction} with id "next". This action maintains
     * its enablement state.
     */
    public static final ActionFactory NEXT = new ActionFactory("next") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.navigate.next"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "nextEditor"): Next editor. This action maintains
     * its enablement state.
     * <p>
     * <code>NEXT_EDITOR</code> and <code>PREVIOUS_EDITOR</code> form a
     * cycle action pair. For a given window, use
     * {@link ActionFactory#linkCycleActionPair
     * ActionFactory.linkCycleActionPair</code>} to connect the two.
     * </p>
     */
    public static final ActionFactory NEXT_EDITOR = new ActionFactory(
            "nextEditor") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			IWorkbenchAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.window.nextEditor", //$NON-NLS-1$
					window);

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
	 * Workbench action (id "nextPart"): Next part. This action maintains its
	 * enablement state.
	 * <p>
	 * <code>NEXT_PART</code> and <code>PREVIOUS_PART</code> form a cycle
	 * action pair. For a given window, use
	 * {@link ActionFactory#linkCycleActionPair
	 * ActionFactory.linkCycleActionPair</code>} to connect the two.
	 * </p>
	 */
    public static final ActionFactory NEXT_PART = new ActionFactory("nextPart") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.window.nextView",window); //$NON-NLS-1$
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
     * Workbench action (id "nextPerspective"): Next perspective. This action
     * maintains its enablement state.
     * <p>
     * <code>NEXT_PERSPECTIVE</code> and <code>PREVIOUS_PERSPECTIVE</code>
     * form a cycle action pair. For a given window, use
     * {@link ActionFactory#linkCycleActionPair
     * ActionFactory.linkCycleActionPair</code>} to connect the two.
     * </p>
     */
    public static final ActionFactory NEXT_PERSPECTIVE = new ActionFactory(
            "nextPerspective") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.window.nextPerspective",window); //$NON-NLS-1$
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
     * Workbench action (id "openNewWindow"): Open a new workbench window. This
     * action maintains its enablement state.
     */
    public static final ActionFactory OPEN_NEW_WINDOW = new ActionFactory(
            "openNewWindow") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new OpenInNewWindowAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "paste"): Paste. This action is a
     * {@link RetargetAction} with id "paste". This action maintains
     * its enablement state.
     */
    public static final ActionFactory PASTE = new ActionFactory("paste") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.paste"); //$NON-NLS-1$
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
     * Workbench action (id "preferences"): Displays the Preferences dialog.
     * This action maintains its enablement state.
     */
    public static final ActionFactory PREFERENCES = new ActionFactory(
            "preferences") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new OpenPreferencesAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "previous"): Previous. This action is a
     * {@link RetargetAction} with id "previous". This action
     * maintains its enablement state.
     */
    public static final ActionFactory PREVIOUS = new ActionFactory("previous") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.navigate.previous"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "previousEditor"): Previous editor. This action
     * maintains its enablement state.
     * <p>
     * <code>NEXT_EDITOR</code> and <code>PREVIOUS_EDITOR</code> form a
     * cycle action pair. For a given window, use
     * {@link ActionFactory#linkCycleActionPair
     * ActionFactory.linkCycleActionPair</code>} to connect the two.
     * </p>
     */
    public static final ActionFactory PREVIOUS_EDITOR = new ActionFactory(
            "previousEditor") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.window.previousEditor", //$NON-NLS-1$
					window);
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
     * Workbench action (id "previousPart"): Previous part. This action
     * maintains its enablement state.
     * <p>
     * <code>NEXT_PART</code> and <code>PREVIOUS_PART</code> form a cycle
     * action pair. For a given window, use
     * {@link ActionFactory#linkCycleActionPair
     * ActionFactory.linkCycleActionPair</code>} to connect the two.
     * </p>
     */
    public static final ActionFactory PREVIOUS_PART = new ActionFactory(
            "previousPart") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.window.previousView",window); //$NON-NLS-1$
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
     * Workbench action (id "previousPerspective"): Previous perspective. This
     * action maintains its enablement state.
     * <p>
     * <code>NEXT_PERSPECTIVE</code> and <code>PREVIOUS_PERSPECTIVE</code>
     * form a cycle action pair. For a given window, use
     * {@link ActionFactory#linkCycleActionPair
     * ActionFactory.linkCycleActionPair</code>} to connect the two.
     * </p>
     */
    public static final ActionFactory PREVIOUS_PERSPECTIVE = new ActionFactory(
            "previousPerspective") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.window.previousPerspective",window); //$NON-NLS-1$
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
     * Workbench action (id "print"): Print. This action is a
     * {@link RetargetAction} with id "print". This action maintains
     * its enablement state.
     */
    public static final ActionFactory PRINT = new ActionFactory("print") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.file.print"); //$NON-NLS-1$
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
     * Workbench action (id "properties"): Properties. This action is a
     * {@link RetargetAction} with id "properties". This action
     * maintains its enablement state.
     */
    public static final ActionFactory PROPERTIES = new ActionFactory(
            "properties") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.file.properties"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "quit"): Quit (close the workbench). This action
     * maintains its enablement state.
     */
    public static final ActionFactory QUIT = new ActionFactory("quit") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.file.exit", window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.Exit_text); 
            action.setToolTipText(WorkbenchMessages.Exit_toolTip);
            window.getWorkbench().getHelpSystem().setHelp(action,
    				IWorkbenchHelpContextIds.QUIT_ACTION);
            return action;
        }
    };

    /**
     * Workbench action (id "redo"): Redo. This action is a
     * {@link RetargetAction} with id "redo". This action maintains
     * its enablement state.
     */
    public static final ActionFactory REDO = new ActionFactory("redo") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.redo"); //$NON-NLS-1$
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
     * Workbench action (id "refresh"): Refresh. This action is a
     * {@link RetargetAction} with id "refresh". This action
     * maintains its enablement state.
     */
    public static final ActionFactory REFRESH = new ActionFactory("refresh") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "rename"): Rename. This action is a
     * {@link RetargetAction} with id "rename". This action maintains
     * its enablement state.
     */
    public static final ActionFactory RENAME = new ActionFactory("rename") {//$NON-NLS-1$
       
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
            action.setActionDefinitionId("org.eclipse.ui.edit.rename"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "resetPerspective"): Resets the current
     * perspective. This action maintains its enablement state.
     */
    public static final ActionFactory RESET_PERSPECTIVE = new ActionFactory(
            "resetPerspective") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new ResetPerspectiveAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "revert"): Revert. This action is a
     * {@link RetargetAction} with id "revert". This action maintains
     * its enablement state.
     */
    public static final ActionFactory REVERT = new ActionFactory("revert") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.file.revert"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "save"): Save the active editor. This action
     * maintains its enablement state.
     */
    public static final ActionFactory SAVE = new ActionFactory("save") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new SaveAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "saveAll"): Save all open editors with unsaved
     * changes. This action maintains its enablement state.
     */
    public static final ActionFactory SAVE_ALL = new ActionFactory("saveAll") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new SaveAllAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "saveAs"): Save As for the active editor. This
     * action maintains its enablement state.
     */
    public static final ActionFactory SAVE_AS = new ActionFactory("saveAs") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new SaveAsAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "savePerspective"): Save the current perspective.
     * This action maintains its enablement state.
     */
    public static final ActionFactory SAVE_PERSPECTIVE = new ActionFactory(
            "savePerspective") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new SavePerspectiveAction(window);
            action.setId(getId());
            return action;
        }
    };

    /**
     * Workbench action (id "selectAll"): Select All. This action is a
     * {@link RetargetAction} with id "selectAll". This action
     * maintains its enablement state.
     */
    public static final ActionFactory SELECT_ALL = new ActionFactory(
            "selectAll") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.selectAll"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "showEditor"): Show/hide the editor area. This
     * action maintains its enablement state.
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
     * Workbench action (id "showOpenEditors"): Show a list of open (and
     * recently closed) editors. This action maintains its enablement state.
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
     * Workbench action (id "showWorkbookEditors"): Shows a list of open editors
     * in the current or last active workbook.
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
	 * Workbench action (id "showQuickAccess"): Shows a list of UI elements like
	 * editors, views, perspectives etc.
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
	 * Workbench action (id "showPartPaneMenu"): Show the part pane menu. This
	 * action maintains its enablement state.
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
     * Workbench action (id "showViewMenu"): Show the view menu. This action
     * maintains its enablement state.
     */
    public static final ActionFactory SHOW_VIEW_MENU = new ActionFactory(
            "showViewMenu") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            WorkbenchCommandAction action=new WorkbenchCommandAction("org.eclipse.ui.window.showViewMenu",window); //$NON-NLS-1$
            action.setId(getId());
            action.setText(WorkbenchMessages.ShowViewMenuAction_text);
            action.setToolTipText(WorkbenchMessages.ShowViewMenuAction_toolTip);
            return action;
        }
    };

    /**
     * Workbench action (id "undo"): Undo. This action is a
     * {@link RetargetAction} with id "undo". This action maintains
     * its enablement state.
     */
    public static final ActionFactory UNDO = new ActionFactory("undo") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.edit.undo"); //$NON-NLS-1$
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
     * Workbench action (id "up"): Up. This action is a
     * {@link RetargetAction} with id "up". This action maintains its
     * enablement state.
     */
    public static final ActionFactory UP = new ActionFactory("up") {//$NON-NLS-1$
        
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
            action.setActionDefinitionId("org.eclipse.ui.navigate.up"); //$NON-NLS-1$
            return action;
        }
    };

    /**
     * Workbench action (id "helpContents"): Open the help contents. This action
     * is always enabled.
     */
    public static final ActionFactory HELP_CONTENTS = new ActionFactory(
            "helpContents") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new HelpContentsAction(window);
            action.setId(getId());
            return action;
        }
    };
    
    /**
     * Workbench action (id "helpSearch"): Open the help search. This action
     * is always enabled.
     *  
     * @since 3.1  
     */
    public static final ActionFactory HELP_SEARCH = new ActionFactory(
            "helpSearch") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new HelpSearchAction(window);
            action.setId(getId());
            return action;
        }
    };
	
    /**
     * Workbench action (id "dynamicHelp"): Open the dynamic help. This action
     * is always enabled.
     *
     * @since 3.1
     */
    public static final ActionFactory DYNAMIC_HELP = new ActionFactory(
            "dynamicHelp") {//$NON-NLS-1$
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }
            IWorkbenchAction action = new DynamicHelpAction(window);
            action.setId(getId());
            return action;
        }
    };
    
    /**
     * Workbench action (id "openPerspectiveDialog"): Open the Open Perspective dialog. This action
     * is always enabled.
     *
     * @since 3.1
     */
    public static final ActionFactory OPEN_PERSPECTIVE_DIALOG = new ActionFactory(
            "openPerspectiveDialog") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
			if (window == null) {
				throw new IllegalArgumentException();
			}
			
			WorkbenchCommandAction action = new WorkbenchCommandAction(
					"org.eclipse.ui.perspectives.showPerspective", window); //$NON-NLS-1$
			action.setId(getId());
	        action.setText(WorkbenchMessages.OpenPerspectiveDialogAction_text);
	        action.setToolTipText(WorkbenchMessages.OpenPerspectiveDialogAction_tooltip);
	        action.setImageDescriptor(WorkbenchImages.getImageDescriptor(
	              IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE));

			return action;
        }
    };
    
    /**
     * Workbench action (id "newEditor"): Open a new editor on the active editor's input. 
     * This action maintains its enablement state.
     *
     * @since 3.1
     */
    public static final ActionFactory NEW_EDITOR = new ActionFactory(
            "newEditor") {//$NON-NLS-1$
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.IWorkbenchWindow)
         */
        public IWorkbenchAction create(IWorkbenchWindow window) {
            if (window == null) {
                throw new IllegalArgumentException();
            }

            WorkbenchCommandAction action = new WorkbenchCommandAction("org.eclipse.ui.window.newEditor", window); //$NON-NLS-1$
			action.setId(getId());			
			action.setText(WorkbenchMessages.NewEditorAction_text);
			action.setToolTipText(WorkbenchMessages.NewEditorAction_tooltip);

			return action;
        }
    };
    
    /**
	 * Workbench action (id "toggleCoolbar"): Toggle the visibility of the
	 * coolbar and perspective switcher. This will only enable visibility of the
	 * coolbar and perspective bar if the window advisor creating the window
	 * allowed for their visibility initially.
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
     * Creates a new workbench action factory with the given id.
     * 
     * @param actionId
     *            the id of actions created by this action factory
     */
    protected ActionFactory(String actionId) {
        this.actionId = actionId;
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

}
