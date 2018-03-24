import com.example.R;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertEquals(R.font.opensans_regular, "/font/OpenSans-Regular.ttf");
        assertEquals(R.font.opensans_bold, "/font/OpenSans-Bold.ttf");
    }
}
