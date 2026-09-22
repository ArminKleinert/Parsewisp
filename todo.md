- Rework README
- Can infinite epsilons happen if there is no nonterm which directly resolves to epsilon?

Bug?
- More epsilon problems?
    `System.out.println(Parsewisp.parser("S = 'a' E 'b' ; E = eps+").parses("ab"));` => `[[:S, "a", [:E], "b"]]` (Expected)
    `System.out.println(Parsewisp.parser("S = 'a' E+ 'b' ; E = eps+").parses("ab"));` => `[[:S, "a", "b"]]` (Would expect to see `[:E]` in tree?)


