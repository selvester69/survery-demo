package com.survery.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Data
@Entity
@Table(name = "panchayats")
public class Panchayat {

    @Id
    @Column(name = "panchayat_id")
    private UUID panchayatId;

    @Column(name = "panchayat_name", nullable = false)
    private String panchayatName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "constituency_id", nullable = false)
    private Constituency constituency;

    @Column(name = "geo_boundary", nullable = false, columnDefinition = "geometry(Polygon,4326)")
    private Polygon geoBoundary;
}
