package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class IWorkbenchWindowTest extends TestCase {

	private IWorkbenchWindow fWin;
	
	public IWorkbenchWindowTest( String testName )
	{
		super( testName );
	}

	protected void setUp() 
	{
		IWorkbenchWindow[] wins = PlatformUI.getWorkbench().getWorkbenchWindows();
		fWin = wins[ 0 ];
	}	

	public void testClose() throws Throwable
	{
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.openWorkbenchWindow( null );
		assert( win.close() == true );
	}
	
/*	
	public void test throws Throwable
	{
		assertNotNull(  );	
	}
*/

	public void testGetActivePage() throws Throwable
	{
		IWorkbenchPage page = fWin.getActivePage();
		assertNotNull( page );	
		
//		some magic		
//		page = fWin.getActivePage();
//		assertNull( page );	
	}

	public void testSetActivePage() throws Throwable
	{
		IWorkbenchPage page = ( IWorkbenchPage )Man.pick( fWin.getPages() );
		
		fWin.setActivePage( page );
		assertEquals( page, fWin.getActivePage() );
	}
	
	public void testGetPages() throws Throwable
	{
		IWorkbenchPage[] pages = fWin.getPages();
		assertNotNull( pages );
		
		for( int i = 0; i < pages.length; i ++ )
			assertNotNull( pages[ i ] );				
	}
	
	public void testGetShell() throws Throwable
	{
		Shell sh = fWin.getShell();
		assertNotNull( sh );	
	}
	
	public void testGetWorkbench() throws Throwable
	{
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow[] wins = wb.getWorkbenchWindows();
		
		IWorkbenchWindow win = wins[ 0 ];
				
		assertEquals( win.getWorkbench(), wb );			
	}
	
	public void testOpenPage() throws Throwable
	{		
		IWorkbenchPage page = fWin.openPage( null );
		
		assertNotNull( page );
		assertEquals( fWin.getActivePage(), page );		
/*
		some magic
		assertNull( page );
*/
	}
	
	public void openPage_String() throws Throwable
	{
		IPerspectiveDescriptor pers = ( IPerspectiveDescriptor )Man.pick( 		
			fWin.getWorkbench().getPerspectiveRegistry().getPerspectives() 
		);

		IWorkbenchPage page = fWin.openPage( pers.getId(), null );

		assertNotNull( page );
		assertEquals( fWin.getActivePage(), page );		
	}
	
	public void testIsApplicationMenu() throws Throwable
	{
		assert( fWin.isApplicationMenu( Man.FakeID ) == false );
/*
		somemagic
		assert( fWin.isApplicationMenu( goodID ) == true );
*/
	}
}

