/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.contentassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;

import org.eclipse.jface.text.IEventConsumer;


/**
 * An <code>AbstractControlContentAssistSubjectAdapter</code> delegates assistance requests from a
 * {@linkplain org.eclipse.jface.text.contentassist.ContentAssistant content assistant}
 * to a <code>Control</code>.
 *
 * A visual feedback can be configured via {@link #setContentAssistCueProvider(ILabelProvider)}.
 *
 * @since 3.0
 */
public abstract class AbstractControlContentAssistSubjectAdapter implements IContentAssistSubjectControl {

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
	 * {@link #fVerifyKeyListeners fVerifyKeyListeners} and {@link #fKeyListeners}.
	 */
	private Listener fControlListener;

	/**
	 * Creates a new {@link AbstractControlContentAssistSubjectAdapter}.
	 */
	public AbstractControlContentAssistSubjectAdapter() {
		fVerifyKeyListeners= new ArrayList(1);
		fKeyListeners= new HashSet(1);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#getControl()
	 */
	public abstract Control getControl();

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistSubjectControl#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener keyListener) {
		fKeyListeners.add(keyListener);

		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#addKeyListener()"); //$NON-NLS-1$

		installControlListener();
	}

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#removeKeyListener(org.eclipse.swt.events.KeyListener)
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

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#supportsVerifyKeyListener()
	 */
	public boolean supportsVerifyKeyListener() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#appendVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean appendVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(verifyKeyListener);

		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#appendVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$

		installControlListener();
		return true;
	}

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#prependVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public boolean prependVerifyKeyListener(final VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.add(0, verifyKeyListener);

		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#prependVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$

