/*******************************************************************************
 * Copyright (c) 2010, 2021 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430075, 430080, 431464, 433336, 472654
 *     René Brandstetter - Bug 419749
 *     Brian de Alwis (MTI) - Bug 433053
 *     Alexandra Buzila - Refactoring, Bug 475934
 *     Gerhard Kreuzer - Bug 561324
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.fragment.MModelFragment;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.model.fragment.MStringModelFragment;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.workbench.modeling.IModelProcessorContribution;
import org.eclipse.e4.ui.workbench.modeling.IModelProcessorContribution.ModelElement;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * The ModelAssembler is responsible for adding {@link MModelFragment fragments}
 * and {@link MApplicationElement} imports to the application model and running
 * pre- and post-processors on the model.
 */
@Component(service = ModelAssembler.class, immediate = true)
public class ModelAssembler {

	private class Bucket {
		SortedSet<ModelFragmentWrapper> wrapper = new TreeSet<>(new ModelFragmentComparator(application));
		Bucket dependentOn;
		Set<Bucket> dependencies = new LinkedHashSet<>();
		Set<String> containedElementIds = new LinkedHashSet<>();
	}

	private class FragmentWrapperElementMapping {
		ModelFragmentWrapper wrapper;
		List<MApplicationElement> elements;
	}

	private class ModelFragmentBundleTracker implements BundleTrackerCustomizer<List<FragmentWrapperElementMapping>> {

		@Override
		public List<FragmentWrapperElementMapping> addingBundle(Bundle bundle, BundleEvent event) {
			// only react on bundles with Model-Fragment header
			if (bundle.getHeaders("").get(MODEL_FRAGMENT_HEADER) != null) { //$NON-NLS-1$
				// add the fragment to the application model
				List<ModelFragmentWrapper> wrappers = getModelFragmentWrapperFromBundle(bundle,
						ModelAssembler.this.initial);

				List<FragmentWrapperElementMapping> mappings = wrappers.stream().map(w -> {
					FragmentWrapperElementMapping mapping = new FragmentWrapperElementMapping();
					mapping.wrapper = w;
					mapping.elements = new ArrayList<>(w.getModelFragment().getElements());
					return mapping;
				}).collect(Collectors.toList());

				// we skip direct processing in case the startup model processing is not done
				// yet
				if (processModelExecuted) {
					uiSync.asyncExec(() -> processFragmentWrappers(wrappers));
				}

				return mappings;
			}
			return null;
		}

		@Override
		public void modifiedBundle(Bundle bundle, BundleEvent event, List<FragmentWrapperElementMapping> mapping) {
			// do nothing
		}

		@Override
		public void removedBundle(Bundle bundle, BundleEvent event, List<FragmentWrapperElementMapping> mappings) {
			// remove fragment elements from application model
			uiSync.asyncExec(() -> {
				if (mappings != null) {
					mappings.stream().flatMap(m -> m.elements.stream()).forEach(appElement -> {
						// TODO implement removal of contributions, e.g. MenuContributions

						if (appElement instanceof MUIElement) {
							MUIElement element = (MUIElement) appElement;
							element.setToBeRendered(false);
							if (element.getParent() != null) {
								element.getParent().getChildren().remove(element);
							}
						}
					});

					// unload resource
					String bundleName = bundle.getSymbolicName();
					String fragmentHeader = bundle.getHeaders("").get(MODEL_FRAGMENT_HEADER); //$NON-NLS-1$
					String[] fr = fragmentHeader.split(";"); //$NON-NLS-1$
					if (fr.length > 0) {
						String attrURI = fr[0];
						E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();
						ResourceSet resourceSet = applicationResource.getResourceSet();
						if (attrURI == null) {
							log(LogLevel.WARN, "Unable to find location for the model extension \"{0}\"", bundleName); //$NON-NLS-1$
							return;
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
							log(LogLevel.WARN, "Invalid location \"{0}\" of model extension \"\"", attrURI, bundleName, //$NON-NLS-1$
									e);
							return;
						}

						try {
							Resource resource = resourceSet.getResource(uri, true);
							resource.unload();
						} catch (RuntimeException e) {
							log(LogLevel.WARN, "Unable to read model extension from \"{0}\" of \"{1}\"", uri, //$NON-NLS-1$
									bundleName);
						}
					}

				}
			});
		}
	}

