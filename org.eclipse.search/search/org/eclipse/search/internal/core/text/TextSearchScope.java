/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.IWorkingSet;

import org.eclipse.search.internal.core.SearchScope;
import org.eclipse.search.internal.ui.SearchMessages;

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
		
		public boolean encloses(IResourceProxy proxy) {
			if (proxy.getType() == IResource.FILE && skipFile(proxy))
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
		Pattern pattern= Pattern.compile(asRegEx(extension), Pattern.CASE_INSENSITIVE);
		fExtensions.add(pattern.matcher("")); //$NON-NLS-1$
	}

	/*
	 * Converts '*' and '?' to regEx variables.
	 */
	private String asRegEx(String pattern) {
		
		StringBuffer out= new StringBuffer(pattern.length());
		
		boolean escaped= false;
		boolean quoting= false;
	
		int i= 0;
		while (i < pattern.length()) {
			char ch= pattern.charAt(i++);
	
			if (ch == '*' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append(".*"); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '?' && !escaped) {
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("."); //$NON-NLS-1$
				escaped= false;
				continue;
			} else if (ch == '\\' && !escaped) {
				escaped= true;
				continue;								
	
			} else if (ch == '\\' && escaped) {
				escaped= false;
				if (quoting) {
					out.append("\\E"); //$NON-NLS-1$
					quoting= false;
				}
				out.append("\\\\"); //$NON-NLS-1$
				continue;								
			}
	
			if (!quoting) {
				out.append("\\Q"); //$NON-NLS-1$
				quoting= true;
			}
			if (escaped && ch != '*' && ch != '?' && ch != '\\')
				out.append('\\');
			out.append(ch);
			escaped= ch == '\\';
	
		}
		if (quoting)
			out.append("\\E"); //$NON-NLS-1$
		
		return out.toString();
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
	public boolean encloses(IResourceProxy proxy) {
		if (proxy.getType() == IResource.FILE && skipFile(proxy))
			return false;
		return super.encloses(proxy);	
	}

	boolean skipFile(IResourceProxy proxy) {
		if (proxy != null) {
			Iterator iter= fExtensions.iterator();
			while (iter.hasNext()) {
				if (((Matcher)iter.next()).reset(proxy.getName()).matches())
					return false;
			}
		}
		return true;
	}

	/**
	 * Implements method from ISearchScope
	 * 
	 * @deprecated As of 2.1, replaced by {@link #encloses(IResourceProxy)}
	 */
	public boolean encloses(IResource element) {
		if (element.getType() == IResource.FILE && skipFile((IFile)element))
			return false;
		return super.encloses(element);	
	}

	boolean skipFile(IFile file) {
		if (file != null) {
			Iterator iter= fExtensions.iterator();
			while (iter.hasNext()) {
				if (((Matcher)iter.next()).reset(file.getName()).matches())
					return false;
			}
		}
		return true;
	}
}
