package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.Arrays;

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

	private ResetModeListener resetModeListener = new ResetModeListener();

	private static class ResetModeListener implements Listener {
		private AcceleratorScope scope;
		private KeyBindingService service;
		public void handleEvent (Event event) {
			if(event.type == SWT.Verify)
				event.doit = false;
			scope.resetMode(service);
		}
	};

	/**
	 * Initializes this contribution item with its window.
	 */
	public KeyBindingMenu(WorkbenchWindow window) {
		super("Key binding menu"); //$NON-NLS-1$
		this.workbenchWindow = window;
	}
	/** 
	 * Creates the cascade menu which will be hidden from the user.
	 */
	public void fill(final Menu parent, int index) {
		cascade = new MenuItem(parent, SWT.CASCADE,index);
		cascade.setText("Key binding menu"); //$NON-NLS-1$
		cascade.setMenu (acceleratorsMenu = new Menu (cascade));
		workbenchWindow.getKeyBindingService().setAcceleratorsMenu(this);
		parent.addListener (SWT.Show, new Listener () {
			public void handleEvent (Event event) {
				cascade.setMenu (null);
				cascade.dispose ();
			}
		});
		parent.addListener (SWT.Hide, new Listener () {
			public void handleEvent (Event event) {
				cascade = new MenuItem (parent, SWT.CASCADE, 0);
				cascade.setText("Key binding menu"); //$NON-NLS-1$
				cascade.setMenu(acceleratorsMenu);
			}
		});
	}
	/** 
	 * Disposes the current menu and create a new one with items for
	 * the specified accelerators.
	 */
	public void setAccelerators(final int accs[],final AcceleratorScope scope,final KeyBindingService activeService,boolean defaultMode) {
		if(acceleratorsMenu != null) {
			acceleratorsMenu.dispose();
			acceleratorsMenu = null;
		}
		acceleratorsMenu = new Menu (cascade);
		cascade.setMenu(acceleratorsMenu);
//		Arrays.sort(accs);
		for (int i = 0; i < accs.length; i++) {
			final int acc = accs[i];
			String accId = scope.getActionDefinitionId(acc);
			if(accId == null || activeService.getAction(accId) == null)
				continue;
			MenuItem item = new MenuItem(acceleratorsMenu,SWT.PUSH);
//			item.setText(Action.convertAccelerator(acc));
			item.setAccelerator(acc);
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent (Event event) {
					scope.processKey(activeService,event,acc);
				}
			});
		}
		resetModeListener.scope = scope;
		resetModeListener.service = activeService;
		updateCancelListener(defaultMode);
	}
	/**
	 * Add/remove the reset mode listener to/from the focus control.
	 * If the control loses focus, is disposed, or any key (which will not
	 * be an accelerator) gets to the control, the mode is reset.
	 */
	private void updateCancelListener(boolean defaultMode) {
		if(defaultMode) {
			if (focusControl != null && !focusControl.isDisposed ()) {
				focusControl.removeListener (SWT.KeyDown, resetModeListener);
				focusControl.removeListener (SWT.Verify, resetModeListener);
				focusControl.removeListener (SWT.FocusOut, resetModeListener);
				focusControl.removeListener (SWT.Dispose, resetModeListener);
			}
		} else {
			Display display = workbenchWindow.getShell().getDisplay ();
			focusControl = display.getFocusControl ();
			//BAD - what about null?
			if (focusControl != null) {
				focusControl.addListener (SWT.KeyDown, resetModeListener);
				focusControl.addListener (SWT.Verify, resetModeListener);				
				focusControl.addListener (SWT.FocusOut, resetModeListener);
				focusControl.addListener (SWT.Dispose, resetModeListener);
			}
		}
	}
}
