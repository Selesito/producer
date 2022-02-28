package morozov.vu.domain;

import static org.assertj.core.api.Assertions.assertThat;

import morozov.vu.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShopOneTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShopOne.class);
        ShopOne shopOne1 = new ShopOne();
        shopOne1.setId(1L);
        ShopOne shopOne2 = new ShopOne();
        shopOne2.setId(shopOne1.getId());
        assertThat(shopOne1).isEqualTo(shopOne2);
        shopOne2.setId(2L);
        assertThat(shopOne1).isNotEqualTo(shopOne2);
        shopOne1.setId(null);
        assertThat(shopOne1).isNotEqualTo(shopOne2);
    }
}
