package org.eclipse.search.internal.core.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	
	private List fExtensions= new ArrayList(3);
	
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
	public void addExtensions(List extensions) {
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