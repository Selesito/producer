package morozov.vu.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import morozov.vu.IntegrationTest;
import morozov.vu.domain.ShopOne;
import morozov.vu.repository.ShopOneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ShopOneResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ShopOneResourceIT {

    private static final String DEFAULT_SHOP_NAME = "AAAAAAAAAA";
    private static final String UPDATED_SHOP_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_OWNER = "AAAAAAAAAA";
    private static final String UPDATED_OWNER = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/shop-ones";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ShopOneRepository shopOneRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restShopOneMockMvc;

    private ShopOne shopOne;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShopOne createEntity(EntityManager em) {
        ShopOne shopOne = new ShopOne().shopName(DEFAULT_SHOP_NAME).owner(DEFAULT_OWNER).category(DEFAULT_CATEGORY).email(DEFAULT_EMAIL);
        return shopOne;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ShopOne createUpdatedEntity(EntityManager em) {
        ShopOne shopOne = new ShopOne().shopName(UPDATED_SHOP_NAME).owner(UPDATED_OWNER).category(UPDATED_CATEGORY).email(UPDATED_EMAIL);
        return shopOne;
    }

    @BeforeEach
    public void initTest() {
        shopOne = createEntity(em);
    }

    @Test
    @Transactional
    void createShopOne() throws Exception {
        int databaseSizeBeforeCreate = shopOneRepository.findAll().size();
        // Create the ShopOne
        restShopOneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(shopOne)))
            .andExpect(status().isCreated());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeCreate + 1);
        ShopOne testShopOne = shopOneList.get(shopOneList.size() - 1);
        assertThat(testShopOne.getShopName()).isEqualTo(DEFAULT_SHOP_NAME);
        assertThat(testShopOne.getOwner()).isEqualTo(DEFAULT_OWNER);
        assertThat(testShopOne.getCategory()).isEqualTo(DEFAULT_CATEGORY);
        assertThat(testShopOne.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    void createShopOneWithExistingId() throws Exception {
        // Create the ShopOne with an existing ID
        shopOne.setId(1L);

        int databaseSizeBeforeCreate = shopOneRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restShopOneMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(shopOne)))
            .andExpect(status().isBadRequest());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllShopOnes() throws Exception {
        // Initialize the database
        shopOneRepository.saveAndFlush(shopOne);

        // Get all the shopOneList
        restShopOneMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(shopOne.getId().intValue())))
            .andExpect(jsonPath("$.[*].shopName").value(hasItem(DEFAULT_SHOP_NAME)))
            .andExpect(jsonPath("$.[*].owner").value(hasItem(DEFAULT_OWNER)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    @Test
    @Transactional
    void getShopOne() throws Exception {
        // Initialize the database
        shopOneRepository.saveAndFlush(shopOne);

        // Get the shopOne
        restShopOneMockMvc
            .perform(get(ENTITY_API_URL_ID, shopOne.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(shopOne.getId().intValue()))
            .andExpect(jsonPath("$.shopName").value(DEFAULT_SHOP_NAME))
            .andExpect(jsonPath("$.owner").value(DEFAULT_OWNER))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));
    }

    @Test
    @Transactional
    void getNonExistingShopOne() throws Exception {
        // Get the shopOne
        restShopOneMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewShopOne() throws Exception {
        // Initialize the database
        shopOneRepository.saveAndFlush(shopOne);

        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();

        // Update the shopOne
        ShopOne updatedShopOne = shopOneRepository.findById(shopOne.getId()).get();
        // Disconnect from session so that the updates on updatedShopOne are not directly saved in db
        em.detach(updatedShopOne);
        updatedShopOne.shopName(UPDATED_SHOP_NAME).owner(UPDATED_OWNER).category(UPDATED_CATEGORY).email(UPDATED_EMAIL);

        restShopOneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedShopOne.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedShopOne))
            )
            .andExpect(status().isOk());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
        ShopOne testShopOne = shopOneList.get(shopOneList.size() - 1);
        assertThat(testShopOne.getShopName()).isEqualTo(UPDATED_SHOP_NAME);
        assertThat(testShopOne.getOwner()).isEqualTo(UPDATED_OWNER);
        assertThat(testShopOne.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testShopOne.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void putNonExistingShopOne() throws Exception {
        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();
        shopOne.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShopOneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, shopOne.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(shopOne))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchShopOne() throws Exception {
        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();
        shopOne.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOneMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(shopOne))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamShopOne() throws Exception {
        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();
        shopOne.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOneMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(shopOne)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateShopOneWithPatch() throws Exception {
        // Initialize the database
        shopOneRepository.saveAndFlush(shopOne);

        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();

        // Update the shopOne using partial update
        ShopOne partialUpdatedShopOne = new ShopOne();
        partialUpdatedShopOne.setId(shopOne.getId());

        partialUpdatedShopOne.shopName(UPDATED_SHOP_NAME).owner(UPDATED_OWNER).category(UPDATED_CATEGORY);

        restShopOneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShopOne.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedShopOne))
            )
            .andExpect(status().isOk());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
        ShopOne testShopOne = shopOneList.get(shopOneList.size() - 1);
        assertThat(testShopOne.getShopName()).isEqualTo(UPDATED_SHOP_NAME);
        assertThat(testShopOne.getOwner()).isEqualTo(UPDATED_OWNER);
        assertThat(testShopOne.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testShopOne.getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    void fullUpdateShopOneWithPatch() throws Exception {
        // Initialize the database
        shopOneRepository.saveAndFlush(shopOne);

        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();

        // Update the shopOne using partial update
        ShopOne partialUpdatedShopOne = new ShopOne();
        partialUpdatedShopOne.setId(shopOne.getId());

        partialUpdatedShopOne.shopName(UPDATED_SHOP_NAME).owner(UPDATED_OWNER).category(UPDATED_CATEGORY).email(UPDATED_EMAIL);

        restShopOneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedShopOne.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedShopOne))
            )
            .andExpect(status().isOk());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
        ShopOne testShopOne = shopOneList.get(shopOneList.size() - 1);
        assertThat(testShopOne.getShopName()).isEqualTo(UPDATED_SHOP_NAME);
        assertThat(testShopOne.getOwner()).isEqualTo(UPDATED_OWNER);
        assertThat(testShopOne.getCategory()).isEqualTo(UPDATED_CATEGORY);
        assertThat(testShopOne.getEmail()).isEqualTo(UPDATED_EMAIL);
    }

    @Test
    @Transactional
    void patchNonExistingShopOne() throws Exception {
        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();
        shopOne.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restShopOneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, shopOne.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(shopOne))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchShopOne() throws Exception {
        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();
        shopOne.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOneMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(shopOne))
            )
            .andExpect(status().isBadRequest());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamShopOne() throws Exception {
        int databaseSizeBeforeUpdate = shopOneRepository.findAll().size();
        shopOne.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restShopOneMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(shopOne)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ShopOne in the database
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteShopOne() throws Exception {
        // Initialize the database
        shopOneRepository.saveAndFlush(shopOne);

        int databaseSizeBeforeDelete = shopOneRepository.findAll().size();

        // Delete the shopOne
        restShopOneMockMvc
            .perform(delete(ENTITY_API_URL_ID, shopOne.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ShopOne> shopOneList = shopOneRepository.findAll();
        assertThat(shopOneList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
