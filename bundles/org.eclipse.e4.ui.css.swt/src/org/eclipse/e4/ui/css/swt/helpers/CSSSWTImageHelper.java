/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSSWTImageHelper {

	public static Image getImage(CSSValue value,
			IResourcesLocatorManager manager, Display display) throws Exception {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE)
			return null;
		CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
		switch (primitiveValue.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_URI:
			String path = primitiveValue.getStringValue();
			return loadImageFromURL(display, path, manager);
		}
		return null;
	}

	private static Image loadImageFromURL(Device device, String path,
			IResourcesLocatorManager manager) throws Exception {
		Image result = null;
		InputStream in = null;
		try {
			// URL url = new URL(path);
			in = manager.getInputStream(path);
			if (in != null) {
				result = new Image(device, in);
			}
			// } catch (IOException e) {
			// e.printStackTrace();
			// return null;
			// } catch (SWTException e) {
			// if (e.code != SWT.ERROR_INVALID_IMAGE) {
			// throw e;
			// }
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// e.printStackTrace();
				throw e;
			}
		}
		return result;
	}

}

