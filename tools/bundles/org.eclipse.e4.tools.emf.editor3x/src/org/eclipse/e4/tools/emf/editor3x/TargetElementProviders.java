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

	@Override
	public void getModelElements(Filter filter, ModelResultHandler handler) {

		ResourceSet resourceSet = Util.getModelElementResources();

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
		// Should now do nothing, as Util.getModelElementResources is now
		// listening
		// to e4xmi changes
		/*
		 * if (resourceSet == null) { return; } for (final Resource r :
		 * resourceSet.getResources()) { r.unload(); } resourceSet = null;
		 */
	}

}
