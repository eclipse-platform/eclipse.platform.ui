/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Freescale - Teodor Madan - Show IP for active frame only (Bug 49730) 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.InstructionPointerManager;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.launch.Decoration;
import org.eclipse.debug.internal.ui.views.launch.DecorationManager;
import org.eclipse.debug.internal.ui.views.launch.SourceNotFoundEditorInput;
import org.eclipse.debug.internal.ui.views.launch.StandardDecoration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IInstructionPointerPresentation;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.debug.ui.sourcelookup.CommonSourceNotFoundEditorInput;
import org.eclipse.debug.ui.sourcelookup.ISourceLookupResult;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utility methods for looking up and displaying source.
 * 
 * @since 3.1
 */
public class SourceLookupFacility implements IPageListener, IPartListener2, IPropertyChangeListener {
    
    /**
     * Singleton source lookup facility
     */
    private static SourceLookupFacility fgDefault;
    
    /**
     * Contains a map of the editor to use for each workbench
     * page, when the 'reuse editor' preference is on. 
     */
    private Map fEditorsByPage;
    
    /**
     * Used to generate annotations for stack frames
     */
    private IInstructionPointerPresentation fPresentation = (IInstructionPointerPresentation) DebugUITools.newDebugModelPresentation();
    
    /**
     * Whether to re-use editors when displaying source.
     */
    private boolean fReuseEditor = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
	
    /**
     * Returns the source lookup facility
     * @return
     */
    public static SourceLookupFacility getDefault() {
        if (fgDefault == null) {
            fgDefault = new SourceLookupFacility();
        }
        return fgDefault;
    }
    
    /**
     * Performs cleanup
     */
    public static void shutdown() {
        if (fgDefault != null) {
            fgDefault.dispose();
        }
    }
    
    /**
     * Constructs a source lookup facility.
     */
    private SourceLookupFacility() {
        fEditorsByPage = new HashMap();
        DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
    }
    
    /**
     * Performs source lookup for the given artifact and returns the result.
     *  
     * @param artifact object for which source is to be resolved
     * @param locator the source locator to use, or <code>null</code>. When <code>null</code>
     *   a source locator is determined from the artifact, if possible. If the artifact
     *   is a debug element, the source locator from its associated launch is used. 
     * @return a source lookup result
     */
    public SourceLookupResult lookup(Object artifact, ISourceLocator locator) {
        SourceLookupResult result = new SourceLookupResult(artifact, null, null, null);
        IDebugElement debugElement = null;
        if (artifact instanceof IDebugElement) {
            debugElement = (IDebugElement)artifact;
        }
		if (locator == null) {
		    ILaunch launch = null;
		    if (debugElement != null) {
		        launch = debugElement.getLaunch();
		    }
		    if (launch != null) {
		        locator = launch.getSourceLocator();
		    }
		}
		if (locator != null) {
			String editorId =null;
			IEditorInput editorInput = null;
			Object sourceElement = null;
			if (locator instanceof ISourceLookupDirector) {
			    ISourceLookupDirector director = (ISourceLookupDirector)locator;
			    sourceElement = director.getSourceElement(artifact);
			} else {
			    if (artifact instanceof IStackFrame) {
			        sourceElement = locator.getSourceElement((IStackFrame)artifact);
			    }
			}
			if (sourceElement == null) {
				if (locator instanceof AbstractSourceLookupDirector) {
				    editorInput = new CommonSourceNotFoundEditorInput(artifact);
					editorId = IDebugUIConstants.ID_COMMON_SOURCE_NOT_FOUND_EDITOR;
				} else {
				    if (artifact instanceof IStackFrame) {
				        IStackFrame frame = (IStackFrame)artifact;
					    editorInput = new SourceNotFoundEditorInput(frame);
						editorId = IInternalDebugUIConstants.ID_SOURCE_NOT_FOUND_EDITOR;
				    }
				}
			} else {
				ISourcePresentation presentation= null;
				if (locator instanceof ISourcePresentation) {
					presentation= (ISourcePresentation) locator;
				} else {
				    if (debugElement != null) {
				        presentation= getPresentation(debugElement.getModelIdentifier());
				    }
				}
				if (presentation != null) {
					editorInput= presentation.getEditorInput(sourceElement);
				}
				if (editorInput != null && presentation != null) {
					editorId= presentation.getEditorId(editorInput, sourceElement);
				}				
			}
			result.setEditorInput(editorInput);
			result.setEditorId(editorId);
			result.setSourceElement(sourceElement);
		}
		return result;
    }
    
