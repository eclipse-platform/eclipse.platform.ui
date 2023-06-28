/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void merge(AnnotationPreference preference) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.merge(preference);
	}

	@Override
	public void setAnnotationImageProvider(IAnnotationImageProvider provider) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setAnnotationImageProvider(provider);
	}

	@Override
	public void setAnnotationImageProviderData(IConfigurationElement configurationElement, String annotationImageProviderAttribute) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setAnnotationImageProviderData(configurationElement,
				annotationImageProviderAttribute);
	}

	@Override
	public void setAnnotationType(Object annotationType) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setAnnotationType(annotationType);
	}

	@Override
	public void setColorPreferenceKey(String colorKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setColorPreferenceKey(colorKey);
	}

	@Override
	public void setColorPreferenceValue(RGB colorValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setColorPreferenceValue(colorValue);
	}

	@Override
	public void setContributesToHeader(boolean contributesToHeader) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setContributesToHeader(contributesToHeader);
	}

	@Override
	public void setHighlightPreferenceKey(String highlightKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setHighlightPreferenceKey(highlightKey);
	}

	@Override
	public void setHighlightPreferenceValue(boolean highlightValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setHighlightPreferenceValue(highlightValue);
	}

	@Override
	public void setImageDescriptor(ImageDescriptor descriptor) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setImageDescriptor(descriptor);
	}

	@Override
	public void setIncludeOnPreferencePage(boolean includeOnPreferencePage) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIncludeOnPreferencePage(includeOnPreferencePage);
	}

	@Override
	public void setIsGoToNextNavigationTarget(boolean isGoToNextNavigationTarget) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToNextNavigationTarget(isGoToNextNavigationTarget);
	}

	@Override
	public void setIsGoToNextNavigationTargetKey(String isGoToNextNavigationTargetKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToNextNavigationTargetKey(isGoToNextNavigationTargetKey);
	}

	@Override
	public void setIsGoToPreviousNavigationTarget(boolean isGoToPreviousNavigationTarget) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToPreviousNavigationTarget(isGoToPreviousNavigationTarget);
	}

	@Override
	public void setIsGoToPreviousNavigationTargetKey(String isGoToPreviousNavigationTargetKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setIsGoToPreviousNavigationTargetKey(isGoToPreviousNavigationTargetKey);
	}

	@Override
	public void setMarkerType(String markerType) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setMarkerType(markerType);
	}

	@Override
	public void setOverviewRulerPreferenceKey(String overviewRulerKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setOverviewRulerPreferenceKey(overviewRulerKey);
	}

	@Override
	public void setOverviewRulerPreferenceValue(boolean overviewRulerValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setOverviewRulerPreferenceValue(overviewRulerValue);
	}

	@Override
	public void setPreferenceLabel(String label) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setPreferenceLabel(label);
	}

	@Override
	public void setPresentationLayer(int presentationLayer) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setPresentationLayer(presentationLayer);
	}

	@Override
	public void setQuickFixImageDescriptor(ImageDescriptor descriptor) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setQuickFixImageDescriptor(descriptor);
	}

	@Override
	public void setSeverity(int severity) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setSeverity(severity);
	}

	@Override
	public void setShowInNextPrevDropdownToolbarAction(boolean showInNextPrevDropdownToolbarAction) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setShowInNextPrevDropdownToolbarAction(showInNextPrevDropdownToolbarAction);
	}

	@Override
	public void setShowInNextPrevDropdownToolbarActionKey(String showInNextPrevDropdownToolbarActionKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setShowInNextPrevDropdownToolbarActionKey(showInNextPrevDropdownToolbarActionKey);
	}

	@Override
	public void setSymbolicImageName(String symbolicImageName) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setSymbolicImageName(symbolicImageName);
	}

	@Override
	public void setTextPreferenceKey(String textKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextPreferenceKey(textKey);
	}

	@Override
	public void setTextPreferenceValue(boolean textValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextPreferenceValue(textValue);
	}

	@Override
	public void setTextStylePreferenceKey(String key) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextStylePreferenceKey(key);
	}

	@Override
	public void setTextStyleValue(String value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setTextStyleValue(value);
	}

	@Override
	protected void setValue(Object attribute, boolean value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setValue(attribute, value);
	}

	@Override
	protected void setValue(Object attribute, int value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setValue(attribute, value);
	}

	@Override
	protected void setValue(Object attribute, Object value) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setValue(attribute, value);
	}

	@Override
	public void setVerticalRulerPreferenceKey(String verticalRulerKey) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setVerticalRulerPreferenceKey(verticalRulerKey);
	}

	@Override
	public void setVerticalRulerPreferenceValue(boolean verticalRulerValue) {
		if (fIsReadOnly)
			throw new UnsupportedOperationException();
		super.setVerticalRulerPreferenceValue(verticalRulerValue);
	}

	public void markReadOnly() {
		fIsReadOnly= true;
	}

}
