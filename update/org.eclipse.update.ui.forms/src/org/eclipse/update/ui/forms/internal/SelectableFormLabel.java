package org.eclipse.update.ui.forms.internal;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;

public class SelectableFormLabel extends FormLabel {
	private boolean hasFocus;

	public boolean getSelection() {
		return hasFocus;
	}
	
	/**
	 * Constructor for SelectableFormLabel
	 */
	public SelectableFormLabel(Composite parent, int style) {
		super(parent, style);
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					// Activation
					notifyListeners(SWT.DefaultSelection);
				}
			}
		});
		addListener(SWT.Traverse, new Listener () {
			public void handleEvent(Event e) {
				if (e.detail != SWT.TRAVERSE_RETURN)
					e.doit = true;
			}
		});
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (!hasFocus) {
				   hasFocus=true;
				   notifyListeners(SWT.Selection);
				   redraw();
				}
			}
			public void focusLost(FocusEvent e) {
				if (hasFocus) {
					hasFocus=false;
					notifyListeners(SWT.Selection);
					redraw();
				}
			}
		});
		textMarginWidth = 1;
		textMarginHeight = 1;
	}
	
	private void notifyListeners(int eventType) {
		Event event = new Event();
		event.type = eventType;
		event.widget = this;
		notifyListeners(eventType, event);
	}

	protected void paint(PaintEvent e) {
		super.paint(e);
	   	if (hasFocus) {
	   		GC gc = e.gc;
	   		Point size = getSize();
	   		gc.setForeground(getForeground());
	   		gc.drawFocus(0, 0, size.x, size.y);
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection,typedListener);
		addListener (SWT.DefaultSelection,typedListener);
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		removeListener (SWT.Selection, listener);
		removeListener (SWT.DefaultSelection, listener);
	}
}