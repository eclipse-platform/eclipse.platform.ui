/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.ui.commands;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>ICommand</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see ICommand#addCommandListener(ICommandListener)
 * @see ICommand#removeCommandListener(ICommandListener)
 * @see org.eclipse.core.commands.ICommandListener
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @noreference This interface is scheduled for deletion.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface ICommandListener {

	/**
	 * Notifies that one or more properties of an instance of <code>ICommand</code>
	 * have changed. Specific details are described in the
	 * <code>CommandEvent</code>.
	 *
	 * @param commandEvent the command event. Guaranteed not to be
	 *                     <code>null</code>.
	 */
	@Deprecated
	void commandChanged(CommandEvent commandEvent);
}
