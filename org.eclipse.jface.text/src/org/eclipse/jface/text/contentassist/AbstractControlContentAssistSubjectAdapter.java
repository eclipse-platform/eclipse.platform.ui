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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;

import org.eclipse.jface.text.IEventConsumer;


/**
 * An <code>AbstractControlContentAssistSubjectAdapter</code> delegates assistance requests from a 
 * {@link org.eclipse.jface.text.contentassist.IContentAssistantExtension content assistant}
 * to a <code>Control</code>.
 * 
 * @since 3.0
 */
public abstract class AbstractControlContentAssistSubjectAdapter implements IContentAssistSubject {

	protected static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/debug/ContentAssistSubjectAdapters"));  //$NON-NLS-1$//$NON-NLS-2$
	
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
	 */
	AbstractControlContentAssistSubjectAdapter(Control control, boolean showCue) {
		fVerifyKeyListeners= new ArrayList(1);
		fKeyListeners= new HashSet(1);
		setShowContentAssistCue(control, showCue);
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
		
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#addKeyListener()"); //$NON-NLS-1$
		
		installControlListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeKeyListener(KeyListener keyListener) {
		boolean deleted= fKeyListeners.remove(keyListener);
		
		if (DEBUG) {
			if (!deleted)
				System.out.println("removeKeyListener -> wasn't here"); //$NON-NLS-1$
			System.out.println("AbstractControlContentAssistSubjectAdapter#removeKeyListener() -> " + fKeyListeners.size()); //$NON-NLS-1$
		}
		
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
		
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#appendVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		
		installControlListener();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean prependVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(0, verifyKeyListener);
		
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#prependVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		
		installControlListener();
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.remove(verifyKeyListener);
		
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#removeVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$
		
		uninstallControlListener();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		// this is not supported
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#setEventConsumer()"); //$NON-NLS-1$
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
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#installControlListener() -> k: " + fKeyListeners.size() + ", v: " + fVerifyKeyListeners.size()); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (fControlListener != null)
			return;
	
		fControlListener= new Listener() {
			public void handleEvent(Event e) {
				if (! getControl().isFocusControl())
					return; //SWT.TRAVERSE_MNEMONIC events can also come in to inactive widgets
				VerifyEvent verifyEvent= new VerifyEvent(e);
				KeyEvent keyEvent= new KeyEvent(e);
				switch (e.type) {
					case SWT.Traverse :
						
						if (DEBUG)
							dump("before traverse", e, verifyEvent); //$NON-NLS-1$
						
						verifyEvent.doit= true;
						for (Iterator iter= fVerifyKeyListeners.iterator(); iter.hasNext(); ) {
							((VerifyKeyListener) iter.next()).verifyKey(verifyEvent);
							if (! verifyEvent.doit) {
								e.detail= SWT.TRAVERSE_NONE;
								e.doit= true;
								if (DEBUG)
									dump("traverse eaten by verify", e, verifyEvent); //$NON-NLS-1$
								return;
							}
							
							if (DEBUG)
								dump("traverse ok", e, verifyEvent); //$NON-NLS-1$
						}
						break;
					
					case SWT.KeyDown:
						for (Iterator iter= fVerifyKeyListeners.iterator(); iter.hasNext(); ) {
							((VerifyKeyListener) iter.next()).verifyKey(verifyEvent);
							if (! verifyEvent.doit) {
								e.doit= verifyEvent.doit;
								if (DEBUG)
									dump("keyDown eaten by verify", e, verifyEvent); //$NON-NLS-1$
								return;
							}
						}
					
						if (DEBUG)
							dump("keyDown ok", e, verifyEvent); //$NON-NLS-1$
						
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
		
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#installControlListener() - installed"); //$NON-NLS-1$
	}

	/**
	 * Uninstalls <code>fControlListener</code> iff there are no <code>KeyListener</code>s and no
	 * <code>VerifyKeyListener</code>s registered.
	 * Otherwise does nothing.
	 */
	private void uninstallControlListener() {
		if (fControlListener == null || fKeyListeners.size() + fVerifyKeyListeners.size() != 0) {
			
			if (DEBUG)
				System.out.println("AbstractControlContentAssistSubjectAdapter#uninstallControlListener() -> k: " + fKeyListeners.size() + ", v: " + fVerifyKeyListeners.size()); //$NON-NLS-1$ //$NON-NLS-2$
			
			return;
		}
		getControl().removeListener(SWT.Traverse, fControlListener);
		getControl().removeListener(SWT.KeyDown, fControlListener);
		fControlListener= null;
		
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#uninstallControlListener() - done"); //$NON-NLS-1$
	}
	
	/**
	 * Uninstalls this control content assist subject adapter.
	 */
	public void uninstall() {
		setShowContentAssistCue(getControl(), false);
	}
	
	/**
	 * Controls visibility of the visual cue for content assist.
	 *
	 * @param enable iff <code>true</code>, show cue
	 */
	public void enableContentAssistCue(boolean enable) {
	}

	/**
	 * Controls visibility of the visual cue for content assist.
	 *
	 * @param control the <code>Control</code>
	 * @param enable iff <code>true</code>, show cue
	 */
	private void setShowContentAssistCue(Control control, boolean enable) {
		SmartFieldController.setSmartCue(control, enable ? "Content Assist Available" : null);
	}
	
	/**
	 * Sets the given error message in the field message hover.
	 *
	 * @param message the message, or <code>null</code> to clear
	 */
	public void setErrorMessage(String message) {
		SmartFieldController.setErrorMessage(getControl(), message);
	
	}
	
	/**
	 * The controller for cues and error messages on <code>Text</code> and <code>Combo</code> <code>Control</code>s.
	 * 
	 * XXX: This is work in progress and can change anytime until API for 3.0 is frozen.
	 * 
	 * @since 3.0
	 */
	private static class SmartFieldController {
			
			class Hover {
				// info hover metrics
				private int HD= 10;	// distance of arrow from left side
				private int HW= 8;	// width of info hover arrow
				private int HH= 10;  // height of info hover arrow
				private int MARGIN= 2;

				Shell fHoverShell;
				String fText= ""; //$NON-NLS-1$
				
				Hover(Shell parent) {
					final Display display= parent.getDisplay();
					fHoverShell= new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
					fHoverShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
					fHoverShell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
					fHoverShell.addPaintListener(new PaintListener() {
						public void paintControl(PaintEvent pe) {
							pe.gc.drawString(fText, MARGIN, MARGIN);
							if (!fgCarbon)
								pe.gc.drawPolygon(getPolygon(true));
						}
					});
					fHoverShell.addMouseListener(new MouseAdapter() {
						public void mouseDown(MouseEvent e) {
							updateHover2(null, null);
						}
					});
				}
				
				int[] getPolygon(boolean border) {
					Point e= getExtent();
					if (border)
						return new int[] { 0,0, e.x-1,0, e.x-1,e.y-1, HD+HW,e.y-1, HD+HW/2,e.y+HH-1, HD,e.y-1, 0,e.y-1, 0,0 };
					return new int[] { 0,0, e.x,0, e.x,e.y, HD+HW,e.y, HD+HW/2,e.y+HH, HD,e.y, 0,e.y, 0,0 };
				}
				
				void dispose() {
					if (!fHoverShell.isDisposed())
						fHoverShell.dispose();
				}
				
				void setVisible(boolean visible) {
					if (visible) {
						if (!fHoverShell.isVisible())
							fHoverShell.setVisible(true);
					} else {
						if (fHoverShell.isVisible())
							fHoverShell.setVisible(false);
					}
				}
					
				void setText(String t) {
					if (t == null)
						t= ""; //$NON-NLS-1$
					if (! t.equals(fText)) {
						Point oldSize= getExtent();
						fText= t;
						fHoverShell.redraw();
						Point newSize= getExtent();
						if (!oldSize.equals(newSize)) {
							Region region= new Region();
							region.add(getPolygon(false));
							fHoverShell.setRegion(region);
						}		
					}
				}

				boolean isVisible() {
					return fHoverShell.isVisible();
				}
				
				void setLocation(Control control) {
					if (control != null) {
						int h= getExtent().y;
						fHoverShell.setLocation(control.toDisplay(-HD+HW/2, -h-HH+1));
					}
				}
				
				Point getExtent() {
					GC gc= new GC(fHoverShell);
					Point e= gc.textExtent(fText);
					gc.dispose();
					e.x+= MARGIN*2;
					e.y+= MARGIN*2;
					return e;
				}
			}
			
			class Annotation {
				
				private Control fControl;	// put icon relative to this control
				private Image fImage;		// visual representation of this annotation
				private int fDx, fDy;		// place icon relative to top-left corner of control
				private String fText;		// the information shown in hover
				
				Annotation(Control control, Image image, int dy, String text) {
					fControl= control;
					fImage= image;
					fText= text;
					fDx= -5;
					fDy= dy;
					if (fgCarbon) {
						if (control instanceof Text) {
							fDy+= 3;
						} else if (control instanceof Combo) {
							fDx= -9;
						}
					} else if (fgWin32) {
						if (control instanceof Text) {
							fDx= -7;
							fDy-= 2;
						}
					}
				}
				
				void paintControl(PaintEvent e) {
					if (!fControl.isDisposed() && !fImage.isDisposed()) {
						Point global= fControl.toDisplay(fDx, fDy);
						Point local= ((Control) e.widget).toControl(global);
						e.gc.drawImage(fImage, local.x, local.y);
					}
				}
				
				boolean handleEvent(MouseEvent e) {
					if (!fImage.isDisposed()) {
						Rectangle r= fImage.getBounds();
						Point global= fControl.toDisplay(fDx, fDy);
						Point local= ((Control) e.widget).toControl(global);
						r.x= local.x;
						r.y= local.y;
						if (r.contains(e.x, e.y)) {
							updateHover2(fControl, fText);
							return true;
						}
					}
					return false;
				}
			}
			
			/**
			 * One AnnotationHandler is registered for a given Control.
			 */
			class AnnotationHandler extends MouseTrackAdapter implements MouseMoveListener, PaintListener  {
				
				ArrayList fAnnotations= new ArrayList();
				
				public void paintControl(PaintEvent e) {
					Iterator iter= fAnnotations.iterator();
					while (iter.hasNext()) {
						Annotation a= (Annotation) iter.next();
						a.paintControl(e);
					}
				}
				
				void add(Annotation a) {
					fAnnotations.add(a);
				}

				boolean remove(Annotation a) {
					fAnnotations.remove(a);
					return fAnnotations.size() <= 0;
				}

				public void mouseEnter(MouseEvent e) {
					if (isTooltipVisible())
						update(e);
				}
				
				public void mouseHover(MouseEvent e) {
						update(e);
				}

				public void mouseMove(MouseEvent e) {
					if (isTooltipVisible())
						update(e);
				}
				
				public void mouseExit(MouseEvent e) {
					if (isTooltipVisible())
						update(e);
				}
				
				void update(MouseEvent e) {
					Iterator iter= fAnnotations.iterator();
					while (iter.hasNext()) {
						Annotation a= (Annotation) iter.next();
						if (a.handleEvent(e))
							return;
					}
					Control c= e.widget.getDisplay().getFocusControl();
					if (c != null)
						updateHover(c);
					else
						updateHover2(c, null);					
				}
			}
			
			class ErrorAnnotationFocusListener implements FocusListener {
				public void focusGained(FocusEvent e) {
					updateHover((Control)e.widget);
				}
				public void focusLost(FocusEvent e) {
					updateHover2((Control)e.widget, null);
				}
			}
			
			class SmartAnnotationFocusListener implements FocusListener {
				Control fControl;
				Annotation fAnnotation;
				
				SmartAnnotationFocusListener(Control c, Annotation a) {
					fControl= c;
					fAnnotation= a;
				}
				
				public void focusGained(FocusEvent e) {
					addAnnotation(fControl, fAnnotation);
				}
				
				public void focusLost(FocusEvent e) {
					removeAnnotation(fControl, fAnnotation);
				}
			}

			private static final String FIELDCONTROLLER= "org.eclipse.SmartFieldController"; //$NON-NLS-1$
			private static final String SMARTFOCUSLISTENER= "org.eclipse.SmartFieldController.smartFocusListener"; //$NON-NLS-1$
			private static final String ANNOTATIONHANDLER= "org.eclipse.SmartFieldController.Listener"; //$NON-NLS-1$
			private static final String INFO_ANNOTATION= "org.eclipse.SmartFieldController.InfoAnnotation"; //$NON-NLS-1$

			private static String fgPlatform= SWT.getPlatform();
			private static boolean fgCarbon= "carbon".equals(fgPlatform); //$NON-NLS-1$
			private static boolean fgWin32= "win32".equals(fgPlatform); //$NON-NLS-1$

			private Image fBulb;
			private Shell fShell;
			private Hover fHover;
			private Control fHoverControl;
			private Image fErrorImage;
			private ErrorAnnotationFocusListener fErrorAnnotationFocusListener= new ErrorAnnotationFocusListener();
			
			
			/**
			 * Shows or clears a message for the given control.
			 * 
			 * @param control the control
			 * @param message the error message
			 */
			public static void setErrorMessage(Control control, String message) {
				getSmartFieldController(control).setInfo(control, message);
			}
			
			/**
			 * Installs or deinstalls a visual cue indicating availability of content assist on the given control.
			 * 
			 * @param control the control on which to install or uninstall the cue
			 * @param message the tool tip message or <code>null</code> to uninstall the cue
			 */
			public static void setSmartCue(Control control, String message) {
				getSmartFieldController(control).setSmartCue2(control, message);
			}

			//---- private implementation 
			
			private SmartFieldController(Shell shell) {
				fShell= shell;
				fShell.setData(FIELDCONTROLLER, this);
				
				Listener l= new Listener() {
					public void handleEvent(Event event) {
						switch (event.type) {
						case SWT.Resize:
						case SWT.Move:
							if (fHover != null)
								fHover.setLocation(fHoverControl);
							break;
						case SWT.Dispose:
							Object data= fShell.getData(FIELDCONTROLLER);
							if (data == SmartFieldController.this) {
								fShell.setData(FIELDCONTROLLER, null);
								handleDispose();
							}
							break;
						//case SWT.Activate:
						case SWT.Deactivate:
						case SWT.Close:
						case SWT.Iconify:
						//case SWT.Deiconify:
							updateHover2(null, null);
							break;
						}
					}
				};
				shell.addListener(SWT.Dispose, l);
				shell.addListener(SWT.Resize, l);
				shell.addListener(SWT.Move, l);
				//shell.addListener(SWT.Activate, l);
				shell.addListener(SWT.Close, l);
				shell.addListener(SWT.Deactivate, l);
				shell.addListener(SWT.Iconify, l);
				//shell.addListener(SWT.Deiconify, l);
			}
			
		 	private void handleDispose() {
		  		fShell= null;
				if (fHover != null) {
					fHover.dispose();
					fHover= null;
				}
				if (fBulb != null) {
					fBulb.dispose();
					fBulb= null;
				}
				if (fErrorImage != null) {
					fErrorImage.dispose();
					fErrorImage= null;
				}
			}
			
			private static SmartFieldController getSmartFieldController(Control control) {
				Shell shell= control.getShell();
				Object data= shell.getData(FIELDCONTROLLER);
				if (! (data instanceof SmartFieldController))
					data= new SmartFieldController(shell);
				return (SmartFieldController) data;
			}
				
			private void setSmartCue2(final Control control, String message) {

				SmartAnnotationFocusListener focuslistener= null;
				Object data= control.getData(SMARTFOCUSLISTENER);
				if (data instanceof SmartAnnotationFocusListener)
					focuslistener= (SmartAnnotationFocusListener)data;
				
				if (message != null) {		
					// add smart stuff
					Annotation annotation= null;
					if (focuslistener == null) {
						annotation= new Annotation(control, getLightBulb(), 10, message);
						focuslistener= new SmartAnnotationFocusListener(control, annotation);
						control.setData(SMARTFOCUSLISTENER, focuslistener);
						control.addFocusListener(focuslistener);
					} else {
						annotation= focuslistener.fAnnotation;
						annotation.fText= message;
					}
						
					if (hasFocus(control))
						addAnnotation(control, annotation);
				} else {
					// remove smart stuff
					Annotation annotation= null;
					if (focuslistener != null) {
						annotation= focuslistener.fAnnotation;
						control.removeFocusListener(focuslistener);
					}
					control.setData(SMARTFOCUSLISTENER, null);
						
					if (hasFocus(control) && annotation != null)
						removeAnnotation(control, annotation);
				}
			}

			private void setInfo(final Control control, String text) {
				if (text != null) {
					Annotation a= (Annotation) control.getData(INFO_ANNOTATION);
					if (a == null) {
						control.addFocusListener(fErrorAnnotationFocusListener);
						a= new Annotation(control, getErrorImage(), 0, text);
						control.setData(INFO_ANNOTATION, a);
						addAnnotation(control, a);
					} else {
						a.fText= text;	
					}
				} else {
					control.removeFocusListener(fErrorAnnotationFocusListener);
					Object data= control.getData(INFO_ANNOTATION);
					if (data instanceof Annotation) {
						removeAnnotation(control, (Annotation) data);
						control.setData(INFO_ANNOTATION, null);
					}
				}
				
				if (hasFocus(control))
					updateHover(control);
			}

			private void updateHover(Control control) {
				String text= null;
				Annotation a= (Annotation) control.getData(INFO_ANNOTATION);
				if (a != null)
					text= a.fText;
				updateHover2(control, text);
			}
				
			private void updateHover2(Control control, String text) {
				if (text != null) {
					fHoverControl= control;
					if (fHover == null)
						fHover= new Hover(fShell);
					fHover.setText(text);
					fHover.setLocation(fHoverControl);
					fHover.setVisible(true);
				} else {
					fHoverControl= null;
					if (fHover != null)
						fHover.setVisible(false);
				}
			}
			
			private boolean isTooltipVisible() {
				return fHover != null && fHover.isVisible();
			}
			
			private static boolean hasFocus(Control control) {
				return control.getDisplay().getFocusControl() == control;
			}
			
			private Image getLightBulb() {
				if (fBulb == null) {
					ImageDescriptor bulbID= ImageDescriptor.createFromFile(SmartFieldController.class, "images/content_assist_cue.gif"); //$NON-NLS-1$
					fBulb= bulbID.createImage(fShell.getDisplay());
				}
				return fBulb;
			}
			
			private Image getErrorImage() {
				if (fErrorImage == null) {
					ImageDescriptor errorID= ImageDescriptor.createFromFile(SmartFieldController.class, "images/error_small.gif"); //$NON-NLS-1$
					fErrorImage= errorID.createImage(fShell.getDisplay());
				}
				return fErrorImage;
			}
			
			private void addAnnotation(Control control, Annotation a) {
				for (Control c= control.getParent(); c != null; c= c.getParent()) {
					AnnotationHandler pl= (AnnotationHandler) c.getData(ANNOTATIONHANDLER);
					if (pl == null) {
						pl= new AnnotationHandler();
						c.addPaintListener(pl);
						c.addMouseTrackListener(pl);
						c.addMouseMoveListener(pl);
						c.setData(ANNOTATIONHANDLER, pl);
					}
					pl.add(a);
					c.redraw();
				}
			}
			
			private void removeAnnotation(Control control, Annotation a) {
				for (Control c= control.getParent(); c != null; c= c.getParent()) {
					AnnotationHandler pl= (AnnotationHandler) c.getData(ANNOTATIONHANDLER);
					if (pl != null) {
						if (pl.remove(a)) {
							c.removePaintListener(pl);
							c.removeMouseTrackListener(pl);
							c.removeMouseMoveListener(pl);	
							c.setData(ANNOTATIONHANDLER, null);
						}
					}
					c.redraw();
				}
			}
			
			static int fgCount;
			
			public static void main(String[] args) {
				Display display= new Display();
				Shell shell= new Shell(display);
				GridLayout gl= new GridLayout(2, false);
				shell.setLayout(gl);
				
				new Label(shell, SWT.NONE).setText("Smart Text:"); //$NON-NLS-1$
				final Text t= new Text(shell, SWT.BORDER);
				t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				t.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String tx= t.getText();
						if (tx.indexOf(' ') >= 0)
							SmartFieldController.setErrorMessage(t, "spaces not allowed in " + tx); //$NON-NLS-1$
						else
							SmartFieldController.setErrorMessage(t, null);					
					}
				});
				SmartFieldController.setSmartCue(t, "Content assist available"); //$NON-NLS-1$
				
				new Label(shell, SWT.NONE).setText("Smart Combo:"); //$NON-NLS-1$
				final Combo c= new Combo(shell, SWT.BORDER);
				c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				c.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String tx= c.getText();
						if (tx.indexOf('.') >= 0)
							SmartFieldController.setErrorMessage(c, "dots not allowed in " + tx); //$NON-NLS-1$
						else
							SmartFieldController.setErrorMessage(c, null);					
					}
				});
				SmartFieldController.setSmartCue(c, "Quick assist available"); //$NON-NLS-1$

				new Label(shell, SWT.NONE).setText("Dumb Text:"); //$NON-NLS-1$
				final Text tt= new Text(shell, SWT.BORDER);
				tt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				tt.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String tx= tt.getText();
						if (tx.indexOf(' ') >= 0)
							SmartFieldController.setErrorMessage(tt, "spaces not allowed in " + tx); //$NON-NLS-1$
						else
							SmartFieldController.setErrorMessage(tt, null);					
					}
				});

				new Label(shell, SWT.NONE).setText("Be Smart:"); //$NON-NLS-1$
				final Button b= new Button(shell, SWT.CHECK);
				b.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						boolean smart= b.getSelection();
						if (smart)
							SmartFieldController.setSmartCue(tt, "Content assist available " + fgCount++); //$NON-NLS-1$
						else
							SmartFieldController.setSmartCue(tt, null);
					}
				});
				
				
				shell.setLocation(200, 200);
				shell.open();
				
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
				display.dispose();
			}
		}
}
