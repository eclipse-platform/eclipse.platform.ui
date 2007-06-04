/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkingSetComparator;
import org.eclipse.ui.internal.WorkingSetMenuContributionItem;
import org.eclipse.ui.internal.actions.ClearWorkingSetAction;
import org.eclipse.ui.internal.actions.EditWorkingSetAction;
import org.eclipse.ui.internal.actions.SelectWorkingSetAction;
import org.eclipse.ui.internal.util.Util;

/**
 * Adds working set filter actions (set / clear / edit)
 * 
 * @since 2.1 
 */
public class WorkingSetFilterActionGroup extends ActionGroup {
    private static final String TAG_WORKING_SET_NAME = "workingSetName"; //$NON-NLS-1$

	private static final String TAG_IS_WINDOW_WORKING_SET = "isWindowWorkingSet"; //$NON-NLS-1$
	
    /**
     * Indicates if working set was changed
     */
    public static final String CHANGE_WORKING_SET = "changeWorkingSet"; //$NON-NLS-1$
    
    private static final String START_SEPARATOR_ID = "workingSetGroupStartSeparator"; //$NON-NLS-1$

    private static final String SEPARATOR_ID = "workingSetGroupSeparator"; //$NON-NLS-1$

	private static final String WORKING_SET_ACTION_GROUP = "workingSetActionGroup"; //$NON-NLS-1$

    private IWorkingSet workingSet = null;

    private ClearWorkingSetAction clearWorkingSetAction;

    private SelectWorkingSetAction selectWorkingSetAction;

    private EditWorkingSetAction editWorkingSetAction;

    private IPropertyChangeListener workingSetUpdater;

    private int mruMenuCount;

    private IMenuManager menuManager;

    private IMenuListener menuListener;

	private IWorkbenchWindow workbenchWindow;

	private IWorkbenchPage page;

	private boolean allowWindowWorkingSetByDefault;

    /**
	 * Creates a new instance of the receiver.
	 * 
	 * @param shell
	 *            shell to open dialogs and wizards on
	 * @param workingSetUpdater
	 *            property change listener notified when a working set is set
	 * @since 3.2 Please note that it is expected that clients treat any
	 *        selected working sets whose
	 *        {@link IWorkingSet#isAggregateWorkingSet()} method returns
	 *        <code>true</code> somewhat differently from traditional working
	 *        sets. Please see the documentation for
	 *        {@link IWorkbenchPage#getAggregateWorkingSet()} for details.
	 */
    public WorkingSetFilterActionGroup(Shell shell,
            IPropertyChangeListener workingSetUpdater) {
        Assert.isNotNull(shell);

        this.workingSetUpdater = workingSetUpdater;
        clearWorkingSetAction = new ClearWorkingSetAction(this);
        selectWorkingSetAction = new SelectWorkingSetAction(this, shell);
        editWorkingSetAction = new EditWorkingSetAction(this, shell);
        
        workbenchWindow = Util.getWorkbenchWindowForShell(shell);
        allowWindowWorkingSetByDefault = false;
		// set the default working set to be that of the window.
		page = workbenchWindow.getActivePage();
		if (page == null) {
			IWorkbenchPage[] pages = workbenchWindow.getPages();
			if (pages.length > 0) {
				page = pages[0];
			}
		}
    }

