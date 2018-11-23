## Unreleased

## Added

Add support for parsing a cursor location message (as sent from terminal to process), i.e. "\e[row;col;H"

## Fixed

## Changed


## v0.1.6 - 2018-08-14

Fix a bug where specific character sequences caused regular bits of string to be
treated as escape sequences, essentially swallowing bits of input.

## v0.1.5 - 2018-07-19

Support \e[m as a reset code, together with \e[0m.

## v0.1.4 - 2018-06-13

More optimizations, get rid of the expensive csi regex.

## v0.1.3 - 2018-06-12

Minor optimizations for ClojureScript

## v0.1.2 - 2018-06-12

Added optimizations. Calling token-stream on plain text without escape sequences
will now short circuit, leading to a significant speedup for the cases where
most input is plain text.

## v0.1.1 - 2018-05-30

Initial release
