/*******************************************************************************
 * Copyright (c) 2016 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Manumitting Technologies Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro.quicklinks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.SerializationException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroPartImplementation;
import org.eclipse.ui.internal.intro.impl.model.IntroTheme;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.intro.config.IIntroContentProvider;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * An Intro content provider that populates a list of frequently-used commands
 * from an extension point. The appearance of these quicklinks is normally taken
 * from the command metadata, including the image icon, but can be tailored.
 * These tailorings can be made optional depending on the current theme.
 * 
 * This is still experimental and subject to change.
 */
public class QuicklinksViewer implements IIntroContentProvider {

	/** Represents the importance of an element */
	enum Importance {
		HIGH("high", 0), MEDIUM("medium", 1), LOW("low", 2);

		String id;
		int level;

		Importance(String text, int importance) {
			this.id = text;
			this.level = importance;
		}

		public static Importance forId(String id) {
			for (Importance i : values()) {
				if (i.id.equals(id)) {
					return i;
				}
			}
			return LOW;
		}
	}

	/** Model holding the relevant attributes of a Quicklink element */
	class Quicklink {
		String commandSpec;
		String label;
		String description;
		String iconUrl;
		boolean standby = true;
		Importance importance = Importance.LOW;

		String bundleSymbolicName;
	}

	/**
	 * Responsible for retrieving Quicklinks and applying any icon overrides
	 */
	class ModelReader implements Supplier<Stream<Quicklink>> {
		private static final String QL_EXT_PT = "org.eclipse.ui.intro.quicklinks"; //$NON-NLS-1$
		private static final String ELMT_QUICKLINK = "quicklink"; //$NON-NLS-1$
		private static final String ATT_COMMAND = "command"; //$NON-NLS-1$
		private static final String ATT_LABEL = "label"; //$NON-NLS-1$
		private static final String ATT_DESCRIPTION = "description"; //$NON-NLS-1$
		private static final String ATT_ICON = "icon"; //$NON-NLS-1$
		private static final String ATT_IMPORTANCE = "importance"; //$NON-NLS-1$
		private static final String ATT_STANDBY = "standby"; //$NON-NLS-1$
		private static final String ELMT_OVERRIDE = "override"; //$NON-NLS-1$
		private static final String ATT_THEME = "theme"; //$NON-NLS-1$

		private List<Quicklink> quicklinks = new ArrayList<>();

		public Stream<Quicklink> get() {
			CommandManager manager = locator.getService(CommandManager.class);

			for (IConfigurationElement ce : getExtensionRegistry().getConfigurationElementsFor(QL_EXT_PT)) {
				if (!ELMT_QUICKLINK.equals(ce.getName())) {
					continue;
				}
				String commandSpec = ce.getAttribute(ATT_COMMAND);
				try {
					ParameterizedCommand pc = manager.deserialize(commandSpec);
					if (pc != null && pc.getCommand().isDefined()) {
						Quicklink ql = new Quicklink();
						ql.bundleSymbolicName = ce.getContributor().getName();
						ql.commandSpec = commandSpec;
						ql.label = Optional.ofNullable(ce.getAttribute(ATT_LABEL)).orElse(pc.getCommand().getName());
						ql.description = Optional.ofNullable(ce.getAttribute(ATT_DESCRIPTION))
								.orElse(pc.getCommand().getDescription());
						ql.iconUrl = QuicklinksViewer.this.getImageURL(ce, ATT_ICON, commandSpec);
						if (ce.getAttribute(ATT_IMPORTANCE) != null) {
							ql.importance = Importance.forId(ce.getAttribute(ATT_IMPORTANCE));
						}
						if (ce.getAttribute(ATT_STANDBY) != null) {
							ql.standby = Boolean.valueOf(ce.getAttribute(ATT_STANDBY));
						}
						quicklinks.add(ql);
					}
				} catch (NotDefinedException | SerializationException e) {
					/* skip */
					System.err.printf("Skipping '%s': %s\n", commandSpec, e);
				}
			}

			for (IConfigurationElement ce : getExtensionRegistry().getConfigurationElementsFor(QL_EXT_PT)) {
				if (!ELMT_OVERRIDE.equals(ce.getName())) {
					continue;
				}
				String theme = ce.getAttribute(ATT_THEME);
				String commandSpecPattern = ce.getAttribute(ATT_COMMAND);
				String icon = ce.getAttribute(ATT_ICON);
				if (theme != null && icon != null && Objects.equals(theme, getCurrentThemeId()) && commandSpecPattern != null) {
					findMatchingQuicklinks(commandSpecPattern)
							.forEach(ql -> ql.iconUrl = QuicklinksViewer.this.getImageURL(ce, ATT_ICON, null));
				}
			}
			return quicklinks.stream();
		}

		private Stream<Quicklink> findMatchingQuicklinks(String commandSpecPattern) {
			commandSpecPattern = commandSpecPattern.replace(".", "\\.").replace("(", "\\(").replace(")", "\\)")
					.replace("*", ".*");
			final Pattern pattern = Pattern.compile(commandSpecPattern);
			return quicklinks.stream().filter(ql -> pattern.matcher(ql.commandSpec).matches());
		}
	}

	/** Source: http://stackoverflow.com/a/417184 */
	private static final int MAX_URL_LENGTH = 2083;

	private IIntroContentProviderSite site;
	private IServiceLocator locator;
	private Map<String, Long> bundleIds;
	private Bundle[] bundles;

