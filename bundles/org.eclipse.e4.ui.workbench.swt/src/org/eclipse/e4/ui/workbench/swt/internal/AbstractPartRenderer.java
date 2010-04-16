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
package org.eclipse.e4.ui.workbench.swt.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.EObject;

public abstract class AbstractPartRenderer {
	public static final String OWNING_ME = "modelElement"; //$NON-NLS-1$

	protected IEclipseContext context;

	public void init(IEclipseContext context) {
		this.context = context;
	}

	public abstract Object createWidget(MUIElement element, Object parent);

	public abstract void processContents(MElementContainer<MUIElement> container);

	public void postProcess(MUIElement childElement) {
	}

	public abstract void bindWidget(MUIElement me, Object widget);

	protected abstract Object getParentWidget(MUIElement element);

	public abstract void disposeWidget(MUIElement part);

	public abstract void hookControllerLogic(final MUIElement me);

	public abstract void childRendered(
			MElementContainer<MUIElement> parentElement, MUIElement element);

	public void hideChild(MElementContainer<MUIElement> parentElement,
			MUIElement child) {
	}

	protected abstract Object getImage(MUILabel element);

	//
	// public Object createMenu(Object widgetObject, MMenu menu) {
	// return null;
	// }
	//
	// public Object createToolBar(Object widgetObject, MToolBar toolBar) {
	// return null;
	// }

	/**
	 * Return a parent context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the parent's closest context, or global context if none in the
	 *         hierarchy
	 */
	protected IEclipseContext getContextForParent(MUIElement part) {
		MContext pwc = getParentWithContext(part);
		return pwc != null ? pwc.getContext() : context;
	}

	/**
	 * Return a parent context for this part. Note that this code uses the EMF
	 * container rather than walking the model's 'parent' references in order to
	 * handle elements that are not contained in the regular child lists.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the parent's closest context, or global context if none in the
	 *         hierarchy
	 */
	protected MContext getParentWithContext(MUIElement part) {
		EObject parent = ((EObject) part).eContainer();
		while (parent != null) {
			if (parent instanceof MContext) {
				if (((MContext) parent).getContext() != null)
					return (MContext) parent;
			}
			parent = parent.eContainer();
		}
		return null;
	}

	/**
	 * Return a context for this part.
	 * 
	 * @param part
	 *            the part to start searching from
	 * @return the closest context, or global context if none in the hierarchy
	 */
	protected IEclipseContext getContext(MUIElement part) {
		if (part instanceof MContext) {
			IEclipseContext theContext = ((MContext) part).getContext();
			if (theContext != null)
				return theContext;
		}
		return getContextForParent(part);
	}

	protected IEclipseContext getToplevelContext(MUIElement part) {
		IEclipseContext result = null;
		if (part.getParent() != null) {
			result = getToplevelContext(part.getParent());
		}
		if (result == null && part instanceof MContext) {
			result = ((MContext) part).getContext();
		}
		return result;
	}

	/**
	 * Activate the part in the hierarchy. This should either still be internal
	 * or be a public method somewhere else.
	 * 
	 * @param element
	 */
	public void activate(MPart element) {
		IEclipseContext curContext = getContext(element);
		EPartService ps = (EPartService) curContext.get(EPartService.class
				.getName());
		ps.activate(element);
	}

	public void removeGui(MUIElement element, Object widget) {
	}

	public Object getUIContainer(MUIElement element) {
		if (element.getParent() != null)
			return element.getParent().getWidget();

		return null;
	}
}
