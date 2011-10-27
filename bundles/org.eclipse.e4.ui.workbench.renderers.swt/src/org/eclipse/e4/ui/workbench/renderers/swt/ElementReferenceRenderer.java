/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Create an element from a reference
 */
public class ElementReferenceRenderer extends SWTPartRenderer {

	private static Map<MUIElement, List<MPlaceholder>> renderedMap = new HashMap<MUIElement, List<MPlaceholder>>();

	/**
	 * Get the list of all place holders that reference the given element
	 * 
	 * @param element
	 *            The element to get place holders for
	 * @return The list of rendered place holders (may be null)
	 */
	public static List<MPlaceholder> getRenderedPlaceholders(MUIElement element) {
		List<MPlaceholder> mapVal = renderedMap.get(element);
		if (mapVal == null)
			return new ArrayList<MPlaceholder>();

		return mapVal;
	}

	@Inject
	IPresentationEngine renderingEngine;

	public Object createWidget(final MUIElement element, Object parent) {
		MPlaceholder ph = (MPlaceholder) element;
		final MUIElement ref = ph.getRef();
		ref.setCurSharedRef(ph);

		List<MPlaceholder> renderedRefs = renderedMap.get(ref);
		if (renderedRefs == null) {
			renderedRefs = new ArrayList<MPlaceholder>();
			renderedMap.put(ref, renderedRefs);
		}

		if (!renderedRefs.contains(ph))
			renderedRefs.add(ph);

		Composite newComp = new Composite((Composite) parent, SWT.NONE);
		newComp.setLayout(new FillLayout());

		Control refWidget = (Control) ref.getWidget();
		if (refWidget == null) {
			ref.setToBeRendered(true);
			refWidget = (Control) renderingEngine.createGui(ref, newComp,
					getContextForParent(ref));
		} else {
			if (refWidget.getParent() != newComp) {
				refWidget.setParent(newComp);
			}
		}

		if (ref instanceof MContext) {
			IEclipseContext context = ((MContext) ref).getContext();
			IEclipseContext newParentContext = getContext(ph);
			if (context.getParent() != newParentContext) {
				context.setParent(newParentContext);
			}
		}

		return newComp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer#disposeWidget
	 * (org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void disposeWidget(MUIElement element) {
		MPlaceholder ph = (MPlaceholder) element;
		MUIElement refElement = ph.getRef();
		Control refCtrl = (Control) refElement.getWidget();

		// Remove the element ref from the rendered list
		List<MPlaceholder> refs = renderedMap.get(refElement);
		refs.remove(ph);

		IEclipseContext curContext = modelService.getContainingContext(ph);

		if (refs.size() == 0) {
			// Ensure that the image is the 'original' image for this
			// part. See bug 347471 for details
			if (refElement instanceof MPart) {
				MPart thePart = (MPart) refElement;
				String imageURI = thePart.getIconURI();
				thePart.setIconURI(null);
				thePart.setIconURI(imageURI);
			}

			renderingEngine.removeGui(refElement);
		} else {
			// Ensure that the dispose of the element reference doesn't cascade
			// to dispose the 'real' part
			if (refCtrl != null && !refCtrl.isDisposed()) {
				MPlaceholder currentRef = refElement.getCurSharedRef();
				if (currentRef == ph) {
					// Find another *rendered* ref to pass the part on to
					for (MPlaceholder aPH : refs) {
						Composite phComp = (Composite) aPH.getWidget();
						if (phComp == null || phComp.isDisposed())
							continue;

						// Reparent the context(s) (if any)
						IEclipseContext newParentContext = modelService
								.getContainingContext(aPH);
						List<MContext> allContexts = modelService.findElements(
								refElement, null, MContext.class, null);
						for (MContext ctxtElement : allContexts) {
							IEclipseContext theContext = ctxtElement
									.getContext();
							// this may be null if it hasn't been rendered yet
							if (theContext != null) {
								if (theContext.getParent() == curContext) {
									// about to reparent the context, if we're
									// the active child of the current parent,
									// deactivate ourselves first
									if (curContext.getActiveChild() == theContext) {
										theContext.deactivate();
									}
									theContext.setParent(newParentContext);
								}
							}
						}

						// reset the 'cur' ref
						refElement.setCurSharedRef(aPH);

						// Reparent the widget
						refCtrl.setParent(phComp);
						break;
					}
				} else if (currentRef != null) {
					Composite phComp = (Composite) currentRef.getWidget();
					if (phComp == null || phComp.isDisposed()) {
						super.disposeWidget(element);
						return;
					}

					// Reparent the context(s) (if any)
					IEclipseContext newParentContext = modelService
							.getContainingContext(currentRef);
					List<MContext> allContexts = modelService.findElements(
							refElement, null, MContext.class, null);
					for (MContext ctxtElement : allContexts) {
						IEclipseContext theContext = ctxtElement.getContext();
						// this may be null if it hasn't been rendered yet
						if (theContext != null
								&& theContext.getParent() == curContext) {
							// about to reparent the context, if we're the
							// active child of the current parent, deactivate
							// ourselves first
							if (curContext.getActiveChild() == theContext) {
								theContext.deactivate();
							}
							theContext.setParent(newParentContext);
						}
					}
				}
			}
		}

		super.disposeWidget(element);
	}
}
