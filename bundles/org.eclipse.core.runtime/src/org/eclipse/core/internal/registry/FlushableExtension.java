/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.lang.ref.SoftReference;
import org.eclipse.core.runtime.IConfigurationElement;

public class FlushableExtension extends Extension {

	public FlushableExtension() {
		super();
	}

	public IConfigurationElement[] getConfigurationElements() {
		synchronized (this) {
			if (!fullyLoaded) {
				fullyLoaded = true;
				RegistryCacheReader reader = getRegistry().getCacheReader();
				if (reader != null)
					elements = new SoftReference(reader.loadConfigurationElements(this, subElementsCacheOffset));
			}
			if (elements == null)
				elements = new IConfigurationElement[0];

			if (((SoftReference) elements).get() == null) {
				RegistryCacheReader reader = getRegistry().getCacheReader();
				if (reader != null)
					elements = new SoftReference(reader.loadConfigurationElements(this, subElementsCacheOffset));
			}
		}
		return (IConfigurationElement[]) ((SoftReference) elements).get();
	}

	public void setSubElements(IConfigurationElement[] value) {
		elements = new SoftReference(value);
	}
}