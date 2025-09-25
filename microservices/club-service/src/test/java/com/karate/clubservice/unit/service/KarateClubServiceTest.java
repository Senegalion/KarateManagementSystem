package com.karate.clubservice.unit.service;

import com.karate.clubservice.domain.exception.ClubNotFoundException;
import com.karate.clubservice.domain.exception.InvalidClubNameException;
import com.karate.clubservice.domain.model.KarateClubEntity;
import com.karate.clubservice.domain.model.KarateClubName;
import com.karate.clubservice.domain.model.dto.KarateClubDto;
import com.karate.clubservice.domain.repository.KarateClubRepository;
import com.karate.clubservice.domain.service.KarateClubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KarateClubServiceTest {

    @Mock
    KarateClubRepository repository;

    @InjectMocks
    KarateClubService service;

    @Test
    void getByName_returnsDto_whenClubExists_andCaseInsensitive() {
        // given
        String param = "gdanski_klub_okinawa_shorin_ryu_karate";
        KarateClubEntity entity = KarateClubEntity.builder()
                .karateClubId(10L)
                .name(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE)
                .build();
        when(repository.findByName(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE))
                .thenReturn(Optional.of(entity));

        // when
        KarateClubDto dto = service.getByName(param);

        // then
        assertThat(dto.karateClubId()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE.name());
        verify(repository).findByName(KarateClubName.GDANSKI_KLUB_OKINAWA_SHORIN_RYU_KARATE);
    }

    @Test
    void getByName_throwsInvalidClubName_whenEnumNotMatched() {
        // given
        String invalid = "NOT_EXISTING_ENUM_NAME";

        // when // then
        assertThatThrownBy(() -> service.getByName(invalid))
                .isInstanceOf(InvalidClubNameException.class)
                .hasMessageContaining("Invalid club name");
        verifyNoInteractions(repository);
    }

    @Test
    void getByName_throwsClubNotFound_whenRepoReturnsEmpty() {
        // given
        String name = KarateClubName.KLUB_OKINAWA_KARATE_DO_WARSZAWA.name();
        when(repository.findByName(KarateClubName.KLUB_OKINAWA_KARATE_DO_WARSZAWA))
                .thenReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> service.getByName(name))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessageContaining("Club not found");
        verify(repository).findByName(KarateClubName.KLUB_OKINAWA_KARATE_DO_WARSZAWA);
    }

    @Test
    void getById_returnsDto_whenClubExists() {
        // given
        long id = 5L;
        KarateClubEntity entity = KarateClubEntity.builder()
                .karateClubId(id)
                .name(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE)
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        // when
        KarateClubDto dto = service.getById(id);

        // then
        assertThat(dto.karateClubId()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo(KarateClubName.WOLOMINSKI_KLUB_SHORIN_RYU_KARATE.name());
        verify(repository).findById(id);
    }

    @Test
    void getById_throwsClubNotFound_whenRepoReturnsEmpty() {
        // given
        long id = 404L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ClubNotFoundException.class)
                .hasMessageContaining("Club not found with ID: " + id);
        verify(repository).findById(id);
    }
}
