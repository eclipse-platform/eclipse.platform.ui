package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.util.Assert;



/**
 * The standard implementation of the <code>IContentAssistant</code> interface.
 * Usually, clients instantiate this class and configure it before using it.
 */
public class ContentAssistant implements IContentAssistant {
	
	/**
	 * A generic closer class used to monitor various
	 * interface events in order to determine whether
	 * content-assist should be terminated and all
	 * associated windows closed.
	 */
	class Closer implements ControlListener, MouseListener, FocusListener, IViewportListener {

		protected void install() {
			Control w= fViewer.getTextWidget();
			if (Helper.okToUse(w)) {
				
				Control shell= w.getShell();
				shell.addControlListener(this);
					
				w.addMouseListener(this);
				w.addFocusListener(this);
			}
			
			fViewer.addViewportListener(this);
		}

		protected void uninstall() {
			Control w= fViewer.getTextWidget();
			if (Helper.okToUse(w)) {
				
				Control shell= w.getShell();
				shell.removeControlListener(this);
				
				w.removeMouseListener(this);
				w.removeFocusListener(this);
			}
			
			fViewer.removeViewportListener(this);
		}

		// WindowListener
		public void controlResized(ControlEvent e) {
			hide();
		}

		public void controlMoved(ControlEvent e) {
			hide();
		}

		// MouseListener
		public void mouseDown(MouseEvent e) {
			hide();
		}

		public void mouseUp(MouseEvent e) {
		}

		public void mouseDoubleClick(MouseEvent e) {
			hide();
		}

		// FocusListener
		public void focusGained(FocusEvent e) {
		}

