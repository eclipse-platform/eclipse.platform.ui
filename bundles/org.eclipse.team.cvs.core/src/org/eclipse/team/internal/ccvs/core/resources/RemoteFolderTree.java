package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateMessageHandler;

/**
 * Whereas the RemoteFolder class provides access to a remote hierarchy using
 * lazy retrieval via <code>getMembers()</code>, the RemoteFolderTree will force 
 * a recursive retrieval of the remote hierarchy in one round trip.
 */
public class RemoteFolderTree //extends RemoteFolder 
{
//	
//	IManagedFolder root;
//	ICVSRemoteFolder remoteRoot;
//
//	public RemoteFolderTree(IManagedFolder root, ICVSRepositoryLocation location, IPath locationRelativePath, String tag) {
//		super(location, locationRelativePath, tag);
//		this.root = root;
//		
//		RemoteFolder remoteRoot = new RemoteFolder(location, locationRelativePath, tag);		
//	}
//	
//	public ICVSRemoteFolder fetchRemoteTree(IProgressMonitor monitor) {
//	
//			final List errors = new ArrayList();
//			final List newRemoteDirectories = new ArrayList();
//			final List newRemoteFiles = new ArrayList();
//			
//			IUpdateMessageListener listener = new IUpdateMessageListener() {
//				public void directoryInformation(IPath path, boolean newDirectory) {
//					if (newDirectory) {
//						IPath repoRelativePath = new Path(RemoteFolderTree.this.getRemotePath()).append(path);			
//						newRemoteDirectories.add(new RemoteFolderTree(null, RemoteFolderTree.this.getRepository(), repoRelativePath, tag));					
//					}
//				}
//				public void directoryDoesNotExist(IPath path) {
//				}
//				public void fileInformation(char type, String filename) {
//					IPath filePath = new Path(filename);	
//					switch(type) {
//						case 'U' : // fall though
//						case 'C' : newRemoteFiles.add(filename);
//							       break;
//					}	
//				}
//			};
//			
//			// either this folder of the parent if it's a file
//			try {
//				Connection c = ((CVSRepositoryLocation)getRepository()).openConnection();
//				
//				Client.execute(
//					Client.UPDATE,
//					new String[] {"-n"}, 
//					new String[] {"-d", "-r", tag},
//					new String[]{"."}, 
//					this,
//					monitor,
//					getPrintStream(),
//					c,
//					new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)}
//					);
//			} catch(CVSException e) {
//				throw CVSTeamProvider.wrapException(e, error);
//			}
//			return null;	
//	}
//	
//	/**
//	 * @see IRemoteFolder#getMembers()
//	 */
//	public IRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException {
//		return getChildren();
//	}	
}

