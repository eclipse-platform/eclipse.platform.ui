package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.part.*;


/**
 * Provides the common behavior for both views
 * and editor panes.
 */
public abstract class PartPane extends LayoutPart
	implements Listener
{
	private boolean isZoomed = false;
	protected IWorkbenchPart part;
	protected WorkbenchPage page;
	protected ViewForm control;
	protected MouseListener mouseListener = new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			requestActivation();
		}
	};
/**
 * Construct a pane for a part.
 */
public PartPane(IWorkbenchPart part, WorkbenchPage workbenchPage) {
	super(part.getSite().getId());
	this.part = part;
	this.page = workbenchPage;
	((PartSite)part.getSite()).setPane(this);
}
/**
 * Factory method for creating the SWT Control hierarchy for this Pane's child.
 */
protected void createChildControl(final Composite parent) {
	String error = WorkbenchMessages.format("PartPane.unableToCreate", new Object[] {part.getTitle()}); //$NON-NLS-1$
	Platform.run(new SafeRunnableAdapter(error) {
		public void run() {	
			part.createPartControl(parent);
		}
		public void handleException(Throwable e) {
			// Log error.
			Workbench wb = (Workbench)WorkbenchPlugin.getDefault().getWorkbench();
			if (!wb.isStarting())
				super.handleException(e);

			// Dispose old part.
			Control children[] = parent.getChildren();
			for (int i = 0; i < children.length; i++){
				children[i].dispose();
			}
			
			// Create new part.
			IWorkbenchPart newPart = createErrorPart((WorkbenchPart)part);
			part.getSite().setSelectionProvider(null);
			newPart.createPartControl(parent);
			part = newPart;
		}
	});
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
	Composite content = new Composite(control, SWT.NONE);
	content.setLayout(new FillLayout());
	createChildControl(content);
	control.setContent(content);
	
	// When the pane or any child gains focus, notify the workbench.
	control.addListener(SWT.Activate, this);
	hookFocus(control);
	hookFocus(content);
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
		page.toggleZoom(getPart());
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
/**
 * Returns the top level SWT Canvas of this Pane. 
 */
protected ViewForm getPane() {
	return control;
}
/**
 * Answer the part child.
 */
public IWorkbenchPart getPart() {
	return part;
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
 * Hook focus on a control.
 */
public void hookFocus(Control ctrl) {
	ctrl.addMouseListener(mouseListener);
}
/**
 * See LayoutPart
 */
public boolean isDragAllowed(Point p) {
	if (isZoomed())
		return false;
	else
		return true;
}
/**
 * Returns true if this part is visible.  A part is visible if it has a control.
 */
public boolean isVisible() {
	return (getControl() != null);
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
	this.page.requestActivation(part);
}
/**
 * Sets the parent for this part.
 */
public void setContainer(ILayoutContainer container) {
	super.setContainer(container);
	if (control != null) {
		if (container != null && !container.allowsBorder())
			control.setBorderVisible(false);
		else
			control.setBorderVisible(true);
	}
}
/**
 * Sets focus to this part.
 */
public void setFocus() {
	requestActivation();
	part.setFocus();
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
 * Show a title label menu for this pane.
 */
protected void showPaneMenu(Control parent,Point point,boolean isFastView) {
	Menu aMenu = new Menu(parent);
	MenuItem item; 

	// Get various view states.
	final boolean isZoomed = ((WorkbenchPage)getPart().getSite().getPage()).isZoomed();
	boolean canZoom = (getWindow() instanceof IWorkbenchWindow);

	// add restore item
	item = new MenuItem(aMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString("ViewPane.restore")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (isZoomed)
				doZoom();
			else
				doPin();
		}
	});
	item.setEnabled(isZoomed || isFastView);

	addFastViewMenuItem(aMenu,isFastView);

	// add maximize item
	item = new MenuItem(aMenu, SWT.NONE);
	item.setText(WorkbenchMessages.getString("ViewPane.maximize")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			doZoom();
		}
	});
	item.setEnabled(!isZoomed && !isFastView && canZoom);

	new MenuItem(aMenu, SWT.SEPARATOR);
	
	// add close item
	item = new MenuItem(aMenu, SWT.CASCADE);
	item.setText(WorkbenchMessages.getString("ViewPane.close")); //$NON-NLS-1$
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			doHide();
		}
	});

	// open menu    
	point = parent.toDisplay(point);
	aMenu.setLocation(point.x, point.y);
	aMenu.setVisible(true);
}

protected void addFastViewMenuItem(Menu parent,boolean isFastView) {}

protected void doPin() {}
}
