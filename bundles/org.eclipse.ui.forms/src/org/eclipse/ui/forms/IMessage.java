/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.forms;

import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Classes that implement this interface encapsulate typed messages as defined
 * in {@link IMessageProvider}. In addition to types and message text, message
 * objects also have unique keys that can be used to look them up and optional
 * data objects for application use.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UA team.
 * </p>
 * 
 * @see IMessageContainerWithDetails
 * @since 3.3
 */
public interface IMessage extends IMessageProvider {
	/**
	 * Returns a unique message key.
	 * 
	 * @return message key
	 */
	Object getKey();

	/**
	 * Returns the application data.
	 * 
	 * @return the application data object or <code>null</code> if not set.
	 */
	Object getData();
}