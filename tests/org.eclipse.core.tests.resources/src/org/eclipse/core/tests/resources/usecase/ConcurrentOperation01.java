package org.eclipse.core.tests.resources.usecase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IWorkspace;
/**
 * This operation does not change any resource. It only has one
 * syncPoint in order to inform that it's runnning.
 */
public class ConcurrentOperation01 extends ConcurrentOperation {

	/** indicates if this operation has entered the run() method */
	protected boolean isRunning;
public ConcurrentOperation01(IWorkspace workspace) {
	super(workspace);
	reset();
}
protected void assertRequisites() throws Exception {}
public boolean isRunning() {
	return isRunning;
}
public void reset() {
	super.reset();
	isRunning = false;
}
public void run(IProgressMonitor monitor) throws CoreException {
	isRunning = true;
	syncPoint();
}
}
