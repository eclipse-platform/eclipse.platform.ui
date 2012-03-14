/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Guy Gurfinkel, guy.g@zend.com - [content assist][api] provide better access to ContentAssistant - https://bugs.eclipse.org/bugs/show_bug.cgi?id=169954
 *     Anton Leherbauer (Wind River Systems) - [content assist][api] ContentAssistEvent should contain information about auto activation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=193728
 *     Marcel Bruch, bruch@cs.tu-darmstadt.de - [content assist] Allow to re-sort proposals - https://bugs.eclipse.org/bugs/show_bug.cgi?id=350991
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.commands.IHandler;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.OpenStrategy;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.text.TextUtilities;


/**
 * The standard implementation of the <code>IContentAssistant</code> interface. Usually, clients
 * instantiate this class and configure it before using it.
 */
public class ContentAssistant implements IContentAssistant, IContentAssistantExtension, IContentAssistantExtension2, IContentAssistantExtension3, IContentAssistantExtension4, IWidgetTokenKeeper, IWidgetTokenKeeperExtension {



	/**
	 * Content assist command identifier for 'select next proposal'.
	 *
	 * @since 3.4
	 */
	public static final String SELECT_NEXT_PROPOSAL_COMMAND_ID= "org.eclipse.ui.edit.text.contentAssist.selectNextProposal"; //$NON-NLS-1$
	/**
	 * Content assist command identifier for 'select previous proposal'.
	 *
	 * @since 3.4
	 */
	public static final String SELECT_PREVIOUS_PROPOSAL_COMMAND_ID= "org.eclipse.ui.edit.text.contentAssist.selectPreviousProposal"; //$NON-NLS-1$


	/**
	 * A generic closer class used to monitor various interface events in order to determine whether
	 * content-assist should be terminated and all associated windows closed.
	 */
	class Closer implements ControlListener, MouseListener, FocusListener, DisposeListener, IViewportListener {

		/** The shell that a <code>ControlListener</code> is registered with. */
		private Shell fShell;
		/**
		 * The control that a <code>MouseListener</code>, a<code>FocusListener</code> and a
		 * <code>DisposeListener</code> are registered with.
		 */
		private Control fControl;

		/**
		 * Installs this closer on it's viewer's text widget.
		 */
		protected void install() {
			Control control= fContentAssistSubjectControlAdapter.getControl();
			fControl= control;
			if (Helper.okToUse(control)) {

				Shell shell= control.getShell();
				fShell= shell;
				shell.addControlListener(this);

				control.addMouseListener(this);
				control.addFocusListener(this);

				/*
				 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of
				 * Internal Errors
				 */
				control.addDisposeListener(this);
			}
			if (fViewer != null)
				fViewer.addViewportListener(this);
		}

		/**
		 * Uninstalls this closer from the viewer's text widget.
		 */
		protected void uninstall() {
			Control shell= fShell;
			fShell= null;
			if (Helper.okToUse(shell))
				shell.removeControlListener(this);

			Control control= fControl;
			fControl= null;
			if (Helper.okToUse(control)) {

				control.removeMouseListener(this);
				control.removeFocusListener(this);

				/*
				 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of
				 * Internal Errors
				 */
				control.removeDisposeListener(this);
			}

			if (fViewer != null)
				fViewer.removeViewportListener(this);
		}

		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		public void controlResized(ControlEvent e) {
			hide();
		}

		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		public void controlMoved(ControlEvent e) {
			hide();
		}

		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
			hide();
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
			hide();
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
			Control control= fControl;
			if (Helper.okToUse(control)) {
				Display d= control.getDisplay();
				if (d != null) {
					d.asyncExec(new Runnable() {
						public void run() {
							if (!fProposalPopup.hasFocus() && (fContextInfoPopup == null || !fContextInfoPopup.hasFocus()))
								hide();
						}
					});
				}
			}
		}

