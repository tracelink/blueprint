# Blueprint

Blueprint simplifies the process of defining and updating OPA policies by generating valid Rego from simple, configurable user inputs.

## Policy Structure

A policy in Blueprint consists of one or more clauses. In order for the policy to evaluate to true, at least one clause must evaluate to true. A clause consists of one or more statements, all of which must evaluate to true for the clause to evaluate to true. This structure follows disjunctive normal form (DNF) for consistency while giving full flexibility to the user to define any required conditions.

### Statements

Each statement within a policy clause is a *configured* statement--the combination of a base statement and any additional required user configurations. Base statements are the building blocks for policies and can be created and managed within Blueprint. Base statements may have the option to be negated, meaning that a user can choose to allow or disallow the condition represented by the base statement (the equivalent of NOT in DNF boolean logic). At its core, a base statement is a single Rego function call. As such, it may require arguments that must be provided by users when creating policies. The number of arguments will vary depending on the base statement.

### Functions

Base statements reference functions that are defined and managed in Blueprint. Functions may have dependencies on other helper functions as well. A function in Blueprint contains the body of the Rego function that will be evaluated for the policy. It also specifies a list of parameters that will be matched against base statement arguments for any base statements that reference the function. Functions provide the core Rego code that will be used to generate full Rego policies for users. Any function may be written into Blueprint, but keep in mind that providing flexible functions that can fit a broad range of use cases will be best for long-term maintenance of policies.
