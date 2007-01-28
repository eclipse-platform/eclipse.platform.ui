/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal;

import org.eclipse.core.databinding.BindingEvent;
import org.eclipse.core.databinding.IBindingListener;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Implementations for tracking the flow of the pipeline.
 * 
 * @since 3.2
 */
class Pipeline {
	/**
	 * Validator that when invoked registers with a listener for tracking
	 * purposes.
	 * 
	 * @since 3.2
	 */
	public static class TrackedValidator implements IValidator {
		int count;
		TrackLastListener listener;

		public TrackedValidator(TrackLastListener listener) {
			this.listener = listener;
		}

		public IStatus validate(Object value) {
			if (listener.validatorInvoked(this)) {
				count++;
			}
			return Status.CANCEL_STATUS;
		}
	}

	/**
	 * Tracks the last position the listener received.
	 * 
	 * @since 3.2
	 */
	public static class TrackLastListener implements IBindingListener {
		boolean active = true;
		IValidator lastValidator;
		int lastPosition = -1;
		int lastCopyType = -1;

		public IStatus handleBindingEvent(BindingEvent e) {
			if (active) {
				lastPosition = e.pipelinePosition;
				lastCopyType = e.copyType;
			}

			return Status.OK_STATUS;
		}

		/**
		 * @param validator
		 * @result <code>true</code> if should proceed
		 */
		public boolean validatorInvoked(IValidator validator) {
			if (!active) {
				return false;
			}
			this.lastValidator = validator;

			return true;
		}
	}

	/**
	 * Tracks the positions the listener was invoked and values from those
	 * invocations.
	 * 
	 * @since 3.2
	 */
	public static class TrackPositionListener implements IBindingListener {
		int[] positions;
		int[] copyTypes;
		int index = -1;
		int count;
		final int expectedCount;

		public TrackPositionListener(int expectedCount) {
			positions = new int[this.expectedCount = expectedCount];
			copyTypes = new int[expectedCount];
		}

		public IStatus handleBindingEvent(BindingEvent e) {
			positions[++index] = e.pipelinePosition;
			copyTypes[index] = e.copyType;
			count++;

			return Status.OK_STATUS;
		}

		public void reset() {
			index = -1;
			count = 0;
			positions = new int[expectedCount];
			copyTypes = new int[expectedCount];
		}
	}
}
