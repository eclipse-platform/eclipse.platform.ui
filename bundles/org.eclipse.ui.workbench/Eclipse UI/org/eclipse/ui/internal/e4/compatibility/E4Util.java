/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 *
 */
public class E4Util {
	public static void unsupported(String msg) throws UnsupportedOperationException {
		WorkbenchPlugin.log("unsupported: " + msg); //$NON-NLS-1$
		// UnsupportedOperationException ex = new
		// UnsupportedOperationException(msg);
		// throw ex;
	}
}
