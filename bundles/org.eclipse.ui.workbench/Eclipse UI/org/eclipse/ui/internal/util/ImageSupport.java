/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
package org.eclipse.ui.internal.util;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

public final class ImageSupport {

	public static ImageDescriptor getImageDescriptor(String path) {
		URL url = BundleUtility.find(PlatformUI.PLUGIN_ID, path);
		return ImageDescriptor.createFromURL(url);
	}

	private ImageSupport() {
	}
}
