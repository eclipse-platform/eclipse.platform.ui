/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;


class CVSParticipantLabelDecorator extends LabelProvider implements IPropertyChangeListener, ILabelDecorator {
	private ISynchronizePageConfiguration configuration;
	public CVSParticipantLabelDecorator(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		// Listen for decorator changed to refresh the viewer's labels.
		CVSUIPlugin.addPropertyChangeListener(this);
		TeamUI.addPropertyChangeListener(this);
	}
	
	public String decorateText(String input, Object element) {
		String text = input;
		if (element instanceof ISynchronizeModelElement) {
			IResource resource =  ((ISynchronizeModelElement)element).getResource();
			if(resource != null && resource.getType() != IResource.ROOT) {
				CVSLightweightDecorator.Decoration decoration = new CVSLightweightDecorator.Decoration();
				CVSLightweightDecorator.decorateTextLabel(resource, decoration, false, true, getRevisionNumber((ISynchronizeModelElement)element));
				StringBuffer output = new StringBuffer(25);
				if(decoration.prefix != null) {
					output.append(decoration.prefix);
				}
				output.append(text);
				if(decoration.suffix != null) {
					output.append(decoration.suffix);
				}
				return output.toString();
			}
		}
		if (element instanceof CommitSetDiffNode) {
		    CommitSet set = ((CommitSetDiffNode)element).getSet();
		    if (CommitSetManager.getInstance().isDefault(set)) {
		        return Policy.bind("CommitSetDiffNode.0", text); //$NON-NLS-1$
		    }
		}
		return text;
	}
	public Image decorateImage(Image base, Object element) {
		return base;
	}
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if(property.equals(CVSUIPlugin.P_DECORATORS_CHANGED) || property.equals(TeamUI.GLOBAL_FILE_TYPES_CHANGED)) {
			Viewer viewer = configuration.getPage().getViewer();
			if(viewer instanceof StructuredViewer && !viewer.getControl().isDisposed()) {
				((StructuredViewer)viewer).refresh(true);
			}
		}
	}
	public void dispose() {
		CVSUIPlugin.removePropertyChangeListener(this);
		TeamUI.removePropertyChangeListener(this);
	}
	
	protected String getRevisionNumber(ISynchronizeModelElement element) {
		if(element instanceof SyncInfoModelElement) {
			SyncInfo info = ((SyncInfoModelElement)element).getSyncInfo();
			if(info != null && info.getLocal().getType() == IResource.FILE && info instanceof CVSSyncInfo) {
				CVSSyncInfo cvsInfo = (CVSSyncInfo)info;
				ICVSRemoteResource remote = (ICVSRemoteResource) cvsInfo.getRemote();
				ICVSRemoteResource local;
				try {
					local = (ICVSRemoteFile) CVSWorkspaceRoot.getRemoteResourceFor(info.getLocal());
				} catch (CVSException e) {
					local = null;
				}
				if(local == null) {
					local = (ICVSRemoteResource)info.getBase();
				}
				StringBuffer revisionString = new StringBuffer();
				String remoteRevision = getRevisionString(remote);
				String localRevision = getRevisionString(local);
				if(localRevision != null) {
					revisionString.append(localRevision);
				}
				if(remoteRevision != null) {
					revisionString.append( (localRevision != null ? " - " : "") + remoteRevision); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return revisionString.toString();
			}
		}
		return null;
	}

	private String getRevisionString(ICVSRemoteResource remoteFile) {
		if(remoteFile instanceof RemoteFile) {
			return ((RemoteFile)remoteFile).getRevision();
		}
		return null;
	}
}