/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.resources.IProject;

public interface IClassContributionProvider {
	public class ContributionData {
		public final String bundleName;
		public final String className;
		public final String sourceType;
		public final String iconPath;
		
		public ContributionData(String bundleName, String className, String sourceType, String iconPath) {
			this.bundleName = bundleName;
			this.className = className;
			this.sourceType = sourceType;
			this.iconPath = iconPath;
		}
	}
	
	public class Filter {
		public final IProject project;
		public final String namePattern;
		
		public Filter(IProject project, String namePattern) {
			this.project = project;
			this.namePattern = namePattern;
		}
	}
	
	public interface ContributionResultHandler {
		public void result(ContributionData data);
	}
	
	public void findContribution(Filter filter, ContributionResultHandler handler);
}
