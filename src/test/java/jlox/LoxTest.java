/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package jlox;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LoxTest {

    @Test
    public void mainRun() throws IOException {
        String[] args = {"script1.lox", "script2.lox"};

        Lox.main(args);

        assertThat(true).isTrue();
    }

}