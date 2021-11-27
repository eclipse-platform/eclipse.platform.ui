/*******************************************************************************
 * Copyright (c) 2017 Patrik Suzzi and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 368977, 504088, 504089, 504090, 504091, 509232, 506019
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.AbstractTableInformationControl;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * Base class to open a dialog to filter and select elements of a {@link Table}.
 *
 * @see AbstractTableInformationControl and CycleBaseHandler
 * @since 4.6.2
 *
 */

public abstract class FilteredTableBaseHandler extends AbstractHandler implements IExecutableExtension {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private boolean bypassFocusLost;

	private Object selection;

	protected IWorkbenchWindow window;

	protected WorkbenchPage page;

	// true to go to next and false to go to previous part
	protected boolean gotoDirection;

	/**
	 * The list of key bindings for the backward command when it is open. This value
	 * is <code>null</code> if the dialog is not open.
	 */
	private TriggerSequence[] backwardTriggerSequences = null;

	protected ParameterizedCommand commandBackward = null;

	protected ParameterizedCommand commandForward = null;
	/**
	 * The list of key bindings for the forward command when it is open. This value
	 * is <code>null</code> if the dialog is not open.
	 */
	private TriggerSequence[] forwardTriggerSequences = null;

	/**
	 * Get the index of the current item (not valid if there are no items).
	 */
	protected int getCurrentItemIndex() {
		return 0;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		page = (WorkbenchPage) window.getActivePage();
		IWorkbenchPart activePart = page.getActivePart();
		getTriggers();
		openDialog(page, activePart);
		clearTriggers();
		activate(page, selection);

		return null;
	}

	protected Object result;
	protected Shell dialog;
	private Text text;
	private Label labelTitle = null;
	private Label labelSeparator;
	private Table table;
	private TableViewer tableViewer;
	private TableColumn tc;
	private TableViewerColumn tableViewerColumn;

