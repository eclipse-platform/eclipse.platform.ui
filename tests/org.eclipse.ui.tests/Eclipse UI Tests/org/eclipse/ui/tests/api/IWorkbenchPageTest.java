package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.ui.*;

public class IWorkbenchPageTest extends TestCase {
	
	private IWorkbenchPage fPage;
	private IWorkbenchWindow fWin;
	
	public IWorkbenchPageTest( String testName )
	{
		super( testName );
	}
	
	public void setUp()
	{		
		fWin = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		fPage = fWin.getActivePage();
	}
	
	public void testSetPerspective()
	{
		IPerspectiveDescriptor per = ( IPerspectiveDescriptor )Tool.pick(
			PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives()
		);
		fPage.setPerspective( per );
		
		assertEquals( per, fPage.getPerspective() );
	}
	
/* 
	This method tests BOTH setEditorAreaVisible() and isEditorAreaVisible()
	as they are closely related to each other.
*/
	public void testSetEditorAreaVisible() throws Throwable
	{
		fPage.setEditorAreaVisible( true );		
		assert( fPage.isEditorAreaVisible() == true );
		
		fPage.setEditorAreaVisible( false );		
		assert( fPage.isEditorAreaVisible() == false );
	}

//	public 

	public void testGetPerspective() throws Throwable
	{
		assertNotNull( fPage.getPerspective() );
	}
	
	public void testGetLabel() throws Throwable
	{
		assertNotNull( fPage.getLabel() );
	}

	public void testGetViews()
	{
/*		IViewPart[] parts = fPage.getViews();		
		f
		
		for( int i = 0; i < parts.length; i ++ ){
			
		
		assertNotNull( fPage.getLabel() );*/
	}
}

