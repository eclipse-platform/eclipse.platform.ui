/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core.text;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import org.eclipse.jface.util.Assert;

import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchMessages;

public class TextSearchEngine {
	
	/**
	 * Search for the given pattern.
	 */
	public IStatus search(IWorkspace workspace, String pattern, String options, ISearchScope scope, ITextSearchResultCollector collector) {
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
		String message= SearchMessages.getString("TextSearchEngine.statusMessage"); //$NON-NLS-1$
		MultiStatus status= new MultiStatus(SearchUI.PLUGIN_ID, IStatus.OK, message, null);
		if (!openProjects.isEmpty()) {
			int amountOfWork= (new AmountOfWorkCalculator(status)).process(openProjects, scope);		
			try {
				monitor.beginTask(SearchMessages.getString("TextSearchEngine.scanning"), amountOfWork); //$NON-NLS-1$
				collector.aboutToStart();
				TextSearchVisitor visitor= new TextSearchVisitor(pattern, options, scope, collector, status);
				visitor.process(openProjects);	
			} catch (CoreException ex) {
				status.add(ex.getStatus());
			} finally {
				monitor.done();
				try {
					collector.done();
				} catch (CoreException ex) {
					status.add(ex.getStatus());
				}
			}
		}
		return status;
	}
}