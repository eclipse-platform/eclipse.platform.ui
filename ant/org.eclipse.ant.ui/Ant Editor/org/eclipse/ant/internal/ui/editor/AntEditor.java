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
 * 	   John-Mason P. Shackelford - bug 40255
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import java.io.File;
import java.util.ResourceBundle;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.ant.internal.ui.IAntUIHelpContextIds;
import org.eclipse.ant.internal.ui.IAntUIPreferenceConstants;
import org.eclipse.ant.internal.ui.editor.actions.FoldingActionGroup;
import org.eclipse.ant.internal.ui.editor.actions.InformationDispatchAction;
import org.eclipse.ant.internal.ui.editor.actions.OpenDeclarationAction;
import org.eclipse.ant.internal.ui.editor.outline.AntEditorContentOutlinePage;
import org.eclipse.ant.internal.ui.editor.text.AntEditorDocumentProvider;
import org.eclipse.ant.internal.ui.editor.text.AntFoldingStructureProvider;
import org.eclipse.ant.internal.ui.editor.text.IReconcilingParticipant;
import org.eclipse.ant.internal.ui.editor.text.XMLTextHover;
import org.eclipse.ant.internal.ui.model.AntElementNode;
import org.eclipse.ant.internal.ui.model.AntModel;
import org.eclipse.ant.internal.ui.model.AntModelChangeEvent;
import org.eclipse.ant.internal.ui.model.AntModelCore;
import org.eclipse.ant.internal.ui.model.AntProjectNode;
import org.eclipse.ant.internal.ui.model.IAntModelListener;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.IProjectionListener;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * The actual editor implementation for Eclipse's Ant integration.
 */
public class AntEditor extends TextEditor implements IReconcilingParticipant, IProjectionListener {
	
	/**
	 * Updates the Ant outline page selection and this editor's range indicator.
	 * 
	 * @since 3.0
	 */
	private class EditorSelectionChangedListener implements ISelectionChangedListener  {
		
		/**
		 * Installs this selection changed listener with the given selection provider. If
		 * the selection provider is a post selection provider, post selection changed
		 * events are the preferred choice, otherwise normal selection changed events
		 * are requested.
		 * 
		 * @param selectionProvider
		 */
		public void install(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}
				
			if (selectionProvider instanceof IPostSelectionProvider)  {
				IPostSelectionProvider provider= (IPostSelectionProvider) selectionProvider;
				provider.addPostSelectionChangedListener(this);
			} else  {
				selectionProvider.addSelectionChangedListener(this);
			}
		}

