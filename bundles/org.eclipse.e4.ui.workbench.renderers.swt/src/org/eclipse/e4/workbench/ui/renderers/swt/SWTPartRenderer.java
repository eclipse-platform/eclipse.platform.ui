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
package org.eclipse.e4.workbench.ui.renderers.swt;

import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MHandledItem;
import org.eclipse.e4.ui.model.application.MItem;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MToolBarItem;
import org.eclipse.e4.ui.model.workbench.MMenuItemRenderer;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtiltities;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.renderers.AbstractPartRenderer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;

public abstract class SWTPartRenderer extends AbstractPartRenderer {

	private static Shell limbo;

	public Object createMenu(MPart<?> part, Object widgetObject, MMenu menu) {
		Widget widget = (Widget) widgetObject;
		Menu swtMenu;

		MenuManager manager = new MenuManager();

		if (widget instanceof Decorations) {
			swtMenu = manager.createMenuBar((Decorations) widgetObject);
			swtMenu.setData(manager);
			((Decorations) widget).setMenuBar(swtMenu);
		} else if (widget instanceof Control) {
			swtMenu = manager.createContextMenu((Control) widget);
			// ((Control) widget).setMenu(swtMenu);
		} else {
			throw new IllegalArgumentException(
					"The widget must be MenuItem, Decorations, or Control but is: " //$NON-NLS-1$
							+ widgetObject);
		}
		swtMenu.setData(AbstractPartRenderer.OWNING_ME, menu);
		for (MMenuItem menuItem : menu.getItems()) {
			createMenuItem(part, manager, menuItem);
		}

		manager.update(true);
		return swtMenu;
	}

	public Object createToolBar(MPart<?> part, Object widgetObject,
			MToolBar toolbar) {

		int orientation = SWT.HORIZONTAL;
		Composite composite = null;
		while (!(widgetObject instanceof Composite)
				&& widgetObject instanceof Control) {
			widgetObject = ((Control) widgetObject).getParent();
		}
		if (!(widgetObject instanceof Composite)) {
			return null;
		}
		composite = (Composite) widgetObject;
		if (composite.getLayout() instanceof RowLayout) {
			RowLayout rl = (RowLayout) composite.getLayout();
			orientation = rl.type;
		}

		ToolBarManager manager = new ToolBarManager(SWT.FLAT | orientation);
		ToolBar swtToolBar = manager.createControl(composite);
		swtToolBar.setData(AbstractPartRenderer.OWNING_ME, toolbar);

		EList<MToolBarItem> items = toolbar.getItems();
		if (items != null && items.size() > 0) {
			for (MToolBarItem toolBarItem : toolbar.getItems()) {
				createToolBarItem(part, manager, toolBarItem);
			}
		}

		manager.update(true);
		return swtToolBar;
	}

	private void createToolBarItem(MPart<?> part, ToolBarManager manager,
			final MToolBarItem toolBarItem) {
		manager.add(new HandledContributionItem(toolBarItem, context));
	}

	private void createMenuItem(MPart<?> part, final MenuManager manager,
			final MHandledItem handledItem) {
		if (handledItem instanceof MMenuItemRenderer) {
			final IContributionItem renderer = (IContributionItem) ((MMenuItemRenderer) handledItem)
					.getRenderer();
			manager.add(renderer);
			return;
		}

		if (handledItem instanceof MMenuItem) {
			final MMenuItem mItem = (MMenuItem) handledItem;
			final String id = mItem.getId();
			if (mItem.isSeparator()) {
				if (!mItem.isVisible()) {
					if (id != null) {
						manager.add(new GroupMarker(id));
					}
				} else {
					if (id == null) {
						manager.add(new Separator());
					} else {
						manager.add(new Separator(id));
					}
				}
				return;
			} else if (mItem.getMenu() != null) {
				MenuManager item = new MenuManager(mItem.getName(), id);
				manager.add(item);
				for (MMenuItem menuItem : mItem.getMenu().getItems()) {
					createMenuItem(part, item, menuItem);
				}
				return;
			}
		}

		manager.add(new HandledContributionItem(handledItem, context));
	}

	public <P extends MPart<?>> void processContents(MPart<P> me) {
		Widget parentWidget = (Widget) me.getWidget();
		if (parentWidget == null)
			return;

		// Process any contents of the newly created ME
		List<P> parts = me.getChildren();
		if (parts != null) {
			for (Iterator<P> childIter = parts.iterator(); childIter.hasNext();) {
				MPart<?> childME = childIter.next();
				renderer.createGui(childME);
			}
		}
	}

	public void bindWidget(MPart<?> me, Object widget) {
		me.setWidget(widget);
		((Widget) widget).setData(OWNING_ME, me);
		final IStylingEngine engine = (IStylingEngine) me.getContext().get(
				IStylingEngine.SERVICE_NAME);
		engine.setId(widget, me.getId()); // also triggers style()
	}

	public Object unbindWidget(MPart<?> me) {
		Widget widget = (Widget) me.getWidget();
		if (widget != null) {
			me.setWidget(null);
			widget.setData(OWNING_ME, null);
		}

		// Clear the factory reference
		me.setOwner(null);

		return widget;
	}

	protected Widget getParentWidget(MPart<?> element) {
		return (element.getParent() instanceof MPart) ? (Widget) ((MPart<?>) (element
				.getParent())).getWidget()
				: null;
	}

