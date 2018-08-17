/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
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
package org.akrogen.tkui.css.swt.resources;

import java.io.InputStream;

public class CSSSWTResources {

	/*--- SWT Styles --*/

	public static InputStream getSWT() {
		return CSSSWTResources.class.getResourceAsStream("swt/swt.css");
	}

	public static InputStream getSWTMatrix() {
		return CSSSWTResources.class.getResourceAsStream("swt/Matrix.css");
	}

	public static InputStream getSWTVista() {
		return CSSSWTResources.class.getResourceAsStream("swt/Vista.css");
	}

	public static InputStream getSWTOsx() {
		return CSSSWTResources.class.getResourceAsStream("swt/Osx.css");
	}

	public static InputStream getSWTPseudoCLass() {
		return CSSSWTResources.class
				.getResourceAsStream("swt/pseudo-class.css");
	}
}
