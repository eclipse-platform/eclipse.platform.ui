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
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.core.text.MatchLocator;
import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPluginImages;

/**
 * An operation to perform a regular text search.
 */
public class TextSearchOperation extends WorkspaceModifyOperation {

	public static final int NO_PRIORITY_CHANGE= -1;
	
	private IWorkspace fWorkspace;
	private MatchLocator fMatchLocator;
	private ISearchScope fScope;
	private TextSearchResultCollector fCollector;
	private IStatus fStatus;
	
	/**
	 * Creates a new text search operation.
	 */
	public TextSearchOperation(IWorkspace workspace,  String pattern, boolean isCaseSensitive, boolean isRegexSearch, 
		ISearchScope scope, TextSearchResultCollector collector)  {
		super(null);
		Assert.isNotNull(collector);
		fWorkspace= workspace;
		fMatchLocator= new MatchLocator(pattern, isCaseSensitive, isRegexSearch);
		fScope= scope;
		fCollector= collector;
		fCollector.setOperation(this);
	}
	
	/**
	 * The actual algorithm.
	 */
	protected void execute(IProgressMonitor monitor) {
		fCollector.setProgressMonitor(monitor);		
		TextSearchEngine engine= new TextSearchEngine();
		fStatus= engine.search(fWorkspace, fScope, false, fCollector, fMatchLocator);
	}	
	
	void searchInFile(IFile file, ITextSearchResultCollector collector) {
		TextSearchEngine engine= new TextSearchEngine();
		TextSearchScope scope= new TextSearchScope(""); //$NON-NLS-1$
		scope.add(file);
		scope.addExtension("*"); //$NON-NLS-1$
		fStatus= engine.search(fWorkspace, scope, false, collector, fMatchLocator);
	}

	String getSingularLabel() {
		String pattern= fMatchLocator.getPattern();
		if (pattern == null || pattern.length() < 1)
			return SearchMessages.getFormattedString("FileSearchOperation.singularLabelPostfix", new String[] {fScope.getDescription()}); //$NON-NLS-1$
		else
			return SearchMessages.getFormattedString("TextSearchOperation.singularLabelPostfix", new String[] {fMatchLocator.getPattern(), fScope.getDescription()}); //$NON-NLS-1$
	}

	String getPluralLabelPattern() {
		String pattern= fMatchLocator.getPattern();
		if (pattern == null || pattern.length() < 1)
			return SearchMessages.getFormattedString("FileSearchOperation.pluralLabelPatternPostfix", new String[] {"{0}", fScope.getDescription()}); //$NON-NLS-2$ //$NON-NLS-1$
		else
			return SearchMessages.getFormattedString("TextSearchOperation.pluralLabelPatternPostfix", new String[] {fMatchLocator.getPattern(), "{0}", fScope.getDescription()}); //$NON-NLS-2$ //$NON-NLS-1$
	}
	
	ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}
	
	IStatus getStatus() {
		return fStatus;
	}
	
	String getPattern() {
		return fMatchLocator.getPattern();
	}	
}
