/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jface.util.Assert;

/**
 * A default implementation of <code>ISearchScope</code>.
 */
public class SearchScope implements ISearchScope {

	private List fElements;
	private String fDescription;

	public SearchScope(String description) {
		Assert.isNotNull(description);
		fDescription= description;
		fElements= new ArrayList(5);
	}
		
	public SearchScope(String description, IResource[] resources) {
		Assert.isNotNull(description);
		int resourceCount= resources.length;
		fDescription= description;		
		fElements= new ArrayList(resourceCount + 5);
		for (int i= 0; i < resourceCount; i++)
			fElements.add(resources[i]);
	}
	
	/*
	 * @see ISearchScope#add(IResource)
	 */
	public void add(IResource element) {
		fElements.add(element);
	}
	
	/*
	 * Implements method from ISearchScope
	 */
	public boolean encloses(IResource element) {
		IPath elementPath= element.getFullPath();
		Iterator iter= elements();
		while (iter.hasNext()) {
			IResource resource= (IResource)iter.next();
			if (resource.getFullPath().isPrefixOf(elementPath))
				return true;
		}
		return false;
	}

	/*
	 * Implements method from ISearchScope
	 */
	public String getDescription() {
		return fDescription;
	}
	
	/**
	 * Returns the search scope elements
	 */
	protected Iterator elements() {
		return fElements.iterator();
	}
}