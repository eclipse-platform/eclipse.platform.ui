/*******************************************************************************
 * Copyright (c) 2015-2015 Veselin Markov.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Veselin Markov <veselin_m84@yahoo.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ComponentLabelProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.ModelEditor;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog presents to the user in a table the available {@link MCommandParameter}s that
 * may be referenced by the specified {@link MParameter}.
 *
 * @author Markov
 * @noinstantiate this dialog uses DI an should not be instantiated by the user.
 */
public class ParameterIdSelectionDialog extends AbstractIdDialog<MParameter, MCommandParameter> {

	@Inject
	protected ModelEditor editor;

	/** The parameter, which {@link MParameter#getName() name} feature has to be modified. */
	protected MParameter parameter;
	protected Set<EStructuralFeature> parametersFeatures = new HashSet<EStructuralFeature>();
	protected Set<EStructuralFeature> commandsFeatures = new HashSet<EStructuralFeature>();

	@Inject
	public ParameterIdSelectionDialog(Shell parentShell, IModelResource resource, MParameter parameter,
		EditingDomain domain, EModelService modelService, @Translation Messages messages) {
		super(parentShell, resource, parameter, domain, modelService, messages);

		this.parameter = parameter;
		parametersFeatures.add(MenuPackageImpl.Literals.HANDLED_ITEM__PARAMETERS);
		commandsFeatures.add(MenuPackageImpl.Literals.HANDLED_ITEM__COMMAND);
	}

	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new ComponentLabelProvider(editor, messages);
	}

	@Override
	protected String getShellTitle() {
		return messages.ParameterIdSelectionDialog_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return messages.ParameterIdSelectionDialog_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return messages.ParameterIdSelectionDialog_DialogMessage;
	}

	@Override
	protected String getLabelText() {
		return messages.ParameterIdSelectionDialog_LabelText;
	}

	@Override
	protected List<MCommandParameter> getViewerInput() {
		return getParametersOfParentNodesCommand();
	}

	@Override
	protected EAttribute getFeatureLiteral() {
		return CommandsPackageImpl.Literals.PARAMETER__NAME;
	}

	@Override
	protected String getListItemInformation(MCommandParameter listItem) {
		return null;
	}

	/**
	 * Reads the parameters of the MComand in the {@link EObject} referencing the given {@link #parameter}.
	 *
	 * Searches for the parent element of {@linkplain #parameter}. Once found reads the {@link MCommandParameter}s of
	 * the referenced {@link MCommand}, given the found parent references a MCommand.
	 *
	 * @return all found MCommandParameters or an empty {@link List}, never a {@code null} value.
	 */
	protected List<MCommandParameter> getParametersOfParentNodesCommand() {
		TreeIterator<EObject> it = EcoreUtil.getAllContents((EObject) resource.getRoot().get(0), true);
		while (it.hasNext()) {
			EObject containerObjectWithCommand = it.next();
			if (containerObjectWithCommand != null && canSupplyParameters(containerObjectWithCommand)) {
				List<MCommandParameter> commandParameters = getCommandParameters(containerObjectWithCommand);
				return commandParameters;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Checks if the given {@code object} references a {@link MCommand} and the {@link #parameter}.
	 *
	 * @param object the object to be checked. May not be {@code null}.
	 * @return {@code true} if {@code object} is a model-element that references a command and a list of parameters with
	 *         {@link #parameter}, {@code false} otherwise.
	 */
	protected boolean canSupplyParameters(EObject object) {
		return referencesCommand(object) && referencesParameters(object)
			&& containsSearchedParameter(object);
	}

	/**
	 * Checks whether the given {@code object} has a {@link EReference} to a {@link MCommand} feature.
	 *
	 * @param object that will be checked. May not be {@code null}.
	 * @return {@code true} if the given {@code object} can reference a MCommand, {@code false} otherwise.
	 */
	protected boolean referencesCommand(EObject object) {
		EList<EReference> eAllReferences = object.eClass().getEAllReferences();
		for (EReference r : eAllReferences) {
			if (commandsFeatures.contains(r)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given {@code object} has a {@link EReference} to a list of {@link MParameter}s feature.
	 *
	 * @param object that will be checked. May not be {@code null}.
	 * @return {@code true} if the given {@code object} can reference a MParameters, {@code false} otherwise.
	 */
	protected boolean referencesParameters(EObject object) {
		EList<EReference> eAllReferences = object.eClass().getEAllReferences();
		for (EReference r : eAllReferences) {
			if (parametersFeatures.contains(r)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the {@link #parameter} is contained in the {@link MParameter}s feature of the given {@code object}.
	 *
	 * @param object that may contain the {@code parameter} we look for.
	 * @return true if {@code object} contains the {@code parameter}, false otherwise.
	 */
	protected boolean containsSearchedParameter(EObject object) {
		for (EStructuralFeature parametersFeature : parametersFeatures) {
			Object parameters = object.eGet(parametersFeature);
			if (parameters != null && parameters instanceof Collection<?>) {
				return ((Collection<?>) parameters).contains(parameter);
			}
		}
		return false;
	}

	/**
	 * Reads the {@link MCommandParameter}s from the {@link MCommand} from the given {@code containerObjectWithCommand}.
	 *
	 * @param containerObjectWithCommand an object containing a {@link EReference} to an {@link MCommand}
	 * @return a {@link List} with the {@link MCommandParameter}s or an empty list but never {@code null} value.
	 *
	 * @throws IllegalArgumentException if {@code containerObjectWithCommand} contains no {@link MCommand}. See
	 *             {@link #referencesCommand(EObject)}.
	 */
	protected List<MCommandParameter> getCommandParameters(EObject containerObjectWithCommand) {
		Object command = containerObjectWithCommand.eGet(MenuPackageImpl.Literals.HANDLED_ITEM__COMMAND);

		if (command != null && command instanceof MCommand) {
			List<MCommandParameter> parameters = ((MCommand) command).getParameters();
			if (parameters != null)
				return parameters;
			return Collections.emptyList();
		}
		throw new IllegalArgumentException("The parameter contains no MCommand"); //$NON-NLS-1$
	}
}
