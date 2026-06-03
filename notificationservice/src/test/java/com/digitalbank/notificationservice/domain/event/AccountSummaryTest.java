package com.digitalbank.notificationservice.domain.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountSummary Tests")
class AccountSummaryTest {

    @Test
    @DisplayName("Should create valid AccountSummary")
    void shouldCreateValidAccountSummary() {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act
        AccountSummary account = new AccountSummary(
                accountId,
                "John Doe",
                "john@example.com"
        );

        // Assert
        assertThat(account).isNotNull();
        assertThat(account.accountId()).isEqualTo(accountId);
        assertThat(account.holderName()).isEqualTo("John Doe");
        assertThat(account.email()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should AccountSummary be a record with all components")
    void shouldAccountSummaryBeRecord() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        AccountSummary account1 = new AccountSummary(
                accountId,
                "John Doe",
                "john@example.com"
        );
        AccountSummary account2 = new AccountSummary(
                accountId,
                "John Doe",
                "john@example.com"
        );

        // Act & Assert - records should have equals and hashCode
        assertThat(account1).isEqualTo(account2);
        assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
    }

    @Test
    @DisplayName("Should distinguish different AccountSummaries")
    void shouldDistinguishDifferentAccountSummaries() {
        // Arrange
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        AccountSummary account1 = new AccountSummary(
                accountId1,
                "John Doe",
                "john@example.com"
        );
        AccountSummary account2 = new AccountSummary(
                accountId2,
                "Jane Smith",
                "jane@example.com"
        );

        // Act & Assert
        assertThat(account1).isNotEqualTo(account2);
        assertThat(account1.hashCode()).isNotEqualTo(account2.hashCode());
    }

    @Test
    @DisplayName("Should support different holder names")
    void shouldSupportDifferentHolderNames() {
        // Act
        AccountSummary account1 = new AccountSummary(
                UUID.randomUUID(),
                "John Doe",
                "john@example.com"
        );
        AccountSummary account2 = new AccountSummary(
                UUID.randomUUID(),
                "João da Silva",
                "joao@example.com"
        );
        AccountSummary account3 = new AccountSummary(
                UUID.randomUUID(),
                "José María García",
                "jose@example.com"
        );

        // Assert
        assertThat(account1.holderName()).isEqualTo("John Doe");
        assertThat(account2.holderName()).isEqualTo("João da Silva");
        assertThat(account3.holderName()).isEqualTo("José María García");
    }

    @Test
    @DisplayName("Should support different email formats")
    void shouldSupportDifferentEmailFormats() {
        // Act
        AccountSummary account1 = new AccountSummary(
                UUID.randomUUID(),
                "John Doe",
                "john@example.com"
        );
        AccountSummary account2 = new AccountSummary(
                UUID.randomUUID(),
                "Jane Smith",
                "jane.smith@company.co.uk"
        );
        AccountSummary account3 = new AccountSummary(
                UUID.randomUUID(),
                "Bob Johnson",
                "bob+tag@domain.com"
        );

        // Assert
        assertThat(account1.email()).isEqualTo("john@example.com");
        assertThat(account2.email()).isEqualTo("jane.smith@company.co.uk");
        assertThat(account3.email()).isEqualTo("bob+tag@domain.com");
    }

    @Test
    @DisplayName("Should handle spaces in holder names")
    void shouldHandleSpacesInHolderNames() {
        // Act
        AccountSummary account = new AccountSummary(
                UUID.randomUUID(),
                "John Michael Doe Smith",
                "john@example.com"
        );

        // Assert
        assertThat(account.holderName()).isEqualTo("John Michael Doe Smith");
    }

    @Test
    @DisplayName("Should AccountSummary have toString method")
    void shouldAccountSummaryHaveToString() {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act
        AccountSummary account = new AccountSummary(
                accountId,
                "John Doe",
                "john@example.com"
        );

        String toString = account.toString();

        // Assert
        assertThat(toString).contains(accountId.toString())
                .contains("John Doe")
                .contains("john@example.com");
    }

    @Test
    @DisplayName("Should support account1 equals account2 when same data")
    void shouldAccountSummaryEqualityWork() {
        // Arrange
        UUID sameId = UUID.randomUUID();
        AccountSummary account1 = new AccountSummary(sameId, "Test", "test@example.com");
        AccountSummary account2 = new AccountSummary(sameId, "Test", "test@example.com");

        // Act & Assert
        assertThat(account1).isEqualTo(account2);
    }

    @Test
    @DisplayName("Should not equal when account IDs differ")
    void shouldNotEqualWhenAccountIdsDiffer() {
        // Arrange
        AccountSummary account1 = new AccountSummary(
                UUID.randomUUID(),
                "Test",
                "test@example.com"
        );
        AccountSummary account2 = new AccountSummary(
                UUID.randomUUID(),
                "Test",
                "test@example.com"
        );

        // Act & Assert
        assertThat(account1).isNotEqualTo(account2);
    }

    @Test
    @DisplayName("Should not equal when holder names differ")
    void shouldNotEqualWhenHolderNamesDiffer() {
        // Arrange
        UUID sameId = UUID.randomUUID();
        AccountSummary account1 = new AccountSummary(sameId, "John", "test@example.com");
        AccountSummary account2 = new AccountSummary(sameId, "Jane", "test@example.com");

        // Act & Assert
        assertThat(account1).isNotEqualTo(account2);
    }

    @Test
    @DisplayName("Should not equal when emails differ")
    void shouldNotEqualWhenEmailsDiffer() {
        // Arrange
        UUID sameId = UUID.randomUUID();
        AccountSummary account1 = new AccountSummary(sameId, "Test", "test1@example.com");
        AccountSummary account2 = new AccountSummary(sameId, "Test", "test2@example.com");

        // Act & Assert
        assertThat(account1).isNotEqualTo(account2);
    }
}

