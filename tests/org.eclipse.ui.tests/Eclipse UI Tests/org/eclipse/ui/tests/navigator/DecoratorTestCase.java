package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.ui.internal.*;

/**
 * @version 	1.0
 */
public class DecoratorTestCase
	extends AbstractNavigatorTest
	implements ILabelProviderListener {

	private DecoratorDefinition definition;
	private boolean updated = false;

	/**
	 * Constructor for DecoratorTestCase.
	 * @param testName
	 */
	public DecoratorTestCase(String testName) {
		super(testName);
	}

	/**
	 * Sets up the hierarchy.
	 */
	protected void setUp() throws Exception {
		createTestFile();
		showNav();
		
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
		super.tearDown();
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

		assertTrue("Got an update", updated);
		updated = false;

	}

	/*
	 * @see ILabelProviderListener#labelProviderChanged(LabelProviderChangedEvent)
	 */
	public void labelProviderChanged(LabelProviderChangedEvent event) {
		updated = true;
	}

}