/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.ILaunchHistoryChangedListener;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
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

public class LaunchConfigurationManager implements ILaunchListener {
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
	 * A set containing the launch modes supported by
	 * current configurations.
	 */
	private Set fLoadedModes = null;
		
	/**
	 * The name of the file used to persist the launch history.
	 */
	private static final String LAUNCH_CONFIGURATION_HISTORY_FILENAME = "launchConfigurationHistory.xml"; //$NON-NLS-1$
	
	/**
	 * The 'HISTORY_' fields are constants that represent node & attribute names used when
	 * writing out the launch history XML file.
	 */
	private static final String HISTORY_ROOT_NODE = "launchHistory"; //$NON-NLS-1$
	private static final String HISTORY_LAUNCH_NODE = "launch"; //$NON-NLS-1$
	private static final String HISTORY_LAST_LAUNCH_NODE = "lastLaunch"; //$NON-NLS-1$
	private static final String HISTORY_MEMENTO_ATT = "memento"; //$NON-NLS-1$
	private static final String HISTORY_MODE_ATT = "mode"; //$NON-NLS-1$
	
	public void startup() {				
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);	

		//update histories for launches already registered
		ILaunch[] launches= launchManager.getLaunches();
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
		if (fLoadedModes == null) {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType[] types = launchManager.getLaunchConfigurationTypes();
			ILaunchMode[] modes = launchManager.getLaunchModes();
			fLoadedModes = new HashSet(3);
			for (int i = 0; i < types.length; i++) {
				ILaunchConfigurationType type = types[i];
				for (int j = 0; j < modes.length; j++) {
					ILaunchMode launchMode = modes[j];
					if (type.supportsMode(launchMode.getIdentifier())) {
						fLoadedModes.add(launchMode.getIdentifier());
					}
				}
			}
		}
		return fLoadedModes.contains(mode);
	}
	
	/**
	 * Returns whether the given launch configuraiton should be visible in the
	 * debug ui. If the config is marked as private, or belongs to a different
	 * category (i.e. non-null), then this configuration should not be displayed
	 * in the debug ui.
	 * 
	 * @param launchConfiguration
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
	 * configs from disabled activities.
	 * 
	 * @param configurations a collection of configurations
	 * @return the given collection minus any configurations from disabled activities
	 */
	public static ILaunchConfiguration[] filterConfigs(ILaunchConfiguration[] configurations) {
		IWorkbenchActivitySupport activitySupport= PlatformUI.getWorkbench().getActivitySupport();
		if (activitySupport == null) {
			return configurations;
		}

		List filteredConfigs= new ArrayList();
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			ILaunchConfigurationType type= null;
			try {
				type = configuration.getType();
				LaunchConfigurationTypeContribution contribution = new LaunchConfigurationTypeContribution(type);
				if (DebugUIPlugin.doLaunchConfigurationFiltering(configuration) & !WorkbenchActivityHelper.filterItem(contribution)) {
					filteredConfigs.add(configuration);
				}
			} 
			catch (CoreException e) {DebugUIPlugin.log(e.getStatus());}
		}
		return (ILaunchConfiguration[]) filteredConfigs.toArray(new ILaunchConfiguration[filteredConfigs.size()]);
	}
	
	public void shutdown() {
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
		if (fLaunchHistories != null) {
			Iterator histories = fLaunchHistories.values().iterator();
			while (histories.hasNext()) {
				LaunchHistory history = (LaunchHistory)histories.next();
				history.dispose();
			}
		}
	}
	
	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}
	
	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {	

	}

	/**
	 * Must not assume that will only be called from the UI thread.
	 *
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(final ILaunch launch) {
		removeTerminatedLaunches(launch);
	}
	
	protected void removeTerminatedLaunches(final ILaunch newLaunch) {
		if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES)) {
		    Job launchRemovalJob = new Job(LaunchConfigurationsMessages.LaunchConfigurationManager_0) { 
		        protected IStatus run(IProgressMonitor monitor) {
		            ILaunchManager lManager= DebugPlugin.getDefault().getLaunchManager();
					Object[] launches= lManager.getLaunches();
					for (int i= 0; i < launches.length; i++) {
						ILaunch launch= (ILaunch)launches[i];
						if (launch != newLaunch && launch.isTerminated()) {
							lManager.removeLaunch(launch);
						}
					}
					return Status.OK_STATUS;
		        }
		        
		    };
		    launchRemovalJob.setPriority(Job.DECORATE);
		    launchRemovalJob.schedule();
		}
	}
	
	/**
	 * Returns the most recent launch for the given group, or <code>null</code>
	 * if none.
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
	 * Add the specified listener to the list of listeners that will be notified when the
	 * launch history changes.
	 */
	public void addLaunchHistoryListener(ILaunchHistoryChangedListener listener) {
		if (!fLaunchHistoryChangedListeners.contains(listener)) {
			fLaunchHistoryChangedListeners.add(listener);
		}
	}
	
	/**
	 * Remove the specified listener from the list of listeners that will be notified when the
	 * launch history changes.
	 */
	public void removeLaunchHistoryListener(ILaunchHistoryChangedListener listener) {
		fLaunchHistoryChangedListeners.remove(listener);
	}
	
	/**
	 * Notify all launch history listeners that the launch history has changed in some way.
	 */
	protected void fireLaunchHistoryChanged() {
		Iterator iterator = fLaunchHistoryChangedListeners.iterator();
		while (iterator.hasNext()) {
			ILaunchHistoryChangedListener listener = (ILaunchHistoryChangedListener) iterator.next();
			listener.launchHistoryChanged();
		}
	}

	protected String getHistoryAsXML() throws CoreException, ParserConfigurationException, TransformerException, IOException {
		Document doc = DebugUIPlugin.getDocument();
		Element historyRootElement = doc.createElement(HISTORY_ROOT_NODE); 
		doc.appendChild(historyRootElement);
		
		Iterator histories = fLaunchHistories.values().iterator();
		while (histories.hasNext()) {
			LaunchHistory history = (LaunchHistory)histories.next();
			createEntry(doc, historyRootElement, history.getLaunchGroup().getMode(), history.getHistory());
			createEntry(doc, historyRootElement, history.getLaunchGroup().getMode(), history.getFavorites());
			ILaunchConfiguration configuration = history.getRecentLaunch();
			if (configuration != null && configuration.exists()) {
				Element last = doc.createElement(HISTORY_LAST_LAUNCH_NODE);
				last.setAttribute(HISTORY_MEMENTO_ATT, configuration.getMemento());
				last.setAttribute(HISTORY_MODE_ATT, history.getLaunchGroup().getMode());
				historyRootElement.appendChild(last);
			}
		}
		
		return DebugUIPlugin.serializeDocument(doc);
	}

	protected void createEntry(Document doc, Element historyRootElement, String mode, ILaunchConfiguration[] configurations) throws CoreException {
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.exists()) {
				Element launch = doc.createElement(HISTORY_LAUNCH_NODE);
				launch.setAttribute(HISTORY_MEMENTO_ATT, configuration.getMemento());
				launch.setAttribute(HISTORY_MODE_ATT, mode);
				historyRootElement.appendChild(launch);
			}
		}
	}
				
	protected IPath getHistoryFilePath() {
		return DebugUIPlugin.getDefault().getStateLocation().append(LAUNCH_CONFIGURATION_HISTORY_FILENAME); 
	}

	/**
	 * Write out an XML file indicating the entries on the run & debug history lists and
	 * the most recent launch.
	 */
	protected void persistLaunchHistory() throws IOException, CoreException, TransformerException, ParserConfigurationException {
		if (fRestoring) {
			return;
		}
		IPath historyPath = getHistoryFilePath();
		String osHistoryPath = historyPath.toOSString();
		String xml = getHistoryAsXML();
		File file = new File(osHistoryPath);
		file.createNewFile();
		
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
		stream.close();
		fireLaunchHistoryChanged();
	}
	
	/**
	 * Find the XML history file and parse it.  Place the corresponding configs
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
		
		FileInputStream stream= null;
		Element rootHistoryElement= null;
		try {
			// Parse the history file
			stream = new FileInputStream(file);
			rootHistoryElement = null;
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
		if (!rootHistoryElement.getNodeName().equalsIgnoreCase(HISTORY_ROOT_NODE)) { 
			return;
		}

		// For each child of the root node, construct a launch config handle and add it to
		// the appropriate history, or set the most recent launch
		Collection l = fLaunchHistories.values();
		LaunchHistory[] histories = (LaunchHistory[])l.toArray(new LaunchHistory[l.size()]);
		NodeList list = rootHistoryElement.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(HISTORY_LAUNCH_NODE)) { 
					createHistoryElement(entry, histories);
				} else if (entry.getNodeName().equalsIgnoreCase(HISTORY_LAST_LAUNCH_NODE)) {
					createRecentElement(entry, histories);
				}
			}
		}
	}
	
	/**
	 * Construct a launch configuration corresponding to the specified XML
	 * element, and place it in the approriate history.
	 */
	private void createHistoryElement(Element entry, LaunchHistory[] histories) {
		String memento = entry.getAttribute(HISTORY_MEMENTO_ATT); 
		String mode = entry.getAttribute(HISTORY_MODE_ATT);     
		try {
			ILaunchConfiguration launchConfig = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(memento);
			if (launchConfig.exists()) {
				for (int i = 0; i < histories.length; i++) {
					LaunchHistory history = histories[i];
					if (history.accepts(launchConfig) && history.getLaunchGroup().getMode().equals(mode)) {
						history.addHistory(launchConfig, false);
					}
				}
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}	
	}
	
	/**
	 * Construct a launch configuration corresponding to the specified XML
	 * element, and place it in the approriate history's recent launch
	 */
	private void createRecentElement(Element entry, LaunchHistory[] histories) {
		String memento = entry.getAttribute(HISTORY_MEMENTO_ATT); 
		String mode = entry.getAttribute(HISTORY_MODE_ATT);     
		try {
			ILaunchConfiguration launchConfig = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(memento);
			if (launchConfig.exists()) {
				for (int i = 0; i < histories.length; i++) {
					LaunchHistory history = histories[i];
					if (history.accepts(launchConfig) && history.getLaunchGroup().getMode().equals(mode)) {
						history.setRecentLaunch(launchConfig);
					}
				}
			}
		} catch (CoreException e) {
			if (e.getStatus().getCode() != DebugException.MISSING_LAUNCH_CONFIGURATION_TYPE) {
				// only log the error if it's not a missing type definition
				DebugUIPlugin.log(e);
			}
		}	
	}	
	
	/**
	 * Load all registered extensions of the 'launch shortcut' extension point.
	 */
	private void loadLaunchShortcuts() {
		// Get the configuration elements
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_LAUNCH_SHORTCUTS);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();

		// Load the configuration elements into a Map 
		fLaunchShortcuts = new ArrayList(infos.length);
		for (int i = 0; i < infos.length; i++) {
			LaunchShortcutExtension ext = new LaunchShortcutExtension(infos[i]);
			fLaunchShortcuts.add(ext);
		}
		Collections.sort(fLaunchShortcuts, new ShortcutComparator());
	}
	
	/**
	 * Load all registered extensions of the 'launch groups' extension point.
	 */
	private void loadLaunchGroups() {
		if (fLaunchGroups == null) {
			// Get the configuration elements
			IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.EXTENSION_POINT_LAUNCH_GROUPS);
			IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
	
			// Load the configuration elements into a Map 
			fLaunchGroups = new HashMap(infos.length);
			for (int i = 0; i < infos.length; i++) {
				LaunchGroupExtension ext = new LaunchGroupExtension(infos[i]);
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
	 * Returns all launch shortcuts for the given category
	 *
	 * @return all launch shortcuts
	 */
	public List getLaunchShortcuts(String category) {
		return filterShortcuts(getLaunchShortcuts(), category);
	}	
	
	/**
	 * Return a list of filtered launch shortcuts, based on the given category.
	 *  
	 * @param unfiltered
	 * @param category
	 * @return List
	 */
	protected List filterShortcuts(List unfiltered, String category) {
		List filtered = new ArrayList(unfiltered.size());
		Iterator iter = unfiltered.iterator();
		while (iter.hasNext()){
			LaunchShortcutExtension extension = (LaunchShortcutExtension)iter.next();
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
	 * @return all launch shortcuts defined for the given perspective,
	 * empty list if none.
	 */
	public List getLaunchShortcuts(String perpsective, String category) {
		if (fLaunchShortcutsByPerspective == null) {
			Iterator shortcuts = getLaunchShortcuts().iterator();
			fLaunchShortcutsByPerspective = new HashMap(10);
			while (shortcuts.hasNext()) {
				LaunchShortcutExtension ext = (LaunchShortcutExtension)shortcuts.next();
				Iterator perspectives = ext.getPerspectives().iterator();
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
	 * Returns the image used to display an error in the given tab
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
	 * 
	 * @return the launch history with the given group id, or <code>null</code>
	 */
	public LaunchHistory getLaunchHistory(String id) {
		if (fLaunchHistories == null) {
			loadLaunchHistories();
		}
		return (LaunchHistory)fLaunchHistories.get(id);
	}	
	
	/**
	 * Restore launch history
	 */
	private void loadLaunchHistories() {
		if (fLaunchHistories == null) {
			fRestoring = true;
			ILaunchGroup[] groups = getLaunchGroups();
			fLaunchHistories = new HashMap(groups.length);
			for (int i = 0; i < groups.length; i++) {
				ILaunchGroup extension = groups[i];
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
	 * @param mode
	 * @return launch group
	 */
	public LaunchGroupExtension getDefaultLanuchGroup(String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return getLaunchGroup(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
		}
		return getLaunchGroup(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
	}
	
	/**
	 * Returns the launch group the given launch configuration belongs to, in
	 * the specified mode, or <code>null</code> if none.
	 * 
	 * @param configuration
	 * @param mode
	 * @return the launch group the given launch configuration belongs to, in
	 * the specified mode, or <code>null</code> if none
	 */
	public ILaunchGroup getLaunchGroup(ILaunchConfiguration configuration, String mode) {
		try {
			String category = configuration.getCategory();
			ILaunchGroup[] groups = getLaunchGroups();
			for (int i = 0; i < groups.length; i++) {
				ILaunchGroup extension = groups[i];
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
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
		return null;
	}

	/**
	 * Returns the private launch configuration used as a placeholder to represent/store
	 * the information associated with a launch configuration type.
	 * 
	 * @param type launch configuration type
	 * @return launch configuration
	 * @since 3.0
	 */
	public static ILaunchConfiguration getSharedTypeConfig(ILaunchConfigurationType type) throws CoreException {
		String id = type.getIdentifier();
		String name = id + ".SHARED_INFO"; //$NON-NLS-1$
		ILaunchConfiguration shared = null;
		ILaunchConfiguration[] configurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
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

}

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
