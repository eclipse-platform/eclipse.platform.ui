/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests;

import org.eclipse.core.runtime.Plugin;

public class RefactoringCoreTestPlugin extends Plugin {

	private static RefactoringCoreTestPlugin fgDefault;

	public RefactoringCoreTestPlugin() {
		fgDefault= this;
	}

	public static RefactoringCoreTestPlugin getDefault() {
		return fgDefault;
	}

	public static String getPluginId() {
		return "org.eclipse.ltk.core.refactoring.tests"; //$NON-NLS-1$
	}
}
