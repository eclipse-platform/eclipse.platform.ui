/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetRegistry;

public class WorkingSetRegistry implements IWorkingSetRegistry, IResourceChangeListener, IResourceDeltaVisitor {
	private static WorkingSetRegistry instance;

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

	public static WorkingSetRegistry getInstance() {
		if (instance == null) {
			instance = new WorkingSetRegistry();
		}
		return instance;
	}
	private WorkingSetRegistry() {
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
		saveWorkingSets();
		firePropertyChange(CHANGE_WORKING_SET_ADD, null, workingSet);
	}
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	public boolean equals(Object o) {
		return (o instanceof IWorkingSetRegistry) && ((IWorkingSetRegistry) o).getWorkingSets().equals(getWorkingSets());
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
	public int hashCode() {
		return workingSets.hashCode();
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
		saveWorkingSets();
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
	
	//--- Persistency -----------------------------------------------

	private void restore() {
/*		WorkingSetReader reader = null;
		IWorkingSet[] workingSets = null;
		try {
			File file = SearchPlugin.getDefault().getStateLocation().append(STORE_NAME).toFile();
			if (!file.exists())
				return;
			reader = new WorkingSetReader(new BufferedInputStream(new FileInputStream(file)));
			workingSets = reader.readXML();
		} catch (IOException ex) {
			String message = WorkingSetMessages.getFormattedString("WorkingSet.error.readingFile", ex.getMessage()); //$NON-NLS-1$
			ExceptionHandler.log(ex, message);
		} catch (SAXException ex) {
			String message = WorkingSetMessages.getFormattedString("WorkingSet.error.badXmlFormat", ex.getMessage()); //$NON-NLS-1$
			ExceptionHandler.log(ex, message);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				String message = WorkingSetMessages.getFormattedString("WorkingSet.error.close", ex.getMessage()); //$NON-NLS-1$
				ExceptionHandler.log(ex, message);
			}
		}
		if (workingSets != null)
			for (int i = 0; i < workingSets.length; i++)
				WorkingSet.add(workingSets[i]);*/
	}

	/* 
	 * For use by WorkingSet#setName/#setItems
	 */
	public void saveWorkingSets() {
/*		WorkingSetWriter writer = null;
		try {
			File file = SearchPlugin.getDefault().getStateLocation().append(STORE_NAME).toFile();
			writer = new WorkingSetWriter(new BufferedOutputStream(new FileOutputStream(file)));
			writer.writeXML(SearchUI.getWorkingSets());
		} catch (IOException ex) {
			String message = WorkingSetMessages.getFormattedString("WorkingSet.error.readingFile", ex.getMessage()); //$NON-NLS-1$
			ExceptionHandler.log(ex, message);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException ex) {
					String message = WorkingSetMessages.getFormattedString("WorkingSet.error.readingFile", ex.getMessage()); //$NON-NLS-1$
					ExceptionHandler.log(ex, message);
				}
		}*/
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