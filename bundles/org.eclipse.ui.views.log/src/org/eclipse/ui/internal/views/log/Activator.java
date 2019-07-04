/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 202583
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ui.views.log"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() { // do nothing
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		registerImageDescriptor(registry, SharedImages.DESC_PREV_EVENT);
		registerImageDescriptor(registry, SharedImages.DESC_NEXT_EVENT);

		registerImageDescriptor(registry, SharedImages.DESC_ERROR_ST_OBJ);
		registerImageDescriptor(registry, SharedImages.DESC_ERROR_STACK_OBJ);
		registerImageDescriptor(registry, SharedImages.DESC_INFO_ST_OBJ);
		registerImageDescriptor(registry, SharedImages.DESC_OK_ST_OBJ);
		registerImageDescriptor(registry, SharedImages.DESC_WARNING_ST_OBJ);
		registerImageDescriptor(registry, SharedImages.DESC_HIERARCHICAL_LAYOUT_OBJ);

		registerImageDescriptor(registry, SharedImages.DESC_CLEAR);
		registerImageDescriptor(registry, SharedImages.DESC_CLEAR_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_OPEN_CONSOLE);
		registerImageDescriptor(registry, SharedImages.DESC_REMOVE_LOG);
		registerImageDescriptor(registry, SharedImages.DESC_REMOVE_LOG_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_EXPORT);
		registerImageDescriptor(registry, SharedImages.DESC_EXPORT_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_FILTER);
		registerImageDescriptor(registry, SharedImages.DESC_FILTER_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_IMPORT);
		registerImageDescriptor(registry, SharedImages.DESC_IMPORT_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_OPEN_LOG);
		registerImageDescriptor(registry, SharedImages.DESC_OPEN_LOG_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_PROPERTIES);
		registerImageDescriptor(registry, SharedImages.DESC_PROPERTIES_DISABLED);
		registerImageDescriptor(registry, SharedImages.DESC_READ_LOG);
		registerImageDescriptor(registry, SharedImages.DESC_READ_LOG_DISABLED);
	}

	private void registerImageDescriptor(ImageRegistry registry, String id) {
		ResourceLocator.imageDescriptorFromBundle(getClass(), id).ifPresent(d -> registry.put(id, d));
	}

}
