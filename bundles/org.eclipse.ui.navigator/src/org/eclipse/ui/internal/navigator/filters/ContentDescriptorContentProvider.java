/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentService;

class ContentDescriptorContentProvider implements ITreeContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];

	private INavigatorContentService contentService;

	private CheckboxTableViewer talbleViewer;

	public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {

		if (aNewInput != null) {

			if (aNewInput instanceof INavigatorContentService) {
				contentService = (INavigatorContentService) aNewInput;
			}

			if (aViewer instanceof CheckboxTableViewer) {
				talbleViewer = (CheckboxTableViewer) aViewer;
			}

			updateCheckState();
		} else {
			contentService = null;
			talbleViewer = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object aParentElement) {
		return NO_CHILDREN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object anElement) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object anElement) {
		return getChildren(anElement).length != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object anInputElement) {
		return contentService != null ? contentService.getVisibleExtensions()
				: NO_CHILDREN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

	}

	private void updateCheckState() {
		if (talbleViewer == null || contentService == null) {
			return;
		}

		INavigatorContentDescriptor descriptor;
		boolean enabled;

		TableItem[] descriptorTableItems = talbleViewer.getTable().getItems();
		for (int i = 0; i < descriptorTableItems.length; i++) {
			if (descriptorTableItems[i].getData() instanceof INavigatorContentDescriptor) {
				descriptor = (INavigatorContentDescriptor) descriptorTableItems[i]
						.getData();
				enabled = contentService.getActivationService()
						.isNavigatorExtensionActive(descriptor.getId());
				talbleViewer.setChecked(descriptor, enabled);
			}
		}
	}

}