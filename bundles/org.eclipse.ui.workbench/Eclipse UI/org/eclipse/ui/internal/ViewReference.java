/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.registry.ViewDescriptor;

public class ViewReference extends WorkbenchPartReference implements IViewReference {

	private ViewDescriptor descriptor;
	private ViewSite viewSite;
	private IMemento memento;

	public ViewReference(IEclipseContext windowContext, IWorkbenchPage page, MPart part,
			ViewDescriptor descriptor) {
		super(windowContext, page, part);
		this.descriptor = descriptor;

		if (descriptor == null) {
			setImageDescriptor(ImageDescriptor.getMissingImageDescriptor());
		} else {
			setImageDescriptor(descriptor.getImageDescriptor());
		}

		String mementoString = getModel().getPersistedState().get(MEMENTO_KEY);
		if (mementoString != null) {
			try {
				memento = XMLMemento.createReadRoot(new StringReader(mementoString));
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log(e);
			}
		}
	}

	void persist() {
		IViewPart view = getView(false);
		if (view != null) {
			XMLMemento root = XMLMemento.createWriteRoot("view"); //$NON-NLS-1$
			view.saveState(root);
			StringWriter writer = new StringWriter();
			try {
				root.save(writer);
				getModel().getPersistedState().put(MEMENTO_KEY, writer.toString());
			} catch (IOException e) {
				WorkbenchPlugin.log(e);
			}
		}
	}

	public String getPartName() {
		return descriptor.getLabel();
	}

	public String getSecondaryId() {
		MPart part = getModel();

		int colonIndex = part.getElementId().indexOf(':');
		if (colonIndex == -1 || colonIndex == (part.getElementId().length() - 1))
			return null;

		return part.getElementId().substring(colonIndex + 1);
	}

	public IViewPart getView(boolean restore) {
		return (IViewPart) getPart(restore);
	}

	public boolean isFastView() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.WorkbenchPartReference#createPart
	 * ()
	 */
	@Override
	public IWorkbenchPart createPart() throws PartInitException {
		try {
			if (descriptor == null) {
				return createErrorPart();
			}

			return descriptor.createView();
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			throw new PartInitException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					status.getCode(), status.getMessage(), e));
		}
	}

	@Override
	IWorkbenchPart createErrorPart() {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, NLS.bind(
				WorkbenchMessages.ViewFactory_initException, getModel().getElementId()));
		return createErrorPart(status);
	}

	@Override
	public IWorkbenchPart createErrorPart(IStatus status) {
		return new ErrorViewPart(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.WorkbenchPartReference#initialize
	 * (org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void initialize(IWorkbenchPart part) throws PartInitException {
		viewSite = new ViewSite(getModel(), part, this, descriptor == null ? null
				: descriptor.getConfigurationElement());
		IViewPart view = (IViewPart) part;
		view.init(viewSite, memento);

		if (view.getSite() != viewSite || view.getViewSite() != viewSite) {
			String id = descriptor == null ? getModel().getElementId() : descriptor.getId();
			throw new PartInitException(NLS.bind(WorkbenchMessages.ViewFactory_siteException, id));
		}

		legacyPart = part;
		addPropertyListeners();
	}

	@Override
	public PartSite getSite() {
		return viewSite;
	}
}
