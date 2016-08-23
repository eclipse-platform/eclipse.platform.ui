/*******************************************************************************
 * Copyright (c) 2010, 2016 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430075, 430080, 431464, 433336, 472654
 *     René Brandstetter - Bug 419749
 *     Brian de Alwis (MTI) - Bug 433053
 *     Alexandra Buzila - Refactoring, Bug 475934
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * The ModelAssembler is responsible for adding {@link MModelFragment fragments}
 * and {@link MApplicationElement} imports to the application model and running
 * pre- and post-processors on the model.
 */
public class ModelAssembler {

	@Inject
	private Logger logger;

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	@Inject
	private IExtensionRegistry registry;

	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$

	// private static final String ALWAYS = "always"; //$NON-NLS-1$
	private static final String INITIAL = "initial"; //$NON-NLS-1$
	private static final String NOTEXISTS = "notexists"; //$NON-NLS-1$

	/**
	 * Processes the application model. This will run pre-processors, process
	 * the fragments, resolve imports and run post-processors, in this order.
	 * <br>
	 * The <strong>org.eclipse.e4.workbench.model</strong> extension point will
	 * be used to retrieve the contributed fragments (with imports) and
	 * processors.<br>
	 * Extension points will be sorted based on the dependencies of their
	 * contributors.
	 *
	 * @param initial
	 *            <code>true</code> if running from a non-persisted state
	 */
	public void processModel(boolean initial) {
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = new ExtensionsSort().sort(extPoint.getExtensions());

		// run processors which are marked to run before fragments
		runProcessors(extensions, initial, false);
		// process fragments (and resolve imports)
		processFragments(extensions, initial);
		// run processors which are marked to run after fragments
		runProcessors(extensions, initial, true);
	}

