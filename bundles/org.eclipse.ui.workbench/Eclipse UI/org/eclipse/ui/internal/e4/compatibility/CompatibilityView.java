/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

public class CompatibilityView extends CompatibilityPart {

	private IViewDescriptor descriptor;

	IViewDescriptor getDescriptor() {
		return descriptor;
	}

	protected IWorkbenchPart createPart() {
		try {
			descriptor = PlatformUI.getWorkbench().getViewRegistry().find(
					part.getId());
			return descriptor.createView();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	protected void initialize(IWorkbenchPart part) throws PartInitException {
		((IViewPart) part).init(createSite(), null);
	}

	private IViewSite createSite() {
		return new IViewSite() {
			
			private IActionBars actionBars;

			public IActionBars getActionBars() {
				if (actionBars == null) {
					actionBars = new ActionBars();
				}
				return actionBars;
			}

			public String getSecondaryId() {
				return null;
			}

			public String getId() {
				return part.getId();
			}

			public String getPluginId() {
				return null;
			}

			public String getRegisteredName() {
				return null;
			}

			public void registerContextMenu(String menuId, MenuManager menuManager,
					ISelectionProvider selectionProvider) {

			}

			public void registerContextMenu(MenuManager menuManager,
					ISelectionProvider selectionProvider) {

			}

			public IKeyBindingService getKeyBindingService() {
				return new IKeyBindingService() {

					public void unregisterAction(IAction action) {
						// TODO Auto-generated method stub

					}

					public void setScopes(String[] scopes) {
						// TODO Auto-generated method stub

					}

					public void registerAction(IAction action) {
						// TODO Auto-generated method stub

					}

					public String[] getScopes() {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}

			public IWorkbenchPart getPart() {
				return CompatibilityView.this.getPart();
			}

			public IWorkbenchPage getPage() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			}

			public ISelectionProvider getSelectionProvider() {
				return new ISelectionProvider() {

					public void setSelection(ISelection selection) {
						// TODO Auto-generated method stub

					}

					public void removeSelectionChangedListener(ISelectionChangedListener listener) {
						// TODO Auto-generated method stub

					}

					public ISelection getSelection() {
						// TODO Auto-generated method stub
						return null;
					}

					public void addSelectionChangedListener(ISelectionChangedListener listener) {
						// TODO Auto-generated method stub

					}
				};
			}

			public Shell getShell() {
				Control control = (Control) part.getWidget();
				return control.getShell();
			}

			public IWorkbenchWindow getWorkbenchWindow() {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			}

			public void setSelectionProvider(ISelectionProvider provider) {
			}

			public Object getService(Class api) {
				return null;
			}

			public boolean hasService(Class api) {
				return false;
			}

			public Object getAdapter(Class adapter) {
				return null;
			}
		};
	}

	public IViewPart getView() {
		return (IViewPart) getPart();
	}

}
