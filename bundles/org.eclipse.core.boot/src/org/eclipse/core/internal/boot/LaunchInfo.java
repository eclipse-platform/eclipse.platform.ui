package org.eclipse.core.internal.boot;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.net.*;
import java.util.*;
import java.io.*;
import org.eclipse.core.boot.*;
import org.eclipse.core.internal.boot.update.BootUpdateManager;
 
public class LaunchInfo implements IInstallInfo {
	
	public static final String PLATFORM_COMPONENT_ID = "org.eclipse.platform";
	public static final String BOOT_PLUGIN_ID = "org.eclipse.core.boot";	

	private static LaunchInfo profile = null;

	private URL installurl;
	private URL baseurl;
	private Properties props;
	private String id;
	private String platform;
	private String app;
	private String appconfig;
	private ArrayList configs;
	private ArrayList configsInact;
	private ArrayList comps;
	private ArrayList compsInact;
	private ArrayList compsDang;
	private ArrayList plugins;
	private ArrayList pluginsInact;
	private ArrayList pluginsUnmgd;
	private ArrayList fragments;
	private ArrayList fragmentsInact;
	private ArrayList fragmentsUnmgd;
	private ArrayList status;
	private int historyCount;
	private boolean changed = false;

	private static final String CONFIGSDIR = "configurations/";
	private static final String COMPSDIR = "components/";
	private static final String PLUGINSDIR = "plugins/";
	private static final String FRAGMENTSDIR = "fragments/";
	private static final String PLUGINXML = "plugin.xml";
	private static final String FRAGMENTXML = "fragment.xml";
	private static final String INSTALL_INFO_DIR = "install/";
	private static final String LAUNCH_SUMMARY_NAME = "install";
	private static final String LAUNCH_SUMMARY_EXT = "properties";
	private static final String LAUNCH_SUMMARY = LAUNCH_SUMMARY_NAME+"."+LAUNCH_SUMMARY_EXT;
	private static final String LAUNCH_PROFILE_NAME = "update";
	private static final String LAUNCH_PROFILE_EXT = "cfg";
	private static final String LAUNCH_PROFILE = LAUNCH_PROFILE_NAME+"."+LAUNCH_PROFILE_EXT;
	
	private static final String ID = "id";
	private static final String PLATFORM = "runtime";
	private static final String APP = "application";
	private static final String APP_CONFIG = "application.configuration";
	private static final String CONFIG_ACT = "configurations.active";
	private static final String CONFIG_INACT = "configurations.inactive";
	private static final String COMP_ACT = "components.active";
	private static final String COMP_INACT = "components.inactive";
	private static final String COMP_DANG = "components.dangling";
	private static final String PLUGIN_ACT = "plugins.active";
	private static final String PLUGIN_INACT = "plugins.inactive";
	private static final String PLUGIN_UNMGD = "plugins.unmanaged";
	private static final String FRAG_ACT = "fragments.active";
	private static final String FRAG_INACT = "fragments.inactive";
	private static final String FRAG_UNMGD = "fragments.unmanaged";
	private static final String HISTORY_COUNT = "history.count";
	private static final String EOF = "eof";
	private static final String EOF_MARKER = EOF+"="+EOF;
	private static final int LIST_SIZE = 10;

	private static final String DEFAULT_PLATFORM = "";
	private static final String DEFAULT_APP = "org.eclipse.ui.workbench";
	private static final String DEFAULT_APP_CONFIG = "";
	private static final int DEFAULT_HISTORY_COUNT = 3;

	private static final String URL_FILE = "file";
	private static final String URL_VA = "valoader";

	private static final String ECLIPSE_CONFIG_URL = EclipseURLConfigurationConnection.CONFIG_URL_STRING;
	private static final String ECLIPSE_COMP_URL = EclipseURLComponentConnection.COMP_URL_STRING;
	
	// debug tracing
	public static boolean DEBUG = false;

	public static class History {
		private URL url;
		private Date date;

		
		public History(URL url,Date date) {
			this.url = url;
			this.date = date;
		}

		public String toString() {
			if (date==null) 
				return "current";
			else return date.toString();
		}

		public URL getLaunchInfoURL() {
			return url;
		}

		public Date getLaunchInfoDate() {
			/**
			*  Return history profile creation date, or null (if curent profile)
			*/
			return date;
		}

		public boolean isCurrent() {
			return date == null;
		}
	}

	public static class Status {
		private String msg;
		private Throwable exc;

		public Status(String msg) {
			this.msg = msg;
			this.exc = null;
		}
		
		public Status(String msg, Throwable exc) {
			this.msg = msg;
			this.exc = exc;
		}

