package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

/**
 * @since 3.2
 *
 */
public class VariablesViewer extends AsynchronousTreeViewer{

	private VariablesView fView;

	private UIJob fRestoreJob = new UIJob("restore viewer state") { //$NON-NLS-1$
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fView.restoreState();
			return Status.OK_STATUS;
		}
	};
	
	public VariablesViewer(Composite parent, int style, VariablesView view) {
		super(parent, style);
		fView = view;
		fRestoreJob.setSystem(true);
	}

	protected void updateComplete(IAsynchronousRequestMonitor update) {
		super.updateComplete(update);
		if (fView != null) {
			fRestoreJob.schedule(100);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.treeviewer.AsynchronousTreeViewer#handlePresentationFailure(org.eclipse.debug.internal.ui.treeviewer.IPresentationRequestMonitor, org.eclipse.core.runtime.IStatus)
	 */
	protected void handlePresentationFailure(IAsynchronousRequestMonitor update, IStatus status) {
		fView.showMessage(status.getMessage());
	}
	
	/* (non-Javadoc)
	 * 
	 * Also update details area if required
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer#internalRefresh(java.lang.Object, org.eclipse.swt.widgets.Widget)
	 */
	protected void internalRefresh(Object element, Widget item) {
		super.internalRefresh(element, item);
		if (fView != null && element.equals(((IStructuredSelection)getSelection()).getFirstElement())) {
			fView.populateDetailPane();
		}
	}
	
	
	
	
}
