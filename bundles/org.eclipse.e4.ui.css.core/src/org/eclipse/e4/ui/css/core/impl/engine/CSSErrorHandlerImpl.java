/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;

/**
 * Basic implementation for CSS Engine error handlers which print stack trace of
 * the exception throwed.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSSErrorHandlerImpl implements CSSErrorHandler {

	public static final CSSErrorHandler INSTANCE = new CSSErrorHandlerImpl();

	@Override
	public void error(Exception e) {
		e.printStackTrace();
	}
}
