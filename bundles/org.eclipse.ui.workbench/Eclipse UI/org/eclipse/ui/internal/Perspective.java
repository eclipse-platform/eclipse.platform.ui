/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveExtensionReader;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

/**
 * The ViewManager is a factory for workbench views.  
 */
public class Perspective
{
	private PerspectiveDescriptor descriptor;
	protected WorkbenchPage page;
	protected LayoutPart editorArea;
	private PartPlaceholder editorHolder;
	private ViewFactory viewFactory;
	private ArrayList visibleActionSets;
	private ArrayList alwaysOnActionSets;
	private ArrayList alwaysOffActionSets;
	private ArrayList newWizardActionIds;
	private ArrayList showViewActionIds;
	private ArrayList perspectiveActionIds;
	private ArrayList fastViews;
	private ArrayList showInPartIds;
	private HashMap showInTimes = new HashMap();
	private IViewReference activeFastView;
	private IViewReference previousActiveFastView;
	private IMemento memento;
	protected PerspectivePresentation presentation;
	final static private String VERSION_STRING = "0.016";//$NON-NLS-1$
	
	// fields used by fast view resizing via a sash
	private static final int SASH_SIZE = 3;
	private static final int FASTVIEW_HIDE_STEPS = 5;
	private static final long FASTVIEW_HIDE_MIN_DURATION = 50;
	private static final long FASTVIEW_HIDE_MAX_DURATION = 250;
	private static final RGB RGB_COLOR1 = new RGB(132, 130, 132);
	private static final RGB RGB_COLOR2 = new RGB(143, 141, 138);
	private static final RGB RGB_COLOR3 = new RGB(171, 168, 165);
	private Color borderColor1;
	private Color borderColor2;
	private Color borderColor3;
	private Map mapFastViewToWidthRatio = new HashMap();
	private Sash fastViewSash;
	private CoolBarLayout toolBarLayout;
	
	// resize listener to update fast view height and width when
	// window resized.
	Listener resizeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.Resize && activeFastView != null) {
				ViewPane pane = getPane(activeFastView);
				if (pane.isZoomed() == false) {
					Rectangle bounds = pane.getBounds();
					bounds.height = Math.max(0, getClientComposite().getSize().y);
					float ratio = getFastViewWidthRatio(pane.getID());
					bounds.width = Math.max(0, (int)((float)(getClientComposite().getSize().x) * ratio));
					pane.setBounds(bounds);
					fastViewSash.setBounds(bounds.width - SASH_SIZE, bounds.y, SASH_SIZE, bounds.height - SASH_SIZE);
					fastViewSash.moveAbove(null);
				}
			}
		}
	};

	private PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent event) {
			if (borderColor1 == null) borderColor1 = WorkbenchColors.getColor(RGB_COLOR1);
			if (borderColor2 == null) borderColor2 = WorkbenchColors.getColor(RGB_COLOR2);
			if (borderColor3 == null) borderColor3 = WorkbenchColors.getColor(RGB_COLOR3);
			
			Point size = fastViewSash.getSize();
			Rectangle d = new Rectangle(0, 0, size.x, size.y);
			GC gc = event.gc;
			
			gc.setForeground(borderColor1);
			gc.drawLine(d.x, d.y, d.x, d.y + d.height);
		
			gc.setForeground(borderColor2);
			gc.drawLine(d.x + 1, d.y + 1, d.x + 1, d.y + d.height);
		
			gc.setForeground(borderColor3);
			gc.drawLine(d.x + 2, d.y + 2, d.x + 2, d.y + d.height);
		}
	};
	private SelectionAdapter selectionListener = new SelectionAdapter () {
		public void widgetSelected(SelectionEvent e) {
			if (e.detail == SWT.DRAG && activeFastView != null)
				checkDragLimit(e);
			if (e.detail != SWT.DRAG && activeFastView != null) {
				ViewPane pane = getPane(activeFastView);
				Rectangle bounds = pane.getBounds();
				bounds.width = Math.max(0, e.x - bounds.x);
				pane.setBounds(bounds);
				Float newRatio = new Float((float)bounds.width/(float)getClientComposite().getSize().x);
				mapFastViewToWidthRatio.put(pane.getID(), newRatio);
				updateFastViewSashBounds(bounds);
				fastViewSash.moveAbove(null);
			}
		}
	};

	private String oldPartID = null;
	private boolean shouldHideEditorsOnActivate = false;

/**
 * ViewManager constructor comment.
 */
public Perspective(PerspectiveDescriptor desc, WorkbenchPage page)
	throws WorkbenchException
{
	this(page);
	descriptor = desc;
	if(desc != null)
		createPresentation(desc);
}
/**
 * ViewManager constructor comment.
 */
protected Perspective(WorkbenchPage page) throws WorkbenchException {
	this.page = page;
	this.editorArea = page.getEditorPresentation().getLayoutPart();
	this.viewFactory = page.getViewFactory();
	visibleActionSets = new ArrayList(2);
	alwaysOnActionSets = new ArrayList(2);
	alwaysOffActionSets = new ArrayList(2);
	fastViews = new ArrayList(2);
}
/**
 * Sets the fast view attribute.
 * Note: The page is expected to update action bars.
 */
public void addFastView(IViewReference ref) {
	ViewPane pane = (ViewPane)((WorkbenchPartReference)ref).getPane();
	if (!isFastView(ref)) {
		// Only remove the part from the presentation if it
		// is actually in the presentation.
		if (presentation.hasPlaceholder(pane.getID()) ||
			pane.getContainer() != null)
				presentation.removePart(pane);
		// We are drag-enabling the pane because it has been disabled
		// when it was removed from the perspective presentation.
		presentation.enableDrag(pane);
		fastViews.add(ref);
		pane.setFast(true);
		Control ctrl = pane.getControl();
		if (ctrl != null)
			ctrl.setEnabled(false); // Remove focus support.
	}
}
/**
 * Moves a part forward in the Z order of a perspective so it is visible.
 *
 * @param part the part to bring to move forward
 * @return true if the part was brought to top, false if not.
 */
public boolean bringToTop(IViewReference ref) {
	if (isFastView(ref)) {
		setActiveFastView(ref);
		return true;
	} else {
		return presentation.bringPartToTop(getPane(ref));
	}
}
/**
 * Returns true if a view can close.
 */
