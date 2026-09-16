program        ::= assignments? term
assignments    ::= (identifier "≡" term)+

term           ::= abstraction | application

abstraction    ::= "λ" variable "." term

application    ::= atom +

atom           ::= identifier
                 | "(" term ")"

variable       ::= identifier
identifier     ::= "{" #"[a-zA-Z]+" "}" | #"[a-zA-Z]"