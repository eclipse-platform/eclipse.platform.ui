/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import org.eclipse.compare.INavigatable;

/**
 * This interface is being kept as it was required by clients in some situations.
 * @deprecated use {@link INavigatable}
 */ 
public interface IOpenable {
	
	static final String OPENABLE_PROPERTY= "org.eclipse.compare.internal.Openable"; //$NON-NLS-1$
	
	/**
	 * Opens the selected element
	 */
	void openSelected();
}
