/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.*;
import org.osgi.framework.Constants;
import org.xml.sax.SAXException;

public class PluginConverter implements IPluginConverter {
	private static final String LEGACY = "Legacy"; //$NON-NLS-1$
	private static final String PLUGIN = "Plugin-Class"; //$NON-NLS-1$

	protected String devPathSpec;
	private IPluginInfo pluginInfo;
	private File bundleManifestLocation;
	private File pluginLocation;
	private PrintWriter out;

	private static final String[] ARCH_LIST = { org.eclipse.osgi.service.environment.Constants.ARCH_PA_RISC, org.eclipse.osgi.service.environment.Constants.ARCH_PPC, org.eclipse.osgi.service.environment.Constants.ARCH_SPARC, org.eclipse.osgi.service.environment.Constants.ARCH_X86 };
	private static final String[] OS_LIST =
		{
			org.eclipse.osgi.service.environment.Constants.OS_AIX,
			org.eclipse.osgi.service.environment.Constants.OS_HPUX,
			org.eclipse.osgi.service.environment.Constants.OS_LINUX,
			org.eclipse.osgi.service.environment.Constants.OS_MACOSX,
			org.eclipse.osgi.service.environment.Constants.OS_QNX,
			org.eclipse.osgi.service.environment.Constants.OS_SOLARIS,
			org.eclipse.osgi.service.environment.Constants.OS_WIN32 };
	private static final String[] WS_LIST =
		{ org.eclipse.osgi.service.environment.Constants.WS_CARBON, org.eclipse.osgi.service.environment.Constants.WS_GTK, org.eclipse.osgi.service.environment.Constants.WS_MOTIF, org.eclipse.osgi.service.environment.Constants.WS_PHOTON, org.eclipse.osgi.service.environment.Constants.WS_WIN32 };

	public PluginConverter() {
		super();
		devPathSpec = System.getProperty("osgi.dev");
	}

	public void convertManifest(IPluginInfo pluginInfo, File pluginLocation, File bundleManifestLocation) {
		this.pluginInfo = pluginInfo;
		this.pluginLocation = pluginLocation;
		this.bundleManifestLocation = bundleManifestLocation;
		generate();
	}

