/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
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

	@Override
	public Object[] getChildren(Object aParentElement) {
		return NO_CHILDREN;
	}

	@Override
	public Object getParent(Object anElement) {
		return null;
	}

	@Override
	public boolean hasChildren(Object anElement) {
		return getChildren(anElement).length != 0;
	}

	@Override
	public Object[] getElements(Object anInputElement) {
		return contentService != null ? contentService.getVisibleExtensions()
				: NO_CHILDREN;
	}

	@Override
	public void dispose() {

	}

	private void updateCheckState() {
		if (talbleViewer == null || contentService == null) {
			return;
		}

		INavigatorContentDescriptor descriptor;
		boolean enabled;

		TableItem[] descriptorTableItems = talbleViewer.getTable().getItems();
		for (TableItem descriptorTableItem : descriptorTableItems) {
			if (descriptorTableItem.getData() instanceof INavigatorContentDescriptor) {
				descriptor = (INavigatorContentDescriptor) descriptorTableItem
						.getData();
				enabled = contentService.getActivationService()
						.isNavigatorExtensionActive(descriptor.getId());
				talbleViewer.setChecked(descriptor, enabled);
			}
		}
	}

}