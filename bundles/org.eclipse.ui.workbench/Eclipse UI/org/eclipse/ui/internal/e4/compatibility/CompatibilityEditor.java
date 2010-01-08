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

public class CompatibilityEditor extends CompatibilityPart {

	private IEditorInput input;

	void setInput(IEditorInput input) {
		this.input = input;
	}

	@Override
	protected IWorkbenchPart createPart() {
		// TODO Auto-generated method stub
		return null;
	}

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
				// TODO Auto-generated method stub
				return null;
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
				// TODO Auto-generated method stub
				return null;
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
				// TODO Auto-generated method stub
				return null;
			}

			public IKeyBindingService getKeyBindingService() {
				// TODO Auto-generated method stub
				return null;
			}

			public String getId() {
				// TODO Auto-generated method stub
				return null;
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
