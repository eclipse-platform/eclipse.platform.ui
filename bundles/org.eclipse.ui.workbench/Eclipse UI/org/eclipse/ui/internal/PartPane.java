package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
**********************************************************************/

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.part.WorkbenchPart;


/**
 * Provides the common behavior for both views
 * and editor panes.
 */
public abstract class PartPane extends LayoutPart
	implements Listener
{
	private boolean isZoomed = false;
	private MenuManager paneMenuManager;
	protected IWorkbenchPartReference partReference;
	protected WorkbenchPage page;
	protected ViewForm control;
	
	public static class Sashes {
		public Sash left;
		public Sash right;
		public Sash top;
		public Sash bottom;
	}
	private class PaneContribution extends ContributionItem {
		public boolean isDynamic() {
			return true;
		}
		public void fill(Menu menu, int index) {
			MenuItem item; 
		
			// Get various view states.
			final boolean isFastView = (page.getActiveFastView() == partReference);
			final boolean isZoomed = page.isZoomed();
			boolean canZoom = (getWindow() instanceof IWorkbenchWindow);
		
			// add restore item
			item = new MenuItem(menu, SWT.NONE);
			item.setText(WorkbenchMessages.getString("PartPane.restore")); //$NON-NLS-1$
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (isZoomed)
						doZoom();
				}
			});
			item.setEnabled(isZoomed);
			
			//Add move menu
			item = new MenuItem(menu, SWT.CASCADE);
			item.setText(WorkbenchMessages.getString("PartPane.move")); //$NON-NLS-1$
			Menu moveMenu = new Menu(menu);
			item.setMenu(moveMenu);
			addMoveItems(moveMenu);
			
			//Add size menu
			item = new MenuItem(menu, SWT.CASCADE);
			item.setText(WorkbenchMessages.getString("PartPane.size")); //$NON-NLS-1$
			Menu sizeMenu = new Menu(menu);
			item.setMenu(sizeMenu);
			addSizeItems(sizeMenu);
			
			addFastViewMenuItem(menu,isFastView);
		
			// add maximize item
			item = new MenuItem(menu, SWT.NONE);
			item.setText(WorkbenchMessages.getString("PartPane.maximize")); //$NON-NLS-1$
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					doZoom();
				}
			});
			item.setEnabled(!isZoomed && canZoom);
		
			addPinEditorItem(menu);
			
			new MenuItem(menu, SWT.SEPARATOR);
			
			// add close item
			item = new MenuItem(menu, SWT.NONE);
			item.setText(WorkbenchMessages.getString("PartPane.close")); //$NON-NLS-1$
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					doHide();
				}
			});			
		}
	}
	
/**
 * Construct a pane for a part.
 */
public PartPane(IWorkbenchPartReference partReference, WorkbenchPage workbenchPage) {
	super(partReference.getId());
	this.partReference = partReference;
	this.page = workbenchPage;
	((WorkbenchPartReference)partReference).setPane(this);
}
/**
 * Factory method for creating the SWT Control hierarchy for this Pane's child.
 */
protected void createChildControl() {
	final IWorkbenchPart part[] = new IWorkbenchPart[]{partReference.getPart(false)};
	if(part[0] == null)
		return;

	if(control == null || control.getContent() != null)
		return;
	
	final Composite content = new Composite(control, SWT.NONE);
	content.setLayout(new FillLayout());
	
	String error = WorkbenchMessages.format("PartPane.unableToCreate", new Object[] {partReference.getTitle()}); //$NON-NLS-1$
	Platform.run(new SafeRunnable(error) {
		public void run() {
			try {
				UIStats.start(UIStats.CREATE_PART_CONTROL,id);
				part[0].createPartControl(content);
			} finally {
				UIStats.end(UIStats.CREATE_PART_CONTROL,id);
			}
		}
		public void handleException(Throwable e) {
			// Log error.
			Workbench wb = (Workbench)WorkbenchPlugin.getDefault().getWorkbench();
			if (!wb.isStarting())
				super.handleException(e);

			// Dispose old part.
			Control children[] = content.getChildren();
			for (int i = 0; i < children.length; i++){
				children[i].dispose();
			}
			
			// Create new part.
			IWorkbenchPart newPart = createErrorPart((WorkbenchPart)part[0]);
			part[0].getSite().setSelectionProvider(null);
			newPart.createPartControl(content);
			((WorkbenchPartReference)partReference).setPart(newPart);
			part[0] = newPart;
		}
	});
	control.setContent(content);
	page.addPart(partReference);
	page.firePartOpened(part[0]);	
}
/**
 * 
 */
