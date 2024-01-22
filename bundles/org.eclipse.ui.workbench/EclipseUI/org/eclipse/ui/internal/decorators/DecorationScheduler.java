/*******************************************************************************
 * Copyright (c) 2000, 2016, 2019 IBM Corporation and others.
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
 *     Francis Upton <francisu@ieee.org> -
 *     		Fix for Bug 216667 [Decorators] DecorationScheduler hangs onto objects forever sometimes
 *     Stefan Winkler <stefan@winklerweb.net> - bug 417255 - Race Condition in DecorationScheduler
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationContext;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The DecorationScheduler is the class that handles the decoration of elements
 * using a background thread.
 *
 * <p>
 * In detail, it works as follows:
 * </p>
 * <ol>
 * <li>When a client (usually the method
 * {@link DecoratorManager#prepareDecoration(Object, String, IDecorationContext)}
 * needs an element decorated, it calls <code>queueForDecoration</code>.</li>
 *
 * <li><code>queueForDecoration</code> inserts (or, if already present, updates)
 * the <code>element</code> into <code>awaitingDecoration</code> and an
 * associated <code>DecorationReference</code> into
 * <code>awaitingDecorationValues</code> and schedules the
 * <code>decorationJob</code> to be executed asynchonously.</li>
 *
 * <li>The <code>decorationJob</code> processes the list of elements in
 * <code>awaitingDecoration</code> one by one. For each element, it calls
 * <code>ensureResultCached</code>, which in turn calls the actual lightweight
 * decorator(s) for the element and stores the decoration result in the
 * <code>resultCache</code> and adds the element to the
 * <code>pendingUpdate</code> collection to remember that it needs to be updated
 * in the UI.</li>
 *
 * <li>When the <code>decorationJob</code> has finished processing all elements
 * in <code>awaitingDecoration</code>, it finally schedules the
 * <code>updateJob</code>.</li>
 *
 * <li>
 * <p>
 * The <code>updateJob</code> runs in the UI thread and fires
 * {@link LabelProviderChangedEvent}s for all elements in
 * <code>pendingUpdate</code>. The {@link LabelProviderChangedEvent} causes the
 * respective viewers to trigger an <code>update</code> for the given elements.
 * </p>
 * <p>
 * Effectively, this again calls
 * {@link DecoratorManager#prepareDecoration(Object, String, IDecorationContext)}
 * (see step 1.). But this time, before calling <code>queueForDecoration</code>,
 * the <code>DecoratorManager</code> first checks <code>isDecorationReady</code>
 * and because a decoration is ready (because it has been computed in the
 * meantime), the decoration result is obtained directly from the
 * <code>resultCache</code> and updated in the UI.
 * </p>
 * <p>
 * Note that <code>isDecorationReady</code> must only return <code>true</code>
 * if the call originates from the <code>updateJob</code>. In all other cases,
 * the original call originates from the application code (possibly to trigger
 * an update of the decoration). This is why the <code>updateJob</code> uses the
 * <code>isUpdateJobRunning</code> flag to indicate that it is currently running
 * in the UI thread.
 * </p>
 * </li>
 * </ol>
 *
 * So, in summary, a decoration is first enqueued, then calculated
 * asynchronously by the <code>decorationJob</code> and enqueued for UI-update,
 * and finally, the <code>updateJob</code> fires events which cause the viewers
 * to update the UI using the now available decoration results.
 */
public class DecorationScheduler {

	private static final ILabelProviderListener[] EMPTY_LISTENER_LIST = new ILabelProviderListener[0];

	// When decorations are computed they are added to this cache via
	// scheduleUpdateJob() method
	private final Map<IDecorationContext, Map<Object, DecorationResult>> resultCache = new ConcurrentHashMap<>();

	/**
	 * Objects that are awaiting a label update. manually synchronized on
	 * DecorationScheduler.pendingUpdate
	 **/
	private final Set<Object> pendingUpdate = new LinkedHashSet<>();

	/** manually synchronized on DecorationScheduler.this **/
	private final LinkedHashMap<Object, DecorationReference> awaitingDecoration = new LinkedHashMap<>();

