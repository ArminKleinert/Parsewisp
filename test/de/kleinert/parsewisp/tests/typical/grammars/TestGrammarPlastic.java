package de.kleinert.parsewisp.tests.typical.grammars;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.parser.Parser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test(s) for the PlasticLang grammar.
 * <p>
 * Grammar and tests from <a href="https://github.com/rogeralsing/PlasticLang">PlasticLang on GitHub</a>.
 */
class TestGrammarPlastic {
    private @NotNull Parser parser() {
        try {
            return Parsewisp.parser(
                    Files.readString(Path.of("testres/grammars/plastic.g"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void core_pla() {
        var text = """
                for := func (@init , @guard, @step, @body) {
                    init()
                    while(guard()) {
                        body()
                        step()
                    }
                }
                
                repeat := func (times, @body) {
                    while(times >= 0) {
                        body()
                        times--
                    }
                }
                
                LinkedList := class {
                    Node := class (value) { next = null; }
                
                    head := null;
                    tail := null;
                    add := func (value) {
                        node := Node(value);
                        if (head == null) {
                            head = node;
                            tail = node;
                        } else {
                            tail.next =  node;
                            tail = node;
                        }
                    }
                
                    each := func (lambda) {
                        current := head;
                        while(current != null) {
                            lambda(current.value);
                            current = current.next;
                        }
                    }
                }
                
                Stack := class {
                    Node := class (value,prev) { next = null; }
                
                    head := null;
                    tail := null;
                    push := func (value) {
                        node = Node(value,tail);
                        if (head == null) {
                            head = node;
                            tail = node;
                        } else {
                            tail.next =  node;
                            tail = node;
                        }
                    }
                
                    each := func (lambda) {
                        current = tail;
                        while(current != null) {
                            lambda(current.value);
                            current = current.prev;
                        }
                    }
                
                    peek := func() {
                        tail.value;
                    }
                
                    pop := func() {
                        res = tail.value;
                        tail = tail.prev;
                        if (tail != null) {
                            tail.next = null;
                        } else {
                            head = null;
                        }
                        res
                    }
                }
                
                
                switch :=  func(exp, @body) {
                    matched := false;
                    case := func (value, @caseBody) {
                        if (exp == value) {
                            caseBody();
                            matched = true;
                        }
                    }
                    default := func (@defaultBody) {
                        if (matched == false) {
                            defaultBody();
                        }
                    }
                    body();
                }
                
                quote := func(@q) {
                    q
                }
                """;
        //System.out.println(parser().parse(text, ParsingOptions.getDefault()));
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test
    void sample_pla() {
        var text = """
                a := 1
                b := 3
                c := a + b
                a := a + 1
                print('c = ' + c)
                print('a = ' + a)
                
                print ('hello'.GetType().Name.Length)
                
                Console := using (System.Console)
                print(Console)
                Console.WriteLine('.NET interop!!')
                
                tuple       := ('hello','this','is','a','tuple')
                arr         := ['hello','this','is','an','array']
                statements  := {'hello','this','is','a','body'}     //this will result in "body" as statements are evaluated directly
                
                
                print (arr.1)
                
                print ('arr length is ' + (arr.'Length' + 100) )
                print ('str length is ' + 'some string'.Length)
                
                closurePrint := x => print(x + a)
                closurePrint('foo')
                
                each(element, arr)
                {
                    print(element)
                }
                
                for (a := 0; a < 10; a ++)
                {
                    print (a)
                }
                
                repeat(3)
                {
                    print('repeat..')
                }
                
                a := 1
                
                if (a == 1)
                {
                    print ('inside if')
                }
                elif (a == 3)
                {
                    print ('inside elif')
                }
                else
                {
                    print ('inside else')
                }
                
                while (a < 5)
                {
                     print ('daisy me rollin`')
                     a++
                }
                
                (x => print('lambda fun ' + x))('yay')
                
                if (true, print('hello'))
                
                f := func(a,b,c)
                {
                    print('abc '+a+' '+b+' '+c)
                }
                
                f(1)(2)(3)
                
                multiply := (x,y) => x*y
                double := multiply(2)
                
                print ('88 doubled is ' + double(88))
                
                BeepMixin := mixin
                {
                    beep := func ()
                    {
                        print ('beep')
                    }
                }
                
                Person := class (firstName,lastName)
                {
                    BeepMixin()
                    sayHello := func ()
                    {
                        print ('Hello {0} {1}',firstName,lastName)
                    }
                }
                
                john := Person('John','Doe')
                jane := Person('Jane','Doe')
                john.extra = Person('testing sub object','oh yes')
                john.firstName = 'Johnny'
                
                john.beep()
                
                john.sayHello()
                jane.sayHello()
                john.extra.sayHello()
                
                list = LinkedList()
                list.add('first')
                list.add('second')
                list.add('third')
                list.add('last')
                list.each(v => {
                    print ('lamda ' + v)
                })
                
                s = Stack()
                s.push(1)
                s.push(2)
                s.push(3)
                
                print (s.pop())
                print (s.pop())
                print (s.pop())
                
                
                bar = 123
                
                print ('testing pattern matching')
                switch (bar) {
                    case(123) {	print('123')}
                    case(888) {	print('888!!!!!!!')}
                    case(999) {	print('999')}
                    default   { print('no match')}
                }
                
                {@body => body()}{print ('strange')}
                
                code = 'print("I am eval!!")'
                eval(code)
                """;
        //System.out.println(parser().parse(text));
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }

    @Test void strings_pla() {
        var text = """
                print ('Hello {0} {1}',firstName,lastName)
                "Hello :name!"
                'Hello :name!'
                "Quote '\\""
                'Quote \\'"'
                """;
        //System.out.println(parser().parse(text));
        Assertions.assertTrue(parser().parse(text).isSuccess());
    }
}