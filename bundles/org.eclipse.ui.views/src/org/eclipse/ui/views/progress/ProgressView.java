package org.eclipse.ui.views.progress;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.progress.ProgressContentProvider;
import org.eclipse.ui.internal.progress.ProgressLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ProgressView extends ViewPart implements IViewPart {

	TreeViewer viewer;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer =
			new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setUseHashlookup(true);
		initContentProvider(viewer);
		initLabelProvider(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// XXX Auto-generated method stub

	}
	/**
	 * Sets the content provider for the viewer.
	 * 
	 * @param viewer the viewer
	 * @since 2.0
	 */
	protected void initContentProvider(TreeViewer viewer) {
		IContentProvider provider = new ProgressContentProvider(viewer);
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
	}

	/**
	 * Sets the label provider for the viewer.
	 * 
	 * @param viewer the viewer
	 */
	protected void initLabelProvider(TreeViewer viewer) {
		viewer.setLabelProvider(new ProgressLabelProvider());

	}
}