		installControlListener();
		return true;
	}

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#removeVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		fVerifyKeyListeners.remove(verifyKeyListener);

		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#removeVerifyKeyListener() -> " + fVerifyKeyListeners.size()); //$NON-NLS-1$

		uninstallControlListener();
	}

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#setEventConsumer(org.eclipse.jface.text.IEventConsumer)
	 */
	public void setEventConsumer(IEventConsumer eventConsumer) {
		// this is not supported
		if (DEBUG)
			System.out.println("AbstractControlContentAssistSubjectAdapter#setEventConsumer()"); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jface.contentassist.IContentAssistSubjectControl#getLineDelimiter()
	 */
	public String getLineDelimiter() {
		return System.getProperty("line.separator"); //$NON-NLS-1$
	}

	/**
	 * Installs <code>fControlListener</code>, which handles VerifyEvents and KeyEvents by
	 * passing them to {@link #fVerifyKeyListeners} and {@link #fKeyListeners}.
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
								dump("traverse OK", e, verifyEvent); //$NON-NLS-1$
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
							dump("keyDown OK", e, verifyEvent); //$NON-NLS-1$

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
			 * @param e the event
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
	 * Sets the visual feedback provider for content assist.
	 * The given {@link ILabelProvider} methods are called with
	 * {@link #getControl()} as argument.
	 *
	 * <ul>
	 *   <li><code>getImage(Object)</code> provides the visual cue image.
	 *     The image can maximally be 5 pixels wide and 8 pixels high.
	 *     If <code>getImage(Object)</code> returns <code>null</code>, a default image is used.
	 *   </li>
	 *   <li><code>getText(Object)</code> provides the hover info text.
	 *     It is shown when hovering over the cue image or the adapted {@link Control}.
	 *     No info text is shown if <code>getText(Object)</code> returns <code>null</code>.
	 *   </li>
	 * </ul>
	 * <p>
	 * The given {@link ILabelProvider} becomes owned by the {@link AbstractControlContentAssistSubjectAdapter},
	 * i.e. it gets disposed when the adapted {@link Control} is disposed
	 * or when another {@link ILabelProvider} is set.
	 * </p>
	 *
	 * @param labelProvider a {@link ILabelProvider}, or <code>null</code>
	 * 	if no visual feedback should be shown
	 */
	public void setContentAssistCueProvider(ILabelProvider labelProvider) {
		SmartFieldController.setSmartCue(getControl(), labelProvider);
	}

	/**
	 * The internal controller for cues and error messages on {@link Text} and
	 * {@link Combo} widgets.
	 */
	private static class SmartFieldController {

		/**
		 * An info Hover to display a message next to a {@link Control}.
		 */
		class Hover {
			/**
			 * Distance of info hover arrow from left side.
			 */
			private int HD= 10;
			/**
			 * Width of info hover arrow.
			 */
			private int HW= 8;
			/**
			 * Height of info hover arrow.
			 */
			private int HH= 10;
			/**
			 * Margin around info hover text.
			 */
			private int LABEL_MARGIN= 2;
			/**
			 * This info hover's shell.
			 */
			Shell fHoverShell;
			/**
			 * This info hover's shell region.
			 * @since 3.1.2
			 */
			Region fHoverRegion;
			/**
			 * The info hover text.
			 */
			String fText= ""; //$NON-NLS-1$

			Hover(Shell parent) {
				final Display display= parent.getDisplay();
				fHoverShell= new Shell(parent, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
				fHoverShell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				fHoverShell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
				fHoverShell.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent pe) {
						pe.gc.drawString(fText, LABEL_MARGIN, LABEL_MARGIN);
						if (!fgCarbon)
							pe.gc.drawPolygon(getPolygon(true));
					}
				});
				fHoverShell.addMouseListener(new MouseAdapter() {
					public void mouseDown(MouseEvent e) {
						showHover(null, null);
					}
				});
			}

			int[] getPolygon(boolean border) {
				Point e= getExtent();
				if (border)
					return new int[] { 0,0, e.x-1,0, e.x-1,e.y-1, HD+HW,e.y-1, HD+HW/2,e.y+HH-1, HD,e.y-1, 0,e.y-1, 0,0 };
				return new int[] { 0,0, e.x,  0, e.x,  e.y,   HD+HW,e.y,   HD+HW/2,e.y+HH,   HD,e.y,   0,e.y,   0,0 };
			}

			void dispose() {
				if (!fHoverShell.isDisposed())
					fHoverShell.dispose();
				if (fHoverRegion != null)
					fHoverRegion.dispose();
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
						Region oldRegion= fHoverRegion;
						fHoverRegion= new Region();
						fHoverRegion.add(getPolygon(false));
						fHoverShell.setRegion(fHoverRegion);
						oldRegion.dispose();
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
				e.x+= LABEL_MARGIN*2;
				e.y+= LABEL_MARGIN*2;
				return e;
			}
		}

		/**
		 * A single plain HoverHandler is registered for the content assist control.
		 * It handles mouse hover events to show/hide the info hover.
		 */
		class HoverHandler extends MouseTrackAdapter {
			/**
			 * The managing FieldFocusListener.
			 */
			FieldFocusListener fFieldFocusListener;
			/**
			 * Create a new HoverHandler.
			 *
			 * @param fieldFocusListener the field focus listener
			 */
			HoverHandler(FieldFocusListener fieldFocusListener) {
				fFieldFocusListener= fieldFocusListener;
			}
			/**
			 * @inheritDoc
			 */
			public void mouseHover(MouseEvent e) {
				handleMouseEvent(e);
			}
			/**
			 * @inheritDoc
			 */
			public void mouseExit(MouseEvent e) {
				if (isHoverVisible())
					fFieldFocusListener.doHideHover();
			}
			/**
			 * Subclasses may extend or reimplement this method.
			 * @param e
			 */
			void handleMouseEvent(MouseEvent e) {
				fFieldFocusListener.doShowHover();
			}
		}

		/**
		 * One CueHandler is registered per ancestor control of the content assist control.
		 * It paints the visual cue icon and handles mouse hover events to show/hide the info hover.
		 */
		class CueHandler extends HoverHandler implements PaintListener  {
			/**
			 * Create a new CueHandler.
			 *
			 * @param fieldFocusListener the field focus listener
			 */
			CueHandler(FieldFocusListener fieldFocusListener) {
				super(fieldFocusListener);
			}
			/**
			 * @inheritDoc
			 */
			public void paintControl(PaintEvent e) {
				fFieldFocusListener.paintControl(e);
			}
			/**
			 * Updates the hover.
			 *
			 * @param event the mouse event
			 */
			void handleMouseEvent(MouseEvent event) {
				fFieldFocusListener.updateHoverOnCue(event);
			}
		}

		class FieldFocusListener implements FocusListener {
			/**
			 * Put icon relative to this control.
			 */
			private Control fControl;
			/**
			 * The icon's horizontal screen distance from top-left corner of control (in pixels).
			 */
			private int fDx;
			/**
			 * The icon's vertical screen distance from top-left corner of control (in pixels).
			 */
			private int fDy;
			/**
			 * The HoverHandler (only when control has focus).
			 */
			private HoverHandler fHoverHandler;

			/**
			 * Create a new FieldFocusListener
			 * @param control the target control
			 */
			FieldFocusListener(Control control) {
				fControl= control;

				fDx= -5;
				fDy= 1;
				if (fgCarbon) {
					if (control instanceof Text) {
						fDy+= 3;
					} else if (control instanceof Combo) {
						fDx-= 4;
					}
				} else if (fgWin32) {
					if (control instanceof Text) {
						fDx-= 2;
						fDy-= 2;
					}
				}
			}

			/**
			 * Paint the cue image.
			 * @param e the PaintEvent
			 */
			void paintControl(PaintEvent e) {
				if (fControl.isDisposed())
					return;
				Image image= getCueImage(fControl);
				Point global= fControl.toDisplay(fDx, fDy);
				Point local= ((Control) e.widget).toControl(global);
				e.gc.drawImage(image, local.x, local.y);
			}

			/**
			 * Show/hide the hover.
			 * @param e the MouseEvent
			 */
			void updateHoverOnCue(MouseEvent e) {
				Image image= getCueImage(fControl);
				Rectangle r= image.getBounds();
				Point global= fControl.toDisplay(fDx, fDy);
				Point local= ((Control) e.widget).toControl(global);
				r.x= local.x;
				r.y= local.y;
				if (r.contains(e.x, e.y))
					doShowHover();
				else
					doHideHover();
			}

			/**
			 * Hide hover.
			 */
			private void doHideHover() {
				showHover(fControl, null);
			}

			/**
			 * Show hover.
			 */
			public void doShowHover() {
				showHover(fControl, fLabelProvider.getText(fControl));
			}

			/*
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained(FocusEvent e) {
				// install a CueHandler on every parent control
				if (DEBUG)
					System.out.println("Focus Gained: " + e.widget); //$NON-NLS-1$

				install();
			}

			/**
			 * Installs the cue and hover handlers.
			 *
			 * @since 3.1
			 */
			public void install() {
				if (fHoverHandler == null) {
					fHoverHandler= new HoverHandler(this);
					fControl.addMouseTrackListener(fHoverHandler);
				}

				Control c= fControl.getParent();
				while (c != null) {
					if (DEBUG)
						System.out.println("install CueHandler: " + c.toString()); //$NON-NLS-1$
					CueHandler cueHandler= new CueHandler(this);
					Assert.isTrue(c.getData(ANNOTATION_HANDLER) == null, "parent control has CueHandler: " + c.toString()); //$NON-NLS-1$
					c.setData(ANNOTATION_HANDLER, cueHandler);
					c.addPaintListener(cueHandler);
					c.addMouseTrackListener(cueHandler);
					c.redraw();
					if (c instanceof Shell)
						break;
					c= c.getParent();
				}
			}

			/*
			 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost(FocusEvent e) {
				if (DEBUG) {
					System.out.println("Focus Lost: " + e.widget + ", at:"); //$NON-NLS-1$ //$NON-NLS-2$
					Thread.dumpStack();
				}

				uninstall();
			}

			/**
			 * Uninstalls the cue and hover handlers.
			 *
			 * @since 3.1
			 */
			public void uninstall() {
				if (fHoverHandler != null)
					fControl.removeMouseTrackListener(fHoverHandler);
				
				doHideHover();

				Control c= fControl.getParent();
				while (c != null) {
					if (DEBUG)
						System.out.println("uninstall CueHandler: " + c.toString()); //$NON-NLS-1$
					CueHandler cueHandler= (CueHandler) c.getData(ANNOTATION_HANDLER);
					//workaround for bug 64052:
					if (cueHandler != null) {
						c.setData(ANNOTATION_HANDLER, null);
						c.removePaintListener(cueHandler);
						c.removeMouseTrackListener(cueHandler);
						c.redraw();
					}
					if (c instanceof Shell)
						break;
					c= c.getParent();
				}
			}
		}

		private static final String SMART_FIELD_CONTROLLER= "org.eclipse.SmartFieldController"; //$NON-NLS-1$
		private static final String SMART_FOCUS_LISTENER= "org.eclipse.SmartFieldController.smartFocusListener"; //$NON-NLS-1$
		private static final String ANNOTATION_HANDLER= "org.eclipse.SmartFieldController.annotationHandler"; //$NON-NLS-1$

		private static String fgPlatform= SWT.getPlatform();
		private static boolean fgCarbon= "carbon".equals(fgPlatform); //$NON-NLS-1$
		private static boolean fgWin32= "win32".equals(fgPlatform); //$NON-NLS-1$

		private Shell fShell;
		private ILabelProvider fLabelProvider;
		private Image fCueImage;
		private Hover fHover;
		private Control fHoverControl;

		/**
		 * Installs or de-installs a visual cue indicating availability of content assist on the given control.
		 * At most one cue and one hover info is shown at any point in time.
		 *
		 * @param control the control on which to install or uninstall the cue
		 * @param labelProvider the label provider or <code>null</code> to uninstall the cue
		 */
		public static void setSmartCue(Control control, ILabelProvider labelProvider) {
			getSmartFieldController(control).internalSetSmartCue(control, labelProvider);
		}

		//---- private implementation

		private SmartFieldController(Shell shell) {
			fShell= shell;
			fShell.setData(SMART_FIELD_CONTROLLER, this);

			Listener l= new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.Resize:
					case SWT.Move:
						if (fHover != null)
							fHover.setLocation(fHoverControl);
						break;
					case SWT.Dispose:
						Object data= fShell.getData(SMART_FIELD_CONTROLLER);
						if (data == SmartFieldController.this) {
							fShell.setData(SMART_FIELD_CONTROLLER, null);
							handleDispose();
						}
						break;
					//case SWT.Activate:
					case SWT.Deactivate:
					case SWT.Close:
					case SWT.Iconify:
					//case SWT.Deiconify:
						showHover(null, null);
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
			fHoverControl= null;
			if (fHover != null) {
				fHover.dispose();
				fHover= null;
			}
			if (fCueImage != null) {
				fCueImage.dispose();
				fCueImage= null;
			}
			if (fLabelProvider != null) {
				fLabelProvider.dispose();
				fLabelProvider= null;
			}
		}

		/**
		 * Gets the smart field controller from the given control's shell.
		 *
		 * @param control the control
		 * @return the smart field controller
		 */
		private static SmartFieldController getSmartFieldController(Control control) {
			Shell shell= control.getShell();
			Object data= shell.getData(SMART_FIELD_CONTROLLER);
			if (! (data instanceof SmartFieldController))
				data= new SmartFieldController(shell);
			return (SmartFieldController) data;
		}

		private void internalSetSmartCue(final Control control, ILabelProvider labelProvider) {
			if (fLabelProvider != null)
				fLabelProvider.dispose();

			fLabelProvider= labelProvider;

			FieldFocusListener focuslistener= (FieldFocusListener) control.getData(SMART_FOCUS_LISTENER);

			if (labelProvider != null) {
				// add smart stuff
				if (focuslistener == null) {
					focuslistener= new FieldFocusListener(control);
					control.setData(SMART_FOCUS_LISTENER, focuslistener);
					control.addFocusListener(focuslistener);
					if (control.isFocusControl())
						focuslistener.install();
				}
			} else {
				// remove smart stuff
				if (focuslistener != null) {
					control.removeFocusListener(focuslistener);
					control.setData(SMART_FOCUS_LISTENER, null);
					if (control.isFocusControl())
						focuslistener.uninstall();
				}

				if (fCueImage != null) {
					fCueImage.dispose();
					fCueImage= null;
				}
			}
		}
		/**
		 * Show or hide hover.
		 *
		 * @param control the control
		 * @param text a {@link String} to show in hover, or <code>null</code> to hide
		 */
		private void showHover(Control control, String text) {
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

		private boolean isHoverVisible() {
			return fHover != null && fHover.isVisible();
		}

		private Image getCueImage(Control control) {
			Image image= null;
			if (fLabelProvider != null)
				image= fLabelProvider.getImage(control);

			return image != null ? image : getCueImage();
		}

		private Image getCueImage() {
			if (fCueImage == null) {
				ImageDescriptor cueID= ImageDescriptor.createFromFile(SmartFieldController.class, "images/content_assist_cue.gif"); //$NON-NLS-1$
				fCueImage= cueID.createImage(fShell.getDisplay());
			}
			return fCueImage;
		}
	}
}
