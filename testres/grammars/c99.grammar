S : translation_unit
  | expression?
  ;

AUTO: 'auto' ;
BREAK: 'break' ;
CASE: 'case' ;
CHAR: 'char' ;
CONST: 'const' ;
CONTINUE: 'continue' ;
DEFAULT: 'default' ;
DO: 'do' ;
DOUBLE: 'double' ;
ELSE: 'else' ;
ENUM: 'enum' ;
EXTERN: 'extern' ;
FLOAT: 'float' ;
FOR: 'for' ;
GOTO: 'goto' ;
IF: 'if' ;
INLINE: 'inline' ;
INT: 'int' ;
LONG: 'long' ;
REGISTER: 'register' ;
RESTRICT: 'restrict' ;
RETURN: 'return' ;
SHORT: 'short' ;
SIGNED: 'signed' ;
SIZEOF: 'sizeof' ;
STATIC: 'static' ;
STRUCT: 'struct' ;
SWITCH: 'switch' ;
TYPEDEF: 'typedef' ;
UNION: 'union' ;
UNSIGNED: 'unsigned' ;
VOID: 'void' ;
VOLATILE: 'volatile' ;
WHILE: 'while' ;
BOOL: '_Bool' ;
COMPLEX: '_Complex' ;
IMAGINARY: '_Imaginary' ;

keyword
  : AUTO
  | BREAK
  | CASE
  | CHAR
  | CONST
  | CONTINUE
  | DEFAULT
  | DO
  | DOUBLE
  | ELSE
  | ENUM
  | EXTERN
  | FLOAT
  | FOR
  | GOTO
  | IF
  | INLINE
  | INT
  | LONG
  | REGISTER
  | RESTRICT
  | RETURN
  | SHORT
  | SIGNED
  | SIZEOF
  | STATIC
  | STRUCT
  | SWITCH
  | TYPEDEF
  | UNION
  | UNSIGNED
  | VOID
  | VOLATILE
  | WHILE
  | BOOL
  | COMPLEX
  | IMAGINARY
  ;

ELLIPSIS: "..." ;
RIGHT_ASSIGN: ">>=" ;
LEFT_ASSIGN: "<<=" ;
ADD_ASSIGN: "+=" ;
SUB_ASSIGN: "-=" ;
MUL_ASSIGN: "*=" ;
DIV_ASSIGN: "/=" ;
MOD_ASSIGN: "%=" ;
AND_ASSIGN: "&=" ;
XOR_ASSIGN: "^=" ;
OR_ASSIGN: "|=" ;
RIGHT_OP: ">>" ;
LEFT_OP: "<<" ;
INC_OP: "++" ;
DEC_OP: "--" ;
PTR_OP: "->" ;
AND_OP: "&&" ;
OR_OP: "||" ;
LE_OP: "<=" ;
GE_OP: ">=" ;
EQ_OP: "==" ;
NE_OP: "!=" ;
SEMICOLON: ";" ;
BLOCK_OPEN: ("{"|"<%") ;
BLOCK_CLOSE: ("}"|"%>") ;
COMMA: "," ;
COLON: ":" ;
ASSIGN: "=" ;
BRACE_OPEN: "(" ;
BRACE_CLOSE: ")" ;
SQUARE_OPEN: ("["|"<:") ;
SQUARE_CLOSE: ("]"|":>") ;
DOT: "." ;
BIT_AND: "&" ;
UNARY_NOT: "!" ;
TILDE: "~" ;
MINUS: "-" ;
PLUS: "+" ;
STAR: "*" ;
SLASH: "/" ;
PERCENT: "%" ;
LESS_THAN: "<" ;
GREATER_THAN: ">" ;
BIT_XOR: "^" ;
BIT_OR: "|" ;
QUESTION: "?" ;

COMMENT_OPEN: "/*" ;
COMMENT_CLOSE: "*/" ;

