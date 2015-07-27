/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

public class ThemeElementDefinitionHelper {
	public static String escapeId(String id) {
		return id.replaceAll("\\.", "-");
	}

	public static String normalizeId(String id) {
		return id.replaceAll("-", ".");
	}
}
