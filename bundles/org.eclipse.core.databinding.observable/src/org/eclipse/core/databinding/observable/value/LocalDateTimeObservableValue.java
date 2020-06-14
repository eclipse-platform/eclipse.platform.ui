/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.runtime.Assert;

/**
 * An {@link IObservableValue}&lt;{@link LocalDateTime}&gt; which supports
 * scenarios where the date and time are presented as separate elements in the
 * user interface. This class combines the year, month, and day portion of the
 * date observable (an {@link IObservableValue}&lt;{@link LocalDate}&gt;) and
 * the hour, minute, second, and millisecond portion of the time observable
 * (also an {@link IObservableValue}&lt;{@link LocalTime}&gt;).
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
 * {@code
 * DataBindingContext bindingContext = new DataBindingContext();
 * IObservableValue&lt;LocalDateTime&gt;  beanValue = BeansObservables.observeValue(...);
 * IObservableValue&lt;LocalDate&gt; dateObservable = WidgetProperties.localDateSelection().observe(dateWidget);
 * IObservableValue&lt;LocalTime&gt; timeObservable = WidgetProperties.localTimeSelection().observe(timeWidget);
 * bindingContext.bindValue(new LocalDateTimeObservableValue(dateObservable, timeObservable), beanValue);
 * }
 * </pre>
 *
 * A second use is editing only the date or time value of a date-and-time value.
 * This can be accomplished by using a widget-specific observable for the
 * editable value and a {@link WritableValue} as a container for the fixed
 * value. The example below allows editing the date while preserving the time:
 *
 * <pre>
 * {@code
 * DataBindingContext bindingContext = new DataBindingContext();
 * IObservableValue<LocalDateTime> beanValue = BeansObservables.observeValue(...);
 * IObservableValue<LocalDate> dateObservable = WidgetProperties.localDateSelection().observe(dateWidget);
 * IObservableValue<LocalTime> timeObservable = new WritableValue<>(dateObservable.getRealm(),
 * 		beanValue.getValue(), LocalTime.class);
 * bindingContext.bindValue(new LocalDateTimeObservableValue(dateObservable, timeObservable), beanValue);
 * }
 * </pre>
 *
 * @since 1.10
 */
public final class LocalDateTimeObservableValue extends AbstractObservableValue<LocalDateTime> {
	private IObservableValue<LocalDate> dateObservable;
	private IObservableValue<LocalTime> timeObservable;
	private PrivateInterface privateInterface;
	private LocalDateTime cachedValue;
	private boolean updating;

	private class PrivateInterface implements IChangeListener, IStaleListener, IDisposeListener {
		@Override
		public void handleDispose(DisposeEvent staleEvent) {
			dispose();
		}

		@Override
		public void handleChange(ChangeEvent event) {
			if (!isDisposed() && !updating) {
				notifyIfChanged();
			}
		}

		@Override
		public void handleStale(StaleEvent staleEvent) {
			if (!isDisposed()) {
				fireStale();
			}
		}
	}

	/**
	 * Constructs a DateAndTimeObservableValue with the specified constituent
	 * observables.
	 *
	 * @param dateObservable the observable used for the date component (year, month
	 *                       and day) of the constructed observable.
	 * @param timeObservable the observable used for the time component (hour,
	 *                       minute, second and millisecond) of the constructed
	 *                       observable.
	 */
	public LocalDateTimeObservableValue(IObservableValue<LocalDate> dateObservable,
			IObservableValue<LocalTime> timeObservable) {
		super(dateObservable.getRealm());
		this.dateObservable = dateObservable;
		this.timeObservable = timeObservable;

		Assert.isTrue(dateObservable.getRealm().equals(timeObservable.getRealm()));

		privateInterface = new PrivateInterface();

		dateObservable.addDisposeListener(privateInterface);
	}

	@Override
	public Object getValueType() {
		return LocalDateTime.class;
	}

	@Override
	protected void firstListenerAdded() {
		cachedValue = doGetValue();

		dateObservable.addChangeListener(privateInterface);
		dateObservable.addStaleListener(privateInterface);

		timeObservable.addChangeListener(privateInterface);
		timeObservable.addStaleListener(privateInterface);
	}

	@Override
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
			LocalDateTime oldValue = cachedValue;
			LocalDateTime newValue = cachedValue = doGetValue();
			if (!Objects.equals(oldValue, newValue)) {
				fireValueChange(Diffs.createValueDiff(oldValue, newValue));
			}
		}
	}

	@Override
	protected LocalDateTime doGetValue() {
		LocalDate dateValue = dateObservable.getValue();
		if (dateValue == null) {
			return null;
		}

		LocalTime timeValue = timeObservable.getValue();

		if (timeValue == null) {
			timeValue = LocalTime.MIDNIGHT;
		}

		return LocalDateTime.of(dateValue, timeValue);
	}

	@Override
	protected void doSetValue(LocalDateTime combined) {
		updating = true;
		try {
			if (combined == null) {
				dateObservable.setValue(null);
				timeObservable.setValue(LocalTime.MIDNIGHT);
			} else {
				dateObservable.setValue(combined.toLocalDate());
				timeObservable.setValue(combined.toLocalTime());
			}
		} finally {
			updating = false;
		}

		notifyIfChanged();
	}

	@Override
	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return dateObservable.isStale() || timeObservable.isStale();
	}

	@Override
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
