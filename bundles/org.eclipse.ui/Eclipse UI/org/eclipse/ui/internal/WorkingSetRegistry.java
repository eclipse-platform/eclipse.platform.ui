/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetRegistry;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistryReader;

public class WorkingSetRegistry implements IWorkingSetRegistry, IResourceChangeListener, IResourceDeltaVisitor {
	// XML tags
	static String TAG_WORKINGSETS = "workingsets"; //$NON-NLS-1$
	static String TAG_WORKINGSET = "workingset"; //$NON-NLS-1$
	static String TAG_NAME = "name"; //$NON-NLS-1$
	static String TAG_CONTENTS = "contents"; //$NON-NLS-1$
	static String TAG_FILE = "file"; //$NON-NLS-1$
	static String TAG_FOLDER = "folder"; //$NON-NLS-1$
	static String TAG_PATH = "path"; //$NON-NLS-1$
	static String TAG_PROJECT = "project"; //$NON-NLS-1$

	// Persistence
	static String STORE_NAME = "workingsets.xml"; //$NON-NLS-1$

	private SortedSet workingSets = new TreeSet(new WorkingSetComparator());
	private ListenerList propertyChangeListeners = new ListenerList();
	private HashMap workingSetDescriptors = new HashMap();

	public WorkingSetRegistry() {
		WorkbenchPlugin.getPluginWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}
	/**
	 * Adds the working set to the workspace.
	 * The working set must not exist yet.
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog. 
	 */
	public void add(IWorkingSet workingSet) {
		Assert.isTrue(!workingSets.contains(workingSet), "working set already registered"); //$NON-NLS-1$
		workingSets.add(workingSet);
		firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
	}
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	public void addWorkingSetDescriptor(WorkingSetDescriptor descriptor) {
		Assert.isTrue(!workingSetDescriptors.containsValue(descriptor), "working set descriptor already registered"); //$NON-NLS-1$
		workingSetDescriptors.put(descriptor.getWorkingSetClassName(), descriptor);
	}
	public boolean equals(Object o) {
		return (o instanceof WorkingSetRegistry) && ((WorkingSetRegistry) o).getWorkingSets().equals(getWorkingSets());
	}
	private void firePropertyChange(String changeId, Object oldValue, Object newValue) {
		Object[] listeners = propertyChangeListeners.getListeners();
		PropertyChangeEvent event = new PropertyChangeEvent(this, changeId, oldValue, newValue);

		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}
	/**
	 * Finds a working set by name.
	 *
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @param name the name the working set
	 * @return the working set with the given name or <code>null</code> if not found
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
	public IWorkingSetDialog getWorkingSetDialog(Class workingSetClass) {
		WorkingSetDescriptor descriptor = (WorkingSetDescriptor) workingSetDescriptors.get(workingSetClass.getName());
		
		if (descriptor != null) {
			return descriptor.createWorkingSetDialog();
		}
		return null;
	}
	public int hashCode() {
		return workingSets.hashCode();
	}
	public void load() {
		WorkingSetRegistryReader reader = new WorkingSetRegistryReader();
		reader.readWorkingSets(Platform.getPluginRegistry(), this);
	}	
	/**
	 * Returns all working sets for the workspace.
	 *
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @return an array of IWorkingSet
	 */
	public IWorkingSet[] getWorkingSets() {
		return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
	}
	/**
	 * Removes the working set from the workspace.
	 * This is a NOP if the working set does not exist in the workspace.
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog.
	 */
	public void remove(IWorkingSet workingSet) {
		workingSets.remove(workingSet);
		firePropertyChange(CHANGE_WORKING_SET_REMOVE, workingSet, null);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}
	/**
	 *
	 * @see IResourceChangeListener#resourceChanged
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) {
			WorkbenchPlugin.log("Problem updating working sets", e.getStatus()); //$NON-NLS-1$	
		}

		Iterator iterator = workingSets.iterator();
		
		while (iterator.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iterator.next();
			IAdaptable[] items = workingSet.getItems();
			
			System.out.println(workingSet.getName());			
			for (int i = 0; i < items.length; i++) {
				System.out.println((IResource) items[i].getAdapter(IResource.class));
			}
		}
		
	}
	
	//--- Persistence -----------------------------------------------

	public void restoreState(IMemento memento) {
		IMemento [] workingSets = memento.getChildren(IWorkbenchConstants.TAG_WORKING_SET);
		
		for (int i = 0; i < workingSets.length; i ++) {
			IMemento workingSetMemento = workingSets[i];
			String factoryID = workingSetMemento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
			
			if (factoryID == null) {
				WorkbenchPlugin.log("Unable to restore working set - no factory ID.");//$NON-NLS-1$
				continue;
			}
			IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
			if (factory == null) {
				WorkbenchPlugin.log("Unable to restore working set - cannot instantiate factory: " + factoryID);//$NON-NLS-1$
				continue;
			}
			IAdaptable input = factory.createElement(workingSetMemento);
			if (input == null) {
				WorkbenchPlugin.log("Unable to restore working set - cannot instantiate working set: " + factoryID);//$NON-NLS-1$
				continue;
			}
			if ((input instanceof IWorkingSet) == false) {
				WorkbenchPlugin.log("Unable to restore working set - element is not an IWorkingSet: " + factoryID);//$NON-NLS-1$
				continue;
			}
			add((IWorkingSet) input);
		}
	}

	/* 
	 * For use by WorkingSet#setName/#setItems
	 */
	public void saveState(IMemento memento) {
		Iterator iterator = workingSets.iterator();

		while (iterator.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iterator.next();
			IPersistableElement persistable = null;

			if (workingSet instanceof IPersistableElement) {
				persistable = (IPersistableElement) workingSet;
			}
			else 
			if (workingSet instanceof IAdaptable) {
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
	 * @see IResourceDeltaVisitor#visit(IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		boolean visitChildren = false;
		
		if (delta.getKind() == IResourceDelta.CHANGED) {
			visitChildren = true;
		}
		else
		if (delta.getKind() == IResourceDelta.REMOVED) {
			IPath deltaPath = delta.getResource().getFullPath();
			Iterator iterator = workingSets.iterator();
			
			while (iterator.hasNext()) {
				IWorkingSet workingSet = (IWorkingSet) iterator.next();
				IAdaptable[] items = workingSet.getItems();
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
					workingSet.setItems(newItems);
				}
			}
		}
		return visitChildren;
	}	
}