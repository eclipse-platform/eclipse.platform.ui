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
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.dnd.AbstractDragSource;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.util.Util;
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
	private CLabel status;

	private boolean fast = false;
	private boolean showFocus = false;
	// toolbars can be locked (i.e. part of the view form and 
	// positioned inside it, or not locked (embedded in a floating
	// toolbar that is not part of the view form
	private boolean locked = true;

	private ToolBar viewToolBar;
	private ToolBarManager viewToolBarMgr;
	ToolBar isvToolBar;
	private ToolBarManager isvToolBarMgr;
	private MenuManager isvMenuMgr;
	private ToolItem pullDownButton;
	boolean hasFocus;

	//private ToolItem lockToolBarButton;
	
	//private ToolbarFloatingWindow floatingWindow;
	
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
			toolBarResized(toolBar, newCount);
			
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
			super("View Local Menu"); //$NON-NLS-1$
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
			
/*			// if we have a toolbar then we want to support floating it
			if (isvToolBarMgr != null) {
				// create the button to allow for a floating toolbar
				lockToolBarButton = new ToolItem(toolbar, SWT.CHECK, index++);
				lockToolBarButton.setSelection(locked);
				Image hoverImage =
					WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW_HOVER);
				lockToolBarButton.setDisabledImage(hoverImage);
				// PR#1GE56QT - Avoid creation of unnecessary image.
				lockToolBarButton.setImage(hoverImage);
				//				pullDownButton.setHotImage(hoverImage);
				lockToolBarButton.setToolTipText("Lock in View"); 
				lockToolBarButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (lockToolBarButton.getSelection() == locked)
							return;
						locked = lockToolBarButton.getSelection();
						recreateToolbars();
					}
				});
			}*/
			
			if (showMenuButton) {
				pullDownButton = new ToolItem(toolbar, SWT.PUSH, index++);
				//				Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU);
				Image hoverImage =
					WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER);
				pullDownButton.setDisabledImage(hoverImage); // TODO: comment this out?
				// PR#1GE56QT - Avoid creation of unnecessary image.
				pullDownButton.setImage(hoverImage);
				//				pullDownButton.setHotImage(hoverImage);
				pullDownButton.setToolTipText(WorkbenchMessages.getString("Menu")); //$NON-NLS-1$
				pullDownButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						showViewMenu();
					}
				});
			} else {
				// clear out the button if we don't need it anymore
				pullDownButton = null;
			}

			// check the current internal fast view state
//			if (fast) {
//				ToolItem dockButton = new ToolItem(toolbar, SWT.CHECK, index++);
//				dockButton.setSelection(true);
//				//				Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW);
//				Image hoverImage =
//					WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW_HOVER);
//				dockButton.setDisabledImage(hoverImage);
//				// PR#1GE56QT - Avoid creation of unnecessary image.
//				dockButton.setImage(hoverImage);
//				//				dockButton.setHotImage(hoverImage);
//				dockButton.setToolTipText(WorkbenchMessages.getString("ViewPane.pin")); //$NON-NLS-1$
//				dockButton.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						doDock();
//					}
//				});
//
//				ToolItem minimizeButton = new ToolItem(toolbar, SWT.PUSH, index++);
//				//				img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW);
//				hoverImage =
//					WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_HOVER);
//				minimizeButton.setDisabledImage(hoverImage);
//				// PR#1GE56QT - Avoid creation of unnecessary image.
//				minimizeButton.setImage(hoverImage);
//				//				minimizeButton.setHotImage(img);
//				minimizeButton.setToolTipText(WorkbenchMessages.getString("ViewPane.minimize")); //$NON-NLS-1$
//				minimizeButton.addSelectionListener(new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						doMinimize();
//					}
//				});
//			}

