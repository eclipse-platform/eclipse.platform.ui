/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.layout.*;

/**
 * Arranges a set of controls along the top of a CTabFolder, and
 * computes the position of the tab folder's client area. The normal usage is this:
 * 
 * <ul>
 * <li>Attach a TabFolderLayout to a CTabFolder.</li>
 * <li>Attach a set of trim controls to the TabFolderLayout</li>
 * <li>Each time the folder is moved or resized, do the following</li>
 * <li>call layout() to reposition the trim controls</li>
 * <li>Reposition the contents of the folder's client area by calling TabFolderLayout.getClientArea()</li> 
 * </ul>
 * 
 * <p>
 * If possible, the controls are right-aligned along the title bar of the tab folder.
 * If there isn't enough room without compressing or hiding tabs, the trim controls
 * are moved to the top of the client area. 
 * </p> 
 * <p>
 * Note that this isn't really an SWT Layout (it can't be, since
 * the controls it arranges are siblings of the tab folder rather than children). However, we
 * use the name "Layout" to indicate that the function of this object is to arrange controls.
 * </p>
 * 
 * @since 3.0
 */
public class TabFolderLayout {
	private CTabFolder tabFolder;
	private Control[] topControls;
	private LayoutCache cache;
	private Rectangle centerArea = new Rectangle(0,0,0,0);
	private int trimStart;
	private boolean trimOnTop = false;
	private int leftAligned = 0;
	
	/**
	 * Creates a new TabFolderLayout which will arrange controls along the title
	 * bar of the given folder.
	 * 
	 * @param folder the folder where controls will be arranged.
	 */
	public TabFolderLayout(CTabFolder folder) {
		this.tabFolder = folder;
		
		setTopRight(new Control[0]);
	}
	
	/**
	 * Should be invoked when this object is no longer needed (cleans up reasources)
	 */
	public void dispose() {
	}
	
	/**
	 * Returns the controls that are currently being arranged by this layout.
	 * 
	 * @return an array of controls currently being arranged by this layout (not null)
	 */
	public Control[] getTopRight() {
		return topControls;
	}

	/**
	 * Returns the bounds within which the current page of the tab folder should be
	 * drawn. The resulting rectangle is in the coordinate system of the tab folder's 
	 * parent. It takes into account any space needed for the trim controls. The
	 * result is accurate as of the last call to layout().
	 * 
	 * @return the bounds of the current tab folder page 
	 */
	public Rectangle getClientBounds() {
		return centerArea;
	}
	
	/**
	 * The current position on the tab folder's title bar where the trim widgets
	 * start. Returns the rightmost side of the folder if there are no widgets
	 * currently on the title bar.
	 * 
	 * @return a position (pixels) relative to the leftmost-side of the tab folder
	 */
	public int getTrimStart() {
		return trimStart;
	}
	
	/**
	 * Sets the number of controls that will be left-aligned. If nonzero, the first
	 * n controls will be left-aligned. 
	 * 
	 * @param leftAligned
	 */
	public void setNumLeftAligned(int leftAligned) {
		this.leftAligned = leftAligned;
	}
	
	/**
	 * Returns true iff the trim widgets were positioned on the title bar in the
	 * last call to layout(). Returns false if the widgets were positioned within
	 * the client area.
	 * 
	 * @return true iff the trim widgets are currently on the title bar
	 */
	public boolean isTrimOnTop() {
		return trimOnTop;
	}
	
	/**
	 * Sets the list of controls to be placed at the top-right of the CTabFolder
	 * 
	 * @param topRight a list of Control
	 */
	public void setTopRight(Control[] upperRight) {
		topControls = upperRight;
		
		cache = new LayoutCache(upperRight);
	}
	
	/**
	 * Arranges all the controls in this layout. All "get" methods in this
	 * object update their result each time this method is called.
	 */
	public void layout() {
		cache.flush();		
		
		// Initialize variables that will be needed for both cases
		Rectangle bounds = tabFolder.getBounds();
		trimStart = bounds.width;
		Rectangle clientBounds = calculatePageBounds(tabFolder);
		
		// Determine if the controls will fit on the top
		Rectangle trimRegion = getTitleTrimRegion();
		Point trimSize = computeTrimSize(leftAligned, topControls.length - leftAligned);
		
		trimOnTop = trimSize.x <= trimRegion.width && trimSize.y <= trimRegion.height;
		
		int idx = 0;
		int lastBottomControl = topControls.length;
		
		// Check if we have room for all our topRight controls on the top border
		if (trimOnTop) {
						
			trimStart = align(leftAligned, topControls.length - leftAligned, trimRegion, true) - bounds.x;

			centerArea = clientBounds;
			
			lastBottomControl = leftAligned;
		}
		
		// Else we need to place the controls below the title
		int leftMargin = 2;
		
		Rectangle currentRect = new Rectangle(clientBounds.x + leftMargin, clientBounds.y, 
				clientBounds.width - leftMargin, 0);
		
		while (idx < lastBottomControl) {
			int startOfRow = idx;
			currentRect.height = 0;
			int rowWidth = 0;
			
			int rowCount = 0;
			while (idx + rowCount < lastBottomControl) {
				Point nextSize = cache.computeSize(idx + rowCount, SWT.DEFAULT, SWT.DEFAULT);
				
				rowWidth += nextSize.x;
				
				if (rowWidth > clientBounds.width) {
					break;
				}
				
				currentRect.height = Math.max(currentRect.height, nextSize.y);
				
				rowCount++;
			}
			
			if (rowCount > 0) {
				align(idx, rowCount, currentRect, false);
				idx += rowCount;
			} else {
				Point size = cache.computeSize(idx, clientBounds.width, SWT.DEFAULT);

				currentRect.height = size.y;
				topControls[idx].setBounds(currentRect);
				
				idx++;
			}
			
			currentRect.y += currentRect.height;
		}
		
		centerArea = new Rectangle(clientBounds.x, currentRect.y, clientBounds.width, 
					clientBounds.height + clientBounds.y - currentRect.y);
	}
	
