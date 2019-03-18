# Development notes

You should be familiar with the Maven build environment. This project follows its conventions closely.

## Versioning

When creating a new version, make sure to update it in:
- the top-level `pom.xml` as well as all the Java module pom.xml files (backend, nsi, pss, shared, migration)
- the static string version in `net.es.oscars.web.rest.MiscController`

## Testing

You can run the unit tests with the command:

```bash
mvn test
```

You may also install only if the tests pass by running:

```bash
mvn install
```

## Creating a release on GitHub

* Make sure the tag associated with the release has a description that contains the version number
* Before releasing a new version, make sure to check the [UPDATING](../UPDATING.md) document

## Pull Requests

In order to aid in keeping track of the project and keep the repo tidy we are using [Conventional Commits](https://www.conventionalcommits.org/) and [Squash Merges](https://blog.github.com/2016-04-01-squash-your-commits/) into develop.

Conventional commits provide a structured way of building commit messages that make it easier to understand the impact of each pull request.

Squash merges simplify the history of repo to make it easy to see what features have been implemented. Generally the small incremental commits done on a topic branch are not of much interest after the feature has been reviewed and merge. Squash commits address this issue. It is important to note that we only use Squash Merges when we are merging into the develop branch. Merging from develop to master is done with a normal merge because it is important to retain the sequence of commits.

Each pull request should be based on a Jira issue and developed on it's own topic branch. It is the responsibility of the person submitting the pull request to handle merging it once it is approved.

The title of PR should look like:

    type: [OS-XXX] Short description 

Where:

    - `type` is one of: 'build', 'ci', 'chore',  'docs',  'feat', 'fix',  'perf', 'refactor', 'revert', 'style', 'test' 
    - `OS-XXX` refers to a Jira issue. 
    - If this is a breaking change prefix the description with "BREAKING CHANGE:". A breaking change will affect other components of netbeam or be visible to end users in a non-backwards compatible way.)