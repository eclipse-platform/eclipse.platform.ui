/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.model.WorkbenchWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class WorkingSet implements IWorkingSet, IAdaptable {
	private static SortedSet workingSets = new TreeSet(new WorkingSetComparator());

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

	String name;
	Set items; // of IAdaptable

	public WorkingSet(String name, Object[] elements) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		setItems(elements, true);
	}
	/**
	 * Adds the working set to the workspace.
	 * The working set must not exist yet.
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog. 
	 */
	public static void add(IWorkingSet workingSet) {
		Assert.isTrue(!workingSets.contains(workingSet), "working set already registered"); //$NON-NLS-1$
		workingSets.add(workingSet);
		saveWorkingSets();
	}
	public boolean equals(Object o) {
		return (o instanceof IWorkingSet) && ((IWorkingSet) o).getName().equals(getName());
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
	public static IWorkingSet find(String name) {
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
	private IProject[] getProjects() {
		HashSet projects = new HashSet();
		Iterator iterator = items.iterator();
		IProject[] projectArray;
		
		while (iterator.hasNext()) {
			IAdaptable adaptable = (IAdaptable) iterator.next();
			IResource resource = (IResource) adaptable.getAdapter(IResource.class);
			if (resource != null) {
				IProject project = resource.getProject();
				if (project != null) {
					projects.add(project);
				}
			}
		}
		projectArray = (IProject[]) projects.toArray(new IProject[projects.size()]);
		return projectArray;
	}
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			Vector resources = new Vector(items.size());
			Iterator iterator = items.iterator();
			IResource[] workingSetResources;
			
			while (iterator.hasNext()) {
				IAdaptable adaptable = (IAdaptable) iterator.next();
				IResource resource = (IResource) adaptable.getAdapter(IResource.class);
				if (resource != null) {
					resources.add(resource);
				}
			}
			// cache working set resources. 
			// not every working set item may adapt to an IResource.
			workingSetResources = new IResource[resources.size()];
			resources.copyInto(workingSetResources);

			IProject[] projects = getProjects();
			
			return new WorkbenchWorkingSet(null, workingSetResources);
		}
		return null;
	}
	/*
	 * @see IWorkingSet#getName()
	 */
	public String getName() {
		return name;
	}
	/*
	 * @see IWorkingSet#getItems()
	 */
	public IAdaptable[] getItems() {
		return (IAdaptable[]) items.toArray(new IAdaptable[items.size()]);
	}
	public int hashCode() {
		return name.hashCode();
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
	public static IWorkingSet[] getWorkingSets() {
		return (IWorkingSet[]) workingSets.toArray(new IWorkingSet[workingSets.size()]);
	}
	/**
	 * Removes the working set from the workspace.
	 * This is a NOP if the working set does not exist in the workspace.
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog.
	 */
	public static void remove(IWorkingSet workingSet) {
		workingSets.remove(workingSet);
		saveWorkingSets();
	}
	/*
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetDialog.
	 */
	public void setItems(Object[] elements) {
		setItems(elements, false);
	}

	private void setItems(Object[] elements, boolean internal) {
		Assert.isNotNull(elements, "IPath array must not be null"); //$NON-NLS-1$
		items = new HashSet(elements.length);
		for (int i = 0; i < elements.length; i++) {
			Assert.isTrue(elements[i] instanceof IAdaptable);
			Assert.isTrue(!items.contains(elements[i]), "elements must only contain each element once"); //$NON-NLS-1$
			items.add(elements[i]);
		}
		if (!internal)
			saveWorkingSets();
	}
	/*
	 * Public for use by org.eclipse.ui.internal.dialogs.WorkingSetDialog.
	 */
	public void setName(String name) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		this.name = name;
		saveWorkingSets();
	}
	//--- Persistency -----------------------------------------------

	private static void restore() {
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

	private static void saveWorkingSets() {
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
}