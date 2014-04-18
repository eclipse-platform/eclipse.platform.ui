/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 404136
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
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.StringMatcher;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
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

public abstract class AbstractIconDialog extends TitleAreaDialog {
	private TableViewer viewer;
	private IProject project;
	private MApplicationElement element;
	private EStructuralFeature feature;
	private EditingDomain editingDomain;
	private Map<IFile, Image> icons = Collections.synchronizedMap(new HashMap<IFile, Image>());
	private SearchScope searchScope = SearchScope.PROJECT;

	protected Messages Messages;
	private Text textFilter;

	public AbstractIconDialog(Shell parentShell, IProject project, EditingDomain editingDomain, MApplicationElement element, EStructuralFeature feature, Messages Messages) {
		super(parentShell);
		this.editingDomain = editingDomain;
		this.element = element;
		this.feature = feature;
		this.project = project;
		this.Messages = Messages;
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

		Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label lblScope = new Label(container, SWT.NONE);
		lblScope.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		lblScope.setText(Messages.AbstractIconDialog_scope);

		Composite compOptions = new Composite(container, SWT.NONE);
		compOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compOptions.setLayout(new RowLayout());

		Button btnProject = new Button(compOptions, SWT.RADIO);
		btnProject.setText(Messages.AbstractIconDialog_current_project);
		btnProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchScope = SearchScope.PROJECT;
				textFilter.notifyListeners(SWT.Modify, new Event());

			}
		});
		btnProject.setSelection(true);

		Button btnWorkspace = new Button(compOptions, SWT.RADIO);
		btnWorkspace.setText(Messages.AbstractIconDialog_all_workspace_bundles);
		btnWorkspace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchScope = SearchScope.WORKSPACE;
				textFilter.notifyListeners(SWT.Modify, new Event());
			}
		});

		btnWorkspace.setSelection(searchScope == SearchScope.WORKSPACE);
		btnProject.setSelection(searchScope == SearchScope.PROJECT);

		Label l = new Label(container, SWT.NONE);
		l.setText(Messages.AbstractIconDialog_IconName);

		textFilter = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		textFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(container, SWT.NONE);

		viewer = new TableViewer(container);
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

		textFilter.addModifyListener(new ModifyListener() {
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
				task = new SearchThread(callback, textFilter.getText(), project, searchScope);
				timer.schedule(task, 500);
			}
		});

		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				clearImages();
			}
		});

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
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (!s.isEmpty()) {
			IFile file = (IFile) s.getFirstElement();
			String bundle = getBundle(file);
			String uri = "platform:/plugin/" + bundle + "/" + file.getProjectRelativePath().toString(); //$NON-NLS-1$//$NON-NLS-2$
			Command cmd = SetCommand.create(editingDomain, element, feature, uri);
			if (cmd.canExecute()) {
				editingDomain.getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
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
		private SearchScope searchScope;

		public SearchThread(IconMatchCallback callback, String pattern, IProject project, SearchScope searchScope) {
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
					if (project.getFile("/META-INF/MANIFEST.MF").exists() == false) { //$NON-NLS-1$
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
