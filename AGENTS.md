# AGENTS.md

This file provides guidance to AI Agents.

## Repository Overview

Eclipse Platform UI provides the building blocks for Eclipse IDE and Eclipse Rich Client Platform (RCP). 
This includes JFace, workbench, commands framework, data binding, dialogs, editors, views, perspectives, and more. Built on top of SWT (Eclipse Standard Widget Toolkit).

**Key Facts:**
- **Language:** Java 21
- **Build System:** Maven 3.9.11 with Tycho (OSGi/Eclipse plugin build)
- **Architecture:** OSGi bundles, E4 application model

## Project Structure

```
eclipse.platform.ui/
├── bundles/          # OSGi bundles (production code)
│   ├── org.eclipse.ui.workbench          # Main workbench implementation
│   ├── org.eclipse.jface                 # JFace toolkit (viewers, dialogs, etc.)
│   ├── org.eclipse.jface.databinding     # Data binding framework
│   ├── org.eclipse.jface.text            # Text editing framework
│   ├── org.eclipse.core.commands         # Commands framework
│   ├── org.eclipse.core.databinding*     # Core data binding
│   ├── org.eclipse.e4.ui.*               # E4 workbench, CSS, DI, model
│   └── org.eclipse.ui.*                  # UI components (IDE, editors, views, etc.)
├── tests/            # Test bundles (mirror structure of bundles/)
├── examples/         # Example bundles
├── features/         # Eclipse feature definitions
├── releng/           # Release engineering artifacts
├── docs/             # Documentation (JFace, RCP, Commands, etc.)
└── .github/          # GitHub workflows and CI configuration
```

### Key Architectural Components

**E4 Platform (Modern):**
- `org.eclipse.e4.ui.model.workbench` - E4 application model
- `org.eclipse.e4.ui.workbench*` - E4 workbench implementation
- `org.eclipse.e4.ui.di` - Dependency injection
- `org.eclipse.e4.ui.css.*` - CSS styling engine
- `org.eclipse.e4.core.commands` - Command framework

**JFace Toolkit:**
- `org.eclipse.jface` - Viewers, dialogs, resources, actions
- `org.eclipse.jface.databinding` - Data binding for UI
- `org.eclipse.jface.text` - Text editing infrastructure

**Legacy Workbench (3.x compatibility):**
- `org.eclipse.ui.workbench` - Workbench implementation
- `org.eclipse.ui.ide` - IDE-specific components
- `org.eclipse.ui.editors` - Editor framework

### OSGi Bundle Structure

Each bundle contains:
- `META-INF/MANIFEST.MF` - Bundle metadata and dependencies
- `build.properties` - Build configuration (what to include in binary)
- `plugin.xml` - Extension point declarations and contributions (optional)
- `src/` or `eclipseui/` - Java source code
- `.settings/` - Eclipse compiler settings

## Build System

### Critical Limitation


Use the `-Pbuild-individual-bundles` profile:

```bash
# Compile single bundle
mvn clean compile -pl :bundle-artifact-id -Pbuild-individual-bundles -q

# Example for building a single bundle
mvn clean verify -Pbuild-individual-bundles mvn clean verify -pl bundles/org.eclipse.ui -DskipTests

### Test Properties

From `pom.xml`:
- `tycho.surefire.useUIHarness=true` - Use Eclipse UI test harness
- `tycho.surefire.useUIThread=true` - Run tests on UI thread
- `failOnJavadocErrors=true` - Fail build on Javadoc errors

## Testing

### Running Tests

**⚠️ IMPORTANT:** Use `mvn verify` (NOT `mvn test`) for Tycho projects. 
Due to Maven Tycho lifecycle binding, tests run in the `integration-test` phase, not the `test` phase. Running `mvn test` will NOT execute tests.

```bash
# Run tests for a specific test bundle from repository root
mvn clean verify -pl :org.eclipse.ui.tests -Pbuild-individual-bundles

# Run specific test class within a bundle
mvn clean verify -pl :org.eclipse.ui.tests -Pbuild-individual-bundles -Dtest=StructuredViewerTest

# Skip tests during compilation
mvn clean compile -Pbuild-individual-bundles -DskipTests
```

**Finding test bundles:** Test bundles mirror production bundles:
- Production: `bundles/org.eclipse.jface`
- Tests: `tests/org.eclipse.jface.tests`

### JUnit Guidelines

- Prefer JUnit 5 (`org.junit.jupiter.api.*`) for new tests

## Common Development Commands

### Compilation


# Compile and run tests
mvn clean test -pl :bundle-artifact-id -Pbuild-individual-bundles
```

### Finding Code

```bash
# Find test files for a bundle
ls tests/org.eclipse.jface.tests/src

# Find bundle MANIFEST
cat bundles/org.eclipse.jface/META-INF/MANIFEST.MF

# Search for specific code pattern
grep -r "pattern" bundles/org.eclipse.jface/src
```

## Critical Development Rules

### 1. OSGi Dependencies

**Always check `META-INF/MANIFEST.MF` before adding imports.** If a package is not in `Import-Package` or `Require-Bundle`, the import will fail at runtime.

```
Require-Bundle: org.eclipse.core.runtime,
 org.eclipse.swt,
 org.eclipse.jface
Import-Package: org.osgi.service.event
```

### 2. SWT Resource Disposal

**Must dispose SWT resources** (except colors and system fonts):

### 3. UI Thread Requirements

**All SWT/JFace UI code must run on the Display thread:**

```java
// Run asynchronously on UI thread
Display.getDefault().asyncExec(() -> {
    label.setText("Updated");
});

// Check if on UI thread
if (Display.getCurrent() != null) {
    // Already on UI thread
} else {
    // Need to use asyncExec/syncExec
}
```

### 4. API Compatibility

**Do not break API compatibility.** The build includes API tools that verify:
- No removal of public API
- No changes to method signatures
- No changes to class hierarchies

Breaking changes will fail CI with errors in `**/target/apianalysis/*.xml`.

### 5. Update build.properties

When adding new files or packages, update `build.properties`:

```properties
# Include in binary build
bin.includes = plugin.xml,\
               META-INF/,\
               .,\
               icons/

# Source folders
source.. = src/

# Output folder
output.. = bin/
```


## CI/GitHub Workflows

- CI definition: `.github/workflows/ci.yml` (runs full aggregator build)
- PRs are gated by: compiler checks, API compatibility, Javadoc, and unit/UI tests
- UI tests run with the Eclipse UI harness in headless mode

## Troubleshooting

### "Non-resolvable parent POM"
Expected when running `mvn verify` at root. Use `-Pbuild-individual-bundles` for individual bundles.

### "Package does not exist"
Check `META-INF/MANIFEST.MF` - add missing package to `Import-Package` or bundle to `Require-Bundle`.

### "API baseline errors"
You've broken API compatibility. Revert the breaking change or mark it appropriately.

### Test hangs
UI tests must run on Display thread. Use `Display.asyncExec()` or ensure `useUIHarness=true`.

### "Widget is disposed"
Attempting to access disposed SWT widget. Check disposal order and lifecycle.

## Documentation

Key docs in `docs/` directory:
- `JFace.md` - JFace framework overview
- `JFaceDataBinding.md` - Data binding guide
- `Eclipse4_RCP_FAQ.md` - E4 RCP frequently asked questions
- `PlatformCommandFramework.md` - Command framework
- `CSS.md` - CSS styling for E4

External links:
- [Platform UI Wiki](https://wiki.eclipse.org/Platform_UI)
- [Contributing Guide](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md)
- [Eclipse Platform Project](https://projects.eclipse.org/projects/eclipse.platform)
