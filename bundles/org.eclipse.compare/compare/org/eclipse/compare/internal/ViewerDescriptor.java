/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.compare.*;

import org.eclipse.jface.viewers.Viewer;

/**
 * Creates <code>Viewer</code>s from an <code>IConfigurationElement</code>.
 */
public class ViewerDescriptor implements IViewerDescriptor {

	private final static String CLASS_ATTRIBUTE= "class";
	private final static String EXTENSIONS_ATTRIBUTE= "extensions";

	private IConfigurationElement fConfiguration;
	private IViewerCreator fViewerCreator;

	public ViewerDescriptor(IConfigurationElement config) {
		fConfiguration= config;
	}

	public Viewer createViewer(Viewer currentViewer, Composite parent, CompareConfiguration mp) {
		String className= fConfiguration.getAttribute(CLASS_ATTRIBUTE);
		if (currentViewer != null && currentViewer.getClass().getName().equals(className)) {
			return currentViewer;
		}
		if (fViewerCreator == null) {
			try {
				fViewerCreator= (IViewerCreator) fConfiguration.createExecutableExtension(CLASS_ATTRIBUTE);
			} catch (CoreException e) {
			}
		}

		if (fViewerCreator != null) {
			Viewer viewer= fViewerCreator.createViewer(parent, mp);
			//if (viewer != null && currentViewer != null && viewer.getClass() == currentViewer.getClass())
			//	return currentViewer;
			return viewer;
		}

		return null;
	}

	public String getExtension() {
		return fConfiguration.getAttribute(EXTENSIONS_ATTRIBUTE);
	}
}
