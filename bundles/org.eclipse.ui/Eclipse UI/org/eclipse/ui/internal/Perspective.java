package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;
import java.io.*;
import java.util.List;	// name resolution problem with swt.widgets.*
import java.util.*;

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
	private ArrayList newWizardActions;
	private ArrayList showViewActions;
	private ArrayList perspectiveActions;
	private ArrayList fastViews;
	private IViewPart activeFastView;
	protected PerspectivePresentation presentation;
	final static private String VERSION_STRING = "0.016";//$NON-NLS-1$

	// resize listener to update fast view height when
	// window resized.
	Listener resizeListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.Resize && activeFastView != null) {
				ViewPane pane = getPane(activeFastView);
				Rectangle bounds = pane.getBounds();
				bounds.height = Math.max(0, getClientComposite().getSize().y);
				pane.setBounds(bounds);
			}
		}
	};
	
	// fields used by fast view resizing via a sash
	private static final int SASH_SIZE = 3;
	private static final RGB RGB_COLOR1 = new RGB(132, 130, 132);
	private static final RGB RGB_COLOR2 = new RGB(143, 141, 138);
	private static final RGB RGB_COLOR3 = new RGB(171, 168, 165);
	private Color borderColor1;
	private Color borderColor2;
	private Color borderColor3;
	private Map mapFastViewToWidth = new HashMap();
	private Sash fastViewSash;
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
			if (e.detail != SWT.DRAG && activeFastView != null) {
				ViewPane pane = getPane(activeFastView);
				Rectangle bounds = pane.getBounds();
				bounds.width = Math.max(0, e.x - bounds.x);
				pane.setBounds(bounds);
				mapFastViewToWidth.put(pane.getID(), new Integer(bounds.width));
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
	fastViews = new ArrayList(2);
}
/**
 * Sets the fast view attribute.
 * Note: The page is expected to update action bars.
 */
