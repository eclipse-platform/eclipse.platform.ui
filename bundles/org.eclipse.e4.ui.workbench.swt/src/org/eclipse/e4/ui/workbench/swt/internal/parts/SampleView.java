/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.swt.internal.parts;

import org.eclipse.e4.core.contexts.IEclipseContext;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * FIXME Eric/Boris what is this needed for????
 */
public class SampleView {
	/**
	 * Create the sample view.
	 * 
	 * @param parent
	 * @param selectionService
	 */
	public SampleView(Composite parent, final IEclipseContext outputContext,
			final IExtensionRegistry registry) {
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.getTree().setData("class", "navigator"); //$NON-NLS-1$ //$NON-NLS-2$
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				outputContext.set(IServiceConstants.SELECTION, event.getSelection());
			}
		});
		viewer.setContentProvider(new ITreeContentProvider() {

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IConfigurationElement) {
					return ((IConfigurationElement) parentElement)
							.getChildren();
				}
				return null;
			}

			public Object getParent(Object element) {
				if (element instanceof IConfigurationElement) {
					return ((IConfigurationElement) element).getParent();
				}
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof IConfigurationElement) {
					return ((IConfigurationElement) element).getChildren().length > 0;
				}
				return false;
			}

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IExtension) {
					return ((IExtension) inputElement)
							.getConfigurationElements();
				}
				return null;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IConfigurationElement) {
					IConfigurationElement c = (IConfigurationElement) element;
					String tag = c.getName();
					String id = c.getAttribute("id"); //$NON-NLS-1$
					if (id == null) {
						id = c.getAttribute("name"); //$NON-NLS-1$
					}
					if (id == null) {
						id = c.getAttribute("api"); //$NON-NLS-1$
					}
					if (id == null) {
						id = c.getAttribute("class"); //$NON-NLS-1$
					}
					return tag + "(" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ""; //$NON-NLS-1$
			}

			@Override
			public Image getImage(Object element) {
				// TODO update this to look for an icon or image attribute
				return super.getImage(element);
			}
		});

		IExtension input = null;
		IExtension[] extensions = registry
				.getExtensions("org.eclipse.e4.ui.workbench"); //$NON-NLS-1$
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].getExtensionPointUniqueIdentifier().equals(
					"org.eclipse.e4.services")) { //$NON-NLS-1$
				input = extensions[i];
				break;
			}
		}
		viewer.setInput(input);
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}
}
