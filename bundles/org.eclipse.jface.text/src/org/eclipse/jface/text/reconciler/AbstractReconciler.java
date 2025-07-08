/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.reconciler;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;


/**
 * Abstract implementation of {@link IReconciler}. The reconciler
 * listens to input document changes as well as changes of
 * the input document of the text viewer it is installed on. Depending on
 * its configuration it manages the received change notifications in a
 * queue folding neighboring or overlapping changes together. The reconciler
 * processes the dirty regions as a background activity after having waited for further
 * changes for the configured duration of time. A reconciler is started using the
 * {@link #install(ITextViewer)} method.  As a first step {@link #initialProcess()} is
 * executed in the background. Then, the reconciling thread waits for changes that
 * need to be reconciled. A reconciler can be resumed by calling {@link #forceReconciling()}
 * independent from the existence of actual changes. This mechanism is for subclasses only.
 * It is the clients responsibility to stop a reconciler using its {@link #uninstall()}
 * method. Unstopped reconcilers do not free their resources.
 * <p>
 * It is subclass responsibility to specify how dirty regions are processed.
 * </p>
 *
 * @see org.eclipse.jface.text.IDocumentListener
 * @see org.eclipse.jface.text.ITextInputListener
 * @see org.eclipse.jface.text.reconciler.DirtyRegion
 * @since 2.0
 */
abstract public class AbstractReconciler implements IReconciler {


	/**
	 * Background thread for the reconciling activity.
	 */
	class BackgroundWorker implements Runnable {

		/** Has the reconciler been canceled. */
		private boolean fCanceled;
		/** Has the reconciler been reset. */
		private boolean fReset;
		/** Some changes need to be processed. */
		private boolean fIsDirty;
		/** Is a reconciling strategy active. */
		private boolean fIsActive;

		private boolean fStarted;

		private String fName;

		private boolean fIsAlive;

		private volatile Thread fThread;

		public BackgroundWorker(String name) {
			fName= name;
		}

		/**
		 * Returns whether a reconciling strategy is active right now.
		 *
		 * @return <code>true</code> if a activity is active
		 */
		public boolean isActive() {
			return fIsActive;
		}

		/**
		 * Returns whether some changes need to be processed.
		 *
		 * @return <code>true</code> if changes wait to be processed
		 * @since 3.0
		 */
		public synchronized boolean isDirty() {
			return fIsDirty;
		}

		/**
		 * Cancels the background thread.
		 */
		public void cancel() {
			fCanceled= true;
			IProgressMonitor pm= fProgressMonitor;
			if (pm != null)
				pm.setCanceled(true);
			synchronized (fDirtyRegionQueue) {
				fDirtyRegionQueue.notifyAll();
			}
		}

		/**
		 * Suspends the caller of this method until this background thread has
		 * emptied the dirty region queue.
		 */
		public void suspendCallerWhileDirty() {
			AbstractReconciler.this.signalWaitForFinish();
			boolean isDirty;
			do {
				synchronized (fDirtyRegionQueue) {
					isDirty= fDirtyRegionQueue.getSize() > 0;
					if (isDirty) {
						try {
							fDirtyRegionQueue.wait();
						} catch (InterruptedException x) {
						}
					}
				}
			} while (isDirty);
		}

		/**
		 * Reset the background thread as the text viewer has been changed,
		 */
		public void reset() {

			if (fDelay > 0) {

				synchronized (this) {
					fIsDirty= true;
					fReset= true;
				}
				synchronized (fDirtyRegionQueue) {
					fDirtyRegionQueue.notifyAll(); // wake up wait(fDelay);
				}

			} else {

				synchronized (this) {
					fIsDirty= true;
				}

				synchronized (fDirtyRegionQueue) {
					fDirtyRegionQueue.notifyAll();
				}
			}

			informNotFinished();
			reconcilerReset();
		}

