package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;
import junit.framework.*;

public class IPerspectiveListenerTest extends AbstractTestCase implements IPerspectiveListener {
	private int fEvent;
	private IWorkbenchWindow fWindow;
	private IWorkbenchPage fPage, fPageMask;
	private IPerspectiveDescriptor fPerMask;
	
	public IPerspectiveListenerTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{
		fEvent = Tool.NONE;		
		try{
			fWindow = openTestWindow();		
		}catch( WorkbenchException e )
		{
			fail();
		}
		fWindow.addPerspectiveListener( this );
	}
	
	public void tearDown()
	{
		fWindow.removePerspectiveListener( this );
	}
	
	public void testPerspectiveActivated()
	{
		fPageMask = fPage;
		fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
		fPage.setPerspective( fPerMask );
		
		assert( Tool.isActivated( fEvent ) );
	}

	public void testPerspectiveChanged()
	{
				fPageMask = fPage;
		fPerMask = fWorkbench.getPerspectiveRegistry().findPerspectiveWithId(EmptyPerspective.PERSP_ID );
		fPage.setPerspective( fPerMask );
		
		assert( Tool.isActivated( fEvent ) );
	}
	
	/**
	 * @see IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
	 */
	public void perspectiveActivated( IWorkbenchPage page, IPerspectiveDescriptor perspective ) 
	{
		if( page == fPageMask && perspective == fPerMask )
			fEvent |= Tool.ACTIVATED;
	}

	/**
	 * @see IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
	 */
	public void perspectiveChanged( IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) 
	{
		if( page == fPageMask && perspective == fPerMask )
			fEvent |= Tool.CHANGED;
	}
}