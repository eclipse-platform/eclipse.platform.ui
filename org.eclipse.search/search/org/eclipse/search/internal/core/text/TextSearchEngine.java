/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.util.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;

public class TextSearchEngine {
	
	/**
	 * Search for the given pattern.
	 */
	public void search(IWorkspace workspace, String pattern, String options, ISearchScope scope, 
			ITextSearchResultCollector collector) throws CoreException {
				
		Assert.isNotNull(workspace);
		Assert.isNotNull(pattern);
		Assert.isNotNull(scope);
		Assert.isNotNull(collector);
		
		IProgressMonitor monitor= collector.getProgressMonitor();
		
		IProject[] projects= workspace.getRoot().getProjects();
		Collection openProjects= new ArrayList(10);
		for (int i= 0; i < projects.length; i++) {
			IProject project= projects[i];
			if (project.isOpen())
				openProjects.add(project);
		}
		if (!openProjects.isEmpty()) {
			int amountOfWork= (new AmountOfWorkCalculator()).process(openProjects, scope);		
			try {
				monitor.beginTask(SearchMessages.getString("TextSearchEngine.scanning"), amountOfWork); //$NON-NLS-1$
				collector.aboutToStart();
				TextSearchVisitor visitor= new TextSearchVisitor(pattern, options,
					scope, collector);
				visitor.process(openProjects);	
			} finally {
				monitor.done();
				collector.done();
			}
		}
	}
}