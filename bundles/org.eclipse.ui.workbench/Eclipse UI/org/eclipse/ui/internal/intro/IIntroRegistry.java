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
package org.eclipse.ui.internal.intro;

import org.eclipse.ui.intro.IIntroDescriptor;

/**
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 */
public interface IIntroRegistry {
	
	/**
	 * Return the number of introduction extensions known by this registry.
	 * 
	 * @return the number of introduction extensions known by this registry 
	 */
	int getIntroCount();
	
	/** 
	 * Return the introduction extensions known by this registry.
	 * 
	 * @return the introduction extensions known by this registry 
	 */
	IIntroDescriptor [] getIntros();
	
	/**
	 * Return the introduction extension that is bound to the given product.
	 * 
	 * @param productId the product identifier
	 * @return the introduction extension that is bound to the given product, 
	 * or <code>null</code> if there is no such binding
	 */
	IIntroDescriptor getIntroForProduct(String productId);
}
