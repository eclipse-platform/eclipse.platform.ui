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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.utils.ResourceSearchItem;

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
public class ResourceSearchDialog extends AbstractSearchDialog {

	private static final String DIALOG_SETTINGS = "org.eclipse.ui.dialogs.ResourceSearchDialog"; //$NON-NLS-1$

	private static final String SHOW_DERIVED = "ShowDerived"; //$NON-NLS-1$

	private ResourceSearcher resourceSearcher;

	private ShowDerivedResourcesAction showDerivedResourcesAction;

	private ResourceSearchItemLabelProvider resourceSearchItemLabelProvider;

	private DecoratingLabelProvider detailsLabelProvider;

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
	public ResourceSearchDialog(Shell shell, boolean multi,
			IContainer container, int typesMask) {
		super(shell, multi);

		setTitle(IDEWorkbenchMessages.OpenResourceDialog_title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.OPEN_RESOURCE_DIALOG);

		resourceSearcher = new ResourceSearcher(container, typesMask);
		setSearcher(resourceSearcher);

		resourceSearchItemLabelProvider = new ResourceSearchItemLabelProvider();

		detailsLabelProvider = new DecoratingLabelProvider(
				new CustomWorkbenchLabelProvider(), PlatformUI.getWorkbench()
						.getDecoratorManager().getLabelDecorator());

		setListLabelProvider(resourceSearchItemLabelProvider);
		setDetailsLabelProvider(detailsLabelProvider);
	}

	/**
	 * Creates a new instance of the class
	 * 
	 * @param shell
	 *            the parent shell
	 * @param multi
	 *            the multi selection flag
	 * @param resourceSearcher
	 *            the resource searcher
	 */
	public ResourceSearchDialog(Shell shell, boolean multi,
			ResourceSearcher resourceSearcher) {
		super(shell, multi);

		setTitle(IDEWorkbenchMessages.OpenResourceDialog_title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.OPEN_RESOURCE_DIALOG);

		this.resourceSearcher = resourceSearcher;
		setSearcher(resourceSearcher);

		resourceSearchItemLabelProvider = new ResourceSearchItemLabelProvider();

		detailsLabelProvider = new DecoratingLabelProvider(
				new CustomWorkbenchLabelProvider(), PlatformUI.getWorkbench()
						.getDecoratorManager().getLabelDecorator());

		setListLabelProvider(resourceSearchItemLabelProvider);
		setDetailsLabelProvider(detailsLabelProvider);
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
		resourceSearcher.setFilterParam(ResourceSearcher.DERIVED, new Boolean(
				showDerived));
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
			resourceSearcher.setFilterParam(ResourceSearcher.DERIVED,
					new Boolean(isChecked()));
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractSearchDialog#refresh()
	 */
	public void refresh() {
		resourceSearchItemLabelProvider.reset();
		super.refresh();
	}
}
