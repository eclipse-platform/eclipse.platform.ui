package org.eclipse.ui.internal;

import java.util.Arrays;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

public class AcceleratorMenu {

	private int[] accelerators;
	private Menu parent, menu;
	private MenuItem item;
	private Control focusControl;
	private SelectionListener selectionListener;
	private VerifyListener verifyListener;
	private Listener menuItemListener, parentListener, focusControlListener;
	
public AcceleratorMenu(Menu parent) {
	
	this.parent = parent;
	menu = new Menu(parent.getParent(), SWT.DROP_DOWN);
	if (!parent.isVisible()) {
		item = new MenuItem(parent, SWT.CASCADE,0);
		item.setText(""); //$NON-NLS-1$
		item.setMenu(menu);
	}
	
	focusControlListener  = new Listener () {
		public void handleEvent (Event event) {
			if (verifyListener == null || menu.isDisposed()) return;
			if (event.type == SWT.KeyDown) {
					switch (event.keyCode) {
						case SWT.CONTROL:
						case SWT.SHIFT:
						case SWT.ALT: 
							return;
					}
			}
			VerifyEvent verifyEvent = new VerifyEvent (event);
			if (event.text == null) event.text = ""; //$NON-NLS-1$
			verifyListener.verifyText(verifyEvent);
			event.text = verifyEvent.text;
			event.doit = verifyEvent.doit;
		}
	};
	
	menuItemListener = new Listener() {
		public void handleEvent(Event event) {
			if (selectionListener != null)  {
				SelectionEvent selectionEvent = new SelectionEvent (event);
				//FIX ME or get EP to just call getAccelerator
				MenuItem item = (MenuItem) event.widget;
				selectionEvent.detail = item.getAccelerator();
				selectionListener.widgetSelected(selectionEvent);
			}
		}
	};
	
	parentListener = new Listener () {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Dispose:
					dispose ();
					break;
				case SWT.Show:
					if(item == null || item.isDisposed())
						break;
					item.setMenu(null);
					item.dispose();
					item = null;
					break;
				case SWT.Hide:
					item = new MenuItem(AcceleratorMenu.this.parent, SWT.CASCADE,0);
					if(menu.isDisposed()) {
						//doing more than needed;
						setAccelerators(getAccelerators());
					} else {
						item.setMenu(menu);
					}
					break;
			}
		}
	};
	parent.addListener(SWT.Show, parentListener);
	parent.addListener(SWT.Hide, parentListener);
	parent.addListener(SWT.Dispose, parentListener);
}

public void addSelectionListener(SelectionListener selectionListener) {
	this.selectionListener = selectionListener;
}

public void addVerifyListener(VerifyListener listener) {
	verifyListener = listener;
	setMultiMode(true);
}

public boolean isDisposed() {
	return item == null || item.isDisposed();
}

public void dispose() {
	setMultiMode(false);
	parent.removeListener(SWT.Show, parentListener);
	parent.removeListener(SWT.Hide, parentListener);
	parent.removeListener(SWT.Dispose, parentListener);
	menu.dispose();
	menu = null;
	if (item != null) item.dispose();
	item = null;
	focusControl = null;
	parent = null;
	verifyListener = null;
	parentListener = null;
	selectionListener = null;
	focusControlListener = null;
	menuItemListener = null;
}

public int[] getAccelerators() {
	if (accelerators == null) return null;
	int[] accelerators = new int[this.accelerators.length];
	System.arraycopy(this.accelerators, 0, accelerators, 0, this.accelerators.length);
	return accelerators;
}

public void removeSelectionListener(SelectionListener selectionListener) {
	selectionListener = null;
}

public void removeVerifyListener(VerifyListener listener) {
	verifyListener = null;
	setMultiMode(false);
}

public void setAccelerators(final int[] accelerators) {
	if (Arrays.equals(this.accelerators, accelerators)) return;	
	
	if (accelerators == null) {
		this.accelerators = null;
	} else {
		this.accelerators = new int[accelerators.length];
		System.arraycopy(accelerators, 0, this.accelerators, 0, accelerators.length);
	}
	
	menu.dispose();
	menu = new Menu(parent.getParent(), SWT.DROP_DOWN);
	if (item != null) item.setMenu (menu);
	
	for (int i = 0; i < accelerators.length; i++) {
		final int key = accelerators[i];
		MenuItem keyMenuItem = new MenuItem(menu, SWT.PUSH);
		keyMenuItem.setAccelerator(key);
		keyMenuItem.addListener(SWT.Selection, menuItemListener);
	}
}

private void setMultiMode (boolean mode) {
	if (focusControl != null && !focusControl.isDisposed ()) {
		focusControl.removeListener (SWT.KeyDown, focusControlListener);
		focusControl.removeListener (SWT.Verify, focusControlListener);
		focusControl.removeListener (SWT.FocusOut, focusControlListener);
		focusControl.removeListener (SWT.Dispose, focusControlListener);
	}
	if (mode) {
		Display display = menu.getDisplay ();
		focusControl = display.getFocusControl ();
		if (focusControl != null) {
			focusControl.addListener (SWT.KeyDown, focusControlListener);
			focusControl.addListener (SWT.Verify, focusControlListener);				
			focusControl.addListener (SWT.FocusOut, focusControlListener);
			focusControl.addListener (SWT.Dispose, focusControlListener);
		}
	}
}
}