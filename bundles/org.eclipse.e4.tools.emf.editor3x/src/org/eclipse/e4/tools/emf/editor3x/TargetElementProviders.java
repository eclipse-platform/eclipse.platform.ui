package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.pde.internal.core.PDEExtensionRegistry;

public class TargetElementProviders implements IModelElementProvider {
	private static final String APP_E4XMI_DEFAULT = "Application.e4xmi"; //$NON-NLS-1$
	private ResourceSet resourceSet;

	@Override
	public void getModelElements(Filter filter, ModelResultHandler handler) {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			final PDEExtensionRegistry reg = new PDEExtensionRegistry();
			IExtension[] extensions = reg.findExtensions("org.eclipse.e4.workbench.model", true); //$NON-NLS-1$
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			for (final IExtension ext : extensions) {
				for (final IConfigurationElement el : ext.getConfigurationElements()) {
					if (el.getName().equals("fragment")) { //$NON-NLS-1$
						URI uri;
						// System.err.println("Model-Ext: Checking: " + ext.getContributor().getName());
						final IProject p = root.getProject(ext.getContributor().getName());
						if (p.exists() && p.isOpen()) {
							uri = URI.createPlatformResourceURI(
								ext.getContributor().getName() + "/" + el.getAttribute("uri"), true); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							uri = URI.createURI("platform:/plugin/" + ext.getContributor().getName() + "/" //$NON-NLS-1$ //$NON-NLS-2$
								+ el.getAttribute("uri")); //$NON-NLS-1$
						}
						// System.err.println(uri);
						try {
							resourceSet.getResource(uri, true);
						} catch (final Exception e) {
							e.printStackTrace();
							// System.err.println("=============> Failing");
						}

					}
				}
			}

			extensions = reg.findExtensions("org.eclipse.core.runtime.products", true); //$NON-NLS-1$
			for (final IExtension ext : extensions) {
				for (final IConfigurationElement el : ext.getConfigurationElements()) {
					if (el.getName().equals("product")) { //$NON-NLS-1$
						boolean xmiPropertyPresent = false;
						for (final IConfigurationElement prop : el.getChildren("property")) { //$NON-NLS-1$
							if (prop.getAttribute("name").equals("applicationXMI")) { //$NON-NLS-1$//$NON-NLS-2$
								final String v = prop.getAttribute("value"); //$NON-NLS-1$
								setUpResourceSet(root, v);
								xmiPropertyPresent = true;
								break;
							}
						}
						if (!xmiPropertyPresent) {
							setUpResourceSet(root, ext.getNamespaceIdentifier() + "/" + APP_E4XMI_DEFAULT); //$NON-NLS-1$
							break;
						}
					}
				}
			}
		}

		applyFilter(filter, handler);
	}

	private void setUpResourceSet(IWorkspaceRoot root, String v) {
		final String[] s = v.split("/"); //$NON-NLS-1$
		URI uri;
		// System.err.println("Product-Ext: Checking: " + v + " => P:" + s[0] + "");
		final IProject p = root.getProject(s[0]);
		if (p.exists() && p.isOpen()) {
			uri = URI.createPlatformResourceURI(v, true);
		} else {
			uri = URI.createURI("platform:/plugin/" + v); //$NON-NLS-1$
		}

		// System.err.println(uri);
		try {
			// prevent some unnecessary calls by checking the uri
			if (resourceSet.getURIConverter().exists(uri, null)) {
				resourceSet.getResource(uri, true);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			// System.err.println("=============> Failing");
		}
	}

	private void applyFilter(Filter filter, ModelResultHandler handler) {
		for (final Resource res : resourceSet.getResources()) {
			final TreeIterator<EObject> it = EcoreUtil.getAllContents(res,
				true);
			while (it.hasNext()) {
				final EObject o = it.next();
				if (o.eContainingFeature() != FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					if (o.eClass().equals(filter.eClass)) {
						// System.err.println("Found: " + o);
						handler.result(o);
					}
				}
			}
		}
	}

	@Override
	public void clearCache() {
		if (resourceSet == null) {
			return;
		}
		for (final Resource r : resourceSet.getResources()) {
			r.unload();
		}
		resourceSet = null;
	}

}
