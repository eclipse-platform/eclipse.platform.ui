/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.ide.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests [I]FileEditorInput API.
 *
 * @since 3.1
 */
public class FileEditorInputTest extends UITestCase {

	/**
	 * @param testName
	 */
	public FileEditorInputTest(String testName) {
		super(testName);
	}

	/**
	 * Regression test for bug 72337 - [IDE] FileEditorInput .equals() not implemented against interface
	 */
	@SuppressWarnings("unlikely-arg-type")
	public void testBug72337() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath path = new Path("/foo/bar.txt");
		IFile fileA = workspace.getRoot().getFile(path);
		FileEditorInput inputA1 = new FileEditorInput(fileA);
		OtherFileEditorInput inputA2 = new OtherFileEditorInput(fileA);
		assertTrue(inputA1.equals(inputA2));
		assertTrue(inputA2.equals(inputA1));
	}

	class OtherFileEditorInput implements IFileEditorInput {
		private IFile file;

		public OtherFileEditorInput(IFile file) {
			this.file = file;
		}

		@Override
		public IFile getFile() {
			return file;
		}

		/**
		 * @throws CoreException if this method fails
		 */
		@Override
		public IStorage getStorage() throws CoreException {
			return file;
		}

		@Override
		public boolean exists() {
			return file.exists();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return null;
		}

		@Override
		public String getName() {
			return file.getName();
		}

		@Override
		public IPersistableElement getPersistable() {
			return null;
		}

		@Override
		public String getToolTipText() {
			return file.getFullPath().toString();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(Class<T> adapter) {
			if (adapter == IResource.class) {
				return (T) file;
			}
			if (adapter == IFile.class) {
				return (T) file;
			}
			return null;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof IFileEditorInput)) {
				return false;
			}
			IFileEditorInput other = (IFileEditorInput) obj;
			return file.equals(other.getFile());
		}

		@Override
		public int hashCode() {
			return file.hashCode();
		}
	}
}