	public void init(IIntroContentProviderSite site) {
		this.site = site;
		// IIntroContentProviderSite should provide services.
		if (site instanceof AbstractIntroPartImplementation) {
			this.locator = ((AbstractIntroPartImplementation) site).getIntroPart().getIntroSite();
		} else {
			this.locator = PlatformUI.getWorkbench();
		}
	}

	public String getCurrentThemeId() {
		if (site instanceof AbstractIntroPartImplementation) {
			IntroTheme theme = ((AbstractIntroPartImplementation) site).getModel().getTheme();
			return theme.getId();
		}
		return null;
	}

	public IExtensionRegistry getExtensionRegistry() {
		return locator.getService(IExtensionRegistry.class);
	}

	public void createContent(String id, PrintWriter out) {
		// Content is already embedded within a <div id="...">
		getQuicklinks().forEach(ql -> {
			try {
				// ah how lovely to embed HTML in code
				String urlEncodedCommand = URLEncoder.encode(ql.commandSpec, "UTF-8");
				out.append("<a class='content-link' id='");
				out.append(asCSSId(ql.commandSpec));
				out.append("' ");
				out.append(" href='http://org.eclipse.ui.intro/execute?command=");
				out.append(urlEncodedCommand);
				out.append("&standby=");
				out.append(Boolean.toString(ql.standby));
				out.append("'>");
				if (ql.iconUrl != null) {
					out.append("<img class='background-image' src='").append(ql.iconUrl).append("'>");
				}
				out.append("\n<div class='link-extra-div'></div>\n"); // UNKNOWN
				out.append("<span class='link-label'>");
				out.append(ql.label);
				out.append("</span>");
				if (ql.description != null) {
					out.append("\n<p><span class='text'>");
					out.append(ql.description);
					out.append("</span></p>");
				}
				out.append("</a>");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		});
	}

	private String asCSSId(String commandSpec) {
		int indexOf = commandSpec.indexOf('(');
		if (indexOf > 0) {
			commandSpec = commandSpec.substring(0, indexOf);
		}
		return commandSpec.replace('.', '_');
	}

	/**
	 * @return URL to image, suitable for using in an external browser; may be a
	 *         <code>data:</code> URL; may be null
	 */
	private String getImageURL(IConfigurationElement ce, String attr, String commandId) {
		String iconURL = MenuHelper.getIconURI(ce, attr);
		if (iconURL != null) {
			return asBrowserURL(iconURL);
		}

		if (commandId == null) {
			return null;
		}
		ICommandImageService images = locator.getService(ICommandImageService.class);
		if (images == null) {
			return null;
		}
		ImageDescriptor descriptor = images.getImageDescriptor(commandId);
		iconURL = MenuHelper.getImageUrl(descriptor);
		if (iconURL != null) {
			return asBrowserURL(iconURL);
		}
		return asDataURL(descriptor);
	}

	private String asBrowserURL(String iconURL) {
		if (iconURL.startsWith("file:") || iconURL.startsWith("http:")) {
			return iconURL;
		}
		try {
			URL original = new URL(iconURL);
			URL toLocal = FileLocator.toFileURL(original);
			if (!toLocal.sameFile(original)) {
				return toLocal.toString();
			}
		} catch (IOException e1) {
			/* ignore */
		}

		// extract content
		try {
			return asDataURL(ImageDescriptor.createFromURL(new URL(iconURL)));
		} catch (MalformedURLException e) {
			// should probably log this
			return iconURL;
		}
	}

	/**
	 * Write out the image as a data: URL if possible or to the file-system.
	 * 
	 * @param descriptor
	 * @return URL with the resulting image
	 */
	private String asDataURL(ImageDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		ImageData data = descriptor.getImageData();
		if (data == null) {
			return null;
		}
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { data };

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		loader.save(output, SWT.IMAGE_PNG);
		if (output.size() * 4 / 3 < MAX_URL_LENGTH) {
			// You'd think there was a more efficient way to do this...
			return "data:image/png;base64," + Base64.getUrlEncoder().encodeToString(output.toByteArray());
		}
		try {
			File tempFile = File.createTempFile("qlink", "png");
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(output.toByteArray());
			fos.close();
			tempFile.deleteOnExit();
			return tempFile.toURI().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void createContent(String id, Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.EXPANDED);
		TableViewer tableViewer = new TableViewer(toolkit.createTable(section, SWT.FULL_SELECTION));
		tableViewer.setLabelProvider(new URLLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Quicklink) {
					return ((Quicklink) element).label;
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof Quicklink) {
					return super.getImage(((Quicklink) element).iconUrl);
				}
				return super.getImage(element);
			}
		});
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(getQuicklinks().toArray());
	}

	private Stream<Quicklink> getQuicklinks() {
		return new ModelReader().get().sorted(this::compareQuicklinks);
	}

	public void dispose() {
	}

	private int compareQuicklinks(Quicklink a, Quicklink b) {
		int impA = a.importance.level;
		int impB = b.importance.level;
		if (impA != impB) {
			return impA - impB;
		}
		long diff = getRank(a) - getRank(b);
		if (diff > 0) {
			return 1;
		}
		if (diff < 0) {
			return -1;
		}
		return 0;
	}

	private long getRank(Quicklink ql) {
		if (bundleIds == null) {
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			bundleIds = new HashMap<>();
			bundles = bundle.getBundleContext().getBundles();
		}
		return bundleIds.computeIfAbsent(ql.bundleSymbolicName, bsn -> {
			for (Bundle b : bundles) {
				if (bsn.equals(b.getSymbolicName()) && (b.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
					return b.getBundleId();
				}
			}
			return Long.MAX_VALUE;
		});
	}
}
