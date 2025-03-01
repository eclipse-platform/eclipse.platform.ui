/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
import org.junit.Assert;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class SwtLeakTestWatcher extends TestWatcher {

	IWorkbench workbench;
	Set<Shell> preExistingShells;

	@Override
	protected void starting(Description description) {
		workbench = PlatformUI.getWorkbench();
		preExistingShells = Set.of(workbench.getDisplay().getShells());
		super.starting(description);
	}

	@Override
	protected void finished(Description description) {
		// Check for shell leak.
		List<String> leakedModalShellTitles = new ArrayList<>();
		Shell[] shells = workbench.getDisplay().getShells();
		for (Shell shell : shells) {
			if (!shell.isDisposed() && !preExistingShells.contains(shell)) {
				leakedModalShellTitles.add(shell.getText());
				// closing shell may introduce "not disposed" errors in next tests :
				// shell.close();
			}
		}
		if (!leakedModalShellTitles.isEmpty()) {
			Assert.fail(description.getClassName() + "." + description.getDisplayName()
					+ " Test leaked modal shell(s): [" + String.join(", ", leakedModalShellTitles) + "]");
		}
		super.finished(description);
	}

}