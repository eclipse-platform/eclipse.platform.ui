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
package org.eclipse.core.filebuffers.manipulation;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 3.1
 */
final class FileBuffersMessages extends NLS {

	private static final String BUNDLE_NAME= FileBuffersMessages.class.getName();

	private FileBuffersMessages() {
		// Do not instantiate
	}

	public static String ConvertLineDelimitersOperation_name;
	public static String ConvertLineDelimitersOperation_task_generatingChanges;
	public static String ConvertLineDelimitersOperation_task_applyingChanges;
	public static String RemoveTrailingWhitespaceOperation_name;
	public static String RemoveTrailingWhitespaceOperation_task_generatingChanges;
	public static String RemoveTrailingWhitespaceOperation_task_applyingChanges;
	public static String FileBufferOperationRunner_task_connecting;
	public static String FileBufferOperationRunner_task_disconnecting;
	public static String FileBufferOperationRunner_task_committing;
	public static String ContainerCreator_task_creatingContainer;
	public static String ContainerCreator_destinationMustBeAContainer;

	static {
		NLS.initializeMessages(BUNDLE_NAME, FileBuffersMessages.class);
	}
}