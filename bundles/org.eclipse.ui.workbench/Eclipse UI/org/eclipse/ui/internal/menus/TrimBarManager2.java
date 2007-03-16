/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.menus.AbstractWorkbenchTrimWidget;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.IWorkbenchWidget;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * <p>
 * An implementation that supports 'trim' elements defined in using the
 * <code>org.eclipse.ui.menus</code> extension point.
 * </p>
 * <p>
 * This class is not intended to be used outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public class TrimBarManager2 {

	/**
	 * A List of the URI's representing the trim areas
	 */
	private MenuLocationURI[] trimAreaURIs = {
			new MenuLocationURI("toolbar:command1"), //$NON-NLS-1$
			new MenuLocationURI("toolbar:command2"), //$NON-NLS-1$
			new MenuLocationURI("toolbar:vertical1"), //$NON-NLS-1$
			new MenuLocationURI("toolbar:vertical2"), //$NON-NLS-1$
			new MenuLocationURI("toolbar:status"), //$NON-NLS-1$
	};

	/**
	 * The SWT 'side' corresponding to a URI
	 */
	int[] swtSides = { SWT.TOP, SWT.TOP, SWT.LEFT, SWT.RIGHT, SWT.BOTTOM }; 
	/**
	 * The window on which this menu manager exists; never <code>null</code>.
	 */
	private STrimBuilder fTrimBuilder;

	private WorkbenchMenuService fMenuService;

	private boolean fDirty;

	/**
	 * Constructs a new instance of <code>TrimBarManager</code>.
	 * 
	 * @param window
	 *            The window on which this menu manager exists; must not be
	 *            <code>null</code>.
	 */
	public TrimBarManager2(final WorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException("The window cannot be null"); //$NON-NLS-1$
		}

		// Remember the parameters
		fMenuService = (WorkbenchMenuService) window.getWorkbench().getService(
				IMenuService.class);
		fTrimBuilder = new STrimBuilder(window);

		// New layouts are always 'dirty'
		fDirty = true;
	}

	/**
	 * Hacked version of the update method that allows the hiding of any trim
	 * sited at SWT.TOP. This is because the Intro management wants there to be
	 * no trim at the top but can only currently indicate this by using the
	 * CoolBar's visibility...
	 * 
	 * @param force
	 * @param recursive
	 * @param hideTopTrim
	 */
	public void update(boolean force, boolean recursive, boolean hideTopTrim) {
		if (force || isDirty()) {
			// Re-render the trim based on the new layout
			fTrimBuilder.build(hideTopTrim);
			setDirty(false);
		}
	}

	/**
	 * Copied from the <code>MenuManager</code> method...
	 * 
	 * @param force
	 *            If true then do the update even if not 'dirty'
	 * @param recursive
	 *            Update recursively
	 * 
	 * @see org.eclipse.jface.action.MenuManager#update(boolean, boolean)
	 */
	public void update(boolean force, boolean recursive) {
		update(force, recursive, false);
	}

	/**
	 * Set the dirty state of the layout
	 * 
	 * @param isDirty
	 */
	private void setDirty(boolean isDirty) {
		fDirty = isDirty;
	}

	/**
	 * Returns the 'dirty' state of the layout
	 * 
	 * @return Always returns 'true' for now
	 */
	private boolean isDirty() {
		return fDirty;
	}

	/**
	 * This is a convenience class that maintains the list of the widgets in the
	 * group. This allows any position / orientation changes to the group to be
	 * passed on to all the widgets for that group.
	 * 
	 * @since 3.2
	 * 
	 */
	private class TrimWidgetProxy extends WindowTrimProxy {

		private List widgets;
		private TrimAdditionCacheEntry cacheEntry;
		private int originalSide;
		private int curSide;

		private Composite parent;

		/**
		 * Constructor that takes in any information necessary to implement an
		 * IWindowTrim and also has enough state to manage a group with multiple
		 * IWidget contributions.
		 * 
		 */
		public TrimWidgetProxy(List widgets, int side, Composite parent,
				TrimAdditionCacheEntry entry, boolean resizeable) {
			super(parent, entry.getId(), entry.getId(), SWT.TOP | SWT.BOTTOM | SWT.LEFT
					| SWT.RIGHT, resizeable);

			// Remember our widget structure
			this.widgets = widgets;
			this.curSide = side;
			this.originalSide = side;
			this.parent = parent;
			this.cacheEntry = entry;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.WindowTrimProxy#dock(int)
		 */
		public void dock(int newSide) {
			// out with the old...
			for (Iterator iter = widgets.iterator(); iter.hasNext();) {
				IWidget widget = (IWidget) iter.next();
				widget.dispose();
				
				cacheEntry.removeWidget(widget);
			}

			// ...in with the new
			for (Iterator iter = widgets.iterator(); iter.hasNext();) {
				IWorkbenchWidget widget = (IWorkbenchWidget) iter.next();
				if (widget instanceof AbstractWorkbenchTrimWidget)
					((AbstractWorkbenchTrimWidget)widget).fill(parent, curSide, newSide);
				else
					widget.fill(parent);
			}

			curSide = newSide;
			parent.layout();
		}

		/**
		 * Disposes all the widgets contributed into this group and then
		 * disposes the group's 'proxy' control
		 */
		public void dispose() {
			for (Iterator iter = widgets.iterator(); iter.hasNext();) {
				IWidget widget = (IWidget) iter.next();
				widget.dispose();

				// Remove the IWidget from the entry's cache
				cacheEntry.removeWidget(widget);
			}

			getControl().dispose();
		}

		/**
		 * @return The side that the trim was declared to be on
		 */
		public int getSide() {
			return originalSide;
		}

		/**
		 * @return Whether this addition is at the start or end of the
		 * containing trim area
		 */
		public boolean isAtStart() {
			//Delegate to the cache entry
			return cacheEntry.isAtStart();
		}
	}

	/**
	 * A convenience class that implements the 'rendering' code necessary to
	 * turn the contributions to the 'trim' bar into actual SWT controls.
	 * 
	 * @since 3.2
	 * 
	 */
	private class STrimBuilder {
		/**
		 * The WorkbenchWindow that this builder is for
		 */
		private WorkbenchWindow fWindow;

		/**
		 * The list of <code>WindowTrimProxy</code> elements currently
		 * rendered in the WorkbenchWindow. Used to support the update mechanism
		 * (specifically, it's needed to implement the <code>tearDown</code>
		 * method).
		 */
		private List curGroups = new ArrayList();

		/**
		 * Map to cache which trim has already been initialized
		 */
		private Map initializedTrim = new HashMap();
		
		/**
		 * Construct a trim builder for the given WorkbenchWindow
		 * 
		 * @param window
		 *            The WorkbenchWindow to render the trim on
		 */
		public STrimBuilder(WorkbenchWindow window) {
			fWindow = window;
		}

		/**
		 * Remove any rendered trim. This method will always be directly
		 * followed by a call to the 'build' method to update the contents.
		 */
		public void tearDown() {
			// First, remove all trim
			for (Iterator iter = curGroups.iterator(); iter.hasNext();) {
				TrimWidgetProxy proxy = (TrimWidgetProxy) iter.next();
				fWindow.getTrimManager().removeTrim(proxy);

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
			curGroups.clear();
		}

		/**
		 * Construct the trim based on the contributions.
		 * 
		 * @param layout
		 *            The new layout information
		 * @param hideTopTrim
		 *            <code>true</code> iff we don't want to display trim
		 *            contributed into the SWT.TOP area. This is because the
		 *            'Intro' View hides the CBanner (and so, presumably, also
		 *            wants to not show any other trim at the top.
		 * 
		 * @param window
		 *            The widnow to 'render' the trim into
		 * 
		 */
		public void build(boolean hideTopTrim) {
			tearDown();
			
			for (int i = 0; i < trimAreaURIs.length; i++) {
				processAdditions(trimAreaURIs[i], hideTopTrim);
			}
		}

		/**
		 * @param menuLocationURI
		 * @param hideTopTrim 
		 */
		private void processAdditions(MenuLocationURI trimURI, boolean hideTopTrim) {
			List additions = fMenuService.getAdditionsForURI(trimURI);
			if (additions.size() == 0)
				return;
			
			int swtSide = getSide(trimURI);
			
			// Dont show trim on the top if it's 'hidden'
			if (swtSide == SWT.TOP && hideTopTrim)
				return;

			// Each trim addition represents a 'group' into which one or more
			// widgets can be placed...
			for (Iterator iterator = additions.iterator(); iterator.hasNext();) {
				TrimAdditionCacheEntry trimEntry = (TrimAdditionCacheEntry) iterator.next();
				String groupId = trimEntry.getId();
				
				// Get the list of IConfgurationElements representing
				// widgets in this group
				List widgets = trimEntry.getWidgets();
				if (widgets.size() == 0)
					continue;
				
				// Create a 'container' composite for the group
				Composite grpComposite = new Composite(fWindow.getShell(), SWT.NONE);
				grpComposite.setToolTipText(groupId);
				
				// Create the layout for the 'group' container...-no- border margins
				RowLayout rl = new RowLayout();
		        rl.marginBottom = rl.marginHeight = rl.marginLeft = rl.marginRight = rl.marginTop = rl.marginWidth = 0;
				grpComposite.setLayout(rl);
				
				// keep track of whether -any- of the widgets are resizeable
				boolean resizeable = false;

				for (Iterator widgetIter = widgets.iterator(); widgetIter.hasNext();) {
					IWorkbenchWidget widget = (IWorkbenchWidget) widgetIter.next();
					IConfigurationElement widgetElement = trimEntry.getElement(widget);
					if (widget != null) {
						resizeable |= trimEntry.fillMajor(widgetElement);
						renderTrim(grpComposite, widget, swtSide);
					}
				}

				// Create the trim proxy for this group
				TrimWidgetProxy groupTrimProxy = new TrimWidgetProxy(widgets,
						swtSide, grpComposite, trimEntry, resizeable);
				curGroups.add(groupTrimProxy);

				// 'Site' the group in its default location
				placeGroup(groupTrimProxy);
			}	
		}

		private void placeGroup(final TrimWidgetProxy proxy) {
			// Get the placement parameters
			final int side = proxy.getSide();
			boolean atStart = proxy.isAtStart();

			// Place the trim before any other trim if it's
			// at the 'start'; otherwise place it at the end
			IWindowTrim beforeMe = null;
			if (atStart) {
				List trim = fWindow.getTrimManager().getAreaTrim(side);
				if (trim.size() > 0)
					beforeMe = (IWindowTrim) trim.get(0);
			}

			// Add the group into trim...safely
			try {
    			proxy.dock(side); // ensure that the widgets are properly oriented
    			TrimLayout tl = (TrimLayout) fWindow.getShell().getLayout();
    			tl.addTrim(side, proxy, beforeMe);
	        } catch (Throwable e) {
	            IStatus status = null;
	            if (e instanceof CoreException) {
	                status = ((CoreException) e).getStatus();
	            } else {
	                status = StatusUtil
	                        .newStatus(
	                                IStatus.ERROR,
	                                "Internal plug-in widget delegate error on dock.", e); //$NON-NLS-1$
	            }
	            StatusUtil.handleStatus(status, "widget delegate failed on dock: id = " + proxy.getId(), //$NON-NLS-1$
	            		StatusManager.LOG);
	        }
		}

		/**
		 * Render a particular SWidget into a given group
		 * 
		 * @param groupComposite
		 *            The parent to create the widgets under
		 * @param widget
		 *            The SWidget to render
		 * @param side
		 */
		private void renderTrim(final Composite groupComposite, IWidget iw,
				final int side) {
			// OK, fill the widget
			if (iw != null) {
            	// The -first- time trim is displayed we'll initialize it
            	if (iw instanceof IWorkbenchWidget && initializedTrim.get(iw) == null) {
            		IWorkbenchWidget iww = (IWorkbenchWidget) iw;
            		iww.init(fWindow);
            		initializedTrim.put(iw, iw);
            	}

            	if (iw instanceof AbstractWorkbenchTrimWidget)
            		((AbstractWorkbenchTrimWidget)iw).fill(groupComposite, SWT.DEFAULT, side);
            	else
            		iw.fill(groupComposite);
			}
		}

		private int getSide(MenuLocationURI uri) {
			for (int i = 0; i < trimAreaURIs.length; i++) {
				if (trimAreaURIs[i].getRawString().equals(uri.getRawString()))
					return swtSides[i];
			}
			return SWT.BOTTOM;
		}


		/**
		 * Reposition any contributed trim whose id is -not- a 'knownId'. If the
		 * id is known then the trim has already been positioned from the stored
		 * workbench state. If it isn't then it's a new contribution whose
		 * default position may have been trashed by the WorkbenchWindow's
		 * 'restoreState' handling.
		 * 
		 * @param knownIds
		 *            A List of strings containing the ids of any trim that was
		 *            explicitly positioned during the restore state.
		 */
		public void updateLocations(List knownIds) {
			for (Iterator iter = curGroups.iterator(); iter.hasNext();) {
				TrimWidgetProxy proxy = (TrimWidgetProxy) iter.next();
				if (!knownIds.contains(proxy.getId())) {
					placeGroup(proxy);
				}
			}
		}
	}

	/**
	 * Updates the placement of any contributed trim that is -not- in the
	 * 'knownIds' list (which indicates that it has already been placed using
	 * cached workspace data.
	 * 
	 * Forward on to the bulder for implementation
	 */
	public void updateLocations(List knownIds) {
		fTrimBuilder.updateLocations(knownIds);
	}
	
	/**
	 * unhook the menu service.
	 */
	public void dispose() {
		fMenuService = null;
		fTrimBuilder = null;
	}
}

