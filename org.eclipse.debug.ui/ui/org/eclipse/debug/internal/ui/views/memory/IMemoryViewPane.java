/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * Represent a view pane in the memory view.
 * 
 * This is an internal interface for mananging the view panes within the Memory View
 * @since 3.1
 */
public interface IMemoryViewPane {
	
	/**
	 * Create the view pane with the given parent composite, and pane
	 * id. 
	 * @param parent
	 * @param paneId
	 * @param label
	 * @return the control of the view pane
	 */
	public Control createViewPane(Composite parent, String paneId, String label);
	
	/**
	 * Restore view pane based on current selection from the debug view.
	 * Create memory blocks or renderings that currently exist in  the debug
	 * session.
	 */
	public void restoreViewPane();
	
	/**
	 * Dispose the view pane
	 */
	public void dispose();
	
	/**
	 * @return unique identifier of the view pane
	 */
	public String getId();
	
	/**
	 * @return array of actions to be contributed to the view pane's 
	 * acition bar.
	 */
	public IAction[] getActions();
	
	/**
	 * Add the given selection listener to the view pane.  The selection
	 * listener gets notified when the selection from the view pane
	 * has changed.
	 * @param listener
	 */
	public void addSelectionListener(ISelectionChangedListener listener);
	
	/**
	 * Remove the selection listener from the view pane. The listener
	 * will no longer get notified when selection is changed.
	 * @param listener
	 */
	public void removeSelctionListener(ISelectionChangedListener listener);
	
	/**
	 * @return the selection provider of the view pane
	 */
	public ISelectionProvider getSelectionProvider();
	
	/**
	 * @return the control for ths memory view pane
	 */
	public Control getControl();
	
	/**
	 * Called when the view pane becomes visible or hidden
	 * @param visible visibility state of teh view pane
	 */
	public void setVisible(boolean visible);
	
	/**
	 * @return if the view pane is currently visible
	 */
	public boolean isVisible();

}
