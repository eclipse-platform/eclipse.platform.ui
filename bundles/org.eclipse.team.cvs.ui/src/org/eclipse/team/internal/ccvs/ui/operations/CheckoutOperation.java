/*
 * Created on 2-Jun-03
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class CheckoutOperation extends RemoteOperation {

	public CheckoutOperation(Shell shell, ICVSRemoteFolder[] remoteFolders) {
		super(shell, remoteFolders);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		String taskName = getTaskName();
		monitor.beginTask(taskName, 100);
		monitor.setTaskName(taskName);
		checkout(getRemoteFolders(), Policy.subMonitorFor(monitor, 100));
	}

	protected ICVSRemoteFolder[] getRemoteFolders() {
		return (ICVSRemoteFolder[])getRemoteResources();
	}
	
	/**
	 * Checkout the selected remote folders in a form appropriate for the operation subclass.
	 * @param folders
	 * @param monitor
	 */
	protected abstract void checkout(ICVSRemoteFolder[] folders, IProgressMonitor monitor)  throws CVSException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#canRunAsJob()
	 */
	public boolean canRunAsJob() {
		return true;
	}
}
