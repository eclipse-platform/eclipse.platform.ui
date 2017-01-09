/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mohamed Hussein (Mentor Graphics) - initial API and implementation (Bug 386673)
 *******************************************************************************/
package org.eclipse.debug.ui;

/**
 * Optional enhancements for {@link ILaunchConfigurationTab}.
 * @since 3.9
 */
public interface ILaunchConfigurationTab2 extends ILaunchConfigurationTab {

	/**
	 * Returns a warning message to be displayed to the user
	 * or <code>null</code> if none is present.
	 * @return Returns a warning message to be displayed to the user
	 * or <code>null</code> if none is present.
	 */
	public String getWarningMessage();
}