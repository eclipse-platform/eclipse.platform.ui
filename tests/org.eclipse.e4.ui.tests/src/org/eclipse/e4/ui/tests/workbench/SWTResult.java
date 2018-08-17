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

package org.eclipse.e4.ui.tests.workbench;

import java.util.ArrayList;

/**
 * Class used to capture the SWT structure expected when rendering a partuclar
 * UI model.
 */
public class SWTResult {
	public Class clazz;
	public String text;
	public ArrayList kids = new ArrayList();

	public SWTResult(Class theClass, String theText, SWTResult[] children) {
		clazz = theClass;
		text = theText;
		if (children != null) {
			for (SWTResult result : children) {
				kids.add(result);
			}
		}
	}
}
