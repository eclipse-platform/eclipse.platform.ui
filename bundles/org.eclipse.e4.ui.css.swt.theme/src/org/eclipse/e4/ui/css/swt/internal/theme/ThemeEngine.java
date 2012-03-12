/*******************************************************************************
 * Copyright (c) 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.internal.theme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.core.util.impl.resources.OSGiResourceLocator;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
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
	private List<Theme> themes = new ArrayList<Theme>();

	private CSSEngine engine;
	private ITheme currentTheme;
	private Display display;

	private List<String> globalStyles = new ArrayList<String>();
	private List<IResourceLocator> globalSourceLocators = new ArrayList<IResourceLocator>();

	private HashMap<String, List<String>> stylesheets = new HashMap<String, List<String>>();
	private HashMap<String, List<String>> modifiedStylesheets = new HashMap<String, List<String>>();
	private HashMap<String, List<IResourceLocator>> sourceLocators = new HashMap<String, List<IResourceLocator>>();

	private static final String THEMEID_KEY = "themeid";

	public ThemeEngine(Display display) {
		this.engine = new CSSSWTEngineImpl(display, true);
		this.display = display;
		this.engine.setErrorHandler(new CSSErrorHandler() {

			public void error(Exception e) {
				// TODO Use the logger
				e.printStackTrace();
			}
		});
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry
				.getExtensionPoint("org.eclipse.e4.ui.css.swt.theme");

		//load any modified style sheets
		File modDir= new File(
				System.getProperty("user.home") + System.getProperty("file.separator") + ".e4css" + System.getProperty("file.separator")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		File[] modifiedFiles = modDir.listFiles();
		
		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : getPlatformMatches(e
					.getConfigurationElements())) {
				if (ce.getName().equals("theme")) {
					try {
						String version = ce.getAttribute("os_version");
						if (version == null) version ="";
						String originalCSSFile;
						String basestylesheeturi = originalCSSFile = ce
								.getAttribute("basestylesheeturi");
						if (!basestylesheeturi.startsWith("platform:/plugin/")) {
							basestylesheeturi = "platform:/plugin/"
									+ ce.getContributor().getName() + "/"
									+ basestylesheeturi;
						}
						String themeId = ce.getAttribute("id") + version;
						registerTheme(
								themeId,
								ce.getAttribute("label"), basestylesheeturi,
								version);
						
						//check for modified files
						if (modifiedFiles != null) {
							int slash = originalCSSFile.lastIndexOf("/");
							if (slash != -1) {
								originalCSSFile = originalCSSFile.substring(slash + 1);
								for (int i = 0; i < modifiedFiles.length; i++) {
									String modifiedFileName = modifiedFiles[i].getName();
									if (modifiedFileName.contains(".css") && modifiedFileName.equals(originalCSSFile)) {  //$NON-NLS-1$
		//								modifiedStylesheets
										ArrayList<String> styleSheets = new ArrayList<String>();
										styleSheets.add("file:/" + modifiedFiles[i].getPath());
										modifiedStylesheets.put(themeId, styleSheets);
									}
								}
							}
						}
					} catch (IllegalArgumentException e1) {
						//TODO Can we somehow use logging?
						e1.printStackTrace();
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
								"platform:/plugin/"
										+ ce.getContributor().getName() + "/"
										+ ce.getAttribute("uri"), themes);

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

		//Resolve to install dir
		registerResourceLocator(new OSGiResourceLocator("platform:/plugin/org.eclipse.platform/css/"));
		
		display.setData("org.eclipse.e4.ui.css.core.engine", engine);
	}

	public synchronized ITheme registerTheme(String id, String label,
			String basestylesheetURI) throws IllegalArgumentException {
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
		if (osVersion != "") theme.setOsVersion(osVersion);
		themes.add(theme);
		registerStyle(id, basestylesheetURI);
		return theme;
	}

	public synchronized void registerStylesheet(String uri, String... themes) {
		Bundle bundle = FrameworkUtil.getBundle(ThemeEngine.class);
		String osname = bundle.getBundleContext().getProperty("osgi.os");
		String wsname = bundle.getBundleContext().getProperty("ogsi.ws");

		uri = uri.replaceAll("\\$os\\$", osname).replaceAll("\\$ws\\$", wsname);

		if (themes.length == 0) {
			globalStyles.add(uri);
		} else {
			for (String t : themes) {
				registerStyle(t, uri);
			}
		}
	}

	public synchronized void registerResourceLocator(IResourceLocator locator,
			String... themes) {
		if (themes.length == 0) {
			globalSourceLocators.add(locator);
		} else {
			for (String t : themes) {
				List<IResourceLocator> list = sourceLocators.get(t);
				if (list == null) {
					list = new ArrayList<IResourceLocator>();
					sourceLocators.put(t, list);
				}
				list.add(locator);
			}
		}
	}

	private void registerStyle(String id, String stylesheet) {
		List<String> s = stylesheets.get(id);
		if (s == null) {
			s = new ArrayList<String>();
			stylesheets.put(id, s);
		}
		s.add(stylesheet);
	}

	private List<String> getAllStyles(String id) {
		List<String> s = stylesheets.get(id);
		if (s == null) {
			s = Collections.emptyList();
		}

		
		List<String> m = modifiedStylesheets.get(id);
		if (m != null) {
			m = new ArrayList<String>(m);
			m.addAll(globalStyles);
			return m;
		}
		
		s = new ArrayList<String>(s);
		s.addAll(globalStyles);
		return s;
		
	}

	private List<IResourceLocator> getResourceLocators(String id) {
		List<IResourceLocator> list = new ArrayList<IResourceLocator>(
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
	 * @param elements
	 *            the elements to check
	 * @return the best matches, if any
	 */
	private IConfigurationElement[] getPlatformMatches(
			IConfigurationElement[] elements) {
		Bundle bundle = FrameworkUtil.getBundle(ThemeEngine.class);
		String osname = bundle.getBundleContext().getProperty("osgi.os");
		// TODO: Need to differentiate win32 versions
		String os_version = System.getProperty("os.version");
		String wsname = bundle.getBundleContext().getProperty("ogsi.ws");
		ArrayList<IConfigurationElement> matchingElements = new ArrayList<IConfigurationElement>();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String elementOs = element.getAttribute("os");
			String elementWs = element.getAttribute("ws");
			String elementOsVersion = element.getAttribute("os_version");
			if (osname != null
					&& (elementOs == null || elementOs.contains(osname))) {
				if (os_version != null && os_version.equalsIgnoreCase(elementOsVersion)) {
					// best match
					matchingElements.add(element);
					continue;
				}
				matchingElements.add(element);
			} else if (wsname != null && wsname.equalsIgnoreCase(elementWs)) {
				matchingElements.add(element);
			}
		}
		return (IConfigurationElement[]) matchingElements
				.toArray(new IConfigurationElement[matchingElements.size()]);
	}

	public void setTheme(String themeId, boolean restore) {
		String osVersion = System.getProperty("os.version");
		if (osVersion != null) {
			boolean found = false;
			for (Theme t : themes) {
				String version = t.getOsVersion();
				if (version != null && osVersion.contains(version)) {
					String themeVersion = themeId + version;
					if (t.getId().equals(themeVersion)) {
						setTheme(t, restore);
						found = true;
						break;
					}
				}
			}
			if (found) return;
		}
		//try generic
		for (Theme t : themes) {
			if (t.getId().equals(themeId)) {
				setTheme(t, restore);
				break;
			}
		}
	}

	public void setTheme(ITheme theme, boolean restore) {
		setTheme(theme, restore, false);
	}
	public void setTheme(ITheme theme, boolean restore, boolean force) {
		Assert.isNotNull(theme, "The theme must not be null");

		if (this.currentTheme != theme || force) {
			if (currentTheme != null) {
				for (IResourceLocator l : getResourceLocators(currentTheme
						.getId())) {
					engine.getResourcesLocatorManager()
							.unregisterResourceLocator(l);
				}
			}

			this.currentTheme = theme;
			engine.reset();

			for (IResourceLocator l : getResourceLocators(theme.getId())) {
				engine.getResourcesLocatorManager().registerResourceLocator(l);
			}
			for (String stylesheet : getAllStyles(theme.getId())) {
				URL url;
				InputStream stream = null;
				try {
					url = FileLocator.resolve(new URL(stylesheet.toString()));
					stream = url.openStream();
					InputSource source = new InputSource();
					source.setByteStream(stream);
					source.setURI(url.toString());
					engine.parseStyleSheet(source);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			Shell[] shells = display.getShells();
			for (Shell s : shells) {
				try {
					s.setRedraw(false);
					s.reskin(SWT.ALL);
					applyStyles(s, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					s.setRedraw(true);
				}
			}
		}
		
		if (restore) {
			IEclipsePreferences pref = getPreferences();
			pref.put(THEMEID_KEY, theme.getId());
			try {
				pref.flush();
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sendThemeChangeEvent(restore);
	}

	/**
	 * Broadcast theme-change event using OSGi Event Admin.
	 */
	private void sendThemeChangeEvent(boolean restore) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null) {
			return;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(IThemeEngine.Events.THEME, currentTheme);
		data.put(IThemeEngine.Events.ENGINE, engine);
		data.put(IThemeEngine.Events.DISPLAY, display);
		data.put(IThemeEngine.Events.RESTORE, restore);
		Event event = new Event(IThemeEngine.Events.THEME_CHANGED, data);
		eventAdmin.sendEvent(event); // synchronous
	}

	private EventAdmin getEventAdmin() {
		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		if (bundle == null) {
			return null;
		}
		BundleContext context = bundle.getBundleContext();
		ServiceReference<EventAdmin> eventAdminRef = context
				.getServiceReference(EventAdmin.class);
		return context.getService(eventAdminRef);
	}

	public synchronized List<ITheme> getThemes() {
		return Collections.unmodifiableList(new ArrayList<ITheme>(themes));
	}

	public void applyStyles(Widget widget, boolean applyStylesToChildNodes) {
		engine.applyStyles(widget, applyStylesToChildNodes);
	}

	// TODO may not be ideal??
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=312842
	public CSSEngine getCSSEngine() {
		return engine;
	}

	private String getPreferenceThemeId() {
		return getPreferences().get(THEMEID_KEY, null);
	}

	private IEclipsePreferences getPreferences() {
		return new InstanceScope().getNode(FrameworkUtil.getBundle(
				ThemeEngine.class).getSymbolicName());
	}

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
	
	public ITheme getActiveTheme() {
		return currentTheme;
	}
	
	public CSSStyleDeclaration getStyle(Widget widget) {
		Element e = engine.getCSSElementContext(widget).getElement();
		if( e == null ) {
			return null;
		}
		return engine.getViewCSS().getComputedStyle(e, null);
	}

	public List<String> getStylesheets(ITheme selection) {
		List<String> ss  = stylesheets.get(selection.getId());
		return ss == null ? new ArrayList<String>() : ss;
	}
	
	public void themeModified(ITheme theme, List<String> paths) {
		modifiedStylesheets.put(theme.getId(), paths);
		setTheme(theme, false, true);
	}
	
	public void resetCurrentTheme() {
		setTheme(currentTheme, false, true);
	}
	
	public List<String> getModifiedStylesheets(ITheme selection) {
		List<String> ss  = modifiedStylesheets.get(selection.getId());
		return ss == null ? new ArrayList<String>() : ss;
	}
	
	public void resetModifiedStylesheets(ITheme selection) {
		List<String> ss = modifiedStylesheets.remove(selection.getId());
	}
}
