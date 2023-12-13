/*******************************************************************************
 *
 * Copyright (c) 2010, 2017 BestSolution.at and others.
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
 * Steven Spungin <steven@spungin.tv> - Bug 404136, Bug 424730, Bug 436281
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.ResourceSearchScope;
import org.eclipse.e4.tools.emf.ui.common.Util;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.StringMatcher;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformIconContributionCollector;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A FilteredContributionDialog with additional options for icon resources.
 * Features in include
 * <ul>
 * <li>Rebuilding the viewer when row height decreases. (Workaround for an SWT limitation)
 * <li>Icon previews are displace in the first column.
 * <li>Limited the maximum image size.
 * </ul>
 *
 * @author "Steven Spungin"
 *
 * @see FilteredContributionDialog
 */
public abstract class AbstractIconDialogWithScopeAndFilter extends FilteredContributionDialog {
	private final IProject project;
	private final Map<IFile, Image> icons = Collections.synchronizedMap(new HashMap<>());

	static public class Entry {
		IFile file;
		String installLocation;
	}

	protected Integer maxDisplayedImageSize;
	protected int maxImageHeight;
	public static TargetPlatformIconContributionCollector collector;

	public AbstractIconDialogWithScopeAndFilter(Shell parentShell, IEclipseContext context) {
		super(parentShell, context);
		project = context.get(IProject.class);
	}

	@Override
	protected String getFilterTextMessage() {
		return Messages.AbstractIconDialogWithScopeAndFilter_typeToStartSearch;
	}

	@Override
	protected String getResourceNameText() {
		return Messages.AbstractIconDialogWithScopeAndFilter_iconName;
	}

	@Override
	protected ClassContributionCollector getCollector() {
		if (collector == null) {
			collector = TargetPlatformIconContributionCollector.getInstance();
		}
		return collector;
	}

