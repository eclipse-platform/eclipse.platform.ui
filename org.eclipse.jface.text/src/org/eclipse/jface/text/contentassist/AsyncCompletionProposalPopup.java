/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - [251156] async content assist
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import static org.eclipse.jface.util.Util.isValid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextUtilities;

/**
 * This class is used to present proposals asynchronously to the user. If additional information
 * exists for a proposal, then selecting that proposal will result in the information being
 * displayed in a secondary window.
 *
 * @since 3.12
 */
class AsyncCompletionProposalPopup extends CompletionProposalPopup {

	private static final int MAX_WAIT_IN_MS= 50; // TODO make it a preference

	/**
	 * This is only used and set when populating the dialog is async (ie computation takes more than
	 * MAX_WAIT_IN_MS
	 */
	private CompletableFuture<?> fAggregatedPopulateFuture;

	private Collection<CompletableFuture<?>> toCancelFutures= new LinkedList<>();

	private PopupVisibleTimer fPopupVisibleTimer= new PopupVisibleTimer();

	private static final class ComputingProposal implements ICompletionProposal, ICompletionProposalExtension {

		private final int fOffset;
		private final int fSize;
		private int fRemaining;

		ComputingProposal(int offset, int size) {
			fSize= size;
			fRemaining = size;
			fOffset = offset;
		}

		@Override
		public void apply(IDocument document) {
			// Nothing to do, maybe show some progress report?
		}

		@Override
		public Point getSelection(IDocument document) {
			return new Point(fOffset, 0);
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getDisplayString() {
			return NLS.bind(JFaceTextMessages.getString("AsyncCompletionProposalPopup.computing"), Long.valueOf(Math.round(100. * (fSize - fRemaining)/fSize))); //$NON-NLS-1$
		}

		@Override
		public String getAdditionalProposalInfo() {
			 return NLS.bind(JFaceTextMessages.getString("AsyncCompletionProposalPopup.computingDetails"), new Object[] { //$NON-NLS-1$;
				Integer.valueOf(fSize),
				Integer.valueOf(fSize - fRemaining),
				Integer.valueOf(fRemaining) });
		}

		@Override
		public void apply(IDocument document, char trigger, int offset) {
			// Nothing to do
		}

		@Override
		public boolean isValidFor(IDocument document, int offset) {
			return false;
		}

		@Override
		public char[] getTriggerCharacters() {
			return null;
		}

		@Override
		public int getContextInformationPosition() {
			return -1;
		}

		void setRemaining(int size) {
			this.fRemaining = size;
		}
	}

	public AsyncCompletionProposalPopup(ContentAssistant contentAssistant, IContentAssistSubjectControl contentAssistSubjectControl, AdditionalInfoController infoController) {
		super(contentAssistant, contentAssistSubjectControl, infoController);
	}

	public AsyncCompletionProposalPopup(ContentAssistant contentAssistant, ITextViewer viewer, AdditionalInfoController infoController) {
		super(contentAssistant, viewer, infoController);
	}

	/**
	 * This methods differs from its super as it will show the list of proposals that
	 * gets augmented as the {@link IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)}
	 * complete. All computations operation happen in a non-UI Thread so they're not blocking UI.
	 */
	@Override
	public String showProposals(boolean autoActivated) {
		if (fKeyListener == null)
			fKeyListener= new ProposalSelectionListener();

		final Control control= fContentAssistSubjectControlAdapter.getControl();

		if (!isValid(fProposalShell) && control != null && !control.isDisposed()) {
			// add the listener before computing the proposals so we don't move the caret
			// when the user types fast.
			fContentAssistSubjectControlAdapter.addKeyListener(fKeyListener);

			fInvocationOffset= fContentAssistSubjectControlAdapter.getSelectedRange().x;
			fFilterOffset= fInvocationOffset;
			fLastCompletionOffset= fFilterOffset;
			// start invocation of processors as Futures, and make them populate the proposals upon completion
			computeAndPopulateProposals(fInvocationOffset, null, true, autoActivated, true);
		} else {
			fLastCompletionOffset= fFilterOffset;
			handleRepeatedInvocation();
		}

		return getErrorMessage();
	}

	@Override
	void handleRepeatedInvocation() {
		cancelFutures();
		computeAndPopulateProposals(fInvocationOffset, null, false, false, false);
	}

