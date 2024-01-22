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
package org.eclipse.ui.commands;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>ICommandManager</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see ICommandManager#addCommandManagerListener(ICommandManagerListener)
 * @see ICommandManager#removeCommandManagerListener(ICommandManagerListener)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead. This
 *             API is scheduled for deletion, see Bug 431177 for details
 * @see org.eclipse.core.commands.ICommandManagerListener
 * @noreference This interface is scheduled for deletion.
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
@Deprecated
@SuppressWarnings("all")
public interface ICommandManagerListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>ICommandManager</code> have changed. Specific details are described in
	 * the <code>CommandManagerEvent</code>.
	 *
	 * @param commandManagerEvent the commandManager event. Guaranteed not to be
	 *                            <code>null</code>.
	 */
	@Deprecated
	void commandManagerChanged(CommandManagerEvent commandManagerEvent);
}
