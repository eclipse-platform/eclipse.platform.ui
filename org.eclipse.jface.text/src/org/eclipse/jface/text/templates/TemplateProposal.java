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
package org.eclipse.jface.text.templates;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;


/**
 * A template completion proposal.
 * <p>
 * Clients may subclass.</p>
 *
 * @since 3.0
 */
public class TemplateProposal implements ICompletionProposal, ICompletionProposalExtension, ICompletionProposalExtension2, ICompletionProposalExtension3 {

	private final Template fTemplate;
	private final TemplateContext fContext;
	private final Image fImage;
	private final IRegion fRegion;
	private int fRelevance;

	private IRegion fSelectedRegion; // initialized by apply()
	private String fDisplayString;
	private InclusivePositionUpdater fUpdater;
	private IInformationControlCreator fInformationControlCreator;

	/**
	 * Creates a template proposal with a template and its context.
	 *
	 * @param template  the template
	 * @param context   the context in which the template was requested.
	 * @param region	the region this proposal is applied to
	 * @param image     the icon of the proposal.
	 */
	public TemplateProposal(Template template, TemplateContext context, IRegion region, Image image) {
		this(template, context, region, image, 0);
	}

	/**
	 * Creates a template proposal with a template and its context.
	 *
	 * @param template  the template
	 * @param context   the context in which the template was requested.
	 * @param image     the icon of the proposal.
	 * @param region	the region this proposal is applied to
	 * @param relevance the relevance of the proposal
	 */
	public TemplateProposal(Template template, TemplateContext context, IRegion region, Image image, int relevance) {
		Assert.isNotNull(template);
		Assert.isNotNull(context);
		Assert.isNotNull(region);

		fTemplate= template;
		fContext= context;
		fImage= image;
		fRegion= region;

		fDisplayString= null;

		fRelevance= relevance;
	}

	/**
	 * Sets the information control creator for this completion proposal.
	 *
	 * @param informationControlCreator the information control creator
	 * @since 3.1
	 */
	public final void setInformationControlCreator(IInformationControlCreator informationControlCreator) {
		fInformationControlCreator= informationControlCreator;
	}

	/**
	 * Returns the template of this proposal.
	 *
	 * @return the template of this proposal
	 * @since 3.1
	 */
	protected final Template getTemplate() {
		return fTemplate;
	}

	/**
	 * Returns the context in which the template was requested.
	 *
	 * @return the context in which the template was requested
	 * @since 3.1
	 */
	protected final TemplateContext getContext() {
		return fContext;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This method is no longer called by the framework and clients should overwrite
	 *             {@link #apply(ITextViewer, char, int, int)} instead
	 */
	public final void apply(IDocument document) {
		// not called anymore
	}

	/**
	 * Inserts the template offered by this proposal into the viewer's document
	 * and sets up a <code>LinkedModeUI</code> on the viewer to edit any of
	 * the template's unresolved variables.
	 *
	 * @param viewer {@inheritDoc}
	 * @param trigger {@inheritDoc}
	 * @param stateMask {@inheritDoc}
	 * @param offset {@inheritDoc}
	 */
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

		IDocument document= viewer.getDocument();
		try {
			fContext.setReadOnly(false);
			int start;
			TemplateBuffer templateBuffer;
			{
				int oldReplaceOffset= getReplaceOffset();
				try {
					// this may already modify the document (e.g. add imports)
					templateBuffer= fContext.evaluate(fTemplate);
				} catch (TemplateException e1) {
					fSelectedRegion= fRegion;
					return;
				}

				start= getReplaceOffset();
				int shift= start - oldReplaceOffset;
				int end= Math.max(getReplaceEndOffset(), offset + shift);

				// insert template string
				String templateString= templateBuffer.getString();
				document.replace(start, end - start, templateString);
			}

			// translate positions
			LinkedModeModel model= new LinkedModeModel();
			TemplateVariable[] variables= templateBuffer.getVariables();
			boolean hasPositions= false;
			for (int i= 0; i != variables.length; i++) {
				TemplateVariable variable= variables[i];

				if (variable.isUnambiguous())
					continue;

				LinkedPositionGroup group= new LinkedPositionGroup();

				int[] offsets= variable.getOffsets();
				int length= variable.getLength();

				LinkedPosition first;
				{
					String[] values= variable.getValues();
					ICompletionProposal[] proposals= new ICompletionProposal[values.length];
					for (int j= 0; j < values.length; j++) {
						ensurePositionCategoryInstalled(document, model);
						Position pos= new Position(offsets[0] + start, length);
						document.addPosition(getCategory(), pos);
						proposals[j]= new PositionBasedCompletionProposal(values[j], pos, length);
					}

					if (proposals.length > 1)
						first= new ProposalPosition(document, offsets[0] + start, length, proposals);
					else
						first= new LinkedPosition(document, offsets[0] + start, length);
				}

				for (int j= 0; j != offsets.length; j++)
					if (j == 0)
						group.addPosition(first);
					else
						group.addPosition(new LinkedPosition(document, offsets[j] + start, length));

				model.addGroup(group);
				hasPositions= true;
			}

			if (hasPositions) {
				model.forceInstall();
				LinkedModeUI ui= new LinkedModeUI(model, viewer);
				ui.setExitPosition(viewer, getCaretOffset(templateBuffer) + start, 0, Integer.MAX_VALUE);
				ui.enter();

				fSelectedRegion= ui.getSelectedRegion();
			} else {
				ensurePositionCategoryRemoved(document);
				fSelectedRegion= new Region(getCaretOffset(templateBuffer) + start, 0);
			}

		} catch (BadLocationException e) {
			openErrorDialog(viewer.getTextWidget().getShell(), e);
			ensurePositionCategoryRemoved(document);
			fSelectedRegion= fRegion;
		} catch (BadPositionCategoryException e) {
			openErrorDialog(viewer.getTextWidget().getShell(), e);
			fSelectedRegion= fRegion;
		}

	}

	private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model) {
		if (!document.containsPositionCategory(getCategory())) {
			document.addPositionCategory(getCategory());
			fUpdater= new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(fUpdater);

			model.addLinkingListener(new ILinkedModeListener() {

				/*
				 * @see org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel, int)
				 */
				public void left(LinkedModeModel environment, int flags) {
					ensurePositionCategoryRemoved(document);
				}

				public void suspend(LinkedModeModel environment) {}
				public void resume(LinkedModeModel environment, int flags) {}
			});
		}
	}

