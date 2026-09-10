## Problems

- Non-productive Grammars, like `S = S` or `S = A S\nA = epsilon`, will terminate, but produce no output and also not
  log a failure. The "productivity" of a grammar can be checked by using the analysis algorithm `isProductive(Sym)` on the grammar. (See example below.)
- Grammars like `S = epsilon | S` produce an infinite number of results if the input is empty. This is technically
  correct behavior, but very confusing to users. This corner case can be checked for my using the `infiniteEmptyRecursionPossible(Sym)` analysis. (See example below.)

```java
import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;

class VerySpecificGrammarProblems {
  void test() {
    var parser1 = Parsewisp.parser("S = A S ; A = epsilon ;");
    var analysis1 = parser1.grammar().analyze();
    System.out.println(analysis1.isProductive(Sym.sym("S"))); // True if the grammar can produce a result

    var parser2 = Parsewisp.parser("S = S | epsilon");
    var analysis2 = parser2.grammar().analyze();
    // TODO: Does not work yet.
    System.out.println(analysis2.infiniteEmptyRecursionPossible(Sym.sym("S"))); // True if the problem is possible.
  }
}
```

## Differences from Instaparse

The biggest difference to Instaparse is that Parsewisp does not require Clojure. Jokes aside, there are a few important
internal differences.

### Smaller things

Parsewisp does not support Instaparse's `:optimize :memory` mode. I found that the additional work is not worth it.
Rest assured that Parsewisp tries its best to save both time and memory by default.

Parsewisp treats some features of Instaparse as bugs. For example, Instaparse treats
`S = epsir \n epsir = 'a'` as equivalent to `S = epsilon ir epsilon\nir = 'a'`. Parsewisp treats it as
`S = epsir \n epsir = 'a'`.

### Production redefinitions

When you write a grammar like `S = A ; S = B ; S = C ;` the question arises: What is the right-hand side of the
production `S`?  
Instaparse chooses to override the previous definitions silently. Parsewisp allows the user to choose between options:

```java
import de.kleinert.parsewisp.Parsewisp;
import parser_options.de.kleinert.parsewisp.RedefinitionOption;
import parser.de.kleinert.parsewisp.Parser;
import parser_options.de.kleinert.parsewisp.ParserCreationOptions;

class RedefTest {
  static void main(String[] args) {
    String grammar = """
            S = 'A' ;
            S = 'B' ;
            S = 'C' ;
            """; // Three different definitions for "S".
    Parser p;
    var opts = ParserCreationOptions.getDefault();

    // Override: The grammar is equal to `S = 'C'`
    p = Parsewisp.parser(grammar, opts.withRedefinitionOption(RedefinitionOption.OVERRIDE));
    System.out.println(p.parse("A").isSuccess()); // false
    System.out.println(p.parse("B").isSuccess()); // false
    System.out.println(p.parse("C").isSuccess()); // true

    // Choice: The grammar is equal to `S = 'A' | 'B' | 'C'`
    p = Parsewisp.parser(grammar, opts.withRedefinitionOption(RedefinitionOption.CHOICE));
    System.out.println(p.parse("A").isSuccess()); // true
    System.out.println(p.parse("B").isSuccess()); // true
    System.out.println(p.parse("C").isSuccess()); // true

    // Keep first: The grammar is equal to `S = 'A'`
    p = Parsewisp.parser(grammar, opts.withRedefinitionOption(RedefinitionOption.KEEP));
    System.out.println(p.parse("A").isSuccess()); // true
    System.out.println(p.parse("B").isSuccess()); // false
    System.out.println(p.parse("C").isSuccess()); // false

    // Error: The grammar is considered invalid and will throw an exception.
    p = Parsewisp.parser(grammar, opts.withRedefinitionOption(RedefinitionOption.ERROR)); // Fails
  }
}
```
## Style considerations

When starting this project, I made a few decisions that are all over the code.

### `@NotNull` and `@Nullable` everywhere.

Despite some
people [being very unhappy with the use of these annotations](https://news.ycombinator.com/item?id=37534184), I found
the compiler warnings helpful. These annotations ultimately
help [make the code more readable](https://stackoverflow.com/a/70817574).

### `final` classes, methods and variables

Despite it apparently cluttering the code, marking final things `final` is simply good style.

### Preferring `record`, but not always

Sometimes, I decided to convert a class to a `record` if it made sense. I always carefully measured performance and
converted back to classes if it made the code faster. This was rare, but very impactful. If you can explain to me why
using records is sometimes 60% slower than equivalent classes, let me know. :)

### `Sym` vs `String`

Strings in Java are very optimized. Still, Parsewisp uses its own type `Sym` for production names. Like Clojure's
`Keyword`, `Sym` instances are interned. Clojure embeds the Keywords into the code directly, while Parsewisp always
instantiates them when needed. The advantage that interning provides is the possibility of a constant `O(1)` equality
check.

The decision comes down to time and readability.

Strings at first use reference equality (`==`) for checks, then falls back on the `O(n)` char-by-char check. Sym always
uses reference checking. `Sym` looses time when it has to wrap strings. Doing some measurements, the times were almost
equal.

So it came down to readability and personal preference.

### Consistent use `LinkedHashMap` and `LinkedHashSet`

I use these types because they are ordered. Some test cases showed inconsistent behavior when using the non-sequenced
types. But only *sometimes*. I prefer deterministic behavior.

### New interfaces for some functions

```java
import de.kleinert.parsewisp.functions.Listener;
import de.kleinert.parsewisp.functions.NegativeListener;
import de.kleinert.parsewisp.functions.Procedure;

// Could be java.util.function.Consumer<ParsewispMessage>. New type for clarity.
de.kleinert.parsewisp.functions.Listener listener = (ParsewispMessage o) -> System.out.println("Listener");

// Could be java.lang.Runnable. New type because Runnable is associated with Threads.
de.kleinert.parsewisp.functions.NegativeListener negativeListener = () -> System.out.println("NegativeListener");
de.kleinert.parsewisp.functions.Procedure procedure = () -> System.out.println("Procedure");
```

### New collection types

When returning a parse forest, a lazy list is used because parse forests can easily have over a million entries. Java
(to my knowledge) does not have these. The only alternative I can think of are `Stream`s, but those can only be iterated
once. A construct like
Clojure's [LazySeq](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/LazySeq.java) could achieve the
same while being simpler, but after testing each approach, I found this new type to be substantially faster.

```java
import de.kleinert.parsewisp.collections.LazySupplierList;

LazySupplierList<T> lazySupplierList;
```


