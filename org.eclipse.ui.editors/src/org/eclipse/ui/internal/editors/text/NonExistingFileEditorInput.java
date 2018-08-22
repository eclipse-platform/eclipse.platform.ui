/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.editors.text;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * @since 3.1
 */
public class NonExistingFileEditorInput implements IEditorInput, ILocationProvider {

	private static int fgNonExisting= 0;

	private IFileStore fFileStore;
	private String fName;

	public NonExistingFileEditorInput(IFileStore fileStore, String namePrefix) {
		Assert.isNotNull(fileStore);
		Assert.isTrue(EFS.SCHEME_FILE.equals(fileStore.getFileSystem().getScheme()));
		fFileStore= fileStore;
		++fgNonExisting;
		fName= namePrefix + " " + fgNonExisting; //$NON-NLS-1$
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(fName);
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return fName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (ILocationProvider.class.equals(adapter))
			return (T) this;
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	@Override
	public IPath getPath(Object element) {
		if (element instanceof NonExistingFileEditorInput) {
			NonExistingFileEditorInput input= (NonExistingFileEditorInput)element;
			return new Path(input.fFileStore.toURI().getPath());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o instanceof NonExistingFileEditorInput) {
			NonExistingFileEditorInput input = (NonExistingFileEditorInput) o;
			return fFileStore.equals(input.fFileStore);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return fFileStore.hashCode();
	}
}
