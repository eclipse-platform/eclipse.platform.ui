/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730, Bug 435625, Bug 436281
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionResultHandler;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.IProviderStatusCallback;
import org.eclipse.e4.tools.emf.ui.common.ProviderStatus;
import org.eclipse.e4.tools.emf.ui.common.ResourceSearchScope;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.AbstractIconDialogWithScopeAndFilter.Entry;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformIconContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.NonReferencedResourceDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.NonReferencedResourceWizard;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.text.bundle.BundleModel;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageObject;
import org.eclipse.pde.internal.core.text.bundle.RequireBundleHeader;
import org.eclipse.pde.internal.core.text.bundle.RequireBundleObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract base class for a find contribution dialog.<br />
 * Includes a filter text box, scope options, and filter options.
 *
 * @author Steven Spungin
 *
 */
public abstract class FilteredContributionDialog extends TitleAreaDialog {

	private static final int MAX_RESULTS = 500;
	private Image contributionTypeImage;
	private TableViewer viewer;
	private ResourceSearchScope searchScope = ResourceSearchScope.PROJECT;
	private EnumSet<ResourceSearchScope> searchScopes = EnumSet.of(ResourceSearchScope.PROJECT);
	// private EnumSet<SearchScope> searchScopes =
	// EnumSet.of(SearchScope.PROJECT, SearchScope.REFERENCES);
	protected ClassContributionCollector collector;
	private Text textBox;
	private Button btnFilterNone;
	private Button btnFilterBundle;
	private Button btnFilterPackage;
	private List<String> filterBundles;
	private List<String> filterPackages;
	private Button btnFilterLocation;
	private List<String> filterLocations;
	private Button btnClearCache;
	private IEclipseContext context;
	private Composite compOptions;
	protected boolean includeNonBundles;
	private Label lblStatus;
	private Button btnIncludeNoneBundle;
	private WritableList viewerList;
	protected BundleImageCache imageCache;
	protected Job currentSearchThread;
	private ContributionResultHandlerImpl currentResultHandler;
	protected ProviderStatus providerStatus;
	protected int hint;
	protected int maxResults;
	protected boolean searching;

	abstract protected ClassContributionCollector getCollector();

	abstract protected String getFilterTextMessage();

	abstract protected String getResourceNameText();

	abstract protected String getDialogMessage();

	abstract protected String getDialogTitle();

	abstract protected String getShellTitle();

	private void updateStatusMessage() {
		String message = ""; //$NON-NLS-1$
		if (searching) {
			message += "Searching...\n";
		}
		// dlg.setStatus("More than " + filter.maxResults +
		// " items were found and have not been displayed");
		if (hint != 0) {
			if (hint == ContributionResultHandler.MORE_CANCELED) {
				message += "The search was cancelled.  Not all results may have been displayed.\n";
			} else {
				message += "More than " + maxResults + " items were found.  Not all results have been displayed.\n";
			}
		}

		if (getCollector() instanceof TargetPlatformContributionCollector) {
			if (providerStatus != null) {
				switch (providerStatus) {
				case READY:
					break;
				case INITIALIZING:
					message += "The provider is initializing.  Results will refresh when complete.";
					break;
				case CANCELLED:
					message += "The provider was cancelled while initializing.  Results may be incomplete.";
					break;
				}
			}
		}
		setMessage(message);
	}

	private class ContributionResultHandlerImpl implements ContributionResultHandler {
		private boolean cancled = false;
		private IObservableList list;

		public ContributionResultHandlerImpl(IObservableList list) {
			this.list = list;
		}

