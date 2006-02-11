package org.eclipse.jface.internal.databinding.api.observable.value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @since 3.2
 * 
 */
public abstract class AbstractVetoableValue extends AbstractObservableValue implements IVetoableValue {

	public void setValue(Object value) {
		Object currentValue = doGetValue();
		ValueDiff diff = new ValueDiff(currentValue, value);
		boolean okToProceed = fireValueChanging(diff);
		if (!okToProceed) {
			throw new ChangeVetoException("Change not permitted"); //$NON-NLS-1$
		}
		doSetValue(value);
		fireValueChange(diff);
	}
	
	private Collection valueChangingListeners = null;

	public void addValueChangingListener(IValueChangingListener listener) {
		if (valueChangingListeners == null) {
			boolean hadListeners = hasListeners();
			valueChangingListeners = new ArrayList();
			valueChangingListeners.add(listener);
			if (!hadListeners) {
				firstListenerAdded();
			}
		} else if (valueChangingListeners.size() > 16) {
			HashSet listenerList = new HashSet();
			listenerList.addAll(valueChangingListeners);
			valueChangingListeners = listenerList;
			valueChangingListeners.add(listener);
		}
	}

	public void removeValueChangingListener(IValueChangingListener listener) {
		valueChangingListeners.remove(listener);
		if (valueChangingListeners.size() == 0) {
			valueChangingListeners = null;
		}
		if (!hasListeners()) {
			lastListenerRemoved();
		}
	}


	/**
	 * Notifies listeners about a pending change, and returns true if no
	 * listener vetoed the change.
	 * 
	 * @param diff
	 * @return false if the change was vetoed, true otherwise
	 */
	protected boolean fireValueChanging(ValueDiff diff) {
		if (valueChangingListeners != null) {
			IValueChangingListener[] listeners = (IValueChangingListener[]) valueChangingListeners
					.toArray(new IValueChangingListener[valueChangingListeners.size()]);
			for (int i = 0; i < listeners.length; i++) {
				boolean okToProceed = listeners[i].handleValueChanging(this, diff);
				if (!okToProceed) {
					return false;
				}
			}
		}
		return true;
	}

	protected abstract void doSetValue(Object value);

	protected boolean hasListeners() {
		return super.hasListeners();
	}

	public void dispose() {
		super.dispose();
	}

}
