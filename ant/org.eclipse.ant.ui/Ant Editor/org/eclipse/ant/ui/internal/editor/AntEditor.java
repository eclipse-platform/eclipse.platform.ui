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
import org.eclipse.ant.ui.internal.model.ColorManager;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.ant.ui.internal.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * The actual editor implementation for Eclipse's Ant integration.
 * 
 * @author Alf Schiefelbein
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
    };
    

    /**
     * The page that shows the outline.
     */
    protected AntEditorContentOutlinePage page;


    /**
     * Constructor for AntEditor.
     */
    public AntEditor() {
        super();
		setDocumentProvider(new AntEditorDocumentProvider(XMLCore.getDefault()));
		setPreferenceStore(AntUIPlugin.getDefault().getPreferenceStore());
    }


    /** The <code>AntEditor</code> implementation of this 
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
		setSourceViewerConfiguration(new AntEditorSourceViewerConfiguration(this));
		setRangeIndicator(new DefaultRangeIndicator());
		setCompatibilityMode(false);
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
			setOutlinePageInput(page, getEditorInput());
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
     * Returns wether the editor is active.
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
		setOutlinePageInput(page, input);
	}

	protected void setOutlinePageInput(AntEditorContentOutlinePage page, IEditorInput input) {
		if (page != null) {
			IDocumentProvider provider= getDocumentProvider();
			if (provider instanceof AntEditorDocumentProvider) {
				AntEditorDocumentProvider documentProvider= (AntEditorDocumentProvider) provider;
				AntModel model= documentProvider.getAntModel(input);
				page.setPageInput(model);
			}
		}
	}
	
	/** Preference key for showing the line number ruler */
	//private final static String LINE_NUMBER_RULER= AntEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
	/** Preference key for the foreground color of the line numbers */
	//private final static String LINE_NUMBER_COLOR= AntEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
	/** Preference key for showing the overview ruler */
	//private final static String OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
	/** Preference key for error indication in overview ruler */
	private final static String ERROR_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for warning indication in overview ruler */
	private final static String WARNING_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for info indication in overview ruler */
	private final static String INFO_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_INFO_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for task indication in overview ruler */
	private final static String TASK_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for bookmark indication in overview ruler */
	private final static String BOOKMARK_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for search result indication in overview ruler */
	private final static String SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;
	/** Preference key for unknown annotation indication in overview ruler */
	private final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER= AntEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;

	/** Preference key for error indication */
	private final static String ERROR_INDICATION= AntEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION;
	/** Preference key for error color */
	private final static String ERROR_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR;
	/** Preference key for warning indication */
	private final static String WARNING_INDICATION= AntEditorPreferenceConstants.EDITOR_WARNING_INDICATION;
	/** Preference key for warning color */
	private final static String WARNING_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_WARNING_INDICATION_COLOR;
	/** Preference key for info indication */
	private final static String INFO_INDICATION= AntEditorPreferenceConstants.EDITOR_INFO_INDICATION;
	/** Preference key for info color */
	private final static String INFO_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_INFO_INDICATION_COLOR;
	/** Preference key for task indication */
	private final static String TASK_INDICATION= AntEditorPreferenceConstants.EDITOR_TASK_INDICATION;
	/** Preference key for task color */
	private final static String TASK_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_TASK_INDICATION_COLOR;
	/** Preference key for bookmark indication */
	private final static String BOOKMARK_INDICATION= AntEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION;
	/** Preference key for bookmark color */
	private final static String BOOKMARK_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR;
	/** Preference key for search result indication */
	private final static String SEARCH_RESULT_INDICATION= AntEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION;
	/** Preference key for search result color */
	private final static String SEARCH_RESULT_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR;
	/** Preference key for unknown annotation indication */
	private final static String UNKNOWN_INDICATION= AntEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION;
	/** Preference key for unknown annotation color */
	private final static String UNKNOWN_INDICATION_COLOR= AntEditorPreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;

	/** Preference key for highlighting current line */
	private final static String CURRENT_LINE= AntEditorPreferenceConstants.EDITOR_CURRENT_LINE;
	/** Preference key for highlight color of current line */
	private final static String CURRENT_LINE_COLOR= AntEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
	/** Preference key for showing print marging ruler */
	private final static String PRINT_MARGIN= AntEditorPreferenceConstants.EDITOR_PRINT_MARGIN;
	/** Preference key for print margin ruler color */
	private final static String PRINT_MARGIN_COLOR= AntEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
	/** Preference key for print margin ruler column */
	private final static String PRINT_MARGIN_COLUMN= AntEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess= createAnnotationAccess();
		ISharedTextColors sharedColors= ColorManager.getDefault();
		fOverviewRuler= new OverviewRuler(fAnnotationAccess, VERTICAL_RULER_WIDTH, sharedColors);
		fOverviewRuler.addHeaderAnnotationType(AnnotationType.WARNING);
		fOverviewRuler.addHeaderAnnotationType(AnnotationType.ERROR);
		
		ISourceViewer sourceViewer= new SourceViewer(parent, ruler, fOverviewRuler, isOverviewRulerVisible(), styles);
		fSourceViewerDecorationSupport= new SourceViewerDecorationSupport(sourceViewer, fOverviewRuler, fAnnotationAccess, sharedColors);
		configureSourceViewerDecorationSupport();
		WorkbenchHelp.setHelp(sourceViewer.getTextWidget(), IAntUIHelpContextIds.ANT_EDITOR);
		return sourceViewer;
	}

	protected IAnnotationAccess createAnnotationAccess() {
		return new AnnotationAccess();
	}

	protected void configureSourceViewerDecorationSupport() {
		
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.UNKNOWN, UNKNOWN_INDICATION_COLOR, UNKNOWN_INDICATION, UNKNOWN_INDICATION_IN_OVERVIEW_RULER, 0);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.BOOKMARK, BOOKMARK_INDICATION_COLOR, BOOKMARK_INDICATION, BOOKMARK_INDICATION_IN_OVERVIEW_RULER, 1);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.TASK, TASK_INDICATION_COLOR, TASK_INDICATION, TASK_INDICATION_IN_OVERVIEW_RULER, 2);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.SEARCH, SEARCH_RESULT_INDICATION_COLOR, SEARCH_RESULT_INDICATION, SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER, 3);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.INFO, INFO_INDICATION_COLOR, INFO_INDICATION, INFO_INDICATION_IN_OVERVIEW_RULER, 4);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.WARNING, WARNING_INDICATION_COLOR, WARNING_INDICATION, WARNING_INDICATION_IN_OVERVIEW_RULER, 5);
		fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(AnnotationType.ERROR, ERROR_INDICATION_COLOR, ERROR_INDICATION, ERROR_INDICATION_IN_OVERVIEW_RULER, 6);
		
		fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
		fSourceViewerDecorationSupport.setMarginPainterPreferenceKeys(PRINT_MARGIN, PRINT_MARGIN_COLOR, PRINT_MARGIN_COLUMN);
		
		fSourceViewerDecorationSupport.setSymbolicFontName(getFontPropertyPreferenceKey());
	}
}
