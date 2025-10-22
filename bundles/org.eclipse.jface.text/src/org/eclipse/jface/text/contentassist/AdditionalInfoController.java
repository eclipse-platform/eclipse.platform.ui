/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.jface.text.contentassist;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.internal.text.InformationControlReplacer;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension3;


/**
 * Displays the additional information available for a completion proposal.
 *
 * @since 2.0
 */
class AdditionalInfoController extends AbstractInformationControlManager {

	/**
	 * A timer thread.
	 *
	 * @since 3.2
	 */
	private static abstract class Timer {
		private static final int DELAY_UNTIL_JOB_IS_SCHEDULED= 50;

		/**
		 * A <code>Task</code> is {@link Task#run() run} when {@link #delay()} milliseconds have
		 * elapsed after it was scheduled without a {@link #reset(ICompletionProposal) reset}
		 * to occur.
		 */
		private abstract static class Task implements Runnable {
			/**
			 * @return the delay in milliseconds before this task should be run
			 */
			public abstract long delay();
			/**
			 * Runs this task.
			 */
			@Override
			public abstract void run();
			/**
			 * @return the task to be scheduled after this task has been run
			 */
			public abstract Task nextTask();
		}

		/**
		 * IDLE: the initial task, and active whenever the info has been shown. It cannot be run,
		 * but specifies an infinite delay.
		 */
		private final Task IDLE= new Task() {
			@Override
			public void run() {
				Assert.isTrue(false);
			}

			@Override
			public Task nextTask() {
				Assert.isTrue(false);
				return null;
			}

			@Override
			public long delay() {
				return Long.MAX_VALUE;
			}

			@Override
			public String toString() {
				return "IDLE"; //$NON-NLS-1$
			}
		};
		/**
		 * FIRST_WAIT: Schedules a platform {@link Job} to fetch additional info from an {@link ICompletionProposalExtension5}.
		 */
		private final Task FIRST_WAIT= new Task() {
			@Override
			public void run() {
				final ICompletionProposalExtension5 proposal= getCurrentProposalEx();
				Job job= new Job(JFaceTextMessages.getString("AdditionalInfoController.job_name")) { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						Object info;
						try {
							info= proposal.getAdditionalProposalInfo(monitor);
						} catch (RuntimeException x) {
							/*
							 * XXX: This is the safest fix at this point so close to end of 3.2.
							 *		Will be revisited when fixing https://bugs.eclipse.org/bugs/show_bug.cgi?id=101033
							 */
							return new Status(IStatus.WARNING, "org.eclipse.jface.text", IStatus.OK, "", x); //$NON-NLS-1$ //$NON-NLS-2$
						}
						setInfo((ICompletionProposal) proposal, info);
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public Task nextTask() {
				return SECOND_WAIT;
			}

			@Override
			public long delay() {
				return DELAY_UNTIL_JOB_IS_SCHEDULED;
			}

			@Override
			public String toString() {
				return "FIRST_WAIT"; //$NON-NLS-1$
			}
		};
		/**
		 * SECOND_WAIT: Allows display of additional info obtained from an
		 * {@link ICompletionProposalExtension5}.
		 */
		private final Task SECOND_WAIT= new Task() {
			@Override
			public void run() {
				// show the info
				allowShowing();
			}

			@Override
			public Task nextTask() {
				return IDLE;
			}

			@Override
			public long delay() {
				return fDelay - DELAY_UNTIL_JOB_IS_SCHEDULED;
			}

			@Override
			public String toString() {
				return "SECOND_WAIT"; //$NON-NLS-1$
			}
		};
		/**
		 * LEGACY_WAIT: Posts a runnable into the display thread to fetch additional info from non-{@link ICompletionProposalExtension5}s.
		 */
		private final Task LEGACY_WAIT= new Task() {
			@Override
			public void run() {
				final ICompletionProposal proposal= getCurrentProposal();
				if (!fDisplay.isDisposed()) {
					fDisplay.asyncExec(() -> {
						synchronized (Timer.this) {
							if (proposal == getCurrentProposal()) {
								Object info= proposal.getAdditionalProposalInfo();
								showInformation(proposal, info);
							}
						}
					});
				}
			}

			@Override
			public Task nextTask() {
				return IDLE;
			}

			@Override
			public long delay() {
				return fDelay;
			}

			@Override
			public String toString() {
				return "LEGACY_WAIT"; //$NON-NLS-1$
			}
		};
		/**
		 * EXIT: The task that triggers termination of the timer thread.
		 */
		private final Task EXIT= new Task() {
			@Override
			public long delay() {
				return 1;
			}

			@Override
			public Task nextTask() {
				Assert.isTrue(false);
				return EXIT;
			}

			@Override
			public void run() {
				Assert.isTrue(false);
			}

			@Override
			public String toString() {
				return "EXIT"; //$NON-NLS-1$
			}
		};

		/** The timer thread. */
		private final Thread fThread;

		/** The currently waiting / active task. */
		private Task fTask;
		/** The next wake up time. */
		private long fNextWakeup;

		private ICompletionProposal fCurrentProposal= null;
		private Object fCurrentInfo= null;
		private boolean fAllowShowing= false;

		private final Display fDisplay;
		private final int fDelay;

		/**
		 * Creates a new timer.
		 *
		 * @param display the display to use for display thread posting.
		 * @param delay the delay until to show additional info
		 */
		public Timer(Display display, int delay) {
			fDisplay= display;
			fDelay= delay;
			long current= System.currentTimeMillis();
			schedule(IDLE, current);

			fThread= new Thread((Runnable) () -> {
				try {
					loop();
				} catch (InterruptedException x) {
				}
			}, JFaceTextMessages.getString("InfoPopup.info_delay_timer_name")); //$NON-NLS-1$
			fThread.start();
		}

		/**
		 * Terminates the timer thread.
		 */
		public synchronized final void terminate() {
			schedule(EXIT, System.currentTimeMillis());
			notifyAll();
		}

		/**
		 * Resets the timer thread as the selection has changed to a new proposal.
		 *
		 * @param p the new proposal
		 */
		public synchronized final void reset(ICompletionProposal p) {
			if (fCurrentProposal != p) {
				fCurrentProposal= p;
				fCurrentInfo= null;
				fAllowShowing= false;

				long oldWakeup= fNextWakeup;
				Task task= taskOnReset(p);
				schedule(task, System.currentTimeMillis());
				if (fNextWakeup < oldWakeup) {
					notifyAll();
				}
			}
		}

		private Task taskOnReset(ICompletionProposal p) {
			if (p == null) {
				return IDLE;
			}
			if (isExt5(p)) {
				return FIRST_WAIT;
			}
			return LEGACY_WAIT;
		}

		private synchronized void loop() throws InterruptedException {
			long current= System.currentTimeMillis();
			Task task= currentTask();

			while (task != EXIT) {
				long delay= fNextWakeup - current;
				if (delay <= 0) {
					task.run();
					task= task.nextTask();
					schedule(task, current);
				} else {
					wait(delay);
					current= System.currentTimeMillis();
					task= currentTask();
				}
			}
		}

		private Task currentTask() {
			return fTask;
		}

		private void schedule(Task task, long current) {
			fTask= task;
			long nextWakeup= current + task.delay();
			if (nextWakeup <= current) {
				fNextWakeup= Long.MAX_VALUE;
			} else {
				fNextWakeup= nextWakeup;
			}
		}

		private boolean isExt5(ICompletionProposal p) {
			return p instanceof ICompletionProposalExtension5;
		}

		ICompletionProposal getCurrentProposal() {
			return fCurrentProposal;
		}

		ICompletionProposalExtension5 getCurrentProposalEx() {
			Assert.isTrue(fCurrentProposal instanceof ICompletionProposalExtension5);
			return (ICompletionProposalExtension5) fCurrentProposal;
		}

		synchronized void setInfo(ICompletionProposal proposal, Object info) {
			if (proposal == fCurrentProposal) {
				fCurrentInfo= info;
				if (fAllowShowing) {
					triggerShowing();
				}
			}
		}

		private void triggerShowing() {
			final Object info= fCurrentInfo;
			if (!fDisplay.isDisposed()) {
				fDisplay.asyncExec(() -> {
					synchronized (Timer.this) {
						if (info == fCurrentInfo) {
							showInformation(fCurrentProposal, info);
						}
					}
				});
			}
		}

		/**
		 * Called in the display thread to show additional info.
		 *
		 * @param proposal the proposal to show information about
		 * @param info the information about <code>proposal</code>
		 */
		protected abstract void showInformation(ICompletionProposal proposal, Object info);

		void allowShowing() {
			fAllowShowing= true;
			triggerShowing();
		}
	}
	/**
	 * Internal table selection listener.
	 */
	private class TableSelectionListener implements SelectionListener {

		@Override
		public void widgetSelected(SelectionEvent e) {
			handleTableSelectionChanged();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	/**
	 * Default control creator for the information control replacer.
	 * @since 3.4
	 */
	private static class DefaultPresenterControlCreator extends AbstractReusableInformationControlCreator {
		@Override
		public IInformationControl doCreateInformationControl(Shell shell) {
			return new DefaultInformationControl(shell, true);
		}
	}

	/** The proposal table. */
	private volatile Table fProposalTable;
	/** The table selection listener */
	private final SelectionListener fSelectionListener= new TableSelectionListener();
	/** The delay after which additional information is displayed */
	private final int fDelay;
	/**
	 * The timer thread.
	 * @since 3.2
	 */
	private volatile Timer fTimer;

	/**
	 * The proposal most recently set by {@link #showInformation(ICompletionProposal, Object)},
	 * possibly <code>null</code>.
	 * @since 3.2
	 */
	private volatile ICompletionProposal fProposal;

	/**
	 * The information most recently set by {@link #showInformation(ICompletionProposal, Object)},
	 * possibly <code>null</code>.
	 * @since 3.2
	 */
	private Object fInformation;

	/**
	 * Creates a new additional information controller.
	 *
	 * @param creator the information control creator to be used by this controller
	 * @param delay time in milliseconds after which additional info should be displayed
	 */
	AdditionalInfoController(IInformationControlCreator creator, int delay) {
		super(creator);
		fDelay= delay;
		setAnchor(ANCHOR_RIGHT);
		setFallbackAnchors(new Anchor[] { ANCHOR_RIGHT, ANCHOR_LEFT, ANCHOR_BOTTOM });

		/*
		 * Adjust the location by one pixel towards the proposal popup, so that the single pixel
		 * border of the additional info popup overlays with the border of the popup. This avoids
		 * having a double black line.
		 */
		int spacing= -1;
		setMargins(spacing, spacing); // see also adjustment in #computeLocation

		InformationControlReplacer replacer= new InformationControlReplacer(new DefaultPresenterControlCreator());
		getInternalAccessor().setInformationControlReplacer(replacer);
	}

	@Override
	public void install(Control control) {
		if (fProposalTable == control) {
			// already installed
			return;
		}
		Assert.isTrue(control instanceof Table);

		super.install(control.getShell());

		fProposalTable= (Table) control;
		((Table) control).addSelectionListener(fSelectionListener);
		getInternalAccessor().getInformationControlReplacer().install(control);

		fTimer= new Timer(control.getDisplay(), fDelay) {
			@Override
			protected void showInformation(ICompletionProposal proposal, Object info) {
				InformationControlReplacer replacer= getInternalAccessor().getInformationControlReplacer();
				if (replacer != null) {
					replacer.hideInformationControl();
				}
				AdditionalInfoController.this.showInformation(proposal, info);
			}
		};
	}

	@Override
	public void disposeInformationControl() {

		Timer timer= fTimer;
		if (timer !=null) {
			timer.terminate();
			fTimer= null;
		}

		Table table= fProposalTable;
		if (table != null && !table.isDisposed()) {
			table.removeSelectionListener(fSelectionListener);
			fProposalTable= null;
		}

		fProposal= null;
		fInformation= null;

		super.disposeInformationControl();
	}

	/**
	 *Handles a change of the line selected in the associated selector.
	 */
	public void handleTableSelectionChanged() {
		Table table= fProposalTable;
		if (table != null && !table.isDisposed() && table.isVisible()) {
			TableItem[] selection= table.getSelection();
			if (selection != null && selection.length > 0) {

				TableItem item= selection[0];

				Object d= item.getData();
				if (d instanceof ICompletionProposal p) {
					Timer timer= fTimer;
					if (timer != null) {
						timer.reset(p);
					}
				}
			}
		}
	}

	void showInformation(ICompletionProposal proposal, Object info) {
		ICompletionProposal oldProposal= fProposal;
		Object oldInformation= fInformation;
		Table table= fProposalTable;
		if (table == null || table.isDisposed()) {
			return;
		}

		if (oldProposal == proposal && ((info == null && oldInformation == null) || (info != null && info.equals(oldInformation)))) {
			return;
		}

		fInformation= info;
		fProposal= proposal;
		showInformation();
	}

	@Override
	protected void computeInformation() {
		Table table= fProposalTable;
		if (table == null || table.isDisposed()) {
			return;
		}

		ICompletionProposal proposal= fProposal;
		if (proposal instanceof ICompletionProposalExtension3) {
			setCustomInformationControlCreator(((ICompletionProposalExtension3) proposal).getInformationControlCreator());
		} else {
			setCustomInformationControlCreator(null);
		}

		table= fProposalTable;
		if (table == null || table.isDisposed()) {
			return;
		}

		// compute subject area
		Point size= table.getShell().getSize();

		// set information & subject area
		setInformation(fInformation, new Rectangle(0, 0, size.x, size.y));
	}

	@Override
	protected Point computeLocation(Rectangle subjectArea, Point controlSize, Anchor anchor) {
		Point location= super.computeLocation(subjectArea, controlSize, anchor);

		Table table= fProposalTable;
		if (table == null) {
			return location;
		}
		/*
		 * The location is computed using subjectControl.toDisplay(), which does not include the
		 * trim of the subject control. As we want the additional info popup aligned with the outer
		 * coordinates of the proposal popup, adjust this here
		 */
		Rectangle trim= table.getShell().computeTrim(0, 0, 0, 0);
		location.x += trim.x;
		location.y += trim.y;

		return location;
	}

	@Override
	protected Point computeSizeConstraints(Control subjectControl, IInformationControl informationControl) {
		// at least as big as the proposal table
		Point sizeConstraint= super.computeSizeConstraints(subjectControl, informationControl);
		Point size= subjectControl.getShell().getSize();

		// AbstractInformationControlManager#internalShowInformationControl(Rectangle, Object) adds trims
		// to the computed constraints. Need to remove them here, to make the outer bounds of the additional
		// info shell fit the bounds of the proposal shell:
		if (fInformationControl instanceof IInformationControlExtension3) {
			Rectangle shellTrim= ((IInformationControlExtension3) fInformationControl).computeTrim();
			size.x -= shellTrim.width;
			size.y -= shellTrim.height;
		}

		if (sizeConstraint.x < size.x) {
			sizeConstraint.x= size.x;
		}
		if (sizeConstraint.y < size.y) {
			sizeConstraint.y= size.y;
		}
		return sizeConstraint;
	}

	@Override
	protected void hideInformationControl() {
		super.hideInformationControl();
		Timer timer= fTimer;
		if (timer != null) {
			timer.reset(null);
		}
	}

	@Override
	protected boolean canClearDataOnHide() {
		return false; // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=293176
	}

	/**
	 * @return the current information control, or <code>null</code> if none available
	 */
	public IInformationControl getCurrentInformationControl2() {
		return getInternalAccessor().getCurrentInformationControl();
	}
}


