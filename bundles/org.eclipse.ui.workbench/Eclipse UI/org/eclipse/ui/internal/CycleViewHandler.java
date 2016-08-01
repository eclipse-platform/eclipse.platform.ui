/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.SWTPartRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;

/**
 * This handler is used to switch between parts using the keyboard.
 * <p>
 * Replacement for CyclePartAction
 * </p>
 *
 * @since 3.3
 *
 */
public class CycleViewHandler extends CycleBaseHandler {

	@Override
	protected void addItems(Table table, WorkbenchPage page) {

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
					createEditorItem(table, page, part);
				}
			} else {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(part.getLabel());
				IWorkbenchWindow iwbw = page.getWorkbenchWindow();
				if (iwbw instanceof WorkbenchWindow) {
					WorkbenchWindow wbw = (WorkbenchWindow) iwbw;
					if (part != null && wbw.getModel().getRenderer() instanceof SWTPartRenderer) {
						SWTPartRenderer r = (SWTPartRenderer) wbw.getModel().getRenderer();
						item.setImage(r.getImage(part));
					}
				}
				item.setData(part);
			}
		});

	}

	private void createEditorItem(Table table, WorkbenchPage page, MPart part) {
		Object object = part.getObject();
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(WorkbenchMessages.CyclePartAction_editor);
		if (object instanceof CompatibilityEditor) {
			IEditorPart editor = ((CompatibilityEditor) object).getEditor();
			item.setImage(editor.getTitleImage());
			if (editor.getSite() instanceof PartSite) {
				item.setData(((PartSite) editor.getSite()).getPartReference());
				return;
			}
		} else {
			item.setImage(getImage(page, part));
		}
		item.setData(part);
	}

	private Image getImage(WorkbenchPage page, MPart part) {
		Object renderer = part.getRenderer();
		if (renderer instanceof SWTPartRenderer) {
			SWTPartRenderer partRenderer = (SWTPartRenderer) renderer;
			return partRenderer.getImage(part);
		}
		WorkbenchWindow wbw = (WorkbenchWindow) page.getWorkbenchWindow();
		if (wbw.getModel().getRenderer() instanceof SWTPartRenderer) {
			SWTPartRenderer partRenderer = (SWTPartRenderer) wbw.getModel().getRenderer();
			return partRenderer.getImage(part);
		}

		return null;
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
