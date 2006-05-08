/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.activities;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.tests.harness.util.ImageTests;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.1
 */
public class ImagesTest extends UITestCase {

	private Image defaultImage;
	private Image image1;
	private Image image2;

	/**
	 * @param testName
	 */
	public ImagesTest(String testName) {
		super(testName);
	}
	
	public void testActivityImages() {
		IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();
		IActivity activity1 = support.getActivityManager().getActivity("org.eclipse.activity1");
		assertNotNull(activity1);
		assertTrue(activity1.isDefined());
		IActivity activity2 = support.getActivityManager().getActivity("org.eclipse.activity2");
		assertNotNull(activity2);
		assertTrue(activity2.isDefined());
		
		ImageDescriptor defaultImageDesc = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY);		
		defaultImage = defaultImageDesc.createImage();
		
		ImageDescriptor desc1 = support.getImageDescriptor(activity1);
		image1 = desc1.createImage();
		
		assertNotSame(defaultImageDesc, desc1);
	    ImageTests.assertNotEquals(defaultImage, image1);
	    
		ImageDescriptor desc2 = support.getImageDescriptor(activity2);
		image2 = desc2.createImage();
		
		assertSame(defaultImageDesc, desc2);
		ImageTests.assertEquals(defaultImage, image2);
	}
	
	
	public void testCategoryImages() {
		IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();
		ICategory category1 = support.getActivityManager().getCategory("org.eclipse.category1");
		assertNotNull(category1);
		assertTrue(category1.isDefined());
		ICategory category2 = support.getActivityManager().getCategory("org.eclipse.category2");
		assertNotNull(category2);
		assertTrue(category2.isDefined());
		
		ImageDescriptor defaultImageDesc = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY_CATEGORY);		
		defaultImage = defaultImageDesc.createImage();
		
		ImageDescriptor desc1 = support.getImageDescriptor(category1);
		image1 = desc1.createImage();
		
		assertNotSame(defaultImageDesc, desc1);
	    ImageTests.assertNotEquals(defaultImage, image1);
	    
		ImageDescriptor desc2 = support.getImageDescriptor(category2);
		image2 = desc2.createImage();
		
		assertSame(defaultImageDesc, desc2);
		ImageTests.assertEquals(defaultImage, image2);
	}


	protected void doTearDown() throws Exception {
		super.doTearDown();
		if (defaultImage != null)
			defaultImage.dispose();
		if (image1 != null)
			image1.dispose();
		if (image2 != null)
			image2.dispose();
	}
}
