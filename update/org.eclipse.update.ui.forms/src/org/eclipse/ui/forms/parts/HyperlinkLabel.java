/*
 * Created on Nov 26, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.parts;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * HyperlinkLabel is a selectable label that acts as
 * a browser hyperlink. It is capable of notifying
 * listeners when it is entered, exited and activated.
 * A group of hyperlinks should be managed using
 * a HyperlinkManager that is responsible for managing
 * shared cursors, color changes, changes in underline
 * status and focus traversal between hyperlinks.
 */
public class HyperlinkLabel extends SelectableFormLabel {
	private Vector listeners;
	
/**
 * 
 * @param parent
 * @param style
 */
	public HyperlinkLabel(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (getSelection())
					handleEnter();
				else
					handleExit();
			}
		});
		addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event e) {
				handleActivate();
			}
		});
		addListener(SWT.MouseEnter, new Listener() {
			public void handleEvent(Event e) {
				handleEnter();
			}
		});
		addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event e) {
				handleExit();
			}
		});
		addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				handleMouseUp(e);
			}
		});
	}
/**
 * 
 * @param listener
 */
	public void addHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			listeners = new Vector();
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
/**
 * 
 * @param listener
 */
	public void removeHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}
	
	/**
	 * 
	 * @param href
	 */
	public void setHref(Object href) {
		setData("href", href);
	}
	/**
	 * 
	 * @return
	 */
	public Object getHref() {
		return getData("href");
	}
	
	void handleEnter() {
		if (listeners==null) return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkEntered(e);
		}
	}
	
	void handleExit() {
		if (listeners==null) return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkExited(e);
		}
	}
	
	void handleActivate() {
		if (listeners==null) return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkActivated(e);
		}
	}
	private void handleMouseUp(Event e) {
		if (e.button != 1)
			return;
		Point size = getSize();
		// Filter out mouse up events outside
		// the link. This can happen when mouse is
		// clicked, dragged outside the link, then
		// released.
		if (e.x < 0)
			return;
		if (e.y < 0)
			return;
		if (e.x >= size.x)
			return;
		if (e.y >= size.y)
			return;
		handleActivate();
	}
}
