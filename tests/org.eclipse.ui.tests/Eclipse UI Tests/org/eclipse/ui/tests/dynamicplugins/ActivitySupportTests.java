/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.dynamicplugins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.tests.harness.util.ImageTests;
import org.junit.Ignore;
import org.junit.Test;

public class ActivitySupportTests extends DynamicTestCase {

	@Override
	protected String getExtensionId() {
		return "newActivitySupport1.testNewActivitySupportAddition";
	}

	@Override
	protected String getExtensionPoint() {
		return IWorkbenchRegistryConstants.PL_ACTIVITYSUPPORT;
	}

	@Override
	protected String getInstallLocation() {
		return "data/org.eclipse.newActivitySupport1";
	}

	@Test
	@Ignore("Bug 405296")
	public void testActivityImages() {
		IActivity baselineActivity = getWorkbench().getActivitySupport()
				.getActivityManager().getActivity("someBogusActivityId");
		assertNotNull(baselineActivity);
		assertFalse(baselineActivity.isDefined());

		IActivity activityWithIcon = getWorkbench().getActivitySupport()
				.getActivityManager().getActivity("org.eclipse.activity2");
		assertNotNull(activityWithIcon);
		assertTrue(activityWithIcon.isDefined());

		ImageDescriptor baselineDescriptor = getWorkbench()
				.getActivitySupport().getImageDescriptor(baselineActivity);
		ImageDescriptor customDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(activityWithIcon);

		Image baselineImage = null, customImage = null;
		try {
			baselineImage = baselineDescriptor.createImage();
			assertNotNull(baselineImage);
			customImage = customDescriptor.createImage();
			assertNotNull(customImage);

			// ensure that the images are the same before loading the plugin
			ImageTests.assertEquals(baselineImage, customImage);
		} finally {
			if (baselineImage != null) {
				baselineImage.dispose();
			}
			if (customImage != null) {
				customImage.dispose();
			}
		}

		getBundle();

		baselineDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(baselineActivity);
		customDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(activityWithIcon);

		try {
			baselineImage = baselineDescriptor.createImage();
			assertNotNull(baselineImage);
			customImage = customDescriptor.createImage();
			assertNotNull(customImage);

			// ensure that the images are differnt after loading the plugin
			ImageTests.assertNotEquals(baselineImage, customImage);
		} finally {
			if (baselineImage != null) {
				baselineImage.dispose();
			}
			if (customImage != null) {
				customImage.dispose();
			}
		}

		removeBundle();

		baselineDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(baselineActivity);
		customDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(activityWithIcon);

		try {
			baselineImage = baselineDescriptor.createImage();
			assertNotNull(baselineImage);
			customImage = customDescriptor.createImage();
			assertNotNull(customImage);

			// ensure that the images are the same after unloading the plugin
			ImageTests.assertEquals(baselineImage, customImage);
		} finally {
			if (baselineImage != null) {
				baselineImage.dispose();
			}
			if (customImage != null) {
				customImage.dispose();
			}
		}

	}


	@Test
	public void testCategoryImages() {
		ICategory baselineCategory = getWorkbench().getActivitySupport()
				.getActivityManager().getCategory("someBogusCategoryId");
		assertNotNull(baselineCategory);
		assertFalse(baselineCategory.isDefined());

		ICategory categoryWithIcon = getWorkbench().getActivitySupport()
				.getActivityManager().getCategory("org.eclipse.category2");
		assertNotNull(categoryWithIcon);
		assertTrue(categoryWithIcon.isDefined());

		ImageDescriptor baselineDescriptor = getWorkbench()
				.getActivitySupport().getImageDescriptor(baselineCategory);
		ImageDescriptor customDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(categoryWithIcon);

		Image baselineImage = null, customImage = null;
		try {
			baselineImage = baselineDescriptor.createImage();
			assertNotNull(baselineImage);
			customImage = customDescriptor.createImage();
			assertNotNull(customImage);

			// ensure that the images are the same before loading the plugin
			ImageTests.assertEquals(baselineImage, customImage);
		} finally {
			if (baselineImage != null) {
				baselineImage.dispose();
			}
			if (customImage != null) {
				customImage.dispose();
			}
		}

		getBundle();

		baselineDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(baselineCategory);
		customDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(categoryWithIcon);

		try {
			baselineImage = baselineDescriptor.createImage();
			assertNotNull(baselineImage);
			customImage = customDescriptor.createImage();
			assertNotNull(customImage);

			// ensure that the images are differnt after loading the plugin
			ImageTests.assertNotEquals(baselineImage, customImage);
		} finally {
			if (baselineImage != null) {
				baselineImage.dispose();
			}
			if (customImage != null) {
				customImage.dispose();
			}
		}

		removeBundle();

		baselineDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(baselineCategory);
		customDescriptor = getWorkbench().getActivitySupport()
				.getImageDescriptor(categoryWithIcon);

		try {
			baselineImage = baselineDescriptor.createImage();
			assertNotNull(baselineImage);
			customImage = customDescriptor.createImage();
			assertNotNull(customImage);

			// ensure that the images are the same after unloading the plugin
			ImageTests.assertEquals(baselineImage, customImage);
		} finally {
			if (baselineImage != null) {
				baselineImage.dispose();
			}
			if (customImage != null) {
				customImage.dispose();
			}
		}

	}
}