		/**
		 * The background activity. Waits until there is something in the
		 * queue managing the changes that have been applied to the text viewer.
		 * Removes the first change from the queue and process it.
		 */
		@Override
		public void run() {
			try {
				while (!fCanceled) {

					delay();

					if (fCanceled)
						break;

					if (!isDirty()) {
						waitFinish= false; //signalWaitForFinish() was called but nothing todo
						continue;
					}

					synchronized (this) {
						if (fReset) {
							fReset= false;
							continue;
						}
					}

					DirtyRegion r= null;
					synchronized (fDirtyRegionQueue) {
						r= fDirtyRegionQueue.removeNextDirtyRegion();
					}

					fIsActive= true;

					fProgressMonitor.setCanceled(false);

					process(r);

					synchronized (fDirtyRegionQueue) {
						if (fDirtyRegionQueue.isEmpty()) {
							synchronized (this) {
								fIsDirty= fProgressMonitor.isCanceled();
							}
							fDirtyRegionQueue.notifyAll();
						}
					}
					fIsActive= false;
				}
			} finally {
				fIsAlive= false;
			}
		}

		boolean isAlive() {
			return fIsAlive;
		}

		/**
		 * Star the reconciling if not running (and calls
		 * {@link AbstractReconciler#initialProcess()}) or {@link #reset()} otherwise.
		 */
		public void startReconciling() {
			if (!fStarted) {
				fIsAlive= true;
				fStarted= true;
				Job.createSystem("Delayed Reconciler startup for " + fName, m -> { //$NON-NLS-1$
					//Until we process some code from the job, the reconciler thread is the current thread
					fThread= Thread.currentThread();
					delay();
					if (fCanceled) {
						return Status.CANCEL_STATUS;
					}
					initialProcess();
					if (fCanceled) {
						return Status.CANCEL_STATUS;
					}
					Thread thread= new Thread(this);
					thread.setName(fName);
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.setDaemon(true);
					//we will no longer process any code here, so hand over to the worker thread.
					fThread= thread;
					thread.start();
					return Status.OK_STATUS;
				}).schedule();
			} else {
				reset();
			}

		}
	}

	/**
	 * Internal document listener and text input listener.
	 */
	class Listener implements IDocumentListener, ITextInputListener {

		@Override
		public void documentAboutToBeChanged(DocumentEvent e) {
		}

		@Override
		public void documentChanged(DocumentEvent e) {

			if (fWorker.isActive() || !fWorker.isDirty() && fWorker.isAlive()) {
				if (!fIsAllowedToModifyDocument && isRunningInReconcilerThread())
					throw new UnsupportedOperationException("The reconciler thread is not allowed to modify the document"); //$NON-NLS-1$
				aboutToBeReconciledInternal();
			}

			/*
			 * The second OR condition handles the case when the document
			 * gets changed while still inside initialProcess().
			 */
			if (fWorker.isActive() || fWorker.isDirty() && fWorker.isAlive())
				fProgressMonitor.setCanceled(true);

			if (fIsIncrementalReconciler)
				createDirtyRegion(e);

			fWorker.reset();

		}

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {

			if (oldInput == fDocument) {

				if (fDocument != null)
					fDocument.removeDocumentListener(this);

				if (fIsIncrementalReconciler) {
					synchronized (fDirtyRegionQueue) {
						fDirtyRegionQueue.purgeQueue();
					}
					if (fDocument != null && fDocument.getLength() > 0 && fWorker.isDirty() && fWorker.isAlive()) {
						DocumentEvent e= new DocumentEvent(fDocument, 0, fDocument.getLength(), ""); //$NON-NLS-1$
						createDirtyRegion(e);
						fWorker.reset();
						fWorker.suspendCallerWhileDirty();
					}
				}

				fDocument= null;
			}
		}

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {

			fDocument= newInput;
			if (fDocument == null)
				return;


			reconcilerDocumentChanged(fDocument);

			fDocument.addDocumentListener(this);

			if (!fWorker.isDirty())
				aboutToBeReconciledInternal();

			startReconciling();
		}
	}

	/** Queue to manage the changes applied to the text viewer. */
	private DirtyRegionQueue fDirtyRegionQueue;
	/** The background thread. */
	private BackgroundWorker fWorker;
	/** Internal document and text input listener. */
	private Listener fListener;
	/** The background thread delay. */
	private int fDelay= 500;
	/** Signal that the the background thread should not delay. */
	volatile boolean waitFinish;
	/** Are there incremental reconciling strategies? */
	private boolean fIsIncrementalReconciler= true;
	/** The progress monitor used by this reconciler. */
	private IProgressMonitor fProgressMonitor;
	/**
	 * Tells whether this reconciler is allowed to modify the document.
	 * @since 3.2
	 */
	private boolean fIsAllowedToModifyDocument= true;


