/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * Content assist processor for regular expressions.
 * 
 * @since 3.0
 */
final class RegExContentAssistProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {

	/**
	 * The available proposal strings.
	 */
	private final static HashMap fgProposalStrings= new HashMap();
	
	/**
	 * The available proposal keys.
	 */
	private final static ArrayList fgProposalKeys= new ArrayList();
	
	
	static {

		//---------- Proposal Keys ----------

		fgProposalKeys.add("\\\\"); //$NON-NLS-1$
		fgProposalKeys.add("\\0"); //$NON-NLS-1$
		fgProposalKeys.add("\\x"); //$NON-NLS-1$
		fgProposalKeys.add("\\u"); //$NON-NLS-1$
		fgProposalKeys.add("\\t"); //$NON-NLS-1$
		fgProposalKeys.add("\\n"); //$NON-NLS-1$
		fgProposalKeys.add("\\r"); //$NON-NLS-1$
		fgProposalKeys.add("\\f"); //$NON-NLS-1$
		fgProposalKeys.add("\\a"); //$NON-NLS-1$
		fgProposalKeys.add("\\e"); //$NON-NLS-1$
		fgProposalKeys.add("\\c"); //$NON-NLS-1$

		fgProposalKeys.add("."); //$NON-NLS-1$
		fgProposalKeys.add("\\d"); //$NON-NLS-1$
		fgProposalKeys.add("\\D"); //$NON-NLS-1$
		fgProposalKeys.add("\\s"); //$NON-NLS-1$
		fgProposalKeys.add("\\S"); //$NON-NLS-1$
		fgProposalKeys.add("\\w"); //$NON-NLS-1$
		fgProposalKeys.add("\\W"); //$NON-NLS-1$

		fgProposalKeys.add("^");  //$NON-NLS-1$
		fgProposalKeys.add("$");  //$NON-NLS-1$
		fgProposalKeys.add("\\b");  //$NON-NLS-1$
		fgProposalKeys.add("\\B");  //$NON-NLS-1$
		fgProposalKeys.add("\\A");  //$NON-NLS-1$
		fgProposalKeys.add("\\G");  //$NON-NLS-1$
//		fgProposalKeys.add("\\Z");  //$NON-NLS-1$
		fgProposalKeys.add("\\z");  //$NON-NLS-1$

		fgProposalKeys.add("?");  //$NON-NLS-1$
		fgProposalKeys.add("*");  //$NON-NLS-1$
		fgProposalKeys.add("+");  //$NON-NLS-1$
		fgProposalKeys.add("{n}");  //$NON-NLS-1$
		fgProposalKeys.add("{n,}");  //$NON-NLS-1$
		fgProposalKeys.add("{n,m}");  //$NON-NLS-1$

		fgProposalKeys.add("??");  //$NON-NLS-1$
		fgProposalKeys.add("*?");  //$NON-NLS-1$
		fgProposalKeys.add("+?");  //$NON-NLS-1$
		fgProposalKeys.add("{n}?");  //$NON-NLS-1$
		fgProposalKeys.add("{n,}?");  //$NON-NLS-1$
		fgProposalKeys.add("{n,m}?");  //$NON-NLS-1$

		fgProposalKeys.add("?+");  //$NON-NLS-1$
		fgProposalKeys.add("*+");  //$NON-NLS-1$
		fgProposalKeys.add("++");  //$NON-NLS-1$
		fgProposalKeys.add("{n}+");  //$NON-NLS-1$
		fgProposalKeys.add("{n,}+");  //$NON-NLS-1$
		fgProposalKeys.add("{n,m}+");  //$NON-NLS-1$
 
		fgProposalKeys.add("UV");  //$NON-NLS-1$
		fgProposalKeys.add("U|V");  //$NON-NLS-1$
		fgProposalKeys.add("(U)");  //$NON-NLS-1$
		
		fgProposalKeys.add("\\i");  //$NON-NLS-1$
		fgProposalKeys.add("$i");  //$NON-NLS-1$

		fgProposalKeys.add("\\");  //$NON-NLS-1$
		fgProposalKeys.add("\\Q");  //$NON-NLS-1$
		fgProposalKeys.add("\\E");  //$NON-NLS-1$

		fgProposalKeys.add("[ecq]"); //$NON-NLS-1$
		fgProposalKeys.add("[^ecq]"); //$NON-NLS-1$
		fgProposalKeys.add("[e-q]"); //$NON-NLS-1$
		fgProposalKeys.add("&&"); //$NON-NLS-1$

//		fgProposalKeys.add("\\p{Lower}");  //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Upper}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{ASCII}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Alpha}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Digit}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Alnum}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Punct}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Graph}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Print}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Blank}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Cntrl}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{XDigit}"); //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Space}"); //$NON-NLS-1$
//
//		fgProposalKeys.add("\\p{InGreek}");  //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Lu}");  //$NON-NLS-1$
//		fgProposalKeys.add("\\p{Sc}");  //$NON-NLS-1$
//		fgProposalKeys.add("\\P{InGreek}");  //$NON-NLS-1$
//		fgProposalKeys.add("[\\p{L}&&[^\\p{Lu}]");  //$NON-NLS-1$

//		fgProposalKeys.add("(?idmsux-idmsux)");  //$NON-NLS-1$
//		fgProposalKeys.add("(?idmsux-idmsux:U)");  //$NON-NLS-1$

//		fgProposalKeys.add("(?:U)");  //$NON-NLS-1$
//		fgProposalKeys.add("(?=U)");  //$NON-NLS-1$
//		fgProposalKeys.add("(?!U)");  //$NON-NLS-1$
//		fgProposalKeys.add("(?<=U)");  //$NON-NLS-1$
//		fgProposalKeys.add("(?<!U)");  //$NON-NLS-1$
//		fgProposalKeys.add("(?>U)");  //$NON-NLS-1$
		
		//---------- Proposals ----------
		
		fgProposalStrings.put("\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\0", "\\0"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\x", "\\x"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\u", "\\u"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\t", "\\t"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\r", "\\r"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\f", "\\f"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\a", "\\a"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\e", "\\e"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\c", "\\c"); //$NON-NLS-1$ //$NON-NLS-2$
		
		fgProposalStrings.put(".", "."); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\d", "\\d"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\D", "\\D"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\s", "\\s"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\S", "\\S"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\w", "\\w"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\W", "\\W"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("^", "^");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("$", "$");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\b", "\\b");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\B", "\\B");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\A", "\\A");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\G", "\\G");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\Z", "\\Z");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\z", "\\z");  //$NON-NLS-1$ //$NON-NLS-2$

		fgProposalStrings.put("?", "?");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("*", "*");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("+", "+");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n}", "{}");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n,}", "{,}");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n,m}", "{,}");  //$NON-NLS-1$ //$NON-NLS-2$

		fgProposalStrings.put("??", "??");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("*?", "*?");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("+?", "+?");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n}?", "{}?");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n,}?", "{,}?");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n,m}?", "{,}?");  //$NON-NLS-1$ //$NON-NLS-2$

		fgProposalStrings.put("?+", "?+");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("*+", "*+");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("++", "++");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n}+", "{}+");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n,}+", "{,}+");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("{n,m}+", "{,}+");  //$NON-NLS-1$ //$NON-NLS-2$
 
		fgProposalStrings.put("UV", "");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("U|V", "|");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(U)", "()");  //$NON-NLS-1$ //$NON-NLS-2$
		
		fgProposalStrings.put("\\i", "\\");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("$i", "$");  //$NON-NLS-1$ //$NON-NLS-2$
		
		fgProposalStrings.put("\\", "\\");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\Q", "\\Q");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\E", "\\E");  //$NON-NLS-1$ //$NON-NLS-2$
		
		fgProposalStrings.put("[ecq]", "[]"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("[^ecq]", "^"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("[e-q]", "[]"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("&&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
		
		fgProposalStrings.put("\\p{Lower}", "\\p{Lower}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Upper}", "\\p{Upper}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{ASCII}", "\\p{ASCII}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Alpha}", "\\p{Alpha}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Digit}", "\\p{Digit}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Alnum}", "\\p{Alnum}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Punct}", "\\p{Punct}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Graph}", "\\p{Graph}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Print}", "\\p{Print}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Blank}", "\\p{Blank}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Cntrl}", "\\p{Cntrl}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{XDigit}", "\\p{XDigit}"); //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Space}", "\\p{Space}"); //$NON-NLS-1$ //$NON-NLS-2$

		fgProposalStrings.put("\\p{InGreek}", "\\p{InGreek}");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Lu}", "\\p{Lu}");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\p{Sc}", "\\p{Sc}");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("\\P{InGreek}", "\\P{InGreek}");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("[\\p{L}&&[^\\p{Lu}]","[\\p{L}&&[^\\p{Lu}]");  //$NON-NLS-1$ //$NON-NLS-2$
 
		fgProposalStrings.put("(?:U)", "(?:)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?idmsux-idmsux)", "(?)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?idmsux-idmsux:U)", "(?:)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?=U)", "(?=)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?!U)", "(?!)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?<=U)", "(?<=)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?<!U)", "(?<!)");  //$NON-NLS-1$ //$NON-NLS-2$
		fgProposalStrings.put("(?>U)", "(?>)");  //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * The context information validator.
	 */
	private IContextInformationValidator fValidator= new SubjectControlContextInformationValidator(this);
	
	/*
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		return computeCompletionProposals((IContentAssistSubjectControl)null, documentOffset);
	}
	
	/*
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		return computeContextInformation((IContentAssistSubjectControl)null, documentOffset);
	}
	
	/*
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {'\\', '[', '('};
	}
	
	/*
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { };
		
	}
	
	/*
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}
	
	/*
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeCompletionProposals(IContentAssistSubjectControl, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		List results= new ArrayList(fgProposalKeys.size());
		Iterator iter= fgProposalKeys.iterator();
		while (iter.hasNext())
			addProposal((String)iter.next(), contentAssistSubjectControl, documentOffset, results, true);

		if (results.isEmpty()) {
			iter= fgProposalKeys.iterator();
			while (iter.hasNext())
				addProposal((String)iter.next(), contentAssistSubjectControl, documentOffset, results, false);
		}

		return (ICompletionProposal[])results.toArray(new ICompletionProposal[results.size()]);
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeContextInformation(IContentAssistSubjectControl, int)
	 */
	public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		return null;
	}
	
	private void addProposal(String proposalKey, IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset, List results, boolean filter) {
		String proposal= (String)fgProposalStrings.get(proposalKey);

		// compute correct replacement
		if (filter) {
			String selection= null;
			try {
				selection = contentAssistSubjectControl.getDocument().get(documentOffset - 1, 1);
			} catch (BadLocationException e) {
				return ;
			}
			if (selection == null || selection.length() == 0 || proposal.length() == 0 || proposal.charAt(0) != selection.charAt(0))
				return;
			
			proposal= proposal.substring(1);
		}

		String displayString= getString(proposalKey, "displayString"); //$NON-NLS-1$
		String additionalInfo= getString(proposalKey, "additionalInfo"); //$NON-NLS-1$
		IContextInformation info= createContextInformation(proposalKey);

//		// Move cursor on to the left if the proposal ends with '}'
		int relativeOffset= proposal.length();
		// XXX: currently there's no smartness: position the cursor after the proposal
//		if (relativeOffset > 0 && proposal.charAt(relativeOffset - 1) == '}')
//			relativeOffset--;
		
		results.add(new CompletionProposal(proposal, documentOffset, 0, Math.max(0, relativeOffset), null, displayString, info, additionalInfo));
	}

	private IContextInformation createContextInformation(String proposalKey) {
		return null;
	}
	
	private String getString(String proposalKey, String type) {
		return EditorMessages.getString("FindReplace.regExContentAssist." + type + "." + proposalKey);  //$NON-NLS-1$//$NON-NLS-2$
	}
}
