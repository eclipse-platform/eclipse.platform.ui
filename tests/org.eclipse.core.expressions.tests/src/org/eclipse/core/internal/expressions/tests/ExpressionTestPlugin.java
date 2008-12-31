/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import org.eclipse.core.runtime.Plugin;

public class ExpressionTestPlugin extends Plugin {

	private static ExpressionTestPlugin fgDefault;

	public ExpressionTestPlugin() {
		fgDefault= this;
	}

	public static ExpressionTestPlugin getDefault() {
		return fgDefault;
	}

	public static String getPluginId() {
		return "org.eclipse.core.expressions.tests"; //$NON-NLS-1$
	}
}
