/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant Corporation - bug #215797
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.internal.presentations.PresentablePart;
import org.eclipse.ui.internal.presentations.util.TabbedStackPresentation;
import org.eclipse.ui.internal.tweaklets.Animations;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Manage all Fast views for a particular perspective. As of 3.3 fast views
 * appear in more than one manner (legacy FVB and Trim Stacks). The manager is
 * responsible for providing a single implementation for the methods relating to
 * fast views regardless of their UI presentation.
 * 
 * @since 3.3
 * 
 */
public class FastViewManager {
	private Perspective perspective;
	private WorkbenchPage page;
	private WorkbenchWindow wbw;
	private TrimLayout tbm;

	/**
	 * Maps a String to a list of IViewReferences. The string represents the
	 * 'id' of either the legacy FBV or the ViewStack id of some stack which may
	 * have elements in the trim.
	 * <p>
	 * NOTE: For TrimStacks, the order of the view ref's in the contained list
	 * is the order in which they will appear in the tab folder when the stack
	 * un-minimizes.
	 * </p>
	 */
	private Map idToFastViewsMap = new HashMap();

	/**
	 * Batch update management
	 */
	private boolean deferringUpdates = false;

	/**
	 * animation whose life-cycle spans a
	 * 'deferUpdates' cycle.
	 */
	private AnimationEngine batchAnimation = null;
	
	/**
	 * Used for non-deferred animations
	 */
	private AnimationEngine oneShotAnimation = null;
	//private RectangleAnimation oneShotAnimation = null;
	
	private IPerspectiveListener2 perspListener = new IPerspectiveListener2() {
		public void perspectiveActivated(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			// Only listen for changes in -this- perspective
			if (FastViewManager.this.perspective.getDesc() == perspective)
				handlePerspectiveActivation(page, perspective);
		}

		public void perspectiveChanged(IWorkbenchPage changedPage,
				IPerspectiveDescriptor perspective,
				IWorkbenchPartReference partRef, String changeId) {
			// Only listen for changes in -this- perspective
			if (FastViewManager.this.perspective.getDesc() == perspective)
				handlePerspectiveChange(changedPage, perspective, partRef,
					changeId);
		}

		public void perspectiveChanged(IWorkbenchPage changedPage,
				IPerspectiveDescriptor perspective, String changeId) {
			// Only listen for changes in -this- perspective
			if (FastViewManager.this.perspective.getDesc() == perspective)
				handlePerspectiveChange(changedPage, perspective, changeId);
		}
	};
	
	/**
	 * Creates a new manager for a particular perspective
	 * 
	 * @param perspective
	 * @param page
	 */
	public FastViewManager(Perspective perspective, WorkbenchPage page) {
		this.perspective = perspective;
		this.page = page;

		// Access the trim manager for this window
		wbw = (WorkbenchWindow) page.getWorkbenchWindow();
		tbm = (TrimLayout) wbw.getTrimManager();
	}

	protected void handlePerspectiveActivation(IWorkbenchPage activatingPage,
			IPerspectiveDescriptor activatingPerspective) {
		// If this perspective is activating then update the
		// legacy FVB to show this perspective's refs
		if (activatingPage == page
				&& perspective.getDesc() == activatingPerspective)
			updateTrim(FastViewBar.FASTVIEWBAR_ID);
	}

	protected void handlePerspectiveChange(IWorkbenchPage changedPage,
			IPerspectiveDescriptor changedPerspective,
			IWorkbenchPartReference partRef, String changeId) {
		// Only handle changes for our perspective
		if (changedPage != page && perspective.getDesc() != changedPerspective)
			return;

		if (changeId.equals(IWorkbenchPage.CHANGE_VIEW_HIDE)) {
			if (partRef instanceof IViewReference) {
				ViewReference ref = (ViewReference) partRef;
				if (ref.getPane().getContainer() instanceof ViewStack) {
					int viewCount = 0;
					LayoutPart[] children = ref.getPane().getContainer().getChildren();
					for (int i = 0; i < children.length; i++) {
						if (children[i] instanceof ViewPane && children[i] != ref.getPane())
							viewCount++;
					}
					
					if (viewCount == 0)
						ref.getPane().getStack().setState(IStackPresentationSite.STATE_RESTORED);
				}
			}
			// Remove the view from any FV list that it may be in
			// Do this here since the view's controls may be about
			// to be disposed...
			removeViewReference((IViewReference) partRef, false, true);
		}

		if (changeId.equals(IWorkbenchPage.CHANGE_FAST_VIEW_REMOVE)) {
			// Remove the view from any FV list that it may be in
			// Do this here since the view's controls may be about
			// to be disposed...
			removeViewReference((IViewReference) partRef, false, true);
		}
	}

