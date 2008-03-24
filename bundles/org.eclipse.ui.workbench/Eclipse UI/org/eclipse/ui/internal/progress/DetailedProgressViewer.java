/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;

/**
 * The DetailedProgressViewer is a viewer that shows the details of all in
 * progress job or jobs that are finished awaiting user input.
 * 
 * @since 3.2
 * 
 */
public class DetailedProgressViewer extends AbstractProgressViewer {

	Composite control;

	private ScrolledComposite scrolled;

	private Composite noEntryArea;

	/**
	 * Create a new instance of the receiver with a control that is a child of
	 * parent with style style.
	 * 
	 * @param parent
	 * @param style
	 */
	public DetailedProgressViewer(Composite parent, int style) {
		scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | style);
		int height = JFaceResources.getDefaultFont().getFontData()[0]
				.getHeight();
		scrolled.getVerticalBar().setIncrement(height * 2);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);

		control = new Composite(scrolled, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		control.setLayout(layout);
		control.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));

		control.addFocusListener(new FocusAdapter() {

			private boolean settingFocus = false;

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				if (!settingFocus) {
					// Prevent new focus events as a result this update
					// occurring
					settingFocus = true;
					setFocus();
					settingFocus = false;
				}
			}
		});

		control.addControlListener(new ControlListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlMoved(ControlEvent e) {
				updateVisibleItems();

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
			 */
			public void controlResized(ControlEvent e) {
				updateVisibleItems();
			}
		});

		PlatformUI.getWorkbench().getHelpSystem().setHelp(control,
				IWorkbenchHelpContextIds.RESPONSIVE_UI);

		scrolled.setContent(control);
		hookControl(control);

		noEntryArea = new Composite(scrolled, SWT.NONE);
		noEntryArea.setLayout(new GridLayout());

		Text noEntryLabel = new Text(noEntryArea, SWT.SINGLE);
		noEntryLabel.setText(ProgressMessages.ProgressView_NoOperations);
		noEntryLabel.setBackground(noEntryArea.getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		GridData textData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		noEntryLabel.setLayoutData(textData);
		noEntryLabel.setEditable(false);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(noEntryLabel,
				IWorkbenchHelpContextIds.RESPONSIVE_UI);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AbstractProgressViewer#add(java.lang.Object[])
	 */
	public void add(Object[] elements) {
		ViewerComparator sorter = getComparator();
		
		//Use a Set in case we are getting something added that exists
		Set newItems = new HashSet(elements.length);

		Control[] existingChildren = control.getChildren();
		for (int i = 0; i < existingChildren.length; i++) {
			newItems.add(existingChildren[i].getData());
		}

		for (int i = 0; i < elements.length; i++) {
			newItems.add(elements[i]);
		}

		JobTreeElement[] infos = new JobTreeElement[newItems.size()];
		newItems.toArray(infos);

		if (sorter != null) {
			sorter.sort(this, infos);
		}

		// Update with the new elements to prevent flash
		for (int i = 0; i < existingChildren.length; i++) {
			((ProgressInfoItem) existingChildren[i]).dispose();
		}

		for (int i = 0; i < newItems.size(); i++) {
			ProgressInfoItem item = createNewItem(infos[i]);
			item.setColor(i);
		}

		control.layout(true);
		updateForShowingProgress();
	}

	/**
	 * Update for the progress being displayed.
	 */
	private void updateForShowingProgress() {
		if (control.getChildren().length > 0) {
			scrolled.setContent(control);
		} else {
			scrolled.setContent(noEntryArea);
		}
	}

	/**
	 * Create a new item for info.
	 * 
	 * @param info
	 * @return ProgressInfoItem
	 */
	private ProgressInfoItem createNewItem(JobTreeElement info) {
		final ProgressInfoItem item = new ProgressInfoItem(control, SWT.NONE,
				info);

		item.setIndexListener(new ProgressInfoItem.IndexListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.ProgressInfoItem.IndexListener#selectNext()
			 */
			public void selectNext() {
				DetailedProgressViewer.this.selectNext(item);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.ProgressInfoItem.IndexListener#selectPrevious()
			 */
			public void selectPrevious() {
				DetailedProgressViewer.this.selectPrevious(item);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.ProgressInfoItem.IndexListener#select()
			 */
			public void select() {

				Control[] children = control.getChildren();
				for (int i = 0; i < children.length; i++) {
					ProgressInfoItem child = (ProgressInfoItem) children[i];
					if (!item.equals(child)) {
						child.selectWidgets(false);
					}
				}
				item.selectWidgets(true);

			}
		});

		// Refresh to populate with the current tasks
		item.refresh();
		return item;
	}

	/**
	 * Select the previous item in the receiver.
	 * 
	 * @param item
	 */
	protected void selectPrevious(ProgressInfoItem item) {
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			ProgressInfoItem child = (ProgressInfoItem) children[i];
			if (item.equals(child)) {
				ProgressInfoItem previous;
				if (i == 0) {
					previous = (ProgressInfoItem) children[children.length - 1];
				} else {
					previous = (ProgressInfoItem) children[i - 1];
				}

				item.selectWidgets(false);
				previous.selectWidgets(true);
				return;
			}
		}
	}

	/**
	 * Select the next item in the receiver.
	 * 
	 * @param item
	 */
	protected void selectNext(ProgressInfoItem item) {
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			ProgressInfoItem child = (ProgressInfoItem) children[i];
			if (item.equals(child)) {
				ProgressInfoItem next;
				if (i == children.length - 1) {
					next = (ProgressInfoItem) children[0];
				} else {
					next = (ProgressInfoItem) children[i + 1];
				}
				item.selectWidgets(false);
				next.selectWidgets(true);

				return;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
	 */
	protected Widget doFindInputItem(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	protected Widget doFindItem(Object element) {
		Control[] existingChildren = control.getChildren();
		for (int i = 0; i < existingChildren.length; i++) {
			if (existingChildren[i].isDisposed()
					|| existingChildren[i].getData() == null) {
				continue;
			}
			if (existingChildren[i].getData().equals(element)) {
				return existingChildren[i];
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget,
	 *      java.lang.Object, boolean)
	 */
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		if (usingElementMap()) {
			unmapElement(item);
		}
		item.dispose();
		add(new Object[] { element });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return scrolled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
	 */
	protected List getSelectionFromWidget() {
		return new ArrayList(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
	 *      java.lang.Object)
	 */
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		refreshAll();
		updateForShowingProgress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	protected void internalRefresh(Object element) {
		if (element == null) {
			return;
		}

		if (element.equals(getRoot())) {
			refreshAll();
			return;
		}
		Widget widget = findItem(element);
		if (widget == null) {
			add(new Object[] { element });
			return;
		}
		((ProgressInfoItem) widget).refresh();

		// Update the minimum size
		Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		size.x += IDialogConstants.HORIZONTAL_SPACING;
		size.y += IDialogConstants.VERTICAL_SPACING;

		scrolled.setMinSize(size);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AbstractProgressViewer#remove(java.lang.Object[])
	 */
	public void remove(Object[] elements) {

		for (int i = 0; i < elements.length; i++) {
			JobTreeElement treeElement = (JobTreeElement) elements[i];
			// Make sure we are not keeping this one
			if (FinishedJobs.getInstance().isKept(treeElement)) {
				Widget item = doFindItem(elements[i]);
				if (item != null) {
					((ProgressInfoItem) item).refresh();
				}

			} else {
				Widget item = doFindItem(treeElement);
				if (item == null) {
					// Is the parent showing?
					Object parent = treeElement.getParent();
					if (parent != null )
						item = doFindItem(parent);
				}
				if (item != null) {
					unmapElement(elements[i]);
					item.dispose();
				}
			}
		}

		Control[] existingChildren = control.getChildren();
		for (int i = 0; i < existingChildren.length; i++) {
			ProgressInfoItem item = (ProgressInfoItem) existingChildren[i];
			item.setColor(i);
		}
		control.layout(true);
		updateForShowingProgress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
	 */
	public void reveal(Object element) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List,
	 *      boolean)
	 */
	protected void setSelectionToWidget(List l, boolean reveal) {

	}

	/**
	 * Cancel the current selection
	 * 
	 */
	public void cancelSelection() {

	}

	/**
	 * Set focus on the current selection.
	 * 
	 */
	public void setFocus() {
		Control[] children = control.getChildren();
		if (children.length > 0) {
			for (int i = 0; i < children.length; i++) {
				ProgressInfoItem item = (ProgressInfoItem) children[i];
				item.setButtonFocus();
				return;
			}
		} else
			noEntryArea.setFocus();
	}

	/**
	 * Refresh everything as the root is being refreshed.
	 */
	private void refreshAll() {

		Object[] infos = getSortedChildren(getRoot());
		Control[] existingChildren = control.getChildren();

		for (int i = 0; i < existingChildren.length; i++) {
			existingChildren[i].dispose();

		}
		// Create new ones if required
		for (int i = 0; i < infos.length; i++) {
			ProgressInfoItem item = createNewItem((JobTreeElement) infos[i]);
			item.setColor(i);
		}

		control.layout(true);
		updateForShowingProgress();

	}

	/**
	 * Set the virtual items to be visible or not depending on the displayed
	 * area.
	 */
	private void updateVisibleItems() {
		Control[] children = control.getChildren();
		int top = scrolled.getOrigin().y;
		int bottom = top + scrolled.getParent().getBounds().height;
		for (int i = 0; i < children.length; i++) {
			ProgressInfoItem item = (ProgressInfoItem) children[i];
			item.setDisplayed(top, bottom);

		}
	}

}