	/** manually synchronized on DecorationScheduler.this **/
	private boolean shutdown = false;

	private final DecoratorManager decoratorManager;

	private final Job decorationJob;

	// Notifies about updateJob or clearJob finishing
	private final class JobChangeListener extends JobChangeAdapter {

		// is called after changeState(job, Job.NONE);
		@Override
		public void done(IJobChangeEvent event) {
			synchronized (this) {
				if (!updatesPending()) { // signal only if no more updates pending
					this.notifyAll(); // also notify if nobody is waiting.
				}
			}
		}

		void sleep(long timeoutMillis) throws InterruptedException {
			synchronized (this) {
				if (updatesPending()) { // avoid wait if no updates pending
					this.wait(timeoutMillis);
				}
			}
		}
	}

	private final JobChangeListener jobFinishListener = new JobChangeListener();

	private UIJob updateJob;

	private Collection<ILabelProviderListener> removedListeners = Collections.synchronizedSet(new HashSet<>());

	private Job clearJob;

	// Static used for the updates to indicate an update is required
	private static final int NEEDS_INIT = -1;

	/** Amount of time to delay the update notification when max reached. */
	private static final int UPDATE_DELAY = 100;

	/**
	 * Flag which is used to indicate that the update job is running in the UI
	 * thread
	 */
	private boolean isUpdateJobRunning = false;

	/**
	 * Return a new instance of the receiver configured for the supplied
	 * DecoratorManager.
	 */
	DecorationScheduler(DecoratorManager manager) {
		decoratorManager = manager;
		decorationJob = createDecorationJob();
	}

	/**
	 * Decorate the text for the receiver. If it has already been done then return
	 * the result, otherwise queue it for decoration.
	 *
	 * @return String
	 * @param adaptedElement The adapted value of element. May be null.
	 * @param context        the decoration context
	 */

	public String decorateWithText(String text, Object element, Object adaptedElement, IDecorationContext context) {

		DecorationResult decoration = getResult(element, adaptedElement, context);

		if (decoration == null) {
			return text;
		}

		return decoration.decorateWithText(text);

	}

	/**
	 * Queue the element and its adapted value if it has not been already.
	 *
	 * @param adaptedElement  The adapted value of element. May be null.
	 * @param forceUpdate     If true then a labelProviderChanged is fired whether
	 *                        decoration occurred or not.
	 * @param undecoratedText The original text for the element if it is known.
	 * @param context         The decoration context
	 */

	synchronized void queueForDecoration(Object element, Object adaptedElement, boolean forceUpdate,
			String undecoratedText, IDecorationContext context) {

		Assert.isNotNull(context);
		DecorationReference reference = awaitingDecoration.get(element);
		if (reference != null) {
			if (forceUpdate) {// Make sure we don't loose a force
				reference.setForceUpdate(forceUpdate);
			}
			reference.addContext(context);
		} else {
			reference = new DecorationReference(element, adaptedElement, context);
			reference.setForceUpdate(forceUpdate);
			reference.setUndecoratedText(undecoratedText);
			awaitingDecoration.put(element, reference);
			if (shutdown) {
				return;
			}
			schedule();
		}

	}

	/*
	 * should not be called before constructor finished. Would leak reference to
	 * incomplete constructed DecorationScheduler.this
	 */
	void schedule() {
		decorationJob.schedule();
	}

	/**
	 * Decorate the supplied image, element and its adapted value.
	 *
	 * @return Image
	 * @param adaptedElement The adapted value of element. May be null.
	 * @param context        the decoration context
	 */
	public Image decorateWithOverlays(Image image, Object element, Object adaptedElement, IDecorationContext context,
			ResourceManager manager) {

		DecorationResult decoration = getResult(element, adaptedElement, context);

		if (decoration == null) {
			return image;
		}
		return decoration.decorateWithOverlays(image, manager);
	}

