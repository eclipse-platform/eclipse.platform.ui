/*******************************************************************************
 * Copyright (c) 2015, 2020 IBM Corporation and others.
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
package org.eclipse.ui.internal.registry;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.e4.migration.PerspectiveBuilder;
import org.eclipse.ui.internal.e4.migration.PerspectiveReader;
import org.eclipse.ui.internal.wizards.preferences.PreferencesExportWizard;
import org.eclipse.ui.internal.wizards.preferences.PreferencesImportWizard;
import org.osgi.service.event.EventHandler;

public class ImportExportPespectiveHandler {

	private static final String PERSPECTIVE_SUFFIX_4X = "_e4persp"; //$NON-NLS-1$

	private static final String PERSPECTIVE_SUFFIX_3X = "_persp"; //$NON-NLS-1$

	private static final String ASCII_ENCODING = "ASCII"; //$NON-NLS-1$

	private static final String TRIMS_KEY = "trims"; //$NON-NLS-1$

	@Inject
	private EModelService modelService;

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	private Logger logger;

	@Inject
	@Preference(nodePath = "org.eclipse.ui.workbench")
	private IEclipsePreferences preferences;

	@Inject
	private IEclipseContext context;

	@Inject
	private PerspectiveRegistry perspectiveRegistry;

	private EventHandler importPreferencesEnd;
	private EventHandler exportPreferencesBegin;
	private EventHandler exportPreferencesEnd;
	private IPreferenceChangeListener preferenceListener;
	private boolean ignoreEvents;

	private List<MPerspective> exportedPersps = new ArrayList<>();
	private List<String> importedPersps = new ArrayList<>();
	private Map<String, String> minMaxPersistedState;

	@PostConstruct
	private void init() {
		initializeEventHandlers();
		preferences.addPreferenceChangeListener(preferenceListener);
		eventBroker.subscribe(PreferencesExportWizard.EVENT_EXPORT_BEGIN, exportPreferencesBegin);
		eventBroker.subscribe(PreferencesExportWizard.EVENT_EXPORT_END, exportPreferencesEnd);
		eventBroker.subscribe(PreferencesImportWizard.EVENT_IMPORT_END, importPreferencesEnd);
	}

	@PreDestroy
	private void dispose() {
		preferences.removePreferenceChangeListener(preferenceListener);
		eventBroker.unsubscribe(exportPreferencesBegin);
		eventBroker.unsubscribe(exportPreferencesEnd);
		eventBroker.unsubscribe(importPreferencesEnd);
	}

	private void importPerspective4x(PreferenceChangeEvent event) {
		importedPersps.add(event.getKey());
		MPerspective perspective = null;
		try {
			perspective = perspFromString((String) event.getNewValue());
			addShowInTags(perspective);
		} catch (IOException e) {
			logError(event, e);
		}
		addPerspectiveToWorkbench(perspective);
	}

	private void addShowInTags(MPerspective perspective) {
		if (perspective != null) {
			String targetId = getOriginalId(perspective.getElementId());
			ArrayList<String> showInTags = PerspectiveBuilder.getShowInPartFromRegistry(targetId);
			if (showInTags != null) {
				List<String> newTags = new ArrayList<>();
				for (String showIn : showInTags) {
					newTags.add(ModeledPageLayout.SHOW_IN_PART_TAG + showIn);
				}
				perspective.getTags().addAll(newTags);
			}
		}
	}

	private String getOriginalId(String id) {
		int index = id.lastIndexOf('.');
		if (index == -1)
			return id;
		return id.substring(0, index);
	}

	private void importPerspective3x(PreferenceChangeEvent event) {
		importedPersps.add(event.getKey());
		StringReader reader = new StringReader((String) event.getNewValue());
		MPerspective perspective = null;
		IEclipseContext childContext = context.createChild();
		try {
			childContext.set(PerspectiveReader.class, new PerspectiveReader(XMLMemento.createReadRoot(reader)));
			perspective = ContextInjectionFactory.make(PerspectiveBuilder.class, childContext).createPerspective();
		} catch (WorkbenchException e) {
			logError(event, e);
		} finally {
			reader.close();
			childContext.dispose();
		}
		addPerspectiveToWorkbench(perspective);
	}

	private void addPerspectiveToWorkbench(MPerspective perspective) {
		if (perspective == null) {
			return;
		}

		IPerspectiveDescriptor perspToOverwrite = perspectiveRegistry.findPerspectiveWithLabel(perspective.getLabel());

		// a new perspective
		if (perspToOverwrite == null) {
			perspectiveRegistry.addPerspective(perspective);
			importToolbarsLocation(perspective);
			return;
		}

		String perspToOverwriteId = perspToOverwrite.getId();
		// a perspective with the same label exists, but has different ID
		if (!perspective.getElementId().equals(perspToOverwriteId)) {
			perspective.setElementId(perspToOverwriteId);
		}
		perspectiveRegistry.deletePerspective(perspToOverwrite);
		perspectiveRegistry.addPerspective(perspective);
		importToolbarsLocation(perspective);
	}

	private void logError(PreferenceChangeEvent event, Exception e) {
		logger.error(e, String.format("Cannot read perspective \"%s\" from preferences", event.getKey())); //$NON-NLS-1$
	}

	private void copyPerspToPreferences(MPerspective persp) throws IOException {
		MPerspective perspClone = (MPerspective) modelService.cloneElement(persp, null);
		exportToolbarsLocation(perspClone);
		String perspAsString = perspToString(perspClone);
		preferences.put(perspClone.getLabel() + PERSPECTIVE_SUFFIX_4X, perspAsString);
	}

	private void exportToolbarsLocation(MPerspective persp) {
		Map<String, String> minMaxPersState = getMinMaxPersistedState();
		if (minMaxPersState == null) {
			return;
		}
		String trimsData = minMaxPersState.get(persp.getElementId());
		persp.getPersistedState().put(TRIMS_KEY, trimsData);
	}

	private void importToolbarsLocation(MPerspective persp) {
		String trimsData = persp.getPersistedState().get(TRIMS_KEY);
		if (trimsData == null || trimsData.trim().isEmpty()) {
			return;
		}
		persp.getPersistedState().remove(TRIMS_KEY);
		Map<String, String> minMaxPersState = getMinMaxPersistedState();
		if (minMaxPersState == null) {
			return;
		}
		minMaxPersState.put(persp.getElementId(), trimsData);
	}

	private Map<String, String> getMinMaxPersistedState() {
		if (minMaxPersistedState != null) {
			return minMaxPersistedState;
		}
		for (MAddon addon : application.getAddons()) {
			if ("MinMax Addon".equals(addon.getElementId())) { //$NON-NLS-1$
				minMaxPersistedState = addon.getPersistedState();
				break;
			}
		}
		return minMaxPersistedState;
	}

	private String perspToString(MPerspective persp) throws IOException {
		Resource resource = new E4XMIResourceFactory().createResource(null);
		resource.getContents().add((EObject) persp);
		try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			Map<String, Object> options = new HashMap<>();
			options.put(E4XMIResource.OPTION_FILTER_PERSIST_STATE, Boolean.TRUE);
			resource.save(output, options);
			resource.getContents().clear();
			return new String(output.toByteArray(), ASCII_ENCODING);
		}
	}

	private MPerspective perspFromString(String perspAsString) throws IOException {
		Resource resource = new E4XMIResourceFactory().createResource(null);
		try (InputStream input = new ByteArrayInputStream(perspAsString.getBytes(ASCII_ENCODING))) {
			resource.load(input, null);
		}
		MPerspective perspective = (MPerspective) resource.getContents().get(0);
		resource.getContents().clear();
		return perspective;
	}

	private void copyPerspsToPreferences() {
		for (MUIElement snippet : application.getSnippets()) {
			if (snippet instanceof MPerspective) {
				MPerspective persp = (MPerspective) snippet;
				exportedPersps.add(persp);
			}
		}
		ignoreEvents = true;
		for (MPerspective persp : exportedPersps) {
			try {
				copyPerspToPreferences(persp);
			} catch (IOException e) {
				logger.error(e, String.format("Cannot save perspective \"%s\" to preferences", persp.getElementId())); //$NON-NLS-1$
			}
		}
		ignoreEvents = false;
	}

	private void removeExportedPreferences() {
		ignoreEvents = true;
		for (MPerspective persp : exportedPersps) {
			preferences.remove(persp.getLabel() + PERSPECTIVE_SUFFIX_4X);
		}
		ignoreEvents = false;
		exportedPersps.clear();
	}

	private void removeImportedPreferences() {
		ignoreEvents = true;
		for (String key : importedPersps) {
			preferences.remove(key);
		}
		ignoreEvents = false;
		importedPersps.clear();

		// remove unused preference imported from Eclipse 3.x
		preferences.remove("perspectives"); //$NON-NLS-1$
	}

	private void initializeEventHandlers() {

		importPreferencesEnd = event -> removeImportedPreferences();

		exportPreferencesBegin = event -> copyPerspsToPreferences();

		exportPreferencesEnd = event -> removeExportedPreferences();

		preferenceListener = event -> {
			if (ignoreEvents) {
				return;
			}

			if (event.getKey().endsWith(PERSPECTIVE_SUFFIX_4X)) {
				importPerspective4x(event);
			} else if (event.getKey().endsWith(PERSPECTIVE_SUFFIX_3X)) {
				importPerspective3x(event);
			}
		};

	}

}
