/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.jarprocessor;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * @author aniefer
 *
 */
public class PackUnpackStep extends PackStep {
	private Set exclusions = null;
	
	public PackUnpackStep(Properties options) {
		super(options);
		exclusions = Utils.getPackExclusions(options);
	}

	public String recursionEffect(String entryName) {
		if (canPack() && entryName.endsWith(".jar") &&  !exclusions.contains(entryName)) { //$NON-NLS-1$
			return entryName;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#preProcess(java.io.File, java.io.File)
	 */
	public File postProcess(File input, File workingDirectory) {
		if (canPack() && packCommand != null) {
			File tempFile = new File(workingDirectory, "temp_" + input.getName()); //$NON-NLS-1$
			try {
				String[] tmp = getCommand(input, tempFile);
				String[] cmd = new String[tmp.length + 1];
				cmd[0] = tmp[0];
				cmd[1] = "-r"; //$NON-NLS-1$
				System.arraycopy(tmp, 1, cmd, 2, tmp.length - 1);

				int result = execute(cmd);
				if (result == 0 && tempFile.exists()) {
					File finalFile = new File(workingDirectory, input.getName());
					if(finalFile.exists())
						finalFile.delete();
					tempFile.renameTo(finalFile);
					return finalFile;
				}
			} catch (IOException e) {
				//didn't work
				return null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.jarprocessor.IProcessStep#postProcess(java.io.File, java.io.File)
	 */
	public File preProcess(File input, File workingDirectory) {
		return null;
	}

}
