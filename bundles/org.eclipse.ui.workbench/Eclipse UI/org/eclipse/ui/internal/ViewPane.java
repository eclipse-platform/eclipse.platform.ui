package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> 
    - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;


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
	private boolean showFocus = false;
	private ToolBar viewToolBar;
	private ToolBarManager viewToolBarMgr;
	private ToolBar isvToolBar;
	private ToolBarManager isvToolBarMgr;
	private MenuManager isvMenuMgr;
	private ToolItem pullDownButton;
	
	/**
	 * Indicates whether a toolbar button is shown for the view local menu.
	 */
	private boolean showMenuButton = false;
	
	//Created in o.e.ui.Perspective, disposed there.
	private Sash fastViewSash;
	

	/**
	 * Toolbar manager for the ISV toolbar.
	 */
	class PaneToolBarManager extends ToolBarManager {
		public PaneToolBarManager(ToolBar paneToolBar) {
			super(paneToolBar);
		}
		
		protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
			toolBar.layout();
			Composite parent = toolBar.getParent();
			parent.layout();
			if (parent.getParent() != null)
				parent.getParent().layout();
		}
	}

	/**
	 * Menu manager for view local menu.
	 */
	class PaneMenuManager extends MenuManager {
		public PaneMenuManager() {
			super("View Local Menu");//$NON-NLS-1$
		}
		protected void update(boolean force, boolean recursive) {
			// Changes to the menu can affect whether the toolbar has a menu button.
			// Update it if necessary.
			if (showMenuButton != !isEmpty()) {
				viewToolBarMgr.update(true);
			}
			super.update(force, recursive);
		}
	}

	/**
	 * Contributes system actions to toolbar.
	 */
	class SystemContribution extends ContributionItem {
		public boolean isDynamic() {
			return true;
		}
		
		public void fill(ToolBar toolbar, int index) {
			showMenuButton = (isvMenuMgr != null && !isvMenuMgr.isEmpty());
			if (showMenuButton) {
				pullDownButton = new ToolItem(toolbar, SWT.PUSH, index++);
//				Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
				Image hoverImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER);
				pullDownButton.setDisabledImage(hoverImage); // PR#1GE56QT - Avoid creation of unnecessary image.
				pullDownButton.setImage(hoverImage);
//				pullDownButton.setHotImage(hoverImage);
				pullDownButton.setToolTipText(WorkbenchMessages.getString("Menu")); //$NON-NLS-1$
				pullDownButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						showViewMenu();
					}
				});
			}
			else {
				// clear out the button if we don't need it anymore
				pullDownButton = null;
			}
		
			// check the current internal fast view state
			if (fast) {
				ToolItem dockButton = new ToolItem(toolbar, SWT.CHECK, index++);
				dockButton.setSelection(true);
//				Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW);
				Image hoverImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW_HOVER);
				dockButton.setDisabledImage(hoverImage); // PR#1GE56QT - Avoid creation of unnecessary image.
				dockButton.setImage(hoverImage);
//				dockButton.setHotImage(hoverImage);
				dockButton.setToolTipText(WorkbenchMessages.getString("ViewPane.pin")); //$NON-NLS-1$
				dockButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						doDock();
					}
				});
		
				ToolItem minimizeButton = new ToolItem(toolbar, SWT.PUSH, index++);
//				img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW);
				hoverImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_HOVER);
				minimizeButton.setDisabledImage(hoverImage); // PR#1GE56QT - Avoid creation of unnecessary image.
				minimizeButton.setImage(hoverImage);
//				minimizeButton.setHotImage(img);
				minimizeButton.setToolTipText(WorkbenchMessages.getString("ViewPane.minimize")); //$NON-NLS-1$
				minimizeButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						doMinimize();
					}
				});
			}
			
			ToolItem closeButton= new ToolItem(toolbar, SWT.PUSH, index++);
//			Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW);
			Image hoverImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_HOVER);
			closeButton.setDisabledImage(hoverImage); // PR#1GE56QT - Avoid creation of unnecessary image.
			closeButton.setImage(hoverImage);
//			closeButton.setHotImage(hoverImage);
			closeButton.setToolTipText(WorkbenchMessages.getString("Close")); //$NON-NLS-1$
			closeButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					doHide();
				}
			});
		}
	}
	
	private SystemContribution systemContribution = new SystemContribution();
	
