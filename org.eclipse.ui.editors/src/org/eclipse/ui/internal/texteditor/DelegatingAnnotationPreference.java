/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.ui.internal.texteditor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.text.source.IAnnotationAccessExtension;

import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;


public class DelegatingAnnotationPreference extends AnnotationPreference {

	private AnnotationType fType;
	private AnnotationPreferenceLookup fLookup;
	private Set<Object> fCache= new HashSet<>();

	public DelegatingAnnotationPreference(AnnotationType type, AnnotationPreferenceLookup lookup) {
		fType= type;
		fLookup= lookup;
	}

	private boolean isCached(Object attribute) {
		return fCache.contains(attribute);
	}

	private void markCached(Object attribute) {
		fCache.add(attribute);
	}

	private AnnotationPreference getDefiningPreference(Object attribute) {

		AnnotationPreference p= fLookup.getAnnotationPreferenceFragment(fType.getType());
		if (p != null && p.hasValue(attribute))
			return p;

		String[] superTypes= fType.getSuperTypes();
		for (int i= 0; i < superTypes.length; i++) {
			p= fLookup.getAnnotationPreferenceFragment(superTypes[i]);
			if (p != null && p.hasValue(attribute))
				return p;
		}

		return null;
	}

	private Object getAttributeValue(Object attribute) {
		if (!isCached(attribute)) {
			AnnotationPreference preference= getDefiningPreference(attribute);
			if (preference != null)
				setValue(attribute, preference.getValue(attribute));
			markCached(attribute);
		}
		return super.getValue(attribute);
	}

	private boolean getBooleanAttributeValue(Object attribute) {
		Object value= getAttributeValue(attribute);
		if (value instanceof Boolean)
			return ((Boolean) value).booleanValue();
		return false;
	}

	@Override
	public Object getAnnotationType() {
		return fType.getType();
	}

	@Override
	public boolean contributesToHeader() {
		return getBooleanAttributeValue(HEADER_VALUE);
	}

	@Override
	public IAnnotationImageProvider getAnnotationImageProvider() {
		if (!isCached(IMAGE_PROVIDER)) {
			AnnotationPreference preference= getDefiningPreference(IMAGE_PROVIDER);
			if (preference != null) {
				fAnnotationImageProvider= preference.fAnnotationImageProvider;
				fAnnotationImageProviderAttribute= preference.fAnnotationImageProviderAttribute;
				fConfigurationElement= preference.fConfigurationElement;
			}
			markCached(IMAGE_PROVIDER);
		}
		return super.getAnnotationImageProvider();
	}

	@Override
	public String getColorPreferenceKey() {
		return (String) getAttributeValue(COLOR_PREFERENCE_KEY);
	}

	@Override
	public RGB getColorPreferenceValue() {
		return (RGB) getAttributeValue(COLOR_PREFERENCE_VALUE);
	}

	@Override
	public String getHighlightPreferenceKey() {
		return (String) getAttributeValue(HIGHLIGHT_PREFERENCE_KEY);
	}

	@Override
	public boolean getHighlightPreferenceValue() {
		return getBooleanAttributeValue(HIGHLIGHT_PREFERENCE_VALUE);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return (ImageDescriptor) getAttributeValue(IMAGE_DESCRIPTOR);
	}

	@Override
	public ImageDescriptor getQuickFixImageDescriptor() {
		return (ImageDescriptor) getAttributeValue(QUICK_FIX_IMAGE_DESCRIPTOR);
	}

	@Override
	public String getIsGoToNextNavigationTargetKey() {
		return (String) getAttributeValue(IS_GO_TO_NEXT_TARGET_KEY);
	}

	@Override
	public String getIsGoToPreviousNavigationTargetKey() {
		return (String) getAttributeValue(IS_GO_TO_PREVIOUS_TARGET_KEY);
	}

	@Override
	public String getOverviewRulerPreferenceKey() {
		return (String) getAttributeValue(OVERVIEW_RULER_PREFERENCE_KEY);
	}

	@Override
	public boolean getOverviewRulerPreferenceValue() {
		return getBooleanAttributeValue(OVERVIEW_RULER_PREFERENCE_VALUE);
	}

	@Override
	public String getPreferenceLabel() {
		return (String) getAttributeValue(PREFERENCE_LABEL);
	}

	@Override
	public int getPresentationLayer() {
		Object value= getAttributeValue(PRESENTATION_LAYER);
		if (value instanceof Integer)
			return ((Integer) value).intValue();
		return IAnnotationAccessExtension.DEFAULT_LAYER;
	}

	@Override
	public String getShowInNextPrevDropdownToolbarActionKey() {
		return (String) getAttributeValue(SHOW_IN_NAVIGATION_DROPDOWN_KEY);
	}

	@Override
	public String getSymbolicImageName() {
		return (String) getAttributeValue(SYMBOLIC_IMAGE_NAME);
	}

	@Override
	public String getTextPreferenceKey() {
		return (String) getAttributeValue(TEXT_PREFERENCE_KEY);
	}

	@Override
	public boolean getTextPreferenceValue() {
		return getBooleanAttributeValue(TEXT_PREFERENCE_VALUE);
	}

	@Override
	public String getVerticalRulerPreferenceKey() {
		return (String) getAttributeValue(VERTICAL_RULER_PREFERENCE_KEY);
	}

	@Override
	public boolean getVerticalRulerPreferenceValue() {
		return getBooleanAttributeValue(VERTICAL_RULER_PREFERENCE_VALUE);
	}

	@Override
	public boolean isGoToNextNavigationTarget() {
		return getBooleanAttributeValue(IS_GO_TO_NEXT_TARGET_VALUE);
	}

	@Override
	public boolean isGoToPreviousNavigationTarget() {
		return getBooleanAttributeValue(IS_GO_TO_PREVIOUS_TARGET_VALUE);
	}

	@Override
	public boolean isShowInNextPrevDropdownToolbarAction() {
		return getBooleanAttributeValue(SHOW_IN_NAVIGATION_DROPDOWN_VALUE);
	}

	@Override
	public String getTextStylePreferenceKey() {
		return (String)getAttributeValue(TEXT_STYLE_PREFERENCE_KEY);
	}

	@Override
	public String getTextStyleValue() {
		return (String)getAttributeValue(TEXT_STYLE_PREFERENCE_VALUE);
	}

	@Override
	public Object getValue(Object attribute) {
		return getAttributeValue(attribute);
	}
}