		/*
		 * @seeDisposeListener#widgetDisposed(DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {
			/*
			 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal
			 * Errors
			 */
			hide();
		}

		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int topIndex) {
			hide();
		}
	}

	/**
	 * An implementation of <code>IContentAssistListener</code>, this class is used to monitor
	 * key events in support of automatic activation of the content assistant. If enabled, the
	 * implementation utilizes a thread to watch for input characters matching the activation
	 * characters specified by the content assist processor, and if detected, will wait the
	 * indicated delay interval before activating the content assistant.
	 *
	 * @since 3.4 protected, was added in 2.1 as private class
	 */
	protected class AutoAssistListener extends KeyAdapter implements Runnable, VerifyKeyListener {

		private Thread fThread;
		private boolean fIsReset= false;
		private Object fMutex= new Object();
		private int fShowStyle;

		private final static int SHOW_PROPOSALS= 1;
		private final static int SHOW_CONTEXT_INFO= 2;

		protected AutoAssistListener() {
		}

		protected void start(int showStyle) {
			fShowStyle= showStyle;
			fThread= new Thread(this, JFaceTextMessages.getString("ContentAssistant.assist_delay_timer_name")); //$NON-NLS-1$
			fThread.start();
		}

		public void run() {
			try {
				while (true) {
					synchronized (fMutex) {
						if (fAutoActivationDelay != 0)
							fMutex.wait(fAutoActivationDelay);
						if (fIsReset) {
							fIsReset= false;
							continue;
						}
					}
					showAssist(fShowStyle);
					break;
				}
			} catch (InterruptedException e) {
			}
			fThread= null;
		}

		protected void reset(int showStyle) {
			synchronized (fMutex) {
				fShowStyle= showStyle;
				fIsReset= true;
				fMutex.notifyAll();
			}
		}

		protected void stop() {
			Thread threadToStop= fThread;
			if (threadToStop != null && threadToStop.isAlive())
				threadToStop.interrupt();
		}

		private boolean contains(char[] characters, char character) {
			if (characters != null) {
				for (int i= 0; i < characters.length; i++) {
					if (character == characters[i])
						return true;
				}
			}
			return false;
		}

		public void keyPressed(KeyEvent e) {
			// Only act on typed characters and ignore modifier-only events
			if (e.character == 0 && (e.keyCode & SWT.KEYCODE_BIT) == 0)
				return;

			if (e.character != 0 && (e.stateMask == SWT.ALT))
				return;

			// Only act on characters that are trigger candidates. This
			// avoids computing the model selection on every keystroke
			if (computeAllAutoActivationTriggers().indexOf(e.character) < 0) {
				stop();
				return;
			}

			int showStyle;
			int pos= fContentAssistSubjectControlAdapter.getSelectedRange().x;
			char[] activation;

			activation= fContentAssistSubjectControlAdapter.getCompletionProposalAutoActivationCharacters(ContentAssistant.this, pos);

			if (contains(activation, e.character) && !isProposalPopupActive())
				showStyle= SHOW_PROPOSALS;
			else {
				activation= fContentAssistSubjectControlAdapter.getContextInformationAutoActivationCharacters(ContentAssistant.this, pos);
				if (contains(activation, e.character) && !isContextInfoPopupActive())
					showStyle= SHOW_CONTEXT_INFO;
				else {
					stop();
					return;
				}
			}

			if (fThread != null && fThread.isAlive())
				reset(showStyle);
			else
				start(showStyle);
		}

		/*
		 * @see org.eclipse.swt.custom.VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
		 */
		public void verifyKey(VerifyEvent event) {
			keyPressed(event);
		}

		protected void showAssist(final int showStyle) {
			final Control control= fContentAssistSubjectControlAdapter.getControl();
			if (control == null)
				return;

			final Display d= control.getDisplay();
			if (d == null)
				return;

			try {
				d.syncExec(new Runnable() {
					public void run() {
						if (isProposalPopupActive())
							return;

						if (control.isDisposed() || !control.isFocusControl())
							return;

						if (showStyle == SHOW_PROPOSALS) {
							if (!prepareToShowCompletions(true))
								return;
							fProposalPopup.showProposals(true);
							fLastAutoActivation= System.currentTimeMillis();
						} else if (showStyle == SHOW_CONTEXT_INFO && fContextInfoPopup != null) {
							promoteKeyListener();
							fContextInfoPopup.showContextProposals(true);
						}
					}
				});
			} catch (SWTError e) {
			}
		}
	}

	/**
	 * The layout manager layouts the various windows associated with the content assistant based on
	 * the settings of the content assistant.
	 */
	class LayoutManager implements Listener {

		// Presentation types.
		/** The presentation type for the proposal selection popup. */
		public final static int LAYOUT_PROPOSAL_SELECTOR= 0;
		/** The presentation type for the context selection popup. */
		public final static int LAYOUT_CONTEXT_SELECTOR= 1;
		/** The presentation type for the context information hover . */
		public final static int LAYOUT_CONTEXT_INFO_POPUP= 2;

		int fContextType= LAYOUT_CONTEXT_SELECTOR;
		Shell[] fShells= new Shell[3];
		Object[] fPopups= new Object[3];

		protected void add(Object popup, Shell shell, int type, int offset) {
			Assert.isNotNull(popup);
			Assert.isTrue(shell != null && !shell.isDisposed());
			checkType(type);

			if (fShells[type] != shell) {
				if (fShells[type] != null)
					fShells[type].removeListener(SWT.Dispose, this);
				shell.addListener(SWT.Dispose, this);
				fShells[type]= shell;
			}

			fPopups[type]= popup;
			if (type == LAYOUT_CONTEXT_SELECTOR || type == LAYOUT_CONTEXT_INFO_POPUP)
				fContextType= type;

			layout(type, offset);
			adjustListeners(type);
		}

		protected void checkType(int type) {
			Assert.isTrue(type == LAYOUT_PROPOSAL_SELECTOR ||
				type == LAYOUT_CONTEXT_SELECTOR || type == LAYOUT_CONTEXT_INFO_POPUP);
		}

		public void handleEvent(Event event) {
			Widget source= event.widget;
			source.removeListener(SWT.Dispose, this);

			int type= getShellType(source);
			checkType(type);
			fShells[type]= null;

			switch (type) {
				case LAYOUT_PROPOSAL_SELECTOR:
					if (fContextType == LAYOUT_CONTEXT_SELECTOR &&
							Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
						// Restore event notification to the tip popup.
						addContentAssistListener((IContentAssistListener) fPopups[LAYOUT_CONTEXT_SELECTOR], CONTEXT_SELECTOR);
					}
					break;

				case LAYOUT_CONTEXT_SELECTOR:
					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						if (fProposalPopupOrientation == PROPOSAL_STACKED)
							layout(LAYOUT_PROPOSAL_SELECTOR, getSelectionOffset());
						// Restore event notification to the proposal popup.
						addContentAssistListener((IContentAssistListener) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);
					}
					fContextType= LAYOUT_CONTEXT_INFO_POPUP;
					break;

				case LAYOUT_CONTEXT_INFO_POPUP:
					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						if (fContextInfoPopupOrientation == CONTEXT_INFO_BELOW)
							layout(LAYOUT_PROPOSAL_SELECTOR, getSelectionOffset());
					}
					fContextType= LAYOUT_CONTEXT_SELECTOR;
					break;
			}
		}

		protected int getShellType(Widget shell) {
			for (int i= 0; i < fShells.length; i++) {
				if (fShells[i] == shell)
					return i;
			}
			return -1;
		}

		/**
		 * Layouts the popup defined by <code>type</code> at the given widget offset.
		 *
		 * @param type the kind of popup to layout
		 * @param offset the widget offset
		 */
		protected void layout(int type, int offset) {
			switch (type) {
				case LAYOUT_PROPOSAL_SELECTOR:
					layoutProposalSelector(offset);
					break;
				case LAYOUT_CONTEXT_SELECTOR:
					layoutContextSelector(offset);
					break;
				case LAYOUT_CONTEXT_INFO_POPUP:
					layoutContextInfoPopup(offset);
					break;
			}
		}

		protected void layoutProposalSelector(int offset) {
			if (fContextType == LAYOUT_CONTEXT_INFO_POPUP &&
					fContextInfoPopupOrientation == CONTEXT_INFO_BELOW &&
					Helper.okToUse(fShells[LAYOUT_CONTEXT_INFO_POPUP])) {
				// Stack proposal selector beneath the tip box.
				Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
				Shell parent= fShells[LAYOUT_CONTEXT_INFO_POPUP];
				shell.setLocation(getStackedLocation(shell, parent));
			} else if (fContextType != LAYOUT_CONTEXT_SELECTOR ||
						!Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
				// There are no other presentations to be concerned with,
				// so place the proposal selector beneath the cursor line.
				Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
				CompletionProposalPopup popup= (CompletionProposalPopup) fPopups[LAYOUT_PROPOSAL_SELECTOR];
				shell.setBounds(computeBoundsBelowAbove(shell, shell.getSize(), offset, popup));
			} else {
				CompletionProposalPopup popup= ((CompletionProposalPopup) fPopups[LAYOUT_PROPOSAL_SELECTOR]);
				switch (fProposalPopupOrientation) {
					case PROPOSAL_REMOVE: {
						// Remove the tip selector and place the
						// proposal selector beneath the cursor line.
						fShells[LAYOUT_CONTEXT_SELECTOR].dispose();
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						shell.setBounds(computeBoundsBelowAbove(shell, shell.getSize(), offset, popup));
						break;
					}
					case PROPOSAL_OVERLAY: {
						// Overlay the tip selector with the proposal selector.
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						shell.setBounds(computeBoundsBelowAbove(shell, shell.getSize(), offset, popup));
						break;
					}
					case PROPOSAL_STACKED: {
						// Stack the proposal selector beneath the tip selector.
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						Shell parent= fShells[LAYOUT_CONTEXT_SELECTOR];
						shell.setLocation(getStackedLocation(shell, parent));
						break;
					}
				}
			}
		}

		protected void layoutContextSelector(int offset) {
			// Always place the context selector beneath the cursor line.
			Shell shell= fShells[LAYOUT_CONTEXT_SELECTOR];
			shell.setBounds(computeBoundsBelowAbove(shell, shell.getSize(), offset, null));

			if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
				switch (fProposalPopupOrientation) {
					case PROPOSAL_REMOVE:
						// Remove the proposal selector.
						fShells[LAYOUT_PROPOSAL_SELECTOR].dispose();
						break;

					case PROPOSAL_OVERLAY:
						// The proposal selector has been overlaid by the tip selector.
						break;

					case PROPOSAL_STACKED: {
						// Stack the proposal selector beneath the tip selector.
						shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						Shell parent= fShells[LAYOUT_CONTEXT_SELECTOR];
						shell.setLocation(getStackedLocation(shell, parent));
						break;
					}
				}
			}
		}

		protected void layoutContextInfoPopup(int offset) {
			switch (fContextInfoPopupOrientation) {
				case CONTEXT_INFO_ABOVE: {
					// Place the popup above the cursor line.
					Shell shell= fShells[LAYOUT_CONTEXT_INFO_POPUP];
					shell.setBounds(computeBoundsAboveBelow(shell, shell.getSize(), offset));
					break;
				}
				case CONTEXT_INFO_BELOW: {
					// Place the popup beneath the cursor line.
					Shell parent= fShells[LAYOUT_CONTEXT_INFO_POPUP];
					parent.setBounds(computeBoundsBelowAbove(parent, parent.getSize(), offset, null));
					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						// Stack the proposal selector beneath the context info popup.
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						shell.setLocation(getStackedLocation(shell, parent));
					}
					break;
				}
			}
		}

		/**
		 * Moves <code>point</code> such that <code>rectangle</code> does not bleed outside of
		 * <code>bounds</code>. All coordinates must have the same reference.
		 *
		 * @param point the point to move if needed
		 * @param shellSize the size of the shell that may be moved
		 * @param bounds the bounds
		 * @since 3.3
		 */
		protected void constrainLocation(Point point, Point shellSize, Rectangle bounds) {
			if (point.x + shellSize.x > bounds.x + bounds.width)
				point.x= bounds.x + bounds.width - shellSize.x;

			if (point.x < bounds.x)
				point.x= bounds.x;

			if (point.y + shellSize.y > bounds.y + bounds.height)
				point.y= bounds.y + bounds.height - shellSize.y;

			if (point.y < bounds.y)
				point.y= bounds.y;
		}

		protected Rectangle constrainHorizontally(Rectangle rect, Rectangle bounds) {
			// clip width
			if (rect.width > bounds.width)
				rect.width= bounds.width;

			if (rect.x + rect.width > bounds.x + bounds.width)
				rect.x= bounds.x + bounds.width - rect.width;
			if (rect.x < bounds.x)
				rect.x= bounds.x;

			return rect;
		}

		/**
		 * Returns the display bounds for <code>shell</code> such that it appears right above
		 * <code>offset</code>, or below it if above is not suitable. The returned bounds lie
		 * within the monitor at the caret location and never overlap with the caret line.
		 *
		 * @param shell the shell to compute the placement for
		 * @param preferred the preferred size for <code>shell</code>
		 * @param offset the caret offset in the subject control
		 * @return the point right above <code>offset</code> in display coordinates
		 * @since 3.3
		 */
		protected Rectangle computeBoundsAboveBelow(Shell shell, Point preferred, int offset) {
			Control subjectControl= fContentAssistSubjectControlAdapter.getControl();
			Display display= subjectControl.getDisplay();
			Rectangle caret= getCaretRectangle(offset);
			Monitor monitor= getClosestMonitor(display, caret);
			Rectangle bounds= monitor.getClientArea();
			Geometry.moveInside(caret, bounds);

			int spaceAbove= caret.y - bounds.y;
			int caretLowerY= caret.y + caret.height;
			int spaceBelow= bounds.y + bounds.height - caretLowerY;
			Rectangle rect;
			if (spaceAbove >= preferred.y)
				rect= new Rectangle(caret.x, caret.y - preferred.y, preferred.x, preferred.y);
			else if (spaceBelow >= preferred.y)
				rect= new Rectangle(caret.x, caretLowerY, preferred.x, preferred.y);
			// we can't fit in the preferred size - squeeze into larger area
			else if (spaceBelow <= spaceAbove)
				rect= new Rectangle(caret.x, bounds.y, preferred.x, spaceAbove);
			else
				rect= new Rectangle(caret.x, caretLowerY, preferred.x, spaceBelow);

			return constrainHorizontally(rect, bounds);
		}

		/**
		 * Returns the display bounds for <code>shell</code> such that it appears right below
		 * <code>offset</code>, or above it if below is not suitable. The returned bounds lie
		 * within the monitor at the caret location and never overlap with the caret line.
		 *
		 * @param shell the shell to compute the placement for
		 * @param preferred the preferred size for <code>shell</code>
		 * @param offset the caret offset in the subject control
		 * @param popup a popup to inform if the location was switched to above, <code>null</code> to do nothing
		 * @return the point right below <code>offset</code> in display coordinates
		 * @since 3.3
		 */
		protected Rectangle computeBoundsBelowAbove(Shell shell, Point preferred, int offset, CompletionProposalPopup popup) {
			Control subjectControl= fContentAssistSubjectControlAdapter.getControl();
			Display display= subjectControl.getDisplay();
			Rectangle caret= getCaretRectangle(offset);
			Monitor monitor= getClosestMonitor(display, caret);
			Rectangle bounds= monitor.getClientArea();
			Geometry.moveInside(caret, bounds);

			int threshold= popup == null ? Integer.MAX_VALUE : popup.getMinimalHeight();
			int spaceAbove= caret.y - bounds.y;
			int spaceBelow= bounds.y + bounds.height - (caret.y + caret.height);
			Rectangle rect;
			boolean switched= false;
			if (spaceBelow >= preferred.y)
				rect= new Rectangle(caret.x, caret.y + caret.height, preferred.x, preferred.y);
			// squeeze in below if we have at least threshold space
			else if (spaceBelow >= threshold)
				rect= new Rectangle(caret.x, caret.y + caret.height, preferred.x, spaceBelow);
			else if (spaceAbove >= preferred.y) {
				rect= new Rectangle(caret.x, caret.y - preferred.y, preferred.x, preferred.y);
				switched= true;
			} else if (spaceBelow >= spaceAbove) {
				// we can't fit in the preferred size - squeeze into larger area
				rect= new Rectangle(caret.x, caret.y + caret.height, preferred.x, spaceBelow);
			} else {
				rect= new Rectangle(caret.x, bounds.y, preferred.x, spaceAbove);
				switched= true;
			}

			if (popup != null)
				popup.switchedPositionToAbove(switched);

			return constrainHorizontally(rect, bounds);
		}

		private Rectangle getCaretRectangle(int offset) {
			Point location= fContentAssistSubjectControlAdapter.getLocationAtOffset(offset);
			Control subjectControl= fContentAssistSubjectControlAdapter.getControl();
			Point controlSize= subjectControl.getSize();
			constrainLocation(location, new Point(0, 0), new Rectangle(0, 0, controlSize.x, controlSize.y));
			location= subjectControl.toDisplay(location);
			Rectangle subjectRectangle= new Rectangle(location.x, location.y, 1, fContentAssistSubjectControlAdapter.getLineHeight());
			return subjectRectangle;
		}

		protected Point getStackedLocation(Shell shell, Shell parent) {
			Point p= parent.getLocation();
			Point size= parent.getSize();
			p.x += size.x / 4;
			p.y += size.y;

			p= parent.toDisplay(p);

			Point shellSize= shell.getSize();
			Monitor monitor= getClosestMonitor(parent.getDisplay(), new Rectangle(p.x, p.y, 0, 0));
			Rectangle displayBounds= monitor.getClientArea();
			constrainLocation(p, shellSize, displayBounds);

			return p;
		}

		protected void adjustListeners(int type) {
			switch (type) {
				case LAYOUT_PROPOSAL_SELECTOR:
					if (fContextType == LAYOUT_CONTEXT_SELECTOR &&
							Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR]))
						// Disable event notification to the tip selector.
						removeContentAssistListener((IContentAssistListener) fPopups[LAYOUT_CONTEXT_SELECTOR], CONTEXT_SELECTOR);
					break;
				case LAYOUT_CONTEXT_SELECTOR:
					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR]))
						// Disable event notification to the proposal selector.
						removeContentAssistListener((IContentAssistListener) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);
					break;
				case LAYOUT_CONTEXT_INFO_POPUP:
					break;
			}
		}

		/**
		 * Copied from org.eclipse.jface.window.Window. Returns the monitor whose client area
		 * contains the given point. If no monitor contains the point, returns the monitor that is
		 * closest to the point. If this is ever made public, it should be moved into a separate
		 * utility class.
		 *
		 * @param toSearch point to find (display coordinates)
		 * @param rectangle rectangle to find (display coordinates)
		 * @return the monitor closest to the given point
		 * @since 3.3
		 */
		private Monitor getClosestMonitor(Display toSearch, Rectangle rectangle) {
			int closest = Integer.MAX_VALUE;

			Point toFind= Geometry.centerPoint(rectangle);
			Monitor[] monitors = toSearch.getMonitors();
			Monitor result = monitors[0];

			for (int idx = 0; idx < monitors.length; idx++) {
				Monitor current = monitors[idx];

				Rectangle clientArea = current.getClientArea();

				if (clientArea.contains(toFind)) {
					return current;
				}

				int distance = Geometry.distanceSquared(Geometry.centerPoint(clientArea), toFind);
				if (distance < closest) {
					closest = distance;
					result = current;
				}
			}

			return result;
		}
	}

	/**
	 * Internal key listener and event consumer.
	 */
	class InternalListener implements VerifyKeyListener, IEventConsumer {

		/**
		 * Verifies key events by notifying the registered listeners. Each listener is allowed to
		 * indicate that the event has been handled and should not be further processed.
		 *
		 * @param e the verify event
		 * @see VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
		 */
		public void verifyKey(VerifyEvent e) {
			IContentAssistListener[] listeners= (IContentAssistListener[]) fListeners.clone();
			for (int i= 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					if (!listeners[i].verifyKey(e) || !e.doit)
						break;
				}
			}
			if (fAutoAssistListener != null)
				fAutoAssistListener.keyPressed(e);
		}

		/*
		 * @see IEventConsumer#processEvent
		 */
		public void processEvent(VerifyEvent event) {

			installKeyListener();

			IContentAssistListener[] listeners= (IContentAssistListener[]) fListeners.clone();
			for (int i= 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					listeners[i].processEvent(event);
					if (!event.doit)
						return;
				}
			}
		}
	}

	/**
	 * Dialog store constants.
	 *
	 * @since 3.0
	 */
	public static final String STORE_SIZE_X= "size.x"; //$NON-NLS-1$
	public static final String STORE_SIZE_Y= "size.y"; //$NON-NLS-1$

	// Content-Assist Listener types
	final static int CONTEXT_SELECTOR= 0;
	final static int PROPOSAL_SELECTOR= 1;
	final static int CONTEXT_INFO_POPUP= 2;

	/**
	 * The popup priority: &gt; linked position proposals and hover pop-ups. Default value:
	 * <code>20</code>;
	 *
	 * @since 3.0
	 */
	public static final int WIDGET_PRIORITY= 20;
	private static final int DEFAULT_AUTO_ACTIVATION_DELAY= 500;


	private IInformationControlCreator fInformationControlCreator;
	private int fAutoActivationDelay= DEFAULT_AUTO_ACTIVATION_DELAY;
	private boolean fIsAutoActivated= false;
	private boolean fIsAutoInserting= false;
	private int fProposalPopupOrientation= PROPOSAL_OVERLAY;
	private int fContextInfoPopupOrientation= CONTEXT_INFO_ABOVE;
	private Map fProcessors;

	/**
	 * The partitioning.
	 *
	 * @since 3.0
	 */
	private String fPartitioning;

	private Color fContextInfoPopupBackground;
	private Color fContextInfoPopupForeground;
	private Color fContextSelectorBackground;
	private Color fContextSelectorForeground;
	private Color fProposalSelectorBackground;
	private Color fProposalSelectorForeground;

	private ITextViewer fViewer;
	private String fLastErrorMessage;

	private Closer fCloser;
	LayoutManager fLayoutManager;
	private AutoAssistListener fAutoAssistListener;
	private InternalListener fInternalListener;
	private CompletionProposalPopup fProposalPopup;
	private ContextInformationPopup fContextInfoPopup;

	/**
	 * Flag which tells whether a verify key listener is hooked.
	 *
	 * @since 3.0
	 */
	private boolean fVerifyKeyListenerHooked= false;
	private IContentAssistListener[] fListeners= new IContentAssistListener[4];
	/**
	 * The content assist subject control.
	 *
	 * @since 3.0
	 */
	private IContentAssistSubjectControl fContentAssistSubjectControl;
	/**
	 * The content assist subject control's shell.
	 *
	 * @since 3.2
	 */
	private Shell fContentAssistSubjectControlShell;
	/**
	 * The content assist subject control's shell traverse listener.
	 *
	 * @since 3.2
	 */
	private TraverseListener fCASCSTraverseListener;
	/**
	 * The content assist subject control adapter.
	 *
	 * @since 3.0
	 */
	private ContentAssistSubjectControlAdapter fContentAssistSubjectControlAdapter;
	/**
	 * The dialog settings for the control's bounds.
	 *
	 * @since 3.0
	 */
	private IDialogSettings fDialogSettings;
	/**
	 * Prefix completion setting.
	 *
	 * @since 3.0
	 */
	private boolean fIsPrefixCompletionEnabled= false;
	/**
	 * The list of completion listeners.
	 *
	 * @since 3.2
	 */
	private ListenerList fCompletionListeners= new ListenerList(ListenerList.IDENTITY);
	/**
	 * The message to display at the bottom of the proposal popup.
	 *
	 * @since 3.2
	 */
	private String fMessage= ""; //$NON-NLS-1$
	/**
	 * The cycling mode property.
	 *
	 * @since 3.2
	 */
	private boolean fIsRepetitionMode= false;
	/**
	 * The show empty property.
	 *
	 * @since 3.2
	 */
	private boolean fShowEmptyList= false;
	/**
	 * The message line property.
	 *
	 * @since 3.2
	 */
	private boolean fIsStatusLineVisible;
	/**
	 * The last system time when auto activation performed.
	 *
	 * @since 3.2
	 */
	private long fLastAutoActivation= Long.MIN_VALUE;
	/**
	 * The iteration key sequence to listen for, or <code>null</code>.
	 *
	 * @since 3.2
	 */
	private KeySequence fRepeatedInvocationKeySequence;

	/**
	 * Maps handler to command identifiers.
	 *
	 * @since 3.4
	 */
	private Map fHandlers;

	/**
	 * Tells whether colored labels support is enabled.
	 *
	 * @since 3.4
	 */
	private boolean fIsColoredLabelsSupportEnabled= false;

	/**
	 * The sorter to be used for sorting the proposals or <code>null</code> if no sorting is
	 * requested.
	 * 
	 * @since 3.8
	 */
	private ICompletionProposalSorter fSorter;

	/**
	 * Creates a new content assistant. The content assistant is not automatically activated,
	 * overlays the completion proposals with context information list if necessary, and shows the
	 * context information above the location at which it was activated. If auto activation will be
	 * enabled, without further configuration steps, this content assistant is activated after a 500
	 * milliseconds delay. It uses the default partitioning.
	 */
	public ContentAssistant() {
		fPartitioning= IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	/**
	 * Sets the document partitioning this content assistant is using.
	 *
	 * @param partitioning the document partitioning for this content assistant
	 * @since 3.0
	 */
	public void setDocumentPartitioning(String partitioning) {
		Assert.isNotNull(partitioning);
		fPartitioning= partitioning;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension#getDocumentPartitioning()
	 * @since 3.0
	 */
	public String getDocumentPartitioning() {
		return fPartitioning;
	}

	/**
	 * Registers a given content assist processor for a particular content type. If there is already
	 * a processor registered for this type, the new processor is registered instead of the old one.
	 *
	 * @param processor the content assist processor to register, or <code>null</code> to remove
	 *        an existing one
	 * @param contentType the content type under which to register
	 */
	public void setContentAssistProcessor(IContentAssistProcessor processor, String contentType) {

		Assert.isNotNull(contentType);

		if (fProcessors == null)
			fProcessors= new HashMap();

		if (processor == null)
			fProcessors.remove(contentType);
		else
			fProcessors.put(contentType, processor);
	}

	/*
	 * @see IContentAssistant#getContentAssistProcessor
	 */
	public IContentAssistProcessor getContentAssistProcessor(String contentType) {
		if (fProcessors == null)
			return null;

		return (IContentAssistProcessor) fProcessors.get(contentType);
	}

	/**
	 * Computes the sorted set of all auto activation trigger characters.
	 *
	 * @return the sorted set of all auto activation trigger characters
	 * @since 3.1
	 */
	private String computeAllAutoActivationTriggers() {
		if (fProcessors == null)
			return ""; //$NON-NLS-1$

		StringBuffer buf= new StringBuffer(5);
		Iterator iter= fProcessors.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry= (Entry) iter.next();
			IContentAssistProcessor processor= (IContentAssistProcessor) entry.getValue();
			char[] triggers= processor.getCompletionProposalAutoActivationCharacters();
			if (triggers != null)
				buf.append(triggers);
			triggers= processor.getContextInformationAutoActivationCharacters();
			if (triggers != null)
				buf.append(triggers);
		}
		return buf.toString();
	}

	/**
	 * Enables the content assistant's auto activation mode.
	 *
	 * @param enabled indicates whether auto activation is enabled or not
	 */
	public void enableAutoActivation(boolean enabled) {
		fIsAutoActivated= enabled;
		manageAutoActivation(fIsAutoActivated);
	}

	/**
	 * Enables the content assistant's auto insertion mode. If enabled, the content assistant
	 * inserts a proposal automatically if it is the only proposal. In the case of ambiguities, the
	 * user must make the choice.
	 *
	 * @param enabled indicates whether auto insertion is enabled or not
	 * @since 2.0
	 */
	public void enableAutoInsert(boolean enabled) {
		fIsAutoInserting= enabled;
	}

	/**
	 * Returns whether this content assistant is in the auto insertion mode or not.
	 *
	 * @return <code>true</code> if in auto insertion mode
	 * @since 2.0
	 */
	boolean isAutoInserting() {
		return fIsAutoInserting;
	}

	/**
	 * Installs and uninstall the listeners needed for auto activation.
	 *
	 * @param start <code>true</code> if listeners must be installed, <code>false</code> if they
	 *        must be removed
	 * @since 2.0
	 */
	private void manageAutoActivation(boolean start) {
		if (start) {

			if ((fContentAssistSubjectControlAdapter != null) && fAutoAssistListener == null) {
				fAutoAssistListener= createAutoAssistListener();
				// For details see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49212
				if (fContentAssistSubjectControlAdapter.supportsVerifyKeyListener())
					fContentAssistSubjectControlAdapter.appendVerifyKeyListener(fAutoAssistListener);
				else
					fContentAssistSubjectControlAdapter.addKeyListener(fAutoAssistListener);
			}

		} else if (fAutoAssistListener != null) {
			// For details see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49212
			if (fContentAssistSubjectControlAdapter.supportsVerifyKeyListener())
				fContentAssistSubjectControlAdapter.removeVerifyKeyListener(fAutoAssistListener);
			else
				fContentAssistSubjectControlAdapter.removeKeyListener(fAutoAssistListener);
			fAutoAssistListener= null;
		}
	}

	/**
	 * This method allows subclasses to provide their own {@link AutoAssistListener}.
	 *
	 * @return a new auto assist listener
	 * @since 3.4
	 */
	protected AutoAssistListener createAutoAssistListener() {
		return new AutoAssistListener();
	}

	/**
	 * Sets the delay after which the content assistant is automatically invoked if the cursor is
	 * behind an auto activation character.
	 * 
	 * @param delay the auto activation delay (as of 3.6 a negative argument will be set to 0)
	 */
	public void setAutoActivationDelay(int delay) {
		fAutoActivationDelay= Math.max(0, delay);
	}

	/**
	 * Gets the delay after which the content assistant is automatically invoked if the cursor is
	 * behind an auto activation character.
	 * 
	 * @return the auto activation delay (will not be negative)
	 * @since 3.4
	 */
	public int getAutoActivationDelay() {
		return fAutoActivationDelay;
	}

	/**
	 * Sets the proposal pop-ups' orientation. The following values may be used:
	 * <ul>
	 *   <li>PROPOSAL_OVERLAY<p>
	 *     proposal popup windows should overlay each other
	 *   </li>
	 *   <li>PROPOSAL_REMOVE<p>
	 *     any currently shown proposal popup should be closed
	 *   </li>
	 *   <li>PROPOSAL_STACKED<p>
	 *     proposal popup windows should be vertical stacked, with no overlap,
	 *     beneath the line containing the current cursor location
	 *   </li>
	 * </ul>
	 *
	 * @param orientation the popup's orientation
	 */
	public void setProposalPopupOrientation(int orientation) {
		fProposalPopupOrientation= orientation;
	}

	/**
	 * Sets the context information popup's orientation.
	 * The following values may be used:
	 * <ul>
	 *   <li>CONTEXT_ABOVE<p>
	 *     context information popup should always appear above the line containing
	 *     the current cursor location
	 *   </li>
	 *   <li>CONTEXT_BELOW<p>
	 *     context information popup should always appear below the line containing
	 *     the current cursor location
	 *   </li>
	 * </ul>
	 *
	 * @param orientation the popup's orientation
	 */
	public void setContextInformationPopupOrientation(int orientation) {
		fContextInfoPopupOrientation= orientation;
	}

	/**
	 * Sets the context information popup's background color.
	 *
	 * @param background the background color
	 */
	public void setContextInformationPopupBackground(Color background) {
		fContextInfoPopupBackground= background;
	}

	/**
	 * Returns the background of the context information popup.
	 *
	 * @return the background of the context information popup
	 * @since 2.0
	 */
	Color getContextInformationPopupBackground() {
		return fContextInfoPopupBackground;
	}

	/**
	 * Sets the context information popup's foreground color.
	 *
	 * @param foreground the foreground color
	 * @since 2.0
	 */
	public void setContextInformationPopupForeground(Color foreground) {
		fContextInfoPopupForeground= foreground;
	}

	/**
	 * Returns the foreground of the context information popup.
	 *
	 *
	 * @return the foreground of the context information popup
	 * @since 2.0
	 */
	Color getContextInformationPopupForeground() {
		return fContextInfoPopupForeground;
	}

	/**
	 * Sets the proposal selector's background color.
	 * <p>
	 * <strong>Note:</strong> As of 3.4, you should only call this
	 * method if you want to override the {@link JFacePreferences#CONTENT_ASSIST_BACKGROUND_COLOR}.
	 * </p>
	 *
	 * @param background the background color
	 * @since 2.0
	 */
	public void setProposalSelectorBackground(Color background) {
		fProposalSelectorBackground= background;
	}

	/**
	 * Returns the custom background color of the proposal selector.
	 *
	 * @return the background of the proposal selector or <code>null</code> if not set
	 * @since 2.0
	 */
	Color getProposalSelectorBackground() {
		return fProposalSelectorBackground;
	}

	/**
	 * Sets the proposal's foreground color.
	 * <p>
	 * <strong>Note:</strong> As of 3.4, you should only call this
	 * method if you want to override the {@link JFacePreferences#CONTENT_ASSIST_FOREGROUND_COLOR}.
	 * </p>
	 *
	 * @param foreground the foreground color
	 * @since 2.0
	 */
	public void setProposalSelectorForeground(Color foreground) {
		fProposalSelectorForeground= foreground;
	}

	/**
	 * Returns the custom foreground color of the proposal selector.
	 *
	 * @return the foreground of the proposal selector or <code>null</code> if not set
	 * @since 2.0
	 */
	Color getProposalSelectorForeground() {
		return fProposalSelectorForeground;
	}

	/**
	 * Sets the context selector's background color.
	 *
	 * @param background the background color
	 * @since 2.0
	 */
	public void setContextSelectorBackground(Color background) {
		fContextSelectorBackground= background;
	}

	/**
	 * Returns the background of the context selector.
	 *
	 * @return the background of the context selector
	 * @since 2.0
	 */
	Color getContextSelectorBackground() {
		return fContextSelectorBackground;
	}

	/**
	 * Sets the context selector's foreground color.
	 *
	 * @param foreground the foreground color
	 * @since 2.0
	 */
	public void setContextSelectorForeground(Color foreground) {
		fContextSelectorForeground= foreground;
	}

	/**
	 * Returns the foreground of the context selector.
	 *
	 * @return the foreground of the context selector
	 * @since 2.0
	 */
	Color getContextSelectorForeground() {
		return fContextSelectorForeground;
	}

	/**
	 * Sets the information control creator for the additional information control.
	 *
	 * @param creator the information control creator for the additional information control
	 * @since 2.0
	 */
	public void setInformationControlCreator(IInformationControlCreator creator) {
		fInformationControlCreator= creator;
	}

	/*
	 * @see IControlContentAssistant#install(IContentAssistSubjectControl)
	 * @since 3.0
	 */
	protected void install(IContentAssistSubjectControl contentAssistSubjectControl) {
		fContentAssistSubjectControl= contentAssistSubjectControl;
		fContentAssistSubjectControlAdapter= new ContentAssistSubjectControlAdapter(fContentAssistSubjectControl);
		install();
	}

	/*
	 * @see IContentAssist#install
	 * @since 3.0
	 */
	public void install(ITextViewer textViewer) {
		fViewer= textViewer;
		fContentAssistSubjectControlAdapter= new ContentAssistSubjectControlAdapter(fViewer);
		install();
	}

	protected void install() {

		fLayoutManager= new LayoutManager();
		fInternalListener= new InternalListener();

		AdditionalInfoController controller= null;
		if (fInformationControlCreator != null)
			controller= new AdditionalInfoController(fInformationControlCreator, OpenStrategy.getPostSelectionDelay());

		fContextInfoPopup= fContentAssistSubjectControlAdapter.createContextInfoPopup(this);
		fProposalPopup= fContentAssistSubjectControlAdapter.createCompletionProposalPopup(this, controller);
		fProposalPopup.setSorter(fSorter);

		registerHandler(SELECT_NEXT_PROPOSAL_COMMAND_ID, fProposalPopup.createProposalSelectionHandler(CompletionProposalPopup.ProposalSelectionHandler.SELECT_NEXT));
		registerHandler(SELECT_PREVIOUS_PROPOSAL_COMMAND_ID, fProposalPopup.createProposalSelectionHandler(CompletionProposalPopup.ProposalSelectionHandler.SELECT_PREVIOUS));

		if (Helper.okToUse(fContentAssistSubjectControlAdapter.getControl())) {
			fContentAssistSubjectControlShell= fContentAssistSubjectControlAdapter.getControl().getShell();
			fCASCSTraverseListener= new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_ESCAPE && isProposalPopupActive())
						e.doit= false;
				}
			};
			fContentAssistSubjectControlShell.addTraverseListener(fCASCSTraverseListener);
		}

		manageAutoActivation(fIsAutoActivated);
	}

	/*
	 * @see IContentAssist#uninstall
	 */
	public void uninstall() {
		hide();
		manageAutoActivation(false);

		if (fHandlers != null) {
			fHandlers.clear();
			fHandlers= null;
		}

		if (fCloser != null) {
			fCloser.uninstall();
			fCloser= null;
		}

		if (Helper.okToUse(fContentAssistSubjectControlShell))
			fContentAssistSubjectControlShell.removeTraverseListener(fCASCSTraverseListener);
		fCASCSTraverseListener= null;
		fContentAssistSubjectControlShell= null;

		fViewer= null;
		fContentAssistSubjectControl= null;
		fContentAssistSubjectControlAdapter= null;
	}

	/**
	 * Adds the given shell of the specified type to the layout. Valid types are defined by
	 * <code>LayoutManager</code>.
	 *
	 * @param popup a content assist popup
	 * @param shell the shell of the content-assist popup
	 * @param type the type of popup
	 * @param visibleOffset the offset at which to layout the popup relative to the offset of the
	 *        viewer's visible region
	 * @since 2.0
	 */
	void addToLayout(Object popup, Shell shell, int type, int visibleOffset) {
		fLayoutManager.add(popup, shell, type, visibleOffset);
	}

	/**
	 * Layouts the registered popup of the given type relative to the given offset. The offset is
	 * relative to the offset of the viewer's visible region. Valid types are defined by
	 * <code>LayoutManager</code>.
	 *
	 * @param type the type of popup to layout
	 * @param visibleOffset the offset at which to layout relative to the offset of the viewer's
	 *        visible region
	 * @since 2.0
	 */
	void layout(int type, int visibleOffset) {
		fLayoutManager.layout(type, visibleOffset);
	}

	/**
	 * Returns the layout manager.
	 *
	 * @return the layout manager
	 * @since 3.3
	 */
	LayoutManager getLayoutManager() {
		return fLayoutManager;
	}

	/**
	 * Notifies the controller that a popup has lost focus.
	 *
	 * @param e the focus event
	 */
	void popupFocusLost(FocusEvent e) {
		fCloser.focusLost(e);
	}

	/**
	 * Returns the offset of the selection relative to the offset of the visible region.
	 *
	 * @return the offset of the selection relative to the offset of the visible region
	 * @since 2.0
	 */
	int getSelectionOffset() {
		return fContentAssistSubjectControlAdapter.getWidgetSelectionRange().x;
	}

	/**
	 * Returns whether the widget token could be acquired. The following are valid listener types:
	 * <ul>
	 *   <li>AUTO_ASSIST</li>
	 *   <li>CONTEXT_SELECTOR</li>
	 *   <li>PROPOSAL_SELECTOR</li>
	 *   <li>CONTEXT_INFO_POPUP</li>
	 * </ul>
	 *
	 * @param type the listener type for which to acquire
	 * @return <code>true</code> if the widget token could be acquired
	 * @since 2.0
	 */
	private boolean acquireWidgetToken(int type) {
		switch (type) {
			case CONTEXT_SELECTOR:
			case PROPOSAL_SELECTOR:
				if (fContentAssistSubjectControl instanceof IWidgetTokenOwnerExtension) {
					IWidgetTokenOwnerExtension extension= (IWidgetTokenOwnerExtension) fContentAssistSubjectControl;
					return extension.requestWidgetToken(this, WIDGET_PRIORITY);
				} else if (fContentAssistSubjectControl instanceof IWidgetTokenOwner) {
					IWidgetTokenOwner owner= (IWidgetTokenOwner) fContentAssistSubjectControl;
					return owner.requestWidgetToken(this);
				} else if (fViewer instanceof IWidgetTokenOwnerExtension) {
					IWidgetTokenOwnerExtension extension= (IWidgetTokenOwnerExtension) fViewer;
					return extension.requestWidgetToken(this, WIDGET_PRIORITY);
				} else if (fViewer instanceof IWidgetTokenOwner) {
					IWidgetTokenOwner owner= (IWidgetTokenOwner) fViewer;
					return owner.requestWidgetToken(this);
				}
		}
		return true;
	}

	/**
	 * Registers a content assist listener. The following are valid listener types:
	 * <ul>
	 *   <li>AUTO_ASSIST</li>
	 *   <li>CONTEXT_SELECTOR</li>
	 *   <li>PROPOSAL_SELECTOR</li>
	 *   <li>CONTEXT_INFO_POPUP</li>
	 * </ul>
	 * Returns whether the listener could be added successfully. A listener can not be added if the
	 * widget token could not be acquired.
	 *
	 * @param listener the listener to register
	 * @param type the type of listener
	 * @return <code>true</code> if the listener could be added
	 */
	boolean addContentAssistListener(IContentAssistListener listener, int type) {

		if (acquireWidgetToken(type)) {

			fListeners[type]= listener;

			if (fCloser == null && getNumberOfListeners() == 1) {
				fCloser= new Closer();
				fCloser.install();
				fContentAssistSubjectControlAdapter.setEventConsumer(fInternalListener);
				installKeyListener();
			} else
				promoteKeyListener();
			return true;
		}

		return false;
	}

	/**
	 * Re-promotes the key listener to the first position, using prependVerifyKeyListener. This
	 * ensures no other instance is filtering away the keystrokes underneath, if we've been up for a
	 * while (e.g. when the context info is showing.
	 *
	 * @since 3.0
	 */
	private void promoteKeyListener() {
		uninstallVerifyKeyListener();
		installKeyListener();
	}

	/**
	 * Installs a key listener on the text viewer's widget.
	 */
	private void installKeyListener() {
		if (!fVerifyKeyListenerHooked) {
			if (Helper.okToUse(fContentAssistSubjectControlAdapter.getControl())) {
				fVerifyKeyListenerHooked= fContentAssistSubjectControlAdapter.prependVerifyKeyListener(fInternalListener);
			}
		}
	}

	/**
	 * Releases the previously acquired widget token if the token is no longer necessary. The
	 * following are valid listener types:
	 * <ul>
	 *   <li>AUTO_ASSIST</li>
	 *   <li>CONTEXT_SELECTOR</li>
	 *   <li>PROPOSAL_SELECTOR</li>
	 *   <li>CONTEXT_INFO_POPUP</li>
	 * </ul>
	 *
	 * @param type the listener type
	 * @since 2.0
	 */
	private void releaseWidgetToken(int type) {
		if (fListeners[CONTEXT_SELECTOR] == null && fListeners[PROPOSAL_SELECTOR] == null) {
			IWidgetTokenOwner owner= null;
			if (fContentAssistSubjectControl instanceof IWidgetTokenOwner)
				owner= (IWidgetTokenOwner) fContentAssistSubjectControl;
			else if (fViewer instanceof IWidgetTokenOwner)
				owner= (IWidgetTokenOwner) fViewer;
			if (owner != null)
				owner.releaseWidgetToken(this);
		}
	}

	/**
	 * Unregisters a content assist listener.
	 *
	 * @param listener the listener to unregister
	 * @param type the type of listener
	 * @see #addContentAssistListener(IContentAssistListener, int)
	 */
	void removeContentAssistListener(IContentAssistListener listener, int type) {
		fListeners[type]= null;

		if (getNumberOfListeners() == 0) {

			if (fCloser != null) {
				fCloser.uninstall();
				fCloser= null;
			}

			uninstallVerifyKeyListener();
			fContentAssistSubjectControlAdapter.setEventConsumer(null);
		}

		releaseWidgetToken(type);
	}

	/**
	 * Uninstall the key listener from the text viewer's widget.
	 *
	 * @since 3.0
	 */
	private void uninstallVerifyKeyListener() {
		if (fVerifyKeyListenerHooked) {
			if (Helper.okToUse(fContentAssistSubjectControlAdapter.getControl()))
				fContentAssistSubjectControlAdapter.removeVerifyKeyListener(fInternalListener);
			fVerifyKeyListenerHooked= false;
		}
	}

	/**
	 * Returns the number of listeners.
	 *
	 * @return the number of listeners
	 * @since 2.0
	 */
	private int getNumberOfListeners() {
		int count= 0;
		for (int i= 0; i <= CONTEXT_INFO_POPUP; i++) {
			if (fListeners[i] != null)
				++count;
		}
		return count;
	}

	/*
	 * @see IContentAssist#showPossibleCompletions
	 */
	public String showPossibleCompletions() {
		if (!prepareToShowCompletions(false))
			return null;
		if (fIsPrefixCompletionEnabled)
			return fProposalPopup.incrementalComplete();
		return fProposalPopup.showProposals(false);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension#completePrefix()
	 * @since 3.0
	 */
	public String completePrefix() {
		if (!prepareToShowCompletions(false))
			return null;
		return fProposalPopup.incrementalComplete();
	}

	/**
	 * Prepares to show content assist proposals. It returns false if auto activation has kicked in
	 * recently.
	 *
	 * @param isAutoActivated  whether completion was triggered by auto activation
	 * @return <code>true</code> if the caller should continue and show the proposals,
	 *         <code>false</code> otherwise.
	 * @since 3.2
	 */
	private boolean prepareToShowCompletions(boolean isAutoActivated) {
		if (!isAutoActivated) {
			int gracePeriod= Math.max(fAutoActivationDelay, 200);
			if (System.currentTimeMillis() < fLastAutoActivation + gracePeriod) {
				return false;
			}
		}
		
		promoteKeyListener();
		fireSessionBeginEvent(isAutoActivated);
		return true;
	}

	/**
	 * Callback to signal this content assistant that the presentation of the possible completions
	 * has been stopped.
	 *
	 * @since 2.1
	 */
	protected void possibleCompletionsClosed() {
		fLastAutoActivation= Long.MIN_VALUE;
		storeCompletionProposalPopupSize();
	}

	/*
	 * @see IContentAssist#showContextInformation
	 */
	public String showContextInformation() {
		promoteKeyListener();
		if (fContextInfoPopup != null)
			return fContextInfoPopup.showContextProposals(false);
		return null;
	}

	/**
	 * Callback to signal this content assistant that the presentation of the context information
	 * has been stopped.
	 *
	 * @since 2.1
	 */
	protected void contextInformationClosed() {
	}

	/**
	 * Requests that the specified context information to be shown.
	 *
	 * @param contextInformation the context information to be shown
	 * @param offset the offset to which the context information refers to
	 * @since 2.0
	 */
	void showContextInformation(IContextInformation contextInformation, int offset) {
		if (fContextInfoPopup != null)
			fContextInfoPopup.showContextInformation(contextInformation, offset);
	}

	/**
	 * Returns the current content assist error message.
	 *
	 * @return an error message or <code>null</code> if no error has occurred
	 */
	String getErrorMessage() {
		return fLastErrorMessage;
	}

	/**
	 * Returns the content assist processor for the content type of the specified document position.
	 *
	 * @param viewer the text viewer
	 * @param offset a offset within the document
	 * @return a content-assist processor or <code>null</code> if none exists
	 * @since 3.0
	 */
	private IContentAssistProcessor getProcessor(ITextViewer viewer, int offset) {
		try {

			IDocument document= viewer.getDocument();
			String type= TextUtilities.getContentType(document, getDocumentPartitioning(), offset, true);

			return getContentAssistProcessor(type);

		} catch (BadLocationException x) {
		}

		return null;
	}

	/**
	 * Returns the content assist processor for the content type of the specified document position.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a offset within the document
	 * @return a content-assist processor or <code>null</code> if none exists
	 * @since 3.0
	 */
	private IContentAssistProcessor getProcessor(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		try {

			IDocument document= contentAssistSubjectControl.getDocument();
			String type;
			if (document != null)
				type= TextUtilities.getContentType(document, getDocumentPartitioning(), offset, true);
			else
				type= IDocument.DEFAULT_CONTENT_TYPE;

			return getContentAssistProcessor(type);

		} catch (BadLocationException x) {
		}

		return null;
	}

	/**
	 * Returns an array of completion proposals computed based on the specified document position.
	 * The position is used to determine the appropriate content assist processor to invoke.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a document offset
	 * @return an array of completion proposals
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 * @since 3.0
	 */
	ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		fLastErrorMessage= null;

		ICompletionProposal[] result= null;

		IContentAssistProcessor p= getProcessor(contentAssistSubjectControl, offset);
		if (p instanceof ISubjectControlContentAssistProcessor) {
			result= ((ISubjectControlContentAssistProcessor) p).computeCompletionProposals(contentAssistSubjectControl, offset);
			fLastErrorMessage= p.getErrorMessage();
		}

		return result;
	}

	/**
	 * Returns an array of completion proposals computed based on the specified document position.
	 * The position is used to determine the appropriate content assist processor to invoke.
	 *
	 * @param viewer the viewer for which to compute the proposals
	 * @param offset a document offset
	 * @return an array of completion proposals or <code>null</code> if no proposals are possible
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		fLastErrorMessage= null;

		ICompletionProposal[] result= null;

		IContentAssistProcessor p= getProcessor(viewer, offset);
		if (p != null) {
			result= p.computeCompletionProposals(viewer, offset);
			fLastErrorMessage= p.getErrorMessage();
		}

		return result;
	}

	/**
	 * Returns an array of context information objects computed based on the specified document
	 * position. The position is used to determine the appropriate content assist processor to
	 * invoke.
	 *
	 * @param viewer the viewer for which to compute the context information
	 * @param offset a document offset
	 * @return an array of context information objects
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		fLastErrorMessage= null;

		IContextInformation[] result= null;

		IContentAssistProcessor p= getProcessor(viewer, offset);
		if (p != null) {
			result= p.computeContextInformation(viewer, offset);
			fLastErrorMessage= p.getErrorMessage();
		}

		return result;
	}

	/**
	 * Returns an array of context information objects computed based on the specified document
	 * position. The position is used to determine the appropriate content assist processor to
	 * invoke.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a document offset
	 * @return an array of context information objects
	 * @see IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 * @since 3.0
	 */
	IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		fLastErrorMessage= null;

		IContextInformation[] result= null;

		IContentAssistProcessor p= getProcessor(contentAssistSubjectControl, offset);
		if (p instanceof ISubjectControlContentAssistProcessor) {
			result= ((ISubjectControlContentAssistProcessor) p).computeContextInformation(contentAssistSubjectControl, offset);
			fLastErrorMessage= p.getErrorMessage();
		}

		return result;
	}

	/**
	 * Returns the context information validator that should be used to determine when the currently
	 * displayed context information should be dismissed. The position is used to determine the
	 * appropriate content assist processor to invoke.
	 *
	 * @param viewer the text viewer
	 * @param offset a document offset
	 * @return an validator
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 * @since 3.0
	 */
	IContextInformationValidator getContextInformationValidator(ITextViewer viewer, int offset) {
		IContentAssistProcessor p= getProcessor(viewer, offset);
		return p != null ? p.getContextInformationValidator() : null;
	}

	/**
	 * Returns the context information validator that should be used to determine when the currently
	 * displayed context information should be dismissed. The position is used to determine the
	 * appropriate content assist processor to invoke.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a document offset
	 * @return an validator
	 * @see IContentAssistProcessor#getContextInformationValidator()
	 * @since 3.0
	 */
	IContextInformationValidator getContextInformationValidator(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		IContentAssistProcessor p= getProcessor(contentAssistSubjectControl, offset);
		return p != null ? p.getContextInformationValidator() : null;
	}

	/**
	 * Returns the context information presenter that should be used to display context information.
	 * The position is used to determine the appropriate content assist processor to invoke.
	 *
	 * @param viewer the text viewer
	 * @param offset a document offset
	 * @return a presenter
	 * @since 2.0
	 */
	IContextInformationPresenter getContextInformationPresenter(ITextViewer viewer, int offset) {
		IContextInformationValidator validator= getContextInformationValidator(viewer, offset);
		if (validator instanceof IContextInformationPresenter)
			return (IContextInformationPresenter) validator;
		return null;
	}

	/**
	 * Returns the context information presenter that should be used to display context information.
	 * The position is used to determine the appropriate content assist processor to invoke.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a document offset
	 * @return a presenter
	 * @since 3.0
	 */
	IContextInformationPresenter getContextInformationPresenter(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		IContextInformationValidator validator= getContextInformationValidator(contentAssistSubjectControl, offset);
		if (validator instanceof IContextInformationPresenter)
			return (IContextInformationPresenter) validator;
		return null;
	}

	/**
	 * Returns the characters which when typed by the user should automatically initiate proposing
	 * completions. The position is used to determine the appropriate content assist processor to
	 * invoke.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a document offset
	 * @return the auto activation characters
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 * @since 3.0
	 */
	char[] getCompletionProposalAutoActivationCharacters(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		IContentAssistProcessor p= getProcessor(contentAssistSubjectControl, offset);
		return p != null ? p.getCompletionProposalAutoActivationCharacters() : null;
	}

	/**
	 * Returns the characters which when typed by the user should automatically initiate proposing
	 * completions. The position is used to determine the appropriate content assist processor to
	 * invoke.
	 *
	 * @param viewer the text viewer
	 * @param offset a document offset
	 * @return the auto activation characters
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	char[] getCompletionProposalAutoActivationCharacters(ITextViewer viewer, int offset) {
		IContentAssistProcessor p= getProcessor(viewer, offset);
		return p != null ? p.getCompletionProposalAutoActivationCharacters() : null;
	}

	/**
	 * Returns the characters which when typed by the user should automatically initiate the
	 * presentation of context information. The position is used to determine the appropriate
	 * content assist processor to invoke.
	 *
	 * @param viewer the text viewer
	 * @param offset a document offset
	 * @return the auto activation characters
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 * @since 3.0
	 */
	char[] getContextInformationAutoActivationCharacters(ITextViewer viewer, int offset) {
		IContentAssistProcessor p= getProcessor(viewer, offset);
		return p != null ? p.getContextInformationAutoActivationCharacters() : null;
	}

	/**
	 * Returns the characters which when typed by the user should automatically initiate the
	 * presentation of context information. The position is used to determine the appropriate
	 * content assist processor to invoke.
	 *
	 * @param contentAssistSubjectControl the content assist subject control
	 * @param offset a document offset
	 * @return the auto activation characters
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 * @since 3.0
	 */
	char[] getContextInformationAutoActivationCharacters(IContentAssistSubjectControl contentAssistSubjectControl, int offset) {
		IContentAssistProcessor p= getProcessor(contentAssistSubjectControl, offset);
		return p != null ? p.getContextInformationAutoActivationCharacters() : null;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
	 * @since 2.0
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner,
	 *      int)
	 * @since 3.0
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		if (priority > WIDGET_PRIORITY) {
			hide();
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#setFocus(org.eclipse.jface.text.IWidgetTokenOwner)
	 * @since 3.0
	 */
	public boolean setFocus(IWidgetTokenOwner owner) {
		if (fProposalPopup != null) {
			fProposalPopup.setFocus();
			return fProposalPopup.hasFocus();
		}
		return false;
	}

	/**
	 * Hides any open pop-ups.
	 *
	 * @since 3.0
	 */
	protected void hide() {
		if (fProposalPopup != null)
			fProposalPopup.hide();

		if (fContextInfoPopup != null)
			fContextInfoPopup.hide();
	}

	// ------ control's size handling dialog settings ------

	/**
	 * Tells this information control manager to open the information control with the values
	 * contained in the given dialog settings and to store the control's last valid size in the
	 * given dialog settings.
	 * <p>
	 * Note: This API is only valid if the information control implements
	 * {@link org.eclipse.jface.text.IInformationControlExtension3}. Not following this restriction
	 * will later result in an {@link UnsupportedOperationException}.
	 * </p>
	 * <p>
	 * The constants used to store the values are:
	 * <ul>
	 * <li>{@link ContentAssistant#STORE_SIZE_X}</li>
	 * <li>{@link ContentAssistant#STORE_SIZE_Y}</li>
	 * </ul>
	 * </p>
	 *
	 * @param dialogSettings the dialog settings
	 * @since 3.0
	 */
	public void setRestoreCompletionProposalSize(IDialogSettings dialogSettings) {
		Assert.isTrue(dialogSettings != null);
		fDialogSettings= dialogSettings;
	}

	/**
	 * Stores the content assist pop-up's size.
	 */
	protected void storeCompletionProposalPopupSize() {
		if (fDialogSettings == null || fProposalPopup == null)
			return;

		Point size= fProposalPopup.getSize();
		if (size == null)
			return;

		fDialogSettings.put(STORE_SIZE_X, size.x);
		fDialogSettings.put(STORE_SIZE_Y, size.y);
	}

	/**
	 * Restores the content assist pop-up's size.
	 *
	 * @return the stored size
	 * @since 3.0
	 */
	protected Point restoreCompletionProposalPopupSize() {
		if (fDialogSettings == null)
			return null;

		Point size= new Point(-1, -1);

		try {
			size.x= fDialogSettings.getInt(STORE_SIZE_X);
			size.y= fDialogSettings.getInt(STORE_SIZE_Y);
		} catch (NumberFormatException ex) {
			size.x= -1;
			size.y= -1;
		}

		// sanity check
		if (size.x == -1 && size.y == -1)
			return null;

		Rectangle maxBounds= null;
		if (fContentAssistSubjectControl != null && Helper.okToUse(fContentAssistSubjectControl.getControl()))
			maxBounds= fContentAssistSubjectControl.getControl().getDisplay().getBounds();
		else {
			// fallback
			Display display= Display.getCurrent();
			if (display == null)
				display= Display.getDefault();
			if (display != null && !display.isDisposed())
				maxBounds= display.getBounds();
		}

		if (size.x > -1 && size.y > -1) {
			if (maxBounds != null) {
				size.x= Math.min(size.x, maxBounds.width);
				size.y= Math.min(size.y, maxBounds.height);
			}

			// Enforce an absolute minimal size
			size.x= Math.max(size.x, 30);
			size.y= Math.max(size.y, 30);
		}

		return size;
	}

	/**
	 * Sets the prefix completion property. If enabled, content assist delegates completion to
	 * prefix completion.
	 *
	 * @param enabled <code>true</code> to enable prefix completion, <code>false</code> to
	 *        disable
	 */
	public void enablePrefixCompletion(boolean enabled) {
		fIsPrefixCompletionEnabled= enabled;
	}

	/**
	 * Returns the prefix completion state.
	 *
     * @return <code>true</code> if prefix completion is enabled, <code>false</code> otherwise
     * @since 3.2
     */
    boolean isPrefixCompletionEnabled() {
	    return fIsPrefixCompletionEnabled;
    }

	/**
	 * Returns whether the content assistant proposal popup has the focus.
	 *
	 * @return <code>true</code> if the proposal popup has the focus
	 * @since 3.0
	 */
	public boolean hasProposalPopupFocus() {
		return fProposalPopup.hasFocus();
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#addCompletionListener(org.eclipse.jface.text.contentassist.ICompletionListener)
	 * @since 3.2
	 */
	public void addCompletionListener(ICompletionListener listener) {
		Assert.isLegal(listener != null);
		fCompletionListeners.add(listener);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#removeCompletionListener(org.eclipse.jface.text.contentassist.ICompletionListener)
	 * @since 3.2
	 */
	public void removeCompletionListener(ICompletionListener listener) {
		fCompletionListeners.remove(listener);
	}

	/**
	 * Fires a session begin event to all registered {@link ICompletionListener}s.
	 *
	 * @param isAutoActivated  <code>true</code> if this session was triggered by auto activation
	 * @since 3.2
	 */
	void fireSessionBeginEvent(boolean isAutoActivated) {
		if (fContentAssistSubjectControlAdapter != null && !isProposalPopupActive()) {
			IContentAssistProcessor processor= getProcessor(fContentAssistSubjectControlAdapter, fContentAssistSubjectControlAdapter.getSelectedRange().x);
			ContentAssistEvent event= new ContentAssistEvent(this, processor, isAutoActivated);
			Object[] listeners= fCompletionListeners.getListeners();
			for (int i= 0; i < listeners.length; i++) {
				ICompletionListener listener= (ICompletionListener)listeners[i];
				listener.assistSessionStarted(event);
			}
		}
	}

	/**
	 * Fires a session restart event to all registered {@link ICompletionListener}s.
	 *
	 * @since 3.4
	 */
	void fireSessionRestartEvent() {
		if (fContentAssistSubjectControlAdapter != null) {
			IContentAssistProcessor processor= getProcessor(fContentAssistSubjectControlAdapter, fContentAssistSubjectControlAdapter.getSelectedRange().x);
			ContentAssistEvent event= new ContentAssistEvent(this, processor);
			Object[] listeners= fCompletionListeners.getListeners();
			for (int i= 0; i < listeners.length; i++) {
				ICompletionListener listener= (ICompletionListener)listeners[i];
				if (listener instanceof ICompletionListenerExtension)
					((ICompletionListenerExtension)listener).assistSessionRestarted(event);
			}
		}
	}

	/**
	 * Fires a session end event to all registered {@link ICompletionListener}s.
	 *
	 * @since 3.2
	 */
	void fireSessionEndEvent() {
		if (fContentAssistSubjectControlAdapter != null) {
			IContentAssistProcessor processor= getProcessor(fContentAssistSubjectControlAdapter, fContentAssistSubjectControlAdapter.getSelectedRange().x);
			ContentAssistEvent event= new ContentAssistEvent(this, processor);
			Object[] listeners= fCompletionListeners.getListeners();
			for (int i= 0; i < listeners.length; i++) {
				ICompletionListener listener= (ICompletionListener)listeners[i];
				listener.assistSessionEnded(event);
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#setRepeatedInvocationMode(boolean)
	 * @since 3.2
	 */
	public void setRepeatedInvocationMode(boolean cycling) {
		fIsRepetitionMode= cycling;
	}

	/**
	 * Returns <code>true</code> if repeated invocation mode is enabled, <code>false</code>
	 * otherwise.
	 *
	 * @return <code>true</code> if repeated invocation mode is enabled, <code>false</code>
	 *         otherwise
	 * @since 3.2
	 */
	boolean isRepeatedInvocationMode() {
		return fIsRepetitionMode;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#setShowEmptyList(boolean)
	 * @since 3.2
	 */
	public void setShowEmptyList(boolean showEmpty) {
		fShowEmptyList= showEmpty;
	}

	/**
	 * Returns <code>true</code> if empty lists should be displayed, <code>false</code>
	 * otherwise.
	 *
	 * @return <code>true</code> if empty lists should be displayed, <code>false</code>
	 *         otherwise
	 * @since 3.2
	 */
	boolean isShowEmptyList() {
		return fShowEmptyList;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#setStatusLineVisible(boolean)
	 * @since 3.2
	 */
	public void setStatusLineVisible(boolean show) {
		fIsStatusLineVisible= show;
		if (fProposalPopup != null)
			fProposalPopup.setStatusLineVisible(show);
	}

	/**
	 * Returns <code>true</code> if a message line should be displayed, <code>false</code>
	 * otherwise.
	 *
	 * @return <code>true</code> if a message line should be displayed, <code>false</code>
	 *         otherwise
	 * @since 3.2
	 */
	boolean isStatusLineVisible() {
		return fIsStatusLineVisible;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#setStatusMessage(java.lang.String)
	 * @since 3.2
	 */
	public void setStatusMessage(String message) {
		Assert.isLegal(message != null);
		fMessage= message;
		if (fProposalPopup != null)
			fProposalPopup.setMessage(message);
	}

	/**
	 * Returns the affordance caption for the cycling affordance at the bottom of the pop-up.
	 *
	 * @return the affordance caption for the cycling affordance at the bottom of the pop-up
	 * @since 3.2
	 */
	String getStatusMessage() {
		return fMessage;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension2#setEmptyMessage(java.lang.String)
	 * @since 3.2
	 */
	public void setEmptyMessage(String message) {
		Assert.isLegal(message != null);
		if (fProposalPopup != null)
			fProposalPopup.setEmptyMessage(message);
	}

	/**
	 * Fires a selection event, see {@link ICompletionListener}.
	 *
	 * @param proposal the selected proposal, possibly <code>null</code>
	 * @param smartToggle true if the smart toggle is on
	 * @since 3.2
	 */
	void fireSelectionEvent(ICompletionProposal proposal, boolean smartToggle) {
		Object[] listeners= fCompletionListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			ICompletionListener listener= (ICompletionListener)listeners[i];
			listener.selectionChanged(proposal, smartToggle);
		}
	}

	/**
	 * Fires an event after applying a proposal, see {@link ICompletionListenerExtension2}.
	 * 
	 * @param proposal the applied proposal
	 * @since 3.8
	 */
	void fireAppliedEvent(ICompletionProposal proposal) {
		Object[] listeners= fCompletionListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			ICompletionListener listener= (ICompletionListener)listeners[i];
			if (listener instanceof ICompletionListenerExtension2)
				((ICompletionListenerExtension2)listener).applied(proposal);
		}
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension3#setInvocationTrigger(org.eclipse.jface.bindings.keys.KeySequence)
	 * @since 3.2
	 */
	public void setRepeatedInvocationTrigger(KeySequence sequence) {
		fRepeatedInvocationKeySequence= sequence;
	}

	/**
	 * Returns the repeated invocation key sequence.
	 *
	 * @return the repeated invocation key sequence or <code>null</code>, if none
	 * @since 3.2
	 */
	KeySequence getRepeatedInvocationKeySequence() {
		return fRepeatedInvocationKeySequence;
	}

	/**
	 * Returns whether proposal popup is active.
	 *
	 * @return <code>true</code> if the proposal popup is active, <code>false</code> otherwise
	 * @since 3.4
	 */
	protected boolean isProposalPopupActive(){
		return fProposalPopup != null && fProposalPopup.isActive();
	}

	/**
	 * Returns whether the context information popup is active.
	 *
	 * @return <code>true</code> if the context information popup is active, <code>false</code> otherwise
	 * @since 3.4
	 */
	protected boolean isContextInfoPopupActive(){
		return fContextInfoPopup != null && fContextInfoPopup.isActive();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public final IHandler getHandler(String commandId) {
		if (fHandlers == null)
			throw new IllegalStateException();

		IHandler handler= (IHandler)fHandlers.get(commandId);
		if (handler != null)
			return handler;

		Assert.isLegal(false);
		return null;
	}

	/**
	 * Registers the given handler under the given command identifier.
	 *
	 * @param commandId the command identifier
	 * @param handler the handler
	 * @since 3.4
	 */
	protected final void registerHandler(String commandId, IHandler handler) {
		if (fHandlers == null)
			fHandlers= new HashMap(2);
		fHandlers.put(commandId, handler);
	}

	/**
	 * Tells whether the support for colored labels is enabled.
	 *
	 * @return <code>true</code> if the support for colored labels is enabled, <code>false</code> otherwise
	 * @since 3.4
	 */
	boolean isColoredLabelsSupportEnabled() {
	    return fIsColoredLabelsSupportEnabled;
	}

	/**
	 * Enables the support for colored labels in the proposal popup.
	 * <p>Completion proposals can implement {@link ICompletionProposalExtension6}
	 * to provide colored proposal labels.</p>
	 *
	 * @param isEnabled if <code>true</code> the support for colored labels is enabled in the proposal popup
	 * @since 3.4
	 */
	public void enableColoredLabels(boolean isEnabled) {
		fIsColoredLabelsSupportEnabled= isEnabled;
	}

	/**
	 * Sets the proposal sorter.
	 * 
	 * @param sorter the sorter to be used, or <code>null</code> if no sorting is requested
	 * @since 3.8
	 */
	public void setSorter(ICompletionProposalSorter sorter) {
		fSorter= sorter;
		if (fProposalPopup != null) {
			fProposalPopup.setSorter(fSorter);
		}
	}
}
