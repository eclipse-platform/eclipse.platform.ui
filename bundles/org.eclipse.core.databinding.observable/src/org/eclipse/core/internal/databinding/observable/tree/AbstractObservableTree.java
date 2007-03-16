/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164134
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable.tree;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;

/**
 * @since 3.3
 * 
 */
public abstract class AbstractObservableTree extends AbstractObservable
		implements IObservableTree {

	private boolean stale;

	private ListenerList treeListeners = new ListenerList(ListenerList.IDENTITY);

	/**
	 * @param realm
	 */
	public AbstractObservableTree(Realm realm) {
		super(realm);
	}

	public void addChild(TreePath parentPath, Object childElement) {
		throw new UnsupportedOperationException();
	}

	public void addTreeChangeListener(ITreeChangeListener listener) {
		treeListeners.add(listener);
	}

	public int getChildCount(TreePath parentPath) {
		return getChildren(parentPath).length;
	}

	public boolean hasChildren(TreePath parentPath) {
		return getChildCount(parentPath) > 0;
	}

	public void insertChild(TreePath parentPath, int index, Object childElement) {
		throw new UnsupportedOperationException();
	}

	public boolean isLazy() {
		return false;
	}

	public boolean isOrdered() {
		return false;
	}

	public void removeChild(TreePath parentPath, Object childElement) {
		throw new UnsupportedOperationException();
	}

	public void removeChild(TreePath parentPath, int index) {
		throw new UnsupportedOperationException();
	}

	public void removeTreeChangeListener(ITreeChangeListener listener) {
		treeListeners.remove(listener);
	}

	public void setChildCount(TreePath parentPath, int count) {
		throw new UnsupportedOperationException();
	}

	public void setChildren(TreePath parentPath, Object[] children) {
		throw new UnsupportedOperationException();
	}

	public void updateChildren(IChildrenUpdate update) {
		TreePath parent = update.getParent();
		Object[] children = getChildren(parent);
		for (int i = 0; i < update.getLength(); i++) {
			int targetIndex = update.getOffset() + i;
			if (targetIndex < children.length) {
				update.setChild(children[targetIndex], targetIndex);
			} else {
				update
						.setStatus(new Status(
								IStatus.WARNING,
								Policy.JFACE_DATABINDING, 
								IStatus.OK,
								BindingMessages
										.getString(BindingMessages.INDEX_OUT_OF_RANGE), 
										null));
			}
		}
		update.done();
	}

	public void updateChildrenCount(IChildrenCountUpdate update) {
		TreePath[] parents = update.getParents();
		for (int i = 0; i < parents.length; i++) {
			update.setChildCount(parents[i], getChildCount(parents[i]));
		}
		update.done();
	}

	public void updateHasChildren(IHasChildrenUpdate update) {
		TreePath[] parents = update.getElements();
		for (int i = 0; i < parents.length; i++) {
			update.setHasChilren(parents[i], hasChildren(parents[i]));
		}
		update.done();
	}

	public boolean isStale() {
		return stale;
	}

	/**
	 * @param stale
	 */
	public void setStale(boolean stale) {
		this.stale = stale;
		if (stale) {
			fireStale();
		}
	}

	protected void fireTreeChange(TreeDiff diff) {
		// fire general change event first
		fireChange();

		Object[] listeners = treeListeners.getListeners();
		TreeChangeEvent event = new TreeChangeEvent(this, diff);
		for (int i = 0; i < listeners.length; i++) {
			((ITreeChangeListener) listeners[i]).handleTreeChange(event);
		}
	}

}
