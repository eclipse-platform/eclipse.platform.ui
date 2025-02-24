/*******************************************************************************
 * Copyright (c) 2013-2025 Pivotal Software, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *    Jozef Tomek - text viewers extension
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.quicksearch.internal.core.preferences.QuickSearchPreferences;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class QuickSearchActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.text.quicksearch"; //$NON-NLS-1$

	// The shared instance
	private static QuickSearchActivator plugin;

	private static final String TEXT_VIEWERS_EXTENSION_POINT= "textViewers"; //$NON-NLS-1$
	private static final String CONTENT_TYPE_BINDING = "contentTypeBinding"; //$NON-NLS-1$
	private static final String VIEWER_TAG = "viewer"; //$NON-NLS-1$
	private static final String VIEWER_ID_ATTRIBUTE = "viewerId"; //$NON-NLS-1$
	private static final String DEFAULT_CREATOR_CLASS = DefaultSourceViewerCreator.class.getName();

	private final ExtensionsRegistry<ViewerDescriptor> fTextViewersRegistry = new ExtensionsRegistry<>();
	private final Map<ImageDescriptor, Image> fgImages= new Hashtable<>(10);
	private final List<Image> fgDisposeOnShutdownImages= new ArrayList<>();

	// Lazy initialized
	private QuickSearchPreferences prefs = null;
	private IContentTypeManager contentTypeManager;
	private ResourceBundle resourceBundle;
	private boolean registryInitialized;
	private IViewerDescriptor defaultViewerDescriptor;

	/**
	 * The constructor
	 */
	public QuickSearchActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		contentTypeManager = Platform.getContentTypeManager();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		for (Image img : fgDisposeOnShutdownImages) {
			if (!img.isDisposed()) {
				img.dispose();
			}
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QuickSearchActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static void log(Throwable exception) {
		log(createErrorStatus(exception));
	}

	public static void log(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
	}

	public static void log(IStatus status) {
//		if (logger == null) {
			getDefault().getLog().log(status);
//		}
//		else {
//			logger.logEntry(status);
//		}
	}

	public static IStatus createErrorStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, exception.getMessage(), exception);
	}

	public QuickSearchPreferences getPreferences() {
		if (prefs==null) {
			prefs = new QuickSearchPreferences(QuickSearchActivator.getDefault().getPreferenceStore());
		}
		return prefs;
	}

	private ResourceBundle getResourceBundle() {
		if (resourceBundle == null)
			resourceBundle = Platform.getResourceBundle(getBundle());
		return resourceBundle;
	}

	static String getFormattedString(String key, String arg) {
		try {
			return MessageFormat.format(getDefault().getResourceBundle().getString(key), arg);
		} catch (MissingResourceException e) {
			return "!" + key + "!";	//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	private static String getFormattedString(String key, String arg0, String arg1) {
		try {
			return MessageFormat.format(getDefault().getResourceBundle().getString(key), arg0, arg1);
		} catch (MissingResourceException e) {
			return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
		}
	}

	private void initializeExtensionsRegistry() {
		if (!registryInitialized) {
			registerExtensions();
			Assert.isNotNull(defaultViewerDescriptor);
			registryInitialized = true;
		}
	}

	private void registerExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// collect all descriptors which define the source viewer extension point
		IConfigurationElement[] elements = registry.getConfigurationElementsFor(PLUGIN_ID, TEXT_VIEWERS_EXTENSION_POINT);
		for (IConfigurationElement element : elements) {
			String name = element.getName();
			if (!CONTENT_TYPE_BINDING.equals(name)) {
				if (!VIEWER_TAG.equals(name))
					log(getFormattedString("QuickSearchActivator.unexpectedTag", name, VIEWER_TAG)); //$NON-NLS-1$
				var viewerDescriptor = new ViewerDescriptor(element);
				fTextViewersRegistry.register(element, viewerDescriptor);
				if (DEFAULT_CREATOR_CLASS.equals(viewerDescriptor.getViewerClass())) {
					defaultViewerDescriptor = viewerDescriptor;
				}
			}
		}
		for (IConfigurationElement element : elements) {
			if (CONTENT_TYPE_BINDING.equals(element.getName()))
				fTextViewersRegistry.createBinding(element, VIEWER_ID_ATTRIBUTE);
		}
	}

	IContentType getContentType(String contentTypeIdentifier) {
		return contentTypeManager.getContentType(contentTypeIdentifier);
	}

	IViewerDescriptor getDefaultViewer() {
		initializeExtensionsRegistry();
		return defaultViewerDescriptor;
	}

	List<IViewerDescriptor> getViewers(IFile file) {
		initializeExtensionsRegistry();
		if (file == null) {
			return List.of(getDefaultViewer());
		}
		return findContentViewerDescriptor(file);
	}

	private List<IViewerDescriptor> findContentViewerDescriptor(IFile input) {
		LinkedHashSet<IViewerDescriptor> result = new LinkedHashSet<>();

		String name = input.getName();

		IContentDescription cDescr = null;
		try {
			cDescr = input.getContentDescription();
		} catch (CoreException e) {
			log(e);
		}
		IContentType ctype = cDescr == null ? null : cDescr.getContentType();
		if (ctype == null) {
			ctype = contentTypeManager.findContentTypeFor(name);
		}
		if (ctype != null) {
			List<ViewerDescriptor> list = fTextViewersRegistry.searchAll(ctype);
			if (list != null) {
				result.addAll(list);
			}
		}

		String type = input.getFileExtension();
		if (type != null) {
			List<ViewerDescriptor> list = fTextViewersRegistry.searchAll(type);
			if (list != null)
				result.addAll(list);
		}

		Set<ViewerDescriptor> editorLinkedDescriptors = findEditorLinkedDescriptors(name, ctype, false);
		result.addAll(editorLinkedDescriptors);

		if (result.isEmpty() || result.size() == 1) {
			// single candidate should always be the default viewer, but in case it's not, add default viewer as well
			result.add(defaultViewerDescriptor);
		} else {
			// more than 1 candidate, make sure default viewer is the last one
			result.remove(defaultViewerDescriptor);
			result.add(defaultViewerDescriptor);
		}
		return new ArrayList<>(result);
	}

	/**
	 * @param fileName      file name for content in search match preview panel
	 * @param contentType   possible content type for content in search match preview panel, may be null
	 * @param firstIsEnough stop searching once first match is found
	 * @return set of descriptors which could be found for given content type via "linked" editor
	 */
	private Set<ViewerDescriptor> findEditorLinkedDescriptors(String fileName, IContentType contentType,
			boolean firstIsEnough) {
		if (fileName == null && contentType == null) {
			return Collections.emptySet();
		}
		if (contentType == null) {
			contentType = contentTypeManager.findContentTypeFor(fileName);
		}

		LinkedHashSet<ViewerDescriptor> viewers = fTextViewersRegistry.getAll().stream()
				.filter(vd -> vd.getLinkedEditorId() != null).collect(Collectors.toCollection(LinkedHashSet::new));
		if (viewers.isEmpty()) {
			return Collections.emptySet();
		}

		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		LinkedHashSet<ViewerDescriptor> result = new LinkedHashSet<>();
		IEditorDescriptor[] editors = editorReg.getEditors(fileName, contentType);
		for (IEditorDescriptor ed : editors) {
			addLinkedEditorContentTypes(viewers, firstIsEnough, ed.getId(), result);
			if (firstIsEnough && !result.isEmpty()) {
				return result;
			}
		}
		return result;
	}

	private void addLinkedEditorContentTypes(LinkedHashSet<ViewerDescriptor> viewers, boolean firstIsEnough,
			String editorId, Set<ViewerDescriptor> result) {
		Stream<ViewerDescriptor> stream = viewers.stream().filter(vd -> editorId.equals(vd.getLinkedEditorId()));
		if (firstIsEnough) {
			Optional<ViewerDescriptor> first = stream.findFirst();
			if (first.isPresent()) {
				result.add(first.get());
			}
		} else {
			stream.collect(Collectors.toCollection(() -> result));
		}
	}

	/**
	 * Returns a shared image for the given adaptable.
	 * This convenience method queries the given adaptable
	 * for its <code>IWorkbenchAdapter.getImageDescriptor</code>, which it
	 * uses to create an image if it does not already have one.
	 * <p>
	 * Note: Images returned from this method will be automatically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param adaptable the adaptable for which to find an image
	 * @return an image
	 */
	public Image getImage(IAdaptable adaptable) {
		if (adaptable != null) {
			IWorkbenchAdapter o= Adapters.adapt(adaptable, IWorkbenchAdapter.class);
			if (o == null) {
				return null;
			}
			ImageDescriptor id= o.getImageDescriptor(adaptable);
			if (id != null) {
				Image image= fgImages.get(id);
				if (image == null) {
					image= id.createImage();
					try {
						fgImages.put(id, image);
					} catch (NullPointerException e) {
						// NeedWork
					}
					fgDisposeOnShutdownImages.add(image);

				}
				return image;
			}
		}
		return null;
	}

}
