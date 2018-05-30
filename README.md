# lambdaisland/ansi [![CircleCI](https://circleci.com/gh/lambdaisland/ansi.svg?style=svg)](https://circleci.com/gh/lambdaisland/ansi)

[![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/ansi.svg)](https://clojars.org/lambdaisland/ansi)

Parse ANSI color codes, optionally convert to Hiccup.

## Features

- Clojure and ClojureScript support (cljc)
- parses color codes and bold, including 8-bit and 24-bit (rgb) colors
- Fine-grained building blocks, convert to Hiccup or a format of your choice

## Usage

Colors in your terminal work by embedding "escape codes" in the text. This
library contains utilities for parsing and transforming these escape codes.

Terminals are stateful, you can set a property like foreground, background, or
bold, and it will stay that way until it gets changes or unset (reset). 

The starting point for dealing with a textual stream is `token-stream`. It takes
a string as input, and returns a sequence of "tokens", either plain text, or a
map of properties that are being set at that point in the stream.

``` clojure
(require '[lambdaisland.ansi :as ansi])

(ansi/token-stream "Hello,\033[1;30;45m world!")
;;=> ["Hello," {:bold true, :foreground [:rgb 0 0 0], :background [:rgb 205 0 205]} " world!"]
```

To convert this to a different format, you need to know which styles apply to
which piece of text, this is what the `apply-props` stateful transducer is for.
It keeps track of the "terminal state", and returns pairs of style information
and text.

``` clojure
(def text "\033[1m here \033[45m we \033[31m go! \033[0m done.")

(sequence ansi/apply-props (ansi/token-stream text))
;;=>
([{:bold true} " here "]
 [{:bold true
   :background [:rgb 205 0 205]} " we "]
 [{:bold true
   :background [:rgb 205 0 205]
   :foreground [:rgb 205 0 0]} " go! "]
 [{} " done."])
```

Now you have all the information available to convert this into Hiccup, you can
convert a single one of these pairs with `chunk->hiccup`.

``` clojure
(ansi/chunk->hiccup [{:bold true, :foreground [:rgb 100 200 0]} "so far..."])
;;=> [:span {:style {:color "rgb(100,200,0)", :font-weight "bold"}} "so far..."]
```

There's a convenience function to do this in one go, `text->hiccup`


``` clojure
(ansi/text->hiccup text)
;;=>
([:span {:style {:font-weight "bold"}} " here "]
 [:span {:style {:background-color "rgb(205,0,205)", :font-weight "bold"}}
  " we "]
 [:span {:style {:color "rgb(205,0,0)",
                 :background-color "rgb(205,0,205)",
                 :font-weight "bold"}}
  " go! "]
 [:span {} " done."])
```

If you deal with streaming text, then you need to incrementally apply these
state changes, carrying over the old state whenever you receive more input.

In this case the `hiccup-xform` might be useful.

``` clojure
(require '[clojure.core.async :as async])

(def channel (chan 1 ansi/hiccup-xform))

(async/go-loop [hiccup-el (async/<! channel)]
  (append-to-dom hiccup-el))

(async/go (async/>! channel "\033[1mhello,\033[35mworld!"))
(async/go (async/>! channel " life is \033[0mgreat!"))
```


## License

&copy; Arne Brasseur 2018
Available under the terms of the Mozilla Public License Version 2.0, see LICENSE.txt
