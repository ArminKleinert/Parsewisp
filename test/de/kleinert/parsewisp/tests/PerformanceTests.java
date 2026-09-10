package de.kleinert.parsewisp.tests;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser_options.ParsingOptions;
import org.junit.jupiter.api.Test;

class PerformanceTests {
//    @Test
//    void fullTest() throws IOException {
//        final boolean doRun = true;
//        final int testNumMultiplierForSlowTests = 100;
//        final int testNumMultiplier = 1000;
//        final @NotNull String c99GrammarText = Files.readString(Path.of("testres/grammars/c99.g"));
//
//        /**/
//        if (!doRun) {
//            return;
//        }
//
//        var text = "int a(int r){return r;}\n            int a(int r, int a){return r;}";
//        var p = Parsewisp.parser(c99GrammarText, ParserCreationOptions.newWithStandardWhitespace());
//
//        System.out.println("Preparing performance tests...");
//        TimeUtil.measureTimeMillis(20,
//                () -> Parsewisp.parser(c99GrammarText, ParserCreationOptions.newWithStandardWhitespace()));
//        TimeUtil.measureTimeMillis(2000,
//                () -> Parsewisp.parse(p, text));
//
//        System.out.println("\n----------------------------------\n--- Standard performance tests ---\n----------------------------------");
//        System.out.println("Make parser without correctness check.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(2 * testNumMultiplierForSlowTests,
//                () -> Parsewisp.parser(c99GrammarText, ParserCreationOptions.newWithStandardWhitespace().withCorrectnessCheck(false))));
//        System.out.println("Previous:    {:lowest 57.658, :highest 103.016, :diff 45.358, :average 63.324, :mid 59.619, :median 61.341, :total 12664.708}");
//        System.out.println("Previous 2:  {:lowest 49.696, :highest 72.810, :diff 23.114, :average 51.662, :mid 50.159, :median 50.327, :total 103323.969}");
//        System.out.println("Original:    {:lowest 105.916, :highest 214.071, :diff 108.155, :average 112.053, :mid 110.125, :median 110.065, :sum 224105.047} // n=2000");
//
//        System.out.println("---");
//        var presetOpts = ParserCreationOptions.newWithStandardWhitespace();
//        System.out.println("Make parser with correctness check.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(2 * testNumMultiplierForSlowTests,
//                () -> Parsewisp.parser(c99GrammarText, presetOpts)));
//        System.out.println("Previous 2:  {:lowest 49.696, :highest 72.810, :diff 23.114, :average 51.662, :mid 50.159, :median 50.327, :total 103323.969}");
//        System.out.println("Original:    {:lowest 105.916, :highest 214.071, :diff 108.155, :average 112.053, :mid 110.125, :median 110.065, :sum 224105.047} // n=2000");
//
//        System.out.println("---");
//        System.out.println("Extract the first parse.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(20 * testNumMultiplier,
//                () -> Parsewisp.parse(p, text)));
//        System.out.println("Previous:    {:lowest 1.382, :highest 6.293, :diff 4.911, :average 1.520, :mid 1.443, :median 1.459, :total 30397.866}");
//        System.out.println("Previous 2:  {:lowest 1.274, :highest 3.420, :diff 2.146, :average 1.309, :mid 1.303, :median 1.303, :total 26187.008}");
//        System.out.println("Original:    {:lowest 3.238, :highest 9.338, :diff 6.100, :average 3.299, :mid 3.279, :median 3.280, :sum 65981.479} // n=20000");
//
//        System.out.println("---");
//        System.out.println("All parses as a lazy list. (Parse Forest)");
//        System.out.println("             " + TimeUtil.measureTimeMillis(20 * testNumMultiplier,
//                () -> Parsewisp.parses(p, text)));
//        System.out.println("Previous:    {:lowest 0.000, :highest 0.045, :diff 0.045, :average 0.001, :mid 0.001, :median 0.001, :total 15.080}");
//        System.out.println("Previous 2:  {:lowest 1.246, :highest 3.653, :diff 2.407, :average 1.287, :mid 1.280, :median 1.280, :total 25736.796}");
//        System.out.println("Original:    {:lowest 3.235, :highest 6.767, :diff 3.532, :average 3.296, :mid 3.281, :median 3.282, :sum 65926.257} // n=20000");
//
//        System.out.println("---");
//        System.out.println("Get all parses (parse forest) and make an array.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(20 * testNumMultiplierForSlowTests,
//                () -> Parsewisp.parses(p, text).toArray()));
//        System.out.println("Previous:    {:lowest 12.567, :highest 36.166, :diff 23.599, :average 13.600, :mid 12.906, :median 12.971, :total 27200.583}");
//        System.out.println("Previous 2:  {:lowest 13.913, :highest 19.723, :diff 5.810, ::average 14.222, :mid 14.141, :median 14.144, :total 284433.845}");
//        System.out.println("Original:    {:lowest 37.369, :highest 60.060, :diff 22.691, :average 38.122, :mid 37.937, :median 37.943, :sum 762444.823} // n=20000");
//
//        System.out.println("---");
//        System.out.println("Get all parses (parse forest) and iterate using a for-each loop.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(20 * testNumMultiplierForSlowTests,
//                () -> {
//                    for (var ignored : Parsewisp.parses(p, text)) ;
//                }));
//        System.out.println("Previous:    {:lowest 12.501, :highest 22.928, :diff 10.427, :average 13.509, :mid 12.809, :median 12.909, :total 27017.503}");
//        System.out.println("Previous 2:  {:lowest 13.906, :highest 23.016, :diff 9.110, :average 14.232, :mid 14.153, :median 14.155, :total 284642.717}");
//        System.out.println("Original:    {:lowest 37.264, :highest 46.080, :diff 8.816, :average 38.005, :mid 37.834, :median 37.833, :sum 760096.656} // n=20000");
//
//        System.out.println("---");
//        System.out.println("Get all parses (parse forest) and turn it into an ArrayList.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(20 * testNumMultiplierForSlowTests,
//                () -> {
//                    var l = new ArrayList<>(Parsewisp.parses(p, text));
//                }));
//        System.out.println("Previous:    {:lowest 12.480, :highest 26.343, :diff 13.862, :average 13.395, :mid 12.804, :median 12.850, :total 26790.117}");
//        System.out.println("Previous 2:  {:lowest 13.909, :highest 23.171, :diff 9.262, :average 14.210, :mid 14.143, :median 14.141, :total 284207.290}");
//        System.out.println("Original:    {:lowest 37.336, :highest 45.696, :diff 8.360, :average 38.093, :mid 37.921, :median 37.919, :sum 761850.134} // n=20000");
//
//        System.out.println("---");
//        System.out.println("Get all parses (parse forest) and count them.");
//        System.out.println("             " + TimeUtil.measureTimeMillis(20 * testNumMultiplierForSlowTests,
//                () -> Parsewisp.parses(p, text).size()));
//        System.out.println("Previous:    {:lowest 12.539, :highest 23.903, :diff 11.364, :average 13.446, :mid 12.850, :median 12.895, :total 26892.826}");
//        System.out.println("Previous 2:  {:lowest 13.960, :highest 23.369, :diff 9.409, :average 14.266, :mid 14.191, :median 14.191, :total 285313.230}");
//        System.out.println("Original:    -");
//
//        System.out.println("Count of parses: " + Parsewisp.parses(p, text).size());
//    }

    @Test
    void testNumberOfParses() // Currently tested with max=23, number is exclusive
    {
        final int max = 15;
        System.out.println("\n----------------------------------\n---   Number of parses tests   ---\n----------------------------------");
        System.gc();
        var grammar = "S : (A | B)+\nA : 'a' | 'b'\nB : 'b' | 'a'";
        var p = Parsewisp.parser(grammar);
        var sb = new StringBuilder();
        for (int n = 0; n < max; n++) {
            int num = p.parses(sb.toString(), ParsingOptions.getDefault()).size();
            System.out.println("Parses for " + n + ": " + num + " (Correct? "
                    + (num == 0 || num == 1 << n)
                    + ")");
            sb.append("a");
        }
    }
}