public boolean canCloseView(IViewPart view) {
	return true;
}

/**
 * Prevents the user from making a fast view too narrow or too wide.
 */
private void checkDragLimit(SelectionEvent event) {
	if (event.x < ((float)getClientComposite().getSize().x * IPageLayout.RATIO_MIN))
		event.x = (int)((float)getClientComposite().getSize().x * IPageLayout.RATIO_MIN);
	if (event.x > ((float)getClientComposite().getSize().x * IPageLayout.RATIO_MAX))
		event.x = (int)((float)getClientComposite().getSize().x * IPageLayout.RATIO_MAX);
}

/**
 * Returns whether a view exists within the perspective.
 */
public boolean containsView(IViewPart view) {
	String id = view.getSite().getId();
	IViewReference ref = findView(id);
	if(ref == null)
		return false;
	return (view == ref.getPart(false));
}
/**
 * Create the initial list of action sets.
 */
private void createInitialActionSets(List stringList) {
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	Iterator iter = stringList.iterator();
	while (iter.hasNext()) {
		String id = (String)iter.next();
		IActionSetDescriptor desc = reg.findActionSet(id);
		if (desc != null)
			visibleActionSets.add(desc);
		else
			WorkbenchPlugin.log("Unable to find Action Set: " + id);//$NON-NLS-1$
	}
}
/**
 * Create a presentation for a perspective.
 */
private void createPresentation(PerspectiveDescriptor persp)
	throws WorkbenchException
{
	if (persp.hasCustomDefinition()) {
		loadCustomPersp(persp);
	} else {
		loadPredefinedPersp(persp);
	}
}
/**
 * Dispose the perspective and all views contained within.
 */
public void dispose() {
	// Get rid of presentation.
	if(presentation == null)
		return;
		
	presentation.deactivate();
	presentation.disposeSashes();
	

	// Release each view.
	IViewReference refs[] = getViewReferences();
	for (int i = 0,length = refs.length; i < length; i ++) {
		getViewFactory().releaseView(refs[i].getId());
	}

	// Dispose of the sash too...
	if (fastViewSash != null) {
		fastViewSash.dispose();
		fastViewSash = null;
	}

	mapFastViewToWidthRatio.clear();
}
/**
 * See IWorkbenchPage@findView.
 */
public IViewReference findView(String id) {
	IViewReference refs[] = getViewReferences();
	for (int i = 0; i < refs.length; i ++) {
		IViewReference ref = refs[i];
		if (id.equals(ref.getId()))
			return ref;
	}
	return null;
}
/**
 * Returns an array of the visible action sets. 
 */
public IActionSetDescriptor[] getActionSets() {
	int size = visibleActionSets.size();
	IActionSetDescriptor [] array = new IActionSetDescriptor[size];
	for (int i = 0; i < size; i ++) {
		array[i] = (IActionSetDescriptor)visibleActionSets.get(i);
	}
	return array;
}
/**
 * Returns the window's client composite widget
 * which views and editor area will be parented.
 */
private Composite getClientComposite() {
	return page.getClientComposite();
}
/**
 * Returns the perspective.
 */
public IPerspectiveDescriptor getDesc() {
	return descriptor;
}
/**
 * Returns the bounds of the given fast view.
 */
/*package*/ Rectangle getFastViewBounds(IViewReference ref) {
	// Copy the bounds of the page composite
	Rectangle bounds = page.getClientComposite().getBounds();
	// get the width ratio of the fast view
	float ratio = getFastViewWidthRatio(ref.getId());
	// Compute the actual width of the fast view.
	bounds.width = (int)(ratio*(float)getClientComposite().getSize().x);
	return bounds;
}
/**
 * Returns the docked views.
 */
public IViewReference [] getFastViews() {
	IViewReference array[] = new IViewReference[fastViews.size()];
	fastViews.toArray(array);
	return array;
}
/**
 * Returns the new wizard actions the page.
 * This is List of Strings.
 */
public ArrayList getNewWizardActionIds() {
	return newWizardActionIds;
}
/**
 * Returns the pane for a view reference.
 */
private ViewPane getPane(IViewReference ref) {
	return (ViewPane)((WorkbenchPartReference)ref).getPane();
}
/**
 * Returns the perspective actions for this page.
 * This is List of Strings.
 */
public ArrayList getPerspectiveActionIds() {
	return perspectiveActionIds;
}
/**
 * Returns the presentation.
 */
public PerspectivePresentation getPresentation() {
	return presentation;
}
/**
 * Retrieves the ratio for the fast view with the given ID. If
 * the ratio is not known, the default ratio for the view is returned.
 */
private float getFastViewWidthRatio(String id) {
	Float f = (Float)mapFastViewToWidthRatio.get(id);
	if (f != null) {
		return f.floatValue();
	} else {
		IViewRegistry reg = WorkbenchPlugin.getDefault().getViewRegistry();	
		float ratio = reg.find(id).getFastViewWidthRatio();
		mapFastViewToWidthRatio.put(id, new Float(ratio));
		return ratio;
	}
}
/**
 * Returns the ids of the parts to list in the Show In... dialog.
 * This is a List of Strings.
 */
public ArrayList getShowInPartIds() {
	return showInPartIds;
}

/**
 * Returns the time at which the last Show In was performed
 * for the given target part, or 0 if unknown.
 */
public long getShowInTime(String partId) {
	Long t = (Long) showInTimes.get(partId);
	return t == null ? 0L : t.longValue();
}
/**
 * Returns the ids of the views to list in the Show View shortcuts.
 * This is a List of Strings.
 */
public ArrayList getShowViewActionIds() {
	return showViewActionIds;
}
/**
 * Returns the toolbar layout for this perspective.
 */
public CoolBarLayout getToolBarLayout() {
	return toolBarLayout;
}
/**
 * Returns the last active fast view.
 */
/*package*/ IViewReference getPreviousActiveFastView() {
	return previousActiveFastView;	
}
/**
 * Returns the view factory.
 */
private ViewFactory getViewFactory() {
	return viewFactory;
}
/**
 * Open the tracker to allow the user to move
 * the specified part using keyboard.
 */
