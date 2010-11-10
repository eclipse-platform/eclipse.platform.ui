/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.HyperlinkDetectorRegistry;
import org.eclipse.ui.texteditor.spelling.SpellingCorrectionProcessor;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;


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
			return true;
		String key= preference.getVerticalRulerPreferenceKey();
		// backward compatibility
		if (key != null && !fPreferenceStore.getBoolean(key))
			return false;

		return true;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getOverviewRulerAnnotationHover(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.2
	 */
	public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover(true) {
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
			return true;
		String key= preference.getOverviewRulerPreferenceKey();
		if (key == null || !fPreferenceStore.getBoolean(key))
			return false;

		return true;
	}

	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
	 * @since 3.2
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		return new int[] { ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK };
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 * @since 3.2
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new TextHover(sourceViewer);
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
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getIndentPrefixes(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 * @since 3.3
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		String[] indentPrefixes= getIndentPrefixesForTab(getTabWidth(sourceViewer));
		if (indentPrefixes == null)
			return null;

		int length= indentPrefixes.length;
		if (length > 2 && fPreferenceStore != null && fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS))  {
			// Swap first with second last
			String first= indentPrefixes[0];
			indentPrefixes[0]= indentPrefixes[length - 2];
			indentPrefixes[length - 2]= first;
		}

		return indentPrefixes;
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

		return getRegisteredHyperlinkDetectors(sourceViewer);
	}

	/**
	 * Returns the registered hyperlink detectors which are used to detect
	 * hyperlinks in the given source viewer.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an array with hyperlink detectors or <code>null</code> if no hyperlink detectors are registered
	 * @since 3.3
	 */
	protected final IHyperlinkDetector[] getRegisteredHyperlinkDetectors(ISourceViewer sourceViewer) {
		HyperlinkDetectorRegistry registry= EditorsUI.getHyperlinkDetectorRegistry();

		Map targets= getHyperlinkDetectorTargets(sourceViewer);
		Assert.isNotNull(targets);

		IHyperlinkDetector[] result= null;
		Iterator iter= targets.entrySet().iterator();
		while (iter.hasNext()) {
			Entry target= (Entry)iter.next();
			String targetId= (String)target.getKey();
			IAdaptable context= (IAdaptable)target.getValue();
			result= merge(result, registry.createHyperlinkDetectors(targetId, context));
		}
		return result;
	}

	/**
	 * Returns the hyperlink detector targets supported by the
	 * given source viewer.
	 * <p>
	 * Subclasses are allowed to modify the returned map.
	 * </p>
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return the hyperlink detector targets with target id (<code>String</code>) as key
	 * 			and the target context (<code>IAdaptable</code>) as value
	 * @since 3.3
	 */
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		Map targets= new HashMap();
		targets.put(EditorsUI.DEFAULT_TEXT_EDITOR_ID, null);
		return targets;
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
			return new MultipleHyperlinkPresenter(new RGB(0, 0, 255));

		return new MultipleHyperlinkPresenter(fPreferenceStore);
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
		if (fPreferenceStore == null || !fPreferenceStore.contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE))
			return super.getUndoManager(sourceViewer);

		int undoHistorySize= fPreferenceStore.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_UNDO_HISTORY_SIZE);
		return new TextViewerUndoManager(undoHistorySize);
	}

	/**
	 * Returns the reconciler ready to be used with the given source viewer.
	 * <p>
	 * This implementation currently returns a {@link MonoReconciler} which
	 * is responsible for spell checking. In the future a different reconciler
	 * taking over more responsibilities might be returned.</p>
	 *
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.3
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null || !fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED))
			return null;

		SpellingService spellingService= EditorsUI.getSpellingService();
		if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) == null)
			return null;

		IReconcilingStrategy strategy= new SpellingReconcileStrategy(sourceViewer, spellingService);
		MonoReconciler reconciler= new MonoReconciler(strategy, false);
		reconciler.setDelay(500);
		return reconciler;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getQuickAssistAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.3
	 */
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		if (fPreferenceStore == null || !fPreferenceStore.getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED))
			return null;

		QuickAssistAssistant assistant= new QuickAssistAssistant();
		assistant.setQuickAssistProcessor(new SpellingCorrectionProcessor());
		assistant.setRestoreCompletionProposalSize(EditorsPlugin.getDefault().getDialogSettingsSection("quick_assist_proposal_size")); //$NON-NLS-1$
		assistant.setInformationControlCreator(getQuickAssistAssistantInformationControlCreator());

		return assistant;
	}

	/**
	 * Returns the information control creator for the quick assist assistant.
	 *
	 * @return the information control creator
	 * @since 3.3
	 */
	private IInformationControlCreator getQuickAssistAssistantInformationControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, EditorsPlugin.getAdditionalInfoAffordanceString());
			}
		};
	}

	/**
	 * Helper method to merge two {@link IHyperlinkDetector} arrays.
	 *
	 * @param array1 an array of hyperlink detectors or <code>null</code>
	 * @param array2 an array of hyperlink detectors or <code>null</code>
	 * @return an array with the merged hyperlink detectors or <code>null</code> if both given arrays are <code>null</code>
	 * @since 3.3
	 */
	private IHyperlinkDetector[] merge(IHyperlinkDetector[] array1, IHyperlinkDetector[] array2) {
		if (array1 == null && array2 == null)
			return null;
		else if (array1 == null)
			return array2;
		else if (array2 == null)
			return array1;
		else {
			IHyperlinkDetector[] allHyperlinkDetectors;
			int size= array1.length + array2.length;
			allHyperlinkDetectors= new IHyperlinkDetector[size];
			System.arraycopy(array1, 0, allHyperlinkDetectors, 0, array1.length);
			System.arraycopy(array2, 0, allHyperlinkDetectors, array1.length, array2.length);
			return allHyperlinkDetectors;
		}
	}

	/**
	 * Text hover with custom control creator that
	 * can show the tool tip affordance.
	 *
	 * @since 3.3
	 */
	private final class TextHover extends DefaultTextHover implements ITextHoverExtension {
		public TextHover(ISourceViewer sourceViewer) {
			super(sourceViewer);
		}

		protected boolean isIncluded(Annotation annotation) {
			return isShownInText(annotation);
		}

		/*
		 * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
		 */
		public IInformationControlCreator getHoverControlCreator() {
			return new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
				}
			};
		}
	}

}
