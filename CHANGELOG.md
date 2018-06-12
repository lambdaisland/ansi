# lambdaisland.ansi Changelog

## v0.1.2 - 2018-06-12

Added optimizations. Calling token-stream on plain text without escape sequences
will now short circuit, leading to a significant speedup for the cases where
most input is plain text.

## v0.1.1 - 2018-05-30

Initial release
