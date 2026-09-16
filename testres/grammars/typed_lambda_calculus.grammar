program        ::= assignments? term
assignments    ::= (identifier "≡" term)

term           ::= abstraction | application

abstraction    ::= "λ" variable ":" type "." term

application    ::= atom +

atom           ::= identifier
                 | "(" term ")"

type           ::= type_atom ("→" type)?
type_atom      ::= type_identifier | "(" type ")"

variable       ::= identifier
type_identifier::= identifier
identifier     ::= "{" #"[a-zA-Z]+" "}" | #"[a-zA-Z]"