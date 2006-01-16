/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.preferences;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.internal.ui.launchConfigurations.PerspectiveManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchViewerSorter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides one place to set perspective preferences
 * 
 * @since 3.2
 */
public class PerspectivePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {

	// constants
	private static final String DEBUG_LAUNCH_GROUP = "org.eclipse.debug.ui.launchGroup.debug"; //$NON-NLS-1$

	private static final String LAST_SELECTED_CONFIGTYPE = "last_selected"; //$NON-NLS-1$

	private static final int LABEL_WIDTH_HINT = 450;

	/**
	 * A composite to add and dispose fComboComposites from
	 */
	private Composite fPerspectiveComp = null;
	
	/**
	 * Launch configuration type to mode mapping. allows us to store all the
	 * changed information until ok, apply or cancel is pressed Formed as
	 * follows: Map<typeId,HashMap<modeId, perspective>>
	 */
	private HashMap fTypeInformationMapping = null;

	/**
	 * The list of perspective labels
	 */
	private String[] fPerspectiveLabels = null;

	/**
	 * A mapping of perspective labels to ids of the form Map<label, id>
	 */
	private HashMap fPerspectiveIds = null;

	/**
	 * the composite to add the combo boxes and their labels to
	 */
	private Composite fComboPlaceHolder = null;

	/**
	 * the viewer for the list of launch configurations
	 */
	private TreeViewer fViewer = null;

	/**
	 * The tree for the viewer
	 */
	private Tree fTree = null;

	/**
	 * Manages a launch configuration
	 */
	private ILaunchManager fLManager = DebugPlugin.getDefault().getLaunchManager();

	/**
	 * The global manager for all launch configurations
	 */
	private LaunchConfigurationManager fLCM = DebugUIPlugin.getDefault().getLaunchConfigurationManager();

	/**
	 * manages perspectives
	 */
	private PerspectiveManager fPmanager = DebugUIPlugin.getDefault().getPerspectiveManager();

	/**
	 * The field for switching perspective on run/debug
	 */
	private RadioGroupFieldEditor perspec = null;

	/**
	 * The field for switching when suspends
	 */
	private RadioGroupFieldEditor suspend = null;

	/**
	 * The currently selected ILaunchConfiguration from the tree
	 */
	private ILaunchConfigurationType fCurrentType = null;