	protected void handlePerspectiveChange(IWorkbenchPage changedPage,
			IPerspectiveDescriptor changedPerspective, String changeId) {
	}

	/**
	 * @return The list of current fast views associated with the given id or
	 *         the complete list of fastviews if the id == null.
	 */
	public List getFastViews(String forId) {
		List fvs = new ArrayList();

		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			if (forId == null || forId.equals(id)) {
				List fvList = (List) idToFastViewsMap.get(id);
				for (Iterator fvIter = fvList.iterator(); fvIter.hasNext();) {
					fvs.add(fvIter.next());
				}
			}
		}

		return fvs;
	}

	/**
	 * Casues the given {@link IViewReference} to be added to the list
	 * identified by the 'id' parameter. The reference is added at the specified
	 * index or at the end if the index is -1. If there was a previous entry for
	 * this ref it will be removed so that only the ref will only ever be in one
	 * list at a time.
	 * <p>
	 * NOTE: The trim life-cycle is managed at the stack level so there -must-
	 * be an entry in the map and a corresponding trim element before calling
	 * this method,
	 * </p>
	 * <p>
	 * The page/perspective are updated to make the view a fastview if
	 * necessary.
	 * </p>
	 * 
	 * @param id
	 *            The id of the {@link IWindowTrim} that is to show the ref
	 * @param index
	 *            The index to insert the ref at
	 * @param ref
	 *            The {@link IViewReference} to add
	 * @param update
	 *            cause the trim to update if <code>true</code>
	 */
	public void addViewReference(String id, int index, IViewReference ref,
			boolean update) {
		if (id == null || ref == null)
			return;

		List fvList = (List) idToFastViewsMap.get(id);
		if (fvList == null) {
			// Not in the model yet, add it
			fvList = new ArrayList();
			idToFastViewsMap.put(id, fvList);
		}

		// bounds checking
		if (index < 0 || index > fvList.size())
			index = fvList.size();

		// Is it already in a list?
		String curLocation = getIdForRef(ref);
		if (curLocation != null) {
			// is it the same list that it's being added to?
			if (id.equals(curLocation)) {
				int curIndex = fvList.indexOf(ref);
				if (index == curIndex)
					return; // No-Op

				// If we're inserting after where we
				// were then decrement the index to
				// account for the removal of the old ref
				if (index > curIndex)
					index--;
			}

			// Remove it...
			removeViewReference(ref, false, true);
		} else {
			// It's not a fastview, make it one
			makeFast(ref, true, false);
		}

		fvList.add(index, ref);

		// Note that the update call will create and show the ViewStackTrimToolbar
		// if necessary
		if (update)
			updateTrim(id);
	}

	/**
	 * Create the Trim element for the stack containing the given reference
	 * 
	 * @param suggestedSide
	 * @param paneOrientation
	 * @param ref
	 *            The {@link IViewReference} whose stack needs trim creation.
	 */
	private ViewStackTrimToolBar getTrimForViewStack(String id,
			int suggestedSide, int paneOrientation) {
		// Do we already have one??
		ViewStackTrimToolBar trim = (ViewStackTrimToolBar) tbm.getTrim(id);
		if (trim == null) {
			int cachedSide = tbm.getPreferredArea(id);
			if (cachedSide != -1)
				suggestedSide = cachedSide;
			
			IWindowTrim beforeMe = tbm.getPreferredLocation(id);
			
			trim = new ViewStackTrimToolBar(id, suggestedSide,
					paneOrientation, wbw);
			tbm.addTrim(suggestedSide, trim, beforeMe);
			updateTrim(trim.getId());
		}

		return trim;
	}

	/**
	 * Causes the trim element associated with the id to synch itself with the
	 * current list of views. This method will create a new ViewStackTrimToolbar
	 * if necessary (i.e. on the first call after views have been added to the map)
	 * and will also hide the trim element when the number of views in the mapped
	 * list goes to zero.
	 * 
	 * @param id
	 *            The id of the {@link IWindowTrim} to update
	 */
	private void updateTrim(String id) {
		// Get the trim part from the trim manager
		IWindowTrim trim = tbm.getTrim(id);

		// If it's not there there's not much we can do
		if (trim == null)
			return;

		// If there are no fast views for the bar then hide it
		List fvs = (List) idToFastViewsMap.get(id);
		if (fvs != null && fvs.size() == 0
				&& !FastViewBar.FASTVIEWBAR_ID.equals(id)) {
			if (trim.getControl().getVisible()) {
				tbm.setTrimVisible(trim, false);
				tbm.forceLayout();
			}
			return;
		}

		// Ensure that the trim is displayed
		if (!trim.getControl().getVisible()) {
			tbm.setTrimVisible(trim, true);
		}

		if (trim instanceof FastViewBar) {
			FastViewBar fvb = (FastViewBar) trim;
			fvb.update(true);
		} else if (trim instanceof ViewStackTrimToolBar) {
			ViewStackTrimToolBar vstb = (ViewStackTrimToolBar) trim;
			vstb.update(true);
			vstb.getControl().pack();
			LayoutUtil.resize(trim.getControl());
		}

		tbm.forceLayout();
	}

	/**
	 * Remove the view reference from its existing location
	 * 
	 * @param ref
	 *            The {@link IViewReference} to remove
	 */
	public void removeViewReference(IViewReference ref, boolean makeUnfast, boolean update) {
		String id = getIdForRef(ref);

		if (id != null) {
			// Remove the ref
			List fvList = (List) idToFastViewsMap.get(id);
			fvList.remove(ref);

			if (makeUnfast)
				makeFast(ref, false, false);
			
			if (update)
				updateTrim(id);
		}
	}

	/**
	 * 
	 * @param ref
	 * @param makeFast
	 * @param activate
	 */
	private void makeFast(IViewReference ref, boolean makeFast, boolean activate) {
		if (ref == null || page == null)
			return;

		if (makeFast) {
			page.makeFastView(ref);
		} else {
			page.removeFastView(ref);

			if (activate) {
				IWorkbenchPart toActivate = ref.getPart(true);
				if (toActivate != null) {
					page.activate(toActivate);
				}
			}
		}
	}

	/**
	 * @param ref
	 *            The IViewRference to check
	 * @return true iff the ref is in -any- list
	 */
	boolean isFastView(IViewReference ref) {
		return getIdForRef(ref) != null;
	}

	/**
	 * @param ref
	 *            The IViewRference to check
	 * @return The id of the trim bar currently showing the reference or
	 *         <code>null</code> if it's not in any list
	 */
	public String getIdForRef(IViewReference ref) {
		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			List fvList = (List) idToFastViewsMap.get(id);
			if (fvList.contains(ref))
				return id;
		}

		return null;
	}

	/**
	 * @return The side that the fast view pane should be attached to based on
	 *         the position of the trim element containing the ref.
	 */
	public int getViewSide(IViewReference ref) {
		IWindowTrim trim = getTrimForRef(ref);
		if (trim == null)
			return SWT.BOTTOM;

		int curSide = SWT.BOTTOM;
		int paneOrientation = SWT.BOTTOM;

		if (trim instanceof ViewStackTrimToolBar) {
			curSide = ((ViewStackTrimToolBar) trim).getCurrentSide();
			paneOrientation = ((ViewStackTrimToolBar) trim)
					.getPaneOrientation();
		} else if (trim instanceof FastViewBar) {
			curSide = ((FastViewBar) trim).getSide();
			paneOrientation = ((FastViewBar) trim).getOrientation(ref);
		}

		// Get trim layout info
		Point trimCenter = Geometry.centerPoint(trim.getControl().getBounds());
		Point shellCenter = Geometry.centerPoint(trim.getControl().getShell()
				.getClientArea());

		// Horizontal has to snap to either TOP or BOTTOM...
		if (paneOrientation == SWT.HORIZONTAL) {
			if (curSide == SWT.TOP || curSide == SWT.BOTTOM)
				return curSide;

			// Are we on the top or bottom 'end' of the trim area?
			return (trimCenter.y < shellCenter.y) ? SWT.TOP : SWT.BOTTOM;
		}

		if (paneOrientation == SWT.VERTICAL) {
			if (curSide == SWT.LEFT || curSide == SWT.RIGHT)
				return curSide;

			// Are we on the left or right 'end' of the trim area?
			return (trimCenter.x < shellCenter.x) ? SWT.LEFT : SWT.RIGHT;
		}

		return SWT.BOTTOM;
	}

	/**
	 * Return the trim element showing the given reference
	 * 
	 * @param ref
	 *            The reference to find
	 * @return the IWindowTrim showing the ref
	 */
	private IWindowTrim getTrimForRef(IViewReference ref) {
		String id = getIdForRef(ref);
		if (id == null)
			return null; // Not in trim

		return tbm.getTrim(id);
	}

	/**
	 * @return a List of <code>IViewReference</code> sorted into the order in
	 *         which they appear in the visual stack.
	 */
	private List getTrueViewOrder(ViewStack stack) {
		List orderedViews = new ArrayList();
		IPresentablePart[] parts = null;
		if (stack.getPresentation() instanceof TabbedStackPresentation) {
			TabbedStackPresentation tsp = (TabbedStackPresentation) stack
					.getPresentation();
			// KLUDGE!! uses a 'testing only' API to get the parts in their 'visible' order
			parts = tsp.getPartList();
		}

		// If we didn't get the parts from the tab list then try the presentable part API
		// ViewStack's declared 'no title' fail the call above, returning an empty array
		if (parts == null || parts.length == 0){
			// We'll have to process the parts in the order given...
			// This is certain to fail on drag re-ordering of the
			// icons in the trim since we have no API to inform the
			// custom presentation
			List partList = stack.getPresentableParts();
			parts = (IPresentablePart[]) partList.toArray(new IPresentablePart[partList.size()]);
		}			

		// Now, process the parts...
		for (int i = 0; i < parts.length; i++) {
			if (parts[i] instanceof PresentablePart) {
				PresentablePart part = (PresentablePart) parts[i];
				IWorkbenchPartReference ref = part.getPane()
						.getPartReference();
				if (ref instanceof IViewReference)
					orderedViews.add(ref);
			}
		}

		return orderedViews;
	}

	public void moveToTrim(ViewStack vs, boolean restoreOnUnzoom) {
		// Don't do anything when initializing...
		if (vs.getBounds().width == 0)
			return; // indicates that we're startin up
			
		// If we're part of a 'maximize' operation then use the cached
		// bounds...
		Rectangle stackBounds = perspective.getPresentation().getCachedBoundsFor(vs.getID());

		// OK, no cache means that we use the current stack position
		if (stackBounds == null)
			stackBounds = vs.getBounds();
		
		int paneOrientation = (stackBounds.width > stackBounds.height) ? SWT.HORIZONTAL
				: SWT.VERTICAL;

		// Remember the tab that was selected when we minimized
		String selId = ""; //$NON-NLS-1$
		PartPane selectedTab = vs.getSelection();
		if (selectedTab != null)
			selId  = selectedTab.getCompoundId();
		
		vs.deferUpdates(true);
		
		// animate the minimize
		RectangleAnimationFeedbackBase animation = (RectangleAnimationFeedbackBase) getDeferrableAnimation().getFeedback();
		animation.addStartRect(vs.getControl());

		//long startTick = System.currentTimeMillis();
		// Update the model first
		List toMove = getTrueViewOrder(vs);
		for (Iterator viewIter = toMove.iterator(); viewIter.hasNext();) {
			IViewReference ref = (IViewReference) viewIter.next();
			addViewReference(vs.getID(), -1, ref, false);
		}

		vs.deferUpdates(false);
		
		// Find (or create) the trim stack to move to
		ViewStackTrimToolBar vstb = getTrimForViewStack(vs.getID(), perspective
				.calcStackSide(stackBounds), paneOrientation);
		vstb.setRestoreOnUnzoom(restoreOnUnzoom);
		vstb.setSelectedTabId(selId);
		updateTrim(vstb.getId());
		
		//System.out.println("minimize time: " + (System.currentTimeMillis()-startTick)); //$NON-NLS-1$
		if (vstb != null) {
			animation.addEndRect(vstb.getControl());
			scheduleDeferrableAnimation();
		}
	}

	/**
	 * Restore the trim element representing a ViewStack back into the
	 * presentation.
	 * 
	 * @param viewStackTrimToolBar
	 *            The trim version to restore
	 */
	public void restoreToPresentation(String id) {
		ViewStackTrimToolBar vstb = getViewStackTrimToolbar(id);
		
		// The IntroPart uses the old min/max behavior; ensure that
		// we were really a minimized trim stack
		if (vstb == null)
			return;
		
		// remove any showing fast view
		page.hideFastView();

		// The stored id may be 'compound' if it's a multi-instance
		// view; split out the secondary id (if any)
		String selectedTabId = vstb.getSelectedTabId();
		String[] idParts = Util.split(selectedTabId, ':');
		if (idParts[0].length() == selectedTabId.length())
			idParts[1] = null;
		
		List fvs = getFastViews(id);
		for (Iterator fvIter = fvs.iterator(); fvIter.hasNext();) {
			IViewReference ref = (IViewReference) fvIter.next();
			removeViewReference(ref, true, !fvIter.hasNext());
		}

		// Restore the correct tab to the 'top'
		LayoutPart stack = perspective.getPresentation().findPart(id, null);
		if (stack instanceof PartStack) {
			LayoutPart selTab = perspective.getPresentation().findPart(idParts[0], idParts[1]);
			if (selTab instanceof PartPane && selTab instanceof ViewPane) {
				((PartStack)stack).setSelection(selTab);
				
				// activate the view if we're not doing a compound operation
				if (!deferringUpdates)
					((ViewPane)selTab).requestActivation();
			}
		}
		
		// Hide the Trim
		updateTrim(id);
	}

	/**
	 * Restore all fact view stacks created as part of a zoom
	 */
	public void restoreZoomedViewStacks() {
		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			IWindowTrim trim = tbm.getTrim(id);
			if (trim != null && trim instanceof ViewStackTrimToolBar) {
				ViewStackTrimToolBar vstb = (ViewStackTrimToolBar) trim;
				if (vstb.restoreOnUnzoom())
					restoreToPresentation(vstb.getId());
			}
		}
	}

	/**
	 * @param ref
	 *            Sets the ref of the icon
	 * @param selected
	 *            the selection state of the icon
	 */
	public void setFastViewIconSelection(IViewReference ref, boolean selected) {
		String id = getIdForRef(ref);
		IWindowTrim trim = tbm.getTrim(id);
		if (trim instanceof ViewStackTrimToolBar) {
			ViewStackTrimToolBar vstb = (ViewStackTrimToolBar) trim;
			vstb.setIconSelection(ref, selected);
		} else if (trim instanceof FastViewBar) {
			FastViewBar fvb = (FastViewBar) trim;
			if (selected) {
				fvb.setSelection(ref);
			} else {
				if (ref == fvb.getSelection()) {
					fvb.setSelection(null);
				}
			}
		}

	}

	/**
	 * Activate the manager. Called from the Perspecive's 'onActivate'
	 */
	public void activate() {
		wbw.addPerspectiveListener(perspListener);
		setTrimStackVisibility(true);
	}

	/**
	 * Activate the manager. Called from the Perspecive's 'onActivate'
	 */
	public void deActivate() {
		wbw.removePerspectiveListener(perspListener);
		setTrimStackVisibility(false);
	}

	/**
	 * Restore any trim stacks. This method is used when the presentation
	 * is switched back to 3.0; if we aren't using the new min/max story
	 * then we shouldn't -have- any trim stacks.
	 */
	public boolean restoreAllTrimStacks() {
		boolean stacksWereRestored = false;
		
		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			
			// Skip the legacy FstViewBar
			if (id.equals(FastViewBar.FASTVIEWBAR_ID))
				continue;
			
			// Restore the views
			List fvs = getFastViews(id);
			for (Iterator fvIter = fvs.iterator(); fvIter.hasNext();) {
				IViewReference ref = (IViewReference) fvIter.next();
				removeViewReference(ref, true, !fvIter.hasNext());
			}

			// Blow the trim away
			IWindowTrim trim = tbm.getTrim(id);
			if (trim != null && trim instanceof ViewStackTrimToolBar) {
				tbm.removeTrim(trim);
				trim.getControl().dispose();
				
				stacksWereRestored = true;
			}
		}
		
		tbm.forceLayout();
		
		return stacksWereRestored;
	}

	/**
	 * Show all non-empty trim stacks. Create the stack if necessary
	 */
	private void setTrimStackVisibility(boolean visible) {
		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			List fvs = getFastViews(id);
			
			// Never show 'empty' stacks
			if (visible && fvs.size() == 0)
				continue;

			IWindowTrim trim = tbm.getTrim(id);
			if (trim != null && trim instanceof ViewStackTrimToolBar) {
				((ViewStackTrimToolBar)trim).update(true);
				tbm.setTrimVisible(trim, visible);
			}
		}
	}

	public void saveState(IMemento memento) {
		// Output legacy fastviews
		FastViewBar fvb = wbw.getFastViewBar();
		if (fvb != null) {
			List fvRefs = getFastViews(FastViewBar.FASTVIEWBAR_ID);
			if (fvRefs.size() > 0) {
				IMemento childMem = memento
						.createChild(IWorkbenchConstants.TAG_FAST_VIEWS);
				Iterator itr = fvRefs.iterator();
				while (itr.hasNext()) {
					IViewReference ref = (IViewReference) itr.next();
					boolean restorable = page.getViewFactory()
							.getViewRegistry().find(ref.getId()).isRestorable();
					if(restorable) {
						IMemento viewMemento = childMem
						.createChild(IWorkbenchConstants.TAG_VIEW);
						String id = ViewFactory.getKey(ref);
						viewMemento.putString(IWorkbenchConstants.TAG_ID, id);
						float ratio = perspective.getFastViewWidthRatio(ref);
						viewMemento.putFloat(IWorkbenchConstants.TAG_RATIO, ratio);
					}
				}
			}
		}

		// Write all the current (non-empty) bars
		IMemento barsMemento = memento
				.createChild(IWorkbenchConstants.TAG_FAST_VIEW_BARS);

		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();

			// Legacy FV's are stored above...
			if (FastViewBar.FASTVIEWBAR_ID.equals(id))
				continue;

			List fvs = getFastViews(id);
			if (fvs.size() > 0) {
				IMemento barMemento = barsMemento
						.createChild(IWorkbenchConstants.TAG_FAST_VIEW_BAR);
				barMemento.putString(IWorkbenchConstants.TAG_ID, id);

				// Orientation / restore
				ViewStackTrimToolBar vstb = (ViewStackTrimToolBar) tbm
						.getTrim(id);
				if (vstb != null) {
					barMemento.putInteger(
							IWorkbenchConstants.TAG_FAST_VIEW_SIDE,
							vstb.getCurrentSide());
					
					barMemento.putInteger(
							IWorkbenchConstants.TAG_FAST_VIEW_ORIENTATION,
							vstb.getPaneOrientation());
					
					int boolVal = vstb.restoreOnUnzoom() ? 1 : 0;
					barMemento.putInteger(IWorkbenchConstants.TAG_FAST_VIEW_STYLE, boolVal);
					
					barMemento.putString(IWorkbenchConstants.TAG_FAST_VIEW_SEL_ID, vstb.getSelectedTabId());
				}

				IMemento viewsMem = barMemento
						.createChild(IWorkbenchConstants.TAG_FAST_VIEWS);
				Iterator itr = fvs.iterator();
				while (itr.hasNext()) {
					IMemento refMem = viewsMem
							.createChild(IWorkbenchConstants.TAG_VIEW);
					IViewReference ref = (IViewReference) itr.next();

					// id
					String viewId = ViewFactory.getKey(ref);
					refMem.putString(IWorkbenchConstants.TAG_ID, viewId);
					
					// width ratio
					float ratio = perspective.getFastViewWidthRatio(ref);
					refMem.putFloat(IWorkbenchConstants.TAG_RATIO, ratio);
				}
			}
		}
	}

	public void restoreState(IMemento memento, MultiStatus result) {
		// Load the fast views
		IMemento fastViewsMem = memento
				.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);

		// -Replace- the current list with the one from the store
		List refsList = new ArrayList();
		idToFastViewsMap.put(FastViewBar.FASTVIEWBAR_ID, refsList);

		if (fastViewsMem != null) {
			IMemento[] views = fastViewsMem
					.getChildren(IWorkbenchConstants.TAG_VIEW);
			for (int x = 0; x < views.length; x++) {
				// Get the view details.
				IMemento childMem = views[x];
				IViewReference ref = perspective.restoreFastView(childMem,
						result);
				if (ref != null)
					refsList.add(ref);
			}
		}

		// Load the Trim Stack info
		IMemento barsMem = memento
				.getChild(IWorkbenchConstants.TAG_FAST_VIEW_BARS);
		
		// It's not there for old workspaces
		if (barsMem == null)
			return;
		
		IMemento[] bars = barsMem
				.getChildren(IWorkbenchConstants.TAG_FAST_VIEW_BAR);
		for (int i = 0; i < bars.length; i++) {
			final String id = bars[i].getString(IWorkbenchConstants.TAG_ID);
			fastViewsMem = bars[i].getChild(IWorkbenchConstants.TAG_FAST_VIEWS);

			// -Replace- the current list with the one from the store
			refsList = new ArrayList();
			idToFastViewsMap.put(id, refsList);

			if (fastViewsMem != null) {
				IMemento[] views = fastViewsMem
						.getChildren(IWorkbenchConstants.TAG_VIEW);
	            result.merge(perspective.createReferences(views));
				
				// Create the trim area for the trim stack
				// Only create the TB if there are views in it
				if (views.length > 0) {
					final int side = bars[i].getInteger(
							IWorkbenchConstants.TAG_FAST_VIEW_SIDE)
							.intValue();
					final int orientation = bars[i].getInteger(
							IWorkbenchConstants.TAG_FAST_VIEW_ORIENTATION)
							.intValue();
					int boolVal = bars[i].getInteger(
							IWorkbenchConstants.TAG_FAST_VIEW_STYLE).intValue();
					final boolean restoreOnUnzoom = (boolVal > 0);
					
					final String selId = bars[i].getString(IWorkbenchConstants.TAG_FAST_VIEW_SEL_ID);
					
					// Create the stack
		        	StartupThreading.runWithoutExceptions(new StartupRunnable() {
						public void runWithException() throws Throwable {
							ViewStackTrimToolBar vstb = getTrimForViewStack(id, side, orientation);
							vstb.setRestoreOnUnzoom(restoreOnUnzoom);
							if (selId != null)
								vstb.setSelectedTabId(selId);
						}
					});
				}
				
				for (int x = 0; x < views.length; x++) {
					// Get the view details.
					IMemento childMem = views[x];
					IViewReference ref = perspective.restoreFastView(childMem,
							result);
					if (ref != null)
						refsList.add(ref);
				}
			}
		}
	}

	/**
	 * Returns the trim element for the given id if it exists. This
	 * will not be <code>null</code> if there are entries in the
	 * 'idToFastViewsMap' for this id.
	 * 
	 * @param id The id of the view stack to get the trim toolbar for.
	 */
	public ViewStackTrimToolBar getViewStackTrimToolbar(String id) {
		return (ViewStackTrimToolBar) tbm.getTrim(id);
	}
	
	public void printFVModel() {
		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			List fvList = (List) idToFastViewsMap.get(id);
			System.out.println("FastView: " + id + " count = " + fvList.size());  //$NON-NLS-1$//$NON-NLS-2$
			for (Iterator fvIter = fvList.iterator(); fvIter.hasNext();) {
				IViewReference ref = (IViewReference) fvIter.next(); 
				System.out.println("  Ref: " + ref.getId()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Informs the manager that a batch operation has started
	 * (say 'maximize', where many stacks will change state).
	 * 
	 * @param defer
	 *  true when starting a batch operation
	 *  false when ending the operation
	 */
	public void deferUpdates(boolean defer) {
		if (defer && !deferringUpdates) {
			deferringUpdates = defer;
			
			deferAnimations(true);
			
			return;
		}
		
		// 'false': reset and run any necessary updates
		deferringUpdates = false;
		deferAnimations(false);
	}

	/**
	 * When 'defer' is true we create a RectangleAnimation object
	 * to be used for any desired feedback. When ending it 
	 * schedules the animation and resets.
	 * 
	 * @param defer
	 *  true when starting a batch operation
	 *  false when ending the operation
	 */
	private void deferAnimations(boolean defer) {
		if (defer) {
			RectangleAnimationFeedbackBase feedback = ((Animations) Tweaklets
					.get(Animations.KEY)).createFeedback(wbw.getShell());
			batchAnimation = new AnimationEngine(feedback, 400);
			return;
		}

		if (batchAnimation != null)
			batchAnimation.schedule();
		batchAnimation = null;
	}

	/**
	 * Returns the animation object appropriate for the deferred state
	 * @return Either a 'one-shot' or a 'batch' animation object
	 */
	private AnimationEngine getDeferrableAnimation() {
		if (deferringUpdates)
			return batchAnimation;
		
		// Create a 'one-shot' animation
		RectangleAnimationFeedbackBase feedback = ((Animations) Tweaklets
				.get(Animations.KEY)).createFeedback(wbw.getShell());
		oneShotAnimation = new AnimationEngine(feedback, 400);
		return oneShotAnimation;
	}
	
	private void scheduleDeferrableAnimation() {
		if (deferringUpdates)
			return;
		
		// We can only schedule the 'one-shot' animations
		// the batch ones are sheduled at batch end
		if (oneShotAnimation != null)
			oneShotAnimation.schedule();
		oneShotAnimation = null;
	}

	/**
	 * Returns the 'bottom/right' trim stack. This is used to
	 * match the old behavior when opening a new view that has no placeholder
	 * in the case where there WB is maximized.
	 * 
	 * @return The 'bottom/right' trim stack or null if there are no
	 * defined trim stacks
	 */
	public ViewStackTrimToolBar getBottomRightTrimStack() {
		ViewStackTrimToolBar blTrimStack = null;
		Point blPt = new Point(0,0);
		
		Iterator mapIter = idToFastViewsMap.keySet().iterator();
		while (mapIter.hasNext()) {
			String id = (String) mapIter.next();
			
			// Skip the legacy FstViewBar
			if (id.equals(FastViewBar.FASTVIEWBAR_ID))
				continue;

			if (getFastViews(id).size() > 0) {
				// if we have views in the model then 'vstt' will not be null
				ViewStackTrimToolBar vstt = getViewStackTrimToolbar(id);
				Point loc = vstt.getControl().getLocation();
				if (loc.y > blPt.y || (loc.y == blPt.y && loc.x > blPt.x)) {
					blPt = loc;
					blTrimStack = vstt;
				}
			}
		}

		return blTrimStack;
	}
}