public void openTracker(ViewPane pane) {
	presentation.openTracker(pane);
}
/**
 * See IWorkbenchPage.
 */
public IViewReference [] getViewReferences() {
	// Get normal views.
	if(presentation == null)
		return new IViewReference[0];
	
	List panes = new ArrayList(5);
	presentation.collectViewPanes(panes);
	
	IViewReference [] resultArray = new IViewReference[panes.size() + fastViews.size()];

	// Copy fast views.
	int nView = 0;
	for (int i = 0; i < fastViews.size(); i++) {
		resultArray[nView] = (IViewReference)fastViews.get(i);
		++ nView;
	}
	
	// Copy normal views.
	for (int i = 0; i < panes.size(); i ++) {
		ViewPane pane = (ViewPane)panes.get(i);
		resultArray[nView] = pane.getViewReference();
		++ nView;
	}
	
	return resultArray;
}
/**
 * @see IWorkbenchPage
 * Note: The page is expected to update action bars.
 */
public void hideActionSet(String id) {
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	IActionSetDescriptor desc = reg.findActionSet(id);
	if (alwaysOnActionSets.contains(desc))
		return;
	if (desc != null)
		visibleActionSets.remove(desc);
}
/**
 * Hide the editor area if visible
 */
protected void hideEditorArea() {
	if (!isEditorAreaVisible())
		return;

	// Replace the editor area with a placeholder so we
	// know where to put it back on show editor area request.
	editorHolder = new PartPlaceholder(editorArea.getID());
	presentation.getLayout().replace(editorArea, editorHolder);

	// Disable the entire editor area so if an editor had
	// keyboard focus it will let it go.
	if (editorArea.getControl() != null)
		editorArea.getControl().setEnabled(false);
		
	setEditorAreaVisible(false);	
}
/**
 * Hides a fast view. The view shrinks equally <code>steps</code> times
 * before disappearing completely.
 */
private void hideFastView(IViewReference ref, int steps) {
	setFastViewIconSelection(ref, false);

	// Get pane.
	ViewPane pane = getPane(ref);
	// Hide the right side sash first
	if (fastViewSash != null)
		fastViewSash.setBounds(0, 0, 0, 0);
	Control ctrl = pane.getControl();
	
	if(steps != 0) {
		// Slide it off screen.
		Rectangle bounds = pane.getBounds();
		int increment = bounds.width / steps;
		
		// Record the longest we can go before cancelling the animation, 
		// and the minimum time we will allow each step to take.
		// Note: We always do at least one step of the animation.
		long endTime = System.currentTimeMillis() + FASTVIEW_HIDE_MAX_DURATION;
		long minStepTime = FASTVIEW_HIDE_MIN_DURATION / steps;

		for (int i = 0; i <= bounds.width - 2; i += increment) {
			long time = System.currentTimeMillis();
			ctrl.setLocation(-i, bounds.y);
			ctrl.getParent().update();
			long afterTime = System.currentTimeMillis();
			if (afterTime >= endTime) {
				// Took too long. Just exit the loop.
				break;
			}
			long stepDuration = afterTime - time;
			if (stepDuration < minStepTime) {
				// Note: This doesn't take into account the overhead of doing
				// the loop and these calculations, so the total delay is
				// always slightly more than "minStepTime".
				try {
					Thread.sleep (minStepTime - stepDuration);
				} catch (InterruptedException ex) {
					// Do nothing.
				}
			}
		}
	}
	// Hide it completely.
	pane.setVisible(false);
	pane.setFastViewSash(null);
	ctrl.setEnabled(false); // Remove focus support.
}
/**
 * Hides the fast view sash for zooming in a fast view.
 */
void hideFastViewSash() {
	if (fastViewSash != null)
		fastViewSash.setBounds(0, 0, 0, 0);
}
public boolean hideView(IViewReference ref) {
	// If the view is locked just return.
	ViewPane pane = getPane(ref);
	
	// Remove the view from the current presentation.
	if (isFastView(ref)) {
		fastViews.remove(ref);
		if(pane != null)
			pane.setFast(false);		//force an update of the toolbar
		if (activeFastView == ref)
			setActiveFastView(null);
		if(pane != null)
			pane.getControl().setEnabled(true);
	} else { 
		presentation.removePart(pane);
	}
	
	// Dispose view if ref count == 0.
	getViewFactory().releaseView(ref.getId());
	return true;
}
/*
 * Return whether the editor area is visible or not.
 */
protected boolean isEditorAreaVisible() {
	return editorHolder == null;
}
/**
 * Returns true if a view is fast.
 */
public boolean isFastView(IViewReference ref) {
	return fastViews.contains(ref);
}
/**
 * Creates a new presentation from a persistence file.
 * Note: This method should not modify the current state of the perspective.
 */
private void loadCustomPersp(PerspectiveDescriptor persp)
{
	//get the layout from the registry	
	PerspectiveRegistry perspRegistry = (PerspectiveRegistry) WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	try {
		IMemento memento = perspRegistry.getCustomPersp(persp.getId());
		// Restore the layout state.
		MultiStatus status = new MultiStatus(
			PlatformUI.PLUGIN_ID,IStatus.OK,
			WorkbenchMessages.format("Perspective.unableToRestorePerspective",new String[]{persp.getLabel()}), //$NON-NLS-1$
			null);
		status.merge(restoreState(memento));
		status.merge(restoreState());
		if(status.getSeverity() != IStatus.OK) {
			unableToOpenPerspective(persp,status);
		}
	} catch (IOException e) {
		unableToOpenPerspective(persp,null);
	} catch (WorkbenchException e) {
		unableToOpenPerspective(persp,e.getStatus());
	}
}
private void unableToOpenPerspective(PerspectiveDescriptor persp,IStatus status) {
	PerspectiveRegistry perspRegistry = (PerspectiveRegistry) WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	perspRegistry.deletePerspective(persp);
	String title = WorkbenchMessages.getString("Perspective.problemRestoringTitle");  //$NON-NLS-1$
	String msg = WorkbenchMessages.getString("Perspective.errorReadingState"); //$NON-NLS-1$
	if(status == null) {
		MessageDialog.openError((Shell)null,title,msg); 
	} else {
		ErrorDialog.openError((Shell)null,title,msg,status); 
	}
}