public void createControl(Composite parent) {
	if (getControl() != null)
		return;

	// Create view form.	
	control = new ViewForm(parent, getStyle());
	control.marginWidth = 0;
	control.marginHeight = 0;

	// Create a title bar.
	createTitleBar();

	// Create content.
	createChildControl();
	
	// When the pane or any child gains focus, notify the workbench.
	control.addListener(SWT.Activate, this);
}

protected abstract WorkbenchPart createErrorPart(WorkbenchPart oldPart);
/**
 * Create a title bar for the pane if required.
 */
protected abstract void createTitleBar();
/**
 * @private
 */
public void dispose() {
	super.dispose();

	if ((control != null) && (!control.isDisposed())) {
		control.removeListener(SWT.Activate, this);
		control.dispose();
		control = null;
	}
	if ((paneMenuManager != null)) {
		paneMenuManager.dispose();	
		paneMenuManager = null;
	}
}
/**
 * User has requested to close the pane.
 * Take appropriate action depending on type.
 */
abstract public void doHide();
/**
 * Zooms in on the part contained in this pane.
 */
protected void doZoom() {
	if (getWindow() instanceof IWorkbenchWindow)
		page.toggleZoom(partReference);
}
/**
 * Gets the presentation bounds.
 */
public Rectangle getBounds() {
	return getControl().getBounds();
}
/**
 * Get the control.
 */
public Control getControl() {
	return control;
}

/*
 * @see LayoutPart#getMinimumHeight()
 */
public int getMinimumHeight() {
	if (control == null || control.isDisposed())
		return super.getMinimumHeight();
	
	// don't assume every future part will have top controls
	boolean top = false;
	
	/* compute title bar height; this should be done by computeTrim 
	 * to correctly handle seperate top center.
	 */
	int leftHeight = 0;
	if (control.getTopLeft() != null && !control.getTopLeft().isDisposed()) {
		leftHeight = control.getTopLeft().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		top = true;
	}
	int centerHeight = 0;
	if (control.getTopCenter() != null && !control.getTopCenter().isDisposed()) {
		centerHeight = control.getTopCenter().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		top=true;
	}
	int rightHeight = 0;
	if (control.getTopRight() != null && !control.getTopRight().isDisposed()) {
		rightHeight = control.getTopRight().computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		top = true;
	}
	
	int topHeight = Math.max(leftHeight, Math.max(centerHeight, rightHeight));
	
	// account for the borders
	topHeight = control.computeTrim(0, 0, 0, topHeight).height;
	
	/* add +1 for highlight line. ViewForm adds this *inside* client area
	 * even though it's arguably an inset; see ViewForm.layout for details.
	 */
	if (top) {
		topHeight += 1;
	}
	
	return topHeight;
}

/**
 * Returns the top level SWT Canvas of this Pane. 
 */
protected ViewForm getPane() {
	return control;
}
/**
 * Answer the part child.
 */
public IWorkbenchPartReference getPartReference() {
	return partReference;
}
/**
 * Answer the SWT widget style.
 */
int getStyle() {
	if (getContainer() != null && !getContainer().allowsBorder())
		return SWT.NONE;
	else
		return SWT.BORDER;
}
/**
 * Get the view form.
 */
protected ViewForm getViewForm() {
	return control;
}
/**
 * @see Listener
 */
public void handleEvent(Event event) {
	if (event.type == SWT.Activate)
		requestActivation();
}
/**
 * Return whether the pane is zoomed or not
 */
public boolean isZoomed() {
	return isZoomed;
}
/**
 * Move the control over another one.
 */
public void moveAbove(Control refControl) {
	if (getControl() != null)
		getControl().moveAbove(refControl);
}
/**
 * Notify the workbook page that the part pane has
 * been activated by the user.
 */
protected void requestActivation() {
	this.page.requestActivation(partReference.getPart(true));
}
/**
 * Sets the parent for this part.
 */
