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

package org.eclipse.jface.internal.databinding.provisional.observable.mapping;

import java.util.Iterator;

import org.eclipse.jface.databinding.observable.IObservableCollection;
import org.eclipse.jface.databinding.observable.list.IListChangeListener;
import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.list.ListDiff;
import org.eclipse.jface.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ISetChangeListener;
import org.eclipse.jface.databinding.observable.set.SetDiff;

/**
 * @since 1.0
 * 
 */
abstract public class ObservableMultiMappingWithDomain extends
		AbstractObservableMultiMapping implements
		IObservableMultiMappingWithDomain {

	private ISetChangeListener listener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			for (Iterator it = diff.getAdditions().iterator(); it.hasNext();) {
				addListenerTo(it.next());
			}
			for (Iterator it = diff.getRemovals().iterator(); it.hasNext();) {
				removeListenerFrom(it.next());
			}
		}
	};

	private IListChangeListener listListener = new IListChangeListener() {

		public void handleListChange(IObservableList source, ListDiff diff) {
			ListDiffEntry[] entries = diff.getDifferences();
			for (int i = 0; i < entries.length; i++) {
				ListDiffEntry entry = entries[i];
				if (entry.isAddition()) {
					addListenerTo(entry.getElement());
				} else {
					removeListenerFrom(entry.getElement());
				}
			}
		}
	};

	private IObservableCollection domain;

	/**
	 * 
	 */
	public ObservableMultiMappingWithDomain() {
	}

	/**
	 * @param domain
	 */
	protected void initDomain(IObservableCollection domain) {
		this.domain = domain;
		if (domain instanceof IObservableList) {
			IObservableList listDomain = (IObservableList) domain;
			listDomain.addListChangeListener(listListener);
		} else {
			((IObservableSet) domain).addSetChangeListener(listener);
		}
		for (Iterator it = getDomainIterator(domain); it.hasNext();) {
			addListenerTo(it.next());
		}
	}

	private Iterator getDomainIterator(IObservableCollection domain) {
		Iterator it;
		if (domain instanceof IObservableList) {
			IObservableList listDomain = (IObservableList) domain;
			it = listDomain.iterator();
		} else {
			IObservableSet setDomain = (IObservableSet) domain;
			it = setDomain.iterator();
		}
		return it;
	}

	/**
	 * @return Returns the domain.
	 */
	public IObservableCollection getDomain() {
		return domain;
	}

	/**
	 * @param domainElement
	 */
	protected abstract void addListenerTo(Object domainElement);

	/**
	 * @param domainElement
	 */
	protected abstract void removeListenerFrom(Object domainElement);

	public void dispose() {
		for (Iterator iter = getDomainIterator(domain); iter.hasNext();) {
			removeListenerFrom(iter.next());
		}
		if (domain instanceof IObservableList) {
			IObservableList listDomain = (IObservableList) domain;
			listDomain.removeListChangeListener(listListener);
		} else {
			IObservableSet setDomain = (IObservableSet) domain;
			setDomain.removeSetChangeListener(listener);
		}
		super.dispose();
	}

}
