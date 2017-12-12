/*******************************************************************************
 * Copyright (c) 2017 InterSystems Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     InterSystems Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Assert;

/**
 * Test legacy model for compatibility with
 * org.eclipse.e4.ui.internal.workbench.ModelAssembler.resolveImports()
 *
 * resolveImports() requires importable model elements to have unique element ID
 *
 * @see https://wiki.eclipse.org/Eclipse4/RCP/Modeled_UI/Contributing_to_the_Model
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=469595
 */
public class Bug469595Test extends UITestCase {
	/**
	 * @param testName
	 */
	public Bug469595Test(String testName) {
		super(testName);
	}

	/**
	 * Key bindings can't be contributed via model fragment without properly
	 * imported binding context, so we need to be sure binding contexts are not
	 * shadowed by other model parts
	 */
	public void testBindingContext() {
		HashMap<String, Set<MApplicationElement>> elementsById = collectModelElements();

		elementsById.values().removeIf(elements -> !elements.stream().anyMatch(MBindingContext.class::isInstance));
		assertUnique(elementsById);
	}

	/**
	 * Command handlers can't be contributed via model fragments unless command is
	 * imported, so we need to be sure commands are not shadowed by other model
	 * parts.
	 */
	public void testCommands() {
		HashMap<String, Set<MApplicationElement>> elementsById = collectModelElements();

		elementsById.values().removeIf(elements -> !elements.stream().anyMatch(MCommand.class::isInstance));
		assertUnique(elementsById);
	}

	/**
	 * @return all model elements grouped by element id
	 */
	private HashMap<String, Set<MApplicationElement>> collectModelElements() {
		MApplication application = getWorkbench().getService(MApplication.class);

		HashMap<String, Set<MApplicationElement>> elementsById = new HashMap<>();
		collect(application, elementsById);
		return elementsById;
	}

	private static void collect(MApplicationElement element, Map<String, Set<MApplicationElement>> elementsById) {
		collect(element, child -> {
				elementsById.computeIfAbsent(child.getElementId(), id -> new HashSet<>()).add(child);
		});
	}

	private static void collect(MApplicationElement element, Consumer<MApplicationElement> consumer) {
		consumer.accept(element);
		for (EObject child : ((EObject) element).eContents()) {
			if (child instanceof MApplicationElement) {
				collect((MApplicationElement) child, consumer);
			}
		}
	}
	private void assertUnique(HashMap<String, Set<MApplicationElement>> elementsById) {
		elementsById.entrySet().removeIf(entry -> entry.getValue().size() <= 1);
		elementsById.remove(null);
		StringBuilder report = new StringBuilder("Model elements with same ids found:\n");
		elementsById.forEach((k, v) -> report.append(
				k + ": [" + v.stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(","))
						+ "]\n"));
		Assert.assertTrue(report.toString(), elementsById.isEmpty());
	}

}
