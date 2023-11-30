/*******************************************************************************
 * Copyright (c) 2009, 2017 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 429728
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public abstract class AbstractPartRenderer {
	public static final String OWNING_ME = "modelElement"; //$NON-NLS-1$

	protected IEclipseContext context;
	protected EModelService modelService;

	public void init(IEclipseContext context) {
		this.context = context;
		modelService = context.get(EModelService.class);
	}

	public abstract Object createWidget(MUIElement element, Object parent);

	public abstract void processContents(MElementContainer<MUIElement> container);

	public void postProcess(MUIElement childElement) {
	}

	public abstract void bindWidget(MUIElement me, Object widget);

	protected abstract Object getParentWidget(MUIElement element);

	public abstract void disposeWidget(MUIElement part);

	public abstract void hookControllerLogic(final MUIElement me);

	public abstract void childRendered(MElementContainer<MUIElement> parentElement, MUIElement element);

	public void hideChild(MElementContainer<MUIElement> parentElement, MUIElement child) {
	}

	protected abstract Object getImage(MUILabel element);

	/**
	 * Return a parent context for this part.
	 *
	 * @param element
	 *            the part to start searching from
	 * @return the parent's closest context, or global context if none in the
	 *         hierarchy
	 */
	protected IEclipseContext getContextForParent(MUIElement element) {
		return modelService.getContainingContext(element);
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
			return ((MContext) part).getContext();
		}
		return getContextForParent(part);
	}

	/**
	 * Activate the part in the hierarchy. This should either still be internal
	 * or be a public method somewhere else.
	 */
	public void activate(MPart element) {
		IEclipseContext curContext = getContext(element);
		if (curContext != null) {
			EPartService ps = curContext.get(EPartService.class);
			if (ps != null)
				ps.activate(element, requiresFocus(element));
		}
	}

	/**
	 * Check if activating {@code element} requires that the part set the focus.
	 *
	 * @return true if the part requires focus
	 */
	protected abstract boolean requiresFocus(MPart element);

	public void removeGui(MUIElement element, Object widget) {
	}

	public Object getUIContainer(MUIElement element) {
		if (element.getParent() != null)
			return element.getParent().getWidget();
		else {
			Object value = element.getTransientData().get(IPresentationEngine.RENDERING_PARENT_KEY);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	/**
	 * Force the UI focus into the element if possible. This method should not
	 * be called directly, it will be called by the IPresentationEngine#focusGui
	 * method if the normal process used to set the focus cannot be performed.
	 */
	public void forceFocus(MUIElement element) {
		// Do nothing by default
	}

	/**
	 * @return Returns the style override bits or -1 if there is no override
	 */
	public int getStyleOverride(MUIElement mElement) {
		String overrideStr = mElement.getPersistedState().get(IPresentationEngine.STYLE_OVERRIDE_KEY);
		if (overrideStr == null || overrideStr.length() == 0) {
			return -1;
		}

		return Integer.parseInt(overrideStr);
	}
}
