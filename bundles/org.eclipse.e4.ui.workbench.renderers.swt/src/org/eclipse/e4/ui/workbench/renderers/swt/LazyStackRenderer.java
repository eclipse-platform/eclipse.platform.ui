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

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * This class encapsulates the functionality necessary to manage stacks of parts
 * in a 'lazy loading' manner. For these stacks only the currently 'active'
 * child <b>most</b> be rendered so in this class we over ride that default
 * behavior for processing the stack's contents to prevent all of the contents
 * from being rendered, calling 'childAdded' instead. This not only saves time
 * and SWT resources but is necessary in an IDE world where we must not
 * arbitrarily cause plug-in loading.
 * 
 */
public abstract class LazyStackRenderer extends SWTPartRenderer {
	public static final String TAG_VIEW_MENU = "ViewMenu"; //$NON-NLS-1$
	static final String TOOL_BAR_MANAGER_RENDERER_VIEW_MENU = "StackRenderer.viewMenu"; //$NON-NLS-1$
	static final String TOOL_BAR_INTERMEDIATE = "StackRenderer.intermediate"; //$NON-NLS-1$

	private EventHandler lazyLoader = new EventHandler() {
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(element instanceof MGenericStack<?>))
				return;

			MGenericStack<MUIElement> stack = (MGenericStack<MUIElement>) element;
			if (stack.getRenderer() != LazyStackRenderer.this)
				return;
			LazyStackRenderer lsr = (LazyStackRenderer) stack.getRenderer();

			// Gather up the elements that are being 'hidden' by this change
			MUIElement oldSel = (MUIElement) event
					.getProperty(UIEvents.EventTags.OLD_VALUE);
			if (oldSel != null) {
				List<MUIElement> goingHidden = new ArrayList<MUIElement>();
				hideElementRecursive(oldSel, goingHidden);
			}

