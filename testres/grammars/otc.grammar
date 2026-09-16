(* From https://esolangs.org/wiki/Obfuscated_Tiny_C *)

program= decl*

NAME   = #'[a-zA-Z][a-zA-Z0-9_]*'
INT    = #"[0-9]+"
CHAR   = #"'[^\\']'"
STRING = #'\"(\\.|[^\\"])*\"'

decl= vardecl
    | fundecl

vardecl= type NAME ";"
       | type NAME "[" INT "]" ";"

fundecl= type NAME "(" args ")" "{" body "}"

args= (( arg "," )* arg)?

arg= type NAME

body= vardecl* stmt*

stmt= ifstmt
    | whilestmt
    | dowhilestmt
    | "return" expr <";">
    | expr <";">
    | "{" stmt* "}"
    | <";">

ifstmt= "if" "(" expr ")" stmt
      |  "if" "(" expr ")" stmt "else" stmt

whilestmt= "while" "(" expr ")" stmt

dowhilestmt= "do" stmt "while" "(" expr ")" <";">

expr= expr binop expr
    | unop expr
    | expr "[" expr "]"
    | "(" expr ")"
    | expr "(" exprs ")"
    | NAME
    | INT
    | CHAR
    | STRING

exprs= ((expr ",")* expr)?

binop= "+" | "-" | "*" | "/" | "%"
     | "="
     | "<" | "==" | "!="

unop= "!" | "-" | "*"

<type>= "int" stars?
      | "char" stars?

stars= "*"+