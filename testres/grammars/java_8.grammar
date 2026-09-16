
input : {inputElement} ;

InputElement
: WhiteSpace
| Comment
| Token
;

Token
: Identifier
| Keyword
| Literal
| Separator
| Operator
;

WhiteSpace
: " "
| "\t"
| "\f"
;

Comment
: TraditionalComment
| EndOfLineComment
;

TraditionalComment : "/*" CommentTail ;

CommentTail
: "*" CommentTailStar
| NotStar CommentTail
;

CommentTailStar
: "/"
| "*" CommentTailStar
| NotStarNotSlash CommentTail
;

NotStar
:



