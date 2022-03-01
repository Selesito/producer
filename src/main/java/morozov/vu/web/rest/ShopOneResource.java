package morozov.vu.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import morozov.vu.domain.ShopOne;
import morozov.vu.repository.ShopOneRepository;
import morozov.vu.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link morozov.vu.domain.ShopOne}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ShopOneResource {

    private final Logger log = LoggerFactory.getLogger(ShopOneResource.class);

    private static final String ENTITY_NAME = "producerShopOne";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ShopOneRepository shopOneRepository;

    @Autowired
    private KafkaTemplate<Long, ShopOne> kafkaTemplate;

    public ShopOneResource(ShopOneRepository shopOneRepository) {
        this.shopOneRepository = shopOneRepository;
    }

    /**
     * {@code POST  /shop-ones} : Create a new shopOne.
     *
     * @param shopOne the shopOne to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new shopOne, or with status {@code 400 (Bad Request)} if the shopOne has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/shop-ones")
    public ResponseEntity<ShopOne> createShopOne(@RequestBody ShopOne shopOne) throws URISyntaxException {
        log.debug("REST request to save ShopOne : {}", shopOne);
        if (shopOne.getId() != null) {
            throw new BadRequestAlertException("A new shopOne cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ShopOne result = shopOneRepository.save(shopOne);
        kafkaTemplate.send("topic", shopOne.getId(), shopOne);
        kafkaTemplate.flush();
        return ResponseEntity
            .created(new URI("/api/shop-ones/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /shop-ones/:id} : Updates an existing shopOne.
     *
     * @param id the id of the shopOne to save.
     * @param shopOne the shopOne to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shopOne,
     * or with status {@code 400 (Bad Request)} if the shopOne is not valid,
     * or with status {@code 500 (Internal Server Error)} if the shopOne couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/shop-ones/{id}")
    public ResponseEntity<ShopOne> updateShopOne(@PathVariable(value = "id", required = false) final Long id, @RequestBody ShopOne shopOne)
        throws URISyntaxException {
        log.debug("REST request to update ShopOne : {}, {}", id, shopOne);
        if (shopOne.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shopOne.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shopOneRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ShopOne result = shopOneRepository.save(shopOne);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shopOne.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /shop-ones/:id} : Partial updates given fields of an existing shopOne, field will ignore if it is null
     *
     * @param id the id of the shopOne to save.
     * @param shopOne the shopOne to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated shopOne,
     * or with status {@code 400 (Bad Request)} if the shopOne is not valid,
     * or with status {@code 404 (Not Found)} if the shopOne is not found,
     * or with status {@code 500 (Internal Server Error)} if the shopOne couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/shop-ones/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ShopOne> partialUpdateShopOne(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ShopOne shopOne
    ) throws URISyntaxException {
        log.debug("REST request to partial update ShopOne partially : {}, {}", id, shopOne);
        if (shopOne.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, shopOne.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!shopOneRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ShopOne> result = shopOneRepository
            .findById(shopOne.getId())
            .map(existingShopOne -> {
                if (shopOne.getShopName() != null) {
                    existingShopOne.setShopName(shopOne.getShopName());
                }
                if (shopOne.getOwner() != null) {
                    existingShopOne.setOwner(shopOne.getOwner());
                }
                if (shopOne.getCategory() != null) {
                    existingShopOne.setCategory(shopOne.getCategory());
                }
                if (shopOne.getEmail() != null) {
                    existingShopOne.setEmail(shopOne.getEmail());
                }

                return existingShopOne;
            })
            .map(shopOneRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, shopOne.getId().toString())
        );
    }

    /**
     * {@code GET  /shop-ones} : get all the shopOnes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of shopOnes in body.
     */
    @GetMapping("/shop-ones")
    public List<ShopOne> getAllShopOnes() {
        log.debug("REST request to get all ShopOnes");
        return shopOneRepository.findAll();
    }

    /**
     * {@code GET  /shop-ones/:id} : get the "id" shopOne.
     *
     * @param id the id of the shopOne to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the shopOne, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/shop-ones/{id}")
    public ResponseEntity<ShopOne> getShopOne(@PathVariable Long id) {
        log.debug("REST request to get ShopOne : {}", id);
        Optional<ShopOne> shopOne = shopOneRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(shopOne);
    }

    /**
     * {@code DELETE  /shop-ones/:id} : delete the "id" shopOne.
     *
     * @param id the id of the shopOne to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/shop-ones/{id}")
    public ResponseEntity<Void> deleteShopOne(@PathVariable Long id) {
        log.debug("REST request to delete ShopOne : {}", id);
        shopOneRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
