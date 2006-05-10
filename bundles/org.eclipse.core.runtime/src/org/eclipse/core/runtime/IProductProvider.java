/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * Product providers define products (units of branding) which have been installed in
 * the current system.  Typically, a configuration agent (i.e., plug-in installer) will 
 * define a product provider so that it can report to the system the products it has installed.
 * <p>
 * Products are normally defined and installed in the system using extensions to the 
 * <code>org.eclipse.core.runtime.products</code> extension point.  In cases where 
 * the notion of product is defined by alternate means (e.g., primary feature), an <code>IProductProvider</code>
 * can be installed in this extension point using an executable extension.  The provider
 * then acts as a proxy for the product extensions that represent the products installed.
 * </p>
 * 
 * @see IProduct
 * @since 3.0
 */
public interface IProductProvider {
	/**
	 * Returns the human-readable name of this product provider.
	 * 
	 * @return the name of this product provider
	 */
	public String getName();

	/**
	 * Returns the products provided by this provider.
	 * 
	 * @return the products provided by this provider
	 */
	public IProduct[] getProducts();
}
