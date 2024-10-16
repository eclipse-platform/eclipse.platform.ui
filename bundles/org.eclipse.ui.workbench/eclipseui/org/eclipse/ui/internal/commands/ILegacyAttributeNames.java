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
package org.eclipse.ui.internal.commands;

import org.eclipse.jface.action.IAction;

/**
 * This defines attribute names that were understood in Eclipse 3.0. This is
 * used to provide legacy support for the attribute map property.
 *
 * @since 3.1
 */
public interface ILegacyAttributeNames {

	/**
	 * Whether the handler is capable of executing right now.
	 */
	String ENABLED = IAction.ENABLED;

	/**
	 * Whether the handler is capable of handling delegation of responsibilities at
	 * this time.
	 */
	String HANDLED = IAction.HANDLED;
}
