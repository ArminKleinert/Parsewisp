S = expression + ;

identifier = #"[^\s\[\]{}('\"`,;)0-9][^\s\[\]{}('\"`,;)]+'?"

expression
  = let_expr
  | define
  | defmacro
  (* | lambda_def *)
  | literal
  (* | symbol *)
  | identifier
  ;

args
  = identifier* varargs_end?
  ;

varargs_end
  = <'&'> identifier
  ;

body
  = expression*
  ;

defmacro
  = <'('> 'defmacro' identifier args body <')'>
  ;

define
  = <'('> 'define' <'('> identifier args <')'> body <')'>
  ;

let_expr
  = <'('> 'let*' <'('> binding+ <')'> body <')'>

binding
  = <'('> identifier expression <')'>
  ;

literal
  = list_literal
  | vector_literal
  | set_literal
  | map_literal
  | string_literal
  | number_literal
  | float_literal
  | ratio_literal
  | char_literal
  | bool_literal
  | nothing_literal
  ;

list_literal
  = <'('> expression* <')'>
  ;

vector_literal
  = <'['> expression* <']'>
  ;

set_literal
  = <'#{'> expression* <'}'>
  ;

map_literal
  = <'{'> (expression expression)* <'}'>
  ;

string_literal
  = #'\"(\\.|[^\\"])*\"'
  ;

number_literal
  = #'-?0b[01]+'
  | #'-?0x[0-9a-fA-F]+'
  | #'-?[0-9]+'
  ;

float_literal
  = #'-?\d+\.\d+'
  ;

ratio_literal
  = #'-?\d+\/\d+'
  ;

bool_literal
  = "#f"
  | "#t"
  ;

char_literal
  = "\\" char_literal_inner

<char_literal_inner>
  = #'u\d{4}'
  | '*'
  | 'newline'
  | 'space'
  | 'tab'
  | 'backspace'
  | 'return'
  | 'formfeed'
  | #'[A-Za-z\d+-\/!?$%&()|\[\]{}]'
  ;

nothing_literal
  = 'Nothing'
  | 'nil'
  ;

























