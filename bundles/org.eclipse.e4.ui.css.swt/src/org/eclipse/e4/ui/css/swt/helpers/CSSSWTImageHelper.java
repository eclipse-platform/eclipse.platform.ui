/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import java.net.URL;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.eclipse.e4.ui.css.core.utils.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSSWTImageHelper {
	private static final String DEFAULT_IMAGE = "defaultImage";

	private static final String DEFAULT_HOT_IMAGE = "defaultHotImage";

	private static final String DEFAULT_DISABLE_IMAGE = "defaultDisableImage";

	public static Image getImage(CSSValue value,
			IResourcesLocatorManager manager, Display display) throws Exception {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return null;
		}
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

		String s = manager.resolve(path);
		if (!StringUtils.isEmpty(s)) {
			result = ImageDescriptor.createFromURL(new URL(s)).createImage();
		}

		return result;
	}

	public static void storeDefaultImage(Shell shell) {
		storeDefaultImage(shell, DEFAULT_IMAGE, shell.getImage());
	}

	public static void storeDefaultImage(Item item) {
		storeDefaultImage(item, DEFAULT_IMAGE, item.getImage());
	}

	public static void storeDefaultImage(ToolItem item) {
		storeDefaultImage((Item) item);
		storeDefaultImage(item, DEFAULT_HOT_IMAGE, item.getHotImage());
		storeDefaultImage(item, DEFAULT_DISABLE_IMAGE, item.getDisabledImage());
	}

	public static void storeDefaultImage(Button button) {
		storeDefaultImage(button, DEFAULT_IMAGE, button.getImage());
	}

	public static void restoreDefaultImage(Shell shell) {
		Image defaultImage = (Image) shell.getData(DEFAULT_IMAGE);
		if (defaultImage != null) {
			shell.setImage(defaultImage.isDisposed() ? null : defaultImage);
		}
	}

	public static void restoreDefaultImage(Item item) {
		Image defaultImage = (Image) item.getData(DEFAULT_IMAGE);
		if (defaultImage != null) {
			item.setImage(defaultImage.isDisposed() ? null : defaultImage);
		}
	}

	public static void restoreDefaultImage(ToolItem item) {
		restoreDefaultImage((Item) item);

		Image defaultImage = (Image) item.getData(DEFAULT_HOT_IMAGE);
		if (defaultImage != null) {
			item.setHotImage(defaultImage.isDisposed() ? null : defaultImage);
		}

		defaultImage = (Image) item.getData(DEFAULT_DISABLE_IMAGE);
		if (defaultImage != null) {
			item.setDisabledImage(defaultImage.isDisposed() ? null
					: defaultImage);
		}
	}

	public static void restoreDefaultImage(Button button) {
		Image defaultImage = (Image) button.getData(DEFAULT_IMAGE);
		if (defaultImage != null) {
			button.setImage(defaultImage.isDisposed() ? null : defaultImage);
		}
	}

	private static void storeDefaultImage(Widget widget, String imageName,
			Image image) {
		if (widget.getData(imageName) == null) {
			widget.setData(imageName, image);
		}
	}

	/** Helper function to avoid setting images unnecessarily */
	public static void setImage(Button button, Image image) {
		if (button.getImage() != image) {
			storeDefaultImage(button);
			button.setImage(image);
		}
	}

	/** Helper function to avoid setting images unnecessarily */
	public static void setBackgroundImage(Control control, Image image) {
		if (control != null && control.getBackgroundImage() != image) {
			control.setBackgroundImage(image);
		}
	}
}

