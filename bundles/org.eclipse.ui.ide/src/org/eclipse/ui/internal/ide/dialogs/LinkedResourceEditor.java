/*******************************************************************************
 * Copyright (c) 2010, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 490700
 ******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * UI to edit the location of the linked resources contained in a project.
 * @since 3.5
 */
public class LinkedResourceEditor {


	private static int NAME_COLUMN = 0;
	private static int PATH_COLUMN = -1;
	private static int LOCATION_COLUMN = 1;

	// sizing constants
	private static final int SIZING_SELECTION_PANE_WIDTH = 400;

	// used to compute layout sizes
	private FontMetrics fontMetrics;

	public LinkedResourceEditor() {
		absoluteImg = IDEWorkbenchPlugin.getIDEImageDescriptor(
				"obj16/warning.png").createImage(); //$NON-NLS-1$
		brokenImg = IDEWorkbenchPlugin.getIDEImageDescriptor(
				"obj16/error_tsk.png").createImage(); //$NON-NLS-1$
		fixedImg = IDEWorkbenchPlugin
				.getIDEImageDescriptor("obj16/folder.png").createImage(); //$NON-NLS-1$

		FIXED = IDEWorkbenchMessages.LinkedResourceEditor_fixed;
		BROKEN = IDEWorkbenchMessages.LinkedResourceEditor_broken;
		ABSOLUTE = IDEWorkbenchMessages.LinkedResourceEditor_absolute;
	}

	public void setProject(IProject project) {
		fProject = project;
	}

	protected void createButtons(Composite parent) {
		Font font = parent.getFont();
		Composite groupComponent = new Composite(parent, SWT.NULL);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);
		groupComponent.setFont(font);

