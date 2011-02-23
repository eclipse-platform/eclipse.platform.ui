/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.link.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.util.OpenStrategy;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenKeeperExtension;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.IWidgetTokenOwnerExtension;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;


/**
 * A custom implementation of the <code>IContentAssistant</code> interface.
 * This implementation is used by the linked mode UI. This is internal and subject
 * to change without notice.
 */
public class ContentAssistant2 implements IContentAssistant, IContentAssistantExtension, IWidgetTokenKeeper, IWidgetTokenKeeperExtension {

	/**
	 * A generic closer class used to monitor various
	 * interface events in order to determine whether
	 * content-assist should be terminated and all
	 * associated windows closed.
	 */
	class Closer implements ControlListener, MouseListener, FocusListener, DisposeListener, IViewportListener {

		/** The shell on which we add listeners. */
		private Shell fShell;
		private long fViewportListenerStartTime;

		/**
		 * Installs this closer on it's viewer's text widget.
		 */
		protected void install() {
			Control w= fViewer.getTextWidget();
			if (Helper2.okToUse(w)) {

				Shell shell= w.getShell();
				fShell= shell;
				shell.addControlListener(this);

				w.addMouseListener(this);
				w.addFocusListener(this);

				/*
				 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal Errors
				 */
				w.addDisposeListener(this);
			}

			fViewer.addViewportListener(this);
			fViewportListenerStartTime= System.currentTimeMillis() + 500;
		}

		/**
		 * Uninstalls this closer from the viewer's text widget.
		 */
		protected void uninstall() {
			Shell shell= fShell;
			fShell= null;
			if (Helper2.okToUse(shell))
				shell.removeControlListener(this);

			Control w= fViewer.getTextWidget();
			if (Helper2.okToUse(w)) {

				w.removeMouseListener(this);
				w.removeFocusListener(this);

				/*
				 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal Errors
				 */
				w.removeDisposeListener(this);
			}

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
			if (fViewer != null) {
				Control control= fViewer.getTextWidget();
				if (control != null) {
					Display d= control.getDisplay();
					if (d != null) {
						d.asyncExec(new Runnable() {
							public void run() {
								if (!hasFocus())
									hide();
							}
						});
					}
				}
			}
		}

		/*
		 * @seeDisposeListener#widgetDisposed(DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {
			/*
			 * 1GGYYWK: ITPJUI:ALL - Dismissing editor with code assist up causes lots of Internal Errors
			 */
			hide();
		}

