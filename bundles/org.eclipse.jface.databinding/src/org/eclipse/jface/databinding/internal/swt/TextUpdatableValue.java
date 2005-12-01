/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.2
 * 
 */
public class TextUpdatableValue extends UpdatableValue {

	private final Text text;

	private boolean updating = false;

	private int updatePolicy;
	
	private String bufferedValue;	
	
	private Listener validateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				fireChangeEvent(ChangeEvent.CHANGE, null, text.getText());
			}
		}
	};

	private Listener updateListener = new Listener() {
		public void handleEvent(Event event) {
			if (!updating) {
				// If we are updating on focus lost then when we fire the change event change the buffered value				
				if (updatePolicy == SWT.FocusOut){
					bufferedValue = text.getText();					
				}
				fireChangeEvent(ChangeEvent.CHANGE, null, text.getText());						
			}
		}
	};

	private VerifyListener verifyListener;
	
	private KeyListener keyListener; 

	/**
	 * @param text
	 * @param validatePolicy
	 * @param updatePolicy
	 */
	public TextUpdatableValue(final Text text, int validatePolicy, int updatePolicy) {
		this.text = text;
		this.updatePolicy = updatePolicy;
		if (updatePolicy != SWT.None) {
			text.addListener(updatePolicy, updateListener);
		}
		// If the update policy is TIME_EARLY then the model is notified of changed on key stroke by key stroke
		// When escape is pressed we need to rollback to the previous value which is done with a key listener, however
		// the bufferedValue (the last remembered change value) must be changed on focus lost
		if(updatePolicy == SWT.Modify){
			text.addListener(SWT.FocusOut, new Listener(){
				public void handleEvent(Event event){
					if(!updating){
						bufferedValue = text.getText();
					}
				}
			});
		}		
		verifyListener = new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (!updating) {
					String currentText = TextUpdatableValue.this.text.getText();
					String newText = currentText.substring(0, e.start) + e.text
							+ currentText.substring(e.end);
					ChangeEvent changeEvent = fireChangeEvent(
							ChangeEvent.VERIFY, currentText, newText);
					if (changeEvent.getVeto()) {
						e.doit = false;
					}
				}
			}
		};
		text.addVerifyListener(verifyListener);
		keyListener = new KeyListener(){
			public void keyPressed(KeyEvent e) {
				if(e.character == SWT.ESC && bufferedValue != null){
					// Revert the value in the text field to the model value
					text.setText(bufferedValue);
				}
			}
			public void keyReleased(KeyEvent e) {	
			}
		};
		text.addKeyListener(keyListener);
	}

	public void setValue(final Object value) {
		
		AsyncRunnable runnable = new AsyncRunnable(){
			public void run(){
				String oldValue = text.getText();
				try {
					updating = true;
					bufferedValue = (String)value;					
					text.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
				} finally {
					updating = false;
				}
				fireChangeEvent(ChangeEvent.CHANGE, oldValue, text.getText());				
			}
		};
		runnable.runOn(text.getDisplay());
	}

	public Object getValue() {
		SyncRunnable runnable = new SyncRunnable(){
			public Object run() {
				return text.getText();
			}			
		};
		return runnable.runOn(text.getDisplay());
	}

	public Class getValueType() {
		return String.class;
	}

	public void dispose() {
		if (!text.isDisposed()) {
			if (updatePolicy != SWT.None) {
				text.removeListener(updatePolicy, updateListener);
			}
			text.removeVerifyListener(verifyListener);
		}
		super.dispose();
	}
}
