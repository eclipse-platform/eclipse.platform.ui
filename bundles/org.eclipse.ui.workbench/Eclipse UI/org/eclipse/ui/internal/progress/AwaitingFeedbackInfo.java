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
package org.eclipse.ui.internal.progress;

import org.eclipse.ui.progress.UIJob;

/**
 * An AwaitingFeedbackInfo is a simple structure for keeping
 * track of a message and UIJob to run.
 */
public class AwaitingFeedbackInfo {

    private String message;

    private UIJob job;

    public AwaitingFeedbackInfo(String infoMessage, UIJob infoJob) {
        this.message = infoMessage;
        this.job = infoJob;
    }

    /**
     * Return the job for the receiver.
     * @return
     */
    public UIJob getJob() {
        return this.job;
    }

    /**
     * Return the message for the receiver.
     * @return
     */
    public String getMessage() {
        return this.message;
    }

}