public void setContainer(ILayoutContainer container) {
	super.setContainer(container);
	if (control != null && container != null)
		control.setBorderVisible(container.allowsBorder());
}
/**
 * Shows the receiver if <code>visible</code> is true otherwise hide it.
 */
public void setVisible(boolean makeVisible) {
	super.setVisible(makeVisible);
	if(makeVisible) //Make sure that the part is restored.
		partReference.getPart(true);
}
/**
 * Sets focus to this part.
 */
public void setFocus() {
	requestActivation();
	IWorkbenchPart part = partReference.getPart(true);
	if (part != null) {
		part.setFocus();
	}
}
/**
 * Sets the workbench page of the view. 
 */
public void setWorkbenchPage(WorkbenchPage workbenchPage) {
	this.page = workbenchPage;
}
/**
 * Set whether the pane is zoomed or not
 */
public void setZoomed(boolean isZoomed) {
	this.isZoomed = isZoomed;
}
/**
 * Informs the pane that it's window shell has
 * been activated.
 */
/* package */ abstract void shellActivated();
/**
 * Informs the pane that it's window shell has
 * been deactivated.
 */
/* package */ abstract void shellDeactivated();
/**
 * Indicate focus in part.
 */
public abstract void showFocus(boolean inFocus);
/**
 * @see IPartDropTarget::targetPartFor
 */
public LayoutPart targetPartFor(LayoutPart dragSource) {
	return this;
}

/**
 * Show a title label menu for this pane.
 */
public abstract void showPaneMenu();
/**
 * Show the context menu for this part.
 */
public abstract void showViewMenu();
/**
 * Show a title label menu for this pane.
 */
final protected void showPaneMenu(Control parent, Point point) {
	if(paneMenuManager == null) {
		paneMenuManager = new MenuManager();
		paneMenuManager.add(new PaneContribution());			
	}
	Menu aMenu = paneMenuManager.createContextMenu(parent);
	// open menu    
	point = parent.toDisplay(point);
	aMenu.setLocation(point.x, point.y);
	aMenu.setVisible(true);
}
/**
 * Return the sashes around this part.
 */
protected abstract Sashes findSashes();
/**
 * Enable the user to resize this part using
 * the keyboard to move the specified sash
 */
protected void moveSash(final Sash sash) {
	final KeyListener listener = new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
			if (e.character == SWT.ESC || e.character == '\r') {
				IWorkbenchPart part = partReference.getPart(true);
				if(part != null)
					part.setFocus();
			}
		}
	};
	sash.addFocusListener(new FocusAdapter() {
		public void focusGained(FocusEvent e) {
			sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			sash.addKeyListener(listener);
		}
		public void focusLost(FocusEvent e) {
			sash.setBackground(null);
			sash.removeKeyListener(listener);
		}
	});
	sash.setFocus();
}
/**
 * Add a menu item to the Size Menu
 */
protected void addSizeItem(Menu sizeMenu, String labelKey,final Sash sash) {
	MenuItem item = new MenuItem(sizeMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString(labelKey)); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			moveSash(sash);
		}
	});
	item.setEnabled(!isZoomed() && sash != null);
}
/**
 * Returns the workbench page of this pane.
 */
public WorkbenchPage getPage() {
	return page;
}
/**
 * Add the Left,Right,Up,Botton menu items to the Size menu.
 */
protected void addSizeItems(Menu sizeMenu) {
	Sashes sashes = findSashes();
	addSizeItem(sizeMenu,"PartPane.sizeLeft",sashes.left); //$NON-NLS-1$
	addSizeItem(sizeMenu,"PartPane.sizeRight",sashes.right); //$NON-NLS-1$
	addSizeItem(sizeMenu,"PartPane.sizeTop",sashes.top); //$NON-NLS-1$
	addSizeItem(sizeMenu,"PartPane.sizeBottom",sashes.bottom); //$NON-NLS-1$
}
/**
 * Add the pin menu item on the editor system menu
 */
protected void addPinEditorItem(Menu parent) {}
/**
 * Add the move items to the Move menu.
 */
protected void addMoveItems(Menu parent) {}
/**
 * Add the Fast View menu item to the part title menu.
 */
protected void addFastViewMenuItem(Menu parent,boolean isFastView) {}
/**
 * Pin this part.
 */
protected void doDock() {}
}
