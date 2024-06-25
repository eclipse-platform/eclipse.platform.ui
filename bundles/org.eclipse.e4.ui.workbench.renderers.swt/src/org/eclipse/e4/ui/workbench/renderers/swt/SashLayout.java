/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class SashLayout extends Layout {
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

	boolean draggingSashes = false;
	List<SashRect> sashesToDrag;

	public boolean layoutUpdateInProgress = false;

	/**
	 * Remember last cursor set on the sash to prevent repeated setCursor calls.
	 * Value can be <code>0</code> which means default cursor was set or one of the
	 * SWT.CURSOR_* constants.
	 */
	int lastCursor = 0;

	public SashLayout(final Composite host, MUIElement root) {
		this.root = root;
		this.host = host;

		host.addMouseTrackListener(MouseTrackListener.mouseExitAdapter(e -> {
			host.setCursor(null);
			lastCursor = 0;
		}));

		host.addMouseMoveListener(this::onMouseMove);

		host.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				host.setCapture(false);
				draggingSashes = false;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) {
					return;
				}

				sashesToDrag = getSashRects(e.x, e.y);
				if (sashesToDrag.size() > 0) {
					draggingSashes = true;
					host.setCapture(true);
				}
			}
		});
	}

	/**
	 * Used to update the layout while dragging or to update the cursor to show
	 * possible drag directions otherwise.
	 *
	 * @param e the mouse event
	 */
	private void onMouseMove(MouseEvent e) {
		if (!draggingSashes) {
			// Set the cursor feedback
			List<SashRect> sashList = getSashRects(e.x, e.y);
			final int newCursor;
			if (sashList.isEmpty()) {
				newCursor = SWT.CURSOR_ARROW;
			} else if (sashList.size() == 1) {
				if (sashList.get(0).container.isHorizontal()) {
					newCursor = SWT.CURSOR_SIZEWE;
				} else {
					newCursor = SWT.CURSOR_SIZENS;
				}
			} else {
				newCursor = SWT.CURSOR_SIZEALL;
			}
			if (lastCursor != newCursor) {
				host.setCursor(host.getDisplay().getSystemCursor(newCursor));
				lastCursor = newCursor;
			}
		} else {
			try {
				layoutUpdateInProgress = true;
				adjustWeights(sashesToDrag, e.x, e.y);
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
		}
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		if (root == null)
			return;

		Rectangle bounds = composite.getBounds();
		if (composite instanceof Shell)
			bounds = composite.getClientArea();
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
