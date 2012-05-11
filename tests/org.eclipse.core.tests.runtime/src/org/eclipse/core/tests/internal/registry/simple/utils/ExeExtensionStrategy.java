/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple.utils;

import java.io.File;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.core.runtime.spi.RegistryStrategy;

/**
 * Registry strategy that uses class loader from this bundle to process executable 
 * extensions.
 * @since 3.2
 */
public class ExeExtensionStrategy extends RegistryStrategy {

	public ExeExtensionStrategy(File[] theStorageDir, boolean[] cacheReadOnly) {
		super(theStorageDir, cacheReadOnly);
	}

	public Object createExecutableExtension(RegistryContributor defaultContributor, String className, String requestedContributorName) {
		Class classInstance = null;
		try {
			classInstance = Class.forName(className);
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			return null;
		}

		// create a new instance
		Object result = null;
		try {
			result = classInstance.newInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}

		return result;
	}
}
