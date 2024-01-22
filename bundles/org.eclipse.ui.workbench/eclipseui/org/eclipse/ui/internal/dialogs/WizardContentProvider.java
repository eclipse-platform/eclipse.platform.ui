/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.AdaptableList;

/**
 * Provider used by the NewWizardNewPage.
 *
 * @since 3.0
 */
public class WizardContentProvider implements ITreeContentProvider {

	private AdaptableList input;

	@Override
	public void dispose() {
		input = null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof WizardCollectionElement) {
			ArrayList list = new ArrayList();
			WizardCollectionElement element = (WizardCollectionElement) parentElement;

			for (Object childCollection : element.getChildren()) {
				handleChild(childCollection, list);
			}

			for (Object childWizard : element.getWizards()) {
				handleChild(childWizard, list);
			}

			// flatten lists with only one category
			if (list.size() == 1 && list.get(0) instanceof WizardCollectionElement) {
				return getChildren(list.get(0));
			}

			return list.toArray();
		} else if (parentElement instanceof AdaptableList) {
			AdaptableList aList = (AdaptableList) parentElement;
			Object[] children = aList.getChildren();
			ArrayList list = new ArrayList(children.length);
			for (Object element : children) {
				handleChild(element, list);
			}
			// if there is only one category, return it's children directly (flatten list)
			if (list.size() == 1 && list.get(0) instanceof WizardCollectionElement) {
				return getChildren(list.get(0));
			}

			return list.toArray();
		} else {
			return new Object[0];
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof WizardCollectionElement) {
			for (Object child : input.getChildren()) {
				if (child.equals(element)) {
					return input;
				}
			}
			return ((WizardCollectionElement) element).getParent(element);
		}
		return null;
	}

	/**
	 * Adds the item to the list, unless it's a collection element without any
	 * children.
	 *
	 * @param element the element to test and add
	 * @param list    the <code>Collection</code> to add to.
	 * @since 3.0
	 */
	private void handleChild(Object element, ArrayList list) {
		if (element instanceof WizardCollectionElement) {
			if (hasChildren(element)) {
				list.add(element);
			}
		} else {
			list.add(element);
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof WizardCollectionElement) {
			if (getChildren(element).length > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		input = (AdaptableList) newInput;
	}
}
