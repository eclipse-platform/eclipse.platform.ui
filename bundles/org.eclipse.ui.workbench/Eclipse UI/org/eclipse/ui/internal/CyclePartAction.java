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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.commands.Manager;
import org.eclipse.ui.internal.commands.Sequence;
import org.eclipse.ui.internal.commands.Stroke;

/**
 * Implements a action to enable the user switch between parts
 * using keyboard.
 */
public class CyclePartAction extends PageEventAction {
	
	String commandForward = null;
	String commandBackward = null;
	boolean forward;
	private Object selection;
	
	/**
	 * Creates a CyclePartAction.
	 */
	protected CyclePartAction(IWorkbenchWindow window, boolean forward) {
		super("", window); //$NON-NLS-1$
		this.forward = forward;
		setText();
		window.getPartService().addPartListener(this);
		updateState();
	}
	
	/**
	 * Set text and tooltips in the action.
	 */
	protected void setText() {
		// TBD: Remove text and tooltip when this becomes an invisible action.
		if (forward) {
			setText(WorkbenchMessages.getString("CyclePartAction.next.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("CyclePartAction.next.toolTip")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_PART_FORWARD_ACTION);
		} else {
			setText(WorkbenchMessages.getString("CyclePartAction.prev.text")); //$NON-NLS-1$
			setToolTipText(WorkbenchMessages.getString("CyclePartAction.prev.toolTip")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(this, IHelpContextIds.CYCLE_PART_BACKWARD_ACTION);
		}
	}
	
	/**
	 * See IPageListener
	 */
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		updateState();
	}
	
	/**
	 * See IPageListener
	 */
	public void pageClosed(IWorkbenchPage page) {
		super.pageClosed(page);
		updateState();
	}
	
	/**
	 * See IPartListener
	 */
	public void partOpened(IWorkbenchPart part) {
		super.partOpened(part);
		updateState();
	}
	
	/**
	 * See IPartListener
	 */
	public void partClosed(IWorkbenchPart part) {
		super.partClosed(part);
		updateState();
	}
	
	/** 
	 * Dispose the resources cached by this action.
	 */
	protected void dispose() {
	}
	
	/**
	 * Updates the enabled state.
	 */
	protected void updateState() {
		WorkbenchPage page = (WorkbenchPage)getActivePage();
		if (page == null) {
			setEnabled(false);
			return;
		}
		// enable iff there is at least one other part to switch to
		// (the editor area counts as one entry)
		int count = page.getViewReferences().length;
		if (page.getSortedEditors().length > 0) {
			++count;
		}
		setEnabled(count >= 1);
	}
	
	/**
	 * @see Action#run()
	 */
	public void runWithEvent(Event e) {
		IWorkbenchPage page = getActivePage();
		openDialog((WorkbenchPage) page); 
		activate(page, selection);	
	}
	
	/**
	 * Activate the selected item.
	 */
	public void activate(IWorkbenchPage page,Object selection) {
		if(selection != null) {
			if (selection instanceof IEditorReference)
				page.setEditorAreaVisible(true);
			
			IWorkbenchPart part = ((IWorkbenchPartReference) selection).getPart(true);
			
			if (part != null) 
				page.activate(part);
		}	
	}
	
	/*
	 * Open a dialog showing all views in the activation order
	 */
	private void openDialog(WorkbenchPage page) {
		final int MAX_ITEMS = 22;
		
		selection = null;
		final Shell dialog = new Shell(getWorkbenchWindow().getShell(),SWT.MODELESS);
		Display display = dialog.getDisplay();
		dialog.setLayout(new FillLayout());
		
		final Table table = new Table(dialog,SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn tc = new TableColumn(table,SWT.NONE);
		tc.setResizable(false);
		tc.setText(getTableHeader());
		addItems(table,page);
		int tableItemCount = table.getItemCount();
	
		switch (tableItemCount) {
			case 0:
				// do nothing;
				break;
			case 1:
				table.setSelection(0);
				break;
			default:
				table.setSelection(forward ? 1 : table.getItemCount() - 1);
		}
	
		tc.pack();
		table.pack();
		Rectangle tableBounds = table.getBounds();
		tableBounds.height = Math.min(tableBounds.height, table.getItemHeight()*MAX_ITEMS);
		table.setBounds(tableBounds);
		dialog.pack();
	
	 	tc.setWidth(table.getClientArea().width);	
		table.setFocus();
		table.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			
			public void focusLost(FocusEvent e) {
				cancel(dialog);
			}
		});
		
		Rectangle dialogBounds = dialog.getBounds();
		Rectangle displayBounds = display.getClientArea();
		Rectangle parentBounds = dialog.getParent().getBounds();
		
		//Place it in the center of its parent;
		dialogBounds.x = parentBounds.x + ((parentBounds.width - dialogBounds.width) / 2);
		dialogBounds.y = parentBounds.y + ((parentBounds.height - dialogBounds.height) / 2);
		if(!displayBounds.contains(dialogBounds.x,dialogBounds.y) ||
			!displayBounds.contains(dialogBounds.x + dialogBounds.width,dialogBounds.y + dialogBounds.height)) {
			//Place it in the center of the display if it is not visible
			//when placed in the center of its parent;
			dialogBounds.x = (displayBounds.width - dialogBounds.width) / 2;
			dialogBounds.y = (displayBounds.height - dialogBounds.height) / 2;
		}			
		dialogBounds.height = dialogBounds.height + 3 - table.getHorizontalBar().getSize().y;
		
		dialog.setBounds(dialogBounds);
	
		table.removeHelpListener(getHelpListener());
		table.addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent event) {
			}
		});
		
		try {
			dialog.open();
			addMouseListener(table, dialog);
			addKeyListener(table, dialog);
			
			while (!dialog.isDisposed())
				if (!display.readAndDispatch())
					display.sleep();
		} finally {
			if(!dialog.isDisposed())
				cancel(dialog);
		}
	}
	
	/**
	 * Returns the string which will be shown in the table header.
	 */ 
	protected String getTableHeader() {
		return WorkbenchMessages.getString("CyclePartAction.header"); //$NON-NLS-1$
	}
	
	/**
	 * Add all views to the dialog in the activation order
	 */
	protected void addItems(Table table,WorkbenchPage page) {
		IWorkbenchPartReference refs[] = page.getSortedParts();
		boolean includeEditor = true;
		
		for (int i = refs.length - 1; i >= 0 ; i--) {
			if(refs[i] instanceof IEditorReference) {
				if(includeEditor) {
					IEditorReference activeEditor = (IEditorReference)refs[i];
					TableItem item = new TableItem(table,SWT.NONE);
					item.setText(WorkbenchMessages.getString("CyclePartAction.editor")); //$NON-NLS-1$
					item.setImage(activeEditor.getTitleImage());
					item.setData(activeEditor);
					includeEditor = false;
				}
			} else {
				TableItem item = new TableItem(table,SWT.NONE);
				item.setText(refs[i].getTitle());
				item.setImage(refs[i].getTitleImage());
				item.setData(refs[i]);
			}
		}
	}
	
	private void addKeyListener(final Table table, final Shell dialog) {
		table.addKeyListener(new KeyListener() {
			private boolean firstKey = true;
			private boolean quickReleaseMode = false;
			
			public void keyPressed(KeyEvent e) {
				int keyCode = e.keyCode;
				int stateMask = e.stateMask;
				char character = e.character;
				int accelerator = stateMask | (keyCode != 0 ? keyCode : convertCharacter(character));

				//System.out.println("\nPRESSED");
				//printKeyEvent(e);
				//System.out.println("accelerat:\t" + accelerator + "\t (" + KeySupport.formatStroke(Stroke.create(accelerator), true) + ")");				
				
				boolean acceleratorForward = false;
				boolean acceleratorBackward = false;

				if (commandForward != null) {
					Map commandMap = Manager.getInstance().getKeyMachine().getCommandMap();		
					SortedSet sequenceSet = (SortedSet) commandMap.get(commandForward);
		
					if (sequenceSet != null) {
						Iterator iterator = sequenceSet.iterator();
						
						while (iterator.hasNext()) {
							Sequence sequence = (Sequence) iterator.next();
							List strokes = sequence.getStrokes();
							int size = strokes.size();

							if (size > 0 && accelerator == ((Stroke) strokes.get(size - 1)).getValue()) {
								acceleratorForward = true;
								break;
							}
						}
					}
				}

				if (commandBackward != null) {
					Map commandMap = Manager.getInstance().getKeyMachine().getCommandMap();		
					SortedSet sequenceSet = (SortedSet) commandMap.get(commandBackward);
		
					if (sequenceSet != null) {
						Iterator iterator = sequenceSet.iterator();
						
						while (iterator.hasNext()) {
							Sequence sequence = (Sequence) iterator.next();
							List strokes = sequence.getStrokes();
							int size = strokes.size();

							if (size > 0 && accelerator == ((Stroke) strokes.get(size - 1)).getValue()) {
								acceleratorBackward = true;
								break;
							}
						}
					}
				}

				if (character == SWT.CR || character == SWT.LF)
					ok(dialog, table);
				else if (acceleratorForward) {
					if (firstKey && e.stateMask != 0)
						quickReleaseMode = true;				
					
					int index = table.getSelectionIndex();
					table.setSelection((index + 1) % table.getItemCount());
				} else if (acceleratorBackward) {
					if (firstKey && e.stateMask != 0)
						quickReleaseMode = true;					

					int index = table.getSelectionIndex();
					table.setSelection(index >= 1 ? index - 1 : table.getItemCount() - 1);
				} else if (keyCode != SWT.ALT && keyCode != SWT.COMMAND && keyCode != SWT.CTRL && keyCode != SWT.SHIFT && 
					keyCode != SWT.ARROW_DOWN && keyCode != SWT.ARROW_UP && keyCode != SWT.ARROW_LEFT && keyCode != SWT.ARROW_RIGHT)
					cancel(dialog);

				firstKey = false;
			}
			
			public void keyReleased(KeyEvent e) {		
				int keyCode = e.keyCode;
				int stateMask = e.stateMask;
				char character = e.character;
				int accelerator = stateMask | (keyCode != 0 ? keyCode : convertCharacter(character));

				//System.out.println("\nRELEASED");
				//printKeyEvent(e);
				//System.out.println("accelerat:\t" + accelerator + "\t (" + KeySupport.formatStroke(Stroke.create(accelerator), true) + ")");
				
				if ((firstKey || quickReleaseMode) && keyCode == stateMask)
					ok(dialog, table);
			}
		});
	}

	private static char convertCharacter(char c) {
		return c >= 0 && c <= 31 ? (char) (c + '@') : Character.toUpperCase(c);
	}

	//private static void printKeyEvent(KeyEvent keyEvent) {
	//	System.out.println("keyCode:\t" + keyEvent.keyCode + "\t (" + KeySupport.formatStroke(Stroke.create(keyEvent.keyCode), true) + ")");
	//	System.out.println("stateMask:\t" + keyEvent.stateMask + "\t (" + KeySupport.formatStroke(Stroke.create(keyEvent.stateMask), true) + ")");
	//	System.out.println("character:\t" + (int) keyEvent.character + "\t (" + keyEvent.character + ")");
	//}

	/*
	 * Close the dialog saving the selection
	 */
	private void ok(Shell dialog, final Table table) {
		TableItem[] items = table.getSelection();
	
		if (items != null && items.length == 1)
			selection = items[0].getData();
	
		dialog.close();
		dispose();
	}
	
	/*
	 * Close the dialog and set selection to null.
	 */
	private void cancel(Shell dialog) {
		selection = null;
		dialog.close();
		dispose();
	}
	
	/*
	 * Add mouse listener to the table closing it when
	 * the mouse is pressed.
	 */			
	private void addMouseListener(final Table table,final Shell dialog) {
		table.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e){
				ok(dialog,table);
			}
			
			public void mouseDown(MouseEvent e){
				ok(dialog,table);
			}
			
			public void mouseUp(MouseEvent e){
				ok(dialog,table);
			}
		});
	}
}
