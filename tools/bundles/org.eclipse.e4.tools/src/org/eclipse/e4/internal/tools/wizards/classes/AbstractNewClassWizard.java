/*******************************************************************************
 * Copyright (c) 2010, 2016 BestSolution.at and others.
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
 * Sopot Cela <sopotcela@gmail.com>
 * Patrik Suzzi <psuzzi@gmail.com> - Bug 421066
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IPackageImportDescription;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public abstract class AbstractNewClassWizard extends Wizard implements INewWizard {
	private static final String JAVA = ".java"; //$NON-NLS-1$
	protected IPackageFragmentRoot root;
	protected IFile file;
	private IStructuredSelection selection;
	private AbstractNewClassPage newClassPage;

	public AbstractNewClassWizard() {
		setWindowTitle(Messages.AbstractNewClassWizard_NewClass);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		root = getFragmentRoot(getInitialJavaElement(selection));
	}

	/**
	 * @return the selection
	 */
	public IStructuredSelection getSelection() {
		return selection;
	}

	/**
	 * {@inheritDoc}
	 * @see org.eclipse.jface.wizard.Wizard#addPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public void addPage(IWizardPage page) {
		super.addPage(page);
		if (page instanceof AbstractNewClassPage) {
			newClassPage = ((AbstractNewClassPage) page);
			newClassPage.init(getSelection());
		}
	}

	protected IJavaElement getInitialJavaElement(IStructuredSelection selection) {
		IJavaElement jelem = null;
		if (selection != null && !selection.isEmpty()) {
			final Object selectedElement = selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				final IAdaptable adaptable = (IAdaptable) selectedElement;

				jelem = adaptable.getAdapter(IJavaElement.class);
				if (jelem == null || !jelem.exists()) {
					jelem = null;
					IResource resource = adaptable.getAdapter(IResource.class);
					if (resource != null && resource.getType() != IResource.ROOT) {
						while (jelem == null && resource.getType() != IResource.PROJECT) {
							resource = resource.getParent();
							jelem = resource.getAdapter(IJavaElement.class);
						}
						if (jelem == null) {
							jelem = JavaCore.create(resource); // java project
						}
					}
				}
			}
		}

		return jelem;
	}

	protected IPackageFragmentRoot getFragmentRoot(IJavaElement elem) {
		IPackageFragmentRoot initRoot = null;
		if (elem != null) {
			initRoot = (IPackageFragmentRoot) elem.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
			try {
				if (initRoot == null || initRoot.getKind() != IPackageFragmentRoot.K_SOURCE) {
					final IJavaProject jproject = elem.getJavaProject();
					if (jproject != null) {
						initRoot = null;
						if (jproject.exists()) {
							final IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
							for (int i = 0; i < roots.length; i++) {
								if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
									initRoot = roots[i];
									break;
								}
							}
						}
						if (initRoot == null) {
							initRoot = jproject.getPackageFragmentRoot(jproject.getResource());
						}
					}
				}
			} catch (final JavaModelException e) {
				// TODO
				e.printStackTrace();
			}
		}
		return initRoot;
	}

	protected abstract String getContent();

	public JavaClass getDomainClass() {
		return ((AbstractNewClassPage) getPages()[0]).getClazz();
	}

	protected void checkRequiredBundles() {
		final IProject project = getDomainClass().getFragmentRoot().getJavaProject().getProject();
		final BundleContext context = FrameworkUtil.getBundle(NewAddonClassWizard.class).getBundleContext();
		final ServiceReference<IBundleProjectService> ref = context.getServiceReference(IBundleProjectService.class);
		final IBundleProjectService service = context.getService(ref);
		try {
			final IBundleProjectDescription description = service.getDescription(project);
			final Set<String> requiredBundles = getRequiredBundles();
			final IRequiredBundleDescription[] arTmp = description.getRequiredBundles();
			final IPackageImportDescription[] currentImportPacks = description.getPackageImports();
			final Set<String> requiredImportPacks = getImportedPackages();
			final List<IRequiredBundleDescription> descs = new ArrayList<>();
			final List<IPackageImportDescription> imDescs = new ArrayList<>();

			if (arTmp != null) {
				descs.addAll(Arrays.asList(arTmp));
			}
			for (final IRequiredBundleDescription bd : descs) {
				requiredBundles.remove(bd.getName());
			}

			if (requiredBundles.size() > 0) {
				for (final String b : requiredBundles) {
					descs.add(service.newRequiredBundle(b, null, false, false));
				}
				description.setRequiredBundles(descs.toArray(new IRequiredBundleDescription[0]));
				description.apply(new NullProgressMonitor());
			}

			if (currentImportPacks != null) {
				imDescs.addAll(Arrays.asList(currentImportPacks));
			}
			for (final IPackageImportDescription ds : imDescs) {
				requiredImportPacks.remove(ds.getName());
			}
			if (!requiredImportPacks.isEmpty()) {
				for (final String i : requiredImportPacks) {
					// jakarta.annotation;version="[2.1.0,3.0.0)";resolution:=optional
					final String[] parts = i.split(";"); //$NON-NLS-1$
					final VersionRange r = findVersionRange(parts);
					final boolean optional = isOptional(parts);

					imDescs.add(service.newPackageImport(parts[0], r, optional));
				}
				description.setPackageImports(imDescs.toArray(new IPackageImportDescription[0]));
				description.apply(new NullProgressMonitor());
			}
		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static VersionRange findVersionRange(String[] parts) {
		for (final String p : parts) {
			if (p.startsWith("version=")) { //$NON-NLS-1$
				final String version = p.substring("version=".length() + 1, p.length() - 1); //$NON-NLS-1$
				return new VersionRange(version.trim());
			}
		}
		return null;
	}

	private static boolean isOptional(String[] parts) {
		for (final String p : parts) {
			if (p.startsWith("resolution:=")) { //$NON-NLS-1$
				return p.contains("optional"); //$NON-NLS-1$
			}
		}
		return false;
	}

	// public static void main(String[] args) {
	// findVersionRange("jakarta.annotation;version=\"[2.1.0,3.0.0)\";resolution:=optional".split(";"));
	// }

	protected Set<String> getRequiredBundles() {
		final Set<String> rv = new HashSet<>();
		//		rv.add("jakarta.inject"); //$NON-NLS-1$
		return rv;
	}

	protected Set<String> getImportedPackages() {
		final Set<String> rv = new HashSet<>();
		rv.add("jakarta.inject;version=\"[2.0.0,3.0.0)\""); //$NON-NLS-1$
		return rv;
	}

	@Override
	public boolean performFinish() {
		final JavaClass clazz = getDomainClass();
		final String content = getContent();

		if (clazz.getFragmentRoot() == null) {
			return false;
		}

		checkRequiredBundles();

		IPackageFragment fragment = clazz.getPackageFragment();
		boolean exists = false;
		if (fragment != null) {
			IJavaElement[] children;
			try {
				children = clazz.getFragmentRoot().getChildren();

				for (final IJavaElement iJavaElement : children) {
					final IPackageFragment pf = (IPackageFragment) iJavaElement;
					if (pf.getElementName().equals(fragment.getElementName())) {
						exists = true;
						break;
					}
				}

				if (!exists) {
					fragment = clazz.getFragmentRoot().createPackageFragment(fragment.getElementName(), true, null);
				}
			} catch (final JavaModelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (newClassPage != null) {
				newClassPage.performFinish();
			}
			final String cuName = clazz.getName() + JAVA;
			final ICompilationUnit unit = fragment.getCompilationUnit(cuName);
			final IResource resource = unit.getResource();
			file = (IFile) resource;
			try {
				if (!file.exists()) {
					file.create(new ByteArrayInputStream(content.getBytes()), true, null);
				} else {
					file.setContents(new ByteArrayInputStream(content.getBytes()),
							IResource.FORCE | IResource.KEEP_HISTORY, null);
				}
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
				// unit.open(null);
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			final String cuName = clazz.getName() + JAVA;
			final IFolder p = (IFolder) clazz.getFragmentRoot().getResource();
			file = p.getFile(cuName);
			try {
				if (!file.exists()) {
					file.create(new ByteArrayInputStream(content.getBytes()), true, null);
				} else {
					file.setContents(new ByteArrayInputStream(content.getBytes()),
							IResource.FORCE | IResource.KEEP_HISTORY, null);
				}
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;

	}

	public IFile getFile() {
		return file;
	}
}
