/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 473063
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 500420
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * Perspective registry.
 */
public class PerspectiveRegistry implements IPerspectiveRegistry, IExtensionChangeHandler {

	@Inject
	private IExtensionRegistry extensionRegistry;

	@Inject
	EModelService modelService;

	@Inject
	MApplication application;

	@Inject
	IEclipseContext context;

	private IEclipseContext impExpHandlerContext;

	@Inject
	Logger logger;

	private Map<String, PerspectiveDescriptor> descriptors = new HashMap<>();

	@PostConstruct
	void postConstruct(MApplication application) {
		IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.perspectives"); //$NON-NLS-1$
		for (IConfigurationElement element : point.getConfigurationElements()) {
			String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
			descriptors.put(id, new PerspectiveDescriptor(id, element));
		}

		List<MUIElement> snippets = application.getSnippets();
		for (MUIElement snippet : snippets) {
			if (snippet instanceof MPerspective) {
				MPerspective perspective = (MPerspective) snippet;
				String id = perspective.getElementId();

				// See if the clone is customizing an a predefined perspective without changing its name
				PerspectiveDescriptor existingDescriptor = descriptors.get(id);

				if (existingDescriptor == null) {
					// A custom perspective with its own name.
					createDescriptor(perspective);
				} else {
					// A custom perspecitve with a name of a pre-defined perspective
					existingDescriptor.setHasCustomDefinition(true);
				}
			}
		}

		impExpHandlerContext = context.createChild();
		impExpHandlerContext.set(PerspectiveRegistry.class, this);
		ContextInjectionFactory.make(ImportExportPespectiveHandler.class, impExpHandlerContext);
	}

	public void addPerspective(MPerspective perspective) {
		application.getSnippets().add(perspective);
		createDescriptor(perspective);
	}

	private void createDescriptor(MPerspective perspective) {
		String label = perspective.getLocalizedLabel();
		String originalId = getOriginalId(perspective);
		PerspectiveDescriptor originalDescriptor = descriptors.get(originalId);
		String id = perspective.getElementId();
		PerspectiveDescriptor newDescriptor = new PerspectiveDescriptor(id, label, originalDescriptor);

		if (perspective.getIconURI() != null) {
			try {
				ImageDescriptor img = ImageDescriptor.createFromURL(new URI(perspective.getIconURI()).toURL());
				newDescriptor.setImageDescriptor(img);
			} catch (MalformedURLException | URISyntaxException e) {
				logger.warn(e, MessageFormat.format("Error on applying configured perspective icon: {0}", //$NON-NLS-1$
						perspective.getIconURI()));
			}
		}

		descriptors.put(id, newDescriptor);
	}

