/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.keys.IBindingService;

/**
 * Its a base class for switching between views/editors/perspectives.
 * 
 * @since 3.3
 * 
 */

public abstract class CycleBaseHandler extends AbstractHandler implements
		IExecutableExtension {
	private Object selection;
	protected IWorkbenchWindow window;
	// true to go to next and false to go to previous part
	protected boolean gotoDirection;
	/**
	 * The list of key bindings for the backward command when it is open. This
	 * value is <code>null</code> if the dialog is not open.
	 */
	private TriggerSequence[] backwardTriggerSequences = null;

	protected ParameterizedCommand commandBackward = null;

	protected ParameterizedCommand commandForward = null;
	/**
	 * The list of key bindings for the forward command when it is open. This
	 * value is <code>null</code> if the dialog is not open.
	 */
	private TriggerSequence[] forwardTriggerSequences = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */

	/**
	 * Add all items to the dialog in the activation order
	 */
	protected abstract void addItems(Table table, WorkbenchPage page);

	/**
	 * Get the index of the current item (not valid if there are no items).
	 */
	protected int getCurrentItemIndex() {
		return 0;
	}
	
	/**
	 * Get the backward command.
	 */
	protected abstract ParameterizedCommand getBackwardCommand();

	/**
	 * Get the forward command.
	 */
	protected abstract ParameterizedCommand getForwardCommand();

	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IWorkbenchPage page = window.getActivePage();
		IWorkbenchPart activePart= page.getActivePart();
		getTriggers();
		openDialog((WorkbenchPage) page, activePart);
		clearTriggers();
		activate(page, selection);

		return null;
	}

	/*
	 * Open a dialog showing all views in the activation order
	 */
	protected void openDialog(WorkbenchPage page, IWorkbenchPart activePart) {
		final int MAX_ITEMS = 22;

		selection = null;
		final Shell dialog = new Shell(window.getShell(), SWT.MODELESS);
		Display display = dialog.getDisplay();
		dialog.setLayout(new FillLayout());

		final Table table = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn tc = new TableColumn(table, SWT.NONE);
		tc.setResizable(false);
		tc.setText(getTableHeader(activePart));
		addItems(table, page);
		int tableItemCount = table.getItemCount();

		switch (tableItemCount) {
		case 0:
			cancel(dialog);
			return;
		case 1:
			table.setSelection(0);
			break;
		default:
			int i;
			if (gotoDirection) {
				i= getCurrentItemIndex() + 1;
				if (i >= tableItemCount)
					i= 0;
			} else {
				i= getCurrentItemIndex() - 1;
				if (i < 0)
					i= tableItemCount - 1;
			}
			table.setSelection(i);
		}

		tc.pack();
		table.pack();
		dialog.pack();

		Rectangle tableBounds = table.getBounds();
		tableBounds.height = Math.min(tableBounds.height, table.getItemHeight()
				* MAX_ITEMS);
		table.setBounds(tableBounds);

		dialog.setBounds(dialog.computeTrim(tableBounds.x, tableBounds.y,
				tableBounds.width, tableBounds.height));

		tc.setWidth(table.getClientArea().width);
		table.setFocus();
		table.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				// Do nothing
			}

			public void focusLost(FocusEvent e) {
				cancel(dialog);
			}
		});

		setDialogLocation(dialog, activePart);

		final IContextService contextService = (IContextService) window
				.getWorkbench().getService(IContextService.class);
		try {
			dialog.open();
			addMouseListener(table, dialog);
			contextService.registerShell(dialog, IContextService.TYPE_NONE);
			addKeyListener(table, dialog);
			addTraverseListener(table);

			while (!dialog.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} finally {
			if (!dialog.isDisposed()) {
				cancel(dialog);
			}
			contextService.unregisterShell(dialog);
		}
	}

	/**
	 * Sets the dialog's location on the screen.
	 * 
	 * @param dialog
	 */
	protected void setDialogLocation(final Shell dialog, IWorkbenchPart activePart) {
		Display display = dialog.getDisplay();
		Rectangle dialogBounds = dialog.getBounds();
		Rectangle parentBounds = dialog.getParent().getBounds();

		// the bounds of the monitor that contains the currently active part.
		Rectangle monitorBounds = activePart == null ? display
				.getPrimaryMonitor().getBounds() : ((PartSite) activePart
				.getSite()).getPane().getControl().getMonitor().getBounds();

		// Place it in the center of its parent;
		dialogBounds.x = parentBounds.x
				+ ((parentBounds.width - dialogBounds.width) / 2);
		dialogBounds.y = parentBounds.y
				+ ((parentBounds.height - dialogBounds.height) / 2);
		if (!monitorBounds.contains(dialogBounds.x, dialogBounds.y)
				|| !monitorBounds.contains(dialogBounds.x + dialogBounds.width,
						dialogBounds.y + dialogBounds.height)) {
			// Place it in the center of the monitor if it is not visible
			// when placed in the center of its parent;
			dialogBounds.x = monitorBounds.x
					+ (monitorBounds.width - dialogBounds.width) / 2;
			dialogBounds.y = monitorBounds.y
					+ (monitorBounds.height - dialogBounds.height) / 2;
		}

		dialog.setLocation(dialogBounds.x, dialogBounds.y);
	}

	/**
	 * Clears the forward and backward trigger sequences.
	 */
	protected void clearTriggers() {
		forwardTriggerSequences = null;
		backwardTriggerSequences = null;
	}

	/**
	 * Fetch the key bindings for the forward and backward commands. They will
	 * not change while the dialog is open, but the context will. Bug 55581.
	 */
	protected void getTriggers() {
		commandForward = getForwardCommand();
		commandBackward = getBackwardCommand();

		final IBindingService bindingService = (IBindingService) window
				.getWorkbench().getService(IBindingService.class);
		forwardTriggerSequences = bindingService
				.getActiveBindingsFor(commandForward);
		backwardTriggerSequences = bindingService
				.getActiveBindingsFor(commandBackward);
	}

	protected void addKeyListener(final Table table, final Shell dialog) {
		table.addKeyListener(new KeyListener() {
			private boolean firstKey = true;

			private boolean quickReleaseMode = false;

			public void keyPressed(KeyEvent e) {
				int keyCode = e.keyCode;
				char character = e.character;
				int accelerator = SWTKeySupport
						.convertEventToUnmodifiedAccelerator(e);
				KeyStroke keyStroke = SWTKeySupport
						.convertAcceleratorToKeyStroke(accelerator);

				boolean acceleratorForward = false;
				boolean acceleratorBackward = false;

				if (commandForward != null) {
					if (forwardTriggerSequences != null) {
						final int forwardCount = forwardTriggerSequences.length;
						for (int i = 0; i < forwardCount; i++) {
							final TriggerSequence triggerSequence = forwardTriggerSequences[i];

							// Compare the last key stroke of the binding.
							final Trigger[] triggers = triggerSequence
									.getTriggers();
							final int triggersLength = triggers.length;
							if ((triggersLength > 0)
									&& (triggers[triggersLength - 1]
											.equals(keyStroke))) {
								acceleratorForward = true;
								break;
							}
						}
					}
				}

				if (commandBackward != null) {
					if (backwardTriggerSequences != null) {
						final int backwardCount = backwardTriggerSequences.length;
						for (int i = 0; i < backwardCount; i++) {
							final TriggerSequence triggerSequence = backwardTriggerSequences[i];

							// Compare the last key stroke of the binding.
							final Trigger[] triggers = triggerSequence
									.getTriggers();
							final int triggersLength = triggers.length;
							if ((triggersLength > 0)
									&& (triggers[triggersLength - 1]
											.equals(keyStroke))) {
								acceleratorBackward = true;
								break;
							}
						}
					}
				}

				if (character == SWT.CR || character == SWT.LF) {
					ok(dialog, table);
				} else if (acceleratorForward) {
					if (firstKey && e.stateMask != 0) {
						quickReleaseMode = true;
					}

					int index = table.getSelectionIndex();
					table.setSelection((index + 1) % table.getItemCount());
				} else if (acceleratorBackward) {
					if (firstKey && e.stateMask != 0) {
						quickReleaseMode = true;
					}

					int index = table.getSelectionIndex();
					table.setSelection(index >= 1 ? index - 1 : table
							.getItemCount() - 1);
				} else if (keyCode != SWT.ALT && keyCode != SWT.COMMAND
						&& keyCode != SWT.CTRL && keyCode != SWT.SHIFT
						&& keyCode != SWT.ARROW_DOWN && keyCode != SWT.ARROW_UP
						&& keyCode != SWT.ARROW_LEFT
						&& keyCode != SWT.ARROW_RIGHT) {
					cancel(dialog);
				}

				firstKey = false;
			}

			public void keyReleased(KeyEvent e) {
				int keyCode = e.keyCode;
				int stateMask = e.stateMask;

				final IPreferenceStore store = WorkbenchPlugin.getDefault()
						.getPreferenceStore();
				final boolean stickyCycle = store
						.getBoolean(IPreferenceConstants.STICKY_CYCLE);
				if ((!stickyCycle && (firstKey || quickReleaseMode))
						&& keyCode == stateMask) {
					ok(dialog, table);
				}
			}
		});
	}

	/**
	 * Adds a listener to the given table that blocks all traversal operations.
	 * 
	 * @param table
	 *            The table to which the traversal suppression should be added;
	 *            must not be <code>null</code>.
	 */
	protected final void addTraverseListener(final Table table) {
		table.addTraverseListener(new TraverseListener() {
			/**
			 * Blocks all key traversal events.
			 * 
			 * @param event
			 *            The trigger event; must not be <code>null</code>.
			 */
			public final void keyTraversed(final TraverseEvent event) {
				event.doit = false;
			}
		});
	}

	/**
	 * Activate the selected item.
	 * 
	 * @param page
	 *            the page
	 * @param selectedItem
	 *            the selected item
	 */
	protected void activate(IWorkbenchPage page, Object selectedItem) {
		if (selectedItem != null) {
			if (selectedItem instanceof IEditorReference) {
				page.setEditorAreaVisible(true);
			}
			if (selectedItem instanceof IWorkbenchPartReference) {
				IWorkbenchPart part = ((IWorkbenchPartReference) selectedItem)
						.getPart(true);
				if (part != null) {
					page.activate(part);
				}
			}
			
			if (selectedItem instanceof IPerspectiveDescriptor){
	            IPerspectiveDescriptor persp = (IPerspectiveDescriptor) selectedItem;
	            page.setPerspective(persp);
			}
		}
	}

	/*
	 * Close the dialog and set selection to null.
	 */
	protected void cancel(Shell dialog) {
		selection = null;
		dialog.close();
	}

	/*
	 * Close the dialog saving the selection
	 */
	protected void ok(Shell dialog, final Table table) {
		TableItem[] items = table.getSelection();

		if (items != null && items.length == 1) {
			selection = items[0].getData();
		}

		dialog.close();
	}

	/*
	 * Add mouse listener to the table closing it when the mouse is pressed.
	 */
	protected void addMouseListener(final Table table, final Shell dialog) {
		table.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				ok(dialog, table);
			}

			public void mouseDown(MouseEvent e) {
				ok(dialog, table);
			}

			public void mouseUp(MouseEvent e) {
				ok(dialog, table);
			}
		});
	}

	protected abstract String getTableHeader(IWorkbenchPart activePart);

	// return WorkbenchMessages.CyclePartAction_header;

	public Object getSelection() {
		return selection;
	}

	public IWorkbenchWindow getWindow() {
		return window;
	}

	public TriggerSequence[] getBackwardTriggerSequences() {
		return backwardTriggerSequences;
	}

	public TriggerSequence[] getForwardTriggerSequences() {
		return forwardTriggerSequences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		gotoDirection = "true".equals(data); //$NON-NLS-1$
	}
}
