/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 278550
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.ListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * @since 3.3
 * 
 */
public class ValuePropertyDetailList extends ListProperty {
	private final IValueProperty masterProperty;
	private final IListProperty detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public ValuePropertyDetailList(IValueProperty masterProperty,
			IListProperty detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	public Object getElementType() {
		return detailProperty.getElementType();
	}

	public IObservableList observe(final Realm realm, final Object source) {
		final IObservableValue[] masterValue = new IObservableValue[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterValue[0] = masterProperty.observe(realm, source);
			}
		});

		IObservableList detailList = detailProperty
				.observeDetail(masterValue[0]);
		PropertyObservableUtil.cascadeDispose(detailList, masterValue[0]);
		return detailList;
	}

	public IObservableList observeDetail(final IObservableValue master) {
		final IObservableValue[] masterValue = new IObservableValue[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterValue[0] = masterProperty.observeDetail(master);
			}
		});

		IObservableList detailList = detailProperty
				.observeDetail(masterValue[0]);
		PropertyObservableUtil.cascadeDispose(detailList, masterValue[0]);
		return detailList;
	}

	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