	/**
	 * Return the DecorationResult for element. If there isn't one queue for
	 * decoration and return <code>null</code>.
	 *
	 * @param element        The element to be decorated. If it is <code>null</code>
	 *                       return <code>null</code>.
	 * @param adaptedElement It's adapted value.
	 * @param context        The deocration context
	 * @return DecorationResult or <code>null</code>
	 */
	private DecorationResult getResult(Object element, Object adaptedElement, IDecorationContext context) {

		// We do not support decoration of null
		if (element == null) {
			return null;
		}

		DecorationResult decoration = internalGetResult(element, context);

		if (decoration == null) {
			queueForDecoration(element, adaptedElement, false, null, context);
			return null;
		}
		return decoration;

	}

	private DecorationResult internalGetResult(Object element, IDecorationContext context) {
		Map<Object, DecorationResult> results = resultCache.get(context);
		if (results != null) {
			return results.get(element);
		}
		return null;
	}

	protected void internalPutResult(Object element, IDecorationContext context, DecorationResult result) {
		Map<Object, DecorationResult> results = resultCache.computeIfAbsent(context, ctx -> new ConcurrentHashMap<>());
		results.put(element, result);
	}

	/**
	 * Execute a label update using the pending decorations.
	 */
	synchronized void scheduleUpdateJob() {

		// Don't bother if we are shutdown now
		if (shutdown) {
			return;
		}

		// Lazy initialize the job
		if (updateJob == null) {
			updateJob = getUpdateJob();
		}

		// Give it a bit of a lag for other updates to occur
		updateJob.schedule(UPDATE_DELAY);
	}

	/**
	 * Shutdown the decoration.
	 */
	synchronized void shutdown() {
		shutdown = true;
	}

	/**
	 * Get the next resource to be decorated.
	 *
	 * @return IResource
	 */
	synchronized DecorationReference removeNextReference() {
		Iterator<Entry<Object, DecorationReference>> iterator = awaitingDecoration.entrySet().iterator();
		if (shutdown || !iterator.hasNext()) {
			return null;
		}
		Entry<Object, DecorationReference> entry = iterator.next();
		iterator.remove();
		DecorationReference reference = entry.getValue();
		return reference;
	}

	/**
	 * Create the Thread used for running decoration.
	 */
	private Job createDecorationJob() {
		Job decorationJob = new Job(WorkbenchMessages.DecorationScheduler_CalculationJobName) {
			@Override
			public IStatus run(IProgressMonitor monitor) {

				synchronized (DecorationScheduler.this) {
					if (shutdown) {
						return Status.CANCEL_STATUS;
					}
				}

				while (updatesPending()) {

					try {
						jobFinishListener.sleep(100);
					} catch (InterruptedException e) {
						// Cancel and try again if there was an error
						schedule();
						return Status.CANCEL_STATUS;
					}
				}

				SubMonitor subMonitor = SubMonitor.convert(monitor,
						WorkbenchMessages.DecorationScheduler_CalculatingTask, awaitingDecoration.size() + 1);
				// will block if there are no resources to be decorated
				DecorationReference reference;

				boolean queued = false;
				// for each in awaitingDecorationValues, but not locking the map during the whole iteration:
				while ((reference = removeNextReference()) != null) {
					subMonitor.split(1);
					queued = true;
					Object element = reference.getElement();
					boolean force = reference.shouldForceUpdate();
					Collection<IDecorationContext> contexts = reference.getContexts();
					for (IDecorationContext context : contexts) {
						queued |= queue(element, force, context);
					}
					subMonitor.setWorkRemaining(awaitingDecoration.size() + 1); // may grow asynchronously
					// Only notify listeners when we have exhausted the
					// queue of decoration requests.
				}
				if (queued) {
					scheduleUpdateJob();
				}
				return Status.OK_STATUS;
			}

			/**
			 * Ensure that a result is cached for the given element and context
			 *
			 * @param element the elements
			 * @param force   whether an update should be forced
			 * @param context the decoration context
			 */
			private boolean queue(Object element, boolean force, IDecorationContext context) {
				DecorationBuilder cacheResult = new DecorationBuilder(context);
				// Calculate the decoration
				decoratorManager.getLightweightManager().getDecorations(element, cacheResult);

				// If we should update regardless then put a result
				// anyways
				if (cacheResult.hasValue() || force) {

					// Synchronize on the result lock as we want to
					// be sure that we do not try and decorate during
					// label update servicing.
					// Note: resultCache and pendingUpdate modifications
					// must be done atomically.

					// Add the decoration even if it's empty in
					// order to indicate that the decoration is
					// ready

					internalPutResult(element, context, cacheResult.createResult());

					// Add an update for only the original element
					// to
					// prevent multiple updates and clear the cache.
					synchronized (pendingUpdate) {
						pendingUpdate.add(element);
					}
					return true;
				}
				return false;
			}

			@Override
			public boolean belongsTo(Object family) {
				return DecoratorManager.FAMILY_DECORATE == family;
			}

			@Override
			public boolean shouldRun() {
				return PlatformUI.isWorkbenchRunning();
			}
		};

		decorationJob.setSystem(true);
		decorationJob.setPriority(Job.DECORATE);
		return decorationJob;
	}

