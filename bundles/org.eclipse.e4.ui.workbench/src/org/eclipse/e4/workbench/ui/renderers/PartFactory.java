/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers;

import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.emf.databinding.EMFDataBindingContext;

public abstract class PartFactory {
	public static final String OWNING_ME = "modelElement"; //$NON-NLS-1$

	protected PartRenderer renderer;
	protected IContributionFactory contributionFactory;
	protected IEclipseContext context;
	protected EMFDataBindingContext dbc;

	public PartFactory() {
		dbc = new EMFDataBindingContext();
	}

	public void init(PartRenderer renderer, IEclipseContext context,
			IContributionFactory contributionFactory) {
		this.renderer = renderer;
		this.contributionFactory = contributionFactory;
		this.context = context;
	}

	public abstract Object createWidget(MPart<?> element);

	public abstract <P extends MPart<?>> void processContents(MPart<P> me);

	public void postProcess(MPart<?> childME) {
	}

	public abstract void bindWidget(MPart<?> me, Object widget);

	protected abstract Object getParentWidget(MPart<?> element);

	public abstract void disposeWidget(MPart<?> part);

	public abstract void hookControllerLogic(final MPart<?> me);

	public abstract void childAdded(MPart<?> parentElement, MPart<?> element);

	public void childRemoved(MPart<?> parentElement, MPart<?> child) {
	}

	protected abstract Object getImage(MApplicationElement element);

	public Object createMenu(MPart<?> part, Object widgetObject, MMenu menu) {
		return null;
	}

	public Object createToolBar(MPart<?> part, Object widgetObject, MToolBar toolBar) {
		return null;
	}

	/**
	 * Return a parent context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the parent's closest context, or global context if none in the hierarchy
	 */
	protected IEclipseContext getContextForParent(MPart<?> part) {
		MPart<?> parent = part.getParent();
		while (parent != null) {
			if (parent.getContext() != null) {
				return parent.getContext();
			}
			parent = parent.getParent();
		}
		return context;
	}

	/**
	 * Return a context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the closest context, or global context if none in the hierarchy
	 */
	protected IEclipseContext getContext(MPart<?> part) {
		if (part.getContext() != null) {
			return part.getContext();
		}
		return getContextForParent(part);
	}

	protected IEclipseContext getToplevelContext(MPart<?> part) {
		IEclipseContext result = null;
		if (part.getParent() != null) {
			result = getToplevelContext(part.getParent());
		}
		if (result == null) {
			result = part.getContext();
		}
		return result;
	}

	/**
	 * Activate the part in the hierarchy. This should either still be internal or be a public
	 * method somewhere else.
	 * 
	 * @param part
	 */
	public void activate(MPart<?> part) {
		MPart<MPart<?>> parent = (MPart<MPart<?>>) part.getParent();
		IEclipseContext partContext = part.getContext();
		while (parent != null) {
			IEclipseContext parentContext = parent.getContext();
			// The context has to be changed first as the events created by #setActiveChild()
			// will use context information.
			if (parentContext != null) {
				parentContext.set(IServiceConstants.ACTIVE_CHILD, partContext);
				partContext = parentContext;
			}
			if (parent.getActiveChild() != part)
				parent.setActiveChild(part);
			part = parent;
			parent = (MPart<MPart<?>>) parent.getParent();
		}
	}

	public void removeGui(MPart element, Object widget) {
	}

}
