package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.UIHackFinder;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.part.*;


/**
 * Provides support for a title bar where the
 * title and icon of the view can be displayed.
 * Along with an X icon to close the view, and
 * a pin icon to attach the view back to the
 * main layout.
 *
 * Also provides support to add tool icons and menu
 * icon on the system bar if required by the view
 * part.
 */
public class ViewPane extends PartPane
	implements IPropertyListener
{
	private CLabel titleLabel;
		
	private boolean fast = false;
	private ToolBar isvToolBar;
	private ToolBarManager isvToolBarMgr;
	private MenuManager isvMenuMgr;
	private ToolBar systemBar;
	
	private ToolItem pullDownButton;
	private ToolItem pinButton;
	private ToolItem minimizeButton;
	private ToolItem closeButton;
	

	/**
	 * Tool bar manager
	 * @private
	 */
	class PaneToolBarManager extends ToolBarManager {
		public PaneToolBarManager(ToolBar paneToolBar) {
			super(paneToolBar);
		}
		protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
			// remove/add the action bar from the view so to avoid
			// having an empty action bar participating in the view's
			// layout calculation (and maybe causing an empty bar to appear)
			if (newCount < 1) {
				if (control.getTopCenter() != null)
					control.setTopCenter(null);
			}
			else {
				toolBar.layout();
				if (control.getTopCenter() == null)
					control.setTopCenter(toolBar);
			}
			Composite parent= toolBar.getParent();
			parent.layout();
			if (parent.getParent() != null)
				parent.getParent().layout();
		}       
	}

	/**
	 * PaneMenuManager
	 */
	public class PaneMenuManager extends MenuManager {
		public PaneMenuManager() {
			super("View Local Menu");
		}
		protected void update(boolean force, boolean recursive) {
			super.update(force, recursive);
			if (!isEmpty())
				createPulldownButton();
			else
				disposePulldownButton();
		}
	}

/**
 * Constructs a view pane for a view part.
 */
public ViewPane(IViewPart part, WorkbenchPage persp) {
	super(part, persp);
}
/**
 * Create control. Add the title bar.
 */
public void createControl(Composite parent) {
	// Only do this once.
	if (getControl() != null && !getControl().isDisposed())
		return;
		
	super.createControl(parent);
	
	Platform.run(new SafeRunnableAdapter() {
		public void run() { 
			// Update pin.
			if (fast)
				createFastButtons();

			// Install the part's tools and menu
			ViewActionBuilder builder = new ViewActionBuilder();
			builder.readActionExtensions(getViewPart());
			updateActionBars();
		}
		public void handleException(Throwable e) {
			//Just have it logged.
		}
	});
}
protected WorkbenchPart createErrorPart(WorkbenchPart oldPart) {
	class ErrorViewPart extends ViewPart {
		private Text text;
		public void createPartControl(Composite parent) {
			text = new Text(parent,SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
			text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
			text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_GRAY));
			text.setText("An error has occurred when creating this view");
		}
		public void setFocus() {
			if (text != null) text.setFocus();
		}
		public void setSite(IWorkbenchPartSite site) {
			super.setSite(site);
		}
	}   
	ErrorViewPart newPart = new ErrorViewPart();
	PartSite site = (PartSite)oldPart.getSite();
	newPart.setSite(site);
	site.setPart(newPart);
	return newPart;
}
/**
 * Create the pin button.
 */
private void createFastButtons() {
	if (pinButton == null) {
		pinButton = new ToolItem(systemBar, SWT.PUSH, systemBar.getItemCount() - 1);
		Image img = WorkbenchImages.getImage(
			IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW);
		pinButton.setImage(img);
		pinButton.setToolTipText("Pin");
		pinButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doPin();
			}
		});
	}
	if (minimizeButton == null) {
		minimizeButton = new ToolItem(systemBar, SWT.PUSH, systemBar.getItemCount() - 1);
		Image img = WorkbenchImages.getImage(
			IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW);
		minimizeButton.setImage(img);
		minimizeButton.setToolTipText("Minimize");
		minimizeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doMinimize();
			}
		});
	}
}
/**
 * Create a pulldown menu on the action bar.
 */
