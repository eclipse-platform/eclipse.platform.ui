package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.*;

/**
 * A multi-page editor is an editor with multiple pages, each of which may
 * contain an editor or an arbitrary SWT control.
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>createPages</code> - to create the required pages by calling one
 *     of the <code>addPage</code> methods</li>
 *   <li><code>IEditorPart.doSave</code> - to save contents of editor</li>
 *   <li><code>IEditorPart.doSaveAs</code> - to save contents of editor</li>
 *   <li><code>IEditorPart.isSaveAsAllowed</code> - to enable Save As</li>
 *   <li><code>IEditorPart.gotoMarker</code> - to scroll to a marker</li>
 * </ul>
 * </p>
 * <p>
 * Multi-page editors have a single action bar contributor, which manages 
 * contributions for all the pages. The contributor must be a subclass of
 * <code>AbstractMultiPageEditorActionBarContributor</code>.
 * Note that since any nested editors are created directly in code by callers of 
 * <code>addPage(IEditorPart,IEditorInput)</code>, nested editors do not have 
 * their own contributors.
 * </p>
 *
 * @see AbstractMultiPageEditorActionBarContributor
 */
public abstract class MultiPageEditorPart extends EditorPart {
	/**
	 * The container widget.
	 */
	private CTabFolder container;

	/**
	 * List of nested editors. Element type: IEditorPart.
	 * Need to hang onto them here, in addition to using get/setData on the items,
	 * because dispose() needs to access them, but widgetry has already been disposed at that point.
	 */
	private ArrayList nestedEditors = new ArrayList(3);
/**
 * Creates an empty multi-page editor with no pages.
 */
protected MultiPageEditorPart() {
	super();
}
/**
 * Creates and adds a new page containing the given control to this multi-page 
 * editor.  The control may be <code>null</code>, allowing it to be created
 * and set later using <code>setControl</code>.
 *
 * @param control the control, or <code>null</code>
 * @return the index of the new page
 *
 * @see #setControl
 */
public int addPage(Control control) {
	createItem(control);
	return getPageCount() - 1;
}
/**
 * Creates and adds a new page containing the given editor to this multi-page 
 * editor. This also hooks a property change listener on the nested editor.
 *
 * @param editor the nested editor
 * @param input the input for the nested editor
 * @return the index of the new page
 * @exception PartInitException if a new page could not be created
 *
 * @see #handlePropertyChange the handler for property change events from the nested editor
 */
public int addPage(IEditorPart editor, IEditorInput input) throws PartInitException {
	IEditorSite site = createSite(editor);
	// call init first so that if an exception is thrown, we have created no new widgets
	editor.init(site, input);
	Composite parent2 = new Composite(getContainer(), SWT.NONE);
	parent2.setLayout(new FillLayout());
	editor.createPartControl(parent2);
	editor.addPropertyListener(
		new IPropertyListener() {
			public void propertyChanged(Object source, int propertyId) {
				MultiPageEditorPart.this.handlePropertyChange(propertyId);
			}
		});
	// create item for page only after createPartControl has succeeded
	Item item = createItem(parent2);
	// remember the editor, as both data on the item, and in the list of editors (see field comment)
	item.setData(editor);
	nestedEditors.add(editor);
	return getPageCount() - 1;
}
/**
 * Creates an empty container.
 * Creates a CTabFolder with no style bits set, and hooks a selection 
 * listener which calls <code>pageChange()</code> whenever the selected tab changes.
 * 
 * @return a new container
 */
private CTabFolder createContainer(Composite parent) {
	final CTabFolder container = new CTabFolder(parent, SWT.BOTTOM);
	container.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			int newPageIndex = container.indexOf((CTabItem) e.item);
			pageChange(newPageIndex);
		}
	});
	return container;
}
/**
 * Creates a tab item and places control in the new item.
 * The item is a CTabItem with no style bits set.
 *
 * @param control is the control to be placed in an item
 * @return a new item
 */
