/*******************************************************************************
 * Copyright (c) 2013, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Pierre-Yves Bigourdan <pyvesdev@gmail.com> - Bug 562536 - Allow changing sash width
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.internal.css.swt.ISashLayout;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class SashLayout extends Layout implements ISashLayout {
	// The minimum value (as a percentage) that a sash can be dragged to
	int minSashPercent = 10;

	int marginLeft = 0;
	int marginRight = 0;
	int marginTop = 0;
	int marginBottom = 0;
	int sashWidth = 4;

	MUIElement root;
	private Composite host;

	static class SashRect {
		Rectangle rect;
		MGenericTile<?> container;
		MUIElement left;
		MUIElement right;

		public SashRect(Rectangle rect, MGenericTile<?> container,
				MUIElement left, MUIElement right) {
			this.container = container;
			this.rect = rect;
			this.left = left;
			this.right = right;
		}
	}

	List<SashRect> sashes = new ArrayList<>();
	List<SashRect> sashesToDrag;

	static enum State {
		DRAGGING, HOVERING_NS, HOVERING_WE, HOVERING_ALL, OTHER
	}

	State state = State.OTHER;

	public boolean layoutUpdateInProgress = false;

	public SashLayout(final Composite host, MUIElement root) {
		this.root = root;
		this.host = host;

		Listener mouseMoveListener = this::onMouseMove;
		Listener mouseUpListener = this::onMouseUp;
		Listener mouseDownListener = this::onMouseDown;
		Listener dragDetectListener = this::onDragDetect;
		Listener activateListener = this::onActivate;

		host.getDisplay().addFilter(SWT.MouseMove, mouseMoveListener);
		host.getDisplay().addFilter(SWT.MouseUp, mouseUpListener);
		host.getDisplay().addFilter(SWT.MouseDown, mouseDownListener);
		host.getDisplay().addFilter(SWT.DragDetect, dragDetectListener);
		host.getDisplay().addFilter(SWT.Activate, activateListener);

		host.addDisposeListener(e -> {
			host.getDisplay().removeFilter(SWT.MouseMove, mouseMoveListener);
			host.getDisplay().removeFilter(SWT.MouseUp, mouseUpListener);
			host.getDisplay().removeFilter(SWT.MouseDown, mouseDownListener);
			host.getDisplay().removeFilter(SWT.DragDetect, dragDetectListener);
			host.getDisplay().removeFilter(SWT.Activate, activateListener);
		});
	}

	/**
	 * Used to update the layout while dragging or to update the cursor to show
	 * possible drag directions otherwise.
	 *
	 * @param e the mouse event
	 */
	private void onMouseMove(Event e) {
		if (!(e.widget instanceof Control)) {
			return;
		}
		Control control = (Control) e.widget;
		if (control.getShell() != host.getShell()) {
			return;
		}
		Point relativeToHost = host.getDisplay().map(control, host, e.x, e.y);
		if (state == State.DRAGGING) {
			try {
				layoutUpdateInProgress = true;
				adjustWeights(sashesToDrag, relativeToHost.x, relativeToHost.y);
				// FIXME SWT Win requires a synchronous layout call to update the UI
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=558392
				// once this is fixed, the requestLayout call should be sufficient
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					try {
						host.setRedraw(false);
						host.layout();
					} finally {
						host.setRedraw(true);
					}
					host.update();
				} else {
					host.requestLayout();
				}
			} finally {
				layoutUpdateInProgress = false;
			}
		} else {
			// Set the cursor feedback. Comparison with the previous state is needed to
			// prevent repeated setCursor calls.
			List<SashRect> sashList = getSashRects(relativeToHost.x, relativeToHost.y);
			if (sashList.isEmpty()) {
				if (state != State.OTHER) {
					state = State.OTHER;
					host.setCursor(host.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				}
				return; // Not an interaction with a sash: return now to avoid e.type = SWT.None call.
			} else if (sashList.size() > 1) {
				if (state != State.HOVERING_ALL) {
					state = State.HOVERING_ALL;
					host.setCursor(host.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
				}
			} else if (sashList.get(0).container.isHorizontal()) {
				if (state != State.HOVERING_WE) {
					state = State.HOVERING_WE;
					host.setCursor(host.getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE));
				}
			} else {
				if (state != State.HOVERING_NS) {
					state = State.HOVERING_NS;
					host.setCursor(host.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
				}
			}
		}
		e.type = SWT.None; // Filter out event as we're interacting with a sash.
	}

	/**
	 * Used to signal the end of any layout changes when the mouse is released.
	 *
	 * @param e the mouse event
	 */
	private void onMouseUp(Event e) {
		if (state == State.OTHER) {
			return;
		}
		host.setCapture(false);
		state = State.OTHER;
		e.type = SWT.None;
	}

	/**
	 * Used to signal and prepare the start of layout changes. The
	 * {@link #onMouseMove(Event)} method will then keep track of the actual
	 * dragging and perform layout updates.
	 *
	 * @param e the mouse event
	 */
	private void onMouseDown(Event e) {
		if (state == State.OTHER || e.button != 1 || !(e.widget instanceof Control)) {
			return;
		}
		Control control = (Control) e.widget;
		if (control.getShell() != host.getShell()) {
			return;
		}

		e.type = SWT.None; // Filter out event as we're interacting with a sash.

		Point relativeToHost = host.getDisplay().map(control, host, e.x, e.y);
		sashesToDrag = getSashRects(relativeToHost.x, relativeToHost.y);
		if (sashesToDrag.size() > 0) {
			state = State.DRAGGING;
			host.setCapture(true);
		}
	}

	/**
	 * Used to filter out drag events when there is an ongoing interaction with a
	 * sash, as layout updates through dragging are handled via the
	 * {@link #onMouseMove(Event)} method.
	 *
	 * @param e the mouse event
	 */
	private void onDragDetect(Event e) {
		if (state == State.OTHER) {
			return;
		}
		e.type = SWT.None; // Filter out event as we're currently interacting with a sash.
	}

	/**
	 * Used to filter out activate events when there is an ongoing interaction with
	 * a sash. Without this, starting to update the sash layout by clicking with the
	 * mouse ({@link #onMouseDown(Event)}) would change the active part.
	 *
	 * @param e the mouse event
	 */
	private void onActivate(Event e) {
		if (state == State.OTHER) {
			return;
		}
		e.type = SWT.None; // Filter out event as we're currently interacting with a sash.
	}

	@Override
	public int getSashWidth() {
		return sashWidth;
	}

	@Override
	public void setSashWidth(int sashWidth) {
		this.sashWidth = sashWidth;
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		if (root == null)
			return;

		Rectangle bounds = composite.getBounds();
		if (composite instanceof Shell)
			bounds = ((Shell) composite).getClientArea();
		else {
			bounds.x = 0;
			bounds.y = 0;
		}

		bounds.width -= (marginLeft + marginRight);
		bounds.height -= (marginTop + marginBottom);
		bounds.x += marginLeft;
		bounds.y += marginTop;

		sashes.clear();
		tileSubNodes(bounds, root);
	}

	protected void adjustWeights(List<SashRect> sashes, int curX, int curY) {
		for (SashRect sr : sashes) {
			int totalWeight = getWeight(sr.left) + getWeight(sr.right);
			int minSashValue = (int) (((totalWeight / 100.0) * minSashPercent) + 0.5);

			Rectangle leftRect = getRectangle(sr.left);
			Rectangle rightRect = getRectangle(sr.right);
			if (leftRect == null || rightRect == null)
				continue;

			int leftWeight;
			int rightWeight;

			if (sr.container.isHorizontal()) {
				double left = leftRect.x;
				double right = rightRect.x + rightRect.width;
				double pct = (curX - left) / (right - left);
				leftWeight = (int) ((totalWeight * pct) + 0.5);
				if (leftWeight < minSashValue)
					leftWeight = minSashValue;
				if (leftWeight > (totalWeight - minSashValue))
					leftWeight = totalWeight - minSashValue;
				rightWeight = totalWeight - leftWeight;
			} else {
				double top = leftRect.y;
				double bottom = rightRect.y + rightRect.height;
				double pct = (curY - top) / (bottom - top);
				leftWeight = (int) ((totalWeight * pct) + 0.5);
				if (leftWeight < minSashValue)
					leftWeight = minSashValue;
				if (leftWeight > (totalWeight - minSashValue))
					leftWeight = totalWeight - minSashValue;
				rightWeight = totalWeight - leftWeight;
			}

			setWeight(sr.left, leftWeight);
			setWeight(sr.right, rightWeight);
		}
	}

	private void setWeight(MUIElement element, int weight) {
		element.setContainerData(Integer.toString(weight));
	}

	private Rectangle getRectangle(MUIElement element) {
		if (element.getWidget() instanceof Rectangle)
			return (Rectangle) element.getWidget();
		else if (element.getWidget() instanceof Control)
			return ((Control) (element.getWidget())).getBounds();
		return null;
	}

	protected List<SashRect> getSashRects(int x, int y) {
		List<SashRect> srs = new ArrayList<>();
		Rectangle target = new Rectangle(x - 5, y - 5, 10, 10);
		for (SashRect sr : sashes) {
			if (sr.rect.intersects(target) && !sr.container.getTags().contains(IPresentationEngine.NO_MOVE))
				srs.add(sr);
		}
		return srs;
	}

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		return new Point(600, 400);
	}

	private int totalWeight(MGenericTile<?> node) {
		int total = 0;
		for (MUIElement subNode : node.getChildren()) {
			if (subNode.isToBeRendered() && subNode.isVisible())
				total += getWeight(subNode);
		}
		return total;
	}

	private void tileSubNodes(Rectangle bounds, MUIElement node) {
		if (node != root)
			setRectangle(node, bounds);

		if (!(node instanceof MGenericTile<?>))
			return;

		MGenericTile<?> sashContainer = (MGenericTile<?>) node;
		List<MUIElement> visibleChildren = getVisibleChildren(sashContainer);
		int childCount = visibleChildren.size();

		// How many pixels do we have?
		int availableWidth = sashContainer.isHorizontal() ? bounds.width
				: bounds.height;

		// Subtract off the room for the sashes
		availableWidth -= ((childCount - 1) * sashWidth);

		// Get the total of the weights
		double totalWeight = totalWeight(sashContainer);
		int tilePos = sashContainer.isHorizontal() ? bounds.x : bounds.y;

		MUIElement prev = null;
		for (MUIElement subNode : visibleChildren) {
			// Add a 'sash' between this node and the 'prev'
			if (prev != null) {
				Rectangle sashRect = sashContainer.isHorizontal() ? new Rectangle(
						tilePos, bounds.y, sashWidth, bounds.height)
						: new Rectangle(bounds.x, tilePos, bounds.width,
								sashWidth);
				sashes.add(new SashRect(sashRect, sashContainer, prev, subNode));
				host.redraw(sashRect.x, sashRect.y, sashRect.width,
						sashRect.height, false);
				tilePos += sashWidth;
			}

			// Calc the new size as a %'age of the total
			double ratio = getWeight(subNode) / totalWeight;
			int newSize = (int) ((availableWidth * ratio) + 0.5);

			Rectangle subBounds = sashContainer.isHorizontal() ? new Rectangle(
					tilePos, bounds.y, newSize, bounds.height) : new Rectangle(
					bounds.x, tilePos, bounds.width, newSize);
			tilePos += newSize;

			tileSubNodes(subBounds, subNode);
			prev = subNode;
		}
	}

	/**
	 * @param node
	 * @param bounds
	 */
	private void setRectangle(MUIElement node, Rectangle bounds) {
		if (node.getWidget() instanceof Control) {
			Control ctrl = (Control) node.getWidget();
			ctrl.setBounds(bounds);
		} else if (node.getWidget() instanceof Rectangle) {
			Rectangle theRect = (Rectangle) node.getWidget();
			theRect.x = bounds.x;
			theRect.y = bounds.y;
			theRect.width = bounds.width;
			theRect.height = bounds.height;
		}
	}

	private List<MUIElement> getVisibleChildren(MGenericTile<?> sashContainer) {
		List<MUIElement> visKids = new ArrayList<>();
		for (MUIElement child : sashContainer.getChildren()) {
			if (child.isToBeRendered() && child.isVisible())
				visKids.add(child);
		}
		return visKids;
	}

	private static int getWeight(MUIElement element) {
		String info = element.getContainerData();
		if (info == null || info.length() == 0) {
			return 0;
		}

		try {
			return Integer.parseInt(info);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
