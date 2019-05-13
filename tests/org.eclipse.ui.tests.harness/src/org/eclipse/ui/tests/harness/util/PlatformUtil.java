/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ui.tests.harness.util;

import org.eclipse.jface.util.Util;

/**
 * The Platform Util class is used to test for which platform we are in
 */
public class PlatformUtil {

	/**
	 * Determine if we are running on the Mac platform.
	 *
	 * @return true if we are runnig on the Mac platform.
	 */
	public static boolean onMac() {
		return Util.isMac();
	}
}
