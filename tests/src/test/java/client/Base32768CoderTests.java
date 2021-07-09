package client;

import mindustry.client.crypto.Base32768Coder;
import mindustry.client.utils.UtilitiesKt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.Random;

public class Base32768CoderTests {

    @Test
    void testCoder() throws IOException {
        for (int i = 0; i < 100; i++) {
            byte[] bytes = new byte[1000];
            new Random().nextBytes(bytes);

            String encoded = Base32768Coder.INSTANCE.encode(bytes);
            Assertions.assertArrayEquals(bytes, Base32768Coder.INSTANCE.decode(encoded));
        }
    }
}
