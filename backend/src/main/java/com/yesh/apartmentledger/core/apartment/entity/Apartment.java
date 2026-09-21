
package com.yesh.apartmentledger.core.apartment.entity;
import com.yesh.apartmentledger.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apartment", schema = "core")
@Getter
@Setter
@NoArgsConstructor
public class Apartment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "apartment_id")
    private Long id;

    @Column(name = "apartment_code", nullable = false, unique = true, length = 20)
    private String apartmentCode;

    @Column(name = "apartment_name", nullable = false, length = 150)
    private String apartmentName;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "account_number", length = 30)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "go_live_year")
    private Short goLiveYear;

    @Column(name = "go_live_month")
    private Short goLiveMonth;
}