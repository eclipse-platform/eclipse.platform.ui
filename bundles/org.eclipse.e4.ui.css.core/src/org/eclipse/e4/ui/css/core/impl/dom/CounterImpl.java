/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.w3c.dom.css.Counter;

public class CounterImpl implements Counter {

	private static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED"; //$NON-NLS-1$

	@Override
	public String getIdentifier() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public String getListStyle() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

	@Override
	public String getSeparator() {
		throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
	}

}
