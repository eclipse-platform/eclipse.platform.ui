/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * Information presenter used to present focused ("sticky") information shells.
 * 
 * @since 3.5
 */
public class FocusedInformationPresenter extends InformationPresenter {

	/**
	 * Information provider used to present focused information shells.
	 */
	public final static class InformationProvider implements IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {

		private IRegion fHoverRegion;
		private Object fHoverInfo;
		private IInformationControlCreator fControlCreator;

		public InformationProvider(IRegion hoverRegion, Object hoverInfo, IInformationControlCreator controlCreator) {
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
		/**
		 * {@inheritDoc}
		 *
		 * @deprecated As of 2.1, replaced by {@link IInformationProviderExtension#getInformation2(ITextViewer, IRegion)}
		 */
		public String getInformation(ITextViewer textViewer, IRegion subject) {
			return fHoverInfo == null ? null : fHoverInfo.toString();
		}
		/*
		 * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
		 * @since 3.2
		 */
		public Object getInformation2(ITextViewer textViewer, IRegion subject) {
			return fHoverInfo;
		}
		/*
		 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
		 */
		public IInformationControlCreator getInformationPresenterControlCreator() {
			return fControlCreator;
		}
	}
	
	/**
	 * The default information control creator.
	 */
	private static class DefaultInformationControlCreator implements IInformationControlCreator {
		public IInformationControl createInformationControl(Shell shell) {
			return new DefaultInformationControl(shell, true);
		}
	}
	
	
	private final ISourceViewer fSourceViewer;
	private final SourceViewerConfiguration fSourceViewerConfiguration;

	/**
	 * Creates a focused information presenter and installs it on the source viewer.
	 * 
	 * @param sourceViewer the source viewer
	 * @param sourceViewerConfiguration the configuration
	 */
	public FocusedInformationPresenter(ISourceViewer sourceViewer, SourceViewerConfiguration sourceViewerConfiguration) {
		super(new DefaultInformationControlCreator());
		fSourceViewer= sourceViewer;
		fSourceViewerConfiguration= sourceViewerConfiguration;
		
		// sizes: see org.eclipse.jface.text.TextViewer.TEXT_HOVER_*_CHARS
		setSizeConstraints(100, 12, true, true);
		install(sourceViewer);
		setDocumentPartitioning(sourceViewerConfiguration.getConfiguredDocumentPartitioning(sourceViewer));
	}

	/**
	 * Tries show a focused ("sticky") annotation hover.
	 *
	 * @param annotationHover the annotation hover to show
	 * @param line the line for which to show the hover
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public boolean openFocusedAnnotationHover(IAnnotationHover annotationHover, int line) {
		
		try {
			// compute the hover information
			Object hoverInfo;
			if (annotationHover instanceof IAnnotationHoverExtension) {
				IAnnotationHoverExtension extension= (IAnnotationHoverExtension) annotationHover;
				ILineRange hoverLineRange= extension.getHoverLineRange(fSourceViewer, line);
				if (hoverLineRange == null)
					return false;
				final int maxVisibleLines= Integer.MAX_VALUE; // allow any number of lines being displayed, as we support scrolling
				hoverInfo= extension.getHoverInfo(fSourceViewer, hoverLineRange, maxVisibleLines);
			} else {
				hoverInfo= annotationHover.getHoverInfo(fSourceViewer, line);
			}

			// hover region: the beginning of the concerned line to place the control right over the line
			IDocument document= fSourceViewer.getDocument();
			int offset= document.getLineOffset(line);
			String contentType= TextUtilities.getContentType(document, fSourceViewerConfiguration.getConfiguredDocumentPartitioning(fSourceViewer), offset, true);

			IInformationControlCreator controlCreator= null;
			if (annotationHover instanceof IInformationProviderExtension2) // this is undocumented, but left here for backwards compatibility
				controlCreator= ((IInformationProviderExtension2) annotationHover).getInformationPresenterControlCreator();
			else if (annotationHover instanceof IAnnotationHoverExtension)
				controlCreator= ((IAnnotationHoverExtension) annotationHover).getHoverControlCreator();

			IInformationProvider informationProvider= new InformationProvider(new Region(offset, 0), hoverInfo, controlCreator);

			setOffset(offset);
			setAnchor(AbstractInformationControlManager.ANCHOR_RIGHT);
			setMargins(4, 0); // AnnotationBarHoverManager sets (5,0), minus SourceViewer.GAP_SIZE_1
			setInformationProvider(informationProvider, contentType);
			showInformation();

			return true;

		} catch (BadLocationException e) {
			return false;
		}
	}
}