punctuator
  : ELLIPSIS
  | RIGHT_ASSIGN
  | LEFT_ASSIGN
  | ADD_ASSIGN
  | SUB_ASSIGN
  | MUL_ASSIGN
  | DIV_ASSIGN
  | MOD_ASSIGN
  | AND_ASSIGN
  | XOR_ASSIGN
  | OR_ASSIGN
  | RIGHT_OP
  | LEFT_OP
  | INC_OP
  | DEC_OP
  | PTR_OP
  | AND_OP
  | OR_OP
  | LE_OP
  | GE_OP
  | EQ_OP
  | NE_OP
  | SEMICOLON
  | BLOCK_OPEN
  | BLOCK_CLOSE
  | COMMA
  | COLON
  | ASSIGN
  | BRACE_OPEN
  | BRACE_CLOSE
  | SQUARE_OPEN
  | SQUARE_CLOSE
  | DOT
  | BIT_AND
  | UNARY_NOT
  | TILDE
  | MINUS
  | PLUS
  | STAR
  | SLASH
  | PERCENT
  | LESS_THAN
  | GREATER_THAN
  | BIT_XOR
  | BIT_OR
  | QUESTION
  | COMMENT_OPEN
  | COMMENT_CLOSE
  ;

identifier : #"_*[a-zA-Z][a-zA-Z0-9_]*" ;

enumeration_constant : identifier ;

integer_constant
  : #"0[xX][a-fA-F0-9]+(ul|uL|Ul|UL|l|L|ll|LL|u|U)?" (* hex int *)
  | #"0[0-9]*(ul|uL|Ul|UL|l|L|ll|LL|u|U)*?" (* zero or octal int *)
  | #"[1-9][0-9]*(ul|uL|Ul|UL|l|L|ll|LL|u|U)*?" (* decimal int *)
  ;

floating_constant
  : #"[0-9]+[Ee][+-]?[0-9]+(f|F|l|L)?" (* float or double *)
  | #"[0-9]*\.[0-9]+([Ee][+-]?[0-9]+)?(f|F|l|L)?" (* float or double *)
  ;

character_constant
  : #"L?'\\[0-7]([0-7][0-7]?)?'" (* octal char *)
  | #"L?'\\\\x[0-9a-fA-F]+(\\\\x[0-9a-fA-F]+)*'" (* hex char. Yes, having multiple chars in one char is valid, but results in a warning. In that case, only the last char is taken *)
  | #"L?'\\['\\\"?abfnFtv]'" (* escape char *)
  | #"L?'[^\\']'" (* Any other char *)
  ;

constant
  : integer_constant
  | floating_constant
  | enumeration_constant
  | character_constant
  ;

string_literal
  : #'L?\"(\\.|[^\\"])*\"'
  ;

COMMENT
  : COMMENT_OPEN #".*" COMMENT_CLOSE
  ;

header_name
  : #"<[^\n>]+>"
  | #'"[^\\"\n]+"'
  ;

primary_expression
  : identifier
  | constant
  | string_literal
  | BRACE_OPEN expression BRACE_CLOSE
  ;

postfix_expression
  : primary_expression
  | postfix_expression SQUARE_OPEN expression SQUARE_CLOSE
  | postfix_expression BRACE_OPEN argument_expression_list? BRACE_CLOSE
  | postfix_expression DOT identifier
  | postfix_expression PTR_OP identifier
  | postfix_expression INC_OP
  | postfix_expression DEC_OP
  | BRACE_OPEN type_name BRACE_CLOSE BLOCK_OPEN initializer_list COMMA? BLOCK_CLOSE
  ;

argument_expression_list
  : (assignment_expression COMMA)* assignment_expression
  ;

unary_expression
  : postfix_expression
  | INC_OP unary_expression
  | DEC_OP unary_expression
  | unary_operator cast_expression
  | SIZEOF (unary_expression | BRACE_OPEN type_name BRACE_CLOSE)
  ;

unary_operator
  : BIT_AND
  | STAR
  | PLUS
  | MINUS
  | TILDE
  | UNARY_NOT
  ;

cast_expression
  : unary_expression
  | BRACE_OPEN type_name BRACE_CLOSE cast_expression
  ;

multiplicative_expression
  : cast_expression
  | multiplicative_expression STAR cast_expression
  | multiplicative_expression SLASH cast_expression
  | multiplicative_expression PERCENT cast_expression
  ;

