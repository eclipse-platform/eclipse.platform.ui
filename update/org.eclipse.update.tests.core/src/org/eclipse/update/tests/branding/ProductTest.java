/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests.branding;

import org.eclipse.core.runtime.*;
import org.eclipse.update.internal.configurator.branding.*;
import org.eclipse.update.tests.*;

public class ProductTest extends UpdateManagerTestCase {

	public ProductTest(String testcase){
		super(testcase);
	}

	public void testMain() throws Exception {
		
		IProduct product = Platform.getProduct();
		if (product == null)
			System.out.println("No product defined");
		else
			System.out.println("Product is: \n" +
					"name=" + product.getName() + "\n" +
					"application=" + product.getApplication() + "\n" +
					"description=" + product.getDescription() + "\n" +
					"id="+ product.getId() + "\n" +
					"about_text:"+product.getProperty(IProductConstants.ABOUT_TEXT) +"\n" +
					"about_image:"+product.getProperty(IProductConstants.ABOUT_IMAGE) +"\n" +
					"app_name:"+product.getProperty(IProductConstants.APP_NAME) +"\n" +
					"window_image:"+product.getProperty(IProductConstants.WINDOW_IMAGE) +"\n" +
					"window_images:"+product.getProperty(IProductConstants.WINDOW_IMAGES) );
				
	}
}
