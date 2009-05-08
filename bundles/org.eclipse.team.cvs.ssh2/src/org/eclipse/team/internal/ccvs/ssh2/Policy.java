/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;

public class Policy {
	// debug constants
	public static boolean DEBUG = false;

	static {
		// init debug options
		if (CVSSSH2Plugin.getDefault().isDebugging())
			DEBUG = true;
	}
}
