/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Danail Nachev <d.nachev@gmail.com> - http://bugs.eclipse.org/348608
 *******************************************************************************/
package org.eclipse.jface.text.link;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.internal.text.link.contentassist.ContentAssistant2;
import org.eclipse.jface.internal.text.link.contentassist.IProposalListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;


/**
 * The UI for linked mode. Detects events that influence behavior of the linked mode
 * UI and acts upon them.
 * <p>
 * <code>LinkedModeUI</code> relies on all added
 * <code>LinkedModeUITarget</code>s to provide implementations of
 * <code>ITextViewer</code> that implement <code>ITextViewerExtension</code>,
 * and the documents being edited to implement <code>IDocumentExtension3</code>.
 * </p>
 * <p>
 * Clients may instantiate and extend this class.
 * </p>
 *
 * @since 3.0
 */
public class LinkedModeUI {

	/* cycle constants */
	/**
	 * Constant indicating that this UI should never cycle from the last
	 * position to the first and vice versa.
	 */
	public static final Object CYCLE_NEVER= new Object();
	/**
	 * Constant indicating that this UI should always cycle from the last
	 * position to the first and vice versa.
	 */
	public static final Object CYCLE_ALWAYS= new Object();
	/**
	 * Constant indicating that this UI should cycle from the last position to
	 * the first and vice versa if its model is not nested.
	 */
	public static final Object CYCLE_WHEN_NO_PARENT= new Object();

	/**
	 * Listener that gets notified when the linked mode UI switches its focus position.
	 * <p>
	 * Clients may implement this interface.
	 * </p>
	 */
	public interface ILinkedModeUIFocusListener {
		/**
		 * Called when the UI for the linked mode leaves a linked position.
		 *
		 * @param position the position being left
		 * @param target the target where <code>position</code> resides in
		 */
		void linkingFocusLost(LinkedPosition position, LinkedModeUITarget target);
		/**
		 * Called when the UI for the linked mode gives focus to a linked position.
		 *
		 * @param position the position being entered
		 * @param target the target where <code>position</code> resides in
		 */
		void linkingFocusGained(LinkedPosition position, LinkedModeUITarget target);
	}

	/**
	 * Null object implementation of focus listener.
	 */
	private static final class EmtpyFocusListener implements ILinkedModeUIFocusListener {

		public void linkingFocusGained(LinkedPosition position, LinkedModeUITarget target) {
			// ignore
		}

		public void linkingFocusLost(LinkedPosition position, LinkedModeUITarget target) {
			// ignore
		}
	}

	/**
	 * A link target consists of a viewer and gets notified if the linked mode UI on
	 * it is being shown.
	 * <p>
	 * Clients may extend this class.
	 * </p>
	 * @since 3.0
	 */
	public static abstract class LinkedModeUITarget implements ILinkedModeUIFocusListener {
		/**
		 * Returns the viewer represented by this target, never <code>null</code>.
		 *
		 * @return the viewer associated with this target.
		 */
		public abstract ITextViewer getViewer();

		/**
		 * The viewer's text widget is initialized when the UI first connects
		 * to the viewer and never changed thereafter. This is to keep the
		 * reference of the widget that we have registered our listeners with,
		 * as the viewer, when it gets disposed, does not remember it, resulting
		 * in a situation where we cannot uninstall the listeners and a memory leak.
		 */
		StyledText fWidget;

		/** The cached shell - same reason as fWidget. */
		Shell fShell;

		/** The registered listener, or <code>null</code>. */
		KeyListener fKeyListener;

		/** The cached custom annotation model. */
		LinkedPositionAnnotations fAnnotationModel;
	}

	private static final class EmptyTarget extends LinkedModeUITarget {

		private ITextViewer fTextViewer;

		/**
		 * @param viewer the viewer
		 */
		public EmptyTarget(ITextViewer viewer) {
			Assert.isNotNull(viewer);
			fTextViewer= viewer;
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedModeUI.ILinkedUITarget#getViewer()
		 */
		public ITextViewer getViewer() {
			return fTextViewer;
		}

		/**
		 * {@inheritDoc}
		 */
		public void linkingFocusLost(LinkedPosition position, LinkedModeUITarget target) {
		}

		/**
		 * {@inheritDoc}
		 */
		public void linkingFocusGained(LinkedPosition position, LinkedModeUITarget target) {
		}

	}

	/**
	 * Listens for state changes in the model.
	 */
	private final class ExitListener implements ILinkedModeListener {
		public void left(LinkedModeModel model, int flags) {
			leave(ILinkedModeListener.EXIT_ALL | flags);
		}

		public void suspend(LinkedModeModel model) {
			disconnect();
			redraw();
		}

		public void resume(LinkedModeModel model, int flags) {
			if ((flags & ILinkedModeListener.EXIT_ALL) != 0) {
				leave(flags);
			} else {
				connect();
				if ((flags & ILinkedModeListener.SELECT) != 0)
					select();
				ensureAnnotationModelInstalled();
				redraw();
			}
		}
	}

