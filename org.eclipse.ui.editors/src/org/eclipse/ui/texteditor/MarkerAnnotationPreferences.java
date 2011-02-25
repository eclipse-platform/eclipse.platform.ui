/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.Bundle;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Objects of this class provide access to all extensions declared for the <code>markerAnnotationSpecification</code> extension point.
 * The extensions are represented as instances of {@link org.eclipse.ui.texteditor.AnnotationPreference}.
 *
 * @since 2.1
 */
public class MarkerAnnotationPreferences {

	/**
	 * Initializes the given preference store with the default marker annotation values.
	 *
	 * @param store the preference store to be initialized
	 * @since 3.0
	 */
	public static void initializeDefaultValues(IPreferenceStore store) {

		boolean ignoreAnnotationsPrefPage= store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.USE_ANNOTATIONS_PREFERENCE_PAGE);
		boolean ignoreQuickDiffPrefPage= store.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE);

		MarkerAnnotationPreferences preferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();

			if (ignoreAnnotationsPrefPage && info.isIncludeOnPreferencePage() && isComplete(info))
				continue;

			if (ignoreQuickDiffPrefPage && (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffChange") //$NON-NLS-1$
					|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffAddition")) //$NON-NLS-1$
					|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) //$NON-NLS-1$
				))
				continue;

			store.setDefault(info.getTextPreferenceKey(), info.getTextPreferenceValue());
			store.setDefault(info.getOverviewRulerPreferenceKey(), info.getOverviewRulerPreferenceValue());
			if (info.getVerticalRulerPreferenceKey() != null)
				store.setDefault(info.getVerticalRulerPreferenceKey(), info.getVerticalRulerPreferenceValue());
			PreferenceConverter.setDefault(store, info.getColorPreferenceKey(), info.getColorPreferenceValue());
			if (info.getShowInNextPrevDropdownToolbarActionKey() != null)
				store.setDefault(info.getShowInNextPrevDropdownToolbarActionKey(), info.isShowInNextPrevDropdownToolbarAction());
			if (info.getIsGoToNextNavigationTargetKey() != null)
				store.setDefault(info.getIsGoToNextNavigationTargetKey(), info.isGoToNextNavigationTarget());
			if (info.getIsGoToPreviousNavigationTargetKey() != null)
				store.setDefault(info.getIsGoToPreviousNavigationTargetKey(), info.isGoToPreviousNavigationTarget());
			if (info.getHighlightPreferenceKey() != null)
				store.setDefault(info.getHighlightPreferenceKey(), info.getHighlightPreferenceValue());
			if (info.getTextStylePreferenceKey() != null)
				store.setDefault(info.getTextStylePreferenceKey(), info.getTextStyleValue());
		}
	}

	/**
	 * Removes the marker annotation values which are shown on the
	 * general Annotations page  from the given store and prevents
	 * setting the default values in the future.
	 * <p>
	 * Note: In order to work this method must be called before any
	 *       call to {@link #initializeDefaultValues(IPreferenceStore)}
	 * </p>
	 * <p>
	 * This method is not part of the API and must only be called
	 * by {@link org.eclipse.ui.editors.text.EditorsUI}
	 * </p>
	 *
	 * @param store the preference store to be initialized
	 * @throws IllegalStateException if not called by {@link org.eclipse.ui.editors.text.EditorsUI}
	 * @since 3.0
	 */
	public static void useAnnotationsPreferencePage(IPreferenceStore store) throws IllegalStateException {
		checkAccess();

		store.putValue(AbstractDecoratedTextEditorPreferenceConstants.USE_ANNOTATIONS_PREFERENCE_PAGE, Boolean.toString(true));

		MarkerAnnotationPreferences preferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();

			// Only reset annotations shown on Annotations preference page
			if (!info.isIncludeOnPreferencePage() || !isComplete(info))
				continue;

			store.setToDefault(info.getTextPreferenceKey());
			store.setToDefault(info.getOverviewRulerPreferenceKey());
			if (info.getVerticalRulerPreferenceKey() != null)
				store.setToDefault(info.getVerticalRulerPreferenceKey());
			store.setToDefault(info.getColorPreferenceKey());
			if (info.getShowInNextPrevDropdownToolbarActionKey() != null)
				store.setToDefault(info.getShowInNextPrevDropdownToolbarActionKey());
			if (info.getIsGoToNextNavigationTargetKey() != null)
				store.setToDefault(info.getIsGoToNextNavigationTargetKey());
			if (info.getIsGoToPreviousNavigationTargetKey() != null)
				store.setToDefault(info.getIsGoToPreviousNavigationTargetKey());
			if (info.getHighlightPreferenceKey() != null)
				store.setToDefault(info.getHighlightPreferenceKey());
			if (info.getTextStylePreferenceKey() != null)
				store.setToDefault(info.getTextStylePreferenceKey());
		}
	}

	/**
	 * Removes the Quick Diff marker annotation values which are shown on the
	 * general Quick Diff page from the given store and prevents
	 * setting the default values in the future.
	 * <p>
	 * Note: In order to work this method must be called before any
	 *       call to {@link #initializeDefaultValues(IPreferenceStore)}
	 * </p>
	 * <p>
	 * This method is not part of the API and must only be called
	 * by {@link EditorsUI}
	 * </p>
	 *
	 * @param store the preference store to be initialized
	 * @throws IllegalStateException if not called by {@link EditorsUI}
	 * @since 3.0
	 */
	public static void useQuickDiffPreferencePage(IPreferenceStore store) throws IllegalStateException {
		checkAccess();

		store.putValue(AbstractDecoratedTextEditorPreferenceConstants.USE_QUICK_DIFF_PREFERENCE_PAGE, Boolean.toString(true));

		MarkerAnnotationPreferences preferences= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();

			// Only reset annotations shown on Quick Diff preference page

			if (!(info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffChange") //$NON-NLS-1$
				|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffAddition")) //$NON-NLS-1$
				|| (info.getAnnotationType().equals("org.eclipse.ui.workbench.texteditor.quickdiffDeletion")) //$NON-NLS-1$
			))
				continue;

			store.setToDefault(info.getTextPreferenceKey());
			store.setToDefault(info.getOverviewRulerPreferenceKey());
			if (info.getVerticalRulerPreferenceKey() != null)
				store.setToDefault(info.getVerticalRulerPreferenceKey());
			store.setToDefault(info.getColorPreferenceKey());
			if (info.getShowInNextPrevDropdownToolbarActionKey() != null)
				store.setToDefault(info.getShowInNextPrevDropdownToolbarActionKey());
			if (info.getIsGoToNextNavigationTargetKey() != null)
				store.setToDefault(info.getIsGoToNextNavigationTargetKey());
			if (info.getIsGoToPreviousNavigationTargetKey() != null)
				store.setToDefault(info.getIsGoToPreviousNavigationTargetKey());
			if (info.getHighlightPreferenceKey() != null)
				store.setToDefault(info.getHighlightPreferenceKey());
			if (info.getTextStylePreferenceKey() != null)
				store.setToDefault(info.getTextStylePreferenceKey());
		}
	}

	private static final class AccessChecker extends SecurityManager {
		public Class[] getClassContext() {
			return super.getClassContext();
		}
	}
	/**
	 * Checks correct access.
	 *
	 * @throws IllegalStateException if not called by {@link EditorsUI}
	 * @since 3.0
	 */
	private static void checkAccess() throws IllegalStateException {
		Class[] elements = new AccessChecker().getClassContext();
		if (!(elements[3].equals(EditorsUI.class)
				|| elements[4].equals(EditorsUI.class)))
			throw new IllegalStateException();
	}


	/** The list of extension fragments. */
	private List/*<AnnotationPreference>*/ fFragments;
	/** The list of extensions. */
	private List/*<AnnotationPreference>*/ fPreferences;

	/**
	 * Creates a new marker annotation preferences to access
	 * marker annotation preferences.
	 */
	public MarkerAnnotationPreferences() {
		this(false);
	}

	/**
	 * Creates a new marker annotation preferences to access
	 * marker annotation preferences.
	 * @param initFromPreferences tells this instance to initialize itself from the preferences
	 *
	 * @since 3.2
	 */
	private MarkerAnnotationPreferences(boolean initFromPreferences) {
		if (initFromPreferences)
			initializeSharedMakerAnnotationPreferences();
	}

	/**
	 * Returns all extensions provided for the <code>markerAnnotationSpecification</code> extension point.
	 *
	 * @return all extensions provided for the <code>markerAnnotationSpecification</code> extension point
	 *         (element type: {@link AnnotationPreference})
	 */
	public List getAnnotationPreferences() {
		if (fPreferences == null)
			initialize();
		return fPreferences;
	}

	/**
	 * Returns all extensions provided for the <code>markerAnnotationSpecification</code>
	 * extension point including fragments. Fragments share the preference part
	 * with a marker annotation specifications provided for a super type but do
	 * change the presentation part.
	 *
	 * @return all extensions provided for the <code>markerAnnotationSpecification</code>
	 *         extension point including fragments (element type: {@link AnnotationPreference})
	 */
	public List getAnnotationPreferenceFragments() {
		if (fFragments == null)
			initialize();
		return fFragments;
	}

	private void initialize() {
		synchronized (EditorsPlugin.getDefault()) {
			if (!EditorsPlugin.getDefault().isMarkerAnnotationPreferencesInitialized())
				EditorsPlugin.getDefault().setMarkerAnnotationPreferences(new MarkerAnnotationPreferences(true));
		}

		MarkerAnnotationPreferences sharedPrefs= EditorsPlugin.getDefault().getMarkerAnnotationPreferences();

		fFragments= cloneAnnotationPreferences(sharedPrefs.fFragments);
		fPreferences= cloneAnnotationPreferences(sharedPrefs.fPreferences);
	}

	/**
	 * Reads all extensions provided for the <code>markerAnnotationSpecification</code> extension point and
	 * translates them into <code>AnnotationPreference</code> objects.
	 */
	private void initializeSharedMakerAnnotationPreferences() {

		// initialize lists - indicates that the initialization happened
		fFragments= new ArrayList(2);
		fPreferences= new ArrayList(2);

		// populate list
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(EditorsUI.PLUGIN_ID, "markerAnnotationSpecification"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
			for (int i= 0; i < elements.length; i++) {
				AnnotationPreference spec= createSpec(elements[i]);
				if (spec != null)
					fFragments.add(spec);
				if (isComplete(spec))
					fPreferences.add(spec);
			}
		}
	}

	/**
	 * Deeply clones the given list of <code>AnnotationPreference</code>.
	 *
	 * @param annotationPreferences a list of <code>AnnotationPreference</code>
	 * @return the cloned list of cloned annotation preferences
	 * @since 3.1
	 */
	private List cloneAnnotationPreferences(List annotationPreferences) {
		if (annotationPreferences == null)
			return null;
		List clone= new ArrayList(annotationPreferences.size());
		Iterator iter= annotationPreferences.iterator();
		while (iter.hasNext())
			clone.add(clone(((AnnotationPreference)iter.next())));
		return clone;
	}

	/**
	 * Clones the given annotation preference.
	 *
	 * @param annotationPreference the annotation preference to clone
	 * @return the cloned annotation preference
	 * @since 3.1
	 */
	private AnnotationPreference clone(AnnotationPreference annotationPreference) {
		if (annotationPreference == null)
			return null;

		AnnotationPreference clone= new AnnotationPreference();
		if (annotationPreference.getAnnotationType() != null) {
			clone.setAnnotationType(annotationPreference.getAnnotationType());
			clone.merge(annotationPreference);
		}

		return clone;
	}

	/**
	 * Checks if <code>spec</code> has all the attributes previously required
	 * by the marker annotation preference extension point. These are: color, text
	 * and overview ruler preference keys.
	 *
	 * @param spec the <code>AnnotationPreference</code> to check
	 * @return <code>true</code> if <code>spec</code> is complete, <code>false</code> otherwise
	 * @since 3.0
	 */
	private static boolean isComplete(AnnotationPreference spec) {
		return spec.getColorPreferenceKey() != null
				&& spec.getColorPreferenceValue() != null
				&& spec.getTextPreferenceKey() != null
				&& spec.getOverviewRulerPreferenceKey() != null;
	}

	/**
	 * Creates a <code>AnnotationPreference</code> the given configuration element.
	 *
	 * @param element the configuration element
	 * @return the created annotation preference
	 */
	private AnnotationPreference createSpec(IConfigurationElement element) {

		String s;
		int i;
		boolean b;

		ReadOnlyAnnotationPreference info= new ReadOnlyAnnotationPreference();

		s= element.getAttribute("annotationType");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setAnnotationType(s);

		s= element.getAttribute("label");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setPreferenceLabel(s);

		s= element.getAttribute("markerType");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setMarkerType(s);

		s= element.getAttribute("markerSeverity");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			i= StringConverter.asInt(s, IMarker.SEVERITY_INFO);
			info.setSeverity(i);
		}

		s= element.getAttribute("textPreferenceKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setTextPreferenceKey(s);

		s= element.getAttribute("textPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setTextPreferenceValue(b);
		}

		s= element.getAttribute("highlightPreferenceKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setHighlightPreferenceKey(s);

		s= element.getAttribute("highlightPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setHighlightPreferenceValue(b);
		}

		s= element.getAttribute("overviewRulerPreferenceKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setOverviewRulerPreferenceKey(s);

		s= element.getAttribute("overviewRulerPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setOverviewRulerPreferenceValue(b);
		}

		s= element.getAttribute("verticalRulerPreferenceKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setVerticalRulerPreferenceKey(s);

		s= element.getAttribute("verticalRulerPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, true);
			info.setVerticalRulerPreferenceValue(b);
		}

		s= element.getAttribute("colorPreferenceKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setColorPreferenceKey(s);

		s= element.getAttribute("colorPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			RGB rgb= StringConverter.asRGB(s);
			info.setColorPreferenceValue(rgb == null ? new RGB(0,0,0) : rgb);
		}

		s= element.getAttribute("presentationLayer");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			i= StringConverter.asInt(s, 0);
			info.setPresentationLayer(i);
		}

		s= element.getAttribute("contributesToHeader");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setContributesToHeader(b);
		}

		s= element.getAttribute("showInNextPrevDropdownToolbarActionKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setShowInNextPrevDropdownToolbarActionKey(s);

		s= element.getAttribute("showInNextPrevDropdownToolbarAction");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setShowInNextPrevDropdownToolbarAction(b);
		}

		s= element.getAttribute("isGoToNextNavigationTargetKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setIsGoToNextNavigationTargetKey(s);

		s= element.getAttribute("isGoToNextNavigationTarget");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setIsGoToNextNavigationTarget(b);
		}

		s= element.getAttribute("isGoToPreviousNavigationTargetKey");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setIsGoToPreviousNavigationTargetKey(s);

		s= element.getAttribute("isGoToPreviousNavigationTarget");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {
			b= StringConverter.asBoolean(s, false);
			info.setIsGoToPreviousNavigationTarget(b);
		}

		s= element.getAttribute("symbolicIcon");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setSymbolicImageName(s);

		s= element.getAttribute("icon");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setImageDescriptor(getImageDescriptor(s, element));

		s= element.getAttribute("quickFixIcon");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setQuickFixImageDescriptor(getImageDescriptor(s, element));

		s= element.getAttribute("annotationImageProvider"); //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setAnnotationImageProviderData(element, "annotationImageProvider"); //$NON-NLS-1$

		s= element.getAttribute("textStylePreferenceKey"); //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setTextStylePreferenceKey(s);

		s= element.getAttribute("textStylePreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0) {

			if (AnnotationPreference.STYLE_BOX.equals(s)
					|| AnnotationPreference.STYLE_DASHED_BOX.equals(s)
					|| AnnotationPreference.STYLE_IBEAM.equals(s)
					|| AnnotationPreference.STYLE_SQUIGGLES.equals(s)
					|| AnnotationPreference.STYLE_PROBLEM_UNDERLINE.equals(s)
					|| AnnotationPreference.STYLE_UNDERLINE.equals(s))
				info.setTextStyleValue(s);
			else
				info.setTextStyleValue(AnnotationPreference.STYLE_NONE);

		}

		s= element.getAttribute("includeOnPreferencePage");  //$NON-NLS-1$
		info.setIncludeOnPreferencePage(s == null || StringConverter.asBoolean(s, true));

		info.markReadOnly();

		return info;
	}

	/**
	 * Returns the image descriptor for the icon path specified by the given configuration
	 * element.
	 *
	 * @param iconPath the icon path
	 * @param element the configuration element
	 * @return the image descriptor
	 * @since 3.0
	 */
	private ImageDescriptor getImageDescriptor(String iconPath, IConfigurationElement element) {
		String pluginId= element.getContributor().getName();
		Bundle bundle= Platform.getBundle(pluginId);
		if (bundle == null)
			return null;

		URL url= FileLocator.find(bundle, new Path(iconPath), null);
		if (url != null)
			return ImageDescriptor.createFromURL(url);

		return ImageDescriptor.getMissingImageDescriptor();
	}
}