		public String getMessage() {
			return msg;
		}

		public Throwable getException() {
			return exc;
		}
	}

	public static class VersionedIdentifier {
		private String id;
		private String version;
		public static final String SEPARATOR = "_";

		public VersionedIdentifier(String s) {
			if (s==null || (s=s.trim()).equals(""))
				throw new IllegalArgumentException();
			int ix = s.lastIndexOf(SEPARATOR);
			if (ix > 0) {
				this.id = s.substring(0, ix);
				this.version = s.substring(ix + 1);
			}
			else {
				this.id = s;
				this.version = "";
			}
		}

		public VersionedIdentifier(String id, String version) {
			if (id==null || (id=id.trim()).equals("") || version==null)
				throw new IllegalArgumentException();
			this.id = id;
			this.version = version.trim();
		}

		public String getIdentifier() {
			return this.id;
		}

		public String getVersion() {
			return this.version;
		}

		public String toString() {
			return this.version.equals("") ? this.id : this.id + SEPARATOR + this.version;
		}

		public boolean equals(Object vid) {
			if (!(vid instanceof VersionedIdentifier)) return false;
			return equals((VersionedIdentifier) vid);
		}
		
		public boolean equals(VersionedIdentifier vid) {
			if (!this.id.equals(vid.id)) return false;
			return this.version.equals(vid.version);
		}
	}

private LaunchInfo() {}

private LaunchInfo(URL info) {
	this(info, getCurrent().installurl);
}

private LaunchInfo(URL info, URL install) {
	super();
	
	baseurl = info;
	installurl = install;
	
	props = new Properties();
	try {
		props.load(baseurl.openStream());
		id = props.getProperty(ID,"");
		if (id.trim().equals(""))
			id = Long.toString((new java.util.Date()).getTime(),Character.MAX_RADIX);
		platform = props.getProperty(PLATFORM,DEFAULT_PLATFORM);
		app = props.getProperty(APP,DEFAULT_APP);
		if (app.trim().equals(""))
			app = DEFAULT_APP;
		appconfig = props.getProperty(APP_CONFIG,DEFAULT_APP_CONFIG);
		String count = props.getProperty(HISTORY_COUNT,"");
		try {
			historyCount = (new Integer(count)).intValue();
		}
		catch (Exception e) {
			historyCount = DEFAULT_HISTORY_COUNT;
		}
		
		configs = loadListProperty(CONFIG_ACT);
		configsInact = loadListProperty(CONFIG_INACT);
		
		comps = loadListProperty(COMP_ACT);
		compsInact = loadListProperty(COMP_INACT);
		compsDang = loadListProperty(COMP_DANG);
	
		plugins = loadListProperty(PLUGIN_ACT);
		pluginsInact = loadListProperty(PLUGIN_INACT);
		pluginsUnmgd = new ArrayList();
	
		fragments = loadListProperty(FRAG_ACT);
		fragmentsInact = loadListProperty(FRAG_INACT);
		fragmentsUnmgd = new ArrayList();
	}
	catch(IOException e) {
		setDefaults();
	}
}

public LaunchInfo(History history) {
	this(history.getLaunchInfoURL());
}

public void addStatus(Status[] status) {
	if (status==null || status.length==0)
		return;
	if (this.status == null)
		this.status = new ArrayList();
	for (int i=0; i<status.length; i++)
		this.status.add(status[i]);
}

public void addStatus(Status status) {
	if (status==null)
		return;
	if (this.status == null)
		this.status = new ArrayList();
	this.status.add(status);
}

private VersionedIdentifier[] computeDelta(String[] list, List active, List inactive) {
	if (active == null)
		active = new ArrayList();
	if (inactive == null)
		inactive = new ArrayList();
	ArrayList delta = new ArrayList();
	VersionedIdentifier vid;
	for (int i = 0; i < list.length; i++) {
		try {
			vid = new VersionedIdentifier(list[i]);
			if (!active.contains(vid) && !inactive.contains(vid))
				delta.add(vid);
		}
		catch(Exception e) { /* skip bad identifiers */ }
	}
	if (delta.size() == 0)
		return new VersionedIdentifier[0];

	VersionedIdentifier[] result = new VersionedIdentifier[delta.size()];
	delta.toArray(result);
	return result;
}

private static void debug(String s) {
	System.out.println("LaunchInfo: "+s);
}

synchronized public void flush() {
	
	if (!changed) return;
	if (baseurl==null || !isFileProtocol(baseurl)) return;
	if (this!=LaunchInfo.getCurrent()) return;
	
	// flush the current state to disk. Only has effect if the state has changed.
	// This method should be called by the UM ui each time the preference page
	// is closed
}

synchronized private VersionedIdentifier[] get(List list) {

	VersionedIdentifier[] result = new VersionedIdentifier[list.size()];
	list.toArray(result);
	return result;	
}

synchronized private VersionedIdentifier[] get(List list1, List list2) {
	ArrayList temp = new ArrayList(list1.size()+list2.size());
	temp.addAll(list1);
	temp.addAll(list2);	
	VersionedIdentifier[] result = new VersionedIdentifier[temp.size()];
	temp.toArray(result);
	return result;	
}

public String getApplication() {

	return app;	
}

public String getApplicationConfiguration() {

	return appconfig;	
}
/**
 * @see ILaunchInfo#getApplicationConfigurationIdentifier
 */
public String getApplicationConfigurationIdentifier() {
	String appCfig = getApplicationConfiguration();
	if (appCfig != null && appCfig.equals(""))
		return null;
	else
		return appCfig;
}
/**
 * @see ILaunchInfo#getComponentInstallURLFor
 */
public URL getComponentInstallURLFor(String componentId) {
	if (componentId==null || componentId.trim().equals(""))
		throw new IllegalArgumentException();
	try {
		return new URL(ECLIPSE_COMP_URL + componentId.trim() + "/");
	} catch (MalformedURLException e) {
		throw new IllegalStateException();
	}
}

public VersionedIdentifier[] getComponents() {
	return get(comps);
}
/**
 * @see ILaunchInfo#getConfigurationInstallURLFor
 */
public URL getConfigurationInstallURLFor(String configurationId) {
	if (configurationId==null || configurationId.trim().equals(""))
		throw new IllegalArgumentException();
	try {
		return new URL(ECLIPSE_CONFIG_URL + configurationId.trim() + "/");
	} catch (MalformedURLException e) {
		throw new IllegalStateException();
	}
}

public VersionedIdentifier[] getConfigurations() {
	return get(configs);
}

public static LaunchInfo getCurrent() {
	return profile;
}
public URL[] getFragmentPath() {

	ArrayList path = new ArrayList();
	VersionedIdentifier vid;
	
	// include active
	for (int i=0; i< fragments.size(); i++) {
		vid = (VersionedIdentifier) fragments.get(i);
		try {
			path.add(new URL(installurl,FRAGMENTSDIR+vid.toString()+"/"+FRAGMENTXML));
		}
		catch (MalformedURLException e) {
		}
	}

	// include unmanaged
	for (int i=0; i< fragmentsUnmgd.size(); i++) {
		vid = (VersionedIdentifier) fragmentsUnmgd.get(i);
		try {
			path.add(new URL(installurl,FRAGMENTSDIR+vid.toString()+"/"+FRAGMENTXML));
		}
		catch (MalformedURLException e) {
		}
	}
	
	URL[] result = new URL[path.size()];
	path.toArray(result);
	return result;

}

public VersionedIdentifier[] getFragments() {
	return get(fragments, fragmentsUnmgd);
}

public int getHistoryCount() {

	return historyCount;	
}

public String getIdentifier() {

	return id;	
}
/**
 * @see ILaunchInfo#getInstalledComponentIdentifiers
 */
public String[] getInstalledComponentIdentifiers() {
	VersionedIdentifier[] c = getComponents();
	String[] result = new String[c.length];
	for (int i=0; i<c.length; i++)
		result[i] = c[i].toString();
	return result;
}
/**
 * @see ILaunchInfo#getInstalledConfigurationIdentifiers
 */
public String[] getInstalledConfigurationIdentifiers() {
	VersionedIdentifier[] c = getConfigurations();
	String[] result = new String[c.length];
	for (int i=0; i<c.length; i++)
		result[i] = c[i].toString();
	return result;
}
/**
 * returns install profile history, sorted from oldest (least recent)
 * to youngest (most recent). Typically the most recent entry is
 * the current profile
 */
 
public History[] getLaunchInfoHistory() {
	
	if (baseurl==null || !isFileProtocol(baseurl)) return new History[] { new History(baseurl,null) };
	
	File dir = (new File(baseurl.getFile().replace('/',File.separatorChar))).getParentFile();
	String[] list = null;
	if (dir != null)
		list = dir.list();
	if (list==null) return new History[] { new History(baseurl,null) };

	Arrays.sort(list);
	ArrayList result = new ArrayList();
	History current = null;
	for (int i=0; i<list.length; i++) {
		if (list[i].startsWith(LAUNCH_PROFILE_NAME) && list[i].endsWith(LAUNCH_PROFILE_EXT)) {
			String time = list[i].substring(LAUNCH_PROFILE_NAME.length(),list[i].length()-LAUNCH_PROFILE_EXT.length()-1);
			Date date = null;
			if (time.length()>0) {
				time = time.substring(1);
				date = new Date(Long.parseLong(time,Character.MAX_RADIX));
			}
			try {
				URL url = new URL(baseurl,list[i]);
				if (time.length()>0)
					result.add(new History(url,date));
				else
					current = new History(url,null);
			}
			catch(MalformedURLException e) {}
		}
	}

	if (current != null) result.add(current);
	History[] array = new History[result.size()];
	result.toArray(array);
	return array;
}
public URL[] getPluginPath() {

	ArrayList path = new ArrayList();
	VersionedIdentifier vid;
	
	// include active
	for (int i=0; i< plugins.size(); i++) {
		vid = (VersionedIdentifier) plugins.get(i);
		try {
			path.add(new URL(installurl,PLUGINSDIR+vid.toString()+"/"+PLUGINXML));
		}
		catch (MalformedURLException e) {
		}
	}

	// include unmanaged
	for (int i=0; i< pluginsUnmgd.size(); i++) {
		vid = (VersionedIdentifier) pluginsUnmgd.get(i);
		try {
			path.add(new URL(installurl,PLUGINSDIR+vid.toString()+"/"+PLUGINXML));
		}
		catch (MalformedURLException e) {
		}
	}
	
	URL[] result = new URL[path.size()];
	path.toArray(result);
	return result;
}

public VersionedIdentifier[] getPlugins() {
	return get(plugins, pluginsUnmgd);
}

public String getRuntime() {

	return platform;	
}
public Status[] getStatus() {
	if (status != null && status.size() == 0)
		return null;
	else
		return (Status[])status.toArray();
}
public boolean hasStatus() {
	if (status == null || status.size() == 0)
		return false;
	else
		return true;
}
public boolean isDanglingComponent(VersionedIdentifier component) {
	return compsDang.contains(component);
}
public void isDanglingComponent(VersionedIdentifier component, boolean isDangling) {
	
	if (!comps.contains(component)) {
		if (!compsInact.contains(component))
			return;
	}
	
	if (isDangling) {
		// check to see if we have to add as dangling
		if (compsDang.contains(component))
			return;
		compsDang.add(component);
	}
	else {
		// check to see if we have to remove as dangling
		compsDang.remove(component);
	}
}

private boolean isFileProtocol(URL u) {

	return URL_FILE.equals(u.getProtocol()) || URL_VA.equals(u.getProtocol());

}

private ArrayList loadListProperty(String name) {

	ArrayList list = new ArrayList();
	String value = (String) props.get(name+".0");
	for (int i=1; value != null; i++) {
		loadListPropertyEntry(list, value);
		value = (String) props.get(name+"."+i);
	}
	return list;
}

private void loadListPropertyEntry(List list, String value) {

	if (value==null) return;
	
	StringTokenizer tokens = new StringTokenizer(value, ",");
	String token;
	VersionedIdentifier vid;
	while (tokens.hasMoreTokens()) {
		token = tokens.nextToken().trim();
		if (!token.equals("")) {
			try {
				vid = new VersionedIdentifier(token);
				list.add(vid);
			}
			catch (Exception e) { /* skip bad entry */ }
		}
	}
	return;
}

synchronized private void remove(VersionedIdentifier id, List active, List inactive) {
	if (active.contains(id)) {
		changed = true;
		active.remove(id);
		if (!inactive.contains(id))
			inactive.add(id);
	}
}

public void removeComponent(VersionedIdentifier component) {
	remove(component, comps, compsInact);
}

public void removeConfiguration(VersionedIdentifier configuration) {	
	remove(configuration, configs, configsInact);
}

public void removeFragment(VersionedIdentifier fragment) {	
	remove(fragment, fragments, fragmentsInact);
}

public void removePlugin(VersionedIdentifier plugin) {
	remove(plugin, plugins, pluginsInact);
}

synchronized public void revertTo(History history) {

	if (history==null) return;
	if (history.isCurrent()) return;
	if (this!=LaunchInfo.getCurrent()) return;
	
	// poof up launch info for specified history;
	LaunchInfo old = new LaunchInfo(history);

	ArrayList newConfigsInact = revertToInactive(configs, configsInact, old.configs, old.configsInact);
	ArrayList newCompsInact = revertToInactive(comps, compsInact, old.comps, old.compsInact);
	ArrayList newPluginsInact = revertToInactive(plugins, pluginsInact, old.plugins, old.pluginsInact);
	ArrayList newFragmentsInact = revertToInactive(fragments, fragmentsInact, old.fragments, old.fragmentsInact);
	
	// update current state
	changed = true;
	platform = old.platform;
	app = old.app;
	appconfig = old.appconfig;
	// keep historyCount from current
	configs = old.configs;
	configsInact = newConfigsInact;
	comps = old.comps;
	compsInact = newCompsInact;
	plugins = old.plugins;
	pluginsInact = newPluginsInact;
	pluginsUnmgd = new ArrayList();
	fragments = old.fragments;
	fragmentsInact = newFragmentsInact;
	fragmentsUnmgd = new ArrayList();
	
}

private ArrayList revertToInactive(List curAct, List curInact, List oldAct, List oldInact) {

	// start with old inactive list
	ArrayList inactive = (ArrayList)oldInact;
	VersionedIdentifier vid;

	// add current inactive
	for (int i=0; i<curInact.size(); i++) {
		vid = (VersionedIdentifier) curInact.get(i);
		if (!inactive.contains(vid))
			inactive.add(vid);
	}

	// add current active that are not active in new state
	for (int i=0; i<curAct.size(); i++) {
		vid = (VersionedIdentifier) curAct.get(i);
		if (!oldAct.contains(vid)) {
			if (!inactive.contains(vid))
				inactive.add(vid);
		}
	}
	
	return inactive;
}
synchronized private void set(VersionedIdentifier id, List active, List inactive) {
	if (id == null)
		return;
	for (int i = 0; i < active.size(); i++) {
		VersionedIdentifier vid = (VersionedIdentifier) active.get(i);
		if (vid.getIdentifier().equals(id.getIdentifier())) {
			if (vid.getVersion().equals(id.getVersion()))
				return; // same identifier already active ... do nothing

			active.remove(i); // different version found ... replace it
			active.add(i,id);
			if (!inactive.contains(vid))
				inactive.add(vid);
			inactive.remove(id);
			changed = true;
			return;
		}
	}
	active.add(id); // did not exist ... add it
	changed = true;
}

public void setApplication(String app) {
	if (this.app!=null && !this.app.equals(app)) {
		changed = true;
		if (app!=null) this.app = app;
		else this.app = DEFAULT_APP;
	}
}
/**
 * @param appconfig is the directory identifier (incl. version suffix)
 * of the "dominant" application configuration.
 */	
public void setApplicationConfiguration(String appconfig) {
	if (this.appconfig!=null && !this.appconfig.equals(appconfig)) {
		changed = true;
		if (appconfig!=null) this.appconfig = appconfig;
		else this.appconfig = DEFAULT_APP_CONFIG;
	}
}
public void setComponent(VersionedIdentifier component) {
	set(component, comps, compsInact);
}

public void setConfiguration(VersionedIdentifier config) {
	set(config, configs, configsInact);
}
/*
 * called after any new configs, components and plugins are processed
 */
private void setDefaultRuntime() {

	boolean found = false;
	if (getRuntime().equals(DEFAULT_PLATFORM)) {
		VersionedIdentifier vid;
		// check active list for runtime
		for (int i=0; i< plugins.size(); i++) {
			vid = (VersionedIdentifier) plugins.get(i);
			if (vid.getIdentifier().equals(BOOT_PLUGIN_ID)) {
				setRuntime(vid.toString());
				found = true;
				break;
			}
		}

		if (!found) {
			// check unmanaged list for runtime
			for (int i=0; i< pluginsUnmgd.size(); i++) {
				vid = (VersionedIdentifier) pluginsUnmgd.get(i);
				if (vid.getIdentifier().equals(BOOT_PLUGIN_ID)) {
					setRuntime(vid.toString());
					found = true;
					break;
				}
			}
		}
		
	}
}

private void setDefaults() {
	changed = true; // force save on shutdown
	id = Long.toString((new java.util.Date()).getTime(),Character.MAX_RADIX);
	platform = DEFAULT_PLATFORM;
	app = DEFAULT_APP;
	appconfig = DEFAULT_APP_CONFIG;
	historyCount = DEFAULT_HISTORY_COUNT;
	
	configs = new ArrayList();
	configsInact = new ArrayList();
	
	comps = new ArrayList();
	compsInact = new ArrayList();
	compsDang = new ArrayList();
	
	plugins = new ArrayList();
	pluginsInact = new ArrayList();
	pluginsUnmgd = new ArrayList();
	
	fragments = new ArrayList();
	fragmentsInact = new ArrayList();
	fragmentsUnmgd = new ArrayList();
}

public void setFragment(VersionedIdentifier fragment) {
	set(fragment, fragments, fragmentsInact);
}

public void setHistoryCount(int count) {

	changed = true;
	if (count<0)
		historyCount = DEFAULT_HISTORY_COUNT;
	else
		historyCount = count;
}

public void setPlugin(VersionedIdentifier plugin) {
	set(plugin, plugins, pluginsInact);
}
/**
 * @param platform is the directory identifier (incl. version suffix)
 * of the plugins subdirectory containing boot.jar. At install time
 * it is conained in a component identifier by 
 * LaunchInfo.PLATFORM_COMPONENT_ID and has the base directory 
 * name (no version suffix) identified by
 * LaunchInfo.BOOT_PLUGIN_ID
 */	
public void setRuntime(String platform) {
	if (this.platform!=null && !this.platform.equals(platform)) {
		changed = true;
		if (platform!=null) this.platform = platform;
		else this.platform = DEFAULT_PLATFORM;
	}
}

static void shutdown() {
	if (profile==null) return;
	try {
		profile.store();
	}
	catch(IOException e) {
		// was not able to save updated install profile
	}
}
static void startup(URL base) {

	if (profile == null) {
		try {
			URL prof = new URL(base, INSTALL_INFO_DIR);
			prof = new URL(prof, LAUNCH_PROFILE);
			profile = new LaunchInfo(prof,base);
		} catch (MalformedURLException e) {
			profile = new LaunchInfo();
			profile.setDefaults();
		}

		// detect changes from last startup
		if (profile.isFileProtocol(base)) {
			String path;
			File dir;
			String[] list;
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (new File(dir, name)).isDirectory();
				}
			};

			// look for configurations
			path =
				(base.getFile() + INSTALL_INFO_DIR + CONFIGSDIR).replace(
					'/',
					File.separatorChar);
			dir = new File(path);
			list = dir.list(filter);
			VersionedIdentifier[] configDelta;
			if (list == null)
				configDelta = new VersionedIdentifier[0];
			else
				configDelta = profile.computeDelta(list, profile.configs, profile.configsInact);

			// look for components	
			path =
				(base.getFile() + INSTALL_INFO_DIR + COMPSDIR).replace('/', File.separatorChar);
			dir = new File(path);
			list = dir.list(filter);
			VersionedIdentifier[] compDelta;
			if (list == null)
				compDelta = new VersionedIdentifier[0];
			else
				compDelta = profile.computeDelta(list, profile.comps, profile.compsInact);

			// complete "installation" of new configurations and components	
			if (configDelta.length > 0 || compDelta.length > 0) 
				profile.addStatus(BootUpdateManager.install(configDelta, compDelta));
				
			// look for plugins	
			path =
				(base.getFile() + PLUGINSDIR).replace('/', File.separatorChar);
			dir = new File(path);
			list = dir.list(filter);
			VersionedIdentifier[] pluginDelta;
			if (list == null)
				pluginDelta = new VersionedIdentifier[0];
			else
				pluginDelta = profile.computeDelta(list, profile.plugins, profile.pluginsInact);
			for (int i=0; i<pluginDelta.length; i++)
				profile.pluginsUnmgd.add(pluginDelta[i]);
				
			// look for fragments	
			path =
				(base.getFile() + FRAGMENTSDIR).replace('/', File.separatorChar);
			dir = new File(path);
			list = dir.list(filter);
			VersionedIdentifier[] fragmentDelta;
			if (list == null)
				fragmentDelta = new VersionedIdentifier[0];
			else
				fragmentDelta = profile.computeDelta(list, profile.fragments, profile.fragmentsInact);
			for (int i=0; i<fragmentDelta.length; i++)
				profile.fragmentsUnmgd.add(fragmentDelta[i]);

			// check to see if runtime is set
			if (profile.getRuntime().equals(DEFAULT_PLATFORM)) {
				profile.setDefaultRuntime();
			}

			// flush the state to disk
			profile.flush();
		}
	}
}