	/**
	 * Exit flags returned if a custom exit policy wants to exit linked mode.
	 * <p>
	 * Clients may instantiate this class.
	 * </p>
	 */
	public static class ExitFlags {
		/** The flags to return in the <code>leave</code> method. */
		public int flags;
		/** The doit flag of the checked <code>VerifyKeyEvent</code>. */
		public boolean doit;
		/**
		 * Creates a new instance.
		 *
		 * @param flags the exit flags
		 * @param doit the doit flag for the verify event
		 */
		public ExitFlags(int flags, boolean doit) {
			this.flags= flags;
			this.doit= doit;
		}
	}

	/**
	 * An exit policy can be registered by a caller to get custom exit
	 * behavior.
	 * <p>
	 * Clients may implement this interface.
	 * </p>
	 */
	public interface IExitPolicy {
		/**
		 * Checks whether the linked mode should be left after receiving the
		 * given <code>VerifyEvent</code> and selection. Note that the event
		 * carries widget coordinates as opposed to <code>offset</code> and
		 * <code>length</code> which are document coordinates.
		 *
		 * @param model the linked mode model
		 * @param event the verify event
		 * @param offset the offset of the current selection
		 * @param length the length of the current selection
		 * @return valid exit flags or <code>null</code> if no special action
		 *         should be taken
		 */
		ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length);
	}

