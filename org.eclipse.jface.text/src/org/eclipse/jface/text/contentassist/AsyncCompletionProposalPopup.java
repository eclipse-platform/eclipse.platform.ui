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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.osgi.util.NLS;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.ISafeRunnable;
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
	private List<CompletableFuture<List<ICompletionProposal>>> fFutures;

	private static final class ComputingProposal implements ICompletionProposal, ICompletionProposalExtension {

		private final int fOffset;
		private final int fSize;
		private int fRemaining;

		public ComputingProposal(int offset, int size) {
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

		public void setRemaining(int size) {
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

		if (!Helper.okToUse(fProposalShell) && control != null && !control.isDisposed()) {
			// add the listener before computing the proposals so we don't move the caret
			// when the user types fast.
			fContentAssistSubjectControlAdapter.addKeyListener(fKeyListener);

			fInvocationOffset= fContentAssistSubjectControlAdapter.getSelectedRange().x;
			fFilterOffset= fInvocationOffset;
			fLastCompletionOffset= fFilterOffset;
			// start invocation of processors as Futures, and make them populate the proposals upon completion
			fFutures= buildCompletionFuturesOrJobs(fInvocationOffset);
			runFutures(fInvocationOffset, null, true, autoActivated, true);
		} else {
			fLastCompletionOffset= fFilterOffset;
			handleRepeatedInvocation();
		}

		return getErrorMessage();
	}

	@Override
	void handleRepeatedInvocation() {
		cancelFutures();
		fFutures= buildCompletionFuturesOrJobs(fInvocationOffset);
		runFutures(fInvocationOffset, null, false, false, false);
	}

	private List<ICompletionProposal> runFutures(int offset, Consumer<List<ICompletionProposal>> callback, boolean createSelector, boolean autoActivated, boolean autoInsert) {
		List<ICompletionProposal> computedProposals= Collections.synchronizedList(new ArrayList<>());
		List<CompletableFuture<Void>> populateFutures= new ArrayList<>(fFutures.size());
		for (CompletableFuture<List<ICompletionProposal>> future : fFutures) {
			populateFutures.add(future.thenAccept(proposals -> computedProposals.addAll(proposals)));
		}

		long requestBeginningTimestamp= System.currentTimeMillis();
		long stillRemainingThreeshold= MAX_WAIT_IN_MS;
		for (CompletableFuture<?> future : populateFutures) {
			try {
				future.get(stillRemainingThreeshold, TimeUnit.MILLISECONDS);
			} catch (TimeoutException | ExecutionException | InterruptedException ex) {
				// future failed or took more time than we want to wait
			}
			stillRemainingThreeshold= MAX_WAIT_IN_MS - (System.currentTimeMillis() - requestBeginningTimestamp);
			if (stillRemainingThreeshold < 0) {
				// we already spent too much time (more than MAX_WAIT_IN_MS), stop waiting.
				break;
			}
		}
		fComputedProposals= computedProposals;
		if (stillRemainingThreeshold > 0) { // everything ready in time, go synchronous
			int count= computedProposals.size();
			if (count == 0 && hideWhenNoProposals(autoActivated))
				return computedProposals;

			if (autoInsert && count == 1 && !autoActivated && canAutoInsert(computedProposals.get(0))) {
				insertProposal(computedProposals.get(0), (char) 0, 0, offset);
				hide();
			} else {
				if (createSelector) {
					createProposalSelector();
				}
				if (callback != null) {
					callback.accept(computedProposals);
				} else {
					setProposals(computedProposals, false);
					displayProposals();
				}
			}
		} else { // processors took too much time, go asynchronous
			if (createSelector) {
				createProposalSelector();
			}
			ComputingProposal computingProposal= new ComputingProposal(offset, fFutures.size());
			computedProposals.add(0, computingProposal);
			setProposals(fComputedProposals, false);
			Set<CompletableFuture<Void>> remaining= Collections.synchronizedSet(new HashSet<>(populateFutures));
			for (CompletableFuture<Void> populateFuture : populateFutures) {
				populateFuture.thenRun(() -> {
					remaining.removeIf(CompletableFuture::isDone);
					computingProposal.setRemaining(remaining.size());
					if (remaining.isEmpty()) {
						computedProposals.remove(computingProposal);
					}
					List<ICompletionProposal> newProposals= new ArrayList<>(computedProposals);
					fComputedProposals= newProposals;
					Display.getDefault().asyncExec(() -> {
						if (autoInsert && !autoActivated && remaining.isEmpty() && newProposals.size() == 1 && canAutoInsert(newProposals.get(0))) {
							if (Helper.okToUse(fProposalShell)) {
								insertProposal(newProposals.get(0), (char) 0, 0, offset);
								hide();
							}
							return;
						}
						if (remaining.isEmpty() && callback != null) {
							callback.accept(newProposals);
						} else {
							setProposals(newProposals, false);
							displayProposals();
						}
					});
				});
			}
			displayProposals();
		}
		return computedProposals;
	}

	@Override
	public String incrementalComplete() {
		cancelFutures();
		if (Helper.okToUse(fProposalShell) && fFilteredProposals != null) {
			return super.incrementalComplete();
		}
		final Control control= fContentAssistSubjectControlAdapter.getControl();

		if (fKeyListener == null)
			fKeyListener= new ProposalSelectionListener();

		if (!Helper.okToUse(fProposalShell) && !control.isDisposed())
			fContentAssistSubjectControlAdapter.addKeyListener(fKeyListener);

		fInvocationOffset= fContentAssistSubjectControlAdapter.getSelectedRange().x;
		fFilterOffset= fInvocationOffset;
		fLastCompletionOffset= fFilterOffset;

		fFutures= buildCompletionFuturesOrJobs(fInvocationOffset);
		fFilteredProposals= runFutures(fInvocationOffset, (List<ICompletionProposal> proposals) -> {
			ensureDocumentListenerInstalled();
			if (proposals.size() > 0 && completeCommonPrefix()) {
				hide();
			} else {
				fFilteredProposals= proposals;
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
		if (fFutures != null) {
			for (Future<?> future : fFutures) {
				future.cancel(true);
			}
			fFutures= null;
		}
	}

	@Override
	protected List<ICompletionProposal> computeFilteredProposals(int offset, DocumentEvent event) {
		if(fComputedProposals != null && fComputedProposals.size() > 0 && fComputedProposals.get(0) instanceof ComputingProposal) {
			Set<CompletableFuture<List<ICompletionProposal>>> remaining = Collections.synchronizedSet(new HashSet<>(fFutures));
			for (CompletableFuture<List<ICompletionProposal>> future : fFutures) {
				future.thenRun(() -> {
					remaining.removeIf(CompletableFuture::isDone);
					if (remaining.isEmpty()) {
						filterProposals();
					}
				});
			}
			return fComputedProposals;
		}
		return super.computeFilteredProposals(offset, event);
	}

	@Override
	public void hide() {
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
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						ICompletionProposal[] proposals= processor.computeCompletionProposals(fViewer, invocationOffset);
						if (proposals == null) {
							result.set(Collections.emptyList());
						} else {
							result.set(Arrays.asList(proposals));
						}
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

}
