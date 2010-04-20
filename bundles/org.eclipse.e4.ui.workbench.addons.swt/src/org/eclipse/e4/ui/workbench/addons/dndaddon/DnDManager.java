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
package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.swt.internal.AbstractPartRenderer;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;

class DnDManager {
	private static final int DRAG_OFFSET = 8;

	DragAndDropUtil dndUtil;

	Rectangle offScreenRect = new Rectangle(10000, -10000, 1, 1);

	DragAgent partDragAgent = new DragAgent() {
		@Override
		public MUIElement getElementToDrag(CursorInfo info) {
			if (info.curElement instanceof MPartStack
					&& info.itemElement instanceof MPart) {
				dragElement = info.itemElement;
				return info.itemElement;
			}
			return null;
		}
	};

	DropAgent partDropAgent = new DropAgent() {
		@Override
		public boolean canDrop(MUIElement dragElement, CursorInfo info) {
			if (dragElement instanceof MPart
					&& info.curElement instanceof MPartStack)
				return true;

			return false;
		}

		@Override
		public boolean drop(MUIElement dragElement, CursorInfo info) {
			MPartStack dropStack = (MPartStack) info.curElement;
			if (dragElement.getParent() != null) {
				dragElement.getParent().getChildren().remove(dragElement);
			}

			if (info.itemIndex == -1) {
				dropStack.getChildren().add((MPart) dragElement);
			} else {
				dropStack.getChildren()
						.add(info.itemIndex, (MPart) dragElement);
			}
			dropStack.setSelectedElement((MPart) dragElement);

			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.DropAgent#getRectangle
		 * (org.eclipse.e4.ui.model.application.ui.MUIElement,
		 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.CursorInfo)
		 */
		@Override
		public Rectangle getRectangle(MUIElement dragElement, CursorInfo info) {
			CTabFolder ctf = (CTabFolder) info.curElement.getWidget();
			if (info.itemElement != null) {
				if (info.curElement.getWidget() instanceof CTabFolder) {
					for (CTabItem cti : ctf.getItems()) {
						if (cti.getData(AbstractPartRenderer.OWNING_ME) == info.itemElement) {
							Rectangle itemRect = cti.getBounds();
							itemRect.width = 3;
							return cti.getDisplay().map(cti.getParent(), null,
									itemRect);
						}
					}
				}
			} else {
				if (ctf.getItemCount() == 0) {
					Rectangle ctfBounds = ctf.getBounds();
					ctfBounds.height = ctf.getTabHeight();
					ctfBounds.width = 3;
					return ctf.getDisplay().map(ctf, null, ctfBounds);
				}

				CTabItem cti = ctf.getItem(ctf.getItemCount() - 1);
				Rectangle itemRect = cti.getBounds();
				itemRect.x = (itemRect.x + itemRect.width) - 3;
				itemRect.width = 3;
				return cti.getDisplay().map(cti.getParent(), null, itemRect);
			}
			return null;
		}
	};

	DropAgent insertDropAgent = new DropAgent() {
		@Override
		public boolean canDrop(MUIElement dragElement, CursorInfo info) {
			if (dragElement instanceof MPart
					&& info.curElement instanceof MPart)
				return true;

			return false;
		}

		private int whereToDrop(Control ctrl, Point cursorPos) {
			Rectangle bb = ctrl.getBounds();
			Rectangle displayBB = ctrl.getDisplay().map(ctrl, null, bb);
			int dxl = cursorPos.x - displayBB.x;
			int dxr = (displayBB.x + displayBB.width) - cursorPos.x;
			int dx = Math.min(dxl, dxr);
			int dyl = cursorPos.y - displayBB.y;
			int dyr = (displayBB.y + displayBB.height) - cursorPos.y;
			int dy = Math.min(dyl, dyr);
			int where;
			if (dx < dy) {
				if (dxl < dxr)
					where = EModelService.LEFT_OF;
				else
					where = EModelService.RIGHT_OF;
			} else {
				if (dyl < dyr)
					where = EModelService.ABOVE;
				else
					where = EModelService.BELOW;
			}

			return where;
		}

		@Override
		public boolean drop(MUIElement dragElement, CursorInfo info) {
			MPart relTo = (MPart) info.curElement;
			if (dragElement.getParent() != null) {
				dragElement.getParent().getChildren().remove(dragElement);
			}

			Control ctrl = (Control) relTo.getWidget();
			int where = whereToDrop(ctrl, info.cursorPos);

			// If we're dropping a part wrap it in a stack
			MUIElement toInsert = dragElement;
			if (dragElement instanceof MPart) {
				MPartStack newPS = BasicFactoryImpl.eINSTANCE.createPartStack();
				newPS.getChildren().add((MPart) dragElement);
				newPS.setSelectedElement((MPart) dragElement);
				toInsert = newPS;
			}

			modelService.insert((MPartSashContainerElement) toInsert,
					(MPartSashContainerElement) relTo, where, 50);
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.DropAgent#getCursor
		 * (org.eclipse.swt.widgets.Display,
		 * org.eclipse.e4.ui.model.application.ui.MUIElement,
		 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.CursorInfo)
		 */
		@Override
		public Cursor getCursor(Display display, MUIElement dragElement,
				CursorInfo info) {
			MPart dropPart = (MPart) info.curElement;
			Control ctrl = (Control) dropPart.getWidget();
			int where = whereToDrop(ctrl, info.cursorPos);
			if (where == EModelService.ABOVE)
				return display.getSystemCursor(SWT.CURSOR_SIZEN);
			if (where == EModelService.BELOW)
				return display.getSystemCursor(SWT.CURSOR_SIZES);
			if (where == EModelService.LEFT_OF)
				return display.getSystemCursor(SWT.CURSOR_SIZEW);
			if (where == EModelService.RIGHT_OF)
				return display.getSystemCursor(SWT.CURSOR_SIZEE);

			return display.getSystemCursor(SWT.CURSOR_HELP);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.DropAgent#getRectangle
		 * (org.eclipse.e4.ui.model.application.ui.MUIElement,
		 * org.eclipse.e4.workbench.ui.renderers.swt.dnd.CursorInfo)
		 */
		@Override
		public Rectangle getRectangle(MUIElement dragElement, CursorInfo info) {
			MPart dropPart = (MPart) info.curElement;
			Control ctrl = (Control) dropPart.getWidget();

			if (ctrl.getParent() instanceof CTabFolder)
				ctrl = ctrl.getParent();
			Rectangle bounds = ctrl.getBounds();
			int where = whereToDrop(ctrl, info.cursorPos);
			if (where == EModelService.ABOVE)
				bounds = new Rectangle(bounds.x, bounds.y, bounds.width,
						bounds.height / 2);
			if (where == EModelService.BELOW)
				bounds = new Rectangle(bounds.x,
						bounds.y + (bounds.height / 2), bounds.width,
						bounds.height / 2);
			if (where == EModelService.LEFT_OF)
				bounds = new Rectangle(bounds.x, bounds.y, bounds.width / 2,
						bounds.height);
			if (where == EModelService.RIGHT_OF)
				bounds = new Rectangle(bounds.x + (bounds.width / 2), bounds.y,
						bounds.width / 2, bounds.height);

			return ctrl.getDisplay().map(ctrl, null, bounds);
		}
	};

	Collection<DragAgent> dragAgents = new ArrayList<DragAgent>();
	DragAgent dragAgent;
	boolean dragging = false;

	Collection<DropAgent> dropAgents = new ArrayList<DropAgent>();
	DropAgent dropAgent;

	protected CursorInfo dndInfo;
	protected Point downPos;
	protected CursorInfo downInfo;

	// private MWindow baseWindow;
	private Display display;

	Listener keyListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.character == SWT.ESC && dragAgent != null) {
				dragAgent.cancelDrag();
				dragAgent = null;
			}
		}
	};

