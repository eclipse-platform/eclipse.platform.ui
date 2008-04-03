/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.swt;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * NON-API - This class can be used to update a table with automatic dependency
 * tracking.
 * 
 * @since 1.1
 * 
 * @noextend This class is not intended to be subclassed by clients. (We do
 *           encourage experimentation for non-production code and are
 *           interested in feedback though.)
 * 
 */
public abstract class TableUpdater {

	private class UpdateRunnable implements Runnable, IChangeListener,
			DisposeListener {
		private TableItem item;

		private boolean dirty = false;

		private IObservable[] dependencies = new IObservable[0];

		private final Object element;

		UpdateRunnable(TableItem item, Object element) {
			this.item = item;
			this.element = element;
			item.addDisposeListener(this);
		}

		// Runnable implementation. This method runs at most once per repaint
		// whenever the
		// value gets marked as dirty.
		public void run() {
			if (table != null && !table.isDisposed() && item != null
					&& !item.isDisposed()) {
				if (table.isVisible()) {
					int tableHeight = table.getClientArea().height;
					int numVisibleItems = tableHeight / table.getItemHeight();
					int indexOfItem = table.indexOf(item);
					int topIndex = table.getTopIndex();
					if (indexOfItem >= topIndex
							&& indexOfItem <= topIndex + numVisibleItems) {
						updateIfNecessary(indexOfItem);
						return;
					}
				}
				table.clear(table.indexOf(item));
			}
		}

		private void updateIfNecessary(final int indexOfItem) {
			if (dirty) {
				dependencies = ObservableTracker.runAndMonitor(new Runnable() {
					public void run() {
						updateItem(indexOfItem, item, element);
					}
				}, this, null);
				dirty = false;
			}
		}

		// IChangeListener implementation (listening to the ComputedValue)
		public void handleChange(ChangeEvent event) {
			// Whenever this updator becomes dirty, schedule the run() method
			makeDirty();
		}

		protected final void makeDirty() {
			if (!dirty) {
				dirty = true;
				stopListening();
				SWTUtil.runOnce(table.getDisplay(), this);
			}
		}

		private void stopListening() {
			// Stop listening for dependency changes
			for (int i = 0; i < dependencies.length; i++) {
				IObservable observable = dependencies[i];

				observable.removeChangeListener(this);
			}
		}

		// DisposeListener implementation
		public void widgetDisposed(DisposeEvent e) {
			stopListening();
			dependencies = null;
			item = null;
		}
	}

	private class PrivateInterface implements Listener, DisposeListener {

		// Listener implementation
		public void handleEvent(Event e) {
			if (e.type == SWT.SetData) {
				UpdateRunnable runnable = (UpdateRunnable) e.item.getData();
				if (runnable == null) {
					runnable = new UpdateRunnable((TableItem) e.item, list.get(e.index));
					e.item.setData(runnable);
					runnable.makeDirty();
				} else {
					runnable.updateIfNecessary(e.index);
				}
			}
		}

		// DisposeListener implementation
		public void widgetDisposed(DisposeEvent e) {
			TableUpdater.this.dispose();
		}

	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Table table;

	private IListChangeListener listChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			ListDiffEntry[] differences = event.diff.getDifferences();
			for (int i = 0; i < differences.length; i++) {
				ListDiffEntry entry = differences[i];
				if (entry.isAddition()) {
					TableItem item = new TableItem(table, SWT.NONE, entry
							.getPosition());
					UpdateRunnable updateRunnable = new UpdateRunnable(item, entry.getElement());
					item.setData(updateRunnable);
					updateRunnable.makeDirty();
				} else {
					table.getItem(entry.getPosition()).dispose();
				}
			}
		}
	};

	private IObservableList list;

	/**
	 * Creates an updator for the given control.
	 * 
	 * @param table
	 *            table to update
	 * @param list
	 * @since 1.2
	 */
	public TableUpdater(Table table, IObservableList list) {
		this.table = table;
		this.list = list;
		Assert.isLegal((table.getStyle() & SWT.VIRTUAL) != 0,
				"TableUpdater requires virtual table"); //$NON-NLS-1$

		table.setItemCount(list.size());
		list.addListChangeListener(listChangeListener);

		table.addDisposeListener(privateInterface);
		table.addListener(SWT.SetData, privateInterface);
	}

	/**
	 * This is called automatically when the control is disposed. It may also be
	 * called explicitly to remove this updator from the control. Subclasses
	 * will normally extend this method to detach any listeners they attached in
	 * their constructor.
	 */
	public void dispose() {
		table.removeDisposeListener(privateInterface);
		table.removeListener(SWT.SetData, privateInterface);
		list.removeListChangeListener(listChangeListener);
		table = null;
		list = null;
	}

	/**
	 * Updates the control. This method will be invoked once after the updator
	 * is created, and once before any repaint during which the control is
	 * visible and dirty.
	 * 
	 * <p>
	 * Subclasses should overload this method to provide any code that changes
	 * the appearance of the widget.
	 * </p>
	 * 
	 * @param index
	 * @param item
	 *            the item to update
	 * @param element
	 * @since 1.2
	 */
	protected abstract void updateItem(int index, TableItem item, Object element);

}
