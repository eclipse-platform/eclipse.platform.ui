package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
//
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
//
import java.io.*;
import java.util.*;
//
public class Synchronizer implements ISynchronizer {
	protected Workspace workspace;
	// Registry of sync partners. Set of qualified names.
	protected Set registry = new HashSet(5);

	// version number used for serialization
	protected static int VERSION = 2;

	//
	protected static final int INT_CONSTANT = 1;
	protected static final int QNAME_CONSTANT = 2;
public Synchronizer(Workspace workspace) {
	super();
	this.workspace = workspace;
}
/**
 * @see ISynchronizer#accept
 */
public void accept(QualifiedName partner, IResource resource, IResourceVisitor visitor, int depth) throws CoreException {
	Assert.isLegal(partner != null);
	Assert.isLegal(resource != null);
	Assert.isLegal(visitor != null);

	// if we don't have sync info for the given identifier, then skip it
	if (getSyncInfo(partner, resource) != null) {
		// visit the resource and if the visitor says to stop the recursion then return
		if (!visitor.visit(resource))
			return;
	}

	// adjust depth if necessary
	if (depth == IResource.DEPTH_ZERO || resource.getType() == IResource.FILE)
		return;
	if (depth == IResource.DEPTH_ONE)
		depth = IResource.DEPTH_ZERO;

	// otherwise recurse over the children
	IResource[] children = ((IContainer) resource).members();
	for (int i = 0; i < children.length; i++)
		accept(partner, children[i], visitor, depth);
}
/**
 * @see ISynchronizer#add
 */
public void add(QualifiedName partner) {
	Assert.isLegal(partner != null);
	registry.add(partner);
}
/**
 * @see ISynchronizer#flushSyncInfo
 */
public void flushSyncInfo(final QualifiedName partner, final IResource root, final int depth) throws CoreException {
	Assert.isLegal(partner != null);
	Assert.isLegal(root != null);

	IWorkspaceRunnable body = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			IResourceVisitor visitor = new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					setSyncInfo(partner, resource, null);
					return true;
				}
			};
			root.accept(visitor, depth, true);
		}
	};
	workspace.run(body, null);
}
/**
 * @see ISynchronizer#getPartners
 */
public QualifiedName[] getPartners() {
	return (QualifiedName[]) registry.toArray(new QualifiedName[registry.size()]);
}
/**
 * @see ISynchronizer#getSyncInfo
 */
public byte[] getSyncInfo(QualifiedName partner, IResource resource) throws CoreException {
	Assert.isLegal(partner != null);
	Assert.isLegal(resource != null);

	if (!isRegistered(partner))
		throw new ResourceException(new ResourceStatus(IResourceStatus.PARTNER_NOT_REGISTERED, partner.toString()));

	// namespace check, if the resource doesn't exist then return null
	ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), true, false);
	return (info == null) ? null : info.getSyncInfo(partner, true);
}
protected boolean isRegistered(QualifiedName partner) {
	Assert.isLegal(partner != null);
	return registry.contains(partner);
}
/**
 * @see #writePartners
 */
public void readPartners(DataInputStream input) throws CoreException {
	SyncInfoReader reader = new SyncInfoReader(workspace, this);
	reader.readPartners(input);
}
/**
 * @see #writeSyncInfo
 */
public void readSyncInfo(DataInputStream input) throws CoreException {
	SyncInfoReader reader = new SyncInfoReader(workspace, this);
	reader.readSyncInfo(input);
}
/**
 * @see ISynchronizer#remove
 */
public void remove(QualifiedName partner) {
	Assert.isLegal(partner != null);
	if (isRegistered(partner)) {
		// remove all sync info for this partner
		try {
			flushSyncInfo(partner, workspace.getRoot(), IResource.DEPTH_INFINITE);
			registry.remove(partner);
		} catch (CoreException e) {
			// XXX: flush needs to be more resilient and not throw exceptions all the time
		}
	}
}
protected void setRegistry(Set registry) {
	this.registry = registry;
}
/**
 * @see ISynchronizer#setSyncInfo
 */
