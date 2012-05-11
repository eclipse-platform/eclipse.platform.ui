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

import org.eclipse.core.runtime.*;

/**
 * Test class for the executable extensions.
 * @since 3.2
 */
public class ExecutableRegistryObject implements IExecutableExtensionFactory {

	public static boolean createCalled = false;

	public ExecutableRegistryObject() {
		// intentionally left empty
	}

	public Object create() throws CoreException {
		if (createCalled) {
			Status status = new Status(IStatus.ERROR, "org.eclipse.core.tests.runtime", 0, "Duplicate executable extension call.", null); //$NON-NLS-1$ //$NON-NLS-2$
			throw new CoreException(status);
		}
		createCalled = true;
		return new ExecutableRegistryObject();
	}
}
