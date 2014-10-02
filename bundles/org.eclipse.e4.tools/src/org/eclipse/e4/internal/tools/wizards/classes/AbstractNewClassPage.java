/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Sopot Cela <sopotcela@gmail.com>
 ******************************************************************************/
package org.eclipse.e4.internal.tools.wizards.classes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.internal.tools.Messages;
import org.eclipse.e4.internal.tools.ToolsPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.databinding.swt.IWidgetValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

@SuppressWarnings("restriction")
public abstract class AbstractNewClassPage extends WizardPage {
	private static final String BUNDLECLASS = "bundleclass://"; //$NON-NLS-1$
	private static final String FRAGMENT_ROOT = "fragmentRoot"; //$NON-NLS-1$
	public static final String PACKAGE_FRAGMENT = "packageFragment"; //$NON-NLS-1$
	public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

	public static class JavaClass {

		protected PropertyChangeSupport support = new PropertyChangeSupport(this);

		private IPackageFragmentRoot fragmentRoot;
		private IPackageFragment packageFragment;
		private String name;

		public JavaClass(IPackageFragmentRoot fragmentRoot) {
			this.fragmentRoot = fragmentRoot;
		}

		public IPackageFragmentRoot getFragmentRoot() {
			return fragmentRoot;
		}

		public void setFragmentRoot(IPackageFragmentRoot fragmentRoot) {
			support.firePropertyChange(FRAGMENT_ROOT, this.fragmentRoot, this.fragmentRoot = fragmentRoot);
		}

		public IPackageFragment getPackageFragment() {
			return packageFragment;
		}

		public void setPackageFragment(IPackageFragment packageFragment) {
			support.firePropertyChange(PACKAGE_FRAGMENT, this.packageFragment, this.packageFragment = packageFragment);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			support.firePropertyChange(PROPERTY_NAME, this.name, this.name = name);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			support.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			support.removePropertyChangeListener(listener);
		}

		public static boolean exists(IPackageFragmentRoot pFragmentRoot, IPackageFragment pPackageFragment, String pName) {

			if (pFragmentRoot == null) {
				return false;
			}

			final String cuName = pName + ".java"; //$NON-NLS-1$
			IFile file;

			if (pPackageFragment != null) {
				final ICompilationUnit unit = pPackageFragment.getCompilationUnit(cuName);
				final IResource resource = unit.getResource();
				file = (IFile) resource;
			} else {
				final IFolder p = (IFolder) pFragmentRoot.getResource();
				file = p.getFile(cuName);
			}
			return file.exists();
		}
	}

	private JavaClass clazz;
	private IPackageFragmentRoot froot;
	private final IWorkspaceRoot fWorkspaceRoot;
	private String initialString;

	protected AbstractNewClassPage(String pageName, String title, String description, IPackageFragmentRoot froot,
		IWorkspaceRoot fWorkspaceRoot) {
		super(pageName);
		this.froot = froot;
		this.fWorkspaceRoot = fWorkspaceRoot;

		setTitle(title);
		setDescription(description);
	}

	protected AbstractNewClassPage(String pageName, String title, String description, IPackageFragmentRoot froot,
		IWorkspaceRoot fWorkspaceRoot, String initialString) {
		this(pageName, title, description, froot, fWorkspaceRoot);
		this.initialString = initialString;
	}

	@Override
	public void createControl(Composite parent) {
		final Image img = new Image(parent.getDisplay(), getClass().getClassLoader().getResourceAsStream(
			"/icons/full/wizban/newclass_wiz.png")); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage(img));