	Listener mouseButtonListener = new Listener() {
		public void handleEvent(Event event) {
			// Only allow left mouse drags (for now?)
			if (event.button != 1) {
				downPos = null;
				return;
			}

			CursorInfo info = dndUtil.getCursorInfo();
			if (event.type == SWT.MouseDown) {
				dragAgent = getDragAgent(info);
				if (dragAgent != null) {
					downPos = new Point(event.x, event.y);
					downInfo = info;
				}
			} else if (event.type == SWT.MouseUp) {
				downPos = null;
				downInfo = null;
				if (dragging) {
					if (dropAgent != null) {
						dropAgent.drop(dragAgent.dragElement, dndUtil
								.getCursorInfo());
					} else if (dragAgent != null) {
						dragAgent.cancelDrag();
					}
					dragging = false;
				}
			}
		}
	};

	Listener mouseMoveListener = new Listener() {
		public void handleEvent(Event event) {
			if (dragging) {
				CursorInfo info = dndUtil.getCursorInfo();
				dragAgent.trackDragFeedback(info);

				DropAgent newDropAgent = getDropAgent(dragAgent.dragElement,
						info);
				if (dropAgent != newDropAgent) {
					if (dropAgent != null)
						dropAgent.dragLeave();

					dropAgent = newDropAgent;

					if (dropAgent != null)
						dropAgent.dragEnter();
				}
			} else {
				if (downPos != null) {
					Point curPos = new Point(event.x, event.y);
					int dx = Math.abs(downPos.x - curPos.x);
					int dy = Math.abs(downPos.y - curPos.y);
					if (dx > DRAG_OFFSET || dy > DRAG_OFFSET) {
						downPos = null;
						startDrag(downInfo);
					}
				}
			}
		}
	};

