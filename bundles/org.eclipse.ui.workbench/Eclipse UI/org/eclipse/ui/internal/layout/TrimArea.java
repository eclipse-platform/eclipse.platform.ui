/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.WindowTrimProxy;

/**
 * Represents one Trim Area.
 * 
 * @since 3.2
 */
public class TrimArea {
	
	/**
	 * This is a convenience class that caches information for a single 'tiled'
	 * line of trim.
	 * 
	 * @since 3.2
	 * 
	 */
	private class TrimLine {
		/** The list of controls in this trim line */
		List controls = new ArrayList();

		/** In horizontal terms this is the 'height' of the tallest control. */
		int minorMax; // Default minimum for empty trim areas

		/** The number of controls that can 'grow' to use leftover space */
		int resizableCount;

		/** The amount of unused space in the line */
		int availableSpace;

		/**
		 * @param majorHint The total amount of space available to tile into
		 */
		public TrimLine(int majorHint) {
			availableSpace = majorHint;
		}

		/**
		 * Add a new control to the current line.
		 * 
		 * @param ctrl The control to add
		 * @param minorSize it's size in the minor dimension
		 * @param dragHandle its drag handle (if any)
		 */
		void addControl(Control ctrl, int tileLength, int minorSize, Control dragHandle) {
			if (dragHandle != null) {
				controls.add(dragHandle);
			}
			
			controls.add(ctrl);
			
			// If the control is re-sizable then we use the default size as its 'minimum' size
			if (getData(ctrl).isResizeable()) {
				resizableCount++;
			}

			// Cache the maximum amount of 'minor' space needed
			if (minorSize > minorMax) {
				minorMax = minorSize;
			}
			
			// Adjust the amount of remaining space
			availableSpace -= tileLength;
		}
		
		/**
		 * Terminates a line by calculating the remaining space and fixing the
		 * minorMax if there was a CBanner in the line.
		 * 
		 * @return The total amount of 'minor' space required for this line
		 */
		int terminate() {
			// OK, now if there was a banner in the current line we'll re-compute
			// its height based on the actual width we'll be displaying it at
			for (Iterator ctrls = controls.iterator(); ctrls.hasNext();) {
				Control ctrl = (Control) ctrls.next();
				if (ctrl instanceof CBanner) {
					CBanner banner = (CBanner) ctrl;
					Point bannerPrefSize = (Point) banner.getData(PREFSIZE_DATA_ID);
					int realWidth = bannerPrefSize.x + (availableSpace / resizableCount);
					Point cbSize = banner.computeSize(realWidth, SWT.DEFAULT);
					banner.setData(PREFSIZE_DATA_ID, new Point(bannerPrefSize.x, cbSize.y));
					
					// Update the minor size if necessary
					if (cbSize.y > minorMax)
						minorMax = cbSize.y;
				}
			}
			
			return minorMax;
		}
	}

	// this is no longer necessary, since every piece of window trim defined
	// itself as the trim layout data.
	private static final IWindowTrim defaultData = new WindowTrimProxy(null, null, null, 0, true);

	private static IWindowTrim getData(Control control) {
		IWindowTrim data = (IWindowTrim) control.getLayoutData();
		if (data == null) {
			data = defaultData;
		}

		return data;
	}

	/** Our area ID. */
	private int fId;

	/** An NLS display name. */
	private String fDisplayName;

	/** The last 'tiled' set of trim lines */
	private List lines = new ArrayList();
	
	/** Each trimArea is an ordered list of TrimDescriptors. */
	private ArrayList fTrim;

	// layout constants
	private static final String PREFSIZE_DATA_ID = "prefSize"; //$NON-NLS-1$
	private static final int MIN_BANNER_LEFT = 150;
	private static int TILE_SPACING = 2;
	private static int LINE_SPACING = 2;
	
	private Rectangle curRect = new Rectangle(0,0,0,0);

	/**
	 * Create the trim area with its ID.
	 * 
	 * @param id
	 * @param displayName
	 *            the NLS display name
	 */
	public TrimArea(int id, String displayName) {
		fTrim = new ArrayList();
		fId = id;
		fDisplayName = displayName;
	}

	/**
	 * return true of the trim area is empty
	 * 
	 * @return <code>true</code>
	 */
	public boolean isEmpty() {
		return fTrim.isEmpty();
	}

	/**
	 * @return The rectangle currently occupied by this trim area
	 */
	public Rectangle getCurRect() {
		return curRect;
	}
	
