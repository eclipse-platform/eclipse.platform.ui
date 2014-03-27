/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Nikolay Botev - bug 240651
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.editorsupport.ComponentSupport;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.util.Util;

public class EditorReference extends WorkbenchPartReference implements IEditorReference {

	private IEditorInput input;
	private EditorDescriptor descriptor;
	private String descriptorId;
	private IMemento editorState;

	public EditorReference(IEclipseContext windowContext, IWorkbenchPage page, MPart part,
			IEditorInput input, EditorDescriptor descriptor, IMemento editorState) {
		super(windowContext, page, part);
		this.input = input;
		this.descriptor = descriptor;
		this.editorState = editorState;

		if (descriptor == null) {
			try {
				String memento = getModel().getPersistedState().get(MEMENTO_KEY);
				if (memento == null) {
					descriptorId = EditorRegistry.EMPTY_EDITOR_ID;
				} else {
					XMLMemento createReadRoot = XMLMemento
							.createReadRoot(new StringReader(memento));
					IEditorRegistry registry = getPage().getWorkbenchWindow().getWorkbench()
							.getEditorRegistry();
					descriptorId = createReadRoot.getString(IWorkbenchConstants.TAG_ID);
					this.descriptor = (EditorDescriptor) registry.findEditor(descriptorId);

					boolean pinnedVal = "true".equals(createReadRoot.getString(IWorkbenchConstants.TAG_PINNED)); //$NON-NLS-1$
					setPinned(pinnedVal);

					String ttip = createReadRoot.getString(IWorkbenchConstants.TAG_TOOLTIP);
					part.getTransientData().put(IPresentationEngine.OVERRIDE_TITLE_TOOL_TIP_KEY,
							ttip);
				}
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log(e);
			}
		} else {
			descriptorId = this.descriptor.getId();
		}
	}

	boolean persist() {
		XMLMemento persistedState = (XMLMemento) getEditorState();
		if (persistedState == null)
			return false;

		StringWriter writer = new StringWriter();
		try {
			persistedState.save(writer);
			getModel().getPersistedState().put(MEMENTO_KEY, writer.toString());
		} catch (IOException e) {
			WorkbenchPlugin.log(e);
			return false;
		}

		return true;
	}

	IMemento getEditorState() {
		IEditorPart editor = getEditor(false);

		// If the editor hasn't been rendered yet then see if we can grab the
		// info from the model
		if (editor == null && getModel() != null) {
			String savedState = getModel().getPersistedState().get(MEMENTO_KEY);
			if (savedState != null) {
				StringReader sr = new StringReader(savedState);
				try {
					XMLMemento memento = XMLMemento.createReadRoot(sr);
					return memento;
				} catch (WorkbenchException e) {
					WorkbenchPlugin.log(e);
					return null;
				}
			}
			return null;
		}

		IEditorInput input = editor.getEditorInput();
		if (input == null) {
			return null;
		}

		IPersistableElement persistable = input.getPersistable();
		if (persistable == null) {
			return null;
		}

		XMLMemento editorMem = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
		editorMem.putString(IWorkbenchConstants.TAG_ID, descriptor.getId());
		editorMem.putString(IWorkbenchConstants.TAG_TITLE, getTitle());
		editorMem.putString(IWorkbenchConstants.TAG_NAME, getName());
		editorMem.putString(IWorkbenchConstants.TAG_ID, getId());
		editorMem.putString(IWorkbenchConstants.TAG_TOOLTIP, getTitleToolTip());
		editorMem.putString(IWorkbenchConstants.TAG_PART_NAME, getPartName());

		if (editor instanceof IWorkbenchPart3) {
			Map properties = ((IWorkbenchPart3) editor).getPartProperties();
			if (!properties.isEmpty()) {
				IMemento propBag = editorMem.createChild(IWorkbenchConstants.TAG_PROPERTIES);
				Iterator i = properties.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry entry = (Map.Entry) i.next();
					IMemento p = propBag.createChild(IWorkbenchConstants.TAG_PROPERTY,
							(String) entry.getKey());
					p.putTextData((String) entry.getValue());
				}
			}
		}

		if (isPinned()) {
			editorMem.putString(IWorkbenchConstants.TAG_PINNED, "true"); //$NON-NLS-1$
		}

		IMemento inputMem = editorMem.createChild(IWorkbenchConstants.TAG_INPUT);
		inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
		persistable.saveState(inputMem);

		if (editor instanceof IPersistableEditor) {
			IMemento editorStateMem = editorMem.createChild(IWorkbenchConstants.TAG_EDITOR_STATE);
			((IPersistableEditor) editor).saveState(editorStateMem);
		}

