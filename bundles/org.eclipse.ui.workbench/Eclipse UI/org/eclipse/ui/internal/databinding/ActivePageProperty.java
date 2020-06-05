package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.ValueProperty;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A property for observing the active {@link IWorkbenchPage} of a
 * {@link IPageService}.
 *
 * @param <S> the source type
 */
public class ActivePageProperty<S extends IPageService> extends ValueProperty<S, IWorkbenchPage> {
	@Override
	public Object getValueType() {
		return IWorkbenchPage.class;
	}

	@Override
	protected IWorkbenchPage doGetValue(S source) {
		return source.getActivePage();
	}

	@Override
	protected void doSetValue(S source, IWorkbenchPage value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IObservableValue<IWorkbenchPage> observe(Realm realm, S source) {
		return new ListeningValue<IWorkbenchPage>(realm) {
			private final IPageListener listener = new IPageListener() {
				@Override
				public void pageActivated(IWorkbenchPage page) {
					protectedSetValue(page);
				}

				@Override
				public void pageClosed(IWorkbenchPage page) {
					if (page == doGetValue()) {
						protectedSetValue(null);
					}
				}

				@Override
				public void pageOpened(IWorkbenchPage page) {
				}
			};

			@Override
			protected void startListening() {
				source.addPageListener(listener);
			}

			@Override
			protected void stopListening() {
				source.removePageListener(listener);
			}

			@Override
			protected IWorkbenchPage calculate() {
				return ActivePageProperty.this.getValue(source);
			}

			@Override
			public Object getValueType() {
				return ActivePageProperty.this.getValueType();
			}
		};
	}
}
