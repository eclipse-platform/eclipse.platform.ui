/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.progress.internal.legacy;

/**
 * A common facility for parsing the <code>org.eclipse.ui/.options</code>
 * file.
 *
 * @since 2.1
 */
public class Policy {
	public static boolean DEFAULT = false;


	/**
	 * Whether or not to show system jobs at all times.
	 */
	public static boolean DEBUG_SHOW_ALL_JOBS = DEFAULT;

}