/**
 * Create a presentation for a perspective.
 * Note: This method should not modify the current state of the perspective.
 */
private void loadPredefinedPersp(
	PerspectiveDescriptor persp)
	throws WorkbenchException
{
	// Create layout engine.
	IPerspectiveFactory factory = null;
	try {
		factory = persp.createFactory();
	} catch (CoreException e) {
		throw new WorkbenchException(WorkbenchMessages.format("Perspective.unableToLoad", new Object[] {persp.getId()})); //$NON-NLS-1$
	}

	// Create layout factory.
	RootLayoutContainer container = new RootLayoutContainer(page);
	PageLayout layout = new PageLayout(container, getViewFactory(), editorArea);

	// Run layout engine.
	factory.createInitialLayout(layout);
	PerspectiveExtensionReader extender = new PerspectiveExtensionReader();
	extender.extendLayout(descriptor.getId(), layout);

	// Retrieve fast view width ratios stored in the page layout.
	mapFastViewToWidthRatio.putAll(layout.getFastViewToWidthRatioMap());

	// Create action sets.
	createInitialActionSets(layout.getActionSets());
	alwaysOnActionSets.addAll(visibleActionSets);
	newWizardActionIds = layout.getNewWizardActionIds();
	showViewActionIds = layout.getShowViewActionIds();
	perspectiveActionIds = layout.getPerspectiveActionIds();
	showInPartIds = layout.getShowInPartIds();
	
	// Create fast views
	fastViews = layout.getFastViews();
		
	// Create presentation.	
	presentation = new PerspectivePresentation(page, container);

	// Hide editor area if requested by factory
	if (!layout.isEditorAreaVisible())
		hideEditorArea();
		
}
/**
 * activate.
 */
protected void onActivate() {
	// Update editor area state.
	if (editorArea.getControl() != null) {
		if (isEditorAreaVisible()) {
			// Enable the editor area now that it will be made
			// visible and can accept keyboard focus again.
			editorArea.getControl().setEnabled(true);
			setEditorAreaVisible(true);
		} else {
			// Disable the entire editor area so if an editor had
			// keyboard focus it will let it go.
			editorArea.getControl().setEnabled(false);
			setEditorAreaVisible(false);
		}
	}

	// Update fast views.
	// Make sure the control for the fastviews are create so they can
	// be activated.
	for (int i = 0; i < fastViews.size(); i++){
		ViewPane pane = getPane((IViewReference)fastViews.get(i));
		if(pane != null) {
			Control ctrl = pane.getControl();
			if (ctrl == null) {
				pane.createControl(getClientComposite());
				ctrl = pane.getControl();
			}
			presentation.enableDrag(pane);		
			ctrl.setEnabled(false); // Remove focus support.
		}
	}
	
	setAllPinsVisible(true);
	presentation.activate(getClientComposite());
	getClientComposite().addListener(SWT.Resize, resizeListener);
	
	if (shouldHideEditorsOnActivate) {
		// We do this here to ensure that createPartControl is called on the top editor
		// before it is hidden. See bug 20166.
		hideEditorArea();
		shouldHideEditorsOnActivate = false;
	}
}
/**
 * deactivate.
 */
protected void onDeactivate() {
	getClientComposite().removeListener(SWT.Resize, resizeListener);
	presentation.deactivate();
	setActiveFastView(null);
	setAllPinsVisible(false);

	// Update fast views.
	for (int i = 0; i < fastViews.size(); i++){
		ViewPane pane = getPane((IViewReference)fastViews.get(i));
		if(pane != null) {
			presentation.disableDrag(pane);
			Control ctrl = pane.getControl();
			if (ctrl != null)
				ctrl.setEnabled(true); // Add focus support.
		}
	}
}
/**
 * Notifies that a part has been activated.
 */
public void partActivated(IWorkbenchPart activePart) {
	// If a fastview is open close it.
	if (activeFastView != null && activeFastView.getPart(false) != activePart)
		setActiveFastView(null);
}

/**
 * The user successfully performed a Show In... action on the specified part.
 * Update the history.
 */
public void performedShowIn(String partId) {
	showInTimes.put(partId, new Long(System.currentTimeMillis()));
}

/**
 * Sets the fast view attribute.
 * Note: The page is expected to update action bars.
 */
public void removeFastView(IViewReference ref) {
	ViewPane pane = getPane(ref);
	if (isFastView(ref)) {
		if (activeFastView == ref)
			setActiveFastView(null);
		fastViews.remove(ref);
		pane.setFast(false);
		Control ctrl = pane.getControl();
		if (ctrl != null)
			ctrl.setEnabled(true); // Modify focus support.
		// We are disabling the pane because it will be enabled when it
		// is added to the presentation. When a pane is enabled a drop
		// listener is added to it, and we do not want to have multiple
		// listeners for a pane
		presentation.disableDrag(pane);	
		presentation.addPart(pane);
	}
}
/**
 * Fills a presentation with layout data.
 * Note: This method should not modify the current state of the perspective.
 */
public IStatus restoreState(IMemento memento) {
	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("Perspective.problemsRestoringPerspective"),null); //$NON-NLS-1$

	// Create persp descriptor.
	descriptor = new PerspectiveDescriptor(null,null,null);
	result.add(descriptor.restoreState(memento));
	PerspectiveDescriptor desc = (PerspectiveDescriptor)WorkbenchPlugin
		.getDefault().getPerspectiveRegistry().findPerspectiveWithId(descriptor.getId());
	if (desc != null)
		descriptor = desc;
		
	// Create the toolbar layout.
	IMemento layoutMem = memento.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
	if (layoutMem != null) {
		toolBarLayout = new CoolBarLayout();
		boolean success = toolBarLayout.restoreState(layoutMem);
		if (!success) toolBarLayout = null;
	}	
	this.memento = memento;
	// Add the visible views.
	IMemento views[] = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
	result.merge(createReferences(views));
	
	memento = memento.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
	if(memento != null) {
		views = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
		result.merge(createReferences(views));	
	}
	return result;
}
private IStatus createReferences(IMemento views[]) {
	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("Perspective.problemsRestoringViews"),null); //$NON-NLS-1$
	
	for (int x = 0; x < views.length; x ++) {
		// Get the view details.
		IMemento childMem = views[x];
		String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);
		// Create and open the view.
		try {
			if(!"true".equals(childMem.getString(IWorkbenchConstants.TAG_REMOVED))) //$NON-NLS-1$
				viewFactory.createView(viewID);
		} catch (PartInitException e) {
			childMem.putString(IWorkbenchConstants.TAG_REMOVED,"true"); //$NON-NLS-1$
			result.add(new Status(IStatus.ERROR,PlatformUI.PLUGIN_ID,0,e.getMessage(),e));
		}
	}
	return result;
}

