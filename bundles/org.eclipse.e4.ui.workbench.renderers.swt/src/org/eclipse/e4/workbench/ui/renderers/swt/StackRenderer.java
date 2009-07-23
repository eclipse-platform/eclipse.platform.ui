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
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.ApplicationPackage;
import org.eclipse.e4.ui.model.application.MItemPart;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MStack;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabFolder2Adapter;
import org.eclipse.e4.ui.widgets.CTabFolderEvent;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.widgets.ETabFolder;
import org.eclipse.e4.ui.widgets.ETabItem;
import org.eclipse.e4.workbench.ui.internal.IValueFunction;
import org.eclipse.e4.workbench.ui.renderers.AbstractPartRenderer;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class StackRenderer extends LazyStackRenderer {

	Image viewMenuImage;

	@In
	IStylingEngine stylingEngine;

	public StackRenderer() {
		super();
	}

	public Object createWidget(MPart<?> part, Object parent) {
		Widget newWidget = null;

		if (!(part instanceof MStack) || !(parent instanceof Composite))
			return null;

		Widget parentWidget = (Widget) parent;
		if (parentWidget instanceof Composite) {

			// HACK!! Set up the close button style based on the 'Policy'
			// Perhaps this should be CSS-based ?
			// TODO: Yes, see bug #279854

			boolean showCloseAlways = false;
			int styleModifier = 0;
			if (part.getPolicy() != null && part.getPolicy().length() > 0) {
				String policy = part.getPolicy();
				if (policy.indexOf("ViewStack") >= 0) { //$NON-NLS-1$
					styleModifier = SWT.CLOSE;
				}
				if (policy.indexOf("EditorStack") >= 0) { //$NON-NLS-1$
					styleModifier = SWT.CLOSE;
					showCloseAlways = true;
				}
			}

			Composite stylingWrapper = createWrapperForStyling(
					(Composite) parentWidget, part.getContext());

			// TODO see bug #267434, SWT.BORDER should be determined from CSS
			// TODO see bug #282901 - [UI] Need better support for switching
			// renderer to use

			final CTabFolder ctf = new ETabFolder(stylingWrapper, SWT.BORDER
					| styleModifier);

			configureForStyling(ctf);

			ctf.setUnselectedCloseVisible(showCloseAlways);

			bindWidget(part, ctf);
			ctf.setVisible(true);

			newWidget = ctf;

			final IEclipseContext folderContext = part.getContext();
			folderContext.set(IContextConstants.DEBUG_STRING, "TabFolder"); //$NON-NLS-1$

			folderContext.set("canCloseFunc", new IValueFunction() { //$NON-NLS-1$
						public Object getValue() {
							return true;
						}
					});
			final IEclipseContext toplevelContext = getToplevelContext(part);
			folderContext.runAndTrack(new Runnable() {
				public void run() {
					// this will cause the tracker to be removed because no
					// context values are accessed
					if (ctf.isDisposed())
						return;
					IEclipseContext currentActive = toplevelContext;
					IEclipseContext child;
					while (currentActive != folderContext
							&& (child = (IEclipseContext) currentActive
									.get("activeChild")) != null && child != currentActive) { //$NON-NLS-1$
						currentActive = child;
					}
					// System.out.println(cti.getText() + " is now " + ((currentActive == tabItemContext) ? "active" : "inactive"));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

					String cssClassName = (currentActive == folderContext) ? "active" //$NON-NLS-1$
							: "inactive"; //$NON-NLS-1$
					stylingEngine.setClassname(ctf, cssClassName);

					// TODO HACK Bug 283073 [CSS] CTabFolder.getTopRight()
					// should get same background color
					if (ctf.getTopRight() != null)
						stylingEngine.setClassname(ctf.getTopRight(),
								cssClassName);

					// TODO HACK: see Bug 283585 [CSS] Specificity fails with
					// descendents
					CTabItem[] items = ctf.getItems();
					for (int i = 0; i < items.length; i++) {
						stylingEngine.setClassname(items[i], cssClassName);
					}
				}
			});
		}

		return newWidget;
	}

	public void postProcess(MPart<?> part) {
		super.postProcess(part);

		if (!(part instanceof MStack))
			return;

		CTabFolder ctf = (CTabFolder) part.getWidget();
		MPart<?> selPart = ((MStack) part).getActiveChild();
		if (selPart == null)
			return;

		// Find the tab associated with the part and set it as the selection
		CTabItem selItem = findItemForPart(part, selPart);
		if (selItem != null) {
			ctf.setSelection(selItem);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.ui.renderers.swt.LazyStackFactory#internalChildAdded
	 * (org.eclipse.e4.ui.model.application.MPart,
	 * org.eclipse.e4.ui.model.application.MPart)
	 */
	@Override
	protected void internalChildAdded(MPart parentElement, MPart element) {
		// TODO Auto-generated method stub
		super.internalChildAdded(parentElement, element);

		MItemPart<?> itemPart = (MItemPart<?>) element;
		CTabFolder ctf = (CTabFolder) parentElement.getWidget();
		int createFlags = 0;

		// if(element instanceof View && ((View)element).isCloseable())
		// createFlags = createFlags | SWT.CLOSE;

		CTabItem cti = findItemForPart(parentElement, element);
		if (cti == null) {
			int index = calcIndexFor(element);
			// TODO see bug 282901 - [UI] Need better support for switching
			// renderer to use
			cti = new ETabItem((ETabFolder) ctf, createFlags, index);

			// TODO HACK: see Bug 283585 [CSS] Specificity fails with
			// descendents
			String cssClassName = (String) ctf
					.getData("org.eclipse.e4.ui.css.CssClassName"); //$NON-NLS-1$
			stylingEngine.setClassname(cti, cssClassName);
		}

		cti.setData(OWNING_ME, element);
		cti.setText(itemPart.getName());
		cti.setImage(getImage(element));
		Control widget = (Control) element.getWidget();
		if (widget != null && widget.getParent() != cti.getParent()) {
			widget.setParent(cti.getParent());
		}
		cti.setControl(widget);

		// Hook up special logic to synch up the Tab Items
		hookTabControllerLogic(parentElement, element, cti);
	}

	private int calcIndexFor(final MPart element) {
		int index = 0;

		// Find the -visible- part before this element
		EList<MPart> kids = element.getParent().getChildren();
		for (Iterator iterator = kids.iterator(); iterator.hasNext();) {
			MPart mPart = (MPart) iterator.next();
			if (mPart == element)
				return index;
			if (mPart.isVisible())
				index++;
		}
		return index;
	}

	@Override
	public void childAdded(final MPart<?> parentElement, MPart<?> element) {
		super.childAdded(parentElement, element);

		if (element instanceof MItemPart<?>) {
			internalChildAdded(parentElement, element);

			// Lazy Loading: On the first pass through this method the
			// part's control will be null (we're just creating the tabs
			Control ctrl = (Control) element.getWidget();
			if (ctrl != null) {
				showTab((MItemPart<?>) element);
				stylingEngine.style(ctrl);
			}
		}
	}

	private CTabItem findItemForPart(MPart<?> folder, MPart<?> part) {
		CTabFolder ctf = (CTabFolder) folder.getWidget();
		if (ctf == null)
			return null;

		CTabItem[] items = ctf.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData(OWNING_ME) == part)
				return items[i];
		}
		return null;
	}

	private void hookTabControllerLogic(final MPart<?> parentElement,
			final MPart<?> childElement, final CTabItem cti) {
		// Handle label changes
		IObservableValue textObs = EMFObservables
				.observeValue((EObject) childElement,
						ApplicationPackage.Literals.MITEM__NAME);
		ISWTObservableValue uiObs = SWTObservables.observeText(cti);
		dbc.bindValue(uiObs, textObs, null, null);

		// Observe tooltip changes
		IObservableValue emfTTipObs = EMFObservables.observeValue(
				(EObject) childElement,
				ApplicationPackage.Literals.MITEM__TOOLTIP);
		IObservableValue uiTTipObs = new AbstractObservableValue() {

			public Object getValueType() {
				return String.class;
			}

			protected Object doGetValue() {
				return cti.getToolTipText();
			}

			protected void doSetValue(Object val) {
				cti.setToolTipText((String) val);
			}
		};
		dbc.bindValue(uiTTipObs, emfTTipObs, null, null);

		// Handle tab item image changes
		((EObject) childElement).eAdapters().add(new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				MPart<?> sm = (MPart<?>) msg.getNotifier();
				if (ApplicationPackage.Literals.MITEM__ICON_URI.equals(msg
						.getFeature())
						&& !cti.isDisposed()) {
					Image image = getImage(sm);
					cti.setImage(image);
				}
			}
		});
	}

	@Override
	public void childRemoved(MPart<?> parentElement, MPart<?> child) {
		super.childRemoved(parentElement, child);

		CTabItem oldItem = findItemForPart(parentElement, child);
		if (oldItem != null) {
			oldItem.setControl(null); // prevent the widget from being disposed
			oldItem.dispose();
		}

		// 'auto-hide' empty stacks
		CTabFolder ctf = (CTabFolder) parentElement.getWidget();
		String policy = parentElement.getPolicy();
		boolean isEditorStack = policy != null
				&& policy.indexOf("EditorStack") >= 0; //$NON-NLS-1$
		if (ctf.getItemCount() == 0 && !isEditorStack) {
			parentElement.setVisible(false);
		}

		// Auto-remove 'editor stack' entries on close
		if (isEditorStack) {
			parentElement.getChildren().remove(child);
		}
	}

	@Override
	public void hookControllerLogic(final MPart<?> me) {
		super.hookControllerLogic(me);

		final MStack sm = (MStack) me;
		// Match the selected TabItem to its Part
		CTabFolder ctf = (CTabFolder) me.getWidget();
		ctf.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				MItemPart<?> newPart = (MItemPart<?>) e.item.getData(OWNING_ME);
				if (sm.getActiveChild() != newPart) {
					activate(newPart);
				}

				showTab(newPart);
			}
		});

		CTabFolder2Adapter closeListener = new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				MPart part = (MPart) event.item
						.getData(AbstractPartRenderer.OWNING_ME);

				// Allow closes to be 'canceled'
				IEclipseContext partContext = part.getContext();
				IValueFunction closeFunc = (IValueFunction) partContext
						.get("canCloseFunc"); //$NON-NLS-1$
				boolean canClose = closeFunc == null
						|| (Boolean) closeFunc.getValue();
				if (!canClose) {
					event.doit = false;
					return;
				}
				part.setVisible(false);
			}
		};
		ctf.addCTabFolder2Listener(closeListener);

		// Detect activation...picks up cases where the user clicks on the
		// (already active) tab
		ctf.addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				CTabFolder ctf = (CTabFolder) event.widget;
				MStack stack = (MStack) ctf.getData(OWNING_ME);
				MItemPart<?> part = stack.getActiveChild();
				if (part != null)
					activate(part);
			}
		});

		((EObject) me).eAdapters().add(0, new AdapterImpl() {
			@Override
			public void notifyChanged(Notification msg) {
				if (ApplicationPackage.Literals.MPART__ACTIVE_CHILD.equals(msg
						.getFeature())) {
					MStack sm = (MStack) msg.getNotifier();
					MPart<?> selPart = sm.getActiveChild();
					CTabFolder ctf = (CTabFolder) ((MStack) msg.getNotifier())
							.getWidget();
					CTabItem item = findItemForPart(sm, selPart);
					if (item != null) {
						if (ctf.getSelection() != item)
							ctf.setSelection(item);
					}
				}
			}
		});
	}

	private void showTab(MItemPart<?> part) {
		CTabFolder ctf = (CTabFolder) getParentWidget(part);
		Control ctrl = (Control) part.getWidget();
		CTabItem cti = findItemForPart(part.getParent(), part);
		cti.setControl(ctrl);

		ToolBar tb = getToolbar(part);
		if (tb != null) {
			Control curTR = ctf.getTopRight();
			if (curTR != null)
				curTR.dispose();

			if (tb.getSize().y > ctf.getTabHeight())
				ctf.setTabHeight(tb.getSize().y);

			// TODO HACK: see Bug 283073 [CSS] CTabFolder.getTopRight() should
			// get same background color
			String cssClassName = (String) ctf
					.getData("org.eclipse.e4.ui.css.CssClassName"); //$NON-NLS-1$
			stylingEngine.setClassname(tb, cssClassName);

			ctf.setTopRight(tb, SWT.RIGHT);
			ctf.layout(true);

			// TBD In 3.x views listening on the "parent" get an intermediary
			// composite parented of the CTabFolder, but in E4 they get
			// the CTabFolder itself.
			// The layout() call above generates resize messages for children,
			// but not for the CTabFlder itself. Hence, children listening for
			// this message on the parent don't receive notifications in E4.
			// For now, send an explicit Resize message to the CTabFolder
			// listeners.
			// The enhancement request 279263 suggests a more general solution.
			ctf.notifyListeners(SWT.Resize, null);
		}

	}

	private ToolBar getToolbar(MItemPart<?> part) {
		if (part.getToolBar() == null && part.getMenu() == null)
			return null;
		CTabFolder ctf = (CTabFolder) getParentWidget(part);

		ToolBar tb;
		MToolBar tbModel = part.getToolBar();
		if (tbModel != null) {
			tb = (ToolBar) createToolBar(part.getParent(), ctf, tbModel);
		} else {
			tb = new ToolBar(ctf, SWT.FLAT | SWT.HORIZONTAL);
		}

		// View menu (if any)
		if (part.getMenu() != null) {
			addMenuButton(part, tb, part.getMenu());
		}

		tb.pack();
		return tb;
	}

	/**
	 * @param tb
	 */
	private void addMenuButton(MItemPart<?> part, ToolBar tb, MMenu menu) {
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
		MMenu menuModel = (MMenu) item.getData("theMenu"); //$NON-NLS-1$
		MItemPart<?> part = (MItemPart<?>) item.getData("thePart"); //$NON-NLS-1$
		Menu menu = (Menu) createMenu(part, item, menuModel);

		// EList<MMenuItem> items = menuModel.getItems();
		// for (Iterator iterator = items.iterator(); iterator.hasNext();) {
		// MMenuItem mToolBarItem = (MMenuItem) iterator.next();
		// MenuItem mi = new MenuItem(menu, SWT.PUSH);
		// mi.setText(mToolBarItem.getName());
		// mi.setImage(getImage(mToolBarItem));
		// }
		Rectangle ib = item.getBounds();
		Point displayAt = item.getParent().toDisplay(ib.x, ib.y + ib.height);
		menu.setLocation(displayAt);
		menu.setVisible(true);

		Display display = Display.getCurrent();
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		menu.dispose();
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

			viewMenuImage = new Image(d, viewMenu.getImageData(), viewMenuMask
					.getImageData());
			viewMenu.dispose();
			viewMenuMask.dispose();
		}
		return viewMenuImage;
	}
}
