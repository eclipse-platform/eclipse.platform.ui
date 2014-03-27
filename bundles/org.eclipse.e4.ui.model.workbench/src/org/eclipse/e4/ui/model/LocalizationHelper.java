/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.model;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * This class provides set of utility method that can be useful in typical
 * localization scenarios.
 */
final public class LocalizationHelper {

	private LocalizationHelper() {
		// prevents instantiation
	}

	public static String getLocalizedFeature(EStructuralFeature feature,
			MApplicationElement element) {
		Object o = ((EObject) element).eGet(feature);
		if (o instanceof String) {
			return getLocalized((String) o, element);
		}
		return null;
	}

	/**
	 * Returns localized accessibilityPhrase for the specified element using
	 * locale information from its context.
	 * 
	 * @param element
	 *            the element
	 * @return localized element's accessibilityPhrase, or <code>null</code> if
	 *         no label can be found
	 */
	public static String getLocalizedAccessibilityPhrase(MUIElement element) {
		String key = element.getAccessibilityPhrase();
		if (key == null)
			return null;
		return getLocalized(key, element);
	}

	/**
	 * Returns localized label for the specified element using locale
	 * information from its context.
	 * 
	 * @param element
	 *            the element
	 * @return localized element's label, or <code>null</code> if no label can
	 *         be found
	 */
	public static String getLocalizedLabel(MUIElement element) {
		if (!(element instanceof MUILabel))
			return null;
		String key = ((MUILabel) element).getLabel();
		if (key == null)
			return null;
		return getLocalized(key, element);
	}

	/**
	 * Returns localized tooltip for the specified element using locale
	 * information from its context.
	 * 
	 * @param element
	 *            the element
	 * @return localized element's tooltip, or <code>null</code> if no tooltip
	 *         can be found
	 */
	public static String getLocalizedTooltip(MUIElement element) {
		if (!(element instanceof MUILabel))
			return null;
		String key = ((MUILabel) element).getTooltip();
		if (key == null)
			return null;
		return getLocalized(key, element);
	}

	/**
	 * Returns localized string for the key using locale information specified
	 * in the element's context.
	 * <p>
	 * This method will return the key itself if the context can not be found
	 * for the model element or there is no translation service registered in 
	 * that context.
	 * </p>
	 * 
	 * @param key
	 *            the key
	 * @param element
	 *            the model element
	 * @return localized key
	 */
	public static String getLocalized(String key, MApplicationElement element) {
		IEclipseContext context = ModelUtils.getContainingContext(element);
		return getLocalized(key, element, context);
	}

	/**
	 * Returns localized string for the key from the application element using
	 * translation service from the context.
	 * <p>
	 * This method will return the key itself if the context is <code>null</code> 
	 * or there is no translation service registered in the given context.
	 * </p>
	 * 
	 * @param key
	 *            the key
	 * @param element
	 *            the model element
	 * @param context
	 *            the context
	 * @return localized key
	 */
	public static String getLocalized(String key, MApplicationElement element,
			IEclipseContext context) {
		if (key == null || context == null)
			return key;
		TranslationService translation = context.get(TranslationService.class);
		if (translation == null)
			return key;
		return translation.translate(key, element.getContributorURI());
	}

}
