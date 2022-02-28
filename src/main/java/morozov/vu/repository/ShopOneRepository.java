package morozov.vu.repository;

import morozov.vu.domain.ShopOne;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ShopOne entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ShopOneRepository extends JpaRepository<ShopOne, Long> {}
