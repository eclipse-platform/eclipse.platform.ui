/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The perspective manager manages the 'perspective' settings
 * defined by launch configurations. Specifically it: <ul>
 * <li>changes perspectives as launches are registered</li>
 * <li>change perspective when a thread suspends</li>
 * </ul>
 * 
 * @see IDebugUIContants.ATTR_RUN_PERSPECTIVE
 * @see IDebugUIContants.ATTR_DEBUG_PERSPECTIVE
 */
public class PerspectiveManager implements ILaunchListener, IDebugEventSetListener {
		
	/**
	 * Table of config types to tables of user specified perspective settings (mode ids
	 * to perspective ids).
	 */	
	private Map fPreferenceMap;
	
	// XML tags
	private static final String ELEMENT_PERSPECTIVES = "launchPerspectives"; //$NON-NLS-1$
	private static final String ELEMENT_PERSPECTIVE = "launchPerspective"; //$NON-NLS-1$
	private static final String ATTR_TYPE_ID = "configurationType"; //$NON-NLS-1$
	private static final String ATTR_MODE_ID = "mode"; //$NON-NLS-1$
	private static final String ATTR_PERSPECTIVE_ID = "perspective";  //$NON-NLS-1$

	/**
	 * Flag used to indicate that the user is already being prompted to
	 * switch perspectives. This flag allows us to not open multiple
	 * prompts at the same time.
	 */
	private boolean fPrompting;
		
