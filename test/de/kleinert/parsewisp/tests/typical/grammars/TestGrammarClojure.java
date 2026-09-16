package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.testutil.PT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class TestGrammarClojure {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/clojure.grammar"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void helloWorld() {
        var text = "(println \"hello world\")";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void ns() {
        var text = """
                (ns instaparse.core
                  (:gen-class)
                  (:require [clojure.walk :as walk]
                            [instaparse.macros :refer [defclone set-global-var!]])
                  (:import (clojure.lang IFn PersistentVector)
                           (java.util ArrayList List)))""";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void defDynamic() {
        var text = "(def ^:dynamic *default-output-format* :hiccup)";
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void bigFunction() {
        var text = """
                (defn parse
                  "Use parser to parse the text.  Returns first parse tree found
                   that completely parses the text.  If no parse tree is possible, returns
                   a Failure object.
                
                   Optional keyword arguments:
                   :start :keyword  (where :keyword is name of starting production rule)
                   :partial true    (parses that don't consume the whole string are okay)
                   :total true      (if parse fails, embed failure node in tree)
                   :unhide <:tags or :content or :all> (for this parse, disable hiding)
                   :optimize :memory   (when possible, employ strategy to use less memory)
                
                   Clj only:
                   :trace true      (print diagnostic trace while parsing)"
                  [parser text & {:as options}]
                  {:pre [(contains? #{:tags :content :all nil} (get options :unhide))
                         (contains? #{:memory nil} (get options :optimize))]}
                  (let [start-production
                        (get options :start (:start-production parser)),
                
                        partial?
                        (get options :partial false)
                
                        optimize?
                        (get options :optimize false)
                
                        unhide
                        (get options :unhide)
                
                        trace?
                        (get options :trace false)
                
                        _ (when (and trace? (not gll/TRACE)) (enable-tracing!))
                
                        parser (unhide-parser parser unhide)]
                    #_(->> (cond
                             (:total options)
                             (gll/parse-total (:grammar parser) start-production text
                                              partial? (red/node-builders (:output-format parser)))
                
                             (and optimize? (not partial?))
                             (let [result (repeat/try-repeating-parse-strategy parser text start-production)]
                               (if (failure? result)
                                 (gll/parse (:grammar parser) start-production text partial?)
                                 result))
                
                             :else
                             (gll/parse (:grammar parser) start-production text partial?))
                
                           #_(gll/bind-trace trace?))
                    (cond
                      (:total options)
                      (gll/parse-total (:grammar parser) start-production text
                                       partial? (red/node-builders (:output-format parser)))
                
                      (and optimize? (not partial?))
                      (let [result (repeat/try-repeating-parse-strategy parser text start-production)]
                        (if (failure? result)
                          (gll/parse (:grammar parser) start-production text partial?)
                          result))
                
                      :else
                      (gll/parse (:grammar parser) start-production text partial?))))
                """;
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    /**
     * This testcase uses my local Instaparse sources. It likely won't work on other machines and is thus commented out.
     *
     * @throws IOException If the sources can't be read.
     */
    @Test
    void testAllInstaparseCljSources() throws IOException {
//        try (var sources = Files.walk(Path.of("/home/tpk/Desktop/programming/instaparse-master/src"))) {
//            var files = sources
//                    .filter(it -> it.toFile().isFile())
//                    .filter(it -> it.toString().endsWith(".clj") || it.toString().endsWith(".cljs") || it.toString().endsWith(".cljc"))
//                    .toList();
//            var p = parser();
////            var start = System.nanoTime();
//            for (Path file : files) {
//                var s = Files.readString(file);
//                var res = p.parse(s);
//                if (res.isFailure())
//                    System.out.println(res);
//                Assertions.assertTrue(res.isSuccess());
//            }
////            var end = System.nanoTime();
////            System.out.println((end - start) / 1000000.0);
//        }
    }

    @Test
    void testComment() {
        var p = parser();
        var text = "\"\\\"\"";
        Assertions.assertEquals(
                PT.create("S", PT.create("sexpr", PT.create("string", "\"\\\"\""))),
                p.parse(text)
        );
    }
}