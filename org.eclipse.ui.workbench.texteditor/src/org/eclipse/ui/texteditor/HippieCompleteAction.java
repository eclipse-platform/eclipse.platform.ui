/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Genady Beryozkin, me@genady.org - initial API and implementation
 *     IBM Corporation - fixes and cleaning
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.internal.texteditor.HippieCompletionEngine;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

/**
 * This class implements the emacs style completion action. Completion action is
 * a stateful action, as the user may invoke it several times in a row in order
 * to scroll the possible completions.
 * 
 * TODO: Sort by editor type
 * TODO: Provide history option
 * 
 * @since 3.1
 * @author Genady Beryozkin, me@genady.org
 */
final class HippieCompleteAction extends TextEditorAction {
	
	/**
	 * This class represents the state of the last completion process. Each time
	 * the user moves to a new position and calls this action an instance of
	 * this inner class is created and saved in
	 * {@link HippieCompleteAction#fLastCompletion}.
	 */
	private static class CompletionState {
		
		/** The length of the last suggestion string */
		int length;
		
		/** The index of next suggestion (index into the suggestion array) */
		int nextSuggestion;
		
		/** The caret position at which we insert the suggestions */
		final int startOffset;
		
		/**
		 * The list of suggestions that was computed when the completion action
		 * was first invoked
		 */
		final String[] suggestions;
		
		/**
		 * Create a new completion state object
		 * 
		 * @param suggestions the array of possible completions
		 * @param startOffset the position in the parent document at which the
		 *        completions will be inserted.
		 */
		CompletionState(String[] suggestions, int startOffset) {
			this.suggestions= suggestions;
			this.startOffset= startOffset;
			length= 0;
			nextSuggestion= 0;
		}
		
		/**
		 * Advances the completion state to represent the next completion.
		 */
		public void advance() {
			length= suggestions[nextSuggestion].length();
			nextSuggestion= (nextSuggestion + 1) % suggestions.length;	
		}
	}
	
	/**
	 * Invalidate the completion state when the document contents changes not as
	 * the result of the completion action itself.
	 */
	class DocumentChangeListener implements IDocumentListener {
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
		
		public void documentChanged(DocumentEvent event) {
			if (!fModifyingLock) {
				clearState();
			}
		}
	}
	
