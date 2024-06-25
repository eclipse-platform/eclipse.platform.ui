package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;

public class SingleSelectionProperty<S extends ISelectionService, T> extends SimpleValueProperty<S, T> {
	private final String partId;
	private final boolean post;
	private final Object elementType;

	public SingleSelectionProperty(String partId, boolean post, Object elementType) {
		this.partId = partId;
		this.post = post;
		this.elementType = elementType;
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		return new SelectionServiceListener<>(this, listener, partId, post);
	}

	@Override
	protected T doGetValue(S source) {
		ISelection selection;
		if (partId != null) {
			selection = source.getSelection(partId);
		} else {
			selection = source.getSelection();
		}
		if (selection instanceof IStructuredSelection) {
			T elem = (T) ((IStructuredSelection) selection).getFirstElement();
			return elem;
		}
		return null;
	}

	@Override
	protected void doSetValue(S source, T value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getValueType() {
		return elementType;
	}
}
