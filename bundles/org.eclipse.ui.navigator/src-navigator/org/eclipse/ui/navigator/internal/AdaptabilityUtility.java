/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * <p>
 * Provides utilities for working with adaptable and non-adaptable objects.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
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
