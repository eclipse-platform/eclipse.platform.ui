/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;


/**
 * This class bundles the configuration space of a source viewer. Instances of
 * this class are passed to the <code>configure</code> method of
 * <code>ISourceViewer</code>.
 * <p>
 * Each method in this class get as argument the source viewer for which it
 * should provide a particular configuration setting such as a presentation
 * reconciler. Based on its specific knowledge about the returned object, the
 * configuration might share such objects or compute them according to some
 * rules.</p>
 * <p>
 * Clients should subclass and override just those methods which must be
 * specific to their needs.</p>
 *
 * @see org.eclipse.jface.text.source.ISourceViewer
 */
public class SourceViewerConfiguration {


	/**
	 * Creates a new source viewer configuration that behaves according to
	 * specification of this class' methods.
	 */
	public SourceViewerConfiguration() {
		super();
	}

	/**
	 * Returns the visual width of the tab character. This implementation always
	 * returns 4.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the tab width
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}

	/**
	 * Returns the undo manager for the given source viewer. This implementation
	 * always returns a new instance of <code>DefaultUndoManager</code> whose
	 * history length is set to 25.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an undo manager or <code>null</code> if no undo/redo should not be supported
	 */
	public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
		return new TextViewerUndoManager(25);
	}

	/**
	 * Returns the reconciler ready to be used with the given source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a reconciler or <code>null</code> if reconciling should not be supported
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Returns the presentation reconciler ready to be used with the given source viewer.
	 *
	 * @param sourceViewer the source viewer
	 * @return the presentation reconciler or <code>null</code> if presentation reconciling should not be supported
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler= new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		return reconciler;
	}

	/**
	 * Returns the content formatter ready to be used with the given source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a content formatter or <code>null</code> if formatting should not be supported
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Returns the content assistant ready to be used with the given source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a content assistant or <code>null</code> if content assist should not be supported
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Returns the quick assist assistant ready to be used with the given
	 * source viewer.
	 * This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return a quick assist assistant or <code>null</code> if quick assist should not be supported
	 * @since 3.2
	 */
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Returns the auto indentation strategy ready to be used with the given source viewer
	 * when manipulating text of the given content type. This implementation always
	 * returns an new instance of <code>DefaultAutoIndentStrategy</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the strategy is applicable
	 * @return the auto indent strategy or <code>null</code> if automatic indentation is not to be enabled
	 * @deprecated since 3.1 use {@link #getAutoEditStrategies(ISourceViewer, String)} instead
	 */
	public org.eclipse.jface.text.IAutoIndentStrategy getAutoIndentStrategy(ISourceViewer sourceViewer, String contentType) {
		return new org.eclipse.jface.text.DefaultAutoIndentStrategy();
	}

	/**
	 * Returns the auto edit strategies ready to be used with the given source viewer
	 * when manipulating text of the given content type. For backward compatibility, this implementation always
	 * returns an array containing the result of {@link #getAutoIndentStrategy(ISourceViewer, String)}.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the strategies are applicable
	 * @return the auto edit strategies or <code>null</code> if automatic editing is not to be enabled
	 * @since 3.1
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		return new IAutoEditStrategy[] { getAutoIndentStrategy(sourceViewer, contentType) };
	}

	/**
	 * Returns the default prefixes to be used by the line-prefix operation
	 * in the given source viewer for text of the given content type. This implementation always
	 * returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the prefix is applicable
	 * @return the default prefixes or <code>null</code> if the prefix operation should not be supported
	 * @since 2.0
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return null;
	}

	/**
	 * Returns the double-click strategy ready to be used in this viewer when double clicking
	 * onto text of the given content type. This implementation always returns a new instance of
	 * <code>DefaultTextDoubleClickStrategy</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the strategy is applicable
	 * @return a double-click strategy or <code>null</code> if double clicking should not be supported
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new DefaultTextDoubleClickStrategy();
	}

	/**
	 * Returns the prefixes to be used by the line-shift operation. This implementation
	 * always returns <code>new String[] { "\t", "    ", "" }</code>.
	 * <p>
	 * <strong>Note:</strong> <em>This default is incorrect but cannot be changed in order not
	 * to break any existing clients. Subclasses should overwrite this method and
	 * use {@link #getIndentPrefixesForTab(int)} if applicable.</em>
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type for which the prefix is applicable
	 * @return the prefixes or <code>null</code> if the prefix operation should not be supported
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "\t", "    ", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Computes and returns the indent prefixes for tab indentation
	 * which is represented as <code>tabSizeInSpaces</code>.
	 *
	 * @param tabWidth the display tab width
	 * @return the indent prefixes
	 * @see #getIndentPrefixes(ISourceViewer, String)
	 * @since 3.3
	 */
	protected String[] getIndentPrefixesForTab(int tabWidth) {
		String[] indentPrefixes= new String[tabWidth + 2];
		for (int i= 0; i <= tabWidth; i++) {
			char[] spaceChars= new char[i];
			Arrays.fill(spaceChars, ' ');
			String spaces= new String(spaceChars);
			if (i < tabWidth)
				indentPrefixes[i]= spaces + '\t';
			else
				indentPrefixes[i]= new String(spaces);
		}
		indentPrefixes[tabWidth + 1]= ""; //$NON-NLS-1$
		return indentPrefixes;
	}

	/**
	 * Returns the annotation hover which will provide the information to be
	 * shown in a hover popup window when requested for the given
	 * source viewer. This implementation always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an annotation hover or <code>null</code> if no hover support should be installed
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Returns the annotation hover which will provide the information to be
	 * shown in a hover popup window when requested for the overview ruler
	 * of the given source viewer.This implementation always returns the general
	 * annotation hover returned by <code>getAnnotationHover</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an annotation hover or <code>null</code> if no hover support should be installed
	 * @since 3.0
	 */
	public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
		return getAnnotationHover(sourceViewer);
	}

	/**
	 * Returns the SWT event state masks for which text hover are configured for
	 * the given content type.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type
	 * @return an <code>int</code> array with the configured SWT event state masks
	 * 			or <code>null</code> if text hovers are not supported for the given content type
	 * @since 2.1
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		return null;
	}

	/**
	 * Returns the text hover which will provide the information to be shown
	 * in a text hover popup window when requested for the given source viewer and
	 * the given content type. This implementation always returns <code>
	 * null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type
	 * @param stateMask the SWT event state mask
	 * @return a text hover or <code>null</code> if no hover support should be installed
	 * @since 2.1
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		if (stateMask == ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK)
			return getTextHover(sourceViewer, contentType);
		return null;
	}

	/**
	 * Returns the text hover which will provide the information to be shown
	 * in a text hover popup window when requested for the given source viewer and
	 * the given content type. This implementation always returns <code>
	 * null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param contentType the content type
	 * @return a text hover or <code>null</code> if no hover support should be installed
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return null;
	}

	/**
	 * Returns the information control creator. The creator is a factory creating information
	 * controls for the given source viewer. This implementation always returns a creator for
	 * <code>DefaultInformationControl</code> instances.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the information control creator or <code>null</code> if no information support should be installed
	 * @since 2.0
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		};
	}

	/**
	 * Returns the information presenter which will determine and shown
	 * information requested for the current cursor position. This implementation
	 * always returns <code>null</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an information presenter <code>null</code> if  no information presenter should be installed
	 * @since 2.0
	 */
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		return null;
	}

	/**
	 * Returns all configured content types for the given source viewer. This list
	 * tells the caller which content types must be configured for the given source
	 * viewer, i.e. for which content types the given source viewer's functionalities
	 * must be specified. This implementation always returns <code>
	 * new String[] { IDocument.DEFAULT_CONTENT_TYPE }</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the configured content types for the given viewer
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE };
	}

	/**
	 * Returns the configured partitioning for the given source viewer. The partitioning is
	 * used when the querying content types from the source viewer's input document.  This
	 * implementation always returns <code>IDocumentExtension3.DEFAULT_PARTITIONING</code>.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the configured partitioning
	 * @see #getConfiguredContentTypes(ISourceViewer)
	 * @since 3.0
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	/**
	 * Returns the hyperlink detectors which be used to detect hyperlinks
	 * in the given source viewer. This
	 * implementation always returns an array with an URL hyperlink detector.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an array with hyperlink detectors or <code>null</code> if no hyperlink support should be installed
	 * @since 3.1
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null)
			return null;

		return new IHyperlinkDetector[] { new URLHyperlinkDetector() };
	}

	/**
	 * Returns the hyperlink presenter for the given source viewer.
	 * This implementation always returns the {@link DefaultHyperlinkPresenter}.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the hyperlink presenter or <code>null</code> if no hyperlink support should be installed
	 * @since 3.1
	 */
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
		return new DefaultHyperlinkPresenter(new RGB(0, 0, 255));
	}

	/**
	 * Returns the SWT event state mask which in combination
	 * with the left mouse button activates hyperlinking.
	 * This implementation always returns the {@link SWT#MOD1}.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the SWT event state mask to activate hyperlink mode
	 * @since 3.1
	 */
	public int getHyperlinkStateMask(ISourceViewer sourceViewer) {
		return SWT.MOD1;
	}
}
