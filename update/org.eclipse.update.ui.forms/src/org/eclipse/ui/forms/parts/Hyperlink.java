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
import org.eclipse.ui.forms.FormsResources;

/**
 * Hyperlink is a selectable label that acts as a browser hyperlink. It is
 * capable of notifying listeners when it is entered, exited and activated. A
 * group of hyperlinks should be managed using a HyperlinkGroup that is
 * responsible for managing shared cursors, color changes, changes in underline
 * status and focus traversal between hyperlinks.
 * 
 * @see org.eclipse.ui.forms.HyperlinkGroup
 * @since 3.0
 */
public class Hyperlink extends TraversableLabel {
	private Vector listeners;
	/**
	 * Creates a new hyperlink control in the provided parent.
	 * 
	 * @param parent
	 *            the control parent
	 * @param style
	 *            the widget style
	 */
	public Hyperlink(Composite parent, int style) {
		super(parent, style);
		Listener listener = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
					case SWT.Selection :
						if (getSelection())
							handleEnter();
						else
							handleExit();
						break;
					case SWT.DefaultSelection :
						handleActivate();
						break;
					case SWT.MouseEnter :
						handleEnter();
						break;
					case SWT.MouseExit :
						handleExit();
						break;
					case SWT.MouseUp :
						handleMouseUp(e);
						break;
				}
			}
		};
		addListener(SWT.Selection, listener);
		addListener(SWT.DefaultSelection, listener);
		addListener(SWT.MouseEnter, listener);
		addListener(SWT.MouseExit, listener);
		addListener(SWT.MouseUp, listener);
		setCursor(FormsResources.getHandCursor());
	}
	/**
	 * @param listener
	 */
	public void addHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			listeners = new Vector();
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	/**
	 * @param listener
	 */
	public void removeHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}

	/**
	 * @param href
	 */
	public void setHref(Object href) {
		setData("href", href);
	}
	/**
	 * @return
	 */
	public Object getHref() {
		return getData("href");
	}

	private void handleEnter() {
		if (listeners == null)
			return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkEntered(e);
		}
	}

	private void handleExit() {
		if (listeners == null)
			return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkExited(e);
		}
	}

	private void handleActivate() {
		if (listeners == null)
			return;
		int size = listeners.size();
		setCursor(FormsResources.getBusyCursor());
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkActivated(e);
		}
		if (!isDisposed())
			setCursor(FormsResources.getHandCursor());
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
