# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

Eclipse Platform UI provides the UI building blocks for Eclipse IDE and Eclipse Rich Client Platform (RCP). This includes JFace, workbench, commands framework, data binding, dialogs, editors, views, perspectives, and more. Built on top of SWT (Eclipse Standard Widget Toolkit).

**Key Facts:**
- **Language:** Java 17
- **Build System:** Maven 3.9.x with Tycho (OSGi/Eclipse plugin build)
- **Architecture:** OSGi bundles, E4 application model
- **Size:** 127 MB, 7,675+ Java files
- **Structure:** 57 production bundles + 34 test bundles + 25 examples

## Project Structure

```
eclipse.platform.ui/
├── bundles/          # 57 OSGi bundles (production code)
│   ├── org.eclipse.ui.workbench          # Main workbench implementation
│   ├── org.eclipse.jface                 # JFace toolkit (viewers, dialogs, etc.)
│   ├── org.eclipse.jface.databinding     # Data binding framework
│   ├── org.eclipse.jface.text            # Text editing framework
│   ├── org.eclipse.core.commands         # Commands framework
│   ├── org.eclipse.core.databinding*     # Core data binding
│   ├── org.eclipse.e4.ui.*               # E4 workbench, CSS, DI, model
│   └── org.eclipse.ui.*                  # UI components (IDE, editors, views, etc.)
├── tests/            # 34 test bundles (mirror structure of bundles/)
├── examples/         # 25 example bundles
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

**⚠️ IMPORTANT:** Standalone `mvn clean verify` at repository root **WILL FAIL** with "Non-resolvable parent POM" error. This repository requires a parent POM from `eclipse.platform.releng.aggregator`.

### Building Individual Bundles

Use the `-Pbuild-individual-bundles` profile:

```bash
# Compile a single bundle
cd bundles/org.eclipse.jface
mvn clean compile -Pbuild-individual-bundles

# Run tests for a single bundle
cd tests/org.eclipse.jface.tests
mvn clean verify -Pbuild-individual-bundles

# Run specific test class
mvn test -Pbuild-individual-bundles -Dtest=ViewerTestClass
```

### Maven Configuration

Default config in `.mvn/maven.config`:
- `-Pbuild-individual-bundles` - Enable individual bundle builds
- `-Dtycho.target.eager=true` - Eager target resolution
- `-Dtycho.localArtifacts=ignore` - Ignore local artifacts

### Test Properties

From `pom.xml`:
- `tycho.surefire.useUIHarness=true` - Use Eclipse UI test harness
- `tycho.surefire.useUIThread=true` - Run tests on UI thread
- `failOnJavadocErrors=true` - Fail build on Javadoc errors

## Testing

### Running Tests

**⚠️ IMPORTANT:** Use `mvn verify` (NOT `mvn test`) for Tycho projects. Due to Maven Tycho lifecycle binding, tests run in the `integration-test` phase, not the `test` phase. Running `mvn test` will NOT execute tests.

```bash
# Run all tests in a specific test bundle
cd tests/org.eclipse.jface.tests
mvn clean verify -Pbuild-individual-bundles

# Run without clean (faster if no changes to dependencies)
mvn verify -Pbuild-individual-bundles

# Run tests for a specific test bundle from repository root
mvn clean verify -pl :org.eclipse.jface.tests -Pbuild-individual-bundles

# Run specific test class within a bundle
cd tests/org.eclipse.jface.tests
mvn clean verify -Pbuild-individual-bundles -Dtest=StructuredViewerTest

# Skip tests during compilation
mvn clean compile -Pbuild-individual-bundles -DskipTests
```

**Finding test bundles:** Test bundles mirror production bundles:
- Production: `bundles/org.eclipse.jface`
- Tests: `tests/org.eclipse.jface.tests`

### JUnit Version Status (October 2025)

**Current Migration State:**
- **JUnit 5 (Modern):** 7 bundles fully migrated, 5 partially migrated
- **JUnit 4 (Current):** 11 bundles ready for migration, majority of tests
- **JUnit 3 (Legacy):** Only in `org.eclipse.ui.tests.harness` as compatibility bridge

**When writing new tests:**
- Prefer JUnit 5 (`org.junit.jupiter.api.*`) for new tests
- Use `@BeforeEach`/`@AfterEach` instead of `@Before`/`@After`
- Use `@Disabled` instead of `@Ignore`
- Use `Assertions.*` instead of `Assert.*`

**Common test pattern:**
```java
@BeforeEach
public void setUp() {
    fDisplay = Display.getDefault();
    fShell = new Shell(fDisplay);
}

@AfterEach
public void tearDown() {
    if (fShell != null) {
        fShell.dispose();
    }
}

@Test
public void testSomething() {
    // Test implementation
    Assertions.assertEquals(expected, actual);
}
```

## Common Development Commands

### Compilation

```bash
# Compile single bundle
mvn clean compile -pl :bundle-artifact-id -Pbuild-individual-bundles -q

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

**Must dispose SWT resources** (except system colors/fonts):

```java
// CORRECT - dispose in finally
Shell shell = new Shell();
try {
    // use shell
} finally {
    shell.dispose();
}

// CORRECT - dispose custom colors/fonts/images
Color color = new Color(display, 255, 0, 0);
try {
    // use color
} finally {
    color.dispose();
}

// INCORRECT - system resources don't need disposal
Color systemColor = display.getSystemColor(SWT.COLOR_RED);
// No dispose needed
```

### 3. UI Thread Requirements

**All SWT/JFace UI code must run on the Display thread:**

```java
// Run asynchronously on UI thread
Display.getDefault().asyncExec(() -> {
    label.setText("Updated");
});

// Run synchronously (blocks until complete)
Display.getDefault().syncExec(() -> {
    button.setEnabled(false);
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

## Common Patterns

### Data Binding

```java
DataBindingContext ctx = new DataBindingContext();

// Bind widget to model
ctx.bindValue(
    WidgetProperties.text(SWT.Modify).observe(textWidget),
    BeanProperties.value("propertyName").observe(model)
);

// Dispose when done
ctx.dispose();
```

### JFace Viewers

```java
TableViewer viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
viewer.setContentProvider(ArrayContentProvider.getInstance());
viewer.setLabelProvider(new LabelProvider() {
    @Override
    public String getText(Object element) {
        return element.toString();
    }
});
viewer.setInput(myList);
```

### Eclipse Commands

Commands are defined in `plugin.xml` and handled via handlers:

```java
@Execute
public void execute(IEclipseContext context) {
    // Command implementation
}
```

## CI/GitHub Workflows

**Primary workflow:** `.github/workflows/ci.yml`
- Triggers on push/PR to master (ignores `docs/` and `*.md`)
- Uses `eclipse.platform.releng.aggregator` for full build
- Runs on Java 21 with xvnc for headless UI tests

**Validation steps:**
1. Compiler checks (Eclipse compiler)
2. API compatibility (API tools)
3. Javadoc generation
4. Unit tests (JUnit with UI harness)
5. Test reports published

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
