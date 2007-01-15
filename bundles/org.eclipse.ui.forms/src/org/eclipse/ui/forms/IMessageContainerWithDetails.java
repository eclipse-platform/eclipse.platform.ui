/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.forms;

/**
 * <p>
 * This interface should be implemented by message containers that are capable
 * of showing messages that have summary and details. These containers can
 * choose to use the provided merged detailed message or construct their own
 * from the provided list of message objects.
 * </p>
 * <p>
 * Detailed text message needs to be provided to simplify rendering for simpler
 * containers. Clients and containers that know about each other can use
 * IMessage objects and their application data fields to pass additional
 * information that can be rendered in more interesting ways.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UA team.
 * </p>
 * 
 * @since 3.3
 */
public interface IMessageContainerWithDetails extends IMessageContainer {

	/**
	 * Sets the message for this container with an indication of what type of
	 * message it is.
	 * <p>
	 * The valid message types are one of <code>NONE</code>,
	 * <code>INFORMATION</code>,<code>WARNING</code>, or
	 * <code>ERROR</code>.
	 * </p>
	 * <p>
	 * Optionally, use the provided array of message objects to show more
	 * detailed information, in which case the message itself should be used as
	 * a summary.
	 * 
	 * @param summary
	 *            the summary message, or <code>null</code> to clear the
	 *            message
	 * @param details
	 *            the merged list of individual messages or <code>null</code>
	 *            if the summary message is sufficient
	 * @param messages
	 *            an array of individual messages grouped by the summary message
	 *            or <code>null</code> if not available. If <code>null</code>,
	 *            <code>summary</code> should be used for the message text.
	 * @param newType
	 *            the message type
	 */
	public void setMessage(String summary, String details, IMessage[] messages,
			int newType);
}