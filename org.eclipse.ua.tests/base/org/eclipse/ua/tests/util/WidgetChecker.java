/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.util;

import junit.framework.Assert;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;

/*
 * A utility for checking widget properties.
 */
public class WidgetChecker {
	
	/*
	 * Checks if the control is non-null, not disposed, and is visible. Msg is the
	 * description of the widget (e.g. "The Tree in the cheat sheets dialog").
	 */
	public static void checkControl(String msg, Control c) {
		Assert.assertNotNull("The following control was unexpectedly null: " + msg, c);
		Assert.assertFalse("The following control was unexpectedly disposed: " + msg, c.isDisposed());
		Assert.assertTrue("The following control was unexpectedly not visible: " + msg, c.isVisible());
	}
	
	/*
	 * Checks if the item is non-null and not disposed. Msg is the description of the item
	 * (e.g. "Tree item in the cheat sheets dialog").
	 */
	public static void checkItem(String msg, Item c) {
		Assert.assertNotNull("The following item was unexpectedly null: " + msg, c);
		Assert.assertFalse("The following item was unexpectedly disposed: " + msg, c.isDisposed());
	}
}