	@Override
	protected void createOptions(Composite compOptions) {
		super.createOptions(compOptions);

		final Label lblMaxSize = new Label(compOptions, SWT.NONE);
		lblMaxSize.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		lblMaxSize.setText(Messages.AbstractIconDialogWithScopeAndFilter_maxDisplayedImageSize);

		final ComboViewer cv = new ComboViewer(compOptions);
		cv.getCombo().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));
		cv.add(10);
		cv.add(20);
		cv.add(30);
		cv.add(50);
		cv.add(100);
		cv.add(150);
		cv.add(200);
		maxDisplayedImageSize = 30;
		cv.setSelection(new StructuredSelection(maxDisplayedImageSize));

		cv.addSelectionChangedListener(event -> {
			maxDisplayedImageSize = (Integer) ((IStructuredSelection) cv.getSelection()).getFirstElement();
			rebuildViewer();
			refreshSearch();
			// combo viewer cannot make rows smaller, so we will need to
			// rebuild it in that case.
		});
	}

	@Override
	protected void rebuildViewer() {
		super.rebuildViewer();
		getViewer().getTable().setHeaderVisible(true);

		((GridData) getViewer().getTable().getLayoutData()).minimumHeight = 100;

		final TableViewerColumn colIcon = new TableViewerColumn(getViewer(), SWT.NONE);
		colIcon.getColumn().setText(Messages.AbstractIconDialogWithScopeAndFilter_icon);
		final TableViewerColumn colText = new TableViewerColumn(getViewer(), SWT.NONE);
		colText.getColumn().setText(Messages.AbstractIconDialogWithScopeAndFilter_details);

		// resize the row height using a MeasureItem listener
		getViewer().getTable().addListener(SWT.MeasureItem, event -> event.height = maxDisplayedImageSize);

		colIcon.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				IFile file;
				if (cell.getElement() instanceof ContributionData) {
					file = new ContributionDataFile((ContributionData) cell.getElement());
				} else if (cell.getElement() instanceof Entry) {
					file = ((Entry) cell.getElement()).file;
				} else {
					file = (IFile) cell.getElement();
				}

				Image img = icons.get(file);
				if (img == null) {
					try (InputStream in = file.getContents()) {
						img = new Image(cell.getControl().getDisplay(), in);
						icons.put(file, img);
					} catch (final Exception e) {
						// e.printStackTrace();
						return;
					}
				}

				Image scaled = Util.scaleImage(img, maxDisplayedImageSize);
				if (!scaled.equals(img)) {
					icons.put(file, scaled);
				}
				final int width = AbstractIconDialogWithScopeAndFilter.this.getViewer().getTable().getColumn(0)
						.getWidth();
				if (scaled.getImageData().width > width) {
					AbstractIconDialogWithScopeAndFilter.this.getViewer().getTable().getColumn(0)
							.setWidth(scaled.getImageData().width);
				}
				final int height = scaled.getImageData().height;
				if (height > maxImageHeight) {
					maxImageHeight = height;
				}

				cell.setImage(scaled);
			}

		});
		colIcon.getColumn().setWidth(30);

		colText.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				IFile file;
				String installLocation = null;
				if (cell.getElement() instanceof ContributionData) {
					final ContributionData contributionData = (ContributionData) cell.getElement();
					file = new ContributionDataFile(contributionData);
					installLocation = contributionData.installLocation;
				} else if (cell.getElement() instanceof Entry) {
					file = ((Entry) cell.getElement()).file;
					installLocation = ((Entry) cell.getElement()).installLocation;
				} else {
					file = (IFile) cell.getElement();
				}
				final StyledString styledString = new StyledString(file.getProjectRelativePath().toString(), null);
				final String bundle = FilteredContributionDialog.getBundle(file);
				if (bundle != null) {
					styledString.append(" - " + bundle, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				} else if (installLocation != null) {
					styledString.append(" - " + installLocation, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
		});
		colText.getColumn().setWidth(400);

		getShell().addDisposeListener(e -> clearImages());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite comp = (Composite) super.createDialogArea(parent);
		maxDisplayedImageSize = 20;
		rebuildViewer();

		return comp;
	}

	private IconMatchCallback callback;
	private String returnValue;
	private SearchThread task;

	@Override
	protected boolean doSearch() {

		// if (getSearchScopes().contains(SearchScope.TARGET_PLATFORM) ||
		// getSearchScopes().contains(SearchScope.WORKSPACE)) {
		if (getSearchScopes().contains(ResourceSearchScope.TARGET_PLATFORM)) {
			return false;
		}
		final Timer timer = new Timer(true);

		if (callback != null) {
			callback.cancel = true;
		}
		if (task != null) {
			task.cancel();
		}
		task = null;

		clearImages();

		@SuppressWarnings("unchecked")
		IObservableList<Entry> fileList = (IObservableList<Entry>) getViewer().getInput();
		callback = new IconMatchCallback(fileList);
		final Filter filter = new Filter(project, getFilterTextBox().getText());
		filter.setSearchScope(getSearchScopes());
		filter.setBundles(getFilterBundles());
		filter.setLocations(getFilterLocations());
		filter.setPackages(getFilterPackages());
		filter.setIncludeNonBundles(includeNonBundles);
		task = new SearchThread(callback, filter);
		timer.schedule(task, 500);
		// }
		return true;
	}

	private void clearImages() {
		for (final Image img : icons.values()) {
			img.dispose();
		}
		icons.clear();
	}

	@Override
	protected void okPressed() {
		returnValue = null;
		IFile file = getSelectedIfile();
		if (file != null) {
			String installLocation = null;
			if (file instanceof ContributionDataFile) {
				final ContributionDataFile cdf = (ContributionDataFile) file;
				installLocation = cdf.getContributionData().installLocation;

			}
			file = checkResourceAccessible(file, installLocation);
			if (file != null) {
				final String bundle = getBundle(file);
				String uri;
				uri = "platform:/plugin/" + bundle + "/" + file.getProjectRelativePath().toString(); //$NON-NLS-1$ //$NON-NLS-2$
				returnValue = uri;
				super.okPressed();
			}
		}
	}

	private static class IconMatchCallback {
		private volatile boolean cancel;
		private final IObservableList<Entry> list;

		private IconMatchCallback(IObservableList<Entry> list) {
			this.list = list;
		}

		public void match(final IFile file, final String installLocation) {
			if (!cancel) {
				list.getRealm().exec(() -> {
					final Entry entry = new Entry();
					entry.file = file;
					entry.installLocation = installLocation;
					list.add(entry);
				});
			}
		}
	}

	private static class SearchThread extends TimerTask {
		private final IconMatchCallback callback;
		private final StringMatcher matcherGif;
		private final StringMatcher matcherJpg;
		private final StringMatcher matcherPng;
		private final StringMatcher matcherBinFolder;
		private final Filter filter;
		private boolean includeNonBundles;


		public SearchThread(IconMatchCallback callback, Filter filter) {
			matcherGif = new StringMatcher("*" + filter.namePattern + "*.gif", true, false); //$NON-NLS-1$//$NON-NLS-2$
			matcherJpg = new StringMatcher("*" + filter.namePattern + "*.jpg", true, false); //$NON-NLS-1$//$NON-NLS-2$
			matcherPng = new StringMatcher("*" + filter.namePattern + "*.png", true, false); //$NON-NLS-1$//$NON-NLS-2$
			matcherBinFolder = new StringMatcher("bin/*", true, false); //$NON-NLS-1$
			this.callback = callback;
			this.filter = filter;
		}

		@Override
		public void run() {
			List<IProject> projects;
			if (filter.getSearchScope().contains(ResourceSearchScope.TARGET_PLATFORM)) {
				// never should be here because it is cached and not run as
				// thread
				return;
			} else if (filter.getSearchScope().contains(ResourceSearchScope.WORKSPACE)) {
				projects = Arrays.asList(filter.project.getWorkspace().getRoot().getProjects());
			} else if (filter.getSearchScope().contains(ResourceSearchScope.PROJECT)) {
				if (filter.getSearchScope().contains(ResourceSearchScope.REFERENCES)) {
					projects = new ArrayList<>();
					projects.add(filter.project);
					try {
						for (final IProject ref : filter.project.getReferencedProjects()) {
							projects.add(ref);
						}
					} catch (final CoreException e) {
						e.printStackTrace();
					}
				} else {
					projects = Arrays.asList(filter.project);
				}
			} else {
				return;
			}

			try {
				for (final IProject project : projects) {
					// Only search bundles unless requested
					if (includeNonBundles == false && filter.project.getFile("/META-INF/MANIFEST.MF").exists() == false) { //$NON-NLS-1$
						continue;
					}
					project.accept(resource -> {
						if (callback.cancel) {
							return false;
						}

						if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
							return true;
						} else if (resource.getType() == IResource.FILE && !resource.isLinked()) {
							final String path = resource.getProjectRelativePath().toString();
							if (!matcherBinFolder.match(path)
									&& (matcherGif.match(path) || matcherPng.match(path) || matcherJpg.match(path))) {
								if (E.notEmpty(filter.getPackages())) {
									if (!filter.getPackages().contains(
											resource.getProjectRelativePath().removeLastSegments(1).toOSString())) {
										return false;
									}
								}
								if (E.notEmpty(filter.getLocations())) {
									if (!filter.getLocations().contains(project.getLocation().toOSString())) {
										return false;
									}
								}
								if (E.notEmpty(filter.getBundles())) {
									final String bundle1 = getBundle(project);
									if (bundle1 == null || !filter.getBundles().contains(bundle1)) {
										return false;
									}
								}
								if (!filter.isIncludeNonBundles()) {
									final String bundle2 = getBundle(project);
									if (bundle2 == null) {
										return false;
									}

								}
								callback.match((IFile) resource, project.getLocation().toOSString());
							}
						}
						return false;
					});
				}
			} catch (final CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public String getValue() {
		return returnValue;
	}
}
