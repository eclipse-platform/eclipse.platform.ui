/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *     John-Mason P. Shackelford - bug 40255
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import org.eclipse.ant.internal.ui.editor.derived.HTMLTextPresenter;
import org.eclipse.ant.internal.ui.editor.formatter.ContentFormatter3;
import org.eclipse.ant.internal.ui.editor.formatter.XmlCommentFormattingStrategy;
import org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormattingStrategy;
import org.eclipse.ant.internal.ui.editor.formatter.XmlElementFormattingStrategy;
import org.eclipse.ant.internal.ui.editor.text.AntEditorPartitionScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorProcInstrScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorTagScanner;
import org.eclipse.ant.internal.ui.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.internal.ui.editor.text.MultilineDamagerRepairer;
import org.eclipse.ant.internal.ui.editor.text.NotifyingReconciler;
import org.eclipse.ant.internal.ui.editor.text.XMLAnnotationHover;
import org.eclipse.ant.internal.ui.editor.text.XMLReconcilingStrategy;
import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ExtendedTextEditorPreferenceConstants;

/**
 * The source viewer configuration for the Ant Editor.
 */
public class AntEditorSourceViewerConfiguration extends SourceViewerConfiguration {

    private AntEditorTagScanner tagScanner;
    private AntEditorProcInstrScanner instructionScanner;
	private MultilineDamagerRepairer damageRepairer;
        
    private AntEditor fEditor;

    private XMLTextHover fTextHover;
    
    private ContentAssistant contentAssistant;
    
    private AntAutoIndentStrategy autoIndentStrategy;
    
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
        AntEditorCompletionProcessor processor = new AntEditorCompletionProcessor(fEditor.getAntModel()); 
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
		contentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));
	
		Color background= JFaceResources.getColorRegistry().get(AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND);			
		contentAssistant.setContextInformationPopupBackground(background);
		contentAssistant.setContextSelectorBackground(background);
		contentAssistant.setProposalSelectorBackground(background);

		Color foreground= JFaceResources.getColorRegistry().get(AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND);
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
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
     */
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            AntEditorPartitionScanner.XML_COMMENT,
            AntEditorPartitionScanner.XML_TAG };
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
     */
    public int getTabWidth(ISourceViewer sourceViewer) {
    	return AntUIPlugin.getDefault().getPreferenceStore().getInt(ExtendedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    }
    
    private AntEditorProcInstrScanner getDefaultScanner() {
        if (instructionScanner == null) {
            instructionScanner = new AntEditorProcInstrScanner();
        }
        return instructionScanner;
    }
    
	private AntEditorTagScanner getTagScanner() {
        if (tagScanner == null) {
            tagScanner = new AntEditorTagScanner();
        }
        return tagScanner;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();

		MultilineDamagerRepairer dr = new MultilineDamagerRepairer(getDefaultScanner(), null);
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        dr = new MultilineDamagerRepairer(getTagScanner(), null);
        reconciler.setDamager(dr, AntEditorPartitionScanner.XML_TAG);
        reconciler.setRepairer(dr, AntEditorPartitionScanner.XML_TAG);

		damageRepairer= new MultilineDamagerRepairer(null,
                new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.XML_COMMENT_COLOR)));
        reconciler.setDamager(damageRepairer, AntEditorPartitionScanner.XML_COMMENT);
        reconciler.setRepairer(damageRepairer, AntEditorPartitionScanner.XML_COMMENT);

        return reconciler;
    }


	/**
	 * Preference colors have changed.  
	 * Update the default tokens of the scanners.
	 */
	public void updateScanners() {
		tagScanner.adaptToColorChange();
		instructionScanner.adaptToColorChange();
				   
		damageRepairer.setDefaultTextAttribute(new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.XML_COMMENT_COLOR)));				  
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
	    NotifyingReconciler reconciler= new NotifyingReconciler(new XMLReconcilingStrategy(fEditor), true);
	    reconciler.setDelay(XMLReconcilingStrategy.DELAY);
	    reconciler.addReconcilingParticipant(fEditor);
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
			fTextHover= new XMLTextHover(fEditor);
		}
		return fTextHover;
	}
	
	protected void changeConfiguration(PropertyChangeEvent event) {
		IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();
		String p= event.getProperty();

		if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION.equals(p)) {
			boolean enabled= store.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION);
			contentAssistant.enableAutoActivation(enabled);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
			contentAssistant.setAutoActivationDelay(delay);
		} else if (AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND.equals(p)) {
			Color c= JFaceResources.getColorRegistry().get(AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND);
			contentAssistant.setProposalSelectorForeground(c);
		} else if (AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND.equals(p)) {
			Color c= JFaceResources.getColorRegistry().get(AntEditorPreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND);
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
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentFormatter(org.eclipse.jface.text.source.ISourceViewer)
     */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {

        ContentFormatter3 formatter = new ContentFormatter3();
        formatter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        
        IFormattingStrategy indentationStrategy = new XmlDocumentFormattingStrategy(sourceViewer);        
        formatter.setFormattingStrategy(indentationStrategy);
        formatter.setFormattingStrategy(indentationStrategy,
                IDocument.DEFAULT_CONTENT_TYPE);

        // TODO This approach would make the formatter run much more quickly
        // if these options aren't used; however this won't really work
        // since the content formatter is probably configured only once at
        // the start up of the editor. In order to make this work I'd need to
        // listen to further changes from the preferences store and reconfigure
        // the editor appropriately.
        //        FormattingPreferences fp = new FormattingPreferences();
        //        if (fp.formatElements()) {
        IFormattingStrategy elementFormattingStrategy = new XmlElementFormattingStrategy(
                sourceViewer);
        formatter.setFormattingStrategy(elementFormattingStrategy,
                AntEditorPartitionScanner.XML_TAG);
        //        }
        //        if(fp.formatComments()){
        IFormattingStrategy commentFormattingStrategy = new XmlCommentFormattingStrategy(
                sourceViewer);
        formatter.setFormattingStrategy(commentFormattingStrategy,
                AntEditorPartitionScanner.XML_COMMENT);
        //        }

        return formatter;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoIndentStrategy(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType) {
		if (AntEditorPartitionScanner.XML_COMMENT.equals(contentType)) {
			return super.getAutoIndentStrategy(sourceViewer, contentType);
		} else {
			if (autoIndentStrategy == null) {
				autoIndentStrategy= new AntAutoIndentStrategy(fEditor.getAntModel());
			}
			return autoIndentStrategy;
		}
	}
}