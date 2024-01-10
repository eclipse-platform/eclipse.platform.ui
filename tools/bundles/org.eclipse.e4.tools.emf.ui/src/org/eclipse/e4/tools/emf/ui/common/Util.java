/*******************************************************************************
 * Copyright (c) 2010, 2019 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Olivier Prouvost <olivier.prouvost@opcoach.com> - added some cache for e4xmi resource management
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.tools.emf.ui.common.IEditorFeature.FeatureClass;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.fragment.impl.FragmentPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

public class Util {

	private static final String APP_E4XMI_DEFAULT = "Application.e4xmi"; //$NON-NLS-1$

	public static final boolean isNullOrEmpty(String element) {
		return element == null || element.trim().length() == 0;
	}

	public static final boolean isImport(EObject object) {
		return object.eContainingFeature() == FragmentPackageImpl.Literals.MODEL_FRAGMENTS__IMPORTS;
	}

	public static final void addClasses(EPackage ePackage, List<FeatureClass> list) {
		for (final EClassifier c : ePackage.getEClassifiers()) {
			if (c instanceof EClass) {
				final EClass eclass = (EClass) c;
				if (eclass != ApplicationPackageImpl.Literals.APPLICATION && !eclass.isAbstract()
						&& !eclass.isInterface()
						&& eclass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
					list.add(new FeatureClass(eclass.getName(), eclass));
				}
			}
		}

		for (final EPackage eSubPackage : ePackage.getESubpackages()) {
			addClasses(eSubPackage, list);
		}
	}

	// TODO In future support different name formats something like
	// ${project}.${classname}.${counter}
	public static final String getDefaultElementId(Resource resource, MApplicationElement element, IProject project) {
		try {
			final EObject o = (EObject) element;
			final String className = o.eClass().getName();
			final String projectName = project.getName();

			final String prefix = (projectName + "." + className).toLowerCase(); //$NON-NLS-1$

			final TreeIterator<EObject> it = resource.getAllContents();
			final SortedSet<Integer> numbers = new TreeSet<>();

			while (it.hasNext()) {
				final EObject tmp = it.next();
				if (tmp instanceof MApplicationElement) {
					final String elementId = ((MApplicationElement) tmp).getElementId();
					if (elementId != null && elementId.length() > prefix.length() && elementId.startsWith(prefix)) {
						final String suffix = elementId.substring(prefix.length());
						if (suffix.startsWith(".") && suffix.length() > 1) { //$NON-NLS-1$
							try {
								numbers.add(Integer.parseInt(suffix.substring(1)));
							} catch (final Exception e) {
								// TODO: handle exception
							}
						}
					}
				}
			}

			int lastNumber = -1;
			for (final Integer number : numbers) {
				if (lastNumber + 1 != number) {
					break;
				}
				lastNumber = number;
			}

			return (prefix + "." + ++lastNumber).toLowerCase(); //$NON-NLS-1$
		} catch (final Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	public static List<InternalPackage> loadPackages() {
		final List<InternalPackage> packs = new ArrayList<>();

		for (final Entry<String, Object> regEntry : EPackage.Registry.INSTANCE.entrySet()) {
			if (regEntry.getValue() instanceof EPackage) {
				final EPackage ePackage = (EPackage) regEntry.getValue();
				final InternalPackage iePackage = new InternalPackage(ePackage);
				boolean found = false;
				for (final EClassifier cl : ePackage.getEClassifiers()) {
					if (cl instanceof EClass) {
						final EClass eClass = (EClass) cl;
						if (eClass.getEAllSuperTypes().contains(ApplicationPackageImpl.Literals.APPLICATION_ELEMENT)) {
							if (!eClass.isInterface() && !eClass.isAbstract()) {
								found = true;
								final InternalClass ieClass = new InternalClass(iePackage, eClass);
								iePackage.classes.add(ieClass);
								for (final EReference f : eClass.getEAllReferences()) {
									ieClass.features.add(new InternalFeature(ieClass, f));
								}
							}
						}
					}
				}
				if (found) {
					packs.add(iePackage);
				}
			}
		}

		return packs;
	}

	public static boolean moveElementByIndex(EditingDomain editingDomain, MUIElement element, boolean liveModel,
			int index, EStructuralFeature feature) {
		if (liveModel) {
			final EObject container = ((EObject) element).eContainer();
			@SuppressWarnings("unchecked")
			final List<Object> l = (List<Object>) container.eGet(feature);
			l.remove(element);

			if (index >= 0) {
				l.add(index, element);
			} else {
				l.add(element);
			}

			return true;
		}
		final EObject container = ((EObject) element).eContainer();
		final Command cmd = MoveCommand.create(editingDomain, container, feature, element, index);

		if (cmd.canExecute()) {
			editingDomain.getCommandStack().execute(cmd);
			return true;
		}
		return false;
	}

	public static boolean moveElementByIndex(EditingDomain editingDomain, MUIElement element, boolean liveModel,
			int index) {
		if (liveModel) {
			final MElementContainer<MUIElement> container = element.getParent();
			container.getChildren().remove(element);

			if (index >= 0) {
				container.getChildren().add(index, element);
			} else {
				container.getChildren().add(element);
			}

			container.setSelectedElement(element);
			return true;
		}
		final MElementContainer<MUIElement> container = element.getParent();
		final Command cmd = MoveCommand.create(editingDomain, container,
				UiPackageImpl.Literals.ELEMENT_CONTAINER__CHILDREN, element, index);

		if (cmd.canExecute()) {
			editingDomain.getCommandStack().execute(cmd);
			return true;
		}
		return false;
	}

	/**
	 * The set of resources containing model element. Updated when one changes
	 * in the workspace
	 */
	private static ResourceSet modelResourceSet;

	// The lis
	private static boolean e4ModelResourceListenerRegistered = false;

	/**
	 * This method searches for fragments or application model elements
	 * resources. It is updated when the workspace changes.. else it returns the
	 * cached values.
	 */
	@SuppressWarnings("restriction") // uses org.eclipse.pde.internal.core.PDEExtensionRegistry
	public static ResourceSet getModelElementResources() {

		// Return previous computed result while workspace did not change...
		if (modelResourceSet != null) {
			return modelResourceSet;
		}

		registerE4XmiListener(); // Done only once.

		modelResourceSet = new ResourceSetImpl();
		final org.eclipse.pde.internal.core.PDEExtensionRegistry reg = new org.eclipse.pde.internal.core.PDEExtensionRegistry();
		IExtension[] extensions = reg.findExtensions("org.eclipse.e4.workbench.model", true); //$NON-NLS-1$
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		for (final IExtension ext : extensions) {
			for (final IConfigurationElement el : ext.getConfigurationElements()) {
				if (el.getName().equals("fragment")) { //$NON-NLS-1$
					URI uri;
					// System.err.println("Model-Ext: Checking: " +
					// ext.getContributor().getName());
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
						modelResourceSet.getResource(uri, true);
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
							setUpResourceSet(modelResourceSet, root, v);
							xmiPropertyPresent = true;
							break;
						}
					}
					if (!xmiPropertyPresent) {
						setUpResourceSet(modelResourceSet, root,
								ext.getNamespaceIdentifier() + "/" + APP_E4XMI_DEFAULT); //$NON-NLS-1$
						break;
					}
				}
			}
		}
		return modelResourceSet;
	}

	/**
	 * A listener to reset the cache of e4Xmi resource for the index research
	 */
	private static void registerE4XmiListener() {
		// Register once on the workspace.
		// the listener could be optimized to remember of changed resource since
		// the last call to getModelElementResources...
		if (!e4ModelResourceListenerRegistered) {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
				@Override
				public void resourceChanged(IResourceChangeEvent event) {
					IResourceDelta delta = event.getDelta();
					// Nothing to do if resource set not yet used or no resource change recorded!
					if (modelResourceSet == null || delta == null) {
						return;
					}
					checkDeltaContainsE4xmi(delta);
				}

				private void checkDeltaContainsE4xmi(IResourceDelta delta) {
					if (modelResourceSet == null) {
						return;
					}

					for (IResourceDelta rd : delta.getAffectedChildren()) {
						IResource r = rd.getResource();
						if (r instanceof IFile)
						{
							if ("e4xmi".equals(((IFile) r).getFileExtension())) { //$NON-NLS-1$
								modelResourceSet = null;
								break;
							}
						} else {
							checkDeltaContainsE4xmi(rd);
						}
					}

				}
			});
			e4ModelResourceListenerRegistered = true;
		}
	}


	private static void setUpResourceSet(ResourceSet resourceSet, IWorkspaceRoot root, String v) {
		final String[] s = v.split("/"); //$NON-NLS-1$
		URI uri;
		// System.err.println("Product-Ext: Checking: " + v + " => P:" + s[0] +
		// "");
		final IProject p = root.getProject(s[0]);
		if (p.exists() && p.isOpen()) {
			uri = URI.createPlatformResourceURI(v, true);
		} else {
			uri = URI.createURI("platform:/plugin/" + v); //$NON-NLS-1$
		}

		try {
			// prevent some unnecessary calls by checking the uri
			if (resourceSet.getURIConverter().exists(uri, null)) {
				resourceSet.getResource(uri, true);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Scales an {@link Image} to the the given size preserving the original aspect
	 * ratio. If a new Image is created, the given Image is disposed.
	 *
	 * @param img     Original Image
	 * @param maxSize Maximum size of the resulting image (maximum width if the
	 *                original image has landscape format and vice versa)
	 * @return Scaled Image or the original image if it is already smaller than
	 *         maxSize x maxSize
	 */
	public static Image scaleImage(Image img, int maxSize) {
		Image result = img;

		double scale1 = (double) maxSize / img.getImageData().height;
		final double scale2 = (double) maxSize / img.getImageData().width;
		if (scale2 < scale1) {
			scale1 = scale2;
		}
		if (scale1 < 1) {
			int width = (int) (img.getImageData().width * scale1);
			if (width == 0) {
				width = 1;
			}
			int height = (int) (img.getImageData().height * scale1);
			if (height == 0) {
				height = 1;
			}
			Image img2 = new Image(img.getDevice(), img.getImageData().scaledTo(width, height));
			img.dispose();
			result = img2;
		}
		return result;
	}

	/**
	 * This method checks if an EClass can be extended using a fragment. ie : it
	 * must have containment EReference to a model object.
	 *
	 * @return true if at least one reference type is not a StringStringToMap or
	 *         other no editable type
	 */
	public static boolean canBeExtendedInAFragment(EClass c) {
		boolean result = false;
		for (EReference r : c.getEAllReferences()) {
			if (referenceIsModelFragmentCompliant(r)) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * This method checks if an EReference can be considered in a model fragment ie
	 * : it must be containment EReference to a model object.
	 *
	 * @return true if the reference is containment and type is not a
	 *         StringStringToMap or other no editable type
	 */
	public static boolean referenceIsModelFragmentCompliant(EReference r) {
		String t = r.getEReferenceType().getName();
		return (r.isContainment() && !t.equals("StringToStringMap") && !t.equals("StringToObjectMap")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	public static final void addDecoration(Control control, Binding binding) {
		final ControlDecoration dec = new ControlDecoration(control, SWT.BOTTOM);
		binding.getValidationStatus().addValueChangeListener(event -> {
			final IStatus s = event.getObservableValue().getValue();
			if (s.isOK()) {
				dec.setDescriptionText(null);
				dec.setImage(null);
			} else {
				dec.setDescriptionText(s.getMessage());

				String fieldDecorationID = null;
				switch (s.getSeverity()) {
				case IStatus.INFO:
					fieldDecorationID = FieldDecorationRegistry.DEC_INFORMATION;
					break;
				case IStatus.WARNING:
					fieldDecorationID = FieldDecorationRegistry.DEC_WARNING;
					break;
				case IStatus.ERROR:
				case IStatus.CANCEL:
					fieldDecorationID = FieldDecorationRegistry.DEC_ERROR;
					break;
				}
				final FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
						.getFieldDecoration(fieldDecorationID);
				dec.setImage(fieldDecoration == null ? null : fieldDecoration.getImage());
			}
		});
	}

	public static class InternalPackage {
		public final EPackage ePackage;
		public List<InternalClass> classes = new ArrayList<>();

		public InternalPackage(EPackage ePackage) {
			this.ePackage = ePackage;
		}

		@Override
		public String toString() {
			return ePackage.toString();
		}

		public List<EClass> getAllClasses() {
			final ArrayList<EClass> rv = new ArrayList<>(classes.size());
			for (final InternalClass c : classes) {
				rv.add(c.eClass);
			}
			return rv;
		}
	}

	public static class InternalClass {
		public final InternalPackage pack;
		public final EClass eClass;
		public List<InternalFeature> features = new ArrayList<>();

		public InternalClass(InternalPackage pack, EClass eClass) {
			this.eClass = eClass;
			this.pack = pack;
		}
	}

	public static class InternalFeature {
		public final InternalClass clazz;
		public final EStructuralFeature feature;

		public InternalFeature(InternalClass clazz, EStructuralFeature feature) {
			this.clazz = clazz;
			this.feature = feature;
		}

	}
}
