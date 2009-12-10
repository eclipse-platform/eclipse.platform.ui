/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import java.util.ArrayList;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

/**
 * Process extensions to E4 model contributed via extensions.
 */
public class ModelExtensionProcessor {

	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$

	/**
	 * Preferred default parent ID, if none specified
	 */
	final private static String preferredID1 = "Horizontal Sash[right]"; //$NON-NLS-1$

	/**
	 * Alternative preferred default parent ID, if none specified
	 */
	final private static String preferredID2 = "bottom"; //$NON-NLS-1$

	/**
	 * Model extensions as described by the extension registry
	 */
	private class ModelExtension {
		private String uri;
		private String parentID;
		private IContributor contributor;

		public ModelExtension(String uri, String parentID, IContributor contributor) {
			super();
			this.uri = uri;
			this.parentID = parentID;
			this.contributor = contributor;
		}

		public URI getURI() {
			if (uri == null)
				return null;
			String bundleName = contributor.getName();
			String path = bundleName + '/' + uri;
			try {
				return URI.createPlatformPluginURI(path, false);
			} catch (RuntimeException e) {
				log("Model extension has invalid location", e); //$NON-NLS-1$
				return null;
			}
		}

		public String getParentID() {
			return parentID;
		}

	}

	private MApplication e4Window;

	/**
	 * Constructs processor for the model extensions on the MWindow
	 * 
	 * @param e4Window
	 */
	public ModelExtensionProcessor(MApplication e4Window) {
		this.e4Window = e4Window;
	}

	/**
	 * Add extensions to the specified window.
	 */
	public void addModelExtensions() {
		ModelExtension[] extensions = readExtensionRegistry();
		for (ModelExtension extension : extensions) {
			URI uri = extension.getURI();
			if (uri == null) {
				log("Unable to find location for the model extension \"{0}\"", //$NON-NLS-1$
						extension.contributor.getName());
				continue;
			}
			Resource resource;
			try {
				resource = new ResourceSetImpl().getResource(uri, true);
			} catch (RuntimeException e) {
				log("Unable to read model extension", e); //$NON-NLS-1$
				continue;
			}
			EList<?> contents = resource.getContents();
			if (contents.isEmpty())
				continue;
			Object extensionRoot = contents.get(0);
			if (!(extensionRoot instanceof MUIElement)) {
				log("Unable to create model extension \"{0}\"", //$NON-NLS-1$
						extension.contributor.getName());
				continue;
			}

			MUIElement root = (MUIElement) extensionRoot;
			MElementContainer<MUIElement> parentElement = null;
			if (root instanceof MWindow)
				parentElement = (MElementContainer<MUIElement>) ((MUIElement) e4Window);
			else
				parentElement = findDefaultParent(extension.getParentID());
			if (parentElement != null)
				parentElement.getChildren().add(root);
		}
	}

	private MElementContainer<MUIElement> findDefaultParent(String parentID) {
		MUIElement defaultElement = null;
		// Try the specified ID
		if (parentID != null) {
			defaultElement = findElementById(e4Window, parentID);
			if (defaultElement != null)
				return (MElementContainer<MUIElement>) defaultElement;
		}

		// Try first preferred ID
		defaultElement = findElementById(e4Window, preferredID1);
		if (defaultElement != null)
			return (MElementContainer<MUIElement>) defaultElement;

		// Try second preferred ID - parent of "bottom"
		defaultElement = findPart(e4Window, preferredID2);
		if (defaultElement != null)
			return defaultElement.getParent();

		return null;
	}

	private ModelExtension[] readExtensionRegistry() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = extPoint.getExtensions();
		ArrayList<ModelExtension> result = new ArrayList<ModelExtension>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if (!"model".equals(ce.getName())) //$NON-NLS-1$
					continue;
				String attrURI = ce.getAttribute("location"); //$NON-NLS-1$
				String attrParent = ce.getAttribute("parentID"); //$NON-NLS-1$
				IContributor contributor = ce.getContributor();
				ModelExtension modelExt = new ModelExtension(attrURI, attrParent, contributor);
				result.add(modelExt);
			}
		}
		ModelExtension[] typedResult = new ModelExtension[result.size()];
		result.toArray(typedResult);
		return typedResult;
	}

	private void log(String msg, Exception e) {
		IEclipseContext context = e4Window.getContext();
		Logger logger = (Logger) context.get(Logger.class.getName());
		if (logger == null)
			e.printStackTrace();
		else
			logger.error(e, msg);
	}

	private void log(String msg, String arg) {
		IEclipseContext context = e4Window.getContext();
		Logger logger = (Logger) context.get(Logger.class.getName());
		if (logger == null)
			System.err.println(msg);
		else
			logger.error(msg, arg);
	}

	private MUIElement findElementById(MUIElement element, String id) {
		if (id == null || id.length() == 0)
			return null;
		// is it me?
		if (id.equals(element.getId()))
			return element;
		// Recurse if this is a container
		if (element instanceof MElementContainer<?>) {
			EList<MUIElement> children = ((MElementContainer<MUIElement>) element).getChildren();
			MUIElement foundElement = null;
			for (MUIElement childME : children) {
				foundElement = findElementById(childME, id);
				if (foundElement != null)
					return foundElement;
			}
		}
		return null;
	}

	private MPart findPart(MUIElement toSearch, String id) {
		if (toSearch == null)
			return null;
		MUIElement found = findElementById(toSearch, id);
		if (found instanceof MPart)
			return (MPart) found;
		return null;
	}

}
