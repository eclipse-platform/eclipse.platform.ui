/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;
 
/**
 * Extension interface for <code>IDocument</code>.  <p>
 * It adds configurability to the mechanism of  post notification replaces and allows to
 * postpone document listener notification.
 * 
 * @since 2.1
 */
public interface IDocumentExtension2 {
		
	/**
	 * Tells the receiver to ignore calls to
	 * <code>registerPostNotificationReplace</code> until
	 * <code>acceptPostNotificationReplaces</code> is called.
	 */
	void ignorePostNotificationReplaces();
	
	/**
	 * Tells the receiver to accept calls to
	 * <code>registerPostNotificationReplace</code> until
	 * <code>ignorePostNotificationReplaces</code> is called.
	 */
	void acceptPostNotificationReplaces();
	
	/**
	 * Can be called prior to a <code>replace</code> operation. After the
	 * <code>replace</code> <code>resumeListenerNotification</code> must be
	 * called. The affect of these calls is that no document listener is notified
	 * until <code>resumeListenerNotification</code> is called. This allows clients
	 * to update structure before any listener is informed about the change.<p>
	 * Listener notification can only be stopped for a single document manipulation.
	 * Otherwise, document change notifications will be lost.
	 */
	void stopListenerNotification();
	
	/**
	 * Resumes the notification of document listeners which must previously
	 * have been stopped by a call to <code>stopListenerNotification</code>. 
	 */
	void resumeListenerNotification();
}
