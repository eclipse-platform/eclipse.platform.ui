package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;


/**
 * Proxy to a launch shortcut extention
 */
public class LaunchShortcutExtension implements ILaunchShortcut {
	
	private ImageDescriptor fImageDescriptor = null;
	private List fPerspectives = null;
	private ILaunchShortcut fDelegate = null;
	private Set fModes = null;
	
	/**
	 * The configuration element defining this tab.
	 */
	private IConfigurationElement fConfig;
	
	/**
	 * Constructs a launch configuration tab extension based
	 * on the given configuration element
	 * 
	 * @param element the configuration element defining the
	 *  attribtues of this launch configuration tab extension
	 * @return a new launch configuration tab extension
	 */
	public LaunchShortcutExtension(IConfigurationElement element) {
		setConfigurationElement(element);
	}
	
	/**
	 * Sets the configuration element that defines the attributes
	 * for this extension.
	 * 
	 * @param element configuration element
	 */
	private void setConfigurationElement(IConfigurationElement element) {
		fConfig = element;
	}
	
	/**
	 * Returns the configuration element that defines the attributes
	 * for this extension.
	 * 
	 * @param configuration element that defines the attributes
	 *  for this launch configuration tab extension
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fConfig;
	}
	
	/**
	 * Returns the label of this shortcut
	 * 
	 * @return the label of this shortcut, or <code>null</code> if not
	 *  specified
	 */
	public String getLabel() {
		return getConfigurationElement().getAttribute("label"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the path of the icon for this shortcut, or <code>null</code>
	 * if none.
	 * 
	 * @return the path of the icon for this shortcut, or <code>null</code>
	 * if none
	 */
	protected String getIconPath() {
		return getConfigurationElement().getAttribute("icon"); //$NON-NLS-1$
	}	
	
	/**
	 * Returns the image for this shortcut, or <code>null</code> if none
	 * 
	 * @return the image for this shortcut, or <code>null</code> if none
	 */
	public ImageDescriptor getImageDescriptor() {
		if (fImageDescriptor == null) {
			URL iconURL = getConfigurationElement().getDeclaringExtension().getDeclaringPluginDescriptor().getInstallURL();
			String iconPath = getIconPath();
			try {
				iconURL = new URL(iconURL, iconPath);
				fImageDescriptor = ImageDescriptor.createFromURL(iconURL);
			} catch (MalformedURLException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fImageDescriptor;
	}
	
	/**
	 * Returns the perspectives this shortcut is registered for.
	 * 
	 * @return list of Strings representing perspective identifiers 
	 */
	public List getPerspectives() {
		if (fPerspectives == null) {
			IConfigurationElement[] perspectives = getConfigurationElement().getChildren("perspective"); //$NON-NLS-1$
			fPerspectives = new ArrayList(perspectives.length);
			for (int i = 0; i < perspectives.length; i++) {
				fPerspectives.add(perspectives[i].getAttribute("id")); //$NON-NLS-1$
			}
		}
		return fPerspectives;
	}
	
	/**
	 * Returns this shortcut's delegate, or <code>null</code> if none
	 * 
	 * @return this shortcut's delegate, or <code>null</code> if none
	 */
	protected ILaunchShortcut getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (ILaunchShortcut)fConfig.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), LaunchConfigurationsMessages.getString("LaunchShortcutExtension.Error_4"), LaunchConfigurationsMessages.getString("LaunchShortcutExtension.Unable_to_use_launch_shortcut_5"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return fDelegate;
	}
	
	/**
	 * @see ILaunchShortcut#launch(IEditorPart, String)
	 */
	public void launch(IEditorPart editor, String mode) {
		ILaunchShortcut shortcut = getDelegate();
		if (shortcut != null) {
			shortcut.launch(editor, mode);
		}
	}

	/**
	 * @see ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		ILaunchShortcut shortcut = getDelegate();
		if (shortcut != null) {
			shortcut.launch(selection, mode);
		}		
	}
	
	/**
	 * Returns the set of modes this shortcut supports.
	 * 
	 * @return the set of modes this shortcut supports
	 */
	public Set getModes() {
		if (fModes == null) {
			String modes= getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet(0);
			}
			StringTokenizer tokenizer= new StringTokenizer(modes, ","); //$NON-NLS-1$
			fModes = new HashSet(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}	

}

