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

package org.eclipse.ui.texteditor.spelling;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
 * </p><p>
 * Not yet for public use. API under construction.
 * </p>
 * 
 * @since 3.1
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
	public void check(IDocument document, IRegion[] regions, SpellingContext context, ISpellingProblemCollector collector, IProgressMonitor monitor) {
		try {
			collector.beginReporting();
			if (fPreferences.getBoolean(PREFERENCE_SPELLING_ENABLED))
				try {
					ISpellingEngine engine= createEngine(fPreferences);
					if (engine != null)
						engine.check(document, regions, context, collector, monitor);
				} catch (CoreException x) {
					TextEditorPlugin.getDefault().getLog().log(x.getStatus());
				}
		} finally {
			collector.endReporting();
		}
	}

	/**
	 * Returns all spelling engine descriptors from extensions to the
	 * spelling engine extension point.
	 * 
	 * @return all spelling engine descriptors
	 */
	public SpellingEngineDescriptor[] getSpellingEngineDescriptors() {
		return SpellingEngineRegistry.getDefault().getDescriptors();
	}

	/**
	 * Returns the default spelling engine descriptor from extensions to
	 * the spelling engine extension point.
	 * 
	 * @return the default spelling engine descriptor or
	 *         <code>null</code> if none could be found
	 */
	public SpellingEngineDescriptor getDefaultSpellingEngineDescriptor() {
		return SpellingEngineRegistry.getDefault().getDefaultDescriptor();
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
		SpellingEngineDescriptor descriptor= null;
		if (preferences.contains(PREFERENCE_SPELLING_ENGINE))
			descriptor= SpellingEngineRegistry.getDefault().getDescriptor(preferences.getString(PREFERENCE_SPELLING_ENGINE));
		if (descriptor == null)
			descriptor= SpellingEngineRegistry.getDefault().getDefaultDescriptor();
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
}
