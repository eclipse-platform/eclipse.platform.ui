/*******************************************************************************
 * Copyright (c) 2000, 2004  John-Mason P. Shackelford and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * 	   John-Mason P. Shackelford - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.templates;

import org.eclipse.ant.internal.ui.editor.text.AntDocumentSetupParticipant;
import org.eclipse.ant.internal.ui.editor.text.AntEditorPartitionScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorProcInstrScanner;
import org.eclipse.ant.internal.ui.editor.text.AntEditorTagScanner;
import org.eclipse.ant.internal.ui.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.internal.ui.editor.text.MultilineDamagerRepairer;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class AntTemplatePreferencePage extends TemplatePreferencePage implements
        IWorkbenchPreferencePage {

    private static class AntSourceViewerConfiguration extends SourceViewerConfiguration{
    	
        private MultilineDamagerRepairer damageRepairer;
        private AntEditorProcInstrScanner instructionScanner;
        
        private AntEditorTagScanner tagScanner;

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
    		if (tagScanner == null) {
    			return; //property change before the editor is fully created
    		}
    		tagScanner.adaptToColorChange();
    		instructionScanner.adaptToColorChange();
    				   
    		damageRepairer.setDefaultTextAttribute(new TextAttribute(JFaceResources.getColorRegistry().get(IAntEditorColorConstants.XML_COMMENT_COLOR)));				  
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
    }

    public AntTemplatePreferencePage() {
        setPreferenceStore(AntUIPlugin.getDefault().getPreferenceStore());
        setTemplateStore(AntTemplateAccess.getDefault().getTemplateStore());
        setContextTypeRegistry(AntTemplateAccess.getDefault().getContextTypeRegistry());
    }

    public boolean performOk() {
        boolean ok = super.performOk();

        AntUIPlugin.getDefault().savePluginPreferences();

        return ok;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
     */
    protected SourceViewer createViewer(Composite parent) {
        SourceViewer viewer = new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        
        SourceViewerConfiguration configuration = new AntSourceViewerConfiguration();        
        IDocument document = new Document();       
        new AntDocumentSetupParticipant().setup(document);
        viewer.setDocument(document);
        viewer.configure(configuration);
		viewer.setEditable(false);	
		Font font= JFaceResources.getFont(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);    
		        
        return viewer;
    }

    protected boolean isShowFormatterSetting() {
        return false;
    }
}
