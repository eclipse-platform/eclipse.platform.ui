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

import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ISetChangeListener;
import org.eclipse.jface.databinding.observable.set.SetDiff;

/**
 * @since 1.0
 * 
 */
abstract public class ObservableMappingWithDomain extends AbstractObservableMapping implements IObservableMappingWithDomain {

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

	private IObservableSet domain;

	/**
	 * 
	 */
	public ObservableMappingWithDomain() {
	}
	
	/**
	 * @param domain
	 */
	protected void initDomain(IObservableSet domain) {
		this.domain = domain;
		domain.addSetChangeListener(listener);
		for (Iterator it = domain.iterator(); it.hasNext();) {
			addListenerTo(it.next());
		}
	}

	/**
	 * @return Returns the domain.
	 */
	public IObservableSet getDomain() {
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
		for (Iterator iter = domain.iterator(); iter.hasNext();) {
			removeListenerFrom(iter.next());
		}
		domain.removeSetChangeListener(listener);
		super.dispose();
	}

}
