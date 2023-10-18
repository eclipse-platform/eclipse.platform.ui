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
package org.eclipse.ltk.ui.refactoring.tests;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RefactoringUITestPlugin extends AbstractUIPlugin {

	private static RefactoringUITestPlugin fgDefault;

	public RefactoringUITestPlugin() {
		fgDefault= this;
	}

	public static RefactoringUITestPlugin getDefault() {
		return fgDefault;
	}

	public static String getPluginId() {
		return "org.eclipse.ltk.ui.refactoring.tests"; //$NON-NLS-1$
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR,  "Internal Error", e));
	}
}
