/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContextInformationValidator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import org.eclipse.search.internal.ui.SearchMessages;

/**
 * Content assist processor for regular expressions.
 * 
 * @since 3.0
 */
final class RegExContentAssistProcessor implements IContentAssistProcessor, ISubjectControlContentAssistProcessor {
	
	private final String[][] getReplaceProposals() {
		return new String[][] {
			{ "\\", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashi, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashi }, //$NON-NLS-1$
		};
	}
	
	private final String[][] getFindProposals() {
		return new String[][] {
			{ "\\\\", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslash, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash }, //$NON-NLS-1$
			{ "\\0", SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash0, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash0 }, //$NON-NLS-1$
			{ "\\x", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashx, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashx }, //$NON-NLS-1$
			{ "\\u", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashu, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashu }, //$NON-NLS-1$
			{ "\\t", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslasht, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslasht }, //$NON-NLS-1$
			{ "\\n", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashn, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashn }, //$NON-NLS-1$
			{ "\\r", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashr, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashr }, //$NON-NLS-1$
			{ "\\f", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashf, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashf }, //$NON-NLS-1$
			{ "\\a", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslasha, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslasha }, //$NON-NLS-1$
			{ "\\e", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashe, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashe }, //$NON-NLS-1$
			{ "\\c", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashc, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashc }, //$NON-NLS-1$
			{ ".", SearchMessages.FindReplace_regExContentAssist_displayString__, SearchMessages.FindReplace_regExContentAssist_additionalInfo__ }, //$NON-NLS-1$
			{ "\\d", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashd, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashd }, //$NON-NLS-1$
			{ "\\D", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashD, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashD }, //$NON-NLS-1$
			{ "\\s", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashs, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashs }, //$NON-NLS-1$
			{ "\\S", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashS, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashS }, //$NON-NLS-1$
			{ "\\w", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashw, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashw }, //$NON-NLS-1$
			{ "\\W", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashW, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashW }, //$NON-NLS-1$
			{ "^", SearchMessages.FindReplace_regExContentAssist_displayString__power, SearchMessages.FindReplace_regExContentAssist_additionalInfo__power }, //$NON-NLS-1$
			{ "$", SearchMessages.FindReplace_regExContentAssist_displayString_$, SearchMessages.FindReplace_regExContentAssist_additionalInfo_$ }, //$NON-NLS-1$
			{ "\\b", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashb, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashb }, //$NON-NLS-1$
			{ "\\B", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashB, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashB }, //$NON-NLS-1$
			{ "\\A", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashA, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashA }, //$NON-NLS-1$
			{ "\\G", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashG, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashG }, //$NON-NLS-1$
			{ "\\z", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashz, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashz }, //$NON-NLS-1$
			{ "?", SearchMessages.FindReplace_regExContentAssist_displayString__question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__question }, //$NON-NLS-1$
			{ "*", SearchMessages.FindReplace_regExContentAssist_displayString__star, SearchMessages.FindReplace_regExContentAssist_additionalInfo__star }, //$NON-NLS-1$
			{ "+", SearchMessages.FindReplace_regExContentAssist_displayString__plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__plus }, //$NON-NLS-1$
			{ "{}", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_rbrace, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_rbrace }, //$NON-NLS-1$
			{ "{,}", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_comma_rbrace, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_comma_rbrace }, //$NON-NLS-1$
			{ "{,}", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_commam_rbrace, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_commam_rbrace }, //$NON-NLS-1$
			{ "??", SearchMessages.FindReplace_regExContentAssist_displayString__question_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__question_question }, //$NON-NLS-1$
			{ "*?", SearchMessages.FindReplace_regExContentAssist_displayString__star_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__star_question }, //$NON-NLS-1$
			{ "+?", SearchMessages.FindReplace_regExContentAssist_displayString__plus_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__plus_question }, //$NON-NLS-1$
			{ "{}?", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_rbrace_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_rbrace_question }, //$NON-NLS-1$
			{ "{,}?", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_comma_rbrace_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_comma_rbrace_question }, //$NON-NLS-1$
			{ "{,}?", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_commam_rbrace_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_commam_rbrace_question }, //$NON-NLS-1$
			{ "?+", SearchMessages.FindReplace_regExContentAssist_displayString__question_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__question_plus }, //$NON-NLS-1$
			{ "*+", SearchMessages.FindReplace_regExContentAssist_displayString__star_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__star_plus }, //$NON-NLS-1$
			{ "++", SearchMessages.FindReplace_regExContentAssist_displayString__plus_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__plus_plus }, //$NON-NLS-1$
			{ "{}+", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_rbrace_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_rbrace_plus }, //$NON-NLS-1$
			{ "{,}+", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_comma_rbrace_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_comma_rbrace_plus }, //$NON-NLS-1$
			{ "{,}+", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_commam_rbrace_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_commam_rbrace_plus }, //$NON-NLS-1$
			{ "", SearchMessages.FindReplace_regExContentAssist_displayString_UV, SearchMessages.FindReplace_regExContentAssist_additionalInfo_UV }, //$NON-NLS-1$
			{ "|", SearchMessages.FindReplace_regExContentAssist_displayString_U_barV, SearchMessages.FindReplace_regExContentAssist_additionalInfo_U_barV }, //$NON-NLS-1$
			{ "()", SearchMessages.FindReplace_regExContentAssist_displayString__lparenU_rparen, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lparenU_rparen }, //$NON-NLS-1$
			{ "\\", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslash, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash }, //$NON-NLS-1$
			{ "\\Q", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashQ, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashQ }, //$NON-NLS-1$
			{ "\\E", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashE, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashE }, //$NON-NLS-1$
			{ "[]", SearchMessages.FindReplace_regExContentAssist_displayString__lbracketecq_rbracket, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracketecq_rbracket }, //$NON-NLS-1$
			{ "^", SearchMessages.FindReplace_regExContentAssist_displayString__lbracket_powerecq_rbracket, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracket_powerecq_rbracket }, //$NON-NLS-1$
			{ "[]", SearchMessages.FindReplace_regExContentAssist_displayString__lbrackete_q_rbracket, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbrackete_q_rbracket }, //$NON-NLS-1$
			{ "&&", SearchMessages.FindReplace_regExContentAssist_displayString__amp_amp, SearchMessages.FindReplace_regExContentAssist_additionalInfo__amp_amp }, //$NON-NLS-1$
			{ "\\\\", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslash, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash }, //$NON-NLS-1$
			{ "\\0", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslash0, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash0 }, //$NON-NLS-1$
			{ "\\x", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashx, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashx }, //$NON-NLS-1$
			{ "\\u", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashu, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashu }, //$NON-NLS-1$
			{ "\\t", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslasht, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslasht }, //$NON-NLS-1$
			{ "\\n", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashn, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashn }, //$NON-NLS-1$
			{ "\\r", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashr, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashr }, //$NON-NLS-1$
			{ "\\f", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashf, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashf }, //$NON-NLS-1$
			{ "\\a", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslasha, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslasha }, //$NON-NLS-1$
			{ "\\e", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashe, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashe }, //$NON-NLS-1$
			{ "\\c", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashc, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashc }, //$NON-NLS-1$
			{ ".", SearchMessages.FindReplace_regExContentAssist_displayString__, SearchMessages.FindReplace_regExContentAssist_additionalInfo__ }, //$NON-NLS-1$
			{ "\\d", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashd, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashd }, //$NON-NLS-1$
			{ "\\D", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashD, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashD }, //$NON-NLS-1$
			{ "\\s", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashs, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashs }, //$NON-NLS-1$
			{ "\\S", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashS, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashS }, //$NON-NLS-1$
			{ "\\w", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashw, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashw }, //$NON-NLS-1$
			{ "\\W", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashW, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashW }, //$NON-NLS-1$
			{ "^", SearchMessages.FindReplace_regExContentAssist_displayString__power, SearchMessages.FindReplace_regExContentAssist_additionalInfo__power }, //$NON-NLS-1$
			{ "$", SearchMessages.FindReplace_regExContentAssist_displayString_$, SearchMessages.FindReplace_regExContentAssist_additionalInfo_$ }, //$NON-NLS-1$
			{ "\\b", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashb, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashb }, //$NON-NLS-1$
			{ "\\B", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashB, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashB }, //$NON-NLS-1$
			{ "\\A", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashA, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashA }, //$NON-NLS-1$
			{ "\\G", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashG, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashG }, //$NON-NLS-1$
			{ "\\z", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashz, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashz }, //$NON-NLS-1$
			{ "?", SearchMessages.FindReplace_regExContentAssist_displayString__question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__question }, //$NON-NLS-1$
			{ "*", SearchMessages.FindReplace_regExContentAssist_displayString__star, SearchMessages.FindReplace_regExContentAssist_additionalInfo__star }, //$NON-NLS-1$
			{ "+", SearchMessages.FindReplace_regExContentAssist_displayString__plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__plus }, //$NON-NLS-1$
			{ "{}", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_rbrace, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_rbrace }, //$NON-NLS-1$
			{ "{,}", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_comma_rbrace, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_comma_rbrace }, //$NON-NLS-1$
			{ "{,}", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_commam_rbrace, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_commam_rbrace }, //$NON-NLS-1$
			{ "??", SearchMessages.FindReplace_regExContentAssist_displayString__question_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__question_question }, //$NON-NLS-1$
			{ "*?", SearchMessages.FindReplace_regExContentAssist_displayString__star_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__star_question }, //$NON-NLS-1$
			{ "+?", SearchMessages.FindReplace_regExContentAssist_displayString__plus_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__plus_question }, //$NON-NLS-1$
			{ "{}?", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_rbrace_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_rbrace_question }, //$NON-NLS-1$
			{ "{,}?", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_comma_rbrace_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_comma_rbrace_question }, //$NON-NLS-1$
			{ "{,}?", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_commam_rbrace_question, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_commam_rbrace_question }, //$NON-NLS-1$
			{ "?+", SearchMessages.FindReplace_regExContentAssist_displayString__question_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__question_plus }, //$NON-NLS-1$
			{ "*+", SearchMessages.FindReplace_regExContentAssist_displayString__star_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__star_plus }, //$NON-NLS-1$
			{ "++", SearchMessages.FindReplace_regExContentAssist_displayString__plus_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__plus_plus }, //$NON-NLS-1$
			{ "{}+", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_rbrace_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_rbrace_plus }, //$NON-NLS-1$
			{ "{,}+", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_comma_rbrace_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_comma_rbrace_plus }, //$NON-NLS-1$
			{ "{,}+", SearchMessages.FindReplace_regExContentAssist_displayString__lbracen_commam_rbrace_plus, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracen_commam_rbrace_plus }, //$NON-NLS-1$
			{ "", SearchMessages.FindReplace_regExContentAssist_displayString_UV, SearchMessages.FindReplace_regExContentAssist_additionalInfo_UV }, //$NON-NLS-1$
			{ "|", SearchMessages.FindReplace_regExContentAssist_displayString_U_barV, SearchMessages.FindReplace_regExContentAssist_additionalInfo_U_barV }, //$NON-NLS-1$
			{ "()", SearchMessages.FindReplace_regExContentAssist_displayString__lparenU_rparen, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lparenU_rparen }, //$NON-NLS-1$
			{ "\\", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashi, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashi }, //$NON-NLS-1$
			{ "\\", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslash, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslash }, //$NON-NLS-1$
			{ "\\Q", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashQ, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashQ }, //$NON-NLS-1$
			{ "\\E", SearchMessages.FindReplace_regExContentAssist_displayString__bslash_bslashE, SearchMessages.FindReplace_regExContentAssist_additionalInfo__bslash_bslashE }, //$NON-NLS-1$
			{ "[]", SearchMessages.FindReplace_regExContentAssist_displayString__lbracketecq_rbracket, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracketecq_rbracket }, //$NON-NLS-1$
			{ "^", SearchMessages.FindReplace_regExContentAssist_displayString__lbracket_powerecq_rbracket, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbracket_powerecq_rbracket }, //$NON-NLS-1$
			{ "[]", SearchMessages.FindReplace_regExContentAssist_displayString__lbrackete_q_rbracket, SearchMessages.FindReplace_regExContentAssist_additionalInfo__lbrackete_q_rbracket }, //$NON-NLS-1$
			{ "&&", SearchMessages.FindReplace_regExContentAssist_displayString__amp_amp, SearchMessages.FindReplace_regExContentAssist_additionalInfo__amp_amp }, //$NON-NLS-1$
		};
	}
	
