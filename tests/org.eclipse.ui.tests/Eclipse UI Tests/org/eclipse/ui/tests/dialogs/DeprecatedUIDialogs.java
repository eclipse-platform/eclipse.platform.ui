/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.dialogs;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.YesNoCancelListSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchPartLabelProvider;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.junit.Test;

@SuppressWarnings("removal")
public class DeprecatedUIDialogs {

	private Shell getShell() {
		return DialogCheck.getShell();
	}

	@Test
	public void testSaveAll() {
		YesNoCancelListSelectionDialog dialog = new YesNoCancelListSelectionDialog(
				getShell(), new AdaptableList(),
				new WorkbenchContentProvider(),
				new WorkbenchPartLabelProvider(), WorkbenchMessages.EditorManager_saveResourcesMessage);
		dialog.setTitle(WorkbenchMessages.EditorManager_saveResourcesTitle);
		DialogCheck.assertDialog(dialog);
	}

}

