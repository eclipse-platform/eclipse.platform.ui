/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IWorkingSet;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.util.StringMatcher;

/**
 * A special text search scope that take file extensions into account.
 */
public class TextSearchScope extends SearchScope {
	
	private static class WorkspaceScope extends TextSearchScope {
	
		private WorkspaceScope() {
			super(SearchMessages.getString("WorkspaceScope")); //$NON-NLS-1$
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
		return new WorkspaceScope();
	}

	public TextSearchScope(String description) {
		super(description);
	}

	public TextSearchScope(String description, IResource[] resources) {
		super(description, resources);
	}

	public TextSearchScope(String description, IAdaptable[] elements) {
		super(description, convertToResources(elements));

	}

	public TextSearchScope(String description, IWorkingSet[] workingSets) {
		super(description, convertToResources(getElements(workingSets)));
	}
	
	private static IResource[] convertToResources(IAdaptable[] elements) {
		int length= elements.length;
		Set resources= new HashSet(length);
		for (int i= 0; i < length; i++) {
			IResource resource= (IResource)elements[i].getAdapter(IResource.class);
			if (resource != null)
				resources.add(resource);
		}
		return (IResource[])resources.toArray(new IResource[resources.size()]);
	}

	private static IAdaptable[] getElements(IWorkingSet[] workingSets) {
		int length= workingSets.length;
		Set elements= new HashSet(length);
		for (int i= 0; i < length; i++) {
			elements.addAll(Arrays.asList(workingSets[i].getElements()));
		}
		return (IAdaptable[])elements.toArray(new IAdaptable[elements.size()]);
	}
	
	/**
	 * Adds an extension to the scope.
	 */
	public void addExtension(String extension) {
		fExtensions.add(new StringMatcher(extension, true, false));
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