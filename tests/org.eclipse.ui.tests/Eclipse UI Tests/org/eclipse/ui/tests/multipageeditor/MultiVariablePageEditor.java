/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * A MultiPageEditorPart with methods that take a peek at things like selection
 * events or selection status or page change events.
 * 
 * @since 3.2
 */
public class MultiVariablePageEditor extends MultiPageEditorPart {

	private Composite lastPage;

	/**
	 * Default with 2 pages, although they're on the same editor input and
	 * they're the TextEditor.
	 */
	@Override
	protected void createPages() {
		try {
			TextEditor section1 = new TextEditor();
			int index = addPage(section1, getEditorInput());
			setPageText(index, section1.getTitle());

			TextEditor section2 = new TextEditor();
			index = addPage(section2, getEditorInput());
			setPageText(index, section2.getTitle());

			ContextTextEditor section3 = new ContextTextEditor();
			index = addPage(section3, getEditorInput());
			setPageText(index, section3.getTitle());

		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException(
				"doSaveAs should not be called.");
	}

	/**
	 * No save as.
	 * 
	 * @return false
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
	 */
	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		IEditorPart part = getEditor(newPageIndex);
		if (part instanceof TextEditor) {
			TextEditor editor = (TextEditor) part;
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument doc = provider.getDocument(getEditorInput());
			FindReplaceDocumentAdapter find = new FindReplaceDocumentAdapter(
					doc);
			try {
				IRegion region = find.find(0, "#section0" + (newPageIndex + 1),
						true, true, false, false);
				if (region != null) {
					editor.selectAndReveal(region.getOffset(), region
							.getLength());
				}
			} catch (BadLocationException e) {
				System.err.println("Failed to find a section");
			}
		}
	}

	/**
	 * Set the active page in this MPEP. Just delegate back to
	 * setActivePage(int).
	 * 
	 * @param index
	 *            The page index which must be valid.
	 */
	public void setPage(int index) {
		super.setActivePage(index);
	}

	/**
	 * Add a page with a composite for testing.
	 * 
	 */
	public void addLastPage() {
		lastPage = new Composite(getContainer(), SWT.NONE);
		Label l = new Label(lastPage, SWT.SHADOW_IN);
		l.setText(getEditorInput().getName());
		addPage(lastPage);
	}

	/**
	 * remove the last page for testing.
	 * 
	 */
	public void removeLastPage() {
		if (getPageCount() > 0) {
			removePage(getPageCount() - 1);
		}
		lastPage = null;
	}

	/**
	 * Get the last page composite for testing.
	 * 
	 * @return the last page.
	 */
	public Control getLastPage() {
		return lastPage;
	}
	
	@Override
	public IEditorPart getEditor(int pageIndex) {
		return super.getEditor(pageIndex);
	}

	/**
	 * Return the control for testing (like the editor control).
	 * 
	 * @param index
	 *            the page index to get
	 * @return the control for that page
	 */
	public Control getTestControl(int index) {
		return getControl(index);
	}
}
