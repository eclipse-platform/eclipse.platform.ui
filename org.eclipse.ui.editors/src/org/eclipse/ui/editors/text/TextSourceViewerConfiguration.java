/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;


/**
 * Source viewer configuration for the text editor.
 * 
 * @since 3.0
 */
public class TextSourceViewerConfiguration extends SourceViewerConfiguration {
	
	/** The preference store used to initialize this configuration. */
	private IPreferenceStore fPreferenceStore;

	/**
	 * A no-op implementation of <code>IAnnotationHover</code> that will trigger the text editor
	 * to set up annotation hover support.
	 */
	private static class NullHover implements IAnnotationHover {

		/*
		 * @see org.eclipse.jface.text.source.IAnnotationHover#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, int)
		 */
		public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
			return null;
		}
		
	}
	
	/**
	 * Creates a text source viewer configuration.
	 */
	public TextSourceViewerConfiguration() {
	}
	
	/**
	 * Creates a text source viewer configuration and
	 * initializes it with the given preference store.
	 * 
	 * @param preferenceStore	the preference store used to initialize this configuration
	 */
	public TextSourceViewerConfiguration(IPreferenceStore preferenceStore) {
		fPreferenceStore= preferenceStore;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new NullHover();
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getTabWidth(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null)
			return super.getTabWidth(sourceViewer);
		return fPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}
}
