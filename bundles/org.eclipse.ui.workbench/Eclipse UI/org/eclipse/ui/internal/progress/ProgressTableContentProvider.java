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
	
	public ProgressTableContentProvider(TableViewer table){
		viewer = table;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#add(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void add(Object[] elements) {
		viewer.add(elements);

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
			viewer.refresh(elements[i],true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#remove(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void remove(Object[] elements) {
		viewer.remove(elements);

	}

}
