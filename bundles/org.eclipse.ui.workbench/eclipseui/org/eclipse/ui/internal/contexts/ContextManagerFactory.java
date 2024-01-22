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

package org.eclipse.ui.internal.contexts;

import org.eclipse.core.commands.contexts.ContextManager;

/**
 * This class allows clients to broker instances of
 * <code>IContextManager</code>.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @since 3.0
 */
public final class ContextManagerFactory {

	/**
	 * Creates a new instance of <code>ContextManagerWrapper</code>.
	 *
	 * @param contextManager The context manager that this context manager wrapper
	 *                       should wrap; must not be <code>null</code>.
	 * @return a new instance of <code>ContextManagerWrapper</code>. Clients should
	 *         not make assumptions about the concrete implementation outside the
	 *         contract of the interface. Guaranteed not to be <code>null</code>.
	 */
	public static ContextManagerLegacyWrapper getContextManagerWrapper(final ContextManager contextManager) {
		return new ContextManagerLegacyWrapper(contextManager);
	}

	/**
	 * This class should not be constructed.
	 */
	private ContextManagerFactory() {
		// Should not be called.
	}
}
