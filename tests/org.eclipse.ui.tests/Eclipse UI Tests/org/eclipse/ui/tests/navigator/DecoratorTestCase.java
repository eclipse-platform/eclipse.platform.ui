package org.eclipse.ui.tests.navigator;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.internal.DecoratorDefinition;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @version 	1.0
 */
public class DecoratorTestCase extends AbstractNavigatorTest {

	private DecoratorDefinition definition;
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

		DecoratorDefinition[] definitions =
			WorkbenchPlugin.getDefault().getDecoratorManager().getDecoratorDefinitions();
		for (int i = 0; i < definitions.length; i++) {
			if (definitions[i].getDecorator().equals(TestDecoratorContributor.contributor))
				definition = definitions[i];

		}
	}

	/**
	 * Test enabling the contributor
	 */
	public void testEnableDecorator() {
		definition.setEnabled(true);
		WorkbenchPlugin.getDefault().getDecoratorManager().reset();
	}

	/**
	 * Test disabling the contributor
	 */
	public void testDisableDecorator() {
		definition.setEnabled(false);
		WorkbenchPlugin.getDefault().getDecoratorManager().reset();
	}

	/**
	 * Refresh the test decorator.
	 */
	public void testRefreshContributor(IAction action) {

		TestDecoratorContributor.contributor.refreshListeners(testFile);

	}

}