/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.dialogs.WorkingSetEditWizard;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * A working set manager stores working sets and provides property 
 * change notification when a working set is added or removed.
 * Working sets are persisted whenever one is added or removed.
 * 
 * @see IWorkingSetManager
 * @since 2.0
 */
public class WorkingSetManager implements IWorkingSetManager {
	// Working set persistence
	private static final String WORKING_SET_STATE_FILENAME = "workingsets.xml"; //$NON-NLS-1$
	/**
	 * Size of the list of most recently used working sets.
	 */
	private static final int MRU_SIZE = 5;

	private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
	private List recentWorkingSets = new ArrayList();
	private ListenerList propertyChangeListeners = new ListenerList();

	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#addRecentWorkingSet(IWorkingSet)
	 */
	public void addRecentWorkingSet(IWorkingSet workingSet) {
		internalAddRecentWorkingSet(workingSet);
		saveState();
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#addWorkingSet(IWorkingSet)
	 */
	public void addWorkingSet(IWorkingSet workingSet) {
		Assert.isTrue(!workingSets.contains(workingSet), "working set already registered"); //$NON-NLS-1$
		workingSets.add(workingSet);
		saveState();
		firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#addPropertyChangeListener(IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#createWorkingSet(String, IAdaptable[])
	 */
	public IWorkingSet createWorkingSet(String name, IAdaptable[] elements) {
		return new WorkingSet(name, elements);
	}
	/**
	 * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetEditWizard(org.eclipse.ui.IWorkingSet)
	 * @since 2.1
	 */
	public IWorkingSetEditWizard createWorkingSetEditWizard(IWorkingSet workingSet) {
		String editPageId = workingSet.getId();
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		IWorkingSetPage editPage = null;
				
		if (editPageId != null) {
			editPage = registry.getWorkingSetPage(editPageId);
		}						
		if (editPage == null) {
			editPage = registry.getDefaultWorkingSetPage();
			if (editPage == null) {
				return null;
			}
		}
		WorkingSetEditWizard editWizard = new WorkingSetEditWizard(editPage);
		editWizard.setSelection(workingSet);
		return editWizard;
	}	
	/**
	 * @deprecated use createWorkingSetSelectionDialog(parent, true) instead
	 */
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent) {
		return createWorkingSetSelectionDialog(parent, true);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetSelectionDialog(Shell, boolean)
	 */
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent, boolean multi) {
		return new WorkingSetSelectionDialog(parent, multi);
	}
	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object object to compare the receiver to
	 * @return true=the object equals the receiver, it has the same 
	 * 	working sets. false otherwise
	 */
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof WorkingSetManager) {
			WorkingSetManager workingSetManager = (WorkingSetManager) object;
			return workingSetManager.workingSets.equals(workingSets);
		}
		return false;
	}
	/**
	 * Notify property change listeners about a change to the list of 
	 * working sets.
	 * 
	 * @param changeId one of 
	 * 	IWorkingSetManager#CHANGE_WORKING_SET_ADD 
	 * 	IWorkingSetManager#CHANGE_WORKING_SET_REMOVE
	 * 	IWorkingSetManager#CHANGE_WORKING_SET_CONTENT_CHANGE 
	 * 	IWorkingSetManager#CHANGE_WORKING_SET_NAME_CHANGE
	 * @param oldValue the removed working set or null if a working set 
	 * 	was added or changed.
	 * @param newValue the new or changed working set or null if a working 
	 * 	set was removed.
	 */
	private void firePropertyChange(String changeId, Object oldValue, Object newValue) {
		final PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Object[] listeners = propertyChangeListeners.getListeners();
				for (int i = 0; i < listeners.length; i++) {
					((IPropertyChangeListener) listeners[i]).propertyChange(event);
				}
			}
		});
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#getRecentWorkingSets()
	 */
	public IWorkingSet[] getRecentWorkingSets() {
		return (IWorkingSet[]) recentWorkingSets.toArray(new IWorkingSet[recentWorkingSets.size()]);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#getWorkingSet(String)
	 */
	public IWorkingSet getWorkingSet(String name) {
		if (name == null || workingSets == null)
			return null;

		Iterator iter = workingSets.iterator();
		while (iter.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iter.next();
			if (name.equals(workingSet.getName()))
				return workingSet;
		}
		return null;
	}
	/**
	 * Returns the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		return workingSets.hashCode();
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#getWorkingSets()
	 */
	public IWorkingSet[] getWorkingSets() {
		return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
	}
	/**
	 * Returns the file used as the persistence store
	 * 
	 * @return the file used as the persistence store
	 */
	private File getWorkingSetStateFile() {
		IPath path = WorkbenchPlugin.getDefault().getStateLocation();
		path = path.append(WORKING_SET_STATE_FILENAME);
		return path.toFile();
	}
	/**
	 * Adds the specified working set to the list of recently used
	 * working sets.
	 * 
	 * @param workingSet working set to added to the list of recently 
	 * 	used working sets.
	 */
	private void internalAddRecentWorkingSet(IWorkingSet workingSet) {
		recentWorkingSets.remove(workingSet);
		recentWorkingSets.add(0, workingSet);
		if (recentWorkingSets.size() > MRU_SIZE) {
			recentWorkingSets.remove(MRU_SIZE);
		}
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#removePropertyChangeListener(IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#removeWorkingSet(IWorkingSet)
	 */
	public void removeWorkingSet(IWorkingSet workingSet) {
		boolean workingSetRemoved = workingSets.remove(workingSet);
		boolean recentWorkingSetRemoved = recentWorkingSets.remove(workingSet);
		 
		if (workingSetRemoved || recentWorkingSetRemoved) { 
			saveState();
			firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
		}
	}
	/**
	 * Restores the list of most recently used working sets from the 
	 * persistence store.
	 * 
	 * @param memento the persistence store
	 */
	private void restoreMruList(IMemento memento) {
		IMemento[] mruWorkingSets = memento.getChildren(IWorkbenchConstants.TAG_MRU_LIST);

		for (int i = mruWorkingSets.length - 1; i >= 0; i--) {
			String workingSetName = mruWorkingSets[i].getString(IWorkbenchConstants.TAG_NAME);
			if (workingSetName != null) {
				IWorkingSet workingSet = getWorkingSet(workingSetName);		
				if (workingSet != null) {
					internalAddRecentWorkingSet(workingSet);		
				}
			}
		}
	}
	/**
	 * Reads the persistence store and creates the working sets 
	 * stored in it.
	 */
	public void restoreState() {
		File stateFile = getWorkingSetStateFile();

		if (stateFile.exists()) {
			try {
				FileInputStream input = new FileInputStream(stateFile);
				BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8")); //$NON-NLS-1$

				IMemento memento = XMLMemento.createReadRoot(reader);
				restoreWorkingSetState(memento);
				restoreMruList(memento);
				reader.close();
			} catch (IOException e) {
				MessageDialog.openError(
					(Shell) null,
					WorkbenchMessages.getString("ProblemRestoringWorkingSetState.title"), //$NON-NLS-1$
					WorkbenchMessages.getString("ProblemRestoringWorkingSetState.message")); //$NON-NLS-1$
			} catch (WorkbenchException e) {
				ErrorDialog.openError(
					(Shell) null, 
					WorkbenchMessages.getString("ProblemRestoringWorkingSetState.title"),//$NON-NLS-1$
					WorkbenchMessages.getString("ProblemRestoringWorkingSetState.message"), //$NON-NLS-1$
					e.getStatus());
			}
		}
	}
	/**
	 * Recreates a working set from the persistence store.
	 * 
	 * @param memento the persistence store
	 * @return the working set created from the memento or null if
	 * 	creation failed.
	 */
	private IWorkingSet restoreWorkingSet(IMemento memento) {
		String factoryID = memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);

		if (factoryID == null) {
			WorkbenchPlugin.log("Unable to restore working set - no factory ID."); //$NON-NLS-1$
			return null;
		}
		IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory == null) {
			WorkbenchPlugin.log("Unable to restore working set - cannot instantiate factory: " + factoryID); //$NON-NLS-1$
			return null;
		}
		IAdaptable adaptable = factory.createElement(memento);
		if (adaptable == null) {
			WorkbenchPlugin.log("Unable to restore working set - cannot instantiate working set: " + factoryID); //$NON-NLS-1$
			return null;
		}
		if ((adaptable instanceof IWorkingSet) == false) {
			WorkbenchPlugin.log("Unable to restore working set - element is not an IWorkingSet: " + factoryID); //$NON-NLS-1$
			return null;
		}
		return (IWorkingSet) adaptable;
	}
	/**
	 * Recreates all working sets from the persistence store
	 * and adds them to the receiver.
	 * 
	 * @param memento the persistence store
	 */
	private void restoreWorkingSetState(IMemento memento) {
		IMemento[] workingSets = memento.getChildren(IWorkbenchConstants.TAG_WORKING_SET);

		for (int i = 0; i < workingSets.length; i++) {
			IWorkingSet workingSet = restoreWorkingSet(workingSets[i]);
			if (workingSet != null) {
				this.workingSets.add(workingSet);
				firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
			}
		}
	}
	/**
	 * Saves the working sets in the persistence store
	 */
	private void saveState() {
		XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WORKING_SET_MANAGER);
		File stateFile = getWorkingSetStateFile();

		saveWorkingSetState(memento);
		saveMruList(memento);
		try {
			FileOutputStream stream = new FileOutputStream(stateFile);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
			writer.close();
		} catch (IOException e) {
			stateFile.delete();
			MessageDialog.openError((Shell) null, WorkbenchMessages.getString("ProblemSavingWorkingSetState.title"), //$NON-NLS-1$
			WorkbenchMessages.getString("ProblemSavingWorkingSetState.message")); //$NON-NLS-1$
		}
	}
	/**
	 * Saves the list of most recently used working sets in the persistence 
	 * store.
	 * 
	 * @param memento the persistence store
	 */
	private void saveMruList(IMemento memento) {
		Iterator iterator = recentWorkingSets.iterator();

		while (iterator.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iterator.next();
			IMemento mruMemento = memento.createChild(IWorkbenchConstants.TAG_MRU_LIST);
			
			mruMemento.putString(IWorkbenchConstants.TAG_NAME, workingSet.getName());
		}
	}
	/**
	 * Saves all persistable working sets in the persistence store.
	 * 
	 * @param memento the persistence store
	 * @see IPersistableElement
	 */
	private void saveWorkingSetState(IMemento memento) {
		Iterator iterator = workingSets.iterator();

		while (iterator.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iterator.next();
			IPersistableElement persistable = null;

			if (workingSet instanceof IPersistableElement) {
				persistable = (IPersistableElement) workingSet;
			} else if (workingSet instanceof IAdaptable) {
				persistable = (IPersistableElement) ((IAdaptable) workingSet).getAdapter(IPersistableElement.class);
			}
			if (persistable != null) {
				IMemento workingSetMemento = memento.createChild(IWorkbenchConstants.TAG_WORKING_SET);
				workingSetMemento.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
				persistable.saveState(workingSetMemento);
			}
		}
	}
	/**
	 * Persists all working sets and fires a property change event for 
	 * the changed working set.
	 * Should only be called by org.eclipse.ui.internal.WorkingSet.
	 * 
	 * @param changedWorkingSet the working set that has changed
	 * @param propertyChangeId the changed property. one of 
	 * 	CHANGE_WORKING_SET_CONTENT_CHANGE and CHANGE_WORKING_SET_NAME_CHANGE
	 */
	public void workingSetChanged(IWorkingSet changedWorkingSet, String propertyChangeId) {
		saveState();
		firePropertyChange(propertyChangeId, null, changedWorkingSet);
	}
}
