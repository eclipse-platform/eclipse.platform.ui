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
package org.eclipse.team.ui.synchronize;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.registry.SynchronizeParticipantDescriptor;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

/**
 * AbstractSynchronizeParticipant is the abstract base class for all
 * synchronize view participants. It provides default lifecycle support
 * for participants.
 * 
 * @see ISynchronizeParticipant
 * @since 3.0
 */
public abstract class AbstractSynchronizeParticipant implements ISynchronizeParticipant {
	// property listeners
	private ListenerList fListeners;

	private String fName;
	private String fId;
	private ImageDescriptor fImageDescriptor;
	protected IConfigurationElement configElement;

	/**
	 * Notifies listeners of property changes, handling any exceptions
	 */
	class PropertyNotifier implements ISafeRunnable {

		private IPropertyChangeListener fListener;
		private PropertyChangeEvent fEvent;

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			TeamUIPlugin.log(IStatus.ERROR, Policy.bind("AbstractSynchronizeParticipant.5"), exception); //$NON-NLS-1$
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fListener.propertyChange(fEvent);
		}

		/**
		 * Notifies listeners of the property change
		 * 
		 * @param property
		 *            the property that has changed
		 */
		public void notify(PropertyChangeEvent event) {
			if (fListeners == null) {
				return;
			}
			fEvent = event;
			Object[] copiedListeners = fListeners.getListeners();
			for (int i = 0; i < copiedListeners.length; i++) {
				fListener = (IPropertyChangeListener) copiedListeners[i];
				Platform.run(this);
			}
			fListener = null;
		}
	}

	public AbstractSynchronizeParticipant() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsole#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsole#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return fImageDescriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.ui.sync.ISynchronizeParticipant#getId()
	 */
	public String getId() {
		return fId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsole#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList();
		}
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsole#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (fListeners != null) {
			fListeners.remove(listener);
		}
	}

	/**
	 * Notify all listeners that the given property has changed.
	 * 
	 * @param source
	 *            the object on which a property has changed
	 * @param property
	 *            identifier of the property that has changed
	 * @param oldValue
	 *            the old value of the property, or <code>null</code>
	 * @param newValue
	 *            the new value of the property, or <code>null</code>
	 */
	public void firePropertyChange(Object source, String property, Object oldValue, Object newValue) {
		if (fListeners == null) {
			return;
		}
		PropertyNotifier notifier = new PropertyNotifier();
		notifier.notify(new PropertyChangeEvent(source, property, oldValue, newValue));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		//	Save config element.
		configElement = config;

		// Id
		fId = config.getAttribute("id"); //$NON-NLS-1$

		// Title.
		fName = config.getAttribute("name"); //$NON-NLS-1$
		if (config == null) {
			fName = "Unknown"; //$NON-NLS-1$
		}

		// Icon.
		String strIcon = config.getAttribute("icon"); //$NON-NLS-1$
		if (strIcon != null) {
			fImageDescriptor = TeamImages.getImageDescriptorFromExtension(configElement.getDeclaringExtension(), strIcon);
		}
	}
	
	protected void setInitializationData(ISynchronizeParticipantDescriptor descriptor) throws CoreException {
		if(descriptor instanceof SynchronizeParticipantDescriptor) {
			setInitializationData(((SynchronizeParticipantDescriptor)descriptor).getConfigurationElement(), null, null);
		} else {
			throw new TeamException(Policy.bind("AbstractSynchronizeParticipant.4")); //$NON-NLS-1$
		}
	}

	/**
	 * Sets the name of this console to the specified value and notifies
	 * property listeners of the change.
	 * 
	 * @param name
	 *            the new name
	 */
	protected void setName(String name) {
		String old = fName;
		fName = name;
		firePropertyChange(this, IBasicPropertyConstants.P_TEXT, old, name);
	}
	
	/**
	 * Sets the image descriptor for this console to the specified value and
	 * notifies property listeners of the change.
	 * 
	 * @param imageDescriptor
	 *            the new image descriptor
	 */
	protected void setImageDescriptor(ImageDescriptor imageDescriptor) {
		ImageDescriptor old = fImageDescriptor;
		fImageDescriptor = imageDescriptor;
		firePropertyChange(this, IBasicPropertyConstants.P_IMAGE, old, imageDescriptor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(IMemento memento) throws PartInitException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
	}
}