		/*
		 * @see IViewportListener#viewportChanged(int)
		 */
		public void viewportChanged(int topIndex) {
			if (System.currentTimeMillis() > fViewportListenerStartTime)
				hide();
		}
	}

	/**
	 * An implementation of <code>IContentAssistListener</code>, this class is
	 * used to monitor key events in support of automatic activation
	 * of the content assistant. If enabled, the implementation utilizes a
	 * thread to watch for input characters matching the activation
	 * characters specified by the content assist processor, and if
	 * detected, will wait the indicated delay interval before
	 * activating the content assistant.
	 */
	class AutoAssistListener implements VerifyKeyListener, Runnable {

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
			fThread= new Thread(this, ContentAssistMessages.getString("ContentAssistant.assist_delay_timer_name")); //$NON-NLS-1$
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
			if (threadToStop != null)
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

		public void verifyKey(VerifyEvent e) {
			// Only act on typed characters and ignore modifier-only events
			if (e.character == 0 && (e.keyCode & SWT.KEYCODE_BIT) == 0)
				return;

			if (e.character != 0 && (e.stateMask == SWT.ALT))
				return;

			int showStyle;
			int pos= fViewer.getSelectedRange().x;
			char[] activation= getCompletionProposalAutoActivationCharacters(fViewer, pos);

			if (contains(activation, e.character) && !fProposalPopup.isActive())
				showStyle= SHOW_PROPOSALS;
			else {
				activation= getContextInformationAutoActivationCharacters(fViewer, pos);
				if (contains(activation, e.character) && !fContextInfoPopup.isActive())
					showStyle= SHOW_CONTEXT_INFO;
				else {
					if (fThread != null && fThread.isAlive())
						stop();
					return;
				}
			}

			if (fThread != null && fThread.isAlive())
				reset(showStyle);
			else
				start(showStyle);
		}

		protected void showAssist(final int showStyle) {
			Control control= fViewer.getTextWidget();
			Display d= control.getDisplay();
			if (d != null) {
				try {
					d.syncExec(new Runnable() {
						public void run() {
							if (showStyle == SHOW_PROPOSALS)
								fProposalPopup.showProposals(true);
							else if (showStyle == SHOW_CONTEXT_INFO)
								fContextInfoPopup.showContextProposals(true);
						}
					});
				} catch (SWTError e) {
				}
			}
		}
	}

	/**
	 * The layout manager layouts the various
	 * windows associated with the content assistant based on the
	 * settings of the content assistant.
	 */
	class LayoutManager implements Listener {

		// Presentation types.
		/** proposal selector */
		public final static int LAYOUT_PROPOSAL_SELECTOR= 0;
		/** context selector */
		public final static int LAYOUT_CONTEXT_SELECTOR= 1;
		/** context info */
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
							Helper2.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
						// Restore event notification to the tip popup.
						addContentAssistListener((IContentAssistListener2) fPopups[LAYOUT_CONTEXT_SELECTOR], CONTEXT_SELECTOR);
					}
					break;

				case LAYOUT_CONTEXT_SELECTOR:
					if (Helper2.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						if (fProposalPopupOrientation == PROPOSAL_STACKED)
							layout(LAYOUT_PROPOSAL_SELECTOR, getSelectionOffset());
						// Restore event notification to the proposal popup.
						addContentAssistListener((IContentAssistListener2) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);
					}
					fContextType= LAYOUT_CONTEXT_INFO_POPUP;
					break;

				case LAYOUT_CONTEXT_INFO_POPUP:
					if (Helper2.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						if (fContextInfoPopupOrientation == CONTEXT_INFO_BELOW)
							layout(LAYOUT_PROPOSAL_SELECTOR, getSelectionOffset());
					}
					fContextType= LAYOUT_CONTEXT_SELECTOR;
					break;
			}
		}

		protected int getShellType(Widget shell) {
			for (int i=0; i<fShells.length; i++) {
				if (fShells[i] == shell)
					return i;
			}
			return -1;
		}

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
					Helper2.okToUse(fShells[LAYOUT_CONTEXT_INFO_POPUP])) {
				// Stack proposal selector beneath the tip box.
				Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
				Shell parent= fShells[LAYOUT_CONTEXT_INFO_POPUP];
				shell.setLocation(getStackedLocation(shell, parent));
			} else if (fContextType != LAYOUT_CONTEXT_SELECTOR ||
						!Helper2.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
				// There are no other presentations to be concerned with,
				// so place the proposal selector beneath the cursor line.
				Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
				shell.setLocation(getBelowLocation(shell, offset));
			} else {
				switch (fProposalPopupOrientation) {
					case PROPOSAL_REMOVE: {
						// Remove the tip selector and place the
						// proposal selector beneath the cursor line.
						fShells[LAYOUT_CONTEXT_SELECTOR].dispose();
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						shell.setLocation(getBelowLocation(shell, offset));
						break;
					}
					case PROPOSAL_OVERLAY: {
						// Overlay the tip selector with the proposal selector.
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						shell.setLocation(getBelowLocation(shell, offset));
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
			shell.setLocation(getBelowLocation(shell, offset));

			if (Helper2.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
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
					shell.setLocation(getAboveLocation(shell, offset));
					break;
				}
				case CONTEXT_INFO_BELOW: {
					// Place the popup beneath the cursor line.
					Shell parent= fShells[LAYOUT_CONTEXT_INFO_POPUP];
					parent.setLocation(getBelowLocation(parent, offset));
					if (Helper2.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						// Stack the proposal selector beneath the context info popup.
						Shell shell= fShells[LAYOUT_PROPOSAL_SELECTOR];
						shell.setLocation(getStackedLocation(shell, parent));
					}
					break;
				}
			}
		}

		protected void shiftHorizontalLocation(Point location, Rectangle shellBounds, Rectangle displayBounds) {
			if (location.x + shellBounds.width > displayBounds.width)
				location.x= displayBounds.width - shellBounds.width;

			if (location.x < displayBounds.x)
				location.x= displayBounds.x;
		}

		protected void shiftVerticalLocation(Point location, Rectangle shellBounds, Rectangle displayBounds) {
			if (location.y + shellBounds.height > displayBounds.height)
				location.y= displayBounds.height - shellBounds.height;

			if (location.y < displayBounds.y)
				location.y= displayBounds.y;
		}

		protected Point getAboveLocation(Shell shell, int offset) {
			StyledText text= fViewer.getTextWidget();
			Point location= text.getLocationAtOffset(offset);
			location= text.toDisplay(location);

			Rectangle shellBounds= shell.getBounds();
			Rectangle displayBounds= shell.getDisplay().getClientArea();

			location.y=location.y - shellBounds.height;

			shiftHorizontalLocation(location, shellBounds, displayBounds);
			shiftVerticalLocation(location, shellBounds, displayBounds);

			return location;
		}

		protected Point getBelowLocation(Shell shell, int offset) {
			StyledText text= fViewer.getTextWidget();
			Point location= text.getLocationAtOffset(offset);
			if (location.x < 0) location.x= 0;
			if (location.y < 0) location.y= 0;
			location= text.toDisplay(location);

			Rectangle shellBounds= shell.getBounds();
			Rectangle displayBounds= shell.getDisplay().getClientArea();

			location.y= location.y + text.getLineHeight(offset);
			shiftHorizontalLocation(location, shellBounds, displayBounds);
			shiftVerticalLocation(location, shellBounds, displayBounds);

			return location;
		}

		protected Point getStackedLocation(Shell shell, Shell parent) {
			Point p= parent.getLocation();
			Point size= parent.getSize();
			p.x += size.x / 4;
			p.y += size.y;

			p= parent.toDisplay(p);

			Rectangle shellBounds= shell.getBounds();
			Rectangle displayBounds= shell.getDisplay().getClientArea();
			shiftHorizontalLocation(p, shellBounds, displayBounds);
			shiftVerticalLocation(p, shellBounds, displayBounds);

			return p;
		}

		protected void adjustListeners(int type) {
			switch (type) {
				case LAYOUT_PROPOSAL_SELECTOR:
					if (fContextType == LAYOUT_CONTEXT_SELECTOR &&
							Helper2.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR]))
						// Disable event notification to the tip selector.
						removeContentAssistListener((IContentAssistListener2) fPopups[LAYOUT_CONTEXT_SELECTOR], CONTEXT_SELECTOR);
					break;
				case LAYOUT_CONTEXT_SELECTOR:
					if (Helper2.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR]))
						// Disable event notification to the proposal selector.
						removeContentAssistListener((IContentAssistListener2) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);
					break;
				case LAYOUT_CONTEXT_INFO_POPUP:
					break;
			}
		}
	}

	/**
	 * Internal key listener and event consumer.
	 */
	class InternalListener implements VerifyKeyListener, IEventConsumer {

		/**
		 * Verifies key events by notifying the registered listeners.
		 * Each listener is allowed to indicate that the event has been
		 * handled and should not be further processed.
		 *
		 * @param e the verify event
		 * @see VerifyKeyListener#verifyKey(org.eclipse.swt.events.VerifyEvent)
		 */
		public void verifyKey(VerifyEvent e) {
			IContentAssistListener2[] listeners= (IContentAssistListener2[]) fListeners.clone();
			for (int i= 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					if (!listeners[i].verifyKey(e) || !e.doit)
						return;
				}
			}
		}

		/*
		 * @see IEventConsumer#processEvent
		 */
		public void processEvent(VerifyEvent event) {

			installKeyListener();

			IContentAssistListener2[] listeners= (IContentAssistListener2[])fListeners.clone();
			for (int i= 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					listeners[i].processEvent(event);
					if (!event.doit)
						return;
				}
			}
		}
	}


	// Content-Assist Listener types
	final static int CONTEXT_SELECTOR= 0;
	final static int PROPOSAL_SELECTOR= 1;
	final static int CONTEXT_INFO_POPUP= 2;

	/**
	 * The popup priority: &gt; info pop-ups, &lt; standard content assist.
	 * Default value: <code>10</code>.
	 *
	 * @since 3.0
	 */
	public static final int WIDGET_PRIORITY= 10;


	private static final int DEFAULT_AUTO_ACTIVATION_DELAY= 500;

	private IInformationControlCreator fInformationControlCreator;
	private int fAutoActivationDelay= DEFAULT_AUTO_ACTIVATION_DELAY;
	private boolean fIsAutoActivated= false;
	private boolean fIsAutoInserting= false;
	private int fProposalPopupOrientation= PROPOSAL_OVERLAY;
	private int fContextInfoPopupOrientation= CONTEXT_INFO_ABOVE;
	private Map fProcessors;
	private String fPartitioning;

	private Color fContextInfoPopupBackground;
	private Color fContextInfoPopupForeground;
	private Color fContextSelectorBackground;
	private Color fContextSelectorForeground;

	private ITextViewer fViewer;
	private String fLastErrorMessage;

	private Closer fCloser;
	private LayoutManager fLayoutManager;
	private AutoAssistListener fAutoAssistListener;
	private InternalListener fInternalListener;
	private CompletionProposalPopup2 fProposalPopup;
	private ContextInformationPopup2 fContextInfoPopup;

	private boolean fKeyListenerHooked= false;
	private IContentAssistListener2[] fListeners= new IContentAssistListener2[4];
	private int fCompletionPosition;
	private String[] fProposalStrings;
	private ICompletionProposal[] fProposals;
	private final List fProposalListeners= new ArrayList();

	/**
	 * Tells whether colored label support is enabled.
	 * @since 3.4
	 */
	private boolean fIsColoredLabelsSupportEnabled= false;


	/**
	 * Creates a new content assistant. The content assistant is not automatically activated,
	 * overlays the completion proposals with context information list if necessary, and
	 * shows the context information above the location at which it was activated. If auto
	 * activation will be enabled, without further configuration steps, this content assistant
	 * is activated after a 500 ms delay. It uses the default partitioning.
	 */
	public ContentAssistant2() {
		setContextInformationPopupOrientation(CONTEXT_INFO_ABOVE);
		setInformationControlCreator(getInformationControlCreator());

//		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
//		IColorManager manager= textTools.getColorManager();
//
//		IPreferenceStore store=  JavaPlugin.getDefault().getPreferenceStore();
//
//		Color c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, manager);
//		setProposalSelectorForeground(c);
//
//		c= getColor(store, PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, manager);
//		setProposalSelectorBackground(c);
	}

	/**
	 * Creates an <code>IInformationControlCreator</code> to be used to display context information.
	 *
	 * @return an <code>IInformationControlCreator</code> to be used to display context information
	 */
	private IInformationControlCreator getInformationControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, false);
			}
		};
	}

	/**
	 * Sets the document partitioning this content assistant is using.
	 *
	 * @param partitioning the document partitioning for this content assistant
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
	 * Registers a given content assist processor for a particular content type.
	 * If there is already a processor registered for this type, the new processor
	 * is registered instead of the old one.
	 *
	 * @param processor the content assist processor to register, or <code>null</code> to remove an existing one
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
	 * Enables the content assistant's auto activation mode.
	 *
	 * @param enabled indicates whether auto activation is enabled or not
	 */
	public void enableAutoActivation(boolean enabled) {
		fIsAutoActivated= enabled;
		manageAutoActivation(fIsAutoActivated);
	}

	/**
	 * Enables the content assistant's auto insertion mode. If enabled,
	 * the content assistant inserts a proposal automatically if it is
	 * the only proposal. In the case of ambiguities, the user must
	 * make the choice.
	 *
	 * @param enabled indicates whether auto insertion is enabled or not
	 * @since 2.0
	 */
	public void enableAutoInsert(boolean enabled) {
		fIsAutoInserting= enabled;
	}

	/**
	 * Returns whether this content assistant is in the auto insertion
	 * mode or not.
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
	 *            must be removed
	 * @since 2.0
	 */
	private void manageAutoActivation(boolean start) {
		if (start) {

			if (fViewer != null && fAutoAssistListener == null) {
				fAutoAssistListener= new AutoAssistListener();
				if (fViewer instanceof ITextViewerExtension) {
					ITextViewerExtension extension= (ITextViewerExtension) fViewer;
					extension.appendVerifyKeyListener(fAutoAssistListener);
				} else {
					StyledText textWidget= fViewer.getTextWidget();
					if (Helper2.okToUse(textWidget))
						textWidget.addVerifyKeyListener(fAutoAssistListener);
				}
			}

		} else if (fAutoAssistListener != null) {

			if (fViewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= (ITextViewerExtension) fViewer;
				extension.removeVerifyKeyListener(fAutoAssistListener);
			} else {
				StyledText textWidget= fViewer.getTextWidget();
				if (Helper2.okToUse(textWidget))
					textWidget.removeVerifyKeyListener(fAutoAssistListener);
			}

			fAutoAssistListener= null;
		}
	}

	/**
	 * Sets the delay after which the content assistant is automatically invoked
	 * if the cursor is behind an auto activation character.
	 *
	 * @param delay the auto activation delay
	 */
	public void setAutoActivationDelay(int delay) {
		fAutoActivationDelay= delay;
	}

	/**
	 * Sets the proposal pop-ups' orientation.
	 * The following values may be used:
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
	 * @return the foreground of the context information popup
	 * @since 2.0
	 */
	Color getContextInformationPopupForeground() {
		return fContextInfoPopupForeground;
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
	 * @see IContentAssist#install
	 */
	public void install(ITextViewer textViewer) {
		Assert.isNotNull(textViewer);

		fViewer= textViewer;

		fLayoutManager= new LayoutManager();
		fInternalListener= new InternalListener();

		AdditionalInfoController2 controller= null;
		if (fInformationControlCreator != null)
			controller= new AdditionalInfoController2(fInformationControlCreator, OpenStrategy.getPostSelectionDelay());

		fContextInfoPopup= new ContextInformationPopup2(this, fViewer);
		fProposalPopup= new CompletionProposalPopup2(this, fViewer, controller);

		manageAutoActivation(fIsAutoActivated);
	}

	/*
	 * @see IContentAssist#uninstall
	 */
	public void uninstall() {

		if (fProposalPopup != null)
			fProposalPopup.hide();

		if (fContextInfoPopup != null)
			fContextInfoPopup.hide();

		manageAutoActivation(false);

		if (fCloser != null) {
			fCloser.uninstall();
			fCloser= null;
		}

		fViewer= null;
	}

	/**
	 * Adds the given shell of the specified type to the layout.
	 * Valid types are defined by <code>LayoutManager</code>.
	 *
	 * @param popup a content assist popup
	 * @param shell the shell of the content-assist popup
	 * @param type the type of popup
	 * @param visibleOffset the offset at which to layout the popup relative to the offset of the viewer's visible region
	 * @since 2.0
	 */
	void addToLayout(Object popup, Shell shell, int type, int visibleOffset) {
		fLayoutManager.add(popup, shell, type, visibleOffset);
	}

	/**
	 * Layouts the registered popup of the given type relative to the
	 * given offset. The offset is relative to the offset of the viewer's visible region.
	 * Valid types are defined by <code>LayoutManager</code>.
	 *
	 * @param type the type of popup to layout
	 * @param visibleOffset the offset at which to layout relative to the offset of the viewer's visible region
	 * @since 2.0
	 */
	void layout(int type, int visibleOffset) {
		fLayoutManager.layout(type, visibleOffset);
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
		StyledText text= fViewer.getTextWidget();
		return text.getSelectionRange().x;
	}

	/**
	 * Returns whether the widget token could be acquired.
	 * The following are valid listener types:
	 * <ul>
	 *   <li>AUTO_ASSIST
	 *   <li>CONTEXT_SELECTOR
	 *   <li>PROPOSAL_SELECTOR
	 *   <li>CONTEXT_INFO_POPUP
	 * <ul>
	 * @param type the listener type for which to acquire
	 * @return <code>true</code> if the widget token could be acquired
	 * @since 2.0
	 */
	private boolean acquireWidgetToken(int type) {
		switch (type) {
			case CONTEXT_SELECTOR:
			case PROPOSAL_SELECTOR:
				if (fViewer instanceof IWidgetTokenOwner) {
					IWidgetTokenOwner owner= (IWidgetTokenOwner) fViewer;
					return owner.requestWidgetToken(this);
				} else if (fViewer instanceof IWidgetTokenOwnerExtension)  {
					IWidgetTokenOwnerExtension extension= (IWidgetTokenOwnerExtension) fViewer;
					return extension.requestWidgetToken(this, WIDGET_PRIORITY);
				}
		}
		return true;
	}

	/**
	 * Registers a content assist listener.
	 * The following are valid listener types:
	 * <ul>
	 *   <li>AUTO_ASSIST
	 *   <li>CONTEXT_SELECTOR
	 *   <li>PROPOSAL_SELECTOR
	 *   <li>CONTEXT_INFO_POPUP
	 * <ul>
	 * Returns whether the listener could be added successfully. A listener
	 * can not be added if the widget token could not be acquired.
	 *
	 * @param listener the listener to register
	 * @param type the type of listener
	 * @return <code>true</code> if the listener could be added
	 */
	boolean addContentAssistListener(IContentAssistListener2 listener, int type) {

		if (acquireWidgetToken(type)) {

			fListeners[type]= listener;

			if (getNumberOfListeners() == 1) {
				fCloser= new Closer();
				fCloser.install();
				fViewer.setEventConsumer(fInternalListener);
				installKeyListener();
			}
			return true;
		}

		return false;
	}

	/**
	 * Installs a key listener on the text viewer's widget.
	 */
	private void installKeyListener() {
		if (!fKeyListenerHooked) {
			StyledText text= fViewer.getTextWidget();
			if (Helper2.okToUse(text)) {

				if (fViewer instanceof ITextViewerExtension) {
					ITextViewerExtension e= (ITextViewerExtension) fViewer;
					e.prependVerifyKeyListener(fInternalListener);
				} else {
					text.addVerifyKeyListener(fInternalListener);
				}

				fKeyListenerHooked= true;
			}
		}
	}

	/**
	 * Releases the previously acquired widget token if the token
	 * is no longer necessary.
	 * The following are valid listener types:
	 * <ul>
	 *   <li>AUTO_ASSIST
	 *   <li>CONTEXT_SELECTOR
	 *   <li>PROPOSAL_SELECTOR
	 *   <li>CONTEXT_INFO_POPUP
	 * <ul>
	 *
	 * @param type the listener type
	 * @since 2.0
	 */
	private void releaseWidgetToken(int type) {
		if (fListeners[CONTEXT_SELECTOR] == null && fListeners[PROPOSAL_SELECTOR] == null) {
			if (fViewer instanceof IWidgetTokenOwner) {
				IWidgetTokenOwner owner= (IWidgetTokenOwner) fViewer;
				owner.releaseWidgetToken(this);
			}
		}
	}

	/**
	 * Unregisters a content assist listener.
	 *
	 * @param listener the listener to unregister
	 * @param type the type of listener
	 *
	 * @see #addContentAssistListener
	 */
	void removeContentAssistListener(IContentAssistListener2 listener, int type) {
		fListeners[type]= null;

		if (getNumberOfListeners() == 0) {

			if (fCloser != null) {
				fCloser.uninstall();
				fCloser= null;
			}

			uninstallKeyListener();
			fViewer.setEventConsumer(null);
		}

		releaseWidgetToken(type);
	}

	/**
	 * Uninstall the key listener from the text viewer's widget.
	 */
	private void uninstallKeyListener() {
		if (fKeyListenerHooked) {
			StyledText text= fViewer.getTextWidget();
			if (Helper2.okToUse(text)) {

				if (fViewer instanceof ITextViewerExtension) {
					ITextViewerExtension e= (ITextViewerExtension) fViewer;
					e.removeVerifyKeyListener(fInternalListener);
				} else {
					text.removeVerifyKeyListener(fInternalListener);
				}

				fKeyListenerHooked= false;
			}
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
				++ count;
		}
		return count;
	}

	/*
	 * @see IContentAssist#showPossibleCompletions
	 */
	public String showPossibleCompletions() {
		return fProposalPopup.showProposals(false);
	}

	/**
	 * Hides the proposal popup.
	 */
	public void hidePossibleCompletions() {
		if (fProposalPopup != null)
			fProposalPopup.hide();
	}

	/**
	 * Hides any open pop-ups.
	 */
	protected void hide() {
		if (fProposalPopup != null)
			fProposalPopup.hide();
		if (fContextInfoPopup != null)
			fContextInfoPopup.hide();
	}

	/**
	 * Callback to signal this content assistant that the presentation of the possible completions has been stopped.
	 * @since 2.1
	 */
	protected void possibleCompletionsClosed() {
	}

	/*
	 * @see IContentAssist#showContextInformation
	 */
	public String showContextInformation() {
		return fContextInfoPopup.showContextProposals(false);
	}


	/**
	 * Callback to signal this content assistant that the presentation of the context information has been stopped.
	 * @since 2.1
	 */
	protected void contextInformationClosed() {
	}

	/**
	 * Requests that the specified context information to be shown.
	 *
	 * @param contextInformation the context information to be shown
	 * @param position the position to which the context information refers to
	 * @since 2.0
	 */
	void showContextInformation(IContextInformation contextInformation, int position) {
		fContextInfoPopup.showContextInformation(contextInformation, position);
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
	 * Returns the content assist processor for the content
	 * type of the specified document position.
	 *
	 * @param viewer the text viewer
	 * @param offset a offset within the document
	 * @return a content-assist processor or <code>null</code> if none exists
	 */
	private IContentAssistProcessor getProcessor(ITextViewer viewer, int offset) {
		try {
			String type= TextUtilities.getContentType(viewer.getDocument(), getDocumentPartitioning(), offset, true);
			return getContentAssistProcessor(type);
		} catch (BadLocationException x) {
		}
		return null;
	}

	/**
	 * Returns an array of completion proposals computed based on
	 * the specified document position. The position is used to
	 * determine the appropriate content assist processor to invoke.
	 *
	 * @param viewer the viewer for which to compute the proposals
	 * @param position a document position
	 * @return an array of completion proposals
	 *
	 * @see IContentAssistProcessor#computeCompletionProposals
	 */
	ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position) {
		if (fProposals != null) {
			return fProposals;
		} else if (fProposalStrings != null) {
			ICompletionProposal[] result= new ICompletionProposal[fProposalStrings.length];
			for (int i= 0; i < fProposalStrings.length; i++) {
				result[i]= new CompletionProposal(fProposalStrings[i], position, fProposalStrings[i].length(), fProposalStrings[i].length());
			}
			return result;
		} else return null;
	}

	/**
	 * Returns an array of context information objects computed based
	 * on the specified document position. The position is used to determine
	 * the appropriate content assist processor to invoke.
	 *
	 * @param viewer the viewer for which to compute the context information
	 * @param position a document position
	 * @return an array of context information objects
	 *
	 * @see IContentAssistProcessor#computeContextInformation
	 */
	IContextInformation[] computeContextInformation(ITextViewer viewer, int position) {
		fLastErrorMessage= null;

		IContextInformation[] result= null;

		IContentAssistProcessor p= getProcessor(viewer, position);
		if (p != null) {
			result= p.computeContextInformation(viewer, position);
			fLastErrorMessage= p.getErrorMessage();
		}

		return result;
	}

	/**
	 * Returns the context information validator that should be used to
	 * determine when the currently displayed context information should
	 * be dismissed. The position is used to determine the appropriate
	 * content assist processor to invoke.
	 *
	 * @param textViewer the text viewer
	 * @param offset a document offset
	 * @return an validator
	 *
	 * @see IContentAssistProcessor#getContextInformationValidator
	 */
	IContextInformationValidator getContextInformationValidator(ITextViewer textViewer, int offset) {
		IContentAssistProcessor p= getProcessor(textViewer, offset);
		return p != null ? p.getContextInformationValidator() : null;
	}

	/**
	 * Returns the context information presenter that should be used to
	 * display context information. The position is used to determine the appropriate
	 * content assist processor to invoke.
	 *
	 * @param textViewer the text viewer
	 * @param offset a document offset
	 * @return a presenter
	 * @since 2.0
	 */
	IContextInformationPresenter getContextInformationPresenter(ITextViewer textViewer, int offset) {
		IContextInformationValidator validator= getContextInformationValidator(textViewer, offset);
		if (validator instanceof IContextInformationPresenter)
			return (IContextInformationPresenter) validator;
		return null;
	}

	/**
	 * Returns the characters which when typed by the user should automatically
	 * initiate proposing completions. The position is used to determine the
	 * appropriate content assist processor to invoke.
	 *
	 * @param textViewer the text viewer
	 * @param offset a document offset
	 * @return the auto activation characters
	 *
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters
	 */
	private char[] getCompletionProposalAutoActivationCharacters(ITextViewer textViewer, int offset) {
		IContentAssistProcessor p= getProcessor(textViewer, offset);
		return p != null ? p.getCompletionProposalAutoActivationCharacters() : null;
	}

	/**
	 * Returns the characters which when typed by the user should automatically
	 * initiate the presentation of context information. The position is used
	 * to determine the appropriate content assist processor to invoke.
	 *
	 * @param textViewer the text viewer
	 * @param offset a document offset
	 * @return the auto activation characters
	 *
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters
	 */
	private char[] getContextInformationAutoActivationCharacters(ITextViewer textViewer, int offset) {
		IContentAssistProcessor p= getProcessor(textViewer, offset);
		return p != null ? p.getContextInformationAutoActivationCharacters() : null;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
	 * @since 2.0
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		hidePossibleCompletions();
		return true;
	}

	/**
	 * @param completionPosition the completion position
	 */
	public void setCompletionPosition(int completionPosition) {
		fCompletionPosition= completionPosition;
	}

	/**
	 * @return the completion position
	 */
	public int getCompletionPosition() {
		return fCompletionPosition;
	}

	/**
	 * @param proposals the proposals
	 */
	public void setCompletions(String[] proposals) {
		fProposalStrings= proposals;
	}

	/**
	 * @param proposals the proposals
	 */
	public void setCompletions(ICompletionProposal[] proposals) {
		fProposals= proposals;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner, int)
	 * @since 3.0
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		if (priority > WIDGET_PRIORITY) {
			hidePossibleCompletions();
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
	 * Returns whether any popups controlled by the receiver have the input focus.
	 *
	 * @return <code>true</code> if any of the managed popups have the focus, <code>false</code> otherwise
	 */
	public boolean hasFocus() {
		return (fProposalPopup != null && fProposalPopup.hasFocus())
				|| (fContextInfoPopup != null && fContextInfoPopup.hasFocus());
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.IContentAssistantExtension#completePrefix()
	 */
	public String completePrefix() {
		return null;
	}

	/**
	 * @param proposal the proposal
	 */
	public void fireProposalChosen(ICompletionProposal proposal) {
		List list= new ArrayList(fProposalListeners);
		for (Iterator it= list.iterator(); it.hasNext();) {
			IProposalListener listener= (IProposalListener) it.next();
			listener.proposalChosen(proposal);
		}

	}

	/**
	 * @param listener the proposal listener
	 */
	public void removeProposalListener(IProposalListener listener) {
		fProposalListeners.remove(listener);
	}

	/**
	 * @param listener the proposal listener
	 */
	public void addProposalListener(IProposalListener listener) {
		fProposalListeners.add(listener);
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
}

