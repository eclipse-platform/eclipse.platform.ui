/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;
import java.util.Comparator;

import org.eclipse.core.internal.dependencies.*;
import org.eclipse.core.internal.dependencies.DependencySystem;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.model.PluginDescriptorModel;
import org.eclipse.core.runtime.model.PluginPrerequisiteModel;

public class RegistryDependencySystemHelper {

	private static class EclipseVersionComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			PluginVersionIdentifier v1 = (PluginVersionIdentifier) arg0;
			PluginVersionIdentifier v2 = (PluginVersionIdentifier) arg1;
			return v1.isGreaterThan(v2) ? 1 : v1.isPerfect(v2) ? 0 : -1;
		}
	}
	private static final IMatchRule COMPATIBLE = new EclipseCompatibleMatchRule();
	private static final IMatchRule EQUIVALENT = new EclipseEquivalentMatchRule();
	private static final IMatchRule GREATER_OR_EQUAL = new EclipseGreaterOrEqualMatchRule();
	private static final IMatchRule PERFECT = new EclipsePerfectMatchRule();

	public static IMatchRule getEclipseMatchRule(byte b) {
		switch (b) {
			case PluginPrerequisiteModel.PREREQ_MATCH_EQUIVALENT :
				return EQUIVALENT;
			case PluginPrerequisiteModel.PREREQ_MATCH_GREATER_OR_EQUAL :
				return GREATER_OR_EQUAL;
			case PluginPrerequisiteModel.PREREQ_MATCH_PERFECT :
				return PERFECT;
			case PluginPrerequisiteModel.PREREQ_MATCH_COMPATIBLE :
				return COMPATIBLE;
			case PluginPrerequisiteModel.PREREQ_MATCH_UNSPECIFIED :
				return COMPATIBLE;
		}
		throw new IllegalArgumentException("match byte: " + b); //$NON-NLS-1$
	}
	private final static class UnsatisfiableRule implements IMatchRule {
		public boolean isSatisfied(Object required, Object available) {
			return false;
		}
		public String toString() {
			return "unsatisfiable"; //$NON-NLS-1$
		}		
	}
	private final static class EclipsePerfectMatchRule implements IMatchRule {
		public boolean isSatisfied(Object required, Object available) {
			return ((PluginVersionIdentifier) available).isPerfect((PluginVersionIdentifier) required);
		}
		public String toString() {
			return "perfect"; //$NON-NLS-1$
		}
	}
	private final static class EclipseCompatibleMatchRule implements IMatchRule {
		public boolean isSatisfied(Object required, Object available) {
			return ((PluginVersionIdentifier) available).isCompatibleWith((PluginVersionIdentifier) required);
		}
		public String toString() {
			return "compatible"; //$NON-NLS-1$
		}
	}
	private final static class EclipseGreaterOrEqualMatchRule implements IMatchRule {
		public boolean isSatisfied(Object required, Object available) {
			return ((PluginVersionIdentifier) available).isGreaterOrEqualTo((PluginVersionIdentifier) required);
		}
		public String toString() {
			return "greaterOrEqual"; //$NON-NLS-1$
		}
	}
	private final static class EclipseEquivalentMatchRule implements IMatchRule {
		public boolean isSatisfied(Object required, Object available) {
			return ((PluginVersionIdentifier) available).isEquivalentTo((PluginVersionIdentifier) required);
		}
		public String toString() {
			return "equivalent"; //$NON-NLS-1$
		}
	}
	final static IElement createElement(PluginDescriptorModel model, IDependencySystem system) {
		// if it is already disabled is because it has invalid/missing data 
		if (!model.getEnabled())
			// ensure it will never be enabled
			return system.createElement(model.getPluginId(), new PluginVersionIdentifier(model.getVersion()), createUnsatisfiablePrerequisites(system), false);
		return system.createElement(model.getPluginId(), new PluginVersionIdentifier(model.getVersion()), createPrerequisites(model.getRequires(), system), !isLibrary(model));
	}
	/**
	 * Creates a prerequisite that cannot be ever satisfied.
	 */
	private static IDependency[] createUnsatisfiablePrerequisites(IDependencySystem system) {
		return new IDependency[] {system.createDependency("",new UnsatisfiableRule(),null,false)}; //$NON-NLS-1$
	}
	private static IDependency[] createPrerequisites(PluginPrerequisiteModel[] pluginPrereqs, IDependencySystem system) {
		if (pluginPrereqs == null || pluginPrereqs.length == 0)
			return new IDependency[0];
		IDependency[] prereqs = new IDependency[pluginPrereqs.length];
		for (int i = 0; i < prereqs.length; i++)
			prereqs[i] = system.createDependency(pluginPrereqs[i].getPlugin(), getEclipseMatchRule(pluginPrereqs[i].getMatchByte()), pluginPrereqs[i].getVersion() == null ? null : new PluginVersionIdentifier(pluginPrereqs[i].getVersion()),pluginPrereqs[i].getOptional());
		return prereqs;
	}
	private static boolean isLibrary(PluginDescriptorModel model) {
		return (model.getDeclaredExtensionPoints() == null || model.getDeclaredExtensionPoints().length == 0) && (model.getDeclaredExtensions() == null || model.getDeclaredExtensions().length == 0);
	}
	public static IDependencySystem createDependencySystem() {
		return new DependencySystem(new RegistryDependencySystemHelper.EclipseVersionComparator(), new Eclipse21SelectionPolicy());
	}
}