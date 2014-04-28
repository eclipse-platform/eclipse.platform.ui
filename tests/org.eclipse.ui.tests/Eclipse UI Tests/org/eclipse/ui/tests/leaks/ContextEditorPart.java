/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 ******************************************************************************/

package org.eclipse.ui.tests.leaks;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.EditorPart;

/**
 * @since 3.4
 *
 */
public class ContextEditorPart extends EditorPart {

	private Text text;
	private ISelectionProvider selectionProvider = null;
	private Menu contextMenu;

	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite arg0, IEditorInput arg1) {
		setSite(arg0);
		setInput(arg1);
		selectionProvider = new ISelectionProvider() {
			@Override
			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			@Override
			public ISelection getSelection() {
				return new StructuredSelection("Hi there");
			}

			@Override
			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
			}

			@Override
			public void setSelection(ISelection selection) {
			}
		};
		getSite().setSelectionProvider(selectionProvider);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.MULTI | SWT.WRAP);
		text.setText("Hi there");
		MenuManager manager = new MenuManager();
		manager.setRemoveAllWhenShown(true);
		contextMenu = manager.createContextMenu(text);
		text.setMenu(contextMenu);

		getSite().registerContextMenu(manager, selectionProvider);
	}

	@Override
	public void setFocus() {
		text.setFocus();
	}

	public void showMenu() {
		contextMenu.notifyListeners(SWT.Show, null);
	}

	public void hideMenu() {
		contextMenu.notifyListeners(SWT.Hide, null);
	}

	public Menu getMenu() {
		return contextMenu;
	}
}
