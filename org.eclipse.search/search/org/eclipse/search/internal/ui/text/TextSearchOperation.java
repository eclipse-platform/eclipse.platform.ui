/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.actions.WorkspaceModifyOperation;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.core.text.TextSearchEngine;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPluginImages;

/**
 * An operation to perform a regular text search.
 */
public class TextSearchOperation extends WorkspaceModifyOperation {

	public static final int NO_PRIORITY_CHANGE= -1;
	
	private IWorkspace fWorkspace;
	private String fPattern;
	private String fOptions;
	private ISearchScope fScope;
	private TextSearchResultCollector fCollector;
	private IStatus fStatus;
	
	/**
	 * Creates a new text search operation.
	 */
	public TextSearchOperation(IWorkspace workspace,  String pattern, String options, 
			ISearchScope scope, TextSearchResultCollector collector)  {
		Assert.isNotNull(collector);
		fWorkspace= workspace;
		fPattern= pattern;
		fOptions= options;
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
		fStatus= engine.search(fWorkspace, fPattern, fOptions, fScope, fCollector);
	}	

	String getSingularLabel() {
		if (fPattern == null || fPattern.length() < 1)
			return SearchMessages.getFormattedString("FileSearchOperation.singularLabelPostfix", new String[] {fScope.getDescription()}); //$NON-NLS-1$
		else
			return SearchMessages.getFormattedString("TextSearchOperation.singularLabelPostfix", new String[] {fPattern, fScope.getDescription()}); //$NON-NLS-1$
	}

	String getPluralLabelPattern() {
		if (fPattern == null || fPattern.length() < 1)
			return SearchMessages.getFormattedString("FileSearchOperation.pluralLabelPatternPostfix", new String[] {"{0}", fScope.getDescription()}); //$NON-NLS-2$ //$NON-NLS-1$
		else
			return SearchMessages.getFormattedString("TextSearchOperation.pluralLabelPatternPostfix", new String[] {fPattern, "{0}", fScope.getDescription()}); //$NON-NLS-2$ //$NON-NLS-1$
	}
	
	ImageDescriptor getImageDescriptor() {
		return SearchPluginImages.DESC_OBJ_TSEARCH_DPDN;
	}
	
	IStatus getStatus() {
		return fStatus;
	}
	
	String getPattern() {
		return fPattern;
	}
}