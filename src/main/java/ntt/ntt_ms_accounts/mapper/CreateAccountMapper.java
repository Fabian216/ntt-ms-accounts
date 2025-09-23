package ntt.ntt_ms_accounts.mapper;

import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.dto.CurrentAccountRequestDto;
import ntt.ntt_ms_accounts.dto.FixedTermAccountRequestDto;
import ntt.ntt_ms_accounts.dto.SavingAccountRequestDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.entity.CurrentAccount;
import ntt.ntt_ms_accounts.entity.FixedTermAccount;
import ntt.ntt_ms_accounts.entity.SavingsAccount;
import org.springframework.stereotype.Component;
import ntt.ntt_ms_accounts.enums.AccountType;

import java.util.function.Consumer;

@Component
public class CreateAccountMapper {

    public BankAccount toEntity(BankAccountRequestDto request) {

        switch (request.getAccountType()) {
            case SAVINGS:
                SavingAccountRequestDto s = (SavingAccountRequestDto) request;
                return SavingsAccount.builder()
                        .customerId(s.getCustomerId())
                        .accountType(s.getAccountType())
                        .accountNumber(s.getAccountNumber())
                        .balance(s.getBalance())
                        .transactionLimit(s.getTransactionLimit())
                        .maintenanceFee(s.getMaintenanceFee())
                        .requiredAvgDailyBalance(s.getRequiredAvgDailyBalance())
                        .build();
            case CURRENT:
                CurrentAccountRequestDto c = (CurrentAccountRequestDto) request;
                return CurrentAccount.builder()
                        .customerId(c.getCustomerId())
                        .accountType(c.getAccountType())
                        .accountNumber(c.getAccountNumber())
                        .balance(c.getBalance())
                        .transactionLimit(c.getTransactionLimit())
                        .maintenanceFee(c.getMaintenanceFee())
                        .build();
            case FIXED_TERM:
                FixedTermAccountRequestDto f = (FixedTermAccountRequestDto) request;
                return FixedTermAccount.builder()
                        .customerId(f.getCustomerId())
                        .accountType(f.getAccountType())
                        .accountNumber(f.getAccountNumber())
                        .balance(f.getBalance())
                        .transactionLimit(f.getTransactionLimit())
                        .maintenanceFee(f.getMaintenanceFee())
                        .allowedTransactionDay(f.getAllowedTransactionDay())
                        .build();
            default:
                throw new IllegalArgumentException("Tipo de cuenta no soportado: " + request.getAccountType());
        }
    }
    private  <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public void updateEntity(BankAccount entity, BankAccountRequestDto request){
        if (entity == null) {
            throw new IllegalArgumentException("entity no puede ser null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request no puede ser null");
        }

        updateIfNotNull(entity::setBalance, request.getBalance());
        updateIfNotNull(entity::setTransactionLimit, request.getTransactionLimit());
        updateIfNotNull(entity::setMaintenanceFee, request.getMaintenanceFee());

        if (entity instanceof SavingsAccount) {
            if (request instanceof SavingAccountRequestDto) {
                SavingAccountRequestDto r = (SavingAccountRequestDto) request;
                updateIfNotNull(((SavingsAccount) entity)::setRequiredAvgDailyBalance, r.getRequiredAvgDailyBalance());
            } else if ( request.getAccountType() != null &&  request.getAccountType() != AccountType.SAVINGS) {
                throw new IllegalArgumentException("Entity SavingsAccount pero request no es SAVINGS");
            }
        } else if (entity instanceof FixedTermAccount) {
            if (request instanceof FixedTermAccountRequestDto) {
                FixedTermAccountRequestDto r = (FixedTermAccountRequestDto) request;
                updateIfNotNull(((FixedTermAccount) entity)::setAllowedTransactionDay, r.getAllowedTransactionDay());
            } else if ( request.getAccountType() != null &&  request.getAccountType() != AccountType.FIXED_TERM) {
                throw new IllegalArgumentException("Entity FixedTermAccount pero request no es FIXED_TERM");
            }
        }
    }

}
