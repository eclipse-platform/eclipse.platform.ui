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

import org.eclipse.ant.internal.ui.editor.text.AntEditorPartitionScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorProcInstrScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorTagScanner;
import org.eclipse.ant.internal.ui.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.internal.ui.editor.text.MultilineDamagerRepairer;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public abstract class AbstractAntSourceViewerConfiguration extends SourceViewerConfiguration {

	private AntEditorTagScanner tagScanner;
    private AntEditorProcInstrScanner instructionScanner;
	private MultilineDamagerRepairer damageRepairer;
	
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
		if (tagScanner == null) {
			return; //property change before the editor is fully created
		}
		tagScanner.adaptToColorChange();
		instructionScanner.adaptToColorChange();
				   
		damageRepairer.setDefaultTextAttribute(new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.XML_COMMENT_COLOR)));				  
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
	    return new String[] {
	        IDocument.DEFAULT_CONTENT_TYPE,
	        AntEditorPartitionScanner.XML_COMMENT,
	        AntEditorPartitionScanner.XML_TAG };
	}

	public int getTabWidth(ISourceViewer sourceViewer) {
		return AntUIPlugin.getDefault().getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}
}