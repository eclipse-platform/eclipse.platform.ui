/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.timeeditor;

import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;


/**
 * Represents the part of SWT Canvas's API that is intended to be surfaced to
 * clients. The purpose of this interface is to restrict SWT's API somewhat for
 * clients and to make it possible to mock out SWT controls inside unit tests.
 * 
 * @since 3.2
 */
public interface ISWTCanvas {
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
	 */
	public void setBackground(Color color);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getBackground()
	 */
	public Color getBackground();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#setData(java.lang.Object)
	 */
	public void setData(Object data);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#getData()
	 */
	public Object getData();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#setData(java.lang.String, java.lang.Object)
	 */
	public void setData(String key, Object value);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#getData(java.lang.String)
	 */
	public Object getData(String key);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	public boolean setFocus();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#forceFocus()
	 */
	public boolean forceFocus();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setCapture(boolean)
	 */
	public void setCapture(boolean capture);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getAccessible()
	 */
	public Accessible getAccessible();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Canvas#getCaret()
	 */
	public Caret getCaret();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Canvas#setCaret(org.eclipse.swt.widgets.Caret)
	 */
	public void setCaret(Caret caret);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
	 */
	public void setCursor(Cursor cursor);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int)
	 */
	public Point computeSize(int wHint, int hHint);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Scrollable#computeTrim(int, int, int, int)
	 */
	public Rectangle computeTrim(int x, int y, int width, int height);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getBounds()
	 */
	public Rectangle getBounds();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setBounds(int, int, int, int)
	 */
	public void setBounds(int x, int y, int width, int height);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setBounds(org.eclipse.swt.graphics.Rectangle)
	 */
	public void setBounds(Rectangle rect);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Scrollable#getClientArea()
	 */
	public Rectangle getClientArea();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getForeground()
	 */
	public Color getForeground();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setForeground(org.eclipse.swt.graphics.Color)
	 */
	public void setForeground(Color color);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getFont()
	 */
	public Font getFont();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Canvas#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getLocation()
	 */
	public Point getLocation();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#getLayoutDeferred()
	 */
	public boolean getLayoutDeferred();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setLayoutDeferred(boolean)
	 */
	public void setLayoutDeferred(boolean defer);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getMenu()
	 */
	public Menu getMenu();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
	 */
	public void setMenu(Menu menu);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getMonitor()
	 */
	public Monitor getMonitor();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getSize()
	 */
	public Point getSize();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getToolTipText()
	 */
	public String getToolTipText();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	public void setToolTipText(String string);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getShell()
	 */
	public Shell getShell();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getVisible()
	 */
	public boolean getVisible();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#isDisposed()
	 */
	public boolean isDisposed();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#isEnabled()
	 */
	public boolean isEnabled();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#getEnabled()
	 */
	public boolean getEnabled();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#isFocusControl()
	 */
	public boolean isFocusControl();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#isVisible()
	 */
	public boolean isVisible();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#notifyListeners(int, org.eclipse.swt.widgets.Event)
	 */
	public void notifyListeners(int eventType, Event event);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#redraw()
	 */
	public void redraw();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#redraw(int, int, int, int, boolean)
	 */
	public void redraw(int x, int y, int width, int height, boolean all);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#toControl(int, int)
	 */
	public Point toControl(int x, int y);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#toControl(org.eclipse.swt.graphics.Point)
	 */
	public Point toControl(Point point);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#toDisplay(int, int)
	 */
	public Point toDisplay(int x, int y);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#toDisplay(org.eclipse.swt.graphics.Point)
	 */
	public Point toDisplay(Point point);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#traverse(int)
	 */
	public boolean traverse(int traversal);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#isListening(int)
	 */
	public boolean isListening(int eventType);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#update()
	 */
	public void update();
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addControlListener(org.eclipse.swt.events.ControlListener)
	 */
	public void addControlListener(ControlListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeControlListener(org.eclipse.swt.events.ControlListener)
	 */
	public void removeControlListener(ControlListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#addDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	public void addFocusListener(FocusListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeFocusListener(org.eclipse.swt.events.FocusListener)
	 */
	public void removeFocusListener(FocusListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addHelpListener(org.eclipse.swt.events.HelpListener)
	 */
	public void addHelpListener(HelpListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeHelpListener(org.eclipse.swt.events.HelpListener)
	 */
	public void removeHelpListener(HelpListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void addMouseListener(MouseListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void removeMouseListener(MouseListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addMouseMoveListener(org.eclipse.swt.events.MouseMoveListener)
	 */
	public void addMouseMoveListener(MouseMoveListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeMouseMoveListener(org.eclipse.swt.events.MouseMoveListener)
	 */
	public void removeMouseMoveListener(MouseMoveListener listener);

	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addMouseTrackListener(org.eclipse.swt.events.MouseTrackListener)
	 */
	public void addMouseTrackListener(MouseTrackListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeMouseTrackListener(org.eclipse.swt.events.MouseTrackListener)
	 */
	public void removeMouseTrackListener(MouseTrackListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addPaintListener(org.eclipse.swt.events.PaintListener)
	 */
	public void addPaintListener(PaintListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removePaintListener(org.eclipse.swt.events.PaintListener)
	 */
	public void removePaintListener(PaintListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addTraverseListener(org.eclipse.swt.events.TraverseListener)
	 */
	public void addTraverseListener(TraverseListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeTraverseListener(org.eclipse.swt.events.TraverseListener)
	 */
	public void removeTraverseListener(TraverseListener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
	 */
	public void addListener(int eventType, Listener listener);
	
	/** (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#removeListener(int, org.eclipse.swt.widgets.Listener)
	 */
	public void removeListener(int eventType, Listener listener);
}
