/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

public abstract class SWTPartRenderer extends AbstractPartRenderer {

	Map<String, Image> imageMap = new HashMap<String, Image>();

	public void processContents(MElementContainer<MUIElement> container) {
		// EMF gives us null lists if empty
		if (container == null)
			return;

		// Process any contents of the newly created ME
		List<MUIElement> parts = container.getChildren();
		if (parts != null) {
			// loading a legacy app will add children to the window while it is
			// being rendered.
			// this is *not* the correct place for this
			// hope that the ADD event will pick up the new part.
			IPresentationEngine renderer = (IPresentationEngine) context
					.get(IPresentationEngine.class.getName());
			MUIElement[] plist = parts.toArray(new MUIElement[parts.size()]);
			for (int i = 0; i < plist.length; i++) {
				MUIElement childME = plist[i];
				renderer.createGui(childME);
			}
		}
	}

	public void setCSSInfo(MUIElement me, Object widget) {
		// Set up the CSS Styling parameters; id & class
		IEclipseContext ctxt = getContext(me);
		if (ctxt == null)
			ctxt = getContext(me);
		final IStylingEngine engine = (IStylingEngine) getContext(me).get(
				IStylingEngine.SERVICE_NAME);
		if (engine == null)
			return;

		// Put all the tags into the class string
		EObject eObj = (EObject) me;
		String cssClassStr = 'M' + eObj.eClass().getName();
		for (String tag : me.getTags())
			cssClassStr += ' ' + tag;

		// this will trigger style()
		String id = me.getElementId();
		if (id != null) {
			id = id.replace(".", "-"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		engine.setClassnameAndId(widget, cssClassStr, id);
	}

	public void bindWidget(MUIElement me, Object widget) {
		if (widget instanceof Widget) {
			((Widget) widget).setData(OWNING_ME, me);

			// Set up the CSS Styling parameters; id & class
			setCSSInfo(me, widget);

			// Ensure that disposed widgets are unbound form the model
			Widget swtWidget = (Widget) widget;
			swtWidget.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					MUIElement element = (MUIElement) e.widget
							.getData(OWNING_ME);
					if (element != null)
						unbindWidget(element);
				}
			});
		}

		// Create a bi-directional link between the widget and the model
		me.setWidget(widget);
	}

	public Object unbindWidget(MUIElement me) {
		Widget widget = (Widget) me.getWidget();
		if (widget != null) {
			me.setWidget(null);
			if (!widget.isDisposed())
				widget.setData(OWNING_ME, null);
		}

		// Clear the factory reference
		me.setRenderer(null);

		return widget;
	}

	protected Widget getParentWidget(MUIElement element) {
		return (Widget) element.getParent().getWidget();
	}

	public void disposeWidget(MUIElement element) {

		if (element.getWidget() instanceof Widget) {
			Widget curWidget = (Widget) element.getWidget();

			if (curWidget != null && !curWidget.isDisposed()) {
				unbindWidget(element);
				try {
					curWidget.dispose();
				} catch (Exception e) {
					Logger logService = context.get(Logger.class);
					if (logService != null) {
						String msg = "Error disposing widget for : " + element.getClass().getName(); //$NON-NLS-1$
						if (element instanceof MUILabel) {
							msg += ' ' + ((MUILabel) element)
									.getLocalizedLabel();
						}
						logService.error(e, msg);
					}
				}
			}
		}
		element.setWidget(null);
	}

	public void hookControllerLogic(final MUIElement me) {
		Object widget = me.getWidget();

		// add an accessibility listener (not sure if this is in the wrong place
		// (factory?)
		if (widget instanceof Control && me instanceof MUILabel) {
			((Control) widget).getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						public void getName(AccessibleEvent e) {
							e.result = ((MUILabel) me).getLocalizedLabel();
						}
					});
		}
	}

	protected Image getImage(MUILabel element) {
		IEclipseContext localContext = context;
		String iconURI = element.getIconURI();
		if (iconURI != null && iconURI.length() > 0) {
			Image image = imageMap.get(iconURI);
			if (image == null) {
				ISWTResourceUtilities resUtils = (ISWTResourceUtilities) localContext
						.get(IResourceUtilities.class.getName());
				image = resUtils.imageDescriptorFromURI(URI.createURI(iconURI))
						.createImage();
				imageMap.put(iconURI, image);
			}
			return image;
		}
		return null;
	}

	/**
	 * Calculates the index of the element in terms of the other <b>rendered</b>
	 * elements. This is useful when 'inserting' elements in the middle of
	 * existing, rendered parents.
	 * 
	 * @param element
	 *            The element to get the index for
	 * @return The visible index or -1 if the element is not a child of the
	 *         parent
	 */
	protected int calcVisibleIndex(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();

		int curIndex = 0;
		for (MUIElement child : parent.getChildren()) {
			if (child == element) {
				return curIndex;
			}

			if (child.getWidget() != null)
				curIndex++;
		}
		return -1;
	}

	protected int calcIndex(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		return parent.getChildren().indexOf(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.renderers.AbstractPartRenderer#childRendered
	 * (org.eclipse.e4.ui.model.application.MElementContainer,
	 * org.eclipse.e4.ui.model.application.MUIElement)
	 */
	@Override
	public void childRendered(MElementContainer<MUIElement> parentElement,
			MUIElement element) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer#init(org
	 * .eclipse.e4.core.contexts.IEclipseContext)
	 */
	@Override
	public void init(IEclipseContext context) {
		super.init(context);
		Display.getCurrent().disposeExec(new Runnable() {
			public void run() {
				for (Image image : imageMap.values()) {
					image.dispose();
				}
			}
		});
	}

	@Override
	protected boolean requiresFocus(MPart element) {
		MUIElement focussed = getModelElement(Display.getDefault()
				.getFocusControl());
		if (focussed == null) {
			return true;
		}
		// we ignore menus
		do {
			if (focussed == element || focussed == element.getToolbar()) {
				return false;
			}
			focussed = focussed.getParent();
		} while (focussed != null);
		return true;
	}

	static protected MUIElement getModelElement(Control ctrl) {
		if (ctrl == null)
			return null;

		MUIElement element = (MUIElement) ctrl
				.getData(AbstractPartRenderer.OWNING_ME);
		if (element != null) {
			return element;
			// FIXME: DndUtil.getModelElement() has the following check;
			// do we need this?
			// if (modelService.getTopLevelWindowFor(element) == topLevelWindow)
			// {
			// return element;
			// }
			// return null;
		}

		return getModelElement(ctrl.getParent());
	}

}
