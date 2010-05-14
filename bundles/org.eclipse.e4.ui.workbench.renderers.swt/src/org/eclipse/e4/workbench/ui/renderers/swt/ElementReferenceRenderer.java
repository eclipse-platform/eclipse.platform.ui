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
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
		return renderedMap.get(element);
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
		newComp.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				List<MPlaceholder> refs = renderedMap.get(ref);
				refs.remove(element);
				if (refs.size() == 0) {
					System.out.println("Last Ref closed"); //$NON-NLS-1$
					// renderingEngine.removeGui(ref);
				}
			}
		});

		Control refWidget = (Control) ref.getWidget();
		if (refWidget == null) {
			refWidget = (Control) renderingEngine.createGui(ref, newComp);
		} else {
			if (refWidget.getParent() != newComp) {
				refWidget.setParent(newComp);
			}
		}

		if (ref instanceof MContext) {
			IEclipseContext context = ((MContext) ref).getContext();
			IEclipseContext newParentContext = getContext(ph);
			if (context.getParent() != newParentContext)
				context.setParent(newParentContext);
		}

		return newComp;
	}
}
