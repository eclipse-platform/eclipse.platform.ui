/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.perf;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Benchmarks basic operations on the IFileStore interface
 */
public class BenchFileStore extends ResourceTest {

	abstract class FileTestRunner extends PerformanceTestRunner {
		protected void setUp() {
			createFiles();
		}

		protected void tearDown() {
			deleteFiles();
		}
	}

	abstract class StoreTestRunner extends PerformanceTestRunner {
		protected void setUp() throws CoreException {
			createStores();
		}

		protected void tearDown() throws CoreException {
			deleteStores();
		}

	}

	private static final int LOOP_SIZE = 20000;
	private static final int REPEATS = 5;
	protected File existingFile;
	protected IFileStore existingStore;

	protected File nonexistingFile;
	protected IFileStore nonexistingStore;

	public BenchFileStore() {
		super();
	}

	public BenchFileStore(String name) {
		super(name);
	}

	protected void createFiles() {
		existingFile = getRandomLocation().toFile();
		try {
			existingFile.createNewFile();
		} catch (IOException e) {
			fail("Failed in createFiles", e);
		}
		nonexistingFile = getRandomLocation().toFile();
	}

	protected void createStores() throws CoreException {
		existingStore = FileSystemCore.getFileSystem(IFileStoreConstants.SCHEME_FILE).getStore(getRandomLocation());
		try {
			existingStore.openOutputStream(IFileStoreConstants.NONE, null).close();
		} catch (IOException e) {
			fail("BenchFileStore.createStores", e);
		}
		nonexistingStore = FileSystemCore.getFileSystem(IFileStoreConstants.SCHEME_FILE).getStore(getRandomLocation());
	}

	protected void deleteFiles() {
		existingFile.delete();
	}

	protected void deleteStores() throws CoreException {
		existingStore.delete(IFileStoreConstants.NONE, null);
	}

	public void testFileExists() {
		new FileTestRunner() {
			protected void test() {
				existingFile.exists();
				nonexistingFile.exists();
			}
		}.run(this, REPEATS, LOOP_SIZE);
	}

	public void testFileLastModified() {
		new FileTestRunner() {
			protected void test() {
				existingFile.lastModified();
				nonexistingFile.lastModified();
			}
		}.run(this, REPEATS, LOOP_SIZE);
	}

	public void testStoreExists() {
		new StoreTestRunner() {
			protected void test() {
				existingStore.fetchInfo().exists();
				nonexistingStore.fetchInfo().exists();
			}
		}.run(this, REPEATS, LOOP_SIZE);
	}

	public void testStoreLastModified() {
		new StoreTestRunner() {
			protected void test() {
				existingStore.fetchInfo().getLastModified();
				nonexistingStore.fetchInfo().getLastModified();
			}
		}.run(this, REPEATS, LOOP_SIZE);
	}
}