/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.BucketIndex;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tests.resources.ResourceTest;

public class BucketIndexTest extends ResourceTest {

	public static Test suite() {
		return new TestSuite(BucketIndexTest.class);
	}

	public BucketIndexTest(String name) {
		super(name);
	}

	public void testDuplicates() {
		IPath baseLocation = getRandomLocation();
		try {
			BucketIndex index1 = new BucketIndex(baseLocation.toFile());
			IPath location1 = baseLocation.append("location1");
			try {
				index1.load(location1.toFile());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			IPath path = new Path("/foo/bar");
			UniversalUniqueIdentifier uuid = new UniversalUniqueIdentifier();
			long lastModified = (long) (Math.random() * Long.MAX_VALUE);
			index1.addBlob(path, uuid, lastModified);
			assertEquals("2.0", 1, index1.getEntryCount());
			index1.addBlob(path, uuid, lastModified);
			assertEquals("3.0", 1, index1.getEntryCount());
			BucketIndex.Entry entry = index1.getEntry(path);
			assertNotNull("3.1", entry);
			assertEquals("3.2", path, entry.getPath());
			assertEquals("3.3", 1, entry.getOccurrences());
			assertEquals("3.4", uuid, entry.getUUID(0));
			assertEquals("3.5", lastModified, entry.getTimestamp(0));
		} finally {
			ensureDoesNotExistInFileSystem(baseLocation.toFile());
		}
	}

	public void testPersistence() {
		IPath baseLocation = getRandomLocation();
		try {
			BucketIndex index1 = new BucketIndex(baseLocation.toFile());
			IPath location = baseLocation.append("location");
			try {
				index1.load(location.toFile());
			} catch (CoreException e) {
				fail("1.0", e);
			}
			assertEquals("1.1", 0, index1.getEntryCount());
			IPath path = new Path("/foo/bar");
			UniversalUniqueIdentifier uuid1 = new UniversalUniqueIdentifier();
			long lastModified1 = (long) (Math.random() * Long.MAX_VALUE);
			index1.addBlob(path, uuid1, lastModified1);
			assertEquals("2.0", 1, index1.getEntryCount());
			try {
				index1.save();
			} catch (CoreException e) {
				fail("2.1", e);
			}
			BucketIndex index2 = new BucketIndex(baseLocation.toFile());
			try {
				index2.load(location.toFile(), false);
			} catch (CoreException e) {
				fail("3.0", e);
			}
			assertEquals("3.1", 1, index1.getEntryCount());
			BucketIndex.Entry entry = index1.getEntry(path);
			assertNotNull("3.2", entry);
			assertEquals("3.3", path, entry.getPath());
			assertEquals("3.4", 1, entry.getOccurrences());
			assertEquals("3.5", uuid1, entry.getUUID(0));
			assertEquals("3.6", lastModified1, entry.getTimestamp(0));
			UniversalUniqueIdentifier uuid2 = new UniversalUniqueIdentifier();
			long lastModified2 = (long) Math.abs((Math.random() * Long.MAX_VALUE));
			index2.addBlob(path, uuid2, lastModified2);
			try {
				index2.save();
			} catch (CoreException e) {
				fail("4.0", e);
			}
			try {
				index1.load(location.toFile(), true);
			} catch (CoreException e) {
				fail("4.1", e);
			}
			assertEquals("4.2", 1, index1.getEntryCount());
			entry = index1.getEntry(path);
			assertNotNull("4.3", entry);
			assertEquals("4.4", path, entry.getPath());
			assertEquals("4.5", 2, entry.getOccurrences());
		} finally {
			ensureDoesNotExistInFileSystem(baseLocation.toFile());
		}
	}

}
