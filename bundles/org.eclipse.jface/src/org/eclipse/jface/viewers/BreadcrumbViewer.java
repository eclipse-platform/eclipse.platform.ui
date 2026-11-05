/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
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
 *     Fabio Zadrozny - Bug 465666
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * A breadcrumb viewer shows a the parent chain of its input element in a list.
 * Each breadcrumb item of that list can be expanded and a sibling of the
 * element presented by the breadcrumb item can be selected.
 * <p>
 * Content providers for breadcrumb viewers must implement the
 * <code>ITreeContentProvider</code> interface.
 * </p>
 * <p>
 * Label providers for breadcrumb viewers must implement the
 * <code>ILabelProvider</code> interface.
 * </p>
 *
 * @since 3.41
 */
public abstract class BreadcrumbViewer extends StructuredViewer {

	private static final boolean IS_GTK = "gtk".equals(SWT.getPlatform()); //$NON-NLS-1$

	private final Composite fContainer;
	private final ArrayList<BreadcrumbItem> fBreadcrumbItems;
	private final ListenerList<MenuDetectListener> fMenuListeners;

	private Image fGradientBackground;
	private BreadcrumbItem fSelectedItem;
	private ILabelProvider fToolTipLabelProvider;
	private final IDialogSettings fDialogSettings = new DialogSettings("BreadcrumbViewer"); //$NON-NLS-1$

