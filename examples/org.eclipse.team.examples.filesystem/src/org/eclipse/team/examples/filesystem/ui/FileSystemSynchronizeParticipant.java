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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.examples.filesystem.subscriber.FileSystemSubscriber;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantDescriptor;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;


/**
 * This is an example synchronize participant for the file system provider. It will allow
 * showing synchronization state for local resources mapped to a remote file system
 * location.
 * 
 * @since 3.0
 */
public class FileSystemSynchronizeParticipant extends SubscriberParticipant {
	
	private static final String ID = "org.eclipse.team.examples.filesystem.participant"; //$NON-NLS-1$
	
	/**
	 * A custom label decorator that will show the remote mapped path for each
	 * file.
	 */
	private class FileSystemParticipantLabelDecorator extends LabelProvider implements ILabelDecorator {

		public Image decorateImage(Image image, Object element) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelDecorator#decorateText(java.lang.String, java.lang.Object)
		 */
		public String decorateText(String text, Object element) {
			try {
				if (element instanceof ISynchronizeModelElement) {
					IResource resource = ((ISynchronizeModelElement) element).getResource();
					if (resource != null) {
						SyncInfo info = FileSystemSubscriber.getInstance().getSyncInfo(resource);
						IResourceVariant variant = info.getRemote();
						if (variant != null) {
							return text + " (" + variant.getContentIdentifier() + ")";
						}
					}
				}
			} catch (TeamException e) {
			}
			return null;
		}
	}
	
	public FileSystemSynchronizeParticipant(ISynchronizeScope scope) {
		super(scope);
		setSubscriber(FileSystemSubscriber.getInstance());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.Subscriber)
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		
		ILabelDecorator labelDecorator = new FileSystemParticipantLabelDecorator();
		configuration.addLabelDecorator(labelDecorator);
		
		// Add support for showing mode buttons
		configuration.setSupportedModes(ISynchronizePageConfiguration.ALL_MODES);
		configuration.setMode(ISynchronizePageConfiguration.BOTH_MODE);
	}
}
