# Eclipse Platform UI - Copilot Instructions

## Repository Overview

Eclipse Platform UI provides UI building blocks for Eclipse IDE and Rich Client Platform (RCP): JFace, commands, databinding, dialogs, editors, views, perspectives, and workbench. Built on SWT.

**Key Facts:** 127 MB, 7,675+ Java files, Maven/Tycho build, 57 bundles + 34 test bundles + 25 examples, Java 17, Maven 3.9.x

## Project Structure

### Top-Level Directories

```
eclipse.platform.ui/
├── bundles/          # 57 OSGi bundles (production code)
├── tests/            # 34 test bundles
├── examples/         # 25 example bundles
├── features/         # Eclipse feature definitions
├── releng/           # Release engineering artifacts
├── docs/             # Extensive documentation (JFace, RCP, Commands, etc.)
├── .github/          # GitHub workflows and CI configuration
├── pom.xml           # Root Maven POM
└── Jenkinsfile       # Jenkins CI configuration
```

### Key Bundles

- `org.eclipse.ui.workbench` - Main workbench
- `org.eclipse.jface` - JFace toolkit
- `org.eclipse.e4.ui.*` - E4 workbench/CSS/DI
- `org.eclipse.core.databinding*` - Data binding
- `org.eclipse.core.commands` - Commands

### Bundle Structure

Each OSGi bundle contains: `META-INF/MANIFEST.MF` (dependencies), `build.properties` (build config), `plugin.xml` (extensions), source in `src/` or `eclipseui/`

## Build System - CRITICAL LIMITATIONS

**⚠️ IMPORTANT:** Standalone `mvn clean verify` **FAILS** - requires parent POM from `eclipse.platform.releng.aggregator`

**Workarounds:**
- Individual bundles: `cd bundles/<bundle>; mvn clean verify -Pbuild-individual-bundles`
- Full CI build: Uses aggregator project workflows
- Local testing: Verify syntax/logic without full Maven build

**Maven Config (`.mvn/maven.config`):** `-Pbuild-individual-bundles -Dtycho.target.eager=true -Dtycho.localArtifacts=ignore`

**CI Profiles:** `-Pbree-libs -Papi-check -Pjavadoc` (Jenkins timeout: 80 min)

## Testing

**Test Command:** `mvn clean verify -Pbuild-individual-bundles -DskipTests=false`

**Properties:** `tycho.surefire.useUIHarness=true` (UI test harness), `tycho.surefire.useUIThread=true` (UI thread)

**Dependencies:** Install "Eclipse Test Framework" from release p2 repo for Mockito/Hamcrest

**Test Pattern:**
```java
@Before
public void setUp() {
    fShell = new Shell(Display.getDefault());
    // setup
}
@After
public void tearDown() { /* cleanup */ }
@Test
public void test() { /* implementation */ }
```

## CI/GitHub Workflows

**Primary Workflows:**
- `ci.yml` - Main build (push/PR to master, ignores docs/md), uses aggregator's mavenBuild.yml
- `pr-checks.yml` - Fast checks (freeze period, merge commits, version increments)
- `unit-tests.yml` - Publishes test results after CI
- `codeql.yml` - Security scanning

**Validation Steps:**
1. Compiler checks (Eclipse compiler)
2. API compatibility (API tools → `**/target/apianalysis/*.xml`)
3. Javadoc generation (`failOnJavadocErrors=true`)
4. Unit tests (JUnit with UI harness)
5. Test reports (`**/target/surefire-reports/TEST-*.xml`)
6. Logs archived (`*.log,**/target/**/*.log`)

**CI Environment:** Java 21, xvnc for headless UI tests, quality gate DELTA=1

## Making Code Changes

**Before Starting:** Identify bundle, check `META-INF/MANIFEST.MF` for dependencies, find test bundle

**Common Pitfalls:**
1. New dependencies → OSGi dependency issues
2. API changes → Breaks compatibility (CI will fail)
3. UI code not on Display thread → Crashes
4. Undisposed SWT resources → Memory leaks
5. Missing build.properties updates → Build failures

**Critical Patterns:**
```java
// Resource disposal
Display display = Display.getDefault();
Shell shell = new Shell(display);
try { /* use */ } finally { shell.dispose(); }

// Async UI
Display.getDefault().asyncExec(() -> { /* UI code */ });

// Data binding
DataBindingContext ctx = new DataBindingContext();
ctx.bindValue(
    WidgetProperties.text(SWT.Modify).observe(widget),
    BeanProperties.value("prop").observe(bean)
);
```

**Key Files:** `MANIFEST.MF` (dependencies), `plugin.xml` (extensions), `build.properties` (build), `.settings/*.prefs` (compiler)

## Critical Rules

1. Check `MANIFEST.MF` for dependencies before adding imports
2. Don't break API compatibility - API tools will fail build
3. Dispose SWT resources (fonts, images, shells) - system colors don't need disposal
4. UI code must run on Display thread (use `asyncExec`/`syncExec`)
5. Use existing test infrastructure (JUnit 4, UI harness)
6. Update `build.properties` when adding files/packages
7. Follow OSGi patterns - declarative services, no static dependencies

## Quick Reference & Documentation

**Docs:** `docs/JFace.md`, `docs/JFaceDataBinding.md`, `docs/Eclipse4_RCP_FAQ.md`, `docs/PlatformCommandFramework.md`
**Links:** [Platform UI wiki](https://wiki.eclipse.org/Platform_UI), [Contributing](https://github.com/eclipse-platform/.github/blob/main/CONTRIBUTING.md)

**Root Files:** `.github/` (workflows), `.mvn/` (Maven config), `bundles/` (57), `tests/` (34), `examples/` (25), `features/`, `releng/`, `docs/`, `pom.xml`, `Jenkinsfile`

## Agent Guidance

**Trust these instructions** - validated and tested. Search only if information is incomplete or incorrect.

**Making Changes:**
1. Identify affected bundle(s)
2. Review `MANIFEST.MF` dependencies
3. Find corresponding test bundle
4. Make minimal, focused changes
5. Update `build.properties` if adding files
6. Verify OSGi manifest updates if needed

**Test Failures:**
- Check UI test needs Display/Shell setup
- Verify `useUIHarness=true` in test config
- Check resource disposal
- Review setup/teardown initialization

**Build Errors:**
- "Non-resolvable parent POM" → Expected, needs aggregator parent
- "Package does not exist" → Check `MANIFEST.MF` imports
- "API baseline errors" → API compatibility broken
- Test hangs → Missing `Display.syncExec`/`asyncExec`
