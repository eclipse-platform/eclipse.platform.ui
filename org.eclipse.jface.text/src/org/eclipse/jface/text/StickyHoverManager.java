/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.util.Geometry;

import org.eclipse.jface.text.information.IInformationProviderExtension2;


/**
 * Implements a sticky hover control, i.e. a control that replaces a hover
 * with an enriched and focusable control.
 * <p>
 * The information control is made visible on request by calling
 * {@link #showInformationControl(Rectangle)}.
 * </p>
 * <p>
 * Clients usually instantiate and configure this class before using it. The configuration
 * must be consistent: This means the used {@link org.eclipse.jface.text.IInformationControlCreator}
 * must create an information control expecting information in the same format the configured
 * {@link org.eclipse.jface.text.information.IInformationProvider}s use to encode the information they provide.
 * </p>
 *
 * @since 3.4
 */
class StickyHoverManager extends AbstractInformationControlManager implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension, IInformationControlReplacer {

	/**
	 * Priority of the info controls managed by this sticky hover manager.
	 * <p>
	 * Note: Only applicable when info control does not have focus.
	 * -5 as value has been chosen in order to be beaten by the hovers of {@link TextViewerHoverManager}.
	 * </p>
	 */
	private static final int WIDGET_PRIORITY= -5;

	
	/**
	 * Default control creator.
	 */
	private static class DefaultInformationControlCreator extends AbstractReusableInformationControlCreator {
		public IInformationControl doCreateInformationControl(Shell shell) {
			int style= SWT.V_SCROLL | SWT.H_SCROLL;
			return new DefaultInformationControl(shell, SWT.RESIZE | SWT.TOOL, style, new HTMLTextPresenter(false));
		}
	}


	/**
	 * Internal information control closer. Listens to several events issued by its subject control
	 * and closes the information control when necessary.
	 */
	class Closer implements IInformationControlCloser, ControlListener, MouseListener, FocusListener, IViewportListener, KeyListener, Listener {
		//TODO: Catch 'Esc' key in fInformationControlToClose: Don't dispose, just hideInformationControl().
		// This would allow to reuse the information control also when the user explicitly closes it.
		
		//TODO: if subject control is a Scrollable, should add selection listeners to both scroll bars
		// (and remove the ViewPortListener, which only listens to vertical scrolling)

		/** The subject control. */
		private Control fSubjectControl;
		/** Indicates whether this closer is active. */
		private boolean fIsActive= false;
		/** The display. */
		private Display fDisplay;

		/*
		 * @see IInformationControlCloser#setSubjectControl(Control)
		 */
		public void setSubjectControl(Control control) {
			fSubjectControl= control;
		}

		/*
		 * @see IInformationControlCloser#setInformationControl(IInformationControl)
		 */
		public void setInformationControl(IInformationControl control) {
			// NOTE: we use fInformationControl from the outer class
		}

		/*
		 * @see IInformationControlCloser#start(Rectangle)
		 */
		public void start(Rectangle informationArea) {

			if (fIsActive)
				return;
			fIsActive= true;

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.addControlListener(this);
				fSubjectControl.addMouseListener(this);
				fSubjectControl.addFocusListener(this);
				fSubjectControl.addKeyListener(this);
			}

			// TODO: must check whether we need a
			
			if (fInformationControl != null)
				fInformationControl.addFocusListener(this);

			fTextViewer.addViewportListener(this);
			
			fDisplay= fSubjectControl.getDisplay();
			if (!fDisplay.isDisposed())
				fDisplay.addFilter(SWT.MouseMove, this);
		}

		/*
		 * @see IInformationControlCloser#stop()
		 */
		public void stop() {

			if (!fIsActive)
				return;
			fIsActive= false;

			fTextViewer.removeViewportListener(this);

			if (fInformationControl != null)
				fInformationControl.removeFocusListener(this);

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeFocusListener(this);
				fSubjectControl.removeKeyListener(this);
			}
			
			if (fDisplay != null && !fDisplay.isDisposed())
				fDisplay.removeFilter(SWT.MouseMove, this);

