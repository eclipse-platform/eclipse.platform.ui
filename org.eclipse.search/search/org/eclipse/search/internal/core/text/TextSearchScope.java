/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.search.internal.core.SearchScope;

/**
 * A special text search scope that take file extensions into account.
 */
public class TextSearchScope extends SearchScope {
	
	private static class WorkspaceScope extends TextSearchScope {
	
		public void add(IResource element) {
			// do nothing
		}
		
		/**
		 * @see ISearchScope#encloses(Object)
		 */
		public boolean encloses(IResource element) {
			if (element instanceof IFile && skipFile((IFile)element))
				return false;
			return true;	
		}	
	}
	
	private Set fExtensions= new HashSet(3);
	
	/**
	 * Adds an extension to the scope.
	 */
	public void addExtension(String extension) {
		fExtensions.add(extension);
	}
	
	/**
	 * Adds all extensions contained in <code>extensions</code> to this
	 * scope.
	 */
	public void addExtensions(Set extensions) {
		if (extensions == null)
			return;
		Iterator iter= extensions.iterator();
		while (iter.hasNext()) {
			fExtensions.add(iter.next());
		}
	}  

	/*
	 * Implements method from ISearchScope
	 */
	public boolean encloses(IResource element) {
		if (element instanceof IFile && skipFile((IFile)element))
			return false;
		return super.encloses(element);	
	}

	/**
	 * Returns a new Workbench scope.
	 */
	public static TextSearchScope newWorkspaceScope() {
		return new WorkspaceScope();
	}
		
	boolean skipFile(IFile file) {
		String extension= file.getFileExtension();
		return !fExtensions.contains(extension);			
	}
	
}