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
public class KeyBindingMenu {

	private WorkbenchWindow workbenchWindow;
	KeyTable keyTable;
	Control focusControl;

	private ResetModeListener resetModeListener = new ResetModeListener();

	private static class ResetModeListener implements Listener {
		private AcceleratorScope scope;
		private KeyBindingService service;
		public void handleEvent (Event event) {
			if(event.type == SWT.Verify)
				event.doit = false;
			AcceleratorScope.resetMode(service);
		}
	};

	/**
	 * Initializes this contribution item with its window.
	 */
	public KeyBindingMenu(WorkbenchWindow window) {
		this.workbenchWindow = window;
		keyTable = new KeyTable(window.getShell());
	}
	/** 
	 * Disposes the current menu and create a new one with items for
	 * the specified accelerators.
	 */
	public void setAccelerators(int accs[],final AcceleratorScope scope,final KeyBindingService activeService,boolean defaultMode) {
		Arrays.sort(accs);
		keyTable.setKeys(accs);
		keyTable.addKeyTableListener(new KeyTable.KeyTableListener() {
			public void keyPressed(int key) {
				scope.processKey(activeService,new Event(),key);
			}
		});
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