	/** The text viewer's document. */
	private IDocument fDocument;
	/** The text viewer */
	private ITextViewer fViewer;


	/**
	 * Processes a dirty region. If the dirty region is <code>null</code> the whole
	 * document is consider being dirty. The dirty region is partitioned by the
	 * document and each partition is handed over to a reconciling strategy registered
	 * for the partition's content type.
	 *
	 * @param dirtyRegion the dirty region to be processed
	 */
	abstract protected void process(DirtyRegion dirtyRegion);

	/**
	 * Hook called when the document whose contents should be reconciled
	 * has been changed, i.e., the input document of the text viewer this
	 * reconciler is installed on. Usually, subclasses use this hook to
	 * inform all their reconciling strategies about the change.
	 *
	 * @param newDocument the new reconciler document
	 */
	abstract protected void reconcilerDocumentChanged(IDocument newDocument);


	/**
	 * Creates a new reconciler without configuring it.
	 */
	protected AbstractReconciler() {
		fProgressMonitor= new NullProgressMonitor();
	}

	/**
	 * Tells the reconciler how long it should wait for further text changes before
	 * activating the appropriate reconciling strategies.
	 *
	 * @param delay the duration in milliseconds of a change collection period.
	 */
	public void setDelay(int delay) {
		fDelay= delay;
	}

	/**
	 * Tells the reconciler whether any of the available reconciling strategies
	 * is interested in getting detailed dirty region information or just in the
	 * fact that the document has been changed. In the second case, the reconciling
	 * can not incrementally be pursued.
	 *
	 * @param isIncremental indicates whether this reconciler will be configured with
	 *		incremental reconciling strategies
	 *
	 * @see DirtyRegion
	 * @see IReconcilingStrategy
	 */
	public void setIsIncrementalReconciler(boolean isIncremental) {
		fIsIncrementalReconciler= isIncremental;
	}

	/**
	 * Tells the reconciler whether it is allowed to change the document
	 * inside its reconciler thread.
	 * <p>
	 * If this is set to <code>false</code> an {@link UnsupportedOperationException}
	 * will be thrown when this restriction will be violated.
	 * </p>
	 *
	 * @param isAllowedToModify indicates whether this reconciler is allowed to modify the document
	 * @since 3.2
	 */
	public void setIsAllowedToModifyDocument(boolean isAllowedToModify) {
		fIsAllowedToModifyDocument= isAllowedToModify;
	}

	/**
	 * Sets the progress monitor of this reconciler.
	 *
	 * @param monitor the monitor to be used
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		Assert.isLegal(monitor != null);
		fProgressMonitor= monitor;
	}

	/**
	 * Returns whether any of the reconciling strategies is interested in
	 * detailed dirty region information.
	 *
	 * @return whether this reconciler is incremental
	 *
	 * @see IReconcilingStrategy
	 */
	protected boolean isIncrementalReconciler() {
		return fIsIncrementalReconciler;
	}

	/**
	 * Returns the input document of the text viewer this reconciler is installed on.
	 *
	 * @return the reconciler document
	 */
	protected IDocument getDocument() {
		return fDocument;
	}

	/**
	 * Returns the text viewer this reconciler is installed on.
	 *
	 * @return the text viewer this reconciler is installed on
	 */
	protected ITextViewer getTextViewer() {
		return fViewer;
	}