		fEditResourceButton = createButton(groupComponent,
				IDEWorkbenchMessages.LinkedResourceEditor_editLinkedLocation);
		fEditResourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editLocation();
			}
		});

		fConvertAbsoluteButton = createButton(groupComponent,
				IDEWorkbenchMessages.LinkedResourceEditor_convertToVariableLocation);
		fConvertAbsoluteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				convertLocation();
			}
		});
		fRemoveButton = createButton(groupComponent,
				IDEWorkbenchMessages.LinkedResourceEditor_remove);
		fRemoveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeSelection();
			}
		});

		updateSelection();
	}

	/**
	 * @return the new button
	 */
	private Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(text);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		return button;
	}

	/**
	 * Creates the widget group. Callers must call <code>dispose</code> when the
	 * group is no longer needed.
	 *
	 * @param parent
	 *            the widget parent
	 * @return container of the widgets
	 */
	public Control createContents(Composite parent) {
		Font font = parent.getFont();

		initializeDialogUnits(parent);

		// define container & its layout
		Composite pageComponent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		pageComponent.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_SELECTION_PANE_WIDTH;
		pageComponent.setLayoutData(data);
		pageComponent.setFont(font);

		// layout the table & its buttons
		Label variableLabel = new Label(pageComponent, SWT.LEFT);
		variableLabel.setText(NLS
				.bind(IDEWorkbenchMessages.LinkedResourceEditor_descriptionBlock,
				fProject != null ? fProject.getName() : "")); //$NON-NLS-1$

		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		variableLabel.setLayoutData(data);
		variableLabel.setFont(font);

		Composite treeComposite = new Composite(pageComponent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		treeComposite.setLayoutData(data);

		fTree = new TreeViewer(treeComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		fTree.addSelectionChangedListener(event -> updateSelection());

		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = fTree.getTree().getItemHeight() * 10;
		data.horizontalSpan = 1;
		fTree.getTree().setLayoutData(data);
		fTree.getTree().setLinesVisible(true);

		fTree.setContentProvider(new ContentProvider());
		fTree.setLabelProvider(new LabelProvider());
		fTree.setInput(this);

		TreeColumn nameColumn = new TreeColumn(fTree.getTree(), SWT.LEFT, NAME_COLUMN);
		nameColumn.setText(IDEWorkbenchMessages.LinkedResourceEditor_resourceName);
		nameColumn.setResizable(true);
		nameColumn.setMoveable(false);

		TreeColumn locationColumn = new TreeColumn(fTree.getTree(), SWT.LEFT, LOCATION_COLUMN);
		locationColumn.setText(IDEWorkbenchMessages.LinkedResourceEditor_location);
		locationColumn.setResizable(true);
		locationColumn.setMoveable(false);

		TreeColumnLayout tableLayout = new TreeColumnLayout();
		treeComposite.setLayout( tableLayout );

		tableLayout.setColumnData(nameColumn, new ColumnWeightData(170));
		tableLayout.setColumnData(locationColumn, new ColumnWeightData(260));

		fTree.getTree().setFont(font);
		fTree.getTree().setHeaderVisible(true);
		createButtons(pageComponent);

		fTree.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (getSelectedResource().length == 1)
					editLocation();
			}
		});
		fTree.getTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					e.doit = false;
					if (getSelectedResource().length > 0)
						removeSelection();
				}
			}
		});

		return pageComponent;
	}

	private void initializeDialogUnits(Control control) {
		// Compute and store a font metric
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
	}

	public void dispose() {
		fixedImg.dispose();
		brokenImg.dispose();
		absoluteImg.dispose();
	}

	class LabelProvider implements ILabelProvider,
			ITableLabelProvider {

		WorkbenchLabelProvider stockProvider = new WorkbenchLabelProvider();
		public LabelProvider() {
			super();
		}

		@Override
		public String getColumnText(Object obj, int index) {
			if (obj instanceof IResource) {
				IResource resource = (IResource) obj;
				if (index == NAME_COLUMN)
					return resource.getName();
				else if (index == PATH_COLUMN)
					return resource.getParent()
							.getProjectRelativePath().toPortableString();
				else {
					IPath rawLocation = resource.getRawLocation();
					if (rawLocation != null)
						return resource.getPathVariableManager().convertToUserEditableFormat(rawLocation.toOSString(), true);
				}
			} else if ((obj instanceof String) && index == 0)
				return (String) obj;
			return null;
		}

		@Override
		public Image getColumnImage(Object obj, int index) {
			if (index == NAME_COLUMN) {
				if (obj instanceof String) {
					if (obj.equals(BROKEN))
						return brokenImg;
					if (obj.equals(ABSOLUTE))
						return absoluteImg;
					return fixedImg;
				}
				return stockProvider.getImage(obj);
			}
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
			stockProvider.dispose();
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return stockProvider.isLabelProperty(element, property);
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

		@Override
		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
	}

	class ContentProvider implements IContentProvider, ITreeContentProvider {

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof LinkedResourceEditor) {
				ArrayList<Object> list = new ArrayList<>();
				Object[] objs = { BROKEN, ABSOLUTE, FIXED };
				for (Object obj : objs) {
					Object[] children = getChildren(obj);
					if (children != null && children.length > 0)
						list.add(obj);
				}
				return list.toArray(new Object[0]);
			} else if (parentElement instanceof String) {
				if (((String) parentElement).equals(BROKEN))
					return fBrokenResources.values().toArray();
				if (((String) parentElement).equals(ABSOLUTE))
					return fAbsoluteResources.values().toArray();
				return fFixedResources.values().toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IResource) {
				String fullPath = ((IResource) element).getFullPath()
						.toPortableString();
				if (fBrokenResources.containsKey(fullPath))
					return BROKEN;
				if (fAbsoluteResources.containsKey(fullPath))
					return ABSOLUTE;
				return FIXED;
			} else if (element instanceof String)
				return this;
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof LinkedResourceEditor) {
				return true;
			} else if (element instanceof String) {
				if (((String) element).equals(BROKEN))
					return !fBrokenResources.isEmpty();
				if (((String) element).equals(ABSOLUTE))
					return !fAbsoluteResources.isEmpty();
				return !fFixedResources.isEmpty();
			}
			return false;
		}

		@Override
		public Object[] getElements(Object parentElement) {
			return getChildren(parentElement);
		}
	}

	void refreshContent() {
		IResource[] projectFiles;
		if (!initialized) {
			final LinkedList<IResource> resources = new LinkedList<>();
			try {
				fProject.accept(resource -> {
					if (resource.isLinked() && !resource.isVirtual())
						resources.add(resource);
					return true;
				});
			} catch (CoreException e) {
			}
			projectFiles = resources.toArray(new IResource[0]);
			initialized = true;
		}
		else {
			ArrayList<IResource> list = new ArrayList<>();
			list.addAll(fBrokenResources.values());
			list.addAll(fFixedResources.values());
			list.addAll(fAbsoluteResources.values());
			projectFiles = list.toArray(new IResource[0]);
		}
		fBrokenResources = new TreeMap<>();
		fFixedResources = new TreeMap<>();
		fAbsoluteResources = new TreeMap<>();
		for (IResource resource : projectFiles) {
			String fullPath = resource.getFullPath().toPortableString();
			try {
				if (exists(resource)) {
					if (isAbsolute(resource))
						fAbsoluteResources.put(fullPath, resource);
					else
						fFixedResources.put(fullPath, resource);
				} else
					fBrokenResources.put(fullPath, resource);
			} catch (CoreException e) {
				fBrokenResources.put(fullPath, resource);
			}
		}
	}

	boolean isAbsolute(IResource res) {
		IPath path = res.getRawLocation();
		return path != null && path.isAbsolute();
	}

	boolean areAbsolute(IResource[] res) {
		for (IResource re : res) {
			if (!isAbsolute(re)) {
				return false;
			}
		}
		return true;
	}

	boolean exists(IResource res) throws CoreException {
		URI uri = res.getLocationURI();
		if (uri != null) {
			IFileStore fileStore = EFS.getStore(uri);
			return (fileStore != null) && fileStore.fetchInfo().exists();
		}
		return false;
	}

	void updateSelection() {
		fEditResourceButton.setEnabled(getSelectedResource().length == 1);
		fConvertAbsoluteButton.setEnabled((getSelectedResource().length > 0)
				&& (areAbsolute(getSelectedResource())
				|| areFixed(getSelectedResource())));
		fRemoveButton.setEnabled(getSelectedResource().length > 0);
	}

	boolean areFixed(IResource[] res) {
		for (IResource resource : res) {
			String fullPath = resource.getFullPath().toPortableString();
			if (!fFixedResources.containsKey(fullPath))
				return false;
		}
		return true;
	}

	IResource[] getSelectedResource() {
		IStructuredSelection selection = fTree.getStructuredSelection();
		Object[] array = selection.toArray();
		if (array.length > 0) {
		    for (Object array1 : array) {
			if (!(array1 instanceof IResource)) {
			    return new IResource[0];
			}
		    }
			IResource[] result = new IResource[array.length];
			System.arraycopy(array, 0, result, 0, array.length);
			return result;
		}
		return new IResource[0];
	}

	private void convertLocation() {
		if (MessageDialog.openConfirm(fConvertAbsoluteButton.getShell(),
				IDEWorkbenchMessages.LinkedResourceEditor_convertTitle,
				IDEWorkbenchMessages.LinkedResourceEditor_convertMessage)) {
			ArrayList<IResource> resources = new ArrayList<>();
			IResource[] selectedResources = getSelectedResource();
			resources.addAll(Arrays.asList(selectedResources));
			if (areFixed(selectedResources))
				convertToAbsolute(resources, selectedResources);
			else
				convertToRelative(resources, selectedResources);
		}
	}

	private void removeSelection() {
		if (MessageDialog.openConfirm(fRemoveButton.getShell(),
				IDEWorkbenchMessages.LinkedResourceEditor_removeTitle,
				IDEWorkbenchMessages.LinkedResourceEditor_removeMessage)) {
			final IResource[] selectedResources = getSelectedResource();
			final ArrayList<IResource> removedResources = new ArrayList<>();

			IRunnableWithProgress op = monitor -> {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						IDEWorkbenchMessages.LinkedResourceEditor_removingMessage, selectedResources.length);
				for (IResource selectedResource : selectedResources) {
					String fullPath = selectedResource.getFullPath().toPortableString();
					try {
						selectedResource.delete(true, subMonitor.split(1));
						removedResources.add(selectedResource);
						fBrokenResources.remove(fullPath);
						fFixedResources.remove(fullPath);
						fAbsoluteResources.remove(fullPath);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			};
			try {
				new ProgressMonitorDialog(fRemoveButton.getShell()).run(true,
						true, op);
			} catch (InvocationTargetException | InterruptedException e) {
				IDEWorkbenchPlugin.log(null, e);
			}
			fTree.refresh();
		}
	}

	private void convertToAbsolute(ArrayList<IResource> resources,
			IResource[] selectedResources) {
		ArrayList<String> report = new ArrayList<>();

		Iterator<IResource> it = resources.iterator();
		while (it.hasNext()) {
			IResource res = it.next();
			IPath location = res.getLocation();

			try {
				setLinkLocation(res, location);
				report.add(NLS.bind(IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
						new Object[] { res.getProjectRelativePath().toPortableString(),
								res.getRawLocation().toOSString(), location.toOSString() }));
			} catch (CoreException e) {
				report.add(NLS.bind(IDEWorkbenchMessages.LinkedResourceEditor_unableToSetLinkLocationForResource,
						res.getProjectRelativePath().toPortableString()));
			}
		}

		reportResult(
				selectedResources,
				report,
				IDEWorkbenchMessages.LinkedResourceEditor_convertRelativePathLocations);
	}

	private void setLinkLocation(IResource res, IPath location) throws CoreException {
		if (res.getType() == IResource.FILE)
			((IFile)res).createLink(location, IResource.REPLACE,
					new NullProgressMonitor());
		if (res.getType() == IResource.FOLDER)
			((IFolder)res).createLink(location, IResource.REPLACE,
					new NullProgressMonitor());
	}

	private void reportResult(IResource[] selectedResources,
			ArrayList<String> report, String title) {
		StringBuilder message = new StringBuilder();
		Iterator<String> stringIt = report.iterator();
		while (stringIt.hasNext()) {
			message.append(stringIt.next());
			if (stringIt.hasNext())
				message.append("\n"); //$NON-NLS-1$
		}
		final String resultMessage = message.toString();
		MessageDialog dialog = new MessageDialog(fConvertAbsoluteButton.getShell(), title, null,
				IDEWorkbenchMessages.LinkedResourceEditor_convertionResults, MessageDialog.INFORMATION, 0,
				IDEWorkbenchMessages.linkedResourceEditor_OK) {

			@Override
			protected boolean isResizable() {
				return true;
			}

			@Override
			protected Control createCustomArea(Composite comp) {
				setShellStyle(getShellStyle() | SWT.RESIZE);
				Composite parent = new Composite(comp, 0);
				GridLayout layout = new GridLayout();
				layout.marginHeight = 0;
				layout.marginWidth = 0;
				layout.marginLeft = 2;
				layout.numColumns = 1;
				layout.verticalSpacing = 9;
				parent.setLayout(layout);
				parent.setLayoutData(new GridData(GridData.FILL_BOTH));

				Text text = new Text(parent, SWT.BORDER | SWT.MULTI
						| SWT.V_SCROLL | SWT.H_SCROLL);
				text.setText(resultMessage);
				GridData data = new GridData(640, 300);
				text.setLayoutData(data);
				return parent;
			}

		};
		dialog.open();
		reparent(selectedResources);
	}

	private IPath convertToProperCase(IPath path) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			return IPath.fromPortableString(path.toPortableString().toLowerCase());
		}
		return path;
	}

	private void convertToRelative(ArrayList<IResource> resources,
			IResource[] selectedResources) {
		ArrayList<String> report = new ArrayList<>();

		// first, try to use the automatic converter
		ArrayList<IResource> remaining = new ArrayList<>();
		Iterator<IResource> it = resources.iterator();
		while (it.hasNext()) {
			IResource res = it.next();
			IPath location = res.getLocation();
			try {
				IPath newLocation = URIUtil.toPath(res.getPathVariableManager().convertToRelative(URIUtil.toURI(location), true, null));
				if (newLocation == null || newLocation.equals(location))
					remaining.add(res);
				else {
					setLinkLocation(res, newLocation);
					report.add(NLS.bind(IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
							new Object[] { res.getProjectRelativePath().toPortableString(), location.toOSString(),
									newLocation.toOSString() }));
				}
			} catch (CoreException e) {
				remaining.add(res);
			}
		}
		resources = remaining;
		remaining = new ArrayList<>();
		// try for each to match with an existing variable
		String[] variables = fProject.getPathVariableManager()
				.getPathVariableNames();

		it = resources.iterator();
		int amountLeft = 0;
		while (it.hasNext()) {
			IResource res = it.next();
			IPath location = res.getLocation();

			int maxCount = 0;
			int variable = -1;
			for (int i = 0; i < variables.length; i++) {
				IPath resolvePath = URIUtil.toPath(res.getPathVariableManager().resolveURI(
						URIUtil.toURI(IPath.fromOSString(variables[i]))));
				if (resolvePath
						.isPrefixOf(convertToProperCase(location))) {
					int count = location
							.matchingFirstSegments(resolvePath);
					if (count > maxCount) {
						maxCount = count;
						variable = i;
					}
				}
			}
			if (variable != -1) {
				IPath newLocation = IPath.fromOSString(variables[variable])
						.append(location.removeFirstSegments(maxCount));
				try {
					setLinkLocation(res, newLocation);
					report
							.add(NLS
									.bind(
											IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
											new Object[] {
													res
															.getProjectRelativePath()
															.toPortableString(),
													location.toOSString(),
													newLocation
															.toOSString() }));
				} catch (CoreException e) {
					variable = -1;
				}
			}

			if (variable == -1) {
				amountLeft++;
				remaining.add(res);
			}
		}
		resources = remaining;

		if (amountLeft > 1) {
			// try to generate a generic variable
			it = resources.iterator();
			IPath commonPath = null;
			while (it.hasNext()) {
				IResource res = it.next();
				IPath location = res.getLocation();

				if (commonPath == null)
					commonPath = location;
				else {
					int count = commonPath.matchingFirstSegments(location);
					if (count < commonPath.segmentCount())
						commonPath = commonPath.removeLastSegments(commonPath
								.segmentCount()
								- count);
				}
				if (commonPath.segmentCount() == 0)
					break;
			}
			if (commonPath.segmentCount() > 1) {
				String variableName = getSuitablePathVariable(commonPath);
				try {
					fProject.getPathVariableManager().setURIValue(variableName,
							URIUtil.toURI(commonPath));
				} catch (CoreException e) {
					report
							.add(NLS
									.bind(
											IDEWorkbenchMessages.LinkedResourceEditor_unableToCreateVariable,
											variableName, commonPath
													.toOSString()));
				}
				it = resources.iterator();
				while (it.hasNext()) {
					IResource res = it.next();
					IPath location = res.getLocation();
					int commonCount = location
							.matchingFirstSegments(commonPath);
					IPath newLocation = IPath.fromOSString(variableName)
							.append(location.removeFirstSegments(commonCount));
					try {
						setLinkLocation(res, newLocation);
						report
								.add(NLS
										.bind(
												IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
												new Object[] {
														res
																.getProjectRelativePath()
																.toPortableString(),
														location
																.toOSString(),
														newLocation
																.toOSString() }));
					} catch (CoreException e) {
						report
								.add(NLS
										.bind(
												IDEWorkbenchMessages.LinkedResourceEditor_unableToSetLinkLocationForResource,
												res.getProjectRelativePath()
														.toPortableString()));
					}
				}
			} else {
				report
						.add(IDEWorkbenchMessages.LinkedResourceEditor_unableToFindCommonPathSegments);
				it = resources.iterator();
				while (it.hasNext()) {
					IResource res = it.next();
					report.add(res.getProjectRelativePath().toPortableString());
				}
			}
		} else if (!resources.isEmpty()) {
			IResource res = resources.get(0);
			IPath resLocation = res.getLocation();
			IPath commonPath = resLocation.removeLastSegments(1);
			String variableName = getSuitablePathVariable(commonPath);
			try {
				fProject.getPathVariableManager().setURIValue(variableName,
						URIUtil.toURI(commonPath));
			} catch (CoreException e) {
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_unableToCreateVariable,
										variableName, commonPath
												.toPortableString()));
			}
			IPath location = res.getLocation();
			int commonCount = location.matchingFirstSegments(commonPath);
			IPath newLocation = IPath.fromOSString(variableName).append(location.removeFirstSegments(commonCount));
			try {
				setLinkLocation(res, newLocation);
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_changedTo,
										new Object[] {
												res.getProjectRelativePath()
														.toPortableString(),
												location.toOSString(),
												newLocation.toOSString() }));
			} catch (CoreException e) {
				report
						.add(NLS
								.bind(
										IDEWorkbenchMessages.LinkedResourceEditor_unableToSetLinkLocationForResource,
										res.getProjectRelativePath()
												.toPortableString()));
			}
		}
		reportResult(
				selectedResources,
				report,
				IDEWorkbenchMessages.LinkedResourceEditor_convertAbsolutePathLocations);
	}

	private String getSuitablePathVariable(IPath commonPath) {
		String variableName = commonPath.lastSegment();
		if (variableName == null) {
			variableName = commonPath.getDevice();
			if (variableName == null)
				variableName = "ROOT"; //$NON-NLS-1$
			else
				variableName = variableName.substring(0, variableName.length() -1); // remove the tailing ':'
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < variableName.length(); i++) {
			char c = variableName.charAt(i);
			if (Character.isLetterOrDigit(c) || (c == '_'))
				buf.append(c);
			else
				buf.append('_');

		}
		int index = 1;
		while (fProject.getPathVariableManager().isDefined(buf.toString())) {
			buf.append(index);
			index++;
		}
		return buf.toString();
	}

	void editLocation() {
		IResource resource = getSelectedResource()[0];

		IPath location = resource.getRawLocation();

		PathVariableDialog dialog = new PathVariableDialog(
				fConvertAbsoluteButton.getShell(),
				PathVariableDialog.EDIT_LINK_LOCATION, resource.getType(),
				resource.getPathVariableManager(), null);
		if (location != null)
			dialog.setLinkLocation(location);
		dialog.setResource(resource);
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		location = IPath.fromOSString(dialog.getVariableValue());
		try {
			setLinkLocation(resource, location);
		} catch (Exception e) {
			IDEWorkbenchPlugin.log(e.getMessage(), e);
		}
		reparent(new IResource[] { resource });
	}

	void reparent(IResource[] resources) {
		boolean changed = false;

		for (IResource resource : resources) {
			boolean isBroken;
			try {
				isBroken = !exists(resource);
			} catch (CoreException e) {
				isBroken = true;
			}
			TreeMap<String, IResource> container = null;
			if (isBroken)
				container = fBrokenResources;
			else if (isAbsolute(resource))
				container = fAbsoluteResources;
			else
				container = fFixedResources;
			String fullPath = resource.getFullPath().toPortableString();

			if (!container.containsKey(fullPath)) {
				fBrokenResources.remove(fullPath);
				fAbsoluteResources.remove(fullPath);
				fFixedResources.remove(fullPath);

				container.put(fullPath, resource);
				changed = true;
			}
		}
		if (changed)
			fTree.refresh();
	}

	boolean initialized = false;
	TreeMap<String, IResource> fBrokenResources = new TreeMap<>();
	TreeMap<String, IResource> fAbsoluteResources = new TreeMap<>();
	TreeMap<String, IResource> fFixedResources = new TreeMap<>();

	IProject fProject;
	TreeViewer fTree;
	Button fEditResourceButton;
	Button fConvertAbsoluteButton;
	Button fRemoveButton;

	Image fixedImg = null;
	Image brokenImg = null;
	Image absoluteImg = null;

	String FIXED;
	String BROKEN;
	String ABSOLUTE;

	/**
	 * @return true
	 */
	public boolean performOk() {
		return true;
	}

	void setEnabled(boolean enableLinking) {
	}

	public void reloadContent() {
		refreshContent();
		fTree.refresh();
		updateSelection();
		fTree.expandAll();
	}
}
