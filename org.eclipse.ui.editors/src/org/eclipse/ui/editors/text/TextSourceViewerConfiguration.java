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

import java.util.StringTokenizer;

import org.eclipse.swt.SWT;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.hyperlink.DefaultHyperlinkController;
import org.eclipse.jface.text.hyperlink.IHyperlinkController;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
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
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinksEnabled(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public boolean getHyperlinksEnabled(ISourceViewer sourceViewer) {
		return fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null)
			return null;
		
		return new IHyperlinkDetector[] {
				new URLHyperlinkDetector(sourceViewer),
		};
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkStateMask(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public int getHyperlinkStateMask(ISourceViewer sourceViewer) {
		String modifiers= fPreferenceStore.getString(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER);
		int modifierMask= computeStateMask(modifiers);
		if (modifierMask == -1) {
			// Fall back to stored state mask
			modifierMask= fPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK);
		}
		return modifierMask;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkController(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IHyperlinkController getHyperlinkController(ISourceViewer sourceViewer) {
		return new DefaultHyperlinkController(fPreferenceStore);
	}
	
	/**
	 * Maps the localized modifier name to a code in the same
	 * manner as #findModifier.
	 * 
	 * @param modifierName the modifier name
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 * @since 3.1
	 */
	protected static final int findLocalizedModifier(String modifierName) {
		if (modifierName == null)
			return 0;
		
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.CTRL)))
			return SWT.CTRL;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.SHIFT)))
			return SWT.SHIFT;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.ALT)))
			return SWT.ALT;
		if (modifierName.equalsIgnoreCase(Action.findModifierString(SWT.COMMAND)))
			return SWT.COMMAND;

		return 0;
	}
	
	/**
	 * Computes the state mask out of the given modifiers string.
	 * 
	 * @param modifiers a string containing modifiers
	 * @return the state mask
	 * @since 3.1
	 */
	protected static final int computeStateMask(String modifiers) {
		if (modifiers == null)
			return -1;
	
		if (modifiers.length() == 0)
			return SWT.NONE;

		int stateMask= 0;
		StringTokenizer modifierTokenizer= new StringTokenizer(modifiers, ",;.:+-* "); //$NON-NLS-1$
		while (modifierTokenizer.hasMoreTokens()) {
			int modifier= findLocalizedModifier(modifierTokenizer.nextToken());
			if (modifier == 0 || (stateMask & modifier) == modifier)
				return -1;
			stateMask= stateMask | modifier;
		}
		return stateMask;
	}
}
