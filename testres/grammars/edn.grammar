Edn = <ws?> expression* <ws?>

expression = ws? expr-part ws?
<expr-part> = List | Vector | Set | Map | Number | Char | Symbol | Keyword | Literal | String | TypeDispatch | Inst | Uuid | SymbolicValue

List = <"("> expression* <")">
Vector = <"["> expression* <"]">
Set = <"#{"> expression* <"}">
Map-entry = expression expression
Map = <"{"> Map-entry* <"}">
Number = Int | Float

Symbol = SymbolPart (<"/"> SymbolPart)?
<SymbolPart> = #"[a-zA-Z.*+!\-_?$%=<>][a-zA-Z.*+!\-_?$%=<>:#]*"
Keyword = <":"> Symbol
Float = #"[+\-]?[0-9]*\.[0-9]+M?"
      | #"[+\-]?[0-9]*\.?[0-9]+([eE][+\-][0-9]+)M?"
Int = #"[+\-]?[0-9]+N?"
SymbolicValue = "##NaN" | "##Inf" | "##-Inf"
Literal = "nil" | "true" | "false"
String = <"\""> #'[^\"]*' <"\""> ;

Char = <"\\"> (CharPredefNames | CharUTF | CharOctal | CharOther)
CharPredefNames = "newline" | "space" | "tab" | "backspace" | "formfeed" | "return"
CharUTF = <"u"> #"[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]"
CharOctal = <"o"> #"[0-7][0-7]?[0-7]?"
CharOther = !(CharUTF | CharPredefNames) #"[^\n \t]"

SymbolWithNS = SymbolPart <"/"> SymbolPart
TypeDispatch = <"#"> !"_" SymbolWithNS ws? expression

Inst = <"#inst"> ws? String
Uuid = <"#uuid"> ws? String

<ws> = <#"[\n\t ]+"> | <Discard> | <LineComment>
Discard = "#_" ws? expression
LineComment = ws? ";" #"[^\n]*" ("\n" | "\r" | EOF) ws?