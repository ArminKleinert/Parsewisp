package de.kleinert.parsewisp.other_formats;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import org.junit.jupiter.api.Test;

public class ABNFTest {
    Parser abnf = Parsewisp.parser(new AbnfBuilder(ParserCreationOptions.getDefault()).build(), ParserCreationOptions.getDefault());

    @Test
    void test() {
        //System.out.println(abnf.show());
//        var tree = abnf.parse("A = \"abc\" \r\n B = %d12 / %d10 %d11");
//        System.out.println(tree);
//        System.out.println(tree.castToParseSuccess().getNode(0).tree().getNode(0).tree().getNode(0).string());
        var g = Abnf.parser("A = \"abc\" 1*4 B \n B = \"1\"", ParserCreationOptions.getDefault());
        System.out.println(g);
        System.out.println(g.parse("abc11"));
    }
}
