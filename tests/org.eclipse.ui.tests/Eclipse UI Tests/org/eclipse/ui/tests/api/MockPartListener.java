package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.tests.util.CallHistory;

public class MockPartListener implements IPartListener {
	private CallHistory callTrace;

	public MockPartListener() {
		callTrace = new CallHistory( this );
	}

	public CallHistory getCallHistory() {
		return callTrace;
	}

	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		callTrace.add("partActivated");
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		callTrace.add("partBroughtToTop");
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		callTrace.add( "partClosed");
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		callTrace.add( "partDeactivated");
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		callTrace.add( "partOpened");
	}
}