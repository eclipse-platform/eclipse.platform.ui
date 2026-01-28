# GlobalNextPrevSearchEntryHandler Tests

This directory contains comprehensive unit tests for the `GlobalNextPrevSearchEntryHandler` class.

## Test Files

### GlobalNextPrevSearchEntryHandlerTest.java
Basic unit tests that verify:
- Handler instantiation works correctly
- `setInitializationData()` method handles different configuration scenarios
- Handler implements required interfaces (`IHandler`, `IExecutableExtension`)

### GlobalNextPrevSearchEntryHandlerIntegrationTest.java
Integration tests that verify:
- Handler creation and configuration in various scenarios
- Multiple handler instances can be created independently
- Handler behavior with different configuration parameters

## Test Coverage

The tests cover the following scenarios:

### Configuration Testing
- ✅ "next" command configuration (default behavior)
- ✅ "previous" command configuration
- ✅ Unknown command configuration (defaults to "next")
- ✅ Null configuration handling
- ✅ Empty string configuration handling

### Interface Implementation
- ✅ `IHandler` interface implementation
- ✅ `IExecutableExtension` interface implementation

### Error Handling
- ✅ Null parameter handling in `setInitializationData()`
- ✅ Unknown command type handling

## Running the Tests

These tests are included in the `AllSearchTests` test suite and can be run as part of the Eclipse platform UI test suite.

## Dependencies

The tests use:
- JUnit 5 (`@Test`, `@BeforeEach`)
- Eclipse Core Runtime (`CoreException`, `IConfigurationElement`)
- Eclipse Search 2 UI (`GlobalNextPrevSearchEntryHandler`)

## Notes

- The tests focus on the testable functionality without requiring complex mocking
- Integration tests verify real behavior in the Eclipse environment
- All tests are designed to run in the Eclipse test harness
- Tests verify both positive and negative scenarios
