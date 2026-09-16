S         ::= expr

expr      ::= sum
            | product
            | value

sum       ::= sum addsub prodvalue
            | prodvalue addsub prodvalue
            | sum '-' parensum
            | prodvalue '-' parensum

addsub    ::= '+' | '-'

parensum  ::= '(' sum ')'

prodvalue ::= value
            | product

product   ::= prodterm muldiv value
            | prodterm muldiv parensum
            | prodterm '/' parenprod

prodterm  ::= prodvalue 
            | parensum

parenprod ::= '(' product ')'

value     ::= #'[0-9]+'
muldiv    ::= '*' | '/'