package org.eclipse.debug.internal.ui.launchConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.ILaunchHistoryChangedListener;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LaunchConfigurationManager implements ILaunchListener, 
														ILaunchConfigurationListener, 
														IPropertyChangeListener,
														ILaunchHistoryChangedListener  {
	/**
	 * The singleton instance of the launch configuration manager
	 */
	private static LaunchConfigurationManager fgLaunchConfigurationManager;
	/**
	 * The length of the Run & Debug history lists.  
	 */
	protected int fMaxHistorySize;
	
	/**
	 * The most recent debug launches
	 */
	private Vector fDebugHistory;
	
	/**
	 * The most recent run launches
	 */
	private Vector fRunHistory;
	
	/**
	 * The most recent debug launches
	 */
	protected Vector fDebugFavorites;
	
	/**
	 * The most recent run launches
	 */
	protected Vector fRunFavorites;
	
	protected boolean fHistoryInitialized= false;
	
	/**
	 * The list of most recent launches, independent of mode.
	 * This list may be empty, but should never be <code>null</code>.
	 */
	protected List fLastLaunchList;	
		
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
	
	private LaunchConfigurationManager() {		
		setEmptyLaunchHistories();		
		fMaxHistorySize = DebugUIPlugin.getDefault().getPreferenceStore().getInt(IDebugUIConstants.PREF_MAX_HISTORY_SIZE);
		fLastLaunchList = new ArrayList(fMaxHistorySize);
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);	
		launchManager.addLaunchConfigurationListener(this);

		//update histories for launches already registered
		ILaunch[] launches= launchManager.getLaunches();
		for (int i = 0; i < launches.length; i++) {
			launchAdded(launches[i]);
		}
		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		addLaunchHistoryListener(this);
	}

	protected void setRunHistoryVector(Vector runHistory) {
		fRunHistory = runHistory;
	}

	protected Vector getRunHistoryVector() {
		if (!fHistoryInitialized) {
			restoreLaunchHistory();
		}
		return fRunHistory;
	}

	protected void setDebugHistoryVector(Vector debugHistory) {
		fDebugHistory = debugHistory;
	}

	protected Vector getDebugHistoryVector() {
		if (!fHistoryInitialized) {
			restoreLaunchHistory();
		}
		return fDebugHistory;
	}
	
	public static LaunchConfigurationManager getDefault() {
		if (fgLaunchConfigurationManager == null) {
			fgLaunchConfigurationManager= new LaunchConfigurationManager();
		}
		return fgLaunchConfigurationManager;
	}
	
	/**
	 * Returns whether the singleton instance of the manager exists
	 */
	public static boolean defaultExists() {
		return fgLaunchConfigurationManager != null;
	}
	
	public void shutdown() throws CoreException {
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
		launchManager.removeLaunchConfigurationListener(this);
		removeLaunchHistoryListener(this);		
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
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
		updateHistories(launch);
		removeTerminatedLaunches(launch);
	}
	
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
	
	protected void updateFavorites(ILaunchConfiguration config) {
		try {
			if (config.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false)) {
				addDebugFavorite(config);
				removeLaunchConfigurationFromHistoryList(getDebugHistoryVector(), config);
			} else {
				removeDebugFavorite(config);
			}
			if (config.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false)) {
				addRunFavorite(config);
				removeLaunchConfigurationFromHistoryList(getRunHistoryVector(), config);
			} else {
				removeRunFavorite(config);
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}	
	}
	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationAdded(ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration config) {		
		updateFavorites(config);
	}
	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationChanged(ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration config) {		
		if (!config.isWorkingCopy()) {
			updateFavorites(config);
		}
	}
	
	/**
	 * If the deleted config appeared in either of the history lists, delete it from the list(s).
	 * 
	 * @see ILaunchConfigurationListener#launchConfigurationRemoved(ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration config) {
		boolean modified = removeLaunchConfigurationFromHistoryList(getRunHistoryVector(), config);
		modified |= removeLaunchConfigurationFromHistoryList(getDebugHistoryVector(), config);
		modified |= removeLaunchConfigurationFromHistoryList(fDebugFavorites, config);
		modified |= removeLaunchConfigurationFromHistoryList(fRunFavorites, config);
		modified |= removeLaunchConfigurationFromHistoryList(fLastLaunchList, config);
				
		if (modified) {
			fireLaunchHistoryChanged();
		}
	}
	
	/**
	 * Remove the specified launch configuration from the specified history list.  If the 
	 * configuration does not appear in the list, this method does nothing.  Return <code>true</code>
	 * if the configuration was removed, <code>false</code> otherwise.
	 */
	protected boolean removeLaunchConfigurationFromHistoryList(List list, ILaunchConfiguration config) {
		ListIterator iterator = list.listIterator();
		while (iterator.hasNext()) {
			LaunchConfigurationHistoryElement element = (LaunchConfigurationHistoryElement) iterator.next();
			ILaunchConfiguration elementConfig = element.getLaunchConfiguration();
			if (config.equals(elementConfig)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
		
	/**
	 * Returns an array of the most recent debug launches, which can be empty.
	 *
	 * @return an array of launches
	 */	
	public LaunchConfigurationHistoryElement[] getDebugHistory() {
		return getHistoryArray(getDebugHistoryVector());
	}
	
	/**
	 * Returns an array of the favorite debug launches, which can be empty.
	 *
	 * @return an array of launches
	 */	
	public LaunchConfigurationHistoryElement[] getDebugFavorites() {
		return getHistoryArray(fDebugFavorites);
	}
	
	/**
	 * Sets the favorite debug launches, which can be empty.
	 *
	 * @param favorites an array of launches
	 */	
	public void setDebugFavorites(Vector favorites) {
		fDebugFavorites = favorites;
	}	
	
	/**
	 * Sets the recent debug launches, which can be empty.
	 *
	 * @param hsitory an array of launches
	 */	
	public void setDebugHistory(Vector history) {
		setDebugHistoryVector(history);
		fireLaunchHistoryChanged();
	}	
	
	/**
	 * Sets the recent run launches, which can be empty.
	 *
	 * @param hsitory an array of launches
	 */	
	public void setRunHistory(Vector history) {
		setRunHistoryVector(history);
		fireLaunchHistoryChanged();
	}			
	
	/**
	 * Sets the favorite run launches, which can be empty.
	 *
	 * @param favorites an array of launches
	 */	
	public void setRunFavorites(Vector favorites) {
		fRunFavorites = favorites;
	}
		
	/**
	 * Returns an array of the most recent run launches, which can be empty.
	 *
	 * @return an array of launches
	 */
	public LaunchConfigurationHistoryElement[] getRunHistory() {
		return getHistoryArray(getRunHistoryVector());
	}
	
	/**
	 * Returns an array of the favorite run launches, which can be empty.
	 *
	 * @return an array of launches
	 */
	public LaunchConfigurationHistoryElement[] getRunFavorites() {
		return getHistoryArray(fRunFavorites);
	}	
	
	protected LaunchConfigurationHistoryElement[] getHistoryArray(Vector history) {
		LaunchConfigurationHistoryElement[] array = new LaunchConfigurationHistoryElement[history.size()];
		history.copyInto(array);
		return array;
	}
	
	/**
	 * Returns the most recent launch, or <code>null</code> if there
	 * have been no launches.
	 *	
	 * @return the last launch, or <code>null</code> if none
	 */	
	public LaunchConfigurationHistoryElement getLastLaunch() {
		if (!fLastLaunchList.isEmpty()) {
			return (LaunchConfigurationHistoryElement) fLastLaunchList.get(0);
		}
		return null;
	}
		
	/**
	 * Erase both (run & debug) launch histories and the last launched list.
	 */
	protected void setEmptyLaunchHistories() {
		setRunHistoryVector(new Vector(fMaxHistorySize));
		setDebugHistoryVector(new Vector(fMaxHistorySize));
		setRunFavorites(new Vector(fMaxHistorySize));
		setDebugFavorites(new Vector(fMaxHistorySize));
		fLastLaunchList = new ArrayList(fMaxHistorySize);
		fireLaunchHistoryChanged();		
	}
	
	/**
	 * Given a launch, try to add it to both of the run & debug histories.
	 * If either history was modified, fire a history modified notification.
	 */
	protected void updateHistories(ILaunch launch) {
		boolean modified = updateHistory(ILaunchManager.DEBUG_MODE, getDebugHistoryVector(), fDebugFavorites, launch);
		modified |= updateHistory(ILaunchManager.RUN_MODE, getRunHistoryVector(), fRunFavorites, launch);
		if (modified) {
			fireLaunchHistoryChanged();
		}
	}

	/**
	 * Add the given launch to the specified history if the launcher supports the mode. 
	 * Return <code>true</code> if the history was modified, <code>false</code> otherwise.
	 */
	protected boolean updateHistory(String mode, Vector history, Vector favorites, ILaunch launch) {
		
		// First make sure the launch configuration exists, supports the mode of the history list,
		// and isn't private
		ILaunchConfiguration launchConfig = launch.getLaunchConfiguration();
		if (launchConfig == null) {
			return false;
		}
		try {
			if (!launchConfig.supportsMode(mode) ||
				 launchConfig.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false)) {
				return false;
			}
		} catch (CoreException ce) {
			return false;
		}
		
		// Create a new history item
		LaunchConfigurationHistoryElement item= new LaunchConfigurationHistoryElement(launchConfig, mode);
		
		// Update the most recent launch list
		boolean modified = false;
		if (launch.getLaunchMode().equals(mode)) {
			int index = findConfigInHistoryList(fLastLaunchList, launchConfig);
			if (index > 0) {
				fLastLaunchList.remove(item);
			}	
			if (index != 0) {		
				fLastLaunchList.add(0, item);
				modified = true;
			}
		}
		
		// Look for an equivalent launch in the favorites
		int index = findConfigInHistoryList(favorites, item.getLaunchConfiguration());
		if (index >= 0) {
			// a favorite, do not add to history
			return modified;
		}
		
		// Look for an equivalent launch in the history list
		index = findConfigInHistoryList(history, item.getLaunchConfiguration());
		
		//It's already listed as the most recent launch, so nothing to do
		if (index == 0) {
			return modified;
		}
		
		// Make it the top item in the list, removing it from it's previous location, if there was one
		if (index > 0) {
			history.remove(index);
		} 			
		history.add(0, item);
		
		if (history.size() > fMaxHistorySize) {
			history.remove(history.size() - 1);
		}

		return true;	
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
	
	/**
	 * Returns whether the given config is displayed in the favorites
	 * menu
	 * 
	 * @param config launch configuration
	 * @return whether the given config is displayed in the favorites
	 *  menu
	 */
	public boolean isDebugFavorite(ILaunchConfiguration config) {
		return (findConfigInHistoryList(fDebugFavorites, config)) >= 0;
	}	
	
	/**
	 * Returns whether the given config is displayed in the favorites
	 * menu
	 * 
	 * @param config launch configuration
	 * @return whether the given config is displayed in the favorites
	 *  menu
	 */
	public boolean isRunFavorite(ILaunchConfiguration config) {
		return(findConfigInHistoryList(fRunFavorites, config)) >= 0;
	}	
	
	/**
	 * Adds the given config to the debug favorites. Has no
	 * effect if already a debug favorite.
	 * 
	 * @param config launch configuration
	 */
	public void addDebugFavorite(ILaunchConfiguration config) {
		if (!isDebugFavorite(config)) {
			LaunchConfigurationHistoryElement hist = new LaunchConfigurationHistoryElement(config, ILaunchManager.DEBUG_MODE);
			fDebugFavorites.add(hist);
		}
	}	
	
	/**
	 * Adds the given config to the run favorites. Has no
	 * effect if already a run favorite.
	 * 
	 * @param config launch configuration
	 */
	public void addRunFavorite(ILaunchConfiguration config) {
		if (!isRunFavorite(config)) {
			LaunchConfigurationHistoryElement hist = new LaunchConfigurationHistoryElement(config, ILaunchManager.RUN_MODE);
			fRunFavorites.add(hist);
		}
	}	
	
	/**
	 * Removes the given config from the debug favorites. Has no
	 * effect if not a favorite.
	 * 
	 * @param config launch configuration
	 */
	public void removeDebugFavorite(ILaunchConfiguration config) {
		int index = findConfigInHistoryList(fDebugFavorites, config);
		if (index >= 0) {
			fDebugFavorites.remove(index);
		}
	}	
	
	/**
	 * Adds the given config to the run favorites. Has no
	 * effect if already a run favorite.
	 * 
	 * @param config launch configuration
	 */
	public void removeRunFavorite(ILaunchConfiguration config) {
		int index = findConfigInHistoryList(fRunFavorites, config);
		if (index >= 0) {
			fRunFavorites.remove(index);
		}
	}
	
	/**
	 * Find the specified history element in the specified list and return the index at which
	 * it was found.  Return -1 if the element wasn't found in the list.
	 */
	protected int findConfigInHistoryList(List list, ILaunchConfiguration config) {
		for (int i = 0; i < list.size(); i++) {
			LaunchConfigurationHistoryElement historyElement = (LaunchConfigurationHistoryElement) list.get(i);
			if (historyElement != null) {
				ILaunchConfiguration historyConfig = historyElement.getLaunchConfiguration();
				if ((historyConfig != null) && historyConfig.equals(config)) {
					return i;
				}
			}
		}
		
		// Element wasn't in list
		return -1;
	}
	
	/**
	 * Find the specified history element in the history list for the mode that is not the one
	 * specified.  For example, if mode is 'debug', the 'run' list is searched.
	 */
	protected int findConfigInOtherHistoryList(String mode, ILaunchConfiguration config) {
		Vector historyList = getOtherHistoryList(mode);
		return findConfigInHistoryList(historyList, config);
	}
	
	/**
	 * Return the 'other' history list from the mode specified.  For example, if
	 * mode is 'debug', return the 'run' history list.
	 */
	protected Vector getOtherHistoryList(String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return getRunHistoryVector();
		} else {
			return getDebugHistoryVector();
		}
	}
	
	protected String getHistoryAsXML() throws IOException, CoreException {
		org.w3c.dom.Document doc = new DocumentImpl();
		Element historyRootElement = doc.createElement(HISTORY_ROOT_NODE); 
		doc.appendChild(historyRootElement);
		
		List all = new ArrayList(getDebugHistoryVector().size() + fDebugFavorites.size() + getRunHistoryVector().size() + fRunFavorites.size());
		all.addAll(fDebugFavorites);
		all.addAll(fRunFavorites);
		all.addAll(getDebugHistoryVector());
		all.addAll(getRunHistoryVector());
		

		Iterator iter = all.iterator();
		while (iter.hasNext()) {
			Element historyElement = getHistoryEntryAsXMLElement(doc, (LaunchConfigurationHistoryElement)iter.next());
			historyRootElement.appendChild(historyElement);
		}
		if (!fLastLaunchList.isEmpty()) {
			Element recent = getRecentLaunchAsXMLElement(doc, (LaunchConfigurationHistoryElement) fLastLaunchList.get(0));
			historyRootElement.appendChild(recent);
		}

		return DebugUIPlugin.serializeDocument(doc);
	}
	
	protected Element getHistoryEntryAsXMLElement(org.w3c.dom.Document doc, LaunchConfigurationHistoryElement element) throws CoreException {
		Element entry = doc.createElement(HISTORY_LAUNCH_NODE); 
		setAttributes(entry, element);
		return entry;
	}
	
	protected Element getRecentLaunchAsXMLElement(org.w3c.dom.Document doc, LaunchConfigurationHistoryElement element) throws CoreException {
		Element entry = doc.createElement(HISTORY_LAST_LAUNCH_NODE); 
		setAttributes(entry, element);
		return entry;
	}
	
	protected void setAttributes(Element entry, LaunchConfigurationHistoryElement element) throws CoreException {
		ILaunchConfiguration config = element.getLaunchConfiguration();
		if (config instanceof ILaunchConfigurationWorkingCopy) {
			config = ((ILaunchConfigurationWorkingCopy)config).getOriginal();
		}
		String memento = config.getMemento();
		entry.setAttribute(HISTORY_MEMENTO_ATT, memento); 
		entry.setAttribute(HISTORY_MODE_ATT, element.getMode());			 
	}
				
	protected IPath getHistoryFilePath() {
		return DebugUIPlugin.getDefault().getStateLocation().append(LAUNCH_CONFIGURATION_HISTORY_FILENAME); 
	}

	/**
	 * Write out an XML file indicating the entries on the run & debug history lists and
	 * the most recent launch.
	 */
	protected void persistLaunchHistory() throws IOException, CoreException {
		IPath historyPath = getHistoryFilePath();
		String osHistoryPath = historyPath.toOSString();
		String xml = getHistoryAsXML();
		File file = new File(osHistoryPath);
		file.createNewFile();
		
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
		stream.close();
	}
	
	/**
	 * Find the XML history file and parse it.  Place the corresponding history elements
	 * in the appropriate history lists, and set the most recent launch.
	 */
	protected void restoreLaunchHistory() {
		fHistoryInitialized= true;
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

		// For each child of the root node, construct a history element wrapper and add it to
		// the appropriate history list, or set the most recent launch
		NodeList list = rootHistoryElement.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(HISTORY_LAUNCH_NODE)) { 
					LaunchConfigurationHistoryElement item = createHistoryElement(entry);
					if (item != null) {
						if (item.isFavorite()) {
							if (item.getMode().equals(ILaunchManager.DEBUG_MODE)) {
								fDebugFavorites.add(item);
							} else {
								fRunFavorites.add(item);
							}							
						} else {
							if (item.getMode().equals(ILaunchManager.DEBUG_MODE)) {
								getDebugHistoryVector().add(item);
							} else {
								getRunHistoryVector().add(item);
							}
						}
					}
				} else if (entry.getNodeName().equalsIgnoreCase(HISTORY_LAST_LAUNCH_NODE)) { 
					fLastLaunchList.add(0, createHistoryElement(entry));
				}
			}
		}
	}
	
	/**
	 * Construct & return a <code>LaunchConfigurationHistoryElement</code> corresponding to
	 * the specified XML element.
	 */
	protected LaunchConfigurationHistoryElement createHistoryElement(Element entry) {
		String memento = entry.getAttribute(HISTORY_MEMENTO_ATT); 
		String mode = entry.getAttribute(HISTORY_MODE_ATT);       
		LaunchConfigurationHistoryElement hist = null;
		try {
			ILaunchConfiguration launchConfig = DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(memento);
			if (launchConfig.exists()) {
				hist = new LaunchConfigurationHistoryElement(launchConfig, mode);
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}	
		return hist;
	}
	
	/**
	 * Load all registered extensions of the 'launch shortcut' extension point.
	 */
	private void loadLaunchShortcuts() {
		// Get the configuration elements
		IPluginDescriptor descriptor= DebugUIPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_LAUNCH_SHORTCUTS);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();

		// Load the configuration elements into a Map 
		fLaunchShortcuts = new ArrayList(infos.length);
		for (int i = 0; i < infos.length; i++) {
			LaunchShortcutExtension ext = new LaunchShortcutExtension(infos[i]);
			fLaunchShortcuts.add(ext);
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
	 * Returns all launch shortcuts defined for the given perspective,
	 * or <code>null</code> if none
	 * 
	 * @param perpsective perspective identifier
	 * @return all launch shortcuts defined for the given perspective,
	 * or <code>null</code> if none
	 */
	public List getLaunchShortcuts(String perpsective) {
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
			// sort the lists
			Iterator perspectives = fLaunchShortcutsByPerspective.keySet().iterator();		
			while (perspectives.hasNext()) {
				String id = (String)perspectives.next();
				List list = (List)fLaunchShortcutsByPerspective.get(id);
				Collections.sort(list, new ShortcutComparator());
			}
		}
		return (List)fLaunchShortcutsByPerspective.get(perpsective);

	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IDebugUIConstants.PREF_MAX_HISTORY_SIZE)) {
			IPreferenceStore prefs = DebugUIPlugin.getDefault().getPreferenceStore();
			int newValue = prefs.getInt(IDebugUIConstants.PREF_MAX_HISTORY_SIZE);
			if (fMaxHistorySize != newValue) {
				shortenHistoryLists(newValue);
				fMaxHistorySize = newValue;
				fireLaunchHistoryChanged();
			}
		}
	}
	
	/**
	 * Adjust the lengths of the history lists, throwing away any entries that are past the new end
	 * of the lists. 
	 */
	protected void shortenHistoryLists(int newLength) {		
		if (newLength < getRunHistoryVector().size()) {
			setRunHistoryVector(new Vector(getRunHistoryVector().subList(0, newLength)));
		}
		if (newLength < getDebugHistoryVector().size()) {
			setDebugHistoryVector(new Vector(getDebugHistoryVector().subList(0, newLength)));
		}
		if (newLength < fLastLaunchList.size()) {
			fLastLaunchList = new ArrayList(fLastLaunchList.subList(0, newLength));		
		}
	}

	/**
	 * @see ILaunchHistoryChangedListener#launchHistoryChanged()
	 */
	public void launchHistoryChanged() {
		try {
			persistLaunchHistory();
		} catch (IOException e) {
			DebugUIPlugin.log(e);
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);			
		}		
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

}

class ShortcutComparator implements Comparator {
	/**
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object a, Object b) {
		return ((LaunchShortcutExtension)a).getLabel().compareToIgnoreCase(((LaunchShortcutExtension)b).getLabel());
	}

}