	/**
	 * The context information validator.
	 */
	private IContextInformationValidator fValidator= new SubjectControlContextInformationValidator(this);

	private final boolean fIsFind;
	
	public RegExContentAssistProcessor(boolean isFind) {
		fIsFind= isFind;
	}
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
		String[][] proposals= fIsFind ? getFindProposals() : getReplaceProposals();
		
		List results= new ArrayList(proposals.length);
		for (int i= 0; i < proposals.length; i++) {
			String[] curr= proposals[i];
			addProposal(curr[0], curr[1], curr[2], contentAssistSubjectControl, documentOffset, results, true);
		}
		if (results.isEmpty()) {
			for (int i= 0; i < proposals.length; i++) {
				String[] curr= proposals[i];
				addProposal(curr[0], curr[1], curr[2], contentAssistSubjectControl, documentOffset, results, false);
			}
		}
		return (ICompletionProposal[])results.toArray(new ICompletionProposal[results.size()]);
	}

	/*
	 * @see ISubjectControlContentAssistProcessor#computeContextInformation(IContentAssistSubjectControl, int)
	 */
	public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
		return null;
	}
			
	private void addProposal(String proposal, String displayString, String additionalInfo, IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset, List results, boolean filter) {
		
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

//		// Move cursor on to the left if the proposal ends with '}'
		int relativeOffset= proposal.length();
		// XXX: currently there's no smartness: position the cursor after the proposal
//		if (relativeOffset > 0 && proposal.charAt(relativeOffset - 1) == '}')
//			relativeOffset--;
		
		results.add(new CompletionProposal(proposal, documentOffset, 0, Math.max(0, relativeOffset), null, displayString, null, additionalInfo));
	}
}