additive_expression
  : multiplicative_expression
  | additive_expression PLUS multiplicative_expression
  | additive_expression MINUS multiplicative_expression
  ;

shift_expression
  : additive_expression
  | shift_expression RIGHT_OP additive_expression
  | shift_expression LEFT_OP additive_expression
  ;

relational_expression
  : shift_expression
  | relational_expression LESS_THAN shift_expression
  | relational_expression GREATER_THAN shift_expression
  | relational_expression LE_OP shift_expression
  | relational_expression GE_OP shift_expression
  ;

equality_expression
  : relational_expression
  | equality_expression EQ_OP relational_expression
  | equality_expression NE_OP relational_expression
  ;

AND_expression
  : equality_expression
  | AND_expression BIT_AND equality_expression
  ;

exclusive_OR_expression
  : AND_expression
  | exclusive_OR_expression BIT_XOR AND_expression
  ;

inclusive_OR_expression
  : exclusive_OR_expression
  | inclusive_OR_expression BIT_OR exclusive_OR_expression
  ;

logical_AND_expression
  : inclusive_OR_expression
  | logical_AND_expression AND_OP inclusive_OR_expression
  ;

logical_OR_expression
  : logical_AND_expression
  | logical_OR_expression OR_OP logical_AND_expression
  ;

conditional_expression
  : logical_OR_expression
  | logical_OR_expression QUESTION expression COLON conditional_expression
  ;

assignment_expression
  : conditional_expression
  | unary_expression assignment_operator assignment_expression
  ;

assignment_operator
  : ASSIGN
  | MUL_ASSIGN
  | DIV_ASSIGN
  | MOD_ASSIGN
  | ADD_ASSIGN
  | SUB_ASSIGN
  | LEFT_ASSIGN
  | RIGHT_ASSIGN
  | AND_ASSIGN
  | XOR_ASSIGN
  | OR_ASSIGN
  ;

expression
  : assignment_expression
  | expression COMMA assignment_expression
  ;

constant_expression
  : conditional_expression
  ;

declaration
  : declaration_specifiers init_declarator_list? SEMICOLON
  ;

declaration_specifiers
  : ( storage_class_specifier | type_specifier | type_qualifier | function_specifier )+
  ;

init_declarator_list
  : init_declarator (COMMA init_declarator)*
  ;

init_declarator
  : declarator (ASSIGN initializer)?
  ;

storage_class_specifier
  : TYPEDEF
  | EXTERN
  | STATIC
  | AUTO
  | REGISTER
  ;

type_specifier
  : VOID
  | CHAR
  | SHORT
  | INT
  | LONG
  | FLOAT
  | DOUBLE
  | SIGNED
  | UNSIGNED
  | BOOL
  | COMPLEX
  | struct_or_union_specifier
  | enum_specifier
  | typedef_name
  ;

struct_or_union_specifier
  : struct_or_union identifier? BLOCK_OPEN struct_declaration_list BLOCK_CLOSE
  | struct_or_union identifier
  ;

struct_or_union
  : STRUCT | UNION
  ;

struct_declaration_list
  : struct_declaration +
  ;

struct_declaration
  : specifier_qualifier_list struct_declarator_list SEMICOLON
  ;

specifier_qualifier_list
  : (type_specifier | type_qualifier)+
  ;

struct_declarator_list
  : struct_declarator (COMMA struct_declarator)*
  ;

struct_declarator
  : declarator
  | declarator? COLON constant_expression
  ;

enum_specifier
  : ENUM identifier? BLOCK_OPEN enumerator_list COMMA? BLOCK_CLOSE
  | ENUM identifier
  ;

enumerator_list
  : enumerator (COMMA enumerator)*
  ;

enumerator
  : enumeration_constant (ASSIGN constant_expression)?
  ;

type_qualifier
  : CONST
  | RESTRICT
  | VOLATILE
  ;

function_specifier
  : INLINE
  ;

declarator
  : pointer? direct_declarator
  ;

