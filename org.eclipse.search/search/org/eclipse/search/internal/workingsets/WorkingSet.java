package org.eclipse.search.internal.workingsets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.util.Assert;

import org.eclipse.search.ui.IWorkingSet;


public class WorkingSet implements IWorkingSet {

	private static Set fgWorkingSets= new HashSet(5);
	
	String fName;
	Set fElements; // of IResources

	WorkingSet(String name, Object[] elements) {
		setName(name);
		setResources(elements);
	}

	void setResources(Object[] elements) {
		Assert.isNotNull(elements, "IPath array must not be null");
		fElements= new HashSet(elements.length);
		for (int i= 0; i < elements.length; i++) {
			Assert.isTrue(elements[i] instanceof IResource);
			Assert.isTrue(!fElements.contains(elements[i]), "elements must only contain each element once");
			fElements.add(elements[i]);
		}
	}

	void setPaths(IPath[] elements) {
		Assert.isNotNull(elements, "IPath array must not be null");
		fElements= new HashSet(elements.length);
		for (int i= 0; i < elements.length; i++) {
			Assert.isTrue(!fElements.contains(elements[i]), "elements must only contain each element once");
			fElements.add(elements[i]);
		}
	}

	/*
	 * @see IWorkingSet#getName()
	 */
	public String getName() {
		return fName;
	}

	void setName(String name) {
		Assert.isNotNull(name, "name must not be null");
		fName= name;
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
	 * Returns the workbench from which this plugin has been loaded.
	 */	
	public static IWorkingSet[] getWorkingSets() {
		return (IWorkingSet[])fgWorkingSets.toArray(new IWorkingSet[fgWorkingSets.size()]);
	}

	/**
	 * Removes the working set from the workspace.
	 * This is a NOP if the working set does not exist in the workspace.
	 */	
	static void remove(IWorkingSet workingSet) {
		fgWorkingSets.remove(workingSet);
	}

	/**
	 * Adds the working set to the workspace.
	 * The working set must not exist yet.
	 */	
	static void add(IWorkingSet workingSet) {
		Assert.isTrue(!fgWorkingSets.contains(workingSet), "working set already registered");
		fgWorkingSets.add(workingSet);
	}
}