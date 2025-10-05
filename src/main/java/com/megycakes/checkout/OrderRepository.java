package com.megycakes.checkout;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("""
            select distinct o
                      from Order o
                      left join fetch o.items i
                      where o.orderNumber = :orderNumber
           """)
    Optional<Order> findWithItemsByOrderNumber(@Param("orderNumber") String orderNumber);

    Page<Order> findAll(Pageable pageable);

    Page<Order> findByOrderNumberContainingIgnoreCaseOrCustomerEmailContainingIgnoreCase(
            String orderNumberTerm, String emailTerm, Pageable pageable);

    @Query("""
        select o from Order o
        left join fetch o.items
        where o.id = :id
        """)
    java.util.Optional<Order> findWithItemsById(Long id);
}