    /**
     * Returns the model presentation for the given debug model, or <code>null</code>
     * if none.
     * 
     * @param id debug model id
     * @return presentation for the model, or <code>null</code> if none.
     */
	protected IDebugModelPresentation getPresentation(String id) {
		return ((DelegatingModelPresentation)DebugUIPlugin.getModelPresentation()).getPresentation(id);
	}   
	
	/**
	 * Returns an editor presentation.
	 * 
	 * @return an editor presentation
	 */
	protected IDebugEditorPresentation getEditorPresentation() {
	    return (DelegatingModelPresentation)DebugUIPlugin.getModelPresentation();
	}
    
    /**
     * Opens an editor in the given workbench page for the given source lookup
     * result. Has no effect if the result has an unknown editor id or editor input.
     * The editor is opened, positioned, and annotated.
     * <p>
     * Honor's the user preference of whether to re-use editors when displaying source.
     * </p> 
     * @param result source lookup result to display
     * @param page the page to display the result in
     */
    public void display(ISourceLookupResult result, IWorkbenchPage page) {
		IEditorPart editor= openEditor(result, page);
		if (editor == null) {
			return;
		}
		IStackFrame frame = null;
        if (result.getArtifact() instanceof IStackFrame) {
            frame = (IStackFrame) result.getArtifact();
        }		
		// position and annotate editor for stack frame
        if (frame != null) {
			IDebugEditorPresentation editorPresentation = getEditorPresentation();
            if (editorPresentation.addAnnotations(editor, frame)) {
				Decoration decoration = new StandardDecoration(editorPresentation, editor, frame.getThread());
				DecorationManager.addDecoration(decoration);				
			} else {
				// perform standard positioning and annotations
				ITextEditor textEditor = null;
				if (editor instanceof ITextEditor) {					
					textEditor = (ITextEditor)editor;
				} else {
					textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
				}
				if (textEditor != null) {
					positionEditor(textEditor, frame);
					InstructionPointerManager.getDefault().removeAnnotations(textEditor); 
					Annotation annotation = fPresentation.getInstructionPointerAnnotation(textEditor, frame);
					InstructionPointerManager.getDefault().addAnnotation(textEditor, frame, annotation);
				}
			}
		}        
    }
    
