/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common;

import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionResultHandler;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.Filter;
import org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.ModelResultHandler;

public class ClassContributionCollector {
	private final CopyOnWriteArrayList<IClassContributionProvider> providers = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<IModelElementProvider> modelElementProviders = new CopyOnWriteArrayList<>();

	public void addContributor(IClassContributionProvider contributor) {
		providers.add(contributor);
	}

	public void removeContributor(IClassContributionProvider contributor) {
		providers.remove(contributor);
	}

	public void addModelElementContributor(IModelElementProvider provider) {
		modelElementProviders.add(provider);
	}

	public void removeModelElementContributor(IModelElementProvider provider) {
		modelElementProviders.remove(provider);
	}

	public void findContributions(Filter filter, ContributionResultHandler resultHandler) {

		for (IClassContributionProvider contributor : providers) {
			contributor.findContribution(filter, resultHandler);
		}
	}

	public void findModelElements(org.eclipse.e4.tools.emf.ui.common.IModelElementProvider.Filter filter, ModelResultHandler resultHandler) {

		for (IModelElementProvider contributor : modelElementProviders) {
			contributor.getModelElements(filter, resultHandler);
		}
	}

	public void clearModelCache() {
		for (IModelElementProvider contributor : modelElementProviders) {
			contributor.clearCache();
		}
	}
}
