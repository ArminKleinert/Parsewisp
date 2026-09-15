S               = WS { sexpr WS }
sexpr           = symbol
                | list
                | vector
                | keyword
                | string
                | number
                | tag
                | set
                | map
                | char
                | readerMacro
                | hint
                | anon_func
                | regex_literal

list            = '(' WS { sexpr WS } ')'
vector          = '[' WS { sexpr WS } ']'
set             = '#{' WS { sexpr WS } '}'
map             = '{' WS { mapEntry WS } '}'
mapEntry        = sexpr WS sexpr

anon_func       = '#(' WS { sexpr WS } ')'
hint            = ( "#^" | "^" ) ( symbol | keyword ) WS sexpr
tag             = "#" symbol WS sexpr

readerMacro     = ( "`" | "'"| "@" | "~" ) sexpr

number          = #'[+-]*[0-9]+(N|(\.[0-9]+)?([eE][+-]?))?'

string          = #'"([^"\\]|\\(.|\\n))*"'
regex_literal   = '#' string

char            = !multiChar #'\\.' | multiChar
multiChar       = '\\newline' | '\\return' | '\\space' | '\\tab' | #'\\\\u[a-fA-F0-9][a-fA-F0-9][a-fA-F0-9][a-fA-F0-9]'

keyword         = ":" [':'] symbol

part            = #'[a-zA-Z_\+\*\.\!\-\?\$\%\&\=\<\>\'][a-zA-Z0-9_\+\*\.\!\-\?\$\%\&\=\<\>\'\:\#]*'
symbol          = '/' | part [ '/' part ]

<WS>            = < { #'[\s,]+' | ";" #'[^\n]*' | "#_" WS sexpr } >