/*******************************************************************************
 * Copyright (c) 20118 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
