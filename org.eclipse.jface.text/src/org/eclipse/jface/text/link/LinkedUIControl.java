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
package org.eclipse.jface.text.link;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.internal.text.link.contentassist.ContentAssistant2;
import org.eclipse.jface.internal.text.link.contentassist.IProposalListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * The UI for linked mode. Detects events that influence behaviour of the linked
 * position UI and acts upon them.
 * <p>
 * <code>LinkedUIControl</code> relies on all added
 * <code>LinkedUITarget</code> s to provide implementations of
 * <code>ITextViewer</code> that implement <code>ITextViewerExtension</code>,
 * and the documents being edited to implement <code>IDocumentExtension3</code>.
 * </p>
 * 
 * @since 3.0
 */
public class LinkedUIControl {
	
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
	 * the first and vice versa if its environment is not nested.
	 */
	public static final Object CYCLE_WHEN_NO_PARENT= new Object();

	/**
	 * Listener that gets notified when the linked ui switches its focus position.
	 */
	public static interface ILinkedFocusListener {
		/**
		 * Called when the UI for the linked mode leaves a linked position.
		 * 
		 * @param position the position being left
		 * @param target the target where <code>position</code> resides in
		 */
		void linkedFocusLost(LinkedPosition position, LinkedUITarget target);
		/**
		 * Called when the UI for the linked mode gives focus to a linked position.
		 * 
		 * @param position the position being entered
		 * @param target the target where <code>position</code> resides in
		 */
		void linkedFocusGained(LinkedPosition position, LinkedUITarget target);
	}
	
	/**
	 * Null object implementation of focus listener.
	 */
	private static final class EmtpyFocusListener implements ILinkedFocusListener {
		
		public void linkedFocusGained(LinkedPosition position, LinkedUITarget target) {
			// ignore
		}
		
		public void linkedFocusLost(LinkedPosition position, LinkedUITarget target) {
			// ignore
		}
	}
	
	/**
	 * A link target consists of a viewer and gets notified if the linked UI on
	 * it is being shown.
	 * 
	 * @since 3.0
	 */
	public static abstract class LinkedUITarget implements ILinkedFocusListener {
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
		LinkedUIKeyListener fKeyListener;
		
		/** The cached custom annotation model. */
		LinkedPositionAnnotations fAnnotationModel;
	}

	private static final class EmptyTarget extends LinkedUITarget {

		private ITextViewer fTextViewer;

