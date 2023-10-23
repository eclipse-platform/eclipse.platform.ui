/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
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
 *     Oakland Software Incorporated - Added to CNF tests
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.jst;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;

public class CompressedJavaProject implements ICompressedNode, IAdaptable {

	private final IProject project;
	private CompressedJavaLibraries compressedLibraries;

	public CompressedJavaProject(StructuredViewer viewer, IProject project) {
		this.project = project;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getLabel() {
		return determineLabel();
	}

	public boolean isFlatteningSourceFolder() {
		return getNonExternalSourceFolders().size() == 1;
	}

	private String determineLabel() {
		List nonextSourceFolders = getNonExternalSourceFolders();
		Object singleRoot = null;
		if (nonextSourceFolders.size() == 1) {
			singleRoot = nonextSourceFolders.get(0);
		}
		return "Compressed Java resources: " + ((singleRoot != null) ? ": " + singleRoot : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public IProject getProject() {
		return project;
	}

	@Override
	public Object[] getChildren(ITreeContentProvider delegateContentProvider) {

		List<Object> nonExternalSourceFolders = getNonExternalSourceFolders();
		if (nonExternalSourceFolders.size() == 1) {
			Object[] sourceFolderChildren = delegateContentProvider
					.getChildren(nonExternalSourceFolders.get(0));
			nonExternalSourceFolders.clear();
			nonExternalSourceFolders
					.addAll(Arrays.asList(sourceFolderChildren));
		}
		nonExternalSourceFolders.add(getCompressedJavaLibraries());
		return nonExternalSourceFolders.toArray();
	}

	public List<Object> getNonExternalSourceFolders() {
		Object[] sourceFolders;
		try {
			Object jProject = WebJavaContentProvider
					.javaCoreCreateProject(project);
			Method m = WebJavaContentProvider.IJAVA_PROJECT_CLASS.getMethod("getPackageFragmentRoots");
			sourceFolders = (Object[]) m.invoke(jProject);
			return new ArrayList<>(Arrays
					.asList(sourceFolders));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public CompressedJavaLibraries getCompressedJavaLibraries() {
		if (compressedLibraries == null)
			compressedLibraries = new CompressedJavaLibraries(this);
		return compressedLibraries;

	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
