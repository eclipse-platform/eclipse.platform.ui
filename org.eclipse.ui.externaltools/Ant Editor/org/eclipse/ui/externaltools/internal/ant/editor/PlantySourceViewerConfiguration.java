/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug 24108
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

import org.eclipse.ui.externaltools.internal.ant.editor.derived.HTMLTextPresenter;
import org.eclipse.ui.externaltools.internal.ant.editor.text.XMLAnnotationHover;
import org.eclipse.ui.externaltools.internal.ant.editor.text.XMLTextHover;
import org.eclipse.ui.externaltools.internal.ant.editor.text.NonRuleBasedDamagerRepairer;
import org.eclipse.ui.externaltools.internal.ant.editor.text.IAntEditorColorConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.text.NotifyingReconciler;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyPartitionScanner;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyProcInstrScanner;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyTagScanner;
import org.eclipse.ui.externaltools.internal.ant.editor.text.XMLReconcilingStrategy;

/**
 * The source viewer configuration for Planty.
 */
public class PlantySourceViewerConfiguration extends SourceViewerConfiguration {

    private PlantyTagScanner tagScanner;
    private PlantyProcInstrScanner instructionScanner;
	private NonRuleBasedDamagerRepairer damageRepairer;
        
    private PlantyEditor fEditor;

    private XMLTextHover fTextHover;
    
    /**
     * Creates an instance with the specified color manager.
     */
    public PlantySourceViewerConfiguration(PlantyEditor editor) {
	    super();
	    fEditor= editor;
    }    

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
     */
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant= new ContentAssistant();
        PlantyCompletionProcessor tempProcessor = new PlantyCompletionProcessor(); 
        assistant.setContentAssistProcessor(tempProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(tempProcessor, PlantyPartitionScanner.XML_TAG);
		assistant.enableAutoActivation(true);
		
        IInformationControlCreator creator = getInformationControlCreator(true);
        assistant.setInformationControlCreator(creator);

        return assistant;
    }


    protected IInformationControlCreator getInformationControlCreator(final boolean cutDown) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                int style= cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
                return new DefaultInformationControl(parent, style, new HTMLTextPresenter(cutDown));
            }
        };
    }

    /* (non-Javadoc)
     * Method declared on SourceViewerConfiguration
     */
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            PlantyPartitionScanner.XML_COMMENT,
            PlantyPartitionScanner.XML_TAG };
    }
    
    /**
     * @return TAB_WIDTH
     * @see SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
     */
    public int getTabWidth(ISourceViewer sourceViewer) {
        return PlantyEditor.TAB_WIDTH;
    }
    
    private PlantyProcInstrScanner getDefaultScanner() {
        if (instructionScanner == null) {
            instructionScanner = new PlantyProcInstrScanner();
            instructionScanner.setDefaultReturnToken(
                new Token(
                    new TextAttribute(ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_DEFAULT))));
        }
        return instructionScanner;
    }
    
	private PlantyTagScanner getTagScanner() {
        if (tagScanner == null) {
            tagScanner = new PlantyTagScanner();
            tagScanner.setDefaultReturnToken(
                new Token(new TextAttribute(ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_TAG))));
        }
        return tagScanner;
    }
    
    
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getDefaultScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getTagScanner());
        reconciler.setDamager(dr, PlantyPartitionScanner.XML_TAG);
        reconciler.setRepairer(dr, PlantyPartitionScanner.XML_TAG);

		damageRepairer= new NonRuleBasedDamagerRepairer(
                new TextAttribute(ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_XML_COMMENT)));
        reconciler.setDamager(damageRepairer, PlantyPartitionScanner.XML_COMMENT);
        reconciler.setRepairer(damageRepairer, PlantyPartitionScanner.XML_COMMENT);

        return reconciler;
    }


	/**
	 * Preference colors have changed.  
	 * Update the default tokens of the scanners.
	 */
	public void updateScanners() {
		tagScanner.setDefaultReturnToken(
				new Token(new TextAttribute(ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_TAG))));
				
		instructionScanner.setDefaultReturnToken(
			   new Token(
				   new TextAttribute(ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_DEFAULT))));
				   
		damageRepairer.setDefaultTextAttribute(new TextAttribute(ExternalToolsPlugin.getPreferenceColor(IAntEditorColorConstants.P_XML_COMMENT)));				  
	}

    /*
     * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
     */
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
	    NotifyingReconciler reconciler= new NotifyingReconciler(new XMLReconcilingStrategy(fEditor), false);
	    reconciler.setDelay(500);
	    return reconciler;
    }

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new XMLAnnotationHover();
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationControlCreator(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return getInformationControlCreator(true);
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTextHover(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (fTextHover == null) {
			fTextHover= new XMLTextHover();
		}
		return fTextHover;
	}

}
