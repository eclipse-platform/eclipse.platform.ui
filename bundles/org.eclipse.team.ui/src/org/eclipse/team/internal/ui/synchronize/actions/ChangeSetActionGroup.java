/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.ChangeSetModelProvider;
import org.eclipse.team.internal.ui.synchronize.ChangeSetModelSorter;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

/**
 * This action group contributes actions that support the management
 * of Change sets to a synchronize page.
 */
public class ChangeSetActionGroup extends SynchronizePageActionGroup {

    /**
     * Menu group that can be added to the context menu
     */
    public final static String CHANGE_SET_GROUP = "change_set_group"; //$NON-NLS-1$
    
	// Constants for persisting sorting options
	private static final String P_LAST_COMMENTSORT = TeamUIPlugin.ID + ".P_LAST_COMMENT_SORT"; //$NON-NLS-1$
    
    public static final FastSyncInfoFilter OUTGOING_RESOURCE_FILTER = new SyncInfoDirectionFilter(
            new int[] { SyncInfo.OUTGOING, SyncInfo.CONFLICTING });
    
	private class CreateChangeSetAction extends SynchronizeModelAction {
	    
        public CreateChangeSetAction(ISynchronizePageConfiguration configuration) {
            super(Policy.bind("ChangeLogModelProvider.0"), configuration); //$NON-NLS-1$
        }
        
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#needsToSaveDirtyEditors()
		 */
		protected boolean needsToSaveDirtyEditors() {
			return false;
		}
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSyncInfoFilter()
         */
        protected FastSyncInfoFilter getSyncInfoFilter() {
            return OUTGOING_RESOURCE_FILTER;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSubscriberOperation(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration, org.eclipse.compare.structuremergeviewer.IDiffElement[])
         */
        protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
            return new SynchronizeModelOperation(configuration, elements) {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    syncExec(new Runnable() {
                        public void run() {
		                    SyncInfo[] infos = getSyncInfoSet().getSyncInfos();
		                    ActiveChangeSet set = createChangeSet(infos);
		                    if (set != null) {
		                        getActiveChangeSetManager().add(set);
		                    }
                        }
                    });
                }
            };
        }
	}

	private abstract class ChangeSetAction extends BaseSelectionListenerAction {

        public ChangeSetAction(String title, ISynchronizePageConfiguration configuration) {
            super(title);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.ui.actions.BaseSelectionListenerAction#updateSelection(org.eclipse.jface.viewers.IStructuredSelection)
         */
        protected boolean updateSelection(IStructuredSelection selection) {
            return getSelectedSet() != null;
        }

        protected ActiveChangeSet getSelectedSet() {
            IStructuredSelection selection = getStructuredSelection();
            if (selection.size() == 1) {
                Object first = selection.getFirstElement();
                if (first instanceof IAdaptable) {
	                Object adapter = ((IAdaptable)first).getAdapter(ChangeSet.class);
	                if (adapter instanceof ActiveChangeSet) {
	                    return (ActiveChangeSet)adapter; 
	                }
                }
            }
            return null;
        }
	}
	
	private class EditChangeSetAction extends ChangeSetAction {

        public EditChangeSetAction(ISynchronizePageConfiguration configuration) {
            super(Policy.bind("ChangeLogModelProvider.6"), configuration); //$NON-NLS-1$
        }
        
        public void run() {
            ActiveChangeSet set = getSelectedSet();
            if (set == null) return;
    		editChangeSet(set);
        }
	}
	
	private class MakeDefaultChangeSetAction extends ChangeSetAction {

        public MakeDefaultChangeSetAction(ISynchronizePageConfiguration configuration) {
            super(Policy.bind("ChangeLogModelProvider.9"), configuration); //$NON-NLS-1$
        }
        
        public void run() {
            ActiveChangeSet set = getSelectedSet();
            if (set == null) return;
    		getActiveChangeSetManager().makeDefault(set);
        }
	    
	}
	
	private class AddToChangeSetAction extends SynchronizeModelAction {
	 
        private final ActiveChangeSet set;
	    
        public AddToChangeSetAction(ISynchronizePageConfiguration configuration, ActiveChangeSet set, ISelection selection) {
            super(set.getTitle(), configuration);
            this.set = set;
            selectionChanged(selection);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSyncInfoFilter()
         */
        protected FastSyncInfoFilter getSyncInfoFilter() {
            return OUTGOING_RESOURCE_FILTER;
        }
        
		protected boolean needsToSaveDirtyEditors() {
			return false;
		}
        
        /* (non-Javadoc)
         * @see org.eclipse.team.ui.synchronize.SynchronizeModelAction#getSubscriberOperation(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration, org.eclipse.compare.structuremergeviewer.IDiffElement[])
         */
        protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
            return new SynchronizeModelOperation(configuration, elements) {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    set.add(getSyncInfoSet().getSyncInfos());
                }
            };
        }
	}
	
	/* *****************************************************************************
	 * Action that allows changing the model providers sort order.
	 */
	private class ToggleSortOrderAction extends Action {
		private int criteria;
		protected ToggleSortOrderAction(String name, int criteria) {
			super(name, Action.AS_RADIO_BUTTON);
			this.criteria = criteria;
			update();		
		}

		public void run() {
			if (isChecked() && sortCriteria != criteria) {
			    sortCriteria = criteria;
				String key = getSettingsKey();
				IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
				if(pageSettings != null) {
					pageSettings.put(key, criteria);
				}
				update();
				provider.setViewerSorter(getViewerSorter());
			}
		}
		
		public void update() {
		    setChecked(criteria == sortCriteria);
		}
		
		protected String getSettingsKey() {
		    return P_LAST_COMMENTSORT;
		}
	}

	/*
	 * The model provider for this action group
	 */
	private ChangeSetModelProvider provider;
	
	/*
	 * The actions created by this group
	 */
	private MenuManager sortByComment;
	private CreateChangeSetAction createChangeSet;
	private MenuManager addToChangeSet;
    private EditChangeSetAction editChangeSet;
    private MakeDefaultChangeSetAction makeDefault;
    
    private SynchronizePageActionGroup subActions;
    
    /*
     * The currently chosen sort criteria
     */
    private int sortCriteria = ChangeSetModelSorter.DATE;
    
    public ChangeSetActionGroup(ChangeSetModelProvider provider) {
        this.provider = provider;
    }
    
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		
		if (getChangeSetCapability().supportsCheckedInChangeSets()) {
			initializeSortCriteria(configuration);
			sortByComment = new MenuManager(Policy.bind("ChangeLogModelProvider.0a"));	 //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.1a"), ChangeSetModelSorter.COMMENT)); //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.2a"), ChangeSetModelSorter.DATE)); //$NON-NLS-1$
			sortByComment.add(new ToggleSortOrderAction(Policy.bind("ChangeLogModelProvider.3a"), ChangeSetModelSorter.USER)); //$NON-NLS-1$
		}
		
		if (getChangeSetCapability().supportsActiveChangeSets()) {
			addToChangeSet = new MenuManager(Policy.bind("ChangeLogModelProvider.12")); //$NON-NLS-1$
			addToChangeSet.setRemoveAllWhenShown(true);
			addToChangeSet.addMenuListener(new IMenuListener() {
	            public void menuAboutToShow(IMenuManager manager) {
	                addChangeSets(manager);
	            }
	        });
			createChangeSet = new CreateChangeSetAction(configuration);
			addToChangeSet.add(createChangeSet);
			addToChangeSet.add(new Separator());
			editChangeSet = new EditChangeSetAction(configuration);
			makeDefault = new MakeDefaultChangeSetAction(configuration);
		}
		
		subActions = getChangeSetCapability().getActionGroup();
		if (subActions != null) {
		    subActions.initialize(configuration);
		}
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {
        if (getChangeSetCapability().enableCheckedInChangeSetsFor(getConfiguration())) {
            appendToGroup(menu, ISynchronizePageConfiguration.SORT_GROUP, sortByComment);
        }
        if (getChangeSetCapability().enableActiveChangeSetsFor(getConfiguration())) {
			appendToGroup(
					menu, 
					CHANGE_SET_GROUP, 
					addToChangeSet);
			appendToGroup(
					menu, 
					CHANGE_SET_GROUP, 
					editChangeSet);
			appendToGroup(
					menu, 
					CHANGE_SET_GROUP, 
					makeDefault);
        }
		if (subActions != null) {
		    subActions.fillContextMenu(menu);
		}
    }
    
    private void initializeSortCriteria(ISynchronizePageConfiguration configuration) {
		try {
			IDialogSettings pageSettings = getConfiguration().getSite().getPageSettings();
			if(pageSettings != null) {
				sortCriteria = pageSettings.getInt(P_LAST_COMMENTSORT);
			}
		} catch(NumberFormatException e) {
			// ignore and use the defaults.
		}
		switch (sortCriteria) {
        case ChangeSetModelSorter.COMMENT:
        case ChangeSetModelSorter.DATE:
        case ChangeSetModelSorter.USER:
            break;
        default:
            sortCriteria = ChangeSetModelSorter.DATE;
            break;
        }
    }
    
    protected void addChangeSets(IMenuManager manager) {
        ChangeSet[] sets = getActiveChangeSetManager().getSets();
        ISelection selection = getContext().getSelection();
        createChangeSet.selectionChanged(selection);
		addToChangeSet.add(createChangeSet);
		addToChangeSet.add(new Separator());
        for (int i = 0; i < sets.length; i++) {
            ActiveChangeSet set = (ActiveChangeSet)sets[i];
            AddToChangeSetAction action = new AddToChangeSetAction(getConfiguration(), set, selection);
            manager.add(action);
        }
    }

    /**
     * Return the change set manager for the current page.
     * @return the change set manager for the current page
     */
    protected SubscriberChangeSetCollector getActiveChangeSetManager() {
        return getChangeSetCapability().getActiveChangeSetManager();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#dispose()
	 */
	public void dispose() {
	    if (addToChangeSet != null) {
			addToChangeSet.dispose();
			addToChangeSet.removeAll();
	    }
	    if (sortByComment != null) {
			sortByComment.dispose();
			sortByComment.removeAll();
	    }
	    if (subActions != null) {
	        subActions.dispose();
	    }
		super.dispose();
	}
	
	
    public void updateActionBars() {
        if (editChangeSet != null)
	        editChangeSet.selectionChanged((IStructuredSelection)getContext().getSelection());
        if (makeDefault != null)
	        makeDefault.selectionChanged((IStructuredSelection)getContext().getSelection());
        super.updateActionBars();
    }
    
    private void syncExec(final Runnable runnable) {
		final Control ctrl = getConfiguration().getPage().getViewer().getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			ctrl.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (!ctrl.isDisposed()) {
					    runnable.run();
					}
				}
			});
		}
    }
    
    /**
     * Return a viewer sorter that utilizes the sort criteria
     * selected by the user.
     */
	public ViewerSorter getViewerSorter() {
		return new ChangeSetModelSorter(provider, sortCriteria);
	}
	
    private ActiveChangeSet createChangeSet(SyncInfo[] infos) {
        return getChangeSetCapability().createChangeSet(getConfiguration(), infos);
    }
    
    private void editChangeSet(ActiveChangeSet set) {
        getChangeSetCapability().editChangeSet(getConfiguration(), set);
    }

    private ChangeSetCapability getChangeSetCapability() {
        return provider.getChangeSetCapability();
    }
}
