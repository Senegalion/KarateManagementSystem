package com.karate.userservice.unit.repository;

import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.repository.AddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Test
    @DisplayName("save and find AddressEntity by id")
    void save_and_find_address_by_id() {
        // given
        var addr = AddressEntity.builder()
                .city("C").street("S").number("1").postalCode("00-000").build();

        // when
        var saved = addressRepository.save(addr);
        var found = addressRepository.findById(saved.getAddressId());

        // then
        assertThat(saved.getAddressId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getCity()).isEqualTo("C");
    }

    @Test
    @DisplayName("delete AddressEntity removes it from DB")
    void delete_address_removes_from_db() {
        // given
        var addr = addressRepository.save(AddressEntity.builder()
                .city("X").street("Y").number("2").postalCode("11-111").build());

        // when
        addressRepository.deleteById(addr.getAddressId());

        // then
        assertThat(addressRepository.findById(addr.getAddressId())).isNotPresent();
    }
}