private CTabItem createItem(Control control) {
	CTabItem item = new CTabItem(getTabFolder(), SWT.NONE);
	item.setControl(control);
	return item;
}
/**
 * Creates the pages of this multi-page editor.
 * <p>
 * Subclasses must implement this method.
 * </p>
 */
protected abstract void createPages();
/**
 * The <code>MultiPageEditor</code> implementation of this 
 * <code>IWorkbenchPart</code> method creates the control for the multi-page
 * editor by calling <code>createContainer</code>, then <code>createPages</code>.
 * Subclasses should implement <code>createPages</code> rather than overriding 
 * this method.
 */
public final void createPartControl(Composite parent) {
	this.container = createContainer(parent);
	createPages();
	// set the active page (page 0 by default), unless it has already been done
	if (getActivePage() == -1)
		setActivePage(0);
}
/**
 * Creates the site for the given nested editor.
 * The <code>MultiPageEditorPart</code> implementation of this method creates an 
 * instance of <code>MultiPageEditorSite</code>. Subclasses may reimplement
 * to create more specialized sites.
 *
 * @param editor the nested editor
 * @return the editor site
 */
protected IEditorSite createSite(IEditorPart editor){
	return new MultiPageEditorSite(this, editor);
}
/**
 * The <code>MultiPageEditorPart</code> implementation of this 
 * <code>IWorkbenchPart</code> method disposes all nested editors.
 * Subclasses may extend.
 */
public void dispose() {
	for (int i = 0; i < nestedEditors.size(); ++i) {
		IEditorPart editor = (IEditorPart) nestedEditors.get(i);
		disposePart(editor);		
	}
	nestedEditors.clear();
}
/**
 * Returns the active nested editor if there is one.
 * <p>
 * Subclasses should not override this method
 * </p>
 * 
 * @return the active nested editor, or <code>null</code> if none
 */
protected IEditorPart getActiveEditor() {
	int index = getActivePage();
	if (index != -1)
		return getEditor(index);
	return null;
}
/**
 * Returns the index of the currently active page,
 * or -1 if there is no active page.
 * <p>
 * Subclasses should not override this method
 * </p>
 *
 * @return the index of the active page, or -1 if there is no active page
 */
protected int getActivePage() {
	CTabFolder tabFolder = getTabFolder();
	if (tabFolder != null && !tabFolder.isDisposed())
		return tabFolder.getSelectionIndex();
	return -1;
}
/**
 * Returns the composite control containing this multi-page editor's pages.
 * This should be used as the parent when creating controls for the individual pages.
 * That is, when calling <code>addPage(Control)</code>, the passed control should be
 * a child of this container.
 * <p>
 * Warning: Clients should not assume that the container is any particular subclass
 * of Composite.  The actual class used may change in order to improve the look and feel of
 * multi-page editors.  Any code making assumptions on the particular subclass would thus be broken.
 * </p>
 * <p>
 * Subclasses should not override this method
 * </p>
 *
 * @return the composite, or <code>null</code> if <code>createPartControl</code>
 *   has not been called yet
 */
protected Composite getContainer() {
	return container;
}
/**
 * Returns the control for the given page index, or <code>null</code>
 * if no control has been set for the page.
 * The page index must be valid.
 * <p>
 * Subclasses should not override this method
 * </p>
 *
 * @param pageIndex the index of the page
 * @return the control for the specified page, or <code>null</code> if none has been set
 */
protected Control getControl(int pageIndex) {
	return getItem(pageIndex).getControl();
}
/**
 * Returns the editor for the given page index.
 * The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @return the editor for the specified page, or <code>null</code> if the
 *   specified page was not created with 
 *   <code>addPage(IEditorPart,IEditorInput)</code>
 */
