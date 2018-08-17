/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

package org.eclipse.ui.dynamic;

import org.eclipse.ui.IStartup;

/**
 * @since 3.1
 */
public class DynamicStartup implements IStartup {

	public static Throwable history;

	/**
	 *
	 */
	public DynamicStartup() {
		super();
	}

	@Override
	public void earlyStartup() {
		history = new Throwable();
		history.fillInStackTrace();
	}
}
