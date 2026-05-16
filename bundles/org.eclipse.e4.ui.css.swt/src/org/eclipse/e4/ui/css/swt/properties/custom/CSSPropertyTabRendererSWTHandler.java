/*******************************************************************************
 * Copyright (c) 2010, 2026 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.properties.custom;

import java.lang.reflect.Constructor;
import java.net.URI;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Bundle;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTabRendererSWTHandler extends AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder tabFolder)) {
			return;
		}
		if (value instanceof CSSPrimitiveValue primitiveValue) {
			if (primitiveValue.getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
				String rendURL = primitiveValue.getStringValue();
				URI uri = new URI(rendURL);
				Bundle bundle = Platform.getBundle(uri.getAuthority());
				String[] segments = getPathSegments(uri);
				if (bundle == null) {
					ILog.of(getClass()).error("Failed to get bundle for: " + rendURL); //$NON-NLS-1$
				} else if (segments.length > 1) {
					//TODO: handle this case?
				} else {
					String clazz = segments[0];
					try {
						Class<?> targetClass = bundle.loadClass(clazz);
						//check to see if the folder already has an instance of the same renderer

						CTabFolderRenderer renderer = tabFolder.getRenderer();
						if (renderer != null && renderer.getClass() == targetClass) {
							return;
						}
						Constructor<?> constructor = targetClass.getConstructor(CTabFolder.class);
						if (constructor != null) {
							Object rend = constructor.newInstance(tabFolder);
							if (rend != null && rend instanceof CTabFolderRenderer tabFolderRenderer) {
								tabFolder.setRenderer(tabFolderRenderer);
							}
						}
					} catch (ClassNotFoundException e) {
						String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
								+ bundle.getBundleId() + "'"; //$NON-NLS-1$
						ILog.of(getClass()).error(message);
					}
				}
			} else {
				tabFolder.setRenderer(null);
			}
		}
	}

	private static String[] getPathSegments(URI uri) {
		String path = uri.getPath();
		if (path == null || path.isEmpty() || path.equals("/")) {
			throw new IllegalArgumentException("Expected segments missing from " + uri);
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path.split("/");
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
