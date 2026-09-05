- Rework README
- Investigate: Could interning (memoizing) ReductionType instances save memory and time? (`ReductionType.defaultNonRawReduction`)
  - What would be the cutoff in grammar size (number of productions)?
- Investigate: Should `ReductionType.applyStandardReductionToProductions` intern (memoize) the rules?
- Can infinite epsilons happen if there is no nonterm which directly resolves to epsilon?

