/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.propertytester;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.PropertyTester;

/**
 * A property tester for testing platform properties. Can test whether or
 * not a given bundle is installed in the running environment, as well as
 * the id of the currently active product.
 * 
 * For example:
 * <test property="org.eclipse.core.runtime.product" args="org.eclipse.sdk.ide"/>
 * <test property="org.eclipse.core.runtime.isBundleInstalled" args="org.eclipse.core.expressions"/>
 */
public class PlatformPropertyTester extends PropertyTester {
	
	private static final String PROPERTY_PRODUCT = "product"; //$NON-NLS-1$
	private static final String PROPERTY_IS_BUNDLE_INSTALLED = "isBundleInstalled"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (Platform.class.equals(receiver)) {
			if (PROPERTY_PRODUCT.equals(property)) {
				IProduct product= Platform.getProduct();
				if (product != null) {
					return product.getId().equals(expectedValue);
				}
				return false;
			} else if (PROPERTY_IS_BUNDLE_INSTALLED.equals(property) && args.length >= 1 && args[0] instanceof String) {
				return Platform.getBundle((String)args[0]) != null;
			}
		}
		return false;
	}
}
