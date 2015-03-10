/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;
import org.eclipse.ui.tests.performance.TestRunnable;
import org.eclipse.ui.tests.performance.UIPerformancePlugin;
import org.osgi.framework.Bundle;

/**
 * ComboViewerRefreshTest is a test of refreshes of difference size in the combo
 * viewer.
 */
public class FileImageDescriptorTest extends BasicPerformanceTest {

	protected static final String IMAGES_DIRECTORY = "/icons/imagetests";

	public FileImageDescriptorTest(String testName, int tagging) {
		super(testName, tagging);

	}

	public FileImageDescriptorTest(String testName) {
		super(testName);

	}

	/**
	 * Test the time for doing a refresh.
	 *
	 * @throws Throwable
	 */
	public void testRefresh() throws Throwable {

		exercise(new TestRunnable() {
			public void run() {
				Class missing = null;
				ArrayList images = new ArrayList();

				Bundle bundle = UIPerformancePlugin.getDefault().getBundle();
				Enumeration bundleEntries = bundle
						.getEntryPaths(IMAGES_DIRECTORY);


				while (bundleEntries.hasMoreElements()) {
					ImageDescriptor descriptor;
					String localImagePath = (String) bundleEntries
							.nextElement();

					if(localImagePath.indexOf('.') < 0)
						continue;

					URL[] files = FileLocator.findEntries(bundle, new Path(
							localImagePath));

					for (int i = 0; i < files.length; i++) {

						startMeasuring();

						try {
							descriptor = ImageDescriptor.createFromFile(missing,
									FileLocator.toFileURL(files[i]).getFile());
						} catch (IOException e) {
							fail(e.getLocalizedMessage(),e);
							continue;
						}

						for (int j = 0; j < 10; j++) {
							Image image = descriptor.createImage();
							images.add(image);
						}

						processEvents();
						stopMeasuring();

					}

				}


				Iterator imageIterator = images.iterator();
				while (imageIterator.hasNext()) {
					((Image) imageIterator.next()).dispose();
				}
			}
		}, 20, 100, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}
}
