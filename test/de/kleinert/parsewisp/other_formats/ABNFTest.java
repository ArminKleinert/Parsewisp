package de.kleinert.parsewisp.other_formats;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ABNFTest {
    // Basic literal matching
    @Test
    void testLiteral() {
        var parser = ABNF.parser("start = \"hello\"");

        var result = parser.parse("hello");
        Assertions.assertTrue(result.isSuccess());
    }

    @Test
    void testLiteralFailure() {
        var parser = ABNF.parser("start = \"hello\"");

        var result = parser.parse("world");
        Assertions.assertTrue(result.isFailure());
    }

    @Test
    void testLiteralIsCaseSensitiveExplicit() {
        var parser = ABNF.parser("start = %s\"hello\"");

        Assertions.assertTrue(parser.parse("hello").isSuccess());
        Assertions.assertTrue(parser.parse("Hello").isFailure());
    }

    @Test
    void testLiteralIsNotCaseSensitive() {
        var parser = ABNF.parser("start = \"hello\"");

        var result = parser.parse("Hello");
        Assertions.assertFalse(result.isFailure());
    }

    @Test
    void testLiteralIsCaseInsensitive() {
        var parser = ABNF.parser("start = \"hello\"");

        var result = parser.parse("Hello");
        Assertions.assertTrue(result.isSuccess());
    }

    @Test
    void testLiteralIsCaseInsensitiveExplicit() {
        var parser = ABNF.parser("start = %i\"hello\"");

        var result = parser.parse("Hello");
        Assertions.assertTrue(result.isSuccess());
    }

    @Test
    void testEmptyInputFailsForNonEmptyGrammar() {
        var parser = ABNF.parser("start = \"hello\"");

        var result = parser.parse("");
        Assertions.assertTrue(result.isFailure());
    }


    // Character classes
    @Test
    void testCharacterClass() {
        var parser = ABNF.parser("start = %d48-57");

        Assertions.assertTrue(parser.parse("0").isSuccess());
        Assertions.assertTrue(parser.parse("5").isSuccess());
        Assertions.assertTrue(parser.parse("9").isSuccess());
    }

    @Test
    void testCharacterClassRejectsOutsideRange() {
        var parser = ABNF.parser("start = %d48-57");

        Assertions.assertTrue(parser.parse("a").isFailure());
        Assertions.assertTrue(parser.parse("10").isFailure());
    }

    @Test
    void testCaseInsensitiveCharacterRange() {
        var parser = ABNF.parser("start = %d65-90");

        Assertions.assertTrue(parser.parse("A").isSuccess());
        Assertions.assertTrue(parser.parse("Z").isSuccess());
        Assertions.assertTrue(parser.parse("a").isFailure());
    }


    // Concatenation
    @Test
    void testConcatenation() {
        var parser = ABNF.parser("start = \"hello\" \"world\"");

        Assertions.assertTrue(parser.parse("helloworld").isSuccess());
    }

    @Test
    void testConcatenationRequiresBothParts() {
        var parser = ABNF.parser("start = \"hello\" \"world\"");

        Assertions.assertTrue(parser.parse("hello").isFailure());
        Assertions.assertTrue(parser.parse("world").isFailure());
    }

    @Test
    void testConcatenationOrderMatters() {
        var parser = ABNF.parser("start = \"hello\" \"world\"");

        Assertions.assertTrue(parser.parse("worldhello").isFailure());
    }


    // Alternatives
    @Test
    void testAlternativeFirstBranch() {
        var parser = ABNF.parser("start = \"cat\" / \"dog\"");

        Assertions.assertTrue(parser.parse("cat").isSuccess());
    }

    @Test
    void testAlternativeSecondBranch() {
        var parser = ABNF.parser("start = \"cat\" / \"dog\"");

        Assertions.assertTrue(parser.parse("dog").isSuccess());
    }

    @Test
    void testAlternativeRejectsOtherValues() {
        var parser = ABNF.parser("start = \"cat\" / \"dog\"");

        Assertions.assertTrue(parser.parse("bird").isFailure());
    }

    @Test
    void testMultipleAlternatives() {
        var parser = ABNF.parser("start = \"a\" / \"b\" / \"c\"");

        Assertions.assertTrue(parser.parse("a").isSuccess());
        Assertions.assertTrue(parser.parse("b").isSuccess());
        Assertions.assertTrue(parser.parse("c").isSuccess());
        Assertions.assertTrue(parser.parse("d").isFailure());
    }


    // Optional elements
    @Test
    void testOptionalPresent() {
        var parser = ABNF.parser("start = \"hello\" [\" world\"]");

        Assertions.assertTrue(parser.parse("hello").isSuccess());
        Assertions.assertTrue(parser.parse("hello world").isSuccess());
    }

    @Test
    void testOptionalDoesNotRequireElement() {
        var parser = ABNF.parser("start = [\"hello\"]");

        Assertions.assertTrue(parser.parse("").isSuccess());
        Assertions.assertTrue(parser.parse("hello").isSuccess());
    }

    @Test
    void testOptionalStillRejectsInvalidInput() {
        var parser = ABNF.parser("start = \"hello\" [\" world\"]");

        Assertions.assertTrue(parser.parse("hello something").isFailure());
    }


    // Repetition
    @Test
    void testZeroOrMoreRepetition() {
        var parser = ABNF.parser("start = *\"a\"");

        Assertions.assertTrue(parser.parse("").isSuccess());
        Assertions.assertTrue(parser.parse("a").isSuccess());
        Assertions.assertTrue(parser.parse("aaaa").isSuccess());
    }

    @Test
    void testZeroOrMoreRejectsOtherCharacters() {
        var parser = ABNF.parser("start = *\"a\"");

        Assertions.assertTrue(parser.parse("b").isFailure());
        Assertions.assertTrue(parser.parse("aaab").isFailure());
    }

    @Test
    void testOneOrMoreRepetition() {
        var parser = ABNF.parser("start = 1*\"a\"");

        Assertions.assertTrue(parser.parse("a").isSuccess());
        Assertions.assertTrue(parser.parse("aaaa").isSuccess());
        Assertions.assertTrue(parser.parse("").isFailure());
    }

    @Test
    void testExactRepetition() {
        var parser = ABNF.parser("start = 3\"a\"");

        Assertions.assertTrue(parser.parse("aaa").isSuccess());
        Assertions.assertTrue(parser.parse("aa").isFailure());
        Assertions.assertTrue(parser.parse("aaaa").isFailure());
    }

    @Test
    void testBoundedRepetition() {
        var parser = ABNF.parser("start = 2*4\"a\"");

        Assertions.assertTrue(parser.parse("aa").isSuccess());
        Assertions.assertTrue(parser.parse("aaa").isSuccess());
        Assertions.assertTrue(parser.parse("aaaa").isSuccess());
        Assertions.assertTrue(parser.parse("a").isFailure());
        Assertions.assertTrue(parser.parse("aaaaa").isFailure());
    }


    // Groups
    @Test
    void testGroup() {
        var parser = ABNF.parser("start = (\"a\" / \"b\") \"c\"");

        Assertions.assertTrue(parser.parse("ac").isSuccess());
        Assertions.assertTrue(parser.parse("bc").isSuccess());
        Assertions.assertTrue(parser.parse("abc").isFailure());
    }

    @Test
    void testGroupedRepetition() {
        var parser = ABNF.parser("start = *(\"a\" / \"b\")");

        Assertions.assertTrue(parser.parse("").isSuccess());
        Assertions.assertTrue(parser.parse("a").isSuccess());
        Assertions.assertTrue(parser.parse("b").isSuccess());
        Assertions.assertTrue(parser.parse("ababab").isSuccess());
    }

    @Test
    void testNestedGroups() {
        var parser = ABNF.parser("start = ((\"a\" \"b\") / \"c\")");

        Assertions.assertTrue(parser.parse("ab").isSuccess());
        Assertions.assertTrue(parser.parse("c").isSuccess());
        Assertions.assertTrue(parser.parse("a").isFailure());
    }


    // Rule references
    @Test
    void testRuleReference() {
        var parser = ABNF.parser("""
                start = greeting
                greeting = "hello"
                """);

        Assertions.assertTrue(parser.parse("hello").isSuccess());
        Assertions.assertTrue(parser.parse("goodbye").isFailure());
    }

    @Test
    void testMultipleRuleReferences() {
        var parser = ABNF.parser("""
                start = greeting " " name
                greeting = "hello" / "hi"
                name = "alice" / "bob"
                """);

        Assertions.assertTrue(parser.parse("hello alice").isSuccess());
        Assertions.assertTrue(parser.parse("hello bob").isSuccess());
        Assertions.assertTrue(parser.parse("hi alice").isSuccess());
        Assertions.assertTrue(parser.parse("hi bob").isSuccess());
    }

    @Test
    void testReferencedRuleFailure() {
        var parser = ABNF.parser("""
                start = greeting " " name
                greeting = "hello"
                name = "alice"
                """);

        Assertions.assertTrue(parser.parse("goodbye alice").isFailure());
        Assertions.assertTrue(parser.parse("hello bob").isFailure());
    }


    // Practical grammars
    @Test
    void testIdentifier() {
        var parser = ABNF.parser("""
                start = letter *word-char
                letter = %d65-90 / %d97-122
                word-char = letter / digit / "_"
                digit = %d48-57
                """);

        Assertions.assertTrue(parser.parse("hello").isSuccess());
        Assertions.assertTrue(parser.parse("hello123").isSuccess());
        Assertions.assertTrue(parser.parse("hello_world").isSuccess());
        Assertions.assertTrue(parser.parse("123hello").isFailure());
        Assertions.assertTrue(parser.parse("").isFailure());
    }

    @Test
    void testInteger() {
        var parser = ABNF.parser("""
                start = 1*digit
                digit = %d48-57
                """);

        Assertions.assertTrue(parser.parse("0").isSuccess());
        Assertions.assertTrue(parser.parse("123").isSuccess());
        Assertions.assertTrue(parser.parse("999999").isSuccess());
        Assertions.assertTrue(parser.parse("").isFailure());
        Assertions.assertTrue(parser.parse("12a").isFailure());
    }

    @Test
    void testSimpleEmailLikeGrammar() {
        var parser = ABNF.parser("""
                start = local "@" domain
                local = 1*(letter / digit / ".")
                domain = 1*(letter / digit / ".")
                letter = %d65-90 / %d97-122
                digit = %d48-57
                """);

        Assertions.assertTrue(parser.parse("alice@example.com").isSuccess());
        Assertions.assertTrue(parser.parse("bob123@test42.org").isSuccess());
        Assertions.assertTrue(parser.parse("alice").isFailure());
        Assertions.assertTrue(parser.parse("@example.com").isFailure());
    }

    @Test
    void testHttpMethod() {
        var parser = ABNF.parser("""
                start = "GET" / "POST" / "PUT" / "DELETE"
                """);

        Assertions.assertTrue(parser.parse("GET").isSuccess());
        Assertions.assertTrue(parser.parse("POST").isSuccess());
        Assertions.assertTrue(parser.parse("PUT").isSuccess());
        Assertions.assertTrue(parser.parse("DELETE").isSuccess());
        Assertions.assertTrue(parser.parse("PATCH").isFailure());
    }

    @Test
    void testSimpleUrl() {
        var parser = ABNF.parser("""
                start = scheme "://" host
                scheme = "http" / "https"
                host = 1*(letter / digit / "." / "-")
                letter = %d65-90 / %d97-122
                digit = %d48-57
                """);

        Assertions.assertTrue(parser.parse("http://example.com").isSuccess());
        Assertions.assertTrue(parser.parse("https://example.com").isSuccess());
        Assertions.assertTrue(parser.parse("ftp://example.com").isFailure());
    }


    // Whitespace and exact consumption
    @Test
    void testWhitespaceIsSignificantWhenExplicit() {
        var parser = ABNF.parser("start = \"hello\" \" \" \"world\"");

        Assertions.assertTrue(parser.parse("hello world").isSuccess());
        Assertions.assertTrue(parser.parse("helloworld").isFailure());
    }

    @Test
    void testLeadingWhitespaceIsRejectedWhenNotSpecified() {
        var parser = ABNF.parser("start = \"hello\"");

        Assertions.assertTrue(parser.parse(" hello").isFailure());
    }

    @Test
    void testTrailingWhitespaceIsRejectedWhenNotSpecified() {
        var parser = ABNF.parser("start = \"hello\"");

        Assertions.assertTrue(parser.parse("hello ").isFailure());
    }

    @Test
    void testExplicitOptionalWhitespace() {
        var parser = ABNF.parser("start = \"hello\" *\" \" \"world\"");

        Assertions.assertTrue(parser.parse("helloworld").isSuccess());
        Assertions.assertTrue(parser.parse("hello world").isSuccess());
        Assertions.assertTrue(parser.parse("hello     world").isSuccess());
    }


    // Empty productions
    @Test
    void testEmptyAlternative() {
        var parser = ABNF.parser("start = \"hello\" / \"\"");

        Assertions.assertTrue(parser.parse("hello").isSuccess());
        Assertions.assertTrue(parser.parse("").isSuccess());
    }

    @Test
    void testOptionalIsEquivalentToEmptyAlternative() {
        var parser = ABNF.parser("start = [\"hello\"]");

        Assertions.assertTrue(parser.parse("").isSuccess());
        Assertions.assertTrue(parser.parse("hello").isSuccess());
    }


    // Numeric values
    @Test
    void testDecimalCharacterValue() {
        var parser = ABNF.parser("start = %d65");

        Assertions.assertTrue(parser.parse("A").isSuccess());
        Assertions.assertTrue(parser.parse("B").isFailure());
    }

    @Test
    void testHexCharacterValue() {
        var parser = ABNF.parser("start = %x41");

        Assertions.assertTrue(parser.parse("A").isSuccess());
        Assertions.assertTrue(parser.parse("B").isFailure());
    }

    @Test
    void testBinaryCharacterValue() {
        var parser = ABNF.parser("start = %b1000001");

        Assertions.assertTrue(parser.parse("A").isSuccess());
        Assertions.assertTrue(parser.parse("B").isFailure());
    }

    @Test
    void testMultipleNumericValues() {
        var parser = ABNF.parser("start = %d65.66.67");

        Assertions.assertTrue(parser.parse("ABC").isSuccess());
        Assertions.assertTrue(parser.parse("AB").isFailure());
    }


    // Comments / grammar formatting
    @Test
    void testGrammarComments() {
        var parser = ABNF.parser("""
        ; This is a comment
        start = "hello"
        """);

        Assertions.assertTrue(parser.parse("hello").isSuccess());
    }

    @Test
    void testWhitespaceInGrammar() {
        var parser = ABNF.parser("""
                start   =   "hello"
                """);

        Assertions.assertTrue(parser.parse("hello").isSuccess());
    }

    @Test
    void testMultilineGrammar() {
        var parser = ABNF.parser("""
                start =
                    "hello"
                    " "
                    "world"
                """);

        Assertions.assertTrue(parser.parse("hello world").isSuccess());
    }


    // Failure / boundary cases
    @Test
    void testPrefixDoesNotCountAsSuccess() {
        var parser = ABNF.parser("start = \"hello\"");

        Assertions.assertTrue(parser.parse("hello!").isFailure());
    }

    @Test
    void testAlmostMatchingInput() {
        var parser = ABNF.parser("start = \"abcdef\"");

        Assertions.assertTrue(parser.parse("abcdeg").isFailure());
    }

    @Test
    void testEmptyRepetitionDoesNotConsumeInput() {
        var parser = ABNF.parser("start = *\"a\" \"b\"");

        Assertions.assertTrue(parser.parse("b").isSuccess());
        Assertions.assertTrue(parser.parse("aaab").isSuccess());
        Assertions.assertTrue(parser.parse("a").isFailure());
    }

    @Test
    void testRepetitionBoundary() {
        var parser = ABNF.parser("start = 2*3\"x\"");

        Assertions.assertTrue(parser.parse("xx").isSuccess());
        Assertions.assertTrue(parser.parse("xxx").isSuccess());
        Assertions.assertTrue(parser.parse("x").isFailure());
        Assertions.assertTrue(parser.parse("xxxx").isFailure());
        Assertions.assertTrue(parser.parse("xxxxx").isFailure());
    }


    // A more complex grammar
    @Test
    void testArithmeticExpressionGrammar() {
        var parser = ABNF.parser("""
                start = number *(operator number)
                operator = "+" / "-"
                number = 1*digit
                digit = %d48-57
                """);

        Assertions.assertTrue(parser.parse("1").isSuccess());
        Assertions.assertTrue(parser.parse("1+2").isSuccess());
        Assertions.assertTrue(parser.parse("1-2").isSuccess());
        Assertions.assertTrue(parser.parse("1+2-3+4").isSuccess());
        Assertions.assertTrue(parser.parse("+1").isFailure());
        Assertions.assertTrue(parser.parse("1+").isFailure());
        Assertions.assertTrue(parser.parse("1++2").isFailure());
    }

    @Test
    void testSimpleJsonLikeObject() {
        var parser = ABNF.parser("""
                start = "{" key-value *("," key-value) "}"
                key-value = "\\\"" key "\\\"" ":" "\\\"" value "\\\""
                key = 1*(letter / digit / "_")
                value = *(letter / digit / " ")
                letter = %d65-90 / %d97-122
                digit = %d48-57
                """);

        Assertions.assertTrue(
                parser.parse("{\"name\":\"Alice\"}").isSuccess()
        );

        Assertions.assertTrue(
                parser.parse("{\"name\":\"Alice\",\"age\":\"30\"}").isSuccess()
        );

        Assertions.assertTrue(
                parser.parse("{}").isFailure()
        );
    }

}
