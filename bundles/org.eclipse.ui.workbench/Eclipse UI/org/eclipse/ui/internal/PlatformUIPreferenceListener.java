/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * The PlatformUIPreferenceListener is a class that listens to changes in the
 * preference store and propogates the change for any special cases that require
 * updating of other values within the workbench.
 */
public class PlatformUIPreferenceListener implements IPropertyChangeListener {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {

		String propertyName = event.getProperty();
		if (IPreferenceConstants.ENABLED_DECORATORS.equals(propertyName)) {
			DecoratorManager manager = WorkbenchPlugin.getDefault().getDecoratorManager();
			manager.applyDecoratorsPreference();
			manager.clearCaches();
			manager.updateForEnablementChange();
			return;
		}

		if (IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID.equals(propertyName)) {
			IWorkbench workbench = PlatformUI.getWorkbench();

			workbench.getPerspectiveRegistry().setDefaultPerspective((String) event.getNewValue());
			return;
		}

		if (IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR.equals(propertyName)) {
			IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
			for (int i = 0; i < workbenchWindows.length; i++) {
				IWorkbenchWindow window = workbenchWindows[i];
				if (window instanceof WorkbenchWindow)
					((WorkbenchWindow) window).setPerspectiveBarLocation(apiStore
							.getString(IWorkbenchPreferenceConstants.DOCK_PERSPECTIVE_BAR));
			}
			return;
		}

		// TODO the banner apperance should have its own preference
		if (IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(propertyName)) {
			boolean newValue = PrefUtil.getAPIPreferenceStore().getBoolean(
					IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);

			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
			for (int i = 0; i < workbenchWindows.length; i++) {
				IWorkbenchWindow window = workbenchWindows[i];
				if (window instanceof WorkbenchWindow)
					((WorkbenchWindow) window).setBannerCurve(newValue);
			}
			return;
		}

		// Update the file associations if they have changed due to an import
		if (IPreferenceConstants.RESOURCES.equals(propertyName)) {
			IEditorRegistry registry = WorkbenchPlugin.getDefault().getEditorRegistry();
			if (registry instanceof EditorRegistry) {
				EditorRegistry editorRegistry = (EditorRegistry) registry;
				IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
				Reader reader = null;
				try {
					String xmlString = store.getString(IPreferenceConstants.RESOURCES);
					if (xmlString != null && xmlString.length() > 0) {
						reader = new StringReader(xmlString);
						// Build the editor map.
						HashMap editorMap = new HashMap();
						int i = 0;
						IEditorDescriptor[] descriptors = editorRegistry
								.getSortedEditorsFromPlugins();
						// Get the internal editors
						for (i = 0; i < descriptors.length; i++) {
							IEditorDescriptor descriptor = descriptors[i];
							editorMap.put(descriptor.getId(), descriptor);
						}
						// Get the external (OS) editors
						descriptors = editorRegistry.getSortedEditorsFromOS();
						for (i = 0; i < descriptors.length; i++) {
							IEditorDescriptor descriptor = descriptors[i];
							editorMap.put(descriptor.getId(), descriptor);
						}
						// Update the file to editor(s) mappings
						editorRegistry.readResources(editorMap, reader);
					}
				} catch (WorkbenchException e) {
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}