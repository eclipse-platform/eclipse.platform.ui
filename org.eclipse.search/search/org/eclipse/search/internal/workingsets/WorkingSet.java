/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.workingsets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.util.Assert;

import org.xml.sax.SAXException;

import org.eclipse.search.ui.IWorkingSet;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

/**
 * @deprecated use org.eclipse.ui.IWorkingSet support - this class will be removed soon
 */
public class WorkingSet implements IWorkingSet {

	private static SortedSet fgWorkingSets= new TreeSet(new WorkingSetComparator());
	

	// XML tags
	static String TAG_WORKINGSETS= "workingsets"; //$NON-NLS-1$
	static String TAG_WORKINGSET= "workingset"; //$NON-NLS-1$
	static String TAG_NAME= "name"; //$NON-NLS-1$
	static String TAG_CONTENTS= "contents"; //$NON-NLS-1$
	static String TAG_FILE= "file"; //$NON-NLS-1$
	static String TAG_FOLDER= "folder"; //$NON-NLS-1$
	static String TAG_PATH= "path"; //$NON-NLS-1$
	static String TAG_PROJECT= "project"; //$NON-NLS-1$

	// Persistency
	static String STORE_NAME= "workingsets.xml"; //$NON-NLS-1$
	static {
		restore();
	}
	
	String fName;
	Set fElements; // of IResources

	WorkingSet(String name, Object[] elements) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		fName= name;
		setResources(elements, true);
	}

	void setResources(Object[] elements) {
		setResources(elements, false);
	}

	private void setResources(Object[] elements, boolean internal) {
		Assert.isNotNull(elements, "IPath array must not be null"); //$NON-NLS-1$
		fElements= new HashSet(elements.length);
		for (int i= 0; i < elements.length; i++) {
			Assert.isTrue(elements[i] instanceof IResource);
			Assert.isTrue(!fElements.contains(elements[i]), "elements must only contain each element once"); //$NON-NLS-1$
			fElements.add(elements[i]);
		}
		if (!internal)
			saveWorkingSets();
	}

	/*
	 * @see IWorkingSet#getName()
	 */
	public String getName() {
		return fName;
	}

	void setName(String name) {
		Assert.isNotNull(name, "name must not be null"); //$NON-NLS-1$
		fName= name;
		saveWorkingSets();
	}

	/*
	 * @see IWorkingSet#getResources()
	 */
	public IResource[] getResources() {
		return (IResource[])fElements.toArray(new IResource[fElements.size()]);
	}

	public boolean equals (Object o) {
		return (o instanceof IWorkingSet) && ((IWorkingSet)o).getName().equals(getName());
	}

	public int hashCode() {
		return fName.hashCode();
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
		return (IWorkingSet[])fgWorkingSets.toArray(new IWorkingSet[fgWorkingSets.size()]);
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
		if (name == null || fgWorkingSets == null)
			return null;
		
		Iterator iter= fgWorkingSets.iterator();
		while (iter.hasNext()) {
			IWorkingSet workingSet= (IWorkingSet)iter.next();
			if (name.equals(workingSet.getName()))
				return workingSet;
		}
		return null;
	}

	/**
	 * Removes the working set from the workspace.
	 * This is a NOP if the working set does not exist in the workspace.
	 */	
	static void remove(IWorkingSet workingSet) {
		fgWorkingSets.remove(workingSet);
		saveWorkingSets();
	}

	/**
	 * Adds the working set to the workspace.
	 * The working set must not exist yet.
	 */	
	static void add(IWorkingSet workingSet) {
		Assert.isTrue(!fgWorkingSets.contains(workingSet), "working set already registered"); //$NON-NLS-1$
		fgWorkingSets.add(workingSet);
		saveWorkingSets();
	}

	//--- Persistency -----------------------------------------------

	private static void restore() {
		WorkingSetReader reader= null;
		IWorkingSet[] workingSets= null;		
		try {
			File file= SearchPlugin.getDefault().getStateLocation().append(STORE_NAME).toFile();
			if (!file.exists())
				return;
			reader= new WorkingSetReader(new BufferedInputStream(new FileInputStream(file)));
			workingSets= reader.readXML();
		} catch (IOException ex) {
			String message= WorkingSetMessages.getFormattedString("WorkingSet.error.readingFile", ex.getMessage()); //$NON-NLS-1$
			ExceptionHandler.log(ex, message);
		} catch (SAXException ex) {
			String message= WorkingSetMessages.getFormattedString("WorkingSet.error.badXmlFormat", ex.getMessage()); //$NON-NLS-1$
			ExceptionHandler.log(ex, message);
		} finally {
			try {
				if (reader != null)
					reader.close();
			}
			catch (IOException ex) {
				String message= WorkingSetMessages.getFormattedString("WorkingSet.error.close", ex.getMessage()); //$NON-NLS-1$
				ExceptionHandler.log(ex, message);
			}
		}
		if (workingSets != null)
			for (int i= 0; i < workingSets.length; i++)
				WorkingSet.add(workingSets[i]);
	}

	private static void saveWorkingSets() {
		WorkingSetWriter writer= null;
		try {
			File file= SearchPlugin.getDefault().getStateLocation().append(STORE_NAME).toFile();
			writer= new WorkingSetWriter(new BufferedOutputStream(new FileOutputStream(file)));
			writer.writeXML(SearchUI.getWorkingSets());
		} catch (IOException ex) {
			String message= WorkingSetMessages.getFormattedString("WorkingSet.error.readingFile", ex.getMessage()); //$NON-NLS-1$
			ExceptionHandler.log(ex, message);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (IOException ex) {
					String message= WorkingSetMessages.getFormattedString("WorkingSet.error.readingFile", ex.getMessage()); //$NON-NLS-1$
					ExceptionHandler.log(ex, message);
				}
		}
	}
}