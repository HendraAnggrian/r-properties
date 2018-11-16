import com.example.R;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertEquals("text-field", R.style.text_field);
        assertEquals("hyperlink", R.style.hyperlink);
        assertEquals("label", R.style.label);

        assertEquals("/style/some.css", R.style.css_some);
        assertEquals("/style/another.css", R.style.css_another);
    }
}