package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
import org.eclipse.core.internal.boot.update.*;

public interface ISessionDefiner {
/**
 * Creates one or more operations for the current session.
 */
void defineOperations( UMSessionManagerSession session, IInstallable[] descriptors, boolean bVerifyJars );
}
