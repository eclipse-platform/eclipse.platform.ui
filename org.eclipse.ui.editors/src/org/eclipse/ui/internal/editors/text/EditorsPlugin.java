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
package org.eclipse.ui.internal.editors.text;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.ISharedTextColors;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.ui.internal.texteditor.AnnotationTypeHierarchy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.spelling.SpellingService;

/**
 * Represents the editors plug-in. It provides a series of convenience methods such as
 * access to the shared text colors and the log.
 *
 * @since 2.1
 */
public class EditorsPlugin extends AbstractUIPlugin {
	private static EditorsPlugin fgInstance;

	public static EditorsPlugin getDefault() {
		return fgInstance;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		if (message == null)
			message= ""; //$NON-NLS-1$
		log(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IEditorsStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi= new MultiStatus(EditorsUI.PLUGIN_ID, IEditorsStatusConstants.INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IEditorsStatusConstants.INTERNAL_ERROR, TextEditorMessages.EditorsPlugin_internal_error, e));
	}


	private ISharedTextColors fSharedTextColors;
	private AnnotationTypeLookup fAnnotationTypeLookup;
	private AnnotationPreferenceLookup fAnnotationPreferenceLookup;
	private AnnotationTypeHierarchy fAnnotationTypeHierarchy;
	private MarkerAnnotationPreferences fMarkerAnnotationPreferences;

	/**
	 * Spelling service.
	 * @since 3.1
	 */
	private SpellingService fSpellingService;

	public EditorsPlugin() {
		Assert.isTrue(fgInstance == null);
		fgInstance= this;
	}

	/**
	 * Returns the shared text colors of this plug-in.
	 *
	 * @return the shared text colors
	 * @since 3.0
	 */
	public ISharedTextColors getSharedTextColors() {
		if (fSharedTextColors == null)
			fSharedTextColors= new SharedTextColors();
		return fSharedTextColors;
	}

	/**
	 * Returns the annotation type lookup of this plug-in.
	 *
	 * @return the annotation type lookup
	 * @since 3.0
	 */
	public AnnotationTypeLookup getAnnotationTypeLookup() {
		if (fAnnotationTypeLookup == null)
			fAnnotationTypeLookup= new AnnotationTypeLookup();
		return fAnnotationTypeLookup;
	}

	/**
	 * Returns the annotation preference lookup of this plug-in.
	 *
	 * @return the annotation preference lookup
	 * @since 3.0
	 */
	public AnnotationPreferenceLookup getAnnotationPreferenceLookup() {
		if (fAnnotationPreferenceLookup == null)
			fAnnotationPreferenceLookup= new AnnotationPreferenceLookup();
		return fAnnotationPreferenceLookup;
	}

	/**
	 * Returns the annotation type hierarchy for this plug-in.
	 *
	 * @return the annotation type hierarchy
	 * @since 3.0
	 */
	public AnnotationTypeHierarchy getAnnotationTypeHierarchy() {
		if (fAnnotationTypeHierarchy == null)
			fAnnotationTypeHierarchy= new AnnotationTypeHierarchy();
		return fAnnotationTypeHierarchy;
	}
	
	/**
	 * Sets the marker annotation preferences.
	 * <p>
	 * Note: This method must only be called once.
	 * </p>
	 *
	 * @param markerAnnotationPreferences the marker annotation preferences
	 * @since 3.1
	 */
	public void setMarkerAnnotationPreferences(MarkerAnnotationPreferences markerAnnotationPreferences) {
		Assert.isTrue(fMarkerAnnotationPreferences == null);
		fMarkerAnnotationPreferences= markerAnnotationPreferences;
	}
	
	/**
	 * Returns the marker annotation preferences.
	 *
	 * @return the marker annotation preferences
	 * @since 3.1
	 */
	public MarkerAnnotationPreferences getMarkerAnnotationPreferences() {
		return fMarkerAnnotationPreferences;
	}

	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 * @since 3.0
	 */
	public void stop(BundleContext context) throws Exception {
		if (fSharedTextColors != null) {
			fSharedTextColors.dispose();
			fSharedTextColors= null;
		}

		fAnnotationTypeLookup= null;
		fAnnotationPreferenceLookup= null;
		fAnnotationTypeHierarchy= null;
		fMarkerAnnotationPreferences= null;

		super.stop(context);
	}

	/**
	 * Returns the spelling service.
	 *
	 * @return the spelling service
	 * @since 3.1
	 */
	public SpellingService getSpellingService() {
		if (fSpellingService == null)
			fSpellingService= new SpellingService(getPreferenceStore());
		return fSpellingService;
	}
}
