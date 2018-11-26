package microservices.book.multiplication.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import microservices.book.multiplication.domain.Multiplication;

/**
* This interface allows us to save and retrieve Multiplications
*/
public interface MultiplicationRepository extends CrudRepository<Multiplication, Long>{

	Optional<Multiplication> findByFactorAAndFactorB(final int factorA, final int factorB);
}