/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.definition;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.definition.ThemeDefinitionElement;
import org.eclipse.e4.ui.css.swt.helpers.URI;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IThemeElementDefinitionOverridable;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyThemeElementDefinitionHandler implements ICSSPropertyHandler {
	private final static String CATEGORY_PROP = "category";

	private final static String LABEL_PROP = "label";

	private final static String DESCRIPTION_PROP = "description";

	private final static String MESSAGE_QUERY_PARAM = "message";

	private Map<Long, ResourceBundle> bundleToResourceBundles = new WeakHashMap<Long, ResourceBundle>();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(element instanceof ThemeDefinitionElement<?>)) {
			return false;
		}

		IThemeElementDefinitionOverridable<?> definition =
				(IThemeElementDefinitionOverridable<?>) ((ThemeDefinitionElement<?>) element)
				.getNativeWidget();

		if (CATEGORY_PROP.equals(property)) {
			definition.setCategoryId(normalizeId(value.getCssText().substring(1)));
		} else if (LABEL_PROP.equals(property)) {
			definition.setName(getLabel(value));
		} else if (DESCRIPTION_PROP.equals(property)) {
			definition.setDescription(value.getCssText());
		}

		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	private String getLabel(CSSValue value) {
		URL resourceBundleURL = getResourceBundleURL(value);
		if (resourceBundleURL != null) {
			String messageId = getMessageId(resourceBundleURL);
			if (messageId != null) {
				ResourceBundle resourceBundle = getResourceBundle(resourceBundleURL);
				String message = resourceBundle != null ? resourceBundle.getString(messageId) : null;
				if (message != null) {
					return resourceBundle.getString(messageId);
				}
			}
		}
		return value.getCssText();
	}

	private String getMessageId(URL resourceBundleURL) {
		String query = resourceBundleURL.getQuery();
		if (query != null) {
			int indexOfMessageParam = query.indexOf(MESSAGE_QUERY_PARAM);
			if (indexOfMessageParam != -1) {
				return query.substring(indexOfMessageParam + MESSAGE_QUERY_PARAM.length() + 1);
			}
		}
		return null;
	}

	private Bundle getBundle(URI uri) throws BundleException {
		Bundle bundle = CSSActivator.getDefault().getBundleForName(uri.lastSegment());
		if (bundle != null && (bundle.getState() & Bundle.ACTIVE) == 0) {
			bundle.start(); // Bundle is lazy init
		}
		return bundle;
	}

	private ResourceBundle getResourceBundle(URL resourceBundleURL) {
		ResourceBundle resourceBundle = null;
		try {
			URI uri = URI.createURI(resourceBundleURL.getPath());
			if (uri != null) {
				resourceBundle = getResourceBundle(getBundle(uri));
			}
		} catch (Exception exc) {
			// do nothing
		}
		return resourceBundle;
	}

	private ResourceBundle getResourceBundle(Bundle bundle) {
		if (bundle == null) {
			return null;
		}
		ResourceBundle resourceBundle = bundleToResourceBundles.get(bundle.getBundleId());
		if (resourceBundle != null) {
			return resourceBundle;
		}

		BundleLocalization localization = getBundleLocalization(bundle);
		if (localization != null) {
			resourceBundle = localization.getLocalization(bundle, null);
		}
		if (resourceBundle != null) {
			bundleToResourceBundles.put(bundle.getBundleId(), resourceBundle);
		}
		return resourceBundle;
	}

	private BundleLocalization getBundleLocalization(Bundle bundle) {
		ServiceReference<BundleLocalization> ref = bundle.getBundleContext().getServiceReference(
				BundleLocalization.class);
		return bundle.getBundleContext().getService(ref);
	}

	private URL getResourceBundleURL(CSSValue value) {
		URL url = null;
		if (hasResourceBundleUrl(value)) {
			try {
				url = new URL(((CSSPrimitiveValue) value).getStringValue());
			} catch (MalformedURLException exc) {
				// do nothing
			}
		}
		return url;
	}

	private boolean hasResourceBundleUrl(CSSValue value) {
		return value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_URI;
	}
}
