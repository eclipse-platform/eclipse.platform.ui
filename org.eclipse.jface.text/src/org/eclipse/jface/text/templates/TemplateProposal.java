/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.text.Assert;
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
import org.eclipse.jface.text.link.ILinkedListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedEnvironment;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.LinkedUIControl;
import org.eclipse.jface.text.link.ProposalPosition;


/**
 * A template proposal. 
 * 
 * XXX This is work in progress. 
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
		
	/**
	 * Creates a template proposal with a template and its context.
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

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public final void apply(IDocument document) {
		// not called anymore
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
	 */
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

		try {
			fContext.setReadOnly(false);
			TemplateBuffer templateBuffer= fContext.evaluate(fTemplate);
			if (templateBuffer == null) {
				fSelectedRegion= fRegion;
				return;
			}
			
			int start, end;
			if (fContext instanceof DocumentTemplateContext) {
				DocumentTemplateContext docContext = (DocumentTemplateContext)fContext;
				start= docContext.getStart();
				end= docContext.getEnd();
			} else {
				start= fRegion.getOffset();
				end= start + fRegion.getLength();
			}
			
			// insert template string
			IDocument document= viewer.getDocument();
			String templateString= templateBuffer.getString();	
			document.replace(start, end - start, templateString);	
			
			
			// translate positions
			LinkedEnvironment env= new LinkedEnvironment();
			TemplateVariable[] variables= templateBuffer.getVariables();
			boolean hasPositions= false;
			for (int i= 0; i != variables.length; i++) {
				TemplateVariable variable= variables[i];

				if (variable.isUnambiguous())
					continue;
				
				LinkedPositionGroup group= new LinkedPositionGroup();
				
				int[] offsets= variable.getOffsets();
				int length= variable.getLength();

				String[] values= variable.getValues();
				ICompletionProposal[] proposals= new ICompletionProposal[values.length];
				for (int j= 0; j < values.length; j++) {
					ensurePositionCategoryInstalled(document, env);
					Position pos= new Position(offsets[0] + start, length);
					document.addPosition(getCategory(), pos);
					proposals[j]= new PositionBasedCompletionProposal(values[j], pos, length);
				}
				
				for (int j= 0; j != offsets.length; j++)
					if (j == 0 && proposals.length > 1)
						group.addPosition(new ProposalPosition(document, offsets[j] + start, length, proposals));
					else
						group.addPosition(new LinkedPosition(document, offsets[j] + start, length));
				
				env.addGroup(group);
				hasPositions= true;
			}
				
			if (hasPositions) {
				env.forceInstall();
				LinkedUIControl editor= new LinkedUIControl(env, viewer);
				editor.setExitPosition(viewer, getCaretOffset(templateBuffer) + start, 0, Integer.MAX_VALUE);
				editor.enter();
				
				fSelectedRegion= editor.getSelectedRegion();
			} else
				fSelectedRegion= new Region(getCaretOffset(templateBuffer) + start, 0);
			
		} catch (BadLocationException e) {
			openErrorDialog(viewer.getTextWidget().getShell(), e);		    
			fSelectedRegion= fRegion;
		} catch (BadPositionCategoryException e) {
			openErrorDialog(viewer.getTextWidget().getShell(), e);		    
			fSelectedRegion= fRegion;
		}

	}	
	
	private void ensurePositionCategoryInstalled(final IDocument document, LinkedEnvironment env) {
		if (!document.containsPositionCategory(getCategory())) {
			document.addPositionCategory(getCategory());
			final InclusivePositionUpdater updater= new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(updater);
			
			env.addLinkedListener(new ILinkedListener() {

				/*
				 * @see org.eclipse.jface.text.link.ILinkedListener#left(org.eclipse.jface.text.link.LinkedEnvironment, int)
				 */
				public void left(LinkedEnvironment environment, int flags) {
					try {
						document.removePositionCategory(getCategory());
					} catch (BadPositionCategoryException e) {
						// ignore
					}
					document.removePositionUpdater(updater);
				}

				public void suspend(LinkedEnvironment environment) {}
				public void resume(LinkedEnvironment environment, int flags) {}
			});
		}
	}

	private String getCategory() {
		return "TemplateProposalCategory_" + toString(); //$NON-NLS-1$
	}

	private int getCaretOffset(TemplateBuffer buffer) {
	
	    TemplateVariable[] variables= buffer.getVariables();
		for (int i= 0; i != variables.length; i++) {
			TemplateVariable variable= variables[i];
			if (variable.getType().equals(GlobalVariables.Cursor.NAME))
				return variable.getOffsets()[0];
		}

		return buffer.getString().length();
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
			TemplateBuffer templateBuffer= fContext.evaluate(fTemplate);
			
			if (templateBuffer == null)
				return null;

			return templateBuffer.getString();

	    } catch (BadLocationException e) {
	    	// FIXME do something
			return null;
		}
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		if (fDisplayString == null) {
			fDisplayString= fTemplate.getName() + TemplateMessages.getString("TemplateProposal.delimiter") + fTemplate.getDescription(); //$NON-NLS-1$
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
		MessageDialog.openError(shell, TemplateMessages.getString("TemplateEvaluator.error.title"), e.getMessage()); //$NON-NLS-1$
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
//		return new TemplateInformationControlCreator();
		return null;
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
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementString()
	 */
	public CharSequence getCompletionText() {
		return fTemplate.getName();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension3#getReplacementOffset()
	 */
	public int getCompletionOffset() {
		return fRegion.getOffset();
	}

	/**
	 * {@inheritdoc}
	 */
	public void apply(IDocument document, char trigger, int offset) {
		// not called any longer		
	}

	/**
	 * {@inheritdoc}
	 */
	public boolean isValidFor(IDocument document, int offset) {
		// not called any longer
		return false;
	}

	/**
	 * {@inheritdoc}
	 */
	public char[] getTriggerCharacters() {
		// no triggers
		return new char[0];
	}

	/**
	 * {@inheritdoc}
	 */
	public int getContextInformationPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateReplacementOffset(int offset) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReplacementString() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateReplacementLength(int length) {
		// TODO Auto-generated method stub
		
	}
}
