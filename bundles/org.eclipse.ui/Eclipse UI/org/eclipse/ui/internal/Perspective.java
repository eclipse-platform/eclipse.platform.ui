package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
**********************************************************************/
import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.part.ViewPart;

import java.util.List;

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
	private IViewPart activeFastView;
	private IViewPart previousActiveFastView;
	private IMemento memento;
	protected PerspectivePresentation presentation;
	final static private String VERSION_STRING = "0.016";//$NON-NLS-1$
	
	// fields used by fast view resizing via a sash
	private static final int SASH_SIZE = 3;
	private static final int FASTVIEW_HIDE_STEPS = 3;
	private static final RGB RGB_COLOR1 = new RGB(132, 130, 132);
	private static final RGB RGB_COLOR2 = new RGB(143, 141, 138);
	private static final RGB RGB_COLOR3 = new RGB(171, 168, 165);
	private Color borderColor1;
	private Color borderColor2;
	private Color borderColor3;
	private Map mapFastViewToWidthRatio = new HashMap();
	private Sash fastViewSash;
	private CoolBarLayout toolBarLayout;
	
	//Number of open editors before reusing. If < 0, use the 
	//user preference settings.
	private int reuseEditors = -1;

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
				fastViewSash.setBounds(bounds.width - SASH_SIZE, bounds.y, SASH_SIZE, bounds.height - SASH_SIZE);
				fastViewSash.moveAbove(null);
			}
		}
	};

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
public void addFastView(IViewPart view) {
	ViewPane pane = getPane(view);
	if (!isFastView(view)) {
		// Only remove the part from the presentation if it
		// is actually in the presentation.
		if (presentation.hasPlaceholder(pane.getID()) ||
			pane.getContainer() != null)
				presentation.removePart(pane);
		// We are drag-enabling the pane because it has been disabled
		// when it was removed from the perspective presentation.
		presentation.enableDrag(pane);
		fastViews.add(view);
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
public boolean bringToTop(IViewPart part) {
	if (isFastView(part)) {
		setActiveFastView(part);
		return true;
	} else {
		ViewPane pane = getPane(part);
		return presentation.bringPartToTop(pane);
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
	IViewPart test = findView(id);
	return (view == test);
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
	if (persp.hasCustomFile()) {
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
	IViewPart [] views = getViews();
	int length = views.length;
	for (int nX = 0; nX < length; nX ++) {
		getViewFactory().releaseView(views[nX].getSite().getId());
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
public IViewPart findView(String id) {
	IViewPart [] views = getViews();
	for (int nX = 0; nX < views.length; nX ++) {
		IViewPart part = views[nX];
		if (id.equals(part.getSite().getId()))
			return part;
	}
	return null;
}
/**
 * Returns an array of the visible action sets. 
 */
public IActionSetDescriptor[] getActionSets() {
	int size = visibleActionSets.size();
	IActionSetDescriptor [] array = new IActionSetDescriptor[size];
	for (int nX = 0; nX < size; nX ++) {
		array[nX] = (IActionSetDescriptor)visibleActionSets.get(nX);
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
/*package*/ Rectangle getFastViewBounds(IViewPart part) {
	// Copy the bounds of the page composite
	Rectangle bounds = page.getClientComposite().getBounds();
	ViewPane pane = (ViewPane)((ViewSite)part.getSite()).getPane();
	// get the width ratio of the fast view
	float ratio = getFastViewWidthRatio(pane.getID());
	// Compute the actual width of the fast view.
	bounds.width = (int)(ratio*(float)getClientComposite().getSize().x);
	return bounds;
}
/**
 * Returns the docked views.
 */
public IViewPart [] getFastViews() {
	int size = fastViews.size();
	IViewPart [] array = new IViewPart[size];
	array = (IViewPart [])fastViews.toArray(array);
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
 * Returns the pane for a view.
 */
private ViewPane getPane(IViewPart view) {
	ViewSite site = (ViewSite)view.getSite();
	return (ViewPane)site.getPane();
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
 * Returns the show view actions the page.
 * This is List of Strings.
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
/*package*/ IViewPart getPreviousActiveFastView() {
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
	for (int i = 0; i < fastViews.size(); i ++) {
		ViewSite site = (ViewSite)((IViewPart)fastViews.get(i)).getSite();
		ViewPane pane = (ViewPane)site.getPane();
		resultArray[nView] = pane.getViewReference();
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
 * See IWorkbenchPage.
 */
public IViewPart [] getViews() {
	// Get normal views.
	if(presentation == null)
		return new IViewPart[0];
	
	List resVector = new ArrayList(5);
	presentation.collectViewPanes(resVector);

	// Create a view array.
	int nFastViews = fastViews.size();
	int nNormalViews = resVector.size();
	IViewPart [] resArray = new IViewPart[nNormalViews + nFastViews];

	// Copy fast views.
	int nView = 0;
	for (int nX = 0; nX < nFastViews; nX ++) {
		resArray[nView] = (IViewPart)fastViews.get(nX);
		++ nView;
	}
	
	// Copy normal views.
	for (int nX = 0; nX < nNormalViews; nX ++) {
		ViewPane pane = (ViewPane)resVector.get(nX);
		resArray[nView] = (IViewPart)pane.getViewReference().getPart(true);
		++ nView;
	}
	
	return resArray;
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
}
/**
 * Hides a fast view. The view shrinks equally <code>steps</code> times
 * before disappearing completely.
 */
private void hideFastView(IViewPart part, int steps) {
	setFastViewIconSelection(part, false);

	// Get pane.
	ViewPane pane = getPane(part);
	// Hide the right side sash first
	if (fastViewSash != null)
		fastViewSash.setBounds(0, 0, 0, 0);
	Control ctrl = pane.getControl();
	
	if(steps != 0) {
		// Slide it off screen.
		Rectangle bounds = pane.getBounds();
		int increment = bounds.width / steps;
		for (int nX = 0; nX <= bounds.width - 2; nX += increment) {
			ctrl.setLocation(-nX, bounds.y);
			ctrl.getParent().update();
		}
	}
	// Hide it completely.
	pane.setBounds(0, 0, 0, 0);
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
/**
 * See IWorkbenchPage
 */
public boolean hideView(IViewPart view) {
	// If the view is locked just return.
	ViewSite site = (ViewSite)view.getSite();
	ViewPane pane = (ViewPane)site.getPane();
	
	// Remove the view from the current presentation.
	if (isFastView(view)) {
		fastViews.remove(view);
		pane.setFast(false);		//force an update of the toolbar
		if (activeFastView == view)
			setActiveFastView(null);
		pane.getControl().setEnabled(true);
	} else { 
		presentation.removePart(pane);
	}
	
	// Dispose view if ref count == 0.
	getViewFactory().releaseView(site.getId());
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
public boolean isFastView(IViewPart part) {
	return fastViews.contains(part);
}
/**
 * Creates a new presentation from a persistence file.
 * Note: This method should not modify the current state of the perspective.
 */
private void loadCustomPersp(PerspectiveDescriptor persp)
{
	try {
		InputStream stream = new FileInputStream(persp.getCustomFile());
		InputStreamReader reader = new InputStreamReader(stream, "utf-8"); //$NON-NLS-1$
		// Restore the layout state.
		IMemento memento = XMLMemento.createReadRoot(reader);
		restoreState(memento);
		restoreState();
		reader.close();
	} catch (IOException e) {
		persp.deleteCustomFile();
		MessageDialog.openError((Shell)null, 
			WorkbenchMessages.getString("Perspective.problemRestoringTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Perspective.errorReadingState")); //$NON-NLS-1$
		return;
	} catch (WorkbenchException e) {
		persp.deleteCustomFile();
		ErrorDialog.openError(
			(Shell) null, 
			WorkbenchMessages.getString("Perspective.problemRestoringTitle"),//$NON-NLS-1$
			WorkbenchMessages.getString("Perspective.errorReadingState"), //$NON-NLS-1$
			e.getStatus());
		return;
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
	RootLayoutContainer container = new RootLayoutContainer(page.getMouseDownListener());
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
		}
		else {
			// Disable the entire editor area so if an editor had
			// keyboard focus it will let it go.
			editorArea.getControl().setEnabled(false);
		}
	}

	// Update fast views.
	// Make sure the control for the fastviews are create so they can
	// be activated.
	for (int i = 0; i < fastViews.size(); i++){
		ViewPane pane = getPane((IViewPart)fastViews.get(i));
		Control ctrl = pane.getControl();
		if (ctrl == null) {
			pane.createControl(getClientComposite());
			ctrl = pane.getControl();
		}
		presentation.enableDrag(pane);		
		ctrl.setEnabled(false); // Remove focus support.
	}
	
	setAllPinsVisible(true);
	presentation.activate(getClientComposite());
	getClientComposite().addListener(SWT.Resize, resizeListener);
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
		ViewPane pane = getPane((IViewPart)fastViews.get(i));
		presentation.disableDrag(pane);
		Control ctrl = pane.getControl();
		if (ctrl != null)
			ctrl.setEnabled(true); // Add focus support.
	}
}
/**
 * Notifies that a part has been activated.
 */
public void partActivated(IWorkbenchPart activePart) {
	// If a fastview is open close it.
	if (activeFastView != null && activeFastView != activePart)
		setActiveFastView(null);
}
/**
 * Sets the fast view attribute.
 * Note: The page is expected to update action bars.
 */
public void removeFastView(IViewPart view) {
	ViewPane pane = getPane(view);
	if (isFastView(view)) {
		if (activeFastView == view)
			setActiveFastView(null);
		fastViews.remove(view);
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
public void restoreState(IMemento memento) {
	// Create persp descriptor.
	descriptor = new PerspectiveDescriptor(null,null,null);
	descriptor.restoreState(memento);
	PerspectiveDescriptor desc = (PerspectiveDescriptor)WorkbenchPlugin
		.getDefault().getPerspectiveRegistry().findPerspectiveWithId(descriptor.getId());
	if (desc != null)
		descriptor = desc;
		
	// Create the toolbar layout.
	IMemento layoutMem = memento.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
	if (layoutMem != null) {
		toolBarLayout = new CoolBarLayout();
		toolBarLayout.restoreState(layoutMem);
	}	
	this.memento = memento;
	// Add the visible views.
	IMemento views[] = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
	createReferences(views);
	
	memento = memento.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
	if(memento != null) {
		views = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
		createReferences(views);	
	}
}
private void createReferences(IMemento views[]) {
	for (int x = 0; x < views.length; x ++) {
		// Get the view details.
		IMemento childMem = views[x];
		String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);
	
		// Create and open the view.
		try {
			viewFactory.createView(viewID);
		} catch (PartInitException e) {
		}
	}
}

/**
 * Fills a presentation with layout data.
 * Note: This method should not modify the current state of the perspective.
 */
public void restoreState() {
	if(this.memento == null)
		return;
		
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
	RootLayoutContainer mainLayout = new RootLayoutContainer(page.getMouseDownListener());
	PerspectivePresentation pres = new PerspectivePresentation(page, mainLayout);

	// Read the layout.
	pres.restoreState(memento.getChild(IWorkbenchConstants.TAG_LAYOUT));

	// Add the editor workbook. Do not hide it now.
	pres.replacePlaceholderWithPart(editorArea);

	// Add the visible views.
	IMemento [] views = memento.getChildren(IWorkbenchConstants.TAG_VIEW);
	List errors = new ArrayList();
	for (int x = 0; x < views.length; x ++) {
		// Get the view details.
		IMemento childMem = views[x];
		String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);

		// Create and open the view.
		IViewReference ref = viewFactory.getView(viewID);
		if(ref == null) {
			WorkbenchPlugin.log("Could not create view: '" + viewID + "'."); //$NON-NLS-1$
			continue;
		}		
		page.addPart(ref);
		IViewPart view = (IViewPart)ref.getPart(true);
		ViewSite site = (ViewSite)view.getSite();
		ViewPane pane = (ViewPane)site.getPane();			
		pres.replacePlaceholderWithPart(pane);
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
				
			IViewReference ref = viewFactory.getView(viewID);
			if(ref == null) {
				WorkbenchPlugin.log("Could not create view: '" + viewID + "'."); //$NON-NLS-1$
				continue;
			}		
			page.addPart(ref);
			fastViews.add(ref.getPart(true));
		}
	}

	if(errors.size() > 0) {
		String message;
		Status s;
		if(errors.size() == 1) {
			s = (Status)errors.get(0);
			message = WorkbenchMessages.getString("Perspective.oneErrorRestoring"); //$NON-NLS-1$
		} else {
			Status allErrors[] = new Status[errors.size()];
			errors.toArray(allErrors);
			message = WorkbenchMessages.getString("Perspective.couldNotCreateAllViews"); //$NON-NLS-1$
			s = new MultiStatus(PlatformUI.PLUGIN_ID,0,allErrors,WorkbenchMessages.getString("Perspective.multipleErrorsRestoring"),null); //$NON-NLS-1$
		}
		ErrorDialog.openError(null,WorkbenchMessages.getString("Error"),message,s); //$NON-NLS-1$
	}
		
	// Load the action sets.
	IMemento [] actions = memento.getChildren(IWorkbenchConstants.TAG_ACTION_SET);
	ArrayList result = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String actionSetID = actions[x].getString(IWorkbenchConstants.TAG_ID);
		result.add(actionSetID);
	}
	createInitialActionSets(result);

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
	if (areaVisible != null && areaVisible.intValue() == 0)
		hideEditorArea();
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
	// Capture the layout state.	
	XMLMemento memento = XMLMemento.createWriteRoot("perspective");//$NON-NLS-1$
	saveState(memento, (PerspectiveDescriptor)desc, false);

	// Save it to a file.
	PerspectiveDescriptor realDesc = (PerspectiveDescriptor)desc;
	try {
		OutputStream stream = new FileOutputStream(realDesc.getCustomFile());
		Writer writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
		memento.save(writer);
		writer.close();
		descriptor = realDesc;
	} catch (IOException e) {
		realDesc.deleteCustomFile();
		MessageDialog.openError((Shell)null, 
			WorkbenchMessages.getString("Perspective.problemSavingTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Perspective.problemSavingMessage")); //$NON-NLS-1$
	}
}
/**
 * Save the layout.
 */
public void saveState(IMemento memento)
{
	saveState(memento, descriptor, true);
	// Save the toolbar layout.
	if (toolBarLayout != null) {
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
		toolBarLayout.saveState(childMem);
	}
}
/**
 * Save the layout.
 */
private void saveState(IMemento memento, PerspectiveDescriptor p,
	boolean saveInnerViewState)
{
	if(this.memento != null) {
		memento.putMemento(this.memento);
		return;
	}
		
	// Save the version number.
	memento.putString(IWorkbenchConstants.TAG_VERSION, VERSION_STRING);
	p.saveState(memento);
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
	boolean active = page.getPerspective().getId().equals(descriptor.getId());
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
			IViewPart view = (IViewPart)enum.next();
			IMemento viewMemento = childMem.createChild(IWorkbenchConstants.TAG_VIEW);
			String id = view.getSite().getId();
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
	presentation.saveState(childMem);

	// Save the editor visibility state
	if (isEditorAreaVisible())
		memento.putInteger(IWorkbenchConstants.TAG_AREA_VISIBLE, 1);
	else
		memento.putInteger(IWorkbenchConstants.TAG_AREA_VISIBLE, 0);
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
	for (int nX = 0; nX < newSize; nX ++) {
		visibleActionSets.add(newArray[nX]);
	}
}
/**
 * Return the active fast view or null if there are no
 * fast views or if there are all minimized.
 */
public IViewPart getActiveFastView() {
	return activeFastView;
}
/**
 * Sets the active fast view. If a different fast view is already open,
 * it shrinks equally <code>steps</code> times before disappearing
 * completely. Then, <code>view</code> becomes active and is shown.
 */
/*package*/ void setActiveFastView(IViewPart view, int steps) {
	if (activeFastView == view)
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
	activeFastView = view;
	if (activeFastView != null) {
		showFastView(activeFastView);
	}
}
/**
 * Sets the active fast view.
 */
/*package*/ void setActiveFastView(IViewPart view) {
	setActiveFastView(view, FASTVIEW_HIDE_STEPS);
}
/**
 * Sets the visibility of all fast view pins.
 */
private void setAllPinsVisible(boolean visible) {
	Iterator iter = fastViews.iterator();
	while (iter.hasNext()) {
		IViewPart view = (IViewPart)iter.next();
		ViewPane pane = getPane(view);
		pane.setFast(visible);
	}
}
/**
 * Sets the selection for the shortcut bar icon representing the givevn fast view.
 */
private void setFastViewIconSelection(IViewPart part, boolean selected) {
	WorkbenchWindow window = (WorkbenchWindow)page.getWorkbenchWindow();
	ToolBar bar = window.getShortcutBar().getControl();
	ToolItem[] items = bar.getItems();
	for(int i=0; i<items.length; i++) {
		if (items[i].getData(ShowFastViewContribution.FAST_VIEW) == part) {
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
 * Sets the show view actions for the page.
 * This is List of Strings.
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

	// Replace the part holder with the editor area.
	presentation.getLayout().replace(editorHolder, editorArea);
	editorHolder = null;
}
/**
 * Shows a fast view.
 */
void showFastView(IViewPart part) {
	// Get pane.
	ViewPane pane = getPane(part);

	// Create the control first
	Control ctrl = pane.getControl();
	if(ctrl == null) {
		pane.createControl(getClientComposite());
		ctrl = pane.getControl();
	}
		
	// Show pane fast.
	ctrl.setEnabled(true); // Add focus support.
	Composite parent = ctrl.getParent();
	Rectangle bounds = getFastViewBounds(part);

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
	fastViewSash.setBounds(bounds.width - SASH_SIZE, bounds.y, SASH_SIZE, bounds.height - SASH_SIZE);
	fastViewSash.moveAbove(null);
	
	setFastViewIconSelection(part, true);
}
/**
 * See IWorkbenchPage.
 */
public IViewPart showView(String viewID) 
	throws PartInitException 
{
	IViewReference ref = getViewFactory().createView(viewID);
	IViewPart part = (IViewPart)ref.getPart(true);
	ViewSite site = (ViewSite)part.getSite();
	ViewPane pane = (ViewPane)site.getPane();
	
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	int openViewMode = store.getInt(IPreferenceConstants.OPEN_VIEW_MODE);
	if (presentation.hasPlaceholder(viewID)) {
		presentation.addPart(pane);
	}
	else if (openViewMode == IPreferenceConstants.OVM_EMBED) {
		presentation.addPart(pane);
	}
	/*
	 * Detached window no longer supported - remove when confirmed
	 *
	 * else if (openViewMode == IPreferenceConstants.OVM_FLOAT && presentation.canDetach()) {
	 * 	presentation.addDetachedPart(pane);
	 * }
	 */
	else {
		showFastView(part);
		addFastView(part);
		//Refresh the part as there might have been an error when showing
	}
	return part;
}
/**
 * Toggles the visibility of a fast view.  If the view is active it
 * is deactivated.  Otherwise, it is activated.
 */
public void toggleFastView(IViewPart view) {
	if (view == activeFastView) {
		setActiveFastView(null);
	} else {
		setActiveFastView(view);
	}
}
}
