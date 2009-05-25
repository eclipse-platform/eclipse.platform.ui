/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers;

import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * This status handler is not bound to any product.
 * @since 3.5
 *
 */
public class FreeStatusHandler extends AbstractStatusHandler {

	private static AbstractStatusHandler tester;

	public synchronized void handle(StatusAdapter statusAdapter, int style) {
		if (tester != null) {
			tester.handle(statusAdapter, style);
		}
	}

	public static synchronized void setTester(AbstractStatusHandler tester) {
		FreeStatusHandler.tester = tester;
	}
}
