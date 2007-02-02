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

import org.eclipse.swt.widgets.Control;

/**
 * This interface computes the prefix of a message that is created in the
 * context of a control. Since messages are rolled up to the header in the
 * message manager, it is important to create a prefix to indicate the context
 * of a message in the form. Typically the prefix is computed by looking at the
 * label that preceeds the control, if present. Alternative providers may
 * include other text to further specify the location of the message.
 * 
 * @see IMessageManager
 * @see IMessage
 * @since 3.3
 */
public interface IMessagePrefixProvider {
	/**
	 * Returns the computed prefix for the provided control.
	 * 
	 * @param control
	 *            the control to provide the prefix for
	 * @return the computed prefix
	 */
	String getPrefix(Control control);
}