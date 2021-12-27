/*******************************************************************************
 * Copyright (c) 2022 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.databinding.bind;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * This class generates the source files for {@link IObservableList} and
 * {@link IObservableSet} from the source files for {@link IObservableValue}.
 * This is done by replacing "Value" with "List" everywhere in the code, and
 * filtering out validation methods (which are specific to Value).
 * <p>
 * To generate: Run main method, then run Clean Up action in Eclipse.
 */
class GenerateSourceFilesForListsAndSets {
	public static void main(String[] args) throws IOException {
		String originalVariant = "Value"; //$NON-NLS-1$
		List<String> derivedVariants = Arrays.asList("List", "Set"); //$NON-NLS-1$//$NON-NLS-2$

		List<String> files = Arrays.asList(
				"src/org/eclipse/core/databinding/bind/steps/?CommonSteps.java", //$NON-NLS-1$
				"src/org/eclipse/core/databinding/bind/steps/?OneWaySteps.java", //$NON-NLS-1$
				"src/org/eclipse/core/databinding/bind/steps/?TwoWaySteps.java", //$NON-NLS-1$
				"src/org/eclipse/core/internal/databinding/bind/?CommonStepsImpl.java", //$NON-NLS-1$
				"src/org/eclipse/core/internal/databinding/bind/?OneWayStepsImpl.java", //$NON-NLS-1$
				"src/org/eclipse/core/internal/databinding/bind/?TwoWayStepsImpl.java"); //$NON-NLS-1$

		for (String variant : derivedVariants) {
			for (String file : files) {
				Path source = Paths.get(file.replace("?", originalVariant)); //$NON-NLS-1$
				Path dest = Paths.get(file.replace("?", variant)); //$NON-NLS-1$

				String contents = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);

				// Import java.util.List and Set
				contents = contents.replaceFirst("\n\n", "\n\nimport java.util." + variant + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				contents = contents.replaceAll("fromComputedValue\\(Supplier<F>", //$NON-NLS-1$
						"fromComputedValue(Supplier<" + variant + "<F>>"); //$NON-NLS-1$ //$NON-NLS-2$

				contents = filterMethodDecl(contents, "validateTwoWay"); //$NON-NLS-1$
				contents = filterMethodDecl(contents, "validateAfterConvert"); //$NON-NLS-1$
				contents = filterMethodImpl(contents, "validateAfterConvert"); //$NON-NLS-1$
				contents = filterMethodDecl(contents, "validateAfterGet"); //$NON-NLS-1$
				contents = filterMethodImpl(contents, "validateAfterGet"); //$NON-NLS-1$
				contents = filterMethodDecl(contents, "validateBeforeSet"); //$NON-NLS-1$
				contents = filterMethodImpl(contents, "validateBeforeSet"); //$NON-NLS-1$
				contents = filterMethodDecl(contents, "convertOnly"); //$NON-NLS-1$
				contents = filterMethodImpl(contents, "convertOnly"); //$NON-NLS-1$
				contents = filterMethodImpl(contents, "validateTwoWay"); //$NON-NLS-1$
				contents = contents.replace(originalVariant, variant);
				contents = contents.replace(originalVariant.toLowerCase(), variant.toLowerCase());

				Files.write(dest, contents.getBytes(StandardCharsets.UTF_8));
				System.out.println("Wrote '" + dest); //$NON-NLS-1$
			}
		}
	}

	// Matches a block comment
	private static final String DOC_PATTERN = "\\t*/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/"; //$NON-NLS-1$

	private static String filterMethodDecl(String source, String methodName) {
		return source.replaceAll("(?sm)" + DOC_PATTERN + "[\\s@\\w<>]+ " + methodName + "[^\\n]*?;\\n*", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	private static String filterMethodImpl(String source, String methodName) {
		return source.replaceAll("(?sm)(" + DOC_PATTERN + ")?[\\s@\\w<>]+ " + methodName + ".*?\\}\\n*", "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
