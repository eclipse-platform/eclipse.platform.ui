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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

public final class ImageSupport {

	public static ImageDescriptor getImageDescriptor(String path) {
		return ImageDescriptor.createFromURLSupplier(true, () -> BundleUtility.find(PlatformUI.PLUGIN_ID, path));
	}

	private ImageSupport() {
	}
}