	private void ensurePositionCategoryRemoved(IDocument document) {
		if (document.containsPositionCategory(getCategory())) {
			try {
				document.removePositionCategory(getCategory());
			} catch (BadPositionCategoryException e) {
				// ignore
			}
			document.removePositionUpdater(fUpdater);
		}
	}

	private String getCategory() {
		return "TemplateProposalCategory_" + toString(); //$NON-NLS-1$
	}

	private int getCaretOffset(TemplateBuffer buffer) {

	    TemplateVariable[] variables= buffer.getVariables();
		for (int i= 0; i != variables.length; i++) {
			TemplateVariable variable= variables[i];
			if (variable.getType().equals(GlobalTemplateVariables.Cursor.NAME))
				return variable.getOffsets()[0];
		}

		return buffer.getString().length();
	}

	/**
	 * Returns the offset of the range in the document that will be replaced by
	 * applying this template.
	 *
	 * @return the offset of the range in the document that will be replaced by
	 *         applying this template
	 * @since 3.1
	 */
	protected final int getReplaceOffset() {
		int start;
		if (fContext instanceof DocumentTemplateContext) {
			DocumentTemplateContext docContext = (DocumentTemplateContext)fContext;
			start= docContext.getStart();
		} else {
			start= fRegion.getOffset();
		}
		return start;
	}

	/**
	 * Returns the end offset of the range in the document that will be replaced
	 * by applying this template.
	 *
	 * @return the end offset of the range in the document that will be replaced
	 *         by applying this template
	 * @since 3.1
	 */
	protected final int getReplaceEndOffset() {
		int end;
		if (fContext instanceof DocumentTemplateContext) {
			DocumentTemplateContext docContext = (DocumentTemplateContext)fContext;
			end= docContext.getEnd();
		} else {
			end= fRegion.getOffset() + fRegion.getLength();
		}
		return end;
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document) {
		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo() {
	    try {
		    fContext.setReadOnly(true);
			TemplateBuffer templateBuffer;
			try {
				templateBuffer= fContext.evaluate(fTemplate);
			} catch (TemplateException e) {
				return null;
			}

			return templateBuffer.getString();

	    } catch (BadLocationException e) {
			return null;
		}
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		if (fDisplayString == null) {
			String[] arguments= new String[] { fTemplate.getName(), fTemplate.getDescription() };
			fDisplayString= JFaceTextTemplateMessages.getFormattedString("TemplateProposal.displayString", arguments); //$NON-NLS-1$
		}
		return fDisplayString;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	public IContextInformation getContextInformation() {
		return null;
	}

	private void openErrorDialog(Shell shell, Exception e) {
		MessageDialog.openError(shell, JFaceTextTemplateMessages.getString("TemplateProposal.errorDialog.title"), e.getMessage()); //$NON-NLS-1$
	}

	/**
	 * Returns the relevance.
	 *
	 * @return the relevance
	 */
	public int getRelevance() {
		return fRelevance;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getInformationControlCreator()
	 */
	public IInformationControlCreator getInformationControlCreator() {
		return fInformationControlCreator;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer, boolean)
	 */
	public void selected(ITextViewer viewer, boolean smartToggle) {
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
	 */
	public void unselected(ITextViewer viewer) {
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
	 */
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		try {
			int replaceOffset= getReplaceOffset();
			if (offset >= replaceOffset) {
				String content= document.get(replaceOffset, offset - replaceOffset);
				return fTemplate.getName().toLowerCase().startsWith(content.toLowerCase());
			}
		} catch (BadLocationException e) {
			// concurrent modification - ignore
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getPrefixCompletionText(org.eclipse.jface.text.IDocument, int)
	 */
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset) {
		return fTemplate.getName();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getPrefixCompletionStart(org.eclipse.jface.text.IDocument, int)
	 */
	public int getPrefixCompletionStart(IDocument document, int completionOffset) {
		return getReplaceOffset();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This method is no longer called by the framework and clients should overwrite
	 *             {@link #apply(ITextViewer, char, int, int)} instead
	 */
	public void apply(IDocument document, char trigger, int offset) {
		// not called any longer
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#isValidFor(org.eclipse.jface.text.IDocument, int)
	 */
	public boolean isValidFor(IDocument document, int offset) {
		// not called any longer
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getTriggerCharacters()
	 */
	public char[] getTriggerCharacters() {
		// no triggers
		return new char[0];
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension#getContextInformationPosition()
	 */
	public int getContextInformationPosition() {
		return fRegion.getOffset();
	}
}
