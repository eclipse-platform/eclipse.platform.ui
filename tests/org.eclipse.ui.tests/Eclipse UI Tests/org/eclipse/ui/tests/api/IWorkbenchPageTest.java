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
		IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
		fWin = wins[ 0 ];
		fPage = fWin.getActivePage();
	}
	
	public void testSetPerspective()
	{
		IPerspectiveDescriptor per = ( IPerspectiveDescriptor )Man.pick(
			PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives()
		);
		fPage.setPerspective( per );
		
		assertEquals( per, fPage.getPerspective() );
	}

	public void testGetPerspective()
	{
		assertNotNull( fPage.getPerspective() );
	}
	
	public void testGetLabel()
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

