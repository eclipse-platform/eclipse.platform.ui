/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.IConfigurationElementConstants;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.ILaunchHistoryChangedListener;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Manages UI related launch configuration artifacts
 * 
 * Since 3.3 the Launch Configuration Manager is an <code>ISaveParticipant</code>, allowing it to participate in
 * workspace persistence life-cycles.
 * 
 * @see ISaveParticipant
 * @see org.eclipse.debug.ui.ILaunchShortcut
 * @see ILaunchGroup
 * @see ILaunchListener
 * @see ILaunchHistoryChangedListener
 * @see DebugUIPlugin
 * @see LaunchHistory
 */
public class LaunchConfigurationManager implements ILaunchListener, ISaveParticipant {
	/**
	 * A comparator for the ordering of launch shortcut extensions
	 * @since 3.3
	 */
	class ShortcutComparator implements Comparator {
		/**
		 * @see Comparator#compare(Object, Object)
		 */
		public int compare(Object a, Object b) {
			LaunchShortcutExtension shorcutA = (LaunchShortcutExtension)a;
			String labelA = shorcutA.getLabel();
			String pathA = shorcutA.getMenuPath();
			LaunchShortcutExtension shortcutB = (LaunchShortcutExtension)b;
			String labelB = shortcutB.getLabel();
			String pathB = shortcutB.getMenuPath();
			
			// group by path, then sort by label
			// a null path sorts last (i.e. highest)
			if (nullOrEqual(pathA, pathB)) {
				// null labels sort last (i.e. highest)
				if (labelA == labelB) {
					return 0;
				}
				if (labelA == null) {
					return 1;
				}
				if (labelB == null) {
					return -1;
				}
				return labelA.compareToIgnoreCase(labelB);
			}
			// compare paths
			if (pathA == null) {
				return 1;
			}
			if (pathB == null) {
				return -1;
			}
			return pathA.compareToIgnoreCase(pathB);
		}
		
		private boolean nullOrEqual(String a, String b) {
			if (a == null) {
				return b == null;
			}
			return a.equals(b);
		}

	}
	
	/**
	 * Launch group extensions, keyed by launch group identifier.
	 */
	protected Map fLaunchGroups;
	
	/**
	 * Launch histories keyed by launch group identifier
	 */	
	protected Map fLaunchHistories;
		
	/**
	 * The list of registered implementors of <code>ILaunchHistoryChangedListener</code>
	 */
	protected List fLaunchHistoryChangedListeners = new ArrayList(3);	

	/**
	 * Launch shortcuts
	 */
	private List fLaunchShortcuts = null;
	
	/**
	 * Launch shortcuts, cached by perspective ids
	 */
	private Map fLaunchShortcutsByPerspective = null;
		
	/**
	 * Cache of launch configuration tab images with error overlays
	 */
	protected ImageRegistry fErrorImages = null;
	
	/**
	 * true when restoring launch history
	 */
	protected boolean fRestoring = false;
		
	/**
	 * The name of the file used to persist the launch history.
	 */
	private static final String LAUNCH_CONFIGURATION_HISTORY_FILENAME = "launchConfigurationHistory.xml"; //$NON-NLS-1$
	
