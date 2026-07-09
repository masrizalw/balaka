package com.artivisi.accountingfinance.service;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.JournalTemplate;
import com.artivisi.accountingfinance.entity.RecurringTransaction;
import com.artivisi.accountingfinance.entity.RecurringTransactionLog;
import com.artivisi.accountingfinance.enums.RecurringFrequency;
import com.artivisi.accountingfinance.enums.RecurringStatus;
import com.artivisi.accountingfinance.repository.RecurringTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for RecurringTransactionService.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("RecurringTransactionService Integration Tests")
class RecurringTransactionServiceTest {

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private JournalTemplateService journalTemplateService;

    // Template ID from V003
    private static final UUID INCOME_CONSULTING_TEMPLATE_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");

    private RecurringTransaction createTestRecurring(RecurringFrequency frequency) {
        JournalTemplate template = journalTemplateService.findById(INCOME_CONSULTING_TEMPLATE_ID);

        RecurringTransaction entity = new RecurringTransaction();
        entity.setName("Test Recurring " + System.currentTimeMillis());
        entity.setJournalTemplate(template);
        entity.setAmount(new BigDecimal("1000000"));
        entity.setDescription("Test recurring transaction");
        entity.setFrequency(frequency);
        entity.setStartDate(LocalDate.now());
        entity.setDayOfMonth(15);
        entity.setAutoPost(false);
        entity.setSkipWeekends(false);

        return recurringTransactionService.create(entity, null);
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTests {

        @Test
        @DisplayName("Should find by ID")
        void shouldFindById() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            RecurringTransaction found = recurringTransactionService.findById(created.getId());

            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo(created.getName());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for invalid ID")
        void shouldThrowForInvalidId() {
            UUID invalidId = UUID.randomUUID();

            assertThatThrownBy(() -> recurringTransactionService.findById(invalidId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Should find by ID with mappings")
        void shouldFindByIdWithMappings() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            RecurringTransaction found = recurringTransactionService.findByIdWithMappings(created.getId());

            assertThat(found).isNotNull();
            assertThat(found.getAccountMappings()).isNotNull();
        }

        @Test
        @DisplayName("Should throw for invalid ID with mappings")
        void shouldThrowForInvalidIdWithMappings() {
            UUID invalidId = UUID.randomUUID();

            assertThatThrownBy(() -> recurringTransactionService.findByIdWithMappings(invalidId))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("Should find all with pagination")
        void shouldFindAllWithPagination() {
            createTestRecurring(RecurringFrequency.MONTHLY);

            Page<RecurringTransaction> page = recurringTransactionService.findAll(null, PageRequest.of(0, 10));

            assertThat(page).isNotNull();
            assertThat(page.getContent()).isNotEmpty();
        }

        @Test
        @DisplayName("Should find all filtered by status")
        void shouldFindAllFilteredByStatus() {
            createTestRecurring(RecurringFrequency.MONTHLY);

            Page<RecurringTransaction> page = recurringTransactionService.findAll(
                    RecurringStatus.ACTIVE, PageRequest.of(0, 10));

            assertThat(page).isNotNull();
            assertThat(page.getContent()).allMatch(r -> r.getStatus() == RecurringStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should count by status")
        void shouldCountByStatus() {
            createTestRecurring(RecurringFrequency.MONTHLY);

            long count = recurringTransactionService.countByStatus(RecurringStatus.ACTIVE);

            assertThat(count).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Create Operations")
    class CreateTests {

        @Test
        @DisplayName("Should create recurring transaction with ACTIVE status")
        void shouldCreateWithActiveStatus() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            assertThat(created.getId()).isNotNull();
            assertThat(created.getStatus()).isEqualTo(RecurringStatus.ACTIVE);
            assertThat(created.getTotalRuns()).isZero();
            assertThat(created.getNextRunDate()).isNotNull();
        }

        @Test
        @DisplayName("Should create daily recurring transaction")
        void shouldCreateDailyRecurring() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.DAILY);

            assertThat(created.getFrequency()).isEqualTo(RecurringFrequency.DAILY);
            assertThat(created.getNextRunDate()).isNotNull();
        }

        @Test
        @DisplayName("Should create weekly recurring transaction")
        void shouldCreateWeeklyRecurring() {
            JournalTemplate template = journalTemplateService.findById(INCOME_CONSULTING_TEMPLATE_ID);

            RecurringTransaction entity = new RecurringTransaction();
            entity.setName("Weekly Recurring " + System.currentTimeMillis());
            entity.setJournalTemplate(template);
            entity.setAmount(new BigDecimal("500000"));
            entity.setDescription("Weekly test");
            entity.setFrequency(RecurringFrequency.WEEKLY);
            entity.setStartDate(LocalDate.now());
            entity.setDayOfWeek(DayOfWeek.MONDAY.getValue());
            entity.setAutoPost(false);
            entity.setSkipWeekends(false);

            RecurringTransaction created = recurringTransactionService.create(entity, null);

            assertThat(created.getFrequency()).isEqualTo(RecurringFrequency.WEEKLY);
            assertThat(created.getNextRunDate()).isNotNull();
            // Next run should be on a Monday
            assertThat(created.getNextRunDate().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }
    }

    @Nested
    @DisplayName("Pause and Resume Operations")
    class PauseResumeTests {

        @Test
        @DisplayName("Should pause active recurring transaction")
        void shouldPauseActiveRecurring() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            RecurringTransaction paused = recurringTransactionService.pause(created.getId());

            assertThat(paused.getStatus()).isEqualTo(RecurringStatus.PAUSED);
        }

        @Test
        @DisplayName("Should throw when pausing non-active recurring")
        void shouldThrowWhenPausingNonActive() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);
            recurringTransactionService.pause(created.getId());

            assertThatThrownBy(() -> recurringTransactionService.pause(created.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("aktif");
        }

        @Test
        @DisplayName("Should resume paused recurring transaction")
        void shouldResumePausedRecurring() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);
            recurringTransactionService.pause(created.getId());

            RecurringTransaction resumed = recurringTransactionService.resume(created.getId());

            assertThat(resumed.getStatus()).isEqualTo(RecurringStatus.ACTIVE);
            assertThat(resumed.getNextRunDate()).isNotNull();
        }

        @Test
        @DisplayName("Should throw when resuming non-paused recurring")
        void shouldThrowWhenResumingNonPaused() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            assertThatThrownBy(() -> recurringTransactionService.resume(created.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("dijeda");
        }
    }

    @Nested
    @DisplayName("Complete Operations")
    class CompleteTests {

        @Test
        @DisplayName("Should complete recurring transaction")
        void shouldCompleteRecurring() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            RecurringTransaction completed = recurringTransactionService.complete(created.getId());

            assertThat(completed.getStatus()).isEqualTo(RecurringStatus.COMPLETED);
            assertThat(completed.getNextRunDate()).isNull();
        }

        @Test
        @DisplayName("Should throw when completing already completed")
        void shouldThrowWhenCompletingAlreadyCompleted() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);
            recurringTransactionService.complete(created.getId());

            assertThatThrownBy(() -> recurringTransactionService.complete(created.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sudah selesai");
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteTests {

        @Test
        @DisplayName("Should soft delete recurring transaction")
        void shouldSoftDeleteRecurring() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            recurringTransactionService.delete(created.getId());

            // Soft delete - findById should throw since @SQLRestriction filters soft deleted
            assertThatThrownBy(() -> recurringTransactionService.findById(created.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update recurring transaction fields")
        void shouldUpdateFields() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            RecurringTransaction updateData = new RecurringTransaction();
            updateData.setName("Updated Name");
            updateData.setAmount(new BigDecimal("2000000"));
            updateData.setDescription("Updated description");
            updateData.setFrequency(RecurringFrequency.MONTHLY);
            updateData.setDayOfMonth(20);
            updateData.setStartDate(created.getStartDate());
            updateData.setJournalTemplate(created.getJournalTemplate());
            updateData.setAutoPost(true);
            updateData.setSkipWeekends(false);

            RecurringTransaction updated = recurringTransactionService.update(
                    created.getId(), updateData, null);

            assertThat(updated.getName()).isEqualTo("Updated Name");
            assertThat(updated.getAmount()).isEqualByComparingTo("2000000");
            assertThat(updated.isAutoPost()).isTrue();
        }

        @Test
        @DisplayName("Should throw when updating completed recurring")
        void shouldThrowWhenUpdatingCompleted() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);
            recurringTransactionService.complete(created.getId());

            RecurringTransaction updateData = new RecurringTransaction();
            updateData.setName("Should Not Update");
            updateData.setJournalTemplate(created.getJournalTemplate());

            assertThatThrownBy(() -> recurringTransactionService.update(
                    created.getId(), updateData, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("selesai");
        }
    }

    @Nested
    @DisplayName("Preview Occurrences")
    class PreviewOccurrencesTests {

        @Test
        @DisplayName("Should preview monthly occurrences")
        void shouldPreviewMonthlyOccurrences() {
            var params = new RecurringTransactionService.PreviewOccurrenceParams(
                    RecurringFrequency.MONTHLY, 15, null,
                    LocalDate.now(), false, null, null);

            List<LocalDate> dates = recurringTransactionService.previewOccurrences(params, 5);

            assertThat(dates).hasSize(5);
        }

        @Test
        @DisplayName("Should preview daily occurrences")
        void shouldPreviewDailyOccurrences() {
            var params = new RecurringTransactionService.PreviewOccurrenceParams(
                    RecurringFrequency.DAILY, null, null,
                    LocalDate.now(), false, null, null);

            List<LocalDate> dates = recurringTransactionService.previewOccurrences(params, 3);

            assertThat(dates).hasSize(3);
        }

        @Test
        @DisplayName("Should preview with end date limit")
        void shouldPreviewWithEndDateLimit() {
            var params = new RecurringTransactionService.PreviewOccurrenceParams(
                    RecurringFrequency.MONTHLY, 15, null,
                    LocalDate.now(), false,
                    LocalDate.now().plusMonths(2), // Only allow 2 months
                    null);

            List<LocalDate> dates = recurringTransactionService.previewOccurrences(params, 10);

            assertThat(dates).hasSizeLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Should preview with max occurrences limit")
        void shouldPreviewWithMaxOccurrencesLimit() {
            var params = new RecurringTransactionService.PreviewOccurrenceParams(
                    RecurringFrequency.DAILY, null, null,
                    LocalDate.now(), false, null, 3);

            List<LocalDate> dates = recurringTransactionService.previewOccurrences(params, 10);

            assertThat(dates).hasSize(3);
        }

        @Test
        @DisplayName("Should preview quarterly occurrences")
        void shouldPreviewQuarterlyOccurrences() {
            var params = new RecurringTransactionService.PreviewOccurrenceParams(
                    RecurringFrequency.QUARTERLY, 15, null,
                    LocalDate.now(), false, null, null);

            List<LocalDate> dates = recurringTransactionService.previewOccurrences(params, 4);

            assertThat(dates).isNotEmpty();
            assertThat(dates).hasSizeLessThanOrEqualTo(4);
        }

        @Test
        @DisplayName("Should preview yearly occurrences")
        void shouldPreviewYearlyOccurrences() {
            var params = new RecurringTransactionService.PreviewOccurrenceParams(
                    RecurringFrequency.YEARLY, 15, null,
                    LocalDate.now(), false, null, null);

            List<LocalDate> dates = recurringTransactionService.previewOccurrences(params, 3);

            assertThat(dates).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Calculate Next Run Date")
    class CalculateNextRunDateTests {

        @Test
        @DisplayName("Should return null for null fromDate")
        void shouldReturnNullForNullFromDate() {
            LocalDate result = RecurringTransactionService.calculateNextRunDate(
                    RecurringFrequency.MONTHLY, 15, null, null, false);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should skip weekends when enabled")
        void shouldSkipWeekendsWhenEnabled() {
            // Find a Saturday
            LocalDate saturday = LocalDate.now();
            while (!saturday.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
                saturday = saturday.plusDays(1);
            }

            LocalDate result = RecurringTransactionService.calculateNextRunDate(
                    RecurringFrequency.DAILY, null, null, saturday, true);

            assertThat(result).isNotNull();
            assertThat(result.getDayOfWeek()).isNotIn(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        }

        @Test
        @DisplayName("Should calculate weekly with specific day")
        void shouldCalculateWeeklyWithSpecificDay() {
            LocalDate result = RecurringTransactionService.calculateNextRunDate(
                    RecurringFrequency.WEEKLY, null, DayOfWeek.FRIDAY.getValue(),
                    LocalDate.now(), false);

            assertThat(result).isNotNull();
            assertThat(result.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        }

        @Test
        @DisplayName("Should calculate monthly when day already passed")
        void shouldCalculateMonthlyWhenDayPassed() {
            // Use day 1, starting from day 15 - should go to next month
            LocalDate startDate = LocalDate.of(2026, 3, 15);
            LocalDate result = RecurringTransactionService.calculateNextRunDate(
                    RecurringFrequency.MONTHLY, 1, null, startDate, false);

            assertThat(result).isNotNull();
            assertThat(result.getMonthValue()).isGreaterThan(3);
            assertThat(result.getDayOfMonth()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle quarterly frequency")
        void shouldHandleQuarterlyFrequency() {
            LocalDate result = RecurringTransactionService.calculateNextRunDate(
                    RecurringFrequency.QUARTERLY, 15, null,
                    LocalDate.of(2026, 1, 1), false);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle yearly without day of month")
        void shouldHandleYearlyWithoutDayOfMonth() {
            LocalDate startDate = LocalDate.of(2026, 6, 15);
            LocalDate result = RecurringTransactionService.calculateNextRunDate(
                    RecurringFrequency.YEARLY, null, null, startDate, false);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2027);
        }
    }

    @Nested
    @DisplayName("Find Logs")
    class FindLogsTests {

        @Test
        @DisplayName("Should find logs by recurring ID")
        void shouldFindLogsByRecurringId() {
            RecurringTransaction created = createTestRecurring(RecurringFrequency.MONTHLY);

            List<RecurringTransactionLog> logs = recurringTransactionService
                    .findLogsByRecurringId(created.getId());

            assertThat(logs).isNotNull();
        }
    }

    @Nested
    @DisplayName("Find Upcoming")
    class FindUpcomingTests {

        @Test
        @DisplayName("Should find upcoming recurring transactions")
        void shouldFindUpcoming() {
            createTestRecurring(RecurringFrequency.DAILY);

            List<RecurringTransaction> upcoming = recurringTransactionService.findUpcoming(
                    LocalDate.now(), LocalDate.now().plusDays(30));

            assertThat(upcoming).isNotNull();
        }
    }
}
