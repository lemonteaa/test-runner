# test-runner

*(Note: Still in Proof-Of-Concept stage)*

Run (Clojure) tests automatically in [LightTable](http://lighttable.com/) and display results in GUI

## Why?

While REPL-driven development is a good thing in any language that supports it (with its high interactivity and tight feedback loop improving programmer happiness and productivity), one potential weakness is that by going too fast one may break things accidentally, especially when one is working with existing, large code base. Static Typing help protects this to some extent, and Unit Testing provides further assurance. Hence one sweetspot is to have REPL together with a full suit of Unit Tests that are re-run automatically at any file save.

Unfortunately, although most decent testing libraries have tooling for automatically running tests (usually called a 'watch files' feature), at least in the case for Clojure they work in the command line. It would be nice if there could be a full integration into IDE such as LightTable so that one doesn't have to context switch mentally to a separate console.

Another possibility opened by such a feature is that the test output can be formatted and presented nicely in the IDE, patching over any deficiency particular testing libraries may have.

## How it works

This plugin provides the frontend for testing, using the usual elements in LightTable's BOT architecture to trigger running tests. Tests are run by sending customized request to the nREPL server of the project. (It is assumed that the project has inserted the tester-nrepl middleware as a plugin)

Results are updated in the GUI as responses arrive. Because there is more than one response messages, and because this doesn't really conform to the eval model implicitly assumed by nREPL (as well as LightTable itself), we need to work directly with the base layer in LightTable instead of using the `:eval.custom` trigger. To make life easier the backend tester-nrepl supports responding in LightTable format (so decoding is automatic and the bencodable issue at the transport layer is also avoided since we use a string).

## Contact

This project is currently developed and maintained by @lemonteaa, which can be reached through email listed on the [github account](https://github.com/lemonteaa).

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
