/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.propertyPages;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.RegistryPageContributor;
import org.eclipse.ui.tests.navigator.AbstractNavigatorTest;

/**
 * @since 3.2
 *
 */
public class PropertyPageEnablementTest extends AbstractNavigatorTest {

	/**
	 * Create an instance of the receiver.
	 *
	 * @param testName
	 */
	public PropertyPageEnablementTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		createTestFile();
	}

	/**
	 * Test the AND condition property page which should only work for files.
	 *
	 */
	public void testAndPage() {

		Collection contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFile);
		assertFalse("Has no file pages", contributors.isEmpty());
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			if (element.getPageId().equals("org.eclipse.ui.tests.and")) {
				return;
			}
		}
		assertTrue("And property page for file not found", false);

		contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFolder);
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			assertFalse("Matching folder for AND", element.getPageId().equals(
					"org.eclipse.ui.tests.and"));

		}

		contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testProject);
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			assertFalse("Matching project for AND", element.getPageId().equals(
					"org.eclipse.ui.tests.and"));

		}

	}

	/**
	 * Test the OR condition property page which should only work for files and
	 * folders.
	 *
	 */
	public void testOrPage() {

		boolean found = false;
		Collection contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFile);
		assertFalse("Has no file pages", contributors.isEmpty());
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			if (element.getPageId().equals("org.eclipse.ui.tests.or")) {
				found = true;
			}
		}
		assertTrue("OR property page for file not found", found);

		found = false;
		contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFolder);
		assertFalse("Has no folder pages", contributors.isEmpty());
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			if (element.getPageId().equals("org.eclipse.ui.tests.or")) {
				found = true;
			}
		}
		assertTrue("OR property page for file not found", found);

		contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testProject);
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			assertFalse("Matching project for OR", element.getPageId().equals(
					"org.eclipse.ui.tests.or"));

		}

	}

	/**
	 * Test the instance of property page which should only work for projects.
	 *
	 */
	public void testInstanceOfPage() {

		Collection contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFile);
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			assertFalse("Matching file for instanceof", element.getPageId()
					.equals("org.eclipse.ui.tests.instanceof"));
		}

		contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testFolder);
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			assertFalse("Matching folder for instanceof", element.getPageId()
					.equals("org.eclipse.ui.tests.instanceof"));

		}

		boolean found = false;
		contributors = PropertyPageContributorManager.getManager()
				.getApplicableContributors(testProject);
		assertFalse("Has no project pages", contributors.isEmpty());
		for (Iterator iter = contributors.iterator(); iter.hasNext();) {
			RegistryPageContributor element = (RegistryPageContributor) iter
					.next();
			if (element.getPageId().equals("org.eclipse.ui.tests.instanceof")) {
				found = true;
			}
		}
		assertTrue("instanceof property page for project not found", found);

	}

}
