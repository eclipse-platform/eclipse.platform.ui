package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class IWorkbenchPageTest extends AbstractTestCase{
	
	private IWorkbenchPage fActivePage;
	private IWorkbenchWindow fWin;
	private IWorkbenchPart partMask;
	
	public IWorkbenchPageTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{		
		fWin = openTestWindow();
		fActivePage = fWin.getActivePage();
	}
	
/* 
	This method tests BOTH setEditorAreaVisible() and isEditorAreaVisible()
	as they are closely related to each other.
*/
	public void testSetEditorAreaVisible() throws Throwable
	{
		fActivePage.setEditorAreaVisible( true );		
		assert( fActivePage.isEditorAreaVisible() == true );
		
		fActivePage.setEditorAreaVisible( false );		
		assert( fActivePage.isEditorAreaVisible() == false );
	}

	public void testGetPerspective() throws Throwable
	{
		assertNotNull( fActivePage.getPerspective() );
		
		IWorkbenchPage page = fWin.openPage( EmptyPerspective.PERSP_ID, ResourcesPlugin.getWorkspace() );
		assertEquals( EmptyPerspective.PERSP_ID, page.getPerspective().getId() );		
	}
	
	public void testSetPerspective() throws Throwable
	{
		IPerspectiveDescriptor per = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId( EmptyPerspective.PERSP_ID);		
		fActivePage.setPerspective( per );
		assertEquals( per, fActivePage.getPerspective() );
	}
	
	public void testGetLabel()
	{
		assertNotNull( fActivePage.getLabel() );
	}

	public void testGetInput() throws Throwable
	{		
		IAdaptable input = ResourcesPlugin.getWorkspace();
		IWorkbenchPage page = fWin.openPage( input );
		assertEquals( input, page.getInput() );
	}
	
	public void testActivate() throws Throwable
	{	
		MockViewPart part = (MockViewPart)fActivePage.showView( MockViewPart.ID );												
		MockViewPart part2 = (MockViewPart)fActivePage.showView( MockViewPart.ID2 );												
		
		MockPartListener listener = new MockPartListener();
		CallHistory callTrace;		
		fActivePage.addPartListener( listener );
		fActivePage.activate( part );		

		listener.setPartMask( part2 );
		callTrace = part2.getCallHistory();				
		callTrace.clear();		
		fActivePage.activate( part2 );		
		assert( callTrace.contains( "setFocus" ) );
		assert( listener.getCallHistory().contains( "partActivated" ) );
		
		listener.setPartMask( part );
		callTrace = part.getCallHistory();				
		callTrace.clear();		
		fActivePage.activate( part );		
		assert( callTrace.contains( "setFocus" ) );
		assert( listener.getCallHistory().contains( "partActivated" ) );
	}
	
	public void testGetWorkbenchWindow() 
	{
		assertEquals( fActivePage.getWorkbenchWindow(), fWin );
		IWorkbenchPage page = openTestPage( fWin );
		assertEquals( page.getWorkbenchWindow(), fWin );
	}
	
	public void testShowView() throws Throwable
	{		
		MockViewPart part = (MockViewPart)fActivePage.showView( MockViewPart.ID );												

		CallHistory callTrace = part.getCallHistory();
		assert( callTrace.verifyOrder( new String[] {
				"init", "createPartControl", "setFocus"
			} ));
	}
}