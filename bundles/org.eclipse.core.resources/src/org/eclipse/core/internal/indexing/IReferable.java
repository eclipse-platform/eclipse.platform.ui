package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

interface IReferable {

/**
 * Adds a reference for this entity to track. Returns the current count.
 */
int addReference();
/**
 * Tests for existing references.
 */
boolean hasReferences();
/**
 * Removes a reference.  Returns the current count.
 */
int removeReference();
}