public void addFastView(IViewPart view) {
	ViewPane pane = getPane(view);
	if (!isFastView(view)) {
		presentation.removePart(pane);
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

	mapFastViewToWidth.clear();
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
public ArrayList getNewWizardActions() {
	return newWizardActions;
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
public ArrayList getPerspectiveActions() {
	return perspectiveActions;
}
/**
 * Returns the presentation.
 */
public PerspectivePresentation getPresentation() {
	return presentation;
}
/**
 * Returns the show view actions the page.
 * This is List of Strings.
 */
public ArrayList getShowViewActions() {
	return showViewActions;
}
/**
 * Returns the view factory.
 */
private ViewFactory getViewFactory() {
	return viewFactory;
}
/**
 * See IWorkbenchPage.
 */
public IViewPart [] getViews() {
	// Get normal views.
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
		resArray[nView] = pane.getViewPart();
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
 * Hides a fast view.
 */
private void hideFastView(IViewPart part) {
	// Get pane.
	ViewPane pane = getPane(part);

	// Hide the right side sash first
	if (fastViewSash != null)
		fastViewSash.setBounds(0, 0, 0, 0);
	
	// Slide it off screen.
	Rectangle bounds = pane.getBounds();
	Control ctrl = pane.getControl();
	for (int nX = 0; nX <= bounds.width; nX += 50) {
		ctrl.setLocation(-nX, bounds.y);
		ctrl.getParent().update();
	}

	// Hide it completely.
	pane.setBounds(0, 0, 0, 0);
	ctrl.setEnabled(false); // Remove focus support.
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
		if (activeFastView == view)
			setActiveFastView(null);
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
		InputStreamReader reader = new InputStreamReader(stream, "utf-8");
		// Restore the layout state.
		IMemento memento = XMLMemento.createReadRoot(reader);
		restoreState(memento);
		reader.close();
	} catch (IOException e) {
		persp.deleteCustomFile();
		MessageDialog.openError((Shell)null, 
			WorkbenchMessages.getString("Perspective.problemRestoringTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Perspective.errorReadingState")); //$NON-NLS-1$
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

	// Create action sets.
	createInitialActionSets(layout.getActionSets());
	newWizardActions = layout.getNewWizardActions();
	showViewActions = layout.getShowViewActions();
	perspectiveActions = layout.getPerspectiveActions();
	
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
	int errors = 0;
	for (int x = 0; x < views.length; x ++) {
		// Get the view details.
		IMemento childMem = views[x];
		String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);

		// Create and open the view.
		ViewPane pane = restoreView(childMem,viewID);
		if(pane != null) 
			pres.replacePlaceholderWithPart(pane);
		else
			errors++;
	}

	// Load the fast views
	IMemento fastViewsMem = memento.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
	if(fastViewsMem != null) {
		views = fastViewsMem.getChildren(IWorkbenchConstants.TAG_VIEW);
		for (int x = 0; x < views.length; x ++) {
			// Get the view details.
			IMemento childMem = views[x];
			String viewID = childMem.getString(IWorkbenchConstants.TAG_ID);
			Integer width = childMem.getInteger(IWorkbenchConstants.TAG_WIDTH);
			if (width != null)
				mapFastViewToWidth.put(viewID,width);
				
			// Create and open the view.
			ViewPane pane = restoreView(childMem,viewID);
			if(pane != null) 
				fastViews.add(pane.getPart());
			else
				errors++;
		}
	}

	if(errors > 0) {
		String message = WorkbenchMessages.getString("Perspectiver.multipleErrorsRestoring"); //$NON-NLS-1$
		if(errors == 1)
			message = WorkbenchMessages.getString("Perspective.oneErrorRestoring"); //$NON-NLS-1$
		MessageDialog.openError(null, WorkbenchMessages.getString("Error"), message); //$NON-NLS-1$
	}
		
	// Load the action sets.
	IMemento [] actions = memento.getChildren(IWorkbenchConstants.TAG_ACTION_SET);
	ArrayList result = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String actionSetID = actions[x].getString(IWorkbenchConstants.TAG_ID);
		result.add(actionSetID);
	}
	createInitialActionSets(result);

	// Load "show view actions".
	actions = memento.getChildren(IWorkbenchConstants.TAG_SHOW_VIEW_ACTION);
	showViewActions = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		showViewActions.add(id);
	}
	
	// Load "new wizard actions".
	actions = memento.getChildren(IWorkbenchConstants.TAG_NEW_WIZARD_ACTION);
	newWizardActions = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		newWizardActions.add(id);
	}
	
	// Load "perspective actions".
	actions = memento.getChildren(IWorkbenchConstants.TAG_PERSPECTIVE_ACTION);
	perspectiveActions = new ArrayList(actions.length);
	for (int x = 0; x < actions.length; x ++) {
		String id = actions[x].getString(IWorkbenchConstants.TAG_ID);
		perspectiveActions.add(id);
	}
	
	// Save presentation.	
	presentation = pres;

	// Hide the editor area if needed. Need to wait for the
	// presentation to be fully setup first.
	Integer areaVisible = memento.getInteger(IWorkbenchConstants.TAG_AREA_VISIBLE);
	if (areaVisible != null && areaVisible.intValue() == 0)
		hideEditorArea();
}
/*
 * Create and return a new ViewPane. Return null if any error occur; 
 */
private ViewPane restoreView(final IMemento memento,final String viewID) {
	final ViewPane pane[] = new ViewPane[1];
	Platform.run(new SafeRunnableAdapter() {
		public void run() {
			try {
				IMemento stateMem = memento.getChild(IWorkbenchConstants.TAG_VIEW_STATE);
				if (stateMem == null)
					pane[0] = getViewFactory().createView(viewID);
				else
					pane[0] = getViewFactory().createView(viewID,stateMem);
			} catch (PartInitException e) {
				WorkbenchPlugin.log(e.getMessage());
			}
		}
		public void handleException(Throwable e) {
			//Execption is already logged.
		}
	});
	return pane[0];
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
		Writer writer = new OutputStreamWriter(stream, "utf-8");
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
}
/**
 * Save the layout.
 */
