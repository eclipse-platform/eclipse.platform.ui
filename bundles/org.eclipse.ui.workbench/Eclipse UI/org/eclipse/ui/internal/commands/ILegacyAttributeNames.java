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
	public final String ENABLED = IAction.ENABLED;

	/**
	 * Whether the handler is capable of handling delegation of responsibilities
	 * at this time.
	 */
	public final String HANDLED = IAction.HANDLED;
}
