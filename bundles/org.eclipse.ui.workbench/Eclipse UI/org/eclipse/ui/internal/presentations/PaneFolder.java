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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.layout.SizeCache;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This class implements the tab folders that contains can contain two toolbars and
 * status text. Wherever possible, the toolbars are aligned with the tabs. 
 * If there is not enough room beside the tabs, the toolbars are aligned with the status text. This
 * is the same tab folder that is used to arrange views and editors in Eclipse. 
 * <p>
 * Although it is not actually a control, the public interface is much like 
 * an SWT control. Implementation-wise, this is actually a combination of a CTabFolder and 
 * a ViewForm. It encapsulates the details of moving the toolbar between the CTabFolder and
 * the ViewForm, and provides a simpler interface to the ViewForm/CTabFolder. 
 * </p>
 * 
 * @since 3.0
 */
public final class PaneFolder {	
	// Tab folder and associated proxy controls
	private CTabFolder tabFolder;
	private Control titleAreaProxy;	
	
	// View form and associated proxy controls
	private ViewForm viewForm;
	
	private ProxyControl contentProxy;	
	private ProxyControl viewFormTopLeftProxy;
	private ProxyControl viewFormTopRightProxy;
	private ProxyControl viewFormTopCenterProxy;
	
	// Cached sizes of the top-right and top-center controls
	private SizeCache topRightCache = new SizeCache();
	private SizeCache topCenterCache = new SizeCache();
	private SizeCache topLeftCache = new SizeCache();
	
	private int tabPosition;

	private boolean putTrimOnTop = true;
	private int trimStart = 0;
		
	/**
	 * List of PaneFolderButtonListener
	 */
	private List buttonListeners = new ArrayList(1);

	private int state = IStackPresentationSite.STATE_RESTORED;
	
	/**
	 * State of the folder at the last mousedown event. This is used to prevent
	 * a mouseup over the minimize or maximize buttons from undoing a state change 
	 * that was caused by the mousedown.
	 */
	private int mousedownState = -1;

	// Background colours
	// TODO: Initialize these to sensible (non-null!) defaults
	private Color backgroundGradientStart;
	private Color backgroundGradientEnd;
	private Color background;
	
	// CTabFolder listener
	private CTabFolder2Adapter expandListener = new CTabFolder2Adapter() {
		public void minimize(CTabFolderEvent event) {
			event.doit = false;
			notifyButtonListeners(IStackPresentationSite.STATE_MINIMIZED);
		}
		
		public void restore(CTabFolderEvent event) {
			event.doit = false;
			notifyButtonListeners(IStackPresentationSite.STATE_RESTORED);
		}
		
		public void maximize(CTabFolderEvent event) {
			event.doit = false;
			notifyButtonListeners(IStackPresentationSite.STATE_MAXIMIZED);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.custom.CTabFolder2Adapter#close(org.eclipse.swt.custom.CTabFolderEvent)
		 */
		public void close(CTabFolderEvent event) {
			event.doit = false;
			notifyCloseListeners((CTabItem)event.item);
		}
		
		public void showList(CTabFolderEvent event) {
			notifyShowListeners(event);
		}
		
	};
	
	private MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			mousedownState = getState();
		}
		
