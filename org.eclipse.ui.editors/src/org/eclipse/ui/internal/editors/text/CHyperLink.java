/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;


/**
 * A hyperlink widget.
 * TODO remove when platform widget becomes available
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=79419
 * @since 3.1
 */
public class CHyperLink extends Canvas {
	protected final Label fWidget;
	private final Set fListeners= new HashSet();
	/* The state of the link is encoded in two booleans: armed and mouse focus state.
	 * - The link is armed on mouseDown and unarmed when the mouse is released
	 * - The link has mouse-focus if the mouse cursor is within the control
	 * 
	 * If the link is both armed and has focus, it is drawn in a different color. In that
	 * state, selection occurs when the mouse button is released.
	 */
	private boolean fArmed;
	private boolean fHasMouseFocus;
	
	/**
	 * Creates a new hyperlink control.
	 * 
	 * @param parent the parent composite
	 * @param style ignored
	 */
	public CHyperLink(Composite parent, int style) {
		super(parent, SWT.NO_RADIO_GROUP);

		fWidget= new Label(this, SWT.NONE);
		markUnarmed();
		setLayout(new FillLayout());
		
		fWidget.addMouseListener(new MouseAdapter() {

			public void mouseDown(MouseEvent e) {
				forceFocus();
				if (e.button == 1) {
					if (fHasMouseFocus)
						markArmed();
					fArmed= true;
					fWidget.redraw();
				}
			}

			public void mouseUp(MouseEvent e) {
				if (e.button == 1) {
					if (fHasMouseFocus && fArmed)
						fireSelected();
					if (fHasMouseFocus)
						markMouseFocus();
					fArmed= false;
					fWidget.redraw();
				}
			}

		});
		fWidget.addMouseTrackListener(new MouseTrackAdapter() {

			public void mouseEnter(MouseEvent e) {
				if (fArmed)
					markArmed();
				else
					markMouseFocus();
				fHasMouseFocus= true;
				fWidget.setCursor(fWidget.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
				fWidget.redraw();
			}

			public void mouseExit(MouseEvent e) {
				if (fArmed)
					markUnarmed();
				else
					markNoMouseFocus();
				fHasMouseFocus= false;
				fWidget.setCursor(null);
				fWidget.redraw();
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == ' ' && e.stateMask == 0)
					fireSelected();
			}
		});
		addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				switch (e.detail) {
					case SWT.TRAVERSE_TAB_NEXT:
					case SWT.TRAVERSE_TAB_PREVIOUS:
						if (!fArmed)
							e.doit= true;
				}
			}
		});
		
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				fWidget.redraw();
			}
			public void focusLost(FocusEvent e) {
				fWidget.redraw();
			}
		});
		
		fWidget.addPaintListener(new PaintListener() {
			
			public void paintControl(PaintEvent e) {
				boolean hasInputFocus= isFocusControl();
				Color foreground= getForeground(hasInputFocus);
				fWidget.setForeground(foreground);
				
				GC gc= e.gc;
				Rectangle bounds= fWidget.getBounds();
				gc.setLineStyle(SWT.LINE_SOLID);
				gc.setLineWidth(1);
				gc.drawLine(bounds.x, bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y + bounds.height - 1);
				if (hasInputFocus) {
					gc.drawFocus(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}

			private Color getForeground(boolean hasInputFocus) {
				if (fHasMouseFocus) {
					if (fArmed)
						return fWidget.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
					return fWidget.getDisplay().getSystemColor(SWT.COLOR_BLUE);
				}
				return fWidget.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
			}
		});
	}
	
	/* delegates to the underlying widget */
	
	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when the control is selected, by sending
	 * it one of the messages defined in the <code>SelectionListener</code>
	 * interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the hyperlink is selected.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		fListeners.add(listener);
	}
	
	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when the control is selected.
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		fListeners.remove(listener);
	}
	
	/**
	 * Returns the receiver's text, which will be an empty
	 * string if it has never been set.
	 *
	 * @return the receiver's text
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public String getText() {
		return fWidget.getText();
	}
	
	/**
	 * Sets the receiver's text.
	 * <p>
	 * This method sets the widget label.  The label may include
	 * the mnemonic character and line delimiters.
	 * </p>
	 * <p>
	 * Mnemonics are indicated by an '&amp;' that causes the next
	 * character to be the mnemonic.  When the user presses a
	 * key sequence that matches the mnemonic, focus is assigned
	 * to the control that follows the label. On most platforms,
	 * the mnemonic appears underlined but may be emphasised in a
	 * platform specific manner.  The mnemonic indicator character
	 *'&amp;' can be escaped by doubling it in the string, causing
	 * a single '&amp;' to be displayed.
	 * </p>
	 * 
	 * @param string the new text
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setText(String string) {
		fWidget.setText(string);
	}
	
	/*
	 * @see org.eclipse.swt.widgets.Control#getToolTipText()
	 */
	public String getToolTipText() {
		return fWidget.getToolTipText();
	}
	
	/*
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	public void setToolTipText(String string) {
		fWidget.setToolTipText(string);
	}
	
	/* events */
	
	private void markUnarmed() {
	}

	private void markArmed() {
	}
	
	private void markMouseFocus() {
	}

	private void markNoMouseFocus() {
	}
	
	private void fireSelected() {
		SelectionListener[] listeners= (SelectionListener[]) fListeners.toArray(new SelectionListener[fListeners.size()]);
		for (int i= 0; i < listeners.length; i++) {
			final SelectionListener listener= listeners[i];
			Platform.run(new ISafeRunnable() {

				public void handleException(Throwable exception) {
					// only log
				}

				public void run() throws Exception {
					Event baseEvent= new Event();
					baseEvent.widget= fWidget;
					SelectionEvent event= new SelectionEvent(baseEvent);
					listener.widgetSelected(event);
				}
				
			});
		}
	}
	
	
	/*
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		fListeners.clear();
		super.dispose();
	}

}
