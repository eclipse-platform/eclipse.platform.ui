package org.eclipse.ui.internal.ide;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.tests.fieldassist.ComboFieldAssistWindow;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.5
 *
 */
public class DirectoryProposalContentAssistWindow extends ComboFieldAssistWindow {

	protected static class DirectoryProposalContentAssistTestExtension extends DirectoryProposalContentAssist {
		@Override
		public void wait(int timeout) throws InterruptedException, ExecutionException, TimeoutException {
			super.wait(timeout);
		}
	}

	private DirectoryProposalContentAssistTestExtension contentAssist;

	@Override
	protected ContentProposalAdapter createContentProposalAdapter(Control control) {
		contentAssist = new DirectoryProposalContentAssistTestExtension();
		contentAssist.apply((Combo) control);
		return contentAssist.getContentProposalAdapter();
	}

	@Override
	public ContentProposalAdapter getContentProposalAdapter() {
		return super.getContentProposalAdapter();
	}

	public DirectoryProposalContentAssistTestExtension getContentAssist() {
		return contentAssist;
	}
}
