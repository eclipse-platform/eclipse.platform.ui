package org.eclipse.ui.externaltools.internal.ant.editor;

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
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.externaltools.internal.ant.editor.outline.PlantyContentOutlinePage;
import org.eclipse.ui.externaltools.internal.ant.editor.text.IAntEditorColorConstants;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyDocumentProvider;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * The actual editor implementation for Planty.
 * 
 * @version 3. Nov. 2002
 * @author Alf Schiefelbein
 */
public class PlantyEditor extends TextEditor {

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
    };
    

    /**
     * The page that shows the outline.
     */
    protected PlantyContentOutlinePage page;


    /**
     * Constructor for PlantyEditor.
     */
    public PlantyEditor() {
        super();
        setDocumentProvider(new PlantyDocumentProvider());
        setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
    }


    /** The <code>PlantyEditor</code> implementation of this 
     * <code>AbstractTextEditor</code> method extend the 
     * actions to add those specific to the receiver
     */
    protected void createActions() {
        super.createActions();

        ContentAssistAction action = new ContentAssistAction(ResourceBundle.getBundle("org.eclipse.ui.externaltools.internal.ant.editor.AntEditorMessages"), "ContentAssistProposal.", this); //$NON-NLS-1$ //$NON-NLS-2$

        // This action definition is associated with the accelerator Ctrl+Space
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action); //$NON-NLS-1$
    }

    public void initializeEditor() {
        // That is where the assistant and its processor is defined
        setSourceViewerConfiguration(new PlantySourceViewerConfiguration());
    }
   
	/* (non-Javadoc)
     * Method declared on IAdaptable
     */
    public Object getAdapter(Class key) {

		// Comment this in to get the outline view
        if (key.equals(IContentOutlinePage.class)) {
            IEditorInput input = getEditorInput();
            if (input instanceof IFileEditorInput) {
                page = new PlantyContentOutlinePage(((IFileEditorInput)input).getFile());
                page.addSelectionChangedListener(selectionChangedListener);
                return page;
            }
        }

        return super.getAdapter(key);
    }


    protected void doSelectionChanged(SelectionChangedEvent aSelectionChangedEvent) {
        IStructuredSelection selection= (IStructuredSelection)aSelectionChangedEvent.getSelection();

        if (!isActivePart() && ExternalToolsPlugin.getActivePage() != null) {
			ExternalToolsPlugin.getActivePage().bringToTop(this);
        }
        
        /*
         * Here the according ISourceReference should be determined and
         * then passed to setSelection.
         */
        XmlElement tempSelectedXmlElement = (XmlElement)selection.getFirstElement(); 
        if(tempSelectedXmlElement != null) {
			setSelection(tempSelectedXmlElement, true);
        }
    }

	
    /**
     * Returns wether the editor is active.
     */
    protected boolean isActivePart() {
        IWorkbenchWindow window= getSite().getWorkbenchWindow();
        IPartService service= window.getPartService();
        IWorkbenchPart part= service.getActivePart();
        return part != null && part.equals(this);
    }
    
    protected void setSelection(XmlElement reference, boolean moveCursor) {
//        ISelection selection= getSelectionProvider().getSelection();
//        if (selection instanceof TextSelection) {
//            TextSelection textSelection= (TextSelection) selection;
//            if (textSelection.getOffset() != 0 || textSelection.getLength() != 0)
//                markInNavigationHistory();
//        }
        
        if (reference != null) {
        	if (reference.isExternal() && !reference.isRootExternal()) {
        		while (!reference.isRootExternal()) {
					//no possible selection for this external element
					//find the root external entity actually in the document
        			reference= reference.getParentNode();
        		}
        	}
            
            StyledText  textWidget= null;
            
            ISourceViewer sourceViewer= getSourceViewer();
            if (sourceViewer != null)
                textWidget= sourceViewer.getTextWidget();
            
            if (textWidget == null)
                return;
                
            try {
                
                int offset= reference.getOffset();
                int length= reference.getLength();
                
                if (offset < 0) {
                    return;
                }
                    
                textWidget.setRedraw(false);
                
                if(length >0) {
	                setHighlightRange(offset, length, moveCursor);
                }
                
                if (!moveCursor) {
                    return;
                }

                offset= reference.getOffset()+1;
                length= reference.getName().length();
                                            
                if (offset > -1 && length > 0) {
                    sourceViewer.revealRange(offset, length);
                    // Selected region begins one index after offset
                    sourceViewer.setSelectedRange(offset, length); 
                }
            } catch (IllegalArgumentException x) {
            } finally {
                if (textWidget != null)
                    textWidget.setRedraw(true);
            }
            
        } else if (moveCursor) {
            resetHighlightRange();
        }
        
//        markInNavigationHistory();
    }


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (page != null) {
			page.update();
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
		if (affectsTextPresentation(event)) {
			((PlantySourceViewerConfiguration)getSourceViewerConfiguration()).updateScanners();
		}
		super.handlePreferenceStoreChanged(event);
	}
}
