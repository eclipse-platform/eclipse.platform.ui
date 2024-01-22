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
 * of changes to one or more instances of <code>IContext</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.0
 * @see org.eclipse.ui.contexts.IContext#addContextListener(IContextListener)
 * @see org.eclipse.ui.contexts.IContext#removeContextListener(IContextListener)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.contexts.IContextListener
 */
@Deprecated
public interface IContextListener {

	/**
	 * Notifies that one or more properties of an instance of <code>IContext</code>
	 * have changed. Specific details are described in the
	 * <code>ContextEvent</code>.
	 *
	 * @param contextEvent the context event. Guaranteed not to be
	 *                     <code>null</code>.
	 */
	void contextChanged(ContextEvent contextEvent);
}
