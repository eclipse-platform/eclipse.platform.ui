package org.eclipse.ui.internal;

/******************************************************************************* 
 * Copyright (c) 2000, 2004 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Cagatay Kavukcuoglu <cagatayk@acm.org>
 *      - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.dnd.AbstractDragSource;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.presentations.IPresentablePart;

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
public class ViewPane extends PartPane implements IPropertyListener {
	private PresentableViewPart presentableAdapter = new PresentableViewPart(this);
	
	private CLabel titleLabel;
	//private CLabel status;
	private boolean busy = false;

	private boolean fast = false;
	private boolean showFocus = false;
	// toolbars can be locked (i.e. part of the view form and 
	// positioned inside it, or not locked (embedded in a floating
	// toolbar that is not part of the view form
	private boolean locked = true;

	ToolBar isvToolBar;
	private ToolBarManager isvToolBarMgr;
	private MenuManager isvMenuMgr;
	boolean hasFocus;
	
	/**
	 * Indicates whether a toolbar button is shown for the view local menu.
	 */
	private boolean hadViewMenu = false;

	/**
	 * Toolbar manager for the ISV toolbar.
	 */
	class PaneToolBarManager extends ToolBarManager {
		public PaneToolBarManager(ToolBar paneToolBar) {
			super(paneToolBar);
		}

		protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
			toolBarResized(toolBar, newCount);
			
			toolBar.layout();
		}
	}

	/**
	 * Menu manager for view local menu.
	 */
	class PaneMenuManager extends MenuManager {
		public PaneMenuManager() {
			super("View Local Menu"); //$NON-NLS-1$
		}
		protected void update(boolean force, boolean recursive) {
			super.update(force, recursive);
			
			boolean hasMenu = !isEmpty();
			if (hasMenu != hadViewMenu) {
				hadViewMenu = hasMenu;
				presentableAdapter.firePropertyChange(IPresentablePart.PROP_PANE_MENU);
			}
		}
	}

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

		setTabList();
		
		DragUtil.addDragSource(control, new AbstractDragSource() {
			
			public Object getDraggedItem(Point position) {
				return ViewPane.this;
			}

			public void dragStarted(Object draggedItem) {
				getPage().getActivePerspective().setActiveFastView(null, 0);
			}

			public Rectangle getDragRectangle(Object draggedItem) {
				return DragUtil.getDisplayBounds(control);
			}
			
		});
	}

	/**
	 * 
	 */
	private void setTabList() {
		// Only include the ISV toolbar and the content in the tab list.
		// All actions on the System toolbar should be accessible on the pane menu.
		if (control.getContent() == null) {
		} else {
			control.setTabList(new Control[] {control.getContent()});
		}
	}
	
	protected void createChildControl() {
		final IWorkbenchPart part[] = new IWorkbenchPart[] { partReference.getPart(false)};
		if (part[0] == null)
			return;

		if (control == null || control.getContent() != null)
			return;

		super.createChildControl();

		Platform.run(new SafeRunnable() {
			public void run() {
				// Install the part's tools and menu
				ViewActionBuilder builder = new ViewActionBuilder();
				IViewPart part = (IViewPart) getViewReference().getPart(true);
				if (part != null) {
					builder.readActionExtensions(part);
					ActionDescriptor[] actionDescriptors = builder.getExtendedActions();
					KeyBindingService keyBindingService =
						(KeyBindingService) part.getSite().getKeyBindingService();

					if (actionDescriptors != null) {
						for (int i = 0; i < actionDescriptors.length; i++) {
							ActionDescriptor actionDescriptor = actionDescriptors[i];

							if (actionDescriptor != null) {
								IAction action = actionDescriptors[i].getAction();

								if (action != null && action.getActionDefinitionId() != null)
									keyBindingService.registerAction(action);
							}
						}
					}
				}
				updateActionBars();
			}
			public void handleException(Throwable e) {
				//Just have it logged.
			}
		});
	}

	private void recreateToolbars() {
		// create new toolbars based on the locked vs !locked state
		createToolBars();
		// create new toolbars
		updateActionBars();
		
	}	
	
	protected WorkbenchPart createErrorPart(WorkbenchPart oldPart) {
		class ErrorViewPart extends ViewPart {
			private Text text;
			public void createPartControl(Composite parent) {
				text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
				text.setForeground(JFaceColors.getErrorText(text.getDisplay()));
				text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				text.setText(WorkbenchMessages.getString("ViewPane.errorMessage")); //$NON-NLS-1$
			}
			public void setFocus() {
				if (text != null)
					text.setFocus();
			}
			public void setSite(IWorkbenchPartSite site) {
				super.setSite(site);
			}
			public void setTitle(String title) {
				super.setTitle(title);
			}
		}
		ErrorViewPart newPart = new ErrorViewPart();
		PartSite site = (PartSite) oldPart.getSite();
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
		return isMoveable() && !overImage(p.x) && !isZoomed();
	}
	
	/*
	 * Return true if <code>x</code> is over the label image.
	 */
	private boolean overImage(int x) {
		if (titleLabel.getImage() == null) {
			return false;
		} else {
			return x < titleLabel.getImage().getBounds().width;
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

		updateTitles();

		// Listen to title changes.
		getPartReference().addPropertyListener(this);

		createToolBars();
	
	}
	
	private void toolBarResized(ToolBar toolBar, int newSize) {
		if (isvToolBar != null) {
			Control ctrl = getControl();
			
			boolean visible = ctrl != null && ctrl.isVisible() && toolbarIsVisible();
			
			isvToolBar.setVisible(visible);
		}
		
		presentableAdapter.firePropertyChange(IPresentablePart.PROP_TOOLBAR);
	}
	
	/**
	 * 
	 */
	private void createToolBars() {
		Composite parentControl = control;
		int barStyle = SWT.FLAT | SWT.WRAP;
		
		// ISV toolbar.
		//			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
		isvToolBar = new ToolBar(parentControl.getParent(), barStyle);
		
		
		
		if (locked) {
			//((ViewForm2)control).setTopCenter(isvToolBar);	
			isvToolBar.addMouseListener(new MouseAdapter(){
				public void mouseDoubleClick(MouseEvent event) {
					if (isvToolBar.getItem(new Point(event.x, event.y)) == null)
						doZoom();
				}
			});
		} else {
			//isvToolBar.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		IContributionItem[] isvItems = null;
		if (isvToolBarMgr != null) {
			isvItems = isvToolBarMgr.getItems();
			isvToolBarMgr.dispose();
		}
		isvToolBarMgr = new PaneToolBarManager(isvToolBar);
		if (isvItems != null) {
			for (int i = 0; i < isvItems.length; i++) {
				isvToolBarMgr.add(isvItems[i]);
			}
		}
		setTabList();
        // only do this once for the non-floating toolbar case.  Otherwise update colors
		// whenever updating the tab colors
		ColorSchemeService.setViewColors(parentControl);
	}
	
    public void dispose() {
		super.dispose();

		/* Bug 42684.  The ViewPane instance has been disposed, but an attempt is
		 * then made to remove focus from it.  This happens because the ViewPane is
		 * still viewed as the active part.  In general, when disposed, the control
		 * containing the titleLabel will also disappear (disposing of the 
		 * titleLabel).  As a result, the reference to titleLabel should be dropped. 
		 */
		if (isvMenuMgr != null)
			isvMenuMgr.dispose();
		if (isvToolBarMgr != null)
			isvToolBarMgr.dispose();
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
	public void doMakeFast() {
	    FastViewBar fastViewBar = ((WorkbenchWindow)getPage().getWorkbenchWindow()).getFastViewBar();
	    if (fastViewBar == null) {
	        return;
	    }
		Control control = getControl();
		Shell shell = control.getShell();
		
		RectangleAnimation animation = new RectangleAnimation(shell,  
				DragUtil.getDisplayBounds(control), 
				fastViewBar.getLocationOfNextIcon(), 
				250);
		
		animation.schedule();
		
		getPage().addFastView(getViewReference());
	}

	/**
	 * Pin the view.
	 */
	protected void doDock() {
		getPage().removeFastView(getViewReference());
	}

	/**
	 * Returns the drag control.
	 */
	public Control getDragHandle() {
		return control;
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
			Control f = tf.getControl();
			return new Control[] { f, c };
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
		return (IViewReference) getPartReference();
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
	}

	/* (non-Javadoc)
	 * Method declared on PartPane.
	 */
	/* package */
	void shellActivated() {
		//	drawGradient();
	}

	/* (non-Javadoc)
	 * Method declared on PartPane.
	 */
	/* package */
	void shellDeactivated() {
		//hideToolBarShell();
		//	drawGradient();
	}
	
	/**
	 * Set the active border.
	 * @param active
	 */
	void setActive(boolean active){
		hasFocus = active;
		
		if(getContainer() instanceof PartTabFolder){
			((PartTabFolder) getContainer()).setActive(active);
		}
	}
	
	/**
	 * Indicate focus in part.
	 */
	public void showFocus(boolean inFocus) {
		setActive(inFocus);

	}

	/**
	 * Shows the pane menu (system menu) for this pane.
	 */
	public void showPaneMenu() {
		ILayoutContainer container = getContainer();
		
		if (container instanceof PartTabFolder) {
			PartTabFolder folder = (PartTabFolder) container;
			
			folder.showSystemMenu();
		}		
	}
	/**
	 * Return true if this view is a fast view.
	 */
	private boolean isFastView() {
		return page.isFastView(getViewReference());
	}
	/**
	 * Return true if this view can be closed or is fixed.
	 */
	boolean isCloseable() {
		return !page.isFixedView(getViewReference());
	}
	/**
	 * Return true if the view may be moved.
	 */
	boolean isMoveable() {
		return !page.isFixedLayout();
	}
	/**
	 * Finds and return the sashes around this part.
	 */
	protected Sashes findSashes() {
		Sashes result = new Sashes();
		
		ILayoutContainer container = getContainer();
		
		if (container == null) {
			return result;
		} 
		
		container.findSashes(this, result);
		return result;
	}

	/**
	 * Return if there should be a view menu at all.
	 * There is no view menu if there is no menu manager,
	 * no pull down button or if the receiver is an
	 * inactive fast view.
	 */
	public boolean hasViewMenu() {
		
		if (isvMenuMgr != null) {
			return !isvMenuMgr.isEmpty();
		}
		
		return false;		
	}

	/**
	 * Show the view menu for this window.
	 */
	public void showViewMenu() {
		ILayoutContainer container = getContainer();
		
		if (container instanceof PartTabFolder) {
			PartTabFolder folder = (PartTabFolder) container;
			
			folder.showPaneMenu();
		}		
	}
	
	public void showViewMenu(Point location) {
		if (!hasViewMenu())
			return;
	
		// If this is a fast view, it may have been minimized. Do nothing in this case.
		if (isFastView() && (page.getActiveFastView() != getViewReference()))
			return;
	
		Menu aMenu = isvMenuMgr.createContextMenu(getControl().getParent());
		aMenu.setLocation(location.x, location.y);
		aMenu.setVisible(true);
	}
	
	public String toString() {

		return getClass().getName() + "@" + Integer.toHexString(hashCode()); //$NON-NLS-1$
	}
	/**
	 * @see ViewActionBars
	 */
	public void updateActionBars() {
		if (isvMenuMgr != null)
			isvMenuMgr.updateAll(false);
		if (isvToolBarMgr != null)
			isvToolBarMgr.update(false);

	}
	/**
	 * Update the title attributes.
	 */
	public void updateTitles() {
		presentableAdapter.firePropertyChange(IPresentablePart.PROP_TITLE);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#setImage(org.eclipse.swt.widgets.TabItem, org.eclipse.swt.graphics.Image)
	 */
	void setImage(CTabItem item, Image image) {
		titleLabel.setImage(image);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#addSizeMenuItem(org.eclipse.swt.widgets.Menu)
	 */
	public void addSizeMenuItem(Menu menu) {
		if(isMoveable())
			super.addSizeMenuItem(menu);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#doZoom()
	 */
	protected void doZoom() {
		if (isMoveable())
			super.doZoom();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#setContainer(org.eclipse.ui.internal.ILayoutContainer)
	 */
	public void setContainer(ILayoutContainer container) {
		ILayoutContainer oldContainer = getContainer();
		if (hasFocus) {
			if (oldContainer != null && oldContainer instanceof PartTabFolder) {
				((PartTabFolder)oldContainer).setActive(false);
			}
			
			if (container != null && container instanceof PartTabFolder) {
				((PartTabFolder)container).setActive(true);
			}
		}

		super.setContainer(container);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#getPresentablePart()
	 */
	public IPresentablePart getPresentablePart() {
		return presentableAdapter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#reparent(org.eclipse.swt.widgets.Composite)
	 */
	public void reparent(Composite newParent) {
		super.reparent(newParent);
		
		if (isvToolBar != null) {
			isvToolBar.setParent(newParent);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#moveAbove(org.eclipse.swt.widgets.Control)
	 */
	public void moveAbove(Control refControl) {
		super.moveAbove(refControl);
		
		isvToolBar.moveAbove(control);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#setVisible(boolean)
	 */
	public void setVisible(boolean makeVisible) {
		super.setVisible(makeVisible);
		
		if (isvToolBar != null) {
			isvToolBar.setVisible(makeVisible && toolbarIsVisible());
		}
	}

	protected boolean toolbarIsVisible() {
		ToolBarManager toolbarManager = getToolBarManager();
		
		if (toolbarManager == null) {
			return false;
		}
		
		ToolBar control = toolbarManager.getControl();
		
		if (control == null || control.isDisposed() ) {
			return false;
		}
		
		return control.getItemCount() > 0;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#setBusy(boolean)
	 */
	public void setBusy(boolean isBusy) {
		if (isBusy != busy) {			
			busy = isBusy;
			presentableAdapter.firePropertyChange(IPresentablePart.PROP_BUSY);
		}
	}

	/**
	 * Return the busy state of the receiver.
	 * @return boolean
	 */
	public boolean isBusy() {
		return busy;
	}
	
}