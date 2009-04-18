/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 169876)
 *     Elias Volanakis <elias@eclipsesource.com> - bug 271720
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.internal.databinding.observable.Util;
import org.eclipse.core.runtime.Assert;

/**
 * An {@link IObservableValue} &lt; {@link java.util.Date} &gt; which supports
 * scenarios where the date and time are presented as separate elements in the
 * user interface. This class combines the year, month, and day portion of the
 * date observable (an {@link IObservableValue} &lt; {@link java.util.Date}
 * &gt;) and the hour, minute, second, and millisecond portion of the time
 * observable (also an {@link IObservableValue} &lt; {@link java.util.Date}
 * &gt;).
 * <p>
 * This observable's value will be null whenever the date observable's value is
 * null. Otherwise the value is the combination of the date portion of the date
 * observable and the time portion of the time observable (a time observable
 * value of null is treated the same as 0:00:00.000).
 * <p>
 * When setting the value of this observable, setting a null value will set null
 * on the date observable, and set a time of 0:00:00.000 on the time observable.
 * When setting non-null values, the non-applicable fields of each observable
 * are left intact. That is, the hour, minute, second and millisecond components
 * of the date observable are preserved, and the year, month and day components
 * of the time observable are preserved.
 * <p>
 * The observables used for the date and time component may impose their own
 * restrictions with regard to supported values. For example some observables do
 * not allow a null value, because the underlying widget lacks support for a
 * null value (example: DateTime).
 * <p>
 * One use for this class is binding a date-and-time value to two separate user
 * interface elements, one for editing date and one for editing time:
 * 
 * <pre>
 * DataBindingContext dbc = new DataBindingContext();
 * IObservableValue beanValue = BeansObservables.observeValue(...);
 * IObservableValue dateObservable = WidgetProperties.selection().observe(
 * 		dateWidget);
 * IObservableValue timeObservable = WidgetProperties.selection().observe(
 * 		timeWidget);
 * dbc.bindValue(new DateAndTimeObservableValue(dateObservable, timeObservable),
 * 		beanValue);
 * </pre>
 * 
 * A second use is editing only the date or time value of a date-and-time value.
 * This can be accomplished by using a widget-specific observable for the
 * editable value and a WritableValue as a container for the fixed value. The
 * example below allows editing the date while preserving the time:
 * 
 * <pre>
 * DataBindingContext dbc = new DataBindingContext();
 * IObservableValue beanValue = BeansObservables.observeValue(...);
 * IObservableValue dateObservable = WidgetProperties.selection().observe(
 * 		dateWidget);
 * IObservableValue timeObservable = new WritableValue(dateObservable.getRealm(),
 * 		beanValue.getValue(), Date.class);
 * dbc.bindValue(new DateAndTimeObservableValue(dateObservable, timeObservable), beanValue);
 * 
 * <pre>
 * 
 * @since 1.2
 */
public class DateAndTimeObservableValue extends AbstractObservableValue {
	private IObservableValue dateObservable;
	private IObservableValue timeObservable;
	private PrivateInterface privateInterface;
	private Object cachedValue;
	private boolean updating;

	private class PrivateInterface implements IChangeListener, IStaleListener,
			IDisposeListener {
		public void handleDispose(DisposeEvent staleEvent) {
			dispose();
		}

		public void handleChange(ChangeEvent event) {
			if (!isDisposed() && !updating)
				notifyIfChanged();
		}

		public void handleStale(StaleEvent staleEvent) {
			if (!isDisposed())
				fireStale();
		}
	}

	// One calendar per thread to preserve thread-safety
	private static final ThreadLocal calendar = new ThreadLocal() {
		protected Object initialValue() {
			return Calendar.getInstance();
		}
	};

