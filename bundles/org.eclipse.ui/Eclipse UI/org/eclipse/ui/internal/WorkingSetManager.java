package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;

/**
 * A working set manager stores working sets and provides property 
 * change notification when a working set is added or removed.
 * Working sets are persisted whenever one is added or removed.
 * When a resource is removed from the workspace it is also removed 
 * from any working sets that contain it.
 * 
 * @see IWorkingSetManager
 * @since 2.0
 */
public class WorkingSetManager implements IWorkingSetManager, IResourceChangeListener, IResourceDeltaVisitor {
	// Working set persistence
	private static final String WORKING_SET_STATE_FILENAME = "workingsets.xml"; //$NON-NLS-1$

	private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
	private ListenerList propertyChangeListeners = new ListenerList();

	/**
	 * Creates a new instance of the receiver.
	 */
	public WorkingSetManager() {
		WorkbenchPlugin.getPluginWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
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
	 * @deprecated use createWorkingSetSelectionDialog(parent, true) instead
	 */
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent) {
		return createWorkingSetSelectionDialog(parent, true);
	}
	/**
	 * Implements IWorkingSetManager.
	 * 
	 * @see org.eclipse.ui.IWorkingSetManager#createWorkingSetSelectionDialog(Shell)
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
		return (object instanceof WorkingSetManager) && ((WorkingSetManager) object).getWorkingSets().equals(getWorkingSets());
	}
	/**
	 * Notify property change listeners about a change to the list of 
	 * working sets.
	 * 
	 * @param changeId one of IWorkingSetManager#CHANGE_WORKING_SET_ADD 
	 * 	and IWorkingSetManager#CHANGE_WORKING_SET_REMOVE
	 * @param oldValue the removed working set or null if a working set 
	 * 	was added.
	 * @param newValue the new working set or null if a working set was 
	 * 	removed.
	 */
	private void firePropertyChange(String changeId, Object oldValue, Object newValue) {
		Object[] listeners = propertyChangeListeners.getListeners();
		PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);

		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
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
		workingSets.remove(workingSet);
		saveState();
		firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
	}
	/**
	 * Removes a deleted resource from any working set that contains it.
	 * 
	 * @param event the resource change event
	 * @see IResourceChangeListener#resourceChanged
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Problem updating working sets", e.getStatus()); //$NON-NLS-1$	
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
				addWorkingSet(workingSet);
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
				InputStreamReader reader = new InputStreamReader(input, "utf-8"); //$NON-NLS-1$

				IMemento memento = XMLMemento.createReadRoot(reader);
				restoreWorkingSetState(memento);
				reader.close();
			} catch (IOException e) {
				MessageDialog.openError((Shell) null, WorkbenchMessages.getString("ProblemRestoringWorkingSetState.title"), //$NON-NLS-1$
				WorkbenchMessages.getString("ProblemRestoringWorkingSetState.message")); //$NON-NLS-1$
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
	 * Saves all working sets and fires a property change event for 
	 * the changed working set.
	 * Should be called by org.eclipse.ui.internal.WorkingSet only.
	 * 
	 * @param changedWorkingSet the working set that has changed
	 * @param propertyChangeId the changed property. one of 
	 * 	CHANGE_WORKING_SET_CONTENT_CHANGE and CHANGE_WORKING_SET_NAME_CHANGE
	 */
	public void workingSetChanged(IWorkingSet changedWorkingSet, String propertyChangeId) {
		saveState();
		firePropertyChange(propertyChangeId, null, changedWorkingSet);
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
	 * Implements IResourceDeltaVisitor.
	 * Removes deleted resources from any working set that contains 
	 * them.
	 * 
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		boolean visitChildren = false;

		if (delta.getKind() == IResourceDelta.CHANGED) {
			visitChildren = true;
		} else if (delta.getKind() == IResourceDelta.REMOVED) {
			IPath deltaPath = delta.getResource().getFullPath();
			Iterator iterator = workingSets.iterator();

			while (iterator.hasNext()) {
				IWorkingSet workingSet = (IWorkingSet) iterator.next();
				IAdaptable[] items = workingSet.getElements();
				int itemCount = items.length;

				for (int i = 0; i < items.length; i++) {
					IResource workingSetResource = (IResource) items[i].getAdapter(IResource.class);

					if (workingSetResource != null && deltaPath.isPrefixOf(workingSetResource.getFullPath())) {
						items[i] = null;
						itemCount--;
					}
				}
				if (itemCount != items.length) {
					IAdaptable[] newItems = new IAdaptable[itemCount];

					for (int i = 0, j = 0; i < items.length; i++) {
						if (items[i] != null) {
							newItems[j++] = items[i];
						}
					}
					workingSet.setElements(newItems);
				}
			}
		}
		return visitChildren;
	}
}