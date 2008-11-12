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
package org.eclipse.ui.editors.text;

import java.nio.charset.UnmappableCharacterException;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferStatusCodes;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.source.ISharedTextColors;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.NLSUtility;
import org.eclipse.ui.keys.IBindingService;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.HyperlinkDetectorRegistry;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.spelling.SpellingService;


/**
 * The central class for access to this plug-in.
 * This class cannot be instantiated; all functionality is provided by
 * static methods.
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class EditorsUI {

	/**
	 * TextEditor Plug-in ID (value <code>"org.eclipse.ui.editors"</code>).
	 */
	public static final String PLUGIN_ID= "org.eclipse.ui.editors"; //$NON-NLS-1$

	/**
	 * The ID of the default text editor.
	 */
	public static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$


	/**
	 * Returns the annotation type lookup of this plug-in.
	 *
	 * @return the annotation type lookup
	 */
	public static AnnotationTypeLookup getAnnotationTypeLookup() {
		return EditorsPlugin.getDefault().getAnnotationTypeLookup();
	}

	/**
	 * Returns the annotation preference lookup of this plug-in.
	 *
	 * @return the annotation preference lookup
	 */
	public static AnnotationPreferenceLookup getAnnotationPreferenceLookup() {
		return EditorsPlugin.getDefault().getAnnotationPreferenceLookup();
	}

	/**
	 * Returns the preference store of this plug-in.
	 *
	 * @return this plug-in's preference store
	 */
	public static IPreferenceStore getPreferenceStore() {
		return EditorsPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Removes all preference which are handled by this plug-in's
	 * general preference pages from the given store and prevents
	 * setting the default values in the future.
	 * <p>
	 * To access the
	 * general preference from another plug-in use a
	 * {@link org.eclipse.ui.texteditor.ChainedPreferenceStore}:
	 * <pre>
	 *		List stores= new ArrayList(3);
	 *		stores.add(YourPlugin.getDefault().getPreferenceStore());
	 *		stores.add(EditorsUI.getPreferenceStore());
	 *		combinedStore= new ChainedPreferenceStore((IPreferenceStore[]) stores.toArray(new IPreferenceStore[stores.size()]));
	 *
	 * </pre>
	 * </p>
	 * <p>
	 * Note: In order to work this method must be called before
	 * the store's default values are set.
	 * </p>
	 *
	 * @param store the preference store to mark
	 */
	public static void useAnnotationsPreferencePage(IPreferenceStore store) {
		MarkerAnnotationPreferences.useAnnotationsPreferencePage(store);
	}

	/**
	 * Removes all preference which are handled by this plug-in's
	 * Quick Diff preference page from the given store and prevents
	 * setting the default values in the future.
	 * <p>
	 * To access the
	 * general preference from another plug-in use a
	 * {@link org.eclipse.ui.texteditor.ChainedPreferenceStore}:
	 * <pre>
	 *		List stores= new ArrayList(3);
	 *		stores.add(YourPlugin.getDefault().getPreferenceStore());
	 *		stores.add(EditorsUI.getPreferenceStore());
	 *		combinedStore= new ChainedPreferenceStore((IPreferenceStore[]) stores.toArray(new IPreferenceStore[stores.size()]));
	 *
	 * </pre>
	 * </p>
	 * <p>
	 * Note: In order to work this method must be called before
	 * the store's default values are set.
	 * </p>
	 *
	 * @param store the preference store to mark
	 */
	public static void useQuickDiffPreferencePage(IPreferenceStore store) {
		MarkerAnnotationPreferences.useQuickDiffPreferencePage(store);

		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE);
		store.setToDefault(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_DEFAULT_PROVIDER);
	}

	private EditorsUI() {
		// block instantiation
	}

	/**
	 * Returns the preferences of this plug-in.
	 * 
	 * @return the plug-in preferences
	 * @see org.eclipse.core.runtime.Plugin#getPluginPreferences()
	 * @deprecated As of 3.5, replaced by {@link #getPreferenceStore()}
	 */
	public static org.eclipse.core.runtime.Preferences getPluginPreferences() {
		return EditorsPlugin.getDefault().getPluginPreferences();
	}

	/**
	 * Returns the spelling service.
	 *
	 * @return the spelling service
	 * @since 3.1
	 */
	public static SpellingService getSpellingService() {
		return EditorsPlugin.getDefault().getSpellingService();
	}

	/**
	 * Returns the shared text colors of this plug-in.
	 *
	 * @return the shared text colors
	 * @since 3.3
	 */
	public static ISharedTextColors getSharedTextColors() {
		return EditorsPlugin.getDefault().getSharedTextColors();
	}

	/**
	 * Returns the registry that contains the hyperlink detectors contributed
	 * by  the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectors</code>
	 * extension point.
	 *
	 * @return the hyperlink detector registry
	 * @since 3.3
	 */
	public static HyperlinkDetectorRegistry getHyperlinkDetectorRegistry() {
		return EditorsPlugin.getDefault().getHyperlinkDetectorRegistry();
	}

	// --------------- Status codes for this plug-in ---------------

	// NOTE: See also IEditorsStatusConstants

	/**
	 * Editor UI plug-in status code indicating that an operation failed
	 * because a character could not be mapped using the given
	 * charset.
	 * <p>
	 * Value: {@value}</p>
	 *
	 * @see UnmappableCharacterException
	 * @since 3.2
	 */
	public static final int CHARSET_MAPPING_FAILED= 1;

	/**
	 * Editor UI plug-in status code indicating that state
	 * validation failed.
	 * <p>
	 * Value: {@value}</p>
	 *
	 * @see IFileBuffer#validateState(org.eclipse.core.runtime.IProgressMonitor, Object)
	 * @since 3.3
	 */
	public static final int STATE_VALIDATION_FAILED= IFileBufferStatusCodes.STATE_VALIDATION_FAILED;

	/**
	 * Editor UI plug-in status code indicating that
	 * a resource is marked derived.
	 * <p>
	 * Value: {@value}</p>
	 *
	 * @see IResource#isDerived()
	 * @since 3.3
	 */
	public static final int DERIVED_FILE= IFileBufferStatusCodes.DERIVED_FILE;

	/**
	 * Returns the tool tip affordance string.
	 *
	 * @return the affordance string which is empty if the preference is enabled
	 *			but the key binding not active or <code>null</code> if the
	 *			preference is disabled or the binding service is unavailable
	 * @since 3.3
	 */
	public static final String getTooltipAffordanceString() {
		if (!getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
			return null;

		IBindingService bindingService= (IBindingService)PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		if (bindingService == null)
			return null;

		String keySequence= bindingService.getBestActiveBindingFormattedFor(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
		if (keySequence == null)
			return ""; //$NON-NLS-1$

		return NLSUtility.format(TextEditorMessages.Editor_toolTip_affordance, keySequence);
	}

}
