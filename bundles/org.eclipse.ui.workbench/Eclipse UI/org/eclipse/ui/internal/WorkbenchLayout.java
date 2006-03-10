/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.internal.layout.TrimCommonUIHandle;

/**
 * This layout implements the handling necessary to support the positioning of
 * all of the 'trim' elements defined for the workbench.
 * <p>
 * NOTE: This class is a part of a 'work in progress' and should not be used
 * without consulting the Platform UI group. No guarantees are made as to the
 * stability of the API.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class WorkbenchLayout extends Layout {
	private static int defaultMargin = 5;

	/**
	 * This is a convenience class that caches information for a single 'tiled'
	 * line of trim.
	 * 
	 * @since 3.2
	 * 
	 */
	private class TrimLine {
		/**
		 * Teh list of controls in this trim line
		 */
		List controls = new ArrayList();

		/**
		 * A cache of the previously computed sizes of each trim control
		 */
		List computedSizes = new ArrayList();

		/**
		 * In horizontal terms this is the 'height' of the tallest control.
		 */
		int minorMax = 0;

		/**
		 * The number of controls in the line that want to 'grab' extra space.
		 * Any unused space in a trim line is shared equally among these
		 * controls
		 */
		int resizableCount = 0;

		/**
		 * The amount of unused space in the line
		 */
		int extraSpace = 0;
	}

	/**
	 * This layout is used to capture the CBanner's calls to 'computeSize' for
	 * the left trim (which is used to determine the height of the CBanner) so
	 * that it will compute its own height to be the max of either the left or
	 * the right control.
	 * <p>
	 * NOTE: This class is expected to be removed once the CBanner mods are in.
	 * </p>
	 * 
	 * @since 3.2
	 * 
	 */
	private class LeftBannerLayout extends Layout {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
		 *      int, int, boolean)
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			// 'topMax' is the maximum height of both the left and
			// the right side
			return new Point(wHint, WorkbenchLayout.this.topMax);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
		 *      boolean)
		 */
		protected void layout(Composite composite, boolean flushCache) {
		}

	}

	// Trim area 'ids'
	public static final String TRIMID_CMD_PRIMARY = "Command Primary"; //$NON-NLS-1$

	public static final String TRIMID_CMD_SECONDARY = "Command Secondary"; //$NON-NLS-1$

	public static final String TRIMID_VERTICAL1 = "vertical1"; //$NON-NLS-1$

	public static final String TRIMID_VERTICAL2 = "vertical2"; //$NON-NLS-1$

	public static final String TRIMID_STATUS = "Status"; //$NON-NLS-1$

	public static final String TRIMID_CENTER = "Center"; //$NON-NLS-1$

	// 'CBanner' info
	public CBanner banner;

	private int topMax;

	// 'Center' composite
	public Composite centerComposite;

	// inter-element spacing
	private int spacing = 0;

	// Trim Areas
	private TrimArea cmdPrimaryTrimArea;

	private TrimArea cmdSecondaryTrimArea;

	private TrimArea leftTrimArea;

	private TrimArea rightTrimArea;

	private TrimArea bottomTrimArea;

	// Drag handle info
	private int horizontalHandleSize = -1;

	private int verticalHandleSize = -1;

	private List dragHandles;

	// statics used in the layout
	private static Composite layoutComposite;

	private static Rectangle clientRect;

	/**
	 * Construct a new layout. This defines the trim areas that trim can be
	 * placed into.
	 */
	public WorkbenchLayout() {
		super();

		// Add the trim areas into the layout
		cmdPrimaryTrimArea = new TrimArea(TRIMID_CMD_PRIMARY, SWT.TOP,
				defaultMargin);
		cmdSecondaryTrimArea = new TrimArea(TRIMID_CMD_SECONDARY, SWT.TOP,
				defaultMargin);
		leftTrimArea = new TrimArea(TRIMID_VERTICAL1, SWT.LEFT, defaultMargin);
		rightTrimArea = new TrimArea(TRIMID_VERTICAL2, SWT.RIGHT, defaultMargin);
		bottomTrimArea = new TrimArea(TRIMID_STATUS, SWT.BOTTOM, defaultMargin);

		// init the list that has the drag handle cache
		dragHandles = new ArrayList();
	}

	/**
	 * Create the CBanner control used to control the horizontal span of the
	 * primary and secondary command areas.
	 * 
	 * @param workbenchComposite
	 *            The workbench acting as the parent of the CBanner
	 */
	public void createCBanner(Composite workbenchComposite) {
		banner = new CBanner(workbenchComposite, SWT.NONE);
		banner.setSimple(false);
		banner.setRightWidth(175);
		banner.setLocation(0, 0);

		// Create the left composite and override its 'computeSize'
		// to delegate to the 'primary' command trim area
		Composite bannerLeft = new Composite(banner, SWT.NONE) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int,
			 *      boolean)
			 */
			public Point computeSize(int wHint, int hHint, boolean changed) {
				// If we're doing a 'real' workbench layout then delegate to the
				// appropriate trim area
				if (WorkbenchLayout.layoutComposite != null) {
					return WorkbenchLayout.this.computeSize(TRIMID_CMD_PRIMARY,
							wHint);
				}

				return super.computeSize(wHint, hHint, changed);
			}
		};
		bannerLeft.setLayout(new LeftBannerLayout());
		bannerLeft.setBackground(workbenchComposite.getDisplay()
				.getSystemColor(SWT.COLOR_DARK_BLUE));
		banner.setLeft(bannerLeft);

		// Create the right hand part of the CBanner
		Composite bannerRight = new Composite(banner, SWT.NONE) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int,
			 *      boolean)
			 */
			public Point computeSize(int wHint, int hHint, boolean changed) {
				// If we're doing a 'real' workbench layout then delegate to the
				// appropriate trim area
				if (WorkbenchLayout.layoutComposite != null) {
					return WorkbenchLayout.this.computeSize(
							TRIMID_CMD_SECONDARY, wHint);
				}

				return super.computeSize(wHint, hHint, changed);
			}
		};
		bannerRight.setBackground(workbenchComposite.getDisplay()
				.getSystemColor(SWT.COLOR_DARK_BLUE));
		banner.setRight(bannerRight);

		// If the right banner control changes size it's because
		// the 'swoop' moved.
		bannerRight.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				Composite leftComp = (Composite) e.widget;
				leftComp.getShell().layout(true);
			}
		});

		// Place the CBanner on the 'bottom' of the z-order
		banner.moveBelow(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 *      int, int, boolean)
	 * 
	 * Note that this is arbitrary since the we're a top level shell (so
	 * computeSize won't be called.
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		Point size = new Point(wHint, hHint);
		if (size.x == SWT.DEFAULT) {
			size.x = 300;
		}
		if (size.y == SWT.DEFAULT) {
			size.y = 300;
		}
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 * 
	 * TODO: Supply a full description of the layout mechanicsS
	 */
	protected void layout(Composite composite, boolean flushCache) {
		layoutComposite = composite;
		clientRect = composite.getClientArea();

		// reset all the drag handles to be invisible
		resetDragHandles();

		// Compute the proper size for each trim area
		// Do Top Right, Top Left, Left, Right then Bottom so the math works out
		if (useCBanner()) {
			banner.moveBelow(null);

			// NOTE: calling 'computeSize' here will, in turn, call the
			// 'computeSize' for the left/right areas, each with the
			// 'correct' width hint...
			Point bannerSize = banner
					.computeSize(clientRect.width, SWT.DEFAULT);

			// set the amount of space used by the 'cmd' areas for use later
			topMax = bannerSize.y;

			banner.setSize(bannerSize);
		} else {
			Point c1Size = computeSize(TRIMID_CMD_PRIMARY, clientRect.width);
			Point c2Size = computeSize(TRIMID_CMD_SECONDARY, clientRect.width);

			// set the amount of space used by the 'cmd' areas for use later
			topMax = c1Size.y + c2Size.y;
		}

		// Now do the vertical areas; their 'major' is whatever is left
		// vertically once the top areas have been computed
		Point v1Size = computeSize(TRIMID_VERTICAL1, clientRect.height - topMax);
		Point v2Size = computeSize(TRIMID_VERTICAL2, clientRect.height - topMax);

		// Finally, the status area's 'major' is whatever is left over
		// horizontally once the vertical areas have been computed.
		computeSize(TRIMID_STATUS, clientRect.width - (v1Size.x + v2Size.x));

		// Now, layout the trim within each area
		// Command primary area
		if (useCBanner()) {
			Point leftLoc = banner.getLeft().getLocation();
			cmdPrimaryTrimArea.areaBounds.x = leftLoc.x;
			cmdPrimaryTrimArea.areaBounds.y = leftLoc.y;
		} else {
			cmdPrimaryTrimArea.areaBounds.x = 0;
			cmdPrimaryTrimArea.areaBounds.y = 0;
		}
		layoutTrim(cmdPrimaryTrimArea, cmdPrimaryTrimArea.areaBounds);

		// Command secondary area
		if (useCBanner()) {
			Point rightLoc = banner.getRight().getLocation();
			cmdSecondaryTrimArea.areaBounds.x = rightLoc.x;
			cmdSecondaryTrimArea.areaBounds.y = rightLoc.y;
		} else {
			cmdSecondaryTrimArea.areaBounds.x = 0;
			cmdSecondaryTrimArea.areaBounds.y = cmdPrimaryTrimArea.areaBounds.height;
		}
		layoutTrim(cmdSecondaryTrimArea, cmdSecondaryTrimArea.areaBounds);

		leftTrimArea.areaBounds.x = 0;
		leftTrimArea.areaBounds.y = topMax;
		layoutTrim(leftTrimArea, leftTrimArea.areaBounds);

		rightTrimArea.areaBounds.x = clientRect.width
				- rightTrimArea.areaBounds.width;
		rightTrimArea.areaBounds.y = topMax;
		layoutTrim(rightTrimArea, rightTrimArea.areaBounds);

		bottomTrimArea.areaBounds.x = leftTrimArea.areaBounds.width;
		bottomTrimArea.areaBounds.y = clientRect.height
				- bottomTrimArea.areaBounds.height;
		layoutTrim(bottomTrimArea, bottomTrimArea.areaBounds);

		// Set the center control's bounds to fill the space remaining
		// after the trim has been arranged
		layoutCenter();
	}

	/**
	 * Indicates whether or not the layout should use the CBanner or tile the
	 * primary and secondary trim areas one above the other.
	 * 
	 * @return <code>true</code> iff the layout should use the CBanner.
	 */
	private boolean useCBanner() {
		return (banner != null && banner.getVisible());
	}

	/**
	 * @param areaId
	 *            The identifier for the TrimArea to return
	 * @return The TrimArea that matches the given areaId
	 */
	private TrimArea getTrimArea(String areaId) {
		if (TRIMID_CMD_PRIMARY.equals(areaId)) {
			return cmdPrimaryTrimArea;
		}
		if (TRIMID_CMD_SECONDARY.equals(areaId)) {
			return cmdSecondaryTrimArea;
		}
		if (TRIMID_VERTICAL1.equals(areaId)) {
			return leftTrimArea;
		}
		if (TRIMID_VERTICAL2.equals(areaId)) {
			return rightTrimArea;
		}
		if (TRIMID_STATUS.equals(areaId)) {
			return bottomTrimArea;
		}

		return null;
	}

	/**
	 * @param areaId
	 *            The TrimArea that we want to get the controls for
	 * @return The list of controls whose TrimLayoutData's areaId matches the
	 *         given areaId parameter
	 */
	private List getTrimContents(String areaId) {
		List trimContents = new ArrayList();
		Control[] children = layoutComposite.getChildren();
		for (int i = 0; i < children.length; i++) {
			// Skip any disposed or invisible widgets
			if (children[i].isDisposed() || !children[i].getVisible()) {
				continue;
			}

			// Only accept children that want to be layed out in a particular
			// trim area
			if (children[i].getLayoutData() instanceof TrimLayoutData) {
				TrimLayoutData tlData = (TrimLayoutData) children[i]
						.getLayoutData();
				if (tlData.areaId.equals(areaId)) {
					trimContents.add(children[i]);
				}
			}
		}

		return trimContents;
	}

	/**
	 * Lays out the center composite once the outer trim areas have all been
	 * done.
	 */
	private void layoutCenter() {
		if (centerComposite != null) {
			// The center is the 'inner' bounding box of the other trim areas
			Rectangle areaBounds = new Rectangle(
					leftTrimArea.areaBounds.x + leftTrimArea.areaBounds.width,
					topMax,
					clientRect.width
							- (leftTrimArea.areaBounds.width + rightTrimArea.areaBounds.width),
					clientRect.height
							- (topMax + bottomTrimArea.areaBounds.height));

			centerComposite.setBounds(areaBounds);
		}
	}

	/**
	 * Computes the size of a trim area given the length of its major dimension.
	 * Depending on whether the trim area is horizontal or vertical one of the
	 * two value will always match the supplied 'majorHint' ('x' if it's
	 * horizontal).
	 * <p>
	 * Effectively, this computes the length of the minor dimension by tiling
	 * the trim area's controls into multiple lines based on the length of the
	 * major dimension.
	 * </p>
	 * 
	 * @param areaId
	 *            The area id to compute the size for
	 * @param majorHint
	 *            The length of the major dimension
	 * 
	 * @return The computed size
	 */
	private Point computeSize(String areaId, int majorHint) {
		TrimArea trimArea = getTrimArea(areaId);
		boolean horizontal = trimArea.orientation == SWT.TOP
				|| trimArea.orientation == SWT.BOTTOM;

		// Gather up all the controls for this area and tile them out
		trimArea.trimContents = getTrimContents(trimArea.areaId);

		// Split the controls list into a list of TrimLines that each fit into
		// the trim area
		trimArea.trimLines = computeWrappedTrim(trimArea, majorHint);

		// Now, calculate the max between the default and the contents
		int minorMax = 0;
		for (Iterator iter = trimArea.trimLines.iterator(); iter.hasNext();) {
			TrimLine curLine = (TrimLine) iter.next();
			minorMax += curLine.minorMax;
		}
		minorMax = Math.max(minorMax, trimArea.defaultMinor);

		// Store the size in the area'a cache...
		Point computedSize = new Point(0, 0);
		if (horizontal) {
			computedSize.x = trimArea.areaBounds.width = majorHint;
			computedSize.y = trimArea.areaBounds.height = minorMax;
		} else {
			computedSize.x = trimArea.areaBounds.width = minorMax;
			computedSize.y = trimArea.areaBounds.height = majorHint;
		}

		// ...and return it
		return computedSize;
	}

	/**
	 * This is where the information required to lay the controls belonging to a
	 * particular trim area out.
	 * <p>
	 * Tile the controls in the trim area into one or more lines. Each line is
	 * guaranteed to take up less than or equal to the 'majorHint' in the major
	 * dimension. The result is a complete cache of the information needed to
	 * lay the controls in the trim area out.
	 * </p>
	 * 
	 * @param trimArea The trim area to create the cache info for
	 * @param majorHint The length of the major dimension
	 * 
	 * @return A List of <code>TrimLine</code> elements
	 */
	private List computeWrappedTrim(TrimArea trimArea, int majorHint) {
		boolean horizontal = trimArea.orientation == SWT.TOP
				|| trimArea.orientation == SWT.BOTTOM;
		// Return a list of 'lines' to tile the control into...
		List lines = new ArrayList();
		TrimLine curLine = new TrimLine();
		lines.add(curLine);
		curLine.minorMax = trimArea.defaultMinor;

		// Init the tilePos to force a 'new' line
		int tilePos = 0;
		for (Iterator iter = trimArea.trimContents.iterator(); iter.hasNext();) {
			Control control = (Control) iter.next();
			TrimLayoutData td = (TrimLayoutData) control.getLayoutData();

			Point prefSize;
			if (horizontal) {
				//prefSize = control.computeSize(majorHint, SWT.DEFAULT);
				prefSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			} else {
				prefSize = control.computeSize(SWT.DEFAULT, majorHint);
			}

			// Will this control fit onto the current line?
			int curTileSize = horizontal ? prefSize.x : prefSize.y;

			// every control except the first one needs to include the 'spacing'
			// in the calc
			if (curLine.controls.size() > 0) {
				curTileSize += spacing;
			}

			// If the control can be re-positioned then we have to add a drag
			// handle to it
			// we have to include the space that it'll occupy in the calcs
			if (td.listener != null) {
				curTileSize += horizontal ? horizontalHandleSize
						: verticalHandleSize;
			}

			// Place the control into the 'current' line if it'll fit (or if
			// it's the
			// -first- control (this handles the case where a control is too
			// large to
			// fit into the current TrimArea's 'major' size.
			if ((tilePos + curTileSize) <= majorHint
					|| curLine.controls.size() == 0) {
				curLine.controls.add(control);

				// Cache the maximum amount of 'minor' space needed
				int minorSize = horizontal ? prefSize.y : prefSize.x;
				if (minorSize > curLine.minorMax) {
					curLine.minorMax = minorSize;
				}

				tilePos += curTileSize;
			} else {
				// Remember how much space was left on the current line
				curLine.extraSpace = majorHint - tilePos;

				// We need a new line...
				curLine = new TrimLine();
				lines.add(curLine);

				curLine.controls.add(control);

				// Cache the maximum amount of 'minor' space needed
				int minorSize = horizontal ? prefSize.y : prefSize.x;
				if (minorSize > curLine.minorMax) {
					curLine.minorMax = minorSize;
				}

				tilePos = curTileSize;
			}

			// Count how many 'resizable' controls there are
			if ((td.flags & TrimLayoutData.GROWABLE) != 0) {
				curLine.resizableCount++;
			}
		}

		// Remember how much space was left on the current line
		curLine.extraSpace = majorHint - tilePos;

		return lines;
	}

	private void layoutTrim(TrimArea trimArea, Rectangle areaBounds) {
		boolean horizontal = trimArea.orientation == SWT.TOP
				|| trimArea.orientation == SWT.BOTTOM;

		// How much space do we have to 'tile' into?
		int areaMajor = horizontal ? areaBounds.width : areaBounds.height;

		// Get the tiled 'List of Lists' for the trim
		List tileAreas = trimArea.trimLines;

		// Tile each 'line' into the trim
		int tilePosMinor = horizontal ? areaBounds.y : areaBounds.x;
		for (Iterator areaIter = tileAreas.iterator(); areaIter.hasNext();) {
			TrimLine curLine = (TrimLine) areaIter.next();

			// Compute how much space to give to 'resizable' controls. Note that
			// we can end up computing -negative- 'extraSpace' values if the
			// control's
			// preferred 'major' size is actually > the 'major' size for the
			// trim area
			int resizePadding = 0;
			if (curLine.resizableCount > 0 && curLine.extraSpace > 0) {
				resizePadding = curLine.extraSpace / curLine.resizableCount;
			}

			// Tile each line
			int tilePosMajor = horizontal ? areaBounds.x : areaBounds.y;
			for (Iterator iter = curLine.controls.iterator(); iter.hasNext();) {
				Control control = (Control) iter.next();
				TrimLayoutData td = (TrimLayoutData) control.getLayoutData();
				Point prefSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);

				int major = horizontal ? prefSize.x : prefSize.y;
				int minor = horizontal ? prefSize.y : prefSize.x;

				// Ensure that controls that are too wide for the area get
				// 'clipped'
				if (major > areaMajor) {
					major = areaMajor;
				}

				// If desired, extend the 'minor' size of the control to fill
				// the area
				if ((td.flags & TrimLayoutData.GRAB_EXCESS_MINOR) != 0) {
					minor = curLine.minorMax;
				}

				// If desired, extend the 'major' size of the control to fill
				// the area
				if ((td.flags & TrimLayoutData.GROWABLE) != 0) {
					major += resizePadding;
				}

				// If we have to show a drag handle then do it here
				if (td.listener != null && createHandles()) {
					TrimCommonUIHandle handle = getDragHandle(trimArea.orientation);
					// handle.setControl(control);

					if (horizontal) {
						handle.setBounds(tilePosMajor, tilePosMinor,
								getHandleSize(true), curLine.minorMax);
					} else {
						handle.setBounds(tilePosMinor, tilePosMajor,
								curLine.minorMax, getHandleSize(false));
					}

					tilePosMajor += horizontal ? getHandleSize(true)
							: getHandleSize(false);
				}

				// Now, lay out the actual control
				switch (trimArea.orientation) {
				case SWT.TOP:
					control.setBounds(tilePosMajor, tilePosMinor, major, minor);
					break;
				case SWT.LEFT:
					control.setBounds(tilePosMinor, tilePosMajor, minor, major);
					break;
				case SWT.RIGHT:
					// Here we want to tile the control to the RIGHT edge
					int rightEdge = tilePosMinor + curLine.minorMax;
					control.setBounds(rightEdge - minor, tilePosMajor, minor,
							major);
					break;
				case SWT.BOTTOM:
					// Here we want to tile the control to the RIGHT edge
					int bottomEdge = tilePosMinor + curLine.minorMax;
					control.setBounds(tilePosMajor, bottomEdge - minor, major,
							minor);
					break;
				}

				// Ensure that the control is above the trim control
				tilePosMajor += major + spacing;
			}
			tilePosMinor += curLine.minorMax;
			tilePosMajor = horizontal ? areaBounds.x : areaBounds.y;
		}
	}

	/**
	 * HACK>>>Remove if found in the wild...
	 * @return
	 */
	private boolean createHandles() {
		return false;
	}

	private void resetDragHandles() {
		for (Iterator iter = dragHandles.iterator(); iter.hasNext();) {
			// TrimCommonUIHandle handle = (TrimCommonUIHandle) iter.next();
			// handle.setControl(null);
		}
	}

	private TrimCommonUIHandle getDragHandle(int orientation) {
		// boolean horizontal = orientation == SWT.TOP
		// || orientation == SWT.BOTTOM;

		for (Iterator iter = dragHandles.iterator(); iter.hasNext();) {
			TrimCommonUIHandle handle = (TrimCommonUIHandle) iter.next();
			// if (handle.toDrag == null && handle.horizontal == horizontal)
			return handle;
		}

		// If we get here then we haven't found a new drag handle so create one
		System.out.println("new Handle"); //$NON-NLS-1$
		// TrimCommonUIHandle newHandle = new
		// TrimCommonUIHandle(layoutComposite,
		// horizontal);
		// dragHandles.add(newHandle);
		// return newHandle;
		return null;
	}

	/**
	 * Return the SWT side that the trim area is on
	 * 
	 * @param areaId The id of the area to get the orientation of
	 * 
	 * @return The SWT  side corresponding that the given area
	 */
	public static int getOrientation(String areaId) {
		if (TRIMID_CMD_PRIMARY.equals(areaId)) {
			return SWT.TOP;
		}
		if (TRIMID_CMD_SECONDARY.equals(areaId)) {
			return SWT.TOP;
		}
		if (TRIMID_VERTICAL1.equals(areaId)) {
			return SWT.LEFT;
		}
		if (TRIMID_VERTICAL2.equals(areaId)) {
			return SWT.RIGHT;
		}
		if (TRIMID_STATUS.equals(areaId)) {
			return SWT.BOTTOM;
		}

		return SWT.NONE;
	}

	/**
	 * Calculate a size for the handle that will be large enough to show the
	 * CoolBar's drag affordance.
	 * 
	 * @return The size that the handle has to be, based on the orientation
	 */
	private int getHandleSize(boolean horizontal) {
		// Do we already have a 'cached' value?
		if (horizontal && horizontalHandleSize != -1) {
			return horizontalHandleSize;
		}

		if (!horizontal && verticalHandleSize != -1) {
			return verticalHandleSize;
		}

		// Must be the first time, calculate the value
		CoolBar bar = new CoolBar(layoutComposite, horizontal ? SWT.HORIZONTAL
				: SWT.VERTICAL);

		CoolItem item = new CoolItem(bar, SWT.NONE);

		Label ctrl = new Label(bar, SWT.PUSH);
		ctrl.setText("Button 1"); //$NON-NLS-1$
		Point size = ctrl.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		Point ps = item.computeSize(size.x, size.y);
		item.setPreferredSize(ps);
		item.setControl(ctrl);

		bar.pack();

		// OK, now the difference between the location of the CB and the
		// location of the
		Point bl = ctrl.getLocation();
		Point cl = bar.getLocation();

		// Toss them now...
		ctrl.dispose();
		item.dispose();
		bar.dispose();

		// The 'size' is the difference between the start of teh CoolBar and
		// start of its first control
		int length;
		if (horizontal) {
			length = bl.x - cl.x;
			horizontalHandleSize = length;
		} else {
			length = bl.y - cl.y;
			verticalHandleSize = length;
		}

		return length;
	}

}
