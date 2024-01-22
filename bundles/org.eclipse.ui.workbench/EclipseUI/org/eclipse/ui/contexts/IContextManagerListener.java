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

package org.eclipse.ui.contexts;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>IContextManager</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see IContextManager#addContextManagerListener(IContextManagerListener)
 * @see IContextManager#removeContextManagerListener(IContextManagerListener)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.contexts.IContextManagerListener
 */
@Deprecated
public interface IContextManagerListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>IContextManager</code> have changed. Specific details are described in
	 * the <code>ContextManagerEvent</code>.
	 *
	 * @param contextManagerEvent the context manager event. Guaranteed not to be
	 *                            <code>null</code>.
	 */
	void contextManagerChanged(ContextManagerEvent contextManagerEvent);
}
