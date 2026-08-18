//package alphaparse.parsing;
//
//import alphaparse.reduction.ReductionType;
//
//import static alphaparse.trampoline.TrampolineListenerNode.TrampolineListenerKey;
//
//import alphaparse.trampoline.TrampolineListenerNode;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
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
//    private OrderedChoiceRule(final @NotNull List<Rule> rules,
//                              final boolean hide,
//                              final @NotNull ReductionType red) {
//        super(hide, red, rules);
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
//        if (rules.size() == 1)
//            return rules.getFirst();
//
//        var compressedRules = new ArrayList<Rule>();
//        var ruleSet = new HashSet<Rule>();
//
//        for (@NotNull Rule rule : rules) {
//            if (rule instanceof OrderedChoiceRule cc) {
//                compressedRules.addAll(cc.getRules());
//                for (Rule ccRule : cc.getRules()) {
//                    if (ruleSet.add(ccRule))
//                        compressedRules.add(ccRule);
//                }
//            } else {
//                if (ruleSet.add(rule))
//                    compressedRules.add(rule);
//            }
//        }
//
//        return new OrderedChoiceRule(
//                compressedRules,
//                defaultHidden, defaultReductionType);
//    }
//
//    @Override
//    public void parse(final int index, final @NotNull Gll runner) {
//        ocListener(index, 0, rules, new TrampolineListenerKey(index, this), runner);
//    }
//
//    @Override
//    public void fullParse(final int index, final @NotNull Gll runner) {
//        ocFullListener(index, 0, rules, new TrampolineListenerKey(index, this), runner);
//    }
//
//    private void ocListener(final int index,
//                            final int ruleIndex,
//                            final @NotNull List<Rule> rules1,
//                            final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
//                            final @NotNull Gll runner) {
//        final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKeyForComb1 =
//                new TrampolineListenerNode.TrampolineListenerKey(index, rules1.getFirst());
//        runner.pushListener(nodeKeyForComb1, runner.nodeListener(nodeKey));
//
//        if (rules1.size() > 1) {
//            runner.pushNegativeListener(
//                    nodeKeyForComb1,
//                    () -> ocListener(index, ruleIndex + 1, rules1.subList(1, rules1.size()), nodeKey, runner));
//        }
//    }
//
//    private void ocFullListener(final int index,
//                                final int ruleIndex,
//                                final @NotNull List<Rule> rules1,
//                                final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKey,
//                                final @NotNull Gll runner) {
//        runner.debug = true;
//        final @NotNull TrampolineListenerNode.TrampolineListenerKey nodeKeyForComb1 =
//                new TrampolineListenerNode.TrampolineListenerKey(index, rules1.getFirst());
//
//        System.out.println("All rules:    " + rules1);
//        System.out.println("Current:      " + rules1.getFirst());
//        System.out.println("Rule index:   " + ruleIndex);
//
//        runner.pushFullListener(nodeKeyForComb1, (res) -> {
//            runner.nodeListener(nodeKey).execute(res);
//            System.out.println("Matched       " + rules1.getFirst() + " (rule index " + ruleIndex + ")");
//        });
//
//        if (rules1.size() <= 1) return;
//
//        runner.pushNegativeListener(
//                nodeKeyForComb1,
//                () -> {
//                    System.out.println("Did not match " + nodeKeyForComb1 + " or reached new generation.");
//                    System.out.println("Try to match  " + rules1.get(1) + " (rule index " + (ruleIndex + 1) + ")");
//                    ocFullListener(index, ruleIndex + 1, rules1.subList(1, rules1.size()), nodeKey, runner);
//                }
//        );
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
//
//    @Override
//    public String toString() {
//        return new StringJoiner(", ", OrderedChoiceRule.class.getSimpleName() + "[", "]")
//                .add("rules=" + rules)
//                .toString();
//    }
//}