private void createPulldownButton() {
	if (systemBar == null)
		return;
	if (pullDownButton == null) {	
		pullDownButton = new ToolItem(systemBar, SWT.PUSH, 0);
		Image img = WorkbenchImages.getImage(
			IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
		pullDownButton.setImage(img);
		pullDownButton.setToolTipText("Menu");
		pullDownButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showViewMenu();
			}
		});
		ViewForm vf = getViewForm();
		if (vf != null)
			vf.layout();
	}
}
/**
 * Create a title bar for the pane.
 * 	- the view icon and title to the far left
 *	- the view toolbar appears in the middle.
 * 	- the view pulldown menu, pin button, and close button to the far right.
 */
protected void createTitleBar() {
	// Only do this once.
	if (titleLabel != null)
		return;

	// Title.   
	titleLabel = new CLabel(control, SWT.SHADOW_NONE);
	hookFocus(titleLabel);
	titleLabel.setAlignment(SWT.LEFT);
	titleLabel.setBackground(null, null);
	titleLabel.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.button == 3)
				showTitleLabelMenu(e);
		}
		public void mouseDoubleClick(MouseEvent event){
			doZoom();
		}
	});
	updateTitles();
	control.setTopLeft(titleLabel);

	// Listen to title changes.
	getViewPart().addPropertyListener(this);
	
	// ISV action bar.
	isvToolBar = new ToolBar(control, SWT.FLAT | SWT.WRAP);
	hookFocus(isvToolBar);
	isvToolBar.setFont(getFont());
	control.setTopCenter(isvToolBar);
	isvToolBar.addMouseListener(new MouseAdapter(){
		public void mouseDoubleClick(MouseEvent event) {
			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
			if (isvToolBar.getItem(new Point(event.x, event.y)) == null)
				doZoom();
		}
	});
	
	// System action bar.  
	systemBar = new ToolBar(control, SWT.FLAT | SWT.WRAP);
	hookFocus(systemBar);
	systemBar.addMouseListener(new MouseAdapter(){
		public void mouseDoubleClick(MouseEvent event) {
			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
			if (systemBar.getItem(new Point(event.x, event.y)) == null)
				doZoom();
		}
	});
	closeButton= new ToolItem(systemBar, SWT.PUSH);
	Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW);
	closeButton.setImage(img);
	closeButton.setToolTipText("Close");
	closeButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			doHide();
		}
	});
	if (isvMenuMgr != null && !isvMenuMgr.isEmpty())
		createPulldownButton();
	control.setTopRight(systemBar);
}
/**
 * Dispose the pin button.
 */
private void disposeFastButtons() {
	if (pinButton != null) {
		pinButton.dispose();
		pinButton = null;
	}
	if (minimizeButton != null) {
		minimizeButton.dispose();
		minimizeButton = null;
	}
}
/**
 * Dispose the pulldown button.
 */
private void disposePulldownButton() {
	if (pullDownButton != null) {
		pullDownButton.dispose();
		pullDownButton = null;
		ViewForm vf = getViewForm();
		if (vf != null)
			vf.layout();
	}
}
/**
 * @see PartPane::doHide
 */
public void doHide() {
	IWorkbenchPage page = getPart().getSite().getPage();
	page.hideView(getViewPart());
}
/**
 * Make this view pane a fast view
 */
protected void doMakeFast() {
	WorkbenchPage page = ((WorkbenchPage)getPart().getSite().getPage());
	page.addFastView(getViewPart());
}
/**
 * Hide the fast view
 */
protected void doMinimize() {
	WorkbenchPage page = ((WorkbenchPage)getPart().getSite().getPage());
	page.toggleFastView(getViewPart());
}
/**
 * Pin the view.
 */
protected void doPin() {
	WorkbenchPage page = (WorkbenchPage)getPart().getSite().getPage();
	page.removeFastView(getViewPart());
}
/**
 * Returns the drag control.
 */
public Control getDragHandle() {
	return titleLabel;
}
/**
 * @see ViewActionBars
 */
public MenuManager getMenuManager() {
	if (isvMenuMgr == null)
		isvMenuMgr = new PaneMenuManager();
	return isvMenuMgr;
}
/**
 * @see ViewActionBars
 */
public ToolBarManager getToolBarManager() {
	if (isvToolBarMgr == null)
		isvToolBarMgr = new PaneToolBarManager(isvToolBar);
	return isvToolBarMgr;
}
/**
 * Answer the view part child.
 */
public IViewPart getViewPart() {
	return (IViewPart)getPart();
}
/**
 * Indicates that a property has changed.
 *
 * @param source the object whose property has changed
 * @param propId the id of the property which has changed; property ids
 *   are generally defined as constants on the source class
 */
