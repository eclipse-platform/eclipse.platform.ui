/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 * This scheduling rule checks that no job with another instance of
 * {@link SubdirectoryOrSameNameSchedulingRule} is running on a subdirectory
 * than the one current {@link SubdirectoryOrSameNameSchedulingRule} is applied
 * to, or is most likely to have the same name as a project being imported
 * (prevents from trying to create twice the same resource.
 * @author mistria
 *
 */
public class SubdirectoryOrSameNameSchedulingRule implements ISchedulingRule {

	private File directory;

	public SubdirectoryOrSameNameSchedulingRule(File directory) {
		this.directory = directory;
	}

	public SubdirectoryOrSameNameSchedulingRule(IResource resource) {
		this(resource.getLocation().toFile());
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		if (rule == this || rule instanceof IResource) {
			return true;
		} else if (rule instanceof MultiRule) {
			for (ISchedulingRule child : ((MultiRule)rule).getChildren()) {
				if (this.contains(child)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof SubdirectoryOrSameNameSchedulingRule) {
			SubdirectoryOrSameNameSchedulingRule otherRule = (SubdirectoryOrSameNameSchedulingRule)rule;
			return
				otherRule.directory.getAbsolutePath().startsWith(this.directory.getAbsolutePath()) ||
				otherRule.directory.getName().equals(this.directory.getName());
		} else if (rule instanceof IResource) {
			return true;
		}  else if (rule instanceof MultiRule) {
			for (ISchedulingRule child : ((MultiRule)rule).getChildren()) {
				if (this.isConflicting(child)) {
					return true;
				}
			}
		}
		return false;

	}

}
