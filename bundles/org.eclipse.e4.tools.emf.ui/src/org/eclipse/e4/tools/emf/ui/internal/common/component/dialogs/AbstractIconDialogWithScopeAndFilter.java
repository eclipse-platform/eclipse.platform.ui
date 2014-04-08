/*******************************************************************************

 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 404136, 424730
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.io.IOException;
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
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.FilterEx;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.common.ResourceSearchScope;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.StringMatcher;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformIconContributionCollector;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * A FilteredContributionDialog with additional options for icon resources.
 * Features in include
 * <ul>
 * <li>Rebuilding the viewer when row height decreases. (Workaround for an SWT
 * limitation)
 * <li>Icon previews are displace in the first column.
 * <li>Limited the maximum image size.
 * </ul>
 *
 * @author "Steven Spungin"
 *
 * @see @FilteredContributionDialog
 */
public abstract class AbstractIconDialogWithScopeAndFilter extends FilteredContributionDialog {
	private IProject project;
	private Map<IFile, Image> icons = Collections.synchronizedMap(new HashMap<IFile, Image>());

	static public class Entry {
		IFile file;
		String installLocation;
	}

	protected Integer maxDisplayedImageSize;
	protected int maxImageHeight;
	public static TargetPlatformIconContributionCollector collector;

