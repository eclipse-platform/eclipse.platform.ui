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
package org.eclipse.core.internal.plugins;

import org.eclipse.core.internal.registry.IExtensionLinker;
import org.eclipse.core.runtime.model.ExtensionModel;
import org.eclipse.core.runtime.model.ExtensionPointModel;
import org.eclipse.core.runtime.registry.IExtension;
import org.eclipse.core.runtime.registry.IExtensionPoint;

public class ExtensionLinker implements IExtensionLinker {
	public void link(IExtensionPoint extPoint, IExtension[] extensions) {
		ExtensionPointModel xpm = (ExtensionPointModel) ((ExtensionPointWrapper)extPoint).getAdapted();
		if (extensions == null || extensions.length == 0) {
			xpm.setDeclaredExtensions(null);
			return;
		}
		ExtensionModel[] extModels = new ExtensionModel[extensions.length];
		for (int i = 0; i < extModels.length; i++)
			extModels[i] = (ExtensionModel) ((ExtensionWrapper)extensions[i]).getAdapted();
		xpm.setDeclaredExtensions(extModels);			
	}
}
