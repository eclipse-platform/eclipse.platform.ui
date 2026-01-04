/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SwtLeakExtension implements BeforeEachCallback, AfterEachCallback {

	private Set<Shell> preExistingShells;

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		preExistingShells = Set.of(workbench.getDisplay().getShells());
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		IWorkbench workbench = PlatformUI.getWorkbench();
		// Check for shell leak.
		List<String> leakedModalShellTitles = new ArrayList<>();
		Shell[] shells = workbench.getDisplay().getShells();
		for (Shell shell : shells) {
			if (!shell.isDisposed() && !preExistingShells.contains(shell)) {
				leakedModalShellTitles.add(shell.getText());
			}
		}
		if (!leakedModalShellTitles.isEmpty()) {
			Assertions.fail(context.getRequiredTestClass().getName() + "." + context.getDisplayName()
					+ " Test leaked modal shell(s): [" + String.join(", ", leakedModalShellTitles) + "]");
		}
	}

}
