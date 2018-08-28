/*******************************************************************************
 * Copyright (c) 20118 Andrey Loskutov.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * Workaround for bug 537046
 */
public interface IMenuServiceWorkaround {

	/**
	 * Disposes contributions created by service for given part. See bug 537046.
	 *
	 * @param site
	 * @param part
	 */
	void clearContributions(PartSite site, MPart part);

}