	/**
	 * listens for widgets to change their selection. In this case we use it
	 * soley for setting changes in any of the combo boxes for perspective
	 * types.
	 */
	private SelectionAdapter fSelectionAdapter = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source instanceof Combo) {
				Combo combo = (Combo) source;
				HashMap map = (HashMap) fTypeInformationMapping.get(fCurrentType);
				if (map == null) {
					map = new HashMap();
				}// end if
				map.put(combo.getData(), fPerspectiveIds.get(combo.getText()));
				fTypeInformationMapping.put(fCurrentType, map);
			}// end if
		}// end widgetselected
	};

	/**
	 * Default constructor
	 */
	public PerspectivePreferencePage() {
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setTitle(DebugPreferencesMessages.PerspectivePreferencePage_6);
		setDescription(DebugPreferencesMessages.PerspectivePreferencePage_0);
	}// end constructor

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		perspec.store();
		suspend.store();
		for (Iterator iter = fTypeInformationMapping.keySet().iterator(); iter.hasNext();) {
			ILaunchConfigurationType typekey = (ILaunchConfigurationType)iter.next();
			HashMap map = (HashMap) fTypeInformationMapping.get(typekey);
			for (Iterator iter2 = map.keySet().iterator(); iter2.hasNext();) {
				String modekey = (String) iter2.next();
				String perspective = (String) map.get(modekey);
				if (perspective != null) {
					if (perspective.equals(DebugPreferencesMessages.PerspectivePreferencePage_4)) {
						perspective = IDebugUIConstants.PERSPECTIVE_NONE;
					}// end if
				}// end if
				else {
					perspective = IDebugUIConstants.PERSPECTIVE_NONE;
				}//end else
				fPmanager.setLaunchPerspective(typekey, modekey, perspective);
			}// end for
		}// end for
		store.setValue(LAST_SELECTED_CONFIGTYPE, fCurrentType.getName());
		DebugUIPlugin.getDefault().savePluginPreferences();
		return super.performOk();
	}// end performOK

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {}

	/**
	 * Restore the state from the preference store
	 */
	private void restoreState() {
		String xml = getPreferenceStore().getString(IInternalDebugUIConstants.PREF_LAUNCH_PERSPECTIVES);
//	bug 111485	in case a new plugin offers a launch type between invocations, do this prior to setting them to saved states
		setDefaultPerspectives();
		if (xml != null && xml.length() > 0) {
			try {
				Element root = null;
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				parser.setErrorHandler(new DefaultHandler());
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
						if (nodeName.equalsIgnoreCase(PerspectiveManager.ELEMENT_PERSPECTIVE)) {
							String type = element.getAttribute(PerspectiveManager.ATTR_TYPE_ID);
							ILaunchConfigurationType typeobj = fLManager.getLaunchConfigurationType(type);
							String mode = element.getAttribute(PerspectiveManager.ATTR_MODE_ID);
							if(typeobj != null) {
								String perspective = fPmanager.getLaunchPerspective(typeobj, mode);
								HashMap map = (HashMap) fTypeInformationMapping.get(typeobj);
								if(map == null) {
									map = new HashMap();
								}//end if
								map.put(mode, (perspective != null ? perspective : IDebugUIConstants.PERSPECTIVE_NONE));
								fTypeInformationMapping.put(typeobj, map);
							}//end if
						}// end if
					}// end if
				}// end for
			}// end try
			catch (ParserConfigurationException e) {DebugUIPlugin.log(e);} 
			catch (SAXException e) {DebugUIPlugin.log(e);}
			catch (IOException e) {DebugUIPlugin.log(e);}
		}// end if
		perspec.load();
		suspend.load();
		TreeItem item = findLastSelected(getPreferenceStore().getString(LAST_SELECTED_CONFIGTYPE));
		if (item != null) {
			fTree.setSelection(new TreeItem[] { item });
		}// end if
		else {
			fTree.setSelection(new TreeItem[] { fTree.getItem(0) });
		}// end else
		fCurrentType = (ILaunchConfigurationType) ((IStructuredSelection) fViewer.getSelection()).getFirstElement();
		buildComboBoxes(fCurrentType);
	}// restoreState

	/**
	 * Sets the default perspectives for the modes of an ILaunchConfiguration
	 */
	private void setDefaultPerspectives() {
		ILaunchConfigurationType[] types = fLManager.getLaunchConfigurationTypes();
		ArrayList modes = null;
		HashMap map = null;
		for (int i = 0; i < types.length; i++) {
			modes = new ArrayList(types[i].getSupportedModes());
			map = new HashMap();
			for (int j = 0; j < modes.size(); j++) {
				String mode = (String) modes.get(j);
				String persp = fPmanager.getDefaultLaunchPerspective(types[i], mode);
				map.put(mode, (persp != null ? persp : IDebugUIConstants.PERSPECTIVE_NONE));
			}// end for
			fTypeInformationMapping.put(types[i], map);
		}// end for
	}//setDefaultPerspective

	/**
	 * Finds the ILaunchConfiguration within the tree based on its text
	 * 
	 * @param last
	 *            the name of the last Launch Configuration selected
	 * @return the treeitem if found or null
	 */
	private TreeItem findLastSelected(String last) {
		TreeItem[] selection = fTree.getItems();
		for (int i = 0; i < selection.length; i++) {
			if (selection[i].getText().equals(last)) {
				return selection[i];
			}// end if
		}// end for
		return null;
	}// end findLastSelected

	/**
	 * Builds all possible combo boxes per supported modes
	 */
	private void buildComboBoxes(ILaunchConfigurationType type) {
		HashMap launchmodes = (HashMap) fTypeInformationMapping.get(type);
		if (fComboPlaceHolder != null) {
			fComboPlaceHolder.dispose();
		}// end if
		Font font = fPerspectiveComp.getFont();
		fComboPlaceHolder = new Composite(fPerspectiveComp, SWT.NONE);
		fComboPlaceHolder.setLayout(new GridLayout(2, false));
		fComboPlaceHolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		fComboPlaceHolder.setFont(font);
		Label label = new Label(fComboPlaceHolder, SWT.NONE);
		label.setText(DebugPreferencesMessages.PerspectivePreferencePage_2);
		label.setFont(font);
		GridData gd = null;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		for(Iterator iter = launchmodes.keySet().iterator(); iter.hasNext();) {
			String modekey = (String)iter.next();
			String persp = (String)launchmodes.get(modekey);
       // build label
			label = new Label(fComboPlaceHolder, SWT.NONE);
			label.setFont(font);
			gd = new GridData(GridData.BEGINNING);
			label.setLayoutData(gd);
			ILaunchMode mode = fLManager.getLaunchMode(modekey);
			String clabel = mode.getLabel();
			//resolve conflict with Default and Debug mneumonics bug 122882
			if(clabel.equals(DebugPreferencesMessages.PerspectivePreferencePage_7)) {
				clabel = DebugPreferencesMessages.PerspectivePreferencePage_8;
			}
			label.setText(MessageFormat.format(DebugPreferencesMessages.PerspectivePreferencePage_3, new String[] { clabel }));
		// build combobox
			Combo combo = new Combo(fComboPlaceHolder, SWT.READ_ONLY);
			combo.setFont(font);
			combo.setItems(fPerspectiveLabels);
			combo.setData(modekey);
			gd = new GridData(GridData.BEGINNING);
			combo.setLayoutData(gd);
			if(persp.equals(IDebugUIConstants.PERSPECTIVE_NONE)) {
				persp = DebugPreferencesMessages.PerspectivePreferencePage_4;
			}//end if
			else {
				IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(persp);
				persp = (desc != null ? desc.getLabel() : DebugPreferencesMessages.PerspectivePreferencePage_4);
			}//end else
			combo.setText(persp);
			combo.addSelectionListener(fSelectionAdapter);
		}//end for
		fPerspectiveComp.layout();
	}// buildComboBoxes

	/**
	 * Handles the change in selection from the launch configuration listing
	 * 
	 * @param event
	 *            the selection changed event
	 */
	private void handleLaunchConfigurationSelectionChanged(SelectionChangedEvent event) {
		// handle prompting and saving before moving on.
		ILaunchConfigurationType type = (ILaunchConfigurationType) ((IStructuredSelection) event.getSelection()).getFirstElement();
		if(!type.equals(fCurrentType)) {
			//if they are the same do nothing
			fCurrentType = type;
			buildComboBoxes(fCurrentType);
		}
	}// end handleLaunchConfigurationSelectionChanged

	/**
	 * Gets the perspective labels
	 * 
	 * @return the (label, id) mappings of perspectives
	 */
	private void getPerspectiveLabels() {
		IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] descriptors = registry.getPerspectives();
		fPerspectiveLabels = new String[descriptors.length + 1];
		fPerspectiveLabels[0] = DebugPreferencesMessages.PerspectivePreferencePage_4;
		fPerspectiveIds = new HashMap();
		for (int i = 0; i < descriptors.length; i++) {
			fPerspectiveLabels[i + 1] = descriptors[i].getLabel();
			fPerspectiveIds.put(descriptors[i].getLabel(), descriptors[i].getId());
		}// end for
	}// end getPerspectiveLabels

	/**
	 * Simple method to create a spacer in the page
	 * 
	 * @param composite
	 *            the composite to add the spacer to
	 * @param columnSpan
	 *            the amount of space for the spacer
	 */
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}// end createSpacer

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		perspec.loadDefault();
		suspend.loadDefault();
		setDefaultPerspectives();
		buildComboBoxes(fCurrentType);
		super.performDefaults();
	}// end performdefaults

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.PERSPECTIVE_PREFERENCE_PAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
	// create the global radio buttons
		createSpacer(composite, 1);
		perspec = new RadioGroupFieldEditor(
				IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE,
				DebugPreferencesMessages.LaunchingPreferencePage_11, 3,
				new String[][] {{DebugPreferencesMessages.LaunchingPreferencePage_12, MessageDialogWithToggle.ALWAYS },
							{ DebugPreferencesMessages.LaunchingPreferencePage_13, MessageDialogWithToggle.NEVER },
							{ DebugPreferencesMessages.LaunchingPreferencePage_14, MessageDialogWithToggle.PROMPT } }, 
							composite,
							true);
		perspec.setPreferenceName(IInternalDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE);
		perspec.setPreferenceStore(getPreferenceStore());
		suspend = new RadioGroupFieldEditor(
				IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND,
				DebugPreferencesMessages.DebugPreferencePage_21, 3,
				new String[][] {{ DebugPreferencesMessages.DebugPreferencePage_22, MessageDialogWithToggle.ALWAYS },
								{ DebugPreferencesMessages.DebugPreferencePage_23, MessageDialogWithToggle.NEVER },
								{ DebugPreferencesMessages.DebugPreferencePage_24, MessageDialogWithToggle.PROMPT } }, 
								composite,
								true);
		suspend.setPreferenceName(IInternalDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND);
		suspend.setPreferenceStore(getPreferenceStore());
		
		createSpacer(composite, 1);
		
		Label lbl = new Label(composite, SWT.LEFT + SWT.WRAP);
		lbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		((GridData)lbl.getLayoutData()).widthHint = LABEL_WIDTH_HINT;
		lbl.setText(MessageFormat.format(DebugPreferencesMessages.PerspectivePreferencePage_5, null));
		
		Composite comp = new Composite(composite, SWT.FILL);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite treeComp = new Composite(comp, SWT.NONE);
		treeComp.setLayout(new GridLayout(1, true));
		treeComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label label = new Label(treeComp, SWT.NONE);
		label.setText(DebugPreferencesMessages.PerspectivePreferencePage_1);
		
		fTree = new Tree(treeComp, SWT.H_SCROLL + SWT.V_SCROLL + SWT.SINGLE + SWT.BORDER);
		fTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewer = new TreeViewer(fTree);
		fViewer.setLabelProvider(DebugUITools.newDebugModelPresentation());
		fViewer.setSorter(new WorkbenchViewerSorter());
		fViewer.setContentProvider(new LaunchConfigurationTreeContentProviderNoChildren(null, parent.getShell()));
		fViewer.addFilter(new LaunchGroupFilter(fLCM.getLaunchGroup(DEBUG_LAUNCH_GROUP)));
		fViewer.setInput(fLManager.getLaunchConfigurationTypes());
		fViewer.expandAll();
		fViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						handleLaunchConfigurationSelectionChanged(event);
					}//end selectionChanged
				});
		fPerspectiveComp = new Composite(comp, SWT.NONE);
		fPerspectiveComp.setLayout(new GridLayout(1, true));
		fPerspectiveComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTypeInformationMapping = new HashMap();
		// get the available labels
		getPerspectiveLabels();
		// restore from preference store
		restoreState();
        
		Dialog.applyDialogFont(composite);
		return composite;
	}// end createControl

}// end class
