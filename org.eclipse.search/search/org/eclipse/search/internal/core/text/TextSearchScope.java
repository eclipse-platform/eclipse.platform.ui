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
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.util.StringMatcher;

/**
 * A special text search scope that take file extensions into account.
 */
public class TextSearchScope extends SearchScope {
	
	private static class WorkspaceScope extends TextSearchScope {
	
		private WorkspaceScope(String description) {
			super(description);
		}
		
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
	 * Returns a new Workbench scope.
	 */
	public static TextSearchScope newWorkspaceScope() {
		return new WorkspaceScope(SearchMessages.getString("WorkspaceScope"));
	}

	public TextSearchScope(String description) {
		super(description);
	}

	public TextSearchScope(String description, IResource[] resources) {
		super(description, resources);
	}
	
	/**
	 * Adds an extension to the scope.
	 */
	public void addExtension(String extension) {
		fExtensions.add(new StringMatcher(extension, false, false));
	}
	/**
	 * Adds all string patterns contained in <code>extensions</code> to this
	 * scope. The allowed pattern characters are <code>*</code> for any character
	 * and <code>?</code> for one character.
	 */
	public void addExtensions(Set extensions) {
		if (extensions == null)
			return;
		Iterator iter= extensions.iterator();
		while (iter.hasNext()) {
			Object obj= iter.next();
			if (obj instanceof String)
				addExtension((String)obj);
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

	boolean skipFile(IFile file) {
		if (file != null) {
			Iterator iter= fExtensions.iterator();
			while (iter.hasNext()) {
				if (((StringMatcher)iter.next()).match(file.getName()))
					return false;
			}
		}
		return true;
	}
}