/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.compare.*;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.internal.ViewerActionBuilder;

/**
 * Creates <code>Viewer</code>s from an <code>IConfigurationElement</code>.
 */
public class ViewerDescriptor implements IViewerDescriptor {

	private final static String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	private final static String EXTENSIONS_ATTRIBUTE= "extensions"; //$NON-NLS-1$

	private IConfigurationElement fConfiguration;
	private IViewerCreator fViewerCreator;
	private Class fViewerClass;

	public ViewerDescriptor(IConfigurationElement config) {
		fConfiguration= config;
	}

	public Viewer createViewer(Viewer currentViewer, Composite parent, CompareConfiguration mp) {
		
		if (currentViewer != null && currentViewer.getClass() == fViewerClass) {
			//System.out.println("reused viewer: " + currentViewer.getClass().getName());
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
			if (viewer != null)
				fViewerClass= viewer.getClass();
			return viewer;
		}

		return null;
	}

	public String getExtension() {
		return fConfiguration.getAttribute(EXTENSIONS_ATTRIBUTE);
	}
}
