/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * A breakpoint manager listener is notified when the breakpoint manager's
 * enablement changes. When the breakpoint manager is disabled, no breakpoints
 * should be honored. When the breakpoint manager is enabled, breakpoints should
 * be honored as usual.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IBreakpointManager
 * @since 3.0
 */
public interface IBreakpointManagerListener {

	/**
	 * Notifies the listener that the breakpoint manager's enablement
	 * has changed.
	 *
	 * @param enabled whether or not the breakpoint manager is enabled
	 */
	public void breakpointManagerEnablementChanged(boolean enabled);

	/**
	 * Notifies the listener that the breakpoint manager's trigger point has
	 * changed.
	 *
	 * @param triggerBreakpoint new trigger breakpoint or <code>null<code>
	 * @since 3.11
	 */
	public default void breakpointManagerTriggerPointChanged(IBreakpoint triggerBreakpoint) {
	}
}