synchronized private void store() throws IOException {
	if (!changed) return;
	if (baseurl==null || !isFileProtocol(baseurl)) return;

	File active = new File(baseurl.getFile().replace('/', File.separatorChar));
	File dir = active.getParentFile();
	if (dir==null) return; // cannot save
	dir.mkdirs();

	String newId;
	if (active.exists()) {
		newId = Long.toString((new java.util.Date()).getTime(),Character.MAX_RADIX); // id for new file
		String suffix = getIdentifier(); // id for history
		File history = new File(dir,LAUNCH_PROFILE_NAME+"_"+suffix+"."+LAUNCH_PROFILE_EXT);
		active.renameTo(history);
	}
	else newId = getIdentifier(); // use id generated on startup

	File summary = new File(dir,LAUNCH_SUMMARY);
	PrintWriter os = null;
	PrintWriter sum = null;
	try {
		// write state
		os = new PrintWriter(new FileOutputStream(active));
		write(newId, os);

		// write summary
		sum = new PrintWriter(new FileOutputStream(summary));
		writeSummary(newId, sum);
	}
	finally {
		if (os!=null) os.close();
		if (sum!=null) sum.close();
	}
}

private void storeListProperty(Properties props,List list, String name) {

	if (list==null || list.size() <= 0) return;
	for (int i=0; LIST_SIZE*i<list.size(); i++) {
		String prop = "";
		for (int j=0; j<10 && LIST_SIZE*i+j<list.size() ; j++) {
			if (j!=0) prop += ",";
			prop += list.get(LIST_SIZE*i+j).toString();
		}
		props.put(name+"."+i, prop);
	}
}

