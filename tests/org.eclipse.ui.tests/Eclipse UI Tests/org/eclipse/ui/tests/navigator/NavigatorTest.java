package org.eclipse.ui.tests.navigator;

import java.io.ByteArrayInputStream;
import java.io.StringBufferInputStream;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.UITestCase;
import org.eclipse.ui.views.navigator.ResourceNavigator;

/**
 * Tests the Resource Navigator view.
 */
public class NavigatorTest extends UITestCase {

	private static final String NAVIGATOR_VIEW_ID = "org.eclipse.ui.views.ResourceNavigator";
	
	private IProject testProject;
	private IFolder testFolder;
	private IFile testFile;
	private ResourceNavigator navigator;
	
	public NavigatorTest(String testName) {
		super(testName);
	}

	private void createTestProject() throws CoreException {
		if (testProject == null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			testProject = workspace.getRoot().getProject("TestProject");
			testProject.create(null);
			testProject.open(null);
		}
	}
	
	private void createTestFolder() throws CoreException {
		if (testFolder == null) {
			createTestProject();
			testFolder = testProject.getFolder("TestFolder");
			testFolder.create(false, false, null);
		}
	}

	private void createTestFile() throws CoreException {
		if (testFile == null) {
			createTestFolder();
			testFile = testFolder.getFile("Foo.txt");
			testFile.create(new ByteArrayInputStream("Some content.".getBytes()), false, null);
		}
	}

	/** Shows the Navigator in a new test window. */
	private void showNav() throws PartInitException {
		IWorkbenchWindow window = openTestWindow();
		navigator = (ResourceNavigator) window.getActivePage().showView(NAVIGATOR_VIEW_ID);
	}
	
	public void tearDown() {
		if (testProject != null) {
			try {
				testProject.delete(true, null);
			}
			catch (CoreException e) {
				fail(e.toString());
			}
			testProject = null;
			testFolder = null;
			testFile = null;
		}
		super.tearDown();
		navigator = null;
	}
	
	/**
	 * Tests that the Navigator is initially populated with
	 * the correct elements from the workspace.
	 */
	public void testInitialPopulation() throws CoreException, PartInitException {
		createTestFile();
		showNav();

		// test its initial content by setting and getting selection on the file
		ISelectionProvider selProv = navigator.getSite().getSelectionProvider();
		StructuredSelection sel = new StructuredSelection(testFile);
		selProv.setSelection(sel);
		assertEquals(sel, selProv.getSelection());
	}

	/**
	 * Tests that the Navigator updates properly when a file is added to the workbench.
	 */
	public void testFileAddition() throws CoreException, PartInitException {
		createTestFolder(); // create the project and folder before the Navigator is shown
		showNav();
		createTestFile();  // create the file after the Navigator is shown
		
		// test its initial content by setting and getting selection on the file
		ISelectionProvider selProv = navigator.getSite().getSelectionProvider();
		StructuredSelection sel = new StructuredSelection(testFile);
		selProv.setSelection(sel);
		assertEquals(sel, selProv.getSelection());
	}

}