    /**
	 * Adds actions for the most recently used working sets to the specified
	 * menu manager.
	 * 
	 * @param menuManager
	 *            menu manager to add actions to
	 */
    private void addMruWorkingSetActions(IMenuManager menuManager) {
        IWorkingSet[] workingSets = PlatformUI.getWorkbench()
                .getWorkingSetManager().getRecentWorkingSets();
        List sortedWorkingSets = Arrays.asList(workingSets);
        Collections.sort(sortedWorkingSets, new WorkingSetComparator());

        Iterator iter = sortedWorkingSets.iterator();
        mruMenuCount = 0;
        while (iter.hasNext()) {
            IWorkingSet workingSet = (IWorkingSet) iter.next();
            if (workingSet != null) {
                IContributionItem item = new WorkingSetMenuContributionItem(
                        ++mruMenuCount, this, workingSet);
                menuManager.insertBefore(SEPARATOR_ID, item);
            }
        }
    }

 
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionGroup#dispose()
     */
    public void dispose() {
        if (menuManager != null) {
			menuManager.removeMenuListener(menuListener);
		}
        super.dispose();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
     */
    public void fillActionBars(IActionBars actionBars) {
        menuManager = actionBars.getMenuManager();
        
        if(menuManager.find(IWorkbenchActionConstants.MB_ADDITIONS) != null) 
        	menuManager.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS, new Separator(WORKING_SET_ACTION_GROUP));
        else
        	menuManager.add(new Separator(WORKING_SET_ACTION_GROUP));
        
        menuManager.appendToGroup(WORKING_SET_ACTION_GROUP, selectWorkingSetAction);
        menuManager.appendToGroup(WORKING_SET_ACTION_GROUP, clearWorkingSetAction);
        menuManager.appendToGroup(WORKING_SET_ACTION_GROUP, editWorkingSetAction);
        menuManager.appendToGroup(WORKING_SET_ACTION_GROUP, new Separator(START_SEPARATOR_ID));
        menuManager.appendToGroup(WORKING_SET_ACTION_GROUP, new Separator(SEPARATOR_ID));

        menuListener = new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                removePreviousMruWorkingSetActions(manager);
                addMruWorkingSetActions(manager);
            }
        };
        menuManager.addMenuListener(menuListener);
    }
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menuManager) {
		menuManager.add(selectWorkingSetAction);
		menuManager.add(clearWorkingSetAction);
		menuManager.add(editWorkingSetAction);
		menuManager.add(new Separator());
		menuManager.add(new Separator(SEPARATOR_ID));

		menuListener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				removePreviousMruWorkingSetActions(manager);
				addMruWorkingSetActions(manager);
			}
		};
		menuManager.addMenuListener(menuListener);
	}

    /**
     * Returns the working set which is currently selected.
     * 
     * @return the working set which is currently selected.
     */
    public IWorkingSet getWorkingSet() {
        return workingSet;
    }

    /**
     * Removes the most recently used working set actions that were
     * added to the specified menu.
     * 
     * @param menuManager menu manager to remove actions from
     */
    private void removePreviousMruWorkingSetActions(IMenuManager menuManager) {
        for (int i = 1; i <= mruMenuCount; i++) {
			menuManager.remove(WorkingSetMenuContributionItem.getId(i));
		}
    }

    /**
     * Sets the current working set.
     * 
     * @param newWorkingSet the new working set
     */
    public void setWorkingSet(IWorkingSet newWorkingSet) {
        IWorkingSet oldWorkingSet = workingSet;

        workingSet = newWorkingSet;
        // Update action
        clearWorkingSetAction.setEnabled(newWorkingSet != null);
        editWorkingSetAction.setEnabled(newWorkingSet != null && newWorkingSet.isEditable());

        firePropertyChange(newWorkingSet, oldWorkingSet);
    }

    /**
     * Fire the property change to the updater if there is one available.
     * 
     * @param newWorkingSet the new working set
     * @param oldWorkingSet the previous working set
     * @since 3.2
     */
	private void firePropertyChange(IWorkingSet newWorkingSet, IWorkingSet oldWorkingSet) {
		// Update viewer
        if (workingSetUpdater != null) {
            workingSetUpdater.propertyChange(new PropertyChangeEvent(this,
                    WorkingSetFilterActionGroup.CHANGE_WORKING_SET,
                    oldWorkingSet, newWorkingSet));
        }
	}

	/**
	 * Saves the state of the filter actions in a memento.
	 * 
	 * @param memento
	 *            the memento
	 * @since 3.3
	 */
	public void saveState(IMemento memento) {
		String workingSetName = ""; //$NON-NLS-1$
		boolean isWindowWorkingSet = false;
		if (workingSet != null) {
			if (workingSet.isAggregateWorkingSet()) {
				isWindowWorkingSet = true;
			} else {
				workingSetName = workingSet.getName();
			}
		}
		memento.putString(TAG_IS_WINDOW_WORKING_SET,
				isWindowWorkingSet ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(TAG_WORKING_SET_NAME, workingSetName);
	}

	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 * 
	 * @param memento
	 * @since 3.3
	 */
	public void restoreState(IMemento memento) {
		boolean isWindowWorkingSet;
		if (memento.getString(TAG_IS_WINDOW_WORKING_SET) != null) {
			isWindowWorkingSet = Boolean.valueOf(
					memento.getString(TAG_IS_WINDOW_WORKING_SET))
					.booleanValue();
		} else {
			isWindowWorkingSet = useWindowWorkingSetByDefault();
		}
		String workingSetName = memento.getString(TAG_WORKING_SET_NAME);
		boolean hasWorkingSetName = workingSetName != null
				&& workingSetName.length() > 0;

		IWorkingSet ws = null;
		// First handle name if present.
		if (hasWorkingSetName) {
			ws = PlatformUI.getWorkbench().getWorkingSetManager()
					.getWorkingSet(workingSetName);
		} else if (isWindowWorkingSet && page != null) {
			ws = page.getAggregateWorkingSet();
		}

		setWorkingSet(ws);
	}

	private boolean useWindowWorkingSetByDefault() {
		return allowWindowWorkingSetByDefault
				&& PlatformUI
						.getPreferenceStore()
						.getBoolean(
								IWorkbenchPreferenceConstants.USE_WINDOW_WORKING_SET_BY_DEFAULT);
	}
}
