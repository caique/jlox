package jlox;

import jlox.tokens.Token;
import jlox.tokens.TokenType;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ScannerTest {

    @Test
    public void supportSingleLineComments() throws IOException {
        String onlyComments = loadCodeSample("onlyComments.lox");

        List<Token> tokens = new Scanner(onlyComments).scanTokens();

        assertThat(tokens).containsExactly(new Token(TokenType.EOF, "", null, 2));
    }

    @Test
    public void supportsMultilineComments() throws IOException {
        String multilineComments = loadCodeSample("multilineComments.lox");

        List<Token> tokens = new Scanner(multilineComments).scanTokens();

        assertThat(tokens).containsExactly(new Token(TokenType.EOF, "", null, 2));
    }

    private String loadCodeSample(String filename) throws IOException {
        InputStream stream = ScannerTest.class.getClassLoader().getResourceAsStream("code-samples/" + filename);
        return IOUtils.toString(stream, Charset.defaultCharset());
    }

}
