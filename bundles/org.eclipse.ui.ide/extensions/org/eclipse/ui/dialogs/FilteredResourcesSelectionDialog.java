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

	private static final String DIALOG_SETTINGS = "org.eclipse.ui.dialogs.ResourceSearchDialog"; //$NON-NLS-1$

	private static final String WORKINGS_SET_SETTINGS = "WorkingSet"; //$NON-NLS-1$

	private static final String SHOW_DERIVED = "ShowDerived"; //$NON-NLS-1$

	private ShowDerivedResourcesAction showDerivedResourcesAction;

	private ResourceSearchItemLabelProvider resourceSearchItemLabelProvider;

	private DecoratingLabelProvider detailsLabelProvider;

	private WorkingSetFilterActionGroup workingSetFilterActionGroup;

	private CustomWorkingSetFilter workingSetFilter = new CustomWorkingSetFilter();

	private String title;

	private IContainer container;

	private int typeMask;

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

		resourceSearchItemLabelProvider = new ResourceSearchItemLabelProvider();

		detailsLabelProvider = new DecoratingLabelProvider(
				new CustomWorkbenchLabelProvider(), PlatformUI.getWorkbench()
						.getDecoratorManager().getLabelDecorator());

		setListLabelProvider(resourceSearchItemLabelProvider);
		setDetailsLabelProvider(detailsLabelProvider);
	}

	public void setTitle(String title) {
		super.setTitle(title);
		this.title = title;
	}

	private void setTitleLabel(String text) {
		if (text == null || text.length() == 0) {
			getShell().setText(title);
		} else {
			getShell().setText(title + " - " + text); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearchDialog#getDialogSettings()
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
	 * @see org.eclipse.ui.dialogs.AbstractSearchDialog#storeDialog(org.eclipse.jface.dialogs.IDialogSettings)
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
	 * @see org.eclipse.ui.dialogs.AbstractSearchDialog#restoreDialog(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	protected void restoreDialog(IDialogSettings settings) {
		super.restoreDialog(settings);

		boolean showDerived = settings.getBoolean(SHOW_DERIVED);
		showDerivedResourcesAction.setChecked(showDerived);
		setSearchDerived(showDerived);

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

		setTitleLabel(ws != null ? ws.getLabel() : null);
		workingSetFilter.setWorkingSet(ws);
		addListFilter(workingSetFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearchDialog#fillViewMenu(org.eclipse.jface.action.IMenuManager)
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
								setTitleLabel(((IWorkingSet) newValue)
										.getLabel());
							} else if (newValue == null) {
								workingSetFilter.setWorkingSet(null);
								setTitleLabel(null);
							}

							scheduleRefresh();
						}
					}
				});

		menuManager.add(new Separator());
		workingSetFilterActionGroup.fillContextMenu(menuManager);
	}

	/**
	 * Sets the derived flag on the SimpleSearchEngine instance
	 */
	private class ShowDerivedResourcesAction extends Action {

		/**
		 * Creates a new instance of the action.
		 * 
		 * @param searchEngine
		 *            the search engine
		 */
		public ShowDerivedResourcesAction() {
			super(
					IDEWorkbenchMessages.ResourceSearchDialog_showDerivedResourcesAction,
					IAction.AS_CHECK_BOX);
		}

		public void run() {
			setSearchDerived(isChecked());
		}
	}

	private void setSearchDerived(boolean isDerived) {

		setFilter(createFilter(getPattern().trim(), this.container, isDerived,
				this.typeMask));
	}

	/**
	 * A label provider for ResourceDecorator objects. It creates labels with a
	 * resource full path for duplicates. It uses the Platform UI label
	 * decorator for providing extra resource info.
	 * 
	 * @since 3.3
	 */
	private class ResourceSearchItemLabelProvider extends LabelProvider
			implements ILabelProviderListener {

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
		public ResourceSearchItemLabelProvider() {
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
			if (!(element instanceof ResourceSearchItem)) {
				return super.getImage(element);
			}

			IResource res = ((ResourceSearchItem) element).getResource();
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
			if (!(element instanceof ResourceSearchItem)) {
				return super.getText(element);
			}

			IResource res = ((ResourceSearchItem) element).getResource();

			map.put(res, element);

			String str = res.getName();

			// extra info for duplicates
			if ((((ResourceSearchItem) element)).isDuplicate())
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
	 * 
	 * @since 3.3
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

	private class CustomWorkingSetFilter extends ViewerFilter {
		private ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();

		/**
		 * Returns the active working set the filter is working with.
		 * 
		 * @return the active working set
		 */
		public IWorkingSet getWorkingSet() {
			return workingSetFilter.getWorkingSet();
		}

		/**
		 * Sets the active working set.
		 * 
		 * @param workingSet
		 *            the working set the filter should work with
		 */
		public void setWorkingSet(IWorkingSet workingSet) {
			workingSetFilter.setWorkingSet(workingSet);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof SearchListSeparator) {
				return true;
			} else if (element instanceof ResourceSearchItem) {
				return workingSetFilter.select(viewer, parentElement,
						((ResourceSearchItem) element).getResource());
			}

			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearchDialog#refresh()
	 */
	public void refresh() {
		resourceSearchItemLabelProvider.reset();
		super.refresh();
	}

	/**
	 * Use this method to further filter resources. As resources are gathered,
	 * if a resource matches the current pattern string, this method will be
	 * called. If this method answers false, the resource will not be included
	 * in the list of matches and the resource's children will NOT be considered
	 * for matching.
	 */
	protected boolean validateSearchedResource(IResource resource) {
		return true;
	}

	private SearchFilter createFilter(String text, IContainer container,
			boolean showDerived, int typeMask) {
		return new ResourceFilter(text, container, showDerived, typeMask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getItemDetails(java.lang.Object)
	 */
	protected Object getItemDetails(Object item) {
		return ((ResourceSearchItem) item).getResource().getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
	 */
	protected IStatus validateItem(Object item) {
		return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, 0, "", null); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFilter(java.lang.String)
	 */
	protected SearchFilter createFilter(String text) {
		return createFilter(text, container, false, typeMask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#createFromElement(org.eclipse.ui.IMemento)
	 */
	protected Object createFromElement(IMemento element) {
		IResource resource = null;
		ResourceFactory resourceFactory = new ResourceFactory();
		resource = (IResource) resourceFactory.createElement(element);
		return new ResourceSearchItem(resource, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getDetails(java.lang.Object)
	 */
	protected Object getDetails(Object item) {
		return ((ResourceSearchItem) item).getResource().getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#getElementsComparator()
	 */
	protected Comparator getElementsComparator() {
		return new Comparator() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				Collator collator = Collator.getInstance();
				ResourceSearchItem resourceDecorator1 = ((ResourceSearchItem) o1);
				ResourceSearchItem resourceDecorator2 = ((ResourceSearchItem) o2);
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
		ResourceSearchItem resourceSearchItem = (ResourceSearchItem) item;
		accessedHistory(resourceSearchItem);
		return resourceSearchItem.getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#searchElements(org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ContentProvider)
	 */
	protected void searchElements(ContentProvider contentProvider)
			throws CoreException {

		contentProvider.beginTask("", container.members().length); //$NON-NLS-1$

		container.accept(new ResourceProxyVisitor(contentProvider),
				IResource.NONE);

		contentProvider.endTask();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#setAttributes(java.lang.Object,
	 *      org.eclipse.ui.IMemento)
	 */
	protected void setAttributes(Object object, IMemento element) {
		IResource resource = ((ResourceSearchItem) object).getResource();
		ResourceFactory resourceFactory = new ResourceFactory(resource);
		resourceFactory.saveState(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateElement(java.lang.Object)
	 */
	protected IStatus validateElement(Object item) {
		return new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * ResourceProxyVisitor to visit resource tree and get matched resources.
	 * During visit resources they update progress monitor and add matched
	 * resources to model.
	 * 
	 * @since 3.3
	 */
	private class ResourceProxyVisitor implements IResourceProxyVisitor {

		private ContentProvider contentProvider;

		private List projects;

		/**
		 * @param contentProvider
		 * @throws CoreException
		 */
		public ResourceProxyVisitor(ContentProvider contentProvider)
				throws CoreException {
			super();
			this.contentProvider = contentProvider;
			this.projects = new ArrayList(Arrays.asList(container.members()));

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceProxyVisitor#visit(org.eclipse.core.resources.IResourceProxy)
		 */
		public boolean visit(IResourceProxy proxy) {

			if (contentProvider.isDeactivated())
				return false;

			IResource res = proxy.requestResource();
			ResourceSearchItem searchItem = new ResourceSearchItem(res);

			if (this.projects.remove((res.getProject()))
					|| this.projects.remove((res))) {
				contentProvider.worked(1);
			}

			contentProvider.addSearchItem(searchItem);

			if (res.getType() == IResource.FILE) {
				return false;
			}

			return true;
		}

	}

	/**
	 * Filters resources using pattern and showDerived flag. It override
	 * SearchFilter.
	 * 
	 * @since 3.3
	 * 
	 */
	private static class ResourceFilter extends SearchFilter {

		private boolean showDerived = false;

		private IContainer container;

		private int typeMask;

		/**
		 * @param text
		 * @param container
		 * @param showDerived
		 *            flag which determine showing derived elements
		 * @param typeMask
		 */
		public ResourceFilter(String text, IContainer container,
				boolean showDerived, int typeMask) {
			super(text);
			this.container = container;
			this.showDerived = showDerived;
			this.typeMask = typeMask;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.AbstractSearcher.SearchFilter#isConsistentElement(java.lang.Object)
		 */
		public boolean isConsistentElement(Object searchitem) {
			ResourceSearchItem resourceSearchItem = (ResourceSearchItem) searchitem;
			IResource resource = resourceSearchItem.getResource();
			if (this.container.findMember(resource.getFullPath()) != null)
				return true;
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.dialogs.AbstractSearcher.SearchFilter#matchesElement(java.lang.Object)
		 */
		public boolean matchesElement(Object element) {
			ResourceSearchItem searchItem = (ResourceSearchItem) element;
			IResource resource = searchItem.getResource();
			if ((!this.showDerived && resource.isDerived())
					|| ((this.typeMask & resource.getType()) == 0))
				return false;
			return matchesName(resource.getName());
		}

		/**
		 * Set showDerived flag
		 * 
		 * @param showDerived
		 */
		public void setDerived(boolean showDerived) {
			this.showDerived = showDerived;
		}

		/**
		 * @return true if show derived flag is true false in other way
		 */
		public boolean isShowDerived() {
			return showDerived;
		}

		public boolean isSubFilter(SearchFilter filter) {
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
	 * @since 3.3
	 */
	private class ResourceSearchItem extends AbstractSearchItem {

		private IResource resource;

		/**
		 * 
		 */
		public ResourceSearchItem(IResource resource) {
			this.resource = resource;
		}

		/**
		 * 
		 */
		public ResourceSearchItem(IResource resource, boolean isHistory) {
			this.resource = resource;
			if (isHistory)
				this.markAsHistory();
		}

		/**
		 * Get decorated resource
		 * 
		 * @return decorated resource
		 */
		public IResource getResource() {
			return this.resource;
		}

		public boolean equals(Object obj) {
			if (obj instanceof ResourceSearchItem) {
				ResourceSearchItem resourceSearchItem = (ResourceSearchItem) obj;
				return getResource().equals(resourceSearchItem.getResource());
			}
			return super.equals(obj);
		}

		public int hashCode() {
			return getResource().hashCode();
		}

	}

}
