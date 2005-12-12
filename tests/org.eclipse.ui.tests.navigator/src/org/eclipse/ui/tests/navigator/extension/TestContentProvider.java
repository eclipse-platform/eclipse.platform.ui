/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TestContentProvider implements ITreeContentProvider {

	private static final String MODEL_ROOT = "root";

	private static final Object[] NO_CHILDREN = new Object[0];

	private static final IPath MODEL_FILE_PATH = new Path("model.properties");

	private final Map rootElements = new HashMap();

	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof IProject) {
			IProject project = (IProject) parentElement;
			if (project.isAccessible()) {
				IFile modelFile = project.getFile(MODEL_FILE_PATH);
				if (rootElements.containsKey(modelFile)) {
					TestExtensionTreeData root = (TestExtensionTreeData)
					   rootElements.get(modelFile);
					return root.getChildren();
				} else {
					Properties model = new Properties();
					if (modelFile.exists()) {
						try {
							model.load(modelFile.getContents());
							TestExtensionTreeData root = new TestExtensionTreeData(
									null, MODEL_ROOT, model);
							rootElements.put(modelFile, root);
							return root.getChildren();
						} catch (IOException e) {
						} catch (CoreException e) {
						}
					}
				}
			}
		} 
		else if (parentElement instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) parentElement;
			return data.getChildren();
		}
		return NO_CHILDREN;
	}

	public Object getParent(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getChildren().length > 0;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		rootElements.clear();

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 
		if(oldInput != null && !oldInput.equals(newInput))
			rootElements.clear();

	}

}
