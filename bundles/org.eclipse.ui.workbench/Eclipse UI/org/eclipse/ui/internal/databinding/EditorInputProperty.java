package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;

/**
 * A property for observing the {@link IEditorInput} of and {@link IEditorPart}.
 *
 * @param <S> the source type
 */
public class EditorInputProperty<S extends IEditorPart> extends ValueProperty<S, IEditorInput> {
	@Override
	public Object getValueType() {
		return IEditorInput.class;
	}

	@Override
	protected IEditorInput doGetValue(S source) {
		return source.getEditorInput();
	}

	@Override
	protected void doSetValue(S source, IEditorInput value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObservableValue<IEditorInput> observe(Realm realm, S source) {
		return new ListeningValue<IEditorInput>(realm) {
			private final IPropertyListener listener = (Object s, int propId) -> {
				if (propId == IWorkbenchPartConstants.PROP_INPUT) {
					protectedSetValue(source.getEditorInput());
				}
			};

			@Override
			protected void startListening() {
				source.addPropertyListener(listener);
			}

			@Override
			protected void stopListening() {
				source.removePropertyListener(listener);
			}

			@Override
			protected IEditorInput calculate() {
				return EditorInputProperty.this.getValue(source);
			}

			@Override
			public Object getValueType() {
				return EditorInputProperty.this.getValueType();
			}
		};
	}
}
