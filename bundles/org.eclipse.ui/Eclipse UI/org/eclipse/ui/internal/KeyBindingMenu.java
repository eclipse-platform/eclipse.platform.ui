package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.internal.registry.AcceleratorScope;

/**
 * A submenu with an item for each accelerator in the AcceleratorScope table.
 */

public class KeyBindingMenu extends ContributionItem {

	private WorkbenchWindow workbenchWindow;
	private Menu acceleratorsMenu;
	private MenuItem cascade;
	private Control focusControl;

	private CancelListener cancelMode = new CancelListener();

	private static class CancelListener implements Listener {
		private AcceleratorScope scope;
		private KeyBindingService service;
		public void handleEvent (Event event) {
			event.doit = false;
			scope.resetMode(service);
		}
	};


	public KeyBindingMenu(WorkbenchWindow window) {
		super("Key binding menu"); //$NON-NLS-1$
		this.workbenchWindow = window;
	}

	public void fill(final Menu parent, int index) {
		cascade = new MenuItem(parent, SWT.CASCADE,index);
		cascade.setText("key binding menu");
		cascade.setMenu (acceleratorsMenu = new Menu (cascade));
		workbenchWindow.getKeyBindingService().setAcceleratorsMenu(this);
//		parent.addListener (SWT.Show, new Listener () {
//			public void handleEvent (Event event) {
//				cascade.setMenu (null);
//				cascade.dispose ();
//			}
//		});
//		parent.addListener (SWT.Hide, new Listener () {
//			public void handleEvent (Event event) {
//				cascade = new MenuItem (parent, SWT.CASCADE, 0);
//				cascade.setMenu(acceleratorsMenu);
//			}
//		});
	}
	
	public void setAccelerators(final int accs[],final AcceleratorScope scope,final KeyBindingService activeService,boolean defaultMode) {
		if(acceleratorsMenu != null) {
			acceleratorsMenu.dispose();
			acceleratorsMenu = null;
		}
		acceleratorsMenu = new Menu (cascade);
		cascade.setMenu(acceleratorsMenu);
		for (int i = 0; i < accs.length; i++) {
			final int acc = accs[i];
			MenuItem item = new MenuItem(acceleratorsMenu,SWT.PUSH);
			item.setText(Action.convertAccelerator(acc));
			item.setAccelerator(acc);
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent (Event event) {
					scope.processKey(activeService,event,acc);
				}
			});
		}
		cancelMode.scope = scope;
		cancelMode.service = activeService;
		addCancelListener(defaultMode);
	}
	
	private void addCancelListener(boolean defaultMode) {
		if(defaultMode) {
			if (focusControl != null && !focusControl.isDisposed ()) {
				focusControl.removeListener (SWT.KeyDown, cancelMode);
				focusControl.removeListener (SWT.Verify, cancelMode);
				focusControl.removeListener (SWT.FocusOut, cancelMode);
				focusControl.removeListener (SWT.Dispose, cancelMode);
			}
		} else {
			Display display = workbenchWindow.getShell().getDisplay ();
			focusControl = display.getFocusControl ();
			//BAD - what about null?
			if (focusControl != null) {
				focusControl.addListener (SWT.KeyDown, cancelMode);
				focusControl.addListener (SWT.Verify, cancelMode);				
				focusControl.addListener (SWT.FocusOut, cancelMode);
				focusControl.addListener (SWT.Dispose, cancelMode);
			}
		}
	}
}
