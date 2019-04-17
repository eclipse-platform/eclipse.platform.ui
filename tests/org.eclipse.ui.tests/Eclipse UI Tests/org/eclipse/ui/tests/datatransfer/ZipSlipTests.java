/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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
package org.eclipse.ui.tests.datatransfer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class ZipSlipTests extends UITestCase {

	public ZipSlipTests() {
		super(ZipSlipTests.class.getName());
	}

	public static final String ZIPSLIP_FILE = "data/zipSlip.zip"; //$NON-NLS-1$

	@Test
	public void testZipFileStructureProvider() throws ZipException, IOException {
		IPath path = getLocalPath(new Path(ZIPSLIP_FILE));
		try (ZipFile zipFile = new ZipFile(path.toFile())) {
			ZipFileStructureProvider zipfileStructureProvider = new ZipFileStructureProvider(zipFile);
			List<?> children = zipfileStructureProvider.getChildren(zipfileStructureProvider.getRoot());
			Assert.assertEquals(1, children.size());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				InputStream stream = zipfileStructureProvider.getContents(entry);
				String name = entry.getName();
				if (name.startsWith("../")) {
					Assert.assertNull(stream);
				} else {
					Assert.assertNotNull(stream);
				}
			}
		}
	}

	@Test
	public void testZipLeveledStructureProvider() throws ZipException, IOException {
		IPath path = getLocalPath(new Path(ZIPSLIP_FILE));
		try (ZipFile zipFile = new ZipFile(path.toFile())) {
			ZipLeveledStructureProvider zipLeveledStructureProvider = new ZipLeveledStructureProvider(zipFile);
			List<?> children = zipLeveledStructureProvider.getChildren(zipLeveledStructureProvider.getRoot());
			Assert.assertEquals(1, children.size());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				InputStream stream = zipLeveledStructureProvider.getContents(entry);
				String name = entry.getName();
				if (name.startsWith("../")) {
					Assert.assertNull(stream);
				} else {
					Assert.assertNotNull(stream);
				}
			}
		}
	}

	private IPath getLocalPath(IPath zipFilePath) {
		Bundle bundle = FrameworkUtil.getBundle(ZipSlipTests.class);
		URL url = FileLocator.find(bundle, zipFilePath, null);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Path(url.getPath());
	}
}
