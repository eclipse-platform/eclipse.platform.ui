/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import javax.inject.Inject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.registry.EditorDescriptor;

public class CompatibilityEditor extends CompatibilityPart {

	private IEditorInput input;
	private EditorDescriptor descriptor;

	@Inject
	private IWorkbenchWindow workbenchWindow;

	void set(IEditorInput input, EditorDescriptor descriptor) {
		this.input = input;
		this.descriptor = descriptor;

		initialized = true;
		create();
	}

	@Override
	protected IWorkbenchPart createPart() {
		try {
			return descriptor.createEditor();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}

	@Override
	public void create() {
		if (initialized) {
			super.create();
		}
	}

	private boolean initialized = false;

	@Override
	protected void initialize(IWorkbenchPart part) throws PartInitException {
		((IEditorPart) part).init(createSite(), input);
	}

	public IEditorPart getEditor() {
		return (IEditorPart) getPart();
	}

	private IEditorSite createSite() {
		return new IEditorSite() {

			public boolean hasService(Class api) {
				// TODO Auto-generated method stub
				return false;
			}

			public Object getService(Class api) {
				// TODO Auto-generated method stub
				return null;
			}

			public Object getAdapter(Class adapter) {
				// TODO Auto-generated method stub
				return null;
			}

			public void setSelectionProvider(ISelectionProvider provider) {
				// TODO Auto-generated method stub

			}

			public IWorkbenchWindow getWorkbenchWindow() {
				return workbenchWindow;
			}

			public Shell getShell() {
				// TODO Auto-generated method stub
				return null;
			}

			public ISelectionProvider getSelectionProvider() {
				// TODO Auto-generated method stub
				return null;
			}

			public IWorkbenchPage getPage() {
				return workbenchWindow.getActivePage();
			}

			public void registerContextMenu(MenuManager menuManager,
					ISelectionProvider selectionProvider) {
				// TODO Auto-generated method stub

			}

			public void registerContextMenu(String menuId, MenuManager menuManager,
					ISelectionProvider selectionProvider) {
				// TODO Auto-generated method stub

			}

			public String getRegisteredName() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getPluginId() {
				// TODO Auto-generated method stub
				return null;
			}

			public IWorkbenchPart getPart() {
				return CompatibilityEditor.this.getPart();
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

			public String getId() {
				return part.getId();
			}

			public void registerContextMenu(String menuId, MenuManager menuManager,
					ISelectionProvider selectionProvider, boolean includeEditorInput) {
				// TODO Auto-generated method stub

			}

			public void registerContextMenu(MenuManager menuManager,
					ISelectionProvider selectionProvider, boolean includeEditorInput) {
				// TODO Auto-generated method stub

			}

			public IActionBars getActionBars() {
				// TODO Auto-generated method stub
				return null;
			}

			public IEditorActionBarContributor getActionBarContributor() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

}
