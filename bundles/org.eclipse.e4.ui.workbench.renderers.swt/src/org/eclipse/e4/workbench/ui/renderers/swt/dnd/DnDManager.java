/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt.dnd;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.workbench.ui.renderers.swt.dnd.DragAndDropUtil.CursorInfo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class DnDManager {
	DragAndDropUtil dndUtil;
	Overlay overlay;
	protected CursorInfo dndInfo;
	protected Point downPos;
	protected CursorInfo downInfo;

	private DragHost dragHost;
	// private MWindow baseWindow;
	private MUIElement curOverlayElement;
	private Display display;

	Listener keyListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.character == SWT.ESC && dragHost != null) {
				cancelDrag();
			}
		}
	};

	Listener mouseButtonListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.button != 1) {
				downPos = null;
				return;
			}

			CursorInfo info = dndUtil.getCursorInfo();
			if (event.type == SWT.MouseDown && dragHost == null
					&& (info.curElement instanceof MGenericStack<?>)
					&& (info.itemElement != null)) {
				downPos = new Point(event.x, event.y);
				downInfo = info;
			} else if (event.type == SWT.MouseUp) {
				downPos = null;
				downInfo = null;
				if (dragHost != null) {
					CursorInfo dropInfo = getDropInfo();
					if (dropInfo.curElement == null)
						cancelDrag();
					else
						endDrag(dropInfo);
				}
			}
		}
	};

	Listener mouseMoveListener = new Listener() {

		public void handleEvent(Event event) {
			if (dragHost != null) {
				CursorInfo dropInfo = getDropInfo();

				// Set the cursor
				if (dropInfo.curElement == null) {
					setCursor(display.getSystemCursor(SWT.CURSOR_NO));
				} else {
					setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
				}

				Point p = event.display.getCursorLocation();
				dragHost.setLocation(p.x, p.y);
				updateOverlay();
			}

			if (downPos != null) {
				Point curPos = new Point(event.x, event.y);
				int dx = Math.abs(downPos.x - curPos.x);
				int dy = Math.abs(downPos.y - curPos.y);
				if (dx > 8 || dy > 8) {
					downPos = null;
					startDrag(downInfo);
				}
			}
		}
	};
	private MWindow baseWindow;

	public DnDManager(MWindow window) {
		baseWindow = window;

		if (baseWindow.getWidget() instanceof Shell) {
			Shell ctrl = (Shell) baseWindow.getWidget();
			display = ctrl.getDisplay();
			enableDragging(true);

			dndUtil = new DragAndDropUtil(display);
			ctrl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (!display.isDisposed()) {
						enableDragging(false);
					}
				}
			});
		}
	}

	/**
	 * @return
	 */
	protected CursorInfo getDropInfo() {
		CursorInfo dropInfo = dndUtil.getCursorInfo();

		// Only drop TI's onto TB's
		MUIElement dragElement = dragHost.getDragElement();
		if (dragElement instanceof MToolItem
				&& !(dropInfo.curElement instanceof MToolBar)) {
			dropInfo.curElement = null; // No drop
		} else if (dragElement instanceof MPart) {
			// Only drop parts onto stacks for now
			MPartStack stack = null;
			if (dropInfo.curElement instanceof MPartStack)
				stack = (MPartStack) dropInfo.curElement;
			else if (dropInfo.curElement instanceof MPart) {
				MUIElement parent = dropInfo.curElement.getParent();
				if (parent instanceof MPartStack) {
					stack = (MPartStack) parent;
				}
			}
			dropInfo.curElement = stack;
		}

		return dropInfo;
	}

	protected void updateOverlay() {
		CursorInfo info = dndUtil.getCursorInfo();
		if (info.itemRect == null)
			overlay.clear();
		else if (info.itemElement != curOverlayElement) {
			overlay.clear();
			overlay.addOutline(info.itemRect, 2);
			curOverlayElement = info.itemElement;
		}

		// if (info.curElement != null) {
		// MGenericStack<?> stack = null;
		// if (info.curElement instanceof MPartStack)
		// stack = (MGenericStack<?>) info.curElement;
		// else {
		// MUIElement element = info.curElement.getParent();
		// while (element != null && !(element instanceof MPartStack))
		// element = element.getParent();
		// stack = (MGenericStack<?>) element;
		// }
		//
		// if (stack != null && stack != curOverlayStack) {
		// curOverlayStack = stack;
		// Control ctf = (Control) stack.getWidget();
		// Rectangle ctfBounds = ctf.getBounds();
		// ctfBounds = display.map(ctf.getParent(), ctf.getShell(),
		// ctfBounds);
		// overlay.addOutline(ctfBounds, 3);
		// }
		// }
	}

	protected void cancelDrag() {
		overlay.dispose();
		overlay = null;

		dragHost.cancel();
		dragHost = null;

		setCursor(null);
	}

	private void setCursor(Cursor cursor) {
		Shell shell = (Shell) baseWindow.getWidget();
		shell.setCursor(cursor);
	}

	protected void endDrag(CursorInfo dndInfo) {
		if (overlay != null) {
			overlay.dispose();
			overlay = null;
		}

		if (dragHost == null)
			return;

		if (dndInfo.curElement == null) {
			MWindow theWindow = dragHost.getModel();
			theWindow.setToBeRendered(false);
			theWindow.getTags().remove(DragHost.DragHostId);
			theWindow.setToBeRendered(true);
		} else {
			if (!(dndInfo.curElement instanceof MElementContainer<?>))
				dndInfo.curElement = dndInfo.curElement.getParent();

			dragHost.drop((MElementContainer<MUIElement>) dndInfo.curElement,
					dndInfo.itemIndex);
		}

		dragHost = null;
		setCursor(null);
	}

	protected void startDrag(CursorInfo dndInfo) {
		MUIElement dragElement = dndInfo.curElement;
		if (dndInfo.itemElement != null) {
			dragElement = dndInfo.itemElement;
		}

		if (dragElement != null) {
			overlay = new Overlay((Shell) baseWindow.getWidget());
			dragHost = new DragHost(dragElement);
		}
	}

	public void enableDragging(boolean enable) {
		if (enable) {
			display.addFilter(SWT.MouseMove, mouseMoveListener);
			display.addFilter(SWT.MouseDown, mouseButtonListener);
			display.addFilter(SWT.MouseUp, mouseButtonListener);
			display.addFilter(SWT.KeyDown, keyListener);

			// Fun Hack! restarts a drag after a shutdown
			MWindow dragWindow = findDragHost();
			if (dragWindow != null) {
				dragHost = new DragHost((Shell) dragWindow.getWidget());
			}
		} else {
			if (dragHost != null) {
				cancelDrag();
			}

			display.removeFilter(SWT.MouseMove, mouseMoveListener);
			display.removeFilter(SWT.MouseDown, mouseButtonListener);
			display.removeFilter(SWT.MouseUp, mouseButtonListener);
			display.removeFilter(SWT.KeyDown, keyListener);
		}
	}

	private MWindow findDragHost() {
		for (MUIElement element : baseWindow.getChildren()) {
			if (element.getTags() != null
					&& element.getTags().indexOf("dragHost") >= 0) //$NON-NLS-1$
				return (MWindow) element;
		}

		return null;
	}
}
