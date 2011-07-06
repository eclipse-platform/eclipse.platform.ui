/*******************************************************************************
 *  Copyright (c) 2004, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software Systems - Mikhail Khodjaiants - Bug 88232
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.internal.core.sourcelookup.SourceLookupMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.text.MessageFormat;

/**
 * Directs source lookup among a collection of source lookup participants,
 * and a common collection of source containers.
 * Each source lookup participant is a source locator itself, which allows
 * more than one source locator to participate in source lookup for a
 * launch. Each source lookup participant searches for source in the source
 * containers managed by this director, and each participant is notified
 * of changes in the source containers (i.e. when the set of source
 * containers changes).
 * <p>
 * When a source director is initialized, it adds it self as a launch listener,
 * and automatically disposes itself when its associated launch is removed
 * from the launch manager. If a source director is instantiated by a client
 * that is not part of a launch, that client is responsible for disposing
 * the source director.
 * </p>
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.0
 * @see org.eclipse.debug.core.model.ISourceLocator
 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer
 * @see org.eclipse.debug.core.sourcelookup.ISourceContainerType
 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputer
 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant
 */
public abstract class AbstractSourceLookupDirector implements ISourceLookupDirector, ILaunchConfigurationListener, ILaunchListener {
	
	// source locator type identifier
	protected String fId;
	//ISourceLocatorParticipants that are listening for container changes
	protected ArrayList fParticipants = new ArrayList();
	//list of current source containers
	protected ISourceContainer[] fSourceContainers = null;
	//the launch config associated with this director
	protected ILaunchConfiguration fConfig; 
	//whether duplicates should be searched for or not
	protected boolean fDuplicates = false;
	// source path computer, or null if default
	protected ISourcePathComputer fComputer = null;
	/**
	 * Cache of resolved source elements when duplicates exist.
	 * Keys are the duplicates, values are the source element to use.
	 */
	protected Map fResolvedElements = null;
	// current participant performing lookup or <code>null</code>
	private ISourceLookupParticipant fCurrentParticipant;
	
	protected static final IStatus fPromptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	protected static final IStatus fResolveDuplicatesStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 205, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	// XML nodes & attributes for persistence
	protected static final String DIRECTOR_ROOT_NODE = "sourceLookupDirector"; //$NON-NLS-1$
	protected static final String CONTAINERS_NODE = "sourceContainers"; //$NON-NLS-1$
	protected static final String DUPLICATES_ATTR = "duplicates"; //$NON-NLS-1$
	protected static final String CONTAINER_NODE = "container"; //$NON-NLS-1$
	protected static final String CONTAINER_TYPE_ATTR = "typeId"; //$NON-NLS-1$
	protected static final String CONTAINER_MEMENTO_ATTR = "memento"; //$NON-NLS-1$
	  
	class SourceLookupQuery implements ISafeRunnable {
		
		private List fSourceElements = new ArrayList();
		private Object fElement = null;
		private Throwable fException = null;
		
		SourceLookupQuery(Object element) {
			fElement = element;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			fException = exception;
		}
		
		/**
		 * Returns any exception that occurred during source lookup.
		 * 
		 * @return the (any) exception that occured during source lookup
		 */
		public Throwable getException() {
			return fException;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			MultiStatus multiStatus = null;
			CoreException single = null;
			ISourceLookupParticipant[] participants = getParticipants();
			try {
				for(int i=0; i < participants.length; i++) {
					setCurrentParticipant(participants[i]);
					Object[] sourceArray;
					try {
						sourceArray = participants[i].findSourceElements(fElement);
						if (sourceArray !=null && sourceArray.length > 0) {
							if (isFindDuplicates()) {
								for(int j=0; j<sourceArray.length; j++)
									if(!checkDuplicate(sourceArray[j], fSourceElements))
										fSourceElements.add(sourceArray[j]);
							} else {
								fSourceElements.add(sourceArray[0]);
								return;
							}
						}
					} catch (CoreException e) {
						if (single == null) {
							single = e;
						} else if (multiStatus == null) {
							multiStatus = new MultiStatus(DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, new IStatus[]{single.getStatus()}, SourceLookupMessages.Source_Lookup_Error, null); 
							multiStatus.add(e.getStatus());
						} else {
							multiStatus.add(e.getStatus());
						}
					}
				}
			} finally {
				setCurrentParticipant(null);
			}
			if (fSourceElements.isEmpty()) {
				// set exception if there was one
				if (multiStatus != null) {
					fException = new CoreException(multiStatus);
				} else if (single != null) {
					fException = single;
				}
			}
		}
		
