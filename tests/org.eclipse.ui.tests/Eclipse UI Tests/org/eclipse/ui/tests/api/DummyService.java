/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

package org.eclipse.ui.tests.api;

import org.eclipse.ui.internal.services.INestable;

/**
 *
 * @since 3.5
 * @author Prakash G.R.
 *
 */
public class DummyService implements INestable {

	private boolean active;


	public boolean isActive() {
		return active;
	}

	@Override
	public void activate() {
		active = true;
	}


	@Override
	public void deactivate() {
		active = false;
	}

}
