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

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;

/**
 * The ProgressTableContentProvider is the content provider for tables
 * that are showing jobs.
 */
public class ProgressTableContentProvider
	extends ProgressContentProvider
	implements IStructuredContentProvider {

	TableViewer viewer;

	public ProgressTableContentProvider(TableViewer table) {
		viewer = table;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#add(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void add(Object[] elements) {
		Object[] infos = getJobInfos(elements);
		if (infos == null)
			return;
		viewer.add(infos);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh()
	 */
	public void refresh() {
		viewer.refresh(true);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void refresh(Object[] elements) {
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JobInfo)
				viewer.refresh(elements[i], true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#remove(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void remove(Object[] elements) {
		Object[] infos = getJobInfos(elements);
		if (infos == null)
			return;
		viewer.remove(infos);

	}
	/**
	 * We are only showing job infos here so filter out the
	 * groups.
	 * @return Object[] or null if there aren't any
	 */
	private Object[] getJobInfos(Object[] elements) {
		Collection result = new HashSet();
		for (int i = 0; i < elements.length; i++) {
			Object object = elements[i];
			if (object instanceof JobInfo)
				result.add(object);
		}

		if (result.isEmpty())
			return null;
		else
			return result.toArray();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return ProgressManager.getInstance().getJobInfos(ProgressViewUpdater.getSingleton().debug);
	}

}
