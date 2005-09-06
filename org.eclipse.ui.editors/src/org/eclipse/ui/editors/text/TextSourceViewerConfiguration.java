/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.util.StringTokenizer;

import org.eclipse.swt.SWT;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.DefaultUndoManager;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.ui.internal.editors.text.URLHyperlinkDetector;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;


/**
 * Source viewer configuration for the text editor.
 *
 * @since 3.0
 */
public class TextSourceViewerConfiguration extends SourceViewerConfiguration {

	/**
	 * The preference store used to initialize this configuration.
	 * <p>
	 * Note: protected since 3.1
	 * </p>
	 */
	protected IPreferenceStore fPreferenceStore;

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
	 * @since 3.2
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover() {
			protected boolean isIncluded(Annotation annotation) {
				return isShowInVerticalRuler(annotation);
			}
		};
	}

	/*
	 * @see DefaultAnnotationHover#isIncluded(Annotation)
	 * @since 3.2
	 */
	protected boolean isShowInVerticalRuler(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;
		String key= preference.getVerticalRulerPreferenceKey();
		// backward compatibility
		if (key != null && !fPreferenceStore.getBoolean(key))
			return false;
		
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getOverviewRulerAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover() {
			protected boolean isIncluded(Annotation annotation) {
				return isShowInOverviewRuler(annotation);
			}
		};
	}

	/*
	 * @see DefaultAnnotationHover#isIncluded(Annotation)
	 * @since 3.2
	 */
	protected boolean isShowInOverviewRuler(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;
		String key= preference.getOverviewRulerPreferenceKey();
		if (key == null || !fPreferenceStore.getBoolean(key))
			return false;
		
		return true;
	}

	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new DefaultTextHover(sourceViewer) {
			protected boolean isIncluded(Annotation annotation) {
				return isShownInText(annotation);
			}
		};
	}
	
	/*
	 * @see DefaultTextHover#isIncluded(Annotation)
	 * @since 3.2
	 */
	protected boolean isShownInText(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;
		String key= preference.getTextPreferenceKey();
		if (key != null) {
			if (!fPreferenceStore.getBoolean(key))
				return false;
		} else {
			key= preference.getHighlightPreferenceKey();
			if (key == null || !fPreferenceStore.getBoolean(key))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns the annotation preference for the given annotation.
	 *
	 * @param annotation the annotation
	 * @return the annotation preference or <code>null</code> if none
	 * @since 3.2
	 */
	private AnnotationPreference getAnnotationPreference(Annotation annotation) {
		if (annotation == null || fPreferenceStore == null)
			return null;
		return EditorsUI.getAnnotationPreferenceLookup().getAnnotationPreference(annotation);
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
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (sourceViewer == null || fPreferenceStore == null)
			return super.getHyperlinkDetectors(sourceViewer);

		if (!fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
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
		if (fPreferenceStore == null)
			return super.getHyperlinkStateMask(sourceViewer);

		String modifiers= fPreferenceStore.getString(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER);
		int modifierMask= computeStateMask(modifiers);
		if (modifierMask == -1) {
			// Fall back to stored state mask
			modifierMask= fPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_KEY_MODIFIER_MASK);
		}
		return modifierMask;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkPresenter(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null)
			return super.getHyperlinkPresenter(sourceViewer);

		return new DefaultHyperlinkPresenter(fPreferenceStore);
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

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getUndoManager(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null)
			return super.getUndoManager(sourceViewer);

		int undoHistorySize= fPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE);
		return new DefaultUndoManager(undoHistorySize);
	}
	
	/*
	 * @see SourceViewerConfiguration#getReconciler(ISourceViewer)
	 * <p>
	 * XXX: This is work in progress and can change anytime until API for 3.2 is frozen.
	 * </p>
	 * @since 3.2
	 */
//	public IReconciler getReconciler(ISourceViewer sourceViewer) {
//		if (fPreferenceStore == null)
//			return super.getReconciler(sourceViewer);
//		
//		IReconcilingStrategy strategy= new SpellingReconcileStrategy(sourceViewer, EditorsUI.getSpellingService(), "org.eclipse.ui.workbench.texteditor.spelling"); //$NON-NLS-1$
//		MonoReconciler reconciler= new MonoReconciler(strategy, false);
//		reconciler.setIsIncrementalReconciler(false);
//		reconciler.setProgressMonitor(new NullProgressMonitor());
//		reconciler.setDelay(500);
//		return reconciler;
//	}
}
