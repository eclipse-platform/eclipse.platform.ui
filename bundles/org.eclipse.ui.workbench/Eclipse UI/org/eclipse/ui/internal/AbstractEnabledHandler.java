/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.HandlerEvent;

/**
 * Abstract base class that provides the enabled state, where changing the state
 * fires the HandlerEvent.
 *
 * @since 3.3
 */
public abstract class AbstractEnabledHandler extends AbstractHandler {

	private boolean enabled = true;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled state. Changing the state fires the HandlerEvent.
	 *
	 * @param isEnabled true to enable the handler, false to disable.
	 */
	protected void setEnabled(boolean isEnabled) {
		if (enabled != isEnabled) {
			enabled = isEnabled;
			fireHandlerChanged(new HandlerEvent(this, true, false));
		}
	}
}
