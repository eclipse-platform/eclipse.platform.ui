/*******************************************************************************
 * Copyright (c) 2026 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.internal.WorkspaceLock;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link org.eclipse.ui.internal.WorkspaceLock}
 */
public class WorkspaceLockTest {

	Path tempDir = FileSystemHelper.getRandomLocation().toPath().resolve("WorkspaceLockTest");

	/**
	 * Test method for {@link org.eclipse.ui.internal.WorkspaceLock#getWorkspaceLockDetails(java.net.URL)}.
	 */
	@Test
	public void testGetWorkspaceLockDetails() throws Exception {
		URL workspaceUrl = tempDir.toUri().toURL();

		// Test when no lock info file exists
		String details = WorkspaceLock.getWorkspaceLockDetails(workspaceUrl);
		assertNull(details, "Should return null when no lock info file exists");

		// Create .metadata/.lock_info with properties
		Path metadataDir = tempDir.resolve(".metadata");
		Files.createDirectories(metadataDir);
		Path lockInfoFile = metadataDir.resolve(".lock_info");

		Properties props = new Properties();
		props.setProperty(WorkspaceLock.USER, "testuser");
		props.setProperty(WorkspaceLock.HOST, "testhost");
		props.setProperty(WorkspaceLock.DISPLAY, ":0");
		props.setProperty(WorkspaceLock.PROCESS_ID, "1234");
		try (var os = Files.newOutputStream(lockInfoFile)) {
			props.store(os, null);
		}

		details = WorkspaceLock.getWorkspaceLockDetails(workspaceUrl);
		assertNotNull(details, "Should return details when lock info file exists");
		assertTrue(details.contains("testuser"), "Should contain user info");
		assertTrue(details.contains("testhost"), "Should contain host info");
		assertTrue(details.contains(":0"), "Should contain display info");
		assertTrue(details.contains("1234"), "Should contain PID info");
	}

	/**
	 * Test method for {@link org.eclipse.ui.internal.WorkspaceLock#getLockInfoFile(java.net.URL)}.
	 */
	@Test
	public void testGetLockInfoFile() throws Exception {
		URL workspaceUrl = tempDir.toUri().toURL();
		Path lockInfoFile = WorkspaceLock.getLockInfoFile(workspaceUrl);
		assertNotNull(lockInfoFile, "Should return a path");
		assertEquals(tempDir.resolve(".metadata").resolve(".lock_info"), lockInfoFile,
				"Should point to .metadata/.lock_info");
	}

	/**
	 * Test method for {@link org.eclipse.ui.internal.WorkspaceLock#getLockFile(java.net.URL)}.
	 */
	@Test
	public void testGetLockFile() throws Exception {
		URL workspaceUrl = tempDir.toUri().toURL();
		Path lockFile = WorkspaceLock.getLockFile(workspaceUrl);
		assertNotNull(lockFile, "Should return a path");
		assertEquals(tempDir.resolve(".metadata").resolve(".lock"), lockFile, "Should point to .metadata/.lock");
	}

	/**
	 * Test method for {@link org.eclipse.ui.internal.WorkspaceLock#isWorkspaceLocked(java.net.URL)}.
	 */
	@Test
	public void testIsWorkspaceLocked() throws Exception {
		URL workspaceUrl = tempDir.toUri().toURL();

		// Test when no lock file exists
		assertFalse(WorkspaceLock.isWorkspaceLocked(workspaceUrl), "Should not be locked when no lock file exists");

		// Create .metadata/.lock file & lock it
		Path metadataDir = tempDir.resolve(".metadata");
		Files.createDirectories(metadataDir);
		Path lockFile = metadataDir.resolve(".lock");
		try (RandomAccessFile lock = lock(lockFile.toFile())) {
			assertTrue(WorkspaceLock.isWorkspaceLocked(workspaceUrl), "Should be locked");
		}
	}

	/**
	 * Mimics
	 * {@linkplain org.eclipse.osgi.internal.location.Locker_JavaNio#lock(File)}
	 * locking a file using Java NIO and returning the RandomAccessFile if
	 * successful, null if the file is already locked by another process.
	 */
	static RandomAccessFile lock(File lockFile) {
		try {
			RandomAccessFile raFile = new RandomAccessFile(lockFile, "rw");
			try {
				FileLock lock = raFile.getChannel().tryLock(0, 1, false);
				if (lock == null) {
					raFile.close();
					return null;
				}
				return raFile;
			} catch (OverlappingFileLockException e) {
				raFile.close();
				return null;
			}
		} catch (IOException e) {
			// already locked by some process, should not happen in test
			return null;
		}
	}
}
