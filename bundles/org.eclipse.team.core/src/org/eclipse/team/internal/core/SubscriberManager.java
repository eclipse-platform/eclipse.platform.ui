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
package org.eclipse.team.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ISubscriberManager;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.subscribers.TeamSubscriberFactory;

/**
 * This class provides the private implementation of <code>ISubscriberManager</code>.
 */
public class SubscriberManager implements ISubscriberManager, ISaveParticipant {

	private static String SUBSCRIBER_EXTENSION = "subscriber"; //$NON-NLS-1$
	final static private String SAVECTX_SUBSCRIBERS = "subscribers";  //$NON-NLS-1$
	final static private String SAVECTX_SUBSCRIBER = "subscriber"; //$NON-NLS-1$
	final static private String SAVECTX_QUALIFIER = "qualifier"; //$NON-NLS-1$
	final static private String SAVECTX_LOCALNAME = "localName"; //$NON-NLS-1$
	
	private Map subscribers = new HashMap();
	private List listeners = new ArrayList(1);
	private Map factories = new HashMap();
	
	static private ISubscriberManager instance;
	
	public static synchronized ISubscriberManager getInstance() {
		if (instance == null) {
			// Initialize the variable before trigering startup.
			// This is done because the startup code can invoke
			// subscriber factories which, in turn will ask for the 
			// subscriber manager.
			instance = new SubscriberManager();
			((SubscriberManager)instance).startup();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberManager#registerSubscriber(org.eclipse.team.core.subscribers.TeamSubscriber)
	 */
	public void registerSubscriber(TeamSubscriber subscriber) {
		boolean fireEvent = false;
		synchronized(subscribers) {
			if(! subscribers.containsKey(subscriber.getId())) {
				subscribers.put(subscriber.getId(), subscriber);
				fireEvent = true;
			}
		}
		if (fireEvent) {
			fireTeamResourceChange(new TeamDelta[] {
				new TeamDelta(subscriber, TeamDelta.SUBSCRIBER_CREATED, null)});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberManager#deregisterSubscriber(org.eclipse.team.core.subscribers.TeamSubscriber)
	 */
	public void deregisterSubscriber(TeamSubscriber subscriber) {
		boolean fireEvent = false;
		synchronized(subscribers) {
			if (subscribers.remove(subscriber.getId()) != null) {
				// Only notify if the subscriber was registered in the first place
				fireEvent = true;
			}
		}
		if (fireEvent) {
			fireTeamResourceChange(new TeamDelta[] {
				new TeamDelta(subscriber, TeamDelta.SUBSCRIBER_DELETED, null)});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberManager#getSubscriber(org.eclipse.core.runtime.QualifiedName)
	 */
	public TeamSubscriber getSubscriber(QualifiedName id) {
		synchronized(subscribers) {
			TeamSubscriber s = (TeamSubscriber)subscribers.get(id);
			return s;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberManager#getSubscribers()
	 */
	public TeamSubscriber[] getSubscribers() {
		synchronized(subscribers) {
			return (TeamSubscriber[])subscribers.values().toArray(
				new TeamSubscriber[subscribers.size()]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberManager#addTeamResourceChangeListener(org.eclipse.team.core.subscribers.ITeamResourceChangeListener)
	 */
	public void addTeamResourceChangeListener(ITeamResourceChangeListener listener) {
		synchronized(listener) {
			listeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISubscriberManager#removeTeamResourceChangeListener(org.eclipse.team.core.subscribers.ITeamResourceChangeListener)
	 */
	public void removeTeamResourceChangeListener(ITeamResourceChangeListener listener) {
		synchronized(listener) {
			listeners.remove(listener);
		}
	}

	public void doneSaving(ISaveContext context) {
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	public void rollback(ISaveContext context) {
	}

	public void saving(ISaveContext context) throws CoreException {
		// save subscribers during snapshot and at full save
		saveSubscribers();
	}
	
	public void startup() {
		try {
			ResourcesPlugin.getWorkspace().addSaveParticipant(TeamPlugin.getPlugin(), this);
		} catch (CoreException e) {
			TeamPlugin.log(e);
		}
		restoreSubscribers();
	}
	
	/*
	 * Fires a team resource change event to all registered listeners
	 * Only listeners registered at the time this method is called are notified.
	 */
	void fireTeamResourceChange(final TeamDelta[] deltas) {
		ITeamResourceChangeListener[] allListeners;
		// Copy the listener list so we're not calling client code while synchronized
		synchronized(listeners) {
			allListeners = (ITeamResourceChangeListener[]) listeners.toArray(new ITeamResourceChangeListener[listeners.size()]);
		}
		// Fire the events
		for (int i = 0; i < allListeners.length; i++) {
			ITeamResourceChangeListener listener = allListeners[i];
			listener.teamResourceChanged(deltas);	
		}
	}	
	
	synchronized void restoreSubscribers() {
		try {
			SaveContext root = SaveContextXMLWriter.readXMLPluginMetaFile(TeamPlugin.getPlugin(), "subscribers"); //$NON-NLS-1$
			if(root != null && root.getName().equals(SAVECTX_SUBSCRIBERS)) {
				SaveContext[] contexts = root.getChildren();
				for (int i = 0; i < contexts.length; i++) {
					SaveContext context = contexts[i];
					if(context.getName().equals(SAVECTX_SUBSCRIBER)) {
						String qualifier = context.getAttribute(SAVECTX_QUALIFIER);
						String localName = context.getAttribute(SAVECTX_LOCALNAME);
						TeamSubscriberFactory factory = create(qualifier);
						if(factory == null) {
							TeamPlugin.log(new TeamException(Policy.bind("TeamProvider.10", qualifier.toString()))); //$NON-NLS-1$
						}
						SaveContext[] children = context.getChildren();
						if(children.length == 1) {			
							TeamSubscriber s = factory.restoreSubscriber(new QualifiedName(qualifier, localName), children[0]);								
							if(s != null) {
								registerSubscriber(s);
							}
						}
					}
				}
			
			}
		} catch (TeamException e) {
			TeamPlugin.log(e);
		}
	}

	synchronized void saveSubscribers() {
		SaveContext root = new SaveContext();
		root.setName(SAVECTX_SUBSCRIBERS);
		List children = new ArrayList();
		try {
			for (Iterator it = subscribers.values().iterator(); it.hasNext();) {			
				TeamSubscriber subscriber = (TeamSubscriber) it.next();			
				String qualifier = subscriber.getId().getQualifier();
				TeamSubscriberFactory factory = create(qualifier);
				if(factory == null) {
					TeamPlugin.log(new TeamException(Policy.bind("TeamProvider.11", qualifier))); //$NON-NLS-1$
				}
				SaveContext child = factory.saveSubscriber(subscriber);
				if(child != null) { 
					SaveContext item = new SaveContext();				
					item.putChild(child);
					item.setName(SAVECTX_SUBSCRIBER);
					Map attributes = new HashMap();
					attributes.put(SAVECTX_QUALIFIER, subscriber.getId().getQualifier());
					attributes.put(SAVECTX_LOCALNAME, subscriber.getId().getLocalName());
					item.setAttributes(attributes);				
					children.add(item);
				}
			}
			root.setChildren((SaveContext[])children.toArray(new SaveContext[children.size()]));
			SaveContextXMLWriter.writeXMLPluginMetaFile(TeamPlugin.getPlugin(), "subscribers", root); //$NON-NLS-1$
		} catch (TeamException e) {
			TeamPlugin.log(e);
		}
	}
	
	private TeamSubscriberFactory create(String id) {
		TeamSubscriberFactory sFactory = (TeamSubscriberFactory)factories.get(id);
		if(sFactory != null) {
			return sFactory;
		}
		
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(SUBSCRIBER_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						try {
							//Its ok not to have a typeClass extension.  In this case, a default instance will be created.
							if(configElements[j].getAttribute("class") != null) { //$NON-NLS-1$
								sFactory = (TeamSubscriberFactory) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							}
							factories.put(sFactory.getID(), sFactory);
							return sFactory;
							} catch (CoreException e) {
								TeamPlugin.log(e);
							} catch (ClassCastException e) {
								String className = configElements[j].getAttribute("class"); //$NON-NLS-1$
								TeamPlugin.log(IStatus.ERROR, Policy.bind("RepositoryProviderType.invalidClass", id.toString(), className), e); //$NON-NLS-1$
							}
						return null;
					}
				}
			}		
		}
		return null;
	}	
}
