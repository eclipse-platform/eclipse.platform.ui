package org.eclipse.ui.tests.api;


import junit.framework.TestCase;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the IWorkbench interface.
 */
public class IWorkbenchTest extends TestCase {

	private IWorkbench fBench;
	
	public IWorkbenchTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		fBench = PlatformUI.getWorkbench();
	}
	
	public void testGetActiveWorkbenchWindow() throws Throwable {		
		IWorkbenchWindow awin = fBench.getActiveWorkbenchWindow();
		assertNotNull(awin);

/*		IWorkbenchWindow[] wins = fBench.getWorkbenchWindows();		
		if( wins.length > 1 )
			for( int i = 0; i < wins.length; i ++ )
				if( wins[ i ] != awin ){
					System.out.println( "whoa whoa, there is more than 
		Shell sh = win.getShell();		
//		win = fBench.getActiveWorkbenchWindow();
//		assertNull(win);*/
	}

	public void testGetEditorRegistry() throws Throwable
	{
		IEditorRegistry reg = fBench.getEditorRegistry();
		assertNotNull( reg );
	}
	
	public void testGetPerspectiveRegistry() throws Throwable
	{
		IPerspectiveRegistry reg = fBench.getPerspectiveRegistry();
		assertNotNull( reg );	
	}
	
	public void testGetPrefereneManager() throws Throwable {
		PreferenceManager mgr = fBench.getPreferenceManager();
		assertNotNull(mgr);
	} 

	public void testGetSharedImages() throws Throwable
	{
		ISharedImages img = fBench.getSharedImages();
		assertNotNull( img );
	} 
	
	public void testGetWorkbenchWindows() throws Throwable
	{
		IWorkbenchWindow[] wins = fBench.getWorkbenchWindows();

		assertNotNull( wins );		
		for( int i = 0; i < wins.length; i ++ )
			assertNotNull( wins[ i ] );
	}
	
	public void testOpenWorkbenchWindow_String() throws Throwable
	{
		IPerspectiveRegistry reg = fBench.getPerspectiveRegistry();
		IPerspectiveDescriptor per = (IPerspectiveDescriptor)Man.pick( reg.getPerspectives() );
		IWorkbenchWindow win = fBench.openWorkbenchWindow( per.getId(), null );

		assertNotNull( win );
				
		assertEquals( win, fBench.getActiveWorkbenchWindow() );
		assertEquals( per, win.getActivePage().getPerspective() );

		win.close();
	}

	public void testOpenWorkbenchWindow() throws Throwable
	{
 		IWorkbenchWindow win = fBench.openWorkbenchWindow( null );
		
		assertNotNull( win );
		assertEquals( win, fBench.getActiveWorkbenchWindow() );
		assertNotNull( win.getActivePage() );
		
		win.close();
	}
	
	public void testClose()
	{
/* 
	close() couldn't be tested because close() needs to be called after all other
	test methods are done, but we can't specifiy the order in which the test
	methods are invoked. testClose() is defined as a place holder.
*/
	//	fBench.close();		
	}
}