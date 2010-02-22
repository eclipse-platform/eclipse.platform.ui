/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Genady Beryozkin, me@genady.org - initial API and implementation
 *     Fabio Zadrozny <fabiofz at gmail dot com> - [typing] HippieCompleteAction is slow  ( Alt+/ ) - https://bugs.eclipse.org/bugs/show_bug.cgi?id=270385
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.internal.texteditor.CompoundEditExitStrategy;
import org.eclipse.ui.internal.texteditor.HippieCompletionEngine;
import org.eclipse.ui.internal.texteditor.ICompoundEditListener;
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
		 * Iterator of Strings with suggestions computed when the completion action is invoked
		 * 
		 * @since 3.6
		 */
		private final Iterator suggestions;

		/**
		 * List of Strings with the suggestions that are already consumed from the iterator
		 * 
		 * @since 3.6
		 */
		private final List consumedSuggestions;

		/**
		 * Do we have only 1 (empty) completion available?
		 * 
		 * @since 3.6
		 */
		private final boolean hasOnly1EmptySuggestion;

		/**
		 * Set with the String completions found so that we can make them unique
		 * 
		 * @since 3.6
		 */
		private final HashSet alreadyFound;

		/**
		 * Create a new completion state object
		 * 
		 * @param suggestions the iterator of Strings with possible completions
		 * @param startOffset the position in the parent document at which the completions will be
		 *            inserted.
		 */
		CompletionState(Iterator suggestions, int startOffset) {
			this.suggestions= suggestions;
			this.consumedSuggestions= new ArrayList();
			this.alreadyFound= new HashSet();
			this.startOffset= startOffset;
			this.length= 0;
			this.nextSuggestion= 0;


			//Let's see if only 1 is available.
			if (this.suggestions.hasNext()) {
				addNewToken((String)this.suggestions.next());

				boolean hasOnly1Temp= true;
				while (this.suggestions.hasNext()) {
					Object next= this.suggestions.next();
					if (consumedSuggestions.contains(next)) {
						continue;
					}
					addNewToken((String)next);
					hasOnly1Temp= false;
					break;
				}
				this.hasOnly1EmptySuggestion= hasOnly1Temp;
			} else {
				throw new AssertionError("At least the empty completion must be available in the iterator!"); //$NON-NLS-1$
			}
		}

		/**
		 * Advances the completion state to represent the next completion (starts cycling when it
		 * gets to the end).
		 * 
		 * @return The next suggestion to be shown to the user.
		 * @since 3.6
		 */
		public String next() {
			String ret= null;
			if (this.consumedSuggestions.size() > nextSuggestion) {
				//We already consumed one that we didn't return
				ret= (String)this.consumedSuggestions.get(nextSuggestion);
				nextSuggestion++;

			}

			while (ret == null &&
					this.consumedSuggestions.size() == nextSuggestion &&
					this.suggestions.hasNext()) {
				//we're just in the place to get a new one from the iterator
				String temp= (String)this.suggestions.next();
				if (this.alreadyFound.contains(temp)) {
					continue;//go to next iteration
				}
				addNewToken(temp);
				ret= temp;
				nextSuggestion++;

			}

			if (ret == null) {
				//we consumed all in the iterator, so, just start cycling.
				ret= (String)this.consumedSuggestions.get(0);
				nextSuggestion= 1; //we just got the 0, so, we can already skip to 1.
			}


			length= ret.length();
			return ret;
		}

		/**
		 * Adds a new suggestion to the found and consumed suggestions
		 * 
		 * @param suggestion the suggestion to be added
		 * @since 3.6
		 */
		private void addNewToken(String suggestion) {
			this.alreadyFound.add(suggestion);
			this.consumedSuggestions.add(suggestion);
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
	 * The completion engine
	 */
	private final HippieCompletionEngine fEngine= new HippieCompletionEngine();

	/** The compound edit exit strategy. */
	private final CompoundEditExitStrategy fExitStrategy= new CompoundEditExitStrategy(ITextEditorActionDefinitionIds.HIPPIE_COMPLETION);

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
		fExitStrategy.addCompoundListener(new ICompoundEditListener() {
			public void endCompoundEdit() {
				clearState();
			}
		});
	}

	/**
	 * Invalidates the cached completions, removes all registered listeners and
	 * sets the cached document to <code>null</code>.
	 */
	private void clearState() {
		fLastCompletion= null;

		ITextEditor editor= getTextEditor();

		if (editor != null) {
			IRewriteTarget target= (IRewriteTarget) editor.getAdapter(IRewriteTarget.class);
			if (target != null) {
				fExitStrategy.disarm();
				target.endCompoundChange();
			}
		}

		fDocument= null;
	}

	/**
	 * Perform the next completion.
	 */
	private void completeNext() {
		try {
			fDocument.replace(fLastCompletion.startOffset, fLastCompletion.length, fLastCompletion.next()); //next() will already advance
		} catch (BadLocationException e) {
			// we should never get here. different from other places to notify the user.
			log(e);
			clearState();
			return;
		}

		// move the caret to the insertion point
		ISourceViewer sourceViewer= ((AbstractTextEditor) getTextEditor()).getSourceViewer();
		sourceViewer.setSelectedRange(fLastCompletion.startOffset + fLastCompletion.length, 0);
		sourceViewer.revealRange(fLastCompletion.startOffset, fLastCompletion.length);

		fExitStrategy.arm(((AbstractTextEditor) getTextEditor()).getSourceViewer());
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
		return fEngine.getPrefixString(fDocument, selection.getOffset());
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

		List documents= HippieCompletionEngine.computeDocuments(getTextEditor());

		if (documents.size() > 0) {
			fDocument= (IDocument)documents.remove(0);

			Iterator suggestions;
			try {
				String prefix= getCurrentPrefix();
				if (prefix == null) {
					notifyUser();
					return;
				}
				suggestions= fEngine.getMultipleDocumentsIterator(
						fDocument, documents, prefix, getSelectionOffset());
			} catch (BadLocationException e) {
				log(e);
				return;
			}

			CompletionState completionState= new CompletionState(
					suggestions, getSelectionOffset());

			// if it is single empty suggestion
			if (completionState.hasOnly1EmptySuggestion) {
				notifyUser();
				return;
			}

			IRewriteTarget target= (IRewriteTarget)getTextEditor().getAdapter(IRewriteTarget.class);
			if (target != null)
				target.beginCompoundChange();

			fLastCompletion= completionState;
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
