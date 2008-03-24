/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Menu;

/**
 * NON-API - A MenuUpdater updates an SWT menu in response to changes in the model. By
 * wrapping a block of code in a MenuUpdater, clients can rely on the fact that
 * the block of code will be re-executed whenever anything changes in the model
 * that might affect its behavior.
 * 
 * <p>
 * MenuUpdaters only execute once their menus are shown. If something changes in
 * the model, the updater is flagged as dirty and it stops listening to the
 * model until the next time the menu is shown. If the menu is visible while the
 * model changes, it will be updated right away.
 * </p>
 * 
 * <p>
 * Clients should subclass this when copying information from the model to a
 * menu. Typical usage:
 * </p>
 * 
 * <ul>
 * <li>Override updateMenu. It should do whatever is necessary to display the
 * contents of the model in the menu.</li>
 * <li>In the constructor, attach listeners to the model. The listeners should
 * call markDirty whenever anything changes in the model that affects
 * updateMenu. Note: this step can be omitted when calling any method tagged
 * with "@TrackedGetter" since MenuUpdater will automatically attach a listener
 * to any object if a "@TrackedGetter" method is called in updateMenu.</li>
 * <li>(optional)Extend dispose() to remove any listeners attached in the
 * constructor</li>
 * </ul>
 * 
 * @since 1.1
 */
public abstract class MenuUpdater {
	
	private class PrivateInterface implements MenuListener, 
		DisposeListener, Runnable, IChangeListener {

		// DisposeListener implementation
		public void widgetDisposed(DisposeEvent e) {
			MenuUpdater.this.dispose();
		}
		
		// Runnable implementation. This method runs at most once per repaint whenever the
		// value gets marked as dirty.
		public void run() {
			if (theMenu != null && !theMenu.isDisposed() && theMenu.isVisible()) {
				updateIfNecessary();
			}
		}
		
		// IChangeListener implementation (listening to the ComputedValue)
		public void handleChange(ChangeEvent event) {
			// Whenever this updator becomes dirty, schedule the run() method 
			makeDirty();
		}

		public void menuHidden(MenuEvent e) {
			// do nothing
		}

		public void menuShown(MenuEvent e) {
			updateIfNecessary();
		}
		
	}
	
	private Runnable updateRunnable = new Runnable() {
		public void run() {
			updateMenu();
		}
	};
	
	private PrivateInterface privateInterface = new PrivateInterface();
	private Menu theMenu;
	private IObservable[] dependencies = new IObservable[0];
	private boolean dirty = false;
	
	/**
	 * Creates an updator for the given menu.  
	 * 
	 * @param toUpdate menu to update
	 */
	public MenuUpdater(Menu toUpdate) {
		theMenu = toUpdate;
		
		theMenu.addDisposeListener(privateInterface);
		theMenu.addMenuListener(privateInterface);
		makeDirty();
	}
	
	private void updateIfNecessary() {
		if (dirty) {
			dependencies = ObservableTracker.runAndMonitor(updateRunnable, privateInterface, null);
			dirty = false;
		}
	}

	/**
	 * This is called automatically when the menu is disposed. It may also
	 * be called explicitly to remove this updator from the menu. Subclasses
	 * will normally extend this method to detach any listeners they attached
	 * in their constructor.
	 */
	public void dispose() {
		theMenu.removeDisposeListener(privateInterface);
		theMenu.removeMenuListener(privateInterface);

		stopListening();
	}

	private void stopListening() {
		// Stop listening for dependency changes
		for (int i = 0; i < dependencies.length; i++) {
			IObservable observable = dependencies[i];
				
			observable.removeChangeListener(privateInterface);
		}
	}

	/**
	 * Updates the menu. This method will be invoked once after the
	 * updater is created, and once for any SWT.Show event if this 
	 * updater is marked as dirty at that time.
	 *  
	 * <p>
	 * Subclasses should overload this method to provide any code that 
	 * udates the menu.
	 * </p>
	 */
	protected abstract void updateMenu();
	
	/**
	 * Marks this updator as dirty. Causes the updateControl method to
	 * be invoked before the next time the control is repainted.
	 */
	protected final void makeDirty() {
		if (!dirty) {
			dirty = true;
			stopListening();
			SWTUtil.runOnce(theMenu.getDisplay(), privateInterface);
		}
	}
	
}
