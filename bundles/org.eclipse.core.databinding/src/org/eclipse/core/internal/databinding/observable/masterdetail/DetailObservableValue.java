/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Brad Reynolds - bug 147515
 *     Ovidio Mallo - bug 241318
 *     Matthew Hall - bug 247875, 246782, 249526
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;

/**
 * @since 1.0
 * 
 */
public class DetailObservableValue extends AbstractObservableValue implements IObserving {

	private boolean updating = false;

	private IValueChangeListener innerChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			if (!updating) {
				fireValueChange(event.diff);
			}
		}
	};

	private Object currentOuterValue;

	private IObservableValue innerObservableValue;

	private Object detailType;

	private IObservableValue outerObservableValue;

	private IObservableFactory factory;

	/**
	 * @param outerObservableValue
	 * @param factory
	 * @param detailType
	 */
	public DetailObservableValue(IObservableValue outerObservableValue,
			IObservableFactory factory, Object detailType) {
		super(outerObservableValue.getRealm());
		this.factory = factory;
		this.detailType = detailType;
		this.outerObservableValue = outerObservableValue;
		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				updateInnerObservableValue();
			}
		});
		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			ObservableTracker.runAndIgnore(new Runnable() {
				public void run() {
					Object oldValue = doGetValue();
					updateInnerObservableValue();
					fireValueChange(Diffs.createValueDiff(oldValue,
							doGetValue()));
				}
			});
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
			ObservableTracker.runAndIgnore(new Runnable() {
				public void run() {
					innerObservableValue = (IObservableValue) factory
							.createObservable(currentOuterValue);
				}
			});
			DetailObservableHelper.warnIfDifferentRealms(getRealm(),
					innerObservableValue.getRealm());

			if (detailType != null) {
				Object innerValueType = innerObservableValue.getValueType();
				Assert
						.isTrue(
								detailType.equals(innerValueType),
								"Cannot change value type in a nested observable value, from " + innerValueType + " to " + detailType); //$NON-NLS-1$ //$NON-NLS-2$
			}
			innerObservableValue.addValueChangeListener(innerChangeListener);
		}
	}

	public void doSetValue(final Object value) {
		if (innerObservableValue != null) {
			ObservableTracker.runAndIgnore(new Runnable() {
				public void run() {
					innerObservableValue.setValue(value);
				}
			});
		}
	}

	public Object doGetValue() {
		if (innerObservableValue == null)
			return null;
		final Object[] result = new Object[1];
		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				result[0] = innerObservableValue.getValue();
			}
		});
		return result[0];
	}

	public Object getValueType() {
		return detailType;
	}

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

	public Object getObserved() {
		if (innerObservableValue instanceof IObserving) {
			return ((IObserving)innerObservableValue).getObserved();
		}
		return null;
	}

}
