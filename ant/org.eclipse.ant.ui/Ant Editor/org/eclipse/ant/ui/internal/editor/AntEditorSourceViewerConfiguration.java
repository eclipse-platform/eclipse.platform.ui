/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug 24108
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor;

import org.eclipse.ant.ui.internal.editor.derived.HTMLTextPresenter;
import org.eclipse.ant.ui.internal.editor.text.AntEditorPartitionScanner;
import org.eclipse.ant.ui.internal.editor.text.AntEditorProcInstrScanner;
import org.eclipse.ant.ui.internal.editor.text.AntEditorTagScanner;
import org.eclipse.ant.ui.internal.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.ui.internal.editor.text.NonRuleBasedDamagerRepairer;
import org.eclipse.ant.ui.internal.editor.text.NotifyingReconciler;
import org.eclipse.ant.ui.internal.editor.text.XMLAnnotationHover;
import org.eclipse.ant.ui.internal.editor.text.XMLReconcilingStrategy;
import org.eclipse.ant.ui.internal.editor.text.XMLTextHover;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.ColorManager;
import org.eclipse.ant.ui.internal.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
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
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

/**
 * The source viewer configuration for the Ant Editor.
 */
public class AntEditorSourceViewerConfiguration extends SourceViewerConfiguration {

    private AntEditorTagScanner tagScanner;
    private AntEditorProcInstrScanner instructionScanner;
	private NonRuleBasedDamagerRepairer damageRepairer;
        
    private AntEditor fEditor;

    private XMLTextHover fTextHover;
    
    private ContentAssistant contentAssistant;
    
    /**
     * Creates an instance with the specified color manager.
     */
    public AntEditorSourceViewerConfiguration(AntEditor editor) {
	    super();
	    fEditor= editor;
    }    

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentAssistant(ISourceViewer)
     */
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        contentAssistant= new ContentAssistant();
        AntEditorCompletionProcessor processor = new AntEditorCompletionProcessor(); 
		contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		contentAssistant.setContentAssistProcessor(processor, AntEditorPartitionScanner.XML_TAG);
        
		IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();
		
		String triggers= store.getString(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS);
		if (triggers != null) {
			processor.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		}
				
		contentAssistant.enableAutoInsert(store.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT));
		contentAssistant.enableAutoActivation(store.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION));
		contentAssistant.setAutoActivationDelay(store.getInt(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_OVERLAY);
		contentAssistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_ABOVE);
		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		ColorManager manager= ColorManager.getDefault();	
		Color background= getColor(store, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, manager);			
		contentAssistant.setContextInformationPopupBackground(background);
		contentAssistant.setContextSelectorBackground(background);
		contentAssistant.setProposalSelectorBackground(background);

		Color foreground= getColor(store, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, manager);
		contentAssistant.setContextInformationPopupForeground(foreground);
		contentAssistant.setContextSelectorForeground(foreground);
		contentAssistant.setProposalSelectorForeground(foreground);
			
        IInformationControlCreator creator = getInformationControlCreator(true);
		contentAssistant.setInformationControlCreator(creator);

        return contentAssistant;
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
            AntEditorPartitionScanner.XML_COMMENT,
            AntEditorPartitionScanner.XML_TAG };
    }
    
    /**
     * @return TAB_WIDTH
     * @see SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
     */
    public int getTabWidth(ISourceViewer sourceViewer) {
        return AntEditor.TAB_WIDTH;
    }
    
    private AntEditorProcInstrScanner getDefaultScanner() {
        if (instructionScanner == null) {
            instructionScanner = new AntEditorProcInstrScanner();
            instructionScanner.setDefaultReturnToken(
                new Token(
                    new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.P_DEFAULT))));
        }
        return instructionScanner;
    }
    
	private AntEditorTagScanner getTagScanner() {
        if (tagScanner == null) {
            tagScanner = new AntEditorTagScanner();
            tagScanner.setDefaultReturnToken(
                new Token(new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.P_TAG))));
        }
        return tagScanner;
    }
    
    
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getDefaultScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new DefaultDamagerRepairer(getTagScanner());
        reconciler.setDamager(dr, AntEditorPartitionScanner.XML_TAG);
        reconciler.setRepairer(dr, AntEditorPartitionScanner.XML_TAG);

		damageRepairer= new NonRuleBasedDamagerRepairer(
                new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.P_XML_COMMENT)));
        reconciler.setDamager(damageRepairer, AntEditorPartitionScanner.XML_COMMENT);
        reconciler.setRepairer(damageRepairer, AntEditorPartitionScanner.XML_COMMENT);

        return reconciler;
    }


	/**
	 * Preference colors have changed.  
	 * Update the default tokens of the scanners.
	 */
	public void updateScanners() {
		tagScanner.setDefaultReturnToken(
				new Token(new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.P_TAG))));
				
		instructionScanner.setDefaultReturnToken(
			   new Token(
				   new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.P_DEFAULT))));
				   
		damageRepairer.setDefaultTextAttribute(new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.P_XML_COMMENT)));				  
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

	private Color getColor(IPreferenceStore store, String key, ColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}
	
	protected void changeConfiguration(PropertyChangeEvent event) {
		IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();
		String p= event.getProperty();

		ColorManager manager= ColorManager.getDefault();
		if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION.equals(p)) {
			boolean enabled= store.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION);
			contentAssistant.enableAutoActivation(enabled);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
			contentAssistant.setAutoActivationDelay(delay);
		} else if (AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND.equals(p)) {
			Color c= getColor(store, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, manager);
			contentAssistant.setProposalSelectorForeground(c);
		} else if (AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND.equals(p)) {
			Color c= getColor(store, AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, manager);
			contentAssistant.setProposalSelectorBackground(c);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT.equals(p)) {
			boolean enabled= store.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT);
			contentAssistant.enableAutoInsert(enabled);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS.equals(p)) {
			changeContentAssistProcessor(store);
		}
	}
	
	private void changeContentAssistProcessor(IPreferenceStore store) {
		String triggers= store.getString(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS);
		if (triggers != null) {
			AntEditorCompletionProcessor cp= (AntEditorCompletionProcessor)contentAssistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
			if (cp != null) {
				cp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			}
		}		
	}
}
