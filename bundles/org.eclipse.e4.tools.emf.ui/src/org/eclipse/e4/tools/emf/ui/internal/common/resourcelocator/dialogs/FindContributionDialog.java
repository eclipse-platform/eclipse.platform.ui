/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *	   Lars Vogel <lars.vogel@gmail.com> - Enhancements
 *     Steven Spungin <steven@spungin.tv> - Modified to be generic picker, Bug 424730, Ongoing Maintenance
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import java.util.Iterator;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionResultHandler;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.Filter;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A find dialog for model contributions. List can be filtered by bundle and/or
 * package
 *
 * @author Steven Spungin
 *
 */
public class FindContributionDialog extends TitleAreaDialog {
	private IProject project;
	private Image javaClassImage;
	private TableViewer viewer;
	private Messages Messages;
	private String bundleclassUri;
	@SuppressWarnings("unused")
	private IEclipseContext context;
	private String packageFilter;
	private String bundleFilter;
	private Bundle bundle;
	private String mode;
	private Image titleImage;
	private String platformUri;

	public FindContributionDialog(IEclipseContext context) {
		super(context.get(Shell.class));
		this.context = context;
		this.project = context.get(IProject.class);
		this.Messages = context.get(Messages.class);
		packageFilter = (String) context.get("package"); //$NON-NLS-1$
		bundleFilter = (String) context.get("bundle"); //$NON-NLS-1$
		bundle = context.get(Bundle.class);
		mode = (String) context.get("mode"); //$NON-NLS-1$
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		// TODO param
		getShell().setText(Messages.ContributionClassDialog_ShellTitle);

		if ("show-bundles".equals(mode)) { //$NON-NLS-1$
			// TODO create icon
		} else if ("show-packages".equals(mode)) { //$NON-NLS-1$
			// TODO create icon
		} else if ("show-icons".equals(mode)) { //$NON-NLS-1$
			// TODO create icon
		} else {
			titleImage = new Image(comp.getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/newclass_wiz.png")); //$NON-NLS-1$
			setTitleImage(titleImage);
		}

		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				javaClassImage.dispose();
				if (titleImage != null) {
					titleImage.dispose();
				}
			}
		});

		javaClassImage = new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/obj16/class_obj.gif")); //$NON-NLS-1$

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		// TODO param
		l.setText(Messages.ContributionClassDialog_Label_Classname);

		final Text t = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// TODO param
		t.setMessage(Messages.ContributionClassDialog_FilterText_Message);

		new Label(container, SWT.NONE);

		viewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				ContributionData data = (ContributionData) cell.getElement();
				StyledString styledString = new StyledString();
				if ("show-bundles".equals(mode)) { //$NON-NLS-1$
					styledString.append(data.bundleName, StyledString.DECORATIONS_STYLER);
				} else if ("show-packages".equals(mode)) { //$NON-NLS-1$
					int dot = data.className.lastIndexOf("."); //$NON-NLS-1$
					String packageName;
					if (dot >= 0) {
						packageName = data.className.substring(0, dot);
					} else {
						packageName = ""; //$NON-NLS-1$
					}
					styledString.append(packageName, StyledString.DECORATIONS_STYLER);
				} else if ("show-icons".equals(mode)) { //$NON-NLS-1$
					styledString.append(data.iconPath, null);

					if (data.bundleName != null) {
						styledString.append(" - " + data.bundleName, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
					}

					if (data.sourceType != null) {
						styledString.append(" - ", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
						styledString.append(data.sourceType + "", StyledString.COUNTER_STYLER); //$NON-NLS-1$
					}

					if (data.iconPath == null) {
						cell.setImage(javaClassImage);
					}
				} else {
					styledString.append(data.className, null);

					if (data.bundleName != null) {
						styledString.append(" - " + data.bundleName, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
					}

					if (data.sourceType != null) {
						styledString.append(" - ", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
						styledString.append(data.sourceType + "", StyledString.COUNTER_STYLER); //$NON-NLS-1$
					}

					if (data.iconPath == null) {
						cell.setImage(javaClassImage);
					}
				}

				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		final WritableList list = new WritableList();
		viewer.setInput(list);

		final ClassContributionCollector collector = getCollector();

		t.addModifyListener(new ModifyListener() {
			private ContributionResultHandlerImpl currentResultHandler;

			@Override
			public void modifyText(ModifyEvent e) {
				if (currentResultHandler != null) {
					currentResultHandler.cancled = true;
				}
				list.clear();
				currentResultHandler = new ContributionResultHandlerImpl(list);
				Filter filter = new Filter(project, t.getText());
				collector.findContributions(filter, currentResultHandler);
				t.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.ARROW_DOWN) {
							if (viewer.getTable().getItemCount() > 0) {
								viewer.getTable().setFocus();
								viewer.getTable().select(0);
							}
						}
					}
				});
				viewer.getTable().addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						super.keyPressed(e);
						if ((e.keyCode == SWT.ARROW_UP) && (viewer.getTable().getSelectionIndex() == 0)) {
							t.setFocus();
						}
					}
				});
			}
		});

		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				ContributionData cd = (ContributionData) element;
				if ("show-bundles".equals(mode)) { //$NON-NLS-1$
					// only add first item from each bundle
					boolean found = false;
					for (Iterator it = list.iterator(); it.hasNext();) {
						ContributionData cd2 = (ContributionData) it.next();
						if (cd2.bundleName == null || cd2.bundleName.equals(cd.bundleName)) {
							if (found == false) {
								found = true;
							} else {
								return false;
							}
						}
					}
					return true;
				} else if ("show-packages".equals(mode)) { //$NON-NLS-1$
					if (bundleFilter != null && bundleFilter.isEmpty() == false) {
						if (!bundleFilter.equals(cd.bundleName)) {
							return false;
						}
					}
					String packageName;
					int last = cd.className.lastIndexOf("."); //$NON-NLS-1$
					if (last >= 0) {
						packageName = cd.className.substring(0, last);
					} else {
						packageName = ""; //$NON-NLS-1$
					}
					// only add first item from each package
					boolean found = false;
					for (Iterator it = list.iterator(); it.hasNext();) {
						boolean matches = false;
						ContributionData cd2 = (ContributionData) it.next();
						if (packageName.isEmpty() && cd2.className.contains(".") == false) { //$NON-NLS-1$
							matches = true;
						} else if (cd2.className.startsWith(packageName + ".")) { //$NON-NLS-1$
							matches = true;
						}
						if (matches) {
							if (found == false) {
								found = true;
							} else {
								return false;
							}
						}
					}
					return true;
				} else if ("show-icons".equals(mode)) { //$NON-NLS-1$
					if (cd.iconPath == null) {
						return false;
					}
					if (bundleFilter != null && bundleFilter.isEmpty() == false) {
						if (!bundleFilter.equals(cd.bundleName)) {
							return false;
						}
					}
					if (packageFilter != null && packageFilter.isEmpty() == false) {
						if (!cd.className.startsWith(packageFilter + ".")) { //$NON-NLS-1$
							return false;
						}
					}
					return true;
				} else {
					if (bundleFilter != null && bundleFilter.isEmpty() == false) {
						if (!bundleFilter.equals(cd.bundleName)) {
							return false;
						}
					}
					if (packageFilter != null && packageFilter.isEmpty() == false) {
						if (!cd.className.startsWith(packageFilter + ".")) { //$NON-NLS-1$
							return false;
						}
					}

					return true;
				}
			}
		} });

		StringBuilder sbFind = new StringBuilder();

		if ("show-bundles".equals(mode)) { //$NON-NLS-1$
			setTitle(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findBundle);
			sbFind.append(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findBundle);
		} else if ("show-packages".equals(mode)) { //$NON-NLS-1$
			setTitle(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findPackage);
			sbFind.append(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findPackage);
			if (E.notEmpty(bundleFilter)) {
				sbFind.append(" " + org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_inBundle + " " + bundleFilter); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else if ("show-icons".equals(mode)) { //$NON-NLS-1$
			setTitle(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findIcon);
			sbFind.append(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findIcon);
			if (E.notEmpty(bundleFilter)) {
				sbFind.append(" " + org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_inBundle + " " + bundleFilter); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (E.notEmpty(packageFilter)) {
				sbFind.append(" " + org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_inPackage + " " + packageFilter); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			setTitle(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findClass);
			sbFind.append(org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_findClass);
			if (E.notEmpty(bundleFilter)) {
				sbFind.append(" " + org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_inBundle + " " + bundleFilter); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (E.notEmpty(packageFilter)) {
				sbFind.append(" " + org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages.FindContributionDialog_inPackage + " " + packageFilter); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		setMessage(sbFind.toString());

		// preload list if few items
		if (list.size() <= 30) {
			t.notifyListeners(SWT.Modify, null);
		}
		return comp;
	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			ContributionData cd = (ContributionData) s.getFirstElement();
			String uri = "bundleclass://" + cd.bundleName + "/" + cd.className; //$NON-NLS-1$ //$NON-NLS-2$
			platformUri = "platform:/plugin/" + cd.bundleName + "/" + cd.className; //$NON-NLS-1$ //$NON-NLS-2$
			setBundleclassUri(uri);
			super.okPressed();
		} else {
			super.cancelPressed();
		}
	}

	private ClassContributionCollector getCollector() {
		if (bundle == null) {
			return null;
		}
		BundleContext context = bundle.getBundleContext();
		ServiceReference ref = context.getServiceReference(ClassContributionCollector.class.getName());
		if (ref != null) {
			return (ClassContributionCollector) context.getService(ref);
		}
		return null;
	}

	public String getBundleclassUri() {
		return bundleclassUri;
	}

	public String getPlatformUri() {
		return platformUri;
	}

	public void setBundleclassUri(String bundleclass) {
		this.bundleclassUri = bundleclass;
	}

	private static class ContributionResultHandlerImpl implements ContributionResultHandler {
		private boolean cancled = false;
		private IObservableList list;

		public ContributionResultHandlerImpl(IObservableList list) {
			this.list = list;
		}

		@Override
		public void result(ContributionData data) {
			if (!cancled) {
				list.add(data);
			}
		}

		@Override
		public void moreResults(int hint, Filter filter) {
		}

	}
}