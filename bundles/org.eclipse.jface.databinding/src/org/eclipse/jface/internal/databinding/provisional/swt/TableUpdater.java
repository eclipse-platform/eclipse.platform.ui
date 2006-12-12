/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 1.1
 * 
 */
public abstract class TableUpdater {

	private class UpdateRunnable implements Runnable, IChangeListener,
			DisposeListener {
		private TableItem item;

		private boolean dirty = false;

		private IObservable[] dependencies = new IObservable[0];

		UpdateRunnable(TableItem item) {
			this.item = item;
			item.addDisposeListener(this);
		}

		// Runnable implementation. This method runs at most once per repaint
		// whenever the
		// value gets marked as dirty.
		public void run() {
			if (theTable != null && !theTable.isDisposed() && item != null && !item.isDisposed()) {
				if (theTable.isVisible()) {
					int tableHeight = theTable.getClientArea().height;
					int numVisibleItems = tableHeight / theTable.getItemHeight();
					int indexOfItem = theTable.indexOf(item);
					int topIndex = theTable.getTopIndex();
					if (indexOfItem >= topIndex && indexOfItem <= topIndex+numVisibleItems) {
						updateIfNecessary();
						return;
					}
				}
				theTable.clear(theTable.indexOf(item));
			}
		}

		private void updateIfNecessary() {
			if (dirty) {
				dependencies = ObservableTracker.runAndMonitor(new Runnable() {
					public void run() {
						updateItem(item);
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
				SWTUtil.runOnce(theTable.getDisplay(), this);
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
					runnable = new UpdateRunnable((TableItem) e.item);
					e.item.setData(runnable);
					runnable.makeDirty();
				} else {
					runnable.updateIfNecessary();
				}
			}
		}

		// DisposeListener implementation
		public void widgetDisposed(DisposeEvent e) {
			TableUpdater.this.dispose();
		}

	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Table theTable;

	/**
	 * Creates an updator for the given control.
	 * 
	 * @param toUpdate
	 *            table to update
	 */
	public TableUpdater(Table toUpdate) {
		Assert.isLegal((toUpdate.getStyle() & SWT.VIRTUAL) != 0, "TableUpdater requires virtual table"); //$NON-NLS-1$
		theTable = toUpdate;

		theTable.addDisposeListener(privateInterface);
		theTable.addListener(SWT.SetData, privateInterface);
	}

	/**
	 * This is called automatically when the control is disposed. It may also be
	 * called explicitly to remove this updator from the control. Subclasses
	 * will normally extend this method to detach any listeners they attached in
	 * their constructor.
	 */
	public void dispose() {
		theTable.removeDisposeListener(privateInterface);
		theTable.removeListener(SWT.SetData, privateInterface);

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
	 */
	protected abstract void updateItem(TableItem item);

}
