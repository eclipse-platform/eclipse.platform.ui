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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.util.Assert;
import org.eclipse.search.internal.core.ISearchScope;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.NewSearchUI;

public class TextSearchEngine {
	
	/**
	 * Search for the given pattern.
	 * @param workspace The workspace
	 * @param scope scope to search in
	 * @param visitDerived if set to true, derived matches will be reported
	 * @param collector the collector for the results
	 * @param matchLocator match locator
	 * @return returns the status of the operation
	 */
	public IStatus search(IWorkspace workspace, ISearchScope scope, boolean visitDerived, ITextSearchResultCollector collector, MatchLocator matchLocator) {
		Assert.isNotNull(workspace);
		Assert.isNotNull(scope);
		Assert.isNotNull(collector);
		Assert.isNotNull(matchLocator);
		IProgressMonitor monitor= collector.getProgressMonitor();
		
		IProject[] projects= workspace.getRoot().getProjects();
		Collection openProjects= new ArrayList(10);
		for (int i= 0; i < projects.length; i++) {
			IProject project= projects[i];
			if (project.isOpen())
				openProjects.add(project);
		}
		String message= SearchMessages.getString("TextSearchEngine.statusMessage"); //$NON-NLS-1$
		MultiStatus status= new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, message, null);
		if (!openProjects.isEmpty()) {
			int amountOfWork= (new AmountOfWorkCalculator(status, visitDerived)).process(openProjects, scope);		
			try {
				monitor.beginTask("", amountOfWork); //$NON-NLS-1$
				if (amountOfWork > 0) {
					Integer[] args= new Integer[] {new Integer(1), new Integer(amountOfWork)};
					monitor.setTaskName(SearchMessages.getFormattedString("TextSearchEngine.scanning", args)); //$NON-NLS-1$
				}				
				collector.aboutToStart();
				TextSearchVisitor visitor= new TextSearchVisitor(matchLocator, scope, visitDerived, collector, status, amountOfWork);
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
