/**********************************************************************
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
//
// Copyright:
// GEBIT Gesellschaft fuer EDV-Beratung
// und Informatik-Technologien mbH, 
// Berlin, Duesseldorf, Frankfurt (Germany) 2002
// All rights reserved.
//
package org.eclipse.ui.externaltools.internal.ant.editor;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

import org.eclipse.ui.externaltools.internal.ant.editor.derived.HTMLTextPresenter;
import org.eclipse.ui.externaltools.internal.ant.editor.text.NonRuleBasedDamagerRepairer;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyColorConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyPartitionScanner;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyProcInstrScanner;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyTagScanner;



/**
 * The source viewer configuration for Planty.
 * 
 * @version 24.09.2002
 * @author Alf Schiefelbein
 */
public class PlantySourceViewerConfiguration extends SourceViewerConfiguration {

    // (IBM)
    private PlantyTagScanner tagScanner;
    private PlantyProcInstrScanner pdeScanner;
        
    /**
     * Creates an instance with the specified color manager.
     */
    public PlantySourceViewerConfiguration() {
    }
    

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
     */
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant= new ContentAssistant();
        PlantyCompletionProcessor tempProcessor = new PlantyCompletionProcessor(); 
        assistant.setContentAssistProcessor(tempProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(tempProcessor, PlantyPartitionScanner.XML_TAG);

        IInformationControlCreator creator = getInformationControlCreator(true);
        assistant.setInformationControlCreator(creator);

		// TODO: Determine how to configure code assist without JDT
        //IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
        //ContentAssistPreference.configure(assistant, store);
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


//    /*
//     * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
//     */
//    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
//        
//        ContentFormatter formatter= new ContentFormatter();
//        IFormattingStrategy strategy= new JavaFormattingStrategy(sourceViewer);
//        
//        formatter.setFormattingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
//        formatter.enablePartitionAwareFormatting(false);        
//        formatter.setPartitionManagingPositionCategories(fJavaTextTools.getPartitionManagingPositionCategories());
//        
//        return formatter;
//    }
    

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
    

    // (IBM)
    protected PlantyProcInstrScanner getDefaultScanner() {
        if (pdeScanner == null) {
            pdeScanner = new PlantyProcInstrScanner();
            pdeScanner.setDefaultReturnToken(
                new Token(
                    new TextAttribute(ExternalToolsPlugin.getPreferenceColor(PlantyColorConstants.P_DEFAULT))));
        }
        return pdeScanner;
    }
    protected PlantyTagScanner getTagScanner() {
        if (tagScanner == null) {
            tagScanner = new PlantyTagScanner();
            tagScanner.setDefaultReturnToken(
                new Token(new TextAttribute(ExternalToolsPlugin.getPreferenceColor(PlantyColorConstants.P_TAG))));
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

        NonRuleBasedDamagerRepairer ndr =
            new NonRuleBasedDamagerRepairer(
                new TextAttribute(ExternalToolsPlugin.getPreferenceColor(PlantyColorConstants.P_XML_COMMENT)));
        reconciler.setDamager(ndr, PlantyPartitionScanner.XML_COMMENT);
        reconciler.setRepairer(ndr, PlantyPartitionScanner.XML_COMMENT);

        return reconciler;
    }

}
