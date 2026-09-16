(* Grammar from https://kops.uni-konstanz.de/server/api/core/bitstreams/6e7a9d65-73a7-4f05-bc0d-5904c83702bc/content *)

system       = funcdefs "." expr
             | expr

funcdefs     = "def" def
             | funcdefs "def" def

defs         = def
             | def ";" defs

def          = name abstraction

abstraction  = "=" expr | name abstraction

expr         = expr "where" defs
             | condexpr

condexpr     = "if" expr "then" condexpr "else" condexpr
             | listexpr

listexpr     = opexpr ":" listexpr
             | opexpr

opexpr       = prefix opexpr
             | opexpr infix opexpr
             | comb

comb         = comb simple
             | simple

simple       = name
             | builtin
             | constant
             | "(" expr ")"

name         = id

builtin      = "hd"
             | "tl"

constant     = num | bool | string | "nil" | list

list         = "[" "]"
             | "[" listelements "]"

listelements = expr
             | listelements "," expr

prefix       = "-" | "+" | "not"
infix        = "+" | "-" | "*" | "/" | "=" | "~=" | "<" | ">" | "<=" | ">=" | "and" | "or"
id           = #"[a-zA-Z_][a-zA-Z_0-9]*"
num          = #"[0-9]+"
bool         = "true" | "false"
string       = #'\"(\\.|[^\\"])*\"'


























