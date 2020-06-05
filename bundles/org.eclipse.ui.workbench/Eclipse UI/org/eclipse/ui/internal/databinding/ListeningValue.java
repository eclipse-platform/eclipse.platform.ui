package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

/**
 * A base class for creating observable values that track the state of a
 * non-{@link IObservable} objects.
 *
 * @param <T> the value type
 */
abstract class ListeningValue<T> extends AbstractObservableValue<T> {
	private T value;
	private boolean isListening;
	private volatile boolean hasListeners;

	public ListeningValue(Realm realm) {
		super(realm);
	}

	@Override
	protected final T doGetValue() {
		// The value is not kept up to date when we are not listening.
		if (isListening) {
			return value;
		}
		return calculate();
	}

	/**
	 * Sets the value. Must be invoked in the {@link Realm} of the observable.
	 * Subclasses must call this method instead of {@link #setValue} or
	 * {@link #doSetValue}.
	 *
	 * @param value the value to set
	 */
	protected final void protectedSetValue(T value) {
		checkRealm();
		if (!isListening)
			throw new IllegalStateException();
		if (this.value != value) {
			fireValueChange(Diffs.createValueDiff(this.value, this.value = value));
		}
	}

	@Override
	protected final void firstListenerAdded() {
		if (getRealm().isCurrent()) {
			startListeningInternal();
		} else {
			getRealm().asyncExec(() -> {
				if (hasListeners && !isListening) {
					startListeningInternal();
				}
			});
		}
		hasListeners = true;
		super.firstListenerAdded();
	}

	@Override
	protected final void lastListenerRemoved() {
		if (getRealm().isCurrent()) {
			stopListeningInternal();
		} else {
			getRealm().asyncExec(() -> {
				if (!hasListeners && isListening) {
					stopListeningInternal();
				}
			});
		}
		hasListeners = false;
		super.lastListenerRemoved();
	}

	private void startListeningInternal() {
		isListening = true;
		value = calculate();
		startListening();
	}

	private void stopListeningInternal() {
		isListening = false;
		value = null;
		stopListening();
	}

	/**
	 * Subclasses must override this method to attach listeners to the
	 * non-{@link IObservable} objects the state of which is tracked by this
	 * observable.
	 */
	protected abstract void startListening();

	/**
	 * Subclasses must override this method to detach listeners from the
	 * non-{@link IObservable} objects the state of which is tracked by this
	 * observable.
	 */
	protected abstract void stopListening();

	/**
	 * Subclasses must override this method to provide the object's value that will
	 * be used when the value is not set explicitly by
	 * {@link AbstractObservableValue#doSetValue(Object)}.
	 *
	 * @return the object's value
	 */
	protected abstract T calculate();
}
