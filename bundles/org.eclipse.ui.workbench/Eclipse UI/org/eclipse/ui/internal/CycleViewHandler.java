/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 454143, 461063, 495917
 *     Friederike Schertel <friederike@schertel.org> - Bug 478336
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 504089, 509224, 509232
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.commands.ICommandService;

/**
 * This handler is used to switch between parts using the keyboard.
 * <p>
 * Replacement for CyclePartAction
 * </p>
 *
 * @since 3.3
 *
 */
public class CycleViewHandler extends FilteredTableBaseHandler {

	@Override
	protected Object getInput(WorkbenchPage page) {
		List<IWorkbenchPartReference> refs = new ArrayList<>();

		EPartService partService = page.getWorkbenchWindow().getService(EPartService.class);
		EModelService modelService = page.getWorkbenchWindow().getService(EModelService.class);
		MPerspective currentPerspective = page.getCurrentPerspective();

		List<MPart> parts = modelService.findElements(currentPerspective, null, MPart.class, null,
				EModelService.PRESENTATION);

		AtomicBoolean includeEditor = new AtomicBoolean(true);

		parts.stream().sorted((firstPart, secondPart) -> {
			Long firstPartActivationTime = (Long) firstPart.getTransientData()
					.getOrDefault(PartServiceImpl.PART_ACTIVATION_TIME, Long.MIN_VALUE);
			Long secondPartActivationTime = (Long) secondPart.getTransientData()
					.getOrDefault(PartServiceImpl.PART_ACTIVATION_TIME, Long.MIN_VALUE);
			// use decreasing order by inverting the result using "-" at the beginning
			return -(firstPartActivationTime.compareTo(secondPartActivationTime));
		}).forEach(part -> {
			if (!partService.isPartOrPlaceholderInPerspective(part.getElementId(), currentPerspective)) {
				return;
			}
			if (part.getTags().contains("Editor")) { //$NON-NLS-1$
				if (includeEditor.getAndSet(false)) {
					addExistingReference(refs, part);
				}
			} else {
				addExistingReference(refs, part);
			}
		});
		return refs;

	}

	/*
	 * Specialized to get the static label that was shown in the past (509232)
	 */
	@Override
	protected String getWorkbenchPartReferenceText(WorkbenchPartReference ref) {
		if (ref instanceof EditorReference) {
			return WorkbenchMessages.CyclePartAction_editor;
		} else if (ref instanceof ViewReference) {
			return ref.getPartName();
		}
		return super.getWorkbenchPartReferenceText(ref);
	}

	/**
	 * Adds the {@link IWorkbenchPartReference} contained in part's transient
	 * data, if exists.
	 */
	protected void addExistingReference(List<IWorkbenchPartReference> refs, MPart part) {
		Object tData = part.getTransientData().get(IWorkbenchPartReference.class.getName());
		if (tData instanceof IWorkbenchPartReference) {
			// instanceof checks also for non null values
			refs.add((IWorkbenchPartReference) tData);
		}
	}

	@Override
	protected ParameterizedCommand getBackwardCommand() {
		return getParametrizedCommand(IWorkbenchCommandConstants.WINDOW_PREVIOUS_VIEW);
	}

	@Override
	protected ParameterizedCommand getForwardCommand() {
		return getParametrizedCommand(IWorkbenchCommandConstants.WINDOW_NEXT_VIEW);
	}

    private ParameterizedCommand getParametrizedCommand(String workbenchCommand)
	{
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(workbenchCommand);
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(command, null);
		return parameterizedCommand;
	}

	@Override
	protected String getTableHeader(IWorkbenchPart activePart) {
		return WorkbenchMessages.CyclePartAction_header;
	}

}
