package org.eclipse.ui.tests.adaptable;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.DecoratorDefinition;
import org.eclipse.ui.internal.DecoratorManager;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.navigator.TestDecoratorContributor;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * @version 	1.0
 */
public class AdaptableDecoratorTestCase
	extends UITestCase
	implements ILabelProviderListener {

	private DecoratorDefinition definition;
	private AdaptedResourceNavigator adaptedNavigator;
	private boolean updated = false;
	public String ADAPTED_NAVIGATOR_ID =
		"org.eclipse.ui.tests.adaptable.adaptedHierarchy";
	protected IProject testProject;
	protected IFolder testFolder;
	protected IFile testFile;

	/**
	 * Constructor for DecoratorTestCase.
	 * @param testName
	 */
	public AdaptableDecoratorTestCase(String testName) {
		super(testName);
	}

	/**
	 * Sets up the hierarchy.
	 */
	protected void setUp() throws Exception {
		createTestFile();
		showAdaptedNav();

		WorkbenchPlugin.getDefault().getDecoratorManager().addListener(this);

		DecoratorDefinition[] definitions =
			WorkbenchPlugin.getDefault().getDecoratorManager().getDecoratorDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].getId().equals("org.eclipse.ui.tests.adaptable.decorator"))
				definition = definitions[i];
		}
	}

	private DecoratorManager getDecoratorManager() {
		return WorkbenchPlugin.getDefault().getDecoratorManager();
	}

	/**
	 * Remove the listener.
	 */

	public void tearDown() throws Exception {

		if (testProject != null) {
			try {
				testProject.delete(true, null);
			} catch (CoreException e) {
				fail(e.toString());
			}
			testProject = null;
			testFolder = null;
			testFile = null;
		}
		super.tearDown();
		adaptedNavigator = null;

		getDecoratorManager().removeListener(this);
	}

	/**
	 * Make a label changed event for resource.
	 */
	private LabelProviderChangedEvent getLabelChangedEvent(IResource resource) {
		return new LabelProviderChangedEvent(getDecoratorManager(), resource);
	}

	/**
	 * Test enabling the contributor
	 */
	public void testEnableDecorator() throws CoreException{
		definition.setEnabled(true);
		getDecoratorManager().reset();

	}

	/**
	 * Test disabling the contributor
	 */
	public void testDisableDecorator() throws CoreException{
		definition.setEnabled(false);
		getDecoratorManager().reset();
	}

	/**
	 * Refresh the test decorator.
	 */
	public void testRefreshContributor() throws CoreException{

		updated = false;
		definition.setEnabled(true);
		getDecoratorManager().reset();
		TestDecoratorContributor.contributor.refreshListeners(testFile);
		assertTrue("Got an update", updated);
		updated = false;

	}

	/*
	 * @see ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 */
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		updated = true;
	}

	/** 
	 * Shows the Adapted Resource Navigator in a new test window. 
	 */
	protected void showAdaptedNav() throws PartInitException {
		IWorkbenchWindow window = openTestWindow();
		adaptedNavigator =
			(AdaptedResourceNavigator) window.getActivePage().showView(
				ADAPTED_NAVIGATOR_ID);
	}

	protected void createTestProject() throws CoreException {
		if (testProject == null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			testProject = workspace.getRoot().getProject("AdaptedTestProject");
			testProject.create(null);
			testProject.open(null);
		}
	}

	protected void createTestFolder() throws CoreException {
		if (testFolder == null) {
			createTestProject();
			testFolder = testProject.getFolder("AdaptedTestFolder");
			testFolder.create(false, false, null);
		}
	}

	protected void createTestFile() throws CoreException {
		if (testFile == null) {
			createTestFolder();
			testFile = testFolder.getFile("AdaptedFoo.txt");
			testFile.create(
				new ByteArrayInputStream("Some content.".getBytes()),
				false,
				null);
		}
	}

}