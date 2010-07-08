package org.eclipse.e4.tools.emf.editor3x;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;

//FIXME In the next release we could probably use EMF-Query
public class ModelElementProvider implements IModelElementProvider {
	private ResourceSet resourceSet;

	public void getModelElements(Filter filter, ModelResultHandler handler) {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			TextSearchEngine engine = TextSearchEngine.createDefault();
			TextSearchScope scope = TextSearchScope
					.newSearchScope(new IResource[] { ResourcesPlugin
							.getWorkspace().getRoot() }, Pattern
							.compile(".+\\.e4xmi"), true);
			engine.search(scope, new RequestorImpl(filter, handler),
					Pattern.compile(".+\\.e4xmi"), null);
		} else {
			applyFilter(filter, handler);
		}
	}
	
	private void applyFilter(Filter filter, ModelResultHandler handler) {
		for (Resource res : resourceSet.getResources()) {
			if (res.getURI().equals(filter.object.eResource().getURI())) {
//				System.err.println("Skipped because self");
			} else {
				TreeIterator<EObject> it = EcoreUtil.getAllContents(res,
						true);
				while (it.hasNext()) {
					EObject o = it.next();
					if (o.eContainingFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
//						System.err
//								.println("Skipped because it is an import");
					} else {
						if (o.eClass().equals(filter.object.eClass())) {

							if (o instanceof MApplicationElement) {
								String elementId = ((MApplicationElement) o).getElementId();
								if( elementId != null && elementId.trim().length() > 0 ) {
									if( filter.elementIdPattern.matcher(elementId).matches() ) {
										handler.result(o);
									}										
								}
							}
						}
					}
				}
			}
		}
	}

	class RequestorImpl extends TextSearchRequestor {
		private final Filter filter;
		private final ModelResultHandler handler;

		public RequestorImpl(Filter filter, ModelResultHandler handler) {
			this.filter = filter;
			this.handler = handler;
		}

		@Override
		public boolean acceptFile(IFile file) throws CoreException {
			try {
				if( file.getProject().isOpen() ) {
					resourceSet.getResource(URI.createPlatformResourceURI("/"
							+ file.getFullPath().makeRelative().toString(), true),
							true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return super.acceptFile(file);
		}

		@Override
		public void endReporting() {
			applyFilter(filter, handler);
			super.endReporting();
		}
	}

	public void clearCache() {
		for (Resource r : resourceSet.getResources()) {
			r.unload();
		}
		resourceSet = null;
	}
}