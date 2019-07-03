/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.ContributionManager;


/**
 * A dummy contribution manager, used just for testing.
 * Does not populate any widgets.
 */
class DummyContributionManager extends ContributionManager {
	@Override
	public void update(boolean force) {
		// ignore
	}
}
