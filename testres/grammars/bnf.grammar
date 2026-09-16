(* Grammar from https://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form#BNF_representation_of_itself *)

syntax         ::= rule | rule syntax
rule           ::= opt_whitespace "<" rule_name ">" opt_whitespace "::=" opt_whitespace expression line_end
opt_whitespace ::= " " opt_whitespace | ""
expression     ::= list | list opt_whitespace "|" opt_whitespace expression
line_end       ::= opt_whitespace "\n" | opt_whitespace "\n" line_end
list           ::= term | term opt_whitespace list
term           ::= literal | "<" rule_name ">"
literal        ::= '"' text1 '"' | "'" text2 "'"
text1          ::= "" | character1 text1
text2          ::= "" | character2 text2
character      ::= letter | digit | symbol
letter         ::= "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z" | "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
digit          ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
symbol         ::= "\n" | "\r" | "\t" | "|" | " " | "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"
character1     ::= character | "'"
character2     ::= character | '"'
rule_name      ::= letter | rule_name rule_char
rule_char      ::= letter | digit | "-"
