/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

public class ExtensionLinker implements IExtensionLinker {
	public void link(IExtensionPoint extPoint, IExtension[] extensions) {
		ExtensionPoint xpm = (ExtensionPoint) extPoint;
		if (extensions == null || extensions.length == 0) {
			xpm.setExtensions(null);
			return;
		}
		xpm.setExtensions(extensions);
	}
}