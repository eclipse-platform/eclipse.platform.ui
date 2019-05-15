/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Oakland Software Incorporated - Added to CNF tests
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.jst;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;

public class CompressedJavaLibraries implements ICompressedNode {

	private CompressedJavaProject compressedProject;

	public CompressedJavaLibraries(CompressedJavaProject compressedProject) {
		this.compressedProject = compressedProject;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getLabel() {
		return "Compressed Libraries";
	}

	@Override
	public Object[] getChildren(ITreeContentProvider delegateContentProvider) {

		List classpathContainers = new ArrayList();
		Object[] delegateChildren = delegateContentProvider
				.getChildren(compressedProject.getProject());
		for (Object delegateChild : delegateChildren) {
			if (WebJavaContentProvider.IPACKAGE_FRAGMENT_ROOT_CLASS.isInstance(delegateChild)) {
				try {
					classpathContainers.add(delegateChild);
				} catch (Exception e) {

				}
			} else if (!WebJavaContentProvider.IJAVA_ELEMENT_CLASS.isInstance(delegateChild)
					&& !(delegateChild instanceof IResource)) {
				classpathContainers.add(delegateChild);
			}
		}
		return classpathContainers.toArray();
	}

	public CompressedJavaProject getCompressedProject() {
		return compressedProject;
	}

}
