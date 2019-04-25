/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 251424
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.swt;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

/**
 * NON-API - This class can be used to update a composite with automatic
 * dependency tracking.
 *
 * @param <E> type of the elements in the model list
 *
 * @since 1.1
 *
 */
public abstract class CompositeUpdater<E> {

	private class UpdateRunnable implements Runnable, IChangeListener {
		private Widget widget;
		E element;

		private boolean dirty = true;

		private IObservable[] dependencies = new IObservable[0];

		UpdateRunnable(Widget widget, E element) {
			this.widget = widget;
			this.element = element;
		}

		// Runnable implementation. This method runs at most once per repaint
		// whenever the
		// value gets marked as dirty.
		@Override
		public void run() {
			if (theComposite != null && !theComposite.isDisposed()
					&& widget != null && !widget.isDisposed()) {
				updateIfNecessary();
			}
		}

		private void updateIfNecessary() {
			if (dirty) {
				dependencies = ObservableTracker.runAndMonitor(() -> updateWidget(widget, element), this, null);
				dirty = false;
			}
		}

		// IChangeListener implementation (listening to any dependency)
		@Override
		public void handleChange(ChangeEvent event) {
			// Whenever this updator becomes dirty, schedule the run() method
			makeDirty();
		}

		protected final void makeDirty() {
			if (!dirty) {
				dirty = true;
				stopListening();
				if (!theComposite.isDisposed()) {
					SWTUtil.runOnce(theComposite.getDisplay(), this);
				}
			}
		}

		private void stopListening() {
			// Stop listening for dependency changes
			for (IObservable observable : dependencies) {
				observable.removeChangeListener(this);
			}
		}
	}

	private class LayoutRunnable implements Runnable {
		private boolean posted = false;
		private Set<Control> controlsToLayout = new HashSet<>();

		void add(Control toLayout) {
			controlsToLayout.add(toLayout);
			if (!posted) {
				posted = true;
				theComposite.getDisplay().asyncExec(this);
			}
		}

		@Override
		public void run() {
			posted = false;
			theComposite.getShell().layout(controlsToLayout.toArray(new Control[controlsToLayout.size()]));
			controlsToLayout.clear();
		}
	}

	private LayoutRunnable layoutRunnable = new LayoutRunnable();

	/**
	 * To be called from {@link #updateWidget(Widget, Object)} or
	 * {@link #createWidget(int)} if this updater's composite's layout may need to
	 * be updated.
	 *
	 * @param control
	 * @since 1.2
	 */
	protected void requestLayout(Control control) {
		layoutRunnable.add(control);
	}

	private class PrivateInterface implements DisposeListener, IListChangeListener<E> {
		// DisposeListener implementation
		@Override
		public void widgetDisposed(DisposeEvent e) {
			CompositeUpdater.this.dispose();
		}

		@Override
		public void handleListChange(ListChangeEvent<? extends E> event) {
			ListDiffEntry<? extends E>[] diffs = event.diff.getDifferences();
			for (ListDiffEntry<? extends E> listDiffEntry : diffs) {
				if (listDiffEntry.isAddition()) {
					createChild(listDiffEntry.getElement(), listDiffEntry.getPosition());
				} else {
					disposeWidget(listDiffEntry.getPosition());
				}
			}
			theComposite.layout();
		}

	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Composite theComposite;

	private IObservableList<? extends E> model;

	/**
	 * Creates an updater for the given control and list. For each element of
	 * the list, a child widget of the composite will be created using
	 * {@link #createWidget(int)}.
	 *
	 * @param toUpdate
	 *            composite to update
	 * @param model
	 *            an observable list to track
	 */
	public CompositeUpdater(Composite toUpdate, IObservableList<? extends E> model) {
		this.theComposite = toUpdate;
		this.model = model;

		model.addListChangeListener(privateInterface);
		theComposite.addDisposeListener(privateInterface);
		ObservableTracker.setIgnore(true);
		try {
			int index = 0;
			for (E element : CompositeUpdater.this.model) {
				createChild(element, index++);
			}
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	/**
	 * @param position
	 * @since 1.2
	 */
	protected void disposeWidget(int position) {
		theComposite.getChildren()[position].dispose();
	}

	/**
	 * This is called automatically when the control is disposed. It may also be
	 * called explicitly to remove this updator from the control. Subclasses
	 * will normally extend this method to detach any listeners they attached in
	 * their constructor.
	 */
	public void dispose() {
		theComposite.removeDisposeListener(privateInterface);
		model.removeListChangeListener(privateInterface);
	}

	/**
	 * Creates a new child widget for the target composite at the given index.
	 *
	 * <p>
	 * Subclasses should implement this method to provide the code that creates a
	 * child widget at a specific index. Note that
	 * {@link #updateWidget(Widget, Object)} will be called after this method
	 * returns. Only those properties of the widget that don't change over time
	 * should be set in this method.
	 * </p>
	 *
	 * @param index the at which to create the widget
	 * @return the widget
	 */
	protected abstract Widget createWidget(int index);

	/**
	 * Updates the given widget based on the element found in the model list.
	 * This method will be invoked once after the widget is created, and once
	 * before any repaint during which the control is visible and dirty.
	 *
	 * <p>
	 * Subclasses should implement this method to provide any code that changes
	 * the appearance of the widget.
	 * </p>
	 *
	 * @param widget
	 *            the widget to update
	 * @param element
	 *            the element associated with the widget
	 */
	protected abstract void updateWidget(Widget widget, E element);

	void createChild(E element, int index) {
		Widget newChild = createWidget(index);
		final UpdateRunnable updateRunnable = new UpdateRunnable(newChild, element);
		newChild.setData(updateRunnable);
		updateRunnable.updateIfNecessary();
	}

}
