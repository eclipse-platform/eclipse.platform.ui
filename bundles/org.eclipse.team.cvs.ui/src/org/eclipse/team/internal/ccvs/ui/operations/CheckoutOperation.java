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

	/**
	 * @param shell
	 */
	public CheckoutOperation(Shell shell, ICVSRemoteFolder[] remoteFolders) {
		super(shell, remoteFolders);
	}

	/**
	 * Return the string that is to be used as the task name for the operation
	 * 
	 * @param remoteFolders
	 * @return
	 */
	protected abstract String getTaskName();
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void execute(IProgressMonitor monitor) throws CVSException, InterruptedException {
		String taskName = getTaskName();
		monitor.beginTask(taskName, 100);
		monitor.setTaskName(taskName);
		checkout(getRemoteFolders(), Policy.subMonitorFor(monitor, 100));
	}

	/**
	 * @return
	 */
	protected ICVSRemoteFolder[] getRemoteFolders() {
		return (ICVSRemoteFolder[])getRemoteResources();
	}
	
	/**
	 * Checkout the selected remote folders in a form appropriate for the operation subclass.
	 * @param folders
	 * @param monitor
	 */
	protected abstract void checkout(ICVSRemoteFolder[] folders, IProgressMonitor monitor)  throws CVSException;
}
