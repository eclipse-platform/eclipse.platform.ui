/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;


/**
 * A completion processor that computes template proposals. Subclasses need to
 * provide implementations for {@link #getTemplates(String)},
 * {@link #getContextType(ITextViewer, IRegion)} and {@link #getImage(Template)}.
 *
 * @since 3.0
 */
public abstract class TemplateCompletionProcessor implements IContentAssistProcessor {

	private static final class ProposalComparator implements Comparator<ICompletionProposal> {
		@Override
		public int compare(ICompletionProposal o1, ICompletionProposal o2) {
			int r1= o1 instanceof TemplateProposal t ? t.getRelevance() : 0;
			int r2= o2 instanceof TemplateProposal t ? t.getRelevance() : 0;
			return r2 - r1;
		}
	}

	private static final Comparator<ICompletionProposal> fgProposalComparator= new ProposalComparator();

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

		// adjust offset to end of normalized selection
		if (selection.getOffset() == offset)
			offset= selection.getOffset() + selection.getLength();

		String prefix= extractPrefix(viewer, offset);
		Region region= new Region(offset - prefix.length(), prefix.length());
		TemplateContext context= createContext(viewer, region);
		if (context == null)
			return new ICompletionProposal[0];

		context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

		Template[] templates= getTemplates(context.getContextType().getId());

		List<ICompletionProposal> matches= new ArrayList<>();
		for (Template template : templates) {
			try {
				context.getContextType().validate(template.getPattern());
			} catch (TemplateException e) {
				continue;
			}
			if (template.matches(prefix, context.getContextType().getId()))
				matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
		}

		Collections.sort(matches, fgProposalComparator);

		return matches.toArray(new ICompletionProposal[matches.size()]);
	}

	/**
	 * Creates a new proposal.
	 * <p>
	 * Forwards to {@link #createProposal(Template, TemplateContext, IRegion, int)}.
	 * Do neither call nor override.
	 * </p>
	 *
	 * @param template the template to be applied by the proposal
	 * @param context the context for the proposal
	 * @param region the region the proposal applies to
	 * @param relevance the relevance of the proposal
	 * @return a new <code>ICompletionProposal</code> for
	 *         <code>template</code>
	 * @deprecated use the version specifying <code>IRegion</code> as third parameter
	 * @since 3.1
	 */
	@Deprecated
	protected ICompletionProposal createProposal(Template template, TemplateContext context, Region region, int relevance) {
		return createProposal(template, context, (IRegion) region, relevance);
	}

	/**
	 * Creates a new proposal.
	 * <p>
	 * The default implementation returns an instance of
	 * {@link TemplateProposal}. Subclasses may replace this method to provide
	 * their own implementations.
	 * </p>
	 *
	 * @param template the template to be applied by the proposal
	 * @param context the context for the proposal
	 * @param region the region the proposal applies to
	 * @param relevance the relevance of the proposal
	 * @return a new <code>ICompletionProposal</code> for
	 *         <code>template</code>
	 */
	protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region, int relevance) {
		return new TemplateProposal(template, context, region, getImage(template), relevance);
	}

	/**
	 * Returns the templates valid for the context type specified by <code>contextTypeId</code>.
	 *
	 * @param contextTypeId the context type id
	 * @return the templates valid for this context type id
	 */
	protected abstract Template[] getTemplates(String contextTypeId);

	/**
	 * Creates a concrete template context for the given region in the document. This involves finding out which
	 * context type is valid at the given location, and then creating a context of this type. The default implementation
	 * returns a <code>DocumentTemplateContext</code> for the context type at the given location.
	 *
	 * @param viewer the viewer for which the context is created
	 * @param region the region into <code>document</code> for which the context is created
	 * @return a template context that can handle template insertion at the given location, or <code>null</code>
	 */
	protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
		TemplateContextType contextType= getContextType(viewer, region);
		if (contextType != null) {
			IDocument document= viewer.getDocument();
			return new DocumentTemplateContext(contextType, document, region.getOffset(), region.getLength());
		}
		return null;
	}

	/**
	 * Returns the context type that can handle template insertion at the given region
	 * in the viewer's document.
	 *
	 * @param viewer the text viewer
	 * @param region the region into the document displayed by viewer
	 * @return the context type that can handle template expansion for the given location, or <code>null</code> if none exists
	 */
	protected abstract TemplateContextType getContextType(ITextViewer viewer, IRegion region);

	/**
	 * Returns the relevance of a template given a prefix. The default
	 * implementation returns a number greater than zero if the template name
	 * starts with the prefix, and zero otherwise.
	 *
	 * @param template the template to compute the relevance for
	 * @param prefix the prefix after which content assist was requested
	 * @return the relevance of <code>template</code>
	 * @see #extractPrefix(ITextViewer, int)
	 */
	protected int getRelevance(Template template, String prefix) {
		if (template.getName().startsWith(prefix))
			return 90;
		return 0;
	}

	/**
	 * Heuristically extracts the prefix used for determining template relevance
	 * from the viewer's document. The default implementation returns the String from
	 * offset backwards that forms a java identifier.
	 *
	 * @param viewer the viewer
	 * @param offset offset into document
	 * @return the prefix to consider
	 * @see #getRelevance(Template, String)
	 */
	protected String extractPrefix(ITextViewer viewer, int offset) {
		int i= offset;
		IDocument document= viewer.getDocument();
		if (i > document.getLength())
			return ""; //$NON-NLS-1$

		try {
			while (i > 0) {
				char ch= document.getChar(i - 1);
				if (!Character.isJavaIdentifierPart(ch))
					break;
				i--;
			}

			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Returns the image to be used for the proposal for <code>template</code>.
	 *
	 * @param template the template for which an image should be returned
	 * @return the image for <code>template</code>
	 */
	protected abstract Image getImage(Template template);

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		return null;
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
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
}
