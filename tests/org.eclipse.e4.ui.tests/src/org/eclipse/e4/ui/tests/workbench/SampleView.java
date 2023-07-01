/*******************************************************************************
 * Copyright (c) 2009, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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
	 */
	@Inject
	public SampleView(Composite parent, final IEclipseContext outputContext,
			final IExtensionRegistry registry) {
		context = outputContext;

		parent.addDisposeListener(e -> {
			if (errorOnWidgetDisposal) {
				throw new RuntimeException();
			}
		});

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setData("class", "navigator"); //$NON-NLS-1$ //$NON-NLS-2$
		viewer.addSelectionChangedListener(
				event -> outputContext.set(IServiceConstants.ACTIVE_SELECTION, event.getSelection()));
		viewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IConfigurationElement ce) {
					return ce.getChildren();
				}
				return null;
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof IConfigurationElement ce) {
					return ce.getParent();
				}
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				if (element instanceof IConfigurationElement ce) {
					return ce.getChildren().length > 0;
				}
				return false;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof IExtension e) {
					return e.getConfigurationElements();
				}
				return null;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IConfigurationElement c) {
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
		for (IExtension extension : extensions) {
			if (extension.getExtensionPointUniqueIdentifier().equals("org.eclipse.e4.services")) { // $NON-NLS-1$
				input = extension;
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
