/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.databinding;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.databinding.typed.WorkbenchProperties;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Factory methods for creating observables for Workbench objects
 *
 * @since 3.5
 * @noreference
 * @deprecated This class will be removed in a future release. See
 *             https://bugs.eclipse.org/bugs/show_bug.cgi?id=546820 for more
 *             information. Use <code>WorkbenchProperties</code> instead.
 */
@Deprecated
public class WorkbenchObservables {
	/**
	 * Returns an observable with values of the given target type. If the wrapped
	 * observable's value is of the target type, or can be adapted to the target
	 * type, this is taken as the value of the returned observable, otherwise
	 * <code>null</code>.
	 *
	 * @param master  the observable whose value should be adapted
	 * @param adapter the target type
	 * @return an observable with values of the given type, or <code>null</code> if
	 *         the current value of the given observable does not adapt to the
	 *         target type
	 */
	public static <T> IObservableValue<T> observeDetailAdaptedValue(IObservableValue<?> master, Class<T> adapter) {
		return observeDetailAdaptedValue(master, adapter, Platform.getAdapterManager());
	}

	/**
	 * Returns an observable with values of the given target type. If the wrapped
	 * observable's value is of the target type, or can be adapted to the target
	 * type, this is taken as the value of the returned observable, otherwise
	 * <code>null</code>.
	 *
	 * @param master         the observable whose value should be adapted
	 * @param adapter        the target type
	 * @param adapterManager the adapter manager used to adapt the master value
	 * @return an observable with values of the given type, or <code>null</code> if
	 *         the current value of the given observable does not adapt to the
	 *         target type
	 */
	static <T> IObservableValue<T> observeDetailAdaptedValue(IObservableValue<?> master, Class<T> adapter,
			IAdapterManager adapterManager) {
		return WorkbenchProperties.adaptedValue(adapter, adapterManager).observeDetail(master);
	}

	/**
	 * Returns an observable value that tracks the post selection of a selection
	 * service obtained through the given service locator, and adapts the first
	 * element of that selection to the given target type.
	 * <p>
	 * This method can be used by view or editor implementers to tie into the
	 * selection service, for example as follows:
	 * </p>
	 *
	 * <pre>
	 * IObservableValue&lt;IResource&gt; selection = WorkbenchObservables.observeAdaptedSingleSelection(getSite(),
	 * 		IResource.class);
	 * </pre>
	 *
	 *
	 * @param locator    a service locator with an available
	 *                   {@link ISelectionService}
	 * @param targetType the target type
	 * @return an observable value whose value type is the given target type
	 */
	public static <T> IObservableValue<T> observeAdaptedSingleSelection(IServiceLocator locator, Class<T> targetType) {
		ISelectionService selectionService = locator.getService(ISelectionService.class);
		Assert.isNotNull(selectionService);
		return WorkbenchProperties.singleSelection(null, true).value(WorkbenchProperties.adaptedValue(targetType))
				.observe(selectionService);
	}

	/**
	 * Returns an observable value that tracks the active workbench window for the
	 * given workbench.
	 *
	 * @param workbench the workbench to get the observable for
	 * @return an observable value that tracks the active workbench window
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchWindow> observeActiveWorkbenchWindow(IWorkbench workbench) {
		Assert.isNotNull(workbench);
		return WorkbenchProperties.activeWindow().observe(workbench);
	}

	/**
	 * Returns an observable value that tracks the active workbench page for the
	 * given workbench window.
	 *
	 * @param window the workbench window to get the observable for
	 * @return an observable value that tracks the active workbench page
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchPage> observeActiveWorkbenchPage(IWorkbenchWindow window) {
		Assert.isNotNull(window);
		return WorkbenchProperties.activePage().observe(window);
	}

	/**
	 * Returns an observable value that tracks the active workbench part for the
	 * given part service.
	 *
	 * @param partService the part service to get the observable for, e.g. a
	 *                    workbench page
	 * @return an observable value that tracks the active workbench part
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchPartReference> observeActivePart(IPartService partService) {
		Assert.isNotNull(partService);
		return WorkbenchProperties.activePartReference().observe(partService);
	}

	/**
	 * Returns an observable value that tracks the active editor for the given part
	 * service.
	 *
	 * @param partService the part service to get the observable for, e.g. a
	 *                    workbench page
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
	 * @param editor the editor to get the observable for
	 * @return an observable value that tracks the editor input
	 * @since 3.110
	 */
	public static IObservableValue<IEditorInput> observeEditorInput(IEditorPart editor) {
		Assert.isNotNull(editor);
		return WorkbenchProperties.editorInput().observe(editor);
	}
}