	/**
	 * Right-alignes a subset of the top controls in the given region.
	 * 
	 * @param firstControl the index of the leftmost control to arrange
	 * @param numControls number of controls to arrange
	 * @param region region in which to arrange the controls
	 */
	private int align(int firstControl, int numControls, Rectangle region, boolean centerAlign) {
		int lastLeftAligned = Math.max(firstControl, Math.min(firstControl + numControls, leftAligned));
		
		Point leftSize = computeTrimSize(firstControl, lastLeftAligned - firstControl);
		Point rightSize = computeTrimSize(lastLeftAligned, numControls + firstControl - lastLeftAligned);
		
		int currentPos = region.x;
		
		Rectangle bounds = tabFolder.getBounds();
		
		currentPos = region.x;
		 
		int start;
		
		if (centerAlign) {
			int minimum = bounds.x + (bounds.width - leftSize.x) / 2;
			int maximum = region.x + region.width - rightSize.x - leftSize.x;
			currentPos = Math.min(Math.max(minimum, region.x), maximum);
		}
		
		if (firstControl < lastLeftAligned) {
			start = currentPos;
		} else {
			start = region.x + region.width - rightSize.x;
		}
		
		// Arrange left-aligned controls (if any)
		int leftAligned = firstControl;
		for (; leftAligned < lastLeftAligned; leftAligned++) {
			Point size = cache.computeSize(leftAligned, SWT.DEFAULT, SWT.DEFAULT);
			
			topControls[leftAligned].setBounds(currentPos, region.y + (region.height - size.y) / 2, size.x, size.y);
			
			currentPos += size.x;
		}
		
		// Arrange right-aligned controls
		currentPos = region.x + region.width;
		
		for (int idx = firstControl + numControls - 1; idx >= leftAligned; idx--) {
			Point size = cache.computeSize(idx, SWT.DEFAULT, SWT.DEFAULT);
			
			topControls[idx].setBounds(currentPos - size.x, region.y + (region.height - size.y) / 2, size.x, size.y);

			currentPos -= size.x;
		}
		
		return start;
	}
	
	
	/**
	 * Returns the region in the title where a toolbar could be rendered in coordinates
	 * relative to the tab folder's parent.
	 * 
	 * @return
	 */
	protected Rectangle getTitleTrimRegion() {
		Rectangle result = new Rectangle(0,0,0,0); 
		
		int itemCount = tabFolder.getItemCount(); 
		if (itemCount > 0) {
			CTabItem item = tabFolder.getItem(itemCount - 1);
			
			Rectangle itemBounds = item.getBounds();
			
			result.x = itemBounds.x + itemBounds.width;
			result.height = itemBounds.height;
			result.y = itemBounds.y;
			result.width = getAvailableSpace(tabFolder);
		}
		
		Rectangle bounds = tabFolder.getBounds();
		
		result.x += bounds.x;
		result.y += bounds.y;
		
		int borderSize = 1;
		result.y += borderSize;
		result.height = tabFolder.getTabHeight() - differenceBetweenTabHeightAndTrimRegion();
		
		// Amount to shift to avoid stomping on the curve
		int xShift = 5;
		result.x += xShift;
		result.width -= xShift;
		
		return result;
	}
	
	public int differenceBetweenTabHeightAndTrimRegion() {
		return 1;
	}
	
	/**
	 * Computes the maximium size available for the trim controls without causing
	 * tabs to disappear.
	 * 
	 * @param folder
	 * @return the amount of empty space to the right of the tabs
	 */
	protected static int getAvailableSpace(CTabFolder folder) {
		int available = folder.getBounds().width;
		
		available -= 2 * folder.getBorderWidth();
		available -= folder.getChevronBounds().width;
		available -= folder.getMaximizeBounds().width;
		available -= folder.getMinimizeBounds().width;
		
		// Add a safety margin to avoid stomping on the curve
		available -= 10;
		
		CTabItem[] tabs = folder.getItems();
		for (int idx = 0; idx < tabs.length; idx++) {
			CTabItem item = tabs[idx];
			
			if (!item.isShowing()) {
				return 0;
			}
			
			available -= item.getBounds().width;
		}
		
		return Math.max(0, available);
	}
		
	/**
	 * Returns the total preferred width of the top controls
	 * 
	 * @return the total preferred width of the top controls
	 */
	protected Point computeTrimSize(int start, int length) {
		int width = 0;
		int height = 0;
		int last = start + length;
		for (int idx = start; idx < last; idx++) {
			Point next = cache.computeSize(idx, SWT.DEFAULT, SWT.DEFAULT);
			width += next.x;
			height = Math.max(height, next.y);
		}
		
		return new Point(width, height);
	}
	
	private static Rectangle calculatePageBounds(CTabFolder folder) {
		if (folder == null)
			return new Rectangle(0, 0, 0, 0);
		Rectangle bounds = folder.getBounds();
		Rectangle offset = folder.getClientArea();
		bounds.x += offset.x;
		bounds.y += offset.y;
		bounds.width = offset.width;
		bounds.height = offset.height;
		return bounds;
	}	

}