public static void todo() {
/*
	* platform:/base/ /plugin/ /componennt/ /configuration/ /resource/
	* computeAdditions, computeDeletions instead of computeDelta, cleanup lists wrt file state
	* cleanup dangling list on startup as well (see item 2), on uninstall
	* use ArrayList instead of Vector
	* deletion/ uninstall processing .... what should be the UI/ trigger?
	* detecting bad failure and recovering from it
	* need to hook in error recovery that recomputes all from scratch (when I can't trust state)
	* ... but how do we know which plugins comps should be inactive vs. be reinstalled?
	* do own writing with EOF marker to detect bad state files
	* install.properties ... write .bak, delete old, rename .bak, write state
	* batch uninstall

	* UI: to call flush() after every batch of updates (eg. on exit from preference page)
*/
}

synchronized public void uninstall() {
	
	if (baseurl==null || !isFileProtocol(baseurl)) return;
	if (this!=LaunchInfo.getCurrent()) return;
	
	History [] history = getLaunchInfoHistory();
	if (history.length <= historyCount) return;

	LaunchInfo historyInfo;
	Set candidateConfigs = new HashSet();
	Set candidateComps = new HashSet();
	Set candidatePlugins = new HashSet();
	Set candidateFragments = new HashSet();
	
	for (int i=0; i<(history.length-historyCount); i++) {
		historyInfo = new LaunchInfo(history[i]);
		uninstallGetCandidates(candidateConfigs, historyInfo.configs, historyInfo.configsInact);
		uninstallGetCandidates(candidateComps, historyInfo.comps, historyInfo.compsInact);
		uninstallGetCandidates(candidatePlugins, historyInfo.plugins, historyInfo.pluginsInact);
		uninstallGetCandidates(candidateFragments, historyInfo.fragments, historyInfo.fragmentsInact);
	}

	Set deleteConfigs = new HashSet();
	Set deleteComps = new HashSet();
	Set deletePlugins = new HashSet();
	Set deleteFragments = new HashSet();
	
	for (int i=historyCount; i<history.length; i++) {
		historyInfo = new LaunchInfo(history[i]);
		uninstallMarkForDeletion(deleteConfigs, candidateConfigs, historyInfo.configs, historyInfo.configsInact);
		uninstallMarkForDeletion(deleteComps, candidateComps, historyInfo.comps, historyInfo.compsInact);
		uninstallMarkForDeletion(deletePlugins, candidatePlugins, historyInfo.plugins, historyInfo.pluginsInact);
		uninstallMarkForDeletion(deleteFragments, candidateFragments, historyInfo.fragments, historyInfo.fragmentsInact);
	}

	VersionedIdentifier[] vidConfigs = (VersionedIdentifier[])deleteConfigs.toArray();
	VersionedIdentifier[] vidComps = (VersionedIdentifier[]) deleteComps.toArray();
	VersionedIdentifier[] vidPlugins = (VersionedIdentifier[]) deletePlugins.toArray();
	VersionedIdentifier[] vidFragments = (VersionedIdentifier[]) deleteFragments.toArray();

	uninstall(vidConfigs, vidComps, vidPlugins, vidFragments);
}
/**
 * deprecated
 */
