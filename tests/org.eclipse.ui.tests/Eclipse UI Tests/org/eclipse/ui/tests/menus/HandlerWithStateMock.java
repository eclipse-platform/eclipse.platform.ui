/*******************************************************************************
 * Copyright (c) 2023, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vasili Gulevich - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.menus;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.State;

public final class HandlerWithStateMock extends AbstractHandlerWithState {
	public static HandlerWithStateMock INSTANCE;

	public HandlerWithStateMock() {
		INSTANCE = this;
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {

	}

	@Override
	public Object execute(ExecutionEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		INSTANCE = null;
		super.dispose();
	}

}
