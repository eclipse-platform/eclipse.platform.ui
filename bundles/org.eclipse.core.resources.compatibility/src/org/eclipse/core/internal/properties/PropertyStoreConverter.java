/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.properties;

import java.io.File;
import org.eclipse.core.internal.indexing.IndexCursor;
import org.eclipse.core.internal.localstore.BucketTree;
import org.eclipse.core.internal.resources.CompatibilityMessages;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class PropertyStoreConverter {
	class ConversionVisitor implements IVisitor {
		private IPath basePath;
		private BucketTree target;
		private boolean worked;

		public ConversionVisitor(IPath basePath, BucketTree target) {
			this.target = target;
			this.basePath = basePath;
		}

		public boolean hasWorked() {
			return worked;
		}

		public boolean requiresValue(ResourceName resourceName, QualifiedName propertyName) {
			// we need the values so we can convert
			return true;
		}

		public void visit(ResourceName resourceName, StoredProperty property, IndexCursor cursor) throws CoreException {
			IPath fullPath = basePath.append(resourceName.getPath());
			target.loadBucketFor(fullPath);
			// copies a single property
			((PropertyBucket) target.getCurrent()).setProperty(fullPath, property.getName(), property.getStringValue());
			worked = true;
		}
	}

	/**
	 * Converts existing persistent property data lying on disk to the new 
	 * property store format.
	 * Returns Status.OK_STATUS if nothing is done, an IStatus.INFO status if
	 * the conversion happens successfully or an IStatus.ERROR status if an error
	 * happened during the conversion process.
	 */
	public IStatus convertProperties(Workspace workspace, final PropertyManager2 destination) {
		// Quickly check whether should try converting persistent properties
		// We cannot pay the cost of checking every project so, instead, we try to find 
		// a single file used by the new implementation  
		File versionFile = destination.getVersionFile();
		if (versionFile.isFile())
			// conversion already done, won't try doing it again
			return Status.OK_STATUS;
		final boolean[] worked = {false};
		final PropertyManager source = new PropertyManager(workspace);
		try {
			// convert the property store for the root and every project 
			workspace.getRoot().accept(new IResourceVisitor() {
				public boolean visit(org.eclipse.core.resources.IResource resource) throws CoreException {
					ConversionVisitor propertyConverter = new ConversionVisitor(resource.getFullPath(), destination.getTree());
					PropertyStore store = source.getPropertyStore(resource, false);
					if (store == null)
						return true;
					store.recordsDeepMatching(new ResourceName("", resource.getProjectRelativePath()), propertyConverter); //$NON-NLS-1$
					source.closePropertyStore(resource);
					worked[0] = worked[0] || propertyConverter.hasWorked();
					return true;
				}
			}, IResource.DEPTH_ONE, IResource.NONE);
			// the last bucket changed will not have been saved			
			destination.getTree().getCurrent().save();
		} catch (CoreException e) {
			// failed while visiting the old data or saving the new data
			String conversionFailed = CompatibilityMessages.properties_conversionFailed;
			return new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, new IStatus[] {e.getStatus()}, conversionFailed, null);
		}
		if (!worked[0])
			// nothing was found to be converted
			return Status.OK_STATUS;
		// conversion actually happened, and everything went fine
		// leave a note to the user so this does not happen silently			
		String conversionOk = CompatibilityMessages.properties_conversionSucceeded;
		return new Status(IStatus.INFO, ResourcesPlugin.PI_RESOURCES, IStatus.OK, conversionOk, null);
	}
}
