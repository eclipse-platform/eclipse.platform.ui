package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.FileUtil;

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
		// Open view.
		MockViewPart part = (MockViewPart)fActivePage.showView( MockViewPart.ID );												
		assertNotNull(part);
		assert( part.getCallHistory().verifyOrder( new String[] {
				"init", "createPartControl", "setFocus"
			} ));
		assertEquals(Tool.arrayHas(fActivePage.getViews(), part), true);
		
		// Close view.
		fActivePage.hideView(part);
		assertNull(fActivePage.findView( MockViewPart.ID ));
		assertEquals(Tool.arrayHas(fActivePage.getViews(), part), false);
	}
	
	public void testOpenEditor() throws Throwable {
		// Create test file.
		IProject proj = FileUtil.createProject("testOpenEditor");
		IFile file = FileUtil.createFile("test.mock1", proj);
		
		// Open editor.
		IEditorPart editor = fActivePage.openEditor(file);
		assertNotNull(editor);
		assertEquals(Tool.arrayHas(fActivePage.getEditors(), editor), true);
		
		// Close editor.
		fActivePage.closeEditor(editor, false);
		assertEquals(Tool.arrayHas(fActivePage.getEditors(), editor), false);
		
		// Cleanup.
		FileUtil.deleteProject(proj);
	}
}