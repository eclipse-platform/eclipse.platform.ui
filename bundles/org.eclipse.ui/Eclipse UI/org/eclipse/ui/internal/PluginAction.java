package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.plugins.ConfigurationElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;

/**
 * A PluginAction is a proxy for an action extension.
 *
 * At startup we read the registry and create a PluginAction for each action extension.
 * This plugin action looks like the real action ( label, icon, etc ) and acts as
 * a proxy for the action until invoked.  At that point the proxy will instantiate 
 * the real action and delegate the run method to the real action.
 * This makes it possible to load the action extension lazily.
 *
 * Occasionally the class will ask if it is OK to 
 * load the delegate (on selection changes).  If the plugin containing
 * the action extension has been loaded then the action extension itself
 * will be instantiated.
 */

public class PluginAction
	extends Action
	implements ISelectionListener, ISelectionChangedListener {
	private IActionDelegate delegate;
	private SelectionEnabler enabler;
	private IStructuredSelection selection;
	private IConfigurationElement configElement;
	private String runAttribute;
	private static int actionCount = 0;

	//a boolean that returns whether or not this action
	//is Adaptable - i.e. is defined on a resource type
	boolean isAdaptableAction = false;
	boolean adaptableNotChecked = true;

	/**
	 * PluginAction constructor.
	 */
	public PluginAction(IConfigurationElement actionElement, String runAttribute) {
		super();

		// Create unique action id.
		setId("PluginAction." + Integer.toString(actionCount)); //$NON-NLS-1$
		++actionCount;

		// Store arguments.
		this.configElement = actionElement;
		this.runAttribute = runAttribute;
		if (configElement.getAttribute(PluginActionBuilder.ATT_ENABLES_FOR) != null)
			this.enabler = new SelectionEnabler(configElement);
		else {
			IConfigurationElement [] kids = configElement.getChildren(PluginActionBuilder.TAG_ENABLEMENT);
			if (kids.length > 0)
				this.enabler = new SelectionEnabler(configElement);
		}

		// Give enabler or delegate a chance to adjust enable state
		selectionChanged(new StructuredSelection());
	}

	/**
	 * Return whether or not this action could have been registered
	 * due to an adaptable - i.e. it is a resource type.
	 */

	private boolean hasAdaptableType() {
		if (adaptableNotChecked) {
			Object parentConfig = ((ConfigurationElement) configElement).getParent();
			String typeName = null;
			if(parentConfig != null && parentConfig instanceof IConfigurationElement) 
				typeName = ((IConfigurationElement) parentConfig).getAttribute("objectClass");
			
			//See if this is typed at all first
			if(typeName == null){
				adaptableNotChecked = false;
				return false;
			}
			Class resourceClass = IResource.class;

			if (typeName.equals(resourceClass.getName())) {
				isAdaptableAction = true;
				adaptableNotChecked = false;
				return isAdaptableAction;
			}
			Class[] children = resourceClass.getDeclaredClasses();
			for (int i = 0; i < children.length; i++) {
				if (children[i].getName().equals(typeName)) {
					isAdaptableAction = true;
					adaptableNotChecked = false;
					return isAdaptableAction;
				}				
			}
			adaptableNotChecked = false;
		}
		return isAdaptableAction;
	}

	/**
	 * Creates an instance of the delegate class as defined on
	 * the configuration element. 
	 * <p>
	 * This method should be called at the last possible moment to 
	 * avoid premature loading of the plugin.
	 * </p>
	 */
	protected IActionDelegate createDelegate() {
		try {
			Object obj = WorkbenchPlugin.createExtension(configElement, runAttribute);
			if (obj instanceof IActionDelegate)
				return (IActionDelegate) obj;
			else
				return null;
		} catch (CoreException e) {
			// cannot safely open dialog so log the problem
			WorkbenchPlugin.log("Could not create action.", e.getStatus()); //$NON-NLS-1$
			return null;
		}
	}
	/**
	 * Return the delegate action or null if not created yet
	 */
	protected IActionDelegate getDelegate() {
		return delegate;
	}
	/**
	 * Returns the current structured selection in the workbench, or an empty
	 * selection if nothing is selected or if selection does not include
	 * objects (for example, raw text).
	 *
	 * @return the current structured selection in the workbench
	 */
	public IStructuredSelection getStructuredSelection() {
		return selection;
	}
	/**
	 * Returns true if the declaring plugin has been loaded
	 * and there is no need to delay creating the delegate
	 * any more.
	 */
	public boolean isOkToCreateDelegate() {
		// test if the plugin has loaded
		IPluginDescriptor plugin =
			configElement.getDeclaringExtension().getDeclaringPluginDescriptor();
		return plugin.isPluginActivated();
	}
	/**
	 * Runs the action.
	 */
	public void run() {
		// this message dialog is problematic.
		if (delegate == null) {
			// High noon to load the delegate.
			delegate = createDelegate();
			if (delegate == null)
				return;
			selectionChanged(selection);
			if (isEnabled() == false) {
				MessageDialog
					.openInformation(
						Display.getDefault().getActiveShell(),
						WorkbenchMessages.getString("Information"),
				//$NON-NLS-1$
				WorkbenchMessages.getString("PluginActino.operationNotAvailableMessage"));
				//$NON-NLS-1$
				return;
			}
		}
		delegate.run(this);
	}
	/**
	 * Handles selection change. If rule-based enabled is
	 * defined, it will be first to call it. If the delegate
	 * is loaded, it will also be given a chance.
	 */
	public void selectionChanged(IStructuredSelection newSelection) {
		this.selection = newSelection;
		if (delegate == null) {
			// We can ask the delegate to process enabling
			// if it is OK to load it.
			if (isOkToCreateDelegate())
				delegate = createDelegate();
		}

		//If this is a resource action then adapt the selection
		if (hasAdaptableType())
			this.selection = getResourceAdapters(newSelection);

		if (enabler != null) {
			setEnabled(enabler.isEnabledForSelection(selection));
		}
		if (delegate != null) {
			delegate.selectionChanged(this, selection);
		}
	}
	/**
	 * The <code>SelectionChangedEventAction</code> implementation of this 
	 * <code>ISelectionChangedListener</code> method calls 
	 * <code>selectionChanged(IStructuredSelection)</code> when the selection is
	 * a structured one.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		if (sel instanceof IStructuredSelection)
			selectionChanged((IStructuredSelection) sel);
	}
	/**
	 * The <code>SelectionChangedEventAction</code> implementation of this 
	 * <code>ISelectionListener</code> method calls 
	 * <code>selectionChanged(IStructuredSelection)</code> when the selection is
	 * a structured one. Subclasses may extend this method to react to the change.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection) sel);
		}
	}
	/**
	 * Set the delegate action or null if not created yet
	 */
	protected void setDelegate(IActionDelegate actionDelegate) {
		this.delegate = actionDelegate;
	}

	/**
	 * Get a new selection with the resource adaptable version 
	 * of this selection
	 */
	private IStructuredSelection getResourceAdapters(IStructuredSelection selection) {

		List adaptables = new ArrayList();
		Object[] elements = selection.toArray();
		for (int i = 0; i < elements.length; i++) {
			Object adaptedValue = ((IAdaptable) elements[i]).getAdapter(IResource.class);
			if (adaptedValue != null)
				adaptables.add(adaptedValue);
		}

		return new StructuredSelection(adaptables);
	}

}