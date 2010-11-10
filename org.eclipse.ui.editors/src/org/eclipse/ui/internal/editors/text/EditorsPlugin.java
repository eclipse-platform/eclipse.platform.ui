/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink <stephan.wahlbrink@walware.de> - [templates] improve logging when reading templates into ContributionTemplateStore - https://bugs.eclipse.org/bugs/show_bug.cgi?id=212252
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.text.source.ISharedTextColors;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.AnnotationTypeHierarchy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.themes.IThemeManager;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.HyperlinkDetectorRegistry;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.spelling.SpellingService;

import org.eclipse.ui.editors.text.EditorsUI;

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

	/*
	 * @since 3.4
	 */
	public static void log(String message, Throwable e) {
		if (message == null)
			message= ""; //$NON-NLS-1$
		log(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IEditorsStatusConstants.INTERNAL_ERROR, message, e));
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
	 * Theme listener.
	 * @since 3.3
	 */
	private IPropertyChangeListener fThemeListener;

	/**
	 * Spelling service.
	 * @since 3.1
	 */
	private SpellingService fSpellingService;

	/**
	 * The hyperlink detector registry.
	 * @since 3.3
	 */
	private HyperlinkDetectorRegistry fHyperlinkDetectorRegistry;

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
	public synchronized void setMarkerAnnotationPreferences(MarkerAnnotationPreferences markerAnnotationPreferences) {
		Assert.isTrue(fMarkerAnnotationPreferences == null);
		fMarkerAnnotationPreferences= markerAnnotationPreferences;
	}

	/**
	 * Tells whether the marker annotation preferences are initialized.
	 *
	 * @return <code>true</code> if initialized
	 * @since 3.2
	 */
	public boolean isMarkerAnnotationPreferencesInitialized() {
		return fMarkerAnnotationPreferences != null;
	}

	/**
	 * Returns the marker annotation preferences.
	 *
	 * @return the marker annotation preferences
	 * @since 3.1
	 */
	public synchronized MarkerAnnotationPreferences getMarkerAnnotationPreferences() {
		if (!isMarkerAnnotationPreferencesInitialized())
			new MarkerAnnotationPreferences().getAnnotationPreferences(); // force creation of shared preferences
		return fMarkerAnnotationPreferences;
	}

	/*
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 * @since 3.3
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		if (PlatformUI.isWorkbenchRunning()) {
			fThemeListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty()))
						EditorsPluginPreferenceInitializer.setThemeBasedPreferences(getPreferenceStore(), true);
				}
			};
			PlatformUI.getWorkbench().getThemeManager().addPropertyChangeListener(fThemeListener);
		}
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

		if (fThemeListener != null) {
			if (PlatformUI.isWorkbenchRunning())
				PlatformUI.getWorkbench().getThemeManager().removePropertyChangeListener(fThemeListener);
			fThemeListener= null;
		}

		fAnnotationTypeLookup= null;
		fAnnotationPreferenceLookup= null;
		fAnnotationTypeHierarchy= null;
		fMarkerAnnotationPreferences= null;
		fHyperlinkDetectorRegistry= null;

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

	/**
	 * Returns the registry that contains the hyperlink detectors contributed
	 * by  the <code>org.eclipse.ui.workbench.texteditor.hyperlinkDetectors</code>
	 * extension point.
	 *
	 * @return the hyperlink detector registry
	 * @since 3.3
	 */
	public synchronized HyperlinkDetectorRegistry getHyperlinkDetectorRegistry() {
		if (fHyperlinkDetectorRegistry == null)
			fHyperlinkDetectorRegistry= new HyperlinkDetectorRegistry(getPreferenceStore());
		return fHyperlinkDetectorRegistry;
	}

	/**
	 * Returns the content assist additional info focus affordance string.
	 *
	 * @return the affordance string which is <code>null</code> if the
	 *			preference is disabled
	 *
	 * @see EditorsUI#getTooltipAffordanceString()
	 * @since 3.4
	 */
	public static final String getAdditionalInfoAffordanceString() {
		if (!EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE))
			return null;

		return TextEditorMessages.EditorsPlugin_additionalInfo_affordance;
	}

	/**
	 * Returns a section in the ui.editors plugin's dialog settings. If the section doesn't exist yet, it is created.
	 *
	 * @param name the name of the section
	 * @return the section of the given name
	 * @since 3.7
	 */
	public IDialogSettings getDialogSettingsSection(String name) {
		IDialogSettings dialogSettings= getDialogSettings();
		IDialogSettings section= dialogSettings.getSection(name);
		if (section == null) {
			section= dialogSettings.addNewSection(name);
		}
		return section;
	}

}
