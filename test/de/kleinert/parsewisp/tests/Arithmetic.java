package de.kleinert.parsewisp.tests;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import de.kleinert.parsewisp.parser_options.RulesAvailable;
import de.kleinert.parsewisp.result.ParseResult;
import org.jetbrains.annotations.NotNull;

/**
 * Examples for usage of the library.
 */
public class Arithmetic {
    private Arithmetic(){}
    /**
     * We use a simple EBNF grammar for arithmetic expressions on the test input. We do not use any special features yet.
     *
     * @return  output for tutorial state
     */
    static @NotNull ParseResult state1() {
        var g = """
                Expression = Term , { ( "+" | "-" ) , Term } ;
                Term       = Factor , { ( "*" | "/" ) , Factor } ;
                Factor     = Number | "(", Expression, ")" ;
                Number     = [ "+" | "-" ] Digit , { Digit } ;
                Digit      = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
                """;
        var p = Parsewisp.parser(g);

        // Since Parsewisp is scannerless, we can not include spaces for now.
        return p.parse("(8-9)*-20/18+1");
    }
    /**
     * We use the alternative string terminals '...' instead of the standard "...". This is still 100% standard EBNF.
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state2() {
        var g = """
                Expression = Term , { ( '+' | '-' ) , Term } ;
                Term       = Factor , { ( '*' | '/' ) , Factor } ;
                Factor     = Number | '(', Expression, ')' ;
                Number     = [ '+' | '-' ] Digit , { Digit } ;
                Digit      = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' ;
                """;
        var p = Parsewisp.parser(g);

        // Since Parsewisp is scannerless, we can not include spaces for now.
        return p.parse("(8-9)*-20/18+1");
    }
    /**
     * We remove the commas (dividing rules in concatenation) and semicolons (production terminators).
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state3() {
        var g = """
                Expression = Term { ( '+' | '-' ) Term }
                Term       = Factor { ( '*' | '/' ) Factor }
                Factor     = Number | '(' Expression ')'
                Number     = [ '+' | '-' ] Digit { Digit }
                Digit      = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
                """;
        var p = Parsewisp.parser(g);

        // Since Parsewisp is scannerless, we can not include spaces for now.
        return p.parse("(8-9)*-20/18+1");
    }
    /**
     * Annoyed by needing to write the expression without spaces? I am too. Let's fix that.
     * <p>
     * Add explicit options to the parser's construction like so:
     * <pre>
     * {@code
     *         var options = ParserCreationOptions
     *                 .getDefault()
     *                 .withWhitespaceParser(Parsewisp.parser("whitespace = ' ' | '\\t' | '\\n'"));
     * }
     * </pre>
     * Since this pattern came up so often, a shorter alternative is provided:
     * <pre>
     * {@code
     *         var options = ParserCreationOptions.newWithStandardWhitespace();
     * }
     * </pre>
     * This modifies the grammar when starting. But the parse tree should remain the same.
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state4() {
        var g = """
                Expression = Term { ( '+' | '-' ) Term }
                Term       = Factor { ( '*' | '/' ) Factor }
                Factor     = Number | '(' Expression ')'
                Number     = [ '+' | '-' ] Digit { Digit }
                Digit      = '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
                """;
        var options = ParserCreationOptions.newWithStandardWhitespace();
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1");
    }
    /**
     * You may have noticed that the definition of "Digit" is pretty long. We can replace it with a regular expression (regex).
     * The format for regexes is {@code #"..."}. The alternative syntax {@code #'...'} is also available.
     * <p>
     * We need to step out of the standard EBNF territory. We can compress {@code '0'|...|'9'} into the less annoying regex {@code #'[0-9]'}.
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state5() {
        var g = """
                Expression = Term { ( '+' | '-' ) Term }
                Term       = Factor { ( '*' | '/' ) Factor }
                Factor     = Number | '(' Expression ')'
                Number     = [ '+' | '-' ] Digit { Digit }
                Digit      = #'[0-9]'
                """;
        var options = ParserCreationOptions.newWithStandardWhitespace();
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1");
    }
    /**
     * We can make the grammar a bit nicer to look at by using '?' for optionals and '*' for repetitions.
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state6() {
        var g = """
                Expression = Term (( '+' | '-' ) Term)*
                Term       = Factor (( '*' | '/' ) Factor)*
                Factor     = Number | '(' Expression ')'
                Number     = ('+' | '-')? Digit Digit*
                Digit      = #'[0-9]'
                """;
        var options = ParserCreationOptions.newWithStandardWhitespace();
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1");
    }
    /**
     * In the "Factor" production, do the '(' and ')' around "Expression" provide any value in the parse tree? Not really? Let's hide them.
     * To hide a rule in the output, wrap it in '<' and '>'.
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state7() {
        var g = """
                Expression = Term (( '+' | '-' ) Term)*
                Term       = Factor (( '*' | '/' ) Factor)*
                Factor     = Number | <'('> Expression <')'>
                Number     = ('+' | '-')? Digit Digit*
                Digit      = #'[0-9]'
                """;
        var options = ParserCreationOptions.newWithStandardWhitespace();
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1");
    }
    /**
     * Parsewisp also supports '+' to shorten our rule "Digit Digit*".
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state8() {
        var g = """
                Expression = Term (('+' | '-') Term)*
                Term       = Factor ( ( '*' | '/' ) Factor )*
                Factor     = Number | <'('> Expression <')'>
                Number     = ('+' | '-')? Digit+
                Digit      = #'[0-9]'
                """;
        var options = ParserCreationOptions.newWithStandardWhitespace();
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1");
    }
    /**
     * We can compress the output futher by flattening "Digit" into "Number". To do this, wrap Digit between "<" and ">", but this time on the left.
     *
     * @return output for tutorial state
     */
    static @NotNull ParseResult state9() {
        var g = """
                Expression = Term (('+' | '-') Term)*
                Term       = Factor ( ( '*' | '/' ) Factor )*
                Factor     = Number | <'('> Expression <')'>
                Number     = ('+' | '-')? Digit+
                <Digit>    = #'[0-9]'
                """;
        var options = ParserCreationOptions.newWithStandardWhitespace();
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1");
    }

    /**
     * If we used ABNF, we could also use value ranges instead of a regular expression for Digit. But we are using EBNF. Still, we can allow that rule explicitly.
     * To do this, we need to modify the creation-options again:
     */
    static ParseResult state10() {
        var g = """
                Expression = Term (('+' | '-') Term)*
                Term       = Factor ( ( '*' | '/' ) Factor )*
                Factor     = Number | <'('> Expression <')'>
                Number     = ('+' | '-')? Digit+
                <Digit>    = %d48-57 (* same as %x30-39 *)
                """;
        var options = ParserCreationOptions
                .newWithStandardWhitespace()
                .addAvailableRule(RulesAvailable.VALUE_RANGE);
        var p = Parsewisp.parser(g, options);

        return p.parse("(8 - 9) * -20 / 18 + 1", ParsingOptions.getDefault());
    }

    /**
     * Example main for tutorial.
     * @param args Ignored.
     */
    public static void main(String[] args) {
        System.out.println("1:  " + state1());
        System.out.println("2:  " + state2());
        System.out.println("3:  " + state3());
        System.out.println("4:  " + state4());
        System.out.println("5:  " + state5());
        System.out.println("6:  " + state6());
        System.out.println("7:  " + state7());
        System.out.println("8:  " + state8());
        System.out.println("9:  " + state9());
        System.out.println("10: " + state10());
    }
}
