package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class LaunchConfigurationWorkingSetActionManager {

	private static final String TAG_WORKING_SET_NAME= "launchConfigurationWorkingSetName"; //$NON-NLS-1$
	private static final String SEPARATOR_ID= "launchConfigurationWorkingSetGroupSeparator"; //$NON-NLS-1$

	private StructuredViewer fViewer;
	private Shell fShell;
	
	private LaunchConfigurationClearWorkingSetAction fClearWorkingSetAction;
	private LaunchConfigurationSelectWorkingSetAction fSelectWorkingSetAction;
	
	private LaunchConfigurationWorkingSetFilter fWorkingSetFilter;
	private IWorkingSet fWorkingSet;

	private int fLRUMenuCount;
	private IPropertyChangeListener fPropertyChangeListener;
	private IMenuManager fMenuManager;
	private IMenuListener fMenuListener;

	public LaunchConfigurationWorkingSetActionManager(StructuredViewer viewer, Shell shell) {
		setViewer(viewer);
		setShell(shell);
		setClearAction(new LaunchConfigurationClearWorkingSetAction(this));
		setSelectAction(new LaunchConfigurationSelectWorkingSetAction(this, getShell()));
		addWorkingSetChangeSupport();
	}	

	/**
	 * Returns the working set which is used by the filter.
	 * 
	 * @return the working set
	 */
	public IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}
		
	/**
	 * Sets this filter's working set.
	 * 
	 * @param workingSet the working set
	 * @param refreshViewer Indiactes if the viewer should be refreshed.
	 */
	public void setWorkingSet(IWorkingSet workingSet, boolean refreshViewer){
		// Update action
		getClearAction().setEnabled(workingSet != null);

		fWorkingSet= workingSet;

		// Update viewer
		if (fWorkingSetFilter != null) {
			fWorkingSetFilter.setWorkingSet(workingSet);	
			fViewer.getControl().setRedraw(false);
			if (refreshViewer) {
				fViewer.refresh();
			}
			fViewer.getControl().setRedraw(true);
		}
	}
	
	/**
	 * Saves the state of the filter actions in a memento.
	 */
	public void saveState(IMemento memento) {
		String workingSetName = ""; //$NON-NLS-1$
		if (fWorkingSet != null) {
			workingSetName = fWorkingSet.getName();
		}
		memento.putString(TAG_WORKING_SET_NAME, workingSetName);
	}

	/**
	 * Restores the state of the filter actions from a memento.
	 * <p>
	 * Note: This method does not refresh the viewer.
	 * </p>
	 * @param memento
	 */	
	public void restoreState(IMemento memento) {
		String workingSetName= memento.getString(TAG_WORKING_SET_NAME);
		IWorkingSet ws= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName);
		setWorkingSet(ws, false);
	}
	

	/**
	 * Adds the filter actions to the menu
	 */
	public void contributeToMenu(IMenuManager mm) {
		mm.add(getSelectAction());
		mm.add(getClearAction());
		mm.add(new Separator());
		mm.add(new Separator(SEPARATOR_ID));
		
		fMenuManager = mm;
		addLRUWorkingSetActions(fMenuManager);
	}
	
	/**
	 * Remove the menu listener from the menu manager.
	 */
	public void dispose() {
	}
	
	private void removePreviousLRUWorkingSetActions(IMenuManager mm) {
		for (int i = 1; i <= fLRUMenuCount; i++) {
			mm.remove(LaunchConfigurationWorkingSetMenuContributionItem.getId(i));
		}
	}

	private void addLRUWorkingSetActions(IMenuManager mm) {
		IWorkingSet[] workingSets= PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
		List sortedWorkingSets= Arrays.asList(workingSets);
		Collections.sort(sortedWorkingSets, new LaunchConfigurationWorkingSetComparator());
		
		Iterator iter= sortedWorkingSets.iterator();
		int i = 0;
		while (iter.hasNext()) {
			IWorkingSet workingSet= (IWorkingSet)iter.next();
			if (workingSet != null) {
				IContributionItem item = new LaunchConfigurationWorkingSetMenuContributionItem(++i, this, workingSet);
				mm.insertBefore(SEPARATOR_ID, item);
			}
		}
		fLRUMenuCount = i;
	}
	
	private IPropertyChangeListener addWorkingSetChangeSupport() {
		final IPropertyChangeListener propertyChangeListener= createWorkingSetChangeListener();

		fWorkingSetFilter= new LaunchConfigurationWorkingSetFilter();
		fViewer.addFilter(fWorkingSetFilter);

		// Register listener on working set manager
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(propertyChangeListener);
		
		// Register dispose listener which removes the listeners
		fViewer.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(propertyChangeListener);
			}
		});
		
		return propertyChangeListener;
	}

	private IPropertyChangeListener createWorkingSetChangeListener() {
		return new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property= event.getProperty();
				if (IWorkbenchPage.CHANGE_WORKING_SET_REPLACE.equals(property)) {
					IWorkingSet newWorkingSet= (IWorkingSet) event.getNewValue();

					fWorkingSetFilter.setWorkingSet(newWorkingSet);	

					fViewer.getControl().setRedraw(false);
					fViewer.refresh();
					fViewer.getControl().setRedraw(true);
				} else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property)) {
					fViewer.getControl().setRedraw(false);
					fViewer.refresh();
					fViewer.getControl().setRedraw(true);
				}
			}
		};
	}
	private void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}

	private StructuredViewer getViewer() {
		return fViewer;
	}

	private void setShell(Shell shell) {
		fShell = shell;
	}

	private Shell getShell() {
		return fShell;
	}

	private void setWorkingSetFilter(LaunchConfigurationWorkingSetFilter filter) {
		fWorkingSetFilter = filter;
	}

	private LaunchConfigurationWorkingSetFilter getWorkingSetFilter() {
		return fWorkingSetFilter;
	}

	private void setClearAction(LaunchConfigurationClearWorkingSetAction action) {
		fClearWorkingSetAction = action;
	}

	private LaunchConfigurationClearWorkingSetAction getClearAction() {
		return fClearWorkingSetAction;
	}

	private void setSelectAction(LaunchConfigurationSelectWorkingSetAction action) {
		fSelectWorkingSetAction = action;
	}

	private LaunchConfigurationSelectWorkingSetAction getSelectAction() {
		return fSelectWorkingSetAction;
	}
	
}