		@Override
		public void result(final ContributionData data) {
			if (!cancled) {
				getShell().getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						list.add(data);
					}
				});
			}
		}

		@Override
		public void moreResults(final int hint, final Filter filter) {
			if (!cancled) {
				getShell().getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						FilteredContributionDialog dlg = (FilteredContributionDialog) filter.userData;
						dlg.hint = hint;
						dlg.maxResults = filter.maxResults;
						dlg.updateStatusMessage();
					}
				});
			}
		}
	}

	@Override
	public boolean close() {
		stopSearchThread(true);
		return super.close();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control ret = super.createContents(parent);
		textBox.notifyListeners(SWT.Modify, new Event());
		return ret;
	}

	public FilteredContributionDialog(Shell parentShell, IEclipseContext context) {
		super(parentShell);
		this.context = context;
		imageCache = new BundleImageCache(context.get(Display.class), getClass().getClassLoader());
	}

	public void setStatus(final String message) {
		getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				lblStatus.setText(message);
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		((GridLayout) parent.getLayout()).numColumns = 4;

		btnClearCache = new Button(parent, SWT.PUSH);
		btnClearCache.setText("Clear Model Cache");
		btnClearCache.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getCollector().clearModelCache();
			}
		});

		btnClearCache.moveAbove(getButton(0));

		lblStatus = new Label(parent, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		lblStatus.setText(""); //$NON-NLS-1$
		lblStatus.moveAbove(btnClearCache);

		// This is called here instead of create contents because btnClearCache
		// is referenced in updateUiState.
		updateUiState();
	}

	// TODO add results found (and/or more indicator)
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				imageCache.dispose();

				if (contributionTypeImage.isDisposed() == false) {
					contributionTypeImage.dispose();
				}
				if (getTitleImageLabel().getImage() != null && getTitleImage().isDisposed() == false) {
					getTitleImageLabel().getImage().dispose();
				}
			}
		});

		getShell().setText(getShellTitle());
		setTitle(getDialogTitle());
		setMessage(getDialogMessage());

		final Image titleImage = getTitleImage();
		setTitleImage(titleImage);

		// TODO param or context
		contributionTypeImage = imageCache.create("/icons/full/obj16/class_obj.gif"); //$NON-NLS-1$

		compOptions = new Composite(comp, SWT.NONE);
		compOptions.setLayoutData(new GridData(GridData.FILL_BOTH));
		compOptions.setLayout(new GridLayout(2, false));

		createOptions(compOptions);

		Label l = new Label(compOptions, SWT.NONE);
		l.setText(getResourceNameText());

		textBox = new Text(compOptions, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		textBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textBox.setMessage(getFilterTextMessage());

		new Label(compOptions, SWT.NONE);

		rebuildViewer();

		collector = getCollector();

		textBox.addKeyListener(new KeyAdapter() {
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
					textBox.setFocus();
				}
			}
		});

		textBox.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				stopSearchThread(true);
				setMessage(""); //$NON-NLS-1$

				viewerList.clear();
				if (doSearch() == true) {
					return;
				}
				searching = true;
				updateStatusMessage();

				currentSearchThread = new Job("Contribution Search") {

					Filter filter;

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask("Contribution Search", IProgressMonitor.UNKNOWN);
						currentResultHandler = new ContributionResultHandlerImpl(viewerList);
						getShell().getDisplay().syncExec(new Runnable() {

							@Override
							public void run() {
								if (searchScopes.contains(ResourceSearchScope.PROJECT)) {
									filter = new Filter(context.get(IProject.class), textBox.getText());
								} else {
									// filter = new Filter(null,
									// textBox.getText());
									filter = new Filter(context.get(IProject.class), textBox.getText());
								}
							}
						});
						filter.maxResults = MAX_RESULTS;
						filter.userData = FilteredContributionDialog.this;
						filter.setBundles(filterBundles);
						filter.setPackages(filterPackages);
						filter.setLocations(filterLocations);
						filter.setSearchScope(searchScopes);
						filter.setIncludeNonBundles(includeNonBundles);
						filter.setProgressMonitor(monitor);
						filter.setProviderStatusCallback(new IProviderStatusCallback() {

							@Override
							public void onStatusChanged(final ProviderStatus status) {
								FilteredContributionDialog.this.providerStatus = status;
								try {
									getShell().getDisplay().asyncExec(new Runnable() {

										@Override
										public void run() {
											updateStatusMessage();
											switch (status) {
											case READY:
												refreshSearch();
												break;
											case CANCELLED:
											case INITIALIZING:
												break;
											}
										}
									});
								} catch (Exception e2) {
									// Dialog may have been closed while
									// provider was still indexing
								}
							}
						});
						collector.findContributions(filter, currentResultHandler);
						currentSearchThread = null;
						monitor.done();
						searching = false;
						getShell().getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								updateStatusMessage();
							}
						});
						return Status.OK_STATUS;
					}

				};

				currentSearchThread.schedule();

			}
		});

		return comp;
	}

	protected Image getTitleImage() {
		return imageCache.create("/icons/full/wizban/newsearch_wiz.gif"); //$NON-NLS-1$
	}

	protected void createOptions(Composite compOptions) {
		{
			Label lblScope = new Label(compOptions, SWT.NONE);
			lblScope.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			lblScope.setText("Scope");

			Composite compScope = new Composite(compOptions, SWT.NONE);
			compScope.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			compScope.setLayout(new RowLayout());

			final Button btnScopeProject = new Button(compScope, SWT.RADIO);
			btnScopeProject.setText("Project Only");
			btnScopeProject.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnScopeProject.getSelection()) {
						searchScope = ResourceSearchScope.PROJECT;
						searchScopes = EnumSet.of(ResourceSearchScope.PROJECT);
						updateUiState();
						getCollector();
						refreshSearch();
					}
				}
			});
			btnScopeProject.setSelection(searchScopes.contains(ResourceSearchScope.PROJECT) && !searchScopes.contains(ResourceSearchScope.REFERENCES));

			final Button btnProjectAndReferences = new Button(compScope, SWT.RADIO);
			btnProjectAndReferences.setText("Project and References");
			btnProjectAndReferences.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnProjectAndReferences.getSelection()) {
						searchScope = ResourceSearchScope.PROJECT;
						searchScopes = EnumSet.of(ResourceSearchScope.PROJECT, ResourceSearchScope.REFERENCES);
						updateUiState();
						getCollector();
						refreshSearch();
					}
				}
			});
			btnProjectAndReferences.setSelection(searchScopes.contains(ResourceSearchScope.PROJECT) && searchScopes.contains(ResourceSearchScope.REFERENCES));

			final Button btnScopeWorkspace = new Button(compScope, SWT.RADIO);
			btnScopeWorkspace.setText("Workspace");
			btnScopeWorkspace.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnScopeWorkspace.getSelection()) {
						searchScope = ResourceSearchScope.WORKSPACE;
						searchScopes = EnumSet.of(ResourceSearchScope.WORKSPACE);
						updateUiState();
						getCollector();
						refreshSearch();
					}
				}
			});
			btnScopeWorkspace.setSelection(searchScopes.contains(ResourceSearchScope.WORKSPACE));

			final Button btnScopeTargetPlatform = new Button(compScope, SWT.RADIO);
			btnScopeTargetPlatform.setText("Target Platform");
			btnScopeTargetPlatform.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnScopeTargetPlatform.getSelection()) {
						searchScope = ResourceSearchScope.TARGET_PLATFORM;
						searchScopes = EnumSet.of(ResourceSearchScope.TARGET_PLATFORM);
						updateUiState();
						getCollector();
						refreshSearch();
					}
				}
			});
			btnScopeTargetPlatform.setSelection(searchScopes.contains(ResourceSearchScope.TARGET_PLATFORM));
		}

		{
			Label lblFilter = new Label(compOptions, SWT.NONE);
			lblFilter.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			lblFilter.setText("Scope Filter");

			Composite compFilter = new Composite(compOptions, SWT.NONE);
			compFilter.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
			compFilter.setLayout(new RowLayout());

			btnFilterNone = new Button(compFilter, SWT.CHECK);
			btnFilterNone.setText("None");
			btnFilterNone.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnFilterNone.getSelection()) {
						removeFilters();
					}
				}
			});

			btnFilterBundle = new Button(compFilter, SWT.CHECK);
			btnFilterBundle.setText("Bundle");
			btnFilterBundle.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnFilterBundle.getSelection()) {
						showBundleFilter();
					} else {
						filterBundles = null;
						refreshSearch();
						updateUiState();
					}
				}
			});

			btnFilterPackage = new Button(compFilter, SWT.CHECK);
			btnFilterPackage.setText("Package");
			btnFilterPackage.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnFilterPackage.getSelection()) {
						showPackageFilter();
					} else {
						filterPackages = null;
						refreshSearch();
						updateUiState();
					}
				}
			});

			btnFilterLocation = new Button(compFilter, SWT.CHECK);
			btnFilterLocation.setText("Location");
			btnFilterLocation.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (btnFilterLocation.getSelection()) {
						showLocationFilter();
					} else {
						filterLocations = null;
						refreshSearch();
						updateUiState();
					}
				}
			});

		}
		{
			Label lblIncludeNoneBundle = new Label(compOptions, SWT.NONE);
			lblIncludeNoneBundle.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			lblIncludeNoneBundle.setText("Non Bundles");

			btnIncludeNoneBundle = new Button(compOptions, SWT.CHECK);
			btnIncludeNoneBundle.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			btnIncludeNoneBundle.setText("");
			btnIncludeNoneBundle.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					includeNonBundles = btnIncludeNoneBundle.getSelection();
					refreshSearch();
				}
			});
		}
	}

	/**
	 *
	 * @return false if default search should be performed, or true if virtual
	 *         function will handle
	 */
	protected boolean doSearch() {
		return false;
	}

	protected void updateUiState() {
		btnFilterNone.setSelection(E.isEmpty(filterBundles) && E.isEmpty(filterPackages) && E.isEmpty(filterLocations));
		btnFilterBundle.setSelection(E.notEmpty(filterBundles));
		btnFilterPackage.setSelection(E.notEmpty(filterPackages));
		btnFilterLocation.setSelection(E.notEmpty(filterLocations));

		// original (default) contribution filter does not support this
		// filtering API
		boolean enabled = !searchScopes.contains(ResourceSearchScope.PROJECT);
		btnFilterNone.setEnabled(enabled);
		btnFilterBundle.setEnabled(enabled);
		btnFilterLocation.setEnabled(enabled);
		btnFilterPackage.setEnabled(enabled);
		btnClearCache.setEnabled(enabled);
		btnIncludeNoneBundle.setEnabled(enabled);
	}

	protected void removeFilters() {
		filterBundles = null;
		setFilterPackages(null);
		filterLocations = null;
		refreshSearch();
		updateUiState();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	public List<String> getFilterPackages() {
		return filterPackages;
	}

	public void setFilterPackages(List<String> filterPackages) {
		this.filterPackages = filterPackages;
	}

	public List<String> getFilterLocations() {
		return filterLocations;
	}

	public void setFilterLocations(List<String> filterLocations) {
		this.filterLocations = filterLocations;
	}

	public List<String> getFilterBundles() {
		return filterBundles;
	}

	public void setFilterBundles(List<String> filterBundles) {
		this.filterBundles = filterBundles;
	}

	protected void refreshSearch() {
		textBox.notifyListeners(SWT.Modify, new Event());
	}

	protected void showBundleFilter() {
		final Collection<String> bundleIds;
		// TODO make HasBundles an interface so we are not tied to
		// implementation
		if (getCollector() instanceof TargetPlatformClassContributionCollector) {
			bundleIds = TargetPlatformClassContributionCollector.getInstance().getBundleIds();
		} else if (getCollector() instanceof TargetPlatformIconContributionCollector) {
			bundleIds = TargetPlatformIconContributionCollector.getInstance().getBundleIds();
		} else {
			return;
		}

		final ArrayList<String> sorted = new ArrayList<String>(bundleIds);
		Collections.sort(sorted);

		TitleAreaFilterDialog dlg = new TitleAreaFilterDialog(getShell(), new ColumnLabelProvider()) {
			@Override
			protected Control createContents(Composite parent) {
				Control ret = super.createContents(parent);
				getViewer().setInput(sorted);
				setMessage("Select the bundle to filter on.");
				setTitle("Bundle Filter");
				getShell().setText("Bundle Filter");
				try {
					setTitleImage(imageCache.create("/icons/full/wizban/plugin_wiz.gif"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return ret;
			}
		};
		if (dlg.open() == Dialog.OK) {
			setFilterBundles((List<String>) dlg.asList());
			refreshSearch();
		}
		updateUiState();
	}

	protected void showPackageFilter() {
		final Collection<String> packages;
		// TODO make HasPackages an interface so we are not tied to
		// implementation
		if (getCollector() instanceof TargetPlatformClassContributionCollector) {
			packages = TargetPlatformClassContributionCollector.getInstance().getPackages();
		} else if (getCollector() instanceof TargetPlatformIconContributionCollector) {
			packages = TargetPlatformIconContributionCollector.getInstance().getPackages();
		} else {
			return;
		}

		final ArrayList<String> sorted = new ArrayList<String>(packages);
		Collections.sort(sorted);

		TitleAreaFilterDialog dlg = new TitleAreaFilterDialog(getShell(), new ColumnLabelProvider()) {
			@Override
			protected Control createContents(Composite parent) {
				Control ret = super.createContents(parent);
				getViewer().setInput(sorted);
				setMessage("Select the package to filter on.");
				setTitle("Package Filter");
				getShell().setText("Package Filter");
				setTitleImage(imageCache.create("/icons/full/wizban/package_wiz.png"));
				return ret;
			}
		};
		if (dlg.open() == Dialog.OK) {
			setFilterPackages((List<String>) dlg.asList());
			refreshSearch();
		}
		updateUiState();
	}

	public ResourceSearchScope getScope() {
		return searchScope;
	}

	public void setScope(ResourceSearchScope scope) {
		this.searchScope = scope;
	}

	public void setCollector(ClassContributionCollector collector) {
		this.collector = collector;
	}

	protected void showLocationFilter() {
		final Collection<String> locations;
		// TODO make HasLocations an interface so we are not tied to
		// implementation
		if (getCollector() instanceof TargetPlatformClassContributionCollector) {
			locations = TargetPlatformClassContributionCollector.getInstance().getLocations();
		} else if (getCollector() instanceof TargetPlatformIconContributionCollector) {
			locations = TargetPlatformIconContributionCollector.getInstance().getLocations();
		} else {
			return;
		}

		// add all parent paths
		final HashSet<String> parentLocations = new HashSet<String>();
		for (String location : locations) {
			if (location.endsWith(".jar")) {
				int index = location.lastIndexOf(File.separator);
				if (index >= 0) {
					location = location.substring(0, index);
					parentLocations.add(location);
				}
			} else {
				parentLocations.add(location);
			}
		}

		final ArrayList<String> sorted = new ArrayList<String>(parentLocations);
		Collections.sort(sorted);

		TitleAreaFilterDialog dlg = new TitleAreaFilterDialog(getShell(), new ColumnLabelProvider()) {
			@Override
			protected Control createContents(Composite parent) {
				Control ret = super.createContents(parent);
				getViewer().setInput(sorted);
				setMessage("Select the location to filter on.");
				setTitle("Location Filter");
				getShell().setText("Location Filter");
				setTitleImage(imageCache.create("/icons/full/wizban/location_wiz.png"));
				return ret;
			}
		};
		if (dlg.open() == Dialog.OK) {
			setFilterLocations((List<String>) dlg.asList());
			refreshSearch();
		}
		updateUiState();
	}

	protected void rebuildViewer() {

		viewerList = new WritableList();

		TableViewer oldViewer = viewer;
		viewer = new TableViewer(compOptions, SWT.FULL_SELECTION | SWT.BORDER);
		if (oldViewer != null) {
			viewer.getTable().moveAbove(oldViewer.getTable());
			oldViewer.getTable().dispose();
		}
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				ContributionData data;
				if (cell.getElement() instanceof ContributionData) {
					data = (ContributionData) cell.getElement();
				} else if (cell.getElement() instanceof ContributionDataFile) {
					data = ((ContributionDataFile) cell.getElement()).getContributionData();
				} else {
					return;
				}

				StyledString styledString = new StyledString();
				if (data.className != null) {
					styledString.append(data.className, null);
				}

				if (data.bundleName != null) {
					styledString.append(" - " + data.bundleName, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				} else if (data.installLocation != null) {
					styledString.append(" - " + data.installLocation, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				if (data.sourceType != null) {
					styledString.append(" - ", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
					styledString.append(data.sourceType + "", StyledString.COUNTER_STYLER); //$NON-NLS-1$
				}

				if (data.iconPath == null) {
					cell.setImage(contributionTypeImage);
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

		viewer.setInput(viewerList);

		if (oldViewer != null) {
			getViewer().getTable().getParent().layout(true, true);
			getViewer().getTable().getParent().redraw();
		}
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setViewer(TableViewer viewer) {
		this.viewer = viewer;
	}

	protected Text getFilterTextBox() {
		return textBox;
	}

	public ResourceSearchScope getSearchScope() {
		return searchScope;
	}

	protected IFile getSelectedIfile() {
		IStructuredSelection s = (IStructuredSelection) getViewer().getSelection();
		if (!s.isEmpty()) {
			Object selected = s.getFirstElement();
			if (selected instanceof ContributionData) {
				ContributionData contributionData = (ContributionData) selected;
				return new ContributionDataFile(contributionData);
			} else if (selected instanceof IFile) {
				return (IFile) selected;
			} else if (selected instanceof Entry) {
				Entry entry = (Entry) selected;
				ContributionData cd = new ContributionData(null, null, "Java", entry.file.getFullPath().toOSString());
				cd.installLocation = entry.installLocation;
				cd.resourceRelativePath = entry.file.getProjectRelativePath().toOSString();
				return new ContributionDataFile(cd);
			}
		}
		return null;
	}

	/**
	 * Returns non null if the selected resource is accessible from the current
	 * project<br />
	 * Restrictions may include non-existent file, non exported class, or the
	 * resource is in a location that is not a bundle.<br />
	 * The function, through user intervention, may find a way to resolve the
	 * file and return a resolution.
	 *
	 * @param file
	 * @param installLocation
	 * @return The original file, a fixed-up (copied or referred) file, or null.
	 */
	protected IFile checkResourceAccessible(final IFile file, String installLocation) {

		// Obviously null is not accessible
		if (file == null) {
			return null;
		}

		// Not a bundle
		final String bundle = getBundle(file);
		if (bundle == null) {
			String message = "The selected resource is not contained in a bundle.";
			NonReferencedResourceWizard wizard = new NonReferencedResourceWizard(getShell(), context.get(IProject.class), bundle, file, installLocation, context);
			wizard.setMessage(message);
			WizardDialog wizDlg = new WizardDialog(getShell(), wizard);
			wizDlg.setBlockOnOpen(true);
			if (wizDlg.open() == IDialogConstants.OK_ID) {
				return wizard.getResult();
			} else {
				return null;
			}
		}

		// Reference by current project
		IProject currentProject = context.get(IProject.class);
		if (currentProject != null && !getBundle(currentProject).equals(bundle)) {
			boolean found = false;
			// search the current project's manifest for require-bundle
			try {
				BundleModel model = loadBundleModel(currentProject);

				RequireBundleHeader rbh = (RequireBundleHeader) model.getBundle().getManifestHeader("Require-Bundle");
				if (rbh != null) {
					for (RequireBundleObject item : rbh.getRequiredBundles()) {
						if (item.getValue().equals(bundle)) {
							found = true;
							break;
						}
					}
				}
				// search the current project's manifest for import-package
				if (!found) {
					if (file instanceof ContributionDataFile) {
						ContributionDataFile cdFile = (ContributionDataFile) file;
						String className = cdFile.getContributionData().className;
						if (className != null) {
							String pakage = NonReferencedResourceDialog.getPackageFromClassName(className);
							ImportPackageHeader iph = (ImportPackageHeader) model.getBundle().getManifestHeader("Import-Package");
							if (iph != null) {
								for (ImportPackageObject item : iph.getPackages()) {
									if (item.getValue().equals(pakage)) {
										found = true;
										break;
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}

			if (!found) {
				String message = "The selected resource's bundle is not referenced by this bundle.";
				NonReferencedResourceWizard wizard = new NonReferencedResourceWizard(getShell(), context.get(IProject.class), bundle, file, installLocation, context);
				wizard.setMessage(message);
				WizardDialog wiz = new WizardDialog(getShell(), wizard);
				wiz.setBlockOnOpen(true);
				if (wiz.open() == IDialogConstants.OK_ID) {
					return wizard.getResult();
				} else {
					return null;
				}
			}
		}
		return file;
	}

	public BundleModel loadBundleModel(IProject currentProject) throws CoreException {
		Document document = new Document();
		String content = new Scanner(PDEProject.getManifest(currentProject).getContents()).useDelimiter("\\Z").next();
		document.set(content);
		BundleModel model = new BundleModel(document, false);
		model.load();
		return model;
	}

	protected EnumSet<ResourceSearchScope> getSearchScopes() {
		return searchScopes;
	}

	public void stopSearchThread(boolean bJoin) {
		if (currentSearchThread != null) {
			currentResultHandler.cancled = true;
			currentSearchThread.cancel();
			if (bJoin) {
				try {
					currentSearchThread.join();
				} catch (InterruptedException e) {
				} finally {
					currentSearchThread = null;
				}
			} else {
				currentSearchThread = null;
			}
		}
	}

	static public String getBundle(IFile file) {

		if (file instanceof ContributionDataFile) {
			ContributionDataFile cdFile = (ContributionDataFile) file;
			String ret = cdFile.getBundle();
			if (ret != null) {
				return ret;
			} else if (cdFile.getContributionData().installLocation != null) {
				return getBundle(cdFile.getContributionData().installLocation);
			} else {
				return null;
			}
		}

		IProject project = file.getProject();
		return getBundle(project);
	}

	static String getBundle(IProject project) {
		IFile f = project.getFile("/META-INF/MANIFEST.MF"); //$NON-NLS-1$

		if (f != null && f.exists()) {
			BufferedReader r = null;
			try {
				InputStream s = f.getContents();
				r = new BufferedReader(new InputStreamReader(s));
				String line;
				while ((line = r.readLine()) != null) {
					if (line.startsWith("Bundle-SymbolicName:")) { //$NON-NLS-1$
						int start = line.indexOf(':');
						int end = line.indexOf(';');
						if (end == -1) {
							end = line.length();
						}
						return line.substring(start + 1, end).trim();
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return null;
	}

	/**
	 * Searches the directory for a manifest and parses the symbolic name.
	 *
	 * @param rootDirectory
	 * @return
	 */
	public static String getBundle(String rootDirectory) {
		File f = new File(new File(rootDirectory), "/META-INF/MANIFEST.MF"); //$NON-NLS-1$

		if (f.exists()) {
			BufferedReader r = null;
			try {
				InputStream s = new FileInputStream(f);
				r = new BufferedReader(new InputStreamReader(s));
				String line;
				while ((line = r.readLine()) != null) {
					if (line.startsWith("Bundle-SymbolicName:")) { //$NON-NLS-1$
						int start = line.indexOf(':');
						int end = line.indexOf(';');
						if (end == -1) {
							end = line.length();
						}
						return line.substring(start + 1, end).trim();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return null;
	}
}