	private MApplication application;
	private IEclipseContext context;
	private UISynchronize uiSync;
	private boolean initial;

	private static final String EXTENSION_POINT_ID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$
	private static final String MODEL_FRAGMENT_HEADER = "Model-Fragment"; //$NON-NLS-1$

	private static final String ALWAYS = "always"; //$NON-NLS-1$
	private static final String INITIAL = "initial"; //$NON-NLS-1$
	private static final String NOTEXISTS = "notexists"; //$NON-NLS-1$

	LoggerFactory factory;
	Logger logger;

	private IExtensionRegistry registry;

	private CopyOnWriteArrayList<ServiceReference<IModelProcessorContribution>> processorContributions = new CopyOnWriteArrayList<>();

	BundleContext bundleContext;

	BundleTracker<List<FragmentWrapperElementMapping>> tracker;

	private boolean processModelExecuted = false;

	@Activate
	void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;

		this.tracker = new BundleTracker<>(bundleContext,
				Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING, new ModelFragmentBundleTracker());
	}

	@Deactivate
	void deactivate() {
		if (this.tracker != null) {
			this.tracker.close();
		}
	}

	/**
	 * Setter for the {@link IExtensionRegistry}. Use public method binding instead
	 * of field binding to enable testing.
	 *
	 * @param registry The {@link IExtensionRegistry} to use.
	 */
	@Reference
	public void setExtensionRegistry(IExtensionRegistry registry) {
		this.registry = registry;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void setModelProcessorContribution(ServiceReference<IModelProcessorContribution> contrib) {
		this.processorContributions.add(contrib);

		// we skip direct processing in case the startup model processing is not done
		// yet
		if (processModelExecuted) {
			uiSync.asyncExec(() -> {
				IModelProcessorContribution service = bundleContext.getService(contrib);
				runProcessor(service);
			});
		}
	}

	void unsetModelProcessorContribution(ServiceReference<IModelProcessorContribution> contrib) {
		this.processorContributions.remove(contrib);
		IModelProcessorContribution service = bundleContext.getService(contrib);

		if (this.context != null) {
			uiSync.asyncExec(() -> {
				try {
					ContextInjectionFactory.invoke(service, PreDestroy.class, context);
				} catch (Exception e) {
					log(LogLevel.WARN, "Could not run PreDestroy on processor {0}: {1}", contrib.getClass().getName(), //$NON-NLS-1$
							e);
				}
			});
		}
	}

	/**
	 * Initialize this {@link ModelAssembler} with references from the Eclipse
	 * layer. Used via CIF#invoke instead of CIF#inject or CIF#make to avoid that
	 * this instance is managed by the Eclipse injection.
	 *
	 * @param application the application model
	 * @param context     the workbench context needed for injection
	 * @param sync        to dynamically add/remove UI elements in the UI thread
	 */
	@PostConstruct
	public void init(MApplication application, IEclipseContext context, UISynchronize sync) {
		this.application = application;
		this.context = context;
		this.uiSync = sync;
	}

	/**
	 * Processes the application model. This will run pre-processors, process the
	 * fragments, resolve imports and run post-processors, in this order. <br>
	 * The <strong>org.eclipse.e4.workbench.model</strong> extension point will be
	 * used to retrieve the contributed fragments (with imports) and processors.<br>
	 * Extension points will be sorted based on the dependencies of their
	 * contributors.
	 *
	 * @param initial     <code>true</code> if running from a non-persisted state
	 */
	@Execute
	public void processModel(boolean initial) {
		this.initial = initial;

		IExtensionPoint extPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
		IExtension[] extensions = new ExtensionsSort().sort(extPoint.getExtensions());

		// run processors which are marked to run before fragments
		runProcessors(extensions, initial, false);
		// process fragments (and resolve imports)
		processFragments(extensions, initial);
		// run processors which are marked to run after fragments
		runProcessors(extensions, initial, true);

		// once we are done, any further handling in the tracker can't be initial
		// anymore
		this.initial = false;
		this.processModelExecuted = true;
	}

	/**
	 * Adds the {@link MApplicationElement model elements} contributed by the
	 * {@link IExtension extensions} to the {@link MApplication application model}.
	 *
	 * @param extensions the list of {@link IExtension} extension elements
	 * @param initial    <code>true</code> if running from a non-persisted state
	 *
	 */
	private void processFragments(IExtension[] extensions, boolean initial) {
		List<ModelFragmentWrapper> wrappers = new ArrayList<>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces) {
				if ("fragment".equals(ce.getName()) && (initial || !INITIAL.equals(ce.getAttribute("apply")))) { //$NON-NLS-1$ //$NON-NLS-2$
					MModelFragments fragmentsContainer = getFragmentsContainer(ce.getAttribute("uri"), //$NON-NLS-1$
							ce.getContributor().getName());
					if (fragmentsContainer == null) {
						continue;
					}
					for (MModelFragment fragment : fragmentsContainer.getFragments()) {
						boolean checkExist = !initial && NOTEXISTS.equals(ce.getAttribute("apply")); //$NON-NLS-1$
						wrappers.add(new ModelFragmentWrapper(fragmentsContainer, fragment,
								ce.getContributor().getName(), URIHelper.constructPlatformURI(ce.getContributor()),
								checkExist)); // $NON-NLS-1$
					}
				}
			}
		}

		if (this.tracker != null) {
			// this triggers initial bundle tracking in the current thread
			// for startup reasons we do not process each fragment on initial tracking by
			// its own, instead we will process the initially tracked bundles together once
			// the initial tracking is done
			this.tracker.open();

			// once the initial tracking is done we process the tracked bundles
			// this is for performance optimization on initial loading to avoid multiple
			// fragment merge operations
			List<ModelFragmentWrapper> collect = this.tracker.getTracked().values().stream()
					.flatMap(List::stream).map(w -> w.wrapper).collect(Collectors.toList());
			wrappers.addAll(collect);
		}

		processFragmentWrappers(wrappers);
	}

	private List<ModelFragmentWrapper> getModelFragmentWrapperFromBundle(Bundle bundle, boolean initial) {
		List<ModelFragmentWrapper> wrappers = new ArrayList<>();
		String fragmentHeader = bundle.getHeaders("").get(MODEL_FRAGMENT_HEADER); //$NON-NLS-1$
		String[] fr = fragmentHeader.split(";"); //$NON-NLS-1$
		if (fr.length > 0) {
			String uri = fr[0];
			String apply = fr.length > 1 ? fr[1].split("=")[1] : "always"; //$NON-NLS-1$ //$NON-NLS-2$

			// check if the value for apply is valid
			if (!ALWAYS.equals(apply) && !INITIAL.equals(apply) && !NOTEXISTS.equals(apply)) {
				log(LogLevel.WARN, "Model-Fragment header apply attribute \"{0}\" is invalid, falling back to always", //$NON-NLS-1$
						apply);
				apply = ALWAYS;
			}

			if (initial || !INITIAL.equals(apply)) {
				MModelFragments fragmentsContainer = getFragmentsContainer(uri, bundle.getSymbolicName());
				if (fragmentsContainer != null) {
					for (MModelFragment fragment : fragmentsContainer.getFragments()) {
						boolean checkExist = !initial && NOTEXISTS.equals(apply);
						wrappers.add(new ModelFragmentWrapper(fragmentsContainer, fragment, bundle.getSymbolicName(),
								URIHelper.constructPlatformURI(bundle), checkExist)); // $NON-NLS-1$
					}
				}
			}
		} else {
			log(LogLevel.ERROR, "Model-Fragment header value \"{0}\" in bundle \\\"{1}\\\" is invalid", //$NON-NLS-1$
					fragmentHeader, bundle.getSymbolicName());
		}

		return wrappers;
	}

	/**
	 * Processes the given list of fragments wrapped in {@link ModelFragmentWrapper}
	 * elements.
	 *
	 * @param wrappers the list of fragments
	 */
	public void processFragmentWrappers(Collection<ModelFragmentWrapper> wrappers) {
		Map<String, Bucket> elementIdToBucket = new LinkedHashMap<>();
		Map<String, Bucket> parentIdToBuckets = new LinkedHashMap<>();
		for (ModelFragmentWrapper fragmentWrapper : wrappers) {
			MModelFragment fragment = fragmentWrapper.getModelFragment();
			String parentId = MStringModelFragment.class.cast(fragment).getParentElementId();
			if (!parentIdToBuckets.containsKey(parentId)) {
				parentIdToBuckets.put(parentId, new Bucket());
			}
			Bucket b = parentIdToBuckets.get(parentId);
			if (elementIdToBucket.containsKey(parentId)) {
				Bucket parentBucket = elementIdToBucket.get(parentId);
				parentBucket.dependencies.add(b);
				b.dependentOn = parentBucket;
			}
			b.wrapper.add(fragmentWrapper); // $NON-NLS-1$

			for (MApplicationElement e : fragment.getElements()) {
				// Error case -> clean up and ignore
				if (parentId == e.getElementId()) {
					continue;
				}
				elementIdToBucket.put(e.getElementId(), b);
				b.containedElementIds.add(e.getElementId());
				if (parentIdToBuckets.containsKey(e.getElementId())) {
					Bucket childBucket = parentIdToBuckets.get(e.getElementId());
					b.dependencies.add(childBucket);
					childBucket.dependentOn = b;
				}
			}
		}
		processFragments(createUnifiedFragmentList(elementIdToBucket));
	}

	private List<ModelFragmentWrapper> createUnifiedFragmentList(Map<String, Bucket> elementIdToBucket) {
		List<ModelFragmentWrapper> fragmentList = new ArrayList<>();
		Set<String> checkedElementIds = new LinkedHashSet<>();
		for (Entry<String, Bucket> entry : elementIdToBucket.entrySet()) {
			if (checkedElementIds.contains(entry.getKey())) {
				continue;
			}
			Bucket bucket = entry.getValue();
			while (bucket.dependentOn != null) {
				bucket = bucket.dependentOn;
			}
			addAllBucketFragmentWrapper(bucket, fragmentList, checkedElementIds);
		}
		return fragmentList;
	}

	private void addAllBucketFragmentWrapper(Bucket bucket, List<ModelFragmentWrapper> fragmentList,
			Set<String> checkedElementIds) {
		fragmentList.addAll(bucket.wrapper);
		checkedElementIds.addAll(bucket.containedElementIds);
		for (Bucket child : bucket.dependencies) {
			addAllBucketFragmentWrapper(child, fragmentList, checkedElementIds);
		}
	}

	public void processFragments(Collection<ModelFragmentWrapper> fragmentList) {
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
	 * @param fragmentsContainer the {@link MModelFragments}
	 * @param fragment           the {@link MModelFragment}
	 * @param contributorName    the name of the element contributing the fragment
	 * @param contributorURI     the URI of the element contribution the fragment
	 * @param checkExist         specifies whether we should check that the
	 *                           application model doesn't already contain the
	 *                           elements contributed by the fragment before merging
	 *                           them
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
			log(LogLevel.ERROR,
					"Fragment from \"{0}\" of \"{1}\" could not be validated and was not merged: " //$NON-NLS-1$
							+ fragment, contributorURI, contributorName);
		}

		List<MApplicationElement> merged = processModelFragment(fragment, contributorURI, checkExist);
		if (!merged.isEmpty()) {
			evalImports = true;
			addedElements.addAll(merged);
		} else {
			log(LogLevel.DEBUG, "Nothing to merge for fragment \"{0}\" of \"{1}\"", contributorURI, //$NON-NLS-1$
					contributorName);
		}
		if (evalImports && fragmentsContainer.getImports().size() > 0) {
			resolveImports(fragmentsContainer.getImports(), addedElements);
		}
	}

	private MModelFragments getFragmentsContainer(String attrURI, String bundleName) {
		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();
		ResourceSet resourceSet = applicationResource.getResourceSet();
		if (attrURI == null) {
			log(LogLevel.WARN, "Unable to find location for the model extension \"{0}\"", bundleName); //$NON-NLS-1$
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
			log(LogLevel.WARN, "Invalid location \"{0}\" of model extension \"\"", attrURI, bundleName, e); //$NON-NLS-1$
			return null;
		}

		Resource resource;
		try {
			resource = resourceSet.getResource(uri, true);
		} catch (RuntimeException e) {
			log(LogLevel.WARN, "Unable to read model extension from \"{0}\" of \"{1}\"", uri, bundleName); //$NON-NLS-1$
			return null;
		}

		EList<?> contents = resource.getContents();
		if (contents.isEmpty()) {
			return null;
		}

		Object extensionRoot = contents.get(0);

		if (!(extensionRoot instanceof MModelFragments)) {
			log(LogLevel.WARN, "Unable to create model extension \"{0}\"", bundleName); //$NON-NLS-1$
			return null;
		}
		return (MModelFragments) extensionRoot;
	}

	/**
	 * Contributes the given {@link MModelFragment} to the application model.
	 *
	 * @param fragment       the fragment to add to the application model
	 * @param contributorURI the URI of the element that contributes this fragment
	 * @param checkExist     specifies whether we should check that the application
	 *                       model doesn't already contain the elements contributed
	 *                       by the fragment before merging them
	 * @return a list of the {@link MApplicationElement} elements that were merged
	 *         into the application model by the fragment
	 */
	public List<MApplicationElement> processModelFragment(MModelFragment fragment, String contributorURI,
			boolean checkExist) {

		E4XMIResource applicationResource = (E4XMIResource) ((EObject) application).eResource();

		List<MApplicationElement> elements = fragment.getElements();
		if (elements.isEmpty()) {
			return new ArrayList<>();
		}

		for (MApplicationElement el : elements) {
			EObject o = (EObject) el;

			E4XMIResource r = (E4XMIResource) o.eResource();

			if (checkExist && applicationResource.getIDToEObjectMap().containsKey(r.getID(o))) {
				continue;
			}

			applicationResource.setID(o, r.getID(o));

			if (contributorURI != null) {
				el.setContributorURI(contributorURI);
			}

			// Remember IDs of subitems
			TreeIterator<EObject> treeIt = EcoreUtil.getAllContents(o, true);
			while (treeIt.hasNext()) {
				EObject eObj = treeIt.next();
				r = (E4XMIResource) eObj.eResource();
				if (contributorURI != null && (eObj instanceof MApplicationElement)) {
					((MApplicationElement) eObj).setContributorURI(contributorURI);
				}
				applicationResource.setID(eObj, r.getInternalId(eObj));
			}
		}

		return fragment.merge(application);
	}

	/**
	 * Executes the processors as declared in provided {@link IExtension extensions}
	 * array.
	 *
	 * @param extensions     the array of {@link IExtension} extensions containing
	 *                       the processors
	 * @param initial        <code>true</code> if the application is running from a
	 *                       non-persisted state
	 * @param afterFragments <code>true</code> if the processors that should be run
	 *                       before model fragments are merged are to be executed,
	 *                       <code>false</code> otherwise
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

		this.processorContributions.stream().filter(sr -> {
			Dictionary<String, Object> dict = sr.getProperties();

			Object before = dict.get(IModelProcessorContribution.BEFORE_FRAGMENT_PROPERTY_KEY);
			boolean beforeFragments = true;
			if (before instanceof Boolean) {
				beforeFragments = (Boolean) before;
			} else if (before instanceof String) {
				beforeFragments = Boolean.parseBoolean((String) before);
			}

			Object applyObject = dict.get(IModelProcessorContribution.APPLY_PROPERTY_KEY);
			String apply = applyObject instanceof String ? (String) applyObject
					: IModelProcessorContribution.APPLY_ALWAYS;

			// check if the value for apply is valid
			if (!ALWAYS.equals(apply) && !INITIAL.equals(apply)) {
				log(LogLevel.WARN,
						"IModelProcessorContribution apply property value \"{0}\" is invalid, falling back to always", //$NON-NLS-1$
						apply);
				apply = IModelProcessorContribution.APPLY_ALWAYS;
			}

			return ((afterFragments != beforeFragments)
					&& (initial || IModelProcessorContribution.APPLY_ALWAYS.equals(apply)));
		}).map(sr -> bundleContext.getService(sr)).forEach(ModelAssembler.this::runProcessor);
	}

	private void runProcessor(IConfigurationElement ce) {
		IEclipseContext localContext = EclipseContextFactory.create();
		IContributionFactory factory = context.get(IContributionFactory.class);

		for (IConfigurationElement ceEl : ce.getChildren("element")) { //$NON-NLS-1$
			String id = ceEl.getAttribute("id"); //$NON-NLS-1$

			if (id == null) {
				log(LogLevel.WARN, "No element id given"); //$NON-NLS-1$
				continue;
			}

			String key = ceEl.getAttribute("contextKey"); //$NON-NLS-1$
			if (key == null) {
				key = id;
			}

			MApplicationElement el = ModelUtils.findElementById(application, id);
			if (el == null) {
				log(LogLevel.WARN, "Could not find element with id '{0}'", id); //$NON-NLS-1$
			}
			localContext.set(key, el);
		}

		try {
			Object o = factory.create("bundleclass://" + ce.getContributor().getName() + "/" + ce.getAttribute("class"), //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					context, localContext);
			if (o == null) {
				log(LogLevel.WARN, "Unable to create processor {0} from {1}", //$NON-NLS-1$
						ce.getAttribute("class"), //$NON-NLS-1$
						ce.getContributor().getName());
			} else {
				ContextInjectionFactory.invoke(o, Execute.class, context, localContext, null);
			}
		} catch (Exception e) {
			log(LogLevel.WARN, "Could not run processor: {0}", e); //$NON-NLS-1$
		}
	}

	private void runProcessor(IModelProcessorContribution processor) {
		IEclipseContext localContext = EclipseContextFactory.create();

		for (ModelElement element : processor.getModelElements()) {
			String id = element.getId();

			if (id == null) {
				log(LogLevel.WARN, "No element id given"); //$NON-NLS-1$
				continue;
			}

			String key = element.getContextKey();
			if (key == null) {
				key = id;
			}

			MApplicationElement el = ModelUtils.findElementById(application, id);
			if (el == null) {
				log(LogLevel.WARN, "Could not find element with id '{0}'", id); //$NON-NLS-1$
			}
			localContext.set(key, el);
		}

		try {
			Object o = null;
			if (processor.getProcessorClass() != null) {
				o = ContextInjectionFactory.make(processor.getProcessorClass(), localContext);
			} else {
				o = processor;
			}
			if (o == null) {
				log(LogLevel.WARN, "Unable to create processor {0} from {1}", //$NON-NLS-1$
						processor.getProcessorClass().getName(),
						FrameworkUtil.getBundle(processor.getProcessorClass()).getSymbolicName());
			} else {
				ContextInjectionFactory.invoke(o, Execute.class, context, localContext, null);
			}
		} catch (Exception e) {
			log(LogLevel.WARN, "Could not run processor: {0}", e); //$NON-NLS-1$
		}
	}

	/**
	 * Resolves the given list of imports used by the specified
	 * <code>addedElements</code> in the application model.
	 *
	 * @param imports       the list of elements that were imported by fragments and
	 *                      should be resolved in the application model
	 * @param addedElements the list of elements contributed by the fragments to the
	 *                      application model
	 */
	public void resolveImports(List<MApplicationElement> imports, List<MApplicationElement> addedElements) {
		if (imports.isEmpty()) {
			return;
		}
		// now that we have all components loaded, resolve imports
		Map<MApplicationElement, MApplicationElement> importMaps = new HashMap<>();
		for (MApplicationElement importedElement : imports) {
			MApplicationElement realElement = ModelUtils.findElementById(application, importedElement.getElementId());
			importMaps.put(importedElement, realElement);
		}

		TreeIterator<EObject> it = EcoreUtil.getAllContents(addedElements);
		List<Runnable> commands = new ArrayList<>();

		while (it.hasNext()) {
			EObject o = it.next();

			EContentsEList.FeatureIterator<EObject> featureIterator = (EContentsEList.FeatureIterator<EObject>) o
					.eCrossReferences().iterator();
			while (featureIterator.hasNext()) {
				EObject importObject = featureIterator.next();
				if (importObject.eContainmentFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					EStructuralFeature feature = featureIterator.feature();

					MApplicationElement el = null;
					if (importObject instanceof MApplicationElement) {
						el = importMaps.get((MApplicationElement) importObject);

						if (el == null) {
							log(LogLevel.WARN, "Could not resolve import for {0}", //$NON-NLS-1$
									((MApplicationElement) importObject).getElementId());
						}
					}

					final EObject interalTarget = o;
					final EStructuralFeature internalFeature = feature;
					final MApplicationElement internalElement = el;
					final EObject internalImportObject = importObject;

					commands.add(() -> {
						if (internalFeature.isMany()) {
							log(LogLevel.ERROR, MessageFormat.format(
									"Replacing in {0}.\n\nFeature={1}.\n\nInternalElement={2} contributed by {3}.\n\nImportObject={4}", //$NON-NLS-1$
									interalTarget, internalFeature.getName(), internalElement.getElementId(),
									internalElement.getContributorURI(), internalImportObject));
							@SuppressWarnings("unchecked")
							List<Object> l = (List<Object>) interalTarget.eGet(internalFeature);
							int index = l.indexOf(internalImportObject);
							if (index >= 0) {
								l.set(index, internalElement);
							}
						} else {
							interalTarget.eSet(internalFeature, internalElement);
						}
					});
				}
			}
		}

		for (Runnable cmd : commands) {
			cmd.run();
		}
	}

	void log(LogLevel level, String message, Object... args) {
		Logger log = this.logger;
		if (log != null) {
			switch (level) {
			case ERROR:
				log.error(message, args);
				break;
			case WARN:
				log.warn(message, args);
				break;
			case INFO:
				log.info(message, args);
				break;
			case DEBUG:
				log.debug(message, args);
				break;
			case AUDIT:
				log.audit(message, args);
				break;
			case TRACE:
				log.trace(message, args);
				break;
			}
		} else {
			// fallback if no LogService is available
			if (LogLevel.ERROR == level) {
				System.err.println(MessageFormat.format(message, args));
			} else {
				System.out.println(MessageFormat.format(message, args));
			}
		}
	}

	/**
	 *
	 * @param factory The {@link LoggerFactory} to retrieve the {@link Logger} from.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setLogger(LoggerFactory factory) {
		this.factory = factory;
		this.logger = factory.getLogger(getClass());
	}

	/**
	 *
	 * @param loggerFactory The {@link LoggerFactory} that was used to retrieve the
	 *                      {@link Logger}.
	 */
	public void unsetLogger(LoggerFactory loggerFactory) {
		if (this.factory == loggerFactory) {
			this.factory = null;
			this.logger = null;
		}
	}
}
