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
	private static final String APP_E4XMI_DEFAULT = "Application.e4xmi";
	private ResourceSet resourceSet;
	
	public void getModelElements(Filter filter, ModelResultHandler handler) {
		if( resourceSet == null ) {
			resourceSet = new ResourceSetImpl();
			PDEExtensionRegistry reg = new PDEExtensionRegistry();
			IExtension[] extensions = reg.findExtensions("org.eclipse.e4.workbench.model", true);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			for( IExtension ext : extensions ) {
				for( IConfigurationElement el : ext.getConfigurationElements() ) {
					if( el.getName().equals("fragment") ) {
						URI uri;
//						System.err.println("Model-Ext: Checking: " + ext.getContributor().getName());
						IProject p = root.getProject(ext.getContributor().getName());
						if( p.exists() && p.isOpen() ) {
							uri = URI.createPlatformResourceURI(ext.getContributor().getName() + "/" + el.getAttribute("uri"), true);	
						} else {
							uri = URI.createURI("platform:/plugin/" + ext.getContributor().getName() + "/" + el.getAttribute("uri") );							
						}
//						System.err.println(uri);
						try {
							resourceSet.getResource(uri, true);							
						} catch (Exception e) {
							e.printStackTrace();
//							System.err.println("=============> Failing");
						}

					}
				}
			}
			
			extensions = reg.findExtensions("org.eclipse.core.runtime.products", true);
			for( IExtension ext : extensions ) {
				for( IConfigurationElement el : ext.getConfigurationElements() ) {
					if( el.getName().equals("product") ) {
						boolean xmiPropertyPresent = false;
						for( IConfigurationElement prop: el.getChildren("property") ) {
							if( prop.getAttribute("name").equals("applicationXMI") ) {
								String v = prop.getAttribute("value");
								setUpResourceSet(root, v);
								xmiPropertyPresent = true;
								break;
							}
						}
						if (!xmiPropertyPresent){
							setUpResourceSet(root, ext.getNamespaceIdentifier()+"/"+APP_E4XMI_DEFAULT);
							break;
						}
					}
				}
			}			
		}
		
		applyFilter(filter, handler);
	}

	private void setUpResourceSet(IWorkspaceRoot root, String v) {
		String[] s = v.split("/");
		URI uri;
//								System.err.println("Product-Ext: Checking: " + v + " => P:" +  s[0] + "");
		IProject p = root.getProject(s[0]);
		if( p.exists() && p.isOpen() ) {
			uri = URI.createPlatformResourceURI(v, true );	
		} else {
			uri = URI.createURI("platform:/plugin/" + v );
		}
		
//		System.err.println(uri);
		try {
			//prevent some unnecessary calls by checking the uri 
			if (resourceSet.getURIConverter().exists(uri, null)
					)
			resourceSet.getResource(uri, true);							
		} catch (Exception e) {
			e.printStackTrace();
//									System.err.println("=============> Failing");
		}
	}
	
	private void applyFilter(Filter filter, ModelResultHandler handler) {
		for (Resource res : resourceSet.getResources()) {
				TreeIterator<EObject> it = EcoreUtil.getAllContents(res,
						true);
				while (it.hasNext()) {
					EObject o = it.next();
					if (o.eContainingFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS) {
//						System.err
//								.println("Skipped because it is an import");
					} else {
						if (o.eClass().equals(filter.eClass)) {
//							System.err.println("Found: " + o);
							handler.result(o);
						}
					}
				}
		}
	}

	public void clearCache() {
		if (resourceSet==null) return;
		for (Resource r : resourceSet.getResources()) {
			r.unload();
		}
		resourceSet = null;
	}
	
}
