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

package org.eclipse.jface.text.contentassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.jface.text.IEventConsumer;


/**
 * A <code>ControlContentAssistSubjectAdapter</code> delegates assistance requests from a 
 * {@link org.eclipse.jface.text.contentassist.IContentAssistantExtension content assistant}
 * to a <code>Control</code>.
 * <p>
 * XXX: This is work in progress and can change anytime until API for 3.0 is frozen.
 * </p>
 * 
 * @since 3.0
 */
public abstract class AbstractControlContentAssistSubjectAdapter implements IContentAssistSubject {

	protected static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/debug/ContentAssistSubjectAdapters"));  //$NON-NLS-1$//$NON-NLS-2$
	
	private static final ImageDescriptor CONTENT_ASSIST_CUE_IMAGE= ImageDescriptor.createFromFile(AbstractControlContentAssistSubjectAdapter.class, "images/content_assist_cue.gif"); //$NON-NLS-1$

	private List fVerifyKeyListeners;
	private Set fKeyListeners;
	private Listener fControlListener;

	/**
	 * Creates a new {@link ControlContentAssistSubjectAdapter}.
	 * 
	 * @param control the control
	 **/
	public AbstractControlContentAssistSubjectAdapter(Control control) {
		this(control, false);
	}

	/**
	 * Creates a new {@link ControlContentAssistSubjectAdapter}.
	 * 
	 * @param control the control
	 * @param showCue show cue on the left side of control
	 **/
	AbstractControlContentAssistSubjectAdapter(Control control, boolean showCue) {
		fVerifyKeyListeners= new ArrayList(1);
		fKeyListeners= new HashSet(1);
		if (showCue)
			installContentAssistCue(control);
	}
	
	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#getControl()
	 */
	public abstract Control getControl();

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener keyListener) {
		fKeyListeners.add(keyListener);
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#addKeyListener()"); //$NON-NLS-1$
		installControlListener();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener keyListener) {
		boolean deleted= fKeyListeners.remove(keyListener);
		if (DEBUG && !deleted)
			System.err.println("removeKeyListener -> wasn't here"); //$NON-NLS-1$
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#removeKeyListener() -> " + fKeyListeners.size()); //$NON-NLS-1$
		uninstallControlListener();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#supportsVerifyKeyListener()
	 */
	public boolean supportsVerifyKeyListener() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#appendVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean appendVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(verifyKeyListener);
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#appendVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		installControlListener();
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#prependVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean prependVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(0, verifyKeyListener);
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#prependVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		installControlListener();
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#removeVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.remove(verifyKeyListener);
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#removeVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		uninstallControlListener();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#setEventConsumer(org.eclipse.jface.text.IEventConsumer)
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		// this is not supported
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#setEventConsumer()"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubject#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}

	private void installControlListener() {
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#installControlListener() -> k: " + fKeyListeners.size() + ", v: " + fVerifyKeyListeners.size()); //$NON-NLS-1$ //$NON-NLS-2$
		if (fControlListener != null)
			return;
	
		fControlListener= new Listener() {
			public void handleEvent(Event e) {
				VerifyEvent verifyEvent= new VerifyEvent(e);
				KeyEvent keyEvent= new KeyEvent(e);
				switch (e.type) {
					case SWT.Traverse :
						if (DEBUG) dump("before traverse", e, verifyEvent); //$NON-NLS-1$
						verifyEvent.doit= true;
						for (Iterator iter= fVerifyKeyListeners.iterator(); iter.hasNext(); ) {
							((VerifyKeyListener) iter.next()).verifyKey(verifyEvent);
							if (! verifyEvent.doit) {
								e.detail= SWT.TRAVERSE_NONE;
								e.doit= true;
								if (DEBUG) dump("traverse eaten by verify", e, verifyEvent); //$NON-NLS-1$
								return;
							}
							if (DEBUG) dump("traverse ok", e, verifyEvent); //$NON-NLS-1$
						}
						break;
					
					case SWT.KeyDown:
						for (Iterator iter= fVerifyKeyListeners.iterator(); iter.hasNext(); ) {
							((VerifyKeyListener) iter.next()).verifyKey(verifyEvent);
							if (! verifyEvent.doit) {
								e.doit= verifyEvent.doit;
								if (DEBUG) dump("keyDown eaten by verify", e, verifyEvent); //$NON-NLS-1$
								return;
							}
						}
						if (DEBUG) dump("keyDown ok", e, verifyEvent); //$NON-NLS-1$
						for (Iterator iter= fKeyListeners.iterator(); iter.hasNext();) {
							((KeyListener) iter.next()).keyPressed(keyEvent);
						}
						break;
	
					default :
						Assert.isTrue(false);
				}
			}
			private void dump(String who, Event e, VerifyEvent ve) {
				StringBuffer sb= new StringBuffer("---\n"); //$NON-NLS-1$
				sb.append(who);
				sb.append(" - e: keyCode="+e.keyCode+hex(e.keyCode)); //$NON-NLS-1$
				sb.append("; character="+e.character+hex(e.character)); //$NON-NLS-1$
				sb.append("; stateMask="+e.stateMask+hex(e.stateMask)); //$NON-NLS-1$
				sb.append("; doit="+e.doit); //$NON-NLS-1$
				sb.append("; detail="+e.detail+hex(e.detail)); //$NON-NLS-1$
				sb.append("\n"); //$NON-NLS-1$
				sb.append("  verifyEvent keyCode="+e.keyCode+hex(e.keyCode)); //$NON-NLS-1$
				sb.append("; character="+e.character+hex(e.character)); //$NON-NLS-1$
				sb.append("; stateMask="+e.stateMask+hex(e.stateMask)); //$NON-NLS-1$
				sb.append("; doit="+ve.doit); //$NON-NLS-1$
				System.out.println(sb);
			}
			private String hex(int i) {
				return "[0x" + Integer.toHexString(i) + ']'; //$NON-NLS-1$
			}
		};
		getControl().addListener(SWT.Traverse, fControlListener);
		getControl().addListener(SWT.KeyDown, fControlListener);
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#installControlListener() - installed"); //$NON-NLS-1$
	}

	private void uninstallControlListener() {
		if (fControlListener == null || fKeyListeners.size() + fVerifyKeyListeners.size() != 0) {
			if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#uninstallControlListener() -> k: " + fKeyListeners.size() + ", v: " + fVerifyKeyListeners.size()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		getControl().removeListener(SWT.Traverse, fControlListener);
		getControl().removeListener(SWT.KeyDown, fControlListener);
		fControlListener= null;
		if (DEBUG) System.err.println("ControlContentAssistSubjectAdapter#uninstallControlListener() - done"); //$NON-NLS-1$
	}

	/**
	 * Installs a visual cue indicating availability of content assist on the given control.
	 */
	private  static void installContentAssistCue(final Control control) {
		
		if (!(control instanceof Text) && !(control instanceof Combo))
			return;
		
		final int dx;
		final int dy;
		if (SWT.getPlatform().equals("carbon")) { //$NON-NLS-1$
			if (control instanceof Combo) {
				dx= -9; dy= 0;
			} else {
				dx= -5; dy= 3;
			}
		} else {
			if (control instanceof Combo) {
				dx= -7; dy= 0;
			} else {
				dx= -8;	dy= 0;
			}
		}
		
		class CueHandler implements FocusListener, PaintListener {
			private Image fBulb;
			
			public void paintControl(PaintEvent e) {
				if (control.isDisposed())
					return;
				Point global= control.toDisplay(dx, dy);
				Point p= ((Control) e.widget).toControl(global);
				if (fBulb == null)
					fBulb= CONTENT_ASSIST_CUE_IMAGE.createImage(control.getDisplay());
				e.gc.drawImage(fBulb, p.x, p.y);
			}
			
			public void focusGained(FocusEvent e) {
				for (Control c= ((Control)e.widget).getParent(); c != null; c= c.getParent()) {
					c.addPaintListener(this);
					c.redraw();
				}
			}
			
			public void focusLost(FocusEvent e) {
				for (Control c= ((Control)e.widget).getParent(); c != null; c= c.getParent()) {
					c.removePaintListener(this);
					c.redraw();
				}
				if (fBulb != null) {
					fBulb.dispose();
					fBulb= null;
				}
			}
		}
		
		control.addFocusListener(new CueHandler());
	}
}
