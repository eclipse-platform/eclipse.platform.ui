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

import org.eclipse.swt.graphics.RGB;


/**
 * @since 2.1
 */
public class AnnotationPreference {
	
	private Object fAnnotationType;
	private String fPreferenceLabel;
	private String fMarkerType;
	private int fSeverity;
	private String fColorKey;
	private RGB fColorValue;
	private String fTextKey;
	private boolean fTextValue;
	private String fOverviewRulerKey;
	private boolean fOverviewRulerValue;
	private int fPresentationLayer;
	private boolean fContributesToHeader;
	
	
	public AnnotationPreference() {
	}
		
	public AnnotationPreference(Object annotationType, String colorKey, String textKey, String overviewRulerKey, int presentationLayer) {
		fAnnotationType= annotationType;
		fColorKey= colorKey;
		fTextKey= textKey;
		fOverviewRulerKey= overviewRulerKey;
		fPresentationLayer= presentationLayer;
	}
	
	public boolean isPreferenceKey(String key) {
		if (key == null)
			return false;
		return key.equals(fColorKey) || key.equals(fOverviewRulerKey) || key.equals(fTextKey);
	}

	public Object getAnnotationType() {
		return fAnnotationType;
	}
	
	public String getMarkerType() {
		return fMarkerType;
	}

	public String getColorPreferenceKey() {
		return fColorKey;
	}

	public RGB getColorPreferenceValue() {
		return fColorValue;
	}

	public String getPreferenceLabel() {
		return fPreferenceLabel;
	}

	public String getOverviewRulerPreferenceKey() {
		return fOverviewRulerKey;
	}

	public boolean getOverviewRulerPreferenceValue() {
		return fOverviewRulerValue;
	}

	public int getPresentationLayer() {
		return fPresentationLayer;
	}

	public int getSeverity() {
		return fSeverity;
	}

	public String getTextPreferenceKey() {
		return fTextKey;
	}

	public boolean getTextPreferenceValue() {
		return fTextValue;
	}
	
	public boolean contributesToHeader() {
		return fContributesToHeader;
	}

	public void setAnnotationType(Object annotationType) {
		fAnnotationType= annotationType;
	}
	
	public void setMarkerType(String markerType) {
		fMarkerType= markerType;
	}
	
	public void setColorPreferenceKey(String colorKey) {
		fColorKey= colorKey;
	}

	public void setColorPreferenceValue(RGB colorValue) {
		fColorValue= colorValue;
	}

	public void setPreferenceLabel(String label) {
		fPreferenceLabel= label;
	}

	public void setOverviewRulerPreferenceKey(String overviewRulerKey) {
		fOverviewRulerKey= overviewRulerKey;
	}

	public void setOverviewRulerPreferenceValue(boolean overviewRulerValue) {
		fOverviewRulerValue= overviewRulerValue;
	}

	public void setPresentationLayer(int presentationLayer) {
		fPresentationLayer= presentationLayer;
	}

	public void setSeverity(int severity) {
		fSeverity= severity;
	}

	public void setTextPreferenceKey(String textKey) {
		fTextKey= textKey;
	}

	public void setTextPreferenceValue(boolean textValue) {
		fTextValue= textValue;
	}
	
	public void setContributesToHeader(boolean contributesToHeader) {
		fContributesToHeader= contributesToHeader;
	}
}
