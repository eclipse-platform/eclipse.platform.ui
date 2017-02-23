/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.databinding;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Factory methods for creating observables for Workbench objects
 *
 * @since 3.5
 */
public class WorkbenchObservables {
	/**
	 * Returns an observable with values of the given target type. If the
	 * wrapped observable's value is of the target type, or can be adapted to
	 * the target type, this is taken as the value of the returned observable,
	 * otherwise <code>null</code>.
	 *
	 * @param master
	 *            the observable whose value should be adapted
	 * @param adapter
	 *            the target type
	 * @return an observable with values of the given type, or <code>null</code>
	 *         if the current value of the given observable does not adapt to
	 *         the target type
	 */
	public static <T> IObservableValue<T> observeDetailAdaptedValue(IObservableValue<?> master, Class<T> adapter) {
		return observeDetailAdaptedValue(master, adapter, Platform
				.getAdapterManager());
	}

	/**
	 * Returns an observable with values of the given target type. If the
	 * wrapped observable's value is of the target type, or can be adapted to
	 * the target type, this is taken as the value of the returned observable,
	 * otherwise <code>null</code>.
	 *
	 * @param master
	 *            the observable whose value should be adapted
	 * @param adapter
	 *            the target type
	 * @param adapterManager
	 *            the adapter manager used to adapt the master value
	 * @return an observable with values of the given type, or <code>null</code>
	 *         if the current value of the given observable does not adapt to
	 *         the target type
	 */
	static <T> IObservableValue<T> observeDetailAdaptedValue(IObservableValue<?> master, Class<T> adapter,
			IAdapterManager adapterManager) {
		return WorkbenchProperties.adaptedValue(adapter, adapterManager)
				.observeDetail(master);
	}

	/**
	 * Returns an observable value that tracks the post selection of a selection
	 * service obtained through the given service locator, and adapts the first
	 * element of that selection to the given target type.
	 * <p>
	 * This method can be used by view or editor implementers to tie into the
	 * selection service, for example as follows:
	 *
	 * <pre>
	 * IObservableValue&lt;IResource&gt; selection = WorkbenchObservables.observeAdaptedSingleSelection(getSite(),
	 * 		IResource.class);
	 * </pre>
	 *
	 * </p>
	 *
	 * @param locator
	 *            a service locator with an available {@link ISelectionService}
	 * @param targetType
	 *            the target type
	 * @return an observable value whose value type is the given target type
	 */
	public static <T> IObservableValue<T> observeAdaptedSingleSelection(IServiceLocator locator, Class<T> targetType) {
		ISelectionService selectionService = locator.getService(ISelectionService.class);
		Assert.isNotNull(selectionService);
		return WorkbenchProperties.singleSelection(null, true).value(
				WorkbenchProperties.adaptedValue(targetType)).observe(
				selectionService);
	}

	/**
	 * Returns an observable value that tracks the active workbench window for
	 * the given workbench.
	 *
	 * @param workbench
	 *            the workbench to get the observable for
	 * @return an observable value that tracks the active workbench window
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchWindow> observeActiveWorkbenchWindow(IWorkbench workbench) {
		Assert.isNotNull(workbench);
		return new ListeningValue<IWorkbenchWindow>() {
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
				workbench.addWindowListener(listener);
			}

			@Override
			protected void stopListening() {
				workbench.removeWindowListener(listener);
			}

			@Override
			protected IWorkbenchWindow calculate() {
				return workbench.getActiveWorkbenchWindow();
			}
		};
	}

	/**
	 * Returns an observable value that tracks the active workbench page for the
	 * given workbench window.
	 *
	 * @param window
	 *            the workbench window to get the observable for
	 * @return an observable value that tracks the active workbench page
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchPage> observeActiveWorkbenchPage(IWorkbenchWindow window) {
		Assert.isNotNull(window);
		return new ListeningValue<IWorkbenchPage>() {
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
				window.addPageListener(listener);
			}

			@Override
			protected void stopListening() {
				window.removePageListener(listener);
			}

			@Override
			protected IWorkbenchPage calculate() {
				return window.getActivePage();
			}
		};
	}

	/**
	 * Returns an observable value that tracks the active workbench part for the
	 * given part service.
	 *
	 * @param partService
	 *            the part service to get the observable for, e.g. a workbench
	 *            page
	 * @return an observable value that tracks the active workbench part
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchPartReference> observeActivePart(IPartService partService) {
		Assert.isNotNull(partService);
		return new ListeningValue<IWorkbenchPartReference>() {
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

				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partClosed(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partOpened(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partHidden(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partVisible(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {
				}
			};

			@Override
			protected void startListening() {
				partService.addPartListener(listener);
			}

			@Override
			protected void stopListening() {
				partService.removePartListener(listener);
			}

			@Override
			protected IWorkbenchPartReference calculate() {
				return partService.getActivePartReference();
			}
		};
	}

	/**
	 * Returns an observable value that tracks the active editor for the given
	 * part service.
	 *
	 * @param partService
	 *            the part service to get the observable for, e.g. a workbench
	 *            page
	 * @return an observable value that tracks the active editor
	 * @since 3.110
	 */
	public static IObservableValue<IEditorReference> observeActiveEditor(IPartService partService) {
		final IObservableValue<IWorkbenchPartReference> partObservable = observeActivePart(partService);
		return ComputedValue.create(() -> {
			IWorkbenchPartReference value = partObservable.getValue();
			return value instanceof IEditorReference ? (IEditorReference) value : null;
		});
	}

	/**
	 * Returns an observable value that tracks the editor input for the given
	 * editor.
	 *
	 * @param editor
	 *            the editor to get the observable for
	 * @return an observable value that tracks the editor input
	 * @since 3.110
	 */
	public static IObservableValue<IEditorInput> observeEditorInput(IEditorPart editor) {
		Assert.isNotNull(editor);
		return new ListeningValue<IEditorInput>() {
			private final IPropertyListener listener = new IPropertyListener() {
				@Override
				public void propertyChanged(Object source, int propId) {
					if (propId == IWorkbenchPartConstants.PROP_INPUT) {
						protectedSetValue(editor.getEditorInput());
					}
				}
			};

			@Override
			protected void startListening() {
				editor.addPropertyListener(listener);
			}

			@Override
			protected void stopListening() {
				editor.removePropertyListener(listener);
			}

			@Override
			protected IEditorInput calculate() {
				return editor.getEditorInput();
			}
		};
	}

	/**
	 * A base class for creating observable values that track the state of a
	 * non-{@link IObservable} objects.
	 */
	private abstract static class ListeningValue<T> extends AbstractObservableValue<T> {
		private T value;
		private boolean isListening;
		private volatile boolean hasListeners;

		@Override
		protected final T doGetValue() {
			// The value is not kept up to date when we are not listening.
			if (isListening) {
				return value;
			}
			return calculate();
		}

		/**
		 * Sets the value. Must be invoked in the {@link Realm} of the
		 * observable. Subclasses must call this method instead of
		 * {@link #setValue} or {@link #doSetValue}.
		 *
		 * @param value
		 *            the value to set
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
		 * Subclasses must override this method to provide the object's value
		 * that will be used when the value is not set explicitly by
		 * {@link #doSetValue(Object)}.
		 *
		 * @return the object's value
		 */
		protected abstract T calculate();

		@Override
		public Object getValueType() {
			return null;
		}
	}
}
