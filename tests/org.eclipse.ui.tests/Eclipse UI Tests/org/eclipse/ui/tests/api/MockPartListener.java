package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class MockPartListener implements IPartListener {
	private CallHistory callTrace;

	private IWorkbenchPart partMask;
	public MockPartListener()
	{
		callTrace = new CallHistory();
	}
	
	public void setPartMask( IWorkbenchPart part )
	{
		partMask = part;
	}
	
	public CallHistory getCallHistory()
	{
		return callTrace;
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if( partMask == part )
			callTrace.add( this, "partActivated" );
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	if( partMask == part )		
		callTrace.add( this, "partBroughtToTop" );
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
			if( partMask == part )
		callTrace.add( this, "partClosed" );
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
			if( partMask == part )
		callTrace.add( this, "partDeactivated" );
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
			if( partMask == part )
		callTrace.add( this, "partOpened" );
	}
}