	/**
	 * A NullObject implementation of <code>IExitPolicy</code>.
	 */
	private static class NullExitPolicy implements IExitPolicy {
		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedModeUI.IExitPolicy#doExit(org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			return null;
		}
	}

	/**
	 * Listens for shell events and acts upon them.
	 */
	private class Closer implements ShellListener, ITextInputListener {

		public void shellActivated(ShellEvent e) {
		}

		public void shellClosed(ShellEvent e) {
			leave(ILinkedModeListener.EXIT_ALL);
		}

		public void shellDeactivated(ShellEvent e) {
// 			TODO re-enable after debugging
//			if (true) return;

			// from LinkedPositionUI:

			// don't deactivate on focus lost, since the proposal popups may take focus
			// plus: it doesn't hurt if you can check with another window without losing linked mode
			// since there is no intrusive popup sticking out.

			// need to check first what happens on reentering based on an open action
			// Seems to be no problem

			// Better:
			// Check with content assistant and only leave if its not the proposal shell that took the
			// focus away.

			StyledText text;
			final ITextViewer viewer;
			Display display;

			if (fCurrentTarget == null || (text= fCurrentTarget.fWidget) == null
					|| text.isDisposed() || (display= text.getDisplay()) == null
					|| display.isDisposed()
					|| (viewer= fCurrentTarget.getViewer()) == null)
			{
				leave(ILinkedModeListener.EXIT_ALL);
			}
			else
			{
				// Post in UI thread since the assistant popup will only get the focus after we lose it.
				display.asyncExec(new Runnable() {
					public void run() {
						if (fIsActive && viewer instanceof IEditingSupportRegistry) {
							IEditingSupport[] helpers= ((IEditingSupportRegistry) viewer).getRegisteredSupports();
							for (int i= 0; i < helpers.length; i++) {
								if (helpers[i].ownsFocusShell())
									return;
							}
						}

						// else
						leave(ILinkedModeListener.EXIT_ALL);

					}
				});
			}
		}

		public void shellDeiconified(ShellEvent e) {
		}

		public void shellIconified(ShellEvent e) {
			leave(ILinkedModeListener.EXIT_ALL);
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
			leave(ILinkedModeListener.EXIT_ALL);
		}

		/*
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		}

	}

	/**
	 * @since 3.1
	 */
	private class DocumentListener implements IDocumentListener {
		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {

			// default behavior: any document change outside a linked position
			// causes us to exit
			int end= event.getOffset() + event.getLength();
			for (int offset= event.getOffset(); offset <= end; offset++) {
				if (!fModel.anyPositionContains(offset)) {
					ITextViewer viewer= fCurrentTarget.getViewer();
					if (fFramePosition != null && viewer instanceof IEditingSupportRegistry) {
						IEditingSupport[] helpers= ((IEditingSupportRegistry) viewer).getRegisteredSupports();
						for (int i= 0; i < helpers.length; i++) {
							if (helpers[i].isOriginator(null, new Region(fFramePosition.getOffset(), fFramePosition.getLength())))
								return;
						}
					}

					leave(ILinkedModeListener.EXTERNAL_MODIFICATION);
					return;
				}
			}

			// Make sure that any document change is done inside a compound change
			beginCompoundChangeIfNeeded();

		}

		/*
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
		}
	}

	/**
	 * Listens for key events, checks the exit policy for custom exit
	 * strategies but defaults to handling Tab, Enter, and Escape.
	 */
	private class KeyListener implements VerifyKeyListener {

		private boolean fIsEnabled= true;

		public void verifyKey(VerifyEvent event) {

			if (!event.doit || !fIsEnabled)
				return;

			Point selection= fCurrentTarget.getViewer().getSelectedRange();
			int offset= selection.x;
			int length= selection.y;

			// if the custom exit policy returns anything, use that
			ExitFlags exitFlags= fExitPolicy.doExit(fModel, event, offset, length);
			if (exitFlags != null) {
				leave(exitFlags.flags);
				event.doit= exitFlags.doit;
				return;
			}

			// standard behavior:
			// (Shift+)Tab:	jumps from position to position, depending on cycle mode
			// Enter:		accepts all entries and leaves all (possibly stacked) environments, the last sets the caret
			// Esc:			accepts all entries and leaves all (possibly stacked) environments, the caret stays
			// ? what do we do to leave one level of a cycling model that is stacked?
			// -> This is only the case if the level was set up with forced cycling (CYCLE_ALWAYS), in which case
			// the caller is sure that one does not need by-level exiting.
			switch (event.character) {
				// [SHIFT-]TAB = hop between edit boxes
				case 0x09:
					if (!(fExitPosition != null && fExitPosition.includes(offset)) && !fModel.anyPositionContains(offset)) {
						// outside any edit box -> leave (all? TODO should only leave the affected, level and forward to the next upper)
						leave(ILinkedModeListener.EXIT_ALL);
						break;
					}

					if (event.stateMask == SWT.SHIFT)
						previous();
					else
						next();

					event.doit= false;
					break;

				// ENTER
				case 0x0A:
				// Ctrl+Enter on WinXP
				case 0x0D:
//					if ((fExitPosition != null && fExitPosition.includes(offset)) || !fModel.anyPositionContains(offset)) {
					if (!fModel.anyPositionContains(offset)) {
//					if ((fExitPosition == null || !fExitPosition.includes(offset)) && !fModel.anyPositionContains(offset)) {
						// outside any edit box or on exit position -> leave (all? TODO should only leave the affected, level and forward to the next upper)
						leave(ILinkedModeListener.EXIT_ALL);
						break;
					}

					// normal case: exit entire stack and put caret to final position
					leave(ILinkedModeListener.EXIT_ALL | ILinkedModeListener.UPDATE_CARET);
					event.doit= false;
					break;

				// ESC
				case 0x1B:
					// exit entire stack and leave caret
					leave(ILinkedModeListener.EXIT_ALL);
					event.doit= false;
					break;

				default:
					if (event.character != 0) {
						if (!controlUndoBehavior(offset, length)) {
							leave(ILinkedModeListener.EXIT_ALL);
							break;
						}
					}
			}
		}

		private boolean controlUndoBehavior(int offset, int length) {
			LinkedPosition position= fModel.findPosition(new LinkedPosition(fCurrentTarget.getViewer().getDocument(), offset, length, LinkedPositionGroup.NO_STOP));
			if (position != null) {

				// if the last position is not the same and there is an open change: close it.
				if (!position.equals(fPreviousPosition))
					endCompoundChangeIfNeeded();

				beginCompoundChangeIfNeeded();
			}

			fPreviousPosition= position;
			return fPreviousPosition != null;
		}

		/**
		 * @param enabled the new enabled state
		 */
		public void setEnabled(boolean enabled) {
			fIsEnabled= enabled;
		}

	}

	/**
	 * Installed as post selection listener on the watched viewer. Updates the
	 * linked position after cursor movement, even to positions not in the
	 * iteration list.
	 */
	private class MySelectionListener implements ISelectionChangedListener {

		/*
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection= event.getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textsel= (ITextSelection) selection;
				if (event.getSelectionProvider() instanceof ITextViewer) {
					IDocument doc= ((ITextViewer) event.getSelectionProvider()).getDocument();
					if (doc != null) {
						int offset= textsel.getOffset();
						int length= textsel.getLength();
						if (offset >= 0 && length >= 0) {
							LinkedPosition find= new LinkedPosition(doc, offset, length, LinkedPositionGroup.NO_STOP);
							LinkedPosition pos= fModel.findPosition(find);
							if (pos == null && fExitPosition != null && fExitPosition.includes(find))
								pos= fExitPosition;

							if (pos != null)
								switchPosition(pos, false, false);
						}
					}
				}
			}
		}

	}

	private class ProposalListener implements IProposalListener {

		/*
		 * @see org.eclipse.jface.internal.text.link.contentassist.IProposalListener#proposalChosen(org.eclipse.jface.text.contentassist.ICompletionProposal)
		 */
		public void proposalChosen(ICompletionProposal proposal) {
			next();
		}
	}

	/** The current viewer. */
	private LinkedModeUITarget fCurrentTarget;
	/**
	 * The manager of the linked positions we provide a UI for.
	 * @since 3.1
	 */
	private LinkedModeModel fModel;
	/** The set of viewers we manage. */
	private LinkedModeUITarget[] fTargets;
	/** The iterator over the tab stop positions. */
	private TabStopIterator fIterator;

	/* Our team of event listeners */
	/** The shell listener. */
	private Closer fCloser= new Closer();
	/** The linked mode listener. */
	private ILinkedModeListener fLinkedListener= new ExitListener();
	/** The selection listener. */
	private MySelectionListener fSelectionListener= new MySelectionListener();
	/** The content assist listener. */
	private ProposalListener fProposalListener= new ProposalListener();
	/**
	 * The document listener.
	 * @since 3.1
	 */
	private IDocumentListener fDocumentListener= new DocumentListener();

	/** The last caret position, used by fCaretListener. */
	private final Position fCaretPosition= new Position(0, 0);
	/** The exit policy to control custom exit behavior */
	private IExitPolicy fExitPolicy= new NullExitPolicy();
	/** The current frame position shown in the UI, or <code>null</code>. */
	private LinkedPosition fFramePosition;
	/** The last visited position, used for undo / redo. */
	private LinkedPosition fPreviousPosition;
	/** The content assistant used to show proposals. */
	private ContentAssistant2 fAssistant;
	/** The exit position. */
	private LinkedPosition fExitPosition;
	/** State indicator to prevent multiple invocation of leave. */
	private boolean fIsActive= false;
	/** The position updater for the exit position. */
	private IPositionUpdater fPositionUpdater= new DefaultPositionUpdater(getCategory());
	/** Whether to show context info. */
	private boolean fDoContextInfo= false;
	/** Whether we have begun a compound change, but not yet closed. */
	private boolean fHasOpenCompoundChange= false;
	/** The position listener. */
	private ILinkedModeUIFocusListener fPositionListener= new EmtpyFocusListener();
	private IAutoEditStrategy fAutoEditVetoer= new IAutoEditStrategy() {

		/*
		 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
		 */
		public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
			// invalidate the change to ensure that the change is performed on the document only.
			if (fModel.anyPositionContains(command.offset)) {
				command.doit= false;
				command.caretOffset= command.offset + command.length;
			}

		}
	};


	/** Whether this UI is in simple highlighting mode or not. */
	private boolean fSimple;

	/**
	 * Creates a new UI on the given model and the set of viewers. The model
	 * must provide a tab stop sequence with a non-empty list of tab stops.
	 *
	 * @param model the linked mode model
	 * @param targets the non-empty list of targets upon which the linked mode
	 *        UI should act
	 */
	public LinkedModeUI(LinkedModeModel model, LinkedModeUITarget[] targets) {
		constructor(model, targets);
	}

	/**
	 * Convenience constructor for just one viewer.
	 *
	 * @param model the linked mode model
	 * @param viewer the viewer upon which the linked mode UI should act
	 */
	public LinkedModeUI(LinkedModeModel model, ITextViewer viewer) {
		constructor(model, new LinkedModeUITarget[]{new EmptyTarget(viewer)});
	}

	/**
	 * Convenience constructor for multiple viewers.
	 *
	 * @param model the linked mode model
	 * @param viewers the non-empty list of viewers upon which the linked mode
	 *        UI should act
	 */
	public LinkedModeUI(LinkedModeModel model, ITextViewer[] viewers) {
		LinkedModeUITarget[] array= new LinkedModeUITarget[viewers.length];
		for (int i= 0; i < array.length; i++) {
			array[i]= new EmptyTarget(viewers[i]);
		}
		constructor(model, array);
	}

	/**
	 * Convenience constructor for one target.
	 *
	 * @param model the linked mode model
	 * @param target the target upon which the linked mode UI should act
	 */
	public LinkedModeUI(LinkedModeModel model, LinkedModeUITarget target) {
		constructor(model, new LinkedModeUITarget[]{target});
	}

	/**
	 * This does the actual constructor work.
	 *
	 * @param model the linked mode model
	 * @param targets the non-empty array of targets upon which the linked mode UI
	 *        should act
	 */
	private void constructor(LinkedModeModel model, LinkedModeUITarget[] targets) {
		Assert.isNotNull(model);
		Assert.isNotNull(targets);
		Assert.isTrue(targets.length > 0);
		Assert.isTrue(model.getTabStopSequence().size() > 0);

		fModel= model;
		fTargets= targets;
		fCurrentTarget= targets[0];
		fIterator= new TabStopIterator(fModel.getTabStopSequence());
		fIterator.setCycling(!fModel.isNested());
		fModel.addLinkingListener(fLinkedListener);

		fAssistant= new ContentAssistant2();
		fAssistant.addProposalListener(fProposalListener);
		// TODO find a way to set up content assistant.
//		fAssistant.setDocumentPartitioning(IJavaPartitions.JAVA_PARTITIONING);
		fCaretPosition.delete();
	}

	/**
	 * Starts this UI on the first position.
	 */
	public void enter() {
		fIsActive= true;
		connect();
		next();
	}

	/**
	 * Sets an <code>IExitPolicy</code> to customize the exit behavior of
	 * this linked mode UI.
	 *
	 * @param policy the exit policy to use.
	 */
	public void setExitPolicy(IExitPolicy policy) {
		fExitPolicy= policy;
	}

	/**
	 * Sets the exit position to move the caret to when linked mode mode is
	 * exited.
	 *
	 * @param target the target where the exit position is located
	 * @param offset the offset of the exit position
	 * @param length the length of the exit position (in case there should be a
	 *        selection)
	 * @param sequence set to the tab stop position of the exit position, or
	 *        <code>LinkedPositionGroup.NO_STOP</code> if there should be no
	 *        tab stop.
	 * @throws BadLocationException if the position is not valid in the viewer's
	 *         document
	 */
	public void setExitPosition(LinkedModeUITarget target, int offset, int length, int sequence) throws BadLocationException {
		// remove any existing exit position
		if (fExitPosition != null) {
			fExitPosition.getDocument().removePosition(fExitPosition);
			fIterator.removePosition(fExitPosition);
			fExitPosition= null;
		}

		IDocument doc= target.getViewer().getDocument();
		if (doc == null)
			return;

		fExitPosition= new LinkedPosition(doc, offset, length, sequence);
		doc.addPosition(fExitPosition); // gets removed in leave()
		if (sequence != LinkedPositionGroup.NO_STOP)
			fIterator.addPosition(fExitPosition);

	}

	/**
	 * Sets the exit position to move the caret to when linked mode is exited.
	 *
	 * @param viewer the viewer where the exit position is located
	 * @param offset the offset of the exit position
	 * @param length the length of the exit position (in case there should be a
	 *        selection)
	 * @param sequence set to the tab stop position of the exit position, or
	 * 		  <code>LinkedPositionGroup.NO_STOP</code> if there should be no tab stop.
	 * @throws BadLocationException if the position is not valid in the
	 *         viewer's document
	 */
	public void setExitPosition(ITextViewer viewer, int offset, int length, int sequence) throws BadLocationException {
		setExitPosition(new EmptyTarget(viewer), offset, length, sequence);
	}

	/**
	 * Sets the cycling mode to either of <code>CYCLING_ALWAYS</code>,
	 * <code>CYCLING_NEVER</code>, or <code>CYCLING_WHEN_NO_PARENT</code>,
	 * which is the default.
	 *
	 * @param mode the new cycling mode.
	 */
	public void setCyclingMode(Object mode) {
		if (mode != CYCLE_ALWAYS && mode != CYCLE_NEVER && mode != CYCLE_WHEN_NO_PARENT)
			throw new IllegalArgumentException();

		if (mode == CYCLE_ALWAYS || mode == CYCLE_WHEN_NO_PARENT && !fModel.isNested())
			fIterator.setCycling(true);
		else
			fIterator.setCycling(false);
	}

	void next() {
		if (fIterator.hasNext(fFramePosition)) {
			switchPosition(fIterator.next(fFramePosition), true, true);
			return;
		}
		leave(ILinkedModeListener.UPDATE_CARET);
	}

	void previous() {
		if (fIterator.hasPrevious(fFramePosition)) {
			switchPosition(fIterator.previous(fFramePosition), true, true);
		} else
			// dont't update caret, but rather select the current frame
			leave(ILinkedModeListener.SELECT);
	}

	private void triggerContextInfo() {
		fAssistant.showContextInformation();
	}

	/** Trigger content assist on choice positions */
	private void triggerContentAssist() {
		if (fFramePosition instanceof ProposalPosition) {
			ProposalPosition pp= (ProposalPosition) fFramePosition;
			ICompletionProposal[] choices= pp.getChoices();
			if (choices != null && choices.length > 0) {
				fAssistant.setCompletions(choices);
				fAssistant.showPossibleCompletions();
				return;
			}
		}

		fAssistant.setCompletions(new ICompletionProposal[0]);
		fAssistant.hidePossibleCompletions();
	}

	private void switchPosition(LinkedPosition pos, boolean select, boolean showProposals) {
		Assert.isNotNull(pos);
		if (pos.equals(fFramePosition))
			return;

		if (fFramePosition != null && fCurrentTarget != null)
			fPositionListener.linkingFocusLost(fFramePosition, fCurrentTarget);

		// undo
		endCompoundChangeIfNeeded();

		redraw(); // redraw current position being left - usually not needed
		IDocument oldDoc= fFramePosition == null ? null : fFramePosition.getDocument();
		IDocument newDoc= pos.getDocument();

		switchViewer(oldDoc, newDoc, pos);
		fFramePosition= pos;

		if (select)
			select();
		if (fFramePosition == fExitPosition && !fIterator.isCycling())
			leave(ILinkedModeListener.NONE);
		else {
			redraw(); // redraw new position
			ensureAnnotationModelInstalled();
		}
		if (showProposals)
			triggerContentAssist();
		if (fFramePosition != fExitPosition && fDoContextInfo)
			triggerContextInfo();

		if (fFramePosition != null && fCurrentTarget != null)
			fPositionListener.linkingFocusGained(fFramePosition, fCurrentTarget);

	}

	private void ensureAnnotationModelInstalled() {
		LinkedPositionAnnotations lpa= fCurrentTarget.fAnnotationModel;
		if (lpa != null) {
			ITextViewer viewer= fCurrentTarget.getViewer();
			if (viewer instanceof ISourceViewer) {
				ISourceViewer sv= (ISourceViewer) viewer;
				IAnnotationModel model= sv.getAnnotationModel();
				if (model instanceof IAnnotationModelExtension) {
					IAnnotationModelExtension ext= (IAnnotationModelExtension) model;
					IAnnotationModel ourModel= ext.getAnnotationModel(getUniqueKey());
					if (ourModel == null) {
						ext.addAnnotationModel(getUniqueKey(), lpa);
					}
				}
			}
		}
	}

	private void uninstallAnnotationModel(LinkedModeUITarget target) {
		ITextViewer viewer= target.getViewer();
		if (viewer instanceof ISourceViewer) {
			ISourceViewer sv= (ISourceViewer) viewer;
			IAnnotationModel model= sv.getAnnotationModel();
			if (model instanceof IAnnotationModelExtension) {
				IAnnotationModelExtension ext= (IAnnotationModelExtension) model;
				ext.removeAnnotationModel(getUniqueKey());
			}
		}
	}

	private void switchViewer(IDocument oldDoc, IDocument newDoc, LinkedPosition pos) {
		if (oldDoc != newDoc) {

			// redraw current document with new position before switching viewer
			if (fCurrentTarget.fAnnotationModel != null)
				fCurrentTarget.fAnnotationModel.switchToPosition(fModel, pos);

			LinkedModeUITarget target= null;
			for (int i= 0; i < fTargets.length; i++) {
				if (fTargets[i].getViewer().getDocument() == newDoc) {
					target= fTargets[i];
					break;
				}
			}
			if (target != fCurrentTarget) {
				disconnect();
				fCurrentTarget= target;
				target.linkingFocusLost(fFramePosition, target);
				connect();
				ensureAnnotationModelInstalled();
				if (fCurrentTarget != null)
					fCurrentTarget.linkingFocusGained(pos, fCurrentTarget);
			}
		}
	}

	private void select() {
		ITextViewer viewer= fCurrentTarget.getViewer();
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension5= (ITextViewerExtension5) viewer;
			extension5.exposeModelRange(new Region(fFramePosition.offset, fFramePosition.length));
		} else if (!viewer.overlapsWithVisibleRegion(fFramePosition.offset, fFramePosition.length)) {
			viewer.resetVisibleRegion();
		}
		viewer.revealRange(fFramePosition.offset, fFramePosition.length);
		viewer.setSelectedRange(fFramePosition.offset, fFramePosition.length);
	}

	private void redraw() {
		if (fCurrentTarget.fAnnotationModel != null)
			fCurrentTarget.fAnnotationModel.switchToPosition(fModel, fFramePosition);
	}

	private void connect() {
		Assert.isNotNull(fCurrentTarget);
		ITextViewer viewer= fCurrentTarget.getViewer();
		Assert.isNotNull(viewer);
		fCurrentTarget.fWidget= viewer.getTextWidget();
		if (fCurrentTarget.fWidget == null)
			leave(ILinkedModeListener.EXIT_ALL);

		if (fCurrentTarget.fKeyListener == null) {
			fCurrentTarget.fKeyListener= new KeyListener();
			((ITextViewerExtension) viewer).prependVerifyKeyListener(fCurrentTarget.fKeyListener);
		} else
			fCurrentTarget.fKeyListener.setEnabled(true);

		registerAutoEditVetoer(viewer);

		((IPostSelectionProvider) viewer).addPostSelectionChangedListener(fSelectionListener);

		createAnnotationModel();

		showSelection();

		fCurrentTarget.fShell= fCurrentTarget.fWidget.getShell();
		if (fCurrentTarget.fShell == null)
			leave(ILinkedModeListener.EXIT_ALL);
		fCurrentTarget.fShell.addShellListener(fCloser);

		fAssistant.install(viewer);

		viewer.addTextInputListener(fCloser);

		viewer.getDocument().addDocumentListener(fDocumentListener);
	}

	/**
	 * Reveals the selection on the current target's widget, if it is valid.
	 */
	private void showSelection() {
		final StyledText widget= fCurrentTarget.fWidget;
		if (widget == null || widget.isDisposed())
			return;

		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=132263
		widget.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!widget.isDisposed())
					try {
					widget.showSelection();
					} catch (IllegalArgumentException e) {
						/*
						 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66914
						 * if the StyledText is in setRedraw(false) mode, its
						 * selection may not be up2date and calling showSelection
						 * will throw an IAE.
						 * We don't have means to find out whether the selection is valid
						 * or whether the widget is redrawing or not therefore we try
						 * and ignore an IAE.
						 */
					}
			}
		});
	}

	/**
	 * Registers our auto edit vetoer with the viewer.
	 *
	 * @param viewer the viewer we want to veto ui-triggered changes within
	 *        linked positions
	 */
	private void registerAutoEditVetoer(ITextViewer viewer) {
		try {
			String[] contentTypes= getContentTypes(viewer.getDocument());
			if (viewer instanceof ITextViewerExtension2) {
				ITextViewerExtension2 vExtension= ((ITextViewerExtension2) viewer);
				for (int i= 0; i < contentTypes.length; i++) {
					vExtension.prependAutoEditStrategy(fAutoEditVetoer, contentTypes[i]);
				}
			} else {
				Assert.isTrue(false);
			}

		} catch (BadPartitioningException e) {
			leave(ILinkedModeListener.EXIT_ALL);
		}
	}

	private void unregisterAutoEditVetoer(ITextViewer viewer) {
		try {
			String[] contentTypes= getContentTypes(viewer.getDocument());
			if (viewer instanceof ITextViewerExtension2) {
				ITextViewerExtension2 vExtension= ((ITextViewerExtension2) viewer);
				for (int i= 0; i < contentTypes.length; i++) {
					vExtension.removeAutoEditStrategy(fAutoEditVetoer, contentTypes[i]);
				}
			} else {
				Assert.isTrue(false);
			}
		} catch (BadPartitioningException e) {
			leave(ILinkedModeListener.EXIT_ALL);
		}
	}

	/**
	 * Returns all possible content types of <code>document</code>.
	 *
	 * @param document the document
	 * @return all possible content types of <code>document</code>
	 * @throws BadPartitioningException if partitioning is invalid for this document
	 * @since 3.1
	 */
	private String[] getContentTypes(IDocument document) throws BadPartitioningException {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 ext= (IDocumentExtension3) document;
			String[] partitionings= ext.getPartitionings();
			Set contentTypes= new HashSet(20);
			for (int i= 0; i < partitionings.length; i++) {
				contentTypes.addAll(Arrays.asList(ext.getLegalContentTypes(partitionings[i])));
			}
			contentTypes.add(IDocument.DEFAULT_CONTENT_TYPE);
			return (String[]) contentTypes.toArray(new String[contentTypes.size()]);
		}
		return document.getLegalContentTypes();
	}

	private void createAnnotationModel() {
		if (fCurrentTarget.fAnnotationModel == null) {
			LinkedPositionAnnotations lpa= new LinkedPositionAnnotations();
			if (fSimple) {
				lpa.markExitTarget(true);
				lpa.markFocus(false);
				lpa.markSlaves(false);
				lpa.markTargets(false);
			}
			lpa.setTargets(fIterator.getPositions());
			lpa.setExitTarget(fExitPosition);
			lpa.connect(fCurrentTarget.getViewer().getDocument());
			fCurrentTarget.fAnnotationModel= lpa;
		}
	}

	private String getUniqueKey() {
		return "linked.annotationmodelkey."+toString(); //$NON-NLS-1$
	}

	private void disconnect() {
		Assert.isNotNull(fCurrentTarget);
		ITextViewer viewer= fCurrentTarget.getViewer();
		Assert.isNotNull(viewer);

		viewer.getDocument().removeDocumentListener(fDocumentListener);

		fAssistant.uninstall();
		fAssistant.removeProposalListener(fProposalListener);

		fCurrentTarget.fWidget= null;

		Shell shell= fCurrentTarget.fShell;
		fCurrentTarget.fShell= null;

		if (shell != null && !shell.isDisposed())
			shell.removeShellListener(fCloser);

		// this one is asymmetric: we don't install the model in
		// connect, but leave it to its callers to ensure they
		// have the model installed if they need it
		uninstallAnnotationModel(fCurrentTarget);

		unregisterAutoEditVetoer(viewer);

		// don't remove the verify key listener to let it keep its position
		// in the listener queue
		if (fCurrentTarget.fKeyListener != null)
			fCurrentTarget.fKeyListener.setEnabled(false);

		((IPostSelectionProvider) viewer).removePostSelectionChangedListener(fSelectionListener);

		redraw();
	}

	void leave(final int flags) {
		if (!fIsActive)
			return;
		fIsActive= false;

		endCompoundChangeIfNeeded();

		Display display= null;
		if (fCurrentTarget.fWidget != null && !fCurrentTarget.fWidget.isDisposed())
			display= fCurrentTarget.fWidget.getDisplay();

		if (fCurrentTarget.fAnnotationModel != null)
			fCurrentTarget.fAnnotationModel.removeAllAnnotations();
		disconnect();

		for (int i= 0; i < fTargets.length; i++) {
			LinkedModeUITarget target= fTargets[i];
			ITextViewer viewer= target.getViewer();
			if (target.fKeyListener != null) {
				((ITextViewerExtension) viewer).removeVerifyKeyListener(target.fKeyListener);
				target.fKeyListener= null;
			}

			viewer.removeTextInputListener(fCloser);
		}

		for (int i= 0; i < fTargets.length; i++) {

			if (fTargets[i].fAnnotationModel != null) {
				fTargets[i].fAnnotationModel.removeAllAnnotations();
				fTargets[i].fAnnotationModel.disconnect(fTargets[i].getViewer().getDocument());
				fTargets[i].fAnnotationModel= null;
			}

			uninstallAnnotationModel(fTargets[i]);
		}


		if ((flags & ILinkedModeListener.UPDATE_CARET) != 0 && fExitPosition != null && fFramePosition != fExitPosition && !fExitPosition.isDeleted())
			switchPosition(fExitPosition, true, false);

		final List docs= new ArrayList();
		for (int i= 0; i < fTargets.length; i++) {
			IDocument doc= fTargets[i].getViewer().getDocument();
			if (doc != null)
				docs.add(doc);
		}

		fModel.stopForwarding(flags);

		Runnable runnable= new Runnable() {
			public void run() {
				if (fExitPosition != null)
					fExitPosition.getDocument().removePosition(fExitPosition);

				for (Iterator iter = docs.iterator(); iter.hasNext(); ) {
					IDocument doc= (IDocument) iter.next();
					doc.removePositionUpdater(fPositionUpdater);
					boolean uninstallCat= false;
					String[] cats= doc.getPositionCategories();
					for (int j= 0; j < cats.length; j++) {
						if (getCategory().equals(cats[j])) {
							uninstallCat= true;
							break;
						}
					}
					if (uninstallCat)
						try {
							doc.removePositionCategory(getCategory());
						} catch (BadPositionCategoryException e) {
							// ignore
						}
				}
				fModel.exit(flags);
			}
		};

		// remove positions (both exit positions AND linked positions in the
		// model) asynchronously to make sure that the annotation painter
		// gets correct document offsets.
		if (display != null)
			display.asyncExec(runnable);
		else
			runnable.run();
	}

	private void endCompoundChangeIfNeeded() {
		if (fHasOpenCompoundChange) {
			ITextViewerExtension extension= (ITextViewerExtension) fCurrentTarget.getViewer();
			IRewriteTarget target= extension.getRewriteTarget();
			target.endCompoundChange();
			fHasOpenCompoundChange= false;
		}
	}

	private void beginCompoundChangeIfNeeded() {
		if (!fHasOpenCompoundChange) {
			ITextViewerExtension extension= (ITextViewerExtension) fCurrentTarget.getViewer();
			IRewriteTarget target= extension.getRewriteTarget();
			target.beginCompoundChange();
			fHasOpenCompoundChange= true;
		}
	}

	/**
	 * Returns the currently selected region or <code>null</code>.
	 *
	 * @return the currently selected region or <code>null</code>
	 */
	public IRegion getSelectedRegion() {
		if (fFramePosition != null)
			return new Region(fFramePosition.getOffset(), fFramePosition.getLength());
		if (fExitPosition != null)
			return new Region(fExitPosition.getOffset(), fExitPosition.getLength());
		return null;
	}

	private String getCategory() {
		return toString();
	}

	/**
	 * Sets the context info property. If set to <code>true</code>, context
	 * info will be invoked on the current target's viewer whenever a position
	 * is switched.
	 *
	 * @param doContextInfo <code>true</code> if context information should be
	 *        displayed
	 */
	public void setDoContextInfo(boolean doContextInfo) {
		fDoContextInfo= doContextInfo;
	}

	/**
	 * Sets the focus callback which will get informed when the focus of the
	 * linked mode UI changes.
	 * <p>
	 * If there is a listener installed already, it will be replaced.
	 * </p>
	 *
	 * @param listener the new listener, never <code>null</code>.
	 */
	protected void setPositionListener(ILinkedModeUIFocusListener listener) {
		Assert.isNotNull(listener);
		fPositionListener= listener;
	}

	/**
	 * Sets the "simple" mode of the receiver. A linked mode UI in simple mode
	 * merely draws the exit position, but not the target, focus, and slave
	 * positions. Default is <code>false</code>. This method must be called
	 * before it is entered.
	 *
	 * @param simple <code>true</code> if the UI should be in simple mode.
	 */
	public void setSimpleMode(boolean simple) {
		fSimple= simple;
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
		fAssistant.enableColoredLabels(isEnabled);
	}

}
