/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation.
 * Licensed Material - Property of IBM.
 * All rights reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.navigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;

/**
 * Provides the properties contained in a *.properties file as children of that
 * file in a Common Navigator.
 * @since 3.2
 */
public class PropertiesContentProvider implements ITreeContentProvider,
		IResourceChangeListener, IResourceDeltaVisitor {

	private static final Object[] NO_CHILDREN = new Object[0];

	private static final Object PROPERTIES_EXT = "properties"; //$NON-NLS-1$

	private final Map<IFile, PropertiesTreeData[]> cachedModelMap = new HashMap<>();

	private StructuredViewer viewer;

	/**
	 * Create the PropertiesContentProvider instance.
	 *
	 * Adds the content provider as a resource change listener to track changes on disk.
	 *
	 */
	public PropertiesContentProvider() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * Return the model elements for a *.properties IFile or
	 * NO_CHILDREN for otherwise.
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;
		if (parentElement instanceof PropertiesTreeData) {
			children = NO_CHILDREN;
		} else if(parentElement instanceof IFile) {
			/* possible model file */
			IFile modelFile = (IFile) parentElement;
			if(PROPERTIES_EXT.equals(modelFile.getFileExtension())) {
				children = cachedModelMap.get(modelFile);
				if(children == null && updateModel(modelFile) != null) {
					children = cachedModelMap.get(modelFile);
				}
			}
		}
		return children != null ? children : NO_CHILDREN;
	}

	/**
	 * Load the model from the given file, if possible.
	 * @param modelFile The IFile which contains the persisted model
	 */
	private synchronized Properties updateModel(IFile modelFile) {

		if(PROPERTIES_EXT.equals(modelFile.getFileExtension()) ) {
			Properties model = new Properties();
			if (modelFile.exists()) {
				try {
					model.load(modelFile.getContents());

					List<PropertiesTreeData> properties = new ArrayList<>();
					for (String propertyName : model.stringPropertyNames()) {
						properties.add(new PropertiesTreeData(propertyName,  model.getProperty(propertyName), modelFile));
					}
					PropertiesTreeData[] propertiesTreeData = properties.toArray(new PropertiesTreeData[properties.size()]);

					cachedModelMap.put(modelFile, propertiesTreeData);
					return model;
				} catch (IOException e) {
				} catch (CoreException e) {
				}
			} else {
				cachedModelMap.remove(modelFile);
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof PropertiesTreeData) {
			PropertiesTreeData data = (PropertiesTreeData) element;
			return data.getFile();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof PropertiesTreeData) {
			return false;
		} else if(element instanceof IFile) {
			return PROPERTIES_EXT.equals(((IFile) element).getFileExtension());
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		cachedModelMap.clear();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		if (oldInput != null && !oldInput.equals(newInput))
			cachedModelMap.clear();
		viewer = (StructuredViewer) aViewer;
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
	public boolean visit(IResourceDelta delta) {

		IResource source = delta.getResource();
		switch (source.getType()) {
		case IResource.ROOT:
		case IResource.PROJECT:
		case IResource.FOLDER:
			return true;
		case IResource.FILE:
			final IFile file = (IFile) source;
			if (PROPERTIES_EXT.equals(file.getFileExtension())) {
				updateModel(file);
				new UIJob("Update Properties Model in CommonViewer") {  //$NON-NLS-1$
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (viewer != null && !viewer.getControl().isDisposed())
							viewer.refresh(file);
						return Status.OK_STATUS;
					}
				}.schedule();
			}
			return false;
		}
		return false;
	}
}
