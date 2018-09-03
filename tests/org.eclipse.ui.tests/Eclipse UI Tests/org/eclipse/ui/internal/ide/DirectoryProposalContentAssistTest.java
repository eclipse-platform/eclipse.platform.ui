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