/**
 * Fills a presentation with layout data.
 * Note: This method should not modify the current state of the perspective.
 */
public IStatus restoreState() {
	if(this.memento == null)
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$

	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("Perspective.problemsRestoringPerspective"),null); //$NON-NLS-1$
				
	IMemento memento = this.memento;
	this.memento = null;
	
	IMemento boundsMem = memento.getChild(IWorkbenchConstants.TAG_WINDOW);
	if(boundsMem != null) {
		Rectangle r = new Rectangle(0,0,0,0);
		r.x = boundsMem.getInteger(IWorkbenchConstants.TAG_X).intValue();
		r.y = boundsMem.getInteger(IWorkbenchConstants.TAG_Y).intValue();
		r.height = boundsMem.getInteger(IWorkbenchConstants.TAG_HEIGHT).intValue();
		r.width = boundsMem.getInteger(IWorkbenchConstants.TAG_WIDTH).intValue();
		if(page.getWorkbenchWindow().getPages().length == 0) {
			page.getWorkbenchWindow().getShell().setBounds(r);
		}
	}
	
	// Create an empty presentation..
	RootLayoutContainer mainLayout = new RootLayoutContainer(page);
	PerspectivePresentation pres = new PerspectivePresentation(page, mainLayout);

	// Read the layout.
	result.merge(pres.restoreState(memento.getChild(IWorkbenchConstants.TAG_LAYOUT)));

	// Add the editor workbook. Do not hide it now.
	pres.replacePlaceholderWithPart(editorArea);

	// Add the visible views.
	IMemento [] views = memento.getChildren(IWorkbenchConstants.TAG_VIEW);

	for (int x = 0; x < views.length; x ++) {
		// Get the view details.
		IMemento childMem = views[x];
		String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);

		// Create and open the view.
		WorkbenchPartReference ref = (WorkbenchPartReference)viewFactory.getView(viewID);
		if(ref == null) {
			WorkbenchPlugin.log("Could not create view: '" + viewID + "'."); //$NON-NLS-1$ //$NON-NLS-2$
			result.add(new Status(
				Status.ERROR,PlatformUI.PLUGIN_ID,0,
				WorkbenchMessages.format("Perspective.couldNotFind", new String[]{viewID}), //$NON-NLS-1$
				null));
			continue;
		}
		if(ref.getPane() == null) {
			ref.setPane(new ViewPane((IViewReference)ref,page));
		}
		page.addPart(ref);
		if(pres.willPartBeVisible(ref.getId())) {
			IStatus restoreStatus = viewFactory.restoreView((IViewReference)ref);
			result.add(restoreStatus);
			if(restoreStatus.getSeverity() == IStatus.OK) {
				IViewPart view = (IViewPart)ref.getPart(true);
				if(view != null) {
					ViewSite site = (ViewSite)view.getSite();
					ViewPane pane = (ViewPane)site.getPane();			
					pres.replacePlaceholderWithPart(pane);
				}
			} else {
				page.removePart(ref);
			}
		} else {
			pres.replacePlaceholderWithPart(ref.getPane());			
		}
	}

	// Load the fast views
	IMemento fastViewsMem = memento.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
	if(fastViewsMem != null) {
		views = fastViewsMem.getChildren(IWorkbenchConstants.TAG_VIEW);
		for (int x = 0; x < views.length; x ++) {
			// Get the view details.
			IMemento childMem = views[x];
			String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);
			Float ratio = childMem.getFloat(IWorkbenchConstants.TAG_RATIO);
			if (ratio == null) {
				Integer viewWidth = childMem.getInteger(IWorkbenchConstants.TAG_WIDTH);
				if (viewWidth == null)
					ratio = new Float(IPageLayout.DEFAULT_FASTVIEW_RATIO);
				else
					ratio = new Float((float)viewWidth.intValue() / (float)getClientComposite().getSize().x);
			}
			mapFastViewToWidthRatio.put(viewID, ratio);
				
			WorkbenchPartReference ref = (WorkbenchPartReference) viewFactory.getView(viewID);
			if(ref == null) {
				WorkbenchPlugin.log("Could not create view: '" + viewID + "'."); //$NON-NLS-1$ //$NON-NLS-2$
				result.add(new Status(
					Status.ERROR,PlatformUI.PLUGIN_ID,0,
					WorkbenchMessages.format("Perspective.couldNotFind", new String[]{viewID}), //$NON-NLS-1$
					null));
				continue;
			}
			// Add to fast view list because creating a view pane
			// will come back to check if its a fast view. We really
			// need to clean up this code.		
			fastViews.add(ref);
			if(ref.getPane() == null) {
				ref.setPane(new ViewPane((IViewReference)ref,page));
			}
			page.addPart(ref);
		}
	}
		
	// Load the action sets.
	IMemento [] actions = memento.getChildren(IWorkbenchConstants.TAG_ACTION_SET);
	ArrayList actionsArray = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String actionSetID = actions[x].getString(IWorkbenchConstants.TAG_ID);
		actionsArray.add(actionSetID);
	}
	createInitialActionSets(actionsArray);

	// Load the always on action sets.
	actions = memento.getChildren(IWorkbenchConstants.TAG_ALWAYS_ON_ACTION_SET);
	for (int x = 0; x < actions.length; x ++) {
		String actionSetID = actions[x].getString(IWorkbenchConstants.TAG_ID);
		IActionSetDescriptor d = 
			WorkbenchPlugin.getDefault().getActionSetRegistry().findActionSet(actionSetID);
		if (d != null) 
			alwaysOnActionSets.add(d);	
	}

	// Load the always off action sets.
	actions = memento.getChildren(IWorkbenchConstants.TAG_ALWAYS_OFF_ACTION_SET);
	for (int x = 0; x < actions.length; x ++) {
		String actionSetID = actions[x].getString(IWorkbenchConstants.TAG_ID);
		IActionSetDescriptor d = 
			WorkbenchPlugin.getDefault().getActionSetRegistry().findActionSet(actionSetID);
		if (d != null) 
			alwaysOffActionSets.add(d);	
	}

	// Load "show view actions".
	actions = memento.getChildren(IWorkbenchConstants.TAG_SHOW_VIEW_ACTION);
	showViewActionIds = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		showViewActionIds.add(id);
	}
	
	// Load "show in times".
	actions = memento.getChildren(IWorkbenchConstants.TAG_SHOW_IN_TIME);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		String timeStr = actions[x].getString(IWorkbenchConstants.TAG_TIME);
		if (id != null && timeStr != null) {
			try {
				long time = Long.parseLong(timeStr);
				showInTimes.put(id, new Long(time));
			}
			catch (NumberFormatException e) {
				// skip this one
			}
		}
	}
	
	// Load "show in parts" from registry, not memento
	showInPartIds = getShowInIdsFromRegistry();
	
	// Load "new wizard actions".
	actions = memento.getChildren(IWorkbenchConstants.TAG_NEW_WIZARD_ACTION);
	newWizardActionIds = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		newWizardActionIds.add(id);
	}
	
	// Load "perspective actions".
	actions = memento.getChildren(IWorkbenchConstants.TAG_PERSPECTIVE_ACTION);
	perspectiveActionIds = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		perspectiveActionIds.add(id);
	}
	
	// Save presentation.	
	presentation = pres;

	// Hide the editor area if needed. Need to wait for the
	// presentation to be fully setup first.
	Integer areaVisible = memento.getInteger(IWorkbenchConstants.TAG_AREA_VISIBLE);
	// Rather than hiding the editors now we must wait until after their controls
	// are created. This ensures that if an editor is instantiated, createPartControl
	// is also called. See bug 20166.
	shouldHideEditorsOnActivate = (areaVisible != null && areaVisible.intValue() == 0);
	return result;
}

