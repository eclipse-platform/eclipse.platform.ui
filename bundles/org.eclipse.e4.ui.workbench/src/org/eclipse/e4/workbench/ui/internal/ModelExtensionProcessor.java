/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brian de Alwis - order of processing of components.e4xmi, bug 314761
 *******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.workbench.modeling.IModelExtension;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;

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
		IExtension[] extensions = topoSort(extPoint.getExtensions());

		List<MApplicationElement> imports = new ArrayList<MApplicationElement>();
		EList<EObject> addedElements = new BasicEList<EObject>();

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

				ResourceSet resourceSet = ((EObject) e4Window).eResource().getResourceSet();
				Resource resource;
				try {
					resource = resourceSet.getResource(uri, true);
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

				MModelComponents components = (MModelComponents) extensionRoot;

				List<MApplicationElement> localImports = components.getImports();
				if (localImports != null)
					imports.addAll(localImports);

				List<MModelComponent> snippets = components.getComponents();
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
								Class<?> pc = bundle.loadClass(snippet.getProcessor());
								IModelExtension me = (IModelExtension) pc.newInstance();
								me.processElement(parentObject);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						continue;
					}

					Object[] elements = ((EObject) snippet).eContents().toArray();
					addedElements.addAll(((EObject) snippet).eContents());
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

		if (imports.isEmpty())
			return;
		// now that we have all components loaded, resolve imports
		Map<MApplicationElement, MApplicationElement> importMaps = new HashMap<MApplicationElement, MApplicationElement>();
		for (MApplicationElement importedElement : imports) {
			MApplicationElement realElement = findElementById(e4Window, importedElement
					.getElementId());
			if (realElement == null)
				log(
						"Could not resolve an import element for '" + realElement + "'", new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
			importMaps.put(importedElement, realElement);
		}

		TreeIterator<EObject> it = EcoreUtil.getAllContents(addedElements);
		List<Runnable> commands = new ArrayList<Runnable>();

		// TODO Probably use EcoreUtil.UsageCrossReferencer
		while (it.hasNext()) {
			EObject o = it.next();

			EContentsEList.FeatureIterator<EObject> featureIterator = (EContentsEList.FeatureIterator<EObject>) o
					.eCrossReferences().iterator();
			while (featureIterator.hasNext()) {
				EObject importObject = featureIterator.next();
				if (importObject.eContainmentFeature() == ApplicationPackageImpl.Literals.MODEL_COMPONENTS__IMPORTS) {
					EStructuralFeature feature = featureIterator.feature();

					MApplicationElement el = importMaps.get(importObject);
					if (el == null) {
						log("Could not resolve import for " + el, new Exception()); //$NON-NLS-1$
					}

					final EObject interalTarget = o;
					final EStructuralFeature internalFeature = feature;
					final MApplicationElement internalElment = el;
					final EObject internalImportObject = importObject;

					commands.add(new Runnable() {

						public void run() {
							if (internalFeature.isMany()) {
								List<Object> l = (List<Object>) interalTarget.eGet(internalFeature);
								int index = l.indexOf(internalImportObject);
								if (index >= 0) {
									l.set(index, internalElment);
								}
							} else {
								interalTarget.eSet(internalFeature, internalElment);
							}
						}
					});
				}
			}
		}

		for (Runnable cmd : commands) {
			cmd.run();
		}
	}

	/**
	 * Sort the provided extensions by the dependencies of their contributors. Note that sorting is
	 * done in-place.
	 * 
	 * @param extensions
	 *            the list of extensions to be sorted
	 * @return the same list of extensions in a topologically-sorted order
	 */
	private IExtension[] topoSort(IExtension[] extensions) {
		if (extensions.length == 0) {
			return extensions;
		}

		PackageAdmin admin = Activator.getDefault().getBundleAdmin();
		final Map<String, Collection<IExtension>> mappedExtensions = new HashMap<String, Collection<IExtension>>();
		// Captures the bundles that are listed as requirements for a particular bundle.
		final Map<String, Collection<String>> requires = new HashMap<String, Collection<String>>();
		// Captures the bundles that list a particular bundle as a requirement
		final Map<String, Collection<String>> depends = new HashMap<String, Collection<String>>();

		// {@code requires} and {@code depends} define a graph where the vertices are
		// bundleIds and the edges are the requires-relation. {@code requires} defines
		// the out-edges for a vertex, and {@code depends} defines the in-edges for a vertex.
		//
		// Description of the algorithm:
		// (1) build up the graph: we only record the bundles actually being considered
		// (i.e., those that are contributors of {@code extensions})
		// (2) sort the list of bundles by their out-degree: the bundles with the least
		// out-edges are those that are depend on the fewest. If there is no bundles
		// with 0 out-edges, then we must have a cycle; oh well, can't win them all.
		// (3) take the bundle with lowest out-degree and add its extensions to the list.
		// Remove the bundle from the list, and remove it from all of its dependents'
		// required lists. This may require that the bundle list be resorted.
		//
		// Note this implementation assumes direct dependencies: if any of the bundles
		// are dependent through a third bundle, then the ordering will fail. To prevent
		// this would require recording the entire dependency subgraph for all contributors
		// of the {@code extensions}.

		// first build up the list of bundles actually being considered
		for (IExtension extension : extensions) {
			IContributor contributor = extension.getContributor();
			Collection<IExtension> exts = mappedExtensions.get(contributor.getName());
			if (exts == null) {
				mappedExtensions.put(contributor.getName(), exts = new ArrayList<IExtension>());
			}
			exts.add(extension);
			requires.put(contributor.getName(), new HashSet<String>());
			depends.put(contributor.getName(), new HashSet<String>());
		}

		// now populate the dependency graph
		for (String bundleId : mappedExtensions.keySet()) {
			assert requires.containsKey(bundleId) && depends.containsKey(bundleId);
			for (RequiredBundle requiredBundle : admin.getRequiredBundles(bundleId)) {
				assert requiredBundle.getSymbolicName().equals(bundleId);
				for (Bundle dependentBundle : requiredBundle.getRequiringBundles()) {
					if (!mappedExtensions.containsKey(dependentBundle.getSymbolicName())) {
						// not a contributor of an extension
						continue;
					}
					String depBundleId = dependentBundle.getSymbolicName();
					Collection<String> depBundleReqs = requires.get(depBundleId);
					depBundleReqs.add(bundleId);
					Collection<String> bundleDeps = depends.get(bundleId);
					assert bundleDeps != null;
					bundleDeps.add(depBundleId);
				}
			}
		}

		int resultIndex = 0;

		// sort by out-degree ({@code depends})
		// I suppose we could make {@code depends} a SortedMap, but we'd still need
		// to explicitly resort anyways
		List<String> sortedByOutdegree = new ArrayList<String>(requires.keySet());
		Comparator<String> outdegreeSorter = new Comparator<String>() {
			public int compare(String o1, String o2) {
				assert requires.containsKey(o1) && requires.containsKey(o2);
				return requires.get(o1).size() - requires.get(o2).size();
			}
		};
		Collections.sort(sortedByOutdegree, outdegreeSorter);
		if (!requires.get(sortedByOutdegree.get(0)).isEmpty()) {
			log("Extensions have a cycle", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		while (!sortedByOutdegree.isEmpty()) {
			// don't sort unnecessarily: the current ordering is fine providing
			// item #0 still has no dependencies
			if (!depends.get(sortedByOutdegree.get(0)).isEmpty()) {
				Collections.sort(sortedByOutdegree, outdegreeSorter);
			}
			String bundleId = sortedByOutdegree.remove(0);
			assert depends.containsKey(bundleId) && requires.containsKey(bundleId);
			for (IExtension ext : mappedExtensions.get(bundleId)) {
				extensions[resultIndex++] = ext;
			}
			assert requires.get(bundleId).isEmpty();
			requires.remove(bundleId);
			for (String depId : depends.get(bundleId)) {
				requires.get(depId).remove(bundleId);
			}
			depends.remove(bundleId);
		}
		assert resultIndex == extensions.length;
		return extensions;
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