//			TODO: RCP will need a close button when views have a titlebar and no tabs etc..
//			need to figure out how to query this combination			
/*			if (isCloseable()) {
				ToolItem closeButton = new ToolItem(toolbar, SWT.PUSH, index++);
				// Image img = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW);
				Image hoverImage =
					WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_HOVER);
				closeButton.setDisabledImage(hoverImage);
				// PR#1GE56QT - Avoid creation of unnecessary image.
				closeButton.setImage(hoverImage);
				// closeButton.setHotImage(hoverImage);
				closeButton.setToolTipText(WorkbenchMessages.getString("Close")); //$NON-NLS-1$
				closeButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						doHide();
					}
				});
			}*/
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
			// content can be null if view creation failed
			if (locked)
				control.setTabList(new Control[] { isvToolBar , viewToolBar });
		} else {
			if (locked)
				control.setTabList(new Control[] { viewToolBar, control.getContent()});
			else
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
		
		// the lock button in the receivers toolbar was selected so we know
		// we are active
/*		showFloatingWindow(true, true);*/
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

	/**
	 * Create a floating shell used to contain the toolbar when it is 
	 * not "locked" in the view.
	 * 
	 * @return a <code>ToolBarFloatingWindow</code> to contain the toolbar 
	 */
/*	private ToolbarFloatingWindow getFloatingWindow() {
		if (floatingWindow != null) 
			return floatingWindow;
		
		floatingWindow = new ToolbarFloatingWindow(getWorkbenchWindow().getShell(), this.getControl(), AssociatedWindow.TRACK_OUTER_TOP_RHS);
		return floatingWindow;
	}*/
	
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
		if (viewToolBar != null)
			return;

		status = new CLabel(control, SWT.LEFT);
		ColorSchemeService.setViewColors(status);

		updateTitles();

		// Listen to title changes.
		getPartReference().addPropertyListener(this);

		createToolBars();
	
	}
	
	private void toolBarResized(ToolBar toolBar, int newSize) {
		if (locked) {
			if (toolBar == viewToolBar) {
				((ViewForm)control).setTopRight(newSize == 0 ? null :viewToolBar);
			} else if (toolBar == isvToolBar) {
				((ViewForm)control).setTopCenter(newSize == 0 ? null : isvToolBar);
			}
		}
	}
	
	/**
	 * 
	 */
	private void createToolBars() {
		Composite parentControl = control;
		int barStyle = SWT.FLAT | SWT.WRAP;
/*		if (!locked) {
			parentControl = getFloatingWindow().getControl();
			barStyle = barStyle | SWT.VERTICAL;
		}*/
		
		// View toolbar
		viewToolBar = new ToolBar(parentControl, barStyle);
		if (locked) {
			//((ViewForm2)parentControl).setTopRight(viewToolBar);
			viewToolBar.addMouseListener(new MouseAdapter() {
				public void mouseDoubleClick(MouseEvent event) {
					if (viewToolBar.getItem(new Point(event.x, event.y)) == null)
						doZoom();
				}
			});
		} else {
			viewToolBar.setLayoutData(new GridData(GridData.FILL_BOTH));	
		}
		
		IContributionItem[] viewItems = null;
		if (viewToolBarMgr != null) {
			viewItems = viewToolBarMgr.getItems();
			viewToolBarMgr.dispose();
		}
		
				// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
		viewToolBarMgr = new PaneToolBarManager(viewToolBar);
		if (viewItems != null && viewItems.length != 0) {
			for (int i = 0; i < viewItems.length; i++) {
				viewToolBarMgr.add(viewItems[i]);
			}
		} else
			viewToolBarMgr.add(systemContribution);
		
		// ISV toolbar.
		//			// 1GD0ISU: ITPUI:ALL - Dbl click on view tool cause zoom
		isvToolBar = new ToolBar(parentControl, barStyle);
		
		
		
		if (locked) {
			//((ViewForm2)control).setTopCenter(isvToolBar);	
			isvToolBar.addMouseListener(new MouseAdapter(){
				public void mouseDoubleClick(MouseEvent event) {
					if (isvToolBar.getItem(new Point(event.x, event.y)) == null)
						doZoom();
				}
			});
		} else {
			isvToolBar.setLayoutData(new GridData(GridData.FILL_BOTH));
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
		if (viewToolBar != null)
		    ColorSchemeService.setViewColors(viewToolBar);
		ColorSchemeService.setViewColors(isvToolBar);
		ColorSchemeService.setViewColors(isvToolBar.getParent());
	}
	
	/**
     * 
     */
//    private void setToolBarColors() {
//        
//		setToolBarColors(
//		        getViewReference()
//		        .getPage()
//		        .getWorkbenchWindow()
//		        .getWorkbench()
//		        .getThemeManager()
//		        .getCurrentTheme()
//		        .getColorRegistry()
//		        .get(IWorkbenchPresentationConstants.BACKGROUND));
//    }
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
		if (viewToolBarMgr != null)
			viewToolBarMgr.dispose();
/*		if (floatingWindow != null) {
			floatingWindow.close();
		}*/
	}
	/**
	 * @see PartPane#doHide
	 */
	public void doHide() {
		getPage().hideView(getViewReference());
		//hideToolBarShell();
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
//	/**
//	 * Hide the fast view
//	 */
//	protected void doMinimize() {
//		getPage().toggleFastView(getViewReference());
//	}
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
		return viewToolBar;
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
		
/*		if (active) {
			setToolBarColors(WorkbenchColors.getActiveEditorGradientEnd());
		} else {
			setToolBarColors(WorkbenchColors.getActiveNoFocusEditorGradientEnd());
		}*/
/*		showFloatingWindow(active, false);
*/			
		if(getContainer() instanceof PartTabFolder){
			((PartTabFolder) getContainer()).setActive(active);
		}
	}

	/**
	 * @param color
	 */