	/**
	 * This class invalidates the completion state when the selection changes.
	 */
	class SelectionChangeListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			if (!fModifyingLock) {
				clearState();
			}
		}
	}
	
	/**
	 * The document that will be manipulated (currently open in the editor)
	 */
	private IDocument fDocument;
	
	/**
	 * The completion state that is used to continue the iteration over
	 * completion suggestions
	 */
	private CompletionState fLastCompletion= null;
	
	/**
	 * Modification lock that will prevent invalidation of the completion state
	 * when the completion action modifies the document
	 */
	private boolean fModifyingLock= false;
	
	/**
	 * The selection listener that is registered with the selection provider of
	 * this editor.
	 */
	private SelectionChangeListener fSelectionListener;
	
	/**
	 * The document change listener that is registered with the currently open
	 * document.
	 */
	private DocumentChangeListener fDocumentListener;
	
	/**
	 * The completion engine
	 */
	private final HippieCompletionEngine fEngine= new HippieCompletionEngine();
	
	/**
	 * Creates a new action.
	 * 
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *        (described in <code>ResourceAction</code> constructor), or
	 *        <code>null</code> if none
	 * @param editor the text editor
	 */
	HippieCompleteAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/**
	 * Invalidates the cached completions, removes all registered listeners and
	 * sets the cached document to <code>null</code>.
	 */
	private void clearState() {
		fLastCompletion= null;
		
		ITextEditor editor= getTextEditor();
		if (editor != null && fSelectionListener != null)
			editor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
		
		if (fDocument != null && fDocumentListener != null)
			fDocument.removeDocumentListener(fDocumentListener);
		
		fDocument= null;
	}
	
	/**
	 * Perform the next completion.
	 */
	private void completeNext() {
		// we don't wish to receive events on our own changes
		fModifyingLock= true;
		try {
			try {
				fDocument.replace(fLastCompletion.startOffset, fLastCompletion.length, fLastCompletion.suggestions[fLastCompletion.nextSuggestion]);
			} catch (BadLocationException e) {
				// we should never get here. different from other places to notify the user.
				log(e);
				clearState();
				return;
			}
			
			// advance the suggestion state
			fLastCompletion.advance();
			
			// move the caret to the insertion point
			((AbstractTextEditor) getTextEditor()).getSourceViewer().setSelectedRange(fLastCompletion.startOffset + fLastCompletion.length, 0);
		} finally {
			// allow changes
			fModifyingLock= false;
		}
	}	
	
	/**
	 * Return the list of suggestions from the current document. First the
	 * document is searched backwards from the caret position and then forwards.
	 * 
	 * @param prefix the completion prefix
	 * @return all possible completions that were found in the current document
	 * @throws BadLocationException if accessing the document fails
	 */
	private ArrayList createSuggestionsFromOpenDocument(String prefix) throws BadLocationException {
		int selectionOffset= getSelectionOffset();
		
		ArrayList completions= new ArrayList();
		completions.addAll(fEngine.getCompletionsBackwards(fDocument, prefix, selectionOffset));
		completions.addAll(fEngine.getCompletionsForward(fDocument, prefix, selectionOffset));
		
		return completions;
	}
	
	/**
	 * Returns the document currently displayed in the editor, or
	 * <code>null</code>
	 * 
	 * @return the document currently displayed in the editor, or
	 *         <code>null</code>
	 */
	private IDocument getCurrentDocument() {
		ITextEditor editor= getTextEditor();
		if (editor == null)
			return null;
		IDocumentProvider provider= editor.getDocumentProvider();
		if (provider == null)
			return null;
		
		IDocument document= provider.getDocument(editor.getEditorInput());
		return document;
	}
	
	/**
	 * Return the part of a word before the caret. If the caret is not at a
	 * middle/end of a word, returns null.
	 * 
	 * @return the prefix at the current cursor position that will be used in
	 *         the search for possible completions
	 * @throws BadLocationException if accessing the document fails
	 */
	private String getCurrentPrefix() throws BadLocationException {
		ITextSelection selection= (ITextSelection) getTextEditor().getSelectionProvider().getSelection();
		if (selection.getLength() > 0) {
			return null;
		}
		int pos= selection.getOffset();
		
		int prevNonAlpha= pos;
		while (prevNonAlpha > 0 && Character.isJavaIdentifierPart(fDocument.getChar(prevNonAlpha-1))) {
			prevNonAlpha--;
		}
		if (prevNonAlpha != pos) {
			return fDocument.get(prevNonAlpha, pos - prevNonAlpha);
		}
		return null;
	}
	
	/**
	 * Returns the current selection (or caret) offset.
	 * 
	 * @return the current selection (or caret) offset
	 */
	private int getSelectionOffset() {
		return ((ITextSelection) getTextEditor().getSelectionProvider().getSelection()).getOffset();
	}
	
	/**
	 * Create the array of suggestions. It scans all open text editors and
	 * prefers suggestions from the currently open editor. It also adds the
	 * empty suggestion at the end.
	 * 
	 * @param prefix the prefix to search for
	 * @return the list of all possible suggestions in the currently open
	 *         editors
	 * @throws BadLocationException if accessing the current document fails
	 */
	private String[] getSuggestions(String prefix) throws BadLocationException {
		
		ArrayList suggestions= createSuggestionsFromOpenDocument(prefix);
		
		IWorkbenchWindow window= getTextEditor().getSite().getWorkbenchWindow();
		IEditorReference editorsArray[]= window.getActivePage().getEditorReferences();
		
		for (int i= 0; i < editorsArray.length; i++) {
			IEditorPart realEditor= editorsArray[i].getEditor(false);
			if (realEditor instanceof ITextEditor &&
					!realEditor.equals(getTextEditor())) { // realEditor != null
				ITextEditor textEditor= (ITextEditor)realEditor;
				IEditorInput input= textEditor.getEditorInput();
				IDocument doc= textEditor.getDocumentProvider().getDocument(input);
				
				suggestions.addAll(fEngine.getCompletions(doc, prefix));
			}
		}		
		// add the empty suggestion
		suggestions.add(""); //$NON-NLS-1$
		
		List uniqueSuggestions= fEngine.makeUnique(suggestions);
		
		return (String[]) uniqueSuggestions.toArray(new String[0]);
	}
	
	/**
	 * Installs the selection and document listeners. Both editor and cached
	 * document must be valid.
	 */
	private void installListeners() {
		ITextEditor editor= getTextEditor();
		Assert.isNotNull(editor);
		Assert.isNotNull(fDocument);
		
		if (fSelectionListener == null)
			fSelectionListener= new SelectionChangeListener();
		editor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
		
		if (fDocumentListener == null)
			fDocumentListener= new DocumentChangeListener();
		fDocument.addDocumentListener(fDocumentListener);
	}
	
	/**
	 * Returns <code>true</code> if the current completion state is still
	 * valid given the current document and selection.
	 * 
	 * @return <code>true</code> if the cached state is valid,
	 *         <code>false</code> otherwise
	 */
	private boolean isStateValid() {
		return fDocument != null 
				&& fDocument.equals(getCurrentDocument()) 
				&& fLastCompletion != null 
				&& fLastCompletion.startOffset + fLastCompletion.length == getSelectionOffset();
	}
	
	/**
	 * Notifies the user that there are no suggestions.
	 */
	private void notifyUser() {
		// TODO notify via status line?
		getTextEditor().getSite().getShell().getDisplay().beep();
	}
	
	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		if (!validateEditorInputState())
			return;
		
		if (!isStateValid())
			updateState();
		
		if (isStateValid())
			completeNext();
	}
	
	/*
	 * @see org.eclipse.jface.action.IAction#isEnabled()
	 */
	public boolean isEnabled() {
		return canModifyEditor();
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		clearState(); // make sure to remove listers before the editor changes!
		super.setEditor(editor);
	}
	
	/**
	 * Update the completion state. The completion cache is updated with the
	 * completions based on the currently displayed document and the current
	 * selection. To track the validity of the cached state, listeners are
	 * registered with the editor and document, and the current document is
	 * cached.
	 */
	private void updateState() {
		Assert.isNotNull(getTextEditor());
		
		clearState();
		
		IDocument document= getCurrentDocument();
		if (document != null) {
			fDocument= document;
			
			String[] suggestions;
			try {
				String prefix= getCurrentPrefix();
				if (prefix == null) {
					notifyUser();
					return;
				}
				suggestions= getSuggestions(prefix);
			} catch (BadLocationException e) {
				log(e);
				return;
			}
			
			// if it is single empty suggestion
			if (suggestions.length == 1) {
				notifyUser();
				return;
			}
			
			installListeners();
			fLastCompletion= new CompletionState(suggestions, getSelectionOffset());
		}
	}
	
	/**
	 * Logs the exception.
	 * 
	 * @param e the exception
	 */
	private void log(BadLocationException e) {
		String msg= e.getLocalizedMessage();
		if (msg == null)
			msg= "unable to access the document"; //$NON-NLS-1$
		TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, msg, e));
	}
}
