/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;
import java.util.HashSet;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
/**
 * The ProgressTableContentProvider is the content provider for tables that are
 * showing jobs.
 */
public class ProgressTableContentProvider extends ProgressContentProvider
		implements
			IStructuredContentProvider {
	TableViewer viewer;
	HashSet jobsToShow;
	final static int numShowItems = 2;
	/**
	 * Initialize the table viewer.
	 * 
	 * @param table
	 *            the table viewer.
	 */
	public ProgressTableContentProvider(TableViewer table) {
		viewer = table;
		jobsToShow = new HashSet();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#add(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void add(Object[] elements) {
		addToShowList(elements);
		viewer.setInput(getRoots(displayNumberToShow(elements), false));
	}
	/**
	 * Add the jobs to the temporary job list.
	 * 
	 * @param elements
	 *            the array of elements.
	 */
	private void addToShowList(Object[] elements) {
		for (int index = 0; index < elements.length; index++)
			jobsToShow.add(elements[index]);
	}
	/**
	 * Return only the elements that we want to display.
	 * 
	 * @param elements
	 *            the array of elements.
	 * @return the elements that we want to display.
	 */
	public Object[] displayNumberToShow(Object[] elements) {
		HashSet jobsLimit = new HashSet();
		for (int index = 0; index < elements.length && index < numShowItems; index++)
			jobsLimit.add(elements[index]);
		return jobsLimit.toArray();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh()
	 */
	public void refresh() {
		viewer.refresh(true);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void refresh(Object[] elements) {
		Object[] refreshes = getRoots(elements, true);
		for (int i = 0; i < refreshes.length; i++) {
			viewer.refresh(refreshes[i], true);
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#remove(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void remove(Object[] elements) {
		jobsToShow.remove(getRoots(elements, false));
		viewer.setInput(getRoots(displayNumberToShow(elements), false));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		JobTreeElement[] elements = ProgressManager.getInstance()
				.getRootElements(ProgressViewUpdater.getSingleton().debug);
		addToShowList(elements);
		return displayNumberToShow(elements);
	}
	/**
	 * Get the root elements of the passed elements as we only show roots.
	 * Replace the element with its parent if subWithParent is true
	 * 
	 * @param elements
	 *            the array of elements.
	 * @param subWithParent
	 *            sub with parent flag.
	 * @return
	 */
	private Object[] getRoots(Object[] elements, boolean subWithParent) {
		if (elements.length == 0)
			return elements;
		HashSet roots = new HashSet();
		for (int i = 0; i < elements.length; i++) {
			JobTreeElement element = (JobTreeElement) elements[i];
			if (element.isJobInfo()) {
				GroupInfo group = ((JobInfo) element).getGroupInfo();
				if (group == null)
					roots.add(element);
				else {
					if (subWithParent)
						roots.add(group);
				}
			} else
				roots.add(element);
		}
		return roots.toArray();
	}
}