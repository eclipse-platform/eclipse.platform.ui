/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Mickael Istria (Red Hat Inc.) - 226046 Add filter for user-spec'd patterns
 ******************************************************************************/
package org.eclipse.ui.navigator.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IAggregateWorkingSet;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.filters.UserFilter;
import org.eclipse.ui.internal.navigator.framelist.Frame;
import org.eclipse.ui.internal.navigator.framelist.FrameList;
import org.eclipse.ui.internal.navigator.framelist.TreeFrame;
import org.eclipse.ui.internal.navigator.resources.ResourceToItemsMapper;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.internal.views.helpers.EmptyWorkspaceHelper;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorContentService;


/**
 *
 * @see CommonNavigator
 * @see INavigatorContentService
 * @since 3.2
 *
 */
public final class ProjectExplorer extends CommonNavigator {

	/**
	 * Provides a constant for the standard instance of the Common Navigator.
	 *
	 * @see PlatformUI#getWorkbench()
	 * @see IWorkbench#getActiveWorkbenchWindow()
	 * @see IWorkbenchWindow#getActivePage()
	 *
	 * @see IWorkbenchPage#findView(String)
	 * @see IWorkbenchPage#findViewReference(String)
	 */
	public static final String VIEW_ID = IPageLayout.ID_PROJECT_EXPLORER;

	/**
	 * @since 3.4
	 */
	public static final int WORKING_SETS = 0;

	/**
	 * @since 3.4
	 */
	public static final int PROJECTS = 1;

	private static final String MEMENTO_REGEXP_FILTER_ELEMENT = "regexpFilter"; //$NON-NLS-1$
	private static final String MEMENTO_REGEXP_FILTER_REGEXP_ATTRIBUTE = "regexp"; //$NON-NLS-1$
	private static final String MEMENTO_REGEXP_FILTER_ENABLED_ATTRIBUTE = "enabled"; //$NON-NLS-1$

	private int rootMode;

	/**
	 * Used only in the case of top level = PROJECTS and only when some
	 * working sets are selected.
	 */
	private String workingSetLabel;