		/**
		 * Removes this selection changed listener from the given selection provider.
		 * 
		 * @param selectionProviderstyle
		 */
		public void uninstall(ISelectionProvider selectionProvider) {
			if (selectionProvider == null) {
				return;
			}
			
			if (selectionProvider instanceof IPostSelectionProvider)  {
				IPostSelectionProvider provider= (IPostSelectionProvider) selectionProvider;
				provider.removePostSelectionChangedListener(this);
			} else  {
				selectionProvider.removeSelectionChangedListener(this);
			}			
		}
		
		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			AntModel model= getAntModel();
			if (model == null) { //external file
				return;
			}
			ISelection selection= event.getSelection();
			AntElementNode node= null;
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection= (ITextSelection)selection;
				int offset= textSelection.getOffset();
				node= model.getNode(offset, false);
			}
		
			if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {
				synchronizeOutlinePage(node, true);
			}
			setSelection(node, false);
		}
	}
	
	static class TabConverter {
		
		private int fTabRatio;
		private ILineTracker fLineTracker;
		
		public void setNumberOfSpacesPerTab(int ratio) {
			fTabRatio= ratio;
		}
		
		public void setLineTracker(ILineTracker lineTracker) {
			fLineTracker= lineTracker;
		}
		
		private int insertTabString(StringBuffer buffer, int offsetInLine) {
			
			if (fTabRatio == 0) {
				return 0;
			}
				
			int remainder= offsetInLine % fTabRatio;
			remainder= fTabRatio - remainder;
			for (int i= 0; i < remainder; i++) {
				buffer.append(' ');
			}
			return remainder;
		}
		
		public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
			String text= command.text;
			if (text == null) {
				return;
			}
				
			int index= text.indexOf('\t');
			if (index > -1) {
				
				StringBuffer buffer= new StringBuffer();
				
				fLineTracker.set(command.text);
				int lines= fLineTracker.getNumberOfLines();
				
				try {
						
					for (int i= 0; i < lines; i++) {
						
						int offset= fLineTracker.getLineOffset(i);
						int endOffset= offset + fLineTracker.getLineLength(i);
						String line= text.substring(offset, endOffset);
						
						int position= 0;
						if (i == 0) {
							IRegion firstLine= document.getLineInformationOfOffset(command.offset);
							position= command.offset - firstLine.getOffset();	
						}
						
						int length= line.length();
						for (int j= 0; j < length; j++) {
							char c= line.charAt(j);
							if (c == '\t') {
								position += insertTabString(buffer, position);
							} else {
								buffer.append(c);
								++ position;
							}
						}
						
					}
						
					command.text= buffer.toString();
						
				} catch (BadLocationException x) {
				}
			}
		}
	}
	
	class StatusLineSourceViewer extends ProjectionViewer{
		
		private boolean fIgnoreTextConverters= false;
		
		public StatusLineSourceViewer(Composite composite, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, int styles) {
			super(composite, verticalRuler, overviewRuler, isOverviewRulerVisible(), styles);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {
			if (getTextWidget() == null || !redraws()) {
				return;
			}

			switch (operation) {
				case CONTENTASSIST_PROPOSALS:
					String msg= fContentAssistant.showPossibleCompletions();
					setStatusLineErrorMessage(msg);
					return;
				case UNDO:
					fIgnoreTextConverters= true;
					break;
				case REDO:
					fIgnoreTextConverters= true;
					break;
			}
			
			super.doOperation(operation);
		}
		
		public void setTextConverter(TabConverter tabConverter) {
			fTabConverter= tabConverter;
		}
		
		public void updateIndentationPrefixes() {
			SourceViewerConfiguration configuration= getSourceViewerConfiguration();
			String[] types= configuration.getConfiguredContentTypes(this);
			for (int i= 0; i < types.length; i++) {
				String[] prefixes= configuration.getIndentPrefixes(this, types[i]);
				if (prefixes != null && prefixes.length > 0) {
					setIndentPrefixes(prefixes, types[i]);
				}
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.TextViewer#customizeDocumentCommand(org.eclipse.jface.text.DocumentCommand)
		 */
		protected void customizeDocumentCommand(DocumentCommand command) {
			super.customizeDocumentCommand(command);
			if (!fIgnoreTextConverters && fTabConverter != null) {
				fTabConverter.customizeDocumentCommand(getDocument(), command);
			}
			fIgnoreTextConverters= false;
		}
	}

	/**
	 * Selection changed listener for the outline view.
	 */
    protected ISelectionChangedListener fSelectionChangedListener = new ISelectionChangedListener(){
        public void selectionChanged(SelectionChangedEvent event) {
        	fSelectionSetFromOutline= false;
            doSelectionChanged(event);
            fSelectionSetFromOutline= true;
        }
    };
    
    private IAntModelListener fAntModelListener= new IAntModelListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.ant.internal.ui.editor.outline.IDocumentModelListener#documentModelChanged(org.eclipse.ant.internal.ui.editor.outline.DocumentModelChangeEvent)
		 */
		public void antModelChanged(AntModelChangeEvent event) {
			if (event.getModel() == getAntModel()) {
				if (event.isPreferenceChange()) {
					updateEditorImage();
				}
				if (fFoldingStructureProvider != null) {
					fFoldingStructureProvider.updateFoldingRegions((AntModel)event.getModel());
				}
			}
		}
	};
    
    /**
     * The page that shows the outline.
     */
    protected AntEditorContentOutlinePage fOutlinePage;
    
    /** The editor's tab to spaces converter */
	private TabConverter fTabConverter;
	
	private boolean fInitialReconcile= true;
	
	/**
	 * The editor selection changed listener.
	 * 
	 * @since 3.0
	 */
	private EditorSelectionChangedListener fEditorSelectionChangedListener;

	private ProjectionSupport fProjectionSupport;
	
	private AntFoldingStructureProvider fFoldingStructureProvider;
	
	private boolean fSelectionSetFromOutline= false;

    private FoldingActionGroup fFoldingGroup;
  
    public AntEditor() {
        super();
		setSourceViewerConfiguration(new AntEditorSourceViewerConfiguration(this));
		setDocumentProvider(new AntEditorDocumentProvider());
		AntModelCore.getDefault().addAntModelListener(fAntModelListener);
		
		if (isFoldingEnabled()) {
			fFoldingStructureProvider= new AntFoldingStructureProvider(this);
		}
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
     */
    protected void createActions() {
        super.createActions();

        ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.ant.internal.ui.editor.AntEditorMessages"); //$NON-NLS-1$
        IAction action = new ContentAssistAction(bundle, "ContentAssistProposal.", this); //$NON-NLS-1$
        
        // This action definition is associated with the accelerator Ctrl+Space
        action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        setAction("ContentAssistProposal", action); //$NON-NLS-1$
        
		action = new TextOperationAction(bundle, "ContentFormat.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
		action.setActionDefinitionId("org.eclipse.ant.ui.format"); //$NON-NLS-1$
        setAction("ContentFormat", action); //$NON-NLS-1$
        
        action = new OpenDeclarationAction(this);
        setAction("OpenDeclaration", action); //$NON-NLS-1$
        
        fFoldingGroup= new FoldingActionGroup(this, getViewer());
        
        ResourceAction resAction= new TextOperationAction(AntEditorMessages.getResourceBundle(), "ShowTooltip.", this, ISourceViewer.INFORMATION, true); //$NON-NLS-1$
		resAction= new InformationDispatchAction(AntEditorMessages.getResourceBundle(), "ShowTooltip.", (TextOperationAction) resAction, this); //$NON-NLS-1$
		resAction.setActionDefinitionId("org.eclipse.ant.ui.showTooltip"); //$NON-NLS-1$
		setAction("ShowTooltip", resAction); //$NON-NLS-1$
    }

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 * Called from TextEditor.<init>
	 */
    protected void initializeEditor() {
		super.initializeEditor();
		setPreferenceStore(createCombinedPreferenceStore());
		setCompatibilityMode(false);
		setHelpContextId(IAntUIHelpContextIds.ANT_EDITOR);	
		setRulerContextMenuId("org.eclipse.ant.internal.ui.editor.AntEditor.RulerContext"); //$NON-NLS-1$
    }
   
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
    public Object getAdapter(Class key) {
        if (key.equals(IContentOutlinePage.class)) {
			return getOutlinePage();
        }
        
        if (fProjectionSupport != null) { 
        	Object adapter= fProjectionSupport.getAdapter(getSourceViewer(), key); 
        	if (adapter != null) {
            	return adapter;
            }
        }

        if (key == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { JavaUI.ID_PACKAGES, IPageLayout.ID_RES_NAV };
				}
			};
		}
        return super.getAdapter(key);
    }

	private AntEditorContentOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			fOutlinePage= new AntEditorContentOutlinePage(AntModelCore.getDefault(), this);
			fOutlinePage.addPostSelectionChangedListener(fSelectionChangedListener);
			setOutlinePageInput(getEditorInput());
		}
		return fOutlinePage;
	}

    private void doSelectionChanged(SelectionChangedEvent selectionChangedEvent) {
        IStructuredSelection selection= (IStructuredSelection)selectionChangedEvent.getSelection();

        if (!isActivePart() && AntUIPlugin.getActivePage() != null) {
			AntUIPlugin.getActivePage().bringToTop(this);
        }
        
        AntElementNode selectedXmlElement = (AntElementNode)selection.getFirstElement(); 
        if(selectedXmlElement != null) {
			setSelection(selectedXmlElement, !isActivePart());
        }
    }

    private boolean isActivePart() {
        IWorkbenchPart part= getActivePart();
        return part != null && part.equals(this);
    }
    
    public void setSelection(AntElementNode reference, boolean moveCursor) {
    	if (fSelectionSetFromOutline) {
    		//the work has all just been done via a selection setting in the outline
    		fSelectionSetFromOutline= false;
    		return;
    	}
        if (reference == null) {
        	if (moveCursor) {
        		 resetHighlightRange();
        	}
        	return;
        } 
    	while (reference.getImportNode() != null) {
    		reference= reference.getImportNode();
    	}
    	if (reference.isExternal()) {
    		return;
    	}
        
        StyledText textWidget= null;
        ISourceViewer sourceViewer= getSourceViewer();
        if (sourceViewer != null) {
            textWidget= sourceViewer.getTextWidget();
        }
        
        if (textWidget == null) {
            return;
        }
            
        try {
            int offset= reference.getOffset();
            if (offset < 0) {
                return;
            }
            int length= reference.getSelectionLength();
            int highLightLength= reference.getLength();
               
            textWidget.setRedraw(false);
            
            if (highLightLength > 0) {
                setHighlightRange(offset, highLightLength, moveCursor);
            }
            
            if (!moveCursor) {
                return;
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
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#affectsTextPresentation(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return ((AntEditorSourceViewerConfiguration)getSourceViewerConfiguration()).affectsTextPresentation(event);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property= event.getProperty();
		
		if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH.equals(property)) {
			Object value= event.getNewValue();
			StatusLineSourceViewer viewer= (StatusLineSourceViewer) getSourceViewer();
			if (value instanceof Integer) {
				viewer.getTextWidget().setTabs(((Integer) value).intValue());
			} else if (value instanceof String) {
				viewer.getTextWidget().setTabs(Integer.parseInt((String) value));
			}
			return;
		}
		
		if (AntEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS.equals(property)) {
			if (isTabConversionEnabled()) {
				startTabConversion();
			} else {
				stopTabConversion();
			}
			return;
		}
		
		AntEditorSourceViewerConfiguration sourceViewerConfiguration= (AntEditorSourceViewerConfiguration)getSourceViewerConfiguration();
		if (affectsTextPresentation(event)) {
			sourceViewerConfiguration.adaptToPreferenceChange(event);
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
		if (fFoldingStructureProvider != null) {
			fFoldingStructureProvider.setDocument(getDocumentProvider().getDocument(input));
		}
	}

	private void setOutlinePageInput(IEditorInput input) {
		if (fOutlinePage != null) {
			IDocumentProvider provider= getDocumentProvider();
			if (provider instanceof AntEditorDocumentProvider) {
				AntEditorDocumentProvider documentProvider= (AntEditorDocumentProvider) provider;
				AntModel model= documentProvider.getAntModel(input);
				fOutlinePage.setPageInput(model);
			}
		}
	}
	
	/**
	 * Returns the Ant model for the current editor input of this editor.
	 * @return the Ant model for this editor or <code>null</code>
	 */
	public AntModel getAntModel() {
		IDocumentProvider provider= getDocumentProvider();
		if (provider instanceof AntEditorDocumentProvider) {
			AntEditorDocumentProvider documentProvider= (AntEditorDocumentProvider) provider;
			return documentProvider.getAntModel(getEditorInput());
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fOverviewRuler= createOverviewRuler(getSharedColors());
		ISourceViewer viewer= new StatusLineSourceViewer(parent, ruler, getOverviewRuler(), styles);
		//ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}
	
	/**
	 * Ses the given message as error message to this editor's status line.
	 * @param msg message to be set
	 */
	protected void setStatusLineErrorMessage(String msg) {
		IEditorStatusLine statusLine= (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
		if (statusLine != null)
			statusLine.setMessage(true, msg, null);	
	}

	public void openReferenceElement() {
		ISelection selection= getSelectionProvider().getSelection();
		String errorMessage= null;
		AntElementNode node= null;
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)selection;
			ISourceViewer viewer= getSourceViewer();
			int textOffset= textSelection.getOffset();
			IRegion region= XMLTextHover.getRegion(viewer, textOffset);
			if (region != null) {
				IDocument document= viewer.getDocument();
				String text= null;
				try {
					text= document.get(region.getOffset(), region.getLength());
				} catch (BadLocationException e) {
				}
				if (text != null && text.length() > 0) {
					AntModel model= getAntModel();
					node= model.getReferenceNode(text);
					if (node == null) {
						node= model.getTargetNode(text);
						if (node == null) {
							node= model.getPropertyNode(text);
							if (node == null) {
								String path= model.getPath(text, region.getOffset());
								if (path != null) {
									errorMessage= openInEditor(path, model.getEditedFile());
									if (errorMessage == null) {
										return;
									}
								}
							}
						}
					}
				}
			}
		}
		if (node != null) {
			errorMessage= openNode(node);
			if (errorMessage == null) {
				return;
			}
		}
		if (errorMessage == null || errorMessage.length() == 0) {
			errorMessage= AntEditorMessages.getString("AntEditor.3"); //$NON-NLS-1$
		}
		setStatusLineErrorMessage(errorMessage);
		getSite().getShell().getDisplay().beep();
	}
	
	private String openNode(AntElementNode node) {
		String errorMessage= null;
		if (node.isExternal()) {
			String path= node.getFilePath();
			errorMessage= openInEditor(path, null);
		} else {
			setSelection(node, true);
		}
		return errorMessage;
	}
	
	private String openInEditor(String path, File buildFile) {
		File buildFileParent= null;
		if (buildFile != null) {
			buildFileParent= buildFile.getParentFile();
		}
		IFile file= AntUtil.getFileForLocation(path, buildFileParent);
		if (file != null && file.exists()) {
			try {
				IWorkbenchPage p= getEditorSite().getPage();
				if (p != null) {
					IDE.openEditor(p, file, isActivePart());
				}
				return null;
			} catch (PartInitException e) {
				return e.getLocalizedMessage();
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		
		IAction action= getAction("ContentFormat"); //$NON-NLS-1$
		if (action != null && action.isEnabled()) {
			menu.add(action);
		}

		action= getAction("OpenDeclaration"); //$NON-NLS-1$
		if (action != null) {
			String openGroup = "group.open"; //$NON-NLS-1$
    	    menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, new Separator(openGroup)); 
			menu.appendToGroup(openGroup, action);
		}
	}
	
	private void startTabConversion() {
		if (fTabConverter == null) {
			fTabConverter= new TabConverter();
			fTabConverter.setLineTracker(new DefaultLineTracker());
			fTabConverter.setNumberOfSpacesPerTab(getTabSize());
			StatusLineSourceViewer viewer= (StatusLineSourceViewer) getSourceViewer();
			viewer.setTextConverter(fTabConverter);
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
			viewer.updateIndentationPrefixes();
		}
	}
	
	private void stopTabConversion() {
		if (fTabConverter != null) {
			StatusLineSourceViewer viewer= (StatusLineSourceViewer) getSourceViewer();
			viewer.setTextConverter(null);
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=19270
			viewer.updateIndentationPrefixes();
			fTabConverter= null;
		}
	}
	
	protected int getTabSize() {
		IPreferenceStore preferences= getPreferenceStore();
		return preferences.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		ProjectionViewer projectionViewer= (ProjectionViewer) getSourceViewer(); 
        createFoldingSupport(projectionViewer);
        if (isFoldingEnabled()) {
        	projectionViewer.doOperation(ProjectionViewer.TOGGLE);
        }
		if (isTabConversionEnabled()) {
			startTabConversion();		
		}
		fEditorSelectionChangedListener= new EditorSelectionChangedListener();
		fEditorSelectionChangedListener.install(getSelectionProvider());
	}
	
	private void createFoldingSupport(ProjectionViewer projectionViewer) {
		fProjectionSupport= new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
    	fProjectionSupport.setHoverControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell shell) {
				return new AntSourceViewerInformationControl(shell);
			}
		});
        fProjectionSupport.install();
		((ProjectionViewer)getViewer()).addProjectionListener(this);
        
	}


	private boolean isFoldingEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(AntEditorPreferenceConstants.EDITOR_FOLDING_ENABLED);
	}

	protected boolean isTabConversionEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(AntEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fEditorSelectionChangedListener != null)  {
			fEditorSelectionChangedListener.uninstall(getSelectionProvider());
			fEditorSelectionChangedListener= null;
		}
		
		if (fProjectionSupport != null) {
			fProjectionSupport.dispose();
			fProjectionSupport= null;
		}
		
		AntModelCore.getDefault().removeAntModelListener(fAntModelListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		getAntModel().updateMarkers();
		updateEditorImage();
	}
	
	private void updateEditorImage() {
		Image titleImage= getTitleImage();
		if (titleImage == null) {
			return;
		}
		AntProjectNode node= getAntModel().getProjectNode();
		if (node != null) {
			postImageChange(node);
		}
	}
	
	private void updateForInitialReconcile() {
		if (getAntModel() == null) {
			return;
		}
		fInitialReconcile= false;
		updateEditorImage();
		getAntModel().updateForInitialReconcile();
	}
	
	private void postImageChange(final AntElementNode node) {
		Shell shell= getSite().getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (getSite().getShell() == null || getSite().getShell().isDisposed()) {
						return;
					}
					Image titleImage= getTitleImage();
					Image newImage= node.getImage();
					if (titleImage != newImage) {
						setTitleImage(newImage);
					}
				}
			});
		}
	}

	public void synchronizeOutlinePage(boolean checkIfOutlinePageActive) {
		if (getSelectionProvider() == null) {
			return;
		}
		AntElementNode node= getNode();
		synchronizeOutlinePage(node, checkIfOutlinePageActive);
		
	}
	
	protected void synchronize(boolean checkIfOutlinePageActive) {
		if (getSelectionProvider() == null) {
			return;
		}
		AntElementNode node= getNode();
		if (AntUIPlugin.getDefault().getPreferenceStore().getBoolean(IAntUIPreferenceConstants.OUTLINE_LINK_WITH_EDITOR)) {
			synchronizeOutlinePage(node, checkIfOutlinePageActive);
		}
		setSelection(node, false);
		
	}
	
	private AntElementNode getNode() {
		AntModel model= getAntModel();
		if (model == null) {
			return null;
		}
		AntElementNode node= null;
		ISelection selection= getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection)selection;
			int offset= textSelection.getOffset();
			node= model.getNode(offset, false);
		}
		return node;
	}
	
	protected void synchronizeOutlinePage(AntElementNode node, boolean checkIfOutlinePageActive) {
		if (fOutlinePage != null && !(checkIfOutlinePageActive && isAntOutlinePageAction())) {
			fOutlinePage.removePostSelectionChangedListener(fSelectionChangedListener);
			fOutlinePage.select(node);
			fOutlinePage.addPostSelectionChangedListener(fSelectionChangedListener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IReconcilingParticipant#reconciled()
	 */
	public void reconciled() {
		if (getSourceViewerConfiguration() == null) {
			return; //editor has been disposed.
		}
		if (fInitialReconcile) {
			updateForInitialReconcile();
		}
		IAutoIndentStrategy strategy= getSourceViewerConfiguration().getAutoIndentStrategy(null, null);
		if (strategy instanceof AntAutoIndentStrategy) {
			((AntAutoIndentStrategy)strategy).reconciled();
		}
		
		Shell shell= getSite().getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (getSite().getShell() == null || getSite().getShell().isDisposed()) {
						return;
					}
					synchronize(true);
				}
			});
		}
	}
	
	private boolean isAntOutlinePageAction() {
		IWorkbenchPart part= getActivePart();
		return part instanceof ContentOutline && ((ContentOutline)part).getCurrentPage() == fOutlinePage;
	}

	private IWorkbenchPart getActivePart() {
		IWorkbenchWindow window= getSite().getWorkbenchWindow();
		IPartService service= window.getPartService();
		return service.getActivePart();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetSelection(org.eclipse.jface.viewers.ISelection)
	 */
	protected void doSetSelection(ISelection selection) {
		super.doSetSelection(selection);
		synchronizeOutlinePage(true);
	}

	/**
	 * Creates a combined preference store, this store is read-only.
	 * 
	 * @return the combined preference store
	 * @since 3.0
	 */
	private IPreferenceStore createCombinedPreferenceStore() {
		IPreferenceStore antStore= AntUIPlugin.getDefault().getPreferenceStore();
		IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore(); 
		return new ChainedPreferenceStore(new IPreferenceStore[] { antStore, generalTextStore });
	}

    /**
     * Returns the viewer associated with this editor
     * @return The viewer associated with this editor
     */
    public ISourceViewer getViewer() {
        return getSourceViewer();
    }

    protected FoldingActionGroup getFoldingActionGroup() {
        return fFoldingGroup;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionEnabled()
     */
    public void projectionEnabled() {
        fFoldingStructureProvider= new AntFoldingStructureProvider(this);
		fFoldingStructureProvider.setDocument(getDocumentProvider().getDocument(getEditorInput()));
		fFoldingStructureProvider.updateFoldingRegions(getAntModel());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.projection.IProjectionListener#projectionDisabled()
     */
    public void projectionDisabled() {
        fFoldingStructureProvider= null;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.ant.ui.AntEditorScope" });  //$NON-NLS-1$
	}
}