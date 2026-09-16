program         := { WS declaration } WS ;

declaration     := class
                 | fun
                 | var
                 | statement
                 ;

class           := "class" WS identifier [ WS "<" WS identifier ] WS "{" { WS funWithOutKey } WS "}" ;

funCmt          := "//:" WS "(" WS [ { identifier WS "," WS } identifier WS ] ")" WS "->" WS identifier <"\n"> ;
fun             := [ funCmt WS ] "fun" WS funWithOutKey ;
funWithOutKey   := identifier WS "(" [ { WS identifier WS "," } WS identifier] WS ")" WS block ;

varCmt          := "//:" WS { varMod WS } identifier <"\n"> ;
varMod          := "final" | "immutable" ;
var             := "var" WS identifier [ WS "=" WS expression ] WS ";" ;

statement       := exprStmt
                 | for
                 | if
                 | print
                 | return
                 | while
                 | block
                 ;

exprStmt        := expression WS ";" ;
for             := "for" WS "(" WS ( var | exprStmt | ";" ) [ WS expression ] WS ";" [ WS expression ] WS ")" WS statement ;
if              := "if" WS "(" WS expression WS ")" WS statement [ WS "else" WS statement ] ;
print           := "print" WS expression WS ";" ;
return          := "return" [ WS expression ] WS ";" ;
while           := "while" WS "(" WS expression WS ")" WS statement ;
block           := "{" { WS declaration } WS "}" ;

expression      := assignment ;

assignment      := ternary [ WS ( "=" | "+=" | "-=" | "*=" | "/=" | "%=" | "<<=" | ">>=" | "&=" | "^=" | "|=" ) WS assignment ] ;
ternary         := logicOr [ WS "?" WS expression WS ":" WS ternary ]  ; (* https://en.cppreference.com/c/language/operator_precedence#cite_note-3 *)
logicOr         := logicAnd [ WS ( "||" | "or" ) WS logicOr ] ;
logicAnd        := binOr [ WS ( "&&" | "and" ) WS logicAnd ] ;
binOr           := binXor [ WS "|" WS binOr ] ;
binXor          := binAnd [ WS "^" WS binXor ] ;
binAnd          := equality [ WS "&" WS binAnd ] ;
equality        := comparison [ WS ( "==" | "!=" ) WS equality ] ;
comparison      := bitShift [ WS ( "<" | "<=" | ">" | ">=" ) WS comparison ] ;
bitShift        := addition [ WS ( "<<" | ">>" ) WS bitShift ] ;
addition        := multiply [ WS ( "+" | "-" ) WS addition ] ;
multiply        := unary [ WS ( "*" | "/" | "%" | "mod" ) WS multiply ] ;
unary           := ( "+" | "-" | "!" ) WS unary | postfix ;
postfix         := callOrAccess [ WS ( "++" | "--" ) ] ;

callOrAccess    := primary { WS callOrAccess } ;
callOrAccessIn  := "(" WS callParams WS ")"
                 | "." WS identifier
                 | "[" WS expression WS "]" ;
primary         := bool
                 | null
                 | this
                 | number
                 | string
                 | identifier
                 | <"("> expression <")">
                 | superAccess ;

callParams      := [ { expression WS "," WS } expression ] ;

number          := integer
                 | float
                 ;
integer         := "0x" #"[0-9a-fA-F]+"
                 | "0b" #"0b[0-1]+"
                 | #"[0-9]+"
                 ;
float           := #"[0-9]+\.[0-9]+f?" ;

symbolInString  := <'$'> ( identifier | <'{'> expression <'}'> ) ;
escapeSequence  := <'\\'> ( '"' | 'b' | 'f' | 'n' | 'r' | 't' | '{' | 'u' 4 #'[a-fA-F0-9]' | 'x' 8 #'[a-fA-F0-9]' ) ;
stringInsertion := <'{'> #'[0-9]+' <'}'> ;
string          := <'"'> { #'[^$"\\\\{]+' | symbolInString | escapeSequence | stringInsertion } <'"'> ;

comment         := blockcomment
                 | linecomment
                 ;
blockcomment    := "/*" #"^(\*/)*" "*/" ;
linecomment     := "//" !":" #"[^\n]*" ( "\n" | EOF ) ;

identifier      := #"[a-zA-Z_][a-zA-Z0-9_]*" ;
this            := "this" ;
bool            := "true"
                 | "false"
                 ;
null            := "nil" ;
superAccess     := "super" "." identifier ;

<WS>            := { <#"[\s\r\n]*"> | comment } ;