/**
 * Constructs a view pane for a view part.
 */
public ViewPane(IViewReference ref, WorkbenchPage page) {
	super(ref, page);
	fast = ref.isFastView();
}
/**
 * Create control. Add the title bar.
 */
public void createControl(Composite parent) {
	// Only do this once.
	if (getControl() != null && !getControl().isDisposed())
		return;
		
	super.createControl(parent);
	
	// Only include the ISV toolbar and the content in the tab list.
	// All actions on the System toolbar should be accessible on the pane menu.
	if (control.getContent() == null) {
		// content can be null if view creation failed
		control.setTabList(new Control[] { isvToolBar, viewToolBar });
	}
	else {
		control.setTabList(new Control[] { isvToolBar, viewToolBar, control.getContent() });
	}
}

protected void createChildControl() {
	final IWorkbenchPart part[] = new IWorkbenchPart[]{partReference.getPart(false)};
	if(part[0] == null)
		return;

	if(control == null || control.getContent() != null)
		return;
		
	super.createChildControl();
	
	Platform.run(new SafeRunnable() {
		public void run() { 
			// Install the part's tools and menu
			ViewActionBuilder builder = new ViewActionBuilder();
			IViewPart part = (IViewPart)getViewReference().getPart(true);
			if(part != null) {
				builder.readActionExtensions(part);
				ActionDescriptor[] extendedActions = builder.getExtendedActions();
				KeyBindingService keyBindingService = (KeyBindingService)part.getSite().getKeyBindingService();
				keyBindingService.registerExtendedActions(extendedActions);
			}
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
			text.setForeground(JFaceColors.getErrorText(text.getDisplay()));
			text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			text.setText(WorkbenchMessages.getString("ViewPane.errorMessage")); //$NON-NLS-1$
		}
		public void setFocus() {
			if (text != null) text.setFocus();
		}
		public void setSite(IWorkbenchPartSite site) {
			super.setSite(site);
		}
		public void setTitle(String title) {
			super.setTitle(title);
		}
	}   
	ErrorViewPart newPart = new ErrorViewPart();
	PartSite site = (PartSite)oldPart.getSite();
	newPart.setSite(site);
	newPart.setTitle(site.getRegisteredName());
	site.setPart(newPart);
	return newPart;
}

/**
 * See LayoutPart
 */
public boolean isDragAllowed(Point p) {
	// See also similar restrictions in addMoveItems method
	// No need to worry about fast views as they do not
	// register for D&D operations
	return !overImage(p.x) && !isZoomed();
}
/*
 * Return true if <code>x</code> is over the label image.
 */
private boolean overImage(int x) {
	return x < titleLabel.getImage().getBounds().width;
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
	titleLabel.setAlignment(SWT.LEFT);
	titleLabel.setBackground(null, null);
	titleLabel.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.button == 3)
				showPaneMenu(titleLabel,new Point(e.x, e.y));
			else if ((e.button == 1) && overImage(e.x))
				showPaneMenu();
		}
		public void mouseDoubleClick(MouseEvent event){
			doZoom();
		}
	});
	updateTitles();
	control.setTopLeft(titleLabel);

	// Listen to title changes.
	getPartReference().addPropertyListener(this);
	
	// View toolbar
	viewToolBar = new ToolBar(control, SWT.FLAT | SWT.WRAP);
	control.setTopRight(viewToolBar);
	viewToolBar.addMouseListener(new MouseAdapter(){
		public void mouseDoubleClick(MouseEvent event) {
			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
			if (viewToolBar.getItem(new Point(event.x, event.y)) == null)
				doZoom();
		}
	});
	viewToolBarMgr = new PaneToolBarManager(viewToolBar);
	viewToolBarMgr.add(systemContribution);
	
	// ISV toolbar.
	isvToolBar = new ToolBar(control, SWT.FLAT | SWT.WRAP);
	control.setTopCenter(isvToolBar);
	isvToolBar.addMouseListener(new MouseAdapter(){
		public void mouseDoubleClick(MouseEvent event) {
			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
			if (isvToolBar.getItem(new Point(event.x, event.y)) == null)
				doZoom();
		}
	});
	isvToolBarMgr = new PaneToolBarManager(isvToolBar);
}
public void dispose() {
	super.dispose();
	if (isvMenuMgr != null)
		isvMenuMgr.dispose();
	if (isvToolBarMgr != null)
		isvToolBarMgr.dispose();
	if (viewToolBarMgr != null)
		viewToolBarMgr.dispose();
}
/**
 * @see PartPane#doHide
 */