synchronized public void uninstall(String[] configId, String[] compId, String[] pluginId, String[] fragId) {

	if (baseurl==null || !isFileProtocol(baseurl)) return;
	if (this!=LaunchInfo.getCurrent()) return;
	
	if (DEBUG) {
		debug("Deleting configurations");
		for (int i=0; i<configId.length; i++) debug("   "+configId[i]);
		debug("Deleting components");
		for (int i=0; i<compId.length; i++) debug("   "+compId[i]);
		debug("Deleting plugins");
		for (int i=0; i<pluginId.length; i++) debug("   "+pluginId[i]);
		debug("Deleting fragments");
		for (int i=0; i<fragId.length; i++) debug("   "+fragId[i]);
	}
}

synchronized public void uninstall(VersionedIdentifier[] configId, VersionedIdentifier[] compId, VersionedIdentifier[] pluginId, VersionedIdentifier[] fragId) {

	if (installurl==null || !isFileProtocol(installurl)) return;
	if (this!=LaunchInfo.getCurrent()) return;
	
	if (DEBUG) {
		debug("Deleting configurations");
		for (int i=0; i<configId.length; i++) debug("   "+configId[i].toString());
		debug("Deleting components");
		for (int i=0; i<compId.length; i++) debug("   "+compId[i].toString());
		debug("Deleting plugins");
		for (int i=0; i<pluginId.length; i++) debug("   "+pluginId[i].toString());
		debug("Deleting fragments");
		for (int i=0; i<fragId.length; i++) debug("   "+fragId[i].toString());
	}
	
	String root = installurl.getFile().replace('/',File.separatorChar);
	File dir;
	
	// uninstall configurations
	for (int i=0; i<configId.length; i++) {
		dir = new File(root+INSTALL_INFO_DIR+CONFIGSDIR+configId[i].toString()+File.separator);
		uninstall(dir);
	}

	// unistall components
	for (int i=0; i<compId.length; i++) {
		dir = new File(root+INSTALL_INFO_DIR+COMPSDIR+compId[i].toString()+File.separator);
		uninstall(dir);
	}

	// uninstall plugins
	for (int i=0; i<pluginId.length; i++) {
		dir = new File(root+PLUGINSDIR+pluginId[i].toString()+File.separator);
		uninstall(dir);
	}

	// uninstall fragments
	for (int i=0; i<fragId.length; i++) {
		dir = new File(root+FRAGMENTSDIR+fragId[i].toString()+File.separator);
		uninstall(dir);
	}
}