	/**
	 * Constructs a DateAndTimeObservableValue with the specified constituent
	 * observables.
	 * 
	 * @param dateObservable
	 *            the observable used for the date component (year, month and
	 *            day) of the constructed observable.
	 * @param timeObservable
	 *            the observable used for the time component (hour, minute,
	 *            second and millisecond) of the constructed observable.
	 */
	public DateAndTimeObservableValue(IObservableValue dateObservable,
			IObservableValue timeObservable) {
		super(dateObservable.getRealm());
		this.dateObservable = dateObservable;
		this.timeObservable = timeObservable;

		Assert.isTrue(dateObservable.getRealm().equals(
				timeObservable.getRealm()));

		privateInterface = new PrivateInterface();
		dateObservable.addDisposeListener(privateInterface);
		timeObservable.addDisposeListener(privateInterface);
	}

	public Object getValueType() {
		return Date.class;
	}

	protected void firstListenerAdded() {
		cachedValue = doGetValue();

		dateObservable.addChangeListener(privateInterface);
		dateObservable.addStaleListener(privateInterface);

		timeObservable.addChangeListener(privateInterface);
		timeObservable.addStaleListener(privateInterface);
	}

	protected void lastListenerRemoved() {
		if (dateObservable != null && !dateObservable.isDisposed()) {
			dateObservable.removeChangeListener(privateInterface);
			dateObservable.removeStaleListener(privateInterface);
		}

		if (timeObservable != null && !timeObservable.isDisposed()) {
			timeObservable.removeChangeListener(privateInterface);
			timeObservable.removeStaleListener(privateInterface);
		}

		cachedValue = null;
	}

	private void notifyIfChanged() {
		if (hasListeners()) {
			Object oldValue = cachedValue;
			Object newValue = cachedValue = doGetValue();
			if (!Util.equals(oldValue, newValue))
				fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}
	}

	protected Object doGetValue() {
		Date dateValue = (Date) dateObservable.getValue();
		if (dateValue == null)
			return null;

		Date timeValue = (Date) timeObservable.getValue();

		Calendar cal = (Calendar) calendar.get();

		cal.setTime(dateValue);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		if (timeValue == null)
			cal.clear();
		else
			cal.setTime(timeValue);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int millis = cal.get(Calendar.MILLISECOND);

		cal.set(year, month, day, hour, minute, second);
		cal.set(Calendar.MILLISECOND, millis);

		return cal.getTime();
	}

	protected void doSetValue(Object value) {
		Date date = (Date) value;

		Date dateValue;
		Date timeValue;

		Calendar cal = (Calendar) calendar.get();
		if (date == null)
			cal.clear();
		else
			cal.setTime(date);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int millis = cal.get(Calendar.MILLISECOND);

		if (date == null) {
			dateValue = null;
		} else {
			dateValue = (Date) dateObservable.getValue();
			if (dateValue == null)
				cal.clear();
			else
				cal.setTime(dateValue);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DAY_OF_MONTH, day);
			dateValue = cal.getTime();
		}

		timeValue = (Date) timeObservable.getValue();
		if (timeValue == null)
			cal.clear();
		else
			cal.setTime(timeValue);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millis);
		timeValue = cal.getTime();

		updating = true;
		try {
			dateObservable.setValue(dateValue);
			timeObservable.setValue(timeValue);
		} finally {
			updating = false;
		}

		notifyIfChanged();
	}

	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return dateObservable.isStale() || timeObservable.isStale();
	}

	public synchronized void dispose() {
		checkRealm();
		if (!isDisposed()) {
			if (!dateObservable.isDisposed()) {
				dateObservable.removeDisposeListener(privateInterface);
				dateObservable.removeChangeListener(privateInterface);
				dateObservable.removeStaleListener(privateInterface);
			}
			if (!timeObservable.isDisposed()) {
				timeObservable.removeDisposeListener(privateInterface);
				timeObservable.removeChangeListener(privateInterface);
				timeObservable.removeStaleListener(privateInterface);
			}
			dateObservable = null;
			timeObservable = null;
			privateInterface = null;
			cachedValue = null;
		}
		super.dispose();
	}
}
