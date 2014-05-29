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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.ResourceSearchScope;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.StringMatcher;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.E;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Based of of AbstractIconDialog, but passively returns selection instead of
 * modifying model.
 *
 * @author Steven Spungin
 *
 */
public abstract class AbstractIconDialogWithHardcodedScope extends TitleAreaDialog {
	private TableViewer viewer;
	private IProject project;
	private Map<IFile, Image> icons = Collections.synchronizedMap(new HashMap<IFile, Image>());
	private ResourceSearchScope searchScope = ResourceSearchScope.PROJECT;

	protected Messages Messages;
	private Text textSearch;
	private String value;
	private IEclipseContext context;

	public AbstractIconDialogWithHardcodedScope(Shell parentShell, IEclipseContext context) {
		super(parentShell);
		this.context = context;
		this.project = context.get(IProject.class);
		Messages = context.get(Messages.class);
	}

	protected IEclipseContext getContext() {
		return context;
	}

	protected abstract String getShellTitle();

	protected abstract String getDialogTitle();

	protected abstract String getDialogMessage();

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		getShell().setText(getShellTitle());
		setTitle(getDialogTitle());
		setMessage(getDialogMessage());
		String bundleFilter = (String) context.get("bundle");
		if (E.notEmpty(bundleFilter)) {
			setMessage("Filtering by bundle " + bundleFilter); //$NON-NLS-1$
		}

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Composite compOptions = new Composite(container, SWT.NONE);
		compOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		compOptions.setLayout(new RowLayout());

		if (E.isEmpty(bundleFilter)) {
			Button btnProject = new Button(compOptions, SWT.RADIO);
			btnProject.setText("Project");
			btnProject.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchScope = ResourceSearchScope.PROJECT;
					textSearch.notifyListeners(SWT.Modify, new Event());

				}
			});
			btnProject.setSelection(true);

			Button btnWorkspace = new Button(compOptions, SWT.RADIO);
			btnWorkspace.setText("Workspace");
			btnWorkspace.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					searchScope = ResourceSearchScope.WORKSPACE;
					textSearch.notifyListeners(SWT.Modify, new Event());
				}
			});

			btnWorkspace.setSelection(searchScope == ResourceSearchScope.WORKSPACE);
			btnProject.setSelection(searchScope == ResourceSearchScope.PROJECT);
		} else {
			searchScope = ResourceSearchScope.WORKSPACE;
		}

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.AbstractIconDialog_IconName);

		textSearch = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		textSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(container, SWT.NONE);

		viewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		viewer.getControl().setLayoutData(gd);
		viewer.setContentProvider(new ObservableListContentProvider());
		viewer.setLabelProvider(new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				IFile file = (IFile) cell.getElement();
				StyledString styledString = new StyledString(file.getProjectRelativePath().toString(), null);

				Image img = icons.get(file);
				if (img == null) {
					InputStream in = null;
					try {
						in = file.getContents();
						img = new Image(cell.getControl().getDisplay(), in);
						icons.put(file, img);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}

				String bundle = getBundle(file);
				if (bundle != null) {
					styledString.append(" - " + bundle, StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
				}

				cell.setImage(img);
				cell.setText(styledString.getString());
				cell.setStyleRanges(styledString.getStyleRanges());
			}
		});

		final WritableList list = new WritableList();
		viewer.setInput(list);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		textSearch.addModifyListener(new ModifyListener() {
			private IconMatchCallback callback;
			private Timer timer = new Timer(true);
			private TimerTask task;

			@Override
			public void modifyText(ModifyEvent e) {
				if (callback != null) {
					callback.cancel = true;
				}
				if (task != null) {
					task.cancel();
				}
				list.clear();

				clearImages();

				callback = new IconMatchCallback(list);
				task = new SearchThread(callback, textSearch.getText(), project, context, searchScope);
				timer.schedule(task, 500);
			}
		});

		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				clearImages();
			}
		});

		textSearch.notifyListeners(SWT.Modify, new Event());

		return comp;
	}

	private void clearImages() {
		for (Image img : icons.values()) {
			img.dispose();
		}
		icons.clear();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		value = null;
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			IFile file = (IFile) s.getFirstElement();
			String bundle = getBundle(file);
			value = "platform:/plugin/" + bundle + "/" + file.getProjectRelativePath().toString(); //$NON-NLS-1$//$NON-NLS-2$

		}
		super.okPressed();
	}

	private String getBundle(IFile file) {
		IProject project = file.getProject();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (r != null) {
					try {
						r.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public String getValue() {
		return value;
	}

	private class IconMatchCallback {
		private volatile boolean cancel;
		private IObservableList list;

		private IconMatchCallback(IObservableList list) {
			this.list = list;
		}

		public void match(final IFile file) {
			if (!cancel) {
				list.getRealm().exec(new Runnable() {

					@Override
					public void run() {
						list.add(file);
					}
				});
			}
		}
	}

	private static class SearchThread extends TimerTask {
		private final IconMatchCallback callback;
		private final IProject project;
		private final StringMatcher matcherGif;
		private final StringMatcher matcherJpg;
		private final StringMatcher matcherPng;
		private IEclipseContext context;
		private ResourceSearchScope searchScope;

		public SearchThread(IconMatchCallback callback, String pattern, IProject project, IEclipseContext context, ResourceSearchScope searchScope) {
			this.context = context;
			this.matcherGif = new StringMatcher("*" + pattern + "*.gif", true, false); //$NON-NLS-1$//$NON-NLS-2$
			this.matcherJpg = new StringMatcher("*" + pattern + "*.jpg", true, false); //$NON-NLS-1$//$NON-NLS-2$
			this.matcherPng = new StringMatcher("*" + pattern + "*.png", true, false); //$NON-NLS-1$//$NON-NLS-2$
			this.callback = callback;
			this.project = project;
			this.searchScope = searchScope;
		}

		@Override
		public void run() {
			List<IProject> projects;
			switch (searchScope) {
			case WORKSPACE:
				projects = Arrays.asList(project.getWorkspace().getRoot().getProjects());
				break;
			case PROJECT:
			default:
				projects = Arrays.asList(project);
				break;
			}
			try {
				for (IProject project : projects) {
					// Only search bundles
					IFile iFile = project.getFile("/META-INF/MANIFEST.MF"); //$NON-NLS-1$
					if (iFile.exists() == false) {
						continue;
					}
					// Only search target bundle if specified
					String bundle = (String) context.get("bundle");
					if (E.notEmpty(bundle)) {
						InputStream inputStream = null;
						try {
							inputStream = iFile.getContents();
							Properties props = new Properties();
							props.load(inputStream);
							String name = props.getProperty("Bundle-SymbolicName"); //$NON-NLS-1$
							String[] parts = name.split(";"); //$NON-NLS-1$
							if (parts[0].equals(bundle) == false) {
								continue;
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							try {
								inputStream.close();
							} catch (Exception ex) {
							}
						}
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
									callback.match((IFile) resource);
								}
							}
							return false;
						}

					});
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