/**
 * Returns the Show In... part ids read from the registry.  
 */
private ArrayList getShowInIdsFromRegistry() {
	PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
	reader.setIncludeOnlyTags(new String[] { PerspectiveExtensionReader.TAG_SHOW_IN_PART });
	PageLayout layout = new PageLayout();
	reader.extendLayout(descriptor.getOriginalId(), layout);
	return layout.getShowInPartIds();
}

/**
 * Save the layout.
 */
public void saveDesc() {
	saveDescAs(descriptor);
}
/**
 * Save the layout.
 */
public void saveDescAs(IPerspectiveDescriptor desc) {		
	PerspectiveDescriptor realDesc = (PerspectiveDescriptor)desc;
	//get the layout from the registry	
	PerspectiveRegistry perspRegistry = (PerspectiveRegistry) WorkbenchPlugin.getDefault().getPerspectiveRegistry();
	// Capture the layout state.	
	XMLMemento memento = XMLMemento.createWriteRoot("perspective");//$NON-NLS-1$
	IStatus status = saveState(memento, realDesc, false);
	if(status.getSeverity() == IStatus.ERROR) {
		ErrorDialog.openError((Shell)null, 
			WorkbenchMessages.getString("Perspective.problemSavingTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Perspective.problemSavingMessage"), //$NON-NLS-1$
			status);
		return;
	}
	//save it to the preference store
	try {
		perspRegistry.saveCustomPersp(realDesc, memento);
		descriptor = realDesc;
	} catch (IOException e) {
		perspRegistry.deletePerspective(realDesc);
		MessageDialog.openError((Shell)null, 
			WorkbenchMessages.getString("Perspective.problemSavingTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Perspective.problemSavingMessage")); //$NON-NLS-1$
	}
}
/**
 * Save the layout.
 */
public IStatus saveState(IMemento memento) {
	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("Perspective.problemsSavingPerspective"),null); //$NON-NLS-1$
		
	result.merge(saveState(memento, descriptor, true));

	return result;
}
/**
 * Save the layout.
 */
