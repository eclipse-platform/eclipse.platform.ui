/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.TestPlugin;

public class ResourceMappingTestView extends ViewPart implements IViewPart {

	private TreeViewer viewer;

	public ResourceMappingTestView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.getTree().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TestResourceMapping) element).getName();
			}
		});

		viewer.setContentProvider(getContentProvider());
		viewer.setInput(new TestResourceMapping(TestPlugin.getWorkspace()
				.getRoot()));
	}

	private IContentProvider getContentProvider() {
		return new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object parentElement) {
				return ((TestResourceMapping) parentElement).getChildren();
			}

			@Override
			public Object getParent(Object element) {
				return ((TestResourceMapping) element).getParent();
			}

			@Override
			public boolean hasChildren(Object element) {
				return ((TestResourceMapping) element).getChildren().length > 0;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return ((TestResourceMapping) inputElement).getChildren();
			}

			@Override
			public void dispose() {

			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

		};
	}

	@Override
	public void setFocus() {
		viewer.setSelection(new StructuredSelection(new TestResourceMapping(
				TestPlugin.getWorkspace().getRoot())));
	}

	public IMarker addMarkerToFirstProject() {

		TestResourceMapping top = ((TestResourceMapping) viewer.getInput());
		IResource element = top.getChildren()[0].element;

		try {
			IMarker marker = element.createMarker("org.eclipse.core.resources.problemmarker");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			return marker;
		} catch (CoreException e) {
			return null;
		}



	}
}