		public void focusLost(FocusEvent e) {
			Control control= fViewer.getTextWidget();
			Display d= control.getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					if (!fProposalPopup.hasFocus() && !fContextInfoPopup.hasFocus()) {
						hide();
					}
				}
			});
		}

		// IViewPortListener
		public void viewportChanged(int topIndex) {
			hide();
		}
		
		/**
		 * Hides any open popups.
		 */
		protected void hide() {
			fProposalPopup.hide();
			fContextInfoPopup.hide();
		}
	};
	
	/**
	 * An implementation of IContentAssistListener, this class is
	 * used to monitor key events in support of automatic activation
	 * of the content assistant. If enabled, the implementation utilizes a
	 * thread to watch for input characters matching the activation
	 * characters specified by the content assist processor, and if
	 * detected, will wait the indicated delay interval before
	 * activating the content assistant.
	 */
	class AutoAssistListener implements IContentAssistListener, Runnable {
		
		private Thread fThread;
		private int fDelayInterval;
		private boolean fIsReset= false;
		private Object fMutex= new Object();
		private int fShowStyle;
		
		private final static int SHOW_PROPOSALS= 1;
		private final static int SHOW_CONTEXT_INFO= 2;
		
		protected AutoAssistListener(int delayInterval) {
			fDelayInterval= delayInterval;
		}

		protected void start(int showStyle) {
			fShowStyle= showStyle;
			fThread= new Thread(this, "AutoAssist Delay"); //$NON-NLS-1$
			fThread.start();
		}

		public void run() {
			try {
				while (true) {
					synchronized (fMutex) {
						fMutex.wait(fDelayInterval);
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
			if (fThread != null) {
				fThread.interrupt();
			}
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
		
		public boolean verifyKey(VerifyEvent e) {
			
			int showStyle;
			int pos= fViewer.getSelectedRange().x;
			char[] activation= getCompletionProposalAutoActivationCharacters(fViewer.getDocument(), pos);
			
			if (contains(activation, e.character) && !fProposalPopup.isActive())
				showStyle= SHOW_PROPOSALS;
			else {
				activation= getContextInformationAutoActivationCharacters(fViewer.getDocument(), pos);
				if (contains(activation, e.character) && !fContextInfoPopup.isActive())
					showStyle= SHOW_CONTEXT_INFO;
				else {
					if (fThread != null && fThread.isAlive())
						stop();
					return true;
				}
			}
			
			if (fThread != null && fThread.isAlive())
				reset(showStyle);
			else
				start(showStyle);
			
			return false;
		}
				
		public void processEvent(VerifyEvent e) {}
		
		protected void showAssist(final int showStyle) {
			Control control= fViewer.getTextWidget();
			Display d= control.getDisplay();
			if (d != null) {
				try {
					d.syncExec(new Runnable() {
						public void run() {
							if (showStyle == SHOW_PROPOSALS)
								fProposalPopup.showProposals(false);
							else if (showStyle == SHOW_CONTEXT_INFO)
								fContextInfoPopup.showContextProposals(false);
						}
					});
				} catch (SWTError e) {
				}
			}
		}
	};
	
	/**
	 * The laypout manager layouts the various
	 * windows associated with the content assistant based on the
	 * settings of the content assistant.
	 */
	class LayoutManager implements Listener {
		
		// Presentation types.
		public final static int LAYOUT_PROPOSAL_SELECTOR= 0;
		public final static int LAYOUT_CONTEXT_SELECTOR= 1;
		public final static int LAYOUT_CONTEXT_INFO_POPUP= 2;

		int fContextType= LAYOUT_CONTEXT_SELECTOR;
		Shell[] fShells= new Shell[3];
		Object[] fPopups= new Object[3];

		protected void add(Object popup, Shell shell, int type) {
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
			
			layout(type);
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
							layout(LAYOUT_PROPOSAL_SELECTOR);
						// Restore event notification to the proposal popup.
						addContentAssistListener((IContentAssistListener) fPopups[LAYOUT_PROPOSAL_SELECTOR], PROPOSAL_SELECTOR);						
					}
					fContextType= LAYOUT_CONTEXT_INFO_POPUP;
					break;
				
				case LAYOUT_CONTEXT_INFO_POPUP:
					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						if (fContextInfoPopupOrientation == CONTEXT_INFO_BELOW)
							layout(LAYOUT_PROPOSAL_SELECTOR);
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
		
		protected void layout(int type) {
			switch (type) {
				case LAYOUT_PROPOSAL_SELECTOR:
					layoutProposalSelector();
					break;
				case LAYOUT_CONTEXT_SELECTOR:
					layoutContextSelector();
					break;
				case LAYOUT_CONTEXT_INFO_POPUP:
					layoutContextInfoPopup();
					break;
			}
		}

		protected void layoutProposalSelector() {
			if (fContextType == LAYOUT_CONTEXT_INFO_POPUP &&
					fContextInfoPopupOrientation == CONTEXT_INFO_BELOW &&
					Helper.okToUse(fShells[LAYOUT_CONTEXT_INFO_POPUP])) {
				// Stack proposal selector beneath the tip box.
				fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getStackedLocation(fShells[LAYOUT_CONTEXT_INFO_POPUP]));
			} else if (fContextType != LAYOUT_CONTEXT_SELECTOR ||
						!Helper.okToUse(fShells[LAYOUT_CONTEXT_SELECTOR])) {
				// There are no other presentations to be concerned with,
				// so place the proposal selector beneath the cursor line.
				fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getBelowLocation());
			} else {
				switch (fProposalPopupOrientation) {
					case PROPOSAL_REMOVE:
						// Remove the tip selector and place the
						// proposal selector beneath the cursor line.
						fShells[LAYOUT_CONTEXT_SELECTOR].dispose();
						fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getBelowLocation());
						break;
	
					case PROPOSAL_OVERLAY:
						// Overlay the tip selector with the proposal selector.
						fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getBelowLocation());
						break;
	
					case PROPOSAL_STACKED:
						// Stack the proposal selector beneath the tip selector.
						fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getStackedLocation(fShells[LAYOUT_CONTEXT_SELECTOR]));
						break;
				}
			}
		}
		
		protected void layoutContextSelector() {
			// Always place the context selector beneath the cursor line.
			fShells[LAYOUT_CONTEXT_SELECTOR].setLocation(getBelowLocation());
			
			if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
				switch (fProposalPopupOrientation) {
					case PROPOSAL_REMOVE:
						// Remove the proposal selector.
						fShells[LAYOUT_PROPOSAL_SELECTOR].dispose();
						break;
	
					case PROPOSAL_OVERLAY:
						// The proposal selector has been overlayed by the tip selector.
						break;
	
					case PROPOSAL_STACKED:
						// Stack the proposal selector beneath the tip selector.
						fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getStackedLocation(fShells[LAYOUT_CONTEXT_SELECTOR]));
						break;
				}
			}
		}
		
		protected void layoutContextInfoPopup() {
			switch (fContextInfoPopupOrientation) {
				case CONTEXT_INFO_ABOVE:
					// Place the popup above the cursor line.
					Point position= getAboveLocation();
					position.y -= fShells[LAYOUT_CONTEXT_INFO_POPUP].getSize().y;
					fShells[LAYOUT_CONTEXT_INFO_POPUP].setLocation(position);
					break;
				
				case CONTEXT_INFO_BELOW:
					// Place the popup beneath the cursor line.
					fShells[LAYOUT_CONTEXT_INFO_POPUP].setLocation(getBelowLocation());
					if (Helper.okToUse(fShells[LAYOUT_PROPOSAL_SELECTOR])) {
						// Stack the proposal selector beneath the context info popup.
						fShells[LAYOUT_PROPOSAL_SELECTOR].setLocation(getStackedLocation(fShells[LAYOUT_CONTEXT_INFO_POPUP]));
					}
					break;
			}
		}
		
		protected Point getAboveLocation() {
			StyledText text= fViewer.getTextWidget();
			int caret= text.getCaretOffset();
			Point p= text.getLocationAtOffset(caret);
			return text.toDisplay(p);
		}
	
		protected Point getBelowLocation() {
			StyledText text= fViewer.getTextWidget();
			Point p= getAboveLocation();
			return new Point(p.x, p.y + text.getLineHeight());
		}
	
		protected Point getStackedLocation(Shell shell) {
			Point p= shell.getLocation();
			Point size= shell.getSize();
			p.x += size.x / 4;
			p.y += size.y;
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
	};
	
	/**
	 * Internal key listener and event consumer.
	 */
	class InternalListener implements VerifyKeyListener, IEventConsumer {
		
		/**
		 * Verifies key events by notifying the registered listeners.
		 * Each listener is allowed to indicate that the event has been
		 * handled and should not be further processed.
		 *
		 * @param event the verify event
		 * @see VerifyKeyListener#verifyKey
		 */
		public void verifyKey(VerifyEvent e) {
			IContentAssistListener[] listeners= (IContentAssistListener[]) fListeners.clone();
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
			
			IContentAssistListener[] listeners= (IContentAssistListener[])fListeners.clone();
			for (int i= 0; i < listeners.length; i++) {
				if (listeners[i] != null) {
					listeners[i].processEvent(event);
					if (!event.doit)
						return;
				}
			}
		}
	};
	
	
	// Content-Assist Listener types
	final static int AUTO_ASSIST= 0;
	final static int CONTEXT_SELECTOR= 1;
	final static int PROPOSAL_SELECTOR= 2;
	final static int CONTEXT_INFO_POPUP= 3;
		
	private int fAutoActivationDelay= 500;
	private boolean fIsAutoActivated= false;
	private int fProposalPopupOrientation= PROPOSAL_OVERLAY;
	private int fContextInfoPopupOrientation= CONTEXT_INFO_ABOVE;
	private Color fContextInfoPopupBackground;
	private Map fProcessors;	
	
	private ITextViewer fViewer;
	private String fLastErrorMessage;
	
	private Closer fCloser;
	private LayoutManager fLayoutManager;
	private AutoAssistListener fAutoAssistListener;
	private InternalListener fInternalListener;
	private CompletionProposalPopup fProposalPopup;
	private ContextInformationPopup fContextInfoPopup;
	
	private boolean fKeyListenerHooked= false;
	private IContentAssistListener[] fListeners= new IContentAssistListener[4];
	
	/**
	 * Creates a new content assistant. The content assistant is not automatically activated,
	 * overlays the completion proposals with context information list if necessary, and
	 * shows the context information above the location at which it was activated. If auto
	 * activation will be enabled, without further configuration steps, this content assistant
	 * is activated after a 500 ms delay.
	 */	
	public ContentAssistant() {
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
	 *
	 * @param listener the listener to register
	 * @param type the type of listener
	 */
	void addContentAssistListener(IContentAssistListener listener, int type) {
		fListeners[type]= listener;
		
		if (fCloser == null && isCloserNeeded()) {
			fCloser= new Closer();
			fCloser.install();
		}
		if (isListenerHookNeeded()) {
			fViewer.setEventConsumer(fInternalListener);
			installKeyListener();
		}
	}
	/**
	 * Adds the given shell of the specified type to the layout.
	 * Valid types are defined by <code>LayoutManager</code>.
	 *
	 * @param popup a content assist popup
	 * @param shell the shell of the content-assist popup
	 * @param type the type of popup
	 */
	void addToLayout(Object popup, Shell shell, int type) {
		fLayoutManager.add(popup, shell, type);
	}
	/**
	 * Returns an array of completion proposals computed based on 
	 * the specified document position. The position is used to 
	 * determine the appropriate content assist processor to invoke.
	 *
	 * @param viewer the viewer for which to compute the prosposals
	 * @param position a document position
	 * @return an array of completion proposals
	 *
	 * @see IContentAssistProcessor#computeCompletionProposals
	 */
	ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int position) {
		fLastErrorMessage= null;
		
		ICompletionProposal[] result= null;
		
		IContentAssistProcessor p= getProcessor(viewer.getDocument(), position);
		if (p != null) {
			result= p.computeCompletionProposals(viewer, position);
			fLastErrorMessage= p.getErrorMessage();
		}
		
		return result;
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
		
		IContentAssistProcessor p= getProcessor(viewer.getDocument(), position);
		if (p != null) {
			result= p.computeContextInformation(viewer, position);
			fLastErrorMessage= p.getErrorMessage();
		}
		
		return result;
	}
	/**
	 * Sets the content assistant's auto activation state.
	 *
	 * @param enabled indicates whether auto activation is enabled or not
	 */
	public void enableAutoActivation(boolean enabled) {
		fIsAutoActivated= enabled;
	}
	/**
	 * Returns the characters which when typed by the user should automatically
	 * initiate proposing completions. The position is used to determine the 
	 * appropriate content assist processor to invoke.
	 *
	 * @param document the document
	 * @param position a document position
	 * @return the auto activation characters
	 *
	 * @see IContentAssistProcessor#getCompletionProposalAutoActivationCharacters
	 */
	private char[] getCompletionProposalAutoActivationCharacters(IDocument document, int position) {
		IContentAssistProcessor p= getProcessor(document, position);
		if (p != null)
			return p.getCompletionProposalAutoActivationCharacters();
		return null;
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
	 * Returns the background of the context information popup.
	 *
	 * @return the background of the context information popup
	 */
	Color getContextInfoPopupBackground() {
		return fContextInfoPopupBackground;
	}
	/**
	 * Returns the characters which when typed by the user should automatically
	 * initiate the presenation of context information. The position is used
	 * to determine the appropriate content assist processor to invoke.
	 *
	 * @param document the document
	 * @param position a document position
	 * @return the auto activation characters
	 *
	 * @see IContentAssistProcessor#getContextInformationAutoActivationCharacters
	 */
	private char[] getContextInformationAutoActivationCharacters(IDocument document, int position) {
		IContentAssistProcessor p= getProcessor(document, position);
		if (p != null)
			return p.getContextInformationAutoActivationCharacters();
		return null;
	}
	/**
	 * Returns the context information validator that should be used to 
	 * determine when the currently displayed context information should
	 * be dismissed. The position is used to determine the appropriate 
	 * content assist processor to invoke.
	 *
	 * @param document the document
	 * @param position a document position
	 * @return an array of validator
	 *
	 * @see IContentAssistProcessor#getContextInformationValidator
	 */
	IContextInformationValidator getContextInformationValidator(IDocument document, int position) {
		IContentAssistProcessor p= getProcessor(document, position);
		if (p != null)
			return p.getContextInformationValidator();
		return null;
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
	 * @param document the document
	 * @param position a position within the document
	 * @return a content-assist processor or <code>null</code> if none exists
	 */
	private IContentAssistProcessor getProcessor(IDocument document, int position) {
		try {
			String type= document.getContentType(position);
			return getContentAssistProcessor(type);
		} catch (BadLocationException x) {
		}
		
		return null;
	}
	/*
	 * @see IContentAssist#install
	 */
	public void install(ITextViewer textViewer) {
		Assert.isNotNull(textViewer);
		
		fViewer= textViewer;
		
		fLayoutManager= new LayoutManager();
		fInternalListener= new InternalListener();
		
		fContextInfoPopup= new ContextInformationPopup(this, fViewer);
		fProposalPopup= new CompletionProposalPopup(this, fViewer, new AdditionalInfoPopup(fAutoActivationDelay));
		
		if (fIsAutoActivated) {
			fAutoAssistListener= new AutoAssistListener(fAutoActivationDelay);
			addContentAssistListener(fAutoAssistListener, AUTO_ASSIST);
		}
	}
	/**
	 * Installs a key listener on the text viewer's widget.
	 */
	private void installKeyListener() {
		if (!fKeyListenerHooked) {
			StyledText text= fViewer.getTextWidget();
			if (Helper.okToUse(text)) {
				text.addVerifyKeyListener(fInternalListener);
				fKeyListenerHooked= true;
			}
		}
	}
	/**
	 * Returns whether a closer should be installed.
	 * A single, shared closer is used to monitor for events
	 * that should result in any open popups being closed.
	 *
	 * @return <code>true</code> if a closer is needed
	 */
	private boolean isCloserNeeded() {
		// Do not include AUTO_ASSIST as it doesn't need a closer.
		for (int i= CONTEXT_SELECTOR; i <= CONTEXT_INFO_POPUP; i++) {
			if (fListeners[i] != null)
				return true;
		}
		return false;
	}
	/**
	 * Returns whether a listener hook should be installed.
	 * A single, shared listener is used to monitor for events
	 * that are to be propagated to the registered listeners.
	 *
	 * @return <code>true</code> if a listener hook is needed
	 */
	private boolean isListenerHookNeeded() {
		for (int i= AUTO_ASSIST; i <= CONTEXT_INFO_POPUP; i++) {
			if (fListeners[i] != null)
				return true;
		}
		return false;
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
	 * Unregisters a content assist listener.
	 *
	 * @param listener the listener to unregister
	 * @param type the type of listener
	 *
	 * @see #addContentAssistListener
	 */
	void removeContentAssistListener(IContentAssistListener listener, int type) {
		fListeners[type]= null;
		
		if (fCloser != null && !isCloserNeeded()) {
			fCloser.uninstall();
			fCloser= null;
		}
		if (!isListenerHookNeeded()) {
			uninstallKeyListener();
			fViewer.setEventConsumer(null);
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
	/**
	 * Sets the context information popup's background color.
	 *
	 * @param background the background color
	 */
	public void setContextInformationPopupBackground(Color background) {
		fContextInfoPopupBackground= background;
	}
	/**
	 * Sets the context information popup's orientation
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
	 * Sets the proposal popups' orientation
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
	/*
	 * @see IContentAssist#showContextInformation
	 */
	public String showContextInformation() {
		return fContextInfoPopup.showContextProposals(true);
	}
	/**
	 * Requests that the specified context information to be shown.
	 *
	 * @param contextInformation the context information to be shown
	 */
	void showContextInformation(IContextInformation contextInformation) {
		fContextInfoPopup.showContextInformation(contextInformation);
	}
	/*
	 * @see IContentAssist#showPossibleCompletions
	 */
	public String showPossibleCompletions() {
		return fProposalPopup.showProposals(true);
	}
	/*
	 * @see IContentAssist#uninstall
	 */
	public void uninstall() {
		if (fAutoAssistListener != null) {
			removeContentAssistListener(fAutoAssistListener, AUTO_ASSIST);
			fAutoAssistListener= null;
		}
	}
	/**
	 * Uninstall the key listener from the text viewer's widget.
	 */
	private void uninstallKeyListener() {
		if (fKeyListenerHooked) {
			StyledText text= fViewer.getTextWidget();
			if (Helper.okToUse(text)) {
				text.removeVerifyKeyListener(fInternalListener);
				fKeyListenerHooked= false;
			}
		}
	}
}
