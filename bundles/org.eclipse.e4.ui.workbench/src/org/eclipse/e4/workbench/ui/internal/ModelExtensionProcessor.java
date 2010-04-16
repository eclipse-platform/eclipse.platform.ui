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

import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MModelComponent;
import org.eclipse.e4.ui.model.application.MModelComponents;
import org.eclipse.e4.workbench.modeling.IModelExtension;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.Bundle;

/**
 * Process extensions to E4 model contributed via extensions.
 */
public class ModelExtensionProcessor {

	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$

	/**
	 * Preferred default parent ID, if none specified
	 */
	//	final private static String preferredID1 = "Horizontal Sash[right]"; //$NON-NLS-1$

	/**
	 * Alternative preferred default parent ID, if none specified
	 */
	//	final private static String preferredID2 = "bottom"; //$NON-NLS-1$

	private MApplication e4Window;

	/**
	 * Constructs processor for the model extensions on the MWindow
	 * 
	 * @param e4Window
	 */
	public ModelExtensionProcessor(MApplication e4Window) {
		this.e4Window = e4Window;
	}

	private Object findDefaultParent(String parentID) {
		MApplicationElement defaultElement = null;
		// Try the specified ID
		if (parentID != null) {
			defaultElement = findElementById(e4Window, parentID);
			if (defaultElement != null)
				return defaultElement;
		}

		// // Try first preferred ID
		// defaultElement = findElementById(e4Window, preferredID1);
		// if (defaultElement != null)
		// return (MElementContainer<MUIElement>) defaultElement;
		//
		// // Try second preferred ID - parent of "bottom"
		// defaultElement = findPart(e4Window, preferredID2);
		// if (defaultElement != null)
		// return defaultElement.getParent();

		return null;
	}

	public void addModelExtensions() {
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = extPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if (!"snippet".equals(ce.getName())) //$NON-NLS-1$
					continue;
				IContributor contributor = ce.getContributor();
				String attrURI = ce.getAttribute("uri"); //$NON-NLS-1$
				if (attrURI == null) {
					log("Unable to find location for the model extension \"{0}\"", //$NON-NLS-1$
							contributor.getName());
					continue;
				}

				URI uri;
				String bundleName = contributor.getName();
				String path = bundleName + '/' + attrURI;
				try {
					uri = URI.createPlatformPluginURI(path, false);
				} catch (RuntimeException e) {
					log("Model extension has invalid location", e); //$NON-NLS-1$
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
				if (!(extensionRoot instanceof MModelComponents)) {
					log("Unable to create model extension \"{0}\"", //$NON-NLS-1$
							contributor.getName());
					continue;
				}
				List<MModelComponent> snippets = ((MModelComponents) extensionRoot).getComponents();
				for (MModelComponent snippet : snippets) {
					Object parentElement = findDefaultParent(snippet.getParentID());
					if (parentElement == null) {
						log("Unable to find parent with ID \"{0}\" for extension \"{1}\"", //$NON-NLS-1$
								snippet.getParentID(), contributor.getName());
						continue;
					}

					EObject parentObject = ((EObject) parentElement);
					if (snippet.getProcessor() != null && snippet.getProcessor().length() > 0) {
						Bundle bundle = Activator.getDefault().getBundleForName(
								contributor.getName());
						if (bundle != null) {
							try {
								Class pc = bundle.loadClass(snippet.getProcessor());
								IModelExtension me = (IModelExtension) pc.newInstance();
								me.processElement(parentObject);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						continue;
					}

					Object[] elements = ((EObject) snippet).eContents().toArray();
					for (int i = 0; i < elements.length; i++) {
						EStructuralFeature sourceFeature = ((EObject) elements[i])
								.eContainingFeature();
						EStructuralFeature destinationFeature = parentObject.eClass()
								.getEStructuralFeature(sourceFeature.getName());

						if (destinationFeature == null) {
							log("Unable to find feature named \"{0}\"", //$NON-NLS-1$
									sourceFeature.getName());
							continue;
						}

						// TBD: 1) isn't it always a list?; 2) is there some utility to do this?
						Object oldValue = parentObject.eGet(destinationFeature);
						if (oldValue instanceof EList<?>)
							((EList) oldValue).add(elements[i]);
						else
							parentObject.eSet(destinationFeature, elements[i]);
					}
				}
			}
		}
	}

	private void log(String msg, Exception e) {
		// IEclipseContext context = e4Window.getContext();
		// Logger logger = (Logger) context.get(Logger.class.getName());
		// if (logger == null)
		e.printStackTrace();
		// else
		// logger.error(e, msg);
	}

	private void log(String msg, String arg) {
		// IEclipseContext context = e4Window.getContext();
		// Logger logger = (Logger) context.get(Logger.class.getName());
		// if (logger == null)
		System.err.println(com.ibm.icu.text.MessageFormat.format(msg, new Object[] { arg }));
		// else
		// logger.error(msg, arg);
	}

	private void log(String msg, String arg, String arg2) {
		// IEclipseContext context = e4Window.getContext();
		// Logger logger = (Logger) context.get(Logger.class.getName());
		// if (logger == null)
		System.err.println(com.ibm.icu.text.MessageFormat.format(msg, new Object[] { arg, arg2 }));
		// else
		// logger.error(msg, arg);
	}

	private MApplicationElement findElementById(MApplicationElement element, String id) {
		if (id == null || id.length() == 0)
			return null;
		// is it me?
		if (id.equals(element.getElementId()))
			return element;
		// Recurse if this is a container
		EList<EObject> elements = ((EObject) element).eContents();
		for (EObject childElement : elements) {
			if (!(childElement instanceof MApplicationElement))
				continue;
			MApplicationElement result = findElementById((MApplicationElement) childElement, id);
			if (result != null)
				return result;
		}
		return null;
	}

}
