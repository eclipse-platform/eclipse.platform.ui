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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;

public class TestContentProvider implements ITreeContentProvider,
		IResourceChangeListener, IResourceDeltaVisitor {

	private static final String MODEL_ROOT = "root";

	private static final Object[] NO_CHILDREN = new Object[0];

	private static final IPath MODEL_FILE_PATH = new Path("model.properties");

	private final Map rootElements = new HashMap();

	private StructuredViewer viewer;
	
	public TestContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	public Object[] getChildren(Object parentElement) {

		if (parentElement instanceof IProject) {
			IProject project = (IProject) parentElement;
			if (project.isAccessible()) {
				IFile modelFile = project.getFile(MODEL_FILE_PATH);
				if (rootElements.containsKey(modelFile)) {
					TestExtensionTreeData model = (TestExtensionTreeData) rootElements
							.get(modelFile);
					return model != null ? model.getChildren() : NO_CHILDREN;
				} else {
					TestExtensionTreeData model = updateModel(modelFile);
					return model != null ? model.getChildren() : NO_CHILDREN;
				}
			}
		} else if (parentElement instanceof TestExtensionTreeData) {
			TestExtensionTreeData data = (TestExtensionTreeData) parentElement;
			return data.getChildren();
		}
		return NO_CHILDREN;
	}

	/**
	 * @param modelFile
	 */
	private TestExtensionTreeData updateModel(IFile modelFile) {
		Properties model = new Properties();
		if (modelFile.exists()) {
			try {
				model.load(modelFile.getContents());
				TestExtensionTreeData root = new TestExtensionTreeData(null,
						MODEL_ROOT, model);
				rootElements.put(modelFile, root);
				return root;
			} catch (IOException e) {
			} catch (CoreException e) {
			}
		}
		return null;

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
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

	}

	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		if (oldInput != null && !oldInput.equals(newInput))
			rootElements.clear();
		viewer = (StructuredViewer)aViewer;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(this);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
	 */
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
				new UIJob("Update Test Model in CommonViewer") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (viewer != null && !viewer.getControl().isDisposed())
							viewer.refresh(file.getParent());
						return Status.OK_STATUS;						
					}
				}.schedule();
			}
			return false;
		}
		return false;
	}
}
