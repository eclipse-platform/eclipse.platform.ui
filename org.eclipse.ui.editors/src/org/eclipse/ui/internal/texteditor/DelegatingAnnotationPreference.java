/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private Set fCache= new HashSet();

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

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getAnnotationType()
	 */
	public Object getAnnotationType() {
		return fType.getType();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#contributesToHeader()
	 */
	public boolean contributesToHeader() {
		return getBooleanAttributeValue(HEADER_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getAnnotationImageProvider()
	 */
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

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getColorPreferenceKey()
	 */
	public String getColorPreferenceKey() {
		return (String) getAttributeValue(COLOR_PREFERENCE_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getColorPreferenceValue()
	 */
	public RGB getColorPreferenceValue() {
		return (RGB) getAttributeValue(COLOR_PREFERENCE_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getHighlightPreferenceKey()
	 */
	public String getHighlightPreferenceKey() {
		return (String) getAttributeValue(HIGHLIGHT_PREFERENCE_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getHighlightPreferenceValue()
	 */
	public boolean getHighlightPreferenceValue() {
		return getBooleanAttributeValue(HIGHLIGHT_PREFERENCE_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return (ImageDescriptor) getAttributeValue(IMAGE_DESCRIPTOR);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getQuickFixImageDescriptor()
	 */
	public ImageDescriptor getQuickFixImageDescriptor() {
		return (ImageDescriptor) getAttributeValue(QUICK_FIX_IMAGE_DESCRIPTOR);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getIsGoToNextNavigationTargetKey()
	 */
	public String getIsGoToNextNavigationTargetKey() {
		return (String) getAttributeValue(IS_GO_TO_NEXT_TARGET_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getIsGoToPreviousNavigationTargetKey()
	 */
	public String getIsGoToPreviousNavigationTargetKey() {
		return (String) getAttributeValue(IS_GO_TO_PREVIOUS_TARGET_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getOverviewRulerPreferenceKey()
	 */
	public String getOverviewRulerPreferenceKey() {
		return (String) getAttributeValue(OVERVIEW_RULER_PREFERENCE_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getOverviewRulerPreferenceValue()
	 */
	public boolean getOverviewRulerPreferenceValue() {
		return getBooleanAttributeValue(OVERVIEW_RULER_PREFERENCE_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getPreferenceLabel()
	 */
	public String getPreferenceLabel() {
		return (String) getAttributeValue(PREFERENCE_LABEL);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getPresentationLayer()
	 */
	public int getPresentationLayer() {
		Object value= getAttributeValue(PRESENTATION_LAYER);
		if (value instanceof Integer)
			return ((Integer) value).intValue();
		return IAnnotationAccessExtension.DEFAULT_LAYER;
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getShowInNextPrevDropdownToolbarActionKey()
	 */
	public String getShowInNextPrevDropdownToolbarActionKey() {
		return (String) getAttributeValue(SHOW_IN_NAVIGATION_DROPDOWN_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getSymbolicImageName()
	 */
	public String getSymbolicImageName() {
		return (String) getAttributeValue(SYMBOLIC_IMAGE_NAME);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getTextPreferenceKey()
	 */
	public String getTextPreferenceKey() {
		return (String) getAttributeValue(TEXT_PREFERENCE_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getTextPreferenceValue()
	 */
	public boolean getTextPreferenceValue() {
		return getBooleanAttributeValue(TEXT_PREFERENCE_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getVerticalRulerPreferenceKey()
	 */
	public String getVerticalRulerPreferenceKey() {
		return (String) getAttributeValue(VERTICAL_RULER_PREFERENCE_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getVerticalRulerPreferenceValue()
	 */
	public boolean getVerticalRulerPreferenceValue() {
		return getBooleanAttributeValue(VERTICAL_RULER_PREFERENCE_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#isGoToNextNavigationTarget()
	 */
	public boolean isGoToNextNavigationTarget() {
		return getBooleanAttributeValue(IS_GO_TO_NEXT_TARGET_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#isGoToPreviousNavigationTarget()
	 */
	public boolean isGoToPreviousNavigationTarget() {
		return getBooleanAttributeValue(IS_GO_TO_PREVIOUS_TARGET_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#isShowInNextPrevDropdownToolbarAction()
	 */
	public boolean isShowInNextPrevDropdownToolbarAction() {
		return getBooleanAttributeValue(SHOW_IN_NAVIGATION_DROPDOWN_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getTextStylePreferenceKey()
	 * @since 3.8
	 */
	public String getTextStylePreferenceKey() {
		return (String)getAttributeValue(TEXT_STYLE_PREFERENCE_KEY);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getTextStyleValue()
	 * @since 3.8
	 */
	public String getTextStyleValue() {
		return (String)getAttributeValue(TEXT_STYLE_PREFERENCE_VALUE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AnnotationPreference#getValue(java.lang.Object)
	 * @since 3.8
	 */
	public Object getValue(Object attribute) {
		return getAttributeValue(attribute);
	}
}
