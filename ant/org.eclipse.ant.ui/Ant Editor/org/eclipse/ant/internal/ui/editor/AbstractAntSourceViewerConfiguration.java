/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import org.eclipse.ant.internal.ui.editor.text.AntDocumentSetupParticipant;
import org.eclipse.ant.internal.ui.editor.text.AntEditorPartitionScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorProcInstrScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorTagScanner;
import org.eclipse.ant.internal.ui.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.internal.ui.editor.text.MultilineDamagerRepairer;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.ColorManager;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public abstract class AbstractAntSourceViewerConfiguration extends SourceViewerConfiguration {

	private AntEditorTagScanner tagScanner;
    private AntEditorProcInstrScanner instructionScanner;
	private MultilineDamagerRepairer damageRepairer;
	private TextAttribute xmlCommentAttribute;
	
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
	    reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
	    
		MultilineDamagerRepairer dr = new MultilineDamagerRepairer(getDefaultScanner(), null);
	    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
	    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	
	    dr = new MultilineDamagerRepairer(getTagScanner(), null);
	    reconciler.setDamager(dr, AntEditorPartitionScanner.XML_TAG);
	    reconciler.setRepairer(dr, AntEditorPartitionScanner.XML_TAG);
	
	    IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();
	    int style= SWT.NORMAL;
	    if (store.getBoolean(IAntEditorColorConstants.XML_COMMENT_COLOR + AntEditorPreferenceConstants.EDITOR_BOLD_SUFFIX)) {
	    	style |= SWT.BOLD;
	    }
	    if (store.getBoolean(IAntEditorColorConstants.XML_COMMENT_COLOR + AntEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX)) {
	    	style |= SWT.ITALIC;
	    }
		
	    xmlCommentAttribute=  new TextAttribute(AntUIPlugin.getPreferenceColor(IAntEditorColorConstants.XML_COMMENT_COLOR), null, style);
		damageRepairer= new MultilineDamagerRepairer(null, xmlCommentAttribute);
	    reconciler.setDamager(damageRepairer, AntEditorPartitionScanner.XML_COMMENT);
	    reconciler.setRepairer(damageRepairer, AntEditorPartitionScanner.XML_COMMENT);
	
	    return reconciler;
	}

	/**
	 * Preference colors have changed.  
	 * Update the default tokens of the scanners.
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		if (tagScanner == null) {
			return; //property change before the editor is fully created
		}
		tagScanner.adaptToPreferenceChange(event);
		instructionScanner.adaptToPreferenceChange(event);
		String property= event.getProperty();
		if (property.startsWith(IAntEditorColorConstants.XML_COMMENT_COLOR)) {
			if (property.endsWith(AntEditorPreferenceConstants.EDITOR_BOLD_SUFFIX)) {
				adaptToStyleChange(event, SWT.BOLD);
			} else if (property.endsWith(AntEditorPreferenceConstants.EDITOR_ITALIC_SUFFIX)) {
				adaptToStyleChange(event, SWT.ITALIC);
			} else {
				adaptToColorChange(event);
			}
			damageRepairer.setDefaultTextAttribute(xmlCommentAttribute);
		} 
	}
	
	private void adaptToStyleChange(PropertyChangeEvent event, int styleAttribute) {
		boolean eventValue= false;
		Object value= event.getNewValue();
		if (value instanceof Boolean) {
			eventValue= ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue= true;
		}
		
		boolean activeValue= (xmlCommentAttribute.getStyle() & styleAttribute) == styleAttribute;
		if (activeValue != eventValue) { 
			xmlCommentAttribute= new TextAttribute(xmlCommentAttribute.getForeground(), xmlCommentAttribute.getBackground(), eventValue ? xmlCommentAttribute.getStyle() | styleAttribute : xmlCommentAttribute.getStyle() & ~styleAttribute);
		}	
	}
	
	 /**
     * Update the text attributes associated with the tokens of this scanner as a color preference has been changed. 
     */
    private void adaptToColorChange(PropertyChangeEvent event) {
    	RGB rgb= null;
		
		Object value= event.getNewValue();
		if (value instanceof RGB) {
			rgb= (RGB) value;
		} else if (value instanceof String) {
			rgb= StringConverter.asRGB((String) value);
		}
			
		if (rgb != null) {
			xmlCommentAttribute= new TextAttribute(ColorManager.getDefault().getColor(rgb), xmlCommentAttribute.getBackground(), xmlCommentAttribute.getStyle());
		}
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
	    return new String[] {
	        IDocument.DEFAULT_CONTENT_TYPE,
	        AntEditorPartitionScanner.XML_COMMENT,
	        AntEditorPartitionScanner.XML_TAG,
			AntEditorPartitionScanner.XML_CDATA};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return AntUIPlugin.getDefault().getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}
	
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		String property= event.getProperty();
		return property.startsWith(IAntEditorColorConstants.TEXT_COLOR) ||
			property.startsWith(IAntEditorColorConstants.PROCESSING_INSTRUCTIONS_COLOR) ||
			property.startsWith(IAntEditorColorConstants.STRING_COLOR) ||
			property.startsWith(IAntEditorColorConstants.TAG_COLOR) ||
			property.startsWith(IAntEditorColorConstants.XML_COMMENT_COLOR);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return AntDocumentSetupParticipant.ANT_PARTITIONING;
	}
}