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
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 *     Semion Chichelnitsky (semion@il.ibm.com) - bug 272564
 *     Craig Foote (Footeware.ca) - https://bugs.eclipse.org/325743
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *     Cornel Izbasa <cizbasa@info.uvt.ro> - Bug 417447
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 23862
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import java.util.HashSet;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISecondarySaveableSource;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.internal.DefaultSaveable;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.internal.views.properties.PropertiesMessages;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.eclipse.ui.part.ShowInContext;

/**
 * Main class for the Property Sheet View.
 * <p>
 * This standard view has id <code>"org.eclipse.ui.views.PropertySheet"</code>.
 * </p>
 * <p>
 * Note that property <i>sheets</i> and property sheet pages are not the same
 * thing as property <i>dialogs</i> and their property pages (the property pages
 * extension point is for contributing property pages to property dialogs).
 * Within the property sheet view, all pages are
 * <code>IPropertySheetPage</code>s.
 * </p>
 * <p>
 * Property sheet pages are discovered by the property sheet view automatically
 * when a part is first activated. The property sheet view asks the active part
 * for its property sheet page; this is done by invoking
 * <code>getAdapter(IPropertySheetPage.class)</code> on the part. If the part
 * returns a page, the property sheet view then creates the controls for that
 * property sheet page (using <code>createControl</code>), and adds the page to
 * the property sheet view. Whenever this part becomes active, its corresponding
 * property sheet page is shown in the property sheet view (which may or may not
 * be visible at the time). A part's property sheet page is discarded when the
 * part closes. The property sheet view has a default page (an instance of
 * <code>PropertySheetPage</code>) which services all parts without a property
 * sheet page of their own.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when a Property Sheet
 * view is needed for a workbench window. This class is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @see IPropertySheetPage
 * @see PropertySheetPage
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PropertySheet extends PageBookView
		implements ISelectionListener, IShowInTarget, IShowInSource, IRegistryEventListener, ISecondarySaveableSource {
	/**
	 * No longer used but preserved to avoid api change
	 */
	public static final String HELP_CONTEXT_PROPERTY_SHEET_VIEW = IPropertiesHelpContextIds.PROPERTY_SHEET_VIEW;

	/**
	 * Extension point used to modify behavior of the view
	 */
	private static final String EXT_POINT = "org.eclipse.ui.propertiesView"; //$NON-NLS-1$

	/**
	 * Message to show on the default page.
	 */
	private final String defaultText = PropertiesMessages.PropertyViewer_noProperties;

	/**
	 * The initial selection when the property sheet opens
	 */
	private ISelection bootstrapSelection;

	/**
	 * The current selection of the property sheet
	 */
	private ISelection currentSelection;

	/**
	 * The current part for which this property sheets is active
	 */
	private IWorkbenchPart currentPart;

	/**
	 * Whether this property sheet instance is pinned or not
	 */
	private IAction pinPropertySheetAction;

	/**
	 * Set of workbench parts, which should not be used as a source for PropertySheet
	 */
	private HashSet<String> ignoredViews;

	/** the view was hidden */
	private boolean wasHidden = true;

	/**
	 * the selection update which was made during the view was hidden need to be
	 * propagated to IPropertySheetPage
	 */
	private boolean selectionUpdatePending;

	private final SaveablesTracker saveablesTracker;

	private boolean needsUpdate = false;


	/**
	 * Propagates state changes of the saveable part tracked by this properties
	 * view, to properly update the dirty status. See bug 495567 comment 18.
	 */
	class SaveablesTracker implements ISaveablesLifecycleListener {

		@Override
		public void handleLifecycleEvent(SaveablesLifecycleEvent event) {
			if (currentPart == null || event.getEventType() != SaveablesLifecycleEvent.DIRTY_CHANGED
					|| !isDirtyStateSupported()) {
				return;
			}
			// to avoid endless loop we must ignore our own instance which
			// reports state changes too
			Saveable[] saveables = event.getSaveables();
			if (saveables == null) {
				return;
			}
			for (Saveable saveable : saveables) {
				// check if the saveable is for the current part
				if (new DefaultSaveable(PropertySheet.this).equals(saveable)) {
					return;
				}
			}

			if (event.getSource() instanceof SaveablesList) {
				SaveablesList saveablesList = (SaveablesList) event.getSource();
				for (Saveable saveable : saveables) {
					IWorkbenchPart[] parts = saveablesList.getPartsForSaveable(saveable);
					for (IWorkbenchPart part : parts) {
						if (PropertySheet.this.currentPart == part) {
							firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
							return;
						}
					}
				}
			}
		}

	}

	/**
	 * Creates a property sheet view.
	 */
	public PropertySheet() {
		super();
		pinPropertySheetAction = new PinPropertySheetAction();
		RegistryFactory.getRegistry().addListener(this, EXT_POINT);
		saveablesTracker = new SaveablesTracker();
	}

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage page = new MessagePage();
		initPage(page);
		page.createControl(book);
		page.setMessage(defaultText);
		return page;
	}

	/**
	 * The <code>PropertySheet</code> implementation of this
	 * <code>IWorkbenchPart</code> method creates a <code>PageBook</code> control
	 * with its default page showing.
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		pinPropertySheetAction.addPropertyChangeListener(event -> {
			if (IAction.CHECKED.equals(event.getProperty())) {
				updateContentDescription();
				if (!isPinned()) {
					selectionChanged(currentPart, currentSelection);
				}
			}
		});
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(pinPropertySheetAction);

		IToolBarManager toolBarManager = getViewSite().getActionBars()
				.getToolBarManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		toolBarManager.add(pinPropertySheetAction);
		ISaveablesLifecycleListener saveables = getSite().getService(ISaveablesLifecycleListener.class);
		if (saveables instanceof SaveablesList) {
			((SaveablesList) saveables).addModelLifecycleListener(saveablesTracker);
		}
		getSite().getPage().getWorkbenchWindow().getWorkbench().getHelpSystem()
				.setHelp(getPageBook(),
						IPropertiesHelpContextIds.PROPERTY_SHEET_VIEW);
	}

	@Override
	public void dispose() {
		IWorkbenchPartSite site = getSite();
		IWorkbenchPage page = site.getPage();
		ISaveablesLifecycleListener saveables = site.getService(ISaveablesLifecycleListener.class);

		// remove ourselves as a selection and registry listener
		page.removePostSelectionListener(this);
		RegistryFactory.getRegistry().removeListener(this);
		if (saveables instanceof SaveablesList) {
			((SaveablesList) saveables).removeModelLifecycleListener(saveablesTracker);
		}
		currentPart = null;
		currentSelection = null;
		pinPropertySheetAction = null;
		super.dispose();
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		// Get a custom property sheet page but not if the part is also a
		// PropertySheet. In this case the child property sheet would
		// accidentally reuse the parent's property sheet page.
		if(part instanceof PropertySheet) {
			return null;
		}
		IPropertySheetPage page = Adapters.adapt(part, IPropertySheetPage.class);
		if (page != null) {
			if (page instanceof IPageBookViewPage) {
				initPage((IPageBookViewPage) page);
			}
			page.createControl(getPageBook());
			return new PageRec(part, page);
		}

		// IContributedContentsView without contributed view, show default page
		IContributedContentsView view = Adapters.adapt(part, IContributedContentsView.class);
		if (view != null && view.getContributingPart() == null) {
			return null;
		}

		// Every part gets its own PropertySheetPage
		IPage dPage = createPropertySheetPage(getPageBook());
		return new PageRec(part, dPage);
	}

	/**
	 * Creates and returns a default properties page for this view. This page is
	 * used when a part does not provide a IPropertySheetPage
	 *
	 * @param book the pagebook control
	 * @return A default properties page
	 *
	 * @since 3.10
	 */
	protected IPage createPropertySheetPage(PageBook book) {
		// First consult platform adaptors for backward compatibility and testing code.
		IPropertySheetPage page = Platform.getAdapterManager().getAdapter(this, IPropertySheetPage.class);
		if (page == null) {
			page = new PropertySheetPage();
		}
		if (page instanceof IPageBookViewPage) {
			initPage((IPageBookViewPage) page);
		}
		page.createControl(book);
		return page;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec rec) {
		IPropertySheetPage page = (IPropertySheetPage) rec.page;
		page.dispose();
		rec.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		IWorkbenchPage page = getSite().getPage();
		if (page == null) {
			return null;
		}
		ISelection originalSel = page.getSelection();
		IWorkbenchPart activePart = page.getActivePart();
		if (activePart != null && activePart != this) {
			bootstrapSelection = originalSel;
			return activePart;
		}
		if (originalSel == null || originalSel.isEmpty()) {
			return null;
		}

		IEditorPart activeEditor = page.getActiveEditor();
		if (activeEditor != null && isImportant(activeEditor)) {
			if (activeEditor.getSite().getSelectionProvider() != null) {
				ISelection selection = activeEditor.getSite().getSelectionProvider().getSelection();
				if (originalSel.equals(selection)) {
					bootstrapSelection = originalSel;
					return activeEditor;
				}
			}
		}
		IViewReference[] viewrefs = page.getViewReferences();
		for (IViewReference ref : viewrefs) {
			IWorkbenchPart part = ref.getPart(false);
			if (part == null || part == this || !page.isPartVisible(part)) {
				continue;
			}
			if (!isImportant(part) || part.getSite().getSelectionProvider() == null) {
				continue;
			}
			ISelection selection = part.getSite().getSelectionProvider().getSelection();
			if (originalSel.equals(selection)) {
				bootstrapSelection = originalSel;
				return part;
			}
		}
		return null;
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
			site.getPage().addPostSelectionListener(this);
			super.init(site);
	}

	@Override
	public void saveState(IMemento memento) {
		// close all but the primary/parent property sheet on shutdown
		IViewSite viewSite = getViewSite();
		String secondaryId = viewSite.getSecondaryId();
		if (null == secondaryId) {
			super.saveState(memento);
		} else if (viewSite.getWorkbenchWindow().isClosing()) {
			viewSite.getPage().hideView(this);
		}
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		// Don't interfere with other property views
		if (part == null) {
			return false;
		}
		IWorkbenchPartSite site = part.getSite();
		if (site == null) {
			return false;
		}
		String partID = site.getId();
		boolean isPropertyView = getSite().getId().equals(partID);
		return !isPinned() && !isPropertyView && !isViewIgnored(partID);
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(currentPart)) {
			if (isPinned()) {
				pinPropertySheetAction.setChecked(false);
			}
			currentSelection = null;
			currentPart = null;
		}
		super.partClosed(part);
	}

	@Override
	protected void partVisible(IWorkbenchPart part) {
		super.partVisible(part);
		if (part == this) {
			wasHidden = false;
			if (selectionUpdatePending) {
				showSelectionAndDescription();
			}
		}
	}

	@Override
	protected void partHidden(IWorkbenchPart part) {
		if (part == this) {
			wasHidden = true;
		}
		// Explicitly ignore parts becoming hidden as this
		// can cause issues when the Property View is maximized
		// See bug 325743 for more details
	}

	/**
	 * The <code>PropertySheet</code> implementation of this <code>IPartListener</code>
	 * method first sees if the active part is an <code>IContributedContentsView</code>
	 * adapter and if so, asks it for its contributing part.
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part == this) {
			wasHidden = false;
			super.partActivated(part);
			if (selectionUpdatePending) {
				showSelectionAndDescription();
			}
			return;
		}

		if (!isImportant(part)) {
			return;
		}

		IContributedContentsView view = Adapters.adapt(part, IContributedContentsView.class);
		IWorkbenchPart source = null;
		if (view != null) {
			source = view.getContributingPart();
		}

		if (source != null && !isImportant(source)) {
			return;
		} else if (source == null) {
			source = part;
		}

		if (wasHidden) {
			IWorkbenchPartSite site = getSite();
			IWorkbenchPage page = site.getPage();
			IViewPart[] stack = page.getViewStack(this);
			if (stack != null) {
				for (IViewPart vPart : stack) {
					if (vPart == source) {
						// don't react on activation of (contributing) parts from same stack,
						// see bug 485154 and 530131.
						return;
					}
				}
			}
		}

		super.partActivated(source);

		if (currentPart == null && bootstrapSelection != null) {
			// When the view is first opened, pass the selection to the page
			currentSelection = bootstrapSelection;
			bootstrapSelection = null;
			selectionUpdatePending = true;
		} else {
			// reset the selection (to allow selectionChanged() accept part change for empty
			// selections)
			currentSelection = null;
		}
		currentPart = part;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		// we ignore selection if we are hidden OR selection is coming from
		// another source as the last one
		if (part == null || !part.equals(currentPart)) {
			return;
		}

		if (isPinned()) {
			currentPart = part;
			currentSelection = sel;
			needsUpdate = true;
			return;
		}

		// we ignore null selection, or if we are pinned, or our own selection
		// or same selection
		if (sel == null || isPinned() || (!needsUpdate && sel.equals(currentSelection))) {
			return;
		}

		currentSelection = sel;
		needsUpdate = false;

		if (wasHidden) {
			selectionUpdatePending = true;
			return;
		}

		// pass the selection to the page
		showSelectionAndDescription();
	}

	private void updateContentDescription() {
		if (isPinned() && currentPart != null) {
			setContentDescription(NLS.bind(PropertiesMessages.Selection_description, currentPart.getTitle()));
		} else {
			setContentDescription(""); //$NON-NLS-1$
		}
		// since our selection changes, our dirty state might change too
		firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
	}

	private void showSelectionAndDescription() {
		selectionUpdatePending = false;
		if (currentPart == null || currentSelection == null) {
			return;
		}
		IPage page = getCurrentPage();
		if (page instanceof ISelectionListener) {
			((ISelectionListener) page).selectionChanged(currentPart, currentSelection);
		}
		updateContentDescription();
	}

	/**
	 * Defines the dirty state indication behavior of the {@link PropertySheet}
	 * instance for the current tracked part if it is a {@link ISaveablePart}
	 * instance or provides an adapter to {@link ISaveablePart}.
	 * <p>
	 * Default return value is {@code false} - the Properties view will not show
	 * the '*' sign if the tracked part is dirty.
	 * <p>
	 * This behavior can be changed by either contributing custom
	 * {@link IPropertySheetPage} to the tracked part, or providing
	 * {@link ISecondarySaveableSource} adapter by the tracked part or by
	 * contributing {@link ISecondarySaveableSource} adapter to the
	 * {@link PropertySheet} class.
	 * <p>
	 * Strategy for the search is going from the smallest scope to the global
	 * scope, searching for the first {@link ISecondarySaveableSource} adapter.
	 * <p>
	 * The current page is asked for the {@link ISecondarySaveableSource}
	 * adapter first, if the adapter is not defined, the current tracked part is
	 * asked for it, and finally the platform adapter manager is consulted. The
	 * first adapter found in the steps above defines the return value of this
	 * method.
	 * <p>
	 * If the contributed page wants change the behavior The page must implement
	 * {@link IAdaptable} and return adapter to
	 * {@link ISecondarySaveableSource}.
	 *
	 * @return returns {@code false} if the dirty state indication behavior is
	 *         not desired.
	 * @since 3.9
	 */
	@Override
	public boolean isDirtyStateSupported() {
		if (currentPart == null) {
			return false;
		}
		// first: ask page if we should show dirty state
		ISecondarySaveableSource source = getAdapter(ISecondarySaveableSource.class);
		if (source != null && source != this) {
			return source.isDirtyStateSupported();
		}
		// second: ask the tracked part if the part provides the adapter;
		// platform adapter manager is asked in the last step
		source = Adapters.adapt(currentPart, ISecondarySaveableSource.class);
		if (source != null && source != this) {
			return source.isDirtyStateSupported();
		}

		// TODO delegate to default implementation if bug 490988 is fixed
		// return ISecondarySaveableSource.super.isDirtyStateIndicationSupported();
		return false;
	}

	/**
	 * The <code>PropertySheet</code> implementation of this
	 * <code>PageBookView</code> method handles the <code>ISaveablePart</code>
	 * adapter case by calling <code>getSaveablePart()</code>.
	 *
	 * @since 3.2
	 */
	@Override
	protected <T> T getViewAdapter(Class<T> key) {
		if (ISaveablePart.class.equals(key)) {
			return key.cast(getSaveablePart());
		}
		return super.getViewAdapter(key);
	}

	/**
	 * Returns an <code>ISaveablePart</code> that delegates to the source part
	 * for the current page if it implements <code>ISaveablePart</code>, or
	 * <code>null</code> otherwise.
	 *
	 * @return an <code>ISaveablePart</code> or <code>null</code>
	 * @since 3.2
	 */
	protected ISaveablePart getSaveablePart() {
		IWorkbenchPart part = getCurrentContributingPart();
		if (part instanceof ISaveablePart) {
			return (ISaveablePart) part;
		}
		return null;
	}

	/**
	 * @return whether this property sheet is currently pinned
	 * @since 3.4
	 */
	public boolean isPinned() {
		return pinPropertySheetAction != null && pinPropertySheetAction.isChecked();
	}

	@Override
	public ShowInContext getShowInContext() {
		return new PropertyShowInContext(currentPart, currentSelection);
	}

	@Override
	public boolean show(ShowInContext aContext) {
		if (!isPinned()
				&& aContext instanceof PropertyShowInContext context) {
			IWorkbenchPart part = context.getPart();
			if (part != null) {
				partActivated(part);
				selectionChanged(part, context.getSelection());
				return true;
			}
		}
		return false;
	}

	/***
	 * @param pinned Whether this sheet should be pinned
	 * @since 3.4
	 */
	public void setPinned(boolean pinned) {
		pinPropertySheetAction.setChecked(pinned);
		updateContentDescription();
	}

	private HashSet<String> getIgnoredViews() {
		if (ignoredViews == null) {
			ignoredViews = new HashSet<>();
			IExtensionRegistry registry = RegistryFactory.getRegistry();
			IExtensionPoint ep = registry.getExtensionPoint(EXT_POINT);
			if (ep != null) {
				IExtension[] extensions = ep.getExtensions();
				for (IExtension extension : extensions) {
					IConfigurationElement[] elements = extension.getConfigurationElements();
					for (IConfigurationElement element : elements) {
						if ("excludeSources".equalsIgnoreCase(element.getName())) { //$NON-NLS-1$
							String id = element.getAttribute("id"); //$NON-NLS-1$
							if (id != null) {
								ignoredViews.add(id);
							}
						}
					}
				}
			}
		}
		return ignoredViews;
	}

	private boolean isViewIgnored(String partID) {
		return getIgnoredViews().contains(partID);
	}

	@Override
	public void added(IExtension[] extensions) {
		ignoredViews = null;
	}

	@Override
	public void added(IExtensionPoint[] extensionPoints) {
		ignoredViews = null;
	}

	@Override
	public void removed(IExtension[] extensions) {
		ignoredViews = null;
	}

	@Override
	public void removed(IExtensionPoint[] extensionPoints) {
		ignoredViews = null;
	}

}