		return editorMem;
	}

	public EditorDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public String getId() {
		// by default we delegate to the model part, which is not correct for
		// editors
		return descriptorId;
	}

	@Override
	public String getFactoryId() {
		IEditorPart editor = getEditor(false);
		if (editor == null) {
			if (input == null) {
				String memento = getModel().getPersistedState().get(MEMENTO_KEY);
				if (memento != null) {
					try {
						XMLMemento createReadRoot = XMLMemento.createReadRoot(new StringReader(
								memento));
						IMemento inputMem = createReadRoot.getChild(IWorkbenchConstants.TAG_INPUT);
						if (inputMem != null) {
							return inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
						}
					} catch (WorkbenchException e) {
						return null;
					}
				}
				return null;
			}

			IPersistableElement persistable = input.getPersistable();
			return persistable == null ? null : persistable.getFactoryId();
		}

		IPersistableElement persistable = editor.getEditorInput().getPersistable();
		return persistable == null ? null : persistable.getFactoryId();
	}

	@Override
	public String getName() {
		IEditorPart editor = getEditor(false);
		if (input == null) {
			return editor == null ? getModel().getLocalizedLabel() : editor.getEditorInput()
					.getName();
		}
		return editor == null ? input.getName() : editor.getEditorInput().getName();
	}

	@Override
	public String getTitle() {
		String label = Util.safeString(getModel().getLocalizedLabel());
		if (label.length() == 0) {
			if (input == null) {
				if (descriptor != null) {
					return descriptor.getLabel();
				}
			} else {
				return Util.safeString(input.getName());
			}
		}
		return label;
	}

	private IEditorInput restoreInput(IMemento editorMem) throws PartInitException {
		return createInput(editorMem);
	}

	public static IEditorInput createInput(IMemento editorMem)
			throws PartInitException {
		String editorId = editorMem.getString(IWorkbenchConstants.TAG_ID);

		IMemento inputMem = editorMem.getChild(IWorkbenchConstants.TAG_INPUT);

		String editorName = null;
		String factoryID = null;
		if (inputMem != null) {
			editorName = inputMem.getString(IWorkbenchConstants.TAG_PATH);
			factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		}
		if (factoryID == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_no_input_factory_ID, editorId, editorName));
		}
		IAdaptable input = null;
		IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
		if (factory == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_bad_element_factory, new Object[] { factoryID,
							editorId, editorName }));
		}

		// Get the input element.
		input = factory.createElement(inputMem);
		if (input == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_create_element_returned_null, new Object[] {
							factoryID, editorId, editorName }));
		}
		if (!(input instanceof IEditorInput)) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_wrong_createElement_result, new Object[] {
							factoryID, editorId, editorName }));
		}
		return (IEditorInput) input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#getEditor(boolean)
	 */
	@Override
	public IEditorPart getEditor(boolean restore) {
		return (IEditorPart) getPart(restore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorReference#getEditorInput()
	 */
	@Override
	public IEditorInput getEditorInput() throws PartInitException {
		IEditorPart editor = getEditor(false);
		if (editor != null) {
			return editor.getEditorInput();
		}

		if (input == null) {
			String memento = getModel().getPersistedState().get(MEMENTO_KEY);
			if (memento == null) {
				input = new NullEditorInput();
			} else {
				try {
					XMLMemento createReadRoot = XMLMemento
							.createReadRoot(new StringReader(memento));
					input = restoreInput(createReadRoot);
				} catch (WorkbenchException e) {
					throw new PartInitException(e.getStatus());
				}
			}
		}
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.WorkbenchPartReference#createPart
	 * ()
	 */
	@Override
	public IWorkbenchPart createPart() throws PartInitException {
		try {
			if (descriptor == null) {
				return createErrorPart();
			} else if (descriptor.getId().equals(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID)) {
				IEditorPart part = ComponentSupport.getSystemInPlaceEditor();
				if (part == null) {
					throw new PartInitException(WorkbenchMessages.EditorManager_no_in_place_support);
				}
				return part;
			}
			return descriptor.createEditor();
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			throw new PartInitException(new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH,
					status.getCode(), status.getMessage(), e));
		}
	}

	@Override
	IWorkbenchPart createErrorPart() {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, NLS.bind(
				WorkbenchMessages.EditorManager_missing_editor_descriptor, descriptorId));
		IEditorRegistry registry = getPage().getWorkbenchWindow().getWorkbench()
				.getEditorRegistry();
		descriptor = (EditorDescriptor) registry.findEditor(EditorRegistry.EMPTY_EDITOR_ID);
		return createErrorPart(status);
	}

	@Override
	public IWorkbenchPart createErrorPart(IStatus status) {
		return new ErrorEditorPart(status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.WorkbenchPartReference#initialize
	 * (org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void initialize(IWorkbenchPart part) throws PartInitException {
		IConfigurationElement element = descriptor.getConfigurationElement();
		EditorSite editorSite = new EditorSite(getModel(), part, this, element);
		if (element == null) {
			editorSite.setExtensionId(descriptor.getId());
		}
		editorSite.setActionBars(createEditorActionBars((WorkbenchPage) getPage(), descriptor,
				editorSite));
		IEditorPart editor = (IEditorPart) part;
		try {
			editor.init(editorSite, getEditorInput());
		} catch (PartInitException e) {
			if (editor instanceof ErrorEditorPart) {
				editor.init(editorSite, new NullEditorInput(this));
			} else {
				throw e;
			}
		}

		if (editor.getSite() != editorSite || editor.getEditorSite() != editorSite) {
			String id = descriptor == null ? getModel().getElementId() : descriptor.getId();
			throw new PartInitException(NLS.bind(WorkbenchMessages.EditorManager_siteIncorrect, id));
		}

		if (part instanceof IPersistableEditor) {
			if (editorState != null) {
				((IPersistableEditor) part).restoreState(editorState);
			} else if (useIPersistableEditor()) {
				String mementoString = getModel().getPersistedState().get(MEMENTO_KEY);
				if (mementoString != null) {
					try {
						IMemento createReadRoot = XMLMemento.createReadRoot(new StringReader(
								mementoString));
						IMemento editorStateMemento = createReadRoot
								.getChild(IWorkbenchConstants.TAG_EDITOR_STATE);
						if (editorStateMemento != null) {
							((IPersistableEditor) part).restoreState(editorStateMemento);
						}
					} catch (WorkbenchException e) {
						throw new PartInitException(e.getStatus());
					}
				}
			}
		}

		legacyPart = part;
		addPropertyListeners();
	}

	@Override
	public PartSite getSite() {
		if (legacyPart != null) {
			return (PartSite) legacyPart.getSite();
		}
		return null;
	}

	private static HashMap<String, Set<EditorActionBars>> actionCache = new HashMap<String, Set<EditorActionBars>>();

	/*
	 * Creates the action bars for an editor. Editors of the same type should
	 * share a single editor action bar, so this implementation may return an
	 * existing action bar vector.
	 */
	private static EditorActionBars createEditorActionBars(WorkbenchPage page,
			EditorDescriptor desc, final IEditorSite site) {
		// Get the editor type.
		String type = desc.getId();

		// If an action bar already exists for this editor type return it.
		Set<EditorActionBars> candidates = actionCache.get(type);
		if (candidates != null) {
			for (EditorActionBars candidate : candidates) {
				if (candidate.getPage() == page) {
					candidate.addRef();
					return candidate;
				}
			}
		}

		// Create a new action bar set.
		EditorActionBars actionBars = new EditorActionBars(page, page.getWorkbenchWindow(), type);
		actionBars.addRef();
		if (candidates == null) {
			candidates = new HashSet<EditorActionBars>(3);
			candidates.add(actionBars);
			actionCache.put(type, candidates);
		} else
			candidates.add(actionBars);

		// Read base contributor.
		IEditorActionBarContributor contr = desc.createActionBarContributor();
		if (contr != null) {
			actionBars.setEditorContributor(contr);
			contr.init(actionBars, page);
		}

		// Read action extensions.
		EditorActionBuilder builder = new EditorActionBuilder();
		contr = builder.readActionExtensions(desc);
		if (contr != null) {
			actionBars.setExtensionContributor(contr);
			contr.init(actionBars, page);
		}

		// Return action bars.
		return actionBars;
	}

	public static void disposeEditorActionBars(EditorActionBars actionBars) {
		actionBars.removeRef();
		if (actionBars.getRef() <= 0) {
			String type = actionBars.getEditorType();
			Set<EditorActionBars> set = actionCache.get(type);
			if (set != null) {
				set.remove(actionBars);
			}
			// refresh the cool bar manager before disposing of a cool item
			ICoolBarManager2 coolBar = (ICoolBarManager2) ((WorkbenchWindow) actionBars.getPage()
					.getWorkbenchWindow()).getCoolBarManager2();
			if (coolBar != null) {
				coolBar.refresh();
			}
			actionBars.dispose();
		}
	}

	private static boolean useIPersistableEditor() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(IPreferenceConstants.USE_IPERSISTABLE_EDITORS);
	}
}
