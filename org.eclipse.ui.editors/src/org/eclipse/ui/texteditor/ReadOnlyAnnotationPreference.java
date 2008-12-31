/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Copy of {@link AnnotationPreference} which can be
 * marked as read-only in which state no modification
 * is allowed.
 * <p>
 * In the read-only state a {@link UnsupportedOperationException}
 * is thrown by methods that modify the preference.
 * </p>
 *
 * @since 3.2
 */
class ReadOnlyAnnotationPreference extends AnnotationPreference {

	private boolean fIsReadOnly;

	public void merge(AnnotationPreference preference) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.merge(preference);
	}

	public void setAnnotationImageProvider(IAnnotationImageProvider provider) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setAnnotationImageProvider(provider);
	}

	public void setAnnotationImageProviderData(IConfigurationElement configurationElement, String annotationImageProviderAttribute) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setAnnotationImageProviderData(configurationElement,
				annotationImageProviderAttribute);
	}

	public void setAnnotationType(Object annotationType) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setAnnotationType(annotationType);
	}

	public void setColorPreferenceKey(String colorKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setColorPreferenceKey(colorKey);
	}

	public void setColorPreferenceValue(RGB colorValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setColorPreferenceValue(colorValue);
	}

	public void setContributesToHeader(boolean contributesToHeader) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setContributesToHeader(contributesToHeader);
	}

	public void setHighlightPreferenceKey(String highlightKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setHighlightPreferenceKey(highlightKey);
	}

	public void setHighlightPreferenceValue(boolean highlightValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setHighlightPreferenceValue(highlightValue);
	}

	public void setImageDescriptor(ImageDescriptor descriptor) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setImageDescriptor(descriptor);
	}

	public void setIncludeOnPreferencePage(boolean includeOnPreferencePage) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIncludeOnPreferencePage(includeOnPreferencePage);
	}

	public void setIsGoToNextNavigationTarget(boolean isGoToNextNavigationTarget) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToNextNavigationTarget(isGoToNextNavigationTarget);
	}

	public void setIsGoToNextNavigationTargetKey(String isGoToNextNavigationTargetKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToNextNavigationTargetKey(isGoToNextNavigationTargetKey);
	}

	public void setIsGoToPreviousNavigationTarget(boolean isGoToPreviousNavigationTarget) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToPreviousNavigationTarget(isGoToPreviousNavigationTarget);
	}

	public void setIsGoToPreviousNavigationTargetKey(String isGoToPreviousNavigationTargetKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToPreviousNavigationTargetKey(isGoToPreviousNavigationTargetKey);
	}

	public void setMarkerType(String markerType) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setMarkerType(markerType);
	}

	public void setOverviewRulerPreferenceKey(String overviewRulerKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setOverviewRulerPreferenceKey(overviewRulerKey);
	}

	public void setOverviewRulerPreferenceValue(boolean overviewRulerValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setOverviewRulerPreferenceValue(overviewRulerValue);
	}

	public void setPreferenceLabel(String label) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setPreferenceLabel(label);
	}

	public void setPresentationLayer(int presentationLayer) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setPresentationLayer(presentationLayer);
	}

	public void setQuickFixImageDescriptor(ImageDescriptor descriptor) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setQuickFixImageDescriptor(descriptor);
	}

	public void setSeverity(int severity) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setSeverity(severity);
	}

	public void setShowInNextPrevDropdownToolbarAction(boolean showInNextPrevDropdownToolbarAction) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setShowInNextPrevDropdownToolbarAction(showInNextPrevDropdownToolbarAction);
	}

	public void setShowInNextPrevDropdownToolbarActionKey(String showInNextPrevDropdownToolbarActionKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setShowInNextPrevDropdownToolbarActionKey(showInNextPrevDropdownToolbarActionKey);
	}

	public void setSymbolicImageName(String symbolicImageName) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setSymbolicImageName(symbolicImageName);
	}

	public void setTextPreferenceKey(String textKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextPreferenceKey(textKey);
	}

	public void setTextPreferenceValue(boolean textValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextPreferenceValue(textValue);
	}

	public void setTextStylePreferenceKey(String key) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextStylePreferenceKey(key);
	}

	public void setTextStyleValue(String value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextStyleValue(value);
	}

	protected void setValue(Object attribute, boolean value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setValue(attribute, value);
	}

	protected void setValue(Object attribute, int value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setValue(attribute, value);
	}

	protected void setValue(Object attribute, Object value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setValue(attribute, value);
	}

	public void setVerticalRulerPreferenceKey(String verticalRulerKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setVerticalRulerPreferenceKey(verticalRulerKey);
	}

	public void setVerticalRulerPreferenceValue(boolean verticalRulerValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setVerticalRulerPreferenceValue(verticalRulerValue);
	}

	public void markReadOnly() {
		fIsReadOnly= true;
	}

}
