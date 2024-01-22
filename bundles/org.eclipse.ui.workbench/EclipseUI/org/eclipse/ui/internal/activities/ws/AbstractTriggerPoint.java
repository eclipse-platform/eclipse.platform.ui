/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.ui.activities.ITriggerPoint;

/**
 * @since 3.1
 */
public abstract class AbstractTriggerPoint implements ITriggerPoint {

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITriggerPoint) {
			return getId().equals(((ITriggerPoint) obj).getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

}
