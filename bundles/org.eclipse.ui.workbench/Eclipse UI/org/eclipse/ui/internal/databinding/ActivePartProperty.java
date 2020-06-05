package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * A property for observing the active {@link IWorkbenchPartReference} of a
 * {@link IPartService}.
 *
 * @param <S> the source type
 */
public class ActivePartProperty<S extends IPartService> extends ValueProperty<S, IWorkbenchPartReference> {
	@Override
	public Object getValueType() {
		return IWorkbenchPartReference.class;
	}

	@Override
	protected IWorkbenchPartReference doGetValue(S source) {
		return source.getActivePartReference();
	}

	@Override
	protected void doSetValue(S source, IWorkbenchPartReference value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObservableValue<IWorkbenchPartReference> observe(Realm realm, S source) {
		return new ListeningValue<IWorkbenchPartReference>(realm) {
			private final IPartListener2 listener = new IPartListener2() {
				@Override
				public void partActivated(IWorkbenchPartReference partRef) {
					protectedSetValue(partRef);
				}

				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {
					if (partRef == doGetValue()) {
						protectedSetValue(null);
					}
				}
			};

			@Override
			protected void startListening() {
				source.addPartListener(listener);
			}

			@Override
			protected void stopListening() {
				source.removePartListener(listener);
			}

			@Override
			protected IWorkbenchPartReference calculate() {
				return ActivePartProperty.this.getValue(source);
			}

			@Override
			public Object getValueType() {
				return ActivePartProperty.this.getValueType();
			}
		};
	}
}
