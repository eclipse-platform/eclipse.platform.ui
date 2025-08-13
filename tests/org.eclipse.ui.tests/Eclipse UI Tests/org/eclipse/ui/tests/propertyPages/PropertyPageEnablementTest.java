/*******************************************************************************
 * Copyright (c) 2006, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.propertyPages;

import java.util.Collection;

import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.2
 */
@RunWith(JUnit4.class)
public class PropertyPageEnablementTest extends AbstractNavigatorTest {

	/**
	 * Create an instance of the receiver.
	 */
	public PropertyPageEnablementTest() {
		super(PropertyPageEnablementTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
	}

	/**
	 * Test the AND condition property page which should only work for files.
	 */
	@Test
	public void testAndPage() {

		Collection<RegistryPageContributor> contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFile);
		assertFalse("Has no file pages", contributors.isEmpty());
		assertTrue("And property page for file not found",
				contributors.stream().anyMatch(element -> element.getPageId().equals("org.eclipse.ui.tests.and")));
	}

	/**
	 * Test the OR condition property page which should only work for files and
	 * folders.
	 */
	@Test
	public void testOrPage() {

		boolean found = false;
		Collection<RegistryPageContributor> contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFile);
		assertFalse("Has no file pages", contributors.isEmpty());
		for (RegistryPageContributor element : contributors) {
			if (element.getPageId().equals("org.eclipse.ui.tests.or")) {
				found = true;
			}
		}
		assertTrue("OR property page for file not found", found);

		found = false;
		contributors = PropertyPageContributorManager.getManager().getApplicableContributors(testFolder);
		assertFalse("Has no folder pages", contributors.isEmpty());
		for (RegistryPageContributor element : contributors) {
			if (element.getPageId().equals("org.eclipse.ui.tests.or")) {
				found = true;
			}
		}
		assertTrue("OR property page for file not found", found);

		contributors = PropertyPageContributorManager.getManager().getApplicableContributors(testProject);
		for (RegistryPageContributor element : contributors) {
			assertFalse("Matching project for OR", element.getPageId().equals("org.eclipse.ui.tests.or"));
		}

	}

	/**
	 * Test the instance of property page which should only work for projects.
	 */
	@Test
	public void testInstanceOfPage() {

		Collection<RegistryPageContributor> contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFile);
		for (RegistryPageContributor registryPageContributor : contributors) {
			RegistryPageContributor element = registryPageContributor;
			assertFalse("Matching file for instanceof", element.getPageId().equals("org.eclipse.ui.tests.instanceof"));
		}

		contributors = PropertyPageContributorManager.getManager().getApplicableContributors(testFolder);
		for (RegistryPageContributor element : contributors) {
			assertFalse("Matching folder for instanceof",
					element.getPageId().equals("org.eclipse.ui.tests.instanceof"));

		}

		boolean found = false;
		contributors = PropertyPageContributorManager.getManager().getApplicableContributors(testProject);
		assertFalse("Has no project pages", contributors.isEmpty());
		for (RegistryPageContributor element : contributors) {
			if (element.getPageId().equals("org.eclipse.ui.tests.instanceof")) {
				found = true;
			}
		}
		assertTrue("instanceof property page for project not found", found);

	}

}
