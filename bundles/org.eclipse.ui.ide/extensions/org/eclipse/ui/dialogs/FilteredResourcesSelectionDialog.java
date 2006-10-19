/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.model.ResourceFactory;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.icu.text.Collator;

/**
 * Shows a list of resources to the user with a text entry field for a string
 * pattern used to filter the list of resources.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public class FilteredResourcesSelectionDialog extends
		FilteredItemsSelectionDialog {

	private static final String DIALOG_SETTINGS = "org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog"; //$NON-NLS-1$

	private static final String WORKINGS_SET_SETTINGS = "WorkingSet"; //$NON-NLS-1$

	private static final String SHOW_DERIVED = "ShowDerived"; //$NON-NLS-1$

	private ShowDerivedResourcesAction showDerivedResourcesAction;

	private ResourceItemLabelProvider resourceItemLabelProvider;

	private DecoratingLabelProvider detailsLabelProvider;

	private WorkingSetFilterActionGroup workingSetFilterActionGroup;

	private CustomWorkingSetFilter workingSetFilter = new CustomWorkingSetFilter();

	private String title;

	private IContainer container;

	private int typeMask;

	private boolean isDerived;

	/**
	 * Creates a new instance of the class
	 * 
	 * @param shell
	 *            the parent shell
	 * @param multi
	 *            the multi selection flag
	 * @param container
	 *            the container
	 * @param typesMask
	 *            the types mask
	 */
	public FilteredResourcesSelectionDialog(Shell shell, boolean multi,
			IContainer container, int typesMask) {
		super(shell, multi);

		setTitle(IDEWorkbenchMessages.OpenResourceDialog_title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.OPEN_RESOURCE_DIALOG);

		this.container = container;
		this.typeMask = typesMask;

		resourceItemLabelProvider = new ResourceItemLabelProvider();

		detailsLabelProvider = new DecoratingLabelProvider(
				new CustomWorkbenchLabelProvider(), PlatformUI.getWorkbench()
						.getDecoratorManager().getLabelDecorator());

		setListLabelProvider(resourceItemLabelProvider);
		setDetailsLabelProvider(detailsLabelProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.SelectionDialog#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		super.setTitle(title);
		this.title = title;
	}

	/**
	 * Adds or replaces subtitle of the dialog
	 * 
	 * @param text
	 */
	private void setSubtitle(String text) {
		if (text == null || text.length() == 0) {
			getShell().setText(title);
		} else {
			getShell().setText(title + " - " + text); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = IDEWorkbenchPlugin.getDefault()
				.getDialogSettings().getSection(DIALOG_SETTINGS);

		if (settings == null) {
			settings = IDEWorkbenchPlugin.getDefault().getDialogSettings()
					.addNewSection(DIALOG_SETTINGS);
		}

		return settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#storeDialog(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	protected void storeDialog(IDialogSettings settings) {
		super.storeDialog(settings);

		settings.put(SHOW_DERIVED, showDerivedResourcesAction.isChecked());

		XMLMemento memento = XMLMemento.createWriteRoot("workingSet"); //$NON-NLS-1$
		workingSetFilterActionGroup.saveState(memento);
		workingSetFilterActionGroup.dispose();
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
			settings.put(WORKINGS_SET_SETTINGS, writer.getBuffer().toString());
		} catch (IOException e) {
			IDEWorkbenchPlugin.log("Problem while storing dialog settings", e); //$NON-NLS-1$
			// don't do anything. Simply don't store the settings
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#restoreDialog(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	protected void restoreDialog(IDialogSettings settings) {
		super.restoreDialog(settings);

		boolean showDerived = settings.getBoolean(SHOW_DERIVED);
		showDerivedResourcesAction.setChecked(showDerived);
		setShowDerived(showDerived);

		String setting = settings.get(WORKINGS_SET_SETTINGS);
		if (setting != null) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(
						setting));
				workingSetFilterActionGroup.restoreState(memento);
			} catch (WorkbenchException e) {
				IDEWorkbenchPlugin.log(
						"Problem while restoring dialog settings", e); //$NON-NLS-1$
				// don't do anything. Simply don't restore the settings
			}
		}

		IWorkingSet ws = workingSetFilterActionGroup.getWorkingSet();

		setSubtitle(ws != null ? ws.getLabel() : null);
		workingSetFilter.setWorkingSet(ws);
		addListFilter(workingSetFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillViewMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillViewMenu(IMenuManager menuManager) {
		super.fillViewMenu(menuManager);

		showDerivedResourcesAction = new ShowDerivedResourcesAction();
		menuManager.add(showDerivedResourcesAction);

		workingSetFilterActionGroup = new WorkingSetFilterActionGroup(
				getShell(), new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						String property = event.getProperty();

						if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET
								.equals(property)) {
							Object newValue = event.getNewValue();

							if (newValue instanceof IWorkingSet) {
								workingSetFilter
										.setWorkingSet((IWorkingSet) newValue);
								setSubtitle(((IWorkingSet) newValue).getLabel());
							} else if (newValue == null) {
								workingSetFilter.setWorkingSet(null);
								setSubtitle(null);
							}

							scheduleRefresh();
						}
					}
				});

		menuManager.add(new Separator());
		workingSetFilterActionGroup.fillContextMenu(menuManager);
	}

	/**
	 * Sets the derived flag on the ResourceFilter instance
	 */
	private class ShowDerivedResourcesAction extends Action {

		/**
		 * Creates a new instance of the action.
		 */
		public ShowDerivedResourcesAction() {
			super(
					IDEWorkbenchMessages.FilteredResourcesSelectionDialog_showDerivedResourcesAction,
					IAction.AS_CHECK_BOX);
		}

		public void run() {
			setShowDerived(isChecked());
		}
	}

	/**
	 * If derived flag set to true, the items list will show all resources no
	 * matter what is the resource derived flag. If set to false the items list
	 * will show only not derived resources.
	 * 
	 * @param isDerived
	 *            derived flag
	 */
	private void setShowDerived(boolean isDerived) {
		this.isDerived = isDerived;
		setFilter(createFilter());
	}

	/**
	 * A label provider for ResourceDecorator objects. It creates labels with a
	 * resource full path for duplicates. It uses the Platform UI label
	 * decorator for providing extra resource info.
	 * 
	 * @since 3.3
	 */
	private class ResourceItemLabelProvider extends LabelProvider implements
			ILabelProviderListener {

		// Need to keep our own list of listeners
		private ListenerList listeners = new ListenerList();

		// Keeps relations between Resource and ResorceDecorator objects.
		// It is used when the provider want to forward label changed
		// notification from inner providers.
		private HashMap map = new HashMap();

		WorkbenchLabelProvider provider = new WorkbenchLabelProvider();

		ILabelDecorator decorator = PlatformUI.getWorkbench()
				.getDecoratorManager().getLabelDecorator();

		/**
		 * Creates a new instance of the class
		 */
		public ResourceItemLabelProvider() {
			super();
			provider.addListener(this);
			decorator.addListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			if (!(element instanceof ResourceItem)) {
				return super.getImage(element);
			}

			IResource res = ((ResourceItem) element).getResource();
			map.put(res, element);

			Image img = provider.getImage(res);

			return decorator.decorateImage(img, res);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (!(element instanceof ResourceItem)) {
				return super.getText(element);
			}

			IResource res = ((ResourceItem) element).getResource();

			map.put(res, element);

			String str = res.getName();

			// extra info for duplicates
			if ((((ResourceItem) element)).isDuplicate())
				str = str
						+ " - " + res.getParent().getFullPath().makeRelative().toString(); //$NON-NLS-1$

			return decorator.decorateText(str, res);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
		 */
		public void dispose() {
			provider.removeListener(this);
			provider.dispose();

			decorator.removeListener(this);
			decorator.dispose();

			super.dispose();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProviderListener#labelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
		 */
		public void labelProviderChanged(LabelProviderChangedEvent event) {
			Object[] elements = event.getElements();

			ArrayList items = null;

			if (elements != null) {
				items = new ArrayList();
				for (int i = 0; i < elements.length; i++) {
					if (map.containsKey(elements[i])) {
						items.add(map.get(elements[i]));
					}
				}
			}

			LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(
					this, items != null ? items.toArray() : null);

			Object[] l = listeners.getListeners();
			for (int i = 0; i < listeners.size(); i++) {
				((ILabelProviderListener) l[i]).labelProviderChanged(newEvent);
			}
		}

		/**
		 * Clears relations map
		 */
		public void reset() {
			map.clear();
		}
	}

	/**
	 * A label provider for IResource objects. It creates labels with a resource
	 * full path.
	 */
	private class CustomWorkbenchLabelProvider extends WorkbenchLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.model.WorkbenchLabelProvider#decorateText(java.lang.String,
		 *      java.lang.Object)
		 */
		protected String decorateText(String input, Object element) {
			IResource resource = ((IResource) element);

			if (resource.getType() == IResource.ROOT) {
				// Get readable name for workspace root ("Workspace"), without
				// duplicating language-specific string here.
				return null;
			}

			return resource.getFullPath().makeRelative().toString();
		}
	}

	/**
	 * Viewer filter which filters resources due to current working set
	 */
	private class CustomWorkingSetFilter extends ViewerFilter {
		private ResourceWorkingSetFilter resourceWorkingSetFilter = new ResourceWorkingSetFilter();

		/**
		 * Returns the active working set the filter is working with.
		 * 
		 * @return the active working set
		 */
		public IWorkingSet getWorkingSet() {
			return resourceWorkingSetFilter.getWorkingSet();
		}

		/**
		 * Sets the active working set.
		 * 
		 * @param workingSet
		 *            the working set the filter should work with
		 */
		public void setWorkingSet(IWorkingSet workingSet) {
			resourceWorkingSetFilter.setWorkingSet(workingSet);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof ItemsListSeparator) {
				return true;
			} else if (element instanceof ResourceItem) {
				return resourceWorkingSetFilter.select(viewer, parentElement,
						((ResourceItem) element).getResource());
			}

			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#refresh()
	 */
	public void refresh() {
		resourceItemLabelProvider.reset();
		super.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractListItem)
	 */
	protected IStatus validateItem(AbstractListItem item) {
		return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, 0, "", null); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter(java.lang.String)
	 */
	protected ItemsFilter createFilter() {
		return new ResourceFilter(container, isDerived, typeMask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemDetails(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractListItem)
	 */
	protected Object getItemDetails(AbstractListItem item) {
		return ((ResourceItem) item).getResource().getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemsComparator()
	 */
	protected Comparator getItemsComparator() {
		return new Comparator() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				Collator collator = Collator.getInstance();
				ResourceItem resourceDecorator1 = ((ResourceItem) o1);
				ResourceItem resourceDecorator2 = ((ResourceItem) o2);
				IResource resource1 = resourceDecorator1.getResource();
				IResource resource2 = resourceDecorator2.getResource();
				String s1 = resource1.getName();
				String s2 = resource2.getName();
				int comparability = collator.compare(s1, s2);
				if (comparability == 0) {
					resourceDecorator1.markAsDuplicate();
					resourceDecorator2.markAsDuplicate();
					s1 = resource1.getFullPath().toString();
					s2 = resource2.getFullPath().toString();
					comparability = collator.compare(s1, s2);
				}

				return comparability;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getObjectToReturn(java.lang.Object)
	 */
	protected Object getObjectToReturn(Object item) {
		ResourceItem resourceItem = (ResourceItem) item;
		accessedHistory(resourceItem);
		return resourceItem.getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ContentProvider)
	 */
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			IProgressMonitor progressMonitor) throws CoreException {

		container.accept(new ResourceProxyVisitor(contentProvider,
				progressMonitor), IResource.NONE);

		if (progressMonitor != null)
			progressMonitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#restoreItemFromMemento(org.eclipse.ui.IMemento)
	 */
	protected AbstractListItem restoreItemFromMemento(IMemento element) {
		IResource resource = null;
		ResourceFactory resourceFactory = new ResourceFactory();
		resource = (IResource) resourceFactory.createElement(element);
		return new ResourceItem(resource, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#storeItemToMemento(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractListItem,
	 *      org.eclipse.ui.IMemento)
	 */
	protected void storeItemToMemento(AbstractListItem item, IMemento element) {
		IResource resource = ((ResourceItem) item).getResource();
		ResourceFactory resourceFactory = new ResourceFactory(resource);
		resourceFactory.saveState(element);
	}

	/**
	 * ResourceProxyVisitor to visit resource tree and get matched resources.
	 * During visit resources it updates progress monitor and adds matched
	 * resources to ContentProvider instance.
	 */
	private class ResourceProxyVisitor implements IResourceProxyVisitor {

		private AbstractContentProvider contentProvider;

		private IProgressMonitor progressMonitor;

		private List projects;

		/**
		 * Creates new ResourceProxyVisitor instance.
		 * 
		 * @param contentProvider
		 * @param progressMonitor
		 * @throws CoreException
		 */
		public ResourceProxyVisitor(AbstractContentProvider contentProvider,
				IProgressMonitor progressMonitor) throws CoreException {
			super();
			this.contentProvider = contentProvider;
			this.progressMonitor = progressMonitor;
			IResource[] resources = container.members();
			this.projects = new ArrayList(Arrays.asList(resources));

			if (progressMonitor != null)
				progressMonitor.beginTask("", projects.size()); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
		public boolean visit(IResourceProxy proxy) {

			if (progressMonitor.isCanceled())
				return false;

			IResource res = proxy.requestResource();
			ResourceItem resourceItem = new ResourceItem(res);

			if (this.projects.remove((res.getProject()))
					|| this.projects.remove((res))) {
				progressMonitor.worked(1);
			}

			contentProvider.add(resourceItem);

			if (res.getType() == IResource.FILE) {
				return false;
			}

			return true;
		}
	}

	/**
	 * Filters resources using pattern and showDerived flag. It overrides
	 * ItemsFilter.
	 */
	private class ResourceFilter extends ItemsFilter {

		private boolean showDerived = false;

		private IContainer filterContainer;

		private int filterTypeMask;

		/**
		 * Creates new ResourceFilter instance
		 * 
		 * @param text
		 * @param container
		 * @param showDerived
		 *            flag which determine showing derived elements
		 * @param typeMask
		 */
		public ResourceFilter(IContainer container, boolean showDerived,
				int typeMask) {
			super();
			this.filterContainer = container;
			this.showDerived = showDerived;
			this.filterTypeMask = typeMask;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#isItemConsistent(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractListItem)
		 */
		public boolean isItemConsistent(AbstractListItem item) {
			ResourceItem resourceItem = (ResourceItem) item;
			IResource resource = resourceItem.getResource();
			if (this.filterContainer.findMember(resource.getFullPath()) != null)
				return true;
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#matchItem(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.AbstractListItem)
		 */
		public boolean matchItem(AbstractListItem item) {
			ResourceItem resourceItem = (ResourceItem) item;
			IResource resource = resourceItem.getResource();
			if ((!this.showDerived && resource.isDerived())
					|| ((this.filterTypeMask & resource.getType()) == 0))
				return false;
			return matches(resource.getName());
		}

		/**
		 * @return true if show derived flag is true false in other way
		 */
		public boolean isShowDerived() {
			return showDerived;
		}

		/**
		 * Returns the container.
		 * 
		 * @return container
		 */
		public IContainer getContainer() {
			return container;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter#isSubFilter(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter)
		 */
		public boolean isSubFilter(ItemsFilter filter) {
			if (!super.isSubFilter(filter))
				return false;
			if (filter instanceof ResourceFilter)
				if (this.showDerived == ((ResourceFilter) filter)
						.isShowDerived())
					return true;
			return false;
		}
	}

	/**
	 * Decorator for IResource implementation
	 */
	private class ResourceItem extends AbstractListItem {

		private IResource resource;

		/**
		 * Creates instance of ResourceItem
		 * 
		 * @param resource
		 * 
		 */
		public ResourceItem(IResource resource) {
			this.resource = resource;
		}

		/**
		 * Creates instance of ResourceItem and marks it as history
		 * 
		 * @param resource
		 * @param isHistory
		 *            is true if this resource is part of history
		 * 
		 */
		public ResourceItem(IResource resource, boolean isHistory) {
			this.resource = resource;
			if (isHistory)
				this.markAsHistory();
		}

		/**
		 * Gets IResource object
		 * 
		 * @return resource
		 */
		public IResource getResource() {
			return this.resource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj instanceof ResourceItem) {
				ResourceItem resourceItem = (ResourceItem) obj;
				return getResource().equals(resourceItem.getResource());
			}
			return super.equals(obj);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return getResource().hashCode();
		}
	}
}
