# lein-auto-install

A Leiningen plugin to automatically run `lein install` for checkouts projects.

## Usage

Put `[lein-auto-install "0.1.0"]` into the `:plugins` vector of your project.clj and add `.lein-auto-install` to your project's `.gitignore`.

After the above setup, you can use lein and checkouts like you normally would, except you should never have to run `lein install` for a checkouts dependency every again.

`lein-auto-install` recursively traverses the `checkouts/` directories of projects, starting from the current project and runs `lein install` for any project with a `project.clj` that it has not see before. A set of MD5 sums of `project.clj` files is maintained in `.lein-auto-install` to avoid running `lein install` unnecessarily. 

`lein install` only needs to be run for newly seen projects or when the dependencies of an already seen project changes. It is assumed that watching for changes in the `project.clj` file is sufficient to detect when dependencies of a project has changed.

## License

Copyright © 2015 Hyper Labs

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
