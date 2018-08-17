/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12.model;
public class M2Core {
	public static M2Resource getModelObject(Object object) {
		if (object instanceof M1File) {
			M1File file = (M1File) object;
			if (file.getResource().getName().indexOf("2") >= 0) {
				return new M2File(file.getResource());
			}
		}
		return null;
	}
}
