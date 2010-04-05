/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.registry.ViewDescriptor;

public class ViewReference extends WorkbenchPartReference implements IViewReference {

	private ViewDescriptor descriptor;
	private ViewSite viewSite;

	public ViewReference(IEclipseContext windowContext, IWorkbenchPage page, MPart part,
			ViewDescriptor descriptor) {
		super(windowContext, page, part);
		this.descriptor = descriptor;

		setImageDescriptor(descriptor.getImageDescriptor());
	}

	public String getPartName() {
		return descriptor.getLabel();
	}

	public String getSecondaryId() {
		// TODO Auto-generated method stub
		return null;
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
			return descriptor.createView();
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			throw new PartInitException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					status.getCode(), status.getMessage(), status.getException()));
		}
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
		viewSite = new ViewSite(getModel(), part, descriptor.getConfigurationElement());
		ContextInjectionFactory.inject(viewSite, getModel().getContext());
		((IViewPart) part).init(viewSite, null);
	}

	@Override
	public PartSite getSite() {
		return viewSite;
	}
}

