/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 278550
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

import java.util.List;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.ListProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <M>
 *            type of the elements in the master list
 * @param <T>
 *            type of the elements in the list, being the type of the value of
 *            the detail property
 * @since 3.3
 *
 */
public class ValuePropertyDetailList<S, M, T> extends ListProperty<S, T> {
	private final IValueProperty<S, M> masterProperty;
	private final IListProperty<? super M, T> detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public ValuePropertyDetailList(IValueProperty<S, M> masterProperty, IListProperty<? super M, T> detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	@Override
	public Object getElementType() {
		return detailProperty.getElementType();
	}

	@Override
	protected List<T> doGetList(S source) {
		M masterValue = masterProperty.getValue(source);
		return detailProperty.getList(masterValue);
	}

	@Override
	protected void doSetList(S source, List<T> list) {
		M masterValue = masterProperty.getValue(source);
		detailProperty.setList(masterValue, list);
	}

	@Override
	protected void doUpdateList(S source, ListDiff<T> diff) {
		M masterValue = masterProperty.getValue(source);
		detailProperty.updateList(masterValue, diff);
	}

	@Override
	public IObservableList<T> observe(Realm realm, S source) {
		IObservableValue<M> masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableList<T> detailList = detailProperty.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailList, masterValue);
		return detailList;
	}

	@Override
	public <U extends S> IObservableList<T> observeDetail(IObservableValue<U> master) {
		IObservableValue<M> masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableList<T> detailList = detailProperty.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailList, masterValue);
		return detailList;
	}

	@Override
	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
