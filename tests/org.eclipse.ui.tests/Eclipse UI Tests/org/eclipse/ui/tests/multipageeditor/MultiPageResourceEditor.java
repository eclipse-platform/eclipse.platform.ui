/*******************************************************************************
 * Copyright (c) 2014, 2017 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class MultiPageResourceEditor extends MultiPageEditorPart {

	static final String EDITOR_ID = "org.eclipse.ui.tests.multipageeditor.MultiPageResourceEditor"; //$NON-NLS-1$

	private SubEditor editor;

	private IPropertySheetPage page;

	public void updateSelection() {
		IFile file = getEditorInput().getAdapter(IFile.class);
		editor.setSelection(new StructuredSelection(file));
	}

	@Override
	protected void createPages() {
		try {
			editor = new SubEditor();
			addPage(editor, getEditorInput());
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IPropertySheetPage.class) {
			if (page == null) {
				page = new PropertySheetPage();
			}
			return (T) page;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// nothing to do
	}

	@Override
	public void doSaveAs() {
		// nothing to do
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	static class SubEditor extends EditorPart implements ISelectionProvider {

		private TreeViewer viewer;

		@Override
		public void createPartControl(Composite parent) {
			viewer = new TreeViewer(parent);
			viewer.setContentProvider(new WorkbenchContentProvider());
			viewer.setLabelProvider(WorkbenchLabelProvider
					.getDecoratingWorkbenchLabelProvider());
			viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
			viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

			getSite().setSelectionProvider(this);
		}

		@Override
		public void setFocus() {
			viewer.getControl().setFocus();
		}

		@Override
		public void doSave(IProgressMonitor monitor) {
			// nothing to do
		}

		@Override
		public void doSaveAs() {
			// nothing to do
		}

		@Override
		public void init(IEditorSite site, IEditorInput input) {
			setSite(site);
			setInput(input);
		}

		@Override
		public boolean isDirty() {
			return false;
		}

		@Override
		public boolean isSaveAsAllowed() {
			return false;
		}

		@Override
		public void addSelectionChangedListener(
				ISelectionChangedListener listener) {
			viewer.addSelectionChangedListener(listener);
		}

		@Override
		public ISelection getSelection() {
			return viewer.getSelection();
		}

		@Override
		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			viewer.removeSelectionChangedListener(listener);
		}

		@Override
		public void setSelection(ISelection selection) {
			viewer.setSelection(selection);
		}

	}
}
