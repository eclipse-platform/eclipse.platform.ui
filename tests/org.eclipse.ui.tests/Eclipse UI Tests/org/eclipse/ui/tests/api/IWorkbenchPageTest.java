package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class IWorkbenchPageTest extends AbstractTestCase {
	
	private IWorkbenchPage fActivePage;
	private IWorkbenchWindow fWin;
	
	public IWorkbenchPageTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{		
		try{
			fWin = openTestWindow();
		}
		catch( WorkbenchException e )
		{
			fail();
		}
		
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
		assertEquals( EmptyPerspective.PERSP_ID, page.getPerspective() );		
		
		page.close();
		assertNull( page.getPerspective() );
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
		IWorkbenchPage page = fWin.openPage( null );		
		assertNull( page.getInput() );
		page.close();
		
		IAdaptable input = ResourcesPlugin.getWorkspace();
		page = fWin.openPage( input );
		assertEquals( input, page.getInput() );
	}
	
	public void testActivate()
	{
		MockViewPart part = new MockViewPart();
		//part.addPropertyListener( this );
		fActivePage.activate( part );
		
		assertEquals( part.setFocusCalled, true );
//		assert( part. );
	}
}