	/**
	 * Return the ordered list of trim for this area.
	 * 
	 * @return a List containing IWindowTrim
	 */
	public List getTrims() {
		List trim = new ArrayList(fTrim.size());
		Iterator d = fTrim.iterator();

		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			trim.add(desc.getTrim());
		}
		return trim;
	}

	/**
	 * Return the ordered list of trim descriptors for this area.
	 * 
	 * @return a List containing TrimDescriptor
	 */
	public List getDescriptors() {
		return (List) fTrim.clone();
	}
	
	/**
	 * Calculates the correct 'preferred size' of the given control.
	 * For controls that cannot be resized (i.e. stretched) the preferred
	 * size is simply the control's current size. For controls that can be
	 * stretched it represents the minimum size that the control can have
	 * before being tiled onto a separate line.
	 * 
	 * This method also managed the two stretch-able controls known by
	 * the Workbench; the StatusLine and the CBanner. These controls
	 * require specialized help (i.e. hacks) since their respective
	 * <code>computeSize</code> methods cannot correctly compute their
	 * preferred size.
	 * 
	 * @param ctrl The control to get the preferred size for
	 * @return The preferred size of the given control
	 */
	private Point getPrefSize(Control ctrl) {
		// A control's prefSize -is- its current size
		Point prefSize = ctrl.getSize();

		// If the control has not yet sized itself then initialize it to its preferred size
		if ((prefSize.x == 0 || prefSize.y == 0) && !getData(ctrl).isResizeable()) {
			prefSize = ctrl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			ctrl.setSize(prefSize);
		}
		
		// If the control is re-sizable then we use the default size as its 'minimum' size
		if (getData(ctrl).isResizeable()) {
			// Special case: we allow sufficient room to ensure that the right area,
			// the 'swoop' and some constant size for the CoolBar are available.
			// The height is set to zero but is re-calculated after the true width that
			// the banner will occupy is calculated
			if (ctrl instanceof CBanner) {
				CBanner banner = (CBanner) ctrl;
				prefSize.x = banner.getRightWidth() + banner.getBorderWidth() + MIN_BANNER_LEFT;
				prefSize.y = 0;  // No height for now, computed later
			}
			else if (getData(ctrl).getId().equals("org.eclipse.jface.action.StatusLineManager")) { //$NON-NLS-1$
				// Hack !! should fix StatusLine itself
				// We have to force the StatusLine to have a consistent
				// preferred width (it's computeSize takes the message into
				// account
				prefSize = new Point(250, 26);
			}
			else {
				// Normal control, expect it to return its true preferred size 
				prefSize = ctrl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			}
			
			// Cache the computed  preferred size
			ctrl.setData(PREFSIZE_DATA_ID, prefSize);
		}
		
		return prefSize;
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
	 * @param majorHint The length of the major dimension
	 * 
	 * @return A List of <code>TrimLine</code> elements
	 */
	public int computeWrappedTrim(int majorHint) {
		int totalMinor = 0;
		
		// Remove any previous tiling information
		lines.clear();
		
		boolean isHorizontal = !isVertical();

		TrimLine curLine = new TrimLine(majorHint);
		lines.add(curLine);

		TrimCommonUIHandle dragHandle = null;
		
		// Initialize the tilePos to force a 'new' line
		List caches = getCaches();
		for (Iterator cacheIter = caches.iterator(); cacheIter.hasNext();) {
			SizeCache cache = (SizeCache) cacheIter.next();
			Control ctrl = cache.getControl();
			
			// Skip invisible trim
			if (ctrl == null || !ctrl.isVisible())
				continue;
			
			// We need to keep the drag handle and the 'real' trim on the same line... 
			if (ctrl instanceof TrimCommonUIHandle) {
				dragHandle = (TrimCommonUIHandle) ctrl;

				// Ensure that the dragHandle is sized correctly
				Point dhSize = dragHandle.getSize();
				if (dhSize.x == 0 || dhSize.y == 0)
					dragHandle.setSize(dragHandle.computeSize(SWT.DEFAULT, SWT.DEFAULT));

				continue;
			}
			
			// A control's prefSize -is- its current size
			Point prefSize = getPrefSize(ctrl);

			// Will this control fit onto the current line?
			int tileLength = isHorizontal ? prefSize.x : prefSize.y;
			int minorSize = isHorizontal ? prefSize.y : prefSize.x;

			// Including its drag handle?
			if (dragHandle != null) {
				Point dhSize = dragHandle.getSize();
				tileLength += isHorizontal ? dhSize.x : dhSize.y;
			}
			
			// Space out the controls
			tileLength += TILE_SPACING;

			// Place the control into the 'current' line if it'll fit or if
			// it's the -first- control (this handles the case where a control is too
			// large to fit into the current TrimArea's 'major' size.)
			//if ((tilePos + tileLength) <= majorHint	|| curLine.controls.size() == 0) {
			if (tileLength < curLine.availableSpace	|| curLine.controls.size() == 0) {
				curLine.addControl(ctrl, tileLength, minorSize, dragHandle);
			} else {
				totalMinor += curLine.terminate();
				
				// We need a new line...
				curLine = new TrimLine(majorHint);
				lines.add(curLine);
				
				curLine.addControl(ctrl, tileLength, minorSize, dragHandle);
			}
			
			// If we get here then we've already handled the drag handle
			dragHandle = null;
		}

		// Remember how much space was needed to tile -all- the lines
		totalMinor += curLine.terminate();

		// Finally, add enough room to provide spacing between the lines
		totalMinor += (lines.size() + 1) * LINE_SPACING;
		
		return totalMinor;
	}

	static int tileCount = 0;
	/**
	 * Re-position and, in the case of re-sizable trim, re-size the
	 * controls in the trim based on the information cached in the
	 * last call to 'computeWrappedTrim'.
	 * 
	 * @param anchorX The X position to start tiling
	 * @param anchorY The Y position to start tiling
	 * @param major The length of the trim area in the major dimension
	 */
	public void tileTrim(int anchorX, int anchorY, int major) {
		// Capture the location of the tiled rectangle
		curRect.x = anchorX;
		curRect.y = anchorY;
		
		boolean isHorizontal = !isVertical();

		int tileX = anchorX;
		int tileY = anchorY;
		
		if (isHorizontal) {
			tileX += TILE_SPACING;
			tileY += LINE_SPACING;
		}
		else {
			tileY += TILE_SPACING;
			tileX += LINE_SPACING;
		}
		
		for (Iterator lineIter = lines.iterator(); lineIter.hasNext();) {
			TrimLine line = (TrimLine) lineIter.next();
			
			int curExtraSpace = line.availableSpace;
			int curResizeCount = line.resizableCount;
			for (Iterator ctrlIter = line.controls.iterator(); ctrlIter.hasNext();) {
				Control ctrl = (Control) ctrlIter.next();
				
				// Make the control the correct size
				Point prefSize = ctrl.getSize();
				if (getData(ctrl).isResizeable() && curResizeCount > 0) {
					// -DONT- trash the actual cached size !!
					Point cachedPrefSize = (Point) ctrl.getData(PREFSIZE_DATA_ID);
					prefSize.x = cachedPrefSize.x;
					prefSize.y = cachedPrefSize.y;
					
					int resizeAmount = curExtraSpace/curResizeCount;
					if (isHorizontal)
						prefSize.x += resizeAmount;
					else
						prefSize.y += resizeAmount;
					
					curExtraSpace -= resizeAmount;
					curResizeCount--;
					
					ctrl.setSize(prefSize);
				}
				
				// Now, position the control
				ctrl.setLocation(tileX, tileY);

				// adjust for the control
				if (isHorizontal)
					tileX += prefSize.x;
				else
					tileY += prefSize.y;
				
				// Adjust the TILE_SPACING (unless it's a handle)
				if (!(ctrl instanceof TrimCommonUIHandle)) {
					if (isHorizontal)
						tileX += TILE_SPACING;
					else
						tileY += TILE_SPACING;
				}
			}
			
			if (isHorizontal) {
				tileY += (line.minorMax + LINE_SPACING);
				tileX = anchorX + TILE_SPACING;
			}
			else {
				tileX += (line.minorMax + LINE_SPACING);
				tileY = anchorY + TILE_SPACING;
			}
		}
		
		// capture the bounds of the tiled area
		if (isHorizontal) {
			curRect.width = major;
			curRect.height = tileY - anchorY;
		}
		else {
			curRect.width = tileX - anchorX;
			curRect.height = major;
		}
	}
	
	/**
	 * return true if this area orientation is vertical.
	 * 
	 * @return <code>true</code>
	 */
	public boolean isVertical() {
		return fId == SWT.LEFT || fId == SWT.RIGHT;
	}

	/**
	 * The ID for this area.
	 * 
	 * @return the ID.
	 */
	public int getId() {
		return fId;
	}

	/**
	 * The NLS display name for this area.
	 * 
	 * @return the String display name.
	 */
	public String getDisplayName() {
		return fDisplayName;
	}

	/**
	 * Add the descriptor representing a piece of trim to this trim area.
	 * 
	 * @param desc
	 *            the trim descriptor
	 */
	public void addTrim(TrimDescriptor desc) {
		fTrim.add(desc);
	}

	/**
	 * Insert this desc before the other desc.  If beforeMe is not
	 * part of this area it just defaults to an add.
	 * 
	 * @param desc
	 *            the window trim
	 * @param beforeMe
	 *            before this trim
	 */
	public void addTrim(TrimDescriptor desc, TrimDescriptor beforeMe) {
		int idx = fTrim.indexOf(beforeMe);
		if (idx == -1) {
			fTrim.add(desc);
		} else {
			ListIterator i = fTrim.listIterator(idx);
			i.add(desc);
		}
	}

	/**
	 * Remove the descriptor representing a piece of trim from this trim area.
	 * 
	 * @param desc
	 *            the trim descriptor
	 */
	public void removeTrim(TrimDescriptor desc) {
		fTrim.remove(desc);
	}

	/**
	 * Does this area contain a piece of trim.
	 * 
	 * @param desc
	 *            the trim
	 * @return <code>true</code> if we contain the trim.
	 */
	public boolean contains(TrimDescriptor desc) {
		return fTrim.contains(desc);
	}

	/**
	 * Takes the trim area and turns it into an List of {@link SizeCache}.
	 * There can be more items in the return list than there are trim
	 * descriptors in the area.
	 * 
	 * @return a list of {@link SizeCache}
	 */
	public List getCaches() {
		ArrayList result = new ArrayList(fTrim.size());
		Iterator d = fTrim.iterator();
		while (d.hasNext()) {
			TrimDescriptor desc = (TrimDescriptor) d.next();
			if (desc.getDockingCache() != null) {
				result.add(desc.getDockingCache());
			}
			result.add(desc.getCache());
		}
		return result;
	}
}