	public AbstractIconDialogWithScopeAndFilter(Shell parentShell, IEclipseContext context) {
		super(parentShell, context);
		this.project = context.get(IProject.class);
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

		Label lblMaxSize = new Label(compOptions, SWT.NONE);
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

		cv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				maxDisplayedImageSize = (Integer) ((IStructuredSelection) cv.getSelection()).getFirstElement();
				rebuildViewer();
				refreshSearch();
				// combo viewer cannot make rows smaller, so we will need to
				// rebuild it in that case.
			}
		});
	}

	@Override
	protected void rebuildViewer() {
		super.rebuildViewer();
		getViewer().getTable().setHeaderVisible(true);

		((GridData) getViewer().getTable().getLayoutData()).minimumHeight = 100;

		TableViewerColumn colIcon = new TableViewerColumn(getViewer(), SWT.NONE);
		colIcon.getColumn().setText(Messages.AbstractIconDialogWithScopeAndFilter_icon);
		TableViewerColumn colText = new TableViewerColumn(getViewer(), SWT.NONE);
		colText.getColumn().setText(Messages.AbstractIconDialogWithScopeAndFilter_details);

		// resize the row height using a MeasureItem listener
		getViewer().getTable().addListener(SWT.MeasureItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// height cannot be per row so simply set
				event.height = maxDisplayedImageSize;
			}
		});

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
					InputStream in = null;
					try {
						in = file.getContents();
						img = new Image(cell.getControl().getDisplay(), in);
						icons.put(file, img);
					} catch (Exception e) {
						// e.printStackTrace();
						return;
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								// e.printStackTrace();
							}
						}
					}
				}

				// scale image if larger then max height
				// also remember max width for column resizing
				if (img != null) {
					double scale1 = ((double) maxDisplayedImageSize) / img.getImageData().height;
					double scale2 = ((double) maxDisplayedImageSize) / img.getImageData().width;
					if (scale2 < scale1) {
						scale1 = scale2;
					}
					if (scale1 < 1) {
						int width = (int) (img.getImageData().width * scale1);
						if (width == 0)
							width = 1;
						int height = (int) (img.getImageData().height * scale1);
						if (height == 0)
							height = 1;
						Image img2 = new Image(img.getDevice(), img.getImageData().scaledTo(width, height));
						img.dispose();
						img = img2;
						icons.put(file, img);
					}
					int width = AbstractIconDialogWithScopeAndFilter.this.getViewer().getTable().getColumn(0).getWidth();
					if (img.getImageData().width > width) {
						AbstractIconDialogWithScopeAndFilter.this.getViewer().getTable().getColumn(0).setWidth(img.getImageData().width);
					}
					int height = img.getImageData().height;
					if (height > maxImageHeight) {
						maxImageHeight = height;
					}
				}

				cell.setImage(img);
			}
		});
		colIcon.getColumn().setWidth(30);

		colText.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				IFile file;
				String installLocation = null;
				if (cell.getElement() instanceof ContributionData) {
					ContributionData contributionData = (ContributionData) cell.getElement();
					file = new ContributionDataFile(contributionData);
					installLocation = contributionData.installLocation;
				} else if (cell.getElement() instanceof Entry) {
					file = ((Entry) cell.getElement()).file;
					installLocation = ((Entry) cell.getElement()).installLocation;
				} else {
					file = (IFile) cell.getElement();
				}
				StyledString styledString = new StyledString(file.getProjectRelativePath().toString(), null);
				String bundle = FilteredContributionDialog.getBundle(file);
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

		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				clearImages();
			}
		});
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
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
		} else {
			Timer timer = new Timer(true);

			if (callback != null) {
				callback.cancel = true;
			}
			if (task != null) {
				task.cancel();
			}
			task = null;

			clearImages();

			callback = new IconMatchCallback((IObservableList) getViewer().getInput());
			FilterEx filter = new FilterEx(project, getFilterTextBox().getText());
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
	}

	private void clearImages() {
		for (Image img : icons.values()) {
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
				ContributionDataFile cdf = (ContributionDataFile) file;
				installLocation = cdf.getContributionData().installLocation;

			}
			file = checkResourceAccessible(file, installLocation);
			if (file != null) {
				String bundle = getBundle(file);
				String uri;
				uri = "platform:/plugin/" + bundle + "/" + file.getProjectRelativePath().toString(); //$NON-NLS-1$ //$NON-NLS-2$
				returnValue = uri;
				super.okPressed();
			}
		}
	}

	private class IconMatchCallback {
		private volatile boolean cancel;
		private IObservableList list;

		private IconMatchCallback(IObservableList list) {
			this.list = list;
		}

		public void match(final IFile file, final String installLocation) {
			if (!cancel) {
				list.getRealm().exec(new Runnable() {

					@Override
					public void run() {
						Entry entry = new Entry();
						entry.file = file;
						entry.installLocation = installLocation;
						list.add(entry);
					}
				});
			}
		}
	}

	private static class SearchThread extends TimerTask {
		private final IconMatchCallback callback;
		private final StringMatcher matcherGif;
		private final StringMatcher matcherJpg;
		private final StringMatcher matcherPng;
		private FilterEx filter;
		private boolean includeNonBundles;

		public SearchThread(IconMatchCallback callback, FilterEx filter) {
			this.matcherGif = new StringMatcher("*" + filter.namePattern + "*.gif", true, false); //$NON-NLS-1$//$NON-NLS-2$
			this.matcherJpg = new StringMatcher("*" + filter.namePattern + "*.jpg", true, false); //$NON-NLS-1$//$NON-NLS-2$
			this.matcherPng = new StringMatcher("*" + filter.namePattern + "*.png", true, false); //$NON-NLS-1$//$NON-NLS-2$
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
					projects = new ArrayList<IProject>();
					projects.add(filter.project);
					try {
						for (IProject ref : filter.project.getReferencedProjects()) {
							projects.add(ref);
						}
					} catch (CoreException e) {
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
					project.accept(new IResourceVisitor() {

						@Override
						public boolean visit(IResource resource) throws CoreException {
							if (callback.cancel) {
								return false;
							}

							if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
								return true;
							} else if (resource.getType() == IResource.FILE && !resource.isLinked()) {
								String path = resource.getProjectRelativePath().toString();
								if (matcherGif.match(path) || matcherPng.match(path) || matcherJpg.match(path)) {
									if (E.notEmpty(filter.getPackages())) {
										if (!filter.getPackages().contains(resource.getProjectRelativePath().removeLastSegments(1).toOSString())) {
											return false;
										}
									}
									if (E.notEmpty(filter.getLocations())) {
										if (!filter.getLocations().contains(project.getLocation().toOSString())) {
											return false;
										}
									}
									if (E.notEmpty(filter.getBundles())) {
										String bundle = getBundle(project);
										if (bundle == null || !filter.getBundles().contains(bundle)) {
											return false;
										}
									}
									if (!filter.isIncludeNonBundles()) {
										String bundle = getBundle(project);
										if (bundle == null) {
											return false;
										}

									}
									callback.match((IFile) resource, project.getLocation().toOSString());
								}
							}
							return false;
						}

					});
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public String getValue() {
		return returnValue;
	}
}