public void setSyncInfo(QualifiedName partner, IResource resource, byte[] info) throws CoreException {
	Assert.isLegal(partner != null);
	Assert.isLegal(resource != null);
	try {
		workspace.prepareOperation();
		workspace.beginOperation(true);
		if (!isRegistered(partner))
			throw new ResourceException(new ResourceStatus(IResourceStatus.PARTNER_NOT_REGISTERED, partner.toString()));
		// we do not store sync info on the workspace root
		if (resource.getType() == IResource.ROOT)
			return;
		// if the resource doesn't yet exist then create a phantom so we can set the sync info on it
		Resource target = (Resource) resource;
		ResourceInfo resourceInfo = workspace.getResourceInfo(target.getFullPath(), true, false);
		int flags = target.getFlags(resourceInfo);
		if (!target.exists(flags, false)) {
			if (info == null)
				return;
			else
				workspace.createResource(resource, true);
		}
		resourceInfo = target.getResourceInfo(true, true);
		resourceInfo.setSyncInfo(partner, info);
		resourceInfo.incrementSyncInfoGenerationCount();
		flags = target.getFlags(resourceInfo);
		if (target.isPhantom(flags) && resourceInfo.getSyncInfo(false) == null) {
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, Status.OK, "OK", null);
			((Resource) resource).deleteResource(false, status);
			if (!status.isOK())
				throw new ResourceException(status);
		}
	} finally {
		workspace.endOperation(false, null);
	}
}
/**
 * @see #readPartners
 */
public void writePartners(DataOutputStream output) throws IOException {
	output.writeInt(registry.size());
	for (Iterator i = registry.iterator(); i.hasNext();) {
		QualifiedName qname = (QualifiedName) i.next();
		output.writeUTF(qname.getQualifier());
		output.writeUTF(qname.getLocalName());
	}
}
/**
VERSION_ID
RESOURCE[]

VERSION_ID:
	int (used for backwards compatibiliy)

RESOURCE:
	String - resource full path
	int - sync info table size
	SYNCINFO[]

SYNCINFO:
	CONST
	(NAME | INT)
	VALUE

CONST:
	INT_CONSTANT
	QNAME_CONSTANT

NAME:
	String - qualifier
	String - local

INT:
	Integer index into list of names which have already been written

VALUE:
	int - byte array length
	byte[] - sync info bytes

 */
public void writeSyncInfo(IResource target, DataOutputStream output, List writtenPartners) throws IOException {
	Resource resource = (Resource) target;
	ResourceInfo info = workspace.getResourceInfo(resource.getFullPath(), true, false);
	if (info == null)
		return;
	HashMap table = info.getSyncInfo(false);
	if (table == null)
		return;
	// if this is the first sync info that we have written, then
	// write the version id for the file.
	if (output.size() == 0)
		output.writeInt(VERSION);
	output.writeUTF(resource.getFullPath().toString());
	output.writeInt(table.size());
	for (Iterator i = table.entrySet().iterator(); i.hasNext();) {
		Map.Entry entry = (Map.Entry) i.next();
		QualifiedName name = (QualifiedName) entry.getKey();
		// if we have already written the partner name once, then write an integer
		// constant to represent it instead to remove duplication
		int index = writtenPartners.indexOf(name);
		if (index == -1) {
			// FIXME: what to do about null qualifier?
			output.writeInt(QNAME_CONSTANT);
			output.writeUTF(name.getQualifier());
			output.writeUTF(name.getLocalName());
			writtenPartners.add(name);
		} else {
			output.writeInt(INT_CONSTANT);
			output.writeInt(index);
		}
		byte[] bytes = (byte[]) entry.getValue();
		output.writeInt(bytes.length);
		output.write(bytes);
	}
}
}
