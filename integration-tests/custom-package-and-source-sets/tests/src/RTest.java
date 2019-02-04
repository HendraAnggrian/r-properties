import my.app.Resources;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTest {

    @Test
    public void test() {
        assertEquals(Resources.layout.layout_a, "/layout/layout_a.fxml");
    }
}