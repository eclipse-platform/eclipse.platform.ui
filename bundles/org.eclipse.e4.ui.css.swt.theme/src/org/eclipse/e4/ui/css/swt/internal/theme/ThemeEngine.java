/*******************************************************************************
 * Copyright (c) 2010, 2018 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Brian de Alwis - added support for multiple CSS engines
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *     IBM Corporation - initial API and implementation
 *     Lucas Bullen (Red Hat Inc.) - [Bug 527806] Ship all themes for all OS & WS
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.internal.theme;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.util.impl.resources.FileResourcesLocatorImpl;
import org.eclipse.e4.ui.css.core.util.impl.resources.OSGiResourceLocator;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;

public class ThemeEngine implements IThemeEngine {
	private List<Theme> themes = new ArrayList<>();
	private Map<String, List<String>> themesToVarients = new HashMap<>();
	private List<CSSEngine> cssEngines = new ArrayList<>();

	// kept for theme notifications only
	private Display display;

	private ITheme currentTheme;

	private List<String> globalStyles = new ArrayList<>();
	private List<IResourceLocator> globalSourceLocators = new ArrayList<>();

	private HashMap<String, List<String>> stylesheets = new HashMap<>();
	private HashMap<String, List<String>> modifiedStylesheets = new HashMap<>();
	private HashMap<String, List<IResourceLocator>> sourceLocators = new HashMap<>();

	private static final String THEMEID_KEY = "themeid";

	public static final String THEME_PLUGIN_ID = "org.eclipse.e4.ui.css.swt.theme";

	public static final String E4_DARK_THEME_ID = "org.eclipse.e4.ui.css.theme.e4_dark";

	public ThemeEngine(Display display) {
		this.display = display;

		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry
				.getExtensionPoint("org.eclipse.e4.ui.css.swt.theme");

		//load any modified style sheets
		Location configLocation = org.eclipse.core.runtime.Platform.getConfigurationLocation();
		String e4CSSPath = null;
		try {
			URL locationURL = new URL(configLocation.getDataArea(ThemeEngine.THEME_PLUGIN_ID).toString());
			File locationFile = new File(locationURL.getFile());
			e4CSSPath = locationFile.getPath();
		} catch (IOException e1) {
		}

		IPath path = new Path(e4CSSPath + File.separator);
		File modDir= new File(path.toFile().toURI());
		if (!modDir.exists()) {
			modDir.mkdirs();
		}

		File[] modifiedFiles = modDir.listFiles();
		String currentOS = Platform.getOS();
		String currentWS = Platform.getWS();
		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				if (ce.getName().equals("theme")) {
					try {
						String id = ce.getAttribute("id");
						String os = ce.getAttribute("os");
						String version = ce.getAttribute("os_version");

						if (version == null) {
							version ="";
						} else {
							// For e4 dark theme on Mac, register the theme with matching OS version only
							if (E4_DARK_THEME_ID.equals(id) && Platform.OS_MACOSX.equals(currentOS) && os != null
									&& os.equals(currentOS)) {
								if (!isOsVersionMatch(version)) {
									continue;
								} else {
									version = "";
								}
							}
						}

						final String themeBaseId = id + version;
						String themeId = themeBaseId;
						String label = ce.getAttribute("label");
						String ws = ce.getAttribute("ws");
						if ((os != null && !os.equals(currentOS)) || (ws != null && !ws.equals(currentWS))) {
							if (!themesToVarients.containsKey(themeBaseId)) {
								themesToVarients.put(themeBaseId, new ArrayList<>());
							}
							themeId = getVarientThemeId(themeBaseId, os, ws);
							themesToVarients.get(themeBaseId).add(themeId);
							label = getVarientThemeLabel(label, os, ws);
						}
						String originalCSSFile;
						String basestylesheeturi = originalCSSFile = ce.getAttribute("basestylesheeturi");
						if (!basestylesheeturi.startsWith("platform:/plugin/")) {
							basestylesheeturi = "platform:/plugin/" + ce.getContributor().getName() + "/"
									+ basestylesheeturi;
						}
						registerTheme(themeId, label, basestylesheeturi, version);

						//check for modified files
						if (modifiedFiles != null) {
							int slash = originalCSSFile.lastIndexOf('/');
							if (slash != -1) {
								originalCSSFile = originalCSSFile.substring(slash + 1);
								for (File modifiedFile : modifiedFiles) {
									String modifiedFileName = modifiedFile.getName();
									if (modifiedFileName.contains(".css") && modifiedFileName.equals(originalCSSFile)) {  //$NON-NLS-1$
										//								modifiedStylesheets
										ArrayList<String> styleSheets = new ArrayList<>();
										styleSheets.add(modifiedFile.toURI().toString());
										modifiedStylesheets.put(themeId, styleSheets);
									}
								}
							}
						}
					} catch (IllegalArgumentException e1) {
						ThemeEngineManager.logError(e1.getMessage(), e1);
					}
				}
			}
		}

		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				if (ce.getName().equals("stylesheet")) {
					IConfigurationElement[] cces = ce.getChildren("themeid");
					if (cces.length == 0) {
						registerStylesheet("platform:/plugin/"
								+ ce.getContributor().getName() + "/"
								+ ce.getAttribute("uri"));

						for (IConfigurationElement resourceEl : ce
								.getChildren("osgiresourcelocator")) {
							String uri = resourceEl.getAttribute("uri");
							if (uri != null) {
								registerResourceLocator(new OSGiResourceLocator(
										uri));
							}
						}
					} else {
						List<String> themes = new ArrayList<>();
						for (IConfigurationElement cce : cces) {
							String refid = cce.getAttribute("refid");
							List<String> varientOSList = themesToVarients.get(refid);
							if (varientOSList != null) {
								themes.addAll(varientOSList);
							}
							themes.add(refid);
						}
						registerStylesheet(
								"platform:/plugin/" + ce.getContributor().getName() + "/" + ce.getAttribute("uri"),
								themes.toArray(new String[themes.size()]));
						for (IConfigurationElement resourceEl : ce
								.getChildren("osgiresourcelocator")) {
							String uri = resourceEl.getAttribute("uri");
							if (uri != null) {
								registerResourceLocator(new OSGiResourceLocator(
										uri));
							}
						}
					}
				}
			}
		}

		// register a default resolver for platform uri's
		registerResourceLocator(new OSGiResourceLocator(
				"platform:/plugin/org.eclipse.ui.themes/css/"));
		// register a default resolver for file uri's
		registerResourceLocator(new FileResourcesLocatorImpl());
		// FIXME: perhaps ResourcesLocatorManager shouldn't have a default?
		// registerResourceLocator(new HttpResourcesLocatorImpl());
	}

	private String getVarientThemeId(String id, String os, String ws) {
		if (os != null) {
			id += '_' + os;
		}
		if (ws != null) {
			id += '-' + ws;
		}
		return id;
	}

	private String getVarientThemeLabel(String label, String os, String ws) {
		String currentOS = Platform.getOS();
		String currentWS = Platform.getWS();
		if (os != null && !os.equals(currentOS)) {
			String osName;
			switch (os) {
			case Platform.OS_LINUX:		osName="Linux";break;
			case Platform.OS_MACOSX:	osName="Mac OS X";break;
			case Platform.OS_WIN32:		osName="Windows";break;
			default:					osName=os;break;
			}
			label += " [" + osName;
		}
		if (ws != null && !ws.equals(currentWS)) {
			String wsName;
			switch (ws) {
			case Platform.WS_COCOA:		wsName="Cocoa";break;
			case Platform.WS_GTK:		wsName="GTK";break;
			case Platform.WS_WPF:		wsName="WPF";break;
			default:					wsName=ws;break;
			}
			label += " - " + wsName;
		}
		if (os != null && !os.equals(currentOS)) {
			label += "]";
		}
		return label;
	}

	private boolean isOsVersionMatch(String osVersionList) {
		boolean found = false;
		String osVersion = System.getProperty("os.version");
		if (osVersion != null) {
			if (osVersionList != null) {
				String[] osVersions = osVersionList.split(","); //$NON-NLS-1$
				for (String osVersionFromTheme : osVersions) {
					if (osVersionFromTheme != null) {
						if (osVersion.contains(osVersionFromTheme)) {
							found = true;
							break;
						}
					}
				}
			}
		}
		return found;
	}

	@Override
	public synchronized ITheme registerTheme(String id, String label, String basestylesheetURI)
			throws IllegalArgumentException {
		return  registerTheme(id, label, basestylesheetURI, "");
	}

	public synchronized ITheme registerTheme(String id, String label,
			String basestylesheetURI, String osVersion) throws IllegalArgumentException {
		for (Theme t : themes) {
			if (t.getId().equals(id)) {
				throw new IllegalArgumentException("A theme with the id '" + id
						+ "' is already registered");
			}
		}
		Theme theme = new Theme(id, label);
		if (osVersion != "") {
			theme.setOsVersion(osVersion);
		}
		themes.add(theme);
		registerStyle(id, basestylesheetURI);
		return theme;
	}

	@Override
	public synchronized void registerStylesheet(String uri, String... themes) {
		Bundle bundle = FrameworkUtil.getBundle(ThemeEngine.class);
		String osname = bundle.getBundleContext().getProperty("osgi.os");
		String wsname = bundle.getBundleContext().getProperty("osgi.ws");

		uri = uri.replaceAll("\\$os\\$", osname).replaceAll("\\$ws\\$", wsname);

		if (themes.length == 0) {
			globalStyles.add(uri);
		} else {
			for (String t : themes) {
				registerStyle(t, uri);
			}
		}
	}

	@Override
	public synchronized void registerResourceLocator(IResourceLocator locator,
			String... themes) {
		if (themes.length == 0) {
			globalSourceLocators.add(locator);
		} else {
			for (String t : themes) {
				List<IResourceLocator> list = sourceLocators.get(t);
				if (list == null) {
					list = new ArrayList<>();
					sourceLocators.put(t, list);
				}
				list.add(locator);
			}
		}
	}

	private void registerStyle(String id, String stylesheet) {
		List<String> s = stylesheets.get(id);
		if (s == null) {
			s = new ArrayList<>();
			stylesheets.put(id, s);
		}
		s.add(stylesheet);
	}

	private List<String> getAllStyles(String id) {
		// check for any modifications first
		List<String> m = modifiedStylesheets.get(id);
		if (m != null) {
			m = new ArrayList<>(m);
			m.addAll(globalStyles);
			return m;
		}

		List<String> s = stylesheets.get(id);
		if (s == null) {
			s = Collections.emptyList();
		}

		s = new ArrayList<>(s);
		s.addAll(globalStyles);
		return s;

	}

	private List<IResourceLocator> getResourceLocators(String id) {
		List<IResourceLocator> list = new ArrayList<>(
				globalSourceLocators);
		List<IResourceLocator> s = sourceLocators.get(id);
		if (s != null) {
			list.addAll(s);
		}

		return list;
	}

	@Override
	public void setTheme(String themeId, boolean restore) {
		String osVersion = System.getProperty("os.version");
		if (osVersion != null) {
			boolean found = false;
			for (Theme t : themes) {
				String osVersionList = t.getOsVersion();
				if (osVersionList != null) {
					String[] osVersions = osVersionList.split(","); //$NON-NLS-1$
					for (String osVersionFromTheme : osVersions) {
						if (osVersionFromTheme != null && osVersion.contains(osVersionFromTheme)) {
							String themeVersion = themeId + osVersionList;
							if (t.getId().equals(themeVersion)) {
								setTheme(t, restore);
								found = true;
								break;
							}
						}
					}
				}
			}
			if (found) {
				return;
			}
		}
		//try generic
		for (Theme t : themes) {
			if (t.getId().equals(themeId)) {
				setTheme(t, restore);
				break;
			}
		}
	}

	@Override
	public void setTheme(ITheme theme, boolean restore) {
		setTheme(theme, restore, false);
	}

	public void setTheme(ITheme theme, boolean restore, boolean force) {
		Assert.isNotNull(theme, "The theme must not be null");

		if (this.currentTheme != theme || force) {
			if (currentTheme != null) {
				for (IResourceLocator l : getResourceLocators(currentTheme
						.getId())) {
					for (CSSEngine engine : cssEngines) {
						engine.getResourcesLocatorManager()
						.unregisterResourceLocator(l);
					}
				}
			}

			this.currentTheme = theme;
			for (CSSEngine engine : cssEngines) {
				engine.reset();
			}

			for (IResourceLocator l : getResourceLocators(theme.getId())) {
				for (CSSEngine engine : cssEngines) {
					engine.getResourcesLocatorManager()
					.registerResourceLocator(l);
				}
			}
			for (String stylesheet : getAllStyles(theme.getId())) {
				URL url;
				InputStream stream = null;
				try {
					url = FileLocator.resolve(new URL(stylesheet.toString()));
					for (CSSEngine engine : cssEngines) {
						try {
							stream = url.openStream();
							InputSource source = new InputSource();
							source.setByteStream(stream);
							source.setURI(url.toString());
							engine.parseStyleSheet(source);
						} catch (IOException e) {
							ThemeEngineManager.logError(e.getMessage(), e);
						} finally {
							if (stream != null) {
								try {
									stream.close();
								} catch (IOException e) {
									ThemeEngineManager.logError(e.getMessage(), e);
								}
							}
						}
					}
				} catch (IOException e) {
					ThemeEngineManager.logError(e.getMessage(), e);
				}
			}
		}

		if (restore) {
			IEclipsePreferences pref = getPreferences();
			EclipsePreferencesHelper.setPreviousThemeId(pref.get(THEMEID_KEY, null));
			EclipsePreferencesHelper.setCurrentThemeId(theme.getId());

			pref.put(THEMEID_KEY, theme.getId());
			try {
				pref.flush();
			} catch (BackingStoreException e) {
				ThemeEngineManager.logError(e.getMessage(), e);
			}
		}
		sendThemeChangeEvent(restore);

		for (CSSEngine engine : cssEngines) {
			engine.reapply();
		}
	}

	/**
	 * Broadcast theme-change event using OSGi Event Admin.
	 */
	private void sendThemeChangeEvent(boolean restore) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null) {
			return;
		}
		Map<String, Object> data = new HashMap<>();
		data.put(IThemeEngine.Events.THEME_ENGINE, this);
		data.put(IThemeEngine.Events.THEME, currentTheme);
		data.put(IThemeEngine.Events.DEVICE, display);
		data.put(IThemeEngine.Events.RESTORE, restore);
		Event event = new Event(IThemeEngine.Events.THEME_CHANGED, data);
		eventAdmin.sendEvent(event); // synchronous
	}

	public List<IResourceLocator> getCurrentResourceLocators() {
		if(currentTheme == null) { return Collections.emptyList(); }
		return getResourceLocators(currentTheme.getId());
	}

	private EventAdmin getEventAdmin() {
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		if (bundle == null) {
			return null;
		}
		BundleContext context = bundle.getBundleContext();
		ServiceReference<EventAdmin> eventAdminRef = context.getServiceReference(EventAdmin.class);
		return context.getService(eventAdminRef);
	}

	@Override
	public synchronized List<ITheme> getThemes() {
		return Collections.unmodifiableList(new ArrayList<ITheme>(themes));
	}

	@Override
	public void applyStyles(Object widget, boolean applyStylesToChildNodes) {
		for (CSSEngine engine : cssEngines) {
			Object element = engine.getElement(widget);
			if (element != null) {
				engine.applyStyles(element, applyStylesToChildNodes);
			}
		}
	}

	private String getPreferenceThemeId() {
		return getPreferences().get(THEMEID_KEY, null);
	}

	private IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(FrameworkUtil.getBundle(ThemeEngine.class).getSymbolicName());
	}

	void copyFile(String from, String to) throws IOException {
		FileInputStream fStream = null;
		BufferedOutputStream outputStream = null;
		try {
			fStream = new FileInputStream(from);
			outputStream = new BufferedOutputStream(new FileOutputStream(to));
			byte[] buffer = new byte[4096];
			int c;
			while ((c = fStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, c);
			}

		} finally {
			if (fStream != null) {
				fStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	@Override
	public void restore(String alternateTheme) {
		String prefThemeId = getPreferenceThemeId();
		boolean flag = true;
		if (prefThemeId != null) {
			for (ITheme t : getThemes()) {
				if (prefThemeId.equals(t.getId())) {
					setTheme(t, false);
					flag = false;
					break;
				}
			}
		}

		if (alternateTheme != null && flag) {
			setTheme(alternateTheme, false);
		}
	}

	@Override
	public ITheme getActiveTheme() {
		return currentTheme;
	}

	@Override
	public CSSStyleDeclaration getStyle(Object widget) {
		for (CSSEngine engine : cssEngines) {
			CSSElementContext context = engine.getCSSElementContext(widget);
			if (context != null) {
				Element e = context.getElement();
				if (e != null) {
					return engine.getViewCSS().getComputedStyle(e, null);
				}
			}
		}
		return null;
	}

	public List<String> getStylesheets(ITheme selection) {
		List<String> ss  = stylesheets.get(selection.getId());
		return ss == null ? new ArrayList<>() : ss;
	}

	public void themeModified(ITheme theme, List<String> paths) {
		modifiedStylesheets.put(theme.getId(), paths);
		setTheme(theme, false, true);
	}

	public void resetCurrentTheme() {
		if (currentTheme != null) {
			setTheme(currentTheme, false, true);
		}
	}

	public List<String> getModifiedStylesheets(ITheme selection) {
		List<String> ss  = modifiedStylesheets.get(selection.getId());
		return ss == null ? new ArrayList<>() : ss;
	}

	public void resetModifiedStylesheets(ITheme selection) {
		modifiedStylesheets.remove(selection.getId());
	}

	@Override
	public void addCSSEngine(CSSEngine cssEngine) {
		cssEngines.add(cssEngine);
		resetCurrentTheme();
	}

	public Collection<CSSEngine> getCSSEngines() {
		return cssEngines;
	}

	@Override
	public void removeCSSEngine(CSSEngine cssEngine) {
		cssEngines.remove(cssEngine);
	}
}
