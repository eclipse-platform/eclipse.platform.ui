/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.internal.provisional.action;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Extends <code>ToolBarManager</code> to implement <code>IToolBarManager2</code>.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 *
 * @since 3.2
 */
public class ToolBarManager2 extends ToolBarManager implements IToolBarManager2 {

	/**
	 * A collection of objects listening to changes to this manager. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient ListenerList<IPropertyChangeListener> listenerList = null;

	/**
	 * Creates a new tool bar manager with the default SWT button style. Use the
	 * <code>createControl</code> method to create the tool bar control.
	 */
	public ToolBarManager2() {
		super();
	}

	/**
	 * Creates a tool bar manager with the given SWT button style. Use the
	 * <code>createControl</code> method to create the tool bar control.
	 *
	 * @param style
	 *            the tool bar item style
	 * @see org.eclipse.swt.widgets.ToolBar for valid style bits
	 */
	public ToolBarManager2(int style) {
		super(style);
	}

	/**
	 * Creates a tool bar manager for an existing tool bar control. This manager
	 * becomes responsible for the control, and will dispose of it when the
	 * manager is disposed.
	 *
	 * @param toolbar
	 *            the tool bar control
	 */
	public ToolBarManager2(ToolBar toolbar) {
		super(toolbar);
	}

	@Override
	public Control createControl2(Composite parent) {
		return createControl(parent);
	}

	@Override
	public Control getControl2() {
		return getControl();
	}

	@Override
	public int getItemCount() {
		ToolBar toolBar = getControl();
		if (toolBar == null || toolBar.isDisposed()) {
			return 0;
		}
		return toolBar.getItemCount();
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (listenerList == null) {
			listenerList = new ListenerList<>(ListenerList.IDENTITY);
		}

		listenerList.add(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (listenerList != null) {
			listenerList.remove(listener);

			if (listenerList.isEmpty()) {
				listenerList = null;
			}
		}
	}

	/**
	 * @return the listeners attached to this event manager.
	 * The listeners currently attached; may be empty, but never
	 * null.
	 */
	protected final Object[] getListeners() {
		final ListenerList<IPropertyChangeListener> list = listenerList;
		if (list == null) {
			return new Object[0];
		}

		return list.getListeners();
	}

	/*
	 * Notifies any property change listeners that a property has changed. Only
	 * listeners registered at the time this method is called are notified.
	 */
	private void firePropertyChange(final PropertyChangeEvent event) {
		final Object[] list = getListeners();
		for (Object element : list) {
			((IPropertyChangeListener) element).propertyChange(event);
		}
	}

	/*
	 * Notifies any property change listeners that a property has changed. Only
	 * listeners registered at the time this method is called are notified. This
	 * method avoids creating an event object if there are no listeners
	 * registered, but calls firePropertyChange(PropertyChangeEvent) if there are.
	 */
	private void firePropertyChange(final String propertyName,
			final Object oldValue, final Object newValue) {
		if (listenerList != null) {
			firePropertyChange(new PropertyChangeEvent(this, propertyName,
					oldValue, newValue));
		}
	}

	@Override
	protected void relayout(ToolBar layoutBar, int oldCount, int newCount) {
		super.relayout(layoutBar, oldCount, newCount);
		firePropertyChange(PROP_LAYOUT, Integer.valueOf(oldCount), Integer.valueOf(newCount));
	}
}
