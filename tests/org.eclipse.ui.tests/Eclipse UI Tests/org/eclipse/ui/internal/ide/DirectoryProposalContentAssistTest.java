/*******************************************************************************
 * Copyright (c) 2018 Fabian Pfaff and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabian Pfaff - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DirectoryProposalContentAssistTest extends DirectoryProposalContentAssistTestCase {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Ignore // see Bug 540441 and Bug 275393
	@Test
	public void fileSeparatorOpensProposalPopup() throws Exception {
		getFieldAssistWindow().open();
		sendFocusInToControl();

		sendKeyEventToControl(File.separatorChar);
		waitForDirectoryContentAssist();

		assertTwoShellsUp();
	}

	@Ignore // see Bug 540441 and Bug 275393
	@Test
	public void opensProposalPopupWithSubfoldersAsProposals() throws Exception {
		folder.newFolder("foo");
		folder.newFolder("bar");

		getFieldAssistWindow().open();
		sendFocusInToControl();

		setControlContent(folder.getRoot().getAbsolutePath());
		sendKeyEventToControl(File.separatorChar);
		waitForDirectoryContentAssist();

		assertProposalSize(2);
	}

}