	/*
	 * Open a dialog showing all views in the activation order
	 */
	public void openDialog(WorkbenchPage page, IWorkbenchPart activePart) {
		final int MAX_ITEMS = 15;
		Shell shell = null;
		selection = null;

		if (activePart != null)
			shell = activePart.getSite().getShell();
		if (shell == null)
			shell = window.getShell();
		dialog = new Shell(shell, SWT.MODELESS);
		dialog.setBackground(getBackground());
		dialog.setMinimumSize(new Point(120, 50));
		Display display = dialog.getDisplay();
		dialog.setLayout(new FillLayout());

		Composite composite = new Composite(dialog, SWT.NONE);
		composite.setBackground(getBackground());
		GridLayout gl_composite = new GridLayout(1, false);
		composite.setLayout(gl_composite);

		if (isFiltered()) {
			text = new Text(composite, SWT.NONE);
			text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			text.setBackground(getBackground());
		} else {
			/*
			 * For issues with dark theme, don't use table.setHeaderVisible(true); and
			 * table.setLinesVisible(true); see:
			 * https://bugs.eclipse.org/bugs/attachment.cgi?id=264527
			 */
			labelTitle = new Label(composite, SWT.NONE);
			labelTitle.setText(getTableHeader(activePart));
			labelTitle.setBackground(getBackground());
			labelTitle.setForeground(getForeground());
		}

		/* for issues with dark theme, don't use SWT.SEPARATOR as style */
		labelSeparator = new Label(composite, SWT.HORIZONTAL);
		Image separatorBgImage = createSeparatorBgImage();
		labelSeparator.setBackgroundImage(separatorBgImage);
		labelSeparator.addDisposeListener(e -> separatorBgImage.dispose());
		GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_label.heightHint = 1;
		labelSeparator.setLayoutData(gd_label);

		tableViewer = new TableViewer(composite, SWT.SINGLE | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setBackground(getBackground());

		tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		setLabelProvider(tableViewerColumn);
		tc = tableViewerColumn.getColumn();
		tc.setResizable(false);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		if (isFiltered()) {
			tableViewer.addFilter(getFilter());
			addModifyListener(text);
			addKeyListener(text);
			text.setMessage(WorkbenchMessages.FilteredTableBase_Filter);
			text.setText(EMPTY_STRING);
		}

		// gets the input from the concrete subclass
		tableViewer.setInput(getInput(page));

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
			int currentItemIndex = getCurrentItemIndex();
			if (gotoDirection) {
				i = currentItemIndex + 1;
				if (i >= tableItemCount) {
					i = 0;
				}
			} else {
				i = currentItemIndex - 1;
				if (i < 0) {
					i = tableItemCount - 1;
				}
			}
			table.setSelection(i);
		}

		tc.pack();
		table.pack();
		dialog.pack();

		Rectangle tableBounds = table.getBounds();
		tableBounds.height = Math.min(tableBounds.height, table.getItemHeight() * MAX_ITEMS);
		table.setBounds(tableBounds);

		Rectangle dialogBounds = dialog.getBounds();
		if (!isFiltered()) {
			dialogBounds.height = labelTitle.getBounds().height + labelSeparator.getBounds().height + tableBounds.height
					+ gl_composite.marginHeight * 2 + gl_composite.verticalSpacing * 2;
		} else {
			dialogBounds.height = text.getBounds().height + +labelSeparator.getBounds().height + tableBounds.height
					+ gl_composite.marginHeight * 2 + gl_composite.verticalSpacing * 2;
		}
		dialog.setBounds(dialogBounds);
		tc.setWidth(table.getClientArea().width);

		if (isFiltered()) {
			text.setFocus();
			text.addFocusListener(fAdapter);
		} else {
			table.setFocus();
		}
		table.addFocusListener(fAdapter);
		dialog.addFocusListener(fAdapter);

		table.addMouseMoveListener(new MouseMoveListener() {
			TableItem fLastItem = null;

			@Override
			public void mouseMove(MouseEvent e) {
				if (table.equals(e.getSource())) {
					TableItem tableItem = table.getItem(new Point(e.x, e.y));
					if (fLastItem == null ^ tableItem == null) {
						table.setCursor(tableItem == null ? null : table.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
					}
					if (tableItem != null) {
						if (!tableItem.equals(fLastItem)) {
							fLastItem = tableItem;
							table.setSelection(new TableItem[] { fLastItem });
						}
					} else {
						fLastItem = null;
					}
				}
			}
		});

		setDialogLocation(dialog, activePart);

		final IContextService contextService = window.getWorkbench().getService(IContextService.class);
		try {
			dialog.open();
			addMouseListener(table, dialog);
			contextService.registerShell(dialog, IContextService.TYPE_NONE);
			addKeyListener(table, dialog);
			addTraverseListener(table);

			keepOpen(display, dialog);
		} finally {
			if (!dialog.isDisposed()) {
				cancel(dialog);
			}
			contextService.unregisterShell(dialog);
		}
	}

	/**
	 * Intended to be overwritten by test classes so the handler won't block the UI
	 * thread
	 */
	protected void keepOpen(Display display, Shell dialog) {
		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Build a 1x1 px gray image to be used as separator. This color, halfway
	 * between white and black, looks good both in Classic and in Dark Theme
	 */
	private Image createSeparatorBgImage() {
		Image backgroundImage = new Image(Display.getDefault(), 1, 1);
		GC gc = new GC(backgroundImage);
		gc.setBackground(new Color(dialog.getDisplay(), 127, 127, 127));
		gc.fillRectangle(0, 0, 1, 1);
		gc.dispose();
		return backgroundImage;
	}

	/**
	 * {@link FocusListener} for active controls. When the focus event is complete,
	 * the listener check if focus is still on one of the active components. If not,
	 * closes the dialog.
	 *
	 */
	private FocusAdapter fAdapter = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
				// once event complete, if dialog still open..
				if (dialog.isDisposed()) {
					return;
				}
				// check if the focus is still in dialog elements
				Control fc = dialog.getDisplay().getFocusControl();
				if (fc != text && fc != table && fc != dialog && !bypassFocusLost) {
					// otherwise, close
					cancel(dialog);
				}
			});
		}
	};

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
		Rectangle monitorBounds = activePart == null ? display.getPrimaryMonitor().getBounds()
				: ((Control) ((PartSite) activePart.getSite()).getModel().getWidget()).getMonitor().getBounds();

		// Place it in the center of its parent;
		dialogBounds.x = parentBounds.x + ((parentBounds.width - dialogBounds.width) / 2);
		dialogBounds.y = parentBounds.y + ((parentBounds.height - dialogBounds.height) / 2);
		if (!monitorBounds.contains(dialogBounds.x, dialogBounds.y)
				|| !monitorBounds.contains(dialogBounds.x + dialogBounds.width, dialogBounds.y + dialogBounds.height)) {
			// Place it in the center of the monitor if it is not visible
			// when placed in the center of its parent.
			// Ensure the origin is visible on the screen.
			dialogBounds.x = Math.max(0, monitorBounds.x + (monitorBounds.width - dialogBounds.width) / 2);
			dialogBounds.y = Math.max(0, monitorBounds.y + (monitorBounds.height - dialogBounds.height) / 2);
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
	 * Fetch the key bindings for the forward and backward commands. They will not
	 * change while the dialog is open, but the context will. Bug 55581.
	 */
	protected void getTriggers() {
		commandForward = getForwardCommand();
		commandBackward = getBackwardCommand();

		final IBindingService bindingService = window.getWorkbench().getService(IBindingService.class);
		forwardTriggerSequences = bindingService.getActiveBindingsFor(commandForward);
		backwardTriggerSequences = bindingService.getActiveBindingsFor(commandBackward);
	}

	private KeyStroke computeKeyStroke(KeyEvent e) {
		int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
		return SWTKeySupport.convertAcceleratorToKeyStroke(accelerator);
	}

	boolean computeAcceleratorForward(KeyEvent e) {
		boolean acceleratorForward = false;
		if (commandForward != null) {
			if (forwardTriggerSequences != null) {
				final int forwardCount = forwardTriggerSequences.length;
				for (int i = 0; i < forwardCount; i++) {
					final TriggerSequence triggerSequence = forwardTriggerSequences[i];

					// Compare the last key stroke of the binding.
					final Trigger[] triggers = triggerSequence.getTriggers();
					final int triggersLength = triggers.length;
					if ((triggersLength > 0) && (triggers[triggersLength - 1].equals(computeKeyStroke(e)))) {
						acceleratorForward = true;
						break;
					}
				}
			}
		}
		return acceleratorForward;
	}

	boolean computeAcceleratorBackward(KeyEvent e) {
		boolean acceleratorBackward = false;
		if (commandBackward != null) {
			if (backwardTriggerSequences != null) {
				final int backwardCount = backwardTriggerSequences.length;
				for (int i = 0; i < backwardCount; i++) {
					final TriggerSequence triggerSequence = backwardTriggerSequences[i];

					// Compare the last key stroke of the binding.
					final Trigger[] triggers = triggerSequence.getTriggers();
					final int triggersLength = triggers.length;
					if ((triggersLength > 0) && (triggers[triggersLength - 1].equals(computeKeyStroke(e)))) {
						acceleratorBackward = true;
						break;
					}
				}
			}
		}
		return acceleratorBackward;
	}

	/**
	 * Add modify listener to the search text, trigger search each time text
	 * changes. After the search the first matching result is selected.
	 */
	protected void addModifyListener(Text text) {
		text.addModifyListener(e -> {
			String searchText = ((Text) e.widget).getText();
			setMatcherString(searchText);
			tableViewer.refresh();
			if (tableViewer.getTable().getColumnCount() > 0) {
				tableViewer.getTable().select(0);
			}
		});
	}

	/** Add Key Listener to the search text. manage key events on search text */
	protected void addKeyListener(Text text) {
		text.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (computeAcceleratorForward(e)) {
					moveForward();
				} else if (computeAcceleratorBackward(e)) {
					moveBackward();
				}
				switch (e.keyCode) {
				case SWT.CR:
				case SWT.KEYPAD_CR:
					ok(dialog, table);
					break;
				case SWT.PAGE_DOWN:
				case SWT.ARROW_DOWN:
					moveForward();
					break;
				case SWT.PAGE_UP:
				case SWT.ARROW_UP:
					moveBackward();
					break;
				case SWT.ESC:
					cancel(dialog);
					break;
				case SWT.DEL:
					// no filter text, closes selected item
					if (text.getText().isEmpty()) {
						deleteSelectedItem();
					}
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});
	}

	protected Color getForeground() {
		return dialog.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	}

	protected Color getBackground() {
		return dialog.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}

	/**
	 * User can type these characters while navigating the table.
	 */
	private static String RE_TEXT = "[ \\w\\d_\\-\\+\\.\\*\\?\\$\\|\\(\\)\\[\\]\\{\\}@#]"; //$NON-NLS-1$

	/**
	 * Add Keylistener to the table, user actions can influence the dialog.
	 */
	protected void addKeyListener(final Table table, final Shell dialog) {
		table.addKeyListener(new KeyListener() {
			private boolean firstKey = true;

			private boolean quickReleaseMode = false;

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.keyCode;
				char character = e.character;

				boolean acceleratorForward = computeAcceleratorForward(e);
				boolean acceleratorBackward = computeAcceleratorBackward(e);

				if (character == SWT.CR || character == SWT.LF) {
					ok(dialog, table);
				} else if (acceleratorForward) {
					if (firstKey && e.stateMask != 0) {
						quickReleaseMode = true;
					}

					moveForward();
				} else if (acceleratorBackward) {
					if (firstKey && e.stateMask != 0) {
						quickReleaseMode = true;
					}

					moveBackward();
				} else if (keyCode == SWT.DEL && isFiltered()) {
					e.doit = false;
					deleteSelectedItem();
				} else if (keyCode != SWT.ALT && keyCode != SWT.COMMAND && keyCode != SWT.CTRL && keyCode != SWT.SHIFT
						&& keyCode != SWT.ARROW_DOWN && keyCode != SWT.ARROW_UP && keyCode != SWT.PAGE_DOWN
						&& keyCode != SWT.PAGE_UP && keyCode != SWT.ARROW_LEFT && keyCode != SWT.ARROW_RIGHT) {
					if (!isFiltered()) {
						cancel(dialog);
					} else {
						String s = Character.toString(e.character);
						if (e.keyCode == SWT.BS) {
							// backspace remove last char from text
							String curText = text.getText();
							if (curText.length() > 0) {
								text.setText(curText.substring(0, curText.length() - 1));
							}
						} else if (s.matches(RE_TEXT)) {
							// alphanumeric, add to text
							text.append(Character.toString(e.character));
						} else {
							cancel(dialog);
						}
					}
				} else if ((keyCode == SWT.ARROW_DOWN || keyCode == SWT.PAGE_DOWN)
						&& table.getSelectionIndex() == table.getItemCount() - 1) {
					/** DOWN is managed by table, except when "rotating" */
					moveForward();
					e.doit = false;
				} else if ((keyCode == SWT.ARROW_UP || keyCode == SWT.PAGE_UP) && table.getSelectionIndex() == 0) {
					/** UP is managed by table, except when "rotating" */
					moveBackward();
					e.doit = false;
				}

				firstKey = false;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.keyCode;
				int stateMask = e.stateMask;

				final IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
				final boolean stickyCycle = store.getBoolean(IPreferenceConstants.STICKY_CYCLE);
				if (!isFiltered() && (!stickyCycle && (firstKey || quickReleaseMode)) && keyCode == stateMask) {
					ok(dialog, table);
				}
			}
		});
	}

	/**
	 * Move to the next element, usually triggered by command, or arrow down
	 */
	private void moveForward() {
		int index = table.getSelectionIndex();
		if (isFiltered()) {
			if (text.isFocusControl()) {
				table.setFocus();
			} else if (index == table.getItemCount() - 1) {
				text.setFocus();
				return;
			}
		}

		// always set +1%count, unless text gets focus
		table.setSelection((index + 1) % table.getItemCount());

	}

	/**
	 * Move to the previous element, usually triggered by command or arrow up
	 */
	private void moveBackward() {
		int index = table.getSelectionIndex();
		if (!isFiltered()) {
			table.setSelection(index >= 1 ? index - 1 : table.getItemCount() - 1);
		} else if (text.isFocusControl()) {
			table.setFocus();
			table.setSelection(index >= 1 ? index - 1 : table.getItemCount() - 1);
		} else if (index == 0) {
			text.setFocus();
		} else {
			table.setSelection(index >= 1 ? index - 1 : table.getItemCount() - 1);
		}
	}

	/** deletes the currently selected item */
	private void deleteSelectedItem() {
		int index = table.getSelectionIndex();
		if (index == -1 || index >= table.getItemCount()) {
			return;
		}
		TableItem item = table.getItem(index);
		close(item);
	}

	/** closes the given item */
	private void close(TableItem ti) {
		// currently works for editors only (Ctrl+E)
		if (ti.getData() instanceof EditorReference) {
			int index = table.indexOf(ti);
			EditorReference ed = (EditorReference) ti.getData();
			bypassFocusLost = true;
			page.closeEditors(new IEditorReference[] { ed }, true);
			bypassFocusLost = false;
			// reset focus when closing active editor
			table.setFocus();
			tableViewer.setInput(getInput(page));

			if (table.getItemCount() == 0) {
				cancel(dialog);
			}

			if (table.isDisposed()) {
				return;
			}

			if (index > 0 && index <= table.getItemCount()) {
				index -= 1;
			}
			table.setSelection(index);
		}
	}

	/**
	 * Adds a listener to the given table that blocks all traversal operations.
	 *
	 * @param table The table to which the traversal suppression should be added;
	 *              must not be <code>null</code>.
	 */
	protected final void addTraverseListener(final Table table) {
		table.addTraverseListener(event -> event.doit = false);
	}

	/**
	 * Activate the selected item.
	 *
	 * @param page         the page
	 * @param selectedItem the selected item
	 */
	protected void activate(IWorkbenchPage page, Object selectedItem) {
		if (selectedItem != null) {
			if (selectedItem instanceof MStackElement) {
				EPartService partService = page.getWorkbenchWindow().getService(EPartService.class);
				partService.showPart(((MStackElement) selectedItem).getElementId(), PartState.ACTIVATE);

				// the if conditions below do not need to be checked then
				return;
			}
			if (selectedItem instanceof IEditorReference) {
				page.setEditorAreaVisible(true);
			}
			if (selectedItem instanceof IWorkbenchPartReference) {
				IWorkbenchPart part = ((IWorkbenchPartReference) selectedItem).getPart(true);
				if (part != null) {
					page.activate(part);
				}
				// the if conditions below do not need to be checked then
				return;
			}
			if (selectedItem instanceof IPerspectiveDescriptor) {
				IPerspectiveDescriptor persp = (IPerspectiveDescriptor) selectedItem;
				page.setPerspective(persp);
				IWorkbenchPart activePart = page.getActivePart();
				if (activePart != null) {
					activePart.setFocus();
				}
			}
		}
	}

	/**
	 * Close the dialog and set selection to null.
	 */
	protected void cancel(Shell dialog) {
		selection = null;
		dialog.close();
	}

	/**
	 * Close the dialog saving the selection
	 */
	protected void ok(Shell dialog, final Table table) {
		TableItem[] items = table.getSelection();

		if (items != null && items.length == 1) {
			selection = items[0].getData();
		}

		dialog.close();
	}

	/**
	 * Add mouse listener to the table closing it when the mouse is pressed.
	 */
	protected void addMouseListener(final Table table, final Shell dialog) {
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ok(dialog, table);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					// right click, nop
				} else {
					ok(dialog, table);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 3) {
					if (table.equals(e.getSource())) {
						TableItem ti = table.getItem(new Point(e.x, e.y));
						if (ti != null && ti.getData() instanceof EditorReference) {
							Menu menu = new Menu(table);
							MenuItem mi = new MenuItem(menu, SWT.NONE);
							mi.setText(WorkbenchMessages.FilteredTableBaseHandler_Close);
							mi.addListener(SWT.Selection, se -> close(ti));
							menu.setVisible(true);
						}
					}
				} else {
					ok(dialog, table);
				}
			}
		});
	}

	/** True to show search text and enable filtering. False by default */
	protected boolean isFiltered() {
		return false;
	}

	/** Return the filter to use. Null by default */
	protected ViewerFilter getFilter() {
		return null;
	}

	/** Set the filter text entered by the User, does nothing by default */
	protected void setMatcherString(String pattern) {
	}

	private PerspectiveLabelProvider perspectiveLabelProvider = null;

	private PerspectiveLabelProvider getPerspectiveLabelProvider() {
		if (perspectiveLabelProvider == null) {
			perspectiveLabelProvider = new PerspectiveLabelProvider(false);
		}
		return perspectiveLabelProvider;
	}

	/** Returns the text for the given {@link WorkbenchPartReference} */
	protected String getWorkbenchPartReferenceText(WorkbenchPartReference ref) {
		if (ref.isDirty()) {
			return "*" + ref.getTitle(); //$NON-NLS-1$
		}
		return ref.getTitle();
	}

	/**
	 * Sets the label provider for the only column visible in the table. Subclasses
	 * can override this method to style the table, using a StyledCellLabelProvider.
	 *
	 * @param tableViewerColumn
	 */
	protected void setLabelProvider(TableViewerColumn tableViewerColumn) {
		tableViewerColumn.setLabelProvider(getColumnLabelProvider());
	}

	/** Default ColumnLabelProvider. The table has only one column */
	protected ColumnLabelProvider getColumnLabelProvider() {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FilteredTableItem) {
					return ((FilteredTableItem) element).text;
				} else if (element instanceof WorkbenchPartReference) {
					return getWorkbenchPartReferenceText((WorkbenchPartReference) element);
				} else if (element instanceof IPerspectiveDescriptor) {
					IPerspectiveDescriptor desc = (IPerspectiveDescriptor) element;
					String text = getPerspectiveLabelProvider().getText(desc);
					return (text == null) ? "" : text; //$NON-NLS-1$
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof FilteredTableItem) {
					return ((FilteredTableItem) element).image;
				} else if (element instanceof WorkbenchPartReference) {
					return ((WorkbenchPartReference) element).getTitleImage();
				} else if (element instanceof IPerspectiveDescriptor) {
					IPerspectiveDescriptor desc = (IPerspectiveDescriptor) element;
					return getPerspectiveLabelProvider().getImage(desc);
				}
				return super.getImage(element);
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof FilteredTableItem) {
					return ((FilteredTableItem) element).tooltipText;
				} else if (element instanceof WorkbenchPartReference) {
					return ((WorkbenchPartReference) element).getTitleToolTip();
				}
				return super.getToolTipText(element);
			}
		};
	}

	/** Add all items to the dialog in the activation order */
	protected abstract Object getInput(WorkbenchPage page);

	/** Get the backward command. */
	protected abstract ParameterizedCommand getBackwardCommand();

	/** Get the forward command. */
	protected abstract ParameterizedCommand getForwardCommand();

	/**
	 * Get TableHeader, return title for non-filtered lists. By default returns an
	 * empty String. Subclasses can use the active part to detect the type of
	 * object.
	 */
	protected String getTableHeader(IWorkbenchPart activePart) {
		return EMPTY_STRING;
	}

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

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// true by default, but depends on data
		gotoDirection = data == null || "true".equals(data); //$NON-NLS-1$
	}

	/** Class used to store items to be displayed */
	public static class FilteredTableItem {
		String text;
		Image image;
		String tooltipText;
		Map<String, Object> dataMap = new HashMap<>();

		public void setText(String text) {
			this.text = text;
		}

		public void setImage(Image image) {
			this.image = image;
		}

		public void putData(String key, Object value) {
			this.dataMap.put(key, value);
		}

		public Object getData(String key) {
			return dataMap.get(key);
		}

	}
}
