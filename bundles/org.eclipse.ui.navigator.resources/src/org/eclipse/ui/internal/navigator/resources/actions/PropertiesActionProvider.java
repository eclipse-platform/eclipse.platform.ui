/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
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
 * 
 */
public class PropertiesActionProvider extends CommonActionProvider {

	private PropertyDialogAction propertiesAction;
	private ISelectionProvider delegateSelectionProvider;

	public void init(final ICommonActionExtensionSite aSite) {
		
		delegateSelectionProvider = new DelegateSelectionProvider( aSite.getViewSite().getSelectionProvider());
		propertiesAction = new PropertyDialogAction(new IShellProvider() {
			public Shell getShell() {
				return aSite.getViewSite().getShell();
			}
		},delegateSelectionProvider);
		propertiesAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_PROPERTIES); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);

		if (propertiesAction.isApplicableForSelection()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_PROPERTIES,
					propertiesAction);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars
	 * )
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(),
				propertiesAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);

		propertiesAction.selectionChanged(delegateSelectionProvider
				.getSelection());

	}

	private class DelegateIAdaptable implements IAdaptable {

		private Object delegate;

		private DelegateIAdaptable(Object o) {
			delegate = o;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class adapter) {
			if (adapter.isInstance(delegate) || delegate == null) {
				return delegate;
			}
			return Platform.getAdapterManager().getAdapter(delegate, adapter);
		}
	}

	private class DelegateSelectionProvider implements ISelectionProvider {

		private ISelectionProvider delegate;

		private DelegateSelectionProvider(ISelectionProvider s) {
			delegate = s;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener(
				ISelectionChangedListener listener) {
			delegate.addSelectionChangedListener(listener);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			delegate.removeSelectionChangedListener(listener);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection(ISelection selection) {
			delegate.setSelection(selection);

		}

	}

}
