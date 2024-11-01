/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Vincent Lorenzo <vincent.lorenzo@cea.fr> - Bug 546847
 *******************************************************************************/
package org.eclipse.ui.part;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.internal.util.Tracing;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.INestableKeyBindingService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A multi-page editor is an editor with multiple pages, each of which may
 * contain an editor or an arbitrary SWT control.
 * <p>
 * Subclasses must implement the following methods:
 * </p>
 * <ul>
 * <li><code>createPages</code> - to create the required pages by calling one of
 * the <code>addPage</code> methods</li>
 * <li><code>IEditorPart.doSave</code> - to save contents of editor</li>
 * <li><code>IEditorPart.doSaveAs</code> - to save contents of editor</li>
 * <li><code>IEditorPart.isSaveAsAllowed</code> - to enable Save As</li>
 * <li><code>IEditorPart.gotoMarker</code> - to scroll to a marker</li>
 * </ul>
 * <p>
 * Multi-page editors have a single action bar contributor, which manages
 * contributions for all the pages. The contributor must be a subclass of
 * <code>MultiPageEditorActionBarContributor</code>. Note that since any nested
 * editors are created directly in code by callers of
 * <code>addPage(IEditorPart,IEditorInput)</code>, nested editors do not have
 * their own contributors.
 * </p>
 * <p>
 * As of 3.5 multi-page editors will post PageChangedEvents at the end of
 * {@link #pageChange(int)}. Subclasses may override {@link #getSelectedPage()}
 * to return a page appropriate to their multi-page editor. IPartListener2
 * listeners registered with the IPartService can implement IPageChangedListener
 * to be notified about all page change events within the workbench page or
 * workbench window.
 * </p>
 *
 * @see org.eclipse.ui.part.MultiPageEditorActionBarContributor
 * @see org.eclipse.jface.dialogs.IPageChangeProvider
 * @see org.eclipse.jface.dialogs.IPageChangedListener
 * @see org.eclipse.ui.IPartService
 */
public abstract class MultiPageEditorPart extends EditorPart implements IPageChangeProvider {

	private static final String COMMAND_NEXT_SUB_TAB = "org.eclipse.ui.navigate.nextSubTab"; //$NON-NLS-1$
	private static final String COMMAND_PREVIOUS_SUB_TAB = "org.eclipse.ui.navigate.previousSubTab"; //$NON-NLS-1$

	/**
	 * Subclasses that override {@link #createPageContainer(Composite)} can use this
	 * constant to get a site for the container that can be active while the current
	 * page is deactivated.
	 *
	 * @since 3.4
	 * @see #activateSite()
	 * @see #deactivateSite(boolean, boolean)
	 * @see #getPageSite(int)
	 */
	protected static final int PAGE_CONTAINER_SITE = 65535;

	/**
	 * Private tracing output.
	 */
	private static final String TRACING_COMPONENT = "MPE"; //$NON-NLS-1$

	/**
	 * The active service locator. This value may be <code>null</code> if there is
	 * no selected page, or if the selected page is a control with no site.
	 */
	private INestable activeServiceLocator;

	/**
	 * The container widget.
	 */
	private CTabFolder container;

	/**
	 * List of nested editors. Element type: IEditorPart. Need to hang onto them
	 * here, in addition to using get/setData on the items, because dispose() needs
	 * to access them, but widgetry has already been disposed at that point.
	 */
	private ArrayList<IEditorPart> nestedEditors = new ArrayList<>(3);

	private List<IServiceLocator> pageSites = new ArrayList<>(3);

	private IServiceLocator pageContainerSite;

	private ListenerList<IPageChangedListener> pageChangeListeners = new ListenerList<>(ListenerList.IDENTITY);

	/**
	 * Creates an empty multi-page editor with no pages and registers a
	 * {@link PropertyChangeListener} to listen for changes to the editor's
	 * preference..
	 */
	protected MultiPageEditorPart() {
		super();
		getAPIPreferenceStore().addPropertyChangeListener(event -> {
			if (isUpdateRequired(event)) {
				updateContainer();
			}
		});
	}

	/**
	 * Determines whether an update is required based on a property change event.
	 *
	 * @param event the {@link PropertyChangeEvent} triggered by a change in the
	 *              preference store
	 * @since 3.134
	 */
	protected boolean isUpdateRequired(PropertyChangeEvent event) {
		if (event.getProperty().equals(IWorkbenchPreferenceConstants.ALIGN_MULTI_PAGE_EDITOR_TABS)) {
			return true;
		}
		return false;
	}

	/**
	 * Creates and adds a new page containing the given control to this multi-page
	 * editor. The control may be <code>null</code>, allowing it to be created and
	 * set later using <code>setControl</code>.
	 *
	 * @param control the control, or <code>null</code>
	 * @return the index of the new page
	 *
	 * @see MultiPageEditorPart#setControl(int, Control)
	 */
	public int addPage(Control control) {
		int index = getPageCount();
		addPage(index, control);
		return index;
	}

	/**
	 * Creates and adds a new page containing the given control to this multi-page
	 * editor. The page is added at the given index. The control may be
	 * <code>null</code>, allowing it to be created and set later using
	 * <code>setControl</code>.
	 *
	 * @param index   the index at which to add the page (0-based)
	 * @param control the control, or <code>null</code>
	 *
	 * @see MultiPageEditorPart#setControl(int, Control)
	 */
	public void addPage(int index, Control control) {
		createItem(index, control);
	}

	/**
	 * Creates and adds a new page containing the given editor to this multi-page
	 * editor. This also hooks a property change listener on the nested editor.
	 *
	 * @param editor the nested editor
	 * @param input  the input for the nested editor
	 * @return the index of the new page
	 * @exception PartInitException if a new page could not be created
	 *
	 * @see MultiPageEditorPart#handlePropertyChange(int) the handler for property
	 *      change events from the nested editor
	 */
	public int addPage(IEditorPart editor, IEditorInput input) throws PartInitException {
		int index = getPageCount();
		addPage(index, editor, input);
		return index;
	}

	/**
	 * Creates and adds a new page containing the given editor to this multi-page
	 * editor. The page is added at the given index. This also hooks a property
	 * change listener on the nested editor.
	 *
	 * @param index  the index at which to add the page (0-based)
	 * @param editor the nested editor
	 * @param input  the input for the nested editor
	 * @exception PartInitException if a new page could not be created
	 *
	 * @see MultiPageEditorPart#handlePropertyChange(int) the handler for property
	 *      change events from the nested editor
	 */
	public void addPage(int index, IEditorPart editor, IEditorInput input) throws PartInitException {
		IEditorSite site = createSite(editor);
		// call init first so that if an exception is thrown, we have created no
		// new widgets
		editor.init(site, input);
		Composite parent2 = new Composite(getContainer(), getOrientation(editor));
		parent2.setLayout(new FillLayout());
		editor.createPartControl(parent2);
		editor.addPropertyListener((source, propertyId) -> MultiPageEditorPart.this.handlePropertyChange(propertyId));
		// create item for page only after createPartControl has succeeded
		Item item = createItem(index, parent2);
		// remember the editor, as both data on the item, and in the list of
		// editors (see field comment)
		item.setData(editor);
		nestedEditors.add(editor);
	}

	/**
	 * Get the orientation of the editor.
	 *
	 * @return int the orientation flag
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#NONE
	 */
	private int getOrientation(IEditorPart editor) {
		if (editor instanceof IWorkbenchPartOrientation) {
			return ((IWorkbenchPartOrientation) editor).getOrientation();
		}
		return getOrientation();
	}

	/**
	 * Creates an empty container. Creates a CTabFolder with no style bits set, and
	 * hooks a selection listener which calls <code>pageChange()</code> whenever the
	 * selected tab changes.
	 *
	 * @param parent The composite in which the container tab folder should be
	 *               created; must not be <code>null</code>.
	 * @return a new container
	 *
	 * @since 3.115
	 */
	protected CTabFolder createContainer(Composite parent) {
		// use SWT.FLAT style so that an extra 1 pixel border is not reserved
		// inside the folder
		parent.setLayout(new FillLayout());
		final CTabFolder newContainer = new CTabFolder(parent, getTabStyle() | SWT.FLAT);
		newContainer.addSelectionListener(widgetSelectedAdapter(e -> {
			int newPageIndex = newContainer.indexOf((CTabItem) e.item);
			pageChange(newPageIndex);
		}));
		newContainer.addTraverseListener(e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_PAGE_NEXT:
			case SWT.TRAVERSE_PAGE_PREVIOUS:
				int detail = e.detail;
				e.doit = true;
				e.detail = SWT.TRAVERSE_NONE;
				Control control = newContainer.getParent();
				do {
					if (control.traverse(detail))
						return;
					if (control.getListeners(SWT.Traverse).length != 0)
						return;
					if (control instanceof Shell)
						return;
					control = control.getParent();
				} while (control != null);
			}
		});
		return newContainer;
	}

	/**
	 * Determines the tab style based on user preferences.
	 * <p>
	 * This method retrieves the user preference for aligning multi-page editor tabs
	 * on top or bottom, and returns the corresponding SWT style constant.
	 * </p>
	 *
	 * @return {@code SWT.TOP} if the user prefers tabs to be aligned on top,
	 *         {@code SWT.BOTTOM} if the user prefers tabs to be aligned on the
	 *         bottom.
	 * @since 3.134
	 */
	protected int getTabStyle() {
		return getAPIPreferenceStore().getInt(IWorkbenchPreferenceConstants.ALIGN_MULTI_PAGE_EDITOR_TABS);
	}

	/**
	 * @since 3.134
	 */
	protected IPreferenceStore getAPIPreferenceStore() {
		return PrefUtil.getAPIPreferenceStore();
	}

	/**
	 * Creates a tab item at the given index and places the given control in the new
	 * item. The item is a CTabItem with no style bits set.
	 *
	 * @param index   the index at which to add the control
	 * @param control is the control to be placed in an item
	 * @return a new item
	 *
	 * @since 3.115
	 */
	protected CTabItem createItem(int index, Control control) {
		CTabItem item = new CTabItem(getTabFolder(), SWT.NONE, index);
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
	 * editor by calling <code>createContainer</code>, then
	 * <code>createPages</code>. Subclasses should implement
	 * <code>createPages</code> rather than overriding this method.
	 *
	 * @param parent The parent in which the editor should be created; must not be
	 *               <code>null</code>.
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite pageContainer = createPageContainer(parent);
		this.container = createContainer(pageContainer);
		createPages();
		// set the active page (page 0 by default), unless it has already been
		// done
		if (getActivePage() == -1) {
			setActivePage(0);
		}
		IEditorPart part = getEditor(getActivePage());
		if (part != null) {
			final IServiceLocator serviceLocator = part.getEditorSite();
			if (serviceLocator instanceof INestable) {
				activeServiceLocator = (INestable) serviceLocator;
				activeServiceLocator.activate();
			}
		}
		initializePageSwitching();
		initializeSubTabSwitching();
	}

	/**
	 * Initialize the MultiPageEditorPart to use the page switching command. Clients
	 * can override this method with an empty body if they wish to opt-out.
	 *
	 * @since 3.4
	 */
	protected void initializePageSwitching() {
		new PageSwitcher(this) {
			@Override
			public Object[] getPages() {
				int pageCount = getPageCount();
				Object[] result = new Object[pageCount];
				for (int i = 0; i < pageCount; i++) {
					result[i] = Integer.valueOf(i);
				}
				return result;
			}

			@Override
			public String getName(Object page) {
				return getPageText(((Integer) page).intValue());
			}

			@Override
			public ImageDescriptor getImageDescriptor(Object page) {
				Image image = getPageImage(((Integer) page).intValue());
				if (image == null)
					return null;

				return ImageDescriptor.createFromImage(image);
			}

			@Override
			public void activatePage(Object page) {
				setActivePage(((Integer) page).intValue());
			}

			@Override
			public int getCurrentPageIndex() {
				return getActivePage();
			}
		};
	}

	/**
	 * Initialize the MultiPageEditorPart to use the sub-tab switching commands.
	 *
	 * @since 3.5
	 */
	private void initializeSubTabSwitching() {
		IHandlerService service = getSite().getService(IHandlerService.class);
		service.activateHandler(COMMAND_NEXT_SUB_TAB, new AbstractHandler() {
			/**
			 * {@inheritDoc}
			 *
			 * @throws ExecutionException if an exception occurred during execution
			 */
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				int n = getPageCount();
				if (n == 0)
					return null;

				int i = getActivePage() + 1;
				if (i >= n)
					i = 0;
				setActivePage(i);
				return null;
			}
		});

		service.activateHandler(COMMAND_PREVIOUS_SUB_TAB, new AbstractHandler() {
			/**
			 * {@inheritDoc}
			 *
			 * @throws ExecutionException if an exception occurred during execution
			 */
			@Override
			public Object execute(ExecutionEvent event) throws ExecutionException {
				int n = getPageCount();
				if (n == 0)
					return null;

				int i = getActivePage() - 1;
				if (i < 0)
					i = n - 1;
				setActivePage(i);
				return null;
			}
		});
	}

	/**
	 * Creates the parent control for the container returned by
	 * {@link #getContainer() }.
	 *
	 * <p>
	 * Subclasses may extend and must call super implementation first.
	 * </p>
	 *
	 * @param parent the parent for all of the editors contents.
	 * @return the parent for this editor's container. Must not be
	 *         <code>null</code>.
	 *
	 * @since 3.2
	 */
	protected Composite createPageContainer(Composite parent) {
		return parent;
	}

	/**
	 * Creates the site for the given nested editor. The
	 * <code>MultiPageEditorPart</code> implementation of this method creates an
	 * instance of <code>MultiPageEditorSite</code>. Subclasses may reimplement to
	 * create more specialized sites.
	 *
	 * @param editor the nested editor
	 * @return the editor site
	 */
	protected IEditorSite createSite(IEditorPart editor) {
		return new MultiPageEditorSite(this, editor);
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors. Subclasses
	 * may extend.
	 */
	@Override
	public void dispose() {
		if (getSite() != null) {
			deactivateSite(true, false);
		}

		pageChangeListeners.clear();
		for (IEditorPart editor : nestedEditors) {
			disposePart(editor);
		}
		nestedEditors.clear();
		if (pageContainerSite instanceof IDisposable) {
			((IDisposable) pageContainerSite).dispose();
			pageContainerSite = null;
		}
		for (IServiceLocator sl : pageSites) {
			if (sl instanceof IDisposable) {
				((IDisposable) sl).dispose();
			}
		}
		pageSites.clear();
		super.dispose();
	}

	/**
	 * Returns the active nested editor if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 *
	 * @nooverride
	 * @return the active nested editor, or <code>null</code> if none
	 */
	protected IEditorPart getActiveEditor() {
		int index = getActivePage();
		if (index != -1) {
			return getEditor(index);
		}
		return null;
	}

	/**
	 * Returns the index of the currently active page, or -1 if there is no active
	 * page.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 *
	 * @nooverride
	 *
	 * @return the index of the active page, or -1 if there is no active page
	 * @since 3.5
	 */
	public int getActivePage() {
		CTabFolder tabFolder = getTabFolder();
		if (tabFolder != null && !tabFolder.isDisposed()) {
			return tabFolder.getSelectionIndex();
		}
		return -1;
	}

	/**
	 * Returns the composite control containing this multi-page editor's pages. This
	 * should be used as the parent when creating controls for the individual pages.
	 * That is, when calling <code>addPage(Control)</code>, the passed control
	 * should be a child of this container.
	 * <p>
	 * Warning: Clients should not assume that the container is any particular
	 * subclass of Composite. The actual class used may change in order to improve
	 * the look and feel of multi-page editors. Any code making assumptions on the
	 * particular subclass would thus be broken.
	 * </p>
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 *
	 * @return the composite, or <code>null</code> if <code>createPartControl</code>
	 *         has not been called yet
	 */
	protected Composite getContainer() {
		return container;
	}

	/**
	 * Returns the control for the given page index, or <code>null</code> if no
	 * control has been set for the page. The page index must be valid.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 *
	 * @param pageIndex the index of the page
	 * @return the control for the specified page, or <code>null</code> if none has
	 *         been set
	 */
	protected Control getControl(int pageIndex) {
		return getItem(pageIndex).getControl();
	}

	/**
	 * Returns the editor for the given page index. The page index must be valid.
	 *
	 * @param pageIndex the index of the page
	 * @return the editor for the specified page, or <code>null</code> if the
	 *         specified page was not created with
	 *         <code>addPage(IEditorPart,IEditorInput)</code>
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
	 * Returns the service locator for the given page index. This method can be used
	 * to create service locators for pages that are just controls. The page index
	 * must be valid.
	 * <p>
	 * This will return the editor site service locator for an editor, and create
	 * one for a page that is just a control.
	 * </p>
	 *
	 * @param pageIndex the index of the page
	 * @return the editor for the specified page, or <code>null</code> if the
	 *         specified page was not created with
	 *         <code>addPage(IEditorPart,IEditorInput)</code>
	 * @since 3.4
	 */
	protected final IServiceLocator getPageSite(int pageIndex) {
		if (pageIndex == PAGE_CONTAINER_SITE) {
			return getPageContainerSite();
		}

		Item item = getItem(pageIndex);
		if (item != null) {
			Object data = item.getData();
			if (data instanceof IEditorPart) {
				return ((IEditorPart) data).getSite();
			} else if (data instanceof IServiceLocator) {
				return (IServiceLocator) data;
			} else if (data == null) {
				IServiceLocatorCreator slc = getSite().getService(IServiceLocatorCreator.class);
				IServiceLocator sl = slc.createServiceLocator(getSite(), null, this::close);
				item.setData(sl);
				pageSites.add(sl);
				return sl;
			}
		}
		return null;
	}

	void close() {
		// 3.x implementation closes the editor when the ISL is disposed
		PartSite partSite = (PartSite) getSite();
		MPart model = partSite.getModel();
		Widget widget = (Widget) model.getWidget();
		if (widget != null && !widget.isDisposed()) {
			getSite().getPage().closeEditor(MultiPageEditorPart.this, true);
		}
	}

	/**
	 * @return A site that can be used with a header.
	 * @since 3.4
	 * @see #createPageContainer(Composite)
	 * @see #PAGE_CONTAINER_SITE
	 * @see #getPageSite(int)
	 */
	private IServiceLocator getPageContainerSite() {
		if (pageContainerSite == null) {
			IServiceLocatorCreator slc = getSite().getService(IServiceLocatorCreator.class);
			pageContainerSite = slc.createServiceLocator(getSite(), null, this::close);
		}
		return pageContainerSite;
	}

	/**
	 * Returns the tab item for the given page index (page index is 0-based). The
	 * page index must be valid.
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
		if (folder != null && !folder.isDisposed()) {
			return folder.getItemCount();
		}
		return 0;
	}

	/**
	 * Returns the image for the page with the given index, or <code>null</code> if
	 * no image has been set for the page. The page index must be valid.
	 *
	 * @param pageIndex the index of the page
	 * @return the image, or <code>null</code> if none
	 */
	protected Image getPageImage(int pageIndex) {
		return getItem(pageIndex).getImage();
	}

	/**
	 * Returns the text label for the page with the given index. Returns the empty
	 * string if no text label has been set for the page. The page index must be
	 * valid.
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
	 * @return the tab folder, or <code>null</code> if
	 *         <code>createPartControl</code> has not been called yet
	 */
	private CTabFolder getTabFolder() {
		return container;
	}

	/**
	 * Handles a property change notification from a nested editor. The default
	 * implementation simply forwards the change to listeners on this multi-page
	 * editor by calling <code>firePropertyChange</code> with the same property id.
	 * For example, if the dirty state of a nested editor changes (property id
	 * <code>IEditorPart.PROP_DIRTY</code>), this method handles it by firing a
	 * property change event for <code>IEditorPart.PROP_DIRTY</code> to property
	 * listeners on this multi-page editor.
	 * <p>
	 * Subclasses may extend or reimplement this method.
	 * </p>
	 *
	 * @param propertyId the id of the property that changed
	 */
	protected void handlePropertyChange(int propertyId) {
		firePropertyChange(propertyId);
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IEditorPart</code> method sets its site to the given site, its input to
	 * the given input, and the site's selection provider to a
	 * <code>MultiPageSelectionProvider</code>. Subclasses may extend this method.
	 *
	 * @param site  The site for which this part is being created; must not be
	 *              <code>null</code>.
	 * @param input The input on which this editor should be created; must not be
	 *              <code>null</code>.
	 * @throws PartInitException If the initialization of the part fails --
	 *                           currently never.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		site.setSelectionProvider(new MultiPageSelectionProvider(this));
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IEditorPart</code> method returns whether the contents of any of this
	 * multi-page editor's nested editors have changed since the last save. Pages
	 * created with <code>addPage(Control)</code> are ignored.
	 * <p>
	 * Subclasses may extend or reimplement this method.
	 * </p>
	 *
	 * @return <code>true</code> if any of the nested editors are dirty;
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean isDirty() {
		// use nestedEditors to avoid SWT requests; see bug 12996
		for (IEditorPart editor : nestedEditors) {
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
	 * The <code>MultiPageEditorPart</code> implementation of this method sets focus
	 * to the new page, and notifies the action bar contributor (if there is one).
	 * This checks whether the action bar contributor is an instance of
	 * <code>MultiPageEditorActionBarContributor</code>, and, if so, calls
	 * <code>setActivePage</code> with the active nested editor. This also fires a
	 * selection change event if required.
	 * </p>
	 * <p>
	 * Subclasses may extend this method.
	 * </p>
	 *
	 * @param newPageIndex the index of the activated page
	 */
	protected void pageChange(int newPageIndex) {
		deactivateSite(false, false);

		IPartService partService = getSite().getService(IPartService.class);
		if (partService.getActivePart() == this) {
			setFocus();
		}

		IEditorPart activeEditor = getEditor(newPageIndex);

		IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
		if (contributor != null && contributor instanceof MultiPageEditorActionBarContributor) {
			((MultiPageEditorActionBarContributor) contributor).setActivePage(activeEditor);
		}

		if (activeEditor != null) {
			ISelectionProvider selectionProvider = activeEditor.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				ISelectionProvider outerProvider = getSite().getSelectionProvider();
				if (outerProvider instanceof MultiPageSelectionProvider) {
					SelectionChangedEvent event = new SelectionChangedEvent(selectionProvider,
							selectionProvider.getSelection());

					MultiPageSelectionProvider provider = (MultiPageSelectionProvider) outerProvider;
					provider.fireSelectionChanged(event);
					provider.firePostSelectionChanged(event);
				} else if (Policy.DEBUG_MPE) {
					Tracing.printTrace(TRACING_COMPONENT, "MultiPageEditorPart " + getTitle() //$NON-NLS-1$
							+ " did not propogate selection for " //$NON-NLS-1$
							+ activeEditor.getTitle());
				}
			}
		}

		activateSite();
		Object selectedPage = getSelectedPage();
		if (selectedPage != null) {
			firePageChanged(new PageChangedEvent(this, selectedPage));
		}
	}

	/**
	 * This method can be used by implementors of
	 * {@link MultiPageEditorPart#createPageContainer(Composite)} to deactivate the
	 * active inner editor services while their header has focus. A deactivateSite()
	 * must have a matching call to activateSite() when appropriate.
	 * <p>
	 * An new inner editor will have its site activated on a
	 * {@link MultiPageEditorPart#pageChange(int)}.
	 * </p>
	 * <p>
	 * <b>Note:</b> This API is evolving in 3.4 and this might not be its final
	 * form.
	 * </p>
	 *
	 * @param immediate           immediately deactivate the legacy keybinding
	 *                            service
	 * @param containerSiteActive Leave the page container site active.
	 * @since 3.4
	 * @see #activateSite()
	 * @see #createPageContainer(Composite)
	 * @see #getPageSite(int)
	 * @see #PAGE_CONTAINER_SITE
	 */
	protected final void deactivateSite(boolean immediate, boolean containerSiteActive) {
		// Deactivate the nested services from the last active service locator.
		if (activeServiceLocator != null) {
			activeServiceLocator.deactivate();
			activeServiceLocator = null;
		}

		final int pageIndex = getActivePage();
		final IKeyBindingService service = getSite().getKeyBindingService();
		if (pageIndex < 0 || pageIndex >= getPageCount() || immediate) {
			// There is no selected page, so deactivate the active service.
			if (service instanceof INestableKeyBindingService) {
				final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
				nestableService.activateKeyBindingService(null);
			} else {
				WorkbenchPlugin.log(
						"MultiPageEditorPart.deactivateSite()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " //$NON-NLS-1$
								+ service.getClass().getName() + " instead."); //$NON-NLS-1$
			}
		}

		if (containerSiteActive) {
			IServiceLocator containerSite = getPageContainerSite();
			if (containerSite instanceof INestable) {
				activeServiceLocator = (INestable) containerSite;
				activeServiceLocator.activate();
			}
		}
	}

	/**
	 * This method can be used by implementors of
	 * {@link #createPageContainer(Composite)} to activate the active inner editor
	 * services when their header loses focus.
	 * <p>
	 * An new inner editor will have its site activated on a
	 * {@link #pageChange(int)}.
	 * </p>
	 * <p>
	 * <b>Note:</b> This API is evolving in 3.4 and this might not be its final
	 * form.
	 * </p>
	 *
	 * @since 3.4
	 * @see #deactivateSite(boolean,boolean)
	 * @see #createPageContainer(Composite)
	 * @see #getPageSite(int)
	 */
	protected final void activateSite() {
		if (activeServiceLocator != null) {
			activeServiceLocator.deactivate();
			activeServiceLocator = null;
		}

		final IKeyBindingService service = getSite().getKeyBindingService();
		final int pageIndex = getActivePage();
		final IEditorPart editor = getEditor(pageIndex);

		if (editor != null) {
			// active the service for this inner editor
			if (service instanceof INestableKeyBindingService) {
				final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
				nestableService.activateKeyBindingService(editor.getEditorSite());

			} else {
				WorkbenchPlugin.log(
						"MultiPageEditorPart.activateSite()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " //$NON-NLS-1$
								+ service.getClass().getName() + " instead."); //$NON-NLS-1$
			}
			// Activate the services for the new service locator.
			final IServiceLocator serviceLocator = editor.getEditorSite();
			if (serviceLocator instanceof INestable) {
				activeServiceLocator = (INestable) serviceLocator;
				activeServiceLocator.activate();
			}

		} else {
			Item item = getItem(pageIndex);

			// There is no selected editor, so deactivate the active service.
			if (service instanceof INestableKeyBindingService) {
				final INestableKeyBindingService nestableService = (INestableKeyBindingService) service;
				nestableService.activateKeyBindingService(null);
			} else {
				WorkbenchPlugin.log(
						"MultiPageEditorPart.activateSite()   Parent key binding service was not an instance of INestableKeyBindingService.  It was an instance of " //$NON-NLS-1$
								+ service.getClass().getName() + " instead."); //$NON-NLS-1$
			}

			if (item.getData() instanceof INestable) {
				activeServiceLocator = (INestable) item.getData();
				activeServiceLocator.activate();
			}
		}
	}

	/**
	 * Disposes the given part and its site.
	 *
	 * @param part The part to dispose; must not be <code>null</code>.
	 */
	private void disposePart(final IWorkbenchPart part) {
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() {
				IWorkbenchPartSite partSite = part.getSite();
				part.dispose();
				if (partSite instanceof MultiPageEditorSite) {
					((MultiPageEditorSite) partSite).dispose();
				}
			}

			@Override
			public void handleException(Throwable e) {
				// Exception has already being logged by Core. Do nothing.
			}
		});
	}

	/**
	 * Removes the page with the given index from this multi-page editor. The
	 * controls for the page are disposed of; if the page has an editor, it is
	 * disposed of too. The page index must be valid.
	 *
	 * @param pageIndex the index of the page
	 * @see MultiPageEditorPart#addPage(Control)
	 * @see MultiPageEditorPart#addPage(IEditorPart, IEditorInput)
	 */
	public void removePage(int pageIndex) {
		Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
		// get editor (if any) before disposing item
		IEditorPart editor = getEditor(pageIndex);

		// get control for the item if it's not an editor
		CTabItem item = getItem(pageIndex);
		IServiceLocator pageLocator = null;
		if (item.getData() instanceof IServiceLocator) {
			pageLocator = (IServiceLocator) item.getData();
		}
		Control pageControl = item.getControl();

		// dispose item before disposing editor, in case there's an exception
		// in editor's dispose
		item.dispose();

		if (pageControl != null) {
			pageControl.dispose();
		}

		// dispose editor (if any)
		if (editor != null) {
			nestedEditors.remove(editor);
			disposePart(editor);
		}
		if (pageLocator != null) {
			pageSites.remove(pageLocator);
			if (pageLocator instanceof IDisposable) {
				((IDisposable) pageLocator).dispose();
			}
		}
	}

	/**
	 * Sets the currently active page.
	 *
	 * @param pageIndex the index of the page to be activated; the index must be
	 *                  valid
	 */
	protected void setActivePage(int pageIndex) {
		Assert.isTrue(pageIndex >= 0 && pageIndex < getPageCount());
		getTabFolder().setSelection(pageIndex);
		pageChange(pageIndex);
	}

	/**
	 * Sets the control for the given page index. The page index must be valid.
	 *
	 * @param pageIndex the index of the page
	 * @param control   the control for the specified page, or <code>null</code> to
	 *                  clear the control
	 */
	protected void setControl(int pageIndex, Control control) {
		getItem(pageIndex).setControl(control);
	}

	/**
	 * The <code>MultiPageEditor</code> implementation of this
	 * <code>IWorkbenchPart</code> method sets focus on the active nested editor, if
	 * there is one.
	 * <p>
	 * Subclasses may extend or reimplement.
	 * </p>
	 */
	@Override
	public void setFocus() {
		setFocus(getActivePage());
	}

	/**
	 * Sets focus to the control for the given page. If the page has an editor, this
	 * calls its <code>setFocus()</code> method. Otherwise, this calls
	 * <code>setFocus</code> on the control for the page.
	 *
	 * @param pageIndex the index of the page
	 */
	private void setFocus(int pageIndex) {
		if (pageIndex < 0 || pageIndex >= getPageCount()) {
			// page index out of bounds, don't set focus.
			return;
		}
		final IEditorPart editor = getEditor(pageIndex);
		if (editor != null) {
			editor.setFocus();

		} else {
			// Give the page's control focus.
			final Control control = getControl(pageIndex);
			if (control != null) {
				control.setFocus();
			}
		}
	}

	/**
	 * Sets the image for the page with the given index, or <code>null</code> to
	 * clear the image for the page. The page index must be valid.
	 *
	 * @param pageIndex the index of the page
	 * @param image     the image, or <code>null</code>
	 */
	protected void setPageImage(int pageIndex, Image image) {
		getItem(pageIndex).setImage(image);
	}

	/**
	 * Sets the text label for the page with the given index. The page index must be
	 * valid. The text label must not be null.
	 *
	 * @param pageIndex the index of the page
	 * @param text      the text label
	 */
	protected void setPageText(int pageIndex, String text) {
		getItem(pageIndex).setText(text);
	}

	/**
	 * If there is an adapter registered against the subclass of MultiPageEditorPart
	 * return that. Otherwise, delegate to the internal editor.
	 *
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		T result = super.getAdapter(adapter);
		// restrict delegating to the UI thread for bug 144851
		if (result == null && Display.getCurrent() != null) {
			IEditorPart innerEditor = getActiveEditor();
			// see bug 138823 - prevent some subclasses from causing
			// an infinite loop
			if (innerEditor != null && innerEditor != this) {
				result = Adapters.adapt(innerEditor, adapter);
			}
		}
		return result;
	}

	/**
	 * Find the editors contained in this multi-page editor whose editor input match
	 * the provided input.
	 *
	 * @param input the editor input
	 * @return the editors contained in this multi-page editor whose editor input
	 *         match the provided input
	 * @since 3.3
	 */
	public final IEditorPart[] findEditors(IEditorInput input) {
		List<IEditorPart> result = new ArrayList<>();
		int count = getPageCount();
		for (int i = 0; i < count; i++) {
			IEditorPart editor = getEditor(i);
			if (editor != null && editor.getEditorInput() != null && editor.getEditorInput().equals(input)) {
				result.add(editor);
			}
		}
		return result.toArray(new IEditorPart[result.size()]);
	}

	/**
	 * Set the active page of this multi-page editor to the page that contains the
	 * given editor part. This method has no effect of the given editor part is not
	 * contained in this multi-page editor.
	 *
	 * @param editorPart the editor part
	 * @since 3.3
	 */
	public final void setActiveEditor(IEditorPart editorPart) {
		int count = getPageCount();
		for (int i = 0; i < count; i++) {
			IEditorPart editor = getEditor(i);
			if (editor == editorPart) {
				setActivePage(i);
				break;
			}
		}
	}

	/**
	 * Returns the selected page for the current active page index, either the
	 * IEditorPart for editors or the Control for other pages.
	 * <p>
	 * <b>Note:</b> clients may override this method to return a page appropriate
	 * for their editors. Maybe be <code>null</code>.
	 * </p>
	 *
	 * @return The IEditorPart or Control representing the current active page, or
	 *         <code>null</code> if there are no active pages.
	 * @since 3.5
	 * @see #getActivePage()
	 */
	@Override
	public Object getSelectedPage() {
		int index = getActivePage();
		if (index == -1) {
			return null;
		}
		IEditorPart editor = getEditor(index);
		if (editor != null) {
			return editor;
		}
		return getControl(index);
	}

	/**
	 * Add the page change listener to be notified when the page changes. The newly
	 * selected page will be the Object returned from {@link #getSelectedPage()}. In
	 * the default case, this will be the active page Control, IEditorPart, or
	 * <code>null</code>.
	 * <p>
	 * This method has no effect if the listener has already been added.
	 * </p>
	 *
	 * @nooverride
	 *
	 * @since 3.5
	 */
	@Override
	public void addPageChangedListener(IPageChangedListener listener) {
		pageChangeListeners.add(listener);
	}

	/**
	 * Remove the page change listener.
	 * <p>
	 * This method has no effect if the listener is not in the list.
	 * </p>
	 *
	 * @nooverride
	 *
	 * @since 3.5
	 */
	@Override
	public void removePageChangedListener(IPageChangedListener listener) {
		pageChangeListeners.remove(listener);
	}

	private void firePageChanged(final PageChangedEvent event) {
		for (final IPageChangedListener l : pageChangeListeners) {
			SafeRunnable.run(new SafeRunnable() {
				@Override
				public void run() {
					l.pageChanged(event);
				}
			});
		}
	}

	/**
	 * Updates the tab position of the container in the multi-page editor.
	 *
	 * <p>
	 * This method retrieves the current container and sets the tab position based
	 * on the user preference.
	 * </p>
	 *
	 * @since 3.134
	 */
	protected void updateContainer() {
		Composite container = getContainer();
		if (container instanceof CTabFolder tabFolder) {
			tabFolder.setTabPosition(getTabStyle());
			tabFolder.requestLayout();
		}
	}

}
