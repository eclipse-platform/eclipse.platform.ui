/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * <p>
 * Provides utilities for working with adaptable and non-adaptable objects.
 * </p>
 *  
 */
public class AdaptabilityUtility {


	/**
	 * <p>
	 * Returns an adapter of the requested type (anAdapterType)
	 * @param anElement The element to adapt, which may or may not implement {@link IAdaptable}
	 * @param anAdapterType The class type to return 
	 * @return
	 */
	public static Object getAdapter(Object anElement, Class anAdapterType) {
		if (anElement == null)
			return null;
		else if (anElement instanceof IAdaptable)
			return ((IAdaptable) anElement).getAdapter(anAdapterType);
		else
			return Platform.getAdapterManager().getAdapter(anElement, anAdapterType);
	}

}