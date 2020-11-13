/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.URI;
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
		if (!(control instanceof CTabFolder)) {
			return;
		}
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			if (((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_URI) {
				String rendURL = ((CSSPrimitiveValue) value).getStringValue();
				URI uri = URI.createURI(rendURL);
				Bundle bundle = Platform.getBundle(uri.authority());
				if (bundle == null) {
					Platform.getLog(getClass()).error("Failed to get bundle for: " + rendURL); //$NON-NLS-1$
				} else if (uri.segmentCount() > 1) {
					//TODO: handle this case?
				} else {
					String clazz = uri.segment(0);
					try {
						Class<?> targetClass = bundle.loadClass(clazz);
						//check to see if the folder already has an instance of the same renderer

						CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
						if (renderer != null && renderer.getClass() == targetClass) {
							return;
						}
						Constructor<?> constructor = targetClass
								.getConstructor(CTabFolder.class);
						if (constructor != null) {
							Object rend = constructor.newInstance(control);
							if (rend != null && rend instanceof CTabFolderRenderer) {
								((CTabFolder) control).setRenderer((CTabFolderRenderer)rend);
							}
						}
					} catch (ClassNotFoundException e) {
						String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
								+ bundle.getBundleId() + "'"; //$NON-NLS-1$
						Platform.getLog(getClass()).error(message);
					}
				}
			} else {
				((CTabFolder) control).setRenderer(null);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
