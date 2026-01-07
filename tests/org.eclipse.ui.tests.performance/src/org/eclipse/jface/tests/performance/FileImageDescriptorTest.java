/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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

package org.eclipse.jface.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * ComboViewerRefreshTest is a test of refreshes of difference size in the combo
 * viewer.
 */
public class FileImageDescriptorTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	protected static final String IMAGES_DIRECTORY = "/icons/imagetests";

	/**
	 * Test the time for doing a refresh.
	 */
	@Test
	public void testRefresh(TestInfo testInfo) throws Throwable {

		Performance perf = Performance.getDefault();
		String scenarioId = this.getClass().getName() + "." + testInfo.getDisplayName();
		PerformanceMeter meter = perf.createPerformanceMeter(scenarioId);

		try {
			exercise(() -> {
				Class<?> missing = null;
				ArrayList<Image> images = new ArrayList<>();

				Bundle bundle = FrameworkUtil.getBundle(getClass());
				Enumeration<String> bundleEntries = bundle.getEntryPaths(IMAGES_DIRECTORY);


				while (bundleEntries.hasMoreElements()) {
					ImageDescriptor descriptor;
					String localImagePath = bundleEntries.nextElement();

					if (localImagePath.indexOf('.') < 0)
						continue;

					URL[] files = FileLocator.findEntries(bundle, IPath.fromOSString(localImagePath));

					for (URL file : files) {
						meter.start();
						descriptor = ImageDescriptor.createFromFile(missing, FileLocator.toFileURL(file).getFile());

						for (int j = 0; j < 10; j++) {
							Image image = descriptor.createImage();
							images.add(image);
						}

						processEvents();
						meter.stop();

					}

				}


				Iterator<Image> imageIterator = images.iterator();
				while (imageIterator.hasNext()) {
					imageIterator.next().dispose();
				}
			}, 20, 100, JFacePerformanceSuite.MAX_TIME);

			meter.commit();
			perf.assertPerformance(meter);
		} finally {
			meter.dispose();
		}
	}
}
