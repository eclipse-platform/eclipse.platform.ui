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
package org.eclipse.team.examples.filesystem.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.examples.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

public class FileSystemSynchronizeParticipant extends SubscriberParticipant {
	
	private static final String ID = "org.eclipse.team.examples.filesystem.participant"; //$NON-NLS-1$
	
	public FileSystemSynchronizeParticipant(ISynchronizeScope scope) {
		super(scope);
		setSubscriber(FileSystemSubscriber.getInstance());
	}
	
	protected void setSubscriber(Subscriber subscriber) {
		super.setSubscriber(subscriber);
		try {
			ISynchronizeParticipantDescriptor descriptor = TeamUI.getSynchronizeManager().getParticipantDescriptor(ID);
			setInitializationData(descriptor);
			setSecondaryId(Long.toString(System.currentTimeMillis()));
		} catch (CoreException e) {
			// ignore
		}
	}
}
