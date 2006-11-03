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

package org.eclipse.jface.databinding.observable.tree;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.observable.AbstractObservable;
import org.eclipse.jface.databinding.observable.Realm;
import org.eclipse.jface.databinding.util.Policy;
import org.eclipse.jface.internal.databinding.internal.BindingMessages;

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
								BindingMessages
										.getString(BindingMessages.INDEX_OUT_OF_RANGE)));
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
		for (int i = 0; i < listeners.length; i++) {
			((ITreeChangeListener) listeners[i]).handleTreeChange(this, diff);
		}
	}

}
