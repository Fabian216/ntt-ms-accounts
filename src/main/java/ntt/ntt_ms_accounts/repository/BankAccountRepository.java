package ntt.ntt_ms_accounts.repository;

import ntt.ntt_ms_accounts.entity.BankAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BankAccountRepository extends ReactiveMongoRepository<BankAccount, String> {
    Flux<BankAccount> findByCustomerId(String customerId);
}
