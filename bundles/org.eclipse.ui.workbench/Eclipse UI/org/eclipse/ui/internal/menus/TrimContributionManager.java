/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.expressions.WorkbenchWindowExpression;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Manage trim contributions added through the 'org.eclipse.ui.menus'
 * extension point.
 * 
 * @since 3.3
 *
 */
public class TrimContributionManager extends ContributionManager {
	private class ToolBarTrimProxy implements IWindowTrim {
		private String id;
		private String uriSpec;
		private WorkbenchMenuService menuService;
		private WorkbenchWindow wbw;
		private ToolBar tb = null;
		private ToolBarManager tbm = null;

		ToolBarTrimProxy(String id, WorkbenchWindow wbw) {
			this.id = id;
			uriSpec = "toolbar:" + id; //$NON-NLS-1$
			this.wbw = wbw;
			
			this.menuService = (WorkbenchMenuService) wbw.getWorkbench().getService(
					IMenuService.class);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#dock(int)
		 */
		public void dock(int dropSide) {
			dispose();
			
			int orientation = SWT.HORIZONTAL;
			if (dropSide == SWT.LEFT || dropSide == SWT.RIGHT)
				orientation = SWT.VERTICAL;
			
			// Create the new control, manager...
			tbm = new ToolBarManager(SWT.FLAT | orientation);
//			menuService.populateContributionManager(tbm, uriSpec);
			menuService.populateContributionManager(wbw, restrictionExpression, tbm, uriSpec, true);
			
			// Set the state for any Control entries
			IContributionItem[] items = tbm.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i] instanceof InternalControlContribution) {
					InternalControlContribution wbwcc = (InternalControlContribution) items[i];
					wbwcc.setWorkbenchWindow(wbw);
					wbwcc.setCurSide(dropSide);
				}
			}
			
			// OK, create the ToolBar (causes an 'update(true)'
			tb = tbm.createControl(wbw.getShell());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#getControl()
		 */
		public Control getControl() {
			return tb;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#getDisplayName()
		 */
		public String getDisplayName() {
			return getId();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#getHeightHint()
		 */
		public int getHeightHint() {
			return SWT.DEFAULT;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#getId()
		 */
		public String getId() {
			return id;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#getValidSides()
		 */
		public int getValidSides() {
			return SWT.TOP | SWT.BOTTOM | SWT.LEFT | SWT.RIGHT;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#getWidthHint()
		 */
		public int getWidthHint() {
			return SWT.DEFAULT;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#handleClose()
		 */
		public void handleClose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#isCloseable()
		 */
		public boolean isCloseable() {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.layout.IWindowTrim#isResizeable()
		 */
		public boolean isResizeable() {
			return false;
		}
		
		/**
		 * Dispose any trim element resources
		 */
		public void dispose() {
			if (tbm != null) {
				tbm.removeAll();
				tbm.dispose();
			}
		}
	}
	
	/**
	 * A List of the URI's representing the trim areas
	 */
	private String[] trimAreaURIs = {
			MenuUtil.TRIM_COMMAND1,
			MenuUtil.TRIM_COMMAND2,
			MenuUtil.TRIM_VERTICAL1,
			MenuUtil.TRIM_VERTICAL2,
			MenuUtil.TRIM_STATUS
	};

	/**
	 * The SWT 'side' corresponding to a URI
	 */
	private int[] swtSides = { SWT.TOP, SWT.TOP, SWT.LEFT, SWT.RIGHT, SWT.BOTTOM }; 

	private WorkbenchWindow wbWindow;
	TrimLayout layout;
	private InternalMenuService menuService;
	
	List contributedTrim = new ArrayList();

	List contributedLists = new ArrayList();

	private Expression restrictionExpression;

	/**
	 * Construct a contribution manager for the given window 
	 */
	public TrimContributionManager(WorkbenchWindow window) {
		wbWindow = window;
		layout = (TrimLayout) wbWindow.getShell().getLayout();
		menuService = (InternalMenuService) window.getService(
				IMenuService.class);
		restrictionExpression = new WorkbenchWindowExpression(wbWindow);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	public void update(boolean force) {
		update(force, false);
	}
	
	public void update(boolean force, boolean hideTopTrim) {
		// Remove any contributed trim
		teardown();
		
		// Process the additions for each 'area'
		for (int i = 0; i < trimAreaURIs.length; i++) {
			// IntroBar want to hide the top trim
			if (hideTopTrim && swtSides[i] == SWT.TOP)
				continue;
			
			List contribs = menuService.getAdditionsForURI(new MenuLocationURI(trimAreaURIs[i]));
			
			for (Iterator cacheIter = contribs.iterator(); cacheIter.hasNext();) {
				AbstractContributionFactory cache = (AbstractContributionFactory) cacheIter.next();
				ContributionRoot ciList = new ContributionRoot(menuService,
						restrictionExpression, this, cache);
				cache.createContributionItems(wbWindow, ciList);
				// save the list for later cleanup of any visibility expressions that were added.
				contributedLists.add(ciList);
				for (Iterator ciIter = ciList.getItems().iterator(); ciIter.hasNext();) {
					IContributionItem ci = (IContributionItem) ciIter.next();
					if (ci instanceof IToolBarContributionItem) {
						// HACK!! Fake this
						ToolBarTrimProxy tbProxy = new ToolBarTrimProxy(ci.getId(), wbWindow);
						tbProxy.dock(swtSides[i]);
						
						// If we're adding to the 'command1' area then we're -before- the CoolBar
						IWindowTrim insertBefore = null;
						if (i == 0) {
							insertBefore = layout.getTrim("org.eclipse.ui.internal.WorkbenchWindow.topBar"); //$NON-NLS-1$
						}
						layout.addTrim(swtSides[i], tbProxy, insertBefore);						
						contributedTrim.add(tbProxy);
					}
				}
			}
		}
	}

	private void teardown() {
		// First, remove all trim
		for (Iterator iter = contributedTrim.iterator(); iter.hasNext();) {
			ToolBarTrimProxy proxy = (ToolBarTrimProxy) iter.next();
			layout.removeTrim(proxy);

			try {
				proxy.dispose();
	        } catch (Throwable e) {
	            IStatus status = null;
	            if (e instanceof CoreException) {
	                status = ((CoreException) e).getStatus();
	            } else {
	                status = StatusUtil
	                        .newStatus(
	                                IStatus.ERROR,
	                                "Internal plug-in widget delegate error on dispose.", e); //$NON-NLS-1$
	            }
	            StatusUtil
						.handleStatus(
								status,
								"widget delegate failed on dispose: id = " + proxy.getId(), StatusManager.LOG); //$NON-NLS-1$
	        }
		}

		// Clear out the old list
		contributedTrim.clear();
		
		// clean up the list of ContributionLists
		for (Iterator iter = contributedLists.iterator(); iter.hasNext();) {
			ContributionRoot list = (ContributionRoot) iter.next();
			list.release();
		}
		
		contributedLists.clear();
	}
	
	/**
	 * 
	 */
	public void dispose() {
		teardown();
	}

	/**
	 * @param knownIds
	 */
	public void updateLocations(List knownIds) {
		// TODO Auto-generated method stub
		
	}
}