private void saveState(IMemento memento, PerspectiveDescriptor p,
	boolean saveInnerViewState)
{
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
	
	// Save the action sets.
	Iterator enum = visibleActionSets.iterator();
	while (enum.hasNext()) {
		IActionSetDescriptor desc = (IActionSetDescriptor)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_ACTION_SET);
		child.putString(IWorkbenchConstants.TAG_ID, desc.getId());
	}

	// Save "show view actions"
	enum = showViewActions.iterator();
	while (enum.hasNext()) {
		String str = (String)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_SHOW_VIEW_ACTION);
		child.putString(IWorkbenchConstants.TAG_ID, str);
	}

	// Save "new wizard actions".
	enum = newWizardActions.iterator();
	while (enum.hasNext()) {
		String str = (String)enum.next();
		IMemento child = memento.createChild(IWorkbenchConstants.TAG_NEW_WIZARD_ACTION);
		child.putString(IWorkbenchConstants.TAG_ID, str);
	}
	
	// Save "perspective actions".
	enum = perspectiveActions.iterator();
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
		IViewPart view = pane.getViewPart();
		IMemento viewMemento = memento.createChild(IWorkbenchConstants.TAG_VIEW);
		viewMemento.putString(IWorkbenchConstants.TAG_ID, view.getSite().getId());
		if (active && saveInnerViewState)
			if(!saveView(view,viewMemento))
				errors++;
	}

	if(fastViews.size() > 0) {
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_FAST_VIEWS);
		enum = fastViews.iterator();
		while (enum.hasNext()) {
			IViewPart view = (IViewPart)enum.next();
			IMemento viewMemento = childMem.createChild(IWorkbenchConstants.TAG_VIEW);
			String id = view.getSite().getId();
			viewMemento.putString(IWorkbenchConstants.TAG_ID, id);
			Integer width = (Integer)mapFastViewToWidth.get(id);
			if (width != null)
				viewMemento.putInteger(IWorkbenchConstants.TAG_WIDTH,width.intValue());
			if (active && saveInnerViewState)
				if(!saveView(view,viewMemento))
					errors++;
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
 * Save the layout.
 */
private boolean saveView(final IViewPart view, final IMemento memento) {
	final boolean result[] = new boolean[1];
	Platform.run(new SafeRunnableAdapter() {
		public void run() {
			view.saveState(memento.createChild(IWorkbenchConstants.TAG_VIEW_STATE));
			result[0] = true;
		}
		public void handleException(Throwable e) {
			result[0] = false;
		}
	});
	return result[0];
}
/**
 * Sets the visible action sets. 
 * Note: The page is expected to update action bars.
 */
public void setActionSets(IActionSetDescriptor[] newArray) {
	visibleActionSets.clear();
	int newSize = newArray.length;
	for (int nX = 0; nX < newSize; nX ++) {
		visibleActionSets.add(newArray[nX]);
	}
}
/**
 * Sets the active fast view.
 */
public void setActiveFastView(IViewPart view) {
	if (activeFastView == view)
		return;
	if (activeFastView != null) {
		hideFastView(activeFastView);
	}
	activeFastView = view;
	if (activeFastView != null) {
		showFastView(activeFastView);
	}
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
 * Sets the new wizard actions for the page.
 * This is List of Strings.
 */
public void setNewWizardActions(ArrayList newList ) {
	newWizardActions = newList;
}
/**
 * Sets the perspective actions for this page.
 * This is List of Strings.
 */
public void setPerspectiveActions(ArrayList list) {
	perspectiveActions = list;
}
/**
 * Sets the show view actions for the page.
 * This is List of Strings.
 */
public void setShowViewActions(ArrayList list) {
	showViewActions = list;
}
/**
 * @see IWorkbenchPage
 * Note: The page is expected to update action bars.
 */
public void showActionSet(String id) {
	ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
	IActionSetDescriptor desc = reg.findActionSet(id);
	if (desc != null)
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
private void showFastView(IViewPart part) {
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
	Rectangle bounds = parent.getClientArea();
	Integer width = (Integer)mapFastViewToWidth.get(pane.getID());
	if (width == null)
		bounds.width = 250;
	else
		bounds.width = width.intValue();
	pane.setBounds(bounds);
	pane.moveAbove(null);
	pane.setFocus();

	// Show the Sash to enable right side resize
	if (fastViewSash == null) {
		fastViewSash = new Sash(parent, SWT.VERTICAL);
		fastViewSash.addPaintListener(paintListener);
		fastViewSash.addSelectionListener(selectionListener);
	}
	fastViewSash.setBounds(bounds.width - SASH_SIZE, bounds.y, SASH_SIZE, bounds.height - SASH_SIZE);
	fastViewSash.moveAbove(null);
}
/**
 * See IWorkbenchPage.
 */
public IViewPart showView(String viewID) 
	throws PartInitException 
{
	ViewPane pane = getViewFactory().createView(viewID);
	presentation.addPart(pane);
	return pane.getViewPart();
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
