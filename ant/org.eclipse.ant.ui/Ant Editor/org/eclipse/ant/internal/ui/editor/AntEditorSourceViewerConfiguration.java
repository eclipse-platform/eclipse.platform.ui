/*******************************************************************************
 * Copyright (c) 2002, 2011 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH,
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *     John-Mason P. Shackelford - bug 40255
 *     Rob Dingwell - bug 68886
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.AntSourceViewerConfiguration;
import org.eclipse.ant.internal.ui.editor.formatter.XmlDocumentFormattingStrategy;
import org.eclipse.ant.internal.ui.editor.formatter.XmlElementFormattingStrategy;
import org.eclipse.ant.internal.ui.editor.text.AntDocumentSetupParticipant;
import org.eclipse.ant.internal.ui.editor.text.AntEditorPartitionScanner;
import org.eclipse.ant.internal.ui.editor.text.AntInformationProvider;
import org.eclipse.ant.internal.ui.editor.text.NotifyingReconciler;
import org.eclipse.ant.internal.ui.editor.text.XMLAnnotationHover;
import org.eclipse.ant.internal.ui.editor.text.XMLReconcilingStrategy;
import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;

/**
 * The source viewer configuration for the Ant Editor.
 */
public class AntEditorSourceViewerConfiguration extends AntSourceViewerConfiguration {
    
    private AntEditor fEditor;

    private XMLTextHover fTextHover;
    
    private ContentAssistant fContentAssistant;
    
    private AntAutoEditStrategy[] fAutoEditorStategies;
    
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
        fContentAssistant= new ContentAssistant();
        AntEditorCompletionProcessor processor = new AntEditorCompletionProcessor(fEditor.getAntModel());
		fContentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		fContentAssistant.setContentAssistProcessor(processor, AntEditorPartitionScanner.XML_TAG);
        fContentAssistant.setDocumentPartitioning(AntDocumentSetupParticipant.ANT_PARTITIONING);
		
		String triggers= fPreferenceStore.getString(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS);
		if (triggers != null) {
			processor.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		}
				
		fContentAssistant.enableAutoInsert(fPreferenceStore.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT));
		fContentAssistant.enableAutoActivation(fPreferenceStore.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION));
		fContentAssistant.setAutoActivationDelay(fPreferenceStore.getInt(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY));
		fContentAssistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		fContentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		fContentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		Color background= JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_BACKGROUND_COLOR);
		fContentAssistant.setContextInformationPopupBackground(background);
		fContentAssistant.setContextSelectorBackground(background);

		Color foreground= JFaceResources.getColorRegistry().get(JFacePreferences.CONTENT_ASSIST_FOREGROUND_COLOR);
		fContentAssistant.setContextInformationPopupForeground(foreground);
		fContentAssistant.setContextSelectorForeground(foreground);
			
        IInformationControlCreator creator = getInformationControlCreator(sourceViewer);
		fContentAssistant.setInformationControlCreator(creator);

		fContentAssistant.setRepeatedInvocationMode(true);
		fContentAssistant.setStatusLineVisible(true);
		fContentAssistant.setShowEmptyList(true);
		fContentAssistant.addCompletionListener(processor);
        return fContentAssistant;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
     */
    public IReconciler getReconciler(ISourceViewer sourceViewer) {
	    NotifyingReconciler reconciler= new NotifyingReconciler(new XMLReconcilingStrategy(fEditor));
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
		return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent, false);
            }
        };
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
		String p= event.getProperty();

		if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION.equals(p)) {
			boolean enabled= fPreferenceStore.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION);
			fContentAssistant.enableAutoActivation(enabled);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY.equals(p) && fContentAssistant != null) {
			int delay= fPreferenceStore.getInt(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY);
			fContentAssistant.setAutoActivationDelay(delay);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT.equals(p) && fContentAssistant != null) {
			boolean enabled= fPreferenceStore.getBoolean(AntEditorPreferenceConstants.CODEASSIST_AUTOINSERT);
			fContentAssistant.enableAutoInsert(enabled);
		} else if (AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS.equals(p)) {
			changeContentAssistProcessor();
		}
	}
	
	private void changeContentAssistProcessor() {
		String triggers= fPreferenceStore.getString(AntEditorPreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS);
		if (triggers != null) {
			AntEditorCompletionProcessor cp= (AntEditorCompletionProcessor)fContentAssistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
			if (cp != null) {
				cp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			}
		}
	}
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getContentFormatter(org.eclipse.jface.text.source.ISourceViewer)
     */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		 
		MultiPassContentFormatter formatter = new MultiPassContentFormatter(
				getConfiguredDocumentPartitioning(sourceViewer),
		         IDocument.DEFAULT_CONTENT_TYPE);
		 
		
		formatter.setMasterStrategy(new XmlDocumentFormattingStrategy());
		
		formatter.setSlaveStrategy(new XmlElementFormattingStrategy(), AntEditorPartitionScanner.XML_TAG);
		
		//formatter.setSlaveStrategy(new XmlCommentFormattingStrategy(), AntEditorPartitionScanner.XML_COMMENT);
		 
		return formatter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationPresenter(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		InformationPresenter presenter= new InformationPresenter(getInformationPresenterControlCreator());
		presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		IInformationProvider provider= new AntInformationProvider(new XMLTextHover(fEditor));
		presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
		presenter.setInformationProvider(provider, AntEditorPartitionScanner.XML_CDATA);
		presenter.setInformationProvider(provider, AntEditorPartitionScanner.XML_COMMENT);
		presenter.setInformationProvider(provider, AntEditorPartitionScanner.XML_DTD);
		presenter.setInformationProvider(provider, AntEditorPartitionScanner.XML_TAG);
		presenter.setSizeConstraints(60, 10, true, true);
		return presenter;
	}
	
	/**
	 * Returns the information presenter control creator. The creator is a factory creating the
	 * presenter controls for the given source viewer. This implementation always returns a creator
	 * for <code>DefaultInformationControl</code> instances.
	 * 
	 * @return an information control creator
	 * @since 3.1
	 */
	public static IInformationControlCreator getInformationPresenterControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, true);
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getIndentPrefixes(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		List list= new ArrayList();

		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces
				
		int tabWidth= getTabWidth(sourceViewer);
		boolean useSpaces= fEditor.isTabsToSpacesConversionEnabled();
		
		for (int i= 0; i <= tabWidth; i++) {
		    StringBuffer prefix= new StringBuffer();
			if (useSpaces) {
			    for (int j= 0; j + i < tabWidth; j++) {
			    	prefix.append(' ');
			    }
		    	
				if (i != 0) {
		    		prefix.append('\t');
				}
			} else {
			    for (int j= 0; j < i; j++) {
			    	prefix.append(' ');
			    }
				if (i != tabWidth) {
		    		prefix.append('\t');
				}
			}
			
			list.add(prefix.toString());
		}

		list.add(IAntCoreConstants.EMPTY_STRING);
		
		return (String[]) list.toArray(new String[list.size()]);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if (AntEditorPartitionScanner.XML_COMMENT.equals(contentType)) {
			return super.getAutoEditStrategies(sourceViewer, contentType);
		}
		if (fAutoEditorStategies == null) {
			fAutoEditorStategies= new AntAutoEditStrategy[] {new AntAutoEditStrategy(fEditor.getAntModel())};
		}
		return fAutoEditorStategies;
	}
    
   /*
    * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getHyperlinkDetectorTargets(org.eclipse.jface.text.source.ISourceViewer)
    */
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("org.eclipse.ant.ui.buildFiles", fEditor); //$NON-NLS-1$
		return targets;
	}
}