protected IEditorPart getEditor(int pageIndex) {
	Item item = getItem(pageIndex);
	if (item != null) {
		Object data = item.getData();
		if (data instanceof IEditorPart) {
			return (IEditorPart) data;
		}
	}
	return null;
}
/**
 * Returns the tab item for the given page index (page index is 0-based).
 * The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @return the tab item for the given page index
 */
private CTabItem getItem(int pageIndex) {
	return getTabFolder().getItem(pageIndex);
}
/**
 * Returns the number of pages in this multi-page editor.
 *
 * @return the number of pages
 */
protected int getPageCount() {
	CTabFolder folder = getTabFolder();
	// May not have been created yet, or may have been disposed.
	if (folder != null && !folder.isDisposed())
		return folder.getItemCount();
	return 0;
}
/**
 * Returns the image for the page with the given index,
 * or <code>null</code> if no image has been set for the page.
 * The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @return the image, or <code>null</code> if none
 */
protected Image getPageImage(int pageIndex) {
	return getItem(pageIndex).getImage();
}
/**
 * Returns the text label for the page with the given index.
 * Returns the empty string if no text label has been set for the page.
 * The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @return the text label for the page
 */
protected String getPageText(int pageIndex) {
	return getItem(pageIndex).getText();
}
/**
 * Returns the tab folder containing this multi-page editor's pages.
 *
 * @return the tab folder, or <code>null</code> if <code>createPartControl</code>
 *   has not been called yet
 */
private CTabFolder getTabFolder() {
	return container;
}
/**
 * Handles a property change notification from a nested editor.
 * The default implementation simply forwards the change to listeners
 * on this multi-page editor by calling <code>firePropertyChange</code>
 * with the same property id.  For example, if the dirty state of a nested editor
 * changes (property id <code>IEditorPart.PROP_DIRTY</code>), this method
 * handles it by firing a property change event for <code>IEditorPart.PROP_DIRTY</code>
 * to property listeners on this multi-page editor.
 * <p>
 * Subclasses may extend or reimplement this method.
 * </p>
 *
 * @param propertyId the id of the property that changed
 */
protected void handlePropertyChange (int propertyId) {
	firePropertyChange(propertyId);
}
/**
 * The <code>MultiPageEditorPart</code> implementation of this 
 * <code>IEditorPart</code> method sets its site to the given site, its 
 * input to the given input, and the site's selection provider to a
 * <code>MultiPageSelectionProvider</code>.
 * Subclasses may extend this method.
 */
public void init(IEditorSite site, IEditorInput input) throws PartInitException {
	setSite(site);
	setInput(input);
	site.setSelectionProvider(new MultiPageSelectionProvider(this));
}
/**
 * The <code>MultiPageEditorPart</code> implementation of this 
 * <code>IEditorPart</code> method returns whether the contents of any of this
 * multi-page editor's nested editors have changed since the last save.
 * Pages created with <code>addPage(Control)</code> are ignored.
 * <p>
 * Subclasses may extend or reimplement this method.
 * </p>
 */
public boolean isDirty() {
	// use nestedEditors to avoid SWT requests; see bug 12996
	for (Iterator i = nestedEditors.iterator(); i.hasNext();) {
		IEditorPart editor = (IEditorPart) i.next();
		if (editor.isDirty()) {
			return true;
		}
	}
	return false;
}
/**
 * Notifies this multi-page editor that the page with the given id has been
 * activated. This method is called when the user selects a different tab.
 * <p>
 * The <code>MultiPageEditorPart</code> implementation of this method 
 * sets focus to the new page, and notifies the action bar contributor (if there is one).
 * This checks whether the action bar contributor is an instance of 
 * <code>MultiPageEditorActionBarContributor</code>, and, if so,
 * calls <code>setActivePage</code> with the active nested editor.
 * This also fires a selection change event if required.
 * </p>
 * <p>
 * Subclasses may extend this method.
 * </p>
 *
 * @param newPageIndex the index of the activated page 
 */