public void doHide() {
	getPage().hideView(getViewReference());
}
/**
 * Make this view pane a fast view
 */
protected void doMakeFast() {
	getPage().addFastView(getViewReference());
}
/**
 * Hide the fast view
 */
protected void doMinimize() {
	getPage().toggleFastView(getViewReference());
}
/**
 * Pin the view.
 */
protected void doDock() {
	getPage().removeFastView(getViewReference());
}

/**
 * Draws the applicable gradient on the view's title area
 */
/* package */ void drawGradient() {
	if (titleLabel == null || viewToolBar == null || isvToolBar == null)
		return;

	if (showFocus) {
		if (getShellActivated()) {
			titleLabel.setBackground(WorkbenchColors.getActiveViewGradient(), WorkbenchColors.getActiveViewGradientPercents());
			titleLabel.setForeground(WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		}
		else {
			titleLabel.setBackground(WorkbenchColors.getDeactivatedViewGradient(), WorkbenchColors.getDeactivatedViewGradientPercents());
			titleLabel.setForeground(WorkbenchColors.getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		}
	}
	else {
		titleLabel.setBackground(null, null);
		titleLabel.setForeground(null);
	}

	titleLabel.update();
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
 * Returns the tab list to use when this part is active.
 * Includes the view and its tab (if applicable), in the appropriate order.
 */
public Control[] getTabList() {
	Control c = getControl();
	if (getContainer() instanceof PartTabFolder) {
		PartTabFolder tf = (PartTabFolder) getContainer();
		CTabFolder f = (CTabFolder) tf.getControl();
		if (f.getItemCount() > 1) {
			if ((f.getStyle() & SWT.TOP) != 0) {
				return new Control[] { f, c };
			}
			else {
				return new Control[] { c, f };
			}
		}
	}
	return new Control[] { c };
}

/**
 * @see ViewActionBars
 */
public ToolBarManager getToolBarManager() {
	return isvToolBarMgr;
}
/**
 * Answer the view part child.
 */
public IViewReference getViewReference() {
	return (IViewReference)getPartReference();
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
	if (viewToolBarMgr != null) {
		viewToolBarMgr.update(true);
	}
}
public void setFastViewSash(Sash s) {
	fastViewSash = s;
}

/* (non-Javadoc)
 * Method declared on PartPane.
 */
/* package */ void shellActivated() {
	drawGradient();
}

/* (non-Javadoc)
 * Method declared on PartPane.
 */
/* package */ void shellDeactivated() {
	drawGradient();
}

/**
 * Indicate focus in part.
 */
public void showFocus(boolean inFocus) {
	if (titleLabel == null)
		return;

	showFocus = inFocus;
	drawGradient();
}

/**
 * Shows the pane menu (system menu) for this pane.
 */
public void showPaneMenu() {
	// If this is a fast view, it may have been minimized. Do nothing in this case.
	if(isFastView() && (page.getActiveFastView() != getViewReference()))
		return;
	Rectangle bounds = titleLabel.getBounds();
	showPaneMenu(titleLabel,new Point(0,bounds.height));
}
/**
 * Return true if this view is a fast view.
 */
private boolean isFastView() {
	return page.isFastView(getViewReference());
}
/**
 * Finds and return the sashes around this part.
 */
protected Sashes findSashes() {
	Sashes result = new Sashes();
	if(isFastView()) {
		result.right = fastViewSash;		
		return result;
	}
	RootLayoutContainer	container = getRootContainer();
	if(container == null)
		return result;
		
	if (this.getContainer() instanceof PartTabFolder)
		container.findSashes((PartTabFolder)this.getContainer(),result);
	else
		container.findSashes(this,result);
	return result;
}
/**
 * Add the Fast View menu item to the view title menu.
 */
protected void addFastViewMenuItem(Menu parent,boolean isFastView) {
	// add fast view item
	MenuItem item = new MenuItem(parent, SWT.CHECK);
	item.setText(WorkbenchMessages.getString("ViewPane.fastView")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (isFastView())
				doDock();
			else
				doMakeFast();
		}
	});
	item.setSelection(isFastView);
	
	if(isFastView) {
		item = new MenuItem(parent, SWT.NONE);
		item.setText(WorkbenchMessages.getString("ViewPane.minimizeView")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doMinimize();
			}
		});
		item.setEnabled(true);
	}
}
/**
 * Add the View and Tab Group items to the Move menu.
 */
