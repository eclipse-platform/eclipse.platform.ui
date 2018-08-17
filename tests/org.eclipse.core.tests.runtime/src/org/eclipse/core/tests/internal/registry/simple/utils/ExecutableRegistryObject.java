/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public Object create() throws CoreException {
		if (createCalled) {
			Status status = new Status(IStatus.ERROR, "org.eclipse.core.tests.runtime", 0, "Duplicate executable extension call.", null); //$NON-NLS-1$ //$NON-NLS-2$
			throw new CoreException(status);
		}
		createCalled = true;
		return new ExecutableRegistryObject();
	}
}
