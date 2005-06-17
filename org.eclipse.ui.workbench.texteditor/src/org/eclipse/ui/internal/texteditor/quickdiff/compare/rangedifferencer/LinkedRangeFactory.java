/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;

/**
 * Memory-monitoring factory for <code>LinkedRangeDifference</code>.
 *
 * @since 3.0
 */
public class LinkedRangeFactory {

	/**
	 * Exception that is thrown after the minimal allowed free memory is reached.
	 * <p>
	 * This class is not intended to be serialized.
	 * </p>
	 *
	 */
	public static class LowMemoryException extends Exception {

		/**
		 * Serial version UID for this class.
		 * <p>
		 * Note: This class is not intended to be serialized.
		 * </p>
		 * @since 3.1
		 */
		private static final long serialVersionUID= 3977582493823939894L;

		/**
		 * Initialize without detail message.
		 */
		public LowMemoryException() {
			super();
		}

		/**
		 * Initialize with the given detail message.
		 *
		 * @param message the detail message
		 */
		public LowMemoryException(String message) {
			super(message);
		}
	}

	/**
	 * Relative amount of memory that must be free in order to allow the creation of additional instances
	 */
	private static final double THRESHOLD= 0.1;
	/**
	 * Number of instantiations after which the amount of free memory is checked
	 */
	private static final long CHECK_INTERVAL= 5000;
	/**
	 * Considered maximal size of a difference object in bytes.
	 */
	private static final long OBJECT_SIZE= 100;
	/**
	 * The maximal memory requirement for the next round in bytes.
	 */
	private static final long MAXIMAL_INTERVAL_REQUIREMENT= CHECK_INTERVAL * OBJECT_SIZE;
	/**
	 * Allowed memory consumption in bytes.
	 */
	private static final long MAX_MEMORY_CONSUMPTION= 10 * 1024 * 1024;
	/**
	 * The maximal number of instances.
	 */
	private static final long MAX_INSTANCES= MAX_MEMORY_CONSUMPTION /  OBJECT_SIZE;


	/**
	 * Preallocated low memory exception
	 */
	private LowMemoryException fLowMemoryException= new LowMemoryException();

	/**
	 * Number of instantiations
	 */
	private long fCount= 0;

	/**
	 * Create a new linked range difference with the given next range and operation.
	 *
	 * @param next the next linked range difference
	 * @param operation the operation
	 * @return the new linked range difference
	 * @throws LowMemoryException
	 */
	public LinkedRangeDifference newRange(LinkedRangeDifference next, int operation) throws LowMemoryException {
		check();
		return new LinkedRangeDifference(next, operation);
	}

	/**
	 * After <code>CHECK_INTERVAL</code> calls check whether at least a fraction of <code>THRESHOLD</code>
	 * of the maximal available memory is free, otherwise throw an {@link LowMemoryException}.
	 *
	 * @throws LowMemoryException
	 */
	private void check() throws LowMemoryException {
		if (fCount % CHECK_INTERVAL == 0) {

			Runtime runtime= Runtime.getRuntime();
			long maxMemory= runtime.maxMemory();
			long maxFreeMemory= maxMemory - (runtime.totalMemory() - runtime.freeMemory());

			if (((float) (maxFreeMemory - MAXIMAL_INTERVAL_REQUIREMENT)) / maxMemory < THRESHOLD)
				throw fLowMemoryException;
		}
		if (++fCount >= MAX_INSTANCES)
			throw fLowMemoryException;
	}
}
