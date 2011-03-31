/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
		IFile file = (IFile) getEditorInput().getAdapter(IFile.class);
		editor.setSelection(new StructuredSelection(file));
	}

	protected void createPages() {
		try {
			editor = new SubEditor();
			addPage(editor, getEditorInput());
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class) {
			if (page == null) {
				page = new PropertySheetPage();
			}
			return page;
		}
		return super.getAdapter(adapter);
	}

	public void doSave(IProgressMonitor monitor) {
		// nothing to do
	}

	public void doSaveAs() {
		// nothing to do
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	static class SubEditor extends EditorPart implements ISelectionProvider {

		private TreeViewer viewer;

		public void createPartControl(Composite parent) {
			viewer = new TreeViewer(parent);
			viewer.setContentProvider(new WorkbenchContentProvider());
			viewer.setLabelProvider(WorkbenchLabelProvider
					.getDecoratingWorkbenchLabelProvider());
			viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
			viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

			getSite().setSelectionProvider(this);
		}

		public void setFocus() {
			viewer.getControl().setFocus();
		}

		public void doSave(IProgressMonitor monitor) {
			// nothing to do
		}

		public void doSaveAs() {
			// nothing to do
		}

		public void init(IEditorSite site, IEditorInput input)
				throws PartInitException {
			setSite(site);
			setInput(input);
		}

		public boolean isDirty() {
			return false;
		}

		public boolean isSaveAsAllowed() {
			return false;
		}

		public void addSelectionChangedListener(
				ISelectionChangedListener listener) {
			viewer.addSelectionChangedListener(listener);
		}

		public ISelection getSelection() {
			return viewer.getSelection();
		}

		public void removeSelectionChangedListener(
				ISelectionChangedListener listener) {
			viewer.removeSelectionChangedListener(listener);
		}

		public void setSelection(ISelection selection) {
			viewer.setSelection(selection);
		}

	}
}
