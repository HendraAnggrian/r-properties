import com.example.R;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertThat(R.string.names()).containsExactly("string_en", "string_in");
        assertEquals(R.string.im, "im");
        assertEquals(R.string.a, "a");
        assertEquals(R.string.little, "little");
        assertEquals(R.string.piggy, "piggy");
    }
}