	private EModelService modelService;
	private MWindow baseWindow;
	private Shell baseShell;

	public DnDManager(MWindow window) {
		baseWindow = window;

		dragAgents.add(partDragAgent);
		dropAgents.add(partDropAgent);
		dropAgents.add(insertDropAgent);

		if (baseWindow.getWidget() instanceof Shell) {
			baseShell = (Shell) baseWindow.getWidget();
			display = baseShell.getDisplay();
			setDisplayFilters(true);

			dndUtil = new DragAndDropUtil(display);

			modelService = (EModelService) baseWindow.getContext().get(
					EModelService.class.getName());

			baseShell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (!display.isDisposed()) {
						setDisplayFilters(false);
					}
				}
			});
		}
	}

	/**
	 * @param downInfo2
	 */
	protected void startDrag(CursorInfo downInfo2) {
		dragging = true;
		setDisplayFilters(false);

		final Tracker tracker = new Tracker(display, SWT.NULL);
		tracker.addListener(SWT.Move, new Listener() {
			public void handleEvent(final Event event) {
				display.syncExec(new Runnable() {
					public void run() {
						CursorInfo dragInfo = dndUtil.getCursorInfo();
						dropAgent = getDropAgent(dragAgent.getDragElement(),
								dragInfo);
						if (dropAgent == null) {
							baseShell.setCursor(baseShell.getDisplay()
									.getSystemCursor(SWT.CURSOR_NO));
							tracker.setRectangles(new Rectangle[] { offScreenRect });
						} else {
							Rectangle rect = dropAgent.getRectangle(
									dragAgent.dragElement, dragInfo);
							if (rect == null)
								rect = offScreenRect;
							tracker.setRectangles(new Rectangle[] { rect });
							baseShell.setCursor(dropAgent.getCursor(display,
									dragAgent.dragElement, dragInfo));
						}
					}
				});
			}
		});

		// HACK: Some control needs to capture the mouse during the drag or
		// other controls will interfere with the cursor
		baseShell.setCapture(true);

		// Run tracker until mouse up occurs or escape key pressed.
		boolean performDrop = tracker.open();
		if (performDrop && dropAgent != null) {
			dropAgent.drop(dragAgent.getDragElement(), dndUtil.getCursorInfo());
		} else {
			System.out.println("Cancel!"); //$NON-NLS-1$
			dragAgent.cancelDrag();
		}

		baseShell.setCursor(null);
		baseShell.setCapture(false);

		dragging = false;
		setDisplayFilters(true);
	}

	public void addDragAgent(DragAgent newAgent) {
		if (!dragAgents.contains(newAgent))
			dragAgents.add(newAgent);
	}

	public void removeDragAgent(DragAgent agentToRemove) {
		dragAgents.remove(agentToRemove);
	}

	public void addDropAgent(DropAgent newAgent) {
		if (!dropAgents.contains(newAgent))
			dropAgents.add(newAgent);
	}

	public void removeDropAgent(DropAgent agentToRemove) {
		dropAgents.remove(agentToRemove);
	}

	private DragAgent getDragAgent(CursorInfo info) {
		for (DragAgent agent : dragAgents) {
			if (agent.canDrag(info))
				return agent;
		}
		return null;
	}

	private DropAgent getDropAgent(MUIElement dragElement, CursorInfo info) {
		for (DropAgent agent : dropAgents) {
			if (agent.canDrop(dragElement, info))
				return agent;
		}
		return null;
	}

	public void setDisplayFilters(boolean enable) {
		if (enable) {
			display.addFilter(SWT.MouseMove, mouseMoveListener);
			display.addFilter(SWT.MouseDown, mouseButtonListener);
			display.addFilter(SWT.MouseUp, mouseButtonListener);
			display.addFilter(SWT.KeyDown, keyListener);
		} else {
			display.removeFilter(SWT.MouseMove, mouseMoveListener);
			display.removeFilter(SWT.MouseDown, mouseButtonListener);
			display.removeFilter(SWT.MouseUp, mouseButtonListener);
			display.removeFilter(SWT.KeyDown, keyListener);
		}
	}
}