	private List<UserFilter> userFilters;
	private EmptyWorkspaceHelper emptyWorkspaceHelper;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		userFilters = new ArrayList<UserFilter>();
		if (memento != null) {
			IMemento[] filters = memento.getChildren(MEMENTO_REGEXP_FILTER_ELEMENT);
			for (IMemento filterMemento : filters) {
				String regexp = filterMemento.getString(MEMENTO_REGEXP_FILTER_REGEXP_ATTRIBUTE);
				Boolean enabled = filterMemento.getBoolean(MEMENTO_REGEXP_FILTER_ENABLED_ATTRIBUTE);
				userFilters.add(new UserFilter(regexp, enabled));
			}
		}
	}

	@Override
	public void saveState(IMemento aMemento) {
		Object data = getCommonViewer().getData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA);
		if (data instanceof Collection) {
			Collection<?> dataAsFilters = (Collection<?>) data;
			for (Object object : dataAsFilters) {
				if (!(object instanceof UserFilter)) {
					continue;
				}
				UserFilter filter = (UserFilter) object;
				IMemento memento = aMemento.createChild(MEMENTO_REGEXP_FILTER_ELEMENT);
				memento.putString(MEMENTO_REGEXP_FILTER_REGEXP_ATTRIBUTE, filter.getRegexp());
				memento.putBoolean(MEMENTO_REGEXP_FILTER_ENABLED_ATTRIBUTE, filter.isEnabled());
			}
		}
		super.saveState(aMemento);
	}

	@Override
	public void createPartControl(Composite aParent) {
		emptyWorkspaceHelper = new EmptyWorkspaceHelper();
		Composite displayAreas = emptyWorkspaceHelper.getComposite(aParent);

		super.createPartControl(displayAreas);
		getCommonViewer().setMapper(new ResourceToItemsMapper(getCommonViewer()));
		getCommonViewer().setData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA, this.userFilters);
		if (this.userFilters.stream().anyMatch(UserFilter::isEnabled)) {
			getCommonViewer().refresh();
		}
	}

	/**
	 * The superclass does not deal with the content description, handle it
	 * here.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public void updateTitle() {
		super.updateTitle();
		Object input = getCommonViewer().getInput();

		if (input == null || input instanceof IAggregateWorkingSet) {
			setContentDescription(""); //$NON-NLS-1$
			return;
		}

		if (!(input instanceof IResource)) {
			String label = ((ILabelProvider) getCommonViewer().getLabelProvider()).getText(input);
			if (label != null) {
				setContentDescription(label);
				return;
			}
			IWorkbenchAdapter wbadapter = Adapters.adapt(input, IWorkbenchAdapter.class);
			if (wbadapter != null) {
				setContentDescription(wbadapter.getLabel(input));
				return;
			}
			setContentDescription(input.toString());
			return;
		}

		IResource res = (IResource) input;
		setContentDescription(res.getName());
	}

	/**
	 * Returns the tool tip text for the given element.
	 *
	 * @param element
	 *            the element
	 * @return the tooltip
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public String getFrameToolTipText(Object element) {
		String result;
		if (!(element instanceof IResource)) {
			if (element instanceof IAggregateWorkingSet) {
				result = WorkbenchNavigatorMessages.ProjectExplorerPart_workingSetModel;
			} else if (element instanceof IWorkingSet) {
				result = ((IWorkingSet) element).getLabel();
			} else {
				result = super.getFrameToolTipText(element);
			}
		} else {
			IPath path = ((IResource) element).getFullPath();
			if (path.isRoot()) {
				result = WorkbenchNavigatorMessages.ProjectExplorerPart_workspace;
			} else {
				result = path.makeRelative().toString();
			}
		}

		if (rootMode == PROJECTS) {
			if (workingSetLabel == null)
				return result;
			if (result.length() == 0)
				return NLS.bind(WorkbenchNavigatorMessages.ProjectExplorer_toolTip,
						new String[] { workingSetLabel });
			return NLS.bind(WorkbenchNavigatorMessages.ProjectExplorer_toolTip2, new String[] {
					result, workingSetLabel });
		}

		// Working set mode. During initialization element and viewer can
		// be null.
		if (element != null && !(element instanceof IWorkingSet)
				&& getCommonViewer() != null) {
			FrameList frameList = getCommonViewer().getFrameList();
			// Happens during initialization
			if (frameList == null)
				return result;
			int index = frameList.getCurrentIndex();
			IWorkingSet ws = null;
			while (index >= 0) {
				Frame frame = frameList.getFrame(index);
				if (frame instanceof TreeFrame) {
					Object input = ((TreeFrame) frame).getInput();
					if (input instanceof IWorkingSet && !(input instanceof IAggregateWorkingSet)) {
						ws = (IWorkingSet) input;
						break;
					}
				}
				index--;
			}
			if (ws != null) {
				return NLS.bind(WorkbenchNavigatorMessages.ProjectExplorer_toolTip3,
						new String[] { ws.getLabel(), result });
			}
			return result;
		}
		return result;

	}

	/**
	 * @param mode
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.4
	 */
	@Override
	public void setRootMode(int mode) {
		rootMode = mode;
	}

	/**
	 * @return the root mode
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.4
	 */
	@Override
	public int getRootMode() {
		return rootMode;
	}

	/**
	 * @param label
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.4
	 */
	@Override
	public void setWorkingSetLabel(String label) {
		workingSetLabel = label;
	}

	/**
	 * @return the working set label
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 3.4
	 */
	@Override
	public String getWorkingSetLabel() {
		return workingSetLabel;
	}

	@Override
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		ICommandService commandService = getViewSite().getService(ICommandService.class);
		Command openProjectCommand = commandService.getCommand(IWorkbenchCommandConstants.PROJECT_OPEN_PROJECT);
		if (openProjectCommand != null && openProjectCommand.isHandled() && openProjectCommand.isEnabled()) {
			IStructuredSelection selection = (IStructuredSelection) anEvent
					.getSelection();
			Object element = selection.getFirstElement();
			if (element instanceof IProject && !((IProject) element).isOpen()) {
				try {
					openProjectCommand.executeWithChecks(new ExecutionEvent());
				} catch (CommandException ex) {
					IStatus status = WorkbenchNavigatorPlugin.createErrorStatus("'Open Project' failed", ex); //$NON-NLS-1$
					WorkbenchNavigatorPlugin.getDefault().getLog().log(status);
				}
				return;
			}
		}
		super.handleDoubleClick(anEvent);
	}

	@Override
	protected CommonViewer createCommonViewer(Composite aParent) {
		CommonViewer viewer = super.createCommonViewer(aParent);
		emptyWorkspaceHelper.setNonEmptyControl(viewer.getControl());
		return viewer;
	}

}
