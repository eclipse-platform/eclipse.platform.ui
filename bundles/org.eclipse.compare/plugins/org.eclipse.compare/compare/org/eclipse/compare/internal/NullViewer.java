package org.eclipse.compare.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import org.eclipse.compare.CompareViewerSwitchingPane;

/**
 * Used whenever the input is null or no viewer can be found.
 */
public class NullViewer extends AbstractViewer {

	private Control fDummy;

	public NullViewer(Composite parent) {

		fDummy= new Tree(parent, SWT.NULL);

		CompareViewerSwitchingPane.clearToolBar(parent);
	}

	public Control getControl() {
		return fDummy;
	}
}
