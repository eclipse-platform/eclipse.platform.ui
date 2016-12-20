package org.eclipse.e4.tools.emf.editor3x;

import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

@SuppressWarnings("restriction")
public class TargetElementProviders implements IModelElementProvider {
	private ResourceSet resourceSet;

	@Override
	public void getModelElements(Filter filter, ModelResultHandler handler) {
		if (resourceSet == null) {
			resourceSet = Util.getModelElementResources();
		}

		applyFilter(filter, handler);
	}


	private void applyFilter(Filter filter, ModelResultHandler handler) {
		for (final Resource res : resourceSet.getResources()) {
			final TreeIterator<EObject> it = EcoreUtil.getAllContents(res,
					true);
			while (it.hasNext()) {
				final EObject o = it.next();
				if (o.eContainingFeature() != FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
					if ((filter.eClass == null) || o.eClass().equals(filter.eClass)) {
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