	/**
	 * Create a new <code>BreadcrumbViewer</code>.
	 * <p>
	 * Style is one of:
	 * <ul>
	 * <li>SWT.NONE</li>
	 * <li>SWT.VERTICAL</li>
	 * <li>SWT.HORIZONTAL</li>
	 * </ul>
	 *
	 * @param parent the container for the viewer
	 * @param style  the style flag used for this viewer
	 */
	public BreadcrumbViewer(Composite parent, int style) {
		fBreadcrumbItems = new ArrayList<>();
		fMenuListeners = new ListenerList<>();

		fContainer = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		fContainer.setLayoutData(layoutData);
		fContainer.addTraverseListener(e -> e.doit = true);
		fContainer.setBackgroundMode(SWT.INHERIT_DEFAULT);
		fContainer.setData("org.eclipse.e4.ui.css.id", "BreadcrumbComposite"); //$NON-NLS-1$ //$NON-NLS-2$

		hookControl(fContainer);

		int columns = 1000;
		if ((SWT.VERTICAL & style) != 0) {
			columns = 1;
		}

		GridLayout gridLayout = new GridLayout(columns, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		fContainer.setLayout(gridLayout);

		fContainer.addListener(SWT.Resize, event -> refresh());
	}

	/**
	 * Configure the given drop down viewer. The given input is used for the viewers
	 * input. Clients must at least set the label and the content provider for the
	 * viewer.
	 *
	 * @param viewer the viewer to configure
	 * @param input  the input for the viewer
	 */
	protected abstract void configureDropDownViewer(TreeViewer viewer, Object input);

	/**
	 * The tool tip to use for the tool tip labels. <code>null</code> if the viewers
	 * label provider should be used.
	 *
	 * @param toolTipLabelProvider the label provider for the tool tips or
	 *                             <code>null</code>
	 */
	public void setToolTipLabelProvider(ILabelProvider toolTipLabelProvider) {
		fToolTipLabelProvider = toolTipLabelProvider;
	}

	/*
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	@Override
	public Control getControl() {
		return fContainer;
	}

	/*
	 * @see org.eclipse.jface.viewers.StructuredViewer#getRoot()
	 */
	@Override
	protected Object getRoot() {
		if (fBreadcrumbItems.isEmpty())
			return null;

		return fBreadcrumbItems.get(0).getData();
	}

	/*
	 * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
	 */
	@Override
	public void reveal(Object element) {
		// all elements are always visible
	}

	/**
	 * Transfers the keyboard focus into the viewer.
	 */
	public void setFocus() {
		fContainer.setFocus();

		if (fSelectedItem != null) {
			fSelectedItem.setFocus(true);
		} else {
			if (fBreadcrumbItems.isEmpty())
				return;

			BreadcrumbItem item = fBreadcrumbItems.get(fBreadcrumbItems.size() - 1);
			if (item.getData() == null) {
				if (fBreadcrumbItems.size() < 2)
					return;

				item = fBreadcrumbItems.get(fBreadcrumbItems.size() - 2);
			}
			item.setFocus(true);
		}
	}

	/**
	 * @return true if any of the items in the viewer is expanded
	 */
	public boolean isDropDownOpen() {
		for (BreadcrumbItem item : fBreadcrumbItems) {
			if (item.isMenuShown())
				return true;
		}

		return false;
	}

	/**
	 * The shell used for the shown drop down or <code>null</code> if no drop down
	 * is shown at the moment.
	 *
	 * @return the drop downs shell or <code>null</code>
	 */
	public Shell getDropDownShell() {
		for (BreadcrumbItem item : fBreadcrumbItems) {
			if (item.isMenuShown())
				return item.getDropDownShell();
		}

		return null;
	}

	/**
	 * Returns the selection provider which provides the selection of the drop down
	 * currently opened or <code>null</code> if no drop down is open at the moment.
	 *
	 * @return the selection provider of the open drop down or <code>null</code>
	 */
	public ISelectionProvider getDropDownSelectionProvider() {
		for (BreadcrumbItem item : fBreadcrumbItems) {
			if (item.isMenuShown()) {
				return item.getDropDownSelectionProvider();
			}
		}

		return null;
	}

	/**
	 * Add the given listener to the set of listeners which will be informed when a
	 * context menu is requested for a breadcrumb item.
	 *
	 * @param listener the listener to add
	 */
	public void addMenuDetectListener(MenuDetectListener listener) {
		fMenuListeners.add(listener);
	}

	/**
	 * Remove the given listener from the set of menu detect listeners. Does nothing
	 * if the listener is not element of the set.
	 *
	 * @param listener the listener to remove
	 */
	public void removeMenuDetectListener(MenuDetectListener listener) {
		fMenuListeners.remove(listener);
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.StructuredViewer#assertContentProviderType(org.
	 * eclipse.jface.viewers.IContentProvider)
	 */
	@Override
	protected void assertContentProviderType(IContentProvider provider) {
		super.assertContentProviderType(provider);
		Assert.isTrue(provider instanceof ITreeContentProvider);
	}

	/*
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected void inputChanged(Object fInput, Object oldInput) {
		if (fContainer.isDisposed())
			return;

		disableRedraw();
		try {
			if (fBreadcrumbItems.size() > 0) {
				BreadcrumbItem last = fBreadcrumbItems.get(fBreadcrumbItems.size() - 1);
				last.setIsLastItem(false);
			}

			int lastIndex = buildItemChain(fInput);

			if (lastIndex > 0) {
				BreadcrumbItem last = fBreadcrumbItems.get(lastIndex - 1);
				last.setIsLastItem(true);
			}

			while (lastIndex < fBreadcrumbItems.size()) {
				BreadcrumbItem item = fBreadcrumbItems.remove(fBreadcrumbItems.size() - 1);
				if (item == fSelectedItem) {
					selectItem(null);
				}
				if (item.getData() != null)
					unmapElement(item.getData());
				item.dispose();
			}

			updateSize();
			fContainer.layout(true, true);
		} finally {
			enableRedraw();
		}
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
	 */
	@Override
	protected Widget doFindInputItem(Object element) {
		if (element == null)
			return null;

		if (element == getInput() || element.equals(getInput()))
			return doFindItem(element);

		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	@Override
	protected Widget doFindItem(Object element) {
		if (element == null)
			return null;

		for (BreadcrumbItem item : fBreadcrumbItems) {
			if (item.getData() == element || element.equals(item.getData()))
				return item;
		}

		return null;
	}

	/*
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.
	 * widgets.Widget, java.lang.Object, boolean)
	 */
	@Override
	protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
		if (widget instanceof BreadcrumbItem) {
			final BreadcrumbItem item = (BreadcrumbItem) widget;

			// remember element we are showing
			if (fullMap) {
				associate(element, item);
			} else {
				Object data = item.getData();
				if (data != null) {
					unmapElement(data, item);
				}
				item.setData(element);
				mapElement(element, item);
			}

			BreadcrumbViewerRow row = new BreadcrumbViewerRow(this, item);
			ViewerCell cell = row.getCell(0);

			((CellLabelProvider) getLabelProvider()).update(cell);

			item.refreshArrow();

			if (fToolTipLabelProvider != null) {
				item.setToolTip(fToolTipLabelProvider.getText(item.getData()));
			} else {
				item.setToolTip(cell.getText());
			}
		}
	}

	/*
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
	 */
	@Override
	protected List getSelectionFromWidget() {
		if (fSelectedItem == null)
			return Collections.EMPTY_LIST;

		if (fSelectedItem.getData() == null)
			return Collections.EMPTY_LIST;

		ArrayList<Object> result = new ArrayList<>();
		result.add(fSelectedItem.getData());
		return result;
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	@Override
	protected void internalRefresh(Object element) {

		disableRedraw();
		try {
			BreadcrumbItem item = (BreadcrumbItem) doFindItem(element);
			if (item == null) {
				for (BreadcrumbItem item1 : fBreadcrumbItems) {
					item1.refresh();
				}
			} else {
				item.refresh();
			}
			if (updateSize())
				fContainer.layout(true, true);
		} finally {
			enableRedraw();
		}
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.
	 * List, boolean)
	 */
	@Override
	protected void setSelectionToWidget(List l, boolean reveal) {
		BreadcrumbItem focusItem = null;

		for (BreadcrumbItem item : fBreadcrumbItems) {
			if (item.hasFocus())
				focusItem = item;

			item.setSelected(false);
		}

		if (l == null)
			return;

		for (Object element : l) {
			BreadcrumbItem item = (BreadcrumbItem) doFindItem(element);
			if (item != null) {
				item.setSelected(true);
				fSelectedItem = item;
				if (item == focusItem) {
					item.setFocus(true);
				}
			}
		}
	}

	/**
	 * Set a single selection to the given item. <code>null</code> to deselect all.
	 *
	 * @param item the item to select or <code>null</code>
	 */
	void selectItem(BreadcrumbItem item) {
		if (fSelectedItem != null)
			fSelectedItem.setSelected(false);

		fSelectedItem = item;
		setSelectionToWidget(getSelection(), false);

		if (item != null) {
			setFocus();
		} else {
			for (BreadcrumbItem listItem : fBreadcrumbItems) {
				listItem.setFocus(false);
			}
		}

		fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
	}

	/**
	 * Returns the item count.
	 *
	 * @return number of items shown in the viewer
	 */
	int getItemCount() {
		return fBreadcrumbItems.size();
	}

	/**
	 * Returns the item for the given item index.
	 *
	 * @param index the index of the item
	 * @return the item ad the given <code>index</code>
	 */
	BreadcrumbItem getItem(int index) {
		return fBreadcrumbItems.get(index);
	}

	/**
	 * Returns the index of the given item.
	 *
	 * @param item the item to search
	 * @return the index of the item or -1 if not found
	 */
	int getIndexOfItem(BreadcrumbItem item) {
		for (int i = 0, size = fBreadcrumbItems.size(); i < size; i++) {
			BreadcrumbItem pItem = fBreadcrumbItems.get(i);
			if (pItem == item)
				return i;
		}

		return -1;
	}

	/**
	 * Notifies all double click listeners.
	 */
	void fireDoubleClick() {
		fireDoubleClick(new DoubleClickEvent(this, getSelection()));
	}

	/**
	 * Notifies all open listeners.
	 */
	void fireOpen() {
		fireOpen(new OpenEvent(this, getSelection()));
	}

	/**
	 * The given element was selected from a drop down menu.
	 *
	 * @param element the selected element
	 */
	void fireMenuSelection(Object element) {
		fireOpen(new OpenEvent(this, new StructuredSelection(element)));
	}

	/**
	 * A context menu has been requested for the selected breadcrumb item.
	 *
	 * @param event the event issued the menu detection
	 */
	void fireMenuDetect(MenuDetectEvent event) {
		for (MenuDetectListener listener : fMenuListeners) {
			listener.menuDetected(event);
		}
	}

	/**
	 * Set selection to the next or previous element if possible.
	 *
	 * @param next <code>true</code> if the next element should be selected,
	 *             otherwise the previous one will be selected
	 */
	void doTraverse(boolean next) {
		if (fSelectedItem == null)
			return;

		int index = fBreadcrumbItems.indexOf(fSelectedItem);
		if (next) {
			if (index == fBreadcrumbItems.size() - 1) {
				BreadcrumbItem current = fBreadcrumbItems.get(index);

				ITreeContentProvider contentProvider = (ITreeContentProvider) getContentProvider();
				if (!contentProvider.hasChildren(current.getData()))
					return;

				current.openDropDownMenu();
				current.getDropDownShell().setFocus();
			} else {
				BreadcrumbItem nextItem = fBreadcrumbItems.get(index + 1);
				selectItem(nextItem);
			}
		} else {
			if (index == 1) {
				BreadcrumbItem root = fBreadcrumbItems.get(0);
				root.openDropDownMenu();
				root.getDropDownShell().setFocus();
			} else {
				selectItem(fBreadcrumbItems.get(index - 1));
			}
		}
	}

	/**
	 * Generates the parent chain of the given element.
	 *
	 * @param element element to build the parent chain for
	 * @return the first index of an item in fBreadcrumbItems which is not part of
	 *         the chain
	 */
	private int buildItemChain(Object element) {
		if (element == null)
			return 0;

		ITreeContentProvider contentProvider = (ITreeContentProvider) getContentProvider();
		Object parent = contentProvider.getParent(element);

		int index = buildItemChain(parent);

		BreadcrumbItem item;
		if (index < fBreadcrumbItems.size()) {
			item = fBreadcrumbItems.get(index);
			if (item.getData() != null)
				unmapElement(item.getData());
		} else {
			item = createItem();
			fBreadcrumbItems.add(item);
		}

		if (equals(element, item.getData())) {
			update(element, null);
		} else {
			item.setData(element);
			item.refresh();
		}
		if (parent == null) {
			// don't show the models root
			item.setDetailsVisible(false);
		}

		mapElement(element, item);

		return index + 1;
	}

	/**
	 * Creates and returns a new instance of a breadcrumb item.
	 *
	 * @return new instance of a breadcrumb item
	 */
	private BreadcrumbItem createItem() {
		BreadcrumbItem result = new BreadcrumbItem(this, fContainer);

		result.setLabelProvider((ILabelProvider) getLabelProvider());
		if (fToolTipLabelProvider != null) {
			result.setToolTipLabelProvider(fToolTipLabelProvider);
		} else {
			result.setToolTipLabelProvider((ILabelProvider) getLabelProvider());
		}
		result.setContentProvider((ITreeContentProvider) getContentProvider());

		return result;
	}

	/**
	 * Update the size of the items such that all items are visible, if possible.
	 *
	 * @return <code>true</code> if any item has changed, <code>false</code>
	 *         otherwise
	 */
	private boolean updateSize() {
		int width = fContainer.getClientArea().width;

		int currentWidth = getCurrentWidth();

		boolean requiresLayout = false;

		if (currentWidth > width) {
			int index = 0;
			while (currentWidth > width && index < fBreadcrumbItems.size() - 1) {
				BreadcrumbItem viewer = fBreadcrumbItems.get(index);
				if (viewer.isShowText()) {
					viewer.setShowText(false);
					currentWidth = getCurrentWidth();
					requiresLayout = true;
				}

				index++;
			}

		} else if (currentWidth < width) {

			int index = fBreadcrumbItems.size() - 1;
			while (currentWidth < width && index >= 0) {

				BreadcrumbItem viewer = fBreadcrumbItems.get(index);
				if (!viewer.isShowText()) {
					viewer.setShowText(true);
					currentWidth = getCurrentWidth();
					if (currentWidth > width) {
						viewer.setShowText(false);
						index = 0;
					} else {
						requiresLayout = true;
					}
				}

				index--;
			}
		}

		return requiresLayout;
	}

	/**
	 * Returns the current width of all items in the list.
	 *
	 * @return the width of all items in the list
	 */
	private int getCurrentWidth() {
		int result = 0;
		for (BreadcrumbItem viewer : fBreadcrumbItems) {
			result += viewer.getWidth();
		}

		return result;
	}

	/**
	 * Enables redrawing of the breadcrumb.
	 */
	private void enableRedraw() {
		if (IS_GTK) // flickers on GTK
			return;

		fContainer.setRedraw(true);
	}

	/**
	 * Disables redrawing of the breadcrumb.
	 *
	 * <p>
	 * <strong>A call to this method must be followed by a call to
	 * {@link #enableRedraw()}</strong>
	 * </p>
	 */
	private void disableRedraw() {
		if (IS_GTK) // flickers on GTK
			return;

		fContainer.setRedraw(false);
	}

	/*
	 * @see
	 * org.eclipse.jface.viewers.StructuredViewer#handleDispose(org.eclipse.swt.
	 * events.DisposeEvent)
	 *
	 */
	@Override
	protected void handleDispose(DisposeEvent event) {
		if (fGradientBackground != null) {
			fGradientBackground.dispose();
			fGradientBackground = null;
		}

		if (fToolTipLabelProvider != null) {
			fToolTipLabelProvider.dispose();
			fToolTipLabelProvider = null;
		}

		if (fBreadcrumbItems != null) {
			Iterator<BreadcrumbItem> iterator = fBreadcrumbItems.iterator();
			while (iterator.hasNext()) {
				BreadcrumbItem item = iterator.next();
				item.dispose();
			}
		}

		super.handleDispose(event);
	}

	/**
	 * Returns whether the given element can be opened directly.
	 * <p>
	 * Subclasses may override this method to define which elements can be opened.
	 * The default implementation returns {@code false}.
	 * </p>
	 *
	 * @param element the element to check
	 * @return {@code true} if the element can be opened directly, {@code false}
	 *         otherwise
	 */
	protected boolean isElementOpenable(Object element) {
		return false;
	}

	/**
	 * Creates the tree viewer used for the breadcrumb drop down.
	 * <p>
	 * Subclasses may override this method to provide a specialized
	 * {@link TreeViewer}. The default implementation creates a standard
	 * {@link TreeViewer}.
	 * </p>
	 *
	 * @param parent the parent composite
	 * @param style  the SWT style bits
	 * @return the tree viewer used for the drop down
	 */
	protected TreeViewer createDropDownViewer(Composite parent, int style) {
		return new TreeViewer(parent, style);
	}

	/**
	 * Opens the drop down menu of the parent breadcrumb item for the given element.
	 * Does nothing if the element is not shown in the breadcrumb or has no parent
	 * item.
	 *
	 * @param element the element whose parent drop down should be opened
	 */
	public void openParentDropDown(Object element) {
		BreadcrumbItem item = (BreadcrumbItem) doFindItem(element);
		if (item == null)
			return;

		int index = getIndexOfItem(item);
		if (index <= 0)
			return;

		BreadcrumbItem parent = getItem(index - 1);
		if (parent != null)
			parent.openDropDownMenu();
	}

	/**
	 * Returns the dialog settings used to store breadcrumb UI state.
	 * <p>
	 * Subclasses may override this method to provide persistent dialog settings.
	 * The default implementation returns non-persistent settings.
	 * </p>
	 *
	 * @return the dialog settings
	 */
	protected IDialogSettings getDialogSettings() {
		return fDialogSettings;
	}
}