public void propertyChanged(Object source, int propId) {
	if (propId == IWorkbenchPart.PROP_TITLE)
		updateTitles();
}
/**
 * Sets the fast view state.  If this view is a fast view then
 * various controls like pin and minimize are added to the
 * system bar.
 */
public void setFast(boolean b) {
	fast = b;
	if (getControl() != null) {
		if (fast)
			createFastButtons();
		else
			disposeFastButtons();
	}
}
/**
 * Indicate focus in part.
 */
public void showFocus(boolean inFocus) {
	if(titleLabel == null)
		return;
		
	if (inFocus) {
		titleLabel.setBackground(WorkbenchColors.getActiveViewGradient(), WorkbenchColors.getActiveViewGradientPercents());
		titleLabel.setForeground(WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		titleLabel.update();
		isvToolBar.setBackground(WorkbenchColors.getActiveViewGradientEnd());
		systemBar.setBackground(WorkbenchColors.getActiveViewGradientEnd());
	}
	else {
		titleLabel.setBackground(null, null);
		titleLabel.setForeground(null);
		titleLabel.update();
		isvToolBar.setBackground(WorkbenchColors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		systemBar.setBackground(WorkbenchColors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	}
}
/**
 * Show a title label menu for this pane.
 */
private void showTitleLabelMenu(MouseEvent e) {
	Menu aMenu = new Menu(titleLabel);
	MenuItem item; 

	// Get various view states.
	final boolean isZoomed = ((WorkbenchPage)getPart().getSite().getPage()).isZoomed();
	boolean isFastView = ((WorkbenchPage)getPart().getSite().getPage()).isFastView(getViewPart());
	boolean canZoom = (getWindow() instanceof IWorkbenchWindow);

	// add restore item
	item = new MenuItem(aMenu, SWT.NONE);
	item.setText("&Restore");
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (isZoomed)
				doZoom();
			else
				doPin();
		}
	});
	item.setEnabled(isZoomed || isFastView);

	// add fast view item
	item = new MenuItem(aMenu, SWT.NONE);
	item.setText("&Fast View");
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			doMakeFast();
		}
	});
	item.setEnabled(!isFastView);

	// add maximize item
	item = new MenuItem(aMenu, SWT.NONE);
	item.setText("Ma&ximize");
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			doZoom();
		}
	});
	item.setEnabled(!isZoomed && !isFastView && canZoom);

	new MenuItem(aMenu, SWT.SEPARATOR);
	
	// add close item
	item = new MenuItem(aMenu, SWT.CASCADE);
	item.setText("&Close");
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			doHide();
		}
	});

	// open menu    
	Point point = new Point(e.x, e.y);
	point = titleLabel.toDisplay(point);
	aMenu.setLocation(point.x, point.y);
	aMenu.setVisible(true);
}
/**
 * Show the context menu for this window.
 */
private void showViewMenu() {
	Menu aMenu = isvMenuMgr.createContextMenu(getControl());
	Point topLeft = new Point(0, 0);
	topLeft.y += systemBar.getBounds().height;
	topLeft = systemBar.toDisplay(topLeft);
	aMenu.setLocation(topLeft.x, topLeft.y);
	aMenu.setVisible(true);
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	ILayoutContainer container = getContainer();
	if (container instanceof PartTabFolder)
		return (PartTabFolder) container;
	else
		return this;
}
public String toString() {
	String label = "disposed";
	if((titleLabel != null) && (!titleLabel.isDisposed()))
		label = titleLabel.getText();
	
	return getClass().getName() + "@" + Integer.toHexString(hashCode()) + 
	"(" + label + ")";
}
/**
 * @see ViewActionBars
 */
public void updateActionBars() {
	if (isvMenuMgr != null)
		isvMenuMgr.updateAll(false);
	if (isvToolBarMgr != null)
		getToolBarManager().update(false);
}
/**
 * Update the title attributes.
 */
public void updateTitles() {
	IViewPart view = getViewPart();
	String text = view.getTitle();
	if (text == null)
		text = "";
	Image image = view.getTitleImage();
	// only update and relayout if text or image has changed
	if (!text.equals(titleLabel.getText()) || image != titleLabel.getImage()) {
		titleLabel.setText(text);
		titleLabel.setImage(image);
		((Composite) getControl()).layout();
	}
	titleLabel.setToolTipText(view.getTitleToolTip());
	// XXX: Workaround for 1GCGA89: SWT:ALL - CLabel tool tip does not always update properly
	titleLabel.update();
}
}
