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
 * 	   IBM Corporation - bug fixes
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor;

import java.util.ResourceBundle;

import org.eclipse.ant.ui.internal.editor.outline.AntEditorContentOutlinePage;
import org.eclipse.ant.ui.internal.editor.outline.AntModel;
import org.eclipse.ant.ui.internal.editor.outline.XMLCore;
import org.eclipse.ant.ui.internal.editor.text.AnnotationAccess;
import org.eclipse.ant.ui.internal.editor.text.AnnotationType;
import org.eclipse.ant.ui.internal.editor.text.AntEditorDocumentProvider;
import org.eclipse.ant.ui.internal.editor.text.IAntEditorColorConstants;
import org.eclipse.ant.ui.internal.editor.xml.IAntEditorConstants;
import org.eclipse.ant.ui.internal.editor.xml.XmlAttribute;
import org.eclipse.ant.ui.internal.editor.xml.XmlElement;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.ant.ui.internal.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * The actual editor implementation for Eclipse's Ant integration.
 */
public class AntEditor extends TextEditor {

	/**
	 * The tab width
	 */
	public static final int TAB_WIDTH = 4;

	/**
	 * Selection changed listener for the outline view.
	 */
    protected SelectionChangedListener selectionChangedListener = new SelectionChangedListener();
    class SelectionChangedListener  implements ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            doSelectionChanged(event);
        }
    }
    
    /**
     * The page that shows the outline.
     */
    protected AntEditorContentOutlinePage page;
  
    /**
     * Constructor for AntEditor.
     */
    public AntEditor() {
        super();
		setSourceViewerConfiguration(new AntEditorSourceViewerConfiguration(this));
		setDocumentProvider(new AntEditorDocumentProvider(XMLCore.getDefault()));
    }


    /** The <code>AntEditor</code> implementation of this 
     * <code>AbstractTextEditor</code> method extend the 
     * actions to add those specific to the receiver
     */
    protected void createActions() {
        super.createActions();

        ContentAssistAction action = new ContentAssistAction(ResourceBundle.getBundle("org.eclipse.ant.ui.internal.editor.AntEditorMessages"), "ContentAssistProposal.", this); //$NON-NLS-1$ //$NON-NLS-2$

        // This action definition is associated with the accelerator Ctrl+Space
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action); //$NON-NLS-1$
    }

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 * Called from TextEditor.<init>
	 */
    protected void initializeEditor() {
		super.initializeEditor();
		setPreferenceStore(AntUIPlugin.getDefault().getPreferenceStore());
		setCompatibilityMode(false);
		setHelpContextId(IAntUIHelpContextIds.ANT_EDITOR);	
    }
   
	/* (non-Javadoc)
     * Method declared on IAdaptable
     */
    public Object getAdapter(Class key) {
        if (key.equals(IContentOutlinePage.class)) {
			return getOutlinePage();
        }
        return super.getAdapter(key);
    }

	private AntEditorContentOutlinePage getOutlinePage() {
		if (page == null) {
			page= new AntEditorContentOutlinePage(XMLCore.getDefault());
			page.addPostSelectionChangedListener(selectionChangedListener);
			setOutlinePageInput(getEditorInput());
		}
		return page;
	}

    private void doSelectionChanged(SelectionChangedEvent aSelectionChangedEvent) {
        IStructuredSelection selection= (IStructuredSelection)aSelectionChangedEvent.getSelection();

        if (!isActivePart() && AntUIPlugin.getActivePage() != null) {
			AntUIPlugin.getActivePage().bringToTop(this);
        }
        
        /*
         * Here the according ISourceReference should be determined and
         * then passed to setSelection.
         */
        XmlElement tempSelectedXmlElement = (XmlElement)selection.getFirstElement(); 
        if(tempSelectedXmlElement != null) {
			setSelection(tempSelectedXmlElement, !isActivePart());
        }
    }

    /**
     * Returns whether the editor is active.
     */
    private boolean isActivePart() {
        IWorkbenchWindow window= getSite().getWorkbenchWindow();
        IPartService service= window.getPartService();
        IWorkbenchPart part= service.getActivePart();
        return part != null && part.equals(this);
    }
    
    protected void setSelection(XmlElement reference, boolean moveCursor) {
        if (reference != null) {
        	if (reference.isExternal()) {
        		while (!reference.isRootExternal() || (reference.getParentNode() != null && reference.getParentNode().isExternal())) {
					//no possible selection for this external element
					//find the root external entity actually in the document
        			reference= reference.getParentNode();
        		}
        	}
            
            StyledText  textWidget= null;
            
            ISourceViewer sourceViewer= getSourceViewer();
            if (sourceViewer != null) {
                textWidget= sourceViewer.getTextWidget();
            }
            
            if (textWidget == null) {
                return;
            }
                
            try {
                
                int offset= reference.getOffset();
                int length= reference.getLength();
                
                if (offset < 0) {
                    return;
                }
                    
                textWidget.setRedraw(false);
                
                if(length > 0) {
	                setHighlightRange(offset, length, moveCursor);
                }
                
                if (!moveCursor) {
                    return;
                }

				XmlAttribute attrType= reference.getAttributeNamed(IAntEditorConstants.ATTR_TYPE);
				if (!reference.isErrorNode() ||
					 (attrType != null &&
					  IAntEditorConstants.TYPE_PROJECT.equalsIgnoreCase(attrType.getValue()))) {
					//NOTE: type is checked because isErrorNode() is true for an error node *and*
					// the root node, which - in this case - should be handled as an normal node  
	                offset= reference.getOffset()+1;
	                length= reference.getName().length();
				}
                                            
                if (offset > -1 && length > 0) {
                    sourceViewer.revealRange(offset, length);
                    // Selected region begins one index after offset
                    sourceViewer.setSelectedRange(offset, length); 
                }
            } catch (IllegalArgumentException x) {
            	AntUIPlugin.log(x);
            } finally {
                if (textWidget != null) {
                    textWidget.setRedraw(true);
                }
            }
            
        } else if (moveCursor) {
            resetHighlightRange();
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#affectsTextPresentation(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		String property= event.getProperty();
		return property.equals(IAntEditorColorConstants.P_DEFAULT) ||
		property.equals(IAntEditorColorConstants.P_PROC_INSTR) ||
		property.equals(IAntEditorColorConstants.P_STRING) ||
		property.equals(IAntEditorColorConstants.P_TAG) ||
		property.equals(IAntEditorColorConstants.P_XML_COMMENT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		AntEditorSourceViewerConfiguration sourceViewerConfiguration= (AntEditorSourceViewerConfiguration)getSourceViewerConfiguration();
		if (affectsTextPresentation(event)) {
			sourceViewerConfiguration.updateScanners();
		}
		
		sourceViewerConfiguration.changeConfiguration(event);
							
		super.handlePreferenceStoreChanged(event);
	}
	
	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#doSetInput(org.eclipse.ui.IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		setOutlinePageInput(input);
	}

	private void setOutlinePageInput(IEditorInput input) {
		if (page != null) {
			IDocumentProvider provider= getDocumentProvider();
			if (provider instanceof AntEditorDocumentProvider) {
				AntEditorDocumentProvider documentProvider= (AntEditorDocumentProvider) provider;
				AntModel model= documentProvider.getAntModel(input);
				page.setPageInput(model);
			}
		}
	}

	protected IAnnotationAccess createAnnotationAccess() {
		return new AnnotationAccess(getAnnotationPreferences());
	}

	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);

		support.setAnnotationPainterPreferenceKeys(AnnotationType.BOOKMARK, AntEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR, AntEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION, AntEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER, 1);
		support.setAnnotationPainterPreferenceKeys(AnnotationType.TASK, AntEditorPreferenceConstants.EDITOR_TASK_INDICATION_COLOR, AntEditorPreferenceConstants.EDITOR_TASK_INDICATION, AntEditorPreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER, 2);
		support.setAnnotationPainterPreferenceKeys(AnnotationType.SEARCH, AntEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR, AntEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION, AntEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER, 3);
		support.setAnnotationPainterPreferenceKeys(AnnotationType.INFO, AntEditorPreferenceConstants.EDITOR_INFO_INDICATION_COLOR, AntEditorPreferenceConstants.EDITOR_INFO_INDICATION, AntEditorPreferenceConstants.EDITOR_INFO_INDICATION_IN_OVERVIEW_RULER, 4);
		support.setAnnotationPainterPreferenceKeys(AnnotationType.WARNING, AntEditorPreferenceConstants.EDITOR_WARNING_INDICATION_COLOR, AntEditorPreferenceConstants.EDITOR_WARNING_INDICATION, AntEditorPreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER, 5);
		support.setAnnotationPainterPreferenceKeys(AnnotationType.ERROR, AntEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR, AntEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION, AntEditorPreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER, 6);
	}
}