	/**
	 * Return whether or not we are waiting on updated
	 *
	 * @return <code>true</code> if there are updates waiting to be served
	 */
	protected boolean updatesPending() {
		if (updateJob != null && updateJob.getState() != Job.NONE) {
			return true;
		}
		if (clearJob != null && clearJob.getState() != Job.NONE) {
			return true;
		}
		return false;
	}

	/**
	 * An external update request has been made. Clear the results as they are
	 * likely obsolete now.
	 */
	void clearResults() {
		if (clearJob == null) {
			clearJob = getClearJob();
		}
		clearJob.schedule();
	}

	private Job getClearJob() {
		Job clear = new Job(WorkbenchMessages.DecorationScheduler_ClearResultsJob) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				resultCache.clear();
				return Status.OK_STATUS;
			}

			@Override
			public boolean shouldRun() {
				return PlatformUI.isWorkbenchRunning();
			}

		};
		clear.setSystem(true);
		clear.addJobChangeListener(jobFinishListener);
		return clear;
	}

	/**
	 * Get the update WorkbenchJob.
	 *
	 * @return WorkbenchJob
	 */
	private WorkbenchJob getUpdateJob() {
		WorkbenchJob job = new WorkbenchJob(WorkbenchMessages.DecorationScheduler_UpdateJobName) {

			int currentIndex = NEEDS_INIT;

			LabelProviderChangedEvent labelProviderChangedEvent;

			ILabelProviderListener[] listeners;

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				// set the flag to true while the job is running
				isUpdateJobRunning = true;

				try {
					synchronized (DecorationScheduler.this) {
						if (shutdown) {
							return Status.CANCEL_STATUS;
						}
					}

					// If this is the first one check again in case
					// someone has already cleared it out.
					if (currentIndex == NEEDS_INIT) {
						if (hasPendingUpdates()) {
							resetState();
							return Status.OK_STATUS;
						}
						setUpUpdates();
					}

					if (listeners.length == 0) {
						resetState();
						return Status.OK_STATUS;
					}

					monitor.beginTask(WorkbenchMessages.DecorationScheduler_UpdatingTask, IProgressMonitor.UNKNOWN);

					long startTime = System.currentTimeMillis();
					while (currentIndex < listeners.length) {
						ILabelProviderListener listener = listeners[currentIndex];
						currentIndex++;

						// If it was removed in the meantime then skip it.
						if (!removedListeners.contains(listener)) {
							decoratorManager.fireListener(labelProviderChangedEvent, listener);
						}

						// If it is taking long enough for the user to notice then
						// cancel the updates.
						if ((System.currentTimeMillis() - startTime) >= UPDATE_DELAY / 2) {
							break;
						}
					}

					monitor.done();

					if (currentIndex >= listeners.length) {
						resetState();
						if (!hasPendingUpdates()) {
							scheduleUpdateJob();
						}
						labelProviderChangedEvent = null;
						listeners = EMPTY_LISTENER_LIST;
					} else {
						schedule(UPDATE_DELAY);// Reschedule if we are not done
					}
					return Status.OK_STATUS;
				}
				finally {
					// reset the flag
					isUpdateJobRunning = false;
				}
			}

			/**
			 * Clear any cached information.
			 */
			private void resetState() {
				currentIndex = NEEDS_INIT;// Reset
				removedListeners.clear();
				// Other decoration requests may have occurred due to
				// updates or we may have timed out updating listeners.
				// Only clear the results if there are none pending.
				if (awaitingDecoration.isEmpty()) {
					resultCache.clear();
				}
			}

			private void setUpUpdates() {
				// Get the elements awaiting update and then
				// clear the list
				removedListeners.clear();
				currentIndex = 0;
				synchronized (pendingUpdate) {
					Object[] elements = pendingUpdate.toArray(new Object[pendingUpdate.size()]);
					pendingUpdate.clear();
					labelProviderChangedEvent = new LabelProviderChangedEvent(decoratorManager, elements);
				}
				listeners = decoratorManager.getListeners();
			}

			@Override
			public boolean belongsTo(Object family) {
				return DecoratorManager.FAMILY_DECORATE == family;
			}

			@Override
			public boolean shouldRun() {
				return PlatformUI.isWorkbenchRunning();
			}
		};

		job.setSystem(true);
		job.addJobChangeListener(jobFinishListener);
		return job;
	}

	/**
	 * Return whether or not there is a decoration for this element ready.
	 *
	 * @param context The decoration context
	 * @return boolean true if the element is ready.
	 */
	public boolean isDecorationReady(Object element, IDecorationContext context) {
		// the decoration is only reported as ready, if we are in the UI thread and if
		// the update job is running.
		// Because the update job is running in the UI thread, it can be the only caller
		// of this method if the flag isUpdateJobRunning is TRUE
		if (isUpdateJobRunning) {
			return internalGetResult(element, context) != null;
		}

		// in all other cases, some other logic than the update job is asking and that
		// should always be answered with false - see Bug 417255
		return false;
	}

	/**
	 * Return the background Color for element. If there is no result cue for
	 * decoration and return null, otherwise return the value in the result.
	 *
	 * @param element        The Object to be decorated
	 * @return Color or <code>null</code> if there is no value or if it is has not
	 *         been decorated yet.
	 */
	public Color getBackgroundColor(Object element, Object adaptedElement) {
		DecorationResult decoration = getResult(element, adaptedElement, DecorationContext.DEFAULT_CONTEXT);

		if (decoration == null) {
			return null;
		}
		return decoration.getBackgroundColor();
	}

	/**
	 * Return the font for element. If there is no result cue for decoration and
	 * return null, otherwise return the value in the result.
	 *
	 * @param element        The Object to be decorated
	 * @return Font or <code>null</code> if there is no value or if it is has not
	 *         been decorated yet.
	 */
	public Font getFont(Object element, Object adaptedElement) {
		DecorationResult decoration = getResult(element, adaptedElement, DecorationContext.DEFAULT_CONTEXT);

		if (decoration == null) {
			return null;
		}
		return decoration.getFont();
	}

	/**
	 * Return the foreground Color for element. If there is no result cue for
	 * decoration and return null, otherwise return the value in the result.
	 *
	 * @param element        The Object to be decorated
	 * @return Color or <code>null</code> if there is no value or if it is has not
	 *         been decorated yet.
	 */
	public Color getForegroundColor(Object element, Object adaptedElement) {
		DecorationResult decoration = getResult(element, adaptedElement, DecorationContext.DEFAULT_CONTEXT);

		if (decoration == null) {
			return null;
		}
		return decoration.getForegroundColor();
	}

	/**
	 * A listener has been removed. If we are updating then skip it.
	 */
	void listenerRemoved(ILabelProviderListener listener) {
		if (updatesPending()) {// Only keep track of them if there are updates
			// pending
			removedListeners.add(listener);
		}
		if (!updatesPending()) {
			removedListeners.remove(listener);
		}

	}

	/**
	 * Return whether or not there are any updates pending.
	 *
	 * @return boolean <code>true</code> if the updates are empty
	 */
	boolean hasPendingUpdates() {
		synchronized (pendingUpdate) {
			return pendingUpdate.isEmpty();
		}

	}
}