private IStatus saveState(IMemento memento, PerspectiveDescriptor p,
	boolean saveInnerViewState)
{
	MultiStatus result = new MultiStatus(
		PlatformUI.PLUGIN_ID,IStatus.OK,
		WorkbenchMessages.getString("Perspective.problemsSavingPerspective"),null); //$NON-NLS-1$

	if(this.memento != null) {
		memento.putMemento(this.memento);
		return result;
	}
			
	// Save the version number.
	memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING);
	result.add(p.saveState(memento));
	if(!saveInnerViewState) {
		Rectangle bounds = page.getWorkbenchWindow().getShell().getBounds();
		IMemento boundsMem = memento.createChild(IWorkbenchConstants.TAG_WINDOW);
		boundsMem.putInteger(IWorkbenchConstants.TAG_X,bounds.x);
		boundsMem.putInteger(IWorkbenchConstants.TAG_Y,bounds.y);
		boundsMem.putInteger(IWorkbenchConstants.TAG_HEIGHT,bounds.height);
		boundsMem.putInteger(IWorkbenchConstants.TAG_WIDTH,bounds.width);
	}
	
	// Save the visible action sets.
	Iterator enum = visibleActionSets.iterator();
	while (enum.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_ACTION_SET);
		child.putString(IWorkbenchConstants.TAG_ID, desc.getId());
	}

	// Save the "always on" action sets.
	enum = alwaysOnActionSets.iterator();
	while (enum.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_ALWAYS_ON_ACTION_SET);
		child.putString(IWorkbenchConstants.TAG_ID, desc.getId());
	}

	// Save the "always off" action sets.
	enum = alwaysOffActionSets.iterator();
	while (enum.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_ALWAYS_OFF_ACTION_SET);
		child.putString(IWorkbenchConstants.TAG_ID, desc.getId());
	}

	// Save "show view actions"
	enum = showViewActionIds.iterator();
	while (enum.hasNext()) {
		String str = (String)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_SHOW_VIEW_ACTION);
		child.putString(IWorkbenchConstants.TAG_ID, str);
	}

	// Save "show in times"
	enum = showInTimes.keySet().iterator();
	while (enum.hasNext()) {
		String id = (String) enum.next();
		Long time = (Long) showInTimes.get(id);
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_SHOW_IN_TIME);
		child.putString(IWorkbenchConstants.TAG_ID, id);
		child.putString(IWorkbenchConstants.TAG_TIME, time.toString());
	}

	// Save "new wizard actions".
	enum = newWizardActionIds.iterator();
	while (enum.hasNext()) {
		String str = (String)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_NEW_WIZARD_ACTION);
		child.putString(IWorkbenchConstants.TAG_ID, str);
	}
	
	// Save "perspective actions".
	enum = perspectiveActionIds.iterator();
	while (enum.hasNext()) {
		String str = (String)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_PERSPECTIVE_ACTION);
		child.putString(IWorkbenchConstants.TAG_ID, str);
	}
	
	// Get visible views.
	List viewPanes = new ArrayList(5);
	presentation.collectViewPanes(viewPanes);

	// Save the views.
	enum = viewPanes.iterator();
	int errors = 0;
	while (enum.hasNext()) {
		ViewPane pane = (ViewPane)enum.next();
		IViewReference ref = pane.getViewReference();
		IMemento viewMemento = memento.createChild(IWorkbenchConstants.TAG_VIEW);
		viewMemento.putString(IWorkbenchConstants.TAG_ID, ref.getId());
	}

	if(fastViews.size() > 0) {
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_FAST_VIEWS);
		enum = fastViews.iterator();
		while (enum.hasNext()) {
			IViewReference ref = (IViewReference)enum.next();
			IMemento viewMemento = childMem.createChild(IWorkbenchConstants.TAG_VIEW);
			String id = ref.getId();
			viewMemento.putString(IWorkbenchConstants.TAG_ID, id);
			float ratio = getFastViewWidthRatio(id);
			viewMemento.putFloat(IWorkbenchConstants.TAG_RATIO, ratio);
		}
	}
	if(errors > 0) {
		String message = WorkbenchMessages.getString("Perspective.multipleErrors"); //$NON-NLS-1$
		if(errors == 1)
			message = WorkbenchMessages.getString("Perspective.oneError"); //$NON-NLS-1$
		MessageDialog.openError(null, WorkbenchMessages.getString("Error"), message); //$NON-NLS-1$
	}
	
	// Save the layout.
	IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_LAYOUT);
	result.add(presentation.saveState(childMem));

	// Save the toolbar layout.
	if (toolBarLayout != null) {
		childMem = memento.createChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
		result.add(toolBarLayout.saveState(childMem));
	}

	// Save the editor visibility state
	if (isEditorAreaVisible())
		memento.putInteger(IWorkbenchConstants.TAG_AREA_VISIBLE, 1);
	else
		memento.putInteger(IWorkbenchConstants.TAG_AREA_VISIBLE, 0);
	return result;
}
/**
 * Sets the visible action sets. 
 * Note: The page is expected to update action bars.
 */
public void setActionSets(IActionSetDescriptor[] newArray) {
	// We assume that changes to action set visibilty should be remembered
	// and not reversed as parts are activated.
	ArrayList turnedOff = (ArrayList)visibleActionSets.clone();
	for (int i = 0; i < newArray.length; i++) {
		IActionSetDescriptor desc = newArray[i];
		turnedOff.remove(desc);
		if (!visibleActionSets.contains(desc)) {
			// make sure this always stays visible
			alwaysOnActionSets.add(desc);
			alwaysOffActionSets.remove(desc);
		}
	}
	for (int i = 0; i < turnedOff.size(); i++) {
		IActionSetDescriptor desc = (IActionSetDescriptor)turnedOff.get(i);
		// make sure this always stays hidden
		alwaysOnActionSets.remove(desc);
		alwaysOffActionSets.add(desc);
	}
	
	visibleActionSets.clear();
	int newSize = newArray.length;
	for (int i = 0; i < newSize; i ++) {
		visibleActionSets.add(newArray[i]);
	}
}
/**
 * Return the active fast view or null if there are no
 * fast views or if there are all minimized.
 */
public IViewReference getActiveFastView() {
	return activeFastView;
}
/**
 * Sets the active fast view. If a different fast view is already open,
 * it shrinks equally <code>steps</code> times before disappearing
 * completely. Then, <code>view</code> becomes active and is shown.
 */
/*package*/ void setActiveFastView(IViewReference ref, int steps) {
	if (activeFastView == ref)
		return;
		
	if (activeFastView != null)
		previousActiveFastView = activeFastView;
		
	if (activeFastView != null) {
		ViewPane pane = getPane(activeFastView);
		if (pane.isZoomed()) {
			presentation.zoomOut();
		}
		hideFastView(activeFastView, steps);
	}
	activeFastView = ref;
	if (activeFastView != null) {
		showFastView(activeFastView);
	}
}
/**
 * Sets the active fast view.
 */
/*package*/ void setActiveFastView(IViewReference ref) {
	setActiveFastView(ref, FASTVIEW_HIDE_STEPS);
}
/**
 * Sets the visibility of all fast view pins.
 */
private void setAllPinsVisible(boolean visible) {
	Iterator iter = fastViews.iterator();
	while (iter.hasNext()) {
		ViewPane pane = getPane((IViewReference)iter.next());
		if(pane != null)
			pane.setFast(visible);
	}
}
/**
 * Sets the selection for the shortcut bar icon representing the givevn fast view.
 */
private void setFastViewIconSelection(IViewReference ref, boolean selected) {
	WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();
	ToolBar bar = window.getShortcutBar().getControl();
	ToolItem[] items = bar.getItems();
	for(int i=0; i<items.length; i++) {
		if (items[i].getData(ShowFastViewContribution.FAST_VIEW) == ref) {
			items[i].setSelection(selected);
		}
	}	
}
/**
 * Sets the new wizard actions for the page.
 * This is List of Strings.
 */
public void setNewWizardActionIds(ArrayList newList ) {
	newWizardActionIds = newList;
}
/**
 * Sets the perspective actions for this page.
 * This is List of Strings.
 */
public void setPerspectiveActionIds(ArrayList list) {
	perspectiveActionIds = list;
}
/**
 * Sets the ids of the parts to list in the Show In... prompter.
 * This is a List of Strings.
 */
