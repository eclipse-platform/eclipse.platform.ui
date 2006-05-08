/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A viewer filter that will exclude all IWizardDescriptors that do not have at
 * least one tag in a provided set.
 * 
 * @since 3.1
 */
public class WizardTagFilter extends ViewerFilter {

	private String [] myTags;
	
	/**
	 * Create a new instance of this filter
	 * @param tags the wizard tags to allow
	 */
	public WizardTagFilter(String [] tags) {
		myTags = tags;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IWizardDescriptor) {
			IWizardDescriptor desc = (IWizardDescriptor)element;
			String [] tags = desc.getTags();
			for (int i = 0; i < tags.length; i++) {
				for (int j = 0; j < myTags.length; j++) {
					if (tags[i].equals(myTags[j])) {
						return true;
					}
				}
			}
			return false;
		}
        Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                .getContentProvider()).getChildren(element);
        if (children.length > 0) {
			return filter(viewer, element, children).length > 0;
		}

		return false;
	}
}
