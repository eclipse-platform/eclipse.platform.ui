/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.SearchMessages;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import org.eclipse.ui.IWorkingSet;

/**
 * A special text search scope that take file extensions into account.
 */
public class TextSearchScope extends SearchScope {
	
	private static final boolean IS_CASE_SENSITIVE_FILESYSTEM = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$
	
	private static class WorkspaceScope extends TextSearchScope {
	
		private WorkspaceScope() {
			super(SearchMessages.getString("WorkspaceScope")); //$NON-NLS-1$
		}
		
		public void add(IResource element) {
			// do nothing
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.search.internal.core.text.TextSearchScope#encloses(IResourceProxy)
		 */
		public boolean encloses(IResourceProxy proxy) {
			// avoid to get the full path of the proxy
			if (proxy.getType() == IResource.FILE && skipFile(proxy.getName()))
				return false;
			return true;	
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.search.internal.core.text.TextSearchScope#encloses(org.eclipse.core.runtime.IPath, int)
		 */
		public boolean encloses(IPath elementPath, int elementType) {
			if (elementType == IResource.FILE && skipFile(elementPath.lastSegment()))
				return false;
			return true;	
		}
		
	}
	
	private Set fExtensions= new HashSet(3);
	private Matcher[] fFileNameMatchers= null;

	/**
	 * @return Returns a workbench scope.
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
	 * @param extension
	 */
	public void addExtension(String extension) {
		if (fExtensions.add(extension)) {
			fFileNameMatchers= null; // clear cache
		}
	}

	private Matcher[] getFileNameMatchers() {
		if (fFileNameMatchers == null) {
			fFileNameMatchers= new Matcher[fExtensions.size()];
			int i= 0;
			for (Iterator iter= fExtensions.iterator(); iter.hasNext();) {
				String ext= (String) iter.next();
				Pattern pattern= PatternConstructor.createPattern(ext, IS_CASE_SENSITIVE_FILESYSTEM, false); 
				fFileNameMatchers[i++]= pattern.matcher(""); //$NON-NLS-1$
			}
		}
		return fFileNameMatchers;
	}

	/**
	 * Adds all string patterns contained in <code>extensions</code> to this
	 * scope. The allowed pattern characters are <code>*</code> for any character
	 * and <code>?</code> for one character.
	 * @param extensions
	 */
	public void addExtensions(String[] extensions) {
		for (int i= 0; i < extensions.length; i++) {
			addExtension(extensions[i]);
		}
	}  

	/*
	 * Implements method from ISearchScope
	 */
	public boolean encloses(IResourceProxy proxy) {
		if (proxy.getType() == IResource.FILE && skipFile(proxy.getName()))
			return false;
		return super.encloses(proxy);	
	}

	protected boolean encloses(IPath elementPath, int elementType) {
		if (elementType == IResource.FILE && skipFile(elementPath.lastSegment()))
			return false;
		return super.encloses(elementPath, elementType);	
	}

	/*package */ final boolean skipFile(String fileName) {
		Matcher[] matchers= getFileNameMatchers();
		for (int i= 0; i < matchers.length; i++) {
			if (matchers[i].reset(fileName).matches()) {
				return false;
			}
		}
		return true;
	}
	
	public String getExtensionsLabel() {
		String[] ext= (String[]) fExtensions.toArray(new String[fExtensions.size()]);
		Arrays.sort(ext);
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < ext.length; i++) {
			if (i > 0) {
				buf.append(", "); //$NON-NLS-1$
			}
			buf.append(ext[i]);
		}
		return buf.toString();
	}
	
}
