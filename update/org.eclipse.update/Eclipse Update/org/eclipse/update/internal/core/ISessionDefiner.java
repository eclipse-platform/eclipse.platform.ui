package org.eclipse.update.internal.core;

/**
 */
import org.eclipse.core.internal.boot.update.*;

public interface ISessionDefiner {
/**
 * Creates one or more operations for the current session.
 */
void defineOperations( UMSessionManagerSession session, IInstallable[] descriptors, boolean bVerifyJars );
}