	/**
	 * Construct a new registry.
	 */
	public PerspectiveRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, null);

	}

	@Override
	public IPerspectiveDescriptor clonePerspective(String id, String label,
			IPerspectiveDescriptor desc) throws IllegalArgumentException {
		// FIXME: compat clonePerspective. Not called in 3.8
		E4Util.unsupported("clonePerspective"); //$NON-NLS-1$
		return null;
	}

	@Override
	public void deletePerspective(IPerspectiveDescriptor toDelete) {
		PerspectiveDescriptor perspective = (PerspectiveDescriptor) toDelete;
		if (perspective.isPredefined())
			return;

		descriptors.remove(perspective.getId());
		removeSnippet(application, perspective.getId());
	}

	private MUIElement removeSnippet(MSnippetContainer snippetContainer, String id) {
		MUIElement snippet = modelService.findSnippet(snippetContainer, id);
		if (snippet != null)
			snippetContainer.getSnippets().remove(snippet);
		return snippet;
	}

	/**
	 * Deletes a list of perspectives
	 *
	 * @param perspToDelete
	 */
	public void deletePerspectives(ArrayList<IPerspectiveDescriptor> perspToDelete) {
		for (int i = 0; i < perspToDelete.size(); i++) {
			deletePerspective(perspToDelete.get(i));
		}
	}


	@Override
	public IPerspectiveDescriptor findPerspectiveWithId(String perspectiveId) {
		return findPerspectiveWithId(perspectiveId, true);
	}

	public IPerspectiveDescriptor findPerspectiveWithId(String perspectiveId,
			boolean considerRestrictRules) {
		IPerspectiveDescriptor candidate = descriptors.get(perspectiveId);
		if (considerRestrictRules && WorkbenchActivityHelper.restrictUseOf(candidate)) {
			return null;
		}
		return candidate;
	}

	@Override
	public IPerspectiveDescriptor findPerspectiveWithLabel(String label) {
		for (IPerspectiveDescriptor descriptor : descriptors.values()) {
			if (descriptor.getLabel().equals(label)) {
				if (WorkbenchActivityHelper.restrictUseOf(descriptor)) {
					return null;
				}
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public String getDefaultPerspective() {
		String defaultId = PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
		// empty string may be returned but we want to return null if nothing
		// found
		if (defaultId.length() == 0 || findPerspectiveWithId(defaultId) == null) {
			Workbench instance = Workbench.getInstance();
			return instance == null ? null : instance.getDefaultPerspectiveId();
		}

		return defaultId;
	}

	@Override
	public IPerspectiveDescriptor[] getPerspectives() {
		Collection<?> descs = WorkbenchActivityHelper.restrictCollection(descriptors.values(),
				new ArrayList<>());
		return descs.toArray(new IPerspectiveDescriptor[descs.size()]);
	}

	/**
	 * @see IPerspectiveRegistry#setDefaultPerspective(String)
	 */
	@Override
	public void setDefaultPerspective(String id) {
		IPerspectiveDescriptor desc = findPerspectiveWithId(id);
		if (desc != null) {
			PrefUtil.getAPIPreferenceStore().setValue(
					IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, id);
		}
	}

	/**
	 * Return <code>true</code> if a label is valid. This checks only the given
	 * label in isolation. It does not check whether the given label is used by
	 * any existing perspectives.
	 *
	 * @param label
	 *            the label to test
	 * @return whether the label is valid
	 */
	public boolean validateLabel(String label) {
		label = label.trim();
		if (label.length() <= 0) {
			return false;
		}
		return true;
	}


	@Override
	public void revertPerspective(IPerspectiveDescriptor perspToRevert) {
		PerspectiveDescriptor perspective = (PerspectiveDescriptor) perspToRevert;
		if (!perspective.isPredefined())
			return;

		perspective.setHasCustomDefinition(false);
		removeSnippet(application, perspective.getId());
	}

	/**
	 * Dispose the receiver.
	 */
	public void dispose() {
		if (impExpHandlerContext != null) {
			impExpHandlerContext.dispose();
		}
		PlatformUI.getWorkbench().getExtensionTracker().unregisterHandler(this);
		// FIXME: what was this listener for?
		// WorkbenchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(
		// preferenceListener);
	}

	@Override
	public void removeExtension(IExtension source, Object[] objects) {
		// TODO compat: what do we do about disappearing extensions
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension addedExtension) {
		// TODO compat: what do we do about appeaering extensions
	}

	/**
	 * Create a new perspective.
	 *
	 * @param label
	 *            the name of the new descriptor
	 * @param originalDescriptor
	 *            the descriptor on which to base the new descriptor
	 * @return a new perspective descriptor or <code>null</code> if the creation
	 *         failed.
	 */
	public PerspectiveDescriptor createPerspective(String label,
			PerspectiveDescriptor originalDescriptor) {

		String newID = createNewId(label, originalDescriptor);
		PerspectiveDescriptor newDescriptor = new PerspectiveDescriptor(newID, label,
				originalDescriptor);
		descriptors.put(newDescriptor.getId(), newDescriptor);
		return newDescriptor;
	}

	/**
	 * Return an id for the new descriptor.
	 *
	 * The id must encode the original id. id is of the form <originalId>.label
	 *
	 * @param label
	 * @param originalDescriptor
	 * @return the new id
	 */
	private String createNewId(String label, PerspectiveDescriptor originalDescriptor) {
		return originalDescriptor.getOriginalId() + '.' + label;
	}

	private String getOriginalId(MPerspective p) {
		String id = p.getElementId();
		String label = p.getLabel();
		int index = id.lastIndexOf('.');
		// Custom perspectives store the user defined names in their labels
		if (id.endsWith(label)) {
			index = id.lastIndexOf(label) - 1;
		}
		if (index == -1) {
			return id;
		}
		return id.substring(0, index);
	}
}