protected void addMoveItems(Menu moveMenu) {
	// See also similar restrictions in isDragAllowed method.
	// No need to worry about mouse cursor over image, just
	// fast views.
	boolean moveAllowed = !isZoomed() && !isFastView();
	
	// Add move view only menu item
	MenuItem item = new MenuItem(moveMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString("ViewPane.moveView")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			page.openTracker(ViewPane.this);
		}
	});
	item.setEnabled(moveAllowed);
	
	// Add move view's tab folder menu item
	item = new MenuItem(moveMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString("ViewPane.moveFolder")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			ILayoutContainer container = getContainer();
			if (container instanceof PartTabFolder)
				((PartTabFolder)container).openTracker((PartTabFolder)container);
		}
	});
	item.setEnabled(moveAllowed && (getContainer() instanceof PartTabFolder));
}

/**
 * Return if there should be a view menu at all.
 * There is no view menu if there is no menu manager,
 * no pull down button or if the receiver is an
 * inactive fast view.
 */
public boolean hasViewMenu(){
	if (isvMenuMgr == null)
		return false;
		
	//If there is no pull down button there is no associated menu
	if(pullDownButton == null || pullDownButton.isDisposed())
		return false;
		
	return true;
}

/**
 * Show the view menu for this window.
 */
public void showViewMenu() {
	
	if(!hasViewMenu())
		return;
		
	// If this is a fast view, it may have been minimized. Do nothing in this case.
	if(isFastView() && (page.getActiveFastView() != getViewReference()))
		return;
				
	Menu aMenu = isvMenuMgr.createContextMenu(getControl());
	Rectangle bounds = pullDownButton.getBounds();
	Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
	topLeft = viewToolBar.toDisplay(topLeft);
	aMenu.setLocation(topLeft.x, topLeft.y);
	aMenu.setVisible(true);
}
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	// When zoomed, its like we are not part of the
	// tab folder so return the view.
	if (isZoomed())
		return this;

	// Make use of the container if a tab folder
	ILayoutContainer container = getContainer();
	if (container instanceof PartTabFolder)
		return (PartTabFolder) container;
	else
		return this;
}
public String toString() {
	String label = "disposed";//$NON-NLS-1$
	if((titleLabel != null) && (!titleLabel.isDisposed()))
		label = titleLabel.getText();
	
	return getClass().getName() + "@" + Integer.toHexString(hashCode()) + //$NON-NLS-1$
	"(" + label + ")";//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * @see ViewActionBars
 */
public void updateActionBars() {
	if (isvMenuMgr != null)
		isvMenuMgr.updateAll(false);
	if (viewToolBarMgr != null)
		viewToolBarMgr.update(false);
	if (isvToolBarMgr != null)
		isvToolBarMgr.update(false);
}
/**
 * Update the title attributes.
 */
public void updateTitles() {
	IViewReference ref = getViewReference();
	String text = ref.getTitle();
	if (text == null)
		text = "";//$NON-NLS-1$
	Image image = ref.getTitleImage();
	// only update and relayout if text or image has changed
	if (!text.equals(titleLabel.getText()) || image != titleLabel.getImage()) {
		titleLabel.setText(text);
		titleLabel.setImage(image);
		((Composite) getControl()).layout();
	}
	titleLabel.setToolTipText(ref.getTitleToolTip());
	// XXX: Workaround for 1GCGA89: SWT:ALL - CLabel tool tip does not always update properly
	titleLabel.update();

	// notify the page that this view's title has changed
	// in case it needs to update its fast view button
	page.updateTitle(ref);
}
}