//	private void setToolBarColors(Color color) {
//		if (viewToolBar != null)
//			viewToolBar.setBackground(color);
//		isvToolBar.setBackground(color);
//		isvToolBar.getParent().setBackground(color);
//	}
	
	/**
	 * Ensure the visible/invisible state of the floating window matches the 
	 * current locked/unlocked setting.
	 * 
	 * @param active <code>boolean</code> the view is currently the active view
	 * @param lockChanged <code>boolean</code> has the state of locked/unlocked just 
	 * 			been changed
	 */
/*	private void showFloatingWindow(boolean active, boolean lockChanged) {
		// if we have a locked toolbar and the toolbar has not just 
		// been locked then we have no floating window to worry about
		if (locked & !lockChanged)
			return;
		// first time opening the floating shell, set bounds
		if (lockChanged & !locked) {
			getFloatingWindow().initializeBounds();
			ViewForm2 vf = (ViewForm2)getControl();
			vf.setTopCenter(null);
			vf.setTopRight(null);
			vf.layout();			
		} else if (lockChanged && locked) {
			((ViewForm2)getControl()).layout();
		}
		// active, then open the window, else close
		if (active & !locked) {
			getFloatingWindow().initializeBounds();
			getFloatingWindow().open();
		} else if (floatingWindow != null) {
			getFloatingWindow().getShell().setVisible(false);
		}		
	}*/
	
	/**
	 * Indicate focus in part.
	 */
	public void showFocus(boolean inFocus) {
		//	showFocus = inFocus;
		//
		//	if (getContainer() instanceof PartTabFolder) {
		//		PartTabFolder tf = (PartTabFolder) getContainer();
		//		CTabFolder2 f = (CTabFolder2) tf.getControl();
		//		f.setBorderVisible(inFocus);
		//	}
		setActive(inFocus);

	}

	/**
	 * Shows the pane menu (system menu) for this pane.
	 */
	public void showPaneMenu() {
		// If this is a fast view, it may have been minimized. Do nothing in this case.
		if (isFastView() && (page.getActiveFastView() != getViewReference()))
			return;
		// TODO should position menu relative to tab, as is done
		// with editors.  Also need to take into account view titlebar if present
		Control ref = control.getContent() != null ? control.getContent() : control;
		Point pos = ref.toDisplay(ref.getLocation());
		showPaneMenu(ref, pos);
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
		if (isFastView()) {
			result.right = fastViewSash;
			return result;
		}
		
		ILayoutContainer container = getContainer();
		
		if (container == null) {
			return result;
		} 
		
		container.findSashes(this, result);
		return result;
	}
	/**
	 * Add the Fast View menu item to the view title menu.
	 */
	protected void addFastViewMenuItem(Menu parent, boolean isFastView) {
		if (isMoveable() && isCloseable()) {
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

//		if (isFastView) {
//			item = new MenuItem(parent, SWT.NONE);
//			item.setText(WorkbenchMessages.getString("ViewPane.minimizeView")); //$NON-NLS-1$
//			item.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					doMinimize();
//				}
//			});
//			item.setEnabled(true);
//		}
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
					 ((PartTabFolder) container).openTracker((PartTabFolder) container);
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
	public boolean hasViewMenu() {
		if (isvMenuMgr == null)
			return false;

		//If there is no pull down button there is no associated menu
		if (pullDownButton == null || pullDownButton.isDisposed())
			return false;

		return true;
	}

	/**
	 * Show the view menu for this window.
	 */
	public void showViewMenu() {

		if (!hasViewMenu())
			return;

		// If this is a fast view, it may have been minimized. Do nothing in this case.
		if (isFastView() && (page.getActiveFastView() != getViewReference()))
			return;

		Menu aMenu = isvMenuMgr.createContextMenu(getControl());
		Rectangle bounds = pullDownButton.getBounds();
		Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
		topLeft = viewToolBar.toDisplay(topLeft);
		aMenu.setLocation(topLeft.x, topLeft.y);
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
		
		if (status != null && !status.isDisposed()) {
			status.setBackground(status.getParent().getBackground());
			boolean changed = false;
			String text = ref.getTitle();
			
			if (text != null) {
				int i = text.indexOf('(');
				int j = text.lastIndexOf(')');
				
				if (i > 0 && j > 0 && j > i)
					text = text.substring(i + 1, j).trim();
				else 
					text = null;
			}
			
			if (!Util.equals(text, status.getText())) {
				status.setText(text);
				changed = true;
			}

			String toolTipText = ref.getTitleToolTip();
			
			if (!Util.equals(toolTipText, status.getToolTipText())) {
				status.setToolTipText(toolTipText);
				changed = true;
			}

			//((ViewForm2) control).setStatus(text != null ? status : null);
			((ViewForm) control).setTopLeft(text != null ? status : null);
			
			if (changed) {
				status.update();
				
				// notify the page that this view's title has changed
				// in case it needs to update its fast view button
				page.updateTitle(ref);
			}
		}

		/*
		IViewReference ref = getViewReference();
		if (titleLabel != null && !titleLabel.isDisposed()) {
			boolean changed = false;

			// only update if text or image has changed 
			String text = ref.getTitle();
			if (text == null)
				text = ""; //$NON-NLS-1$
			if (!text.equals(titleLabel.getText())) {
				titleLabel.setText(text);
				changed = true;
			}
			Image image = ref.getTitleImage();
			if (image != titleLabel.getImage()) {
				titleLabel.setImage(image);
				changed = true;
			}
			// only relayout if text or image has changed
			if (changed) {
				((Composite) getControl()).layout();
			}

			String tooltip = ref.getTitleToolTip();
			if (!(tooltip == null
				? titleLabel.getToolTipText() == null
				: tooltip.equals(titleLabel.getToolTipText()))) {
				titleLabel.setToolTipText(ref.getTitleToolTip());
				changed = true;
			}

			if (changed) {
				// XXX: Workaround for 1GCGA89: SWT:ALL - CLabel tool tip does not always update properly
				titleLabel.update();

				// notify the page that this view's title has changed
				// in case it needs to update its fast view button
				page.updateTitle(ref);
			}
		}
		*/
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#setImage(org.eclipse.swt.widgets.TabItem, org.eclipse.swt.graphics.Image)
	 */
	void setImage(CTabItem item, Image image) {
		titleLabel.setImage(image);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#addCloseMenuItem(org.eclipse.swt.widgets.Menu)
	 */
	protected void addCloseMenuItem(Menu menu) {
		if(isCloseable())
			super.addCloseMenuItem(menu);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#addMoveMenuItem(org.eclipse.swt.widgets.Menu)
	 */
	protected void addMoveMenuItem(Menu menu) {
		if(isMoveable())
			super.addMoveMenuItem(menu);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartPane#addPinEditorItem(org.eclipse.swt.widgets.Menu)
	 */
	protected void addPinEditorItem(Menu parent) {
		if(isMoveable() && isCloseable())
			super.addPinEditorItem(parent);
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
}