		public List getSourceElements() {
			return fSourceElements;
		}

		public void dispose() {
			fElement = null;
			fSourceElements = null;
			fException = null;
		}
		
	}
	
	/**
	 * Constructs source lookup director
	 */
	public AbstractSourceLookupDirector() {
	}
	
	/**
	 * Sets the type identifier for this source locator's type
	 *  
	 * @param id corresponds to source locator type identifier for a
	 *  persistable source locator 
	 */
	public void setId(String id) {
		fId = id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2#dispose()
	 */
	public synchronized void dispose() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchConfigurationListener(this);
		launchManager.removeLaunchListener(this);
		Iterator iterator = fParticipants.iterator();
		while (iterator.hasNext()) {
			ISourceLookupParticipant participant = (ISourceLookupParticipant) iterator.next();
			//director may also be a participant
			if(participant != this)
				participant.dispose();
		}
		fParticipants.clear();
		if (fSourceContainers != null) {
			for (int i = 0; i < fSourceContainers.length; i++) {
				fSourceContainers[i].dispose();
			}
		}
		fSourceContainers = null;
		fResolvedElements = null;
	}
	
	/**
	 * Throws an exception with the given message and underlying exception.
	 * 
	 * @param message error message
	 * @param exception underlying exception, or <code>null</code>
	 * @throws CoreException if a problem is encountered
	 */
	protected void abort(String message, Throwable exception) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.ERROR, message, exception);
		throw new CoreException(status);
	}
	
	/**
	 * Constructs source containers from a list of container mementos.
	 * 
	 * @param list the list of nodes to be parsed
	 * @exception CoreException if parsing encounters an error
	 * @return a list of source containers
	 */
	private List parseSourceContainers(NodeList list) throws CoreException {
		List containers = new ArrayList();
		for (int i=0; i < list.getLength(); i++) {
			if(!(list.item(i).getNodeType() == Node.ELEMENT_NODE))
 				continue;
			Element element = (Element)list.item(i);
			String typeId = element.getAttribute(CONTAINER_TYPE_ATTR);
			if (typeId == null || typeId.equals("")) {	 //$NON-NLS-1$
				abort(SourceLookupMessages.AbstractSourceLookupDirector_11, null); 
			}
			ISourceContainerType type = DebugPlugin.getDefault().getLaunchManager().getSourceContainerType(typeId);
			if(type != null) {		
				String memento = element.getAttribute(CONTAINER_MEMENTO_ATTR);
				if (memento == null || memento.equals("")) {	 //$NON-NLS-1$
					abort(SourceLookupMessages.AbstractSourceLookupDirector_13, null); 
				}
				ISourceContainer container = type.createSourceContainer(memento);
				containers.add(container);
			}
			else {
				abort(MessageFormat.format(SourceLookupMessages.AbstractSourceLookupDirector_12, new String[]{typeId}), null); 
			}	
		}	
		return containers;
	}
	
	/**
	 * Registers the given source lookup participant. Has no effect if an identical
	 * participant is already registered. Participants receive notification
	 * when the source containers associated with this source director change. 
	 * 
	 * @param participant the participant to register
	 */
	private synchronized void addSourceLookupParticipant(ISourceLookupParticipant participant) {
		if (!fParticipants.contains(participant)) {
			fParticipants.add(participant);
			participant.init(this);
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#getSourceContainers()
	 */
	public synchronized ISourceContainer[] getSourceContainers() {
		if (fSourceContainers == null) {
			return new ISourceContainer[0];
		}
		ISourceContainer[] copy = new ISourceContainer[fSourceContainers.length];
		System.arraycopy(fSourceContainers, 0, copy, 0, fSourceContainers.length);
		return copy;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#isFindDuplicates()
	 */
	public boolean isFindDuplicates() {			
		return fDuplicates;			
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#setFindDuplicates(boolean)
	 */
	public void setFindDuplicates(boolean duplicates) {			
		fDuplicates = duplicates;			
	}	
	
	/**
	 * Removes the given participant from the list of registered participants.
	 * Has no effect if an identical participant is not already registered.
	 * 
	 * @param participant the participant to remove
	 */
	private synchronized void removeSourceLookupParticipant(ISourceLookupParticipant participant) {
		if (fParticipants.remove(participant)) {
			participant.dispose();
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		ILaunchConfiguration from = DebugPlugin.getDefault().getLaunchManager().getMovedFrom(configuration);
		if (from != null && from.equals(getLaunchConfiguration())) {
			fConfig = configuration;
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * Updates source containers in response to changes in underlying launch
	 * configuration. Only responds to changes in non-working copies.
	 * 
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (fConfig == null || configuration.isWorkingCopy()) {
			return;
		}
		if(fConfig.equals(configuration)) {
			try{
				String locatorMemento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO,(String)null);
				if (locatorMemento == null) {
					initializeDefaults(configuration);
				} else {
					initializeFromMemento(locatorMemento, configuration);
				}
			} catch (CoreException e){
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		if (configuration.equals(getLaunchConfiguration())) {
			if (DebugPlugin.getDefault().getLaunchManager().getMovedTo(configuration) == null) {
				fConfig = null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#getMemento()
	 */
	public synchronized String getMemento() throws CoreException {
		Document doc = DebugPlugin.newDocument();
		Element rootNode = doc.createElement(DIRECTOR_ROOT_NODE);
		doc.appendChild(rootNode);
				
		Element pathNode = doc.createElement(CONTAINERS_NODE);		
		if(fDuplicates) {
			pathNode.setAttribute(DUPLICATES_ATTR, "true"); //$NON-NLS-1$
		} else {
			pathNode.setAttribute(DUPLICATES_ATTR, "false"); //$NON-NLS-1$
		}
		rootNode.appendChild(pathNode);
		if(fSourceContainers !=null){
			for(int i=0; i<fSourceContainers.length; i++){
				Element node = doc.createElement(CONTAINER_NODE);
				ISourceContainer container = fSourceContainers[i];
				ISourceContainerType type = container.getType();
				node.setAttribute(CONTAINER_TYPE_ATTR, type.getId());
				node.setAttribute(CONTAINER_MEMENTO_ATTR, type.getMemento(container));
				pathNode.appendChild(node);
			}
		}
		return DebugPlugin.serializeDocument(doc);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeFromMemento(java.lang.String)
	 */
	public void initializeFromMemento(String memento) throws CoreException {
	    doInitializeFromMemento(memento, true);
	}
	
	/**
	 * Initializes this source lookup director from the given memento.
	 * Disposes itself before initialization if specified.
	 * 
	 * @param memento source locator memento
	 * @param dispose whether to dispose any current source containers and participants
	 *  before initializing
	 * @throws CoreException if an exception occurs during initialization
	 * @since 3.1
	 */
	protected void doInitializeFromMemento(String memento, boolean dispose) throws CoreException {
	    if (dispose) {
	        dispose();
	    }
		Element rootElement = DebugPlugin.parseDocument(memento);		
		if (!rootElement.getNodeName().equalsIgnoreCase(DIRECTOR_ROOT_NODE)) { 
			abort(SourceLookupMessages.AbstractSourceLookupDirector_14, null); 
		}
		NodeList list = rootElement.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if(entry.getNodeName().equalsIgnoreCase(CONTAINERS_NODE)){
					setFindDuplicates("true".equals(entry.getAttribute(DUPLICATES_ATTR))); //$NON-NLS-1$
					NodeList children = entry.getChildNodes();
					List containers = parseSourceContainers(children);
					setSourceContainers((ISourceContainer[]) containers.toArray(new ISourceContainer[containers.size()]));
				}
			}
		}
		initializeParticipants();
	}	
	
	/**
	 * Sets the source containers used by this source lookup
	 * director.
	 * 
	 * @param containers source containers to search
	 */
	public void setSourceContainers(ISourceContainer[] containers) {
		synchronized (this) {
			List list = Arrays.asList( containers );
			ISourceContainer[] old = getSourceContainers();
			for (int i = 0; i < old.length; i++) {
				// skip overlapping containers
				if (!list.contains(old[i]))
					old[i].dispose();
			}
			fSourceContainers = containers;
			for (int i = 0; i < containers.length; i++) {
				ISourceContainer container = containers[i];
				container.init(this);
			}
		}
		// clear resolved duplicates
		fResolvedElements = null;
		// notify participants
		ISourceLookupParticipant[] participants = getParticipants();
		for (int i = 0; i < participants.length; i++) {
			ISourceLookupParticipant participant = participants[i];
			participant.sourceContainersChanged(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISourceLocator#getSourceElement(org.eclipse.debug.core.model.IStackFrame)
	 * Would be better to accept Object so this can be used for breakpoints and other objects.
	 */
	public Object getSourceElement(IStackFrame stackFrame) {
		return getSourceElement((Object)stackFrame);
	}
	
	/**
	 * Performs a source lookup query for the given element
	 * returning the source elements associated with the element.
	 * 
	 * @param element stack frame
	 * @return list of associated source elements
	 */
	protected List doSourceLookup(Object element) {
		SourceLookupQuery query = new SourceLookupQuery(element);
		SafeRunner.run(query);
		List sources = query.getSourceElements();
		Throwable exception = query.getException();
		if (exception != null) {
			if (exception instanceof CoreException) {
				CoreException ce = (CoreException) exception;
				if (ce.getStatus().getSeverity() == IStatus.ERROR) {
					DebugPlugin.log(ce);
				}
			} else {
				DebugPlugin.log(exception);
			}
		}
		query.dispose();
		return sources;
	}
	
	/**
	 * Returns the source element to associate with the given element.
	 * This method is called when more than one source element has been found
	 * for an element, and allows the source director to select a single
	 * source element to associate with the element.
	 * <p>
	 * Subclasses should override this method as appropriate. For example,
	 * to prompt the user to choose a source element.
	 * </p>
	 * @param element the debug artifact for which source is being searched for
	 * @param sources the source elements found for the given element
	 * @return a single source element for the given element
	 */
	public Object resolveSourceElement(Object element, List sources) {
		// check the duplicates cache first
		Iterator duplicates = sources.iterator();
		while (duplicates.hasNext()) {
			Object dup = duplicates.next();
			Object resolved = getCachedElement(dup);
			if (resolved != null) {
				return resolved;
			}
		}
		// consult a status handler
		IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(fPromptStatus);
		if (prompter != null) {
			try {
				Object result = prompter.handleStatus(fResolveDuplicatesStatus, new Object[]{element, sources});
				if (result != null) {
					cacheResolvedElement(sources, result);
					return result;
				}
			} catch (CoreException e) {
			}
		}
		return sources.get(0);
	}

	/**
	 * Checks if the object being added to the list of sources is a duplicate of what's already in the list
	 * @param sourceToAdd the new source file to be added 
	 * @param sources the list that the source will be compared against
	 * @return true if it is already in the list, false if it is a new object
	 */
	private boolean checkDuplicate(Object sourceToAdd, List sources){
		if(sources.size() == 0)
			return false;
		Iterator iterator = sources.iterator();
		while(iterator.hasNext())
			if(iterator.next().equals(sourceToAdd))
				return true;
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2#initializeFromMemento(java.lang.String, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFromMemento(String memento, ILaunchConfiguration configuration) throws CoreException {
	    dispose();
		setLaunchConfiguration(configuration);
		doInitializeFromMemento(memento, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IPersistableSourceLocator#initializeDefaults(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
		dispose();
		setLaunchConfiguration(configuration);
		setSourceContainers(new ISourceContainer[]{new DefaultSourceContainer()});
		initializeParticipants();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#getLaunchConfiguration()
	 */
	public ILaunchConfiguration getLaunchConfiguration() {
		return fConfig;
	}
	
	/**
	 * Sets the launch configuration associated with this source lookup
	 * director. If the given configuration is a working copy, this director
	 * will respond to changes the working copy. If the given configuration
	 * is a persisted launch configuration, this director will respond to changes
	 * in the persisted launch configuration.
	 * 
	 * @param configuration launch configuration to associate with this
	 *  source lookup director, or <code>null</code> if none
	 */
	protected void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fConfig = configuration;
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchConfigurationListener(this);
		launchManager.addLaunchListener(this);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
		if (this.equals(launch.getSourceLocator())) {
			dispose();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#getParticipants()
	 */
	public synchronized ISourceLookupParticipant[] getParticipants() {
		return (ISourceLookupParticipant[]) fParticipants.toArray(new ISourceLookupParticipant[fParticipants.size()]);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#supportsSourceContainerType(org.eclipse.debug.core.sourcelookup.ISourceContainerType)
	 */
	public boolean supportsSourceContainerType(ISourceContainerType type) {
		return true;
	}
	
	/**
	 * Caches the resolved source element to use when one of the following
	 * duplicates is found.
	 * 
	 * @param duplicates duplicates source elements
	 * @param sourceElement chosen source element to use in place of the
	 *  duplicates
	 */
	protected void cacheResolvedElement(List duplicates, Object sourceElement) {
		if (fResolvedElements == null) {
			fResolvedElements = new HashMap(10);
		}
		Iterator iterator = duplicates.iterator();
		while (iterator.hasNext()) {
			Object dup = iterator.next();
			fResolvedElements.put(dup, sourceElement);
		}
		
	}
	
	/**
	 * Returns the cached source element to use when the given duplicate
	 * is encountered.
	 * 
	 * @param duplicate duplicates source element
	 * @return element to use in the duplicate's place
	 */
	protected Object getCachedElement(Object duplicate) {
		if (fResolvedElements != null) {
			return fResolvedElements.get(duplicate);
		}
		return null;
	}
	
	/**
	 * Clears any cached source element associated with the given duplicate
	 * is source element.
	 * 
	 * @param duplicate duplicate source element to cache resolved results
	 *  for
	 */
	protected void clearCachedElement(Object duplicate) {
		if (fResolvedElements != null) {
			fResolvedElements.remove(duplicate);
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#clearSourceElements(java.lang.Object)
	 */
	public void clearSourceElements(Object element) {
		List list = doSourceLookup(element);
		if (list.size() > 0) {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				clearCachedElement(iterator.next());
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#addParticipants(org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant[])
	 */
	public void addParticipants(ISourceLookupParticipant[] participants) {
		for (int i = 0; i < participants.length; i++) {
			ISourceLookupParticipant participant = participants[i];
			addSourceLookupParticipant(participant);
			participant.sourceContainersChanged(this);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#removeParticipants(org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant[])
	 */
	public void removeParticipants(ISourceLookupParticipant[] participants) {
		for (int i = 0; i < participants.length; i++) {
			removeSourceLookupParticipant(participants[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#getId()
	 */
	public String getId() {
		return fId;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#getSourcePathComputer()
	 */
	public ISourcePathComputer getSourcePathComputer() {
		if (fComputer == null && getLaunchConfiguration() != null) {
			try {
				return DebugPlugin.getDefault().getLaunchManager().getSourcePathComputer(getLaunchConfiguration());
			} catch (CoreException e) {
			}
		}
		return fComputer;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#setSourcePathComputer(org.eclipse.debug.core.sourcelookup.ISourcePathComputer)
	 */
	public void setSourcePathComputer(ISourcePathComputer computer) {
		fComputer = computer;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#findSourceElements(java.lang.Object)
	 */
	public Object[] findSourceElements(Object object) throws CoreException {
		SourceLookupQuery query = new SourceLookupQuery(object);
		SafeRunner.run(query);
		List sources = query.getSourceElements();
		Throwable exception = query.getException();
		query.dispose();
		if (exception != null && sources.isEmpty()) {
			if (exception instanceof CoreException) {
				throw (CoreException)exception;
			}
			abort(SourceLookupMessages.AbstractSourceLookupDirector_10, exception);
		}
		return sources.toArray();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#getSourceElement(java.lang.Object)
	 */
	public Object getSourceElement(Object element) {
		List sources = doSourceLookup(element);
		if(sources.size() == 1) {
			return sources.get(0);
		} else if(sources.size() > 1) {
			return resolveSourceElement(element, sources);
		} else { 
			return null;
		}
	}
	
	/**
	 * Sets the current participant or <code>null</code> if none.
	 * 
	 * @param participant active participant or <code>null</code>
	 */
	private void setCurrentParticipant(ISourceLookupParticipant participant) {
		fCurrentParticipant = participant;
	}
	
	/**
	 * Returns the participant currently looking up source or <code>null</code>
	 * if none.
	 * 
	 * @return the participant currently looking up source or <code>null</code>
	 * if none
	 * @since 3.5
	 */
	public ISourceLookupParticipant getCurrentParticipant() {
		return fCurrentParticipant;
	}
}
