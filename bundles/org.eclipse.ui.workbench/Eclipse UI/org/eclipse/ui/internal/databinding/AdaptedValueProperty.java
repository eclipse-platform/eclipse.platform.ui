package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.runtime.IAdapterManager;

public final class AdaptedValueProperty<S, T> extends SimpleValueProperty<S, T> {
	private final Class<T> adapter;
	private final IAdapterManager adapterManager;

	public AdaptedValueProperty(Class<T> adapter, IAdapterManager adapterManager) {
		this.adapter = adapter;
		this.adapterManager = adapterManager;
	}

	@Override
	public Object getValueType() {
		return adapter;
	}

	@Override
	protected T doGetValue(S source) {
		if (adapter.isInstance(source))
			return (T) source;
		return adapterManager.getAdapter(source, adapter);
	}

	@Override
	protected void doSetValue(S source, T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		return null;
	}
}
