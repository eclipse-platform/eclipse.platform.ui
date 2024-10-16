package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A property for observing the active {@link IWorkbenchWindow} of a
 * {@link IWorkbench}.
 *
 * @param <S> the source type
 */
public class ActiveWindowProperty<S extends IWorkbench> extends ValueProperty<S, IWorkbenchWindow> {
	@Override
	public Object getValueType() {
		return IWorkbenchWindow.class;
	}

	@Override
	protected IWorkbenchWindow doGetValue(S source) {
		return source.getActiveWorkbenchWindow();
	}

	@Override
	protected void doSetValue(S source, IWorkbenchWindow value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObservableValue<IWorkbenchWindow> observe(Realm realm, S source) {
		return new ListeningValue<>(realm) {
			private final IWindowListener listener = new IWindowListener() {
				@Override
				public void windowActivated(IWorkbenchWindow window) {
					protectedSetValue(window);
				}

				@Override
				public void windowDeactivated(IWorkbenchWindow window) {
					if (window == doGetValue()) {
						protectedSetValue(null);
					}
				}

				@Override
				public void windowClosed(IWorkbenchWindow window) {
				}

				@Override
				public void windowOpened(IWorkbenchWindow window) {
				}
			};

			@Override
			protected void startListening() {
				source.addWindowListener(listener);
			}

			@Override
			protected void stopListening() {
				source.removeWindowListener(listener);
			}

			@Override
			protected IWorkbenchWindow calculate() {
				return ActiveWindowProperty.this.getValue(source);
			}

			@Override
			public Object getValueType() {
				return ActiveWindowProperty.this.getValueType();
			}
		};
	}
}
