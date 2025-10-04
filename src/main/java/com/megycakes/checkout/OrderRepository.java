package com.megycakes.checkout;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("""
            select distinct o
                      from Order o
                      left join fetch o.items i
                      where o.orderNumber = :orderNumber
           """)
    Optional<Order> findWithItemsByOrderNumber(@Param("orderNumber") String orderNumber);
}