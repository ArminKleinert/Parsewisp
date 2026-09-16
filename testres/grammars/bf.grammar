(* From https://esolangs.org/wiki/Brainfuck *)
program = (code | <comment>)*
code    = token | loop
loop    = '[' (code | <comment>)* ']'
token   = '<' | '>' | '+' | '-' | ',' | '.'
comment = !code #'[^+\-<>\[\],.]+'
