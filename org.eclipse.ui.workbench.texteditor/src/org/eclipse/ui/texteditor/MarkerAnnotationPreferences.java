/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * @since 2.1
 */
public class MarkerAnnotationPreferences {
	
	private List fPreferences;
	
	public MarkerAnnotationPreferences() {
	}
	
	public List getAnnotationPreferences() {
		if (fPreferences == null)
			initialize();
		return fPreferences;
	}
	
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
	}

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
		
		i= StringConverter.asInt(element.getAttribute("markerSeverity"), IMarker.SEVERITY_INFO);  //$NON-NLS-1$
		info.setSeverity(i);
		
		s= element.getAttribute("textPreferenceKey");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setTextPreferenceKey(s);

		b= StringConverter.asBoolean(element.getAttribute("textPreferenceValue"), false);  //$NON-NLS-1$
		info.setTextPreferenceValue(b);
		
		s= element.getAttribute("overviewRulerPreferenceKey");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setOverviewRulerPreferenceKey(s);

		b= StringConverter.asBoolean(element.getAttribute("overviewRulerPreferenceValue"), false);  //$NON-NLS-1$
		info.setOverviewRulerPreferenceValue(b);

		s= element.getAttribute("colorPreferenceKey");  //$NON-NLS-1$
		if (s == null || s.trim().length() == 0) return null;
		info.setColorPreferenceKey(s);
						
		RGB rgb= StringConverter.asRGB(element.getAttribute("colorPreferenceValue"), new RGB(0, 0, 0));  //$NON-NLS-1$
		info.setColorPreferenceValue(rgb);
		
		i= StringConverter.asInt(element.getAttribute("presentationLayer"), 0);  //$NON-NLS-1$
		info.setPresentationLayer(i);
		
		b= StringConverter.asBoolean(element.getAttribute("contributesToHeader"), false);  //$NON-NLS-1$
		info.setContributesToHeader(b);
		
		return info;
	}
}