		parent.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				img.dispose();
				setImageDescriptor(null);
			}
		});

		parent = new Composite(parent, SWT.NULL);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		parent.setLayout(new GridLayout(3, false));

		clazz = createInstance();
		if (froot != null && initialString != null) {
			clazz.setPackageFragment(froot.getPackageFragment(parseInitialStringForPackage(initialString) == null ? "" //$NON-NLS-1$
				: parseInitialStringForPackage(initialString)));
			clazz.setName(parseInitialStringForClassName(initialString));
		}
		final DataBindingContext dbc = new DataBindingContext();
		WizardPageSupport.create(this, dbc);

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.AbstractNewClassPage_SourceFolder);

			final Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			t.setEditable(false);

			final Binding bd = dbc.bindValue(
				WidgetProperties.text().observe(t),
				BeanProperties.value(FRAGMENT_ROOT).observe(clazz),
				new UpdateValueStrategy().setBeforeSetValidator(new PFRootValidator()),
				new UpdateValueStrategy().setConverter(new PackageFragmentRootToStringConverter())
				);

			final Button b = new Button(parent, SWT.PUSH);
			b.setText(Messages.AbstractNewClassPage_Browse);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final IPackageFragmentRoot root = choosePackageRoot();
					if (root != null) {
						froot = root;
						clazz.setFragmentRoot(root);
					}
					bd.updateModelToTarget();
				}
			});
		}

		{
			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.AbstractNewClassPage_Package);
			final Text t = new Text(parent, SWT.BORDER);
			t.setEditable(true);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final Binding bd = dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(t),
				BeanProperties.value(PACKAGE_FRAGMENT).observe(clazz),
				new UpdateValueStrategy().setConverter(new StringToPackageFragmentConverter(clazz)),
				new UpdateValueStrategy().setConverter(new PackageFragmentToStringConverter())
				);

			final Button b = new Button(parent, SWT.PUSH);
			b.setText(Messages.AbstractNewClassPage_Browse);
			b.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final IPackageFragment fragment = choosePackage();
					if (fragment != null) {
						clazz.setPackageFragment(fragment);
					}
					bd.updateModelToTarget(); // TODO Find out why this is needed
				}
			});
		}

		{
			final IWidgetValueProperty textProp = WidgetProperties.text(SWT.Modify);

			final Label l = new Label(parent, SWT.NONE);
			l.setText(Messages.AbstractNewClassPage_Name);

			final Text t = new Text(parent, SWT.BORDER);
			t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			dbc.bindValue(textProp.observe(t), BeanProperties.value(PROPERTY_NAME, String.class).observe(clazz),
				new UpdateValueStrategy().setBeforeSetValidator(new ClassnameValidator(clazz)), null);

			new Label(parent, SWT.NONE);
		}

		{
			final Label l = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			l.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 3, 1));
		}

		createFields(parent, dbc);
		setControl(parent);
	}

	private String parseInitialStringForPackage(String initialString2) {
		if (initialString2 == null) {
			return null;
		}
		final int ioBC = initialString2.indexOf(BUNDLECLASS);
		final int iSecondSlash = initialString2.lastIndexOf('/');
		if (

			initialString2.length() == 0 || // empty
			ioBC == -1 || // no bundle class
			iSecondSlash == -1 || // no package &| class name
			initialString2.indexOf('.') == -1// no package
			) {
			return null;
		}

		final int lastDot = initialString2.lastIndexOf('.');
		final String packageString = initialString2.substring(iSecondSlash + 1, lastDot);
		return packageString;
	}

	private String parseInitialStringForClassName(String initialString) {
		if (initialString == null) {
			return null;
		}
		final int ioBC = initialString.indexOf(BUNDLECLASS);
		final int iSecondSlash = initialString.lastIndexOf('/');
		if (

			initialString.length() == 0 || // empty
			ioBC == -1 || // no bundle class
			iSecondSlash == -1 || // no package &| class name
			initialString.indexOf('.') == -1// no package
			) {
			return null;
		}
		final int lastDot = initialString.lastIndexOf('.');
		if (lastDot != -1) {
			return initialString.substring(lastDot + 1);
		}
		return null;
	}

	private IPackageFragmentRoot choosePackageRoot() {
		final IJavaElement initElement = clazz.getFragmentRoot();
		Class[] acceptedClasses = new Class[] { IPackageFragmentRoot.class, IJavaProject.class };
		final TypedElementSelectionValidator validator = new TypedElementSelectionValidator(acceptedClasses, false) {
			@Override
			public boolean isSelectedValid(Object element) {
				try {
					if (element instanceof IJavaProject) {
						final IJavaProject jproject = (IJavaProject) element;
						final IPath path = jproject.getProject().getFullPath();
						return jproject.findPackageFragmentRoot(path) != null;
					} else if (element instanceof IPackageFragmentRoot) {
						return ((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE;
					}
					return true;
				} catch (final JavaModelException e) {
					JavaPlugin.log(e.getStatus()); // just log, no UI in validation
				}
				return false;
			}
		};

		acceptedClasses = new Class[] { IJavaModel.class, IPackageFragmentRoot.class, IJavaProject.class };
		final ViewerFilter filter = new TypedViewerFilter(acceptedClasses) {
			@Override
			public boolean select(Viewer viewer, Object parent, Object element) {
				if (element instanceof IPackageFragmentRoot) {
					try {
						return ((IPackageFragmentRoot) element).getKind() == IPackageFragmentRoot.K_SOURCE;
					} catch (final JavaModelException e) {
						JavaPlugin.log(e.getStatus()); // just log, no UI in validation
						return false;
					}
				}
				return super.select(viewer, parent, element);
			}
		};

		final StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
		final ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, provider);
		dialog.setValidator(validator);
		dialog.setComparator(new JavaElementComparator());
		dialog.setTitle(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_title);
		dialog.setMessage(NewWizardMessages.NewContainerWizardPage_ChooseSourceContainerDialog_description);
		dialog.addFilter(filter);
		dialog.setInput(JavaCore.create(fWorkspaceRoot));
		dialog.setInitialSelection(initElement);
		dialog.setHelpAvailable(false);

		if (dialog.open() == Window.OK) {
			final Object element = dialog.getFirstResult();
			if (element instanceof IJavaProject) {
				final IJavaProject jproject = (IJavaProject) element;
				return jproject.getPackageFragmentRoot(jproject.getProject());
			} else if (element instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot) element;
			}
			return null;
		}
		return null;
	}

	private IPackageFragment choosePackage() {
		IJavaElement[] packages = null;
		try {
			if (froot != null && froot.exists()) {
				packages = froot.getChildren();
			}
		} catch (final JavaModelException e) {
			e.printStackTrace();
		}
		if (packages == null) {
			packages = new IJavaElement[0];
		}

		final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
			new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
		dialog.setIgnoreCase(false);
		dialog.setTitle(Messages.AbstractNewClassPage_ChoosePackage);
		dialog.setMessage(Messages.AbstractNewClassPage_ChooseAPackage);
		dialog.setEmptyListMessage(Messages.AbstractNewClassPage_NeedToSelectAPackage);
		dialog.setElements(packages);
		dialog.setHelpAvailable(false);
		final IPackageFragment pack = clazz.getPackageFragment();
		if (pack != null) {
			dialog.setInitialSelections(new Object[] { pack });
		}

		if (dialog.open() == Window.OK) {
			return (IPackageFragment) dialog.getFirstResult();
		}
		return null;
	}

	protected abstract void createFields(Composite parent, DataBindingContext dbc);

	protected abstract JavaClass createInstance();

	public JavaClass getClazz() {
		return clazz;
	}

	static class ClassnameValidator implements IValidator {

		private final JavaClass clazz;

		public ClassnameValidator(JavaClass clazz) {
			this.clazz = clazz;
		}

		@Override
		public IStatus validate(Object value) {
			final String name = value.toString();
			if (name.length() == 0) {
				return new Status(IStatus.ERROR, ToolsPlugin.PLUGIN_ID, Messages.AbstractNewClassPage_NameNotEmpty);
			}
			if (name.indexOf('.') != -1 || name.trim().indexOf(' ') != -1) {
				return new Status(IStatus.ERROR, ToolsPlugin.PLUGIN_ID,
					Messages.AbstractNewClassPage_NameMustBeQualified);
			}
			if (JavaClass.exists(clazz.getFragmentRoot(), clazz.getPackageFragment(), name)) {
				return new Status(IStatus.ERROR, ToolsPlugin.PLUGIN_ID, Messages.AbstractNewClassPage_ClassExists);
			}

			return JavaConventions.validateJavaTypeName(name, JavaCore.VERSION_1_3, JavaCore.VERSION_1_3);
		}
	}

	static class PFRootValidator implements IValidator {

		@Override
		public IStatus validate(Object value) {
			final String name = value.toString();
			if (name.length() == 0) {
				return new Status(IStatus.ERROR, ToolsPlugin.PLUGIN_ID,
					Messages.AbstractNewClassPage_SourceFolderNotEmpty);
			}

			return new Status(IStatus.OK, ToolsPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
		}
	}

	static class PackageFragmentRootToStringConverter extends Converter {

		public PackageFragmentRootToStringConverter() {
			super(IPackageFragmentRoot.class, String.class);
		}

		@Override
		public Object convert(Object fromObject) {
			final IPackageFragmentRoot f = (IPackageFragmentRoot) fromObject;
			if (f == null) {
				return ""; //$NON-NLS-1$
			}
			return f.getPath().makeRelative().toString();
		}
	}

	static class PackageFragmentToStringConverter extends Converter {

		public PackageFragmentToStringConverter() {
			super(IPackageFragment.class, String.class);
		}

		@Override
		public Object convert(Object fromObject) {
			if (fromObject == null) {
				return ""; //$NON-NLS-1$
			}
			final IPackageFragment f = (IPackageFragment) fromObject;
			return f.getElementName();
		}
	}

	static class StringToPackageFragmentConverter extends Converter {

		private final JavaClass clazz;

		public StringToPackageFragmentConverter(JavaClass clazz) {
			super(String.class, IPackageFragment.class);
			this.clazz = clazz;
		}

		@Override
		public Object convert(Object fromObject) {
			if (clazz.getFragmentRoot() == null) {
				return null;
			}
			if (fromObject == null) {
				return clazz.getFragmentRoot().getPackageFragment(""); //$NON-NLS-1$
			}

			return clazz.getFragmentRoot().getPackageFragment((String) fromObject);

		}
	}
}
