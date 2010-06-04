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
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;

class DnDManager {
	private static final int DRAG_OFFSET = 8;

	DragAndDropUtil dndUtil;

	Rectangle offScreenRect = new Rectangle(10000, -10000, 1, 1);

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
						dropAgent.drop(dragAgent.dragElement, dndUtil.getCursorInfo());
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

				DropAgent newDropAgent = getDropAgent(dragAgent.dragElement, info);
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

		dragAgents.add(new PartDragAgent());
		dropAgents.add(new StackDropAgent());

		modelService = (EModelService) window.getContext().get(EModelService.class.getName());
		dropAgents.add(new SplitDropAgent(modelService));

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
						dropAgent = getDropAgent(dragAgent.getDragElement(), dragInfo);
						if (dropAgent == null) {
							baseShell.setCursor(baseShell.getDisplay().getSystemCursor(
									SWT.CURSOR_NO));
							tracker.setRectangles(new Rectangle[] { offScreenRect });
						} else {
							Rectangle rect = dropAgent
									.getRectangle(dragAgent.dragElement, dragInfo);
							if (rect == null)
								rect = offScreenRect;
							tracker.setRectangles(new Rectangle[] { rect });
							baseShell.setCursor(dropAgent.getCursor(display, dragAgent.dragElement,
									dragInfo));
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