			fDisplay= null;
		}

		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		 public void controlResized(ControlEvent e) {
			 hideInformationControl();
		}

		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		 public void controlMoved(ControlEvent e) {
			 hideInformationControl();
		}

		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		 public void mouseDown(MouseEvent e) {
			 hideInformationControl();
		}

		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
		}

		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			hideInformationControl();
		}

		/*
		 * @see FocusListener#focusGained(FocusEvent)
		 */
		public void focusGained(FocusEvent e) {
		}

		/*
		 * @see FocusListener#focusLost(FocusEvent)
		 */
		 public void focusLost(FocusEvent e) {
			Display d= fSubjectControl.getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					if (fInformationControl == null || !fInformationControl.isFocusControl())
						hideInformationControl();
				}
			});
		}

		/*
		 * @see IViewportListenerListener#viewportChanged(int)
		 */
		public void viewportChanged(int topIndex) {
			hideInformationControl();
		}

		/*
		 * @see KeyListener#keyPressed(KeyEvent)
		 */
		public void keyPressed(KeyEvent e) {
			hideInformationControl();
		}

		/*
		 * @see KeyListener#keyReleased(KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
		}
		
		/*
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseMove) {
				if (!(event.widget instanceof Control))
					return;
				
				if (fInformationControl != null && !fInformationControl.isFocusControl() && fInformationControl instanceof IInformationControlExtension3) {
					IInformationControlExtension3 iControl3= (IInformationControlExtension3)fInformationControl;
					Rectangle controlBounds= iControl3.getBounds();
					if (controlBounds != null) {
						Point mouseLoc= event.display.map((Control) event.widget, null, event.x, event.y);
						int margin= getKeepUpMargin();
						Geometry.expand(controlBounds, margin, margin, margin, margin);
						if (!controlBounds.contains(mouseLoc)) {
							hideInformationControl();
						}
					}
					
				} else {
					/*
					 * TODO: need better understanding of why/if this is needed.
					 * Looks like the same panic code we have in org.eclipse.jface.text.AbstractHoverInformationControlManager.Closer.handleMouseMove(Event)
					 */
					if (fDisplay != null && !fDisplay.isDisposed())
						fDisplay.removeFilter(SWT.MouseMove, this);
				}
			}
		}
	}

	
	private TextViewer fTextViewer;
	private AbstractInformationControlManager fActiveReplacable;
	private Object fReplacableInformation;
	private Rectangle fReplaceableArea;
	private IInformationControlCreator fReplaceableControlCreator;

	
	/**
	 * Creates a new sticky hover manager.
	 * 
	 * @param textViewer the text viewer
	 */
	public StickyHoverManager(TextViewer textViewer) {
		super(new DefaultInformationControlCreator());
		
		fTextViewer= textViewer;
		setCloser(new Closer());
		takesFocusWhenVisible(false);
		
		install(fTextViewer.getTextWidget());
	}

	/*
	 * @see AbstractInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		if (fActiveReplacable != null) {
			setInformation(fReplacableInformation, fReplaceableArea);
			return;
		}
		
		if (DEBUG)
			System.out.println("StickyHover: no active replaceable"); //$NON-NLS-1$
	}

	/*
	 * @see AbstractInformationControlManager#showInformationControl(Rectangle)
	 */
	protected void showInformationControl(Rectangle subjectArea) {
		if (fTextViewer != null && fTextViewer.requestWidgetToken(this, WIDGET_PRIORITY))
			super.showInformationControl(subjectArea);
		else
			if (DEBUG)
				System.out.println("cancelled StickyHoverManager.showInformationControl(..): did not get widget token (with prio)"); //$NON-NLS-1$
	}

	/*
	 * @see AbstractInformationControlManager#hideInformationControl()
	 */
	protected void hideInformationControl() {
		try {
			super.hideInformationControl();
		} finally {
			if (fTextViewer != null)
				fTextViewer.releaseWidgetToken(this);
		}
	}

	/*
	 * @see AbstractInformationControlManager#handleInformationControlDisposed()
	 */
	protected void handleInformationControlDisposed() {
		try {
			super.handleInformationControlDisposed();
		} finally {
			if (fTextViewer != null)
				fTextViewer.releaseWidgetToken(this);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		hideInformationControl();
		if (DEBUG)
			System.out.println("StickyHoverManager gave up widget token (no prio)"); //$NON-NLS-1$
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner, int)
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		if (fInformationControl != null) {
			if (fInformationControl.isFocusControl()) {
				if (DEBUG)
					System.out.println("StickyHoverManager kept widget token (focused)"); //$NON-NLS-1$
				return false;
			} else if (priority > WIDGET_PRIORITY) {
				hideInformationControl();
				if (DEBUG)
					System.out.println("StickyHoverManager gave up widget token (prio)"); //$NON-NLS-1$
				return true;
			} else {
				if (DEBUG)
					System.out.println("StickyHoverManager kept widget token (prio)"); //$NON-NLS-1$
				return false;
			}
		}
		if (DEBUG)
			System.out.println("StickyHoverManager gave up widget token (no iControl)"); //$NON-NLS-1$
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#setFocus(org.eclipse.jface.text.IWidgetTokenOwner)
	 */
	public boolean setFocus(IWidgetTokenOwner owner) {
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlReplacer#replaceInformationControl(org.eclipse.jface.text.AbstractInformationControlManager)
	 */
	public void replaceInformationControl(
			AbstractInformationControlManager informationControlManager,
			Object information,
			Rectangle area) {
		
		try {
			fActiveReplacable= informationControlManager;
			fReplacableInformation= information;
			fReplaceableArea= area;
			
			ITextHover textHover= ((ITextViewerExtension2) fTextViewer).getCurrentTextHover(); //cast is OK (see constructor)
			fReplaceableControlCreator= null;
			if (textHover instanceof IInformationProviderExtension2) {
				//TODO: Where is this documented? It looks like IInformationProviderExtension2 should have been called ITextHoverExtension2.
				fReplaceableControlCreator= ((IInformationProviderExtension2)textHover).getInformationPresenterControlCreator();
			} else {
				if (DEBUG)
					System.out.println("StickyHoverManager#replaceInformationControl() couldn't get an IInformationControlCreator "); //$NON-NLS-1$
			}
			//TODO: setMargins(fActiveReplacable.getMargins());
			setSizeConstraints(60, 10, false, true); //TODO: copied from TextViewer.ensureHoverControlManagerInstalled()
			
			setCustomInformationControlCreator(fReplaceableControlCreator);
		
			showInformation();
		} finally {
			fActiveReplacable= null;
			fReplacableInformation= null;
			fReplaceableArea= null;
			setCustomInformationControlCreator(null); //TODO: unnecessary?
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlReplacer#getKeepUpMargin()
	 */
	public int getKeepUpMargin() {
		return 15;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlReplacer#getActiveReplaceable()
	 */
	public AbstractInformationControlManager getActiveReplaceable() {
		return fActiveReplacable;
	}
}