direct_declarator
  : identifier
  | BRACE_OPEN declarator BRACE_CLOSE
  | direct_declarator SQUARE_OPEN type_qualifier_list? assignment_expression? SQUARE_CLOSE
  | direct_declarator SQUARE_OPEN STATIC type_qualifier_list? assignment_expression SQUARE_CLOSE
  | direct_declarator SQUARE_OPEN type_qualifier_list STATIC assignment_expression SQUARE_CLOSE
  | direct_declarator SQUARE_OPEN type_qualifier_list? STAR SQUARE_CLOSE
  | direct_declarator BRACE_OPEN parameter_type_list BRACE_CLOSE
  | direct_declarator BRACE_OPEN identifier_list? BRACE_CLOSE
  ;

pointer
  : STAR type_qualifier_list? pointer?
  ;

type_qualifier_list
  : type_qualifier +
  ;

parameter_type_list
  : parameter_list (COMMA ELLIPSIS)?
  ;

parameter_list
  : parameter_declaration (COMMA parameter_declaration)*
  ;

parameter_declaration
  : declaration_specifiers (declarator | abstract_declarator?)
  ;

identifier_list
  : identifier (COMMA identifier)*
  ;

type_name
  : specifier_qualifier_list abstract_declarator?
  ;

abstract_declarator
  : pointer
  | pointer? direct_abstract_declarator
  ;

direct_abstract_declarator
  : BRACE_OPEN abstract_declarator BRACE_CLOSE
  | direct_abstract_declarator? SQUARE_OPEN type_qualifier_list? assignment_expression? SQUARE_CLOSE
  | direct_abstract_declarator? SQUARE_OPEN STATIC type_qualifier_list? assignment_expression SQUARE_CLOSE
  | direct_abstract_declarator? SQUARE_OPEN type_qualifier_list STATIC assignment_expression SQUARE_CLOSE
  | direct_abstract_declarator? SQUARE_OPEN STAR SQUARE_CLOSE
  | direct_abstract_declarator? BRACE_OPEN parameter_type_list? BRACE_CLOSE
  ;

typedef_name
  : identifier
  ;

initializer
  : assignment_expression
  | BRACE_OPEN initializer_list BRACE_CLOSE
  | BLOCK_OPEN initializer_list COMMA BLOCK_CLOSE
  ;

initializer_list
  : designation? initializer (COMMA designation? initializer)*
  ;

designation
  : designator_list ASSIGN
  ;

designator_list
  : designator +
  ;

designator
  : SQUARE_OPEN constant_expression SQUARE_CLOSE
  | DOT identifier
  ;

statement
  : labeled_statement
  | compound_statement
  | expression_statement
  | selection_statement
  | iteration_statement
  | jump_statement
  ;

labeled_statement
  : identifier COLON statement
  | CASE constant_expression COLON statement
  | DEFAULT COLON statement
  ;

compound_statement
  : BLOCK_OPEN block_item_list? BLOCK_CLOSE
  ;

block_item_list
  : block_item +
  ;

block_item
  : declaration
  | statement
  ;

expression_statement
  : expression? SEMICOLON
  ;

selection_statement
  : IF BRACE_OPEN expression BRACE_CLOSE statement
  | IF BRACE_OPEN expression BRACE_CLOSE statement ELSE statement
  | SWITCH BRACE_OPEN expression BRACE_CLOSE statement (* [sic] technically does not require any cases, apparently... *)
  ;

iteration_statement
  : WHILE BRACE_OPEN expression BRACE_CLOSE statement
  | DO statement WHILE BRACE_OPEN expression BRACE_CLOSE SEMICOLON
  | FOR BRACE_OPEN expression? SEMICOLON expression? SEMICOLON expression? BRACE_CLOSE statement
  | FOR BRACE_OPEN declaration expression? SEMICOLON expression? BRACE_CLOSE statement
  ;

jump_statement
  : GOTO identifier SEMICOLON
  | CONTINUE SEMICOLON
  | BREAK SEMICOLON
  | RETURN expression? SEMICOLON
  ;

translation_unit
  : external_declaration +
  ;

external_declaration
  : function_definition
  | declaration
  ;

function_definition
  : declaration_specifiers declarator declaration_list? compound_statement
  ;

declaration_list
  : declaration +
  ;

new_line
  : '\n'
  ;