	/**
	 * Called by the debug ui plug-in on startup.
	 * The perspective manager starts listening for
	 * launches to be registered.
	 */
	public void startup() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getLaunchManager().addLaunchListener(this);
		plugin.addDebugEventListener(this);
	}

	/**
	 * Called by the debug ui plug-in on shutdown.
	 * The perspective manager de-registers as a 
	 * launch listener.
	 */
	public void shutdown() {
		DebugPlugin plugin = DebugPlugin.getDefault();
		plugin.getLaunchManager().removeLaunchListener(this);
		plugin.removeDebugEventListener(this);
	}

	/**
	 * Do nothing.
	 * 
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}
	
	/**
	 * Do nothing.
	 * 
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}	

	/** 
	 * Switch to the perspective specified by the
	 * launch configuration.
	 * 
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		String perspectiveId = null;
		// check event filters
		try {
			perspectiveId = getPerspectiveId(launch);
		} catch (CoreException e) {
			String name = DebugUIPlugin.getModelPresentation().getText(launch);
			switchFailed(e, name);
		}
		
		// don't switch if a private config
		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
		if (configuration != null) {
			if (!LaunchConfigurationManager.isVisible(configuration)) {
				perspectiveId = null;
			}
		}
		
		final String id= perspectiveId;
		// switch
		async(new Runnable() {
			public void run() {
				if (id != null && shouldSwitchPerspectiveForLaunch(id)) {
					switchToPerspective(id);
				}
			}
		});
		
	}


	/**
	 * Switches to the specified perspective
	 * 
	 * @param id perspective identifier
	 */
	protected void switchToPerspective(String id) {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			try {
				window.getWorkbench().showPerspective(id, window);
			} catch (WorkbenchException e) {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(),
				LaunchConfigurationsMessages.getString("PerspectiveManager.Error_1"),  //$NON-NLS-1$
				MessageFormat.format(LaunchConfigurationsMessages.getString("PerspectiveManager.Unable_to_switch_to_perspective__{0}_2"), new String[]{id}), //$NON-NLS-1$
				e);
			}
		}
	}
	
	/**
	 * Utility method to submit an asnychronous runnable to the UI
	 */
	protected void async(Runnable r) {
		Display d = DebugUIPlugin.getStandardDisplay();
		if (d != null && !d.isDisposed()) {
			d.asyncExec(r);
		}
	}
	
	/**
	 * Utility method to submit a synchronous runnable to the UI
	 */
	protected void sync(Runnable r) {
		Display d = DebugUIPlugin.getStandardDisplay();
		if (d != null && !d.isDisposed()) {
			d.syncExec(r);
		}
	}	

	/**
	 * Reports failure to switch perspectives to the user
	 * 
	 * @param status exception status describing failure
	 * @param launchName the name of the launch that the
	 *  failure is associated with
	 */
	protected void switchFailed(final Throwable t, final String launchName) {
		sync(new Runnable() {
			public void run() {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), LaunchConfigurationsMessages.getString("PerspectiveManager.Error_1"),  //$NON-NLS-1$
				 MessageFormat.format(LaunchConfigurationsMessages.getString("PerspectiveManager.Unable_to_switch_perpsectives_as_specified_by_launch__{0}_4"), new String[] {launchName}), //$NON-NLS-1$
				 t);
			}});
	}
	
	/**
	 * On a SUSPEND event, show the debug view. If no debug view is open,
	 * switch to the perspective specified by the launcher.
	 *
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		// open the debugger if this is a suspend event and the debug view is not yet open
		// and the preferences are set to switch
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getKind() == DebugEvent.SUSPEND && event.getDetail() == DebugEvent.BREAKPOINT) {
				doPerspectiveSwitch(event);
			}
		}
	}
	
	/**
	 * Makes the debug view visible.
	 */
	protected void doShowDebugView() {
		if (fPrompting) {
			// Wait until the user has dismissed the perspective
			// switching dialog before opening the view.
			Thread thread= new Thread(new Runnable() {
				public void run() {
					if (fPrompting) {
						synchronized (PerspectiveManager.this) {
							try {
								PerspectiveManager.this.wait();
							} catch (InterruptedException e) {
							}
						}
					}
					async(new Runnable() {
						public void run() {
							doShowDebugView0();
						}
					});
				}
			});
			thread.start();
			return;
		}
		doShowDebugView0();
	}
	
	private void doShowDebugView0() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					page.showView(IDebugUIConstants.ID_DEBUG_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) {
					DebugUIPlugin.log(e);
				}
			}
		}
	}
	/**
	 * Carries out perspective switching as appropriate for
	 * the given debug event. 
	 * 
	 * @param event the suspend or step end event
	 */
	private void doPerspectiveSwitch(DebugEvent event) {
		// apply event filters
		ILaunch launch= null;
		Object source = event.getSource();
		if (source instanceof IDebugElement) {
			launch = ((IDebugElement)source).getLaunch();
		} else if (source instanceof IProcess) {
			launch = ((IProcess)source).getLaunch();
		}
		String perspectiveId = null;
		try {
			perspectiveId = getPerspectiveId(launch);
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
		// if no perspective specified, always switch to debug
		// perspective 

		// this has to be done in an asynch, such that the workbench
		// window can be accessed
		final String id = perspectiveId;
		Runnable r = new Runnable() {
			public void run() {
				String targetId = id;
				IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
				if (window != null && targetId != null) {
					// re-open the window if minimized 
					Shell shell= window.getShell();
					if (shell != null) {
						if (shell.getMinimized()) {
							shell.setMinimized(false);
						}
						if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH)) {
							shell.forceActive();
						}
					}
					if (shouldSwitchPerspectiveForSuspend(targetId)) {
						switchToPerspective(targetId);
					}
				}
				if (DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW)) {
					doShowDebugView();
				}
			}
		};
		async(r);
	}
	
	/**
	 * Returns whether or not the user wishes to switch to the specified
	 * perspective when a suspend occurs.
	 * 
	 * @param perspectiveName the name of the perspective that will be presented
	 *  to the user for confirmation if they've asked to be prompted about
	 *  perspective switching for suspension
	 * @return whether or not the user wishes to switch to the specified perspective
	 *  automatically when the given launch suspends
	 */
	protected boolean shouldSwitchPerspectiveForSuspend(String perspectiveId) {
		return shouldSwitchPerspective(perspectiveId, LaunchConfigurationsMessages.getString("PerspectiveManager.13"), IDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND); //$NON-NLS-1$
	}
	
	/**
	 * Returns whether or not the user wishes to switch to the specified
	 * perspective when a launch occurs.
	 * 
	 * @param perspectiveName the name of the perspective that will be presented
	 *  to the user for confirmation if they've asked to be prompted about
	 *  perspective switching
	 * @return whether or not the user wishes to switch to the specified perspective
	 *  automatically when a launch occurs
	 */
	protected boolean shouldSwitchPerspectiveForLaunch(String perspectiveId) {
		return shouldSwitchPerspective(perspectiveId, LaunchConfigurationsMessages.getString("PerspectiveManager.15"), IDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE); //$NON-NLS-1$
	}
	
	/**
	 * Returns whether or not the user wishes to switch to the specified
	 * perspective when a launch occurs.
	 * 
	 * @param perspectiveName the name of the perspective that will be presented
	 *  to the user for confirmation if they've asked to be prompted about
	 *  perspective switching
	 * @param message a message to be presented to the user. This message is expected to
	 *  contain a slot for the perspective name to be inserted ("{0}").
	 * @param preferenceKey the preference key of the perspective switching preference
	 * @return whether or not the user wishes to switch to the specified perspective
	 *  automatically
	 */
	private boolean shouldSwitchPerspective(String perspectiveId, String message, String preferenceKey) {
		if (isCurrentPerspective(perspectiveId) || fPrompting) {
			return false;
		}
		String perspectiveName= getPerspectiveLabel(perspectiveId);
		Shell shell= Display.getDefault().getActiveShell();
		if (perspectiveName == null || shell == null) {
			return false;
		}
		String switchPerspective= DebugUIPlugin.getDefault().getPreferenceStore().getString(preferenceKey);
		if (AlwaysNeverDialog.ALWAYS.equals(switchPerspective)) {
			return true;
		} else if (AlwaysNeverDialog.NEVER.equals(switchPerspective)) {
			return false;
		}
		fPrompting= true;
		boolean answer= AlwaysNeverDialog.openQuestion(shell, LaunchConfigurationsMessages.getString("PerspectiveManager.12"), MessageFormat.format(message, new String[] { perspectiveName }), preferenceKey, DebugUIPlugin.getDefault().getPreferenceStore()); //$NON-NLS-1$
		synchronized (this) {
			fPrompting= false;
			notifyAll();
		}
		return answer;
	}
	
	/**
	 * Returns whether the given perspective identifier matches the
	 * identifier of the current perspective.
	 * 
	 * @param perspectiveId the identifier
	 * @return whether the given perspective identifier matches the
	 *  identifier of the current perspective
	 */
	protected boolean isCurrentPerspective(String perspectiveId) {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		boolean isCurrent= false;
		if (window != null) {
			isCurrent= perspectiveId.equals(window.getActivePage().getPerspective().getId());
		}
		return isCurrent;
	}
	
	/**
	 * Returns the label of the perspective with the given identifier or
	 * <code>null</code> if no such perspective exists.
	 * 
	 * @param perspectiveId the identifier
	 * @return the label of the perspective with the given identifier or
	 *  <code>null</code> if no such perspective exists 
	 */
	protected String getPerspectiveLabel(String perspectiveId) {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		IPerspectiveDescriptor newPerspective = window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
		if (newPerspective == null) {
			return null;
		}
		return newPerspective.getLabel();
	}

	/** 
	 * Returns the perspective associated with the
	 * given launch, or <code>null</code> if none.
	 * 
	 * @param launch a launch
	 * @return the perspective associated with the launch,
	 * 	or <code>null</code>
	 * @exception CoreException if unable to retrieve a required
	 *  launch configuration attribute
	 */
	protected String getPerspectiveId(ILaunch launch) throws CoreException {
		if (launch == null) {
			return null;
		}
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		if (config == null) {
			return null;
		}
		String perspectiveId = null;
		perspectiveId = DebugUITools.getLaunchPerspective(config.getType(), launch.getLaunchMode());
		if (perspectiveId != null && perspectiveId.equals(IDebugUIConstants.PERSPECTIVE_NONE)) {
			perspectiveId = null;
		}
		return perspectiveId;
	}
	
	/**
	 * Returns the perspective to switch to when a configuration of the given type
	 * is launched in the given mode, or <code>null</code> if no switch should take
	 * place.
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode identifier
	 * @return perspective identifier or <code>null</code>
	 * @since 3.0
	 */
	public String getLaunchPerspective(ILaunchConfigurationType type, String mode) {
		String id = getUserSpecifiedLaunchPerspective(type, mode);
		if (id == null) {
			// get the default
			id = getDefaultLaunchPerspective(type, mode);
		} else if (id.equals(IDebugUIConstants.PERSPECTIVE_NONE)) {
			// translate NONE to null
			id = null;
		}
		return id;
	}
	
	/**
	 * Sets the perspective to switch to when a configuration of the given type
	 * is launched in the given mode. <code>PERSPECTIVE_NONE</code> indicates no
	 * perspective switch should take place. <code>PERSPECTIVE_DEFAULT</code> indicates
	 * a default perspective switch should take place, as defined by the associated
	 * launch tab group extension. Saves plug-in preferences.
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode identifier
	 * @param perspective identifier, <code>PERSPECTIVE_NONE</code>, or
	 *   <code>PERSPECTIVE_DEFAULT</code>
	 * @since 3.0
	 */
	public void setLaunchPerspective(ILaunchConfigurationType type, String mode, String perspective) {
		internalSetLaunchPerspective(type.getIdentifier(), mode, perspective);
		// update preference
		String xml;
		try {
			xml = generatePerspectiveXML();
			DebugUIPlugin.getDefault().getPreferenceStore().putValue(IInternalDebugUIConstants.PREF_LAUNCH_PERSPECTIVES, xml);
			DebugUIPlugin.getDefault().savePluginPreferences();				
		} catch (IOException e) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus(LaunchConfigurationsMessages.getString("PerspectiveManager.9"), e)); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus(LaunchConfigurationsMessages.getString("PerspectiveManager.9"), e)); //$NON-NLS-1$
		} catch (TransformerException e) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus(LaunchConfigurationsMessages.getString("PerspectiveManager.9"), e)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Sets the perspective to switch to when a configuration of the given type
	 * is launched in the given mode. <code>PERSPECTIVE_NONE</code> indicates no
	 * perspective switch should take place. <code>PERSPECTIVE_DEFAULT</code> indicates
	 * a default perspective switch should take place, as defined by the associated
	 * launch tab group extension.
	 * 
	 * @param type launch configuration type identifier
	 * @param mode launch mode identifier
	 * @param perspective identifier, <code>PERSPECTIVE_NONE</code>, or
	 *   <code>PERSPECTIVE_DEFAULT</code>
	 * @since 3.0
	 */
	private void internalSetLaunchPerspective(String type, String mode, String perspective) {
		if (fPreferenceMap == null) {
			initPerspectives();
		}		
		Map modeMap = (Map)fPreferenceMap.get(type);
		if (modeMap == null) {
			modeMap = new HashMap();
			fPreferenceMap.put(type, modeMap);
		}
		if (perspective.equals(IDebugUIConstants.PERSPECTIVE_DEFAULT)) {
			// remove user preference setting
			modeMap.remove(mode);
		} else {
			// override default setting
			modeMap.put(mode, perspective);
		}
	}
	
	/**
	 * Generates XML for the user specified perspective settings.
	 *  
	 * @return XML
	 * @exception IOException if unable to generate the XML
     * @exception TransformerException if unable to generate the XML
     * @exception ParserConfigurationException if unable to generate the XML
	 */
	private String generatePerspectiveXML() throws ParserConfigurationException, TransformerException, IOException {
		
		Document doc = DebugUIPlugin.getDocument();
		Element configRootElement = doc.createElement(ELEMENT_PERSPECTIVES);
		doc.appendChild(configRootElement);
		
		Iterator configTypes = fPreferenceMap.keySet().iterator();
		while (configTypes.hasNext()) {
			String type = (String)configTypes.next();
			Map modeMap = (Map)fPreferenceMap.get(type);
			if (modeMap != null && !modeMap.isEmpty()) {
				Iterator modes = modeMap.keySet().iterator();
				while (modes.hasNext()) {
					String mode = (String)modes.next();
					String perspective = (String)modeMap.get(mode);
					Element element = doc.createElement(ELEMENT_PERSPECTIVE);
					element.setAttribute(ATTR_TYPE_ID, type);
					element.setAttribute(ATTR_MODE_ID, mode);
					element.setAttribute(ATTR_PERSPECTIVE_ID, perspective);
					configRootElement.appendChild(element);
				}
			}			
		}

		return DebugUIPlugin.serializeDocument(doc);		
	}

	/**
	 * Returns the default perspective to switch to when a configuration of the given
	 * type is launched in the given mode, or <code>null</code> if none.
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode
	 * @return perspective identifier, or <code>null</code>
	 */
	protected String getDefaultLaunchPerspective(ILaunchConfigurationType type, String mode) {
		LaunchConfigurationTabGroupExtension extension = LaunchConfigurationPresentationManager.getDefault().getExtension(type.getIdentifier(), mode);
		if (extension != null) {
			String id = extension.getPerspective(mode);
			if (id == null) {
				// revert to hard coded default (for backwards compatibility)
				// since nothing is specified in XML
				if (mode.equals(ILaunchManager.DEBUG_MODE)) {
					return IDebugUIConstants.ID_DEBUG_PERSPECTIVE;
				}	
			} else {
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Returns the user specified perspective to switch to when a configuration of the
	 * given type is launched in the given mode, or <code>null</code> if unspecified.
	 * Returns <code>PERSPECTIVE_NONE</code> to indicate no switch
	 * 
	 * @param type launch configuration type
	 * @param mode launch mode
	 * @return perspective identifier, <code>PERSPECTIVE_NONE</code>, or <code>null</code>
	 */
	protected String getUserSpecifiedLaunchPerspective(ILaunchConfigurationType type, String mode) {
		String id = null;
		if (fPreferenceMap == null) {
			initPerspectives();
		}
		Map modeMap = (Map)fPreferenceMap.get(type.getIdentifier());
		if (modeMap != null) {
			id = (String)modeMap.get(mode);
		}
		return id;
	}

	/**
	 * Initialize the preference map with settings from user preference
	 */
	private void initPerspectives() {
		fPreferenceMap = new HashMap();
		String xml = DebugUIPlugin.getDefault().getPreferenceStore().getString(IInternalDebugUIConstants.PREF_LAUNCH_PERSPECTIVES);
		if (xml != null && xml.length() > 0) {
			try {
				Element root = null;
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				StringReader reader = new StringReader(xml);
				InputSource source = new InputSource(reader);
				root = parser.parse(source).getDocumentElement();
				
				NodeList list = root.getChildNodes();
				int length = list.getLength();
				for (int i = 0; i < length; ++i) {
					Node node = list.item(i);
					short nt = node.getNodeType();
					if (nt == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						String nodeName = element.getNodeName();
						if (nodeName.equalsIgnoreCase(ELEMENT_PERSPECTIVE)) {
							String type = element.getAttribute(ATTR_TYPE_ID);
							String mode = element.getAttribute(ATTR_MODE_ID);
							String perpsective = element.getAttribute(ATTR_PERSPECTIVE_ID);
							internalSetLaunchPerspective(type, mode, perpsective);
						}
					}
				}				
			} catch (ParserConfigurationException e) {
				DebugUIPlugin.log(e);			
			} catch (SAXException e) {
				DebugUIPlugin.log(e);
			} catch (IOException e) {
				DebugUIPlugin.log(e);
			}
		}
		
	}
	
}
