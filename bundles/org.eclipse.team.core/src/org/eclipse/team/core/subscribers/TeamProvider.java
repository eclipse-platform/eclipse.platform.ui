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
package org.eclipse.team.core.subscribers;

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
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.SaveContext;
import org.eclipse.team.internal.core.SaveContextXMLWriter;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * This is a temporary class that contains methods for TeamSubscriber support. At
 * some time it should be merged into the existing Team plugin class.
 */
public class TeamProvider implements ISaveParticipant {
	
	private static String SUBSCRIBER_EXTENSION = "subscriber";
	final static private String SAVECTX_SUBSCRIBERS = "subscribers"; 
	final static private String SAVECTX_SUBSCRIBER = "subscriber";
	final static private String SAVECTX_QUALIFIER = "qualifier";
	final static private String SAVECTX_LOCALNAME = "localName";
	
	static private Map subscribers = new HashMap();
	static private List listeners = new ArrayList(1);
	static private Map factories = new HashMap();
	
	public TeamProvider() {
		startup();
	}

	static public TeamSubscriber getSubscriber(QualifiedName id) throws TeamException {
		TeamSubscriber s = (TeamSubscriber)subscribers.get(id);
		return s;
	}
			
	static public TeamSubscriber[] getSubscribers() {
		return (TeamSubscriber[])subscribers.values().toArray(
						new TeamSubscriber[subscribers.size()]);
	}
	
	static public void registerSubscriber(TeamSubscriber subscriber) {
		if(! subscribers.containsKey(subscriber.getId())) {
			subscribers.put(subscriber.getId(), subscriber);
			fireTeamResourceChange(new TeamDelta[] {
					new TeamDelta(subscriber, TeamDelta.SUBSCRIBER_CREATED, null)});
		}
	}
	
	static public void deregisterSubscriber(TeamSubscriber subscriber) {
		subscribers.remove(subscriber.getId());
		fireTeamResourceChange(new TeamDelta[] {
				new TeamDelta(subscriber, TeamDelta.SUBSCRIBER_DELETED, null)});
	}
	
	/* (non-Javadoc)
	 * Method declared on IBaseLabelProvider.
	 */
	static public void addListener(ITeamResourceChangeListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * Method declared on IBaseLabelProvider.
	 */
	static public void removeListener(ITeamResourceChangeListener listener) {
		listeners.remove(listener);
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
	static protected void fireTeamResourceChange(final TeamDelta[] deltas) {
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			final ITeamResourceChangeListener l = (ITeamResourceChangeListener) it.next();
			l.teamResourceChanged(deltas);	
		}
	}	
	
	private synchronized static void restoreSubscribers() {
		try {
			SaveContext root = SaveContextXMLWriter.readXMLPluginMetaFile(TeamPlugin.getPlugin(), "subscribers");
			if(root != null && root.getName().equals(SAVECTX_SUBSCRIBERS)) {
				SaveContext[] contexts = root.getChildren();
				for (int i = 0; i < contexts.length; i++) {
					SaveContext context = contexts[i];
					if(context.getName().equals(SAVECTX_SUBSCRIBER)) {
						String qualifier = context.getAttribute(SAVECTX_QUALIFIER);
						String localName = context.getAttribute(SAVECTX_LOCALNAME);
						TeamSubscriberFactory factory = create(qualifier);
						if(factory == null) {
							TeamPlugin.log(new TeamException("Error restoring subscribers. Cannot find factory with id: " + qualifier.toString()).getStatus());
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
			TeamPlugin.log(e.getStatus());
		}
	}

	private synchronized static void saveSubscribers() {
		SaveContext root = new SaveContext();
		root.setName(SAVECTX_SUBSCRIBERS);
		List children = new ArrayList();
		try {
			for (Iterator it = subscribers.values().iterator(); it.hasNext();) {			
				TeamSubscriber subscriber = (TeamSubscriber) it.next();			
				String qualifier = subscriber.getId().getQualifier();
				TeamSubscriberFactory factory = create(qualifier);
				if(factory == null) {
					TeamPlugin.log(new TeamException("Error saving subscribers. Cannot find factory with id: " + qualifier).getStatus());
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
			SaveContextXMLWriter.writeXMLPluginMetaFile(TeamPlugin.getPlugin(), "subscribers", root);
		} catch (TeamException e) {
			TeamPlugin.log(e.getStatus());
		}
	}
	
	private static TeamSubscriberFactory create(String id) {
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
								TeamPlugin.log(e.getStatus());
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