	/**
	 * Adds the {@link MApplicationElement model elements} contributed by the
	 * {@link IExtension extensions} to the {@link MApplication application
	 * model}.
	 *
	 * @param extensions
	 *            the list of {@link IExtension} extension elements
	 * @param initial
	 *            <code>true</code> if running from a non-persisted state
	 *
	 */
	private void processFragments(IExtension[] extensions, boolean initial) {
		Set<ModelFragmentWrapper> fragmentList = new TreeSet<>(new ModelFragmentComparator());
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if ("fragment".equals(ce.getName()) && (initial || !INITIAL.equals(ce.getAttribute("apply")))) { //$NON-NLS-1$ //$NON-NLS-2$
					MModelFragments fragmentsContainer = getFragmentsContainer(ce);
					if (fragmentsContainer == null)
						continue;
					for (MModelFragment fragment : fragmentsContainer.getFragments()) {
						boolean checkExist = !initial && NOTEXISTS.equals(ce.getAttribute("apply")); //$NON-NLS-1$
						fragmentList.add(new ModelFragmentWrapper(fragmentsContainer, fragment,
								ce.getContributor().getName(), URIHelper.constructPlatformURI(ce.getContributor()),
								checkExist)); // $NON-NLS-1$
					}
				}
			}
		}
		processFragments(fragmentList);
	}

	/**
	 * Processes the given list of fragments wrapped in
	 * {@link ModelFragmentWrapper} elements.
	 *
	 * @param fragmentList
	 *            the list of fragments
	 */
	public void processFragments(Set<ModelFragmentWrapper> fragmentList) {
		for (ModelFragmentWrapper fragmentWrapper : fragmentList) {
			processFragment(fragmentWrapper.getFragmentContainer(), fragmentWrapper.getModelFragment(),
					fragmentWrapper.getContributorName(), fragmentWrapper.getContributorURI(),
					fragmentWrapper.isCheckExists());
		}
	}

	/**
	 * Adds the {@link MApplicationElement model elements} contributed by the
	 * {@link IConfigurationElement} to the application model and resolves any
	 * fragment imports along the way.
	 *
	 * @param fragmentsContainer
	 *            the {@link MModelFragments}
	 * @param fragment
	 *            the {@link MModelFragment}
	 * @param contributorName
	 *            the name of the element contributing the fragment
	 * @param contributorURI
	 *            the URI of the element contributin the fragment
	 * @param checkExist
	 *            specifies whether we should check that the application model
	 *            doesn't already contain the elements contributed by the
	 *            fragment before merging them
	 */
	public void processFragment(MModelFragments fragmentsContainer, MModelFragment fragment, String contributorName,
			String contributorURI, boolean checkExist) {
		/**
		 * The application elements that were added by the given
		 * IConfigurationElement to the application model
		 */
		List<MApplicationElement> addedElements = new ArrayList<>();

		if (fragmentsContainer == null) {
			return;
		}
		boolean evalImports = false;
		Diagnostic validationResult = Diagnostician.INSTANCE.validate((EObject) fragment);
		int severity = validationResult.getSeverity();
		if (severity == Diagnostic.ERROR) {
			logger.error(
					"Fragment from \"" + "uri.toString()" + "\" of \"" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ contributorName + "\" could not be validated and was not merged \"{0}\"", //$NON-NLS-1$
					fragment.toString());
		}

		List<MApplicationElement> merged = processModelFragment(fragment, contributorURI, checkExist);
		if (merged.size() > 0) {
			evalImports = true;
			addedElements.addAll(merged);
		} else {
			logger.debug("Nothing to merge for fragment \"{0}\" of \"{1}\"", contributorURI, //$NON-NLS-1$
					contributorName);
		}
		if (evalImports && fragmentsContainer.getImports().size() > 0) {
			resolveImports(fragmentsContainer.getImports(), addedElements);
		}
	}

	private MModelFragments getFragmentsContainer(IConfigurationElement ce) {
		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();
		ResourceSet resourceSet = applicationResource.getResourceSet();
		IContributor contributor = ce.getContributor();
		String attrURI = ce.getAttribute("uri"); //$NON-NLS-1$
		String bundleName = contributor.getName();
		if (attrURI == null) {
			logger.warn("Unable to find location for the model extension \"{0}\"", bundleName); //$NON-NLS-1$
			return null;
		}

		URI uri;
		try {
			// check if the attrURI is already a platform URI
			if (URIHelper.isPlatformURI(attrURI)) {
				uri = URI.createURI(attrURI);
			} else {
				String path = bundleName + '/' + attrURI;
				uri = URI.createPlatformPluginURI(path, false);
			}
		} catch (RuntimeException e) {
			logger.warn(e, "Invalid location \"" + attrURI + "\" of model extension \"" + bundleName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		}

		Resource resource;
		try {
			resource = resourceSet.getResource(uri, true);
		} catch (RuntimeException e) {
			logger.warn(e, "Unable to read model extension from \"" + uri.toString() + "\" of \"" + bundleName + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		}

		EList<?> contents = resource.getContents();
		if (contents.isEmpty()) {
			return null;
		}

		Object extensionRoot = contents.get(0);

		if (!(extensionRoot instanceof MModelFragments)) {
			logger.warn("Unable to create model extension \"{0}\"", bundleName); //$NON-NLS-1$
			return null;
		}
		return (MModelFragments) extensionRoot;
	}

	/**
	 * Contributes the given {@link MModelFragment} to the application model.
	 *
	 * @param fragment
	 *            the fragment to add to the application model
	 * @param contributorURI
	 *            the URI of the element that contributes this fragment
	 * @param checkExist
	 *            specifies whether we should check that the application model
	 *            doesn't already contain the elements contributed by the
	 *            fragment before merging them
	 * @return a list of the {@link MApplicationElement} elements that were
	 *         merged into the application model by the fragment
	 */
	public List<MApplicationElement> processModelFragment(MModelFragment fragment, String contributorURI,
			boolean checkExist) {

		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();

		List<MApplicationElement> elements = fragment.getElements();
		if (elements.size() == 0) {
			return new ArrayList<>();
		}

		for (MApplicationElement el : elements) {
			EObject o = (EObject) el;

			E4XMIResource r = (E4XMIResource) o.eResource();

			if (checkExist && applicationResource.getIDToEObjectMap().containsKey(r.getID(o))) {
				continue;
			}

			applicationResource.setID(o, r.getID(o));

			if (contributorURI != null)
				el.setContributorURI(contributorURI);

			// Remember IDs of subitems
			TreeIterator<EObject> treeIt = EcoreUtil.getAllContents(o, true);
			while (treeIt.hasNext()) {
				EObject eObj = treeIt.next();
				r = (E4XMIResource) eObj.eResource();
				if (contributorURI != null && (eObj instanceof MApplicationElement))
					((MApplicationElement) eObj).setContributorURI(contributorURI);
				applicationResource.setID(eObj, r.getInternalId(eObj));
			}
		}

		return fragment.merge(application);
	}

	/**
	 * Executes the processors as declared in provided {@link IExtension
	 * extensions} array.
	 *
	 * @param extensions
	 *            the array of {@link IExtension} extensions containing the
	 *            processors
	 * @param initial
	 *            <code>true</code> if the application is running from a
	 *            non-persisted state
	 * @param afterFragments
	 *            <code>true</code> if the processors that should be run before
	 *            model fragments are merged are to be executed,
	 *            <code>false</code> otherwise
	 */
	public void runProcessors(IExtension[] extensions, boolean initial, boolean afterFragments) {
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				boolean parseBoolean = Boolean.parseBoolean(ce.getAttribute("beforefragment")); //$NON-NLS-1$
				if ("processor".equals(ce.getName()) && afterFragments != parseBoolean) { //$NON-NLS-1$
					if (initial || !INITIAL.equals(ce.getAttribute("apply"))) { //$NON-NLS-1$
						runProcessor(ce);
					}
				}
			}
		}
	}

	private void runProcessor(IConfigurationElement ce) {
		IEclipseContext localContext = EclipseContextFactory.create();
		IContributionFactory factory = context.get(IContributionFactory.class);

		for (IConfigurationElement ceEl : ce.getChildren("element")) { //$NON-NLS-1$
			String id = ceEl.getAttribute("id"); //$NON-NLS-1$

			if (id == null) {
				logger.warn("No element id given"); //$NON-NLS-1$
				continue;
			}

			String key = ceEl.getAttribute("contextKey"); //$NON-NLS-1$
			if (key == null) {
				key = id;
			}

			MApplicationElement el = ModelUtils.findElementById(application, id);
			if (el == null) {
				logger.warn("Could not find element with id '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			localContext.set(key, el);
		}

		try {
			Object o = factory.create("bundleclass://" + ce.getContributor().getName() + "/" + ce.getAttribute("class"), //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					context, localContext);
			if (o == null) {
				logger.warn("Unable to create processor " + ce.getAttribute("class") + " from " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ ce.getContributor().getName());
			} else {
				ContextInjectionFactory.invoke(o, Execute.class, context, localContext);
			}
		} catch (Exception e) {
			logger.warn(e, "Could not run processor"); //$NON-NLS-1$
		}
	}

	/**
	 * Resolves the given list of imports used by the specified
	 * <code>addedElements</code> in the application model.
	 *
	 * @param imports
	 *            the list of elements that were imported by fragments and
	 *            should be resolved in the application model
	 * @param addedElements
	 *            the list of elements contributed by the fragments to the
	 *            application model
	 */
	public void resolveImports(List<MApplicationElement> imports, List<MApplicationElement> addedElements) {
		if (imports.isEmpty())
			return;
		// now that we have all components loaded, resolve imports
		Map<MApplicationElement, MApplicationElement> importMaps = new HashMap<>();
		for (MApplicationElement importedElement : imports) {
			MApplicationElement realElement = ModelUtils.findElementById(application, importedElement.getElementId());
			if (realElement == null) {
				logger.warn("Could not resolve an import element for '" + realElement + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			importMaps.put(importedElement, realElement);
		}

		TreeIterator<EObject> it = EcoreUtil.getAllContents(addedElements);
		List<Runnable> commands = new ArrayList<>();

		// TODO Probably use EcoreUtil.UsageCrossReferencer
		while (it.hasNext()) {
			EObject o = it.next();

			EContentsEList.FeatureIterator<EObject> featureIterator = (EContentsEList.FeatureIterator<EObject>) o
					.eCrossReferences().iterator();
			while (featureIterator.hasNext()) {
				EObject importObject = featureIterator.next();
				if (importObject.eContainmentFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					EStructuralFeature feature = featureIterator.feature();

					MApplicationElement el = importMaps.get(importObject);
					if (el == null) {
						logger.warn("Could not resolve import for " + el); //$NON-NLS-1$
					}

					final EObject interalTarget = o;
					final EStructuralFeature internalFeature = feature;
					final MApplicationElement internalElment = el;
					final EObject internalImportObject = importObject;

					commands.add(new Runnable() {

						@Override
						public void run() {
							if (internalFeature.isMany()) {
								logger.error("Replacing"); //$NON-NLS-1$
								@SuppressWarnings("unchecked")
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
}
