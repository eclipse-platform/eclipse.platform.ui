/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;


/**
 * Objects of this class provide access to all extensions declared for the <code>markerAnnotationSpecification</code> extension point.
 * The extensions are represented as instances of <code>AnnotationPreference</code>.
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
		MarkerAnnotationPreferences preferences= new MarkerAnnotationPreferences();
		Iterator e= preferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference info= (AnnotationPreference) e.next();
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
		}
	}
	
	
	
	/** The list of extension fragments */
	private List fFragments;
	/** The list of extensions */
	private List fPreferences;
	
	/**
	 * Creates a new marker annotation preferences accessor.
	 */
	public MarkerAnnotationPreferences() {
	}
	
	/**
	 * Returns all extensions provided for the <code>markerAnnotationSpecification</code> extension point.
	 * 
	 * @return all extensions provided for the <code>markerAnnotationSpecification</code> extension point
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
	 *         extension point including fragments
	 */
	public List getAnnotationPreferenceFragments() {
		if (fFragments == null)
			initialize();
		return fFragments;
	}
	
	/**
	 * Reads all extensions provided for the <code>markerAnnotationSpecification</code> extension point and
	 * translates them into <code>AnnotationPreference</code> objects.
	 */
	private void initialize() {
		
		// initialize lists - indicates that the initialization happened
		fFragments= new ArrayList(2);
		fPreferences= new ArrayList(2);
		
		// populate list
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(EditorsPlugin.getPluginId(), "markerAnnotationSpecification"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
			for (int i= 0; i < elements.length; i++) {
				AnnotationPreference spec= createSpec(elements[i]);
				if (spec != null)
					fFragments.add(spec);
				if (spec.getColorPreferenceKey() != null)
					fPreferences.add(spec);
			}
		}

		final Collator collator= Collator.getInstance();
		Collections.sort(fFragments, new Comparator() {
			/*
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				if (o1 == o2)
					return 0;
					
				AnnotationPreference ap1= (AnnotationPreference)o1;
				AnnotationPreference ap2= (AnnotationPreference)o2;

				String label1= ap1.getPreferenceLabel();
				String label2= ap2.getPreferenceLabel();
				
				if (label1 == null && label2 == null)
					return 0;
				
				if (label1 == null)
					return -1;
				
				if (label2 == null)
					return 1;

				return collator.compare(label1, label2);
			}
		});
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
			
		AnnotationPreference info= new AnnotationPreference();
		
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
		
		s= element.getAttribute("annotationImageProvider"); //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			info.setAnnotationImageProviderData(element, "annotationImageProvider"); //$NON-NLS-1$
		
		return info;
	}
	
	/**
	 * Returns the image descritor for the icon path specified by the given configuration
	 * element.
	 * 
	 * @param iconPath the icon path
	 * @param element the configuration element
	 * @since 3.0
	 */
	private ImageDescriptor getImageDescriptor(String iconPath, IConfigurationElement element) {
		IPluginDescriptor descriptor = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		try {
			return ImageDescriptor.createFromURL(new URL(descriptor.getInstallURL(), iconPath));
		} catch (MalformedURLException x) {
			EditorsPlugin.log(x);
		}
		return null;
	}
}
