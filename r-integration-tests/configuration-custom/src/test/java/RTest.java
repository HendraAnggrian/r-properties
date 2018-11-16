import com.example.R;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertEquals("my.file", R.custom.my_file);

        assertEquals("/custom/my.file", R.custom.file_my);
    }
}