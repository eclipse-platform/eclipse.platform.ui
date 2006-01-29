/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.SearchView;

abstract public class RetrieverAction extends Action {
	public RetrieverAction() {
	}

	public void run() {
		IWorkbenchPage page= getWorkbenchPage();
		if (page == null) {
			return;
		}

		RetrieverQuery query= new RetrieverQuery(page);
		RetrieverPage.initializeQuery(query);
		if (modifyQuery(query)) {
			String searchPattern= query.getSearchText();
			if (searchPattern == null || searchPattern.length() == 0) {
				SearchView view= (SearchView) InternalSearchUI.getInstance().getSearchViewManager().activateSearchView(true);
				view.showEmptySearchPage(RetrieverPage.ID);
			} else {
				NewSearchUI.runQueryInBackground(query);
			}
		}
	}

	abstract protected boolean modifyQuery(RetrieverQuery query);
	abstract protected IWorkbenchPage getWorkbenchPage();

	final protected String extractSearchTextFromEditor(IEditorPart editor) {
		if (editor != null) {
			ITextSelection selection= null;
			ISelectionProvider provider= editor.getEditorSite().getSelectionProvider();
			if (provider != null) {
				ISelection s= provider.getSelection();
				if (s instanceof ITextSelection) {
					selection= (ITextSelection) s;
				}
			}

			if (selection != null) {
				if (selection.getLength() == 0) {
					ITextEditor txtEditor= getTextEditor(editor);
					if (txtEditor != null) {
						IDocument document= txtEditor.getDocumentProvider().getDocument(txtEditor.getEditorInput());
						selection= expandSelection(selection, document, null);
					}
				}

				if (selection.getLength() > 0) {
					return trimSearchString(selection.getText());
				}
			}
		}
		return null;
	}

	final protected String extractSearchTextFromSelection(ISelection sel) {
		if (sel instanceof ITextSelection) {
			return trimSearchString(((ITextSelection) sel).getText());
		}
		return null;
	}

	private String trimSearchString(String text) {
		int idx= text.indexOf('\n');
		int idx2= text.indexOf('\r');
		if (idx2 >= 0 && idx2 < idx) {
			idx= idx2;
		}
		if (idx >= 0) {
			text= text.substring(0, idx);
		}
		return text.trim();
	}

	private ITextEditor getTextEditor(IEditorPart editor) {
		if (editor instanceof ITextEditor) {
			return (ITextEditor) editor;
		} else
			if (editor instanceof FormEditor) {
				FormEditor me= (FormEditor) editor;
				editor= me.getActiveEditor();
				if (editor instanceof ITextEditor) {
					return (ITextEditor) editor;
				}
			}
		return null;
	}

	private ITextSelection expandSelection(ITextSelection sel, IDocument document, String stopChars) {
		int offset= sel.getOffset();
		int length= sel.getLength();

		// in case the length is zero we have to decide whether to go
		// left or right.
		if (length == 0) {
			// try right
			char chr= 0;
			char chl= 0;
			try {
				chr= document.getChar(offset);
			} catch (BadLocationException e2) {
			}
			try {
				chl= document.getChar(offset - 1);
			} catch (BadLocationException e2) {
			}

			if (isPartOfIdentifier(chr)) {
				length= 1;
			} else
				if (isPartOfIdentifier(chl)) {
					offset--;
					length= 1;
				} else
					if (stopChars != null && stopChars.indexOf(chr) == -1) {
						length= 1;
					} else
						if (stopChars != null && stopChars.indexOf(chl) == -1) {
							offset--;
							length= 1;
						} else {
							return sel;
						}
		}

		int a= offset + length - 1;
		int z= a;

		// move z one behind last character.
		try {
			char ch= document.getChar(z);
			while (isValidChar(stopChars, ch)) {
				ch= document.getChar(++z);
			}
		} catch (BadLocationException e2) {
		}
		// move a one before the first character
		try {
			char ch= document.getChar(a);
			while (isValidChar(stopChars, ch)) {
				ch= document.getChar(--a);
			}
		} catch (BadLocationException e2) {
		}

		if (a == z) {
			offset= a;
			length= 0;
		} else {
			offset= a + 1;
			length= z - a - 1;
		}
		return new TextSelection(document, offset, length);
	}

	private boolean isValidChar(String stopChars, char ch) {
		return stopChars == null ? isPartOfIdentifier(ch) : stopChars.indexOf(ch) == -1;
	}

	private boolean isPartOfIdentifier(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_';
	}

}
