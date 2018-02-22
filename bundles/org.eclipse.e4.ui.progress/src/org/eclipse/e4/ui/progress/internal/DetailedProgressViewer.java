/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.e4.ui.progress.IProgressService;
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

/**
 * The DetailedProgressViewer is a viewer that shows the details of all in
 * progress job or jobs that are finished awaiting user input.
 *
 * @since 3.2
 *
 */
public class DetailedProgressViewer extends AbstractProgressViewer {

	//Maximum number of entries to display so that the view does not flood the UI with events
	private static final int MAX_DISPLAYED = 20;

	Composite control;

	private ScrolledComposite scrolled;

	private Composite noEntryArea;

	private IProgressService progressService;

	private FinishedJobs finishedJobs;

	/**
	 * Create a new instance of the receiver with a control that is a child of
	 * parent with style style.
	 *
	 * @param parent
	 * @param style
	 */
	public DetailedProgressViewer(Composite parent, int style,
	        IProgressService progressService, FinishedJobs finishedJobs) {
		this.progressService = progressService;
		this.finishedJobs = finishedJobs;

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

			@Override
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
			@Override
			public void controlMoved(ControlEvent e) {
				updateVisibleItems();

			}

			@Override
			public void controlResized(ControlEvent e) {
				updateVisibleItems();
			}
		});

		// TODO E4 - missing e4 replacement
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(control,
		// IWorkbenchHelpContextIds.RESPONSIVE_UI);

		scrolled.setContent(control);
		hookControl(control);

		noEntryArea = new Composite(scrolled, SWT.NONE);
		noEntryArea.setLayout(new GridLayout());
		noEntryArea.setBackground(noEntryArea.getDisplay()
				.getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		Text noEntryLabel = new Text(noEntryArea, SWT.SINGLE);
		noEntryLabel.setText(ProgressMessages.ProgressView_NoOperations);
		noEntryLabel.setBackground(noEntryArea.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		GridData textData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		noEntryLabel.setLayoutData(textData);
		noEntryLabel.setEditable(false);

		// TODO E4 - missing e4 replacement
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(noEntryLabel,
		//		IWorkbenchHelpContextIds.RESPONSIVE_UI);

	}

	@Override
	public void add(Object[] elements) {
		ViewerComparator sorter = getComparator();

		// Use a Set in case we are getting something added that exists
		Set<Object> newItems = new HashSet<>(elements.length);

		Control[] existingChildren = control.getChildren();
		for (Control element : existingChildren) {
			if (element.getData() != null)
				newItems.add(element.getData());
		}

		for (Object element : elements) {
			if (element != null)
				newItems.add(element);
		}

		JobTreeElement[] infos = new JobTreeElement[newItems.size()];
		newItems.toArray(infos);

		if (sorter != null) {
			sorter.sort(this, infos);
		}

		// Update with the new elements to prevent flash
		for (Control element : existingChildren) {
			((ProgressInfoItem) element).dispose();
		}

		int totalSize = Math.min(newItems.size(), MAX_DISPLAYED);

		for (int i = 0; i < totalSize; i++) {
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
				info, progressService, finishedJobs);

		item.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
				updateVisibleProgressItems(item);
			}

			@Override
			public void controlResized(ControlEvent e) {
				updateVisibleProgressItems(item);
			}
		});

		item.setIndexListener(new ProgressInfoItem.IndexListener() {
			@Override
			public void selectNext() {
				DetailedProgressViewer.this.selectNext(item);

			}

			@Override
			public void selectPrevious() {
				DetailedProgressViewer.this.selectPrevious(item);

			}

			@Override
			public void select() {

				Control[] children = control.getChildren();
				for (Control element : children) {
					ProgressInfoItem child = (ProgressInfoItem) element;
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

	@Override
	protected Widget doFindInputItem(Object element) {
		return null;
	}

	@Override
	protected Widget doFindItem(Object element) {
		Control[] existingChildren = control.getChildren();
		for (Control controlElement : existingChildren) {
			if (controlElement.isDisposed()
					|| controlElement.getData() == null) {
				continue;
			}
			if (controlElement.getData().equals(element)) {
				return controlElement;
			}
		}
		return null;
	}

	@Override
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		if (usingElementMap()) {
			unmapElement(item);
		}
		item.dispose();
		add(new Object[] { element });
	}

	@Override
	public Control getControl() {
		return scrolled;
	}

	@Override
	protected List<Object> getSelectionFromWidget() {
		return new ArrayList<>(0);
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		refreshAll();
		updateForShowingProgress();
	}

	@Override
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

	@Override
	public void remove(Object[] elements) {

		for (Object element : elements) {
			JobTreeElement treeElement = (JobTreeElement) element;
			// Make sure we are not keeping this one
			if (finishedJobs.isKept(treeElement)) {
				Widget item = doFindItem(element);
				if (item != null) {
					((ProgressInfoItem) item).refresh();
				}

			} else {
				Widget item = doFindItem(treeElement);
				if (item == null) {
					// Is the parent showing?
					Object parent = treeElement.getParent();
					if (parent != null)
						item = doFindItem(parent);
				}
				if (item != null) {
					unmapElement(element);
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

	@Override
	public void reveal(Object element) {

	}

	@Override
	protected void setSelectionToWidget(@SuppressWarnings("rawtypes") List l, boolean reveal) {

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
			((ProgressInfoItem)children[0]).setButtonFocus();
		} else {
			noEntryArea.setFocus();
		}
	}

	/**
	 * Refresh everything as the root is being refreshed.
	 */
	private void refreshAll() {

		Object[] infos = getSortedChildren(getRoot());
		Control[] existingChildren = control.getChildren();

		for (Control element : existingChildren) {
			element.dispose();

		}

		int maxLength = Math.min(infos.length,MAX_DISPLAYED);
		// Create new ones if required
		for (int i = 0; i < maxLength; i++) {
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
		updateVisibleProgressItems(control.getChildren());
	}

	private void updateVisibleProgressItems(Control... progressInfoItems) {
		int top = scrolled.getOrigin().y;
		int bottom = top + scrolled.getParent().getBounds().height;
		for (Control element : progressInfoItems) {
			ProgressInfoItem item = (ProgressInfoItem) element;
			item.setDisplayed(top, bottom);

		}
	}

	public ProgressInfoItem[] getProgressInfoItems() {
		Control[] children = control.getChildren();
		ProgressInfoItem[] progressInfoItems = new ProgressInfoItem[children.length];
		System.arraycopy(children, 0, progressInfoItems, 0, children.length);
		return progressInfoItems;
	}

}
