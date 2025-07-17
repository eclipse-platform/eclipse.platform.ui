/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import jakarta.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.EditorActionBars;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.testing.ContributionInfoMessages;
import org.eclipse.ui.part.AbstractMultiEditor;
import org.eclipse.ui.part.MultiEditor;
import org.eclipse.ui.part.MultiEditorInput;
import org.eclipse.ui.testing.ContributionInfo;

public class CompatibilityEditor extends CompatibilityPart {

	public static final String MODEL_ELEMENT_ID = "org.eclipse.e4.ui.compatibility.editor"; //$NON-NLS-1$

	private EditorReference reference;

	@Inject
	private EModelService modelService;

	@Inject
	CompatibilityEditor(MPart part, EditorReference ref) {
		super(part);
		reference = ref;
	}

	@Override
	IWorkbenchPart createPart(WorkbenchPartReference reference) throws PartInitException {
		IWorkbenchPart part = super.createPart(reference);
		if (part instanceof MultiEditor) {
			IEditorInput input = ((EditorReference) reference).getEditorInput();
			if (input instanceof MultiEditorInput) {
				createMultiEditorChildren(part, input);
			}
		}
		return part;
	}

	private void createMultiEditorChildren(IWorkbenchPart part, IEditorInput input) throws PartInitException {
		IWorkbenchPage page = reference.getPage();
		MPart model = getModel();
		MWindow window = modelService.getTopLevelWindowFor(model);
		IEditorRegistry registry = model.getContext().get(IEditorRegistry.class);
		MultiEditorInput multiEditorInput = (MultiEditorInput) input;
		IEditorInput[] inputs = multiEditorInput.getInput();
		String[] editorIds = multiEditorInput.getEditors();
		IEditorPart[] editors = new IEditorPart[editorIds.length];
		for (int i = 0; i < editorIds.length; i++) {
			EditorDescriptor innerDesc = (EditorDescriptor) registry.findEditor(editorIds[i]);
			if (innerDesc == null) {
				throw new PartInitException(
						NLS.bind(WorkbenchMessages.EditorManager_unknownEditorIDMessage, editorIds[i]));
			}

			EditorReference innerReference = new EditorReference(window.getContext(), page, model, inputs[i], innerDesc,
					null);
			editors[i] = (IEditorPart) innerReference.createPart();
			innerReference.initialize(editors[i]);
		}

		((AbstractMultiEditor) part).setChildren(editors);
	}

	@Override
	protected boolean createPartControl(final IWorkbenchPart legacyPart, Composite parent) {
		super.createPartControl(legacyPart, parent);

		clearMenuItems();

		part.getContext().set(IEditorPart.class, (IEditorPart) legacyPart);

		EditorDescriptor descriptor = reference.getDescriptor();
		if (descriptor != null) {
			IConfigurationElement element = descriptor.getConfigurationElement();
			if (element != null) {
				String iconURI = MenuHelper.getIconURI(element, IWorkbenchRegistryConstants.ATT_ICON);
				part.setIconURI(iconURI);
			}
			if (descriptor.getPluginId() != null) {
				parent.setData(new ContributionInfo(descriptor.getPluginId(),
						ContributionInfoMessages.ContributionInfo_Editor, null));
			}
		}

		if (legacyPart instanceof AbstractMultiEditor && !(legacyPart instanceof MultiEditor)) {
			try {
				createMultiEditorChildren(legacyPart, reference.getEditorInput());
			} catch (PartInitException e) {
				handlePartInitException(e);
				return false;
			}
		}

		return true;
	}

	public IEditorPart getEditor() {
		return (IEditorPart) getPart();
	}

	@Override
	public WorkbenchPartReference getReference() {
		return reference;
	}

	@Override
	void disposeSite(PartSite site) {
		EditorActionBars bars = (EditorActionBars) site.getActionBars();
		EditorReference.disposeEditorActionBars(bars);
		super.disposeSite(site);
	}
}
