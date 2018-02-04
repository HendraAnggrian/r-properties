import org.junit.Test;
import r.example.R;

import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertEquals(R.string.im, "im");
        assertEquals(R.string.a, "a");
        assertEquals(R.string.little, "little");
        assertEquals(R.string.piggy, "piggy");
    }
}
