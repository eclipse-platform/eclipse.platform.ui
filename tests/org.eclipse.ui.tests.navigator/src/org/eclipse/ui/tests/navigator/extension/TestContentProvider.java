/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
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
 * Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;

public class TestContentProvider implements ITreeContentProvider,
		IResourceChangeListener, IResourceDeltaVisitor {

	private static final String MODEL_ROOT = "root";

	private static final Object[] NO_CHILDREN = new Object[0];

	public static final IPath MODEL_FILE_PATH = new Path("model.properties");

	private final Map rootElements = new HashMap();

	private StructuredViewer viewer;

	public static TestExtensionTreeData _modelRoot;

	public static boolean _dieOnSetInput;
	public static boolean _diedOnSetInput;

	public TestContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		_dieOnSetInput = false;
		_diedOnSetInput = false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) parentElement;
			return data.getChildren();
		} else {

			IProject project = adaptToProject(parentElement);
			if (project != null && project.isAccessible()) {
				IFile modelFile = project.getFile(MODEL_FILE_PATH);
				if (rootElements.containsKey(modelFile)) {
					TestExtensionTreeData model = (TestExtensionTreeData) rootElements.get(modelFile);
					return model != null ? model.getChildren() : NO_CHILDREN;
				} else {
					TestExtensionTreeData model = updateModel(modelFile);
					return model != null ? model.getChildren() : NO_CHILDREN;
				}
			}
		}
		return NO_CHILDREN;
	}

	/**
	 * @param parentElement
	 * @param class1
	 * @return
	 */
	private IProject adaptToProject(Object parentElement) {
		if(parentElement instanceof IProject)
			return (IProject) parentElement;
		else if(parentElement instanceof IAdaptable)
			return ((IAdaptable) parentElement).getAdapter(IProject.class);
		else
			return Platform.getAdapterManager().getAdapter(parentElement, IProject.class);
	}

	/**
	 * @param modelFile
	 */
	private TestExtensionTreeData updateModel(IFile modelFile) {
		Properties model = new Properties();
		if (modelFile.exists()) {
			try (InputStream is = modelFile.getContents()) {
				model.load(is);
				is.close();
				TestExtensionTreeData root = new TestExtensionTreeData(null,
						MODEL_ROOT, model, modelFile);
				_modelRoot = root;
				rootElements.put(modelFile, root);
				return root;
			} catch (IOException | CoreException e) {
			}
		} else {
			rootElements.remove(modelFile);
		}
		return null;

	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) element;
			return data.getChildren().length > 0;
		}
		return false;
	}

	@Override
	public void dispose() {
		rootElements.clear();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

	}

	@Override
	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		if (_dieOnSetInput)
			_diedOnSetInput = true;
		if (oldInput != null && !oldInput.equals(newInput))
			rootElements.clear();
		viewer = (StructuredViewer)aViewer;

	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {

		IResource source = delta.getResource();
		switch (source.getType()) {
		case IResource.ROOT:
		case IResource.PROJECT:
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			final IFile file = (IFile) source;
			if ("model.properties".equals(file.getName())) {
				updateModel(file);
				UIJob.create("Update Test Model in CommonViewer", m -> {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh(file.getParent());
					}
				}).schedule();
			}
			return false;
		}
		return false;
	}
}
