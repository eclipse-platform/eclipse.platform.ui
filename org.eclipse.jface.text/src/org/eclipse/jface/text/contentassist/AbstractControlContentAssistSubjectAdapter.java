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
 * An <code>AbstractControlContentAssistSubjectAdapter</code> delegates assistance requests from a 
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
	
	/**
	 * ImageDescriptor of the visual cue for content assist in dialog fields.
	 */
	private static final ImageDescriptor CONTENT_ASSIST_CUE_IMAGE= ImageDescriptor.createFromFile(AbstractControlContentAssistSubjectAdapter.class, "images/content_assist_cue.gif"); //$NON-NLS-1$
	/**
	 * VerifyKeyListeners for the control.
	 */
	private List fVerifyKeyListeners;
	/**
	 * KeyListeners for the control.
	 */
	private Set fKeyListeners;
	/**
	 * The Listener installed on the control which passes events to
	 * {@link #fVerifyKeyListeners fVerifyKeyListeners} and {@link #fKeyListeners fKeyListeners}.
	 */
	private Listener fControlListener;

	/**
	 * Creates a new {@link AbstractControlContentAssistSubjectAdapter}.
	 * 
	 * @param control the control
	 **/
	public AbstractControlContentAssistSubjectAdapter(Control control) {
		this(control, false);
	}

	/**
	 * Creates a new {@link AbstractControlContentAssistSubjectAdapter}.
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
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#addKeyListener()"); //$NON-NLS-1$
		installControlListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeKeyListener(KeyListener keyListener) {
		boolean deleted= fKeyListeners.remove(keyListener);
		if (DEBUG && !deleted)
			System.err.println("removeKeyListener -> wasn't here"); //$NON-NLS-1$
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#removeKeyListener() -> " + fKeyListeners.size()); //$NON-NLS-1$
		uninstallControlListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supportsVerifyKeyListener() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean appendVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(verifyKeyListener);
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#appendVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		installControlListener();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean prependVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(0, verifyKeyListener);
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#prependVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		installControlListener();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.remove(verifyKeyListener);
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#removeVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		uninstallControlListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		// this is not supported
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#setEventConsumer()"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLineDelimiter() {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}
	
	/**
	 * Installs <code>fControlListener</code>, which handles VerifyEvents and KeyEvents by
	 * passing them to <code>fVerifyKeyListeners</code> and <code>fKeyListeners</code>.
	 */
	private void installControlListener() {
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#installControlListener() -> k: " + fKeyListeners.size() + ", v: " + fVerifyKeyListeners.size()); //$NON-NLS-1$ //$NON-NLS-2$
		if (fControlListener != null)
			return;
	
		fControlListener= new Listener() {
			public void handleEvent(Event e) {
				if (e.widget != getControl())
					return; //SWT.TRAVERSE_MNEMONIC events can also come from other widgets
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
			
			/**
			 * Dump the given events to "standard" output.
			 * 
			 * @param who who dump's
			 * @param e  the event
			 * @param ve the verify event
			 */
			private void dump(String who, Event e, VerifyEvent ve) {
				StringBuffer sb= new StringBuffer("--- [AbstractControlContentAssistSubjectAdapter]\n"); //$NON-NLS-1$
				sb.append(who);
				sb.append(" - e: keyCode="+e.keyCode+hex(e.keyCode)); //$NON-NLS-1$
				sb.append("; character="+e.character+hex(e.character)); //$NON-NLS-1$
				sb.append("; stateMask="+e.stateMask+hex(e.stateMask)); //$NON-NLS-1$
				sb.append("; doit="+e.doit); //$NON-NLS-1$
				sb.append("; detail="+e.detail+hex(e.detail)); //$NON-NLS-1$
				sb.append("; widget="+e.widget); //$NON-NLS-1$
				sb.append("\n"); //$NON-NLS-1$
				sb.append("  verifyEvent keyCode="+e.keyCode+hex(e.keyCode)); //$NON-NLS-1$
				sb.append("; character="+e.character+hex(e.character)); //$NON-NLS-1$
				sb.append("; stateMask="+e.stateMask+hex(e.stateMask)); //$NON-NLS-1$
				sb.append("; doit="+ve.doit); //$NON-NLS-1$
				sb.append("; widget="+e.widget); //$NON-NLS-1$
				System.out.println(sb);
			}
			
			private String hex(int i) {
				return "[0x" + Integer.toHexString(i) + ']'; //$NON-NLS-1$
			}
		};
		getControl().addListener(SWT.Traverse, fControlListener);
		getControl().addListener(SWT.KeyDown, fControlListener);
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#installControlListener() - installed"); //$NON-NLS-1$
	}

	/**
	 * Uninstalls <code>fControlListener</code> iff there are no <code>KeyListener</code>s and no
	 * <code>VerifyKeyListener</code>s registered.
	 * Otherwise does nothing.
	 */
	private void uninstallControlListener() {
		if (fControlListener == null || fKeyListeners.size() + fVerifyKeyListeners.size() != 0) {
			if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#uninstallControlListener() -> k: " + fKeyListeners.size() + ", v: " + fVerifyKeyListeners.size()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		getControl().removeListener(SWT.Traverse, fControlListener);
		getControl().removeListener(SWT.KeyDown, fControlListener);
		fControlListener= null;
		if (DEBUG) System.err.println("AbstractControlContentAssistSubjectAdapter#uninstallControlListener() - done"); //$NON-NLS-1$
	}

	/**
	 * Installs the handler which shows a visual cue besides
	 * the <code>Text</code> or <code>Combo</code> control.
	 * Does nothing if already installed or the control is
	 * of an unsupported type.
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
			
			/**
			 * {@inheritDoc}
			 */
			public void paintControl(PaintEvent e) {
				if (control.isDisposed())
					return;
				Point global= control.toDisplay(dx, dy);
				Point p= ((Control) e.widget).toControl(global);
				if (fBulb == null)
					fBulb= CONTENT_ASSIST_CUE_IMAGE.createImage(control.getDisplay());
				e.gc.drawImage(fBulb, p.x, p.y);
			}
			
			/**
			 * {@inheritDoc}
			 */
			public void focusGained(FocusEvent e) {
				for (Control c= ((Control)e.widget).getParent(); c != null; c= c.getParent()) {
					c.addPaintListener(this);
					c.redraw();
				}
			}
			
			/**
			 * {@inheritDoc}
			 */
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
