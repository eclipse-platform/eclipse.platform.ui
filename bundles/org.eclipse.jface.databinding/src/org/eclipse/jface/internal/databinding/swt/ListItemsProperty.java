/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 251611
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.3
 *
 */
public class ListItemsProperty extends ControlStringListProperty<List> {
	@Override
	protected void doUpdateStringList(final List list, ListDiff<String> diff) {
		diff.accept(new ListDiffVisitor<String>() {
			@Override
			public void handleAdd(int index, String element) {
				list.add(element, index);
			}

			@Override
			public void handleRemove(int index, String element) {
				list.remove(index);
			}

			// public void handleMove(int oldIndex, int newIndex, Object
			// element) {
			// int selectionIndex = combo.getSelectionIndex();
			// Listener[] modifyListeners = combo.getListeners(SWT.Modify);
			// if (selectionIndex == oldIndex) {
			// for (int i = 0; i < modifyListeners.length; i++)
			// combo.removeListener(SWT.Modify, modifyListeners[i]);
			// }
			//
			// super.handleMove(oldIndex, newIndex, element);
			//
			// if (selectionIndex == oldIndex) {
			// combo.select(newIndex);
			// for (int i = 0; i < modifyListeners.length; i++)
			// combo.addListener(SWT.Modify, modifyListeners[i]);
			// }
			// }

			@Override
			public void handleReplace(int index, String oldElement, String newElement) {
				list.setItem(index, newElement);
			}
		});
	}

	@Override
	public String[] doGetStringList(List control) {
		return control.getItems();
	}

	@Override
	public String toString() {
		return "List.items[] <String>"; //$NON-NLS-1$
	}
}