	public boolean convertManifest(File pluginLocation, File bundleManifestLocation) {
		/* parse the plugin manifest to find out:
		 * - the plug-in unique identifier
		 * - the plug-in version
		 * - runtime/libraries entries
		 * - the plug-in class
		 * - the master plugin (for a fragment)
		 */
		this.pluginLocation = pluginLocation;
		this.bundleManifestLocation = bundleManifestLocation;
		File pluginManifestLocation = new File(pluginLocation, "plugin.xml"); //$NON-NLS-1$
		if (!pluginManifestLocation.isFile()) {
			pluginManifestLocation = new File(pluginLocation, "fragment.xml"); //$NON-NLS-1$
			if (!pluginManifestLocation.isFile())
				// location does not contain a plugin manifest
				return false;
		}
		try {
			pluginInfo = new PluginParser().parse(pluginManifestLocation.toString());
			generate();
		} catch (IOException e) {
			//TODO: what to do here? 
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			//TODO: what to do here?		
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected void generate() {
		openFile();
		generateHeaders();
		generateClasspath();
		generateLegacy();
		generateActivator();
		generatePluginClass();
		generateProvidePackage();
		generateImports();
		generateRequireBundle();
		closeFile();
	}

	private void generateRequireBundle() {
		String[] requiredBundles = pluginInfo.getRequires();
		if (requiredBundles == null && !pluginInfo.getUniqueId().equals(IPlatform.PI_RUNTIME)) // to avoid cycles
			requiredBundles = new String[] { IPlatform.PI_RUNTIME };

		writeEntry(Constants.REQUIRE_BUNDLE, requiredBundles);
	}

	private void generateLegacy() {
		writeEntry(LEGACY, "true"); //$NON-NLS-1$
	}

	private void generateImports() {
		// TODO We should not need DynamicImport-Package or Import-Package
		// writeEntry(Constants.DYNAMICIMPORT_PACKAGE, "*");
	}

	private void openFile() {
		try {
			bundleManifestLocation.getParentFile().mkdirs();
			bundleManifestLocation.createNewFile();
		} catch (IOException e) {
			//TODO Log an error
			System.err.println("error creating directory writing manifest for " + pluginInfo.getUniqueId());
			return;
		}
		BufferedOutputStream outputFile = null;
		try {
			outputFile = new BufferedOutputStream(new FileOutputStream(bundleManifestLocation));
		} catch (FileNotFoundException e) {
			//TODO Log an error
			System.err.println("error writing manifest for " + pluginInfo.getUniqueId());
			return;
		}
		out = new PrintWriter(outputFile);
		// so it is easy to tell which ones are generated
		out.println("Generated: true");
	}

	private void writeEntry(String key, Collection value) {
		if (value == null || value.size() == 0)
			return;
		if (value.size() == 1) {
			out.println(key + ": " + value.iterator().next()); //$NON-NLS-1$
			return;
		}
		key = key + ": "; //$NON-NLS-1$
		out.println(key);
		out.print(' ');
		boolean first = true;
		for (Iterator i = value.iterator(); i.hasNext();) {
			if (first)
				first = false;
			else {
				out.println(',');
				out.print(' ');
			}
			out.print(i.next());
		}
		out.println();
	}

	private void writeEntry(String key, String[] value) {
		if (value == null || value.length == 0)
			return;
		if (value.length == 1) {
			out.println(key + ": " + value[0]); //$NON-NLS-1$
			return;
		}
		key = key + ": "; //$NON-NLS-1$
		out.println(key);
		out.print(' ');
		boolean first = true;
		for (int i = 0; i < value.length; i++) {
			if (first)
				first = false;
			else {
				out.println(',');
				out.print(' ');
			}
			out.print(value[i]);
		}
		out.println();
	}

	private void writeEntry(String key, String value) {
		if (value != null && value.length() > 0)
			out.println(key + ": " + value); //$NON-NLS-1$
	}

	private void closeFile() {
		out.close();
	}

	private void generateHeaders() {
		writeEntry(Constants.BUNDLE_NAME, pluginInfo.getUniqueId());
		writeEntry(Constants.BUNDLE_VERSION, pluginInfo.getVersion());
		writeEntry(Constants.BUNDLE_GLOBALNAME, pluginInfo.getUniqueId());
	}
	private void generateProvidePackage() {
		StringBuffer providePackage = new StringBuffer();
		Set exports = getExports();
		if (exports != null) {
			Iterator iter = exports.iterator();
			boolean firstPkg = true;
			while (iter.hasNext()) {
				String pkg = (String) iter.next();
				if (firstPkg) {
					providePackage.append("\n "); //$NON-NLS-1$
					firstPkg = false;
				} else {
					providePackage.append(",\n "); //$NON-NLS-1$
				}
				providePackage.append(pkg);
			}
			writeEntry(Constants.PROVIDE_PACKAGE, providePackage.toString());
		}

		if (pluginInfo.isFragment()) {
			StringBuffer hostBundle = new StringBuffer();

			hostBundle.append(pluginInfo.getMasterId()).append("; ");
			hostBundle.append(Constants.BUNDLE_VERSION_ATTRIBUTE).append("=");
			hostBundle.append(pluginInfo.getMasterVersion());

			writeEntry(Constants.HOST_BUNDLE, hostBundle.toString());
		}
	}

	private ArrayList getLibrariesExpandingVariables(String libraryPath, boolean filter) {
		String var = hasPrefix(libraryPath);
		if (var == null) {
			ArrayList returnValue = new ArrayList(1);
			returnValue.add(libraryPath);
			return returnValue;
		}
		if (var.equals("ws")) { //$NON-NLS-1$
			return findWSJars(pluginLocation, libraryPath, filter);
		}
		if (var.equals("os")) { //$NON-NLS-1$
			return findOSJars(pluginLocation, libraryPath, filter);
		}
		return new ArrayList(0);
	}

	private void generateClasspath() {
		String[] libraries = pluginInfo.getLibrariesName();
		ArrayList expandedPath = new ArrayList();
		for (int i = 0; i < libraries.length; i++) {
			expandedPath.addAll(getLibrariesExpandingVariables(libraries[i], true));
		}
		writeEntry(Constants.BUNDLE_CLASSPATH, expandedPath);
	}

	private ArrayList findWSJars(File pluginRoot, String path, boolean filter) {
		path = path.substring(4);
		ArrayList found = new ArrayList(0);
		for (int i = 0; i < WS_LIST.length; i++) {
			String searchedPath = "ws/" + WS_LIST[i] + path; //$NON-NLS-1$
			if (new File(pluginRoot, searchedPath).exists()) {
				found.add(searchedPath + (filter ? ";(ws=" + WS_LIST[i] + ")" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return found;
	}

	private ArrayList findOSJars(File pluginRoot, String path, boolean filter) {
		path = path.substring(4);
		ArrayList found = new ArrayList(0);
		for (int i = 0; i < OS_LIST.length; i++) {
			//look for os/osname/path
			String searchedPath = "os/" + OS_LIST[i] + "/" + path; //$NON-NLS-1$ //$NON-NLS-2$
			if (new File(pluginRoot, searchedPath).exists())
				found.add(searchedPath + (filter ? ";(os=" + WS_LIST[i] + ")" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			//look for os/osname/archname/path
			for (int j = 0; j < ARCH_LIST.length; j++) {
				searchedPath = "os/" + OS_LIST[i] + "/" + ARCH_LIST[j] + "/" + path; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (new File(pluginRoot, searchedPath).exists()) {
					found.add(searchedPath + (filter ? ";(& (os=" + WS_LIST[i] + ") (arch=" + ARCH_LIST[j] + ")" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
			}
		}
		return found;
	}
	//return a String representing the string found between the $s
	private String hasPrefix(String libPath) {
		if (libPath.startsWith("$ws$")) //$NON-NLS-1$
			return "ws"; //$NON-NLS-1$
		if (libPath.startsWith("$os$")) //$NON-NLS-1$
			return "os"; //$NON-NLS-1$
		if (libPath.startsWith("$nl$")) //$NON-NLS-1$
			return "nl"; //$NON-NLS-1$
		return null;
	}

	private void generateActivator() {
		if (!pluginInfo.isFragment())
			writeEntry(Constants.BUNDLE_ACTIVATOR, "org.eclipse.core.internal.compatibility.PluginActivator"); //$NON-NLS-1$
	}

	private void generatePluginClass() {
		writeEntry(PLUGIN, pluginInfo.getPluginClass());
	}

	private Set filterExport(Collection exportToFilter, Collection filter) {
		if (filter == null || filter.contains("*")) //$NON-NLS-1$
			return (Set) exportToFilter;

		Set filteredExport = new HashSet(exportToFilter.size());
		for (Iterator iter = exportToFilter.iterator(); iter.hasNext();) {
			String anExport = (String) iter.next();

			for (Iterator iter2 = filter.iterator(); iter2.hasNext();) {
				String aFilter = (String) iter2.next();
				if (anExport.startsWith(aFilter)) {
					filteredExport.add(anExport);
				}
			}
		}
		return filteredExport;
	}

	private Set getExportsFromJAR(File jarFile) {
		Set names = new HashSet();
		JarFile file = null;
		try {
			file = new JarFile(jarFile);
		} catch (IOException e) {
			//TODO Log a warning
			System.err.println("Ignore " + jarFile);
			return names;
		}

		//Run through the entries
		for (Enumeration enum = file.entries(); enum.hasMoreElements();) {
			JarEntry entry = (JarEntry) enum.nextElement();
			String name = entry.getName();

			if (! isValidPackageName(name))
				break;
			
			int lastSlash = name.lastIndexOf("/"); //$NON-NLS-1$
			//Ignore folders that do not contain files
			if (lastSlash != -1 && lastSlash != name.length() - 1) {
				if (name.lastIndexOf(' ') == -1)
					names.add(name.substring(0, lastSlash).replace('/', '.'));
			}
		}
		return names;
	}

	private boolean isValidPackageName(String name) {
		if (name.indexOf(' ') > 0 || name.equalsIgnoreCase("meta-inf")) //$NON-NLS-1$
			return false;
		return true;
	}

	private Set getExportsFromDir(File location) {
		return getExportsFromDir(location, ""); //$NON-NLS-1$
	}

	private Set getExportsFromDir(File location, String packageName) {
		String prefix = (packageName.length() > 0) ? (packageName + '.') : ""; //$NON-NLS-1$
		File[] files = location.listFiles();
		Set exportedPaths = new HashSet();
		boolean containsFile = false;
		for (int i = 0; i < files.length; i++) {
			if (! isValidPackageName(files[i].getName()))
				break;
			
			if (files[i].isDirectory())
				exportedPaths.addAll(getExportsFromDir(files[i], prefix + files[i].getName()));
			else
				containsFile = true;
		}
		if (containsFile && packageName.length() > 0)
			exportedPaths.add(packageName);
		return exportedPaths;
	}

	private Set getExports() {
		Map libs = pluginInfo.getLibraries();
		if (libs == null)
			return null;

		// Based on similar code from EclipseStarter
		// Check the osgi.dev property to see if dev classpath entries have been defined.
		String[] devClassPath = null;
		if (devPathSpec != null) {
			// Add each dev classpath entry
			Vector tokens = new Vector(6);
			StringTokenizer t = new StringTokenizer(devPathSpec, ","); //$NON-NLS-1$
			while (t.hasMoreTokens()) {
				String token = t.nextToken();
				if (!token.equals("")) { //$NON-NLS-1$
					tokens.addElement(token);
				}
			}
			devClassPath = new String[tokens.size()];
			tokens.toArray(devClassPath);
		}

		// add the dev. time classpath entries
		List starExport = new ArrayList(1);
		starExport.add("*"); //$NON-NLS-1$
		if (devClassPath != null) {
			for (int i = 0; i < devClassPath.length; i++) {
				libs.put(devClassPath[i], starExport);
			}
		}
		Set result = new HashSet(7);
		Set libEntries = libs.entrySet();
		for (Iterator iter = libEntries.iterator(); iter.hasNext();) {
			Map.Entry element = (Map.Entry) iter.next();
			List filter = (List) element.getValue();
			if (filter.size() == 0) //If the library is not exported, then ignore it
				continue;

			File libraryLocation = new File(pluginLocation, (String) element.getKey());
			Set exports = null;
			if (libraryLocation.exists()) {
				if (libraryLocation.isFile())
					exports = filterExport(getExportsFromJAR(libraryLocation), filter); //TODO Need to handle $xx$ variables
				else if (libraryLocation.isDirectory())
					exports = filterExport(getExportsFromDir(libraryLocation), filter);
			} else {
				ArrayList expandedLibs = getLibrariesExpandingVariables((String) element.getKey(), false);
				exports = new HashSet();
				for (Iterator iterator = expandedLibs.iterator(); iterator.hasNext();) {
					String libName = (String) iterator.next();
					File libFile = new File(pluginLocation, libName);
					if (libFile.isFile()) {
						exports.addAll(filterExport(getExportsFromJAR(libFile), filter));
					}
				}
			}

			if (exports != null) {
				result.addAll(exports);
			}
		}
		return result;
	}

}
