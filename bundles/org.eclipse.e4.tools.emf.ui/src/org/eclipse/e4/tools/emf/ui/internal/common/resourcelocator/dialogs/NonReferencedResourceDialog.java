/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730, Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.Plugin;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.ContributionDataFile;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.FilteredContributionDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.BundleConverter;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NonReferencedResourceDialog extends TitleAreaDialog {
	private String bundle;
	private final IFile file;
	private IFile result;
	private final IProject project;
	private String className;
	private final String installLocation;
	protected Runnable okAction;
	private BundleImageCache imageCache;
	private final IEclipseContext context;

	public NonReferencedResourceDialog(Shell parentShell, IProject project, String bundle, IFile file,
		String installLocation, IEclipseContext context) {
		super(parentShell);
		this.project = project;
		this.bundle = bundle;
		this.file = file;
		this.installLocation = installLocation;
		this.context = context;

		if (bundle == null && installLocation != null) {
			this.bundle = FilteredContributionDialog.getBundle(installLocation);
		}
		if (file instanceof ContributionDataFile) {
			final ContributionDataFile cdf = (ContributionDataFile) file;
			className = cdf.getContributionData().className;
		}
	}

	@Override
	protected void okPressed() {
		if (okAction != null) {
			try {
				okAction.run();
				super.okPressed();
			} catch (final Exception e) {
			}
		} else {
			super.okPressed();
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite compParent = (Composite) super.createDialogArea(parent);

		final Composite comp = new Composite(compParent, SWT.NONE);
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 2;
		comp.setLayoutData(layoutData);
		comp.setLayout(new GridLayout(2, false));

		final String message = ""; //$NON-NLS-1$
		Button defaultButton = null;

		if (installLocation != null) {
			final Label label = new Label(comp, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label.setText(Messages.NonReferencedResourceDialog_installLocation);

			final Text label2 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label2.setText(installLocation);
		}

		if (className != null) {
			final ContributionData cd = ((ContributionDataFile) file).getContributionData();
			final Label label = new Label(comp, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label.setText(Messages.NonReferencedResourceDialog_2);

			final Text label2 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			if (bundle != null) {
				label2.setText(bundle);
			} else {
				label2.setText(Messages.NonReferencedResourceDialog__ast_notInABundle_ast);
			}

			final Label label3 = new Label(comp, SWT.NONE);
			label3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label3.setText(Messages.NonReferencedResourceDialog_package);

			final Text label4 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label4.setText(getPackageFromClassName(className));

			final Label label5 = new Label(comp, SWT.NONE);
			label5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label5.setText(Messages.NonReferencedResourceDialog_class);

			final Text label6 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label6.setText(cd.className.substring(cd.className.lastIndexOf('.') + 1));

			if (bundle != null) {
				final Label label7 = new Label(comp, SWT.NONE);
				label7.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				label7.setText(Messages.NonReferencedResourceDialog_url);

				final Text label8 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
				label8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				label8.setText("bundleclass://" + bundle + "/" + className); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			final Label label = new Label(comp, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label.setText(Messages.NonReferencedResourceDialog_bundle);

			final Text label2 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			if (bundle != null) {
				label2.setText(bundle);
			} else {
				label2.setText(Messages.NonReferencedResourceDialog_ast_notInABundle_ast);
			}

			final Label label7 = new Label(comp, SWT.NONE);
			label7.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label7.setText(Messages.NonReferencedResourceDialog_directory);

			final Text label8 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label8.setText(file.getFullPath().removeFirstSegments(1).removeLastSegments(1).toOSString());

			final Label label3 = new Label(comp, SWT.NONE);
			label3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label3.setText(Messages.NonReferencedResourceDialog_resource);

			final Text label4 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
			label4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			label4.setText(file.getFullPath().lastSegment());

			if (bundle != null) {
				final Label label5 = new Label(comp, SWT.NONE);
				label5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				label5.setText(Messages.NonReferencedResourceDialog_url);

				final Text label6 = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.READ_ONLY);
				label6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				label6
					.setText("platform:/plugin/" + bundle + "/" + file.getFullPath().removeFirstSegments(1).toOSString()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		final Label lblMessage = new Label(comp, SWT.NONE);
		lblMessage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 0));
		lblMessage.setText(message);

		final Group group = new Group(comp, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 0));
		group.setLayout(new GridLayout(1, false));
		group.setText(Messages.NonReferencedResourceDialog_Action);

		if (bundle != null) {
			final Button btnRequire = new Button(group, SWT.RADIO);
			btnRequire.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnRequire.setText(Messages.NonReferencedResourceDialog_requireBundle);
			btnRequire.setImage(imageCache.loadFromKey(ResourceProvider.IMG_Obj16_bundle));
			btnRequire.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnRequire.getSelection()) {
						okAction = new Runnable() {

							@Override
							public void run() {
								doRequireBundle(bundle, installLocation);
							}
						};
					}
				}
			});
			defaultButton = btnRequire;
		}

		// make sure className is not in the default package (contains '.')
		if (className != null && bundle != null && className.contains(".")) { //$NON-NLS-1$
			final Button btnImport = new Button(group, SWT.RADIO);
			btnImport.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnImport.setText(Messages.NonReferencedResourceDialog_importPackage);
			btnImport.setImage(imageCache.create("/icons/full/obj16/package_obj.gif")); //$NON-NLS-1$
			btnImport.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnImport.getSelection()) {
						okAction = new Runnable() {
							@Override
							public void run() {
								final IFile fileManifest = project.getFile("META-INF/MANIFEST.MF"); //$NON-NLS-1$
								Manifest manifest;
								try {
									manifest = new Manifest(fileManifest.getContents());
									String value = manifest.getMainAttributes().getValue("Import-Package"); //$NON-NLS-1$

									final String packageName = getPackageFromClassName(className);
									// TODO ensure the packageName is not
									// already in the manifest (although it
									// should not be if we are here)
									if (value == null) {
										value = packageName;
									} else {
										value += "," + packageName; //$NON-NLS-1$
									}
									manifest.getMainAttributes().putValue("Import-Package", value); //$NON-NLS-1$
									final ByteArrayOutputStream bos = new ByteArrayOutputStream();
									manifest.write(bos);
									final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
									fileManifest.setContents(bis, true, true, null);
									result = file;
								} catch (final Exception e) {
									e.printStackTrace();
								}
							}
						};
					}
				}
			});
		}

		if (bundle != null) {
			final Button btnUseAnyway = new Button(group, SWT.RADIO);
			btnUseAnyway.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnUseAnyway.setText(Messages.NonReferencedResourceDialog_useAnyway);
			btnUseAnyway.setImage(imageCache.create("/icons/full/obj16/use_anyway.gif")); //$NON-NLS-1$
			btnUseAnyway.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnUseAnyway.getSelection()) {
						result = file;
						okAction = null;
					}
				}
			});

		} else {
			if (file instanceof ContributionDataFile) {
				final ContributionDataFile cdf = (ContributionDataFile) file;
				final Button btnConvertToBundle = new Button(group, SWT.RADIO);
				btnConvertToBundle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				btnConvertToBundle.setText(Messages.NonReferencedResourceDialog_convertAndRequire);
				btnConvertToBundle.setImage(imageCache.create("/icons/full/obj16/bundle.png")); //$NON-NLS-1$
				btnConvertToBundle.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (btnConvertToBundle.getSelection()) {
							okAction = new Runnable() {

								@Override
								public void run() {
									String bundleId;
									try {
										final ContributionData contributionData = cdf.getContributionData();
										bundleId = BundleConverter.convertProjectToBundle(
											contributionData.installLocation, project.getWorkspace());
										if (bundleId != null) {

											final ContributionData cdConverted = new ContributionData(bundleId,
												contributionData.className, contributionData.sourceType,
												contributionData.iconPath);
											cdConverted.installLocation = installLocation;
											cdConverted.resourceRelativePath = Path
												.fromOSString(contributionData.iconPath).removeFirstSegments(1)
												.toOSString();
											doRequireBundle(bundleId, installLocation);
											result = new ContributionDataFile(cdConverted);
										}
									} catch (final Exception e1) {
										MessageDialog.openError(getShell(), Messages.NonReferencedResourceDialog_error,
											e1.getMessage());
									}
								}
							};
						}
					}
				});
			}
		}

		if (className == null) {
			final Button btnCopy = new Button(group, SWT.RADIO);
			defaultButton = btnCopy;
			btnCopy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnCopy.setText(Messages.NonReferencedResourceDialog_copyToThisProject);
			btnCopy.setImage(imageCache.create("/icons/full/obj16/copy_to_project.png")); //$NON-NLS-1$
			btnCopy.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnCopy.getSelection()) {
						okAction = new Runnable() {

							@Override
							public void run() {
								copyResourceToProject(project);
							}
						};
					}
				}
			});
		}

		if (className == null) {
			final Button btnCopy2 = new Button(group, SWT.RADIO);
			btnCopy2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			btnCopy2.setText(Messages.NonReferencedResourceDialog_copyToReferenedProject);
			btnCopy2.setImage(imageCache.create("/icons/full/obj16/copy_to_project.png")); //$NON-NLS-1$
			btnCopy2.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnCopy2.getSelection()) {
						okAction = new Runnable() {

							@Override
							public void run() {
								final ReferencedProjectPickerDialog dlg = new ReferencedProjectPickerDialog(getShell(),
									project) {
									@Override
									protected Control createContents(Composite parent) {
										final Control ret = super.createContents(parent);
										setMessage(Messages.NonReferencedResourceDialog_selectProjectToReceiveCopy);
										setTitleImage(imageCache.create("/icons/full/wizban/plugin_wiz.gif")); //$NON-NLS-1$

										return ret;
									}
								};
								if (dlg.open() == IDialogConstants.OK_ID) {
									copyResourceToProject((IProject) dlg.getFirstElement());
								}
							}
						};
					}
				}
			});
		}

		if (defaultButton != null) {
			defaultButton.setSelection(true);
			defaultButton.notifyListeners(SWT.Selection, new Event());
		}
		return comp;
	}

	// @Refactor
	static public String getPackageFromClassName(String className) {
		final int index = className.lastIndexOf('.');
		if (index >= 0) {
			return className.substring(0, index);
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected Control createContents(Composite parent) {
		imageCache = new BundleImageCache(parent.getDisplay(), getClass().getClassLoader(), context);
		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				imageCache.dispose();
			}
		});

		final Control ret = super.createContents(parent);
		setMessage(Messages.NonReferencedResourceDialog_resourceNotReferenced);
		final String message = getMessage();
		setMessage(message);
		setTitle(Messages.NonReferencedResourceDialog_resourceReferenceWarning);
		getShell().setText(Messages.NonReferencedResourceDialog_resourceReferenceWarning);
		try {
			setTitleImage(imageCache.create(Plugin.ID, "/icons/full/wizban/newefix_wizban.png")); //$NON-NLS-1$
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public IFile getResult() {
		return result;
	}

	public void copyResourceToProject(IProject project) {
		try {
			final ProjectFolderPickerDialog dlg = new ProjectFolderPickerDialog(getShell(), project, file.getFullPath()
				.toOSString()) {
				@Override
				protected Control createContents(Composite parent) {
					final Control ret = super.createContents(parent);
					setMessage(Messages.NonReferencedResourceDialog_selectTheFolderResourceCopy);
					setTitleImage(imageCache.create(Plugin.ID, "/icons/full/wizban/add_to_dir_wiz.png")); //$NON-NLS-1$

					return ret;
				}
			};

			if (dlg.open() == IDialogConstants.OK_ID) {
				// String filename = ((ContributionDataFile)
				// file).getContributionData().className + ".class";
				IPath newPath = Path.fromOSString(dlg.getValue());
				if (newPath.isEmpty() == false) {
					CoreUtility.createFolder(project.getFolder(newPath));
				}
				if (className != null) {
					newPath.append(className + ".class"); //$NON-NLS-1$
				} else {
					final String name = file.getFullPath().lastSegment();
					newPath = newPath.append(name);
				}
				final IFile fileClone = project.getFile(newPath);
				fileClone.create(file.getContents(), false, null);
				result = fileClone;
			}
		} catch (final CoreException e1) {
			e1.printStackTrace();
			MessageDialog.openError(getShell(), "Error", e1.getMessage()); //$NON-NLS-1$

		}
	}

	public void doRequireBundle(String bundle, String installLocation) {

		// Get source bundle version from manifest
		String version = null;
		InputStream srcStream = null;
		try {
			Manifest manifestSource;
			if (installLocation.endsWith(".jar")) { //$NON-NLS-1$
				final ZipFile zip = new ZipFile(installLocation);
				srcStream = zip.getInputStream(zip.getEntry("META-INF/MANIFEST.MF")); //$NON-NLS-1$
				manifestSource = new Manifest(srcStream);
				zip.close();
			} else {
				srcStream = new BufferedInputStream(new FileInputStream(installLocation + "/META-INF/MANIFEST.MF")); //$NON-NLS-1$
				manifestSource = new Manifest(srcStream);
			}
			version = manifestSource.getMainAttributes().getValue("Bundle-Version"); //$NON-NLS-1$
			if (version != null) {
				version = version.replaceFirst("\\.qualifier", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return;
		} finally {
			try {
				srcStream.close();
			} catch (final Exception e) {
			}
		}

		final IFile fileManifest = project.getFile("META-INF/MANIFEST.MF"); //$NON-NLS-1$
		Manifest manifest;
		try {
			manifest = new Manifest(fileManifest.getContents());
			String value = manifest.getMainAttributes().getValue("Require-Bundle"); //$NON-NLS-1$
			if (value == null) {
				manifest.getMainAttributes().putValue("Require-Bundle", bundle); //$NON-NLS-1$
			} else {
				value += "," + bundle; //$NON-NLS-1$
				if (version != null) {
					value += ";bundle-version=" + version; //$NON-NLS-1$
				}
				manifest.getMainAttributes().putValue("Require-Bundle", value); //$NON-NLS-1$
			}
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			manifest.write(bos);
			// StringReader reader = new
			final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			fileManifest.setContents(bis, true, true, null);
			result = file;
		} catch (final IOException e1) {
			e1.printStackTrace();
		} catch (final CoreException e1) {
			e1.printStackTrace();
		}
	}
}