			if (stack.getSelectedElement() != null)
				lsr.showTab(stack.getSelectedElement());
		}
	};
	Image viewMenuImage;

	@Inject
	IPresentationEngine renderer;

	public LazyStackRenderer() {
		super();
	}

	public void init(IEventBroker eventBroker) {
		// Ensure that there only ever *one* listener. Each subclass
		// will call this method
		eventBroker.unsubscribe(lazyLoader);

		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), lazyLoader);
	}

	/**
	 * @param eventBroker
	 */
	public void contextDisposed(IEventBroker eventBroker) {
		eventBroker.unsubscribe(lazyLoader);
	}

	public void postProcess(MUIElement element) {
		if (!(element instanceof MGenericStack<?>))
			return;

		MGenericStack<MUIElement> stack = (MGenericStack<MUIElement>) element;
		MUIElement selPart = stack.getSelectedElement();
		if (selPart != null) {
			showTab(selPart);
		} else if (stack.getChildren().size() > 0) {
			// Set the selection to the first renderable element
			for (MUIElement kid : stack.getChildren()) {
				if (kid.isToBeRendered() && kid.isVisible()) {
					stack.setSelectedElement(kid);
					break;
				}
			}
		}
	}

	@Override
	public void processContents(MElementContainer<MUIElement> me) {
		// Lazy Loading: here we only process the contents through childAdded,
		// we specifically do not render them
		for (MUIElement part : me.getChildren()) {
			if (part.isToBeRendered() && part.isVisible())
				createTab(me, part);
		}
	}

	/**
	 * This method is necessary to allow the parent container to show affordance
	 * (i.e. tabs) for child elements -without- creating the actual part
	 * 
	 * @param me
	 *            The parent model element
	 * @param part
	 *            The child to show the affordance for
	 */
	protected void createTab(MElementContainer<MUIElement> me, MUIElement part) {
	}

	protected void showTab(MUIElement element) {
		// Now process any newly visible elements
		List<MUIElement> becomingVisible = new ArrayList<MUIElement>();
		MUIElement curSel = element.getParent().getSelectedElement();
		if (curSel != null) {
			showElementRecursive(curSel, becomingVisible);
		}
	}

	private void hideElementRecursive(MUIElement element,
			List<MUIElement> goingHidden) {
		if (element == null || element.getWidget() == null)
			return;

		if (element instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) element;
			MUIElement ref = ph.getRef();
			if (ref instanceof MPart && ph.getParent() != null
					&& ph.getParent().getWidget() instanceof CTabFolder) {
				// Reparent the existing Toolbar
				// MPart part = (MPart) ref;
				CTabFolder ctf = (CTabFolder) ph.getParent().getWidget();
				// IPresentationEngine renderer = part.getContext().get(
				// IPresentationEngine.class);

				// Dispose the existing toolbar
				if (ctf.getTopRight() != null) {
					Control curTB = ctf.getTopRight();
					ctf.setTopRight(null);
					if (!curTB.isDisposed()) {
						MUIElement tbME = (MUIElement) curTB
								.getData(AbstractPartRenderer.OWNING_ME);
						if (tbME instanceof MToolBar) {
							// renderer.removeGui(tbME);
							curTB.setVisible(false);
							curTB.moveBelow(null);
						} else
							curTB.dispose();
					}
				}
			}

			element = ((MPlaceholder) element).getRef();
		}

		// Hide any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			element.setVisible(false);
		}

		goingHidden.add(element);

		if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being hidden
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			hideElementRecursive(curSel, goingHidden);
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			for (MUIElement childElement : container.getChildren()) {
				hideElementRecursive(childElement, goingHidden);
			}

			// OK, now process detached windows
			if (element instanceof MWindow) {
				for (MWindow w : ((MWindow) element).getWindows()) {
					hideElementRecursive(w, goingHidden);
				}
			} else if (element instanceof MPerspective) {
				for (MWindow w : ((MPerspective) element).getWindows()) {
					hideElementRecursive(w, goingHidden);
				}
			}
		}
	}

	private void showElementRecursive(MUIElement element,
			List<MUIElement> becomingVisible) {
		if (!element.isToBeRendered())
			return;

		if (element instanceof MPlaceholder && element.getWidget() != null) {
			MPlaceholder ph = (MPlaceholder) element;
			MUIElement ref = ph.getRef();
			ref.setCurSharedRef(ph);

			Composite phComp = (Composite) ph.getWidget();
			Control refCtrl = (Control) ph.getRef().getWidget();
			refCtrl.setParent(phComp);
			phComp.layout(new Control[] { refCtrl }, SWT.DEFER);

			if (ref instanceof MPart
					&& ph.getParent().getWidget() instanceof CTabFolder) {
				// Reparent the existing Toolbar
				MPart part = (MPart) ref;
				CTabFolder ctf = (CTabFolder) ph.getParent().getWidget();
				// IPresentationEngine renderer = modelService
				// .getContainingContext(ph)
				// .get(IPresentationEngine.class);

				MToolBar tbModel = part.getToolbar();

				// Dispose the existing toolbar
				if (ctf.getTopRight() != null) {
					Control curTB = ctf.getTopRight();
					ctf.setTopRight(null);
					if (!curTB.isDisposed()) {
						MUIElement tbME = (MUIElement) curTB
								.getData(AbstractPartRenderer.OWNING_ME);
						if (tbME instanceof MToolBar) {
							// tbME.setVisible(false);
							curTB.setVisible(false);
							curTB.moveBelow(null);
						} else
							curTB.dispose();
					}
				}

				if (tbModel != null) {
					tbModel.setVisible(true);
					MMenu viewMenu = getViewMenu(part);
					final Composite intermediate = createIntermediate(part,
							tbModel, viewMenu, ctf);

					if (intermediate != null && !intermediate.isDisposed()) {
						intermediate.setVisible(true);
						intermediate.moveAbove(null);
						intermediate.pack();
						ctf.setTopRight(intermediate, SWT.RIGHT | SWT.WRAP);
						ctf.layout();
					}
				}
			}

			element = ref;
		}

		if (element instanceof MContext) {
			IEclipseContext context = ((MContext) element).getContext();
			if (context != null) {
				IEclipseContext newParentContext = modelService
						.getContainingContext(element);
				if (context.getParent() != newParentContext) {
					//					System.out.println("Update Context: " + context.toString() //$NON-NLS-1$
					//							+ " new parent: " + newParentContext.toString()); //$NON-NLS-1$
					context.setParent(newParentContext);
				}
			}
		}

		// Show any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			int visCount = 0;
			for (MUIElement kid : ((MWindow) element).getChildren()) {
				if (kid.isToBeRendered() && kid.isVisible())
					visCount++;
			}
			if (visCount > 0)
				element.setVisible(true);
		}

		becomingVisible.add(element);

		if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being visible
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			if (curSel == null && container.getChildren().size() > 0)
				curSel = container.getChildren().get(0);
			if (curSel != null)
				showElementRecursive(curSel, becomingVisible);
		} else if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			List<MUIElement> kids = new ArrayList<MUIElement>(
					container.getChildren());
			for (MUIElement childElement : kids) {
				showElementRecursive(childElement, becomingVisible);
			}

			// OK, now process detached windows
			if (element instanceof MWindow) {
				for (MWindow w : ((MWindow) element).getWindows()) {
					showElementRecursive(w, becomingVisible);
				}
			} else if (element instanceof MPerspective) {
				for (MWindow w : ((MPerspective) element).getWindows()) {
					showElementRecursive(w, becomingVisible);
				}
			}
		}
	}

	protected Composite createIntermediate(MPart part, MUIElement tbModel,
			MMenu viewMenu, Composite parent) {
		Composite tbi = (Composite) tbModel.getTransientData().get(
				TOOL_BAR_INTERMEDIATE);
		if (tbi != null && !tbi.isDisposed()) {
			tbi.setParent(parent);
			return tbi;
		}
		final Composite intermediate = new Composite((Composite) parent,
				SWT.NONE);
		intermediate.setData(AbstractPartRenderer.OWNING_ME, tbModel);
		int orientation = SWT.HORIZONTAL;
		RowLayout layout = RowLayoutFactory.fillDefaults().wrap(false)
				.spacing(0).type(orientation).create();
		layout.marginLeft = 3;
		layout.center = true;
		intermediate.setLayout(layout);
		tbModel.getTransientData().put(TOOL_BAR_INTERMEDIATE, intermediate);
		Control c = (Control) renderer.createGui(tbModel, intermediate,
				part.getContext());
		if (c == null) {
			intermediate.dispose();
		} else {
			c.addListener(SWT.Dispose, new Listener() {
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					intermediate.dispose();
				}
			});
			if (viewMenu != null) {
				addMenuButton(part, intermediate, viewMenu);
			}
		}
		return intermediate;
	}

	protected MMenu getViewMenu(MPart part) {
		if (part.getMenus() == null) {
			return null;
		}
		for (MMenu menu : part.getMenus()) {
			if (menu.getTags().contains(TAG_VIEW_MENU) && menu.isToBeRendered()) {
				return menu;
			}
		}
		return null;
	}

	protected void addMenuButton(MPart part, Composite intermediate, MMenu menu) {
		ToolBar tb = new ToolBar(intermediate, SWT.FLAT | SWT.RIGHT);
		tb.setData(TOOL_BAR_MANAGER_RENDERER_VIEW_MENU);
		ToolItem ti = new ToolItem(tb, SWT.PUSH);
		ti.setImage(getViewMenuImage());
		ti.setHotImage(null);
		ti.setToolTipText("View Menu"); //$NON-NLS-1$
		ti.setData("theMenu", menu); //$NON-NLS-1$
		ti.setData("thePart", part); //$NON-NLS-1$

		ti.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showMenu((ToolItem) e.widget);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				showMenu((ToolItem) e.widget);
			}
		});
	}

	/**
	 * @param item
	 */
	protected void showMenu(ToolItem item) {
		// Create the UI for the menu
		final MMenu menuModel = (MMenu) item.getData("theMenu"); //$NON-NLS-1$
		Menu menu = null;
		Object obj = menuModel.getWidget();
		if (obj instanceof Menu) {
			menu = (Menu) obj;
		}
		if (menu == null || menu.isDisposed()) {
			MPart part = (MPart) item.getData("thePart"); //$NON-NLS-1$
			Control ctrl = (Control) part.getWidget();
			final Menu tmpMenu = (Menu) renderer.createGui(menuModel,
					ctrl.getShell(), part.getContext());
			menu = tmpMenu;
			if (tmpMenu != null) {
				ctrl.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (!tmpMenu.isDisposed()) {
							tmpMenu.dispose();
						}
					}
				});
			}
		}
		if (menu == null) {
			return;
		}

		// ...and Show it...
		Rectangle ib = item.getBounds();
		Point displayAt = item.getParent().toDisplay(ib.x, ib.y + ib.height);
		menu.setLocation(displayAt);
		menu.setVisible(true);

		Display display = Display.getCurrent();
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		if (menu.getData() instanceof MenuManager) {
			MenuManager manager = (MenuManager) menu.getData();
			manager.dispose();
		} else {
			menu.dispose();
		}
	}

	private Image getViewMenuImage() {
		if (viewMenuImage == null) {
			Display d = Display.getCurrent();

			Image viewMenu = new Image(d, 16, 16);
			Image viewMenuMask = new Image(d, 16, 16);

			Display display = Display.getCurrent();
			GC gc = new GC(viewMenu);
			GC maskgc = new GC(viewMenuMask);
			gc.setForeground(display
					.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
			gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

			int[] shapeArray = new int[] { 6, 1, 15, 1, 11, 5, 10, 5 };
			gc.fillPolygon(shapeArray);
			gc.drawPolygon(shapeArray);

			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			Color white = display.getSystemColor(SWT.COLOR_WHITE);

			maskgc.setBackground(black);
			maskgc.fillRectangle(0, 0, 16, 16);

			maskgc.setBackground(white);
			maskgc.setForeground(white);
			maskgc.fillPolygon(shapeArray);
			maskgc.drawPolygon(shapeArray);
			gc.dispose();
			maskgc.dispose();

			ImageData data = viewMenu.getImageData();
			data.transparentPixel = data.getPixel(0, 0);

			viewMenuImage = new Image(d, viewMenu.getImageData(),
					viewMenuMask.getImageData());
			viewMenu.dispose();
			viewMenuMask.dispose();
		}
		return viewMenuImage;
	}
}
