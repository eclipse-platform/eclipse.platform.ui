/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164653, 147515
 *     Ovidio Mallo - bug 241318
 *     Matthew Hall - bugs 247875, 246782, 249526, 268022, 251424
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;

/**
 * @param <M>
 *            type of the master observable
 * @param <T>
 *            type of inner observable value
 * @since 1.0
 *
 */
public class DetailObservableValue<M, T> extends AbstractObservableValue<T>
		implements IObserving {

	private boolean updating = false;

	private IValueChangeListener<T> innerChangeListener = new IValueChangeListener<T>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends T> event) {
			if (!updating) {
				fireValueChange(Diffs.unmodifiableDiff(event.diff));
			}
		}
	};

	private M currentOuterValue;

	private IObservableValue<T> innerObservableValue;

	private Object detailType;

	private IObservableValue<M> outerObservableValue;

	private IObservableFactory<? super M, IObservableValue<T>> factory;

	/**
	 * @param outerObservableValue
	 * @param factory
	 * @param detailType
	 */
	public DetailObservableValue(IObservableValue<M> outerObservableValue,
			IObservableFactory<? super M, IObservableValue<T>> factory,
			Object detailType) {
		super(outerObservableValue.getRealm());
		Assert.isTrue(!outerObservableValue.isDisposed(),
				"Master observable is disposed"); //$NON-NLS-1$

		this.factory = factory;
		this.detailType = detailType;
		this.outerObservableValue = outerObservableValue;

		outerObservableValue.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent staleEvent) {
				dispose();
			}
		});

		ObservableTracker.setIgnore(true);
		try {
			updateInnerObservableValue();
		} finally {
			ObservableTracker.setIgnore(false);
		}
		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener<M> outerChangeListener = new IValueChangeListener<M>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends M> event) {
			if (isDisposed())
				return;
			ObservableTracker.setIgnore(true);
			try {
				T oldValue = doGetValue();
				updateInnerObservableValue();
				fireValueChange(Diffs.createValueDiff(oldValue, doGetValue()));
			} finally {
				ObservableTracker.setIgnore(false);
			}
		}
	};

	private void updateInnerObservableValue() {
		currentOuterValue = outerObservableValue.getValue();
		if (innerObservableValue != null) {
			innerObservableValue.removeValueChangeListener(innerChangeListener);
			innerObservableValue.dispose();
		}
		if (currentOuterValue == null) {
			innerObservableValue = null;
		} else {
			ObservableTracker.setIgnore(true);
			try {
				innerObservableValue = factory
						.createObservable(currentOuterValue);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			DetailObservableHelper.warnIfDifferentRealms(getRealm(),
					innerObservableValue.getRealm());

			if (detailType != null) {
				Object innerValueType = innerObservableValue.getValueType();
				Assert.isTrue(
						detailType.equals(innerValueType),
						"Cannot change value type in a nested observable value, from " + innerValueType + " to " + detailType); //$NON-NLS-1$ //$NON-NLS-2$
			}
			innerObservableValue.addValueChangeListener(innerChangeListener);
		}
	}

	@Override
	public void doSetValue(final T value) {
		if (innerObservableValue != null) {
			ObservableTracker.setIgnore(true);
			try {
				innerObservableValue.setValue(value);
			} finally {
				ObservableTracker.setIgnore(false);
			}
		}
	}

	@Override
	public T doGetValue() {
		if (innerObservableValue == null)
			return null;
		ObservableTracker.setIgnore(true);
		try {
			return innerObservableValue.getValue();
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public Object getValueType() {
		return detailType;
	}

	@Override
	public synchronized void dispose() {
		super.dispose();

		if (outerObservableValue != null) {
			outerObservableValue.removeValueChangeListener(outerChangeListener);
		}
		if (innerObservableValue != null) {
			innerObservableValue.removeValueChangeListener(innerChangeListener);
			innerObservableValue.dispose();
		}
		outerObservableValue = null;
		outerChangeListener = null;
		currentOuterValue = null;
		factory = null;
		innerObservableValue = null;
		innerChangeListener = null;
	}

	@Override
	public Object getObserved() {
		if (innerObservableValue instanceof IObserving) {
			return ((IObserving) innerObservableValue).getObserved();
		}
		return null;
	}

}
