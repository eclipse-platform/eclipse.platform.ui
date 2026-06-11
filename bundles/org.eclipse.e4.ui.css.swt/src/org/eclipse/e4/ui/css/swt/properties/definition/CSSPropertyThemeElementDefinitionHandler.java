/*******************************************************************************
 * Copyright (c) 2013, 2026 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.properties.definition;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.normalizeId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.definition.ThemeDefinitionElement;
import org.eclipse.e4.ui.internal.css.swt.definition.IThemeElementDefinitionOverridable;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyThemeElementDefinitionHandler implements ICSSPropertyHandler {
	private static final String CATEGORY_PROP = "category";

	private static final String LABEL_PROP = "label";

	private static final String DESCRIPTION_PROP = "description";

	private static final String MESSAGE_QUERY_PARAM = "message";

	private static final String EDITABLE_PROP = "editable";

	private final Map<Long, ResourceBundle> bundleToResourceBundles = new WeakHashMap<>();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(element instanceof ThemeDefinitionElement<?> themeDefinition) || property == null) {
			return false;
		}
		IThemeElementDefinitionOverridable<?> definition = (IThemeElementDefinitionOverridable<?>) themeDefinition
				.getNativeWidget();

		switch (property) {
		case CATEGORY_PROP:
			definition.setCategoryId(normalizeId(value.getCssText().substring(1)));
			break;
		case LABEL_PROP:
			definition.setName(getLabel(value));
			break;
		case DESCRIPTION_PROP:
			definition.setDescription(getLabel(value));
			break;
		case EDITABLE_PROP:
			Boolean editable = (Boolean) engine.convert(value, Boolean.class, null);
			definition.setEditable(editable);
			break;
		default:
			return false;
		}

		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	private String getLabel(CSSValue value) {
		URI resourceBundleURL = getResourceBundleURI(value);
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

	private String getMessageId(URI resourceBundleURL) {
		String query = resourceBundleURL.getQuery();
		if (query != null) {
			int indexOfMessageParam = query.indexOf(MESSAGE_QUERY_PARAM);
			if (indexOfMessageParam != -1) {
				return query.substring(indexOfMessageParam + MESSAGE_QUERY_PARAM.length() + 1);
			}
		}
		return null;
	}

	private static Bundle getBundle(URI uri) throws BundleException {
		String path = uri.getPath();
		if (path == null) {
			return null;
		}
		String[] segments = path.split("/");
		Bundle bundle = Platform.getBundle(segments[segments.length - 1]);
		if (bundle != null && (bundle.getState() & Bundle.ACTIVE) == 0) {
			bundle.start(); // Bundle is lazy init
		}
		return bundle;
	}

	private ResourceBundle getResourceBundle(URI uri) {
		ResourceBundle resourceBundle = null;
		try {
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

	private static URI getResourceBundleURI(CSSValue value) {
		if (value instanceof CssText text && text.kind() == CssText.Kind.URI) {
			try {
				return new URI(text.value());
			} catch (URISyntaxException exc) {
				// do nothing
			}
		}
		return null;
	}
}
