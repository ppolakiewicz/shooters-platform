# Lessons Learned

> Append-only register of recurring rules and patterns. Re-read at start by /10x-frame, /10x-research, /10x-plan,
> /10x-plan-review, /10x-implement, /10x-impl-review.

## Replace magic numbers with named constants

- **Context**: All domain code.
- **Problem**: Magic numbers make code harder to understand.
- **Rule**: Always replace magic numbers with meaningfully named static constants.
- **Applies to**: implement, impl-review

## Use package-private constructors for Spring components

- **Context**: All classes marked as Spring Boot services or components.
- **Problem**: Prevent manual construction of these classes in code.
- **Rule**: Classes marked as Spring Boot services or components should have package-private constructors.
- **Applies to**: implement, impl-review

## Describe every Spock test step

- **Context**: All Spock tests and their test steps.
- **Problem**: Tests without step descriptions are harder to read.
- **Rule**: Every Spock test step should include a descriptive label.
- **Applies to**: implement, impl-review

## Do not test Flyway-managed schema directly

- **Context**: Integration tests for database schema.
- **Problem**: Schema behavior is already exercised by use case integration tests.
- **Rule**: Do not test Flyway-managed schema structure directly; verify it through use case integration tests.
- **Applies to**: plan, plan-review, implement, impl-review
