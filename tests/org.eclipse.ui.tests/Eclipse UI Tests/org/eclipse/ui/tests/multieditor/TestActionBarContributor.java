package org.eclipse.ui.tests.multieditor;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

public class TestActionBarContributor extends EditorActionBarContributor {

	public TestActionBarContributor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToCoolBar(org.eclipse.jface.action.ICoolBarManager)
	 */
	public void contributeToCoolBar(ICoolBarManager coolBarManager) {
		super.contributeToCoolBar(coolBarManager);
	}
}