	/**
	 * Opens the editor used to display the source for an element selected in
	 * this view and returns the editor that was opened or <code>null</code> if
	 * no editor could be opened.
	 */
	private IEditorPart openEditor(ISourceLookupResult result, IWorkbenchPage page) {
		IEditorPart editor = null;
		IEditorInput input= result.getEditorInput();
		String id= result.getEditorId();
		if (input == null || id == null) {
			return null;
		}
		
		if (fReuseEditor) {
			IEditorReference[] references = page.findEditors(input, id, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
			if (references.length > 0) {
				// activate the editor we want to reuse
				IEditorPart refEditor= references[0].getEditor(false);
				editor = refEditor;
				page.bringToTop(editor);	
			}
			if (editor == null) {
			    IEditorPart editorForPage = getEditor(page);
				if (editorForPage == null || editorForPage.isDirty() || page.isEditorPinned(editorForPage)) {
				    // open a new editor
					editor = openEditor(page, input, id);
					editorForPage = editor;
				} else if (editorForPage instanceof IReusableEditor && editorForPage.getSite().getId().equals(id)) {
				    // re-use editor
					page.reuseEditor((IReusableEditor)editorForPage, input);
					editor = editorForPage;
                    if(!page.isPartVisible(editor)) {
                        page.bringToTop(editor);
                    }
				} else {
				    // close editor, open a new one
					editor = openEditor(page, input, id);
					page.closeEditor(editorForPage, false);
					editorForPage = editor;
				}
				setEditor(page, editorForPage);
			}
		} else {
			// Open a new editor
			editor = openEditor(page, input, id);
		}
		return editor;
	}   
	
	/**
	 * Positions the text editor for the given stack frame
	 */
	private void positionEditor(ITextEditor editor, IStackFrame frame) {
		try {
			int charStart = frame.getCharStart();
			if (charStart >= 0) {
				editor.selectAndReveal(charStart, 0);
				return;
			}
			int lineNumber = frame.getLineNumber();
			lineNumber--; // Document line numbers are 0-based. Debug line numbers are 1-based.
			IRegion region= getLineInformation(editor, lineNumber);
			if (region != null) {
				editor.selectAndReveal(region.getOffset(), 0);
			}
		} catch (DebugException e) {
		}
	}
	
	/**
	 * Returns the line information for the given line in the given editor
	 */
	private IRegion getLineInformation(ITextEditor editor, int lineNumber) {
		IDocumentProvider provider= editor.getDocumentProvider();
		IEditorInput input= editor.getEditorInput();
		try {
			provider.connect(input);
		} catch (CoreException e) {
			return null;
		}
		try {
			IDocument document= provider.getDocument(input);
			if (document != null)
				return document.getLineInformation(lineNumber);
		} catch (BadLocationException e) {
		} finally {
			provider.disconnect(input);
		}
		return null;
	}	
	/**
	 * Opens an editor in the workbench and returns the editor that was opened
	 * or <code>null</code> if an error occurred while attempting to open the
	 * editor.
	 */
	private IEditorPart openEditor(final IWorkbenchPage page, final IEditorInput input, final String id) {
		final IEditorPart[] editor = new IEditorPart[] {null};
		Runnable r = new Runnable() {
			public void run() {
				if (!page.getWorkbenchWindow().getWorkbench().isClosing()) {
					try {
						editor[0] = page.openEditor(input, id, false, IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
					} catch (PartInitException e) {
						DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), 
							DebugUIViewsMessages.LaunchView_Error_1,  
							DebugUIViewsMessages.LaunchView_Exception_occurred_opening_editor_for_debugger__2,  
							e);
					}
				}
			}
		}; 
		BusyIndicator.showWhile(DebugUIPlugin.getStandardDisplay(), r);
		return editor[0];
	}	

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageActivated(IWorkbenchPage page) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageClosed(IWorkbenchPage page) {
        fEditorsByPage.remove(page);
        page.removePartListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageOpened(IWorkbenchPage page) {
    	page.addPartListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partActivated(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partClosed(IWorkbenchPartReference partRef) {
        // clear the cached editor for the page if it has been closed
        IWorkbenchPage page = partRef.getPage();
        IEditorPart editor = getEditor(page);
        IWorkbenchPart part = partRef.getPart(false);
		if (part != null && part.equals(editor)) {
			fEditorsByPage.remove(page);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partOpened(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partHidden(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partVisible(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
		if (property.equals(IDebugUIConstants.PREF_REUSE_EDITOR)) {
			fReuseEditor = DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_REUSE_EDITOR);
		}
    }
    
    /**
     * Returns the editor to use to display source in the given page, or
     * <code>null</code> if a new editor should be opened.
     * 
     * @param page workbench page
     * @return the editor to use to display source in the given page, or
     * <code>null</code> if a new editor should be opened
     */
    protected IEditorPart getEditor(IWorkbenchPage page) {
        return (IEditorPart) fEditorsByPage.get(page);
    }
    
    /**
     * Sets the editor to use to display source in the given page, or
     * <code>null</code> if a new editor should be opened.
     * 
     * @param page workbench page
     * @return the editor to use to display source in the given page, or
     * <code>null</code> if a new editor should be opened
     */
    protected void setEditor(IWorkbenchPage page, IEditorPart editorPart) {
        if (editorPart == null) {
            fEditorsByPage.remove(page);
        } else {
            fEditorsByPage.put(page, editorPart);
        }
        page.addPartListener(this);
        page.getWorkbenchWindow().addPageListener(this);
    } 
  
    /**
     * Performs cleanup.
     */
    protected void dispose() {
        DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
        fEditorsByPage.clear();
        fPresentation.dispose();
    }
}
