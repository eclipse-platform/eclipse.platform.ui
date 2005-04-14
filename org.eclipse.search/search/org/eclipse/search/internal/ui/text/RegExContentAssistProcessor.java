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
			{ "\\", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\i"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\i") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		};
	}
	
	private final String[][] getFindProposals() {
		return new String[][] {
			{ "\\\\", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\\\"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\\\") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\0", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\0"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\0") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\x", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\x"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\x") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\u", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\u"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\u") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\t", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\t"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\t") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\n", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\n"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\n") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\r", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\r"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\r") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\f", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\f"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\f") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\a", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\a"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\a") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\e", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\e"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\e") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\c", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\c"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\c") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ ".", SearchMessages.getString("FindReplace.regExContentAssist.displayString.."), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo..") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\d", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\d"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\d") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\D", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\D"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\D") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\s", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\s"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\s") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\S", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\S"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\S") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\w", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\w"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\w") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\W", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\W"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\W") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "^", SearchMessages.getString("FindReplace.regExContentAssist.displayString.^"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.^") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "$", SearchMessages.getString("FindReplace.regExContentAssist.displayString.$"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.$") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\b", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\b"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\b") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\B", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\B"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\B") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\A", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\A"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\A") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\G", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\G"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\G") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\z", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\z"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\z") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "*", SearchMessages.getString("FindReplace.regExContentAssist.displayString.*"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.*") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{}", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n}"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n}") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,}"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,}") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,m}"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,m}") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "??", SearchMessages.getString("FindReplace.regExContentAssist.displayString.??"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.??") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "*?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.*?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.*?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "+?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.+?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.+?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{}?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n}?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n}?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,}?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,}?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,m}?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,m}?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "?+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.?+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.?+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "*+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.*+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.*+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "++", SearchMessages.getString("FindReplace.regExContentAssist.displayString.++"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.++") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{}+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n}+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n}+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,}+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,}+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,m}+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,m}+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "", SearchMessages.getString("FindReplace.regExContentAssist.displayString.UV"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.UV") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "|", SearchMessages.getString("FindReplace.regExContentAssist.displayString.U|V"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.U|V") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "()", SearchMessages.getString("FindReplace.regExContentAssist.displayString.(U)"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.(U)") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\Q", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\Q"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\Q") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\E", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\E"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\E") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "[]", SearchMessages.getString("FindReplace.regExContentAssist.displayString.[ecq]"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.[ecq]") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "^", SearchMessages.getString("FindReplace.regExContentAssist.displayString.[^ecq]"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.[^ecq]") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "[]", SearchMessages.getString("FindReplace.regExContentAssist.displayString.[e-q]"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.[e-q]") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "&&", SearchMessages.getString("FindReplace.regExContentAssist.displayString.&&"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.&&") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\\\", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\\\"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\\\") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\0", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\0"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\0") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\x", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\x"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\x") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\u", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\u"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\u") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\t", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\t"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\t") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\n", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\n"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\n") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\r", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\r"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\r") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\f", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\f"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\f") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\a", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\a"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\a") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\e", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\e"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\e") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\c", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\c"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\c") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ ".", SearchMessages.getString("FindReplace.regExContentAssist.displayString.."), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo..") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\d", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\d"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\d") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\D", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\D"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\D") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\s", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\s"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\s") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\S", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\S"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\S") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\w", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\w"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\w") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\W", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\W"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\W") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "^", SearchMessages.getString("FindReplace.regExContentAssist.displayString.^"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.^") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "$", SearchMessages.getString("FindReplace.regExContentAssist.displayString.$"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.$") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\b", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\b"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\b") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\B", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\B"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\B") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\A", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\A"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\A") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\G", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\G"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\G") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\z", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\z"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\z") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "*", SearchMessages.getString("FindReplace.regExContentAssist.displayString.*"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.*") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{}", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n}"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n}") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,}"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,}") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,m}"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,m}") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "??", SearchMessages.getString("FindReplace.regExContentAssist.displayString.??"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.??") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "*?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.*?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.*?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "+?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.+?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.+?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{}?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n}?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n}?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,}?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,}?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}?", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,m}?"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,m}?") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "?+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.?+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.?+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "*+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.*+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.*+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "++", SearchMessages.getString("FindReplace.regExContentAssist.displayString.++"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.++") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{}+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n}+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n}+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,}+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,}+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "{,}+", SearchMessages.getString("FindReplace.regExContentAssist.displayString.{n,m}+"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.{n,m}+") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "", SearchMessages.getString("FindReplace.regExContentAssist.displayString.UV"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.UV") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "|", SearchMessages.getString("FindReplace.regExContentAssist.displayString.U|V"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.U|V") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "()", SearchMessages.getString("FindReplace.regExContentAssist.displayString.(U)"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.(U)") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\i"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\i") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\Q", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\Q"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\Q") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "\\E", SearchMessages.getString("FindReplace.regExContentAssist.displayString.\\E"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.\\E") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "[]", SearchMessages.getString("FindReplace.regExContentAssist.displayString.[ecq]"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.[ecq]") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "^", SearchMessages.getString("FindReplace.regExContentAssist.displayString.[^ecq]"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.[^ecq]") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "[]", SearchMessages.getString("FindReplace.regExContentAssist.displayString.[e-q]"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.[e-q]") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			{ "&&", SearchMessages.getString("FindReplace.regExContentAssist.displayString.&&"), SearchMessages.getString("FindReplace.regExContentAssist.additionalInfo.&&") },//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
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
