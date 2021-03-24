import my.app.R2;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertEquals(R2.layout.layout_a, "/layout/layout_a.fxml");
    }
}