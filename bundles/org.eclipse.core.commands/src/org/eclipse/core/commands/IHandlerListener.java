/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.core.commands;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>IHandler</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.1
 * @see IHandler#addHandlerListener(IHandlerListener)
 * @see IHandler#removeHandlerListener(IHandlerListener)
 */
public interface IHandlerListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>IHandler</code> have changed. Specific details are described in
	 * the <code>HandlerEvent</code>.
	 *
	 * @param handlerEvent
	 *            the handler event. Guaranteed not to be <code>null</code>.
	 */
	void handlerChanged(HandlerEvent handlerEvent);
}
