/*******************************************************************************
 * Copyright (c) 2004, 2020 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     SAP SE - port to platform.ui
 *******************************************************************************/

package org.eclipse.jface.notifications.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class AnimationUtil {

	public static final long FADE_RESCHEDULE_DELAY = 80;

	public static final int FADE_IN_INCREMENT = 15;

	public static final int FADE_OUT_INCREMENT = -20;

	public static FadeJob fastFadeIn(Shell shell, IFadeListener listener) {
		return new FadeJob(shell, 2 * FADE_IN_INCREMENT, FADE_RESCHEDULE_DELAY, listener);
	}

	public static FadeJob fadeIn(Shell shell, IFadeListener listener) {
		return new FadeJob(shell, FADE_IN_INCREMENT, FADE_RESCHEDULE_DELAY, listener);
	}

	public static FadeJob fadeOut(Shell shell, IFadeListener listener) {
		return new FadeJob(shell, FADE_OUT_INCREMENT, FADE_RESCHEDULE_DELAY, listener);
	}

	public static class FadeJob extends Job {

		private final Shell shell;

		private final int increment;

		private volatile boolean stopped;

		private volatile int currentAlpha;

		private final long delay;

		private final IFadeListener fadeListener;

		public FadeJob(Shell shell, int increment, long delay, IFadeListener fadeListener) {
			super("FaceJob");
			if (increment < -255 || increment == 0 || increment > 255) {
				throw new IllegalArgumentException("-255 <= increment <= 255 && increment != 0"); //$NON-NLS-1$
			}
			if (delay < 1) {
				throw new IllegalArgumentException("delay must be > 0"); //$NON-NLS-1$
			}
			this.currentAlpha = shell.getAlpha();
			this.shell = shell;
			this.increment = increment;
			this.delay = delay;
			this.fadeListener = fadeListener;

			setSystem(true);
			schedule(delay);
		}

		@Override
		protected void canceling() {
			this.stopped = true;
		}

		private void reschedule() {
			if (this.stopped) {
				return;
			}
			schedule(this.delay);
		}

		public void cancelAndWait(final boolean setAlpha) {
			if (this.stopped) {
				return;
			}
			cancel();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (setAlpha) {
						FadeJob.this.shell.setAlpha(getLastAlpha());
					}
				}
			});
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (this.stopped) {
				return Status.OK_STATUS;
			}

			this.currentAlpha += this.increment;
			if (this.currentAlpha <= 0) {
				this.currentAlpha = 0;
			} else if (this.currentAlpha >= 255) {
				this.currentAlpha = 255;
			}

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (FadeJob.this.stopped) {
						return;
					}

					if (FadeJob.this.shell.isDisposed()) {
						FadeJob.this.stopped = true;
						return;
					}

					FadeJob.this.shell.setAlpha(FadeJob.this.currentAlpha);

					if (FadeJob.this.fadeListener != null) {
						FadeJob.this.fadeListener.faded(FadeJob.this.shell, FadeJob.this.currentAlpha);
					}
				}
			});

			if (this.currentAlpha == 0 || this.currentAlpha == 255) {
				this.stopped = true;
			}

			reschedule();
			return Status.OK_STATUS;
		}

		private int getLastAlpha() {
			return (this.increment < 0) ? 0 : 255;
		}

	}

	public static interface IFadeListener {

		public void faded(Shell shell, int alpha);

	}

}
