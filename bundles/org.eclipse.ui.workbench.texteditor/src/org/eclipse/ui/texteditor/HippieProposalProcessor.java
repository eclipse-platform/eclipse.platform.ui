/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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
 *     Genady Beryozkin, me@genady.org - #getSuggestions implementation copied from HippieCompleteAction
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.viewers.StyledString;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension7;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.HippieCompletionEngine;

/**
 * A completion proposal computer for hippie word completions.
 * <p>
 * Clients may instantiate.
 * </p>
 *
 * @since 3.2
 */
public final class HippieProposalProcessor implements IContentAssistProcessor {

	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];

	private static final class Proposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3, ICompletionProposalExtension4,
			ICompletionProposalExtension6, ICompletionProposalExtension7 {

		private final String fString;
		private final String fPrefix;
		private final int fOffset;
		private StyledString fDisplayString;

		public Proposal(String string, String prefix, int offset) {
			fString= string;
			fPrefix= prefix;
			fOffset= offset;
		}

		@Override
		public void apply(IDocument document) {
			apply(null, '\0', 0, fOffset);
		}

		@Override
		public Point getSelection(IDocument document) {
			return new Point(fOffset + fString.length(), 0);
		}

		@Override
		public String getAdditionalProposalInfo() {
			return null;
		}

		@Override
		public String getDisplayString() {
			return fPrefix + fString;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public void apply(IDocument document, char trigger, int offset) {
			try {
				String replacement= fString.substring(offset - fOffset);
				document.replace(offset, 0, replacement);
			} catch (BadLocationException x) {
				// ignore
			}
		}

		@Override
		public boolean isValidFor(IDocument document, int offset) {
			return validate(document, offset, null);
		}

		@Override
		public char[] getTriggerCharacters() {
			return null;
		}

		@Override
		public int getContextInformationPosition() {
			return 0;
		}

		@Override
		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			apply(viewer.getDocument(), trigger, offset);
		}

		@Override
		public void selected(ITextViewer viewer, boolean smartToggle) {
		}

		@Override
		public void unselected(ITextViewer viewer) {
		}

		@Override
		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			try {
				int prefixStart= fOffset - fPrefix.length();
				return offset >= fOffset && offset < fOffset + fString.length() && document.get(prefixStart, offset - (prefixStart)).equals((fPrefix + fString).substring(0, offset - prefixStart));
			} catch (BadLocationException x) {
				return false;
			}
		}

		@Override
		public IInformationControlCreator getInformationControlCreator() {
			return null;
		}

		@Override
		public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
			return fPrefix + fString;
		}

		@Override
		public int getPrefixCompletionStart(IDocument document, int completionOffset) {
			return fOffset - fPrefix.length();
		}

		@Override
		public boolean isAutoInsertable() {
			return true;
		}

		@Override
		public StyledString getStyledDisplayString() {
			if (fDisplayString == null) {
				fDisplayString= new StyledString(getDisplayString());
			}
			return fDisplayString;
		}

		@Override
		public StyledString getStyledDisplayString(IDocument document, int offset, BoldStylerProvider boldStylerProvider) {
			StyledString styledDisplayString= new StyledString();
			styledDisplayString.append(getStyledDisplayString());

			int start= getPrefixCompletionStart(document, offset);
			int patternLength= offset - start;
			if (patternLength > 0) {
				styledDisplayString.setStyle(0, patternLength, boldStylerProvider.getBoldStyler());
			}
			return styledDisplayString;
		}

	}

	private final HippieCompletionEngine fEngine= new HippieCompletionEngine();

	/**
	 * Creates a new hippie completion proposal computer.
	 */
	public HippieProposalProcessor() {
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			String prefix= getPrefix(viewer, offset);
			if (prefix == null || prefix.isEmpty()) {
				return NO_PROPOSALS;
			}

			List<ICompletionProposal> result= new ArrayList<>();
			for (String string : getSuggestions(viewer, offset, prefix)) {
				if (!string.isEmpty()) {
					result.add(createProposal(string, prefix, offset));
				}
			}

			return result.toArray(new ICompletionProposal[result.size()]);

		} catch (BadLocationException x) {
			// ignore and return no proposals
			return NO_PROPOSALS;
		}
	}

	private String getPrefix(ITextViewer viewer, int offset) throws BadLocationException {
		IDocument doc= viewer.getDocument();
		if (doc == null || offset > doc.getLength()) {
			return null;
		}

		int length= 0;
		while (--offset >= 0 && Character.isJavaIdentifierPart(doc.getChar(offset))) {
			length++;
		}

		return doc.get(offset + 1, length);
	}

	private ICompletionProposal createProposal(String string, String prefix, int offset) {
		return new Proposal(string, prefix, offset);
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// no context informations for hippie completions
		return NO_CONTEXTS;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * Return the list of suggestions from the current document. First the document is searched
	 * backwards from the caret position and then forwards.
	 *
	 * @param offset the offset
	 * @param viewer the viewer
	 * @param prefix the completion prefix
	 * @return all possible completions that were found in the current document
	 * @throws BadLocationException if accessing the document fails
	 */
	private ArrayList<String> createSuggestionsFromOpenDocument(ITextViewer viewer, int offset, String prefix) throws BadLocationException {
		IDocument document= viewer.getDocument();
		ArrayList<String> completions= new ArrayList<>();
		completions.addAll(fEngine.getCompletionsBackwards(document, prefix, offset));
		completions.addAll(fEngine.getCompletionsForward(document, prefix, offset - prefix.length(), true));

		return completions;
	}

	/**
	 * Create the array of suggestions. It scans for other documents or editors,
	 * and prefers suggestions from the currently open editor. It also adds the
	 * empty suggestion at the end.
	 *
	 * @param viewer
	 *            the viewer
	 * @param offset
	 *            the offset
	 * @param prefix
	 *            the prefix to search for
	 * @return the list of all possible suggestions in the currently open
	 *         editors
	 * @throws BadLocationException
	 *             if accessing the current document fails
	 */
	private List<String> getSuggestions(ITextViewer viewer, int offset, String prefix) throws BadLocationException {
		ArrayList<String> suggestions= createSuggestionsFromOpenDocument(viewer, offset, prefix);
		IDocument currentDocument= viewer.getDocument();
		for (ITextEditor editor : findTextEditors()) {
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			if (doc != null && !doc.equals(currentDocument)) {
				suggestions.addAll(fEngine.getCompletionsForward(doc, prefix, 0, false));
			}
		}
		// add the empty suggestion
		suggestions.add(""); //$NON-NLS-1$
		return fEngine.makeUnique(suggestions);
	}

	private Collection<ITextEditor> findTextEditors() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Stream<IWorkbenchWindow> windows = null;
		if (window != null) {
			windows = Collections.singleton(window).stream();
		} else {
			windows = Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows());
		}
		return windows.flatMap(aWindow -> Arrays.stream(aWindow.getPages())) //
				.flatMap(workbenchPage -> Arrays.stream(workbenchPage.getEditorReferences())) //
				.map(editorRef -> editorRef.getEditor(false)) //
				.filter(Objects::nonNull) //
				.filter(ITextEditor.class::isInstance) //
				.map(ITextEditor.class::cast) //
				.collect(Collectors.toSet());
	}

	@Override
	public String getErrorMessage() {
		return null; // no custom error message
	}
}
