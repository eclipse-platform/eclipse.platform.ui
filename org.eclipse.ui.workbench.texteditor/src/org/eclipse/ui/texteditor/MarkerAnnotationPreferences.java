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


import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * Objects of this class provide access to all extensions declared for the <code>markerAnnotationSpecification</code> extension point.
 * The extensions are represented as instances of <code>AnnotationPreference</code>.
 * 
 * @since 2.1
 */
public class MarkerAnnotationPreferences {
	
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
	 * Reads all extensions provided for the <code>markerAnnotationSpecification</code> extension point and
	 * translates them into <code>AnnotationPreference</code> objects.
	 */
	private void initialize() {
		
		// initialize lists - indicates that the initialization happened
		fPreferences= new ArrayList(2);
		
		// populate list
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(TextEditorPlugin.PLUGIN_ID, "markerAnnotationSpecification"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
			for (int i= 0; i < elements.length; i++) {
				AnnotationPreference spec= createSpec(elements[i]);
				if (spec != null)
					fPreferences.add(createSpec(elements[i]));
			}
		}

		final Collator collator= Collator.getInstance();
		Collections.sort(fPreferences, new Comparator() {
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
		if (s == null || s.trim().length() == 0) return null;
		info.setPreferenceLabel(s);

		s= element.getAttribute("markerType");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setMarkerType(s);
		
		i= IMarker.SEVERITY_INFO;
		s= element.getAttribute("markerSeverity");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			i= StringConverter.asInt(s, IMarker.SEVERITY_INFO);
		info.setSeverity(i);
		
		s= element.getAttribute("textPreferenceKey");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setTextPreferenceKey(s);

		b= false;
		s= element.getAttribute("textPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			b= StringConverter.asBoolean(s, false);
		info.setTextPreferenceValue(b);
		
		s= element.getAttribute("overviewRulerPreferenceKey");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setOverviewRulerPreferenceKey(s);

		b= false;
		s= element.getAttribute("overviewRulerPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			b= StringConverter.asBoolean(s, false);
		info.setOverviewRulerPreferenceValue(b);

		s= element.getAttribute("colorPreferenceKey");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setColorPreferenceKey(s);
		
		RGB rgb= null;
		s= element.getAttribute("colorPreferenceValue");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			rgb= StringConverter.asRGB(s);
		info.setColorPreferenceValue(rgb == null ? new RGB(0, 0, 0) : rgb);
		
		i= 0;
		s= element.getAttribute("presentationLayer");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			i= StringConverter.asInt(s, 0);
		info.setPresentationLayer(i);
		
		b= false;
		s= element.getAttribute("contributesToHeader");  //$NON-NLS-1$
		if (s != null && s.trim().length() > 0)
			b= StringConverter.asBoolean(s, false);
		info.setContributesToHeader(b);
		
		return info;
	}
}
