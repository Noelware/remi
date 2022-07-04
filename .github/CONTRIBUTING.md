# 🧶 Contributing to Remi
Thanks for stopping by and reading this! I'm going to assume you want to contribute to this library!

Before you do, you must read the [Code of Conduct](./CODE_OF_CONDUCT.md) before continuing to make sure what
the conduct is before submitting an issue or Pull Request.

## Requirements
If you wish to tamper with the code, you will need:

- Gradle 7.4.2 (you can see what Gradle version we're using via [gradle.properties](../gradle/wrapper/gradle-wrapper.properties))
- IntelliJ, Eclipse, or Netbeans
- Java 17 or higher

If you want to just edit some documentation, you can visit [github.dev](https://github.dev/Noelware/remi) to open up Visual Studio Code
to edit the documentation~

## Tips
- Make sure when you're submitting an issue or PR, it isn't a duplicate of a pre-existing issue/PR.
- Make sure you're using the right labels when submitting an issue or pull request;
    - Use the `bug` label if this contains fixes of a bug.
    - Use the `documentation` label if this issue/PR is cleaning up documentation.
    - Use the `enhancement` label if this issue/PR considers breaking changes via binary/API. You will need to run
      the `./scripts/deploy-docs.sh` command to update the documentation.
    - If you're using the wrong labels, the contributors might edit the labels to be correct!
- Be clear and concise with the title, issue/PR body, so it'll be easier to link other issues/PRs with solutions!
- Specify on how we can reproduce the bug. If you can't really, then it'll be impossible to fix it (aka it works on my machine:tm:)
- Clarify the library version, what environments you're using (Java Version, Kotlin Version, Gradle Version).

Have fun contributing!~ ^3^
