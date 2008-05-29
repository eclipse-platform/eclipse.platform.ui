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

package org.eclipse.ui.texteditor.spelling;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
import org.eclipse.ui.internal.texteditor.spelling.SpellingEngineRegistry;

/**
 * System wide spelling service.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class SpellingService {

	/**
	 * A named preference that controls if spelling is enabled or disabled.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String PREFERENCE_SPELLING_ENABLED= "spellingEnabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls which spelling engine is used.
	 * The value is the spelling engine's extension id.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 */
	public static final String PREFERENCE_SPELLING_ENGINE= "spellingEngine"; //$NON-NLS-1$

	/** Preferences */
	private IPreferenceStore fPreferences;

	/**
	 * Initializes the spelling service with the given preferences.
	 *
	 * @param preferences the preferences
	 * @see SpellingService#PREFERENCE_SPELLING_ENABLED
	 * @see SpellingService#PREFERENCE_SPELLING_ENGINE
	 */
	public SpellingService(IPreferenceStore preferences) {
		fPreferences= preferences;
	}

	/**
	 * Checks the given document. Reports all found spelling problems to the
	 * collector. The spelling engine is chosen based on the settings
	 * from the given preferences.
	 *
	 * @param document the document to check
	 * @param context the context
	 * @param collector the problem collector
	 * @param monitor the progress monitor, can be <code>null</code>
	 */
	public void check(IDocument document, SpellingContext context, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		check(document, new IRegion[] { new Region(0, document.getLength()) }, context, collector, monitor);
	}

	/**
	 * Checks the given regions in the given document. Reports all found
	 * spelling problems to the collector. The spelling engine is chosen
	 * based on the settings from the given preferences.
	 *
	 * @param document the document to check
	 * @param regions the regions to check
	 * @param context the context
	 * @param collector the problem collector
	 * @param monitor the progress monitor, can be <code>null</code>
	 */
	public void check(final IDocument document, final IRegion[] regions, final SpellingContext context, final ISpellingProblemCollector collector, final IProgressMonitor monitor) {
		try {
			collector.beginCollecting();
			if (fPreferences.getBoolean(PREFERENCE_SPELLING_ENABLED))
				try {
					final ISpellingEngine engine= createEngine(fPreferences);
					if (engine != null) {
						ISafeRunnable runnable= new ISafeRunnable() {
							public void run() throws Exception {
								engine.check(document, regions, context, collector, monitor);
							}
							public void handleException(Throwable x) {
							}
						};
						SafeRunner.run(runnable);
					}
				} catch (CoreException x) {
					TextEditorPlugin.getDefault().getLog().log(x.getStatus());
				}
		} finally {
			collector.endCollecting();
		}
	}

	/**
	 * Returns all spelling engine descriptors from extensions to the
	 * spelling engine extension point.
	 *
	 * @return all spelling engine descriptors
	 */
	public SpellingEngineDescriptor[] getSpellingEngineDescriptors() {
		SpellingEngineRegistry registry= getSpellingEngineRegistry();
		if (registry == null)
			return new SpellingEngineDescriptor[0];
		return registry.getDescriptors();
	}

	/**
	 * Returns the default spelling engine descriptor from extensions to
	 * the spelling engine extension point.
	 *
	 * @return the default spelling engine descriptor or
	 *         <code>null</code> if none could be found
	 */
	public SpellingEngineDescriptor getDefaultSpellingEngineDescriptor() {
		SpellingEngineRegistry registry= getSpellingEngineRegistry();
		if (registry == null)
			return null;
		return registry.getDefaultDescriptor();
	}

	/**
	 * Returns the descriptor of the active spelling engine based on the
	 * value of the <code>PREFERENCE_SPELLING_ENGINE</code> preference
	 * in the given preferences.
	 *
	 * @param preferences the preferences
	 * @return the descriptor of the active spelling engine or
	 *         <code>null</code> if none could be found
	 * @see SpellingService#PREFERENCE_SPELLING_ENGINE
	 */
	public SpellingEngineDescriptor getActiveSpellingEngineDescriptor(IPreferenceStore preferences) {
		SpellingEngineRegistry registry= getSpellingEngineRegistry();
		if (registry == null)
			return null;

		SpellingEngineDescriptor descriptor= null;
		if (preferences.contains(PREFERENCE_SPELLING_ENGINE))
			descriptor= registry.getDescriptor(preferences.getString(PREFERENCE_SPELLING_ENGINE));
		if (descriptor == null)
			descriptor= registry.getDefaultDescriptor();
		return descriptor;
	}

	/**
	 * Creates a spelling engine based on the value of the
	 * <code>PREFERENCE_SPELLING_ENGINE</code> preference in the given
	 * preferences.
	 *
	 * @param preferences the preferences
	 * @return the created spelling engine or <code>null</code> if none
	 *         could be created
	 * @throws CoreException if the creation failed
	 * @see SpellingService#PREFERENCE_SPELLING_ENGINE
	 */
	private ISpellingEngine createEngine(IPreferenceStore preferences) throws CoreException {
		SpellingEngineDescriptor descriptor= getActiveSpellingEngineDescriptor(preferences);
		if (descriptor != null)
			return descriptor.createEngine();
		return null;
	}

	/**
	 * Returns the spelling engine registry.
	 *
	 * @return the spelling engine registry or <code>null</code> if the plug-in has been shutdown
	 */
	private SpellingEngineRegistry getSpellingEngineRegistry() {
		return TextEditorPlugin.getDefault().getSpellingEngineRegistry();
	}
}
