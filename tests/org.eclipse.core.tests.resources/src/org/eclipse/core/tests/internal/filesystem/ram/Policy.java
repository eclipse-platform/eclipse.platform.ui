/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.filesystem.ram;

import org.eclipse.core.runtime.*;

/**
 * 
 */
public class Policy {

	public static void error(String message) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.core.tests.resources", 1, message, null));
	}

	private Policy() {
		super();
	}

}
