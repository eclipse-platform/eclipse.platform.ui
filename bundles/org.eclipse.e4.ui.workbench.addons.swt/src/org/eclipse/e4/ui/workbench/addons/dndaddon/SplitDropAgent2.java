/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.List;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class SplitDropAgent2 extends DropAgent {
	private static final int TOLERANCE = 35;

	private MUIElement relToElement = null;

	private SplitFeedbackOverlay feedback = null;

	private int dragElementLocation;

	private int where;

	private float ratio = 0.5f;

	private Rectangle trackRect;

	private boolean wasModified = false;

	/**
	 * @param manager
	 *            the DnDManager using this agent
	 */
	public SplitDropAgent2(DnDManager manager) {
		super(manager);
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		if (!(dragElement instanceof MStackElement) && !(dragElement instanceof MPartStack))
			return false;

		relToElement = getTargetElement(dragElement, info);
		return relToElement != null;
	}

	private MUIElement getTargetElement(MUIElement dragElement, DnDInfo info) {
		MUIElement target = null;

		dragElementLocation = dndManager.getModelService().getElementLocation(dragElement);

		// If we're inside a perspective somewhere check the shared area's edges
		if (dragElementLocation == EModelService.IN_SHARED_AREA
				|| dragElementLocation == EModelService.IN_ACTIVE_PERSPECTIVE) {
			target = checkAreaEdge(dragElement, info);
		}

		if (target == null && dragElementLocation == EModelService.IN_ACTIVE_PERSPECTIVE
				|| dragElementLocation == EModelService.OUTSIDE_PERSPECTIVE) {
			target = checkPerspectiveEdge(dragElement, info);
		}

		if (target == null) {
			target = checkStacks(dragElement, info);
		}

		return target;
	}

	private boolean isInCursorShell(DnDInfo info, Control ctrl) {
		if (info.curCtrl == null || info.curCtrl.isDisposed())
			return false;
		Shell infoShell = (Shell) (info.curCtrl instanceof Shell ? info.curCtrl : info.curCtrl
				.getShell());
		return ctrl.getShell() == infoShell;
	}

	private MPartStack checkStacks(final MUIElement dragElement, final DnDInfo info) {
		MPartStack candidateStack = null;

		// Collect up the elements we can be split 'relative to'
		final EModelService ms = dndManager.getModelService();
		List<MPartStack> stacks = ms.findElements(ms.getTopLevelWindowFor(dragElement),
				MPartStack.class, EModelService.PRESENTATION, new Selector() {
					@Override
					public boolean select(MApplicationElement element) {
						MPartStack stack = (MPartStack) element;

						// Has to be visible...
						if (!stack.isVisible() || !(stack.getWidget() instanceof CTabFolder))
							return false;

						// ...not disposed...
						CTabFolder ctf = (CTabFolder) stack.getWidget();
						if (ctf.isDisposed())
							return false;

						// ...and in the shell the cursor is over
						if (!isInCursorShell(info, ctf))
							return false;

						// ...and the cursor must be in the CTF's client area
						Rectangle bb = ctf.getClientArea();
						bb = ctf.getDisplay().map(ctf, null, bb);
						if (!bb.contains(info.cursorPos))
							return false;

						// Can't split with ourselves if we're dragging a stack
						if (dragElement instanceof MPartStack && stack == dragElement)
							return false;

						// Can't split with ourselves if we're dragging the only visible element in
						// a stack
						MUIElement deParent = dragElement.getParent();
						if (dragElement instanceof MStackElement && stack == deParent
								&& ms.countRenderableChildren(deParent) == 1)
							return false;

						return true;
					}
				});

		if (stacks.size() > 0) {
			candidateStack = stacks.get(0);
			if (candidateStack.getWidget() instanceof CTabFolder) {
				CTabFolder ctf = (CTabFolder) (candidateStack.getWidget());
				trackRect = ctf.getClientArea();
				trackRect = ctf.getDisplay().map(ctf, null, trackRect);
			}
		}

		return candidateStack;
	}

	private MUIElement checkAreaEdge(MUIElement dragElement, DnDInfo info) {
		MPerspective persp = dndManager.getModelService().getPerspectiveFor(dragElement);
		List<MArea> areaList = dndManager.getModelService().findElements(persp, null, MArea.class,
				null, EModelService.IN_SHARED_AREA);
		if (areaList.size() > 0) {
			MArea area = areaList.get(0);
			Control ctrl = (Control) area.getWidget();
			if (checkEdge(info, ctrl)) {
				return area;
			}
		}

		return null;
	}

	private MUIElement checkPerspectiveEdge(MUIElement dragElement, DnDInfo info) {
		MWindow win = dndManager.getModelService().getTopLevelWindowFor(dragElement);
		MPerspective persp = dndManager.getModelService().getActivePerspective(win);
		if (persp != null && persp.getWidget() instanceof Control) {
			Control ctrl = (Control) persp.getWidget();
			if (checkEdge(info, ctrl)) {
				return persp;
			}
		}

		return null;
	}

	private boolean checkEdge(DnDInfo info, Control ctrl) {
		boolean onEdge = false;

		if (!isInCursorShell(info, ctrl))
			return false;

		Rectangle bb = ctrl.getBounds();
		bb = ctrl.getDisplay().map(ctrl.getParent(), null, bb);
		if (bb.contains(info.cursorPos)) {
			Point p = info.cursorPos;
			if (p.x - bb.x < TOLERANCE) {
				where = EModelService.LEFT_OF;
				trackRect = new Rectangle(bb.x, bb.y, TOLERANCE, bb.height);
				onEdge = true;
				// } else if (p.y - bb.y < TOLERANCE) {
				// where = EModelService.ABOVE;
				// trackRect = new Rectangle(bb.x, bb.y, bb.width, TOLERANCE);
				// onEdge = true;
			} else if ((bb.x + bb.width) - p.x < TOLERANCE) {
				where = EModelService.RIGHT_OF;
				trackRect = new Rectangle(bb.x + (bb.width - TOLERANCE), bb.y, TOLERANCE,
						bb.height);
				onEdge = true;
			} else if ((bb.y + bb.height) - p.y < TOLERANCE) {
				where = EModelService.BELOW;
				trackRect = new Rectangle(bb.x, bb.y + (bb.height - TOLERANCE), bb.width,
						TOLERANCE);
				onEdge = true;
			}
		}
		return onEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragEnter(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public void dragEnter(final MUIElement dragElement, DnDInfo info) {
		super.dragEnter(dragElement, info);

		where = setRelToInfo(info);

		showFeedback();
	}

	private int setRelToInfo(DnDInfo info) {
		if (relToElement instanceof MPerspective || relToElement instanceof MArea) {
			ratio = 0.33f; // 'where' has already been set
			return where;
		} else if (relToElement instanceof MPartStack) {
			ratio = 0.5f;
			Point p = info.cursorPos;
			int dTop = p.y - trackRect.y;
			int dBottom = (trackRect.y + trackRect.height) - p.y;
			int dLeft = p.x - trackRect.x;
			int dRight = (trackRect.x + trackRect.width) - p.x;

			if (trackRect.width >= trackRect.height) {
				int thirdOfWidth = trackRect.width / 3;
				// System.out.println("tow: " + thirdOfWidth + " dLeft: " + dLeft + " dRight: "
				// + dRight);
				if (dLeft < thirdOfWidth)
					return EModelService.LEFT_OF;
				else if (dRight < thirdOfWidth)
					return EModelService.RIGHT_OF;
				else {
					return dTop < dBottom ? EModelService.ABOVE : EModelService.BELOW;
				}
			}

			int thirdOfHeight = trackRect.height / 3;
			if (dTop < thirdOfHeight)
				return EModelService.ABOVE;
			else if (dBottom < thirdOfHeight)
				return EModelService.BELOW;
			else if (relToElement != null) {
				return dLeft < dRight ? EModelService.LEFT_OF : EModelService.RIGHT_OF;
			}
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#dragLeave(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		dndManager.clearOverlay();
		clearFeedback();
		relToElement = null;

		super.dragLeave(dragElement, info);
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		MPartSashContainerElement toInsert = (MPartSashContainerElement) dragElement;
		if (dragElement instanceof MPartStack) {
			// Ensure we restore the stack to the presentation first
			if (toInsert.getTags().contains(IPresentationEngine.MINIMIZED)) {
				toInsert.getTags().remove(IPresentationEngine.MINIMIZED);
			}

			toInsert.getParent().getChildren().remove(toInsert);
		} else {
			// wrap it in a stack if it's a part
			MStackElement stackElement = (MStackElement) dragElement;
			MPartStack newStack = BasicFactoryImpl.eINSTANCE.createPartStack();
			newStack.getChildren().add(stackElement);
			newStack.setSelectedElement(stackElement);
			toInsert = newStack;
		}

		// treat the lone editor area stack as if it were the area
		if (dndManager.getModelService().isLastEditorStack(relToElement)) {
			MUIElement targetParent = relToElement.getParent();
			while (!(targetParent instanceof MArea))
				targetParent = targetParent.getParent();
			relToElement = targetParent;
		}

		// Adjust the relToElement based on the location of the dragElement
		if (relToElement instanceof MArea) {
			if (!isModified()) {
				if (dragElementLocation == EModelService.IN_SHARED_AREA) {
					MArea area = (MArea) relToElement;
					relToElement = area.getChildren().get(0);
				} else if (dragElementLocation == EModelService.IN_ACTIVE_PERSPECTIVE) {
					relToElement = relToElement.getCurSharedRef();
				}
			} else {
				if (dragElementLocation == EModelService.IN_SHARED_AREA) {
					relToElement = relToElement.getCurSharedRef();
				} else if (dragElementLocation == EModelService.IN_ACTIVE_PERSPECTIVE) {
					MArea area = (MArea) relToElement;
					relToElement = area.getChildren().get(0);
				}
			}
		} else if (relToElement instanceof MPerspective) {
			if (dragElementLocation == EModelService.OUTSIDE_PERSPECTIVE) {
				relToElement = relToElement.getParent();
			} else if (dragElementLocation == EModelService.IN_ACTIVE_PERSPECTIVE) {
				MPerspective persp = (MPerspective) relToElement;
				relToElement = persp.getChildren().get(0);
			}
		}

		dndManager.getModelService().insert(toInsert, (MPartSashContainerElement) relToElement,
				where, ratio);
		reactivatePart(dragElement);

		return true;
	}

	private boolean isModified() {
		return dndManager.isModified
				&& (relToElement instanceof MArea || relToElement instanceof MPerspective || dndManager
						.getModelService().isLastEditorStack(relToElement));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.addons.dndaddon.DropAgent#track(org.eclipse.e4.ui.model.application
	 * .ui.MUIElement, org.eclipse.e4.ui.workbench.addons.dndaddon.DnDInfo)
	 */
	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {
		if (getTargetElement(dragElement, info) != relToElement) {
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
			return false;
		}

		if (!trackRect.contains(info.cursorPos)) {
			dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_NO));
			return false;
		}

		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));

		int curWhere = where;
		where = setRelToInfo(info);

		if (where == curWhere && wasModified == isModified())
			return true;
		wasModified = isModified();

		showFeedback();

		return true;
	}

	private void showFeedback() {
		if (relToElement == null || !(relToElement.getWidget() instanceof Control))
			return;

		int side = -1;
		if (where == EModelService.ABOVE) {
			side = SWT.TOP;
		} else if (where == EModelService.BELOW) {
			side = SWT.BOTTOM;
		} else if (where == EModelService.LEFT_OF) {
			side = SWT.LEFT;
		} else if (where == EModelService.RIGHT_OF) {
			side = SWT.RIGHT;
		}

		if (feedback != null)
			feedback.dispose();

		Control ctrl = (Control) relToElement.getWidget();
		Rectangle bb = ctrl.getBounds();
		bb = ctrl.getDisplay().map(ctrl.getParent(), null, bb);
		feedback = new SplitFeedbackOverlay(ctrl.getShell(), bb, side, ratio, isModified(),
				isModified());
		feedback.setVisible(true);
	}

	private void clearFeedback() {
		if (feedback == null)
			return;

		feedback.dispose();
		feedback = null;
	}
}
