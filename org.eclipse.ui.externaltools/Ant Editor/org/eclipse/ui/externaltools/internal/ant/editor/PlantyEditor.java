package org.eclipse.ui.externaltools.internal.ant.editor;

//
// PlantyEditor.java
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
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.eclipse.ui.externaltools.internal.ant.editor.outline.PlantyContentOutlinePage;
import org.eclipse.ui.externaltools.internal.ant.editor.text.PlantyDocumentProvider;
import org.eclipse.ui.externaltools.internal.ant.editor.xml.XmlElement;

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
        // (T) that is the font problem (though it seems that we don't need it!
        //        IPreferenceStore store = PlantyPlugin.getDefault().getPreferenceStore();
        //        setPreferenceStore(store);
    }


    /** The <code>JavaEditor</code> implementation of this 
     * <code>AbstractTextEditor</code> method extend the 
     * actions to add those specific to the receiver
     */
    protected void createActions() {
        super.createActions();

        ContentAssistAction action = new ContentAssistAction(ResourceBundle.getBundle("org.eclipse.ui.externaltools.internal.ant.editor.PlantyMessages"), "ContentAssistProposal.", this);

        // This action definition is associated with the accelerator Ctrl+Space
        action.setActionDefinitionId("org.eclipse.jdt.ui.edit.text.java.content.assist.proposals");
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
            
            StyledText  textWidget= null;
            
            ISourceViewer sourceViewer= getSourceViewer();
            if (sourceViewer != null)
                textWidget= sourceViewer.getTextWidget();
            
            if (textWidget == null)
                return;
                
            try {
                
                int offset= reference.getOffset();
                int length= reference.getLength();
                
                if (offset < 0)
                    return;
                    
                textWidget.setRedraw(false);
                
                if(length >0) {
	                setHighlightRange(offset, length, moveCursor);
                }
                
                if (!moveCursor)
                    return;

                offset= reference.getOffset()+1;
                length= reference.getName().length();
                                            
//                offset= -1;
//                length= -1;
                
//                if (reference instanceof IMember) {
//                    range= ((IMember) reference).getNameRange();
//                    if (range != null) {
//                        offset= range.getOffset();
//                        length= range.getLength();
//                    }
//                } else if (reference instanceof IImportDeclaration) {
//                    String name= ((IImportDeclaration) reference).getElementName();
//                    if (name != null && name.length() > 0) {
//                        String content= reference.getSource();
//                        offset= range.getOffset() + content.indexOf(name);
//                        length= name.length();
//                    }
//                } else if (reference instanceof IPackageDeclaration) {
//                    String name= ((IPackageDeclaration) reference).getElementName();
//                    if (name != null && name.length() > 0) {
//                        String content= reference.getSource();
//                        offset= range.getOffset() + content.indexOf(name);
//                        length= name.length();
//                    }
//                }
                
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


	/** (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if (page != null)
			page.update();
	}

}
