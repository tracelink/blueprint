# Blueprint

Blueprint simplifies the process of defining and updating OPA policies by generating valid Rego from simple, configurable user inputs. 

Users design policies via a UI by selecting base statements to add to policy clauses, and configuring statements with any required arguments. Users can choose to save policies to the database or export the policies to Rego code, which can be copied and used in an OPA engine or playground environment. The JSON format of the policy is also displayed, and can be copied to reimport to Blueprint at a later time. Blueprint is designed to simplify some of the features of the Rego language to reduce errors when writing Rego policies and standardize policy format across a range of use cases.

## Technologies

Spring Boot - For web server technology  
Flyway - For data migration  
PostgresDB - For database
Thymeleaf - For UI template rendering

## Installation

From source code, type `mvn clean package` in the root directory. After packaging, the server jar is located in `blueprint-app/target/blueprint-app*.jar`.

To execute this jar requires a `JDBC_URL`, `JDBC_USERNAME`, and `JDBC_PASSWORD` or a link to a configuration properties file as described in the [Spring Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config) to supply these variables. An example can be seen [here](./blueprint-app/src/main/resources/application-prd.yaml).

By default, the server runs on port 8081 and will configure the database tables automatically, including any necessary migrations from version to version. Blueprint can be run in a container using Docker. A local setup is configured in [Dockerfile](Dockerfile).

For development purposes and initial setup of Blueprint, an initial user is configured by default in the Blueprint user database. This user is given the username "admin" and a random password that is output to the Blueprint logs. Once Blueprint has been deployed, set up a new, named admin user with a secure password and the Admin role, and then disable or delete the default admin user to prevent unauthorized access to Blueprint configurations.

On every startup, Blueprint will check for this admin user. If the user exists and is not disabled, Blueprint will automatically reset the password and output it to the logs. If the user doesn't exist, Blueprint will ensure a user does exist that has the Admin role. If such a user does not exist, Blueprint will re-create the admin and grant it the Admin role so that administrators cannot get locked out while modifying users or roles.

## Core Concepts

### Policies

A policy in Blueprint consists of one or more clauses. In order for the policy to evaluate to true, at least one clause must evaluate to true. A clause consists of one or more statements, all of which must evaluate to true for the clause to evaluate to true. This structure follows disjunctive normal form (DNF) for consistency while giving full flexibility to the user to define any required conditions.

### Statements

Each statement within a policy clause is a *configured* statement--the combination of a base statement and any additional required user configurations. Base statements are the building blocks for policies and can be created and managed within Blueprint. Base statements may have the option to be negated, meaning that a user can choose to allow or disallow the condition represented by the base statement (the equivalent of NOT in DNF boolean logic). At its core, a base statement is a single Rego function call. As such, it may include arguments that must be provided by users when creating policies. The number of arguments will vary depending on the base statement. These arguments provide the core flexibility of Blueprint: base statements can be defined to call any Rego function, and users build policies by selecting base statements and then configuring arguments for a specific use case.

### Functions

Base statements reference functions that are defined and managed in Blueprint. Functions may have dependencies on other helper functions as well. A function in Blueprint contains the body of the Rego function that will be evaluated for the policy. It also specifies a list of parameters that will be matched against base statement arguments for any base statements that reference the function. Functions provide the core Rego code that will be used to generate full Rego policies for users. Any function may be written into Blueprint, but keep in mind that providing flexible functions that can fit a broad range of use cases will be best for long-term maintenance of policies.

### Examples

Example base statements and functions are located in [baseStatements.json](./blueprint-app/src/main/resources/static/baseStatements.json) and will be automatically loaded into the Blueprint database on startup.

## Versioning

Policies, base statements and functions are versioned in Blueprint. Different versions of the same policy element are linked by their names, which remain constant. Each of these policy elements also maintains its current state: either Draft, Released, or Deprecated. Policy elements begin in the Draft state when they are created, and can be saved in the Draft state to return to at a later time. However, policy elements must be promoted to the Released state before they can be incorporated into another policy element. For example, a particular base statement version must be released before it can be used in a policy clause. Each policy element can only have one Draft version at a given time. 

Once a version has been released, it can only be deprecated or deleted. Policy elements can only be deleted in Blueprint if the policy element version is not in use by any other Blueprint element. For base statements, this means the base statement version cannot be used in any policy. For functions, this means the function cannot be used by any base statement or as a dependency of any other function.

If a policy element is no longer useful or best practice, it can be promoted to the Deprecated state. Deprecated policy elements may still be used by other policy elements stored in Blueprint. However, if a user tries to edit a policy element that includes a Deprecated policy element, they will not be able to save their work until the Deprecated element is updated to a Released version or removed from use. If a pollicy element is out of date (i.e. there is a newer Released version of the same element available), then a warning will be diplayed to the user but they will not be forced to update before saving.

## Rules

To help provide guardrails when users are designing a policy, Blueprint implements a rules system using the Visitor pattern to traverse a policy and identify any logic flaws or design issues. Logic flaws include mistakes such as crafting a policy that always evaluates to true (e.g. A v ~A), or always evaluates to false (e.g. A ^ ~A). The rules are applied to policies, base statements and functions prior to saving in the database, and if a rule fails with an error, the user must fix the problem before saving the policy element.

## Authentication

Users can register and authenticate to Blueprint via a local login form and, optionally, an OIDC SSO login flow. To configure SSO login, specify the `CLIENT_ID`, `CLIENT_SECRET`, and `ISSUER_URI` environment variables listed in [application-prd.yml](./blueprint-app/src/main/resources/application-prd.yml). To disable SSO login, comment out the YAML configuration under `spring.security` in the same file. 

## Authorization

Blueprint is defined with four core roles that can be assigned to give users different levels of access.

|Role|Description|
|----|-----------|
|User|This is the most basic level of access, which allows users to view and create policies, as well as edit and delete any policies that they have authored. Assigned automatically to new users when they create an account or login via SSO for the first time.|
|Base Statement Editor|Allows users to view and create base statements, as well as edit, delete, or update the state of any base statements they have authored. Some knowledge of the Rego language is helpful.|
|Function Editor|Allows users to view and create base statement functions, as well as edit, delete, or update the state of any base statement functions they have authored. Requires an in-depth understanding of the Rego language.|
|Admin|Full access to all policy, base statement, and base statement function features, as well as DB console access and ability to manage users.|

## Authors

[Maddie Cool](https://github.com/madisoncool)
[Chris Smith](https://github.com/tophersmith)

## License

[MIT License](https://opensource.org/licenses/MIT)
