package org.eclipse.core.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class can be used to aggregate status values from a data binding context
 * into a single status value. Instances of this class can be used as an
 * observable value with a value type of {@link IStatus}, or the static methods
 * can be called directly if an aggregated status result is only needed once.
 * 
 * @since 1.0
 * 
 */
public final class AggregateValidationStatus implements IObservableValue {

	private IObservableValue implementation;

	/**
	 * Constant denoting an aggregation strategy that merges multiple non-OK
	 * status objects in a {@link MultiStatus}. Returns an OK status result if
	 * all statuses from the given bindings are the an OK status. Returns a
	 * single status if there is only one non-OK status.
	 * 
	 * @see #getStatusMerged(Collection)
	 */
	public static final int MERGED = 1;

	/**
	 * Constant denoting an aggregation strategy that always returns the most
	 * severe status from the given bindings. If there is more than one status
	 * at the same severity level, it picks the first one it encounters.
	 * 
	 * @see #getStatusMaxSeverity(Collection)
	 */
	public static final int MAX_SEVERITY = 2;

	/**
	 * @param bindings
	 *            an observable collection containing elements of type IStatus
	 * @param strategy
	 *            a strategy constant, one of {@link #MERGED} or
	 *            {@link #MAX_SEVERITY}.
	 */
	public AggregateValidationStatus(final IObservableCollection bindings,
			int strategy) {
		if (strategy == MERGED) {
			implementation = new ComputedValue() {
				protected Object calculate() {
					return getStatusMerged(bindings);
				}
			};
		} else {
			implementation = new ComputedValue() {
				protected Object calculate() {
					return getStatusMaxSeverity(bindings);
				}
			};
		}
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.IObservable#addChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
	 */
	public void addChangeListener(IChangeListener listener) {
		implementation.addChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.IObservable#addStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
	 */
	public void addStaleListener(IStaleListener listener) {
		implementation.addStaleListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#addValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
	 */
	public void addValueChangeListener(IValueChangeListener listener) {
		implementation.addValueChangeListener(listener);
	}

	/**
	 * 
	 * @see org.eclipse.core.databinding.observable.IObservable#dispose()
	 */
	public void dispose() {
		implementation.dispose();
	}

	/**
	 * @return
	 * @see org.eclipse.core.databinding.observable.IObservable#getRealm()
	 */
	public Realm getRealm() {
		return implementation.getRealm();
	}

	/**
	 * @return
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValue()
	 */
	public Object getValue() {
		return implementation.getValue();
	}

	/**
	 * @return
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
	 */
	public Object getValueType() {
		return implementation.getValueType();
	}

	/**
	 * @return
	 * @see org.eclipse.core.databinding.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return implementation.isStale();
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.IObservable#removeChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
	 */
	public void removeChangeListener(IChangeListener listener) {
		implementation.removeChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.IObservable#removeStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
	 */
	public void removeStaleListener(IStaleListener listener) {
		implementation.removeStaleListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#removeValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
	 */
	public void removeValueChangeListener(IValueChangeListener listener) {
		implementation.removeValueChangeListener(listener);
	}

	/**
	 * @param value
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		implementation.setValue(value);
	}

	/**
	 * @param bindings
	 * @return
	 */
	public static IStatus getStatusMerged(Collection bindings) {
		List statuses = new ArrayList();
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			IStatus status = (IStatus) binding
					.getValidationStatus().getValue();
			if (!status.isOK()) {
				statuses.add(status);
			}
		}
		if (statuses.size() == 1) {
			return (IStatus) statuses.get(0);
		}
		if (!statuses.isEmpty()) {
			MultiStatus result = new MultiStatus(
					Policy.JFACE_DATABINDING,
					0,
					BindingMessages
							.getString(BindingMessages.MULTIPLE_PROBLEMS),
					null);
			for (Iterator it = statuses.iterator(); it.hasNext();) {
				IStatus status = (IStatus) it.next();
				result.merge(status);
			}
			return result;
		}
		return Status.OK_STATUS;
	}

	/**
	 * @param bindings
	 * @return
	 */
	public static IStatus getStatusMaxSeverity(Collection bindings) {
		int maxSeverity = IStatus.OK;
		IStatus maxStatus = Status.OK_STATUS;
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			IStatus status = (IStatus) binding
					.getValidationStatus().getValue();
			if (status.getSeverity() > maxSeverity) {
				maxSeverity = status.getSeverity();
				maxStatus = status;
			}
		}
		return maxStatus;
	}

}