protected void pageChange(int newPageIndex) {
	// XXX: Workaround for 1GCN531: SWT:WIN2000 - CTabFolder child's visibility is false on notification
	Control control = getControl(newPageIndex);
	if (control != null) {
		control.setVisible(true);
	}
	// XXX: End workaround
	setFocus();
	IEditorPart activeEditor = getEditor(newPageIndex);
	IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
	if (contributor != null && contributor instanceof MultiPageEditorActionBarContributor) {
		((MultiPageEditorActionBarContributor) contributor).setActivePage(activeEditor);
	}
	if (activeEditor != null) {
		//Workaround for 1GAUS7C: ITPUI:ALL - Editor not activated when restored from previous session
		//do not need second if once fixed
		ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
		if (selectionProvider != null) {
			SelectionChangedEvent event = new SelectionChangedEvent(selectionProvider, selectionProvider.getSelection());
			((MultiPageSelectionProvider) getSite().getSelectionProvider()).fireSelectionChanged(event);
		}
	}
}
private void disposePart(final IWorkbenchPart part) {
	Platform.run(new SafeRunnable() {
		public void run() {
			if (part.getSite() instanceof MultiPageEditorSite) {
				MultiPageEditorSite partSite = (MultiPageEditorSite) part.getSite();
				partSite.dispose();
			}
			part.dispose();
		}
		public void handleException(Throwable e) {
			//Exception has already being logged by Core. Do nothing.
		}
	});
}
/**
 * Removes the page with the given index from this multi-page editor.
 * The controls for the page are disposed of; if the page has an editor, 
 * it is disposed of too. The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @see #addPage
 */
public void removePage(int pageIndex) {
	Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
	// get editor (if any) before disposing item
	IEditorPart editor = getEditor(pageIndex);
	// dispose item before disposing editor, in case there's an exception in editor's dispose
	getItem(pageIndex).dispose();
	// dispose editor (if any)
	if (editor != null) {
		nestedEditors.remove(editor);
		disposePart(editor);
	}
}
/**
 * Sets the currently active page.
 *
 * @param pageIndex the index of the page to be activated; the index must be valid
 */
protected void setActivePage(int pageIndex) {
	Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
	getTabFolder().setSelection(pageIndex);
}
/**
 * Sets the control for the given page index.
 * The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @param control the control for the specified page, or <code>null</code> to clear the control
 */
protected void setControl(int pageIndex, Control control) {
	getItem(pageIndex).setControl(control);
}
/**
 * The <code>MultiPageEditor</code> implementation of this 
 * <code>IWorkbenchPart</code> method sets focus on the active nested editor,
 * if there is one.
 * <p>
 * Subclasses may extend or reimplement.
 * </p>
 */
public void setFocus() {
	int index = getActivePage();
	if (index != -1)
		setFocus(index);
}
/**
 * Sets focus to the control for the given page.
 * If the page has an editor, this calls its <code>setFocus()</code> method.
 * Otherwise, this calls <code>setFocus</code> on the control for the page.
 *
 * @pageIndex the index of the page
 */
private void setFocus(int pageIndex) {
	if (pageIndex < 0 || pageIndex >= getPageCount())
		return;
	IEditorPart editor = getEditor(pageIndex);
	if (editor != null) {
		editor.setFocus();
	} else {
		Control control = getControl(pageIndex);
		if (control != null) {
			control.setFocus();
		}
	}
}
/**
 * Sets the image for the page with the given index, or <code>null</code>
 * to clear the image for the page.
 * The page index must be valid.
 *
 * @param pageIndex the index of the page
 * @param image the image, or <code>null</code>
 */
protected void setPageImage(int pageIndex, Image image) {
	getItem(pageIndex).setImage(image);
}
/**
 * Sets the text label for the page with the given index.
 * The page index must be valid.
 * The text label must not be null.
 *
 * @param pageIndex the index of the page
 * @param text the text label
 */
protected void setPageText(int pageIndex, String text) {
	getItem(pageIndex).setText(text);
}
}
