/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.*;

public class KeyTable {

	public interface KeyTableListener {
		void keyPressed(int key);
	}
	
 	private Shell shell;
	private int[] keys;
	private Menu menu;
	private MenuItem menuItem;

	public KeyTable(Shell shell) {
		this.shell = shell;
		initMenu();
	}

	public void initMenu() {
		Menu parent = shell.getMenuBar();
		
		if (parent == null || parent.getItemCount() < 1)
			throw new SWTException();
			
		MenuItem parentItem = parent.getItem(parent.getItemCount() - 1);
		final Menu child = parentItem.getMenu();
		
		if (child == null)
			throw new SWTException();
				
		menuItem = new MenuItem(child, SWT.CASCADE,0);
		menuItem.setMenu(menu = new Menu(menuItem));
		
		child.addListener(SWT.Show, new Listener () {
			public void handleEvent(Event event) {
				if(menuItem == null || menuItem.isDisposed())
					return;
				menuItem.setMenu(null);
				menuItem.dispose();
			}
		});
		
		child.addListener(SWT.Hide, new Listener () {
			public void handleEvent(Event event) {
				//It seems that we are getting this event twice
				if(menuItem == null || menuItem.isDisposed()) {
					menuItem = new MenuItem(child, SWT.CASCADE,0);
					menuItem.setMenu(menu);
				}
			}
		});	
	}

	private KeyTableListener keyTableListener;

	public void addKeyTableListener(KeyTableListener keyTableListener) {
		this.keyTableListener = keyTableListener;
		// TBD: add listener to list
	}

	public void removeKeyTableListener(KeyTableListener keyTableListener) {
		// TBD: remove listener from list
	}

	void handleEvent(Event event) {
		MenuItem eventMenuItem = (MenuItem) event.widget;
		int key = eventMenuItem.getAccelerator();
		// TBD: send key to all listeners
		if (keyTableListener != null) 	
			keyTableListener.keyPressed(key);
	}

	public int[] getKeys() {
		if (keys == null)
			return null;
		else {
			int[] keys = new int[this.keys.length];
			System.arraycopy(this.keys, 0, keys, 0, this.keys.length);
			return keys;
		}
	}

	public void setKeys(final int[] keys) {
		if (keys == null)
			this.keys = null;
		else 
			System.arraycopy(keys, 0, this.keys = new int[keys.length], 0, keys.length);
			
		if (menu != null) {
			menu.dispose();
			menu = null;
		}

		// Arrays.sort(keys);		
		if(menuItem == null || menuItem.isDisposed()) {
			initMenu();
		} else {	
			menu = new Menu(menuItem);
			menuItem.setMenu(menu);
		}
		
		for (int i = 0; i < keys.length; i++) {
			final int key = keys[i];
			MenuItem keyMenuItem = new MenuItem(menu, SWT.PUSH);
			keyMenuItem.setAccelerator(key);
			
			//keyMenuItem.setText(Action.convertAccelerator(key));
			
			keyMenuItem.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					KeyTable.this.handleEvent(event);
				}
			});
		}
	}
}