	public void disposeWidget(MPart<?> part) {
		Widget curWidget = (Widget) part.getWidget();

		// If we're disposing a control find its 'outermost'
		if (curWidget instanceof Control)
			curWidget = getOutermost((Control) curWidget);

		if (curWidget != null && !curWidget.isDisposed()) {
			unbindWidget(part);
			curWidget.dispose();
		}
	}

	public void hookControllerLogic(final MPart<?> me) {
		Widget widget = (Widget) me.getWidget();

		// Clean up if the widget is disposed
		widget.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				MPart<?> model = (MPart<?>) e.widget.getData(OWNING_ME);
				if (model != null)
					model.setWidget(null);
			}
		});

		// add an accessibility listener (not sure if this is in the wrong place
		// (factory?)
		if (widget instanceof Control && me instanceof MItemPart<?>) {
			((Control) widget).getAccessible().addAccessibleListener(
					new AccessibleAdapter() {
						public void getName(AccessibleEvent e) {
							e.result = ((MItemPart<?>) me).getName();
						}
					});
		}
	}

	protected Image getImage(MApplicationElement element) {
		if (element instanceof MItem) {
			IEclipseContext localContext = context;
			if (element instanceof MPart<?>) {
				localContext = getContext((MPart<?>) element);
			}
			String iconURI = ((MItem) element).getIconURI();
			if (iconURI != null && !iconURI.equals("null")) { //$NON-NLS-1$
				ISWTResourceUtiltities resUtils = (ISWTResourceUtiltities) localContext
						.get(IResourceUtiltities.class.getName());
				ImageDescriptor desc = resUtils.imageDescriptorFromURI(URI
						.createURI(iconURI));
				if (desc != null)
					return desc.createImage();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#childAdded(org.eclipse
	 * .e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void childAdded(MPart<?> parentElement, MPart<?> element) {
		if (parentElement != null && element != null)
			return;
		// if the new part already has a Control then re-parent it under the
		// element
		if (element.getWidget() instanceof Control
				&& parentElement.getWidget() instanceof Composite) {
			Composite parentComp = (Composite) parentElement.getWidget();
			Control ctrl = (Control) element.getWidget();
			locallyShow(parentComp, ctrl);
		} else if (element.isVisible()) {
			// Ensure the widget for the element exists
			renderer.createGui(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.PartFactory#childRemoved(org.eclipse
	 * .e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	public void childRemoved(MPart<?> parentElement, MPart<?> child) {
		super.childRemoved(parentElement, child);

		if (child.getWidget() instanceof Control) {
			Control ctrl = (Control) child.getWidget();
			locallyHide(ctrl);
		}
	}

	private Control getOutermost(Control ctrl) {
		// Find the 'outermost' Composite that is *not* bound
		// to a model element
		Composite curComposite = ctrl.getParent();
		Control outerMost = ctrl;
		while (curComposite != null
				&& curComposite.getData(AbstractPartRenderer.OWNING_ME) == null
				&& !(curComposite instanceof Shell)) {
			outerMost = curComposite;
			curComposite = curComposite.getParent();
		}

		return outerMost;
	}

	/**
	 * Restore an existing control to the UI by reparenting it (from 'limbo')
	 * under its new owner
	 * 
	 * @param parent
	 *            The composite to place the control under
	 * @param ctrl
	 *            The control to be restored
	 */
	private void locallyShow(Composite parent, Control ctrl) {
		// Find the 'outermost' Composite that is *not* bound
		// to a model element
		Control toReparent = getOutermost(ctrl);

		// Prevent No-ops
		if (toReparent.getParent() != parent) {
			toReparent.setParent(parent);
			parent.layout(true);
		}
	}

	/**
	 * If a model element containing a widget is removed from its parent we have
	 * to remove the control immediately from its parent's structure (to get the
	 * layout correct) so we'll place it in 'limbo' (an invisible shell).
	 * 
	 * @param ctrl
	 */
	private void locallyHide(Control ctrl) {
		if (limbo == null) {
			limbo = new Shell(ctrl.getShell(), SWT.NONE);
			limbo.setVisible(false);
		}

		// Find the 'outermost' Composite that is *not* bound
		// to a model element
		Control toReparent = getOutermost(ctrl);

		Composite curParent = toReparent.getParent();
		toReparent.setParent(limbo);
		curParent.layout(true);
	}

	/*
	 * HACK: Create a wrapper composite with appropriate layout for the purpose
	 * of styling margins. See bug #280632
	 */
	protected Composite createWrapperForStyling(Composite parentWidget,
			IEclipseContext context) {
		Composite layoutHolder = new Composite(parentWidget, SWT.NONE);
		addLayoutForStyling(layoutHolder);
		layoutHolder.setData("org.eclipse.e4.ui.css.swt.marginWrapper", true); //$NON-NLS-1$
		final IStylingEngine engine = (IStylingEngine) context
				.get(IStylingEngine.SERVICE_NAME);
		engine.setClassname(layoutHolder, "marginWrapper"); //$NON-NLS-1$
		return layoutHolder;
	}

	/*
	 * HACK: Add layout information to the composite for the purpose of styling
	 * margins. See bug #280632
	 */
	protected void addLayoutForStyling(Composite composite) {
		GridLayout gl = new GridLayout(1, true);
		composite.setLayout(gl);
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
	}

	/*
	 * HACK: Prep the control with layout information for the purpose of styling
	 * margins. See bug #280632
	 */
	protected void configureForStyling(Control control) {
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
}