	/**
	 * performs initialization of the manager when it is started 
	 */
	public void startup() {				
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);	
		DebugUIPlugin.getDefault().addSaveParticipant(this);
		//update histories for launches already registered
		ILaunch[] launches = launchManager.getLaunches();
		for (int i = 0; i < launches.length; i++) {
			launchAdded(launches[i]);
		}
	}
	
	/**
	 * Returns whether any launch config supports the given mode.
	 * 
	 * @param mode launch mode
	 * @return whether any launch config supports the given mode
	 */
	public boolean launchModeAvailable(String mode) {
		return ((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).launchModeAvailable(mode);
	}
	
	/**
	 * Returns whether the given launch configuration should be visible in the
	 * debug UI. If the config is marked as private, or belongs to a different
	 * category (i.e. non-null), then this configuration should not be displayed
	 * in the debug UI.
	 * 
	 * @param launchConfiguration the configuration to check for the {@link IDebugUIConstants#ATTR_PRIVATE} attribute
	 * @return boolean
	 */
	public static boolean isVisible(ILaunchConfiguration launchConfiguration) {
		try {
			return !(launchConfiguration.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false));
		} catch (CoreException e) {
		}
		return false;
	}
	
	/**
	 * Returns a collection of launch configurations that does not contain
	 * configurations from disabled activities.
	 * 
	 * @param configurations a collection of configurations
	 * @return the given collection minus any configurations from disabled activities
	 */
	public static ILaunchConfiguration[] filterConfigs(ILaunchConfiguration[] configurations) {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		if (activitySupport == null) {
			return configurations;
		}
		List filteredConfigs = new ArrayList();
		ILaunchConfigurationType type = null;
		LaunchConfigurationTypeContribution contribution = null;
		ILaunchConfiguration configuration = null;
		for (int i = 0; i < configurations.length; i++) {
			configuration = configurations[i];
			try {
				type = configuration.getType();
				contribution = new LaunchConfigurationTypeContribution(type);
				if (DebugUIPlugin.doLaunchConfigurationFiltering(configuration) & !WorkbenchActivityHelper.filterItem(contribution)) {
					filteredConfigs.add(configuration);
				}
			} 
			catch (CoreException e) {DebugUIPlugin.log(e.getStatus());}
		}
		return (ILaunchConfiguration[]) filteredConfigs.toArray(new ILaunchConfiguration[filteredConfigs.size()]);
	}

	/**
	 * Returns a listing of <code>IlaunchDeleagtes</code> that does not contain any delegates from disabled activities
	 * @param type the type to get the delegates from
	 * @param modes the set of launch modes to get delegates for
	 * @return the filtered listing of <code>ILaunchDelegate</code>s or an empty array, never <code>null</code>.
	 * @throws CoreException if an exception occurs
	 * @since 3.3
	 */
	public static ILaunchDelegate[] filterLaunchDelegates(ILaunchConfigurationType type, Set modes) throws CoreException {
		IWorkbenchActivitySupport as = PlatformUI.getWorkbench().getActivitySupport();
		ILaunchDelegate[] delegates = type.getDelegates(modes);
		if(as == null) {
			return delegates;
		}
		HashSet set = new HashSet();
		for(int i = 0; i < delegates.length; i++) {
			//filter by capabilities
			if(!WorkbenchActivityHelper.filterItem(new LaunchDelegateContribution(delegates[i]))) {
				set.add(delegates[i]);
			}
		}
		return (ILaunchDelegate[]) set.toArray(new ILaunchDelegate[set.size()]);
	}
	
	/**
	 * Performs cleanup operations when the manager is being disposed of. 
	 */
	public void shutdown() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
		if (fLaunchHistories != null) {
			Iterator histories = fLaunchHistories.values().iterator();
			while (histories.hasNext()) {
				LaunchHistory history = (LaunchHistory)histories.next();
				history.dispose();
			}
		}
		DebugUIPlugin.getDefault().removeSaveParticipant(this);
	}
	
	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {}
	
	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {}

	/**
	 * Must not assume that will only be called from the UI thread.
	 *
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(final ILaunch launch) {
		removeTerminatedLaunches(launch);
	}
	
	/**
	 * Removes terminated launches from the launch view, leaving the specified launch in the view
	 * @param newLaunch the newly added launch to leave in the view
	 */
	protected void removeTerminatedLaunches(ILaunch newLaunch) {
	    if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES)) {
	        ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
	        Object[] launches= lManager.getLaunches();
	        for (int i= 0; i < launches.length; i++) {
	            ILaunch launch= (ILaunch)launches[i];
	            if (launch != newLaunch && launch.isTerminated()) {
	                lManager.removeLaunch(launch);
	            }
	        }
	    }
	}
	
	/**
	 * Returns the most recent launch for the given group, or <code>null</code>
	 * if none. This method does not include any filtering for the returned launch configuration.
	 *	
	 * This method is exposed via DebugTools.getLastLaunch
	 * @param groupId the identifier of the {@link ILaunchGroup} to get the last launch from
	 *
	 * @return the last launch, or <code>null</code> if none
	 */	
	public ILaunchConfiguration getLastLaunch(String groupId) {
		LaunchHistory history = getLaunchHistory(groupId);
		if (history != null) {
			return history.getRecentLaunch();
		}
		return null;
	}
	
	/**
	 * Returns the most recent launch for the given group taking launch configuration
	 * filters into account, or <code>null</code> if none.
	 * 
	 * @param groupId launch group
	 * @return the most recent, un-filtered launch
	 */
	public ILaunchConfiguration getFilteredLastLaunch(String groupId) {
		LaunchHistory history = getLaunchHistory(groupId);
		if (history != null) {
			ILaunchConfiguration[] filterConfigs = history.getCompleteLaunchHistory();
			if (filterConfigs.length > 0) {
				return filterConfigs[0];
			}
		}
		return null;
	}
	
	/**
	 * Add the specified listener to the list of listeners that will be notified when the
	 * launch history changes.
	 * @param listener the listener to add - adding a duplicate listener has no effect
	 */
	public void addLaunchHistoryListener(ILaunchHistoryChangedListener listener) {
		if (!fLaunchHistoryChangedListeners.contains(listener)) {
			fLaunchHistoryChangedListeners.add(listener);
		}
	}
	
	/**
	 * Remove the specified listener from the list of listeners that will be notified when the
	 * launch history changes.
	 * @param listener the listener to remove
	 */
	public void removeLaunchHistoryListener(ILaunchHistoryChangedListener listener) {
		fLaunchHistoryChangedListeners.remove(listener);
	}
	
	/**
	 * Notify all launch history listeners that the launch history has changed in some way.
	 */
	protected void fireLaunchHistoryChanged() {
		Iterator iterator = fLaunchHistoryChangedListeners.iterator();
		ILaunchHistoryChangedListener listener = null;
		while (iterator.hasNext()) {
			listener = (ILaunchHistoryChangedListener) iterator.next();
			listener.launchHistoryChanged();
		}
	}

	/**
	 * Returns the history listing as XML
	 * @return the history listing as XML
	 * @throws CoreException if an exception occurs
	 * @throws ParserConfigurationException if there is a problem creating the XML for the launch history
	 */
	protected String getHistoryAsXML() throws CoreException, ParserConfigurationException {
		Document doc = DebugUIPlugin.getDocument();
		Element historyRootElement = doc.createElement(IConfigurationElementConstants.LAUNCH_HISTORY); 
		doc.appendChild(historyRootElement);
		
		Iterator histories = fLaunchHistories.values().iterator();
		LaunchHistory history = null;
		while (histories.hasNext()) {
			history = (LaunchHistory)histories.next();
			Element groupElement = doc.createElement(IConfigurationElementConstants.LAUNCH_GROUP);
			groupElement.setAttribute(IConfigurationElementConstants.ID, history.getLaunchGroup().getIdentifier());
			historyRootElement.appendChild(groupElement);
			Element historyElement = doc.createElement(IConfigurationElementConstants.MRU_HISTORY);
			groupElement.appendChild(historyElement);
			createEntry(doc, historyElement, history.getCompleteLaunchHistory());
			Element favs = doc.createElement(IConfigurationElementConstants.FAVORITES);
			groupElement.appendChild(favs);
			createEntry(doc, favs, history.getFavorites());
			history.setSaved(true);
		}
		return DebugPlugin.serializeDocument(doc);
	}

	/**
	 * Creates a new launch history element and adds it to the specified <code>Document</code>
	 * @param doc the <code>Document</code> to add the new element to
	 * @param historyRootElement the root element
	 * @param configurations the configurations to create entries for
	 * @throws CoreException is an exception occurs
	 */
	protected void createEntry(Document doc, Element historyRootElement, ILaunchConfiguration[] configurations) throws CoreException {
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.exists()) {
				Element launch = doc.createElement(IConfigurationElementConstants.LAUNCH);
				launch.setAttribute(IConfigurationElementConstants.MEMENTO, configuration.getMemento());
				historyRootElement.appendChild(launch);
			}
		}
	}
				
	/**
	 * Returns the path to the local file for the launch history
	 * @return the file path for the launch history file
	 */
	protected IPath getHistoryFilePath() {
		return DebugUIPlugin.getDefault().getStateLocation().append(LAUNCH_CONFIGURATION_HISTORY_FILENAME); 
	}

	/**
	 * Write out an XML file indicating the entries on the run & debug history lists and
	 * the most recent launch.
	 * @throws IOException if writing the history file fails
	 * @throws CoreException is an exception occurs
	 * @throws ParserConfigurationException if there is a problem reading the XML
	 */
	protected void persistLaunchHistory() throws IOException, CoreException, ParserConfigurationException {
		synchronized (this) {
			if (fLaunchHistories == null || fRestoring) {
				return;
			}			
		}
		boolean shouldsave = false;
		for(Iterator iter = fLaunchHistories.values().iterator(); iter.hasNext();) {
			shouldsave |= ((LaunchHistory)iter.next()).needsSaving();
		}
		if(shouldsave) {
			IPath historyPath = getHistoryFilePath();
			String osHistoryPath = historyPath.toOSString();
			String xml = getHistoryAsXML();
			File file = new File(osHistoryPath);
			file.createNewFile();
			
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
			stream.close();
		}
	}
	
	/**
	 * Find the XML history file and parse it.  Place the corresponding configurations
	 * in the appropriate history, and set the most recent launch.
	 */
	private void restoreLaunchHistory() {
		// Find the history file
		IPath historyPath = getHistoryFilePath();
		String osHistoryPath = historyPath.toOSString();
		File file = new File(osHistoryPath);
		// If no history file, nothing to do
		if (!file.exists()) {
			return;
		}
		InputStream stream= null;
		Element rootHistoryElement= null;
		try {
			// Parse the history file
			stream = new BufferedInputStream(new FileInputStream(file));
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
				rootHistoryElement = parser.parse(new InputSource(stream)).getDocumentElement();
			} catch (SAXException e) {
				DebugUIPlugin.log(e);
				return;
			} catch (ParserConfigurationException e) {
				DebugUIPlugin.log(e);
				return;
			} finally {
				stream.close();
			}
		} catch (IOException exception) {
			DebugUIPlugin.log(exception);
			return;
		}	
		// If root node isn't what we expect, return
		if (!rootHistoryElement.getNodeName().equalsIgnoreCase(IConfigurationElementConstants.LAUNCH_HISTORY)) { 
			return;
		}
		// For each child of the root node, construct a launch config handle and add it to
		// the appropriate history, or set the most recent launch
		Collection l = fLaunchHistories.values();
		LaunchHistory[] histories = (LaunchHistory[])l.toArray(new LaunchHistory[l.size()]);
		NodeList list = rootHistoryElement.getChildNodes();
		int length = list.getLength();
		Node node = null;
		Element entry = null;
		for (int i = 0; i < length; ++i) {
			node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(IConfigurationElementConstants.LAUNCH)) { 
					createHistoryElement(entry, histories, false);
				} else if (entry.getNodeName().equalsIgnoreCase(IConfigurationElementConstants.LAST_LAUNCH)) {
					createHistoryElement(entry, histories, true);
				} else if (entry.getNodeName().equals(IConfigurationElementConstants.LAUNCH_GROUP)) {
					String id = entry.getAttribute(IConfigurationElementConstants.ID);
					if (id != null) {
						LaunchHistory history = getLaunchHistory(id);
						if (history != null) { 
							restoreHistory(entry, history);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Restores the given launch history.
	 * 
	 * @param groupElement launch group history
	 * @param history associated history cache
	 */
	private void restoreHistory(Element groupElement, LaunchHistory history) {
		NodeList nodes = groupElement.getChildNodes();
		int length = nodes.getLength();
		for (int i = 0; i < length; i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				if (element.getNodeName().equals(IConfigurationElementConstants.MRU_HISTORY)) {
					ILaunchConfiguration[] configs = getLaunchConfigurations(element);
					for (int j = 0; j < configs.length; j++) {
						history.addHistory(configs[j], false);
					}
				} else if (element.getNodeName().equals(IConfigurationElementConstants.FAVORITES)) {
					ILaunchConfiguration[] favs = getLaunchConfigurations(element);
					history.setFavorites(favs);
					// add any favorites that have been added to the workspace before this plug-in
					// was loaded - @see bug 231600
					ILaunchConfiguration[] configurations = getLaunchManager().getLaunchConfigurations();
					for (int j = 0; j < configurations.length; j++) {
						history.checkFavorites(configurations[j]);
					}
				}
			}
		}
	}
	
	/** 
	 * Restores a list of configurations.
	 * @param root element
	 * @return list of configurations under the element
	 */
	private ILaunchConfiguration[] getLaunchConfigurations(Element root) {
		List configs = new ArrayList();
		NodeList nodes = root.getChildNodes();
		int length = nodes.getLength();
		for (int i = 0; i < length; i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				if (element.getNodeName().equals(IConfigurationElementConstants.LAUNCH)) {
					String memento = element.getAttribute(IConfigurationElementConstants.MEMENTO); 
					if (memento != null) {
						try {
							ILaunchConfiguration configuration = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(memento);
							//touch the config to see if its type exists
							configuration.getType();
							if (configuration.exists()) {
								configs.add(configuration);
							}
						} catch (CoreException e) {
							//do nothing as we don't care about non-existent, or configs with no type
						}
					}
				}
			}
		}
		return (ILaunchConfiguration[]) configs.toArray(new ILaunchConfiguration[configs.size()]);
	}
	
	/**
	 * Construct a launch configuration corresponding to the specified XML
	 * element, and place it in the appropriate history.
	 * @param entry the XML entry to read from
	 * @param histories the array of histories to try and add the restored configurations to
	 * @param prepend if any restored items should be added to to top of the launch history
	 */
	private void createHistoryElement(Element entry, LaunchHistory[] histories, boolean prepend) {
		String memento = entry.getAttribute(IConfigurationElementConstants.MEMENTO); 
		String mode = entry.getAttribute(IConfigurationElementConstants.MODE);     
		try {
			ILaunchConfiguration launchConfig = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(memento);
			//touch the type to see if its type exists
			launchConfig.getType();
			if (launchConfig.exists()) {
				LaunchHistory history = null;
				for (int i = 0; i < histories.length; i++) {
					history = histories[i];
					if (history.accepts(launchConfig) && history.getLaunchGroup().getMode().equals(mode)) {
						history.addHistory(launchConfig, prepend);
					}
				}
			}
		} catch (CoreException e) {
			//do nothing, as we want to throw away invalid launch history entries silently
		}	
	}
	
	/**
	 * Load all registered extensions of the 'launch shortcut' extension point.
	 */
	private synchronized void loadLaunchShortcuts() {
		if(fLaunchShortcuts == null) {
			// Get the configuration elements
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_LAUNCH_SHORTCUTS);
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
	
			// Load the configuration elements into a Map 
			fLaunchShortcuts = new ArrayList(infos.length);
			for (int i = 0; i < infos.length; i++) {
				fLaunchShortcuts.add(new LaunchShortcutExtension(infos[i]));
			}
			Collections.sort(fLaunchShortcuts, new ShortcutComparator());
		}
	}
	
	/**
	 * Load all registered extensions of the 'launch groups' extension point.
	 */
	private synchronized void loadLaunchGroups() {
		if (fLaunchGroups == null) {
			// Get the configuration elements
			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_LAUNCH_GROUPS);
			IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
	
			// Load the configuration elements into a Map 
			fLaunchGroups = new HashMap(infos.length);
			LaunchGroupExtension ext = null;
			for (int i = 0; i < infos.length; i++) {
				ext = new LaunchGroupExtension(infos[i]);
				fLaunchGroups.put(ext.getIdentifier(), ext);
			}
		}
	}	
	
	/**
	 * Returns all launch shortcuts
	 * 
	 * @return all launch shortcuts
	 */
	public List getLaunchShortcuts() {
		if (fLaunchShortcuts == null) {
			loadLaunchShortcuts();
		}
		return fLaunchShortcuts;
	}

	/**
	 * Creates a listing of the launch shortcut extensions that are applicable to the underlying resource
	 * @param resource the underlying resource
	 * @return a listing of applicable launch shortcuts or an empty list, never <code>null</code>
	 * @since 3.3
	 */
	public List getLaunchShortcuts(IResource resource) {
		List list = new ArrayList(); 
		List sc = getLaunchShortcuts();
		List ctxt = new ArrayList();
		if(resource != null) {
			ctxt.add(resource);
		}
		IEvaluationContext context = DebugUIPlugin.createEvaluationContext(ctxt);
		context.addVariable("selection", ctxt); //$NON-NLS-1$
		LaunchShortcutExtension ext = null;
		for(Iterator iter = sc.iterator(); iter.hasNext();) {
			ext = (LaunchShortcutExtension) iter.next();
			try {
				if(ext.evalEnablementExpression(context, ext.getContextualLaunchEnablementExpression()) && !WorkbenchActivityHelper.filterItem(ext)) {
					if(!list.contains(ext)) {
						list.add(ext);
					}
				}
			}
			catch(CoreException ce) {/*do nothing*/}
		}
		return list;
	}
	
	/**
	 * Returns an array of all of the ids of the <code>ILaunchConfigurationType</code>s that apply to the currently
	 * specified <code>IResource</code>.
	 * 
	 * @param resource the resource context
	 * @return an array of applicable <code>ILaunchConfigurationType</code>  ids, or an empty array, never <code>null</code>
	 * @since 3.3
	 * CONTEXTLAUNCHING
	 */
	public String[] getApplicableConfigurationTypes(IResource resource) {
		List types = new ArrayList();
		List exts = getLaunchShortcuts();
		LaunchShortcutExtension ext = null;
		List list = new ArrayList();
		list.add(resource);
		IEvaluationContext context = DebugUIPlugin.createEvaluationContext(list);
		context.setAllowPluginActivation(true);
		context.addVariable("selection", list); //$NON-NLS-1$
		HashSet set = new HashSet();
		for(Iterator iter = exts.listIterator(); iter.hasNext();) {
			ext = (LaunchShortcutExtension) iter.next();
			try {
				if(ext.evalEnablementExpression(context, ext.getContextualLaunchEnablementExpression())) {
					set.addAll(ext.getAssociatedConfigurationTypes());
				}
			}
			catch(CoreException ce) {
				IStatus status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), "Launch shortcut '" + ext.getId() + "' enablement expression caused exception. Shortcut was removed.", ce); //$NON-NLS-1$ //$NON-NLS-2$
				DebugUIPlugin.log(status);
				iter.remove();
			}
		}
		LaunchManager lm = (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = null;
		for(Iterator iter = set.iterator(); iter.hasNext();) {
			type = lm.getLaunchConfigurationType((String)iter.next());
			if(type != null) { 
				if(!types.contains(type) && type.isPublic() && !"org.eclipse.ui.externaltools.builder".equals(type.getCategory())) { //$NON-NLS-1$
					types.add(type.getIdentifier());
				}
			}
		}
		return (String[]) types.toArray(new String[types.size()]);
	}
	
	/**
	 * Returns an array of the <code>ILaunchConfiguration</code>s that apply to the specified <code>IResource</code>
	 * @param types the array of launch configuration type identifiers
	 * @param resource the resource
	 * @return an array of applicable <code>ILaunchConfiguration</code>s for the specified <code>IResource</code> or an empty 
	 * array if none, never <code>null</code>
	 * @since 3.3
	 */
	public ILaunchConfiguration[] getApplicableLaunchConfigurations(String[] types, IResource resource) {
		ArrayList list = new ArrayList();
		try {
			if(resource != null) {
				String[] ctypes = types;
				if(ctypes == null) {
					ctypes = getApplicableConfigurationTypes(resource);
				}
				//copy into collection for hashcode matching
				HashSet typeset = new HashSet(ctypes.length);
				for(int i = 0; i < ctypes.length; i++) {
					typeset.add(ctypes[i]);
				}
				ILaunchConfiguration[] configurations = filterConfigs(getLaunchManager().getLaunchConfigurations());
				ILaunchConfiguration configuration = null;
				IResource[] resrcs = null;
				for(int i = 0; i < configurations.length; i++) {
					configuration = configurations[i];
					if(typeset.contains(configuration.getType().getIdentifier()) && acceptConfiguration(configuration)) {
						resrcs = configuration.getMappedResources();
						if (resrcs != null) {
							for (int j = 0; j < resrcs.length; j++) {
								if (resource.equals(resrcs[j]) || resource.getFullPath().isPrefixOf(resrcs[j].getFullPath())) {
									list.add(configuration);
									break;
								}
							}
						}
						else {
							//in the event the config has no mapping
							list.add(configuration);
						}
					}
				}
			}
		} catch (CoreException e) {
			list.clear();
			DebugPlugin.log(e);
		}
		return (ILaunchConfiguration[]) list.toArray(new ILaunchConfiguration[list.size()]);
	}
	
	/**
	 * Returns if the specified configuration should be considered as a potential candidate
	 * @param config to configuration
	 * @return if the specified configuration should be considered as a potential candidate
	 * @throws CoreException if an exception occurs
	 */
	private boolean acceptConfiguration(ILaunchConfiguration config) throws CoreException {
		if(config != null && !DebugUITools.isPrivate(config)) {
			if(!"org.eclipse.ui.externaltools".equals(config.getType().getCategory())) { //$NON-NLS-1$
				return true;
			}
			else {
				IResource[] res = config.getMappedResources();
				if(res != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns all launch shortcuts for the given category
	 * @param category the identifier of the category
	 *
	 * @return all launch shortcuts
	 */
	public List getLaunchShortcuts(String category) {
		return filterShortcuts(getLaunchShortcuts(), category);
	}	
	
	/**
	 * Return a list of filtered launch shortcuts, based on the given category.
	 *  
	 * @param unfiltered the raw list of shortcuts to filter
	 * @param category the category to filter by
	 * @return List
	 */
	protected List filterShortcuts(List unfiltered, String category) {
		List filtered = new ArrayList(unfiltered.size());
		Iterator iter = unfiltered.iterator();
		LaunchShortcutExtension extension = null;
		while (iter.hasNext()){
			extension = (LaunchShortcutExtension)iter.next();
			if (category == null) {
				if (extension.getCategory() == null) {
					filtered.add(extension);
				}
			} else if (category.equals(extension.getCategory())){
				filtered.add(extension);
			}
		}
		return filtered;		
	}
	
	/**
	 * Returns all launch shortcuts defined for the given perspective,
	 * empty list if none.
	 * 
	 * @param perpsective perspective identifier
	 * @param category the category for the shortcut
	 * @return all launch shortcuts defined for the given perspective,
	 * empty list if none.
	 * @deprecated the use of perspectives for launch shortcuts has been 
	 * deprecated since 3.1, use a contextualLaunch element instead
	 */
	public List getLaunchShortcuts(String perpsective, String category) {
		if (fLaunchShortcutsByPerspective == null) {
			Iterator shortcuts = getLaunchShortcuts().iterator();
			fLaunchShortcutsByPerspective = new HashMap(10);
			LaunchShortcutExtension ext = null;
			Iterator perspectives = null;
			while (shortcuts.hasNext()) {
				ext = (LaunchShortcutExtension)shortcuts.next();
				perspectives = ext.getPerspectives().iterator();
				while (perspectives.hasNext()) {
					String id = (String)perspectives.next();
					List list = (List)fLaunchShortcutsByPerspective.get(id);
					if (list == null) {
						list = new ArrayList(4);
						fLaunchShortcutsByPerspective.put(id, list);
					}
					list.add(ext);
				}
			}
		}
		List list = (List)fLaunchShortcutsByPerspective.get(perpsective); 
		if (list == null) {
			return new ArrayList();
		} 
		return filterShortcuts(list, category);
	}
	
	/**
	 * Returns the first occurrence of any one of the configurations in the provided list, if they are found in the launch history
	 * for the corresponding launch group
	 * @param configurations the raw list of configurations to examine
	 * @param group the launch group to get the launch history from
	 * @param resource the {@link IResource} context
	 * @return the associated launch configuration from the MRU listing or <code>null</code> if there isn't one
	 * @since 3.3
	 */
	public ILaunchConfiguration getMRUConfiguration(List configurations, ILaunchGroup group, IResource resource) {
		if(group != null) {
			ArrayList candidates = new ArrayList();
			LaunchHistory history = getLaunchHistory(group.getIdentifier());
			if(history != null) {
				ILaunchConfiguration[] configs = history.getCompleteLaunchHistory();
				for(int i = 0; i < configs.length; i++) {
					if(configurations.contains(configs[i])) {
						if(resource instanceof IContainer) {
							return configs[i];
						}
						else {
							candidates.add(configs[i]);
						}
					}
				}
				ILaunchConfiguration config = null;
				if(resource != null) {
					//first try to find a config that exactly matches the resource mapping, and collect partial matches
					IResource[] res = null;
					for(Iterator iter = candidates.iterator(); iter.hasNext();) {
						config = (ILaunchConfiguration) iter.next();
						try {
							res = config.getMappedResources();
							if(res != null) {
								for(int i = 0; i < res.length; i++) {
									if(res[i].equals(resource)) {
										return config;
									}
								}
							}
						}
						catch(CoreException ce) {}
					}
				}
				for(int i = 0; i < configs.length; i++) {
					if(candidates.contains(configs[i])) {
						return configs[i];
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the shared config from the selected resource or <code>null</code> if the selected resources is not a shared config
	 * @param receiver the object to test if it is a shared launch configuration
	 * @return the shared config from the selected resource or <code>null</code> if the selected resources is not a shared config
	 * @since 3.3
	 */
	public ILaunchConfiguration isSharedConfig(Object receiver) {
		if(receiver instanceof IFile) {
			IFile file = (IFile) receiver;
			String ext = file.getFileExtension();
			if(ext == null) {
				return null;
			}
			if(ext.equals("launch")) { //$NON-NLS-1$
				ILaunchConfiguration config = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(file);
				if(config != null && config.exists()) {
					return config;
				}
			}
		}
		else if(receiver instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) receiver;
			return isSharedConfig(input.getFile());
		}
		else if(receiver instanceof IEditorPart) {
			return isSharedConfig(((IEditorPart) receiver).getEditorInput());
		}
		else if (receiver instanceof IAdaptable) {
			IFile file = (IFile) ((IAdaptable)receiver).getAdapter(IFile.class);
			if (file != null) {
				return isSharedConfig(file);
			}
		}
		return null;
	}
	
	/**
	 * Returns the image used to display an error in the given tab
	 * @param tab the tab to get the error image for
	 * @return the error image associated with the given tab
	 */
	public Image getErrorTabImage(ILaunchConfigurationTab tab) {
		if (fErrorImages == null) {
			fErrorImages = new ImageRegistry();
		}
		String key = tab.getClass().getName();
		Image image = fErrorImages.get(key);
		if (image == null) {
			// create image
			Image base = tab.getImage();
			if (base == null) {
				base = DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OVR_TRANSPARENT);
			}
			base = new Image(Display.getCurrent(), base, SWT.IMAGE_COPY);
			LaunchConfigurationTabImageDescriptor desc = new LaunchConfigurationTabImageDescriptor(base, LaunchConfigurationTabImageDescriptor.ERROR);
			image = desc.createImage();
			fErrorImages.put(key, image);
		}
		return image;
	}
	
	/**
	 * Return the launch group with the given id, or <code>null</code>
	 * @param id the identifier of the {@link LaunchGroupExtension}
	 * 
	 * @return the launch group with the given id, or <code>null</code>
	 */
	public LaunchGroupExtension getLaunchGroup(String id) {
		if (fLaunchGroups == null) {
			loadLaunchGroups();
		}
		return (LaunchGroupExtension)fLaunchGroups.get(id);
	}
	
	/**
	 * Return all defined launch groups
	 * 
	 * @return all defined launch groups
	 */
	public ILaunchGroup[] getLaunchGroups() {
		if (fLaunchGroups == null) {
			loadLaunchGroups();
		}
		Collection groups = fLaunchGroups.values();
		return (ILaunchGroup[])groups.toArray(new ILaunchGroup[groups.size()]);
	}	
	
	/**
	 * Return the launch history with the given group id, or <code>null</code>
	 * @param id the identifier of the launch history 
	 * @return the launch history with the given group id, or <code>null</code>
	 */
	public LaunchHistory getLaunchHistory(String id) {
		loadLaunchHistories();
		return (LaunchHistory)fLaunchHistories.get(id);
	}	
	
	/**
	 * Returns the singleton instance of the launch manager
	 * @return the singleton instance of the launch manager
	 * @since 3.3
	 */
	private LaunchManager getLaunchManager() {
		return (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Restore launch history
	 */
	private synchronized void loadLaunchHistories() {
		if (fLaunchHistories == null) {
			fRestoring = true;
			ILaunchGroup[] groups = getLaunchGroups();
			fLaunchHistories = new HashMap(groups.length);
			ILaunchGroup extension = null;
			for (int i = 0; i < groups.length; i++) {
				extension = groups[i];
				if (extension.isPublic()) {
					fLaunchHistories.put(extension.getIdentifier(), new LaunchHistory(extension));
				}
			}
			restoreLaunchHistory();
			fRestoring = false;
		}
	}
	
	/**
	 * Returns the default launch group for the given mode.
	 * 
	 * @param mode the mode identifier
	 * @return launch group
	 */
	public LaunchGroupExtension getDefaultLaunchGroup(String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return getLaunchGroup(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		}
		return getLaunchGroup(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
	}
	
	/**
	 * Returns the launch group the given launch configuration type belongs to, in
	 * the specified mode, or <code>null</code> if none.
	 * 
	 * @param type the type
	 * @param mode the mode
	 * @return the launch group the given launch configuration belongs to, in
	 * the specified mode, or <code>null</code> if none
	 */
	public ILaunchGroup getLaunchGroup(ILaunchConfigurationType type, String mode) {
		if (!type.supportsMode(mode)) {
			return null;
		}
		String category = type.getCategory();
		ILaunchGroup[] groups = getLaunchGroups();
		ILaunchGroup extension = null;
		for (int i = 0; i < groups.length; i++) {
			extension = groups[i];
			if (category == null) {
				if (extension.getCategory() == null && extension.getMode().equals(mode)) {
					return extension;
				}
			} else if (category.equals(extension.getCategory())) {
				if (extension.getMode().equals(mode)) {
					return extension;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link ILaunchGroup} for the given mode set and 
	 * {@link ILaunchConfigurationType}.
	 * @param type the type
	 * @param modeset the set of modes, which are combined to one mode string
	 * @return the associated {@link ILaunchGroup} or <code>null</code>
	 * 
	 * @since 3.4.0
	 */
	public ILaunchGroup getLaunchGroup(ILaunchConfigurationType type, Set modeset) {
		StringBuffer buff = new StringBuffer();
		Object item = null;
		for(Iterator iter = modeset.iterator(); iter.hasNext();) {
			item = iter.next();
			if(item instanceof String) {
				buff.append(item);
				if(iter.hasNext()) {
					buff.append(","); //$NON-NLS-1$
				}
			}
		}
		return getLaunchGroup(type, buff.toString());
	}
	
	/**
	 * Returns the private launch configuration used as a place-holder to represent/store
	 * the information associated with a launch configuration type.
	 * 
	 * @param type launch configuration type
	 * @return launch configuration
	 * @throws CoreException if an excpetion occurs
	 * @since 3.0
	 */
	public static ILaunchConfiguration getSharedTypeConfig(ILaunchConfigurationType type) throws CoreException {
		String id = type.getIdentifier();
		String name = id + ".SHARED_INFO"; //$NON-NLS-1$
		ILaunchConfiguration shared = null;
		ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
		ILaunchConfiguration configuration = null;
		for (int i = 0; i < configurations.length; i++) {
			configuration = configurations[i];
			if (configuration.getName().equals(name)) {
				shared = configuration;
				break;
			}
		}
		
		if (shared == null) {
			// create a new shared config
			ILaunchConfigurationWorkingCopy workingCopy;
			workingCopy = type.newInstance(null, name);
			workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
			// null entries indicate default settings
			// save
			shared = workingCopy.doSave();
		}
		return shared;
	}


	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {}

	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {}

	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {}

	/**
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
		try {
			persistLaunchHistory();
		}  catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), "Internal error saving launch history", e)); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), "Internal error saving launch history", e)); //$NON-NLS-1$
		} 
	}
	
	/**
	 * Sets the given launch to be the most recent launch in the launch
	 * history (for applicable histories).
	 * <p>
	 * @param launch the launch to prepend to its associated histories 
	 * @since 3.3
	 */
	public void setRecentLaunch(ILaunch launch) {
		ILaunchGroup[] groups = DebugUITools.getLaunchGroups();
		int size = groups.length;
		for (int i = 0; i < size; i++) {
			String id = groups[i].getIdentifier();
			LaunchHistory history = getLaunchHistory(id);
			if (history != null)
				history.launchAdded(launch);
		}
	}	

}
