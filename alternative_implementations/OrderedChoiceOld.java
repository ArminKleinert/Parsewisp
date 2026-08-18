//package alphaparse.parsing;
//
//import alphaparse.functions.Listener;
//import alphaparse.reduction.ReductionType;
//
//import static alphaparse.trampoline.TrampolineListenerNode.TrampolineListenerKey;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//
///**
// * This class is an alternative to the {@link AlternationRule}.
// * <p>
// * It represents the ABNF choice operator {@code (p1 / p2)} (where p1 and p2 are instances of {@link Rule})
// * and should work like the PEG extension which makes it "ordered".
// * <p>
// * As of now, it does not work right,
// * so it can be considered a worse alternative to the {@link AlternationRule}.
// */
//public final class OrderedChoiceRule extends RuleWithManyChildren {
//    private final @NotNull Rule rule1;
//    private final @NotNull Rule rule2;
//
//    private OrderedChoiceRule(final boolean hide, final @NotNull ReductionType red,
//                              final @NotNull Rule rule1,
//                              final @NotNull Rule rule2) {
//        super(hide, red, List.of(rule1, rule2));
//        this.rule1 = rule1;
//        this.rule2 = rule2;
//    }
//
//    private OrderedChoiceRule(final @NotNull List<Rule> rules,
//                              final boolean hide,
//                              final @NotNull ReductionType red) {
//        this(hide, red, setupParsers(rules).rule1, setupParsers(rules).rule2);
//    }
//
//    private OrderedChoiceRule(final @NotNull Rule rule1,
//                              final @NotNull Rule rule2) {
//        this(defaultHidden, ReductionType.standardInitialReduction(), rule1, rule2);
//    }
//
//    private static @NotNull OrderedChoiceRule setupParsers(
//            final @NotNull List<@NotNull Rule> parsers) {
//        if (parsers.size() < 2)
//            throw new IllegalArgumentException();
//
//        if (parsers.size() == 2)
//            return new OrderedChoiceRule(parsers.get(0), parsers.get(1));
//
//        var restParsers = parsers.subList(1, parsers.size());
//        return new OrderedChoiceRule(parsers.getFirst(), setupParsers(restParsers));
//    }
//
//    /**
//     * Create a new instance. Depending on the implementation, allows for buffering or create a different type of rule.
//     *
//     * @param rules The wrapped symbol.
//     * @return A rule.
//     */
//    public static @NotNull Rule create(final @NotNull List<Rule> rules) {
//        if (rules.isEmpty())
//            return EpsilonTerm.getDefault();
//
//        var distinctRules = rules.stream().distinct().toList();
//
//        if (distinctRules.size() == 1)
//            return distinctRules.getFirst();
//        var setup = setupParsers(distinctRules);
//
//        return new OrderedChoiceRule(
//                defaultHidden, defaultReductionType,
//                setup.rule1, setup.rule2);
//    }
//
//    @Override
//    public void parse(final int index, final @NotNull Gll runner) {
//        final @NotNull Rule rule1 = this.rule1;
//        final @NotNull Rule rule2 = this.rule2;
//        final @NotNull TrampolineListenerKey nodeKeyForComb1 =
//                new TrampolineListenerKey(index, rule1);
//        final @NotNull TrampolineListenerKey nodeKeyForComb2 =
//                new TrampolineListenerKey(index, rule2);
//        final @NotNull Listener listener =
//                runner.nodeListener(new TrampolineListenerKey(index, this));
//        runner.pushListener(nodeKeyForComb1, listener);
//        runner.pushNegativeListener(nodeKeyForComb1, () -> runner.pushListener(nodeKeyForComb2, listener));
//    }
//
//    @Override
//    public void fullParse(final int index, final @NotNull Gll runner) {
//        final @NotNull Rule rule1 = this.rule1;
//        final @NotNull Rule rule2 = this.rule2;
//        final @NotNull TrampolineListenerKey nodeKeyForComb1 =
//                new TrampolineListenerKey(index, rule1);
//        final @NotNull TrampolineListenerKey nodeKeyForComb2 =
//                new TrampolineListenerKey(index, rule2);
//        final @NotNull Listener listener =
//                runner.nodeListener(new TrampolineListenerKey(index, this));
//        runner.pushFullListener(nodeKeyForComb1, listener);
//        runner.pushNegativeListener(nodeKeyForComb1, () -> runner.pushFullListener(nodeKeyForComb2, listener));
//    }
//
//    @Override
//    public @NotNull OrderedChoiceRule withHideTag(final boolean hide) {
//        return isHidden() == hide ? this : new OrderedChoiceRule(getRules(), hide, this.getReduction());
//    }
//
//    @Override
//    public @NotNull OrderedChoiceRule withReduction(final @NotNull ReductionType red) {
//        return getReduction() == red ? this : new OrderedChoiceRule(getRules(), isHidden(), red);
//    }
//
//    @Override
//    public @NotNull OrderedChoiceRule withRules(final @NotNull List<@NotNull Rule> rules) {
//        return new OrderedChoiceRule(rules, isHidden(), getReduction());
//    }
//}
