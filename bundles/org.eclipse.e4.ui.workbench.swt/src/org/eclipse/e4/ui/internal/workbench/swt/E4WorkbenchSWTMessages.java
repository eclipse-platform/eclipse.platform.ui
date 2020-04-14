/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import org.eclipse.osgi.util.NLS;

public class E4WorkbenchSWTMessages extends NLS {

	static {
		NLS.initializeMessages(E4WorkbenchSWTMessages.class.getPackage().getName() + ".messages", //$NON-NLS-1$
				E4WorkbenchSWTMessages.class);
	}

	public static String openCommandFromURIHandler_confirm_title;
	public static String openCommandFromURIHandler_confirm_message;
	public static String openCommandFromUIHandler_undefined;

}
