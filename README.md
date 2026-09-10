# Parsewisp 0.9.6

A tool to generate and use parsers at runtime.

This project started as a conversion of the great Clojure-library [Instaparse](https://github.com/engelberg/instaparse)
but has grown beyond it.

## Features

- [x] Handles any kind of context-free grammar (left-recursive, right-recursive, ambiguous). For caveats see below.
- [x] Works for EBNF and ABNF.
- [x] Supports PEG-like syntax for lookahead and negative lookahead.
- [x] Detailed reporting of parse errors.
- [x] Can produce lazy sequences of all parses. This is useful for ambiguous grammars.
- [x] Grammars can be built using combinators.
- [x] Heavily optimized, with some tradeoffs where good style demanded it.
- [x] Many options for customizing the parser-construction and parsing-process.

Missing features and problems:

- [ ] ABNFand PEG line comments are not implemented yet.

## First parser

To create a parser, you'll typically want to use the `Parsewisp.parser` static method. It takes a string as its first argument.

```java
import de.kleinert.parsewisp.Parsewisp;
import parser_options.de.kleinert.parsewisp.ParserCreationOptions;

class MyFirstParser {
  public static void main(String[] args) {
    // Using multiline strings can be helpful. This grammar defines  arithmetic expressions
    var grammar = """
            sum          = product ('+'|'-') sum   | product
            product      = power ('*'|'/') product | power
            power        = paren-or-val '^' power  | paren-or-val
            paren-or-val = '(' sum ')'             | number
            number       = ('+'|'-')? ('0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9')+
            """;
    var p = Parsewisp.parser(grammar);

    // Start the parser by using its parse(String) method.
    // The parser will output the parse tree.
    System.out.println(p.parse("1")); // [:sum, [:product, [:power, [:paren-or-val, [:number, "1"]]]]]

    // The grammar can not yet handle whitespace.
    // The following returns an error:
    System.out.println(p.parse("1 + 2"));

    // You can manually add support for whitespace to the grammar or use the optional argument for the Parsewisp.parser method:
    p = Parsewisp.parser(grammar, ParserCreationOptions.newWithStandardWhitespace());
    // Now, the parse works. Go on, try it. :)
    System.out.println(p.parse("1 + 2"));
  }
}
```

## Usage

1. Download the `.jar` file or compile it yourself.
2. Add the library to your classpath. I recommend using an IDE for this.

Older versions of Parsewisp required Java 21.

## User-side Priorities

These are priorities that directly impact the usage.

- Parse tree format follows OOP style: Instaparse uses raw objects and supports two different formats for parse trees.
  Parsewisp has only one type for parse trees which uses a wrapping type `Node`.
- Parse trees are smaller. If the grammar is ambiguous, Parsewisp can hold more output trees than Instaparse, at least
  on my machine.
- Smaller library `.jar` size. I set a maximum size goal of 220 kB.

## Grammar elements

### Basic options

| Category                   | Notations                             | Example                 | Note                                            |
|----------------------------|---------------------------------------|-------------------------|-------------------------------------------------|
| Rule                       | `:` `:=` `::=` `=`                    | `S = A`                 |                                                 |
| End of rule                | `;` `.` (optional)                    | `S = A;`                |                                                 |
| Alternation                | <code>&#124;</code> and `/`           | <code>A &#124; B</code> | Also known as "Choice"; ABNF uses `/` for this. |
| Concatenation              | whitespace or `,`                     | `A B`                   |                                                 |
| Grouping                   | `()`                                  | `(A  B)+ C`             |                                                 |
| Optional                   | `[]`                                  | `[A]`                   |                                                 |
| Optional (alt)             | `?`                                   | `A?`                    |                                                 |
| One or more                | `+`                                   | `A+`                    |                                                 |
| Zero or more               | `{}`                                  | `{A}`                   |                                                 |
| Zero or more (alt)         | `*`                                   | `A*`                    |                                                 |
| String terminal            | `""`                                  | `"a"`                   |                                                 |
| String terminal (alt)      | `''`                                  | `'a'`                   | Not in ABNF                                     |
| Regex terminal             | `#""` `#''`                           | `#"[0-9]"` `#'[0-9]'`   |                                                 |
| Epsilon                    | `Epsilon epsilon EPSILON eps ε "" ''` | `S = epsilon`           |                                                 |
| Comment                    | `(* *)`                               | `(* Comment *)`         |                                                 |
| End of file / end of input | `EOF`                                 | `EOF`                   |                                                 |

### Extended options

| Category                           | Notations                                         | Example          | Note            |
|------------------------------------|---------------------------------------------------|------------------|-----------------|
| Variable repetition (zero or more) | `*`                                               | `* A`            | ABNF, see below |
| Variable repetition (n or more)    | `n*`                                              | `5* A`           | ABNF            |
| Variable repetition (zero to m)    | `*m`                                              | `*5 A`           | ABNF            |
| Variable repetition (n to m)       | `n*m`                                             | `5*19 A`         | ABNF            |
| Variable repetition (exactly n)    | `n`                                               | `5 A`            | ABNF            |
| Value range                        | `%xXXXX[-XXXX]`, `%bBBBB[-BBBB]`, `%dDDDD[-DDDD]` | `%x41-5a`        | ABNF            |
| ABNF core rules                    | See below.                                        |                  |                 |
| Explicit string case sensitivity   | `%i"..."` `%s"..."` (and `%i'...'` `%s'...'`)     | `%i"A"`, `%s"A"` | ABNF            |
| Exclusion / Exception              | `-`                                               | `A - B`          | EBNF            |

- For available value range formats, see [the specification](https://datatracker.ietf.org/doc/html/rfc5234#autoid-11).
- For available ABNF core rules, see [the specification](https://datatracker.ietf.org/doc/html/rfc5234#autoid-25).


## Design goals

- Small `.jar` file
- High performance
- Keep output memory small
- Use only the Java standard libraries
- Determinism
