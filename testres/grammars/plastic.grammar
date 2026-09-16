(* Grammar constructed from my understanding of https://github.com/rogeralsing/PlasticLang/blob/master/Plastic/PlasticParser.cs *)

S = Statements

MultiplyOperator = '*'
DivideOperator = '/'
AddOperator = '+'
SubtractOperator = '-'
EqualsOperator = '=='
NotEqualsOperator = '!='
GreaterThanOperator = '>'
GreateerOrEqualOperator = '>='
LessThanOperator = '<'
LessOrEqualOperator = '<='
BooleanOr = '||'
BooleanAnd = '&&'
DotOperator = '.'
Separator = ',' | ';'

Symbol = #'[a-zA-Z_@][a-zA-Z0-9!?_]*'
Number = ('+' | '-')? #'[0-9]+'

SymbolInString = <':'> Symbol
EscapeSequence = <'\\'> ('u' 4 #'[a-fA-F0-9]' | 'x' 8 #'[a-fA-F0-9]' | 'b' | 'f' | 'n' | 'r' | 't' | '{')
StringInsertion = '{' #'[0-9]+' '}'
QuotedString = '"' ('\\"' | #'[^:"\\\\{]+' | SymbolInString | EscapeSequence | StringInsertion)* '"' (* Double-quoted *)
             | "'" ("\\'" | #"[^:'\\\\{]+" | SymbolInString | EscapeSequence | StringInsertion)* "'" (* Single-quoted *)

Literal = Symbol | Number | QuotedString

NotExpression = <'!'> Dot

IdentifierInc = Symbol WS '++'
IdentifierDec = Symbol WS '--'

Value = TupleOrParenValue
      | NotExpression
      | ArrayValue
      | IdentifierInc
      | IdentifierDec
      | Literal
      | Body

Dot = Dot WS <'.'> WS InvocationOrValue | InvocationOrValue

Term = Term WS ('+' | '-') WS InnerTerm | InnerTerm

InnerTerm = InnerTerm WS ('*' | '/') WS Dot | Dot

Compare = Compare WS ('==' | '!=' | '>=' | '>' | '<=' | '<') WS Term | Term

BooleanLogic = BooleanLogic WS ('&&' | '||') WS Compare | Compare

Assignment = Assignment WS <'='> WS BooleanLogic | BooleanLogic

LetAssign = Symbol WS <':='> WS Expression

Expression = LambdaDeclaration | LetAssign | Assignment

TerminatedStatement = Expression WS (Separator WS)?

Statement = TerminatedStatement

Statements = (Statement WS)*

Body = <'{'> WS Statements WS <'}'>

LambdaBody = Expression

LambdaArgs = WS <'('> WS ((Expression WS Separator WS)* Expression)? WS <')'> WS
           | WS Symbol WS

LambdaDeclaration = WS LambdaArgs WS '=>' WS LambdaBody

TupleOrParenValue = WS <'('> WS (Expression WS Separator WS)* Expression WS <')'> WS

InvocationArgs = WS <'('> WS ((Expression WS Separator WS)* Expression)? WS <')'> WS

ArrayValue = WS <'['> WS ((Expression WS Separator WS)* Expression)? WS <']'> WS

InvocationOrValue = Value InvocationArgs* Body?

<WS> = <#'[\s]*'> | <(('//' #'[^\n]*' ('\n' | EOF)) WS)> | <('/*' #'.*' '*/' WS)>

