private void uninstall(File f) {

	if (f.isDirectory()) {
		File[] list = f.listFiles();
		if (list!=null) {
			for (int i=0; i<list.length; i++)
				uninstall(list[i]);
		}
	}
	
	boolean ok = f.delete();
	if (DEBUG)
		debug((ok?"Unistalled ":"Unable to uninstall ")+f.toString());
}

private void uninstallGetCandidates(Set candidates, List active, List inactive) {
	candidates.addAll(active);
	candidates.addAll(inactive);
}

private void uninstallMarkForDeletion(Set delete, Set candidates, List active, List inactive) {
	VersionedIdentifier vid;
	Iterator all = candidates.iterator();
	while(all.hasNext()) {
		vid = (VersionedIdentifier) all.next();
		if (!active.contains(vid) && !inactive.contains(vid)) {
			delete.add(vid);
		}
	}
}

synchronized private void write(String id, PrintWriter w) throws IOException {

		w.println(ID+"="+id);
		w.println(PLATFORM+"="+platform);
		w.println(APP+"="+app);
		w.println(APP_CONFIG+"="+appconfig);
		w.println(HISTORY_COUNT+"="+Integer.toString(historyCount));
		
		writeList(w, configs, CONFIG_ACT);
		writeList(w, configsInact, CONFIG_INACT);
		writeList(w, comps, COMP_ACT);
		writeList(w, compsInact, COMP_INACT);
		writeList(w, compsDang, COMP_DANG);
		writeList(w, plugins, PLUGIN_ACT);
		writeList(w, pluginsInact, PLUGIN_INACT);
		writeList(w, pluginsUnmgd, PLUGIN_UNMGD);
		writeList(w, fragments, FRAG_ACT);
		writeList(w, fragmentsInact, FRAG_INACT);
		writeList(w, fragmentsUnmgd, FRAG_UNMGD);

		w.println(EOF_MARKER);
}

private void writeList(PrintWriter w, List list, String id) throws IOException {
	
	if (list==null || list.size() <= 0) return;
	for (int i=0; LIST_SIZE*i<list.size(); i++) {
		String prop = "";
		for (int j=0; j<10 && LIST_SIZE*i+j<list.size() ; j++) {
			if (j!=0) prop += ",";
			prop += list.get(LIST_SIZE*i+j).toString();
		}
		w.println(id+"."+i+"="+prop);
	}
}

synchronized private void writeSummary(String id, PrintWriter w) throws IOException {
	w.println(ID+"="+id);
	w.println(PLATFORM+"="+platform);
	w.println(APP+"="+app);
	w.println(APP_CONFIG+"="+appconfig);
}
}