	private void computeAndPopulateProposals(int offset, Consumer<List<ICompletionProposal>> callback, boolean createSelector, boolean autoActivated, boolean autoInsert) {
		List<CompletableFuture<List<ICompletionProposal>>> computationFutures= buildCompletionFuturesOrJobs(offset);
		toCancelFutures.addAll(computationFutures);
		fComputedProposals= Collections.synchronizedList(new ArrayList<>());
		List<CompletableFuture<Void>> populateFutures= computationFutures.stream().map(future -> future.thenAccept(fComputedProposals::addAll)).collect(Collectors.toList());
		toCancelFutures.addAll(populateFutures);
		CompletableFuture<?> aggregatedPopulateFuture= CompletableFuture.allOf(populateFutures.toArray(new CompletableFuture[populateFutures.size()]));
		toCancelFutures.add(aggregatedPopulateFuture);

		boolean useAsyncMode= false;
		try {
			aggregatedPopulateFuture.get(MAX_WAIT_IN_MS, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			useAsyncMode= true;
		} catch (ExecutionException | InterruptedException ex) {
			// nothing to do
		}
		if (!useAsyncMode) {
			int count= fComputedProposals.size();
			if (count == 0 && hideWhenNoProposals(autoActivated)) {
				return;
			}

			if (autoInsert && count == 1 && !autoActivated &&
					fContentAssistant.isAutoActivateCompletionOnType() && canAutoInsert(fComputedProposals.get(0))) {
				insertProposal(fComputedProposals.get(0), (char) 0, 0, offset);
				hide();
			} else {
				if (createSelector) {
					createProposalSelector();
				}
				if (callback != null) {
					callback.accept(fComputedProposals);
				} else {
					setProposals(fComputedProposals, false);
					displayProposals();
				}
			}
		} else {
			if (createSelector) {
				createProposalSelector();
			}
			ComputingProposal computingProposal= new ComputingProposal(offset, populateFutures.size());
			fComputedProposals.add(0, computingProposal);
			setProposals(fComputedProposals, false);
			AtomicInteger remaining= new AtomicInteger(populateFutures.size());
			final List<ICompletionProposal> requestSpecificProposals= fComputedProposals; //fComputedProposals can be changed/reset later
			populateFutures= populateFutures.stream().map(future -> future.thenRun(() -> {
				computingProposal.setRemaining(remaining.decrementAndGet());
				if (remaining.get() == 0) {
					requestSpecificProposals.remove(computingProposal);
				}
				Control control= fContentAssistSubjectControlAdapter.getControl();
				if (!control.isDisposed() && offset == fInvocationOffset) {
					control.getDisplay().asyncExec(() -> {
						// Skip if offset has changed while runnable was scheduled
						// nor when completion "session" was modified or canceled.
						if (offset != fInvocationOffset || fComputedProposals != requestSpecificProposals) {
							return;
						}
						if (autoInsert
								&& !autoActivated
								&& !fComputedProposals.contains(computingProposal)
								&& fComputedProposals.size() == 1
								&& remaining.get() == 0
								&& canAutoInsert(fComputedProposals.get(0))) {
							if (isValid(fProposalShell)) {
								insertProposal(fComputedProposals.get(0), (char) 0, 0, offset);
								hide();
							}
							return;
						}
						if (!fComputedProposals.contains(computingProposal) && callback != null) {
							callback.accept(fComputedProposals);
						} else {
							boolean stillComputing= fComputedProposals.contains(computingProposal);
							boolean hasProposals= (stillComputing && fComputedProposals.size() > 1)
									|| (!stillComputing && !fComputedProposals.isEmpty());

							if ((autoActivated && hasProposals) || !autoActivated) {
								setProposals(fComputedProposals, false);
								displayProposals(true);
							} else if (isValid(fProposalShell) && !fProposalShell.isVisible() && remaining.get() == 0) {
								hide(); // we only tear down if the popup is not visible.
							}
						}
					});
				}
			})).collect(Collectors.toList());
			toCancelFutures.addAll(populateFutures);
			fAggregatedPopulateFuture= CompletableFuture.allOf(populateFutures.toArray(new CompletableFuture[populateFutures.size()]));
			toCancelFutures.add(fAggregatedPopulateFuture);
		}
		displayProposals(!autoActivated);
	}

	@Override
	void displayProposals(boolean showPopup) {
		if (showPopup) {
			fPopupVisibleTimer.stop();
		}

		super.displayProposals(showPopup);
		if (!showPopup) {
			fPopupVisibleTimer.start();
		}
	}

	@Override
	public String incrementalComplete() {
		cancelFutures();
		if (isValid(fProposalShell) && fFilteredProposals != null) {
			return super.incrementalComplete();
		}
		final Control control= fContentAssistSubjectControlAdapter.getControl();

		if (fKeyListener == null)
			fKeyListener= new ProposalSelectionListener();

		if (!isValid(fProposalShell) && !control.isDisposed())
			fContentAssistSubjectControlAdapter.addKeyListener(fKeyListener);

		fInvocationOffset= fContentAssistSubjectControlAdapter.getSelectedRange().x;
		fFilterOffset= fInvocationOffset;
		fLastCompletionOffset= fFilterOffset;

		computeAndPopulateProposals(fInvocationOffset, (List<ICompletionProposal> proposals) -> {
			ensureDocumentListenerInstalled();
			fFilteredProposals= proposals;
			if (!proposals.isEmpty() && completeCommonPrefix()) {
				hide();
			} else {
				setProposals(proposals, false);
				displayProposals();
			}
		}, true, false, true);
		return getErrorMessage();
	}

	@Override
	List<ICompletionProposal> computeProposals(int offset) {
		if (fProposalShell != null) {
			fProposalShell.dispose();
		}
		showProposals(true);
		return fComputedProposals;
	}

	@Override
	void createProposalSelector() {
		super.createProposalSelector();
		fProposalShell.addDisposeListener(e -> cancelFutures());
	}

	void cancelFutures() {
		toCancelFutures.forEach(future -> future.cancel(true));
		toCancelFutures.clear();
	}

	@Override
	protected List<ICompletionProposal> computeFilteredProposals(int offset, DocumentEvent event) {
		if (fAggregatedPopulateFuture != null && !fAggregatedPopulateFuture.isDone()) {
			// user typed a char & computation still pending -> let all futures complete then invoke "filterProposals" upon completion
			fAggregatedPopulateFuture.thenRun(this::filterProposals);
			return fComputedProposals;
		}
		return super.computeFilteredProposals(offset, event);
	}

	@Override
	public void hide() {
		fPopupVisibleTimer.stop();
		super.hide();
		cancelFutures();
	}

	protected List<CompletableFuture<List<ICompletionProposal>>> buildCompletionFuturesOrJobs(int invocationOffset) {
		Set<IContentAssistProcessor> processors = null;
		try {
			processors= fContentAssistant.getContentAssistProcessors(getTokenContentType(invocationOffset));
		} catch (BadLocationException e) {
			// ignore
		}
		if (processors == null) {
			return Collections.emptyList();
		}
		List<CompletableFuture<List<ICompletionProposal>>> futures = new ArrayList<>(processors.size());
		for (IContentAssistProcessor processor : processors) {
			futures.add(CompletableFuture.supplyAsync(() -> {
				AtomicReference<List<ICompletionProposal>> result= new AtomicReference<>();
				SafeRunner.run(() -> {
					ICompletionProposal[] proposals= processor.computeCompletionProposals(fViewer, invocationOffset);
					if (proposals == null) {
						result.set(Collections.emptyList());
					} else {
						result.set(Arrays.asList(proposals));
					}
				});
				List<ICompletionProposal> proposals= result.get();
				if (proposals == null) { // an error occurred during computeCompletionProposal,
					// possible improvement: give user feedback by returning an error "proposal" shown
					// in completion popup and providing details
					return Collections.emptyList();
				}
				return proposals;
			}));
		}
		return futures;
	}

	private String getTokenContentType(int invocationOffset) throws BadLocationException {
		if (fContentAssistSubjectControl != null) {
			IDocument document= fContentAssistSubjectControl.getDocument();
			if (document != null) {
				return TextUtilities.getContentType(document, fContentAssistant.getDocumentPartitioning(), invocationOffset, true);
			}
		} else {
			return TextUtilities.getContentType(fViewer.getDocument(), fContentAssistant.getDocumentPartitioning(), invocationOffset, true);
		}
		return IDocument.DEFAULT_CONTENT_TYPE;
	}

	private class PopupVisibleTimer implements Runnable {
		private Thread fThread;

		private Object fMutex= new Object();

		private int fAutoActivationDelay= 500;

		protected void start() {
			fThread= new Thread(this, JFaceTextMessages.getString("ContentAssistant.assist_delay_timer_name")); //$NON-NLS-1$
			fThread.start();
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (fMutex) {
						if (fAutoActivationDelay != 0)
							fMutex.wait(fAutoActivationDelay);
					}
					Optional<Display> display= Optional.ofNullable(fContentAssistSubjectControlAdapter.getControl()).map(Control::getDisplay);
					display.ifPresent(d -> d.asyncExec(() -> displayProposals(true)));
					break;
				}
			} catch (InterruptedException e) {
			}
			fThread= null;
		}

		protected void stop() {
			Thread threadToStop= fThread;
			if (threadToStop != null && threadToStop.isAlive())
				threadToStop.interrupt();
		}

	}
}