		public void mouseDoubleClick(MouseEvent e) {
		}		
	};
	
	/**
	 * Creates a pane folder. This will create exactly one child control in the
	 * given parent.
	 * 
	 * @param parent
	 * @param flags
	 */
	public PaneFolder(Composite parent, int flags) {
		// Initialize tab folder
		{
			tabFolder = new CTabFolder(parent, flags);
			
			// Create a proxy control to measure the title area of the tab folder
			titleAreaProxy = new Composite(tabFolder, SWT.NONE);
			titleAreaProxy.setVisible(false);
			tabFolder.setTopRight(titleAreaProxy, SWT.FILL);
			
			tabFolder.addCTabFolder2Listener(expandListener);
			
			tabFolder.addMouseListener(mouseListener);
		}
		
		// Initialize view form
		{
			viewForm = new ViewForm(tabFolder, SWT.NONE);

			// Only attach these to the viewForm when there's actuall a control to display
			viewFormTopLeftProxy = new ProxyControl(viewForm);
			viewFormTopCenterProxy = new ProxyControl(viewForm);
			viewFormTopRightProxy = new ProxyControl(viewForm);
			
			contentProxy = new ProxyControl(viewForm);
			viewForm.setContent(contentProxy.getControl());
		}
	}
	
	/**
	 * Return the main control for this pane folder
	 * 
	 * @return
	 */
	public Composite getControl() {
		return tabFolder;
	}
	
	/**
	 * Sets the top-center control (usually a toolbar), or null if none.
	 * Note that the control can have any parent.
	 * 
	 * @param topCenter the top-center control or null if none
	 */
	public void setTopCenter(Control topCenter) {
		topCenterCache.setControl(topCenter);
		if (topCenter != null) {
			if (!putTrimOnTop) {
				viewFormTopCenterProxy.setTarget(topCenterCache);
				viewForm.setTopCenter(viewFormTopCenterProxy.getControl());
			}
			topCenter.setBackground(putTrimOnTop ? backgroundGradientEnd : background );
		} else {
			if (!putTrimOnTop) {
				viewForm.setTopCenter(null);
			}
		}
	}
	
	/**
	 * Sets the top-right control (usually a dropdown), or null if none
	 * 
	 * @param topRight
	 */
	public void setTopRight(Control topRight) {
		topRightCache.setControl(topRight);
		if (topRight != null) {
			if (!putTrimOnTop) {
				viewFormTopRightProxy.setTarget(topRightCache);
				viewForm.setTopRight(viewFormTopRightProxy.getControl());
			}
			topRight.setBackground(putTrimOnTop ? backgroundGradientEnd : background );
		} else {
			if (!putTrimOnTop) {
				viewForm.setTopRight(null);
			}
		}
	}
	
	/**
	 * Sets the top-left control (usually a title label), or null if none
	 * 
	 * @param topLeft
	 */
	public void setTopLeft(Control topLeft) {
		if (topLeftCache.getControl() != topLeft) {
			topLeftCache.setControl(topLeft);
			// The top-left control always goes directly in the ViewForm
			if (topLeft != null) {
				viewFormTopLeftProxy.setTarget(topLeftCache);
				viewForm.setTopLeft(viewFormTopLeftProxy.getControl());
				topLeft.setBackground(background);
			} else {
				viewFormTopLeftProxy.setTarget(null);
				viewForm.setTopLeft(null);
			}
		}
	}
	
	public void layout(boolean flushCache) {
		// Flush the cached sizes if necessary
		if (flushCache) {
			topLeftCache.flush();
			topRightCache.flush();
			topCenterCache.flush();
		}
		
		Rectangle currentBounds = tabFolder.getBounds();
		
		// HACK: Force the tab folder to do a layout, since it doesn't always
		// resize its title area each time setBounds is called.
		tabFolder.setTopRight(titleAreaProxy, SWT.FILL);
		// END OF HACK
		
		Rectangle titleArea = DragUtil.getDisplayBounds(titleAreaProxy);

		
		Point topRightSize = topRightCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point topCenterSize = topCenterCache.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		
		// Determine if there is enough room for the trim in the title area
		int requiredWidth = topRightSize.x + topCenterSize.x;
		int requiredHeight = Math.max(topRightSize.y, topCenterSize.y);
		
		boolean lastTrimOnTop = putTrimOnTop;
		putTrimOnTop = (titleArea.width >= requiredWidth && titleArea.height >= requiredHeight);
		
		Control topRight = topRightCache.getControl();
		Control topCenter = topCenterCache.getControl();
		
		if (putTrimOnTop) {
			// Try to avoid calling setTop* whenever possible, since this will trigger a layout
			// of the viewForm.
			if (!lastTrimOnTop) {
				//	Arrange controls in the title bar
				viewFormTopCenterProxy.setTarget(null);
				viewFormTopRightProxy.setTarget(null);
				viewForm.setTopCenter(null);
				viewForm.setTopRight(null);
				
				updateTrimColours();
			}

			Rectangle topRightArea = new Rectangle (titleArea.x + titleArea.width - topRightSize.x, 
					titleArea.y + (titleArea.height - topRightSize.y) / 2, topRightSize.x, topRightSize.y); 
			
			if (topRight != null) {
				topRight.setBounds(Geometry.toControl(topRight.getParent(), topRightArea));
			}
			
			if (topCenter != null) {
				Rectangle topCenterArea = new Rectangle(topRightArea.x - topCenterSize.x,
						titleArea.y + (titleArea.height - topCenterSize.y) / 2, topCenterSize.x, topCenterSize.y);
				
				trimStart = topCenterArea.x - titleArea.x;
				
				Rectangle localCoords = Geometry.toControl(topCenter.getParent(), topCenterArea); 
				
				topCenter.setBounds(localCoords);
			}	
		} else {
			if (lastTrimOnTop) {
				if (topCenter != null) {
					viewFormTopCenterProxy.setTarget(topCenterCache);
					viewForm.setTopCenter(viewFormTopCenterProxy.getControl());
				}
				
				if (topRight != null) {
					viewFormTopRightProxy.setTarget(topRightCache);
					viewForm.setTopRight(viewFormTopRightProxy.getControl());
				}
				
				updateTrimColours();
			}
		}
		
		viewForm.setBounds(tabFolder.getClientArea());
		viewFormTopRightProxy.layout();
		viewFormTopLeftProxy.layout();
		viewFormTopCenterProxy.layout();
		
		updateBackgroundColors();
	}
	
	/**
	 * Returns the client area for this PaneFolder, relative to the pane folder's control.
	 * 
	 * @return
	 */
	public Rectangle getClientArea() {
		Rectangle bounds = contentProxy.getControl().getBounds();
		
		Rectangle formArea = viewForm.getBounds();
		
		bounds.x += formArea.x;
		bounds.y += formArea.y;
		
		return bounds;
	}
	
	/**
	 * Returns the current state of the folder (as shown on the button icons)
	 * 
	 * @return one of the IStackPresentationSite.STATE_* constants
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param buttonId one of the IStackPresentationSite.STATE_* constants
	 */
	protected void notifyButtonListeners(int buttonId) {
		if (mousedownState == getState()) {
			Iterator iter = buttonListeners.iterator();
			
			while (iter.hasNext()) {
				PaneFolderButtonListener listener = (PaneFolderButtonListener)iter.next();
				
				listener.stateButtonPressed(buttonId);
			}
		}
	}

	/**
	 * Notifies all listeners that the user clicked on the chevron
	 * 
	 * @param tabItem
	 */
	protected void notifyShowListeners(CTabFolderEvent event) {
		Iterator iter = buttonListeners.iterator();
		
		while (iter.hasNext()) {
			PaneFolderButtonListener listener = (PaneFolderButtonListener)iter.next();
			
			listener.showList(event);
		}
	}
	
	/**
	 * Notifies all listeners that the close button was pressed
	 * 
	 * @param tabItem
	 */
	protected void notifyCloseListeners(CTabItem tabItem) {
		Iterator iter = buttonListeners.iterator();
		
		while (iter.hasNext()) {
			PaneFolderButtonListener listener = (PaneFolderButtonListener)iter.next();
			
			listener.closeButtonPressed(tabItem);
		}
	}

	/**
	 * Sets the state that will be shown on the CTabFolder's buttons
	 * 
	 * @param state one of the IStackPresentationSite.STATE_* constants
	 */
	public void setState(int state) {
		this.state = state;
		
		tabFolder.setMinimized(state == IStackPresentationSite.STATE_MINIMIZED);
		tabFolder.setMaximized(state == IStackPresentationSite.STATE_MAXIMIZED);
	}
		
	public void addButtonListener(PaneFolderButtonListener listener) {
		buttonListeners.add(listener);
	}
	
	public void removeButtonListener(PaneFolderButtonListener listener) {
		buttonListeners.remove(listener);
	}
	
	public void setTabPosition(int newTabPosition) {
		tabPosition = newTabPosition;
		tabFolder.setTabPosition(tabPosition);
	}
	
	public int getTabPosition() {
		return tabPosition;
	}
	
	public boolean isDisposed() {
		return tabFolder == null || tabFolder.isDisposed();
	}
	
	/**
	 * Use this method instead of setting the background colours directly on the CTabFolder.
	 * This will cause the correct colours to be applied to the tab folder 
	 * 
	 * @param gradientStart
	 * @param gradientEnd
	 * @param background
	 */
	public void setBackgroundColors(Color gradientStart, Color gradientEnd, Color background) {
		backgroundGradientStart = gradientStart;
		backgroundGradientEnd = gradientEnd;
		this.background = background;
		updateBackgroundColors();
		
		updateTrimColours();
		
		if (topLeftCache.getControl() != null) {
			topLeftCache.getControl().setBackground(background);
		}
	}
	
	/**
	 * Update the colours of the trim widgets based on whether they are
	 * currently in the title bar or below it.
	 */
	protected void updateTrimColours() {
		Color background = putTrimOnTop ? getBackgroundGradientEnd() : getBackground() ;
		
		Control control;
		if (topCenterCache.getControl() != null) {
			topCenterCache.getControl().setBackground(background);
		}
		if (topRightCache.getControl() != null) {
			topRightCache.getControl().setBackground(background);
		}
	}

	
	public Color getBackgroundGradientEnd() {
		return backgroundGradientEnd;
	}
	
	public Color getBackground() {
		return background;
	}
	
	public Color getBackgroundGradientStart() {
		return backgroundGradientStart;
	}
	
	/**
	 * Causes the current background colours to be reapplied to the underlying CTabFolder. This
	 * should be invoked whenever the colours change or when the toolbars may have been resized
	 * or repositioned, since the gradient percentage is computed as a function of the toolbar
	 * position.
	 */
	protected void updateBackgroundColors() {
		Color [] c = new Color[3];
		c[0] = backgroundGradientStart;
		c[1] = backgroundGradientEnd;
		c[2] = c[1];
	
		int[] percents = new int[] {getGradientPercentage(), 100};				
	   
		tabFolder.setBackground(c, percents, false);
		tabFolder.setBackground(background);
		viewForm.setBackground(background);
	}
	
	/**
	 * Returns the current percentage for the background gradient (this is the percentage
	 * of the way along the tab folder where the toolbar begins) 
	 * 
	 * @return a percentage (0 - 100)
	 */
	private int getGradientPercentage() {
		Rectangle clientBounds = tabFolder.getBounds();
		
		int topTrimStart = putTrimOnTop ? trimStart : clientBounds.width; 
		
		int percentage = clientBounds.width == 0 ? 100 : Math.min(100, 
				100 * topTrimStart / clientBounds.width);
		
		if (percentage < 0) {
			percentage = 0;
		}
		
		return percentage;	    
	}

	public CTabItem createItem(int style, int index) {
		return new CTabItem(tabFolder, style, index);
	}
	
	
	// The remainder of the methods in this class redirect directly to CTabFolder methods
	
	public void setSelection(int selection) {
		tabFolder.setSelection(selection);
	}

	/**
	 * @param i
	 * @param j
	 * @param k
	 * @param l
	 * @return
	 */
	public Rectangle computeTrim(int i, int j, int k, int l) {
		return tabFolder.computeTrim(i, j, k, l);
	}

	/**
	 * @param b
	 */
	public void setUnselectedCloseVisible(boolean b) {
		tabFolder.setUnselectedCloseVisible(b);
	}
	
	/**
	 * @param fgColor
	 */
	public void setSelectionForeground(Color fgColor) {
		tabFolder.setSelectionForeground(fgColor);
	}

	/**
	 * @param bgColors
	 * @param percentages
	 * @param vertical
	 */
	public void setSelectionBackground(Color[] bgColors, int[] percentages, boolean vertical) {
		tabFolder.setSelectionBackground(bgColors, percentages, vertical);
	}
	
	public CTabItem getItem(int idx) {
		return tabFolder.getItem(idx);
	}

	public int getSelectionIndex() {
		return tabFolder.getSelectionIndex();
	}
	
	public int getTabHeight() {
		return tabFolder.getTabHeight();
	}
	
	public int indexOf(CTabItem toFind) {
		return tabFolder.indexOf(toFind);
	}
	
	public void setTabHeight(int height) {
		tabFolder.setTabHeight(height + 1);
	}

	/**
	 * @return
	 */
	public int getItemCount() {
		return tabFolder.getItemCount();
	}

	/**
	 * @return
	 */
	public CTabItem[] getItems() {
		return tabFolder.getItems();
	}
	
	public CTabItem getItem(Point toGet) {
		return tabFolder.getItem(toGet);
	}
	
	public CTabItem getSelection() {
		return tabFolder.getSelection();
	}

	/**
	 * @param isVisible
	 */
	public void setMinimizeVisible(boolean isVisible) {
		tabFolder.setMinimizeVisible(isVisible);
	}
	
	/**
	 * @param isVisible
	 */
	public void setMaximizeVisible(boolean isVisible) {
		tabFolder.setMaximizeVisible(isVisible);
	}

	/**
	 * @param traditionalTab
	 */
	public void setSimpleTab(boolean traditionalTab) {
		tabFolder.setSimpleTab(traditionalTab);
	}

	/**
	 * @param b
	 */
	public void setUnselectedImageVisible(boolean b) {
		tabFolder.setUnselectedImageVisible(b);
	}

	/**
	 * @param b
	 */
	public void setSingleTab(boolean b) {
		tabFolder.setSingleTab(b);
	}
}