		/**
		 * @param viewer the viewer
		 */
		public EmptyTarget(ITextViewer viewer) {
			Assert.isNotNull(viewer);
			fTextViewer= viewer;
		}
		
		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedUIControl.ILinkedUITarget#getViewer()
		 */
		public ITextViewer getViewer() {
			return fTextViewer;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void linkedFocusLost(LinkedPosition position, LinkedUITarget target) {
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void linkedFocusGained(LinkedPosition position, LinkedUITarget target) {
		}

	}

	/**
	 * Listens for state changes in the model.
	 */
	private final class ExitListener implements ILinkedListener {
		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment.ILinkedListener#left(org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment, int)
		 */
		public void left(LinkedEnvironment environment, int flags) {
			leave(ILinkedListener.EXIT_ALL | flags);
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment.ILinkedListener#suspend(org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment)
		 */
		public void suspend(LinkedEnvironment environment) {
			disconnect();
			redraw();
		}

		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment.ILinkedListener#resume(org.eclipse.jdt.internal.ui.text.link2.LinkedEnvironment)
		 */
		public void resume(LinkedEnvironment environment, int flags) {
			if ((flags & ILinkedListener.EXIT_ALL) != 0) {
				leave(flags);
			} else {
				connect();
				if ((flags & ILinkedListener.SELECT) != 0)
					select();
				ensureAnnotationModelInstalled();
				redraw();
			}
		}
	}

	/**
	 * Exit flags returned if a custom exit policy wants to exit linked mode.
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
	 * behaviour.
	 */
	public interface IExitPolicy {
		/**
		 * Checks whether the linked mode should be left after receiving the
		 * given <code>VerifyEvent</code> and selection. Note that the event
		 * carries widget coordinates as opposed to <code>offset</code> and 
		 * <code>length</code> which are document coordinates.
		 * 
		 * @param environment the linked environment
		 * @param event the verify event
		 * @param offset the offset of the current selection
		 * @param length the length of the current selection
		 * @return valid exit flags or <code>null</code> if no special action
		 *         should be taken
		 */
		ExitFlags doExit(LinkedEnvironment environment, VerifyEvent event, int offset, int length);
	}

	/**
	 * A NullObject implementation of <code>IExitPolicy</code>.
	 */
	private static class NullExitPolicy implements IExitPolicy {
		/*
		 * @see org.eclipse.jdt.internal.ui.text.link2.LinkedUIControl.IExitPolicy#doExit(org.eclipse.swt.events.VerifyEvent, int, int)
		 */
		public ExitFlags doExit(LinkedEnvironment environment, VerifyEvent event, int offset, int length) {
			return null;
		}
	}

	/**
	 * Listens for shell events and acts upon them.
	 */
	private class LinkedUICloser implements ShellListener {

		public void shellActivated(ShellEvent e) {
		}

		public void shellClosed(ShellEvent e) {
			leave(ILinkedListener.EXIT_ALL);
		}

		public void shellDeactivated(ShellEvent e) {
// 			T ODO reenable after debugging 
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
			Display display;

			if (fAssistant == null || fCurrentTarget == null || (text= fCurrentTarget.fWidget) == null 
					|| text.isDisposed() || (display= text.getDisplay()) == null || display.isDisposed()) {
				leave(ILinkedListener.EXIT_ALL);
			} else {
				// Post in UI thread since the assistant popup will only get the focus after we lose it.
				display.asyncExec(new Runnable() {
					public void run() {
						if (fIsActive && (fAssistant == null || !fAssistant.hasFocus()))  {
							leave(ILinkedListener.EXIT_ALL);
						}
					}
				});
			}
		}

		public void shellDeiconified(ShellEvent e) {
		}

		public void shellIconified(ShellEvent e) {
			leave(ILinkedListener.EXIT_ALL);
		}

	}

	/**
	 * Listens for key events, checks the exit policy for custom exit
	 * strategies but defaults to handling Tab, Enter, and Escape.
	 */
	private class LinkedUIKeyListener implements VerifyKeyListener {

		private boolean fIsEnabled= true;

		public void verifyKey(VerifyEvent event) {

			if (!event.doit || !fIsEnabled)
				return;

			Point selection= fCurrentTarget.getViewer().getSelectedRange();
			int offset= selection.x;
			int length= selection.y;

			// if the custom exit policy returns anything, use that
			ExitFlags exitFlags= fExitPolicy.doExit(fEnvironment, event, offset, length);
			if (exitFlags != null) {
				leave(exitFlags.flags);
				event.doit= exitFlags.doit;
				return;
			}

			// standard behaviour:
			// (Shift+)Tab:	jumps from position to position, depending on cycle mode
			// Enter:		accepts all entries and leaves all (possibly stacked) environments, the last sets the caret
			// Esc:			accepts all entries and leaves all (possibly stacked) environments, the caret stays
			// ? what do we do to leave one level of a cycling environment that is stacked?
			// -> This is only the case if the level was set up with forced cycling (CYCLE_ALWAYS), in which case
			// the caller is sure that one does not need by-level exiting.
			switch (event.character) {
				// [SHIFT-]TAB = hop between edit boxes
				case 0x09:
					if (!(fExitPosition != null && fExitPosition.includes(offset)) && !fEnvironment.anyPositionContains(offset)) {
						// outside any edit box -> leave (all? TODO should only leave the affected, level and forward to the next upper)
						leave(ILinkedListener.EXIT_ALL);
						break;
					} else {
						if (event.stateMask == SWT.SHIFT)
							previous();
						else
							next();
					}

					event.doit= false;
					break;

				// ENTER
				case 0x0A:
				// Ctrl+Enter on WinXP
				case 0x0D:
//					if ((fExitPosition != null && fExitPosition.includes(offset)) || !fEnvironment.anyPositionContains(offset)) {
					if (!fEnvironment.anyPositionContains(offset)) {
//					if ((fExitPosition == null || !fExitPosition.includes(offset)) && !fEnvironment.anyPositionContains(offset)) {
						// outside any edit box or on exit position -> leave (all? TODO should only leave the affected, level and forward to the next upper)
						leave(ILinkedListener.EXIT_ALL);
						break;
					} else {
						// normal case: exit entire stack and put caret to final position
						leave(ILinkedListener.EXIT_ALL | ILinkedListener.UPDATE_CARET);
						event.doit= false;
						break;
					}

				// ESC
				case 0x1B:
					// exit entire stack and leave caret
					leave(ILinkedListener.EXIT_ALL);
					event.doit= false;
					break;

				default:
					if (event.character != 0) {
						if (!controlUndoBehavior(offset, length)) {
							leave(ILinkedListener.EXIT_ALL);
							break;
						}
					}
			}
		}

		private boolean controlUndoBehavior(int offset, int length) {
			LinkedPosition position= fEnvironment.findPosition(new LinkedPosition(fCurrentTarget.getViewer().getDocument(), offset, length, LinkedPositionGroup.NO_STOP));
			if (position != null) {

				// if the last position is not the same and there is an open change: close it.
				if (!position.equals(fPreviousPosition))
					endCompoundChange();

				beginCompoundChange();
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
							LinkedPosition pos= fEnvironment.findPosition(find);
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
	private LinkedUITarget fCurrentTarget;
	/** The manager of the linked positions we provide a UI for. */
	private LinkedEnvironment fEnvironment;
	/** The set of viewers we manage. */
	private LinkedUITarget[] fTargets;
	/** The iterator over the tab stop positions. */
	private TabStopIterator fIterator;

	/* Our team of event listeners */
	/** The shell listener. */
	private LinkedUICloser fCloser= new LinkedUICloser();
	/** The linked listener. */
	private ILinkedListener fLinkedListener= new ExitListener();
	/** The selection listener. */
	private MySelectionListener fSelectionListener= new MySelectionListener();
	/** The content assist listener. */
	private ProposalListener fProposalListener= new ProposalListener();
	
	/** The last caret position, used by fCaretListener. */
	private final Position fCaretPosition= new Position(0, 0);
	/** The exit policy to control custom exit behaviour */
	private IExitPolicy fExitPolicy= new NullExitPolicy();
	/** The current frame position shown in the UI, or <code>null</code>. */
	private LinkedPosition fFramePosition;
	/** The last visisted position, used for undo / redo. */
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
	private ILinkedFocusListener fPositionListener= new EmtpyFocusListener();
	private IAutoEditStrategy fAutoEditVetoer= new IAutoEditStrategy() {
		
		/*
		 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
		 */
		public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
			// invalidate the change to ensure that the change is performed on the document only.
			if (fEnvironment.anyPositionContains(command.offset)) {
				command.doit= false;
				command.caretOffset= command.offset + command.length;
			}
			
		}
	};

	/**
	 * Creates a new UI on the given model (environment) and the set of
	 * viewers. The environment must provide a tab stop sequence with a
	 * non-empty list of tab stops.
	 * 
	 * @param environment the linked position model
	 * @param targets the non-empty list of targets upon which the linked ui
	 *        should act
	 */
	public LinkedUIControl(LinkedEnvironment environment, LinkedUITarget[] targets) {
		constructor(environment, targets);
	}

	/**
	 * Conveniance ctor for just one viewer.
	 * 
	 * @param environment the linked position model
	 * @param viewer the viewer upon which the linked ui
	 *        should act
	 */
	public LinkedUIControl(LinkedEnvironment environment, ITextViewer viewer) {
		constructor(environment, new LinkedUITarget[]{new EmptyTarget(viewer)});
	}

	/**
	 * Conveniance ctor for multiple viewers.
	 * 
	 * @param environment the linked position model
	 * @param viewers the non-empty list of viewers upon which the linked ui
	 *        should act
	 */
	public LinkedUIControl(LinkedEnvironment environment, ITextViewer[] viewers) {
		LinkedUITarget[] array= new LinkedUITarget[viewers.length];
		for (int i= 0; i < array.length; i++) {
			array[i]= new EmptyTarget(viewers[i]);
		}
		constructor(environment, array);
	}

	/**
	 * Conveniance ctor for one target.
	 * 
	 * @param environment the linked position model
	 * @param target the target upon which the linked ui
	 *        should act
	 */
	public LinkedUIControl(LinkedEnvironment environment, LinkedUITarget target) {
		constructor(environment, new LinkedUITarget[]{target});
	}

	/**
	 * This does the actual constructor work.
	 * 
	 * @param environment the linked position model
	 * @param targets the non-empty array of targets upon which the linked ui
	 *        should act
	 */
	private void constructor(LinkedEnvironment environment, LinkedUITarget[] targets) {
		Assert.isNotNull(environment);
		Assert.isNotNull(targets);
		Assert.isTrue(targets.length > 0);
		Assert.isTrue(environment.getTabStopSequence().size() > 0);

		fEnvironment= environment;
		fTargets= targets;
		fCurrentTarget= targets[0];
		fIterator= new TabStopIterator(fEnvironment.getTabStopSequence());
		fIterator.setCycling(!fEnvironment.isNested());
		fEnvironment.addLinkedListener(fLinkedListener);

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
	 * Sets an <code>IExitPolicy</code> to customize the exit behaviour of
	 * this linked UI.
	 * 
	 * @param policy the exit policy to use.
	 */
	public void setExitPolicy(IExitPolicy policy) {
		fExitPolicy= policy;
	}

	/**
	 * Sets the exit position to move the caret to when linked mode is exited.
	 * 
	 * @param target the target where the exit position is located
	 * @param offset the offset of the exit position
	 * @param length the length of the exit position (in case there should be a
	 *        selection)
	 * @param sequence set to the tab stop position of the exit position, or 
	 * 		  <code>LinkedPositionGroup.NO_STOP</code> if there should be no tab stop.
	 * @throws BadLocationException if the position is not valid in the
	 *         viewer's document
	 */
	public void setExitPosition(LinkedUITarget target, int offset, int length, int sequence) throws BadLocationException {
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
		
		if (mode == CYCLE_ALWAYS || mode == CYCLE_WHEN_NO_PARENT && !fEnvironment.isNested())
			fIterator.setCycling(true);
		else
			fIterator.setCycling(false);
	}

	void next() {
		if (fIterator.hasNext(fFramePosition)) {
			switchPosition(fIterator.next(fFramePosition), true, true);
			return;
		} else
			leave(ILinkedListener.UPDATE_CARET);
	}

	void previous() {
		if (fIterator.hasPrevious(fFramePosition)) {
			switchPosition(fIterator.previous(fFramePosition), true, true);
		} else
			// dont't update caret, but rather select the current frame
			leave(ILinkedListener.SELECT);
	}
	
	private void triggerContextInfo() {
		ITextOperationTarget target= fCurrentTarget.getViewer().getTextOperationTarget();
		if (target != null) {
			if (target.canDoOperation(ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION))
				target.doOperation(ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
		}
	}

	/** Trigger content assist on choice positions */
	private void triggerContentAssist() {
		if (fFramePosition instanceof ProposalPosition) {
			ProposalPosition pp= (ProposalPosition) fFramePosition;
			fAssistant.setCompletions(pp.getChoices());
			fAssistant.showPossibleCompletions();
		} else {
			fAssistant.setCompletions(new ICompletionProposal[0]);
			fAssistant.hidePossibleCompletions();
		}
	}

	private void switchPosition(LinkedPosition pos, boolean select, boolean showProposals) {
		Assert.isNotNull(pos);
		if (pos.equals(fFramePosition))
			return;
		
		if (fFramePosition != null && fCurrentTarget != null)
			fPositionListener.linkedFocusLost(fFramePosition, fCurrentTarget);
	
		// undo
		endCompoundChange();
	
		redraw(); // redraw current position being left - usually not needed
		IDocument oldDoc= fFramePosition == null ? null : fFramePosition.getDocument();
		IDocument newDoc= pos.getDocument();
		
		switchViewer(oldDoc, newDoc, pos);
		fFramePosition= pos;

		if (select)
			select();
		if (fFramePosition == fExitPosition && !fIterator.isCycling())
			leave(ILinkedListener.NONE);
		else {
			redraw(); // redraw new position
			ensureAnnotationModelInstalled();
		}
		if (showProposals)
			triggerContentAssist();
		if (fFramePosition != fExitPosition && fDoContextInfo)
			triggerContextInfo();
		
		if (fFramePosition != null && fCurrentTarget != null)
			fPositionListener.linkedFocusGained(fFramePosition, fCurrentTarget);
		
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
	
	private void uninstallAnnotationModel(LinkedUITarget target) {
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
				fCurrentTarget.fAnnotationModel.switchToPosition(fEnvironment, pos);
			
			LinkedUITarget target= null;
			for (int i= 0; i < fTargets.length; i++) {
				if (fTargets[i].getViewer().getDocument() == newDoc) {
					target= fTargets[i];
					break;
				}
			}
			if (target != fCurrentTarget) {
				disconnect();
				fCurrentTarget= target;
				target.linkedFocusLost(fFramePosition, target);
				connect();
				ensureAnnotationModelInstalled();
				if (fCurrentTarget != null)
					fCurrentTarget.linkedFocusGained(pos, fCurrentTarget);
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
			fCurrentTarget.fAnnotationModel.switchToPosition(fEnvironment, fFramePosition);
	}

	private void connect() {
		Assert.isNotNull(fCurrentTarget);
		ITextViewer viewer= fCurrentTarget.getViewer();
		Assert.isNotNull(viewer);
		fCurrentTarget.fWidget= viewer.getTextWidget();
		if (fCurrentTarget.fWidget == null)
			leave(ILinkedListener.EXIT_ALL);

		if (fCurrentTarget.fKeyListener == null) {
			fCurrentTarget.fKeyListener= new LinkedUIKeyListener();
			((ITextViewerExtension) viewer).prependVerifyKeyListener(fCurrentTarget.fKeyListener);
		} else
			fCurrentTarget.fKeyListener.setEnabled(true);
		
		registerAutoEditVetoer(viewer);
		
		((IPostSelectionProvider) viewer).addPostSelectionChangedListener(fSelectionListener);

		createAnnotationModel();
		
		fCurrentTarget.fWidget.showSelection();

		fCurrentTarget.fShell= fCurrentTarget.fWidget.getShell();
		if (fCurrentTarget.fShell == null)
			leave(ILinkedListener.EXIT_ALL);
		fCurrentTarget.fShell.addShellListener(fCloser);

		fAssistant.install(viewer);
	}

	/**
	 * Registers our auto edit vetoer with the viewer.
	 * 
	 * @param viewer the viewer we want to veto ui-triggered changes within
	 *        linked positions
	 */
	private void registerAutoEditVetoer(ITextViewer viewer) {
		try {
			if (viewer.getDocument() instanceof IDocumentExtension3) {
				IDocumentExtension3 ext= (IDocumentExtension3) viewer.getDocument();
				String[] contentTypes= ext.getLegalContentTypes(IDocumentExtension3.DEFAULT_PARTITIONING);
				if (viewer instanceof ITextViewerExtension2) {
					ITextViewerExtension2 vExtension= ((ITextViewerExtension2) viewer);
					for (int i= 0; i < contentTypes.length; i++) {
						vExtension.prependAutoEditStrategy(fAutoEditVetoer, contentTypes[i]);
					}
				} else {
					Assert.isTrue(false);
				}
			}

		} catch (BadPartitioningException e) {
			leave(ILinkedListener.EXIT_ALL);
		}
	}

	private void unregisterAutoEditVetoer(ITextViewer viewer) {
		try {
			if (viewer.getDocument() instanceof IDocumentExtension3) {
				IDocumentExtension3 ext= (IDocumentExtension3) viewer.getDocument();
				String[] contentTypes= ext.getLegalContentTypes(IDocumentExtension3.DEFAULT_PARTITIONING);
				if (viewer instanceof ITextViewerExtension2) {
					ITextViewerExtension2 vExtension= ((ITextViewerExtension2) viewer);
					for (int i= 0; i < contentTypes.length; i++) {
						vExtension.removeAutoEditStrategy(fAutoEditVetoer, contentTypes[i]);
					}
				}
			}

		} catch (BadPartitioningException e) {
			leave(ILinkedListener.EXIT_ALL);
		}
	}

	private void createAnnotationModel() {
		if (fCurrentTarget.fAnnotationModel == null) {
			LinkedPositionAnnotations lpa= new LinkedPositionAnnotations();
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
		fCurrentTarget.fKeyListener.setEnabled(false);
		
		((IPostSelectionProvider) viewer).removePostSelectionChangedListener(fSelectionListener);

		redraw();
	}

	void leave(final int flags) {
		if (!fIsActive)
			return;
		fIsActive= false;

		endCompoundChange();
		
		Display display= null;
		if (fCurrentTarget.fWidget != null && !fCurrentTarget.fWidget.isDisposed())
			display= fCurrentTarget.fWidget.getDisplay();
		
//		// debug trace
//		JavaPlugin.log(new Status(IStatus.INFO, JavaPlugin.getPluginId(), IStatus.OK, "leaving linked mode", null));
		if (fCurrentTarget.fAnnotationModel != null)
			fCurrentTarget.fAnnotationModel.removeAllAnnotations();
		disconnect();
		
		for (int i= 0; i < fTargets.length; i++) {
			if (fCurrentTarget.fKeyListener != null) {
				((ITextViewerExtension) fTargets[i].getViewer()).removeVerifyKeyListener(fCurrentTarget.fKeyListener);
				fCurrentTarget.fKeyListener= null;
			}
		}
		
		for (int i= 0; i < fTargets.length; i++) {
			
			if (fTargets[i].fAnnotationModel != null) {
				fTargets[i].fAnnotationModel.removeAllAnnotations();
				fTargets[i].fAnnotationModel.disconnect(fCurrentTarget.getViewer().getDocument());
				fTargets[i].fAnnotationModel= null;
			}
			
			uninstallAnnotationModel(fTargets[i]);
		}

		
		if (fExitPosition != null)
			fExitPosition.getDocument().removePosition(fExitPosition);

		if ((flags & ILinkedListener.UPDATE_CARET) != 0 && fExitPosition != null && fFramePosition != fExitPosition && !fExitPosition.isDeleted())
			switchPosition(fExitPosition, true, false);

		final List docs= new ArrayList();
		for (int i= 0; i < fTargets.length; i++) {
			IDocument doc= fTargets[i].getViewer().getDocument();
			if (doc != null)
				docs.add(doc);
		}

		Runnable runnable= new Runnable() {
			public void run() {
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
				fEnvironment.exit(flags);
			}
		};

		// remove positions (both exit positions AND linked positions in the
		// environment) async to make sure that the annotation painter
		// gets correct document offsets.
		if (display != null)
			display.asyncExec(runnable);
		else
			runnable.run();
	}

	/**
	 * 
	 */
	private void endCompoundChange() {
		if (fHasOpenCompoundChange) {
			ITextViewerExtension extension= (ITextViewerExtension) fCurrentTarget.getViewer();
			IRewriteTarget target= extension.getRewriteTarget();
			target.endCompoundChange();
			fHasOpenCompoundChange= false;
		}
	}
	
	private void beginCompoundChange() {
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
		if (fFramePosition == null)
			if (fExitPosition != null)
				return new Region(fExitPosition.getOffset(), fExitPosition.getLength());
			else
				return null;
		else
			return new Region(fFramePosition.getOffset(), fFramePosition.getLength());
	}

	

	private String getCategory() {
		return toString();
	}

	/**
	 * Sets the context info property. If set to <code>true</code>, context
	 * info will be invoked on the current target's viewer whenever a position
	 * is switched.
	 * 
	 * @param doContextInfo The doContextInfo to set.
	 */
	public void setDoContextInfo(boolean doContextInfo) {
		fDoContextInfo= doContextInfo;
	}
	
	/**
	 * Sets the focus callback which will get informed when the focus of the 
	 * linked mode ui changes.
	 * <p>
	 * If there is a listener installed already, it will be replaced.</p> 
	 *  
	 * @param listener the new listener, never <code>null</code>.
	 */
	public void setPositionListener(ILinkedFocusListener listener) {
		Assert.isNotNull(listener);
		fPositionListener= listener;
	}

}
