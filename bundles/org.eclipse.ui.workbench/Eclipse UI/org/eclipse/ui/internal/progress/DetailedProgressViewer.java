package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * The DetailedProgressViewer is a viewer that shows the details of all in
 * progress job or jobs that are finished awaiting user input.
 * 
 * @since 3.2
 * 
 */
public class DetailedProgressViewer extends AbstractProgressViewer {

	Composite control;

	/**
	 * Create a new instance of the receiver with a control that is a child of
	 * parent with style style.
	 * 
	 * @param parent
	 * @param style
	 */
	public DetailedProgressViewer(Composite parent, int style) {
		control = new Composite(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		control.setLayout(layout);
		control.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AbstractProgressViewer#add(java.lang.Object[])
	 */
	public void add(Object[] elements) {
		ViewerSorter sorter = getSorter();
		ArrayList newItems = new ArrayList(control.getChildren().length
				+ elements.length);

		Control[] existingChildren = control.getChildren();
		for (int i = 0; i < existingChildren.length; i++) {
			newItems.add(existingChildren[i].getData());
		}

		for (int i = 0; i < elements.length; i++) {
			newItems.add(elements[i]);
		}

		JobInfo[] infos = new JobInfo[newItems.size()];
		newItems.toArray(infos);

		if (sorter != null) {
			sorter.sort(this, infos);
		}

		// Update with the new elements to prevent flash
		for (int i = 0; i < existingChildren.length; i++) {
			((JobInfoItem) existingChildren[i]).remap(infos[i]);
			((JobInfoItem) existingChildren[i]).setColor(i);
		}

		for (int i = existingChildren.length; i < newItems.size(); i++) {
			JobInfoItem item = new JobInfoItem(control, SWT.NONE, infos[i]);
			item.setColor(i);
		}

		control.layout(true);
	}

	/**
	 * Clear all the children.
	 */
	public void clearAll() {
		Control[] children = control.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
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
			if(existingChildren[i].isDisposed() || existingChildren[i].getData() == null)
				continue;
			JobInfo info = (JobInfo) existingChildren[i].getData();
			if (info.equals(element))
				return existingChildren[i];
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
			mapElement(element, item);
		}
		((JobInfoItem) item).remap((JobInfo) element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
	 */
	protected List getSelectionFromWidget() {
		return new ArrayList(0);
	}

	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	protected void internalRefresh(Object element) {
		if(element == null)
			return;
		
		if(element.equals(getRoot()))
			refreshAll();
		Widget widget = findItem(element);
		if (widget == null)
			return;
		((JobInfoItem) widget).refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AbstractProgressViewer#remove(java.lang.Object[])
	 */
	public void remove(Object[] elements) {

		for (int i = 0; i < elements.length; i++) {
			Widget item = doFindItem(elements[i]);
			if (item != null) {
				unmapElement(elements[i]);
				item.dispose();
			}
		}

		control.layout(true);
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

	}

	/**
	 * Refresh everything as the root is being refreshed. 
	 */
	private void refreshAll() {
		
		Object[] infos = getSortedChildren(getRoot());
		Control[] existingChildren = control.getChildren();
		
		int reuseLength = Math.min(infos.length,existingChildren.length);
		
		// Update with the new elements to prevent flash
		for (int i = 0; i < reuseLength; i++) {
			JobInfoItem item = (JobInfoItem) existingChildren[i].getData();
			item.remap((JobInfo) infos[i]);
			item.setColor(i);
		}

		// Create new ones if required
		for (int i = existingChildren.length; i < infos.length; i++) {
			JobInfoItem item = new JobInfoItem(control, SWT.NONE, (JobInfo) infos[i]);
			item.setColor(i);
		}
		
		// Delete old ones if not
		for (int i = infos.length; i < existingChildren.length; i++) {
			existingChildren[i].dispose();
		}
		
	}
	
}
