package microservices.book.multiplication.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import microservices.book.multiplication.domain.Multiplication;
import microservices.book.multiplication.domain.MultiplicationResultAttempt;
import microservices.book.multiplication.domain.User;
import microservices.book.multiplication.event.EventDispatcher;
import microservices.book.multiplication.event.MultiplicationSolvedEvent;
import microservices.book.multiplication.repository.MultiplicationRepository;
import microservices.book.multiplication.repository.MultiplicationResultAttemptRepository;
import microservices.book.multiplication.repository.UserRepository;

@Service
public class MultiplicationServiceImpl implements MultiplicationService {

	private RandomGeneratorService randomGeneratorService;
	private MultiplicationResultAttemptRepository attemptRepository;
	private UserRepository userRepository;
	private MultiplicationRepository multiplicationRepository;
	private EventDispatcher eventDispatcher;
	
	@Autowired
	public MultiplicationServiceImpl(final RandomGeneratorService randomGeneratorService,
			final MultiplicationResultAttemptRepository attemptRepository,
			final UserRepository userRepository,
			final MultiplicationRepository multiplicationRepository,
			final EventDispatcher eventDispatcher) {
				this.randomGeneratorService = randomGeneratorService;
				this.attemptRepository = attemptRepository;
				this.userRepository = userRepository;
				this.multiplicationRepository = multiplicationRepository;
				this.eventDispatcher = eventDispatcher;
	}
	
	@Override
	public Multiplication createRandomMultiplication() {
		int factorA = randomGeneratorService.generateRandomFactor();
		int factorB = randomGeneratorService.generateRandomFactor();
		
		return new Multiplication(factorA, factorB);
	}

	@Transactional
	@Override
	public boolean checkAttempt(MultiplicationResultAttempt resultAttempt) {
		
		// Check if the user already exists for that alias
		Optional<User> user = userRepository.findByAlias(resultAttempt.getUser().getAlias());
		
		// Check if the user already exists for that alias
		Multiplication multiplication = resultAttempt.getMultiplication();
		// Check if the multiplication already exists by factor A and factor B
		Optional<Multiplication> verifiedMultiplication = multiplicationRepository.findByFactorAAndFactorB(multiplication.getFactorA(), multiplication.getFactorB());
		
		// Avoids 'hack' attempts
		Assert.isTrue(!resultAttempt.isCorrect(), "You can't send an attempt marked as correct!!");		
		
		// Checks if it's correct
		boolean correct = resultAttempt.getResultAttempt() ==
				resultAttempt.getMultiplication().getFactorA() *
				resultAttempt.getMultiplication().getFactorB();
		
		// Creates a copy, now setting the 'correct' field accordingly
		MultiplicationResultAttempt checkedAttempt =
				new MultiplicationResultAttempt(user.orElse(resultAttempt.getUser()),
						verifiedMultiplication.orElse( resultAttempt.getMultiplication()),
						resultAttempt.getResultAttempt(),
						correct);
		
		// Stores the attempt
		attemptRepository.save(checkedAttempt);
		
		// Communicates the result via Event
		//eventDispatcher.send(
				//new MultiplicationSolvedEvent(checkedAttempt.getId(),
											  //checkedAttempt.getUser().getId(),
											  //checkedAttempt.isCorrect()));
		
		// Returns the result		
		return correct;
	}
	
	@Override
	public List<MultiplicationResultAttempt> getStatsForUser(String userAlias) {
		return attemptRepository.findTop5ByUserAliasOrderByIdDesc(userAlias);
	}

	@Override
	public MultiplicationResultAttempt getResultById(Long attemptId) {		
		Optional<MultiplicationResultAttempt> attempt = attemptRepository.findById(attemptId);
		return attempt.orElse(null);
	}
}