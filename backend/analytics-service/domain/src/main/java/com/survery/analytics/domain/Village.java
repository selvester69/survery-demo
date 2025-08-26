package com.survery.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Data
@Entity
@Table(name = "villages")
public class Village {

    @Id
    @Column(name = "village_id")
    private UUID villageId;

    @Column(name = "village_name", nullable = false)
    private String villageName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panchayat_id", nullable = false)
    private Panchayat panchayat;

    @Column(name = "geo_point", nullable = false, columnDefinition = "geometry(Point,4326)")
    private Point geoPoint;
}