	/**
	 * Returns the progress monitor of this reconciler.
	 *
	 * @return the progress monitor of this reconciler
	 */
	protected IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}

	@Override
	public void install(ITextViewer textViewer) {

		Assert.isNotNull(textViewer);
		fViewer= textViewer;

		synchronized (this) {
			if (fWorker != null)
				return;
			fWorker= new BackgroundWorker(getClass().getName());
		}

		fDirtyRegionQueue= new DirtyRegionQueue();

		fListener= new Listener();
		fViewer.addTextInputListener(fListener);

		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=67046
		// if the reconciler gets installed on a viewer that already has a document
		// (e.g. when reusing editors), we force the listener to register
		// itself as document listener, because there will be no input change
		// on the viewer.
		// In order to do that, we simulate an input change.
		IDocument document= textViewer.getDocument();
		if (document != null) {
			fListener.inputDocumentAboutToBeChanged(fDocument, document);
			fListener.inputDocumentChanged(fDocument, document);
		}
	}

	@Override
	public void uninstall() {
		if (fListener != null) {

			fViewer.removeTextInputListener(fListener);
			if (fDocument != null) {
				fListener.inputDocumentAboutToBeChanged(fDocument, null);
				fListener.inputDocumentChanged(fDocument, null);
			}
			fListener= null;

			synchronized (this) {
				// http://dev.eclipse.org/bugs/show_bug.cgi?id=19135
				BackgroundWorker bt= fWorker;
				fWorker= null;
				bt.cancel();
			}
		}
	}

	/**
	 * Creates a dirty region for a document event and adds it to the queue.
	 *
	 * @param e the document event for which to create a dirty region
	 */
	private void createDirtyRegion(DocumentEvent e) {
		synchronized (fDirtyRegionQueue) {
			if (e.getLength() == 0 && e.getText() != null) {
				// Insert
				fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getText().length(), DirtyRegion.INSERT, e.getText()));

			} else if (e.getText() == null || e.getText().isEmpty()) {
				// Remove
				fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getLength(), DirtyRegion.REMOVE, null));

			} else {
				// Replace (Remove + Insert)
				fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getLength(), DirtyRegion.REMOVE, null));
				fDirtyRegionQueue.addDirtyRegion(new DirtyRegion(e.getOffset(), e.getText().length(), DirtyRegion.INSERT, e.getText()));
			}
		}
	}

	/**
	 * Hook for subclasses which want to perform some
	 * action as soon as reconciliation is needed.
	 * <p>
	 * Default implementation is to do nothing.
	 * </p>
	 *
	 * @since 3.0
	 */
	protected void aboutToBeReconciled() {
	}

	/**
	 * Hook for subclasses which want to perform some action as soon as the reconciler starts work
	 * (initial or reconciling) or waiting.
	 * <p>
	 * Default implementation is to do nothing. Implementors may call
	 * {@link #signalWaitForFinish()}.
	 * </p>
	 *
	 * @since 3.20
	 * @see #signalWaitForFinish
	 */
	protected void aboutToWork() {
	}

	/**
	 * Signal reconciling should finish as soon as possible.
	 *
	 * @since 3.20
	 * @see #aboutToWork
	 */
	public void signalWaitForFinish() {
		synchronized (fDirtyRegionQueue) {
			waitFinish= true;
			fDirtyRegionQueue.notifyAll(); // notify AbstractReconciler#delay about waitFinish
		}
	}

	private void informNotFinished() {
		waitFinish= false;
		aboutToWork();
	}

	private void aboutToBeReconciledInternal() {
		aboutToBeReconciled();
		informNotFinished();
	}


	private void delay() {
		synchronized (fDirtyRegionQueue) {
			if (waitFinish) {
				return; // do not delay when waiting;
			}
			try {
				fDirtyRegionQueue.wait(fDelay);
			} catch (InterruptedException x) {
			}
		}
	}

	/**
	 * This method is called on startup of the background activity. It is called only
	 * once during the life time of the reconciler. Clients may reimplement this method.
	 */
	protected void initialProcess() {
	}

	/**
	 * Forces the reconciler to reconcile the structure of the whole document.
	 * Clients may extend this method.
	 */
	protected void forceReconciling() {

		if (fDocument != null) {

			if (!fWorker.isDirty()&& fWorker.isAlive())
				aboutToBeReconciledInternal();

			if (fWorker.isActive())
				fProgressMonitor.setCanceled(true);

			if (fIsIncrementalReconciler) {
				DocumentEvent e= new DocumentEvent(fDocument, 0, fDocument.getLength(), fDocument.get());
				createDirtyRegion(e);
			}

			startReconciling();
		}
	}

	/**
	 * Starts the reconciler to reconcile the queued dirty-regions.
	 * Clients may extend this method.
	 */
	protected synchronized void startReconciling() {
		if (fWorker == null)
			return;

		fWorker.startReconciling();
	}

	/**
	 * Hook that is called after the reconciler thread has been reset.
	 */
	protected void reconcilerReset() {
	}

	/**
	 * Tells whether the code is running in this reconciler's
	 * background thread.
	 *
	 * @return <code>true</code> if running in this reconciler's background thread
	 * @since 3.4
	 */
	protected synchronized boolean isRunningInReconcilerThread() {
		if (fWorker == null)
			return false;
		return Thread.currentThread() == fWorker.fThread;
	}
}
