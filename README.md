# Sherlock

A PoC implementation of [GumTree](https://doi.org/10.1145/2642937.2642982)-based AST-diff plagiarism engine,

based on https://github.com/DCS-Sherlock/Sherlock.

> [!IMPORTANT]
> When using the AST Diff Detector, due to some development timeline constraints, some issues should be noted:
>
> You may ignore the language option when choosing the AST Diff Detector, as the detector is not language-specific.
> The full UI integration is still TBD.
> So you should only submit two same-language files in one single workspace for
> using the AST Diff Detector.
> The final plagiarism result will only be shown in the overall score, and the code
> highlighting is not yet supported.

## Get Started

### Use the application

Go to https://sherlock.xcc.tw for using the online version of Sherlock.
However, since it is not hosted on a powerful server, the performance may not be as good as expected.

> [!NOTE]
> You may need to contact us to get the account for using the online version of Sherlock.

### Use the pre-built docker image

[![docker](https://img.shields.io/badge/Docker-2496ED.svg?style=flat&logo=Docker&logoColor=white)](https://hub.docker.com/r/xanonymous/sherlock)

#### Requirements

- [Docker](https://www.docker.com/get-started/)

Use our pre-built docker image to run Sherlock on your local machine is the easiest way to get started.

You don't have to face the risks of missing dependencies or misconfigurations.

And you can also use the given [docker compose](https://docs.docker.com/compose/) file to run Sherlock with only one
command.

```bash
# In the root directory of this project
docker compose up -d
```

Then you may visit `http://localhost:8080` to use Sherlock.
The default email and password are `admin@sherlock.xcc.tw` and `sherlock`, respectively.
(If using the given docker-compose file)

### Build from source

#### Requirements

- JDK 21

1. Clone the repository
    ```bash
    # recursively clone the repository
    git clone --recursive git@github.com:Xanonymous-GitHub/sherlock-with-dsl.git
    ```

2. Build the UI project, and move the static files into the resource folder.
   The UI project is https://github.com/Xanonymous-GitHub/sherlock-js-bundle;
   You may follow the instructions in its README to build the project.
   After built, move all files in the `dist/assets` folder (in the UI project folder) to `src/main/resources/static`
   folder (in this project's folder).

3. Run the project
    ```bash
    ./gradlew bootRun
    ```
