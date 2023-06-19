package com.override.unittests;

import com.override.unittests.enums.ClientType;
import com.override.unittests.exception.CannotBePayedException;
import com.override.unittests.exception.CentralBankNotRespondingException;
import com.override.unittests.service.CentralBankService;
import com.override.unittests.service.CreditCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditCalculatorTest {

    @InjectMocks
    private CreditCalculator creditCalculator;

    @Mock
    private CentralBankService centralBankService;

    /**
     * Метод тест по переплате по кредиту с использованием ParameterizedTest
     * Метод тестирует сразу все типы клиентов
     */
    @ParameterizedTest
    @EnumSource(ClientType.class)
    public void calculateOverpaymentAllTypeClientTest(ClientType clientType) {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 100000d;
        double monthPaymentAmount = 10000d;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, clientType);
        if (clientType == ClientType.GOVERMENT) {
            Assertions.assertEquals(10000d, result);
        }
        if (clientType == ClientType.BUSINESS) {
            Assertions.assertEquals(11000d, result);
        }
        if (clientType == ClientType.INDIVIDUAL) {
            Assertions.assertEquals(12000d, result);
        }
    }

    @Test
    public void calculateOverpaymentGovermentTest() {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 100000d;
        double monthPaymentAmount = 10000d;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.GOVERMENT);
        Assertions.assertEquals(10000d, result);
    }

    @Test
    public void calculateOverpaymentBusinessTest() {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 100000d;
        double monthPaymentAmount = 10000d;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.BUSINESS);
        Assertions.assertEquals(11000d, result);
    }

    @Test
    public void calculateOverpaymentIndividualTest() {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 100000d;
        double monthPaymentAmount = 10000d;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.INDIVIDUAL);
        Assertions.assertEquals(12000d, result);
    }

    @Test
    public void calculateOverpaymentOnTooBigAmountTest() {
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 1000000000d;
        double monthPaymentAmount = 10000d;
        assertThrows(CannotBePayedException.class, () -> creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.GOVERMENT));
    }

    @Test
    public void calculateOverpaymentOnManyYearCreditTest() {
        //TODO тест для случая, когда кредит все таки можно выплатить, но проценты будут начисляться много лет
        when(centralBankService.getKeyRate()).thenReturn(10d);
        double amount = 100000d;
        double monthPaymentAmount = 917d;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.BUSINESS);
        int years = (int) ((result + amount) / monthPaymentAmount) / 12;
        Assertions.assertEquals(75, years);
    }

    @Test
    public void calculateOverpaymentWhenNoConnectionTest() {
        when(centralBankService.getKeyRate()).thenThrow(CentralBankNotRespondingException.class);
        when(centralBankService.getDefaultCreditRate()).thenReturn(30d);
        double amount = 100000d;
        double monthPaymentAmount = 10000d;
        double result = creditCalculator.calculateOverpayment(amount, monthPaymentAmount, ClientType.INDIVIDUAL);
        Assertions.assertEquals(33000d, result);
    }
}


