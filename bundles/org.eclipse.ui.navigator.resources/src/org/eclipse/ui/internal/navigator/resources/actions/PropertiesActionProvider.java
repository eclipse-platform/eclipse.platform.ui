/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * Adds the properties action to the menu.
 *
 * @since 3.2
 */
public class PropertiesActionProvider extends CommonActionProvider {

	private PropertyDialogAction propertiesAction;
	private ISelectionProvider delegateSelectionProvider;

	@Override
	public void init(final ICommonActionExtensionSite aSite) {

		delegateSelectionProvider = new DelegateSelectionProvider( aSite.getViewSite().getSelectionProvider());
		propertiesAction = new PropertyDialogAction(() -> aSite.getViewSite().getShell(),delegateSelectionProvider);
		propertiesAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_PROPERTIES);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);

		if (propertiesAction.isApplicableForSelection()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES,
					propertiesAction);
		}

	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),
				propertiesAction);
	}

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);

		propertiesAction.selectionChanged(delegateSelectionProvider
				.getSelection());

	}

	private static class DelegateIAdaptable implements IAdaptable {

		private Object delegate;

		private DelegateIAdaptable(Object o) {
			delegate = o;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter.isInstance(delegate) || delegate == null) {
				return (T) delegate;
			}
			return Platform.getAdapterManager().getAdapter(delegate, adapter);
		}
	}

	private static class DelegateSelectionProvider implements ISelectionProvider {

		private ISelectionProvider delegate;

		private DelegateSelectionProvider(ISelectionProvider s) {
			delegate = s;
		}

		@Override
		public void addSelectionChangedListener(
				ISelectionChangedListener listener) {
			delegate.addSelectionChangedListener(listener);

		}

		@Override
		public ISelection getSelection() {
			if (delegate.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection sSel = (IStructuredSelection) delegate
						.getSelection();
				if (sSel.getFirstElement() instanceof IAdaptable) {
					return sSel;
				}

				return new StructuredSelection(new DelegateIAdaptable(sSel
						.getFirstElement()));
			}
			return delegate.getSelection();
		}

		@Override
		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			delegate.removeSelectionChangedListener(listener);

		}

		@Override
		public void setSelection(ISelection selection) {
			delegate.setSelection(selection);

		}

	}

}
