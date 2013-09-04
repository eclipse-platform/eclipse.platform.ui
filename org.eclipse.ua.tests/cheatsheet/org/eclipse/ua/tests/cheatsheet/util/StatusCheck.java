/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.util;

import org.junit.Assert;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;


/**
 * Utilities for checking status
 */
public class StatusCheck {

	public static void assertStatusContains(IStatus status, String text) {
		if (status.getMessage().indexOf(text) == -1) {
			Assert.fail("Expected status message to contain '" + text + "' actual message is '"
					+ status.getMessage() + "'");
		}
	}
	
	public static void assertMultiStatusContains(IStatus status, String text) {
		Assert.assertTrue(status instanceof MultiStatus);
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i].getMessage().indexOf(text) >= 0) {
				return;
			}
		}
		if (status.getMessage().indexOf(text) == -1) {
			Assert.fail("Expected status message to contain '" + text + "' status.toString = '"
					+ status.toString() + "'");
		}
	}
}
