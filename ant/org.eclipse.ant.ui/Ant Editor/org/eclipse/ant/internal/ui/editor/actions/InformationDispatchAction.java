/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.actions;

import java.util.ResourceBundle;

import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.editor.AntEditorSourceViewerConfiguration;
import org.eclipse.ant.internal.ui.editor.text.AntDocumentSetupParticipant;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;

public class InformationDispatchAction extends TextEditorAction {

	/** The wrapped text operation action. */
	private TextOperationAction fTextOperationAction;
	
	 /** The information presenter that shows all information with scroll bars */
	private InformationPresenter fInformationPresenter;
	
	/**
	 * Creates a dispatch action.
	 * 
	 * @param resourceBundle the resource bundle
	 * @param prefix the prefix
	 * @param textOperationAction the text operation action
	 */
	public InformationDispatchAction(ResourceBundle resourceBundle, String prefix, TextOperationAction textOperationAction, AntEditor editor) {
		super(resourceBundle, prefix, editor);
		if (textOperationAction == null) {
			throw new IllegalArgumentException();
		}
		fTextOperationAction= textOperationAction;
	}
	
	/*
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {

		/**
		 * Information provider used to present the information.
		 * 
		 * @since 3.0
		 */
		class InformationProvider implements IInformationProvider, IInformationProviderExtension2 {

			private IRegion fHoverRegion;
			private String fHoverInfo;
			private IInformationControlCreator fControlCreator;
			
			InformationProvider(IRegion hoverRegion, String hoverInfo, IInformationControlCreator controlCreator) {
				fHoverRegion= hoverRegion;
				fHoverInfo= hoverInfo;
				fControlCreator= controlCreator;
			}
			/*
			 * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer, int)
			 */
			public IRegion getSubject(ITextViewer textViewer, int invocationOffset) {					
				return fHoverRegion;
			}
			/*
			 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
			 */
			public String getInformation(ITextViewer textViewer, IRegion subject) {
				return fHoverInfo;
			}
			/*
			 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
			 * @since 3.0
			 */
			public IInformationControlCreator getInformationPresenterControlCreator() {
				return fControlCreator;
			}
		}

		ISourceViewer sourceViewer= ((AntEditor)getTextEditor()).getViewer();
		if (sourceViewer == null) {	
			fTextOperationAction.run();
			return;
		}
			
		if (sourceViewer instanceof ITextViewerExtension4)  {
			ITextViewerExtension4 extension4= (ITextViewerExtension4) sourceViewer;
			if (extension4.moveFocusToWidgetToken()) {
				return;
			}
		}
		
		if (! (sourceViewer instanceof ITextViewerExtension2)) {
			fTextOperationAction.run();
			return;
		}
			
		ITextViewerExtension2 textViewerExtension2= (ITextViewerExtension2) sourceViewer;
		
		// does a text hover exist?
		ITextHover textHover= textViewerExtension2.getCurrentTextHover();
		if (textHover == null) {
			fTextOperationAction.run();
			return;				
		}

		Point hoverEventLocation= textViewerExtension2.getHoverEventLocation();
		int offset= computeOffsetAtLocation(sourceViewer, hoverEventLocation.x, hoverEventLocation.y);
		if (offset == -1) {
			fTextOperationAction.run();
			return;				
		}				

		try {
			// get the text hover content
			String contentType= TextUtilities.getContentType(sourceViewer.getDocument(), AntDocumentSetupParticipant.ANT_PARTITIONING, offset, true);

			IRegion hoverRegion= textHover.getHoverRegion(sourceViewer, offset);						
			if (hoverRegion == null)
				return;
			
			String hoverInfo= textHover.getHoverInfo(sourceViewer, hoverRegion);

			IInformationControlCreator controlCreator= null;				
			if (textHover instanceof IInformationProviderExtension2)
				controlCreator= ((IInformationProviderExtension2)textHover).getInformationPresenterControlCreator();

			IInformationProvider informationProvider= new InformationProvider(hoverRegion, hoverInfo, controlCreator);
			InformationPresenter presenter= getInformationPresenter();
			presenter.setOffset(offset);	
			presenter.setDocumentPartitioning(AntDocumentSetupParticipant.ANT_PARTITIONING);
			presenter.setInformationProvider(informationProvider, contentType);
			presenter.showInformation();

		} catch (BadLocationException e) {				
		}
	}

	// modified version from TextViewer
	private int computeOffsetAtLocation(ITextViewer textViewer, int x, int y) {
		
		StyledText styledText= textViewer.getTextWidget();
		IDocument document= textViewer.getDocument();
		
		if (document == null) {
			return -1;		
		}

		try {
			int widgetLocation= styledText.getOffsetAtLocation(new Point(x, y));
			if (textViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) textViewer;
				return extension.widgetOffset2ModelOffset(widgetLocation);
			} 
			IRegion visibleRegion= textViewer.getVisibleRegion();
			return widgetLocation + visibleRegion.getOffset();
		} catch (IllegalArgumentException e) {
			return -1;	
		}
	}
	
	private InformationPresenter getInformationPresenter() {
		if (fInformationPresenter == null) {
			IInformationControlCreator informationControlCreator= AntEditorSourceViewerConfiguration.getInformationPresenterControlCreator();
			fInformationPresenter= new InformationPresenter(informationControlCreator);
			fInformationPresenter.setSizeConstraints(60, 10, true, true);		
			fInformationPresenter.install(((AntEditor)getTextEditor()).getViewer());
		} 
		return fInformationPresenter;
	}
}
