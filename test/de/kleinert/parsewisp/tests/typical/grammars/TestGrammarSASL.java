package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test(s) for the SASL grammar.
 * <p>
 * Grammar from <a href="https://kops.uni-konstanz.de/server/api/core/bitstreams/6e7a9d65-73a7-4f05-bc0d-5904c83702bc/content">Uni Konstanz</a>.
 */
class TestGrammarSASL {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/sasl.g")),
                    ParserCreationOptions.newWithStandardWhitespace()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void helloWorld() {
        var text = "def hello = \"Hello World\" . nil";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void fnWithMultipleArgs() {
        var text = "def add a b = a+b . nil";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void moveValue() {
        var text = """
                def id x = x
                
                def until p f x = if p x then x else until p f (f x)
                def comp f g x = f (g x)
                def map f l = if l=nil then nil
                  else (f x):(map f xs) where x = hd l;
                                              xs = tl l
                def fold m z l = if l=nil then z
                         else m x (fold m z xs) where x = hd l;
                                                      xs = tl l
                def append l1 l2 = if l1=nil then l2
                        else x:(append xs l2) where x = hd l1;
                xs = tl l1
                def reverse l = if l=nil then nil
                        else append (reverse (tl l)) ((hd l):nil)
                def filter p l = if l=nil then nil
                        else (if p x then x:(filter p xs)
                        else filter p xs) where x = hd l;
                                                xs = tl l
                def sort p l = if l=nil then nil
                        else insert p (hd l) (sort p (tl l))
                        where
                        insert pp e ll = if ll=nil then (e:nil)
                                else
                                if pp e (hd ll) then (e:ll)
                                else
                                ((hd ll):(insert pp e (tl ll)))
                def drop n l = if n<=0 then l
                        else if l=nil then nil
                        else drop (n-1) (tl l)
                def take n l = if n=0 or l=nil then nil
                        else x:take (n-1) xs where x = hd l;
                                                   xs = tl l
                def at n l = if n=0 then hd l
                        else at (n-1) (tl l)
                def null l = l=nil
                def length l = if l=nil then 0
                        else 1+(length(tl l))
                def sum = fold plus 0
                def product = fold times 1
                def plus x y = x+y
                def mul x y = x*y
                def div x y = x/y
                def div2 y x = y/x
                def minus x y = x-y
                def minus2 y x = y-x
                def lt x y = x<y
                def leq x y = x<=y
                def eq x y = x=y
                def geq x y = x>=y
                def gt x y = x>y
                
                def zipWith f x y = if x=nil then nil
                        else f (hd x) (hd y):zipWith f (tl x) (tl y).
                
                at 19 fib where fib = 1:1:(zipWith plus fib (tl fib))
                """;
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }
}