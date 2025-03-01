/*******************************************************************************
 * Copyright (c) 2010, 2023 Tom Schindl and others.
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
	private List<CSSEngine> cssEngines = new ArrayList<>();

	// kept for theme notifications only
	private Display display;

	private ITheme currentTheme;

	private List<String> globalStyles = new ArrayList<>();
	private List<IResourceLocator> globalSourceLocators = new ArrayList<>();

	private HashMap<String, List<String>> stylesheets = new HashMap<>();
	private HashMap<String, List<String>> stylesheetPluginExtensions = new HashMap<>();
	private HashMap<String, List<String>> modifiedStylesheets = new HashMap<>();
	private HashMap<String, List<IResourceLocator>> sourceLocators = new HashMap<>();

	private static final String THEMEID_KEY = "themeid";

	public static final String THEME_PLUGIN_ID = "org.eclipse.e4.ui.css.swt.theme";

	public static final String E4_DARK_THEME_ID = "org.eclipse.e4.ui.css.theme.e4_dark";

	public static final String DISABLE_OS_DARK_THEME_INHERIT = "org.eclipse.e4.ui.css.theme.disableOSDarkThemeInherit";

	public ThemeEngine(Display display) {
		this.display = display;

		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint(THEME_PLUGIN_ID);

		//load any modified style sheets
		Location configLocation = org.eclipse.core.runtime.Platform.getConfigurationLocation();
		String e4CSSPath = null;
		try {
			URL locationURL = new URL(configLocation.getDataArea(ThemeEngine.THEME_PLUGIN_ID).toString());
			File locationFile = new File(locationURL.getFile());
			e4CSSPath = locationFile.getPath();
		} catch (IOException e1) {
		}

		IPath path = IPath.fromOSString(e4CSSPath + File.separator);
		File modDir= new File(path.toFile().toURI());
		if (!modDir.exists()) {
			modDir.mkdirs();
		}

		File[] modifiedFiles = modDir.listFiles();
		String currentOS = Platform.getOS();
		boolean e4_dark_mac_found = false;

		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : getPlatformMatches(e.getConfigurationElements())) {
				if (ce.getName().equals("theme")) {
					try {
						String id = ce.getAttribute("id");
						String os = ce.getAttribute("os");
						String version = ce.getAttribute("os_version");

						/*
						 * Code to support e4 dark theme on Mac 10.13 and older. For e4 dark theme on
						 * Mac, register the theme with matching OS version if specified.
						 */
						if (E4_DARK_THEME_ID.equals(id) && Platform.OS_MACOSX.equals(currentOS) && os != null
								&& os.equals(currentOS)) {
							// If e4 dark theme on Mac was already registered, don't try to match or
							// register again.
							if (e4_dark_mac_found) {
								continue;
							}
							if (version != null && !isOsVersionMatch(version)) {
								continue;
							} else {
								e4_dark_mac_found = true;
								version = "";
							}
						}

						if (version == null) {
							version = "";
						}

						final String themeBaseId = id + version;
						String themeId = themeBaseId;
						String label = ce.getAttribute("label");
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
			for (IConfigurationElement ce : getPlatformMatches(e.getConfigurationElements())) {
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
						String[] themes = new String[cces.length];
						for (int i = 0; i < cces.length; i++) {
							themes[i] = cces[i].getAttribute("refid");
						}
						registerStylesheet(
								"platform:/plugin/" + ce.getContributor().getName() + "/" + ce.getAttribute("uri"),
								themes);

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
		registerStyle(id, basestylesheetURI, false);
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
				registerStyle(t, uri, true);
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

	private void registerStyle(String id, String stylesheet, boolean isStyleSheetPluginExtension) {
		List<String> s = stylesheets.get(id);
		if (s == null) {
			s = new ArrayList<>();
			stylesheets.put(id, s);
		}
		s.add(stylesheet);
		if (isStyleSheetPluginExtension) {
			s = stylesheetPluginExtensions.get(id);
			if (s == null) {
				s = new ArrayList<>();
				stylesheetPluginExtensions.put(id, s);
			}
			s.add(stylesheet);
		}
	}

	private List<String> getAllStyles(ITheme theme) {
		String id = theme.getId();
		String idWithoutVersion = null;
		if (theme instanceof Theme) {
			Theme th = (Theme) theme;
			String osVersion = th.getOsVersion();
			if (osVersion != null && osVersion.length() > 0 && id.endsWith(osVersion)) {
				idWithoutVersion = id.substring(0, id.length() - osVersion.length());
			}
		}
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
		if (idWithoutVersion != null) { // stylesheetPluginExtensions don't have a os_version; ensure that they will
			// always taken; independent from current os_version
			List<String> stylesheetPluginExtensionList = stylesheetPluginExtensions.get(idWithoutVersion);
			if (stylesheetPluginExtensionList != null && stylesheetPluginExtensionList.size() > 0) {
				s = new ArrayList<>(s);
				for (String styleSheet : stylesheetPluginExtensionList) {
					if (!s.contains(styleSheet)) {
						s.add(styleSheet);
					}
				}

			}
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

	/**
	 * Get all elements that have os/ws attributes that best match the current
	 * platform.
	 *
	 * @param elements the elements to check
	 * @return the best matches, if any
	 */
	private IConfigurationElement[] getPlatformMatches(IConfigurationElement[] elements) {
		Bundle bundle = FrameworkUtil.getBundle(ThemeEngine.class);
		String osname = bundle.getBundleContext().getProperty("osgi.os");
		// TODO: Need to differentiate win32 versions
		String wsname = bundle.getBundleContext().getProperty("osgi.ws");
		ArrayList<IConfigurationElement> matchingElements = new ArrayList<>();
		for (IConfigurationElement element : elements) {
			String elementOs = element.getAttribute("os");
			String elementWs = element.getAttribute("ws");
			if (osname != null && (elementOs == null || elementOs.contains(osname))) {
				matchingElements.add(element);
			} else if (wsname != null && wsname.equalsIgnoreCase(elementWs)) {
				matchingElements.add(element);
			}
		}
		return matchingElements.toArray(new IConfigurationElement[matchingElements.size()]);
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
			for (String stylesheet : getAllStyles(theme)) {
				URL url;
				InputStream stream = null;
				try {
					url = FileLocator.resolve(new URL(stylesheet));
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
		return Collections.unmodifiableList(new ArrayList<>(themes));
	}

	@Override
	public void applyStyles(Object widget, boolean applyStylesToChildNodes) {
		for (CSSEngine engine : cssEngines) {
			Element element = engine.getElement(widget);
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
		try (FileInputStream fStream = new FileInputStream(from);
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(to))) {
			byte[] buffer = new byte[4096];
			int c;
			while ((c = fStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, c);
			}
		}
	}

	@Override
	public void restore(String alternateTheme) {
		String prefThemeId = getPreferenceThemeId();

		// Bug 562794, 563601: Eclipse once contained two identical themes named
		// "Classic" and "Windows Classic" and the second was removed with bug 562794.
		// An old workspace using the removed "Windows Classic" theme would be reseted
		// to the default theme on update. Since both themes are identical we silently
		// change the theme to the remaining "Classic" theme and don't disturb the user.
		if ("org.eclipse.e4.ui.css.theme.e4_classic6.0,6.1,6.2,6.3".equals(prefThemeId)) { //$NON-NLS-1$
			prefThemeId = "org.eclipse.e4.ui.css.theme.e4_classic"; //$NON-NLS-1$
		}

		// use theme from preferences if it exists
		if (prefThemeId != null) {
			for (ITheme t : getThemes()) {
				if (prefThemeId.equals(t.getId())) {
					setTheme(t, false);
					return;
				}
			}
		}

		boolean hasDarkTheme = getThemes().stream().anyMatch(t -> t.getId().startsWith(E4_DARK_THEME_ID));
		boolean overrideWithDarkTheme = false;
		if (hasDarkTheme) {
			if (prefThemeId != null) {
				/*
				 * The user had previously selected a theme which is not available anymore. In
				 * this case want to fall back to respect whether that previous choice was dark
				 * or not. https://github.com/eclipse-platform/eclipse.platform.ui/issues/2776
				 */
				overrideWithDarkTheme = prefThemeId.contains("dark");
			} else {
				/*
				 * No previous theme selection in preferences. In this case check if the system
				 * has dark appearance set and let Eclipse inherit that. Can be disabled using a
				 * system property.
				 */
				overrideWithDarkTheme = Display.isSystemDarkTheme()
						&& !"true".equalsIgnoreCase(System.getProperty(DISABLE_OS_DARK_THEME_INHERIT));
			}
		}

		String themeToRestore = overrideWithDarkTheme ? E4_DARK_THEME_ID : alternateTheme;
		if (themeToRestore != null) {
			setTheme(themeToRestore, false);
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
