package org.eclipse.search.internal.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * A default implementation of <code>ISearchScope</code>.
 */
public class SearchScope implements ISearchScope {

	private static class WorkbenchScope implements ISearchScope {
	
		WorkbenchScope() {
		}
		
		/* 
		 * Implements method from ISearchScope
		 */
		public boolean encloses(IResource element) {
			return true;
		}	

		/* 
		 * Implements method from ISearchScope
		 */
		public void add(IResource element) {
			// nothing to be added
		}	
	}
		
	public static final ISearchScope WORKBENCH= new WorkbenchScope();
	
	private List fElements;

	public SearchScope() {
		fElements= new ArrayList(5);
	}
	
	/**
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
	
	/**
	 * Returns the search scope elements
	 */
	protected Iterator elements() {
		return fElements.iterator();
	}
}