public void setShowInPartIds(ArrayList list) {
	showInPartIds = list;
}
/**
 * Sets the ids of the views to list in the Show View shortcuts.
 * This is a List of Strings.
 */
public void setShowViewActionIds(ArrayList list) {
	showViewActionIds = list;
}
/**
 * Sets the toolbar layout for this perspective.
 */
public void setToolBarLayout(CoolBarLayout layout) {
	toolBarLayout = layout;
}
/**
 * @see IWorkbenchPage
 * Note: The page is expected to update action bars.
 */
public void showActionSet(String id) {
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	IActionSetDescriptor desc = reg.findActionSet(id);
	if (alwaysOffActionSets.contains(desc))
		return;
	if (desc != null && !visibleActionSets.contains(desc))
		visibleActionSets.add(desc);
}
/**
 * Show the editor area if not visible
 */
protected void showEditorArea() {
	if (isEditorAreaVisible())
		return;

	// Enable the editor area now that it will be made
	// visible and can accept keyboard focus again.
	if (editorArea.getControl() != null)
		editorArea.getControl().setEnabled(true);

	setEditorAreaVisible(true);

	// Replace the part holder with the editor area.
	presentation.getLayout().replace(editorHolder, editorArea);
	editorHolder = null;
}

private void setEditorAreaVisible(boolean visible) {
	ArrayList workbooks = ((EditorArea)editorArea).getEditorWorkbooks();
	for (Iterator iter = workbooks.iterator(); iter.hasNext();) {
		EditorWorkbook workbook = (EditorWorkbook) iter.next();
		workbook.setVisible(visible);
		EditorPane pane = workbook.getVisibleEditor();
		if(pane != null)
			pane.setVisible(visible);
	}
	editorArea.setVisible(visible);
}
/**
 * Shows a fast view.
 */
void showFastView(IViewReference ref) {
	// Make sure the part is restored.
	if(ref.getPart(true) == null)
		return;
	
	ViewPane pane = getPane(ref);	

	// Create the control first
	Control ctrl = pane.getControl();
	if(ctrl == null) {
		pane.createControl(getClientComposite());
		ctrl = pane.getControl();
	}
		
	// Show pane fast.
	ctrl.setEnabled(true); // Add focus support.
	Composite parent = ctrl.getParent();
	Rectangle bounds = getFastViewBounds(ref);

	pane.setVisible(true);
	pane.setBounds(bounds);
	pane.moveAbove(null);
	pane.setFocus();

	// Show the Sash to enable right side resize
	if (fastViewSash == null) {
		fastViewSash = new Sash(parent, SWT.VERTICAL);
		fastViewSash.addPaintListener(paintListener);
		fastViewSash.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				fastViewSash.removePaintListener(paintListener);
			}
			public void focusLost(FocusEvent e) {
				fastViewSash.addPaintListener(paintListener);
			}
		});
		fastViewSash.addSelectionListener(selectionListener);
	}
	pane.setFastViewSash(fastViewSash);
	updateFastViewSashBounds(bounds);
	fastViewSash.moveAbove(null);
	
	setFastViewIconSelection(ref, true);
}

public void updateFastViewSashBounds(Rectangle partBounds) {
	fastViewSash.setBounds(partBounds.x + partBounds.width - 1, partBounds.y + 1, SASH_SIZE, partBounds.height - 2);
}
/**
 * See IWorkbenchPage.
 */
public IViewPart showView(String viewID) 
	throws PartInitException 
{
	ViewFactory factory = getViewFactory();
	IViewReference ref = factory.createView(viewID);
	IViewPart part = (IViewPart)ref.getPart(false);
	if(part == null) {
		IStatus status = factory.restoreView(ref);
		if(status.getSeverity() == IStatus.ERROR) {
			if(status.getException() instanceof PartInitException)
				throw (PartInitException)status.getException();
			else
				throw new PartInitException(status);
		} else { //No error so the part has been created
			part = (IViewPart)ref.getPart(false);
		}
	}
	ViewSite site = (ViewSite)part.getSite();
	ViewPane pane = (ViewPane)site.getPane();
	
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	int openViewMode = store.getInt(IPreferenceConstants.OPEN_VIEW_MODE);
	if (presentation.hasPlaceholder(viewID)) {
		presentation.addPart(pane);
	} else if (openViewMode == IPreferenceConstants.OVM_EMBED) {
		presentation.addPart(pane);
	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * } else if (openViewMode == IPreferenceConstants.OVM_FLOAT && presentation.canDetach()) {
	 * 	   presentation.addDetachedPart(pane);
	 */
	} else {
		showFastView(ref);
		addFastView(ref);
		//Refresh the part as there might have been an error when showing
	}
	return part;
}
/**
 * Toggles the visibility of a fast view.  If the view is active it
 * is deactivated.  Otherwise, it is activated.
 */
public void toggleFastView(IViewReference ref) {
	if (ref == activeFastView) {
		setActiveFastView(null);
	} else {
		setActiveFastView(ref);
	}
}
/**
 * Returns the oldPartID.
 * @return String
 */
public String getOldPartID() {
	return oldPartID;
}

/**
 * Sets the oldPartID.
 * @param oldPartID The oldPartID to set
 */
public void setOldPartID(String oldPartID) {
	this.oldPartID = oldPartID;
}
/**
 * Method moveFastView.  Moves draggedView to the position above
 * destinationView.  If placeAtEnd is true, add view to the end. Otherwise,
 * either place above destination view, or at the beginning if the
 * destinationView is null
 * @param draggedView
 * @param destinationView
 * @param useDestination
 */

/*package*/ void moveFastView(IViewReference draggedView, IViewReference destinationView, boolean placeAtEnd) {
	//PR 6988
	
	//do nothing if views are the same
	if (draggedView == destinationView)
		return;
		
	//move the view
	fastViews.remove(draggedView);
	//determine where to place the view
	if (placeAtEnd) {//add it to the end
		fastViews.add(draggedView);
	} else { 
		if (destinationView == null) //add it to the beginning
			fastViews.add(0, draggedView);
		else
			fastViews.add(fastViews.indexOf(destinationView), draggedView);
	}
}

}
