# lambdaisland/ansi [![CircleCI](https://circleci.com/gh/lambdaisland/ansi.svg?style=svg)](https://circleci.com/gh/lambdaisland/ansi)

Parse ANSI color codes to Hiccup (HTML)

``` clojure
(require '[lambdaisland.ansi :refer [ansi->hiccup]])

(ansi->hiccup (str "\033[31m this is red"
                   "\033[45m magenta background"
                   "\033[1m bold"
                   "\033[32m green foreground"))
;;=> ([:span {:class "red"} " this is red"]
;;    [:span {:class "red bg-magenta"} " magenta background"]
;;    [:span {:class "bold red bg-magenta"} " bold"]
;;    [:span {:class "bold green bg-magenta"} " green foreground"])
```

Only the original 8 colors are supported, multi-byte extensions are not implemented.

There's a main namespace that you can use to convert a file from the command line

```
command_that_generates_colorful_output > output.log
clj -m lambdaisland.ansi.convert output.log > output.html
```

## License

&copy; Arne Brasseur 2018
Available under the terms of the Eclipse Public License 1.0, see LICENSE.txt
