/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.internal.themes.ThemeRegistry;
import org.eclipse.ui.internal.themes.ThemeRegistryReader;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

class ExtensionEventHandler implements IRegistryChangeListener {

	private Workbench workbench;

	private List<?> changeList = new ArrayList<>(10);

	public ExtensionEventHandler(Workbench workbench) {
		this.workbench = workbench;
	}

	@Override
	public void registryChanged(IRegistryChangeEvent event) {
		try {
			IExtensionDelta delta[] = event.getExtensionDeltas(WorkbenchPlugin.PI_WORKBENCH);
			IExtension ext;
			IExtensionPoint extPt;
			IWorkbenchWindow[] win = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (win.length == 0) {
				return;
			}
			Display display = win[0].getShell().getDisplay();
			if (display == null) {
				return;
			}
			ArrayList<IExtensionDelta> appearList = new ArrayList<>(5);
			ArrayList<IExtensionDelta> revokeList = new ArrayList<>(5);
			String id = null;
			int numPerspectives = 0;
			int numActionSetPartAssoc = 0;

			// push action sets and perspectives to the top because incoming
			// actionSetPartAssociations and perspectiveExtensions may depend upon
			// them for their bindings.
			for (IExtensionDelta extensionDelta : delta) {
				id = extensionDelta.getExtensionPoint().getSimpleIdentifier();
				if (extensionDelta.getKind() == IExtensionDelta.ADDED) {
					if (id.equals(IWorkbenchRegistryConstants.PL_ACTION_SETS)) {
						appearList.add(0, extensionDelta);
					} else if (!id.equals(IWorkbenchRegistryConstants.PL_PERSPECTIVES)
							&& !id.equals(IWorkbenchRegistryConstants.PL_VIEWS)
							&& !id.equals(IWorkbenchRegistryConstants.PL_ACTION_SETS)) {
						appearList.add(appearList.size() - numPerspectives, extensionDelta);
					}
				} else if (extensionDelta.getKind() == IExtensionDelta.REMOVED) {
					switch (id) {
					case IWorkbenchRegistryConstants.PL_ACTION_SET_PART_ASSOCIATIONS:
						revokeList.add(0, extensionDelta);
						numActionSetPartAssoc++;
						break;
					case IWorkbenchRegistryConstants.PL_PERSPECTIVES:
						revokeList.add(numActionSetPartAssoc, extensionDelta);
						break;
					default:
						revokeList.add(extensionDelta);
						break;
					}
				}
			}
			Iterator<IExtensionDelta> iter = appearList.iterator();
			IExtensionDelta extDelta = null;
			while (iter.hasNext()) {
				extDelta = iter.next();
				extPt = extDelta.getExtensionPoint();
				ext = extDelta.getExtension();
				asyncAppear(display, extPt, ext);
			}
			// Suspend support for removing a plug-in until this is more stable
			// iter = revokeList.iterator();
			// while(iter.hasNext()) {
			// extDelta = (IExtensionDelta) iter.next();
			// extPt = extDelta.getExtensionPoint();
			// ext = extDelta.getExtension();
			// asyncRevoke(display, extPt, ext);
			// }

			resetCurrentPerspective(display);
		} finally {
			// ensure the list is cleared for the next pass through
			changeList.clear();
		}

	}

	private void asyncAppear(Display display, final IExtensionPoint extpt, final IExtension ext) {
		Runnable run = () -> appear(extpt, ext);
		display.syncExec(run);
	}

	private void appear(IExtensionPoint extPt, IExtension ext) {
		String name = extPt.getSimpleIdentifier();
		if (name.equalsIgnoreCase(IWorkbenchRegistryConstants.PL_FONT_DEFINITIONS)) {
			loadFontDefinitions(ext);
			return;
		}
		if (name.equalsIgnoreCase(IWorkbenchRegistryConstants.PL_THEMES)) {
			loadThemes(ext);
			return;
		}
	}

	private void loadFontDefinitions(IExtension ext) {
		ThemeRegistryReader reader = new ThemeRegistryReader();
		reader.setRegistry((ThemeRegistry) WorkbenchPlugin.getDefault().getThemeRegistry());
		for (IConfigurationElement configElement : ext.getConfigurationElements()) {
			reader.readElement(configElement);
		}

		Collection<FontDefinition> fonts = reader.getFontDefinitions();
		FontDefinition[] fontDefs = fonts.toArray(new FontDefinition[fonts.size()]);
		ThemeElementHelper.populateRegistry(workbench.getThemeManager().getTheme(IThemeManager.DEFAULT_THEME), fontDefs,
				PrefUtil.getInternalPreferenceStore());
	}

	// TODO: confirm
	private void loadThemes(IExtension ext) {
		ThemeRegistryReader reader = new ThemeRegistryReader();
		ThemeRegistry registry = (ThemeRegistry) WorkbenchPlugin.getDefault().getThemeRegistry();
		reader.setRegistry(registry);
		for (IConfigurationElement configElement : ext.getConfigurationElements()) {
			reader.readElement(configElement);
		}

		Collection<ColorDefinition> colors = reader.getColorDefinitions();
		ColorDefinition[] colorDefs = colors.toArray(new ColorDefinition[colors.size()]);

		ITheme theme = workbench.getThemeManager().getTheme(IThemeManager.DEFAULT_THEME);
		ThemeElementHelper.populateRegistry(theme, colorDefs, PrefUtil.getInternalPreferenceStore());

		Collection<FontDefinition> fonts = reader.getFontDefinitions();
		FontDefinition[] fontDefs = fonts.toArray(new FontDefinition[fonts.size()]);
		ThemeElementHelper.populateRegistry(theme, fontDefs, PrefUtil.getInternalPreferenceStore());

		Map<String, String> data = reader.getData();
		registry.addData(data);
	}

	private void resetCurrentPerspective(Display display) {
		if (changeList.isEmpty()) {
			return;
		}

		final StringBuilder message = new StringBuilder(
				ExtensionEventHandlerMessages.ExtensionEventHandler_following_changes);

		for (Object element : changeList) {
			message.append(element);
		}

		message.append(ExtensionEventHandlerMessages.ExtensionEventHandler_need_to_reset);

		display.asyncExec(() -> {
			Shell parentShell = null;
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window == null) {
				if (workbench.getWorkbenchWindowCount() == 0) {
					return;
				}
				window = workbench.getWorkbenchWindows()[0];
			}

			parentShell = window.getShell();

			if (MessageDialog.openQuestion(parentShell,
					ExtensionEventHandlerMessages.ExtensionEventHandler_reset_perspective, message.toString())) {
				IWorkbenchPage page = window.getActivePage();
				if (page == null) {
					return;
				}
				page.resetPerspective();
			}
		});

	}
}
