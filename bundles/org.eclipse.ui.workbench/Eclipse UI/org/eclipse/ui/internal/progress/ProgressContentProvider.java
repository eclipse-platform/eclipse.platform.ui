/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The ProgressContentProvider is the content provider used for classes that
 * listen to the progress changes.
 */
public class ProgressContentProvider implements ITreeContentProvider {

	ProgressTreeViewer viewer;

	public ProgressContentProvider(ProgressTreeViewer mainViewer) {
		viewer = mainViewer;
		ProgressViewUpdater.getSingleton().addContentProvider(this);
	}
	/*
	 * (non-Javadoc) @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		return ((JobTreeElement) parentElement).getChildren();
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element == this)
			return null;
		else
			return ((JobTreeElement) element).getParent();
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element == this)
			return ProgressManager.getInstance().hasJobInfos();
		else
			return ((JobTreeElement) element).hasChildren();
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {

		return ProgressManager.getInstance().getJobInfos(
				ProgressViewUpdater.getSingleton().debug);

	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		ProgressViewUpdater.getSingleton().removeContentProvider(this);
	}

	/*
	 * (non-Javadoc) @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 * java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer updateViewer, Object oldInput, Object newInput) {
	}

}
