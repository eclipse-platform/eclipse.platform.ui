/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.4
 *
 */
public class BooleanCellEditor extends CellEditor {
	private Button button;
	private ViewerRow row;
	private int index;
	private String restoredText;
	private Image restoredImage;
	private KeyListener macSelectionListener = new KeyListener(){
	
		public void keyReleased(KeyEvent e) {
			
		}
	
		public void keyPressed(KeyEvent e) {
			if( e.character == ' ' ) {
				button.setSelection(!button.getSelection());	
			}
		}
	};

	private boolean changeOnActivation;
	
	/**
	 * @param parent
	 */
	public BooleanCellEditor(Composite parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public BooleanCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	public LayoutData getLayoutData() {
		LayoutData data = super.getLayoutData();
		data.horizontalAlignment=SWT.CENTER;
		data.grabHorizontal = false;
		return data;
	}

	protected Control createControl(Composite parent) {
		Font font = parent.getFont();
		Color bg = parent.getBackground();

		button = new Button(parent, getStyle() | SWT.CHECK);
		button.setFont(font);
		button.setBackground(bg);

		button.addKeyListener(new KeyAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				if( e.character == SWT.ESC ) {
					fireCancelEditor();
				}
			}

		});

		return button;
	}

	protected Object doGetValue() {
		return new Boolean(button.getSelection());
	}

	protected void doSetValue(Object value) {
		boolean selection = Boolean.TRUE.equals(value);
		button.setSelection(selection);
	}

	protected void doSetFocus() {
		if (button != null) {
			button.setFocus();
		}
	}

	protected void deactivate(ColumnViewerEditorDeactivationEvent event) {
		super.deactivate(event);
		if( event.eventType == ColumnViewerEditorDeactivationEvent.EDITOR_CANCELED ) {
			row.setImage(index, restoredImage);
			row.setText(index, restoredText);
		}

//TODO Add a way to enable key traversal when CheckBoxes don't get focus
//		if( Util.isMac() ) {
//			button.getParent().removeKeyListener(macSelectionListener);
//		}
		
		row = null;
		restoredImage = null;
		restoredText = null;
	}

	public void activate(ColumnViewerEditorActivationEvent activationEvent) {
		ViewerCell cell = (ViewerCell)activationEvent.getSource();
		index = cell.getColumnIndex();
		row = (ViewerRow) cell.getViewerRow().clone();
		restoredImage = row.getImage(index);
		restoredText = row.getText(index);
		row.setImage(index, null);
		row.setText(index, ""); //$NON-NLS-1$
		
    	if (activationEvent.eventType != ColumnViewerEditorActivationEvent.TRAVERSAL && changeOnActivation) {
    		button.setSelection(!button.getSelection());
    	}
    	
//TODO Add a way to enable key traversal when CheckBoxes don't get focus
//    	if( Util.isMac() ) {
//    		button.getParent().addKeyListener(macSelectionListener);
//    	}
    	
    	super.activate(activationEvent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellEditor#getDoubleClickTimeout()
	 */
	protected int getDoubleClickTimeout() {
		return 0;
	}
    
    public void setChangeOnActivation(boolean changeOnActivation) {
    	this.changeOnActivation = changeOnActivation;
    }
}