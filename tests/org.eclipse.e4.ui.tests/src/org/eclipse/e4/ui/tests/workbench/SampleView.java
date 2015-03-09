/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class SampleView {

	private IEclipseContext context;

	private boolean destroyed = false;

	boolean errorOnWidgetDisposal = false;

	boolean errorOnPreDestroy = false;

	boolean nullParentContext = false;

	private TreeViewer viewer;

	private boolean statePersisted;

	/**
	 * Create the sample view.
	 *
	 * @param parent
	 * @param selectionService
	 */
	@Inject
	public SampleView(Composite parent, final IEclipseContext outputContext,
			final IExtensionRegistry registry) {
		context = outputContext;

		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (errorOnWidgetDisposal) {
					throw new RuntimeException();
				}
			}
		});

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setData("class", "navigator"); //$NON-NLS-1$ //$NON-NLS-2$
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				outputContext.set(IServiceConstants.ACTIVE_SELECTION,
						event.getSelection());
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

	@PreDestroy
	void preDestroy() {
		destroyed = true;
		nullParentContext = context.getParent() == null;

		if (errorOnPreDestroy) {
			throw new RuntimeException();
		}
	}

	@PersistState
	void persistState() {
		if (!viewer.getControl().isDisposed()) {
			statePersisted = true;
		}
	}

	public boolean isStatePersisted() {
		return statePersisted;
	}

	public boolean isDestroyed() {
		